package com.example.roperescue.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.roperescue.core.engine.RopeRescueEngine
import com.example.roperescue.core.level.RopeRescueLevelManager
import com.example.roperescue.core.model.RescueGameState
import com.example.roperescue.core.model.RescueLevelConfig
import com.example.roperescue.core.model.Zipliner
import com.example.roperescue.core.repository.RopeRescueRepository
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

class RopeRescueViewModel(
    val repository: RopeRescueRepository,
    val soundManager: SoundManager,
    val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(RescueGameState())
    val gameState: StateFlow<RescueGameState> = _gameState.asStateFlow()

    private var activeConfig: RescueLevelConfig = RopeRescueLevelManager.getLevel(1)
    private var gameLoopJob: Job? = null
    private var spawnJob: Job? = null

    val soundEnabledFlow = repository.soundEnabledFlow
    val hapticEnabledFlow = repository.hapticEnabledFlow
    val highestLevelFlow = repository.highestLevelFlow
    val progressFlow = repository.progressFlow
    val completedLevelsFlow = repository.completedLevelsFlow

    // Levels and game loop are started on demand when the user or screen starts a level

    fun stopGameLoop() {
        gameLoopJob?.cancel()
        spawnJob?.cancel()
    }

    fun startLevel(levelNumber: Int) {
        gameLoopJob?.cancel()
        spawnJob?.cancel()

        val config = RopeRescueLevelManager.getLevel(levelNumber)
        activeConfig = config

        val start = Offset(config.startX, config.startY)
        val target = Offset(config.targetX, config.targetY)

        _gameState.value = RescueGameState(
            levelNumber = levelNumber,
            startPos = start,
            targetPos = target,
            wheels = config.wheels,
            hazards = config.hazards,
            ropePivots = listOf(start),
            currentRopeEnd = start,
            isRopeAttached = false,
            isDeploying = false,
            totalHostages = config.totalHostages,
            remainingAtStart = config.totalHostages,
            zipliners = emptyList(),
            savedCount = 0,
            lostCount = 0,
            requiredSaved = config.requiredSaved,
            isWon = false,
            isGameOver = false
        )

        startGameLoop()
    }

    fun onDragRope(xPercent: Float, yPercent: Float) {
        val current = _gameState.value
        if (current.isRopeAttached || current.isWon || current.isGameOver) return

        val touchPos = Offset(xPercent.coerceIn(0.05f, 0.95f), yPercent.coerceIn(0.05f, 0.95f))
        val (newPivots, isAttached) = RopeRescueEngine.updateRopePath(
            pivots = current.ropePivots,
            touchPos = touchPos,
            wheels = current.wheels,
            targetPos = current.targetPos
        )

        if (isAttached && !current.isRopeAttached) {
            if (soundEnabledFlow.value) soundManager.play(GameSound.SELECT)
            if (hapticEnabledFlow.value) hapticManager.medium()
        }

        _gameState.value = current.copy(
            ropePivots = newPivots,
            currentRopeEnd = if (isAttached) current.targetPos else touchPos,
            isRopeAttached = isAttached
        )
    }

    fun startDeploying() {
        val current = _gameState.value
        if (!current.isRopeAttached || current.isWon || current.isGameOver) return

        _gameState.value = current.copy(isDeploying = true)
        spawnJob?.cancel()
        spawnJob = viewModelScope.launch {
            while (isActive && _gameState.value.isDeploying && _gameState.value.remainingAtStart > 0) {
                spawnZipliner()
                delay(300)
            }
        }
    }

    fun stopDeploying() {
        spawnJob?.cancel()
        _gameState.value = _gameState.value.copy(isDeploying = false)
    }

    private fun spawnZipliner() {
        val current = _gameState.value
        if (current.remainingAtStart <= 0) return

        val newZipliner = Zipliner(id = current.totalHostages - current.remainingAtStart + 1)
        if (soundEnabledFlow.value) soundManager.play(GameSound.BUTTON)

        _gameState.value = current.copy(
            remainingAtStart = current.remainingAtStart - 1,
            zipliners = current.zipliners + newZipliner
        )
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (isActive) {
                updateFrame()
                delay(16)
            }
        }
    }

    private fun updateFrame() {
        val current = _gameState.value
        if (current.isGameOver || current.isWon) return

        // Rotate hazard saw blades
        val updatedHazards = current.hazards.map {
            it.copy(rotation = (it.rotation + 6f) % 360f)
        }

        val fullRopePoints = current.ropePivots + if (current.isRopeAttached) current.targetPos else current.currentRopeEnd

        var newlySaved = 0
        var newlyLost = 0

        val updatedZipliners = current.zipliners.map { z ->
            if (!z.isAlive || z.isRescued) {
                z
            } else {
                val newProgress = z.progress + 0.012f
                val pos = RopeRescueEngine.getPointAlongPolyline(fullRopePoints, newProgress)

                if (RopeRescueEngine.checkHazardCollision(pos, updatedHazards)) {
                    newlyLost++
                    z.copy(progress = newProgress, isAlive = false)
                } else if (newProgress >= 1.0f) {
                    newlySaved++
                    z.copy(progress = 1.0f, isRescued = true)
                } else {
                    z.copy(progress = newProgress)
                }
            }
        }

        if (newlyLost > 0) {
            if (soundEnabledFlow.value) soundManager.play(GameSound.INVALID)
            if (hapticEnabledFlow.value) hapticManager.error()
        }
        if (newlySaved > 0) {
            if (soundEnabledFlow.value) soundManager.play(GameSound.SELECT)
            if (hapticEnabledFlow.value) hapticManager.light()
        }

        val totalSaved = current.savedCount + newlySaved
        val totalLost = current.lostCount + newlyLost

        var won = false
        var over = false

        // Check if all hostages have finished
        val allFinished = current.remainingAtStart == 0 &&
                updatedZipliners.isNotEmpty() &&
                updatedZipliners.all { !it.isAlive || it.isRescued }

        if (allFinished) {
            if (totalSaved >= current.requiredSaved) {
                won = true
                if (soundEnabledFlow.value) soundManager.play(GameSound.WIN)
                if (hapticEnabledFlow.value) hapticManager.success()
                viewModelScope.launch {
                    repository.saveLevelCompletion(current.levelNumber)
                }
            } else {
                over = true
                if (soundEnabledFlow.value) soundManager.play(GameSound.INVALID)
                if (hapticEnabledFlow.value) hapticManager.error()
            }
        }

        _gameState.value = current.copy(
            hazards = updatedHazards,
            zipliners = updatedZipliners,
            savedCount = totalSaved,
            lostCount = totalLost,
            isWon = won,
            isGameOver = over
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
        gameLoopJob?.cancel()
        spawnJob?.cancel()
    }

    class Factory(
        private val repository: RopeRescueRepository,
        private val soundManager: SoundManager,
        private val hapticManager: HapticManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RopeRescueViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
