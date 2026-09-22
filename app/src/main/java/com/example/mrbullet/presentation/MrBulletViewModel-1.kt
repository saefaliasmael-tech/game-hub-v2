package com.example.mrbullet.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mrbullet.core.engine.MrBulletPhysicsEngine
import com.example.mrbullet.core.level.MrBulletLevelManager
import com.example.mrbullet.core.model.ActiveBullet
import com.example.mrbullet.core.model.BulletGamePhase
import com.example.mrbullet.core.model.MrBulletLevelConfig
import com.example.mrbullet.core.model.MrBulletState
import com.example.mrbullet.core.repository.MrBulletRepository
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

class MrBulletViewModel(
    private val repository: MrBulletRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(MrBulletState())
    val gameState: StateFlow<MrBulletState> = _gameState.asStateFlow()

    val soundEnabledFlow = repository.soundEnabledFlow
    val hapticEnabledFlow = repository.hapticEnabledFlow
    val progressFlow = repository.progressFlow
    val completedLevelsFlow = repository.completedLevelsFlow

    private var currentConfig: MrBulletLevelConfig = MrBulletLevelManager.getLevel(1)
    private var bulletLoopJob: Job? = null

    fun stopSimulation() {
        bulletLoopJob?.cancel()
    }

    fun startLevel(levelNumber: Int) {
        stopSimulation()
        val config = MrBulletLevelManager.getLevel(levelNumber)
        currentConfig = config

        _gameState.value = MrBulletState(
            levelNumber = levelNumber,
            phase = BulletGamePhase.AIMING,
            bulletsRemaining = config.maxBullets,
            maxBullets = config.maxBullets,
            aimAngleRad = 0f,
            isAiming = false,
            trajectoryPoints = emptyList(),
            activeBullets = emptyList(),
            enemies = config.enemies.map { it.copy() },
            barrels = config.barrels.map { it.copy() },
            stars = 0,
            score = 0
        )
    }

    fun onAim(touchX: Float, touchY: Float) {
        val state = _gameState.value
        if (state.phase != BulletGamePhase.AIMING) return

        val hero = currentConfig.hero
        val angle = atan2(touchY - (hero.y - 45f), touchX - hero.x)

        val trajectory = MrBulletPhysicsEngine.calculateTrajectory(
            startX = hero.x,
            startY = hero.y - 45f,
            angleRad = angle,
            walls = currentConfig.walls
        )

        _gameState.value = state.copy(
            isAiming = true,
            aimAngleRad = angle,
            trajectoryPoints = trajectory
        )
    }

    fun onReleaseAim() {
        val state = _gameState.value
        if (state.phase != BulletGamePhase.AIMING || !state.isAiming) return
        if (state.bulletsRemaining <= 0) return

        val hero = currentConfig.hero
        val angle = state.aimAngleRad
        val speed = 16f

        val bullet = ActiveBullet(
            x = hero.x,
            y = hero.y - 45f,
            vx = cos(angle) * speed,
            vy = sin(angle) * speed
        )

        _gameState.value = state.copy(
            phase = BulletGamePhase.BULLET_FLYING,
            isAiming = false,
            trajectoryPoints = emptyList(),
            bulletsRemaining = state.bulletsRemaining - 1,
            activeBullets = state.activeBullets + bullet
        )

        if (soundEnabledFlow.value) soundManager.play(GameSound.KNIFE_THROW)
        if (hapticEnabledFlow.value) hapticManager.tap()

        startBulletSimulation()
    }

    private fun startBulletSimulation() {
        bulletLoopJob?.cancel()
        bulletLoopJob = viewModelScope.launch {
            while (isActive) {
                val state = _gameState.value
                val bullets = state.activeBullets.filter { it.isAlive }

                if (bullets.isEmpty()) {
                    // Check if won or lost
                    val allDead = state.enemies.none { it.isAlive }
                    if (allDead) {
                        handleVictory(state)
                    } else if (state.bulletsRemaining <= 0) {
                        handleDefeat(state)
                    } else {
                        // More bullets left, back to aiming
                        _gameState.value = state.copy(
                            phase = BulletGamePhase.AIMING,
                            activeBullets = emptyList()
                        )
                    }
                    break
                }

                for (bullet in bullets) {
                    MrBulletPhysicsEngine.updateBullet(
                        bullet = bullet,
                        walls = currentConfig.walls,
                        enemies = state.enemies,
                        barrels = state.barrels,
                        onEnemyKilled = { enemy ->
                            if (soundEnabledFlow.value) soundManager.play(GameSound.HIT)
                            if (hapticEnabledFlow.value) hapticManager.success()
                        },
                        onTntExploded = { barrel ->
                            if (soundEnabledFlow.value) soundManager.play(GameSound.KNIFE_HIT)
                            if (hapticEnabledFlow.value) hapticManager.strong()
                        }
                    )
                }

                // Check mid-flight win (all enemies defeated)
                if (state.enemies.none { it.isAlive }) {
                    delay(200)
                    handleVictory(state)
                    break
                }

                _gameState.value = state.copy(
                    activeBullets = bullets.filter { it.isAlive },
                    enemies = state.enemies.toList(),
                    barrels = state.barrels.toList()
                )

                delay(16)
            }
        }
    }

    private fun handleVictory(state: MrBulletState) {
        val bulletsUsed = currentConfig.maxBullets - state.bulletsRemaining
        val stars = when {
            bulletsUsed <= 1 -> 3
            bulletsUsed == 2 -> 2
            else -> 1
        }

        _gameState.value = state.copy(
            phase = BulletGamePhase.WON,
            stars = stars,
            activeBullets = emptyList()
        )

        if (soundEnabledFlow.value) soundManager.play(GameSound.WIN)
        if (hapticEnabledFlow.value) hapticManager.success()

        viewModelScope.launch {
            repository.saveLevelCompletion(state.levelNumber, state.bulletsRemaining, stars)
        }
    }

    private fun handleDefeat(state: MrBulletState) {
        _gameState.value = state.copy(
            phase = BulletGamePhase.LOST,
            activeBullets = emptyList()
        )

        if (soundEnabledFlow.value) soundManager.play(GameSound.GAME_OVER)
        if (hapticEnabledFlow.value) hapticManager.error()
    }

    fun restartLevel() {
        startLevel(_gameState.value.levelNumber)
    }

    fun nextLevel() {
        val next = (_gameState.value.levelNumber + 1).coerceAtMost(MrBulletLevelManager.TOTAL_LEVELS)
        startLevel(next)
    }

    fun toggleSound() {
        val current = soundEnabledFlow.value
        repository.setSoundEnabled(!current)
    }

    fun toggleHaptic() {
        val current = hapticEnabledFlow.value
        repository.setHapticEnabled(!current)
    }

    class Factory(
        private val repository: MrBulletRepository,
        private val soundManager: SoundManager,
        private val hapticManager: HapticManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MrBulletViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
