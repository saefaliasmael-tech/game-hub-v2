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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StopTheTimeViewModel(
    private val repository: StopTheTimeRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(StopTheTimeGameState())
    val gameState: StateFlow<StopTheTimeGameState> = _gameState.asStateFlow()

    val progressFlow = repository.progressFlow
    val totalStarsFlow = repository.totalStarsFlow
    val completedLevelsFlow = repository.completedLevelsCountFlow
    val soundEnabledFlow = repository.soundEnabledFlow
    val hapticEnabledFlow = repository.hapticEnabledFlow
    val reduceMotionFlow = repository.reduceMotionFlow

    private var timerJob: Job? = null
    private var countdownJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeFirstLevel()
        }
    }

    fun startCampaignLevel(levelNumber: Int) {
        val config = StopTheTimeLevelManager.getLevel(levelNumber)
        _gameState.value = StopTheTimeGameState(
            levelNumber = levelNumber,
            mode = StopGameMode.CAMPAIGN,
            difficulty = config.difficulty,
            targetTimeMs = config.targetTimeMs,
            totalRounds = config.totalRounds,
            attemptsRemaining = config.maxAttempts,
            status = GameStatus.IDLE
        )
        startCountdownAndRun(config)
    }

    fun startCustomGame(
        mode: StopGameMode,
        difficulty: StopDifficulty = StopDifficulty.NORMAL,
        customTargetMs: Long? = null,
        playerCount: Int = 2
    ) {
        val target = customTargetMs ?: when (mode) {
            StopGameMode.RANDOM -> StopTheTimeEngine.generateRandomTargetMs()
            StopGameMode.DAILY_CHALLENGE -> StopTheTimeEngine.getDailyChallengeTargets().first()
            StopGameMode.BLIND -> 5000L
            StopGameMode.SPEED_VARIATION -> 6000L
            StopGameMode.ONE_ATTEMPT -> 7500L
            StopGameMode.MULTI_ROUND -> 4000L
            StopGameMode.LOCAL_MULTIPLAYER -> 5000L
            else -> 5000L
        }

        val totalRounds = when (mode) {
            StopGameMode.MULTI_ROUND -> 3
            StopGameMode.DAILY_CHALLENGE -> 3
            else -> 1
        }

        _gameState.value = StopTheTimeGameState(
            levelNumber = 1,
            mode = mode,
            difficulty = difficulty,
            targetTimeMs = target,
            totalRounds = totalRounds,
            multiplayerPlayerCount = playerCount,
            attemptsRemaining = if (mode == StopGameMode.ONE_ATTEMPT) 1 else 3,
            status = GameStatus.IDLE
        )

        val config = StopLevelConfig(
            levelNumber = 1,
            targetTimeMs = target,
            difficulty = difficulty,
            isBlind = (mode == StopGameMode.BLIND),
            hasSpeedVariation = (mode == StopGameMode.SPEED_VARIATION),
            hasDistractions = false,
            totalRounds = totalRounds,
            maxAttempts = if (mode == StopGameMode.ONE_ATTEMPT) 1 else 3
        )
        startCountdownAndRun(config)
    }

    private fun startCountdownAndRun(config: StopLevelConfig) {
        timerJob?.cancel()
        countdownJob?.cancel()

        countdownJob = viewModelScope.launch {
            _gameState.value = _gameState.value.copy(
                status = GameStatus.COUNTDOWN,
                countdownNumber = 3,
                elapsedTimeMs = 0L,
                stoppedTimeMs = 0L,
                differenceMs = 0L,
                accuracy = null,
                isTimerHidden = false
            )

            for (i in 3 downTo 1) {
                _gameState.value = _gameState.value.copy(countdownNumber = i)
                soundManager.play(GameSound.COUNTDOWN)
                hapticManager.tap()
                delay(700L)
            }

            // Start running!
            _gameState.value = _gameState.value.copy(
                status = GameStatus.RUNNING,
                countdownNumber = 0
            )
            soundManager.play(GameSound.BUTTON)
            hapticManager.light()

            runTimer(config)
        }
    }

    private fun runTimer(config: StopLevelConfig) {
        val startTime = System.currentTimeMillis()
        timerJob = viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val elapsed = now - startTime

                val shouldHide = config.isBlind && (elapsed >= config.blindHideAfterMs)
                _gameState.value = _gameState.value.copy(
                    elapsedTimeMs = elapsed,
                    isTimerHidden = shouldHide
                )

                delay(16L) // ~60fps smooth loop
            }
        }
    }

    fun onStopPressed() {
        val current = _gameState.value
        if (current.status != GameStatus.RUNNING) return

        timerJob?.cancel()
        val stopped = current.elapsedTimeMs
        val target = current.targetTimeMs
        val diff = StopTheTimeEngine.calculateDifference(stopped, target)
        val accuracy = StopTheTimeEngine.evaluateAccuracy(diff)
        val newCombo = StopTheTimeEngine.updateCombo(current.currentCombo, accuracy)
        val roundScore = StopTheTimeEngine.calculateScore(accuracy, newCombo, diff)
        val stars = StopTheTimeEngine.calculateStars(diff, current.difficulty)

        // Sound & Haptic
        when (accuracy) {
            StopAccuracy.PERFECT -> {
                soundManager.play(GameSound.PERFECT)
                hapticManager.strong()
            }
            StopAccuracy.EXCELLENT, StopAccuracy.GREAT -> {
                soundManager.play(GameSound.STOP)
                hapticManager.medium()
            }
            StopAccuracy.GOOD -> {
                soundManager.play(GameSound.STOP)
                hapticManager.light()
            }
            StopAccuracy.MISS -> {
                soundManager.play(GameSound.INVALID)
                hapticManager.error()
            }
        }

        // Record persistent statistics
        repository.recordStopResult(
            differenceMs = diff,
            isPerfect = (accuracy == StopAccuracy.PERFECT),
            combo = newCombo,
            score = current.score + roundScore
        )

        // Handle multiplayer pass & play
        if (current.mode == StopGameMode.LOCAL_MULTIPLAYER) {
            val playerResult = PlayerTurnResult(
                playerIndex = current.currentMultiplayerPlayer,
                playerName = "Player ${current.currentMultiplayerPlayer + 1}",
                targetMs = target,
                stoppedMs = stopped,
                differenceMs = diff,
                accuracy = accuracy,
                score = roundScore
            )
            val updatedResults = current.multiplayerResults + playerResult
            val nextPlayer = current.currentMultiplayerPlayer + 1

            if (nextPlayer >= current.multiplayerPlayerCount) {
                // All players finished!
                _gameState.value = current.copy(
                    stoppedTimeMs = stopped,
                    differenceMs = diff,
                    accuracy = accuracy,
                    score = current.score + roundScore,
                    stars = stars,
                    status = GameStatus.ROUND_COMPLETE,
                    isTimerHidden = false,
                    multiplayerResults = updatedResults.sortedBy { it.differenceMs }
                )
            } else {
                // Next player's turn
                _gameState.value = current.copy(
                    stoppedTimeMs = stopped,
                    differenceMs = diff,
                    accuracy = accuracy,
                    score = current.score + roundScore,
                    stars = stars,
                    status = GameStatus.STOPPED,
                    isTimerHidden = false,
                    currentMultiplayerPlayer = nextPlayer,
                    multiplayerResults = updatedResults
                )
            }
            return
        }

        // Multi-round progression
        val updatedRounds = current.roundDifferences + diff
        val isLastRound = current.currentRound >= current.totalRounds

        if (isLastRound) {
            val totalScore = current.score + roundScore
            val isWon = stars > 0

            _gameState.value = current.copy(
                stoppedTimeMs = stopped,
                differenceMs = diff,
                accuracy = accuracy,
                currentCombo = newCombo,
                score = totalScore,
                stars = stars,
                status = if (isWon) GameStatus.LEVEL_WON else GameStatus.GAME_OVER,
                isTimerHidden = false,
                roundDifferences = updatedRounds
            )

            // Save campaign progress if applicable
            if (current.mode == StopGameMode.CAMPAIGN && isWon) {
                viewModelScope.launch {
                    repository.saveLevelCompletion(
                        levelNumber = current.levelNumber,
                        stars = stars,
                        score = totalScore,
                        diffMs = diff
                    )
                }
            }

            // Mark daily challenge if applicable
            if (current.mode == StopGameMode.DAILY_CHALLENGE) {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                repository.markDailyChallengeCompleted(todayStr)
            }
        } else {
            // More rounds remaining
            _gameState.value = current.copy(
                stoppedTimeMs = stopped,
                differenceMs = diff,
                accuracy = accuracy,
                currentCombo = newCombo,
                score = current.score + roundScore,
                stars = stars,
                status = GameStatus.ROUND_COMPLETE,
                isTimerHidden = false,
                roundDifferences = updatedRounds
            )
        }
    }

    fun proceedToNextRound() {
        val current = _gameState.value
        val nextRound = current.currentRound + 1
        val nextTarget = when (current.mode) {
            StopGameMode.DAILY_CHALLENGE -> {
                val targets = StopTheTimeEngine.getDailyChallengeTargets()
                targets.getOrElse(nextRound - 1) { 5000L }
            }
            StopGameMode.MULTI_ROUND -> {
                val targets = listOf(4000L, 6500L, 8500L, 10000L)
                targets.getOrElse(nextRound - 1) { 5000L }
            }
            else -> current.targetTimeMs
        }

        _gameState.value = current.copy(
            currentRound = nextRound,
            targetTimeMs = nextTarget,
            elapsedTimeMs = 0L,
            stoppedTimeMs = 0L,
            differenceMs = 0L,
            accuracy = null,
            status = GameStatus.IDLE
        )

        val config = StopLevelConfig(
            levelNumber = current.levelNumber,
            targetTimeMs = nextTarget,
            difficulty = current.difficulty,
            isBlind = (current.mode == StopGameMode.BLIND),
            hasSpeedVariation = (current.mode == StopGameMode.SPEED_VARIATION),
            totalRounds = current.totalRounds
        )
        startCountdownAndRun(config)
    }

    fun proceedToNextMultiplayerTurn() {
        val current = _gameState.value
        _gameState.value = current.copy(
            elapsedTimeMs = 0L,
            stoppedTimeMs = 0L,
            differenceMs = 0L,
            accuracy = null,
            status = GameStatus.IDLE
        )
        val config = StopLevelConfig(
            levelNumber = 1,
            targetTimeMs = current.targetTimeMs,
            difficulty = current.difficulty
        )
        startCountdownAndRun(config)
    }

    fun retryCurrentGame() {
        val current = _gameState.value
        if (current.mode == StopGameMode.CAMPAIGN) {
            startCampaignLevel(current.levelNumber)
        } else {
            startCustomGame(
                mode = current.mode,
                difficulty = current.difficulty,
                customTargetMs = current.targetTimeMs,
                playerCount = current.multiplayerPlayerCount
            )
        }
    }

    fun nextCampaignLevel() {
        val current = _gameState.value
        if (current.levelNumber < StopTheTimeRepository.TOTAL_LEVELS) {
            startCampaignLevel(current.levelNumber + 1)
        }
    }

    fun toggleSound(enabled: Boolean) {
        repository.setSoundEnabled(enabled)
        soundManager.soundEnabled = enabled
    }

    fun toggleHaptic(enabled: Boolean) {
        repository.setHapticEnabled(enabled)
        hapticManager.hapticsEnabled = enabled
    }

    fun toggleReduceMotion(enabled: Boolean) {
        repository.setReduceMotion(enabled)
    }

    // Statistics getters
    fun getGamesPlayed() = repository.getGamesPlayed()
    fun getBestAccuracyMs() = repository.getBestAccuracyMs()
    fun getAverageDifferenceMs() = repository.getAverageDifferenceMs()
    fun getPerfectStops() = repository.getPerfectStops()
    fun getHighestCombo() = repository.getHighestCombo()
    fun getHighestScore() = repository.getHighestScore()
    fun isDailyCompletedToday(): Boolean {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return repository.getDailyCompletedDate() == todayStr
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        countdownJob?.cancel()
    }

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
