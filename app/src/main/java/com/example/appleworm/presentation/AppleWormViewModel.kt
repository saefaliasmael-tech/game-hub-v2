package com.example.appleworm.presentation

import androidx.lifecycle.ViewModel
import com.example.appleworm.engine.AppleWormEngine
import com.example.appleworm.engine.AppleWormLevels
import com.example.appleworm.model.AppleWormLevel
import com.example.appleworm.model.AppleWormState
import com.example.appleworm.model.Direction
import com.example.appleworm.repository.AppleWormRepository
import com.example.watersort.core.audio.GameSound
import com.example.watersort.core.audio.HapticManager
import com.example.watersort.core.audio.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppleWormViewModel(
    private val repository: AppleWormRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {
    private val engine = AppleWormEngine()

    private val _currentLevelIndex = MutableStateFlow(0)
    val currentLevelIndex: StateFlow<Int> = _currentLevelIndex.asStateFlow()

    private val _state = MutableStateFlow(engine.initState(AppleWormLevels.levels[0]))
    val state: StateFlow<AppleWormState> = _state.asStateFlow()

    val currentLevel: AppleWormLevel
        get() = AppleWormLevels.levels[_currentLevelIndex.value.coerceIn(0, AppleWormLevels.levels.size - 1)]

    fun startLevel(levelIndex: Int) {
        val safeIndex = levelIndex.coerceIn(0, AppleWormLevels.levels.size - 1)
        _currentLevelIndex.value = safeIndex
        _state.value = engine.initState(AppleWormLevels.levels[safeIndex])
    }

    fun move(direction: Direction) {
        val current = _state.value
        if (current.isWon || current.isGameOver) return

        val next = engine.move(current, currentLevel, direction)
        if (next != current) {
            hapticManager.tap()
            if (next.applesRemaining.size < current.applesRemaining.size) {
                soundManager.play(GameSound.APPLE)
            } else if (next.isWon) {
                soundManager.play(GameSound.WIN)
                hapticManager.success()
                repository.markLevelCompleted(currentLevel.levelNumber)
            } else if (next.isGameOver) {
                soundManager.play(GameSound.GAME_OVER)
                hapticManager.error()
            } else {
                soundManager.play(GameSound.SELECT)
            }
            _state.value = next
        }
    }

    fun restart() {
        _state.value = engine.initState(currentLevel)
        soundManager.play(GameSound.BUTTON)
    }

    fun nextLevel() {
        if (_currentLevelIndex.value < AppleWormLevels.levels.size - 1) {
            startLevel(_currentLevelIndex.value + 1)
        }
    }

    fun undo() {
        val updated = engine.undo(_state.value)
        _state.value = updated
        soundManager.play(GameSound.BUTTON)
    }
}
