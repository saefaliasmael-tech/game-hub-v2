package com.example.paperio2.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paperio2.engine.PaperIoEngine
import com.example.paperio2.model.PaperIoState
import com.example.paperio2.repository.PaperIoRepository
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

class PaperIoViewModel(
    private val repository: PaperIoRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {
    private val engine = PaperIoEngine()
    private var loopJob: Job? = null
    var inputVx: Float = 0f
    var inputVy: Float = 0f

    private val _state = MutableStateFlow(
        engine.initState().copy(bestPercent = repository.bestPercent.value)
    )
    val state: StateFlow<PaperIoState> = _state.asStateFlow()

    fun startGame() {
        stopGame()
        inputVx = 1f
        inputVy = 0f
        _state.value = engine.initState().copy(bestPercent = repository.bestPercent.value)

        loopJob = viewModelScope.launch {
            while (isActive) {
                delay(25L) // 40fps
                if (_state.value.isGameOver) break

                val next = engine.update(
                    state = _state.value,
                    playerInputVx = inputVx,
                    playerInputVy = inputVy,
                    onKill = {
                        soundManager.play(GameSound.COIN)
                        hapticManager.medium()
                    },
                    onPlayerDied = {
                        soundManager.play(GameSound.GAME_OVER)
                        hapticManager.error()
                        repository.savePercent(_state.value.player.territoryPercent)
                    }
                )
                _state.value = next
            }
        }
    }

    fun steer(dx: Float, dy: Float) {
        inputVx = dx
        inputVy = dy
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
