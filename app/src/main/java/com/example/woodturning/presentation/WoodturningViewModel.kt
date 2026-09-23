package com.example.woodturning.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watersort.core.audio.GameSound
import com.example.watersort.core.audio.HapticManager
import com.example.watersort.core.audio.SoundManager
import com.example.woodturning.engine.WoodLevels
import com.example.woodturning.engine.WoodturningEngine
import com.example.woodturning.model.WoodLevel
import com.example.woodturning.model.WoodturningState
import com.example.woodturning.repository.WoodturningRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WoodturningViewModel(
    private val repository: WoodturningRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {
    val engine = WoodturningEngine()
    private var particleJob: Job? = null

    private val _currentLevelIndex = MutableStateFlow(0)
    val currentLevelIndex: StateFlow<Int> = _currentLevelIndex.asStateFlow()

    private val _state = MutableStateFlow(engine.initState(WoodLevels.levels[0]))
    val state: StateFlow<WoodturningState> = _state.asStateFlow()

    val currentLevel: WoodLevel
        get() = WoodLevels.levels[_currentLevelIndex.value.coerceIn(0, WoodLevels.levels.size - 1)]

    fun startLevel(index: Int) {
        val safeIndex = index.coerceIn(0, WoodLevels.levels.size - 1)
        _currentLevelIndex.value = safeIndex
        _state.value = engine.initState(WoodLevels.levels[safeIndex])
        startParticleLoop()
    }

    fun carve(normX: Float, normDepth: Float) {
        if (_state.value.isFinished) return
        val next = engine.carve(_state.value, normX, normDepth) {
            hapticManager.light()
        }
        _state.value = next
    }

    fun finishPiece() {
        val acc = _state.value.accuracy
        val stars = when {
            acc >= 0.95f -> 3
            acc >= 0.88f -> 2
            else -> 1
        }
        soundManager.play(GameSound.WIN)
        hapticManager.success()
        repository.saveLevel(currentLevel.levelNumber + 1)
        _state.value = _state.value.copy(isFinished = true, stars = stars)
    }

    fun startParticleLoop() {
        particleJob?.cancel()
        particleJob = viewModelScope.launch {
            while (isActive) {
                delay(30L)
                engine.updateParticles()
            }
        }
    }

    fun restart() {
        startLevel(_currentLevelIndex.value)
        soundManager.play(GameSound.BUTTON)
    }

    fun nextLevel() {
        if (_currentLevelIndex.value < WoodLevels.levels.size - 1) {
            startLevel(_currentLevelIndex.value + 1)
        }
    }

    fun stopLoop() {
        particleJob?.cancel()
        particleJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopLoop()
    }
}
