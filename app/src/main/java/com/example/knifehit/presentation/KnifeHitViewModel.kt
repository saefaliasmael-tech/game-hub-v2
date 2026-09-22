package com.example.knifehit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.knifehit.core.engine.KnifeHitEngine
import com.example.knifehit.core.level.KnifeHitLevelManager
import com.example.knifehit.core.model.*
import com.example.knifehit.core.repository.KnifeHitRepository
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
import kotlin.math.sin

class KnifeHitViewModel(
    private val repository: KnifeHitRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(KnifeHitGameState())
    val gameState: StateFlow<KnifeHitGameState> = _gameState.asStateFlow()

    val progressFlow = repository.progressFlow
    val totalStarsFlow = repository.totalStarsFlow
    val completedLevelsFlow = repository.completedLevelsCountFlow
    val soundEnabledFlow = repository.soundEnabledFlow
    val hapticEnabledFlow = repository.hapticEnabledFlow
    val applesFlow = repository.applesFlow
    val equippedSkinFlow = repository.equippedSkinFlow
    val unlockedSkinsFlow = repository.unlockedSkinsFlow

    private var gameLoopJob: Job? = null
    private var lastTimeNanos = 0L
    private var totalElapsedTime = 0f

    init {
        viewModelScope.launch {
            repository.initializeFirstLevel()
        }
    }

    fun startCampaignLevel(levelNumber: Int) {
        val config = KnifeHitLevelManager.getLevel(levelNumber)
        setupStage(
            mode = KnifeHitGameMode.CAMPAIGN,
            levelNumber = levelNumber,
            config = config
        )
    }

    fun startBossRushStage(stage: Int) {
        val config = KnifeHitLevelManager.generateBossRushLevel(stage)
        setupStage(
            mode = KnifeHitGameMode.BOSS_RUSH,
            levelNumber = stage,
            config = config
        )
    }

    private fun setupStage(
        mode: KnifeHitGameMode,
        levelNumber: Int,
        config: KnifeHitLevelConfig
    ) {
        val initialAttachedKnives = config.initialKnives.map { AttachedKnife(it) }
        val attachedApples = config.apples.map { AttachedApple(it) }

        _gameState.value = KnifeHitGameState(
            mode = mode,
            levelNumber = levelNumber,
            targetType = config.targetType,
            targetAngle = 0f,
            rotationSpeed = config.rotationSpeed,
            knivesRemaining = config.knivesRequired,
            totalKnivesThisStage = config.knivesRequired,
            attachedKnives = initialAttachedKnives,
            attachedApples = attachedApples,
            flyingKnives = emptyList(),
            targetShards = emptyList(),
            isGameOver = false,
            isStageWon = false,
            stageShatterAnimation = false
        )

        startGameLoop(config)
    }

    private fun startGameLoop(config: KnifeHitLevelConfig) {
        gameLoopJob?.cancel()
        lastTimeNanos = System.nanoTime()
        totalElapsedTime = 0f

        gameLoopJob = viewModelScope.launch {
            while (isActive) {
                val now = System.nanoTime()
                val dt = ((now - lastTimeNanos) / 1_000_000_000f).coerceIn(0.008f, 0.033f)
                lastTimeNanos = now
                totalElapsedTime += dt

                updateGame(dt, config)
                delay(16L) // ~60fps
            }
        }
    }

    private fun updateGame(dt: Float, config: KnifeHitLevelConfig) {
        val current = _gameState.value
        if (current.isGameOver || current.isStageWon) return

        // Target rotation with optional oscillation
        val currentSpeed = if (config.hasOscillation) {
            config.rotationSpeed * (1f + 0.8f * sin(totalElapsedTime * config.oscillationFrequency))
        } else {
            config.rotationSpeed
        }
        val newTargetAngle = (current.targetAngle + currentSpeed * dt) % 360f

        // Decay recoil
        val newRecoil = (current.targetRecoilY * 0.85f)

        // Update flying knives
        val targetY = 250f // Center Y in relative coordinate space
        val hitDistance = targetY + KnifeHitEngine.TARGET_RADIUS

        var attachedKnives = current.attachedKnives
        var apples = current.attachedApples
        var knivesRemaining = current.knivesRemaining
        var combo = current.currentCombo
        var stageWon = false
        var gameOver = false
        var shards = current.targetShards
        val activeFlying = mutableListOf<FlyingKnife>()

        for (fk in current.flyingKnives) {
            fk.y += fk.velocityY * dt
            if (fk.y <= hitDistance) {
                // Knife reached target!
                val impactAngle = KnifeHitEngine.calculateImpactAngle(newTargetAngle)
                val collided = KnifeHitEngine.checkKnifeCollision(impactAngle, attachedKnives)

                if (collided) {
                    gameOver = true
                    soundManager.play(GameSound.INVALID)
                    hapticManager.strong()
                    repository.recordGameSession(false, combo)
                } else {
                    // Safe Hit!
                    attachedKnives = attachedKnives + AttachedKnife(impactAngle)
                    knivesRemaining--
                    combo++
                    soundManager.play(GameSound.KNIFE_HIT)
                    hapticManager.medium()

                    // Check apple hit
                    val hitApple = KnifeHitEngine.checkAppleHit(impactAngle, apples)
                    if (hitApple != null) {
                        hitApple.isSliced = true
                        repository.addApples(2)
                        soundManager.play(GameSound.APPLE)
                        hapticManager.light()
                    }

                    // Check if stage cleared
                    if (knivesRemaining == 0) {
                        stageWon = true
                        shards = KnifeHitEngine.generateTargetShards(0f, targetY, current.targetType)
                        if (current.targetType.isBoss) {
                            soundManager.play(GameSound.BOSS)
                            repository.recordGameSession(true, combo)
                        } else {
                            soundManager.play(GameSound.LEVEL_COMPLETE)
                            repository.recordGameSession(false, combo)
                        }
                        hapticManager.success()

                        if (current.mode == KnifeHitGameMode.CAMPAIGN) {
                            val stars = 3
                            viewModelScope.launch {
                                repository.saveLevelCompletion(current.levelNumber, stars, current.levelNumber * 100)
                            }
                        }
                    }
                }
            } else {
                activeFlying.add(fk)
            }
        }

        // Update target shards if shattered
        val updatedShards = KnifeHitEngine.updateShards(shards, dt)

        _gameState.value = current.copy(
            targetAngle = newTargetAngle,
            targetRecoilY = newRecoil,
            attachedKnives = attachedKnives,
            attachedApples = apples,
            knivesRemaining = knivesRemaining,
            currentCombo = combo,
            flyingKnives = activeFlying,
            targetShards = updatedShards,
            isGameOver = gameOver,
            isStageWon = stageWon
        )

        if (gameOver || stageWon) {
            gameLoopJob?.cancel()
        }
    }

    fun onThrowKnife() {
        val current = _gameState.value
        if (current.isGameOver || current.isStageWon || current.knivesRemaining <= 0) return
        if (current.flyingKnives.isNotEmpty()) return // One knife at a time

        soundManager.play(GameSound.KNIFE_THROW)
        hapticManager.light()

        val spawnY = 600f // Start Y at bottom of screen
        val newKnife = FlyingKnife(y = spawnY)
        _gameState.value = current.copy(flyingKnives = listOf(newKnife))
    }

    fun retryStage() {
        val current = _gameState.value
        if (current.mode == KnifeHitGameMode.CAMPAIGN) {
            startCampaignLevel(current.levelNumber)
        } else {
            startBossRushStage(current.levelNumber)
        }
    }

    fun nextStage() {
        val current = _gameState.value
        if (current.mode == KnifeHitGameMode.CAMPAIGN) {
            if (current.levelNumber < KnifeHitRepository.TOTAL_LEVELS) {
                startCampaignLevel(current.levelNumber + 1)
            }
        } else {
            startBossRushStage(current.levelNumber + 1)
        }
    }

    fun buySkin(skin: KnifeSkin) {
        if (repository.spendApples(skin.costApples)) {
            repository.unlockSkin(skin.id)
            repository.equipSkin(skin.id)
            soundManager.play(GameSound.COIN)
            hapticManager.success()
        } else {
            soundManager.play(GameSound.INVALID)
            hapticManager.error()
        }
    }

    fun equipSkin(skinId: String) {
        repository.equipSkin(skinId)
        soundManager.play(GameSound.SELECT)
        hapticManager.light()
    }

    fun toggleSound(enabled: Boolean) {
        repository.setSoundEnabled(enabled)
        soundManager.soundEnabled = enabled
    }

    fun toggleHaptic(enabled: Boolean) {
        repository.setHapticEnabled(enabled)
        hapticManager.hapticsEnabled = enabled
    }

    fun getGamesPlayed() = repository.getGamesPlayed()
    fun getBossesDefeated() = repository.getBossesDefeated()
    fun getHighestCombo() = repository.getHighestCombo()

    fun addBonusApples(count: Int = 10) {
        repository.addApples(count)
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }

    class Factory(
        private val repository: KnifeHitRepository,
        private val soundManager: SoundManager,
        private val hapticManager: HapticManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return KnifeHitViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
