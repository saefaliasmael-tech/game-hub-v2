package com.example.iqboost.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iqboost.engine.IQBoostEngine
import com.example.iqboost.model.IQBoostState
import com.example.iqboost.model.MiniGameType
import com.example.iqboost.model.ReflexPhase
import com.example.iqboost.repository.IQBoostRepository
import com.example.watersort.core.audio.GameSound
import com.example.watersort.core.audio.HapticManager
import com.example.watersort.core.audio.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class IQBoostViewModel(
    private val repository: IQBoostRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {
    private val engine = IQBoostEngine()
    private var reflexStartTime: Long = 0L
    private var reflexTimerJob: Job? = null

    private val _state = MutableStateFlow(
        IQBoostState(bestScore = repository.bestScore.value)
    )
    val state: StateFlow<IQBoostState> = _state.asStateFlow()

    init {
        startRound(1)
    }

    fun startRound(round: Int) {
        val miniGame = when (round) {
            1 -> MiniGameType.MEMORY_MATRIX
            2 -> MiniGameType.SPEED_MATH
            3 -> MiniGameType.STROOP_COLOR
            4 -> MiniGameType.NUMBER_PATTERN
            else -> MiniGameType.REFLEX_SPEED
        }

        _state.update { current ->
            current.copy(
                currentMiniGame = miniGame,
                roundIndex = round,
                isRoundComplete = false,
                isGameFinished = false
            )
        }

        when (miniGame) {
            MiniGameType.MEMORY_MATRIX -> setupMemoryMatrix(round)
            MiniGameType.SPEED_MATH -> {
                _state.update { it.copy(mathQuestion = engine.generateSpeedMath()) }
            }
            MiniGameType.STROOP_COLOR -> {
                _state.update { it.copy(stroopQuestion = engine.generateStroop()) }
            }
            MiniGameType.NUMBER_PATTERN -> {
                _state.update { it.copy(patternQuestion = engine.generatePattern()) }
            }
            MiniGameType.REFLEX_SPEED -> setupReflex()
        }
    }

    private fun setupMemoryMatrix(round: Int) {
        val highlighted = engine.generateMemoryMatrix(round)
        _state.update {
            it.copy(
                matrixHighlighted = highlighted,
                matrixSelected = emptySet(),
                isMatrixShowingPattern = true
            )
        }
        viewModelScope.launch {
            delay(1800L)
            _state.update { it.copy(isMatrixShowingPattern = false) }
        }
    }

    fun onMatrixCellClicked(cellIndex: Int) {
        val current = _state.value
        if (current.isMatrixShowingPattern || current.isRoundComplete) return

        val newSelected = current.matrixSelected + cellIndex
        val isCorrect = current.matrixHighlighted.contains(cellIndex)

        if (isCorrect) {
            soundManager.play(GameSound.SELECT)
            hapticManager.tap()
        } else {
            soundManager.play(GameSound.INVALID)
            hapticManager.error()
        }

        val won = newSelected == current.matrixHighlighted
        if (won) {
            addScore(150)
            advanceRound()
        } else if (!isCorrect) {
            advanceRound()
        } else {
            _state.update { it.copy(matrixSelected = newSelected) }
        }
    }

    fun onMathOptionSelected(index: Int) {
        val q = _state.value.mathQuestion ?: return
        if (index == q.correctIndex) {
            soundManager.play(GameSound.COIN)
            hapticManager.light()
            addScore(150)
        } else {
            soundManager.play(GameSound.INVALID)
            hapticManager.error()
        }
        advanceRound()
    }

    fun onStroopAnswer(userSaysMatch: Boolean) {
        val q = _state.value.stroopQuestion ?: return
        if (userSaysMatch == q.isMatch) {
            soundManager.play(GameSound.COIN)
            hapticManager.light()
            addScore(150)
        } else {
            soundManager.play(GameSound.INVALID)
            hapticManager.error()
        }
        advanceRound()
    }

    fun onPatternOptionSelected(index: Int) {
        val q = _state.value.patternQuestion ?: return
        if (index == q.correctIndex) {
            soundManager.play(GameSound.COIN)
            hapticManager.light()
            addScore(150)
        } else {
            soundManager.play(GameSound.INVALID)
            hapticManager.error()
        }
        advanceRound()
    }

    private fun setupReflex() {
        _state.update { it.copy(reflexState = ReflexPhase.WAITING) }
        reflexTimerJob?.cancel()
        reflexTimerJob = viewModelScope.launch {
            val waitMs = Random.nextLong(1500L, 3500L)
            delay(waitMs)
            reflexStartTime = System.currentTimeMillis()
            _state.update { it.copy(reflexState = ReflexPhase.READY_TO_TAP) }
            soundManager.play(GameSound.COUNTDOWN)
        }
    }

    fun onReflexTapped() {
        if (_state.value.reflexState == ReflexPhase.READY_TO_TAP) {
            val elapsed = System.currentTimeMillis() - reflexStartTime
            soundManager.play(GameSound.WIN)
            hapticManager.success()
            val points = (500 - elapsed).coerceIn(50, 400).toInt()
            addScore(points)
            _state.update { it.copy(reflexState = ReflexPhase.RESULT, reflexTimeMs = elapsed) }
            viewModelScope.launch {
                delay(1200L)
                finishGame()
            }
        } else if (_state.value.reflexState == ReflexPhase.WAITING) {
            // Tapped too early
            reflexTimerJob?.cancel()
            soundManager.play(GameSound.INVALID)
            hapticManager.error()
            finishGame()
        }
    }

    private fun addScore(points: Int) {
        _state.update { it.copy(score = it.score + points) }
    }

    private fun advanceRound() {
        if (_state.value.roundIndex < _state.value.totalRounds) {
            startRound(_state.value.roundIndex + 1)
        } else {
            finishGame()
        }
    }

    private fun finishGame() {
        soundManager.play(GameSound.WIN)
        hapticManager.success()
        val finalScore = _state.value.score
        repository.saveScore(finalScore)
        _state.update { it.copy(isGameFinished = true) }
    }

    fun restart() {
        _state.value = IQBoostState(bestScore = repository.bestScore.value)
        startRound(1)
        soundManager.play(GameSound.BUTTON)
    }
}
