package com.example.aa.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aa.core.engine.AAEngine
import com.example.aa.core.level.AALevelManager
import com.example.aa.core.model.AAGameState
import com.example.aa.core.model.AALevelConfig
import com.example.aa.core.model.PinnedBall
import com.example.aa.core.model.ShootingBall
import com.example.aa.core.repository.AARepository
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

class AAViewModel(
    val repository: AARepository,
    val soundManager: SoundManager,
    val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(AAGameState())
    val gameState: StateFlow<AAGameState> = _gameState.asStateFlow()

    private var activeConfig: AALevelConfig = AALevelManager.getLevel(1)
    private var gameLoopJob: Job? = null
    private var currentSpeed: Float = 60f
    private var lastDirectionChangeMs: Long = 0L

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
        val config = AALevelManager.getLevel(levelNumber)
        activeConfig = config
        currentSpeed = config.rotationSpeed
        lastDirectionChangeMs = System.currentTimeMillis()

        val initialPins = config.initialPinnedAngles.mapIndexed { index, angle ->
            PinnedBall(
                id = index,
                angleDegrees = angle,
                number = -(index + 1)
            )
        }

        val ballNumbers = (config.ballsToShoot downTo 1).toList()

        _gameState.value = AAGameState(
            levelNumber = levelNumber,
            currentAngle = 0f,
            pinnedBalls = initialPins,
            remainingBallNumbers = ballNumbers,
            shootingBall = null,
            isGameOver = false,
            isWon = false,
            isPaused = false,
            collisionAngle = null,
            score = 0
        )

        startGameLoop()
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTimeNanos = System.nanoTime()

            while (isActive) {
                delay(16) // ~60 FPS
                val nowNanos = System.nanoTime()
                val deltaSec = (nowNanos - lastTimeNanos) / 1_000_000_000f
                lastTimeNanos = nowNanos

                val current = _gameState.value
                if (current.isGameOver || current.isWon || current.isPaused) continue

                // Check direction reverse if enabled for level
                if (activeConfig.reversesDirection && activeConfig.reverseIntervalMs > 0L) {
                    val nowMs = System.currentTimeMillis()
                    if (nowMs - lastDirectionChangeMs >= activeConfig.reverseIntervalMs) {
                        currentSpeed = -currentSpeed
                        lastDirectionChangeMs = nowMs
                    }
                }

                val newAngle = AAEngine.updateRotation(
                    currentAngle = current.currentAngle,
                    speed = currentSpeed,
                    deltaTimeSec = deltaSec
                )

                // Update shooting ball progress if ball is flying
                var updatedShooting = current.shootingBall
                var newPinned = current.pinnedBalls
                var isGameOver = false
                var isWon = false
                var collisionAngle: Float? = null

                if (updatedShooting != null) {
                    val newProgress = updatedShooting.progress + deltaSec * 8f // Flight speed
                    if (newProgress >= 1f) {
                        // Ball arrived at target
                        val (collided, collAngle) = AAEngine.checkCollision(
                            pinnedBalls = current.pinnedBalls,
                            currentWheelAngle = newAngle
                        )

                        if (collided) {
                            isGameOver = true
                            collisionAngle = collAngle
                            updatedShooting = null
                            if (soundEnabledFlow.value) soundManager.play(GameSound.INVALID)
                            if (hapticEnabledFlow.value) hapticManager.error()
                        } else {
                            val pinAngleOnWheel = AAEngine.calculatePinAngleOnWheel(newAngle)
                            val newPin = PinnedBall(
                                id = current.pinnedBalls.size + 1,
                                angleDegrees = pinAngleOnWheel,
                                number = updatedShooting.number
                            )
                            newPinned = current.pinnedBalls + newPin
                            updatedShooting = null
                            repository.incrementBallsPinned(1)

                            if (soundEnabledFlow.value) soundManager.play(GameSound.SELECT)
                            if (hapticEnabledFlow.value) hapticManager.light()

                            if (current.remainingBallNumbers.isEmpty()) {
                                isWon = true
                                if (soundEnabledFlow.value) soundManager.play(GameSound.WIN)
                                if (hapticEnabledFlow.value) hapticManager.success()
                                viewModelScope.launch {
                                    repository.saveLevelCompletion(current.levelNumber)
                                }
                            }
                        }
                    } else {
                        updatedShooting = updatedShooting.copy(progress = newProgress)
                    }
                }

                _gameState.value = current.copy(
                    currentAngle = newAngle,
                    pinnedBalls = newPinned,
                    shootingBall = updatedShooting,
                    isGameOver = isGameOver,
                    isWon = isWon,
                    collisionAngle = collisionAngle
                )
            }
        }
    }

    fun shootBall() {
        val current = _gameState.value
        if (current.isGameOver || current.isWon || current.isPaused) return
        if (current.shootingBall != null) return // Ball already in flight
        if (current.remainingBallNumbers.isEmpty()) return

        val nextNumber = current.remainingBallNumbers.first()
        val remaining = current.remainingBallNumbers.drop(1)

        _gameState.value = current.copy(
            remainingBallNumbers = remaining,
            shootingBall = ShootingBall(
                id = nextNumber,
                number = nextNumber,
                progress = 0f
            )
        )

        if (soundEnabledFlow.value) soundManager.play(GameSound.BUTTON)
        if (hapticEnabledFlow.value) hapticManager.tap()
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
        private val repository: AARepository,
        private val soundManager: SoundManager,
        private val hapticManager: HapticManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AAViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
