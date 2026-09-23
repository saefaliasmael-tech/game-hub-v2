package com.example.holeio.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.holeio.engine.HoleIoEngine
import com.example.holeio.model.HoleIoState
import com.example.holeio.repository.HoleIoRepository
import com.example.watersort.core.audio.GameSound
import com.example.watersort.core.audio.HapticManager
import com.example.watersort.core.audio.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HoleIoViewModel(
    private val repository: HoleIoRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {
    private val engine = HoleIoEngine()
    private var physicsJob: Job? = null
    private var timerJob: Job? = null

    var dragTargetX: Float = HoleIoEngine.ARENA_WIDTH / 2f
    var dragTargetY: Float = HoleIoEngine.ARENA_HEIGHT / 2f

    private val _state = MutableStateFlow(
        engine.initState().copy(bestScore = repository.bestScore.value)
    )
    val state: StateFlow<HoleIoState> = _state.asStateFlow()

    fun startGame() {
        stopGame()
        dragTargetX = HoleIoEngine.ARENA_WIDTH / 2f
        dragTargetY = HoleIoEngine.ARENA_HEIGHT / 2f
        _state.value = engine.initState().copy(bestScore = repository.bestScore.value)

        // Physics loop
        physicsJob = viewModelScope.launch {
            while (isActive) {
                delay(20L) // 50fps
                if (_state.value.isGameOver) break

                val next = engine.updatePhysics(
                    state = _state.value,
                    playerTargetX = dragTargetX,
                    playerTargetY = dragTargetY,
                    onItemEaten = {
                        soundManager.play(GameSound.APPLE)
                        hapticManager.light()
                    }
                )
                _state.value = next
            }
        }

        // 60-second timer loop
        timerJob = viewModelScope.launch {
            while (isActive && _state.value.timeLeftSeconds > 0) {
                delay(1000L)
                _state.update { current ->
                    val newTime = current.timeLeftSeconds - 1
                    val isEnd = newTime <= 0
                    if (isEnd) {
                        soundManager.play(GameSound.WIN)
                        hapticManager.success()
                        repository.saveScore(current.playerHole.score)
                    }
                    current.copy(timeLeftSeconds = newTime, isGameOver = isEnd)
                }
            }
        }
    }

    fun restart() {
        startGame()
        soundManager.play(GameSound.BUTTON)
    }

    fun stopGame() {
        physicsJob?.cancel()
        physicsJob = null
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopGame()
    }
}
