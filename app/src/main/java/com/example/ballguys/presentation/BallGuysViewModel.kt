package com.example.ballguys.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ballguys.engine.BallGuysEngine
import com.example.ballguys.model.BallGuysState
import com.example.ballguys.model.BallTiers
import com.example.ballguys.repository.BallGuysRepository
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
import kotlin.random.Random

class BallGuysViewModel(
    private val repository: BallGuysRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {
    private val engine = BallGuysEngine()
    private var physicsJob: Job? = null
    var boxWidth: Float = 600f
    var boxHeight: Float = 900f

    private val _state = MutableStateFlow(
        BallGuysState(bestScore = repository.bestScore.value)
    )
    val state: StateFlow<BallGuysState> = _state.asStateFlow()

    fun startGame() {
        stopGame()
        _state.value = BallGuysState(
            currentDropTier = 1,
            nextDropTier = Random.nextInt(1, 4),
            bestScore = repository.bestScore.value
        )

        physicsJob = viewModelScope.launch {
            while (isActive) {
                delay(16L) // 60fps
                if (_state.value.isGameOver) break

                val next = engine.updatePhysics(
                    state = _state.value,
                    boxWidth = boxWidth,
                    boxHeight = boxHeight,
                    onMerge = { pts ->
                        soundManager.play(GameSound.ACHIEVEMENT)
                        hapticManager.medium()
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

    fun setAimX(normX: Float) {
        _state.update { it.copy(dropAimX = normX.coerceIn(0.1f, 0.9f)) }
    }

    fun dropBall() {
        if (_state.value.isGameOver) return

        val tier = _state.value.currentDropTier
        val aimX = _state.value.dropAimX * boxWidth
        val newBall = engine.createBall(tier, aimX, 60f)

        soundManager.play(GameSound.HIT)
        hapticManager.light()

        _state.update { current ->
            current.copy(
                balls = current.balls + newBall,
                currentDropTier = current.nextDropTier,
                nextDropTier = Random.nextInt(1, 4)
            )
        }
    }

    fun restart() {
        startGame()
        soundManager.play(GameSound.BUTTON)
    }

    fun stopGame() {
        physicsJob?.cancel()
        physicsJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopGame()
    }
}
