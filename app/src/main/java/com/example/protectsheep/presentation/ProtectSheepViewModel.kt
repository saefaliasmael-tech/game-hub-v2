package com.example.protectsheep.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.protectsheep.core.engine.ProtectSheepPhysicsEngine
import com.example.protectsheep.core.level.ProtectSheepLevelManager
import com.example.protectsheep.core.model.BarrierStroke
import com.example.protectsheep.core.model.ProtectSheepLevelConfig
import com.example.protectsheep.core.model.ProtectSheepState
import com.example.protectsheep.core.model.SheepGamePhase
import com.example.protectsheep.core.repository.ProtectSheepRepository
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
import kotlin.math.sqrt

class ProtectSheepViewModel(
    private val repository: ProtectSheepRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(ProtectSheepState())
    val gameState: StateFlow<ProtectSheepState> = _gameState.asStateFlow()

    val soundEnabledFlow = repository.soundEnabledFlow
    val hapticEnabledFlow = repository.hapticEnabledFlow
    val progressFlow = repository.progressFlow
    val completedLevelsFlow = repository.completedLevelsFlow

    private var currentConfig: ProtectSheepLevelConfig = ProtectSheepLevelManager.getLevel(1)
    private var simulationJob: Job? = null

    fun stopSimulation() {
        simulationJob?.cancel()
    }

    fun startLevel(levelNumber: Int) {
        stopSimulation()
        val config = ProtectSheepLevelManager.getLevel(levelNumber)
        currentConfig = config

        _gameState.value = ProtectSheepState(
            levelNumber = levelNumber,
            phase = SheepGamePhase.DRAWING_BARRIER,
            strokes = emptyList(),
            currentStrokePoints = emptyList(),
            totalInkUsed = 0f,
            maxInkLength = config.maxInkLength,
            sheepList = config.sheepList.map { it.copy() },
            wolves = config.wolves.map { it.copy() },
            timeRemainingSeconds = config.surviveSeconds,
            totalSurviveSeconds = config.surviveSeconds,
            stars = 0
        )
    }

    fun onTouchDown(point: Offset) {
        val state = _gameState.value
        if (state.phase != SheepGamePhase.DRAWING_BARRIER) return
        if (state.totalInkUsed >= state.maxInkLength) return

        _gameState.value = state.copy(
            currentStrokePoints = listOf(point)
        )
    }

    fun onTouchMove(point: Offset) {
        val state = _gameState.value
        if (state.phase != SheepGamePhase.DRAWING_BARRIER) return
        if (state.currentStrokePoints.isEmpty()) return

        val lastPoint = state.currentStrokePoints.last()
        val dx = point.x - lastPoint.x
        val dy = point.y - lastPoint.y
        val dist = sqrt(dx * dx + dy * dy)

        if (dist < 4f) return

        val newTotalInk = state.totalInkUsed + dist
        if (newTotalInk > state.maxInkLength) return

        val updatedPoints = state.currentStrokePoints + point
        _gameState.value = state.copy(
            currentStrokePoints = updatedPoints,
            totalInkUsed = newTotalInk
        )
    }

    fun onTouchUp() {
        val state = _gameState.value
        if (state.phase != SheepGamePhase.DRAWING_BARRIER) return

        if (state.currentStrokePoints.size >= 2) {
            var strokeLen = 0f
            for (i in 0 until state.currentStrokePoints.size - 1) {
                val p1 = state.currentStrokePoints[i]
                val p2 = state.currentStrokePoints[i + 1]
                val dx = p2.x - p1.x
                val dy = p2.y - p1.y
                strokeLen += sqrt(dx * dx + dy * dy)
            }
            val stroke = BarrierStroke(state.currentStrokePoints, strokeLen)
            _gameState.value = state.copy(
                strokes = state.strokes + stroke,
                currentStrokePoints = emptyList()
            )
            if (hapticEnabledFlow.value) hapticManager.tap()
        } else {
            _gameState.value = state.copy(currentStrokePoints = emptyList())
        }
    }

    fun undoLastStroke() {
        val state = _gameState.value
        if (state.phase != SheepGamePhase.DRAWING_BARRIER || state.strokes.isEmpty()) return

        val last = state.strokes.last()
        _gameState.value = state.copy(
            strokes = state.strokes.dropLast(1),
            totalInkUsed = (state.totalInkUsed - last.length).coerceAtLeast(0f)
        )
        if (hapticEnabledFlow.value) hapticManager.tap()
    }

    fun startSurviving() {
        val state = _gameState.value
        if (state.phase != SheepGamePhase.DRAWING_BARRIER) return

        _gameState.value = state.copy(
            phase = SheepGamePhase.SURVIVING
        )

        if (soundEnabledFlow.value) soundManager.play(GameSound.SELECT)

        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            val dt = 0.016f
            while (isActive) {
                val currentState = _gameState.value
                if (currentState.phase != SheepGamePhase.SURVIVING) break

                val newTime = currentState.timeRemainingSeconds - dt
                if (newTime <= 0f) {
                    handleVictory(currentState)
                    break
                }

                var sheepEaten = false
                ProtectSheepPhysicsEngine.updateSimulation(
                    wolves = currentState.wolves,
                    sheepList = currentState.sheepList,
                    barriers = currentState.strokes,
                    onSheepTouched = { sheep ->
                        sheepEaten = true
                    }
                )

                if (sheepEaten) {
                    handleDefeat(currentState)
                    break
                }

                _gameState.value = currentState.copy(
                    timeRemainingSeconds = newTime,
                    wolves = currentState.wolves.toList(),
                    sheepList = currentState.sheepList.toList()
                )

                delay(16)
            }
        }
    }

    private fun handleVictory(state: ProtectSheepState) {
        val inkRemainingRatio = 1f - (state.totalInkUsed / state.maxInkLength).coerceIn(0f, 1f)
        val stars = when {
            inkRemainingRatio >= 0.55f -> 3
            inkRemainingRatio >= 0.25f -> 2
            else -> 1
        }

        _gameState.value = state.copy(
            phase = SheepGamePhase.WON,
            timeRemainingSeconds = 0f,
            stars = stars
        )

        if (soundEnabledFlow.value) soundManager.play(GameSound.WIN)
        if (hapticEnabledFlow.value) hapticManager.success()

        viewModelScope.launch {
            repository.saveLevelCompletion(state.levelNumber, state.totalSurviveSeconds.toInt(), stars)
        }
    }

    private fun handleDefeat(state: ProtectSheepState) {
        _gameState.value = state.copy(
            phase = SheepGamePhase.LOST
        )

        if (soundEnabledFlow.value) soundManager.play(GameSound.GAME_OVER)
        if (hapticEnabledFlow.value) hapticManager.error()
    }

    fun restartLevel() {
        startLevel(_gameState.value.levelNumber)
    }

    fun nextLevel() {
        val next = (_gameState.value.levelNumber + 1).coerceAtMost(ProtectSheepLevelManager.TOTAL_LEVELS)
        startLevel(next)
    }

    fun toggleSound() {
        val current = soundEnabledFlow.value
        repository.setSoundEnabled(!current)
    }

    fun toggleHaptic() {
        val current = hapticEnabledFlow.value
        repository.setHapticEnabled(!current)
    }

    class Factory(
        private val repository: ProtectSheepRepository,
        private val soundManager: SoundManager,
        private val hapticManager: HapticManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProtectSheepViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
