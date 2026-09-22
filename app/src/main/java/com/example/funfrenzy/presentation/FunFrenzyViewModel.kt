package com.example.funfrenzy.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.funfrenzy.core.engine.FunFrenzyEngine
import com.example.funfrenzy.core.level.FunFrenzyLevelManager
import com.example.funfrenzy.core.model.*
import com.example.funfrenzy.core.repository.FunFrenzyRepository
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

class FunFrenzyViewModel(
    val repository: FunFrenzyRepository,
    val soundManager: SoundManager,
    val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(FunFrenzyGameState())
    val gameState: StateFlow<FunFrenzyGameState> = _gameState.asStateFlow()

    private var currentLevelConfig: FunFrenzyLevelConfig = FunFrenzyLevelManager.getLevel(1)
    private var gameLoopJob: Job? = null

    val soundEnabledFlow = repository.soundEnabledFlow
    val hapticEnabledFlow = repository.hapticEnabledFlow
    val highestLevelFlow = repository.highestLevelFlow
    val progressFlow = repository.progressFlow
    val completedLevelsFlow = repository.completedLevelsFlow

    init {
        startLevel(1)
    }

    fun startLevel(levelNumber: Int) {
        gameLoopJob?.cancel()
        currentLevelConfig = FunFrenzyLevelManager.getLevel(levelNumber)

        val firstSpec = currentLevelConfig.microGames[0]
        val firstSubState = FunFrenzyEngine.initSubState(firstSpec.type, System.currentTimeMillis())

        _gameState.value = FunFrenzyGameState(
            levelNumber = levelNumber,
            microIndex = 0,
            totalMicroGames = currentLevelConfig.microGames.size,
            currentSpec = firstSpec,
            timeLeftSec = firstSpec.durationSec,
            totalDurationSec = firstSpec.durationSec,
            lives = 3,
            maxLives = 3,
            subState = firstSubState,
            microResult = MicroResult.PENDING,
            isIntermission = false,
            isStageWon = false,
            isStageOver = false,
            stars = 0
        )

        startGameLoop()
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            val stepSec = 0.016f
            while (isActive) {
                tick(stepSec)
                delay(16)
            }
        }
    }

    private fun tick(dt: Float) {
        val current = _gameState.value
        if (current.isIntermission || current.isStageWon || current.isStageOver) return

        val newTime = (current.timeLeftSec - dt).coerceAtLeast(0f)

        // Step dynamic microgames (falling gem, rocks, needle)
        val (newSubState, dynamicResult) = FunFrenzyEngine.stepDynamic(
            type = current.currentSpec.type,
            state = current.subState,
            dt = dt,
            speedMult = currentLevelConfig.speedMultiplier
        )

        if (dynamicResult == MicroResult.SUCCESS) {
            handleMicroResult(MicroResult.SUCCESS, newSubState)
            return
        } else if (dynamicResult == MicroResult.FAIL) {
            handleMicroResult(MicroResult.FAIL, newSubState)
            return
        }

        // Check if time expired
        if (newTime <= 0f) {
            // For dodge rocks, surviving till time expires IS success!
            if (current.currentSpec.type == MicroGameType.DODGE_ROCKS) {
                handleMicroResult(MicroResult.SUCCESS, newSubState)
            } else {
                handleMicroResult(MicroResult.FAIL, newSubState)
            }
            return
        }

        _gameState.value = current.copy(
            timeLeftSec = newTime,
            subState = newSubState
        )
    }

    private fun handleMicroResult(result: MicroResult, finalSubState: MicroSubState) {
        val current = _gameState.value
        if (current.microResult != MicroResult.PENDING) return

        val newLives = if (result == MicroResult.FAIL) (current.lives - 1).coerceAtLeast(0) else current.lives

        if (result == MicroResult.SUCCESS) {
            if (soundEnabledFlow.value) soundManager.play(GameSound.LEVEL_COMPLETE)
            if (hapticEnabledFlow.value) hapticManager.success()
        } else {
            if (soundEnabledFlow.value) soundManager.play(GameSound.INVALID)
            if (hapticEnabledFlow.value) hapticManager.error()
        }

        _gameState.value = current.copy(
            microResult = result,
            lives = newLives,
            subState = finalSubState,
            isIntermission = true
        )

        viewModelScope.launch {
            delay(1200)
            advanceAfterMicroGame()
        }
    }

    private fun advanceAfterMicroGame() {
        val current = _gameState.value
        if (current.lives <= 0) {
            // Stage Defeat
            _gameState.value = current.copy(isStageOver = true, isIntermission = false)
            return
        }

        val nextIndex = current.microIndex + 1
        if (nextIndex >= current.totalMicroGames) {
            // Stage Victory!
            val stars = when (current.lives) {
                3 -> 3
                2 -> 2
                else -> 1
            }
            _gameState.value = current.copy(
                isStageWon = true,
                isIntermission = false,
                stars = stars
            )
            if (soundEnabledFlow.value) soundManager.play(GameSound.WIN)
            viewModelScope.launch {
                repository.saveLevelCompletion(current.levelNumber, stars)
            }
            return
        }

        // Next microgame in stage
        val nextSpec = currentLevelConfig.microGames[nextIndex]
        val nextSubState = FunFrenzyEngine.initSubState(nextSpec.type, System.currentTimeMillis() + nextIndex)

        _gameState.value = current.copy(
            microIndex = nextIndex,
            currentSpec = nextSpec,
            timeLeftSec = nextSpec.durationSec,
            totalDurationSec = nextSpec.durationSec,
            subState = nextSubState,
            microResult = MicroResult.PENDING,
            isIntermission = false
        )
    }

    // User interactions
    fun onTapRush() {
        val current = _gameState.value
        if (current.isIntermission || current.currentSpec.type != MicroGameType.TAP_RUSH) return

        val newTaps = current.subState.currentTaps + 1
        val req = current.subState.requiredTaps
        if (soundEnabledFlow.value) soundManager.play(GameSound.KNIFE_THROW)
        if (hapticEnabledFlow.value) hapticManager.light()

        val updatedSub = current.subState.copy(currentTaps = newTaps)
        if (newTaps >= req) {
            handleMicroResult(MicroResult.SUCCESS, updatedSub)
        } else {
            _gameState.value = current.copy(subState = updatedSub)
        }
    }

    fun onBucketDrag(x: Float) {
        val current = _gameState.value
        if (current.isIntermission || current.currentSpec.type != MicroGameType.CATCH_FALLING) return
        _gameState.value = current.copy(
            subState = current.subState.copy(bucketX = x.coerceIn(0.1f, 0.9f))
        )
    }

    fun onPopBalloon(id: Int) {
        val current = _gameState.value
        if (current.isIntermission || current.currentSpec.type != MicroGameType.POP_BALLOONS) return

        val balloons = current.subState.balloons.map {
            if (it.id == id && !it.isPopped) it.copy(isPopped = true) else it
        }
        val popped = balloons.count { it.isPopped }
        if (soundEnabledFlow.value) soundManager.play(GameSound.APPLE)
        if (hapticEnabledFlow.value) hapticManager.light()

        val updatedSub = current.subState.copy(balloons = balloons, poppedCount = popped)
        if (popped >= current.subState.requiredPops) {
            handleMicroResult(MicroResult.SUCCESS, updatedSub)
        } else {
            _gameState.value = current.copy(subState = updatedSub)
        }
    }

    fun onDodgeMove(x: Float) {
        val current = _gameState.value
        if (current.isIntermission || current.currentSpec.type != MicroGameType.DODGE_ROCKS) return
        _gameState.value = current.copy(
            subState = current.subState.copy(playerX = x.coerceIn(0.1f, 0.9f))
        )
    }

    fun onStopNeedle() {
        val current = _gameState.value
        if (current.isIntermission || current.currentSpec.type != MicroGameType.STOP_NEEDLE) return
        if (current.subState.isNeedleStopped) return

        val stoppedSub = current.subState.copy(isNeedleStopped = true)
        val angle = (stoppedSub.needleAngle % 360f + 360f) % 360f
        val inZone = angle >= stoppedSub.targetZoneStartAngle && angle <= stoppedSub.targetZoneEndAngle
        handleMicroResult(if (inZone) MicroResult.SUCCESS else MicroResult.FAIL, stoppedSub)
    }

    fun onCutWire(colorName: String) {
        val current = _gameState.value
        if (current.isIntermission || current.currentSpec.type != MicroGameType.CUT_WIRE) return

        val wires = current.subState.wires.map {
            if (it.colorName == colorName) it.copy(isCut = true) else it
        }
        val updatedSub = current.subState.copy(wires = wires)
        val isCorrect = colorName == current.subState.targetWireColorName
        handleMicroResult(if (isCorrect) MicroResult.SUCCESS else MicroResult.FAIL, updatedSub)
    }

    fun onSelectOddItem(index: Int) {
        val current = _gameState.value
        if (current.isIntermission || current.currentSpec.type != MicroGameType.FIND_ODD_ONE) return

        val isCorrect = index == current.subState.oddIndex
        val updatedSub = current.subState.copy(selectedIndex = index)
        handleMicroResult(if (isCorrect) MicroResult.SUCCESS else MicroResult.FAIL, updatedSub)
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
    }

    class Factory(
        private val repository: FunFrenzyRepository,
        private val soundManager: SoundManager,
        private val hapticManager: HapticManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FunFrenzyViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
