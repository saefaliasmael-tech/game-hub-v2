package com.example.protectsheep.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.protectsheep.core.engine.ProtectSheepEngine
import com.example.protectsheep.core.level.ProtectSheepLevelManager
import com.example.protectsheep.core.model.Bee
import com.example.protectsheep.core.model.ProtectSheepGameState
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
import kotlin.random.Random

class ProtectSheepViewModel(
    val repository: ProtectSheepRepository,
    val soundManager: SoundManager,
    val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(ProtectSheepGameState())
    val gameState: StateFlow<ProtectSheepGameState> = _gameState.asStateFlow()

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

        val config = ProtectSheepLevelManager.getLevel(levelNumber)

        _gameState.value = ProtectSheepGameState(
            levelNumber = levelNumber,
            sheepList = config.sheepList,
            hives = config.hives,
            hazards = config.hazards,
            bees = emptyList(),
            drawnPoints = emptyList(),
            maxInk = config.maxInk,
            usedInk = 0f,
            isSimulating = false,
            survivalTimeLeftSec = 10f,
            isWon = false,
            isGameOver = false,
            stars = 0
        )
    }

    fun onDrawingStart(point: Offset) {
        val current = _gameState.value
        if (current.isSimulating || current.isWon || current.isGameOver) return

        _gameState.value = current.copy(drawnPoints = listOf(point), usedInk = 0f)
    }

    fun onDrawingMove(point: Offset) {
        val current = _gameState.value
        if (current.isSimulating || current.isWon || current.isGameOver) return

        val pts = current.drawnPoints.toMutableList()
        pts.add(point)

        val len = ProtectSheepEngine.calculatePathLength(pts)
        if (len <= current.maxInk) {
            _gameState.value = current.copy(drawnPoints = pts, usedInk = len)
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

    private fun startSimulation() {
        val current = _gameState.value
        if (current.isSimulating) return

        // Spawn initial bees from hives
        val bees = mutableListOf<Bee>()
        val rng = Random(42)
        for (hive in current.hives) {
            for (i in 0 until hive.beeCount) {
                val offsetAngle = rng.nextFloat() * 6.28f
                val dist = 0.03f + rng.nextFloat() * 0.04f
                val bx = hive.x + kotlin.math.cos(offsetAngle) * dist
                val by = hive.y + kotlin.math.sin(offsetAngle) * dist
                bees.add(Bee(bx, by, 0f, 0f))
            }
        }

        _gameState.value = current.copy(
            bees = bees,
            isSimulating = true,
            survivalTimeLeftSec = 10f
        )

        if (soundEnabledFlow.value) soundManager.play(GameSound.KNIFE_THROW)

        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            val stepSec = 0.016f
            while (isActive) {
                updatePhysics(stepSec)
                delay(16)
            }
        }
    }

    private fun updatePhysics(dt: Float) {
        val current = _gameState.value
        if (current.isWon || current.isGameOver) return

        val newTime = (current.survivalTimeLeftSec - dt).coerceAtLeast(0f)

        val (updatedBees, anyStung) = ProtectSheepEngine.stepBees(
            bees = current.bees,
            sheepList = current.sheepList,
            drawnPoints = current.drawnPoints,
            hazards = current.hazards,
            dt = dt
        )

        var isWon = false
        var isGameOver = false
        var stars = current.stars

        if (anyStung) {
            isGameOver = true
            if (soundEnabledFlow.value) soundManager.play(GameSound.INVALID)
            if (hapticEnabledFlow.value) hapticManager.error()
            simulationJob?.cancel()
        } else if (newTime <= 0f) {
            isWon = true
            stars = ProtectSheepEngine.calculateStars(current.inkRemainingRatio)
            if (soundEnabledFlow.value) soundManager.play(GameSound.WIN)
            if (hapticEnabledFlow.value) hapticManager.success()
            simulationJob?.cancel()

            viewModelScope.launch {
                repository.saveLevelCompletion(current.levelNumber, stars)
            }
        }

        _gameState.value = current.copy(
            bees = updatedBees,
            survivalTimeLeftSec = newTime,
            isWon = isWon,
            isGameOver = isGameOver,
            stars = stars
        )
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
