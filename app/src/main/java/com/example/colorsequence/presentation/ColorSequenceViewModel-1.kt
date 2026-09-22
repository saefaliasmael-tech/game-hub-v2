package com.example.colorsequence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.colorsequence.core.engine.ColorSequenceEngine
import com.example.colorsequence.core.model.ColorSequenceGameState
import com.example.colorsequence.core.model.GamePhase
import com.example.colorsequence.core.model.SequenceColor
import com.example.colorsequence.core.repository.ColorSequenceRepository
import com.example.watersort.core.database.GameProgressEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ColorSequenceViewModel(
    private val repository: ColorSequenceRepository,
    private val engine: ColorSequenceEngine = ColorSequenceEngine()
) : ViewModel() {

    val levelProgressFlow: StateFlow<List<GameProgressEntity>> = repository.progressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStarsFlow: StateFlow<Int> = repository.totalStarsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedLevelsFlow: StateFlow<Int> = repository.completedLevelsCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _gameState = MutableStateFlow<ColorSequenceGameState?>(null)
    val gameState: StateFlow<ColorSequenceGameState?> = _gameState.asStateFlow()

    private val _isColorBlindMode = MutableStateFlow(true) // Enabled by default for accessibility
    val isColorBlindMode: StateFlow<Boolean> = _isColorBlindMode.asStateFlow()

    private var playbackJob: Job? = null

    fun toggleColorBlindMode() {
        _isColorBlindMode.value = !_isColorBlindMode.value
    }

    fun startLevel(levelNumber: Int) {
        playbackJob?.cancel()
        val initialState = engine.startLevel(levelNumber)
        _gameState.value = initialState
        startSequencePlayback(initialState)
    }

    fun retryCurrentLevel() {
        val current = _gameState.value ?: return
        startLevel(current.levelNumber)
    }

    fun playNextLevel() {
        val current = _gameState.value ?: return
        if (current.levelNumber < ColorSequenceRepository.TOTAL_LEVELS) {
            startLevel(current.levelNumber + 1)
        }
    }

    private fun startSequencePlayback(state: ColorSequenceGameState) {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val config = engine.generateLevelConfig(state.levelNumber)
            val interval = config.displayIntervalMs

            // Initial brief delay before playback starts
            delay(500)

            for (index in state.targetSequence.indices) {
                val color = state.targetSequence[index]
                _gameState.value = _gameState.value?.copy(
                    phase = GamePhase.SHOWING_SEQUENCE,
                    activeDisplayColor = color,
                    activeDisplayIndex = index
                )
                delay(interval)

                // Brief blank period between sequence items so duplicate colors blink clearly
                _gameState.value = _gameState.value?.copy(
                    activeDisplayColor = null,
                    activeDisplayIndex = -1
                )
                delay(150)
            }

            // Sequence finished, switch to awaiting player input
            _gameState.value?.let { curr ->
                val readyState = engine.onSequenceDisplayFinished(curr)
                _gameState.value = readyState
                repository.saveMidGameState(readyState)
            }
        }
    }

    fun onPlayerColorTap(color: SequenceColor) {
        val current = _gameState.value ?: return
        if (current.phase != GamePhase.AWAITING_INPUT) return

        val newState = engine.processPlayerTap(current, color)
        _gameState.value = newState

        viewModelScope.launch {
            when (newState.phase) {
                GamePhase.LEVEL_WON -> {
                    repository.saveLevelCompletion(
                        levelNumber = newState.levelNumber,
                        stars = newState.starsAwarded,
                        score = newState.score
                    )
                }
                GamePhase.LEVEL_FAILED -> {
                    repository.clearMidGameState()
                }
                GamePhase.AWAITING_INPUT -> {
                    repository.saveMidGameState(newState)
                }
                else -> Unit
            }
        }
    }

    fun exitGame() {
        playbackJob?.cancel()
    }

    class Factory(private val repository: ColorSequenceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ColorSequenceViewModel(repository) as T
        }
    }
}
