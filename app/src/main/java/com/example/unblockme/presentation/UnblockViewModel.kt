package com.example.unblockme.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.unblockme.core.level.UnblockLevelManager
import com.example.unblockme.core.model.*
import com.example.unblockme.core.repository.UnblockRepository
import com.example.unblockme.core.solver.UnblockSolver
import com.example.watersort.core.audio.GameSound
import com.example.watersort.core.audio.HapticManager
import com.example.watersort.core.audio.SoundManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UnblockViewModel(
    private val repository: UnblockRepository,
    private val soundManager: SoundManager? = null,
    private val hapticManager: HapticManager? = null
) : ViewModel() {

    private val _gameState = MutableStateFlow(UnblockGameState())
    val gameState: StateFlow<UnblockGameState> = _gameState.asStateFlow()

    val soundEnabled: StateFlow<Boolean> = repository.soundEnabledFlow
    val hapticEnabled: StateFlow<Boolean> = repository.hapticEnabledFlow

    val progressFlow = repository.progressFlow
    val totalStarsFlow = repository.totalStarsFlow
    val completedLevelsCountFlow = repository.completedLevelsCountFlow

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeFirstLevel()
        }
    }

    fun startLevel(levelNumber: Int) {
        val level = UnblockLevelManager.getLevel(levelNumber)
        timerJob?.cancel()

        _gameState.value = UnblockGameState(
            levelNumber = level.levelNumber,
            difficulty = level.difficulty,
            blocks = level.blocks,
            minMoves = level.minMoves,
            movesCount = 0,
            moveHistory = emptyList(),
            isWon = false,
            hintsUsed = 0,
            highlightedBlockId = null,
            hintMessage = null,
            elapsedTimeSeconds = 0,
            starsAwarded = 0,
            bestMoves = 0
        )

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _gameState.value
                if (!current.isWon) {
                    _gameState.value = current.copy(
                        elapsedTimeSeconds = current.elapsedTimeSeconds + 1
                    )
                }
            }
        }
    }

    /**
     * Attempts to move a block to a new position (newCol if horizontal, newRow if vertical).
     */
    fun moveBlock(blockId: String, newPos: Int) {
        val current = _gameState.value
        if (current.isWon) return

        val blockIndex = current.blocks.indexOfFirst { it.id == blockId }
        if (blockIndex == -1) return
        val block = current.blocks[blockIndex]

        val currentPos = if (block.orientation == Orientation.HORIZONTAL) block.col else block.row
        if (currentPos == newPos) return

        // Verify valid slide within bounds
        val (minPos, maxPos) = UnblockSolver.getSlidingBounds(block, current.blocks)
        val clampedPos = newPos.coerceIn(minPos, maxPos)
        if (clampedPos == currentPos) return

        val updatedBlock = if (block.orientation == Orientation.HORIZONTAL) {
            block.copy(col = clampedPos)
        } else {
            block.copy(row = clampedPos)
        }

        val updatedBlocks = current.blocks.toMutableList()
        updatedBlocks[blockIndex] = updatedBlock

        val move = UnblockMove(
            blockId = block.id,
            fromRow = block.row,
            fromCol = block.col,
            toRow = updatedBlock.row,
            toCol = updatedBlock.col
        )

        val newHistory = current.moveHistory + move
        val newMovesCount = current.movesCount + 1
        val isWon = updatedBlock.isEscaped

        val stars = if (isWon) {
            calculateStars(newMovesCount, current.minMoves)
        } else 0

        _gameState.value = current.copy(
            blocks = updatedBlocks,
            movesCount = newMovesCount,
            moveHistory = newHistory,
            isWon = isWon,
            starsAwarded = stars,
            highlightedBlockId = null,
            hintMessage = null
        )

        if (isWon) {
            soundManager?.play(GameSound.WIN)
            hapticManager?.success()
            viewModelScope.launch {
                repository.saveLevelCompletion(current.levelNumber, stars, newMovesCount)
                repository.clearActiveGame()
            }
        } else {
            soundManager?.play(GameSound.BUTTON)
            hapticManager?.tap()
            viewModelScope.launch {
                repository.saveActiveGame(_gameState.value)
            }
        }
    }

    fun undoMove() {
        val current = _gameState.value
        if (current.isWon || current.moveHistory.isEmpty()) return

        val lastMove = current.moveHistory.last()
        val remainingHistory = current.moveHistory.dropLast(1)

        val blockIndex = current.blocks.indexOfFirst { it.id == lastMove.blockId }
        if (blockIndex == -1) return

        val block = current.blocks[blockIndex]
        val restoredBlock = block.copy(row = lastMove.fromRow, col = lastMove.fromCol)

        val updatedBlocks = current.blocks.toMutableList()
        updatedBlocks[blockIndex] = restoredBlock

        _gameState.value = current.copy(
            blocks = updatedBlocks,
            moveHistory = remainingHistory,
            movesCount = (current.movesCount - 1).coerceAtLeast(0),
            highlightedBlockId = null,
            hintMessage = null
        )

        soundManager?.play(GameSound.BUTTON)
        hapticManager?.tap()
    }

    fun resetLevel() {
        val current = _gameState.value
        startLevel(current.levelNumber)
        soundManager?.play(GameSound.BUTTON)
        hapticManager?.tap()
    }

    fun requestHint() {
        val current = _gameState.value
        if (current.isWon) return

        viewModelScope.launch {
            val solution = withContext(Dispatchers.Default) {
                UnblockSolver.solve(current.blocks)
            }

            if (!solution.isNullOrEmpty()) {
                val nextMove = solution.first()
                val targetBlock = current.blocks.find { it.id == nextMove.blockId }
                val direction = if (targetBlock?.orientation == Orientation.HORIZONTAL) {
                    if (nextMove.toCol > nextMove.fromCol) "Right" else "Left"
                } else {
                    if (nextMove.toRow > nextMove.fromRow) "Down" else "Up"
                }

                _gameState.value = current.copy(
                    hintsUsed = current.hintsUsed + 1,
                    highlightedBlockId = nextMove.blockId,
                    hintMessage = "Slide highlighted block $direction"
                )
                soundManager?.play(GameSound.ACHIEVEMENT)
                hapticManager?.tap()
            } else {
                _gameState.value = current.copy(
                    hintMessage = "No direct moves found. Try undoing a move."
                )
            }
        }
    }

    fun dismissHint() {
        _gameState.value = _gameState.value.copy(hintMessage = null, highlightedBlockId = null)
    }

    fun toggleSound() {
        val newVal = !soundEnabled.value
        repository.setSoundEnabled(newVal)
        soundManager?.soundEnabled = newVal
    }

    fun toggleHaptic() {
        val newVal = !hapticEnabled.value
        repository.setHapticEnabled(newVal)
        hapticManager?.hapticsEnabled = newVal
    }

    fun nextLevel() {
        val current = _gameState.value
        if (current.levelNumber < UnblockRepository.TOTAL_LEVELS) {
            startLevel(current.levelNumber + 1)
        }
    }

    private fun calculateStars(movesCount: Int, minMoves: Int): Int {
        return when {
            movesCount <= minMoves + 1 -> 3
            movesCount <= (minMoves * 1.5).toInt() + 2 -> 2
            else -> 1
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    class Factory(
        private val repository: UnblockRepository,
        private val soundManager: SoundManager? = null,
        private val hapticManager: HapticManager? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UnblockViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
