package com.example.picpuzzle.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.picpuzzle.core.engine.PicPuzzleEngine
import com.example.picpuzzle.core.level.PicPuzzleLevelManager
import com.example.picpuzzle.core.model.PicGameState
import com.example.picpuzzle.core.repository.PicPuzzleRepository
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

class PicPuzzleViewModel(
    val repository: PicPuzzleRepository,
    val soundManager: SoundManager,
    val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(PicGameState())
    val gameState: StateFlow<PicGameState> = _gameState.asStateFlow()

    private var timerJob: Job? = null

    val soundEnabledFlow = repository.soundEnabledFlow
    val hapticEnabledFlow = repository.hapticEnabledFlow
    val showHintsFlow = repository.showHintsFlow
    val highestLevelFlow = repository.highestLevelFlow
    val progressFlow = repository.progressFlow
    val completedLevelsFlow = repository.completedLevelsFlow

    // Levels are initialized when started by screen or user

    fun stopTimer() {
        timerJob?.cancel()
    }

    fun startLevel(levelNumber: Int) {
        timerJob?.cancel()

        val config = PicPuzzleLevelManager.getLevel(levelNumber)
        val tiles = PicPuzzleEngine.generateBoard(
            gridSize = config.gridSize,
            shuffleMoves = config.shuffleMoves,
            seed = levelNumber * 104729L
        )

        _gameState.value = PicGameState(
            levelNumber = levelNumber,
            gridSize = config.gridSize,
            tiles = tiles,
            movesCount = 0,
            elapsedTimeSeconds = 0L,
            isSolved = false,
            isPaused = false,
            showNumberHints = showHintsFlow.value,
            theme = config.theme
        )

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (!_gameState.value.isSolved && !_gameState.value.isPaused) {
                    _gameState.value = _gameState.value.copy(
                        elapsedTimeSeconds = _gameState.value.elapsedTimeSeconds + 1
                    )
                }
            }
        }
    }

    fun onTileClick(tileId: Int) {
        val current = _gameState.value
        if (current.isSolved || current.isPaused) return

        val (newTiles, didMove) = PicPuzzleEngine.slideTile(
            tiles = current.tiles,
            clickedTileId = tileId,
            gridSize = current.gridSize
        )

        if (didMove) {
            val newMoves = current.movesCount + 1
            if (soundEnabledFlow.value) soundManager.play(GameSound.BUTTON)
            if (hapticEnabledFlow.value) hapticManager.light()

            val solved = PicPuzzleEngine.isSolved(newTiles)
            if (solved) {
                if (soundEnabledFlow.value) soundManager.play(GameSound.WIN)
                if (hapticEnabledFlow.value) hapticManager.success()
                viewModelScope.launch {
                    repository.saveLevelCompletion(current.levelNumber, newMoves)
                }
            }

            _gameState.value = current.copy(
                tiles = newTiles,
                movesCount = newMoves,
                isSolved = solved
            )
        } else {
            if (hapticEnabledFlow.value) hapticManager.error()
        }
    }

    fun toggleHints() {
        val newSetting = !_gameState.value.showNumberHints
        repository.setShowHints(newSetting)
        _gameState.value = _gameState.value.copy(showNumberHints = newSetting)
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
        timerJob?.cancel()
    }

    class Factory(
        private val repository: PicPuzzleRepository,
        private val soundManager: SoundManager,
        private val hapticManager: HapticManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PicPuzzleViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
