package com.example.doodlejump.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doodlejump.engine.DoodleJumpEngine
import com.example.doodlejump.model.DoodleJumpState
import com.example.doodlejump.repository.DoodleJumpRepository
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

class DoodleJumpViewModel(
    private val repository: DoodleJumpRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {
    private val engine = DoodleJumpEngine()
    private var loopJob: Job? = null
    var horizontalInput: Float = 0f

    private val _state = MutableStateFlow(DoodleJumpState())
    val state: StateFlow<DoodleJumpState> = _state.asStateFlow()

    fun startGame() {
        stopGame()
        val (player, platforms) = engine.initGame()
        _state.value = DoodleJumpState(
            player = player,
            platforms = platforms,
            cameraY = 0f,
            score = 0,
            bestScore = repository.bestScore.value,
            isGameOver = false
        )

        loopJob = viewModelScope.launch {
            while (isActive) {
                delay(16L) // 60fps
                if (_state.value.isGameOver) break

                val next = engine.update(
                    state = _state.value,
                    moveDirectionX = horizontalInput,
                    onBounce = { isSpring ->
                        if (isSpring) {
                            soundManager.play(GameSound.ACHIEVEMENT)
                            hapticManager.strong()
                        } else {
                            soundManager.play(GameSound.JUMP)
                            hapticManager.light()
                        }
                    },
                    onGameOver = {
                        soundManager.play(GameSound.GAME_OVER)
                        hapticManager.error()
                        repository.saveScore(_state.value.score)
                    }
                )
                _state.value = next
            }
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
