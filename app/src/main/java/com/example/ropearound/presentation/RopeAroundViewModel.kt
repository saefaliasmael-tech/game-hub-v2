package com.example.ropearound.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ropearound.core.engine.RopeAroundEngine
import com.example.ropearound.core.level.RopeAroundLevelManager
import com.example.ropearound.core.model.Peg
import com.example.ropearound.core.model.RopeGameState
import com.example.ropearound.core.model.RopeLevelConfig
import com.example.ropearound.core.repository.RopeAroundRepository
import com.example.watersort.core.audio.GameSound
import com.example.watersort.core.audio.HapticManager
import com.example.watersort.core.audio.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RopeAroundViewModel(
    val repository: RopeAroundRepository,
    val soundManager: SoundManager,
    val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(RopeGameState())
    val gameState: StateFlow<RopeGameState> = _gameState.asStateFlow()

    private var activeConfig: RopeLevelConfig = RopeAroundLevelManager.getLevel(1)

    val soundEnabledFlow = repository.soundEnabledFlow
    val hapticEnabledFlow = repository.hapticEnabledFlow
    val highestLevelFlow = repository.highestLevelFlow
    val progressFlow = repository.progressFlow
    val completedLevelsFlow = repository.completedLevelsFlow

    init {
        startLevel(1)
    }

    fun startLevel(levelNumber: Int) {
        val config = RopeAroundLevelManager.getLevel(levelNumber)
        activeConfig = config

        val anchor = Offset(config.anchorXPercent, config.anchorYPercent)

        _gameState.value = RopeGameState(
            levelNumber = levelNumber,
            pegs = config.pegs,
            obstacles = config.obstacles,
            pivotPoints = listOf(anchor),
            currentTouchPos = anchor,
            isDragging = false,
            isWon = false,
            isGameOver = false,
            isPaused = false,
            ropeLengthPercent = 0f,
            maxRopeLength = config.maxRopeLengthRatio
        )
    }

    fun onTouchUpdate(xPercent: Float, yPercent: Float, isDragging: Boolean) {
        val current = _gameState.value
        if (current.isGameOver || current.isWon || current.isPaused) return

        val touchPos = Offset(xPercent.coerceIn(0.05f, 0.95f), yPercent.coerceIn(0.05f, 0.95f))

        val (newPivots, newPegs) = RopeAroundEngine.updatePivots(
            pivots = current.pivotPoints,
            touchPos = touchPos,
            pegs = current.pegs
        )

        // Count newly wrapped pegs to play chime
        val newlyWrapped = newPegs.count { it.isWrapped } > current.pegs.count { it.isWrapped }
        if (newlyWrapped) {
            if (soundEnabledFlow.value) soundManager.play(GameSound.SELECT)
            if (hapticEnabledFlow.value) hapticManager.light()
        }

        val totalLength = RopeAroundEngine.calculateTotalLength(newPivots, touchPos)

        // Check obstacle collision
        val hitObstacle = RopeAroundEngine.checkObstacleCollision(newPivots, touchPos, current.obstacles)
        val lengthExceeded = totalLength > current.maxRopeLength

        var isGameOver = false
        var isWon = false

        if (hitObstacle || lengthExceeded) {
            isGameOver = true
            if (soundEnabledFlow.value) soundManager.play(GameSound.INVALID)
            if (hapticEnabledFlow.value) hapticManager.error()
        } else if (newPegs.all { it.isWrapped }) {
            isWon = true
            if (soundEnabledFlow.value) soundManager.play(GameSound.WIN)
            if (hapticEnabledFlow.value) hapticManager.success()
            viewModelScope.launch {
                repository.saveLevelCompletion(current.levelNumber)
            }
        }

        _gameState.value = current.copy(
            pivotPoints = newPivots,
            pegs = newPegs,
            currentTouchPos = touchPos,
            isDragging = isDragging,
            ropeLengthPercent = totalLength,
            isGameOver = isGameOver,
            isWon = isWon
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

    fun toggleHaptic() {
        repository.setHapticEnabled(!hapticEnabledFlow.value)
    }

    class Factory(
        private val repository: RopeAroundRepository,
        private val soundManager: SoundManager,
        private val hapticManager: HapticManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RopeAroundViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
