package com.example.colormaze3d.presentation

import androidx.lifecycle.ViewModel
import com.example.colormaze3d.engine.ColorMazeEngine
import com.example.colormaze3d.engine.ColorMazeLevels
import com.example.colormaze3d.model.ColorMazeLevel
import com.example.colormaze3d.model.ColorMazeState
import com.example.colormaze3d.repository.ColorMazeRepository
import com.example.watersort.core.audio.GameSound
import com.example.watersort.core.audio.HapticManager
import com.example.watersort.core.audio.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ColorMazeViewModel(
    private val repository: ColorMazeRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {
    private val engine = ColorMazeEngine()

    private val _currentLevelIndex = MutableStateFlow(0)
    val currentLevelIndex: StateFlow<Int> = _currentLevelIndex.asStateFlow()

    private val _state = MutableStateFlow(engine.initState(ColorMazeLevels.levels[0]))
    val state: StateFlow<ColorMazeState> = _state.asStateFlow()

    val currentLevel: ColorMazeLevel
        get() = ColorMazeLevels.levels[_currentLevelIndex.value.coerceIn(0, ColorMazeLevels.levels.size - 1)]

    fun startLevel(levelIndex: Int) {
        val safeIndex = levelIndex.coerceIn(0, ColorMazeLevels.levels.size - 1)
        _currentLevelIndex.value = safeIndex
        _state.value = engine.initState(ColorMazeLevels.levels[safeIndex])
    }

    fun swipe(dx: Int, dy: Int) {
        val current = _state.value
        if (current.isWon) return

        val next = engine.roll(current, currentLevel, dx, dy)
        if (next != current) {
            hapticManager.light()
            if (next.isWon) {
                soundManager.play(GameSound.WIN)
                hapticManager.success()
                repository.saveLevel(currentLevel.levelNumber + 1)
            } else {
                soundManager.play(GameSound.COLOR_CHANGE)
            }
            _state.value = next
        }
    }

    fun restart() {
        _state.value = engine.initState(currentLevel)
        soundManager.play(GameSound.BUTTON)
    }

    fun nextLevel() {
        if (_currentLevelIndex.value < ColorMazeLevels.levels.size - 1) {
            startLevel(_currentLevelIndex.value + 1)
        }
    }
}
