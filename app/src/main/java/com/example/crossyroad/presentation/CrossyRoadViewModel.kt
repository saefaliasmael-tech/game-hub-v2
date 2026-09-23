package com.example.crossyroad.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crossyroad.engine.CrossyRoadEngine
import com.example.crossyroad.model.CrossyPlayer
import com.example.crossyroad.model.CrossyRoadState
import com.example.crossyroad.repository.CrossyRoadRepository
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

class CrossyRoadViewModel(
    private val repository: CrossyRoadRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {
    private val engine = CrossyRoadEngine()
    private var loopJob: Job? = null

    private val _state = MutableStateFlow(
        CrossyRoadState(
            rows = engine.generateInitialRows(),
            bestScore = repository.bestScore.value
        )
    )
    val state: StateFlow<CrossyRoadState> = _state.asStateFlow()

    fun startGame() {
        stopGame()
        _state.value = CrossyRoadState(
            player = CrossyPlayer(x = 4, y = 0),
            rows = engine.generateInitialRows(),
            score = 0,
            bestScore = repository.bestScore.value,
            isGameOver = false
        )

        loopJob = viewModelScope.launch {
            while (isActive) {
                delay(20L) // 50fps
                if (_state.value.isGameOver) break

                val next = engine.update(
                    state = _state.value,
                    onCollision = { reason ->
                        soundManager.play(GameSound.GAME_OVER)
                        hapticManager.error()
                        repository.saveScore(_state.value.score)
                    }
                )
                _state.value = next
            }
        }
    }

    fun hop(dx: Int, dy: Int) {
        if (_state.value.isGameOver) return
        val next = engine.hop(_state.value, dx, dy)
        if (next != _state.value) {
            soundManager.play(GameSound.JUMP)
            hapticManager.tap()
            _state.value = next
        }
    }

    fun restart() {
        startGame()
        soundManager.play(GameSound.BUTTON)
    }

    fun stopGame() {
        loopJob?.cancel()
        loopJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopGame()
    }
}
