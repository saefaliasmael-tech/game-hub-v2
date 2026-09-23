package com.example.tombofthemask.presentation

import androidx.lifecycle.ViewModel
import com.example.tombofthemask.engine.MaskLevels
import com.example.tombofthemask.engine.TombOfTheMaskEngine
import com.example.tombofthemask.model.MaskLevel
import com.example.tombofthemask.model.TombOfTheMaskState
import com.example.tombofthemask.repository.TombOfTheMaskRepository
import com.example.watersort.core.audio.GameSound
import com.example.watersort.core.audio.HapticManager
import com.example.watersort.core.audio.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TombOfTheMaskViewModel(
    private val repository: TombOfTheMaskRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {
    private val engine = TombOfTheMaskEngine()

    private val _currentLevelIndex = MutableStateFlow(0)
    val currentLevelIndex: StateFlow<Int> = _currentLevelIndex.asStateFlow()

    private val _state = MutableStateFlow(engine.initState(MaskLevels.levels[0]))
    val state: StateFlow<TombOfTheMaskState> = _state.asStateFlow()

    val currentLevel: MaskLevel
        get() = MaskLevels.levels[_currentLevelIndex.value.coerceIn(0, MaskLevels.levels.size - 1)]

    fun startLevel(index: Int) {
        val safeIndex = index.coerceIn(0, MaskLevels.levels.size - 1)
        _currentLevelIndex.value = safeIndex
        _state.value = engine.initState(MaskLevels.levels[safeIndex])
    }

    fun dash(dx: Int, dy: Int) {
        val current = _state.value
        if (current.isGameOver || current.isWon) return

        val next = engine.dash(
            state = current,
            level = currentLevel,
            dx = dx,
            dy = dy,
            onDotCollected = {
                soundManager.play(GameSound.SELECT)
                hapticManager.light()
            },
            onCoinCollected = {
                soundManager.play(GameSound.COIN)
                hapticManager.medium()
            }
        )

        if (next != current) {
            if (next.isWon) {
                soundManager.play(GameSound.WIN)
                hapticManager.success()
                repository.saveLevel(currentLevel.levelNumber + 1)
            } else if (next.isGameOver) {
                soundManager.play(GameSound.GAME_OVER)
                hapticManager.error()
            }
            _state.value = next
        }
    }

    fun restart() {
        startLevel(_currentLevelIndex.value)
        soundManager.play(GameSound.BUTTON)
    }

    fun nextLevel() {
        if (_currentLevelIndex.value < MaskLevels.levels.size - 1) {
            startLevel(_currentLevelIndex.value + 1)
        }
    }
}
