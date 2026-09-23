package com.example.sandloop.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sandloop.engine.SandLoopEngine
import com.example.sandloop.engine.SandLoopLevels
import com.example.sandloop.model.SandLoopLevel
import com.example.sandloop.model.SandLoopState
import com.example.sandloop.repository.SandLoopRepository
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

class SandLoopViewModel(
    private val repository: SandLoopRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {
    val engine = SandLoopEngine()
    private var loopJob: Job? = null

    private val _currentLevelIndex = MutableStateFlow(0)
    val currentLevelIndex: StateFlow<Int> = _currentLevelIndex.asStateFlow()

    private val _state = MutableStateFlow(SandLoopState())
    val state: StateFlow<SandLoopState> = _state.asStateFlow()

    val currentLevel: SandLoopLevel
        get() = SandLoopLevels.levels[_currentLevelIndex.value.coerceIn(0, SandLoopLevels.levels.size - 1)]

    fun startLevel(index: Int) {
        val safeIndex = index.coerceIn(0, SandLoopLevels.levels.size - 1)
        _currentLevelIndex.value = safeIndex
        val level = SandLoopLevels.levels[safeIndex]
        engine.reset(level)
        _state.value = SandLoopState(
            levelNumber = level.levelNumber,
            collectedCount = 0,
            targetCount = level.targetCount,
            isSpawning = true,
            isWon = false,
            isGameOver = false
        )
        startGameLoop()
    }

    fun carve(pos: Offset) {
        engine.carve(pos)
        hapticManager.light()
    }

    fun startGameLoop() {
        stopGameLoop()
        loopJob = viewModelScope.launch {
            val level = currentLevel
            while (isActive) {
                delay(20L) // 50fps
                if (_state.value.isWon || _state.value.isGameOver) break

                val newCollected = engine.update(
                    level = level,
                    spawnAllowed = _state.value.isSpawning,
                    onParticleCollected = {
                        soundManager.play(GameSound.POUR)
                        hapticManager.tap()
                    }
                )

                if (newCollected > 0) {
                    _state.update { cur ->
                        val total = cur.collectedCount + newCollected
                        val won = total >= cur.targetCount
                        if (won) {
                            soundManager.play(GameSound.WIN)
                            hapticManager.success()
                            repository.saveLevel(level.levelNumber + 1)
                        }
                        cur.copy(collectedCount = total, isWon = won)
                    }
                }
            }
        }
    }

    fun restart() {
        startLevel(_currentLevelIndex.value)
        soundManager.play(GameSound.BUTTON)
    }

    fun nextLevel() {
        if (_currentLevelIndex.value < SandLoopLevels.levels.size - 1) {
            startLevel(_currentLevelIndex.value + 1)
        }
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
