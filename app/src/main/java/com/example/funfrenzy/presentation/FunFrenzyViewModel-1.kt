package com.example.funfrenzy.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.funfrenzy.core.engine.FunFrenzyPhysicsEngine
import com.example.funfrenzy.core.level.FunFrenzyLevelManager
import com.example.funfrenzy.core.model.ExitPortal
import com.example.funfrenzy.core.model.FrenzyGamePhase
import com.example.funfrenzy.core.model.FunFrenzyLevelConfig
import com.example.funfrenzy.core.model.FunFrenzyState
import com.example.funfrenzy.core.model.RescueBuddy
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
    private val repository: FunFrenzyRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(
        FunFrenzyState(
            buddy = RescueBuddy(200f, 280f),
            portal = ExitPortal(androidx.compose.ui.geometry.Rect(140f, 540f, 260f, 600f))
        )
    )
    val gameState: StateFlow<FunFrenzyState> = _gameState.asStateFlow()

    val soundEnabledFlow = repository.soundEnabledFlow
    val hapticEnabledFlow = repository.hapticEnabledFlow
    val progressFlow = repository.progressFlow
    val completedLevelsFlow = repository.completedLevelsFlow

    private var currentConfig: FunFrenzyLevelConfig = FunFrenzyLevelManager.getLevel(1)
    private var loopJob: Job? = null

    fun stopSimulation() {
        loopJob?.cancel()
    }

    fun startLevel(levelNumber: Int) {
        stopSimulation()
        val config = FunFrenzyLevelManager.getLevel(levelNumber)
        currentConfig = config

        _gameState.value = FunFrenzyState(
            levelNumber = levelNumber,
            phase = FrenzyGamePhase.PLAYING,
            buddy = config.buddy.copy(),
            anchors = config.anchors.map { it.copy() },
            ropes = config.ropes.map { it.copy() },
            hazards = config.hazards.map { it.copy() },
            portal = config.portal,
            timeRemainingSeconds = config.timeLimitSeconds,
            hintRopeId = null,
            hintsRemaining = 3,
            extraTimeUsed = false,
            stars = 0
        )

        startLoop()
    }

    fun onSwipeSlice(p1: Offset, p2: Offset) {
        val state = _gameState.value
        if (state.phase != FrenzyGamePhase.PLAYING) return

        val anchorsMap = state.anchors.associateBy { it.id }
        val cutRope = FunFrenzyPhysicsEngine.checkSwipeCut(p1, p2, state.ropes, anchorsMap, state.buddy)

        if (cutRope != null) {
            if (soundEnabledFlow.value) soundManager.play(GameSound.SELECT)
            if (hapticEnabledFlow.value) hapticManager.tap()

            _gameState.value = state.copy(
                ropes = state.ropes.toList(),
                hintRopeId = if (state.hintRopeId == cutRope.id) null else state.hintRopeId
            )
        }
    }

    fun useHint() {
        val state = _gameState.value
        if (state.phase != FrenzyGamePhase.PLAYING || state.hintsRemaining <= 0) return

        val nextRope = state.ropes.filter { !it.isCut }.minByOrNull { it.optimalCutOrder }
        if (nextRope != null) {
            _gameState.value = state.copy(
                hintRopeId = nextRope.id,
                hintsRemaining = state.hintsRemaining - 1
            )
            if (hapticEnabledFlow.value) hapticManager.tap()
        }
    }

    fun useExtraTime() {
        val state = _gameState.value
        if (state.phase != FrenzyGamePhase.PLAYING || state.extraTimeUsed) return

        _gameState.value = state.copy(
            timeRemainingSeconds = state.timeRemainingSeconds + 10f,
            extraTimeUsed = true
        )
        if (soundEnabledFlow.value) soundManager.play(GameSound.SELECT)
        if (hapticEnabledFlow.value) hapticManager.success()
    }

    private fun startLoop() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            val dt = 0.016f
            while (isActive) {
                val state = _gameState.value
                if (state.phase != FrenzyGamePhase.PLAYING) break

                val newTime = state.timeRemainingSeconds - dt
                if (newTime <= 0f) {
                    handleDefeat(state)
                    break
                }

                val anchorsMap = state.anchors.associateBy { it.id }
                FunFrenzyPhysicsEngine.updatePhysics(
                    buddy = state.buddy,
                    ropes = state.ropes,
                    anchors = anchorsMap,
                    hazards = state.hazards,
                    portal = state.portal
                )

                if (state.buddy.isSaved) {
                    handleVictory(state)
                    break
                } else if (state.buddy.isDead) {
                    handleDefeat(state)
                    break
                }

                _gameState.value = state.copy(
                    timeRemainingSeconds = newTime,
                    buddy = state.buddy
                )

                delay(16)
            }
        }
    }

    private fun handleVictory(state: FunFrenzyState) {
        val timeRatio = state.timeRemainingSeconds / currentConfig.timeLimitSeconds
        val stars = when {
            timeRatio >= 0.5f -> 3
            timeRatio >= 0.2f -> 2
            else -> 1
        }

        _gameState.value = state.copy(
            phase = FrenzyGamePhase.WON,
            stars = stars
        )

        if (soundEnabledFlow.value) soundManager.play(GameSound.WIN)
        if (hapticEnabledFlow.value) hapticManager.success()

        viewModelScope.launch {
            repository.saveLevelCompletion(state.levelNumber, state.timeRemainingSeconds.toInt(), stars)
        }
    }

    private fun handleDefeat(state: FunFrenzyState) {
        _gameState.value = state.copy(
            phase = FrenzyGamePhase.LOST
        )

        if (soundEnabledFlow.value) soundManager.play(GameSound.GAME_OVER)
        if (hapticEnabledFlow.value) hapticManager.error()
    }

    fun restartLevel() {
        startLevel(_gameState.value.levelNumber)
    }

    fun nextLevel() {
        val next = (_gameState.value.levelNumber + 1).coerceAtMost(FunFrenzyLevelManager.TOTAL_LEVELS)
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
