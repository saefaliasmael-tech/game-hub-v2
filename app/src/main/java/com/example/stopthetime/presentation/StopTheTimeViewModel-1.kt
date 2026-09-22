package com.example.stopthetime.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stopthetime.core.engine.StopTheTimeEngine
import com.example.stopthetime.core.level.StopTheTimeLevelManager
import com.example.stopthetime.core.model.*
import com.example.stopthetime.core.repository.StopTheTimeRepository
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

class StopTheTimeViewModel(
    private val repository: StopTheTimeRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(StopTheTimeState())
    val gameState: StateFlow<StopTheTimeState> = _gameState.asStateFlow()

    val soundEnabledFlow = repository.soundEnabledFlow
    val hapticEnabledFlow = repository.hapticEnabledFlow
    val highestLevelFlow = repository.highestLevelFlow
    val highScoreFlow = repository.highScoreFlow
    val progressFlow = repository.progressFlow

    private var timerJob: Job? = null

    init {
        startCampaignLevel(1)
    }

    fun startCampaignLevel(levelNumber: Int) {
        timerJob?.cancel()
        val config = StopTheTimeLevelManager.getLevel(levelNumber)
        _gameState.value = StopTheTimeState(
            mode = StopGameMode.CAMPAIGN,
            phase = StopStatePhase.IDLE,
            levelNumber = config.levelNumber,
            difficulty = config.difficulty,
            targetTimeMs = config.targetTimeMs,
            currentTimeMs = 0L,
            stoppedTimeMs = 0L,
            differenceMs = 0L,
            accuracy = null,
            stars = 0,
            score = 0,
            combo = 0,
            isBlind = config.isBlind,
            blindHideAfterMs = config.blindHideAfterMs,
            isTimerHidden = false,
            attemptsLeft = config.maxAttempts,
            currentRound = 1,
            totalRounds = config.totalRounds
        )
    }

    fun startCustomGame(
        mode: StopGameMode,
        difficulty: StopDifficulty,
        customTarget: Long?,
        playerCount: Int
    ) {
        timerJob?.cancel()
        val target = customTarget ?: StopTheTimeEngine.generateRandomTargetMs()
        _gameState.value = StopTheTimeState(
            mode = mode,
            phase = StopStatePhase.IDLE,
            levelNumber = 1,
            difficulty = difficulty,
            targetTimeMs = target,
            currentTimeMs = 0L,
            stoppedTimeMs = 0L,
            differenceMs = 0L,
            accuracy = null,
            stars = 0,
            score = 0,
            combo = 0,
            isBlind = (difficulty >= StopDifficulty.HARD),
            blindHideAfterMs = if (difficulty >= StopDifficulty.HARD) 2000L else 0L,
            isTimerHidden = false,
            attemptsLeft = 3,
            currentPlayer = 1
        )
    }

    fun startTimer() {
        val state = _gameState.value
        if (state.phase == StopStatePhase.RUNNING) return

        timerJob?.cancel()
        _gameState.value = state.copy(
            phase = StopStatePhase.RUNNING,
            currentTimeMs = 0L,
            isTimerHidden = false,
            differenceMs = 0L,
            accuracy = null
        )

        if (soundEnabledFlow.value) soundManager.play(GameSound.SELECT)
        if (hapticEnabledFlow.value) hapticManager.tap()

        val startTime = System.currentTimeMillis()

        timerJob = viewModelScope.launch {
            while (isActive) {
                val elapsed = System.currentTimeMillis() - startTime
                val currentState = _gameState.value

                val hideTimer = currentState.isBlind && elapsed >= currentState.blindHideAfterMs

                _gameState.value = currentState.copy(
                    currentTimeMs = elapsed,
                    isTimerHidden = hideTimer
                )

                // Stop if exceeded max target + 5 seconds
                if (elapsed >= currentState.targetTimeMs + 5000L) {
                    stopTimer()
                    break
                }

                delay(16)
            }
        }
    }

    fun stopTimer() {
        val state = _gameState.value
        if (state.phase != StopStatePhase.RUNNING) return

        timerJob?.cancel()
        val stopped = state.currentTimeMs
        val diff = StopTheTimeEngine.calculateDifference(stopped, state.targetTimeMs)
        val accuracy = StopTheTimeEngine.evaluateAccuracy(diff)
        val stars = StopTheTimeEngine.calculateStars(diff, state.difficulty)
        val roundScore = StopTheTimeEngine.calculateScore(accuracy, state.combo, diff)
        val newCombo = StopTheTimeEngine.updateCombo(state.combo, accuracy)
        val newScore = state.score + roundScore

        val isWon = stars > 0

        if (isWon) {
            if (soundEnabledFlow.value) soundManager.play(GameSound.WIN)
            if (hapticEnabledFlow.value) hapticManager.success()
        } else {
            if (soundEnabledFlow.value) soundManager.play(GameSound.GAME_OVER)
            if (hapticEnabledFlow.value) hapticManager.error()
        }

        if (state.mode == StopGameMode.MULTIPLAYER) {
            if (state.currentPlayer == 1) {
                // Pass to player 2
                _gameState.value = state.copy(
                    phase = StopStatePhase.STOPPED,
                    stoppedTimeMs = stopped,
                    differenceMs = diff,
                    accuracy = accuracy,
                    isTimerHidden = false,
                    currentPlayer = 2,
                    p1DiffMs = diff
                )
            } else {
                // Game over multiplayer
                val winner = when {
                    state.p1DiffMs < diff -> 1
                    diff < state.p1DiffMs -> 2
                    else -> 0 // Tie
                }
                _gameState.value = state.copy(
                    phase = StopStatePhase.WON,
                    stoppedTimeMs = stopped,
                    differenceMs = diff,
                    accuracy = accuracy,
                    isTimerHidden = false,
                    p2DiffMs = diff,
                    multiplayerWinner = winner
                )
            }
            return
        }

        _gameState.value = state.copy(
            phase = if (isWon) StopStatePhase.WON else StopStatePhase.LOST,
            stoppedTimeMs = stopped,
            differenceMs = diff,
            accuracy = accuracy,
            stars = stars,
            score = newScore,
            combo = newCombo,
            isTimerHidden = false
        )

        if (isWon && state.mode == StopGameMode.CAMPAIGN) {
            viewModelScope.launch {
                repository.saveLevelCompletion(state.levelNumber, diff, stars)
            }
        }
        repository.saveHighScore(newScore)
    }

    fun nextLevel() {
        val next = (_gameState.value.levelNumber + 1).coerceAtMost(StopTheTimeRepository.TOTAL_LEVELS)
        startCampaignLevel(next)
    }

    fun retryLevel() {
        startCampaignLevel(_gameState.value.levelNumber)
    }

    fun setSoundEnabled(enabled: Boolean) = repository.setSoundEnabled(enabled)
    fun setHapticEnabled(enabled: Boolean) = repository.setHapticEnabled(enabled)

    class Factory(
        private val repository: StopTheTimeRepository,
        private val soundManager: SoundManager,
        private val hapticManager: HapticManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StopTheTimeViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
