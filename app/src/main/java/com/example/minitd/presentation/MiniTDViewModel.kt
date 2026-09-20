package com.example.minitd.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minitd.core.engine.MiniTDEngine
import com.example.minitd.core.level.MiniTDLevelManager
import com.example.minitd.core.model.*
import com.example.minitd.core.repository.MiniTDRepository
import com.example.watersort.core.audio.GameSound
import com.example.watersort.core.audio.HapticManager
import com.example.watersort.core.audio.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MiniTDViewModel(
    val repository: MiniTDRepository,
    val soundManager: SoundManager,
    val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(TDGameState())
    val gameState: StateFlow<TDGameState> = _gameState.asStateFlow()

    private var activeConfig: TDLevelConfig = MiniTDLevelManager.getLevel(1)
    private var gameLoopJob: Job? = null

    // Wave spawning state
    private var currentWaveSpawns = mutableListOf<EnemySpawn>()
    private var waveTimerSec = 0f
    private var nextSpawnIndex = 0

    val soundEnabledFlow = repository.soundEnabledFlow
    val hapticEnabledFlow = repository.hapticEnabledFlow
    val highestLevelFlow = repository.highestLevelFlow
    val progressFlow = repository.progressFlow
    val completedLevelsFlow = repository.completedLevelsFlow

    init {
        startLevel(1)
    }

    fun startLevel(levelNumber: Int) {
        gameLoopJob?.cancel()
        val config = MiniTDLevelManager.getLevel(levelNumber)
        activeConfig = config

        _gameState.value = TDGameState(
            levelNumber = levelNumber,
            waveIndex = 0,
            totalWaves = config.waves.size,
            gold = config.startingGold,
            lives = config.startingLives,
            maxLives = config.startingLives,
            towers = emptyList(),
            enemies = emptyList(),
            projectiles = emptyList(),
            slots = config.slots,
            selectedSlot = null,
            selectedTower = null,
            isGameOver = false,
            isVictory = false,
            isWaveInProgress = false,
            isPaused = false,
            gameSpeedMultiplier = 1f
        )

        startGameLoop()
    }

    fun startNextWave() {
        val current = _gameState.value
        if (current.isGameOver || current.isVictory || current.isWaveInProgress) return
        if (current.waveIndex >= current.totalWaves) return

        val nextWave = activeConfig.waves[current.waveIndex]
        currentWaveSpawns = nextWave.spawns.toMutableList()
        waveTimerSec = 0f
        nextSpawnIndex = 0

        _gameState.value = current.copy(
            waveIndex = current.waveIndex + 1,
            isWaveInProgress = true
        )

        if (soundEnabledFlow.value) soundManager.play(GameSound.BUTTON)
        if (hapticEnabledFlow.value) hapticManager.tap()
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTimeNanos = System.nanoTime()

            while (isActive) {
                delay(16) // ~60fps
                val nowNanos = System.nanoTime()
                val rawDeltaSec = (nowNanos - lastTimeNanos) / 1_000_000_000f
                lastTimeNanos = nowNanos

                val current = _gameState.value
                if (current.isPaused || current.isGameOver || current.isVictory) continue

                val deltaSec = rawDeltaSec * current.gameSpeedMultiplier

                updateGame(deltaSec)
            }
        }
    }

    private fun updateGame(deltaSec: Float) {
        val current = _gameState.value
        var gold = current.gold
        var lives = current.lives
        var isGameOver = false
        var isVictory = false

        val updatedEnemies = current.enemies.map { it.copy() }.toMutableList()
        val updatedProjectiles = current.projectiles.map { it.copy() }.toMutableList()
        val updatedTowers = current.towers.map { it.copy() }.toMutableList()

        // 1. Spawn Wave Enemies
        if (current.isWaveInProgress && nextSpawnIndex < currentWaveSpawns.size) {
            waveTimerSec += deltaSec
            while (nextSpawnIndex < currentWaveSpawns.size &&
                waveTimerSec >= currentWaveSpawns[nextSpawnIndex].delaySec
            ) {
                val spawn = currentWaveSpawns[nextSpawnIndex]
                val firstWp = activeConfig.waypoints.first()
                val newEnemy = Enemy(
                    id = (System.nanoTime() + nextSpawnIndex).toInt(),
                    type = spawn.type,
                    maxHp = spawn.type.baseHp * (1f + (current.levelNumber - 1) * 0.15f),
                    currentHp = spawn.type.baseHp * (1f + (current.levelNumber - 1) * 0.15f),
                    speed = spawn.type.baseSpeed,
                    waypointIndex = 1,
                    xPercent = firstWp.xPercent,
                    yPercent = firstWp.yPercent
                )
                updatedEnemies.add(newEnemy)
                nextSpawnIndex++
            }
        }

        // 2. Move Enemies
        val survivingEnemies = mutableListOf<Enemy>()
        for (enemy in updatedEnemies) {
            if (enemy.isDead) continue

            val reachedEnd = MiniTDEngine.moveEnemy(enemy, activeConfig.waypoints, deltaSec)
            if (reachedEnd) {
                lives--
                if (soundEnabledFlow.value) soundManager.play(GameSound.INVALID)
                if (hapticEnabledFlow.value) hapticManager.error()
                if (lives <= 0) {
                    isGameOver = true
                }
            } else {
                survivingEnemies.add(enemy)
            }
        }

        // 3. Towers Target and Shoot
        for (i in updatedTowers.indices) {
            val tower = updatedTowers[i]
            val newShotTimer = tower.timeSinceLastShotSec + deltaSec

            if (newShotTimer >= tower.attackIntervalSec) {
                val target = MiniTDEngine.findTarget(tower, survivingEnemies)
                if (target != null) {
                    // Fire projectile
                    val proj = Projectile(
                        id = System.nanoTime(),
                        startX = tower.xPercent,
                        startY = tower.yPercent,
                        currentX = tower.xPercent,
                        currentY = tower.yPercent,
                        targetEnemyId = target.id,
                        targetX = target.xPercent,
                        targetY = target.yPercent,
                        speed = 0.8f,
                        damage = tower.damage,
                        towerType = tower.type
                    )
                    updatedProjectiles.add(proj)
                    updatedTowers[i] = tower.copy(timeSinceLastShotSec = 0f)
                } else {
                    updatedTowers[i] = tower.copy(timeSinceLastShotSec = newShotTimer)
                }
            } else {
                updatedTowers[i] = tower.copy(timeSinceLastShotSec = newShotTimer)
            }
        }

        // 4. Update Projectiles & Hits
        val activeProjectiles = mutableListOf<Projectile>()
        for (proj in updatedProjectiles) {
            val target = survivingEnemies.find { it.id == proj.targetEnemyId && !it.isDead }
            val tx = target?.xPercent ?: proj.targetX
            val ty = target?.yPercent ?: proj.targetY

            val dist = MiniTDEngine.distance(proj.currentX, proj.currentY, tx, ty)
            val moveStep = proj.speed * deltaSec

            if (dist <= moveStep || dist < 0.03f) {
                // Hit!
                if (target != null) {
                    target.currentHp -= proj.damage

                    if (proj.towerType.slowsEnemy) {
                        target.slowTimerSec = 1.5f
                    }

                    if (proj.towerType.splashRadius > 0f) {
                        for (other in survivingEnemies) {
                            if (other.id != target.id && !other.isDead) {
                                val splashDist = MiniTDEngine.distance(tx, ty, other.xPercent, other.yPercent)
                                if (splashDist <= proj.towerType.splashRadius) {
                                    other.currentHp -= proj.damage * 0.5f
                                    if (other.currentHp <= 0f && !other.isDead) {
                                        other.isDead = true
                                        gold += other.type.goldReward
                                    }
                                }
                            }
                        }
                    }

                    if (target.currentHp <= 0f && !target.isDead) {
                        target.isDead = true
                        gold += target.type.goldReward
                        if (soundEnabledFlow.value) soundManager.play(GameSound.HIT)
                        if (hapticEnabledFlow.value) hapticManager.light()
                    }
                }
            } else {
                val angle = atan2(ty - proj.currentY, tx - proj.currentX)
                proj.currentX += cos(angle) * moveStep
                proj.currentY += sin(angle) * moveStep
                activeProjectiles.add(proj)
            }
        }

        // 5. Check Wave & Level Completion
        var waveInProgress = current.isWaveInProgress
        val aliveEnemies = survivingEnemies.filter { !it.isDead }

        if (current.isWaveInProgress && nextSpawnIndex >= currentWaveSpawns.size && aliveEnemies.isEmpty()) {
            waveInProgress = false
            repository.incrementWavesCleared()

            if (current.waveIndex >= current.totalWaves) {
                isVictory = true
                if (soundEnabledFlow.value) soundManager.play(GameSound.WIN)
                if (hapticEnabledFlow.value) hapticManager.success()
                viewModelScope.launch {
                    repository.saveLevelCompletion(current.levelNumber)
                }
            }
        }

        _gameState.value = current.copy(
            gold = gold,
            lives = lives.coerceAtLeast(0),
            isGameOver = isGameOver,
            isVictory = isVictory,
            isWaveInProgress = waveInProgress,
            enemies = aliveEnemies,
            projectiles = activeProjectiles,
            towers = updatedTowers
        )
    }

    fun selectSlot(slot: TowerSlot?) {
        _gameState.value = _gameState.value.copy(
            selectedSlot = slot,
            selectedTower = null
        )
    }

    fun selectTower(tower: Tower?) {
        _gameState.value = _gameState.value.copy(
            selectedTower = tower,
            selectedSlot = null
        )
    }

    fun buildTower(type: TowerType) {
        val current = _gameState.value
        val slot = current.selectedSlot ?: return
        if (current.gold < type.baseCost) return

        val newTower = Tower(
            id = (System.currentTimeMillis() % 100000).toInt(),
            slotId = slot.id,
            type = type,
            level = 1,
            xPercent = slot.xPercent,
            yPercent = slot.yPercent,
            damage = type.baseDamage,
            range = type.baseRange,
            attackIntervalSec = type.attackIntervalSec
        )

        val updatedSlots = current.slots.map {
            if (it.id == slot.id) it.copy(isOccupied = true) else it
        }

        _gameState.value = current.copy(
            gold = current.gold - type.baseCost,
            towers = current.towers + newTower,
            slots = updatedSlots,
            selectedSlot = null
        )

        if (soundEnabledFlow.value) soundManager.play(GameSound.SELECT)
        if (hapticEnabledFlow.value) hapticManager.tap()
    }

    fun upgradeTower() {
        val current = _gameState.value
        val tower = current.selectedTower ?: return
        if (tower.level >= 3) return
        if (current.gold < tower.upgradeCost) return

        val updatedTower = tower.copy(
            level = tower.level + 1,
            damage = tower.damage * 1.5f,
            range = tower.range * 1.15f,
            attackIntervalSec = tower.attackIntervalSec * 0.85f
        )

        val updatedTowers = current.towers.map {
            if (it.id == tower.id) updatedTower else it
        }

        _gameState.value = current.copy(
            gold = current.gold - tower.upgradeCost,
            towers = updatedTowers,
            selectedTower = updatedTower
        )

        if (soundEnabledFlow.value) soundManager.play(GameSound.ACHIEVEMENT)
        if (hapticEnabledFlow.value) hapticManager.medium()
    }

    fun toggleGameSpeed() {
        val current = _gameState.value
        val newSpeed = if (current.gameSpeedMultiplier == 1f) 2f else 1f
        _gameState.value = current.copy(gameSpeedMultiplier = newSpeed)
    }

    fun restartLevel() {
        startLevel(_gameState.value.levelNumber)
    }

    fun nextLevel() {
        startLevel(_gameState.value.levelNumber + 1)
    }

    fun togglePause() {
        val current = _gameState.value
        _gameState.value = current.copy(isPaused = !current.isPaused)
    }

    fun toggleSound() {
        repository.setSoundEnabled(!soundEnabledFlow.value)
    }

    fun toggleHaptic() {
        repository.setHapticEnabled(!hapticEnabledFlow.value)
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }

    class Factory(
        private val repository: MiniTDRepository,
        private val soundManager: SoundManager,
        private val hapticManager: HapticManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MiniTDViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
