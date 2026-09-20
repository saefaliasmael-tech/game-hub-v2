package com.example.memory.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.memory.core.engine.MemoryEngine
import com.example.memory.core.model.*
import com.example.memory.core.repository.MemoryRepository
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
import java.util.Random

class MemoryViewModel(
    private val repository: MemoryRepository,
    private val soundManager: SoundManager? = null,
    private val hapticManager: HapticManager? = null
) : ViewModel() {

    private val _gameState = MutableStateFlow(MemoryGameState())
    val gameState: StateFlow<MemoryGameState> = _gameState.asStateFlow()

    val soundEnabled: StateFlow<Boolean> = repository.soundEnabledFlow
    val hapticEnabled: StateFlow<Boolean> = repository.hapticEnabledFlow

    val progressFlow = repository.progressFlow
    val totalStarsFlow = repository.totalStarsFlow
    val completedLevelsCountFlow = repository.completedLevelsCountFlow

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeProgress()
        }
    }

    fun startNewGame(
        boardSize: MemoryBoardSize = MemoryBoardSize.NORMAL,
        mode: MemoryGameMode = MemoryGameMode.CLASSIC,
        theme: MemoryTheme = MemoryTheme.ANIMALS
    ) {
        timerJob?.cancel()

        val random = when (mode) {
            MemoryGameMode.DAILY_CHALLENGE -> {
                val epochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
                Random(epochDay * 31337L)
            }
            else -> Random()
        }

        val cards = MemoryEngine.createBoard(boardSize, theme, random)

        val timeLimit = when (mode) {
            MemoryGameMode.TIMED -> when (boardSize) {
                MemoryBoardSize.EASY -> 30
                MemoryBoardSize.NORMAL -> 45
                MemoryBoardSize.HARD -> 75
                MemoryBoardSize.EXPERT -> 120
                MemoryBoardSize.MASTER -> 180
            }
            else -> 0
        }

        val maxMoves = when (mode) {
            MemoryGameMode.LIMITED_MOVES -> (boardSize.totalPairs * 2.2).toInt()
            else -> 0
        }

        _gameState.value = MemoryGameState(
            boardSize = boardSize,
            mode = mode,
            theme = theme,
            cards = cards,
            firstSelectedIndex = null,
            secondSelectedIndex = null,
            isBusyChecking = false,
            movesCount = 0,
            matchesCount = 0,
            mistakesCount = 0,
            timeLimitSeconds = timeLimit,
            elapsedTimeSeconds = 0,
            maxMoves = maxMoves,
            isWon = false,
            isGameOver = false,
            starsAwarded = 0,
            score = 0
        )

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _gameState.value
                if (!current.isGameOver && !current.isWon) {
                    val newElapsed = current.elapsedTimeSeconds + 1
                    var isGameOver = false

                    if (current.mode == MemoryGameMode.TIMED && newElapsed >= current.timeLimitSeconds) {
                        isGameOver = true
                        soundManager?.play(GameSound.INVALID)
                        hapticManager?.error()
                    }

                    _gameState.value = current.copy(
                        elapsedTimeSeconds = newElapsed,
                        isGameOver = isGameOver
                    )
                }
            }
        }
    }

    /**
     * Card flip interaction.
     * Prevents any action while checking or if the card is already matched/flipped.
     */
    fun onCardClicked(cardIndex: Int) {
        val current = _gameState.value
        if (current.isGameOver || current.isWon || current.isBusyChecking) return
        if (cardIndex !in current.cards.indices) return

        val card = current.cards[cardIndex]
        if (card.isFlipped || card.isMatched) return

        soundManager?.play(GameSound.BUTTON)
        hapticManager?.tap()

        val firstIdx = current.firstSelectedIndex
        if (firstIdx == null) {
            // First card flipped
            val updatedCards = current.cards.toMutableList()
            updatedCards[cardIndex] = card.copy(isFlipped = true)
            _gameState.value = current.copy(
                cards = updatedCards,
                firstSelectedIndex = cardIndex
            )
        } else {
            // Second card flipped -> check pair
            val updatedCards = current.cards.toMutableList()
            updatedCards[cardIndex] = card.copy(isFlipped = true)
            val newMoves = current.movesCount + 1

            _gameState.value = current.copy(
                cards = updatedCards,
                secondSelectedIndex = cardIndex,
                movesCount = newMoves,
                isBusyChecking = true
            )

            val firstCard = updatedCards[firstIdx]
            val secondCard = updatedCards[cardIndex]

            viewModelScope.launch {
                if (firstCard.pairId == secondCard.pairId) {
                    // Match found!
                    delay(300L)
                    val matchedCards = _gameState.value.cards.toMutableList()
                    matchedCards[firstIdx] = firstCard.copy(isMatched = true, isFlipped = true)
                    matchedCards[cardIndex] = secondCard.copy(isMatched = true, isFlipped = true)

                    val newMatches = _gameState.value.matchesCount + 1
                    val isWon = newMatches == _gameState.value.boardSize.totalPairs

                    val stars = if (isWon) {
                        MemoryEngine.calculateStars(newMoves, _gameState.value.boardSize.totalPairs)
                    } else 0

                    val score = if (isWon) {
                        MemoryEngine.calculateScore(
                            movesCount = newMoves,
                            pairsCount = _gameState.value.boardSize.totalPairs,
                            timeSeconds = _gameState.value.elapsedTimeSeconds,
                            mode = _gameState.value.mode
                        )
                    } else 0

                    _gameState.value = _gameState.value.copy(
                        cards = matchedCards,
                        firstSelectedIndex = null,
                        secondSelectedIndex = null,
                        matchesCount = newMatches,
                        isBusyChecking = false,
                        isWon = isWon,
                        starsAwarded = stars,
                        score = score
                    )

                    soundManager?.play(if (isWon) GameSound.WIN else GameSound.COIN)
                    hapticManager?.success()

                    if (isWon) {
                        repository.saveGameCompletion(_gameState.value.boardSize, stars, newMoves, score)
                        repository.clearActiveGame()
                    } else {
                        repository.saveActiveGame(_gameState.value)
                    }
                } else {
                    // Mismatch
                    delay(800L)
                    val revertedCards = _gameState.value.cards.toMutableList()
                    revertedCards[firstIdx] = firstCard.copy(isFlipped = false)
                    revertedCards[cardIndex] = secondCard.copy(isFlipped = false)

                    val newMistakes = _gameState.value.mistakesCount + 1
                    val isOutOfMoves = _gameState.value.mode == MemoryGameMode.LIMITED_MOVES &&
                            newMoves >= _gameState.value.maxMoves

                    _gameState.value = _gameState.value.copy(
                        cards = revertedCards,
                        firstSelectedIndex = null,
                        secondSelectedIndex = null,
                        mistakesCount = newMistakes,
                        isBusyChecking = false,
                        isGameOver = isOutOfMoves
                    )

                    if (isOutOfMoves) {
                        soundManager?.play(GameSound.INVALID)
                        hapticManager?.error()
                    } else {
                        soundManager?.play(GameSound.INVALID)
                        hapticManager?.tap()
                    }
                }
            }
        }
    }

    fun restartGame() {
        val current = _gameState.value
        startNewGame(current.boardSize, current.mode, current.theme)
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

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    class Factory(
        private val repository: MemoryRepository,
        private val soundManager: SoundManager? = null,
        private val hapticManager: HapticManager? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MemoryViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
