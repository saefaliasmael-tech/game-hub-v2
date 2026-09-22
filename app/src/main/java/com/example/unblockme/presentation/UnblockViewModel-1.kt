package com.example.unblockme.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.unblockme.core.engine.UnblockEngine
import com.example.unblockme.core.level.UnblockLevelManager
import com.example.unblockme.core.model.Block
import com.example.unblockme.core.model.Orientation
import com.example.unblockme.core.model.UnblockState
import com.example.unblockme.core.repository.UnblockRepository
import com.example.watersort.core.audio.GameSound
import com.example.watersort.core.audio.HapticManager
import com.example.watersort.core.audio.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UnblockViewModel(
    private val repository: UnblockRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(UnblockState())
    val gameState: StateFlow<UnblockState> = _gameState.asStateFlow()

    val soundEnabledFlow = repository.soundEnabledFlow
    val hapticEnabledFlow = repository.hapticEnabledFlow
    val highestLevelFlow = repository.highestLevelFlow
    val progressFlow = repository.progressFlow

    init {
        startLevel(1)
    }

    fun startLevel(levelNumber: Int) {
        val level = UnblockLevelManager.getLevel(levelNumber)
        _gameState.value = UnblockState(
            levelNumber = level.levelNumber,
            difficulty = level.difficulty,
            blocks = level.blocks.map { it.copy() },
            moveCount = 0,
            minMoves = level.minMoves,
            isWon = false,
            stars = 0,
            moveHistory = emptyList(),
            hintMove = null
        )
    }

    fun moveBlock(blockId: String, delta: Int) {
        val state = _gameState.value
        if (state.isWon || delta == 0) return

        val block = state.blocks.find { it.id == blockId } ?: return
        val newBlocks = UnblockEngine.moveBlock(block, delta, state.blocks) ?: return

        if (soundEnabledFlow.value) soundManager.play(GameSound.SELECT)
        if (hapticEnabledFlow.value) hapticManager.tap()

        val isWon = UnblockEngine.checkWin(newBlocks)
        val newMoveCount = state.moveCount + 1

        val stars = if (isWon) {
            when {
                newMoveCount <= state.minMoves -> 3
                newMoveCount <= state.minMoves + 4 -> 2
                else -> 1
            }
        } else 0

        _gameState.value = state.copy(
            blocks = newBlocks,
            moveCount = newMoveCount,
            isWon = isWon,
            stars = stars,
            moveHistory = state.moveHistory + listOf(state.blocks),
            hintMove = null
        )

        if (isWon) {
            if (soundEnabledFlow.value) soundManager.play(GameSound.WIN)
            if (hapticEnabledFlow.value) hapticManager.success()

            viewModelScope.launch {
                repository.saveLevelCompletion(state.levelNumber, newMoveCount, stars)
            }
        }
    }

    fun undo() {
        val state = _gameState.value
        if (state.isWon || state.moveHistory.isEmpty()) return

        val prevBlocks = state.moveHistory.last()
        _gameState.value = state.copy(
            blocks = prevBlocks,
            moveCount = (state.moveCount - 1).coerceAtLeast(0),
            moveHistory = state.moveHistory.dropLast(1),
            hintMove = null
        )

        if (hapticEnabledFlow.value) hapticManager.tap()
    }

    fun resetLevel() {
        startLevel(_gameState.value.levelNumber)
    }

    fun nextLevel() {
        val next = (_gameState.value.levelNumber + 1).coerceAtMost(UnblockRepository.TOTAL_LEVELS)
        startLevel(next)
    }

    fun provideHint() {
        val state = _gameState.value
        if (state.isWon) return

        // Find any valid move that clears the path for the target block
        val target = state.blocks.find { it.isTarget } ?: return
        for (col in (target.col + target.length) until UnblockEngine.GRID_SIZE) {
            val blocker = state.blocks.find { it.id != target.id && it.occupies(2, col) }
            if (blocker != null) {
                // Check if blocker can move up or down
                if (blocker.orientation == Orientation.VERTICAL) {
                    if (UnblockEngine.canMove(blocker, -1, state.blocks)) {
                        _gameState.value = state.copy(hintMove = Pair(blocker.id, -1))
                        if (hapticEnabledFlow.value) hapticManager.tap()
                        return
                    }
                    if (UnblockEngine.canMove(blocker, 1, state.blocks)) {
                        _gameState.value = state.copy(hintMove = Pair(blocker.id, 1))
                        if (hapticEnabledFlow.value) hapticManager.tap()
                        return
                    }
                }
            }
        }
        // If target can move forward directly
        if (UnblockEngine.canMove(target, 1, state.blocks)) {
            _gameState.value = state.copy(hintMove = Pair(target.id, 1))
            if (hapticEnabledFlow.value) hapticManager.tap()
        }
    }

    fun setSoundEnabled(enabled: Boolean) = repository.setSoundEnabled(enabled)
    fun setHapticEnabled(enabled: Boolean) = repository.setHapticEnabled(enabled)

    class Factory(
        private val repository: UnblockRepository,
        private val soundManager: SoundManager,
        private val hapticManager: HapticManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UnblockViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
