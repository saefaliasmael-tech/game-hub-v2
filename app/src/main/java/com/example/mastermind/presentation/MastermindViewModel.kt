package com.example.mastermind.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mastermind.core.engine.HintResult
import com.example.mastermind.core.engine.MastermindEngine
import com.example.mastermind.core.model.*
import com.example.mastermind.core.repository.MastermindRepository
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

class MastermindViewModel(
    private val repository: MastermindRepository,
    private val soundManager: SoundManager? = null,
    private val hapticManager: HapticManager? = null
) : ViewModel() {

    private val _gameState = MutableStateFlow(MastermindGameState())
    val gameState: StateFlow<MastermindGameState> = _gameState.asStateFlow()

    private val _selectedSlotIndex = MutableStateFlow<Int?>(0)
    val selectedSlotIndex: StateFlow<Int?> = _selectedSlotIndex.asStateFlow()

    val colorBlindMode: StateFlow<Boolean> = repository.colorBlindModeFlow
    val soundEnabled: StateFlow<Boolean> = repository.soundEnabledFlow
    val hapticEnabled: StateFlow<Boolean> = repository.hapticEnabledFlow

    val progressFlow = repository.progressFlow
    val totalStarsFlow = repository.totalStarsFlow
    val completedLevelsCountFlow = repository.completedLevelsCountFlow

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeFirstLevel()
        }
    }

    fun startCampaignLevel(levelNumber: Int) {
        val config = MastermindEngine.getLevelConfig(levelNumber)
        startNewGame(
            levelNumber = config.levelNumber,
            mode = MastermindGameMode.CAMPAIGN,
            difficulty = config.difficulty,
            secretCode = config.targetSecret ?: MastermindEngine.generateSecretCode(
                config.codeLength, config.colorCount, config.allowDuplicates
            ),
            codeLength = config.codeLength,
            maxAttempts = config.maxAttempts
        )
    }

    fun startQuickPlay(difficulty: MastermindDifficulty) {
        val secret = MastermindEngine.generateSecretCode(
            difficulty.codeLength, difficulty.colorCount, difficulty.allowDuplicates
        )
        startNewGame(
            levelNumber = 1,
            mode = MastermindGameMode.QUICK_PLAY,
            difficulty = difficulty,
            secretCode = secret,
            codeLength = difficulty.codeLength,
            maxAttempts = difficulty.maxAttempts
        )
    }

    fun startDailyChallenge() {
        val todayEpoch = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        val config = MastermindEngine.getDailyChallengeConfig(todayEpoch)
        startNewGame(
            levelNumber = 9999,
            mode = MastermindGameMode.DAILY_CHALLENGE,
            difficulty = config.difficulty,
            secretCode = config.targetSecret ?: MastermindEngine.generateSecretCode(
                config.codeLength, config.colorCount, config.allowDuplicates
            ),
            codeLength = config.codeLength,
            maxAttempts = config.maxAttempts
        )
    }

    private fun startNewGame(
        levelNumber: Int,
        mode: MastermindGameMode,
        difficulty: MastermindDifficulty,
        secretCode: List<PegColor>,
        codeLength: Int,
        maxAttempts: Int
    ) {
        timerJob?.cancel()
        _gameState.value = MastermindGameState(
            levelNumber = levelNumber,
            mode = mode,
            difficulty = difficulty,
            secretCode = secretCode,
            attempts = emptyList(),
            currentGuess = List(codeLength) { null },
            maxAttempts = maxAttempts,
            isWon = false,
            isGameOver = false,
            hintsUsed = 0,
            eliminatedColors = emptySet(),
            revealedPositions = emptyMap(),
            hintMessage = null,
            startTimeMs = System.currentTimeMillis(),
            elapsedTimeSeconds = 0,
            starsAwarded = 0,
            score = 0
        )
        _selectedSlotIndex.value = 0
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _gameState.value
                if (!current.isGameOver && !current.isWon) {
                    _gameState.value = current.copy(
                        elapsedTimeSeconds = current.elapsedTimeSeconds + 1
                    )
                }
            }
        }
    }

    fun selectSlot(index: Int) {
        val current = _gameState.value
        if (index in current.currentGuess.indices) {
            _selectedSlotIndex.value = index
            soundManager?.play(GameSound.BUTTON)
            hapticManager?.tap()
        }
    }

    fun selectColor(color: PegColor) {
        val current = _gameState.value
        if (current.isGameOver || current.isWon) return
        if (current.eliminatedColors.contains(color)) return

        val slot = _selectedSlotIndex.value ?: return
        val updatedGuess = current.currentGuess.toMutableList()
        updatedGuess[slot] = color

        // Advance slot to next unfilled or next index
        var nextSlot: Int? = null
        for (i in (slot + 1) until updatedGuess.size) {
            if (updatedGuess[i] == null) {
                nextSlot = i
                break
            }
        }
        if (nextSlot == null) {
            for (i in 0 until slot) {
                if (updatedGuess[i] == null) {
                    nextSlot = i
                    break
                }
            }
        }
        if (nextSlot == null && slot + 1 < updatedGuess.size) {
            nextSlot = slot + 1
        }

        _gameState.value = current.copy(currentGuess = updatedGuess)
        _selectedSlotIndex.value = nextSlot

        soundManager?.play(GameSound.SELECT)
        hapticManager?.tap()
    }

    fun clearSlot(slotIndex: Int) {
        val current = _gameState.value
        if (current.isGameOver || current.isWon) return

        if (slotIndex in current.currentGuess.indices) {
            val updated = current.currentGuess.toMutableList()
            updated[slotIndex] = null
            _gameState.value = current.copy(currentGuess = updated)
            _selectedSlotIndex.value = slotIndex
            soundManager?.play(GameSound.BUTTON)
            hapticManager?.tap()
        }
    }

    fun clearCurrentRow() {
        val current = _gameState.value
        if (current.isGameOver || current.isWon) return

        _gameState.value = current.copy(
            currentGuess = List(current.difficulty.codeLength) { null }
        )
        _selectedSlotIndex.value = 0
        soundManager?.play(GameSound.BUTTON)
        hapticManager?.tap()
    }

    fun submitGuess() {
        val current = _gameState.value
        if (current.isGameOver || current.isWon) return
        if (!current.isCurrentGuessComplete) {
            soundManager?.play(GameSound.INVALID)
            hapticManager?.error()
            return
        }

        val completedGuess = current.currentGuess.filterNotNull()
        val feedback = MastermindEngine.calculateFeedback(current.secretCode, completedGuess)
        val newAttempt = GuessRow(guess = completedGuess, feedback = feedback)
        val newAttempts = current.attempts + newAttempt

        val isWon = feedback.exactMatches == current.secretCode.size
        val isGameOver = isWon || newAttempts.size >= current.maxAttempts

        val stars = if (isWon) {
            MastermindEngine.calculateStars(newAttempts.size, current.maxAttempts, current.hintsUsed)
        } else 0

        val score = if (isWon) {
            MastermindEngine.calculateScore(
                attemptsCount = newAttempts.size,
                maxAttempts = current.maxAttempts,
                timeSeconds = current.elapsedTimeSeconds,
                difficulty = current.difficulty,
                hintsUsed = current.hintsUsed
            )
        } else 0

        _gameState.value = current.copy(
            attempts = newAttempts,
            currentGuess = List(current.difficulty.codeLength) { null },
            isWon = isWon,
            isGameOver = isGameOver,
            starsAwarded = stars,
            score = score
        )
        _selectedSlotIndex.value = 0

        if (isWon) {
            soundManager?.play(GameSound.WIN)
            hapticManager?.success()
            if (current.mode == MastermindGameMode.CAMPAIGN) {
                viewModelScope.launch {
                    repository.saveLevelCompletion(current.levelNumber, stars, score, newAttempts.size)
                    repository.clearActiveGame()
                }
            }
        } else if (isGameOver) {
            soundManager?.play(GameSound.INVALID)
            hapticManager?.error()
            viewModelScope.launch { repository.clearActiveGame() }
        } else {
            soundManager?.play(GameSound.POUR)
            hapticManager?.tap()
            viewModelScope.launch { repository.saveActiveGame(_gameState.value) }
        }
    }

    fun requestHint() {
        val current = _gameState.value
        if (current.isGameOver || current.isWon) return

        when (val result = MastermindEngine.provideHint(current)) {
            is HintResult.ColorEliminated -> {
                _gameState.value = current.copy(
                    hintsUsed = current.hintsUsed + 1,
                    eliminatedColors = current.eliminatedColors + result.color,
                    hintMessage = result.message
                )
            }
            is HintResult.PositionRevealed -> {
                val updatedRevealed = current.revealedPositions + (result.position to result.color)
                val updatedGuess = current.currentGuess.toMutableList()
                updatedGuess[result.position] = result.color
                _gameState.value = current.copy(
                    hintsUsed = current.hintsUsed + 1,
                    revealedPositions = updatedRevealed,
                    currentGuess = updatedGuess,
                    hintMessage = result.message
                )
            }
            is HintResult.GeneralClue -> {
                _gameState.value = current.copy(
                    hintsUsed = current.hintsUsed + 1,
                    hintMessage = result.message
                )
            }
        }
        soundManager?.play(GameSound.ACHIEVEMENT)
        hapticManager?.tap()
    }

    fun dismissHint() {
        _gameState.value = _gameState.value.copy(hintMessage = null)
    }

    fun toggleColorBlindMode() {
        repository.setColorBlindMode(!colorBlindMode.value)
    }

    fun toggleSound() {
        val newVal = !soundEnabled.value
        repository.setSoundEnabled(newVal)
        soundManager?.soundEnabled = newVal
    }

    fun toggleHaptic() {
        val newVal = !hapticEnabled.value
        repository.setHapticEnabled(newVal)
        hapticManager?.hapticsEnabled = newVal
    }

    fun restartCurrentGame() {
        val current = _gameState.value
        when (current.mode) {
            MastermindGameMode.CAMPAIGN -> startCampaignLevel(current.levelNumber)
            MastermindGameMode.QUICK_PLAY -> startQuickPlay(current.difficulty)
            MastermindGameMode.DAILY_CHALLENGE -> startDailyChallenge()
            MastermindGameMode.ENDLESS -> startQuickPlay(current.difficulty)
        }
    }

    fun nextLevel() {
        val current = _gameState.value
        if (current.levelNumber < MastermindRepository.TOTAL_LEVELS) {
            startCampaignLevel(current.levelNumber + 1)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    class Factory(
        private val repository: MastermindRepository,
        private val soundManager: SoundManager? = null,
        private val hapticManager: HapticManager? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MastermindViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
