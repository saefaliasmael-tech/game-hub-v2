package com.example.pullthepin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pullthepin.core.engine.PullThePinEngine
import com.example.pullthepin.core.level.PullThePinLevelManager
import com.example.pullthepin.core.model.PinGameState
import com.example.pullthepin.core.repository.PullThePinRepository
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

class PullThePinViewModel(
    val repository: PullThePinRepository,
    val soundManager: SoundManager,
    val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(PinGameState())
    val gameState: StateFlow<PinGameState> = _gameState.asStateFlow()

    private var physicsJob: Job? = null

    val soundEnabledFlow = repository.soundEnabledFlow
    val hapticEnabledFlow = repository.hapticEnabledFlow
    val highestLevelFlow = repository.highestLevelFlow
    val progressFlow = repository.progressFlow
    val completedLevelsFlow = repository.completedLevelsFlow

    init {
        startLevel(1)
    }

    fun startLevel(levelNumber: Int) {
        physicsJob?.cancel()

        val config = PullThePinLevelManager.getLevel(levelNumber)

        _gameState.value = PinGameState(
            levelNumber = levelNumber,
            balls = config.balls,
            pins = config.pins,
            bombs = config.bombs,
            walls = config.walls,
            bucket = config.bucket,
            totalBalls = config.balls.size,
            collectedCount = 0,
            requiredCount = config.requiredBalls,
            isWon = false,
            isGameOver = false
        )

        startPhysicsLoop()
    }

    fun onTouchScreen(pxPercent: Float, pyPercent: Float) {
        val current = _gameState.value
        if (current.isWon || current.isGameOver) return

        val touchedPin = current.pins.firstOrNull { pin ->
            PullThePinEngine.isPointNearPinHandle(pin, pxPercent, pyPercent)
        }

        if (touchedPin != null) {
            val updatedPins = PullThePinEngine.pullPin(current.pins, touchedPin.id)
            if (soundEnabledFlow.value) soundManager.play(GameSound.BUTTON)
            if (hapticEnabledFlow.value) hapticManager.medium()
            _gameState.value = current.copy(pins = updatedPins)
        }
    }

    private fun startPhysicsLoop() {
        physicsJob?.cancel()
        physicsJob = viewModelScope.launch {
            while (isActive) {
                updatePhysics()
                delay(16)
            }
        }
    }

    private fun updatePhysics() {
        val current = _gameState.value
        if (current.isWon || current.isGameOver) return

        val (newBalls, newBombs, newlyCollected, bombExploded) = PullThePinEngine.stepPhysics(
            balls = current.balls,
            pins = current.pins,
            bombs = current.bombs,
            walls = current.walls,
            bucket = current.bucket
        )

        if (bombExploded) {
            if (soundEnabledFlow.value) soundManager.play(GameSound.HIT)
            if (hapticEnabledFlow.value) hapticManager.strong()
        }

        if (newlyCollected > 0) {
            if (soundEnabledFlow.value) soundManager.play(GameSound.COIN)
            if (hapticEnabledFlow.value) hapticManager.light()
        }

        val totalCollected = current.collectedCount + newlyCollected

        // Check completion condition
        val allFinished = newBalls.all { !it.isAlive || it.inBucket }
        var isWon = false
        var isGameOver = false

        if (allFinished && newBalls.isNotEmpty()) {
            if (totalCollected >= current.requiredCount) {
                isWon = true
                if (soundEnabledFlow.value) soundManager.play(GameSound.WIN)
                if (hapticEnabledFlow.value) hapticManager.success()
                viewModelScope.launch {
                    repository.saveLevelCompletion(current.levelNumber, totalCollected)
                }
            } else {
                isGameOver = true
                if (soundEnabledFlow.value) soundManager.play(GameSound.INVALID)
                if (hapticEnabledFlow.value) hapticManager.error()
            }
        }

        _gameState.value = current.copy(
            balls = newBalls,
            bombs = newBombs,
            collectedCount = totalCollected,
            isWon = isWon,
            isGameOver = isGameOver
        )
    }

    fun restartLevel() {
        startLevel(_gameState.value.levelNumber)
    }

    fun nextLevel() {
        startLevel(_gameState.value.levelNumber + 1)
    }

    fun toggleSound() {
        repository.setSoundEnabled(!soundEnabledFlow.value)
    }

    override fun onCleared() {
        super.onCleared()
        physicsJob?.cancel()
    }

    class Factory(
        private val repository: PullThePinRepository,
        private val soundManager: SoundManager,
        private val hapticManager: HapticManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PullThePinViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
