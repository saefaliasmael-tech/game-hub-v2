package com.example.happyglass.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.happyglass.core.engine.HappyGlassEngine
import com.example.happyglass.core.level.HappyGlassLevelManager
import com.example.happyglass.core.model.HappyGlassGameState
import com.example.happyglass.core.repository.HappyGlassRepository
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

class HappyGlassViewModel(
    val repository: HappyGlassRepository,
    val soundManager: SoundManager,
    val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(HappyGlassGameState())
    val gameState: StateFlow<HappyGlassGameState> = _gameState.asStateFlow()

    private var simulationJob: Job? = null

    val soundEnabledFlow = repository.soundEnabledFlow
    val hapticEnabledFlow = repository.hapticEnabledFlow
    val highestLevelFlow = repository.highestLevelFlow
    val progressFlow = repository.progressFlow
    val completedLevelsFlow = repository.completedLevelsFlow

    init {
        startLevel(1)
    }

    fun startLevel(levelNumber: Int) {
        simulationJob?.cancel()

        val config = HappyGlassLevelManager.getLevel(levelNumber)

        _gameState.value = HappyGlassGameState(
            levelNumber = levelNumber,
            tap = config.tap,
            glass = config.glass,
            obstacles = config.obstacles,
            maxInkLength = config.maxInkLength,
            usedInkLength = 0f,
            drawnPoints = emptyList(),
            waterParticles = emptyList(),
            isSimulating = false,
            dispensedCount = 0,
            waterInGlassCount = 0,
            isWon = false,
            isGameOver = false,
            stars = 0
        )
    }

    fun onDrawingStart(point: Offset) {
        val current = _gameState.value
        if (current.isSimulating || current.isWon || current.isGameOver) return

        _gameState.value = current.copy(drawnPoints = listOf(point), usedInkLength = 0f)
    }

    fun onDrawingMove(point: Offset) {
        val current = _gameState.value
        if (current.isSimulating || current.isWon || current.isGameOver) return

        val pts = current.drawnPoints.toMutableList()
        pts.add(point)

        val length = HappyGlassEngine.calculatePathLength(pts)
        if (length <= current.maxInkLength) {
            _gameState.value = current.copy(drawnPoints = pts, usedInkLength = length)
        }
    }

    fun onDrawingEnd() {
        val current = _gameState.value
        if (current.isSimulating || current.isWon || current.isGameOver) return

        if (current.drawnPoints.size >= 2) {
            startSimulation()
        }
    }

    fun clearDrawing() {
        val current = _gameState.value
        if (current.isWon) return
        startLevel(current.levelNumber)
    }

    fun startSimulation() {
        val current = _gameState.value
        if (current.isSimulating) return

        _gameState.value = current.copy(isSimulating = true)
        if (soundEnabledFlow.value) soundManager.play(GameSound.POUR)

        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            var checksAfterDispensed = 0
            while (isActive) {
                updatePhysics()

                val s = _gameState.value
                if (s.dispensedCount >= s.tap.totalWater) {
                    checksAfterDispensed++
                    // Allow 120 frames (approx 2 seconds) for remaining droplets to settle
                    if (checksAfterDispensed > 120 && !s.isWon && !s.isGameOver) {
                        checkFinalResult()
                    }
                }

                delay(16)
            }
        }
    }

    private fun updatePhysics() {
        val current = _gameState.value
        if (current.isWon || current.isGameOver) return

        val (newParticles, newDispensed, inGlassCount) = HappyGlassEngine.step(
            particles = current.waterParticles,
            tap = current.tap,
            glass = current.glass,
            obstacles = current.obstacles,
            drawnPoints = current.drawnPoints,
            dispensedCount = current.dispensedCount,
            isSimulating = current.isSimulating
        )

        // Check if glass just became full / happy
        val required = current.glass.requiredWater
        val wasWon = current.isWon
        var isWon = wasWon
        var stars = current.stars

        if (!wasWon && inGlassCount >= required) {
            isWon = true
            stars = HappyGlassEngine.calculateStars(current.inkRemainingRatio)
            if (soundEnabledFlow.value) soundManager.play(GameSound.WIN)
            if (hapticEnabledFlow.value) hapticManager.success()

            viewModelScope.launch {
                repository.saveLevelCompletion(current.levelNumber, stars)
            }
        }

        _gameState.value = current.copy(
            waterParticles = newParticles,
            dispensedCount = newDispensed,
            waterInGlassCount = inGlassCount,
            isWon = isWon,
            stars = stars
        )
    }

    private fun checkFinalResult() {
        val current = _gameState.value
        if (current.waterInGlassCount >= current.glass.requiredWater) {
            // Already won
        } else {
            // Defeat
            _gameState.value = current.copy(isGameOver = true)
            if (soundEnabledFlow.value) soundManager.play(GameSound.INVALID)
            if (hapticEnabledFlow.value) hapticManager.error()
        }
    }

    fun restartLevel() {
        startLevel(_gameState.value.levelNumber)
    }

    fun nextLevel() {
        startLevel(_gameState.value.levelNumber + 1)
    }

    fun toggleSound() {
        repository.setSoundEnabled(!soundEnabledFlow.value)
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }

    class Factory(
        private val repository: HappyGlassRepository,
        private val soundManager: SoundManager,
        private val hapticManager: HapticManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HappyGlassViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
