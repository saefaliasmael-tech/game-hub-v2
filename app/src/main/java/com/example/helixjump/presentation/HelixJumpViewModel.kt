package com.example.helixjump.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helixjump.engine.HelixJumpEngine
import com.example.helixjump.model.HelixBall
import com.example.helixjump.model.HelixJumpState
import com.example.helixjump.repository.HelixJumpRepository
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

class HelixJumpViewModel(
    private val repository: HelixJumpRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {
    private val engine = HelixJumpEngine()
    private var loopJob: Job? = null

    private val _state = MutableStateFlow(
        HelixJumpState(
            floors = engine.generateFloors(1),
            bestScore = repository.bestScore.value
        )
    )
    val state: StateFlow<HelixJumpState> = _state.asStateFlow()

    fun rotateTower(dragAmountX: Float) {
        _state.update { current ->
            current.copy(towerRotation = (current.towerRotation + dragAmountX * 0.4f) % 360f)
        }
    }

    fun startGame() {
        stopGameLoop()
        loopJob = viewModelScope.launch {
            while (isActive) {
                delay(16L) // ~60fps
                val currentState = _state.value
                if (currentState.isGameOver || currentState.isWon) break

                val nextState = engine.updatePhysics(
                    state = currentState,
                    onBounce = { isSmash ->
                        if (isSmash) {
                            soundManager.play(GameSound.ACHIEVEMENT)
                            hapticManager.strong()
                        } else {
                            soundManager.play(GameSound.JUMP)
                            hapticManager.light()
                        }
                    },
                    onPassFloor = { streak ->
                        soundManager.play(GameSound.COIN)
                        hapticManager.tap()
                    },
                    onHazardHit = {
                        soundManager.play(GameSound.GAME_OVER)
                        hapticManager.error()
                        repository.saveScore(_state.value.score)
                    },
                    onWin = {
                        soundManager.play(GameSound.WIN)
                        hapticManager.success()
                        repository.saveScore(_state.value.score)
                        repository.saveLevel(_state.value.currentLevel + 1)
                    }
                )
                _state.value = nextState
            }
        }
    }

    fun restart() {
        stopGameLoop()
        val level = _state.value.currentLevel
        _state.value = HelixJumpState(
            ball = HelixBall(),
            towerRotation = 0f,
            floors = engine.generateFloors(level),
            score = 0,
            bestScore = repository.bestScore.value,
            currentLevel = level
        )
        startGame()
    }

    fun nextLevel() {
        stopGameLoop()
        val newLevel = _state.value.currentLevel + 1
        _state.value = HelixJumpState(
            ball = HelixBall(),
            towerRotation = 0f,
            floors = engine.generateFloors(newLevel),
            score = _state.value.score,
            bestScore = repository.bestScore.value,
            currentLevel = newLevel
        )
        startGame()
    }

    fun stopGameLoop() {
        loopJob?.cancel()
        loopJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
    }
}
