package com.example.happyglass.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.happyglass.core.engine.HappyGlassPhysicsEngine
import com.example.happyglass.core.level.HappyGlassLevelManager
import com.example.happyglass.core.model.DrawnStroke
import com.example.happyglass.core.model.GamePhase
import com.example.happyglass.core.model.HappyGlassLevelConfig
import com.example.happyglass.core.model.HappyGlassState
import com.example.happyglass.core.model.WaterDrop
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
import kotlin.math.sqrt
import kotlin.random.Random

class HappyGlassViewModel(
    private val repository: HappyGlassRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(HappyGlassState())
    val gameState: StateFlow<HappyGlassState> = _gameState.asStateFlow()

    val soundEnabledFlow = repository.soundEnabledFlow
    val hapticEnabledFlow = repository.hapticEnabledFlow
    val progressFlow = repository.progressFlow
    val completedLevelsFlow = repository.completedLevelsFlow

    private var currentConfig: HappyGlassLevelConfig = HappyGlassLevelManager.getLevel(1)
    private var simulationJob: Job? = null
    private var spawnJob: Job? = null

    fun stopSimulation() {
        simulationJob?.cancel()
        spawnJob?.cancel()
    }

    fun startLevel(levelNumber: Int) {
        stopSimulation()
        val config = HappyGlassLevelManager.getLevel(levelNumber)
        currentConfig = config

        _gameState.value = HappyGlassState(
            levelNumber = levelNumber,
            phase = GamePhase.DRAWING,
            strokes = emptyList(),
            currentStrokePoints = emptyList(),
            totalInkUsed = 0f,
            maxInkLength = config.maxInkLength,
            drops = emptyList(),
            dropsInGlass = 0,
            dropsLost = 0,
            requiredDrops = config.requiredDrops,
            totalDropsToSpawn = config.faucet.totalDrops,
            spawnedDropsCount = 0,
            stars = 0,
            isSadGlass = true,
            waterLevelRatio = 0f
        )
    }

    fun onTouchDown(point: Offset) {
        val state = _gameState.value
        if (state.phase != GamePhase.DRAWING) return
        if (state.totalInkUsed >= state.maxInkLength) return

        _gameState.value = state.copy(
            currentStrokePoints = listOf(point)
        )
    }

    fun onTouchMove(point: Offset) {
        val state = _gameState.value
        if (state.phase != GamePhase.DRAWING) return
        if (state.currentStrokePoints.isEmpty()) return

        val lastPoint = state.currentStrokePoints.last()
        val dx = point.x - lastPoint.x
        val dy = point.y - lastPoint.y
        val dist = sqrt(dx * dx + dy * dy)

        if (dist < 4f) return // Minimum delta for smoothing

        val newTotalInk = state.totalInkUsed + dist
        if (newTotalInk > state.maxInkLength) return // Exceeded ink limit

        val updatedPoints = state.currentStrokePoints + point
        _gameState.value = state.copy(
            currentStrokePoints = updatedPoints,
            totalInkUsed = newTotalInk
        )
    }

    fun onTouchUp() {
        val state = _gameState.value
        if (state.phase != GamePhase.DRAWING) return
        if (state.currentStrokePoints.size >= 2) {
            var strokeLen = 0f
            for (i in 0 until state.currentStrokePoints.size - 1) {
                val p1 = state.currentStrokePoints[i]
                val p2 = state.currentStrokePoints[i + 1]
                val dx = p2.x - p1.x
                val dy = p2.y - p1.y
                strokeLen += sqrt(dx * dx + dy * dy)
            }
            val stroke = DrawnStroke(state.currentStrokePoints, strokeLen)
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
        if (state.phase != GamePhase.DRAWING || state.strokes.isEmpty()) return

        val last = state.strokes.last()
        _gameState.value = state.copy(
            strokes = state.strokes.dropLast(1),
            totalInkUsed = (state.totalInkUsed - last.length).coerceAtLeast(0f)
        )
        if (hapticEnabledFlow.value) hapticManager.tap()
    }

    fun startPouring() {
        val state = _gameState.value
        if (state.phase != GamePhase.DRAWING) return

        _gameState.value = state.copy(
            phase = GamePhase.POURING,
            drops = emptyList(),
            dropsInGlass = 0,
            dropsLost = 0,
            spawnedDropsCount = 0
        )

        if (soundEnabledFlow.value) soundManager.play(GameSound.SELECT)
        startWaterSimulation()
    }

    private fun startWaterSimulation() {
        stopSimulation()

        // Spawning coroutine
        spawnJob = viewModelScope.launch {
            val faucet = currentConfig.faucet
            for (i in 0 until faucet.totalDrops) {
                if (!isActive) break
                val state = _gameState.value
                if (state.phase != GamePhase.POURING) break

                val jitterX = Random.nextFloat() * 10f - 5f
                val jitterVx = Random.nextFloat() * 0.4f - 0.2f
                val newDrop = WaterDrop(
                    x = faucet.x + jitterX,
                    y = faucet.y + 12f,
                    vx = jitterVx,
                    vy = 1.8f
                )

                _gameState.value = _gameState.value.copy(
                    drops = _gameState.value.drops + newDrop,
                    spawnedDropsCount = i + 1
                )
                delay(38)
            }
        }

        // Physics tick loop
        simulationJob = viewModelScope.launch {
            var settleFrames = 0
            while (isActive) {
                val state = _gameState.value
                if (state.phase != GamePhase.POURING) break

                val currentDrops = state.drops
                var inGlassCount = 0
                var lostCount = 0

                for (drop in currentDrops) {
                    HappyGlassPhysicsEngine.updateDrop(
                        drop = drop,
                        strokes = state.strokes,
                        glass = currentConfig.glass,
                        obstacles = currentConfig.obstacles
                    )
                    if (drop.inGlass) inGlassCount++
                    if (drop.isLost) lostCount++
                }

                val waterRatio = (inGlassCount.toFloat() / state.requiredDrops).coerceIn(0f, 1f)
                val isHappy = inGlassCount >= state.requiredDrops

                _gameState.value = state.copy(
                    drops = currentDrops,
                    dropsInGlass = inGlassCount,
                    dropsLost = lostCount,
                    waterLevelRatio = waterRatio,
                    isSadGlass = !isHappy
                )

                // Check end condition
                val allSpawned = state.spawnedDropsCount >= state.totalDropsToSpawn
                if (allSpawned) {
                    settleFrames++
                    if (settleFrames > 90 || inGlassCount >= state.requiredDrops + 10) {
                        evaluateLevelOutcome(inGlassCount, state)
                        break
                    }
                }

                delay(16)
            }
        }
    }

    private fun evaluateLevelOutcome(inGlassCount: Int, state: HappyGlassState) {
        val won = inGlassCount >= state.requiredDrops
        if (won) {
            val inkRemainingRatio = 1f - (state.totalInkUsed / state.maxInkLength).coerceIn(0f, 1f)
            val stars = when {
                inkRemainingRatio >= 0.55f -> 3
                inkRemainingRatio >= 0.25f -> 2
                else -> 1
            }

            _gameState.value = state.copy(
                phase = GamePhase.WON,
                stars = stars,
                isSadGlass = false
            )

            if (soundEnabledFlow.value) soundManager.play(GameSound.WIN)
            if (hapticEnabledFlow.value) hapticManager.success()

            viewModelScope.launch {
                repository.saveLevelCompletion(state.levelNumber, inGlassCount, stars)
            }
        } else {
            _gameState.value = state.copy(
                phase = GamePhase.LOST,
                isSadGlass = true
            )
            if (soundEnabledFlow.value) soundManager.play(GameSound.GAME_OVER)
            if (hapticEnabledFlow.value) hapticManager.error()
        }
    }

    fun restartLevel() {
        startLevel(_gameState.value.levelNumber)
    }

    fun nextLevel() {
        val next = (_gameState.value.levelNumber + 1).coerceAtMost(HappyGlassLevelManager.TOTAL_LEVELS)
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
