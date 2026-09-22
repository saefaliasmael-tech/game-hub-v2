package com.example.game2048.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.game2048.core.engine.Game2048Engine
import com.example.game2048.core.model.Game2048State
import com.example.game2048.core.model.MoveDirection
import com.example.game2048.core.repository.Game2048Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class Game2048ViewModel(
    private val repository: Game2048Repository,
    private val engine: Game2048Engine = Game2048Engine()
) : ViewModel() {

    private val _gameState = MutableStateFlow(engine.createInitialState())
    val gameState: StateFlow<Game2048State> = _gameState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSavedGameOrNew()
    }

    fun loadSavedGameOrNew() {
        viewModelScope.launch {
            val saved = repository.loadState()
            val bestScore = repository.getBestScore()

            if (saved != null && !saved.isGameOver && saved.board.any { it > 0 }) {
                _gameState.value = saved.copy(bestScore = maxOf(saved.bestScore, bestScore))
            } else {
                val fresh = engine.createInitialState(bestScore = bestScore)
                _gameState.value = fresh
                repository.saveGameState(fresh)
            }
            _isLoading.value = false
        }
    }

    fun onMove(direction: MoveDirection) {
        val current = _gameState.value
        if (current.isGameOver) return

        val nextState = engine.makeMove(current, direction)
        if (nextState != current) {
            _gameState.value = nextState
            viewModelScope.launch {
                repository.saveGameState(nextState)
            }
        }
    }

    fun continuePlayingAfterWin() {
        val current = _gameState.value
        val updated = engine.continuePlayingAfterWin(current)
        _gameState.value = updated
        viewModelScope.launch {
            repository.saveGameState(updated)
        }
    }

    fun startNewGame() {
        val current = _gameState.value
        val best = maxOf(current.bestScore, current.score)
        val fresh = engine.createInitialState(bestScore = best)
        _gameState.value = fresh
        viewModelScope.launch {
            repository.saveGameState(fresh)
        }
    }

    class Factory(private val repository: Game2048Repository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return Game2048ViewModel(repository) as T
        }
    }
}
