package com.example.colorswitch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.colorswitch.core.engine.ColorSwitchEngine
import com.example.colorswitch.core.level.ColorSwitchLevelManager
import com.example.colorswitch.core.model.*
import com.example.colorswitch.core.repository.ColorSwitchRepository
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
import kotlin.math.abs
import kotlin.math.min

class ColorSwitchViewModel(
    private val repository: ColorSwitchRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(ColorSwitchGameState())
    val gameState: StateFlow<ColorSwitchGameState> = _gameState.asStateFlow()

    val progressFlow = repository.progressFlow
    val totalStarsFlow = repository.totalStarsFlow
    val completedLevelsFlow = repository.completedLevelsCountFlow
    val soundEnabledFlow = repository.soundEnabledFlow
    val hapticEnabledFlow = repository.hapticEnabledFlow
    val colorBlindFlow = repository.colorBlindFlow

    private var gameLoopJob: Job? = null
    private var lastFrameTime = 0L

    init {
        viewModelScope.launch {
            repository.initializeFirstLevel()
        }
    }

    fun startCampaignLevel(levelNumber: Int) {
        val config = ColorSwitchLevelManager.getLevel(levelNumber)
        val obstacles = ColorSwitchLevelManager.generateObstaclesForLevel(config)

        _gameState.value = ColorSwitchGameState(
            mode = ColorSwitchGameMode.CAMPAIGN,
            levelNumber = levelNumber,
            ball = ColorSwitchBall(y = 0f, velocityY = 0f, color = SwitchColor.CYAN),
            obstacles = obstacles,
            cameraY = 0f,
            score = 0,
            isGameOver = false,
            isWon = false,
            isStarted = false
        )
    }

    fun startEndlessMode(mode: ColorSwitchGameMode = ColorSwitchGameMode.ENDLESS) {
        val initialObstacles = (0 until 4).map { i ->
            ColorSwitchLevelManager.generateEndlessObstacle(i, -280f * (i + 1))
        }

        _gameState.value = ColorSwitchGameState(
            mode = mode,
            levelNumber = 1,
            ball = ColorSwitchBall(y = 0f, velocityY = 0f, color = SwitchColor.CYAN),
            obstacles = initialObstacles,
            cameraY = 0f,
            score = 0,
            isGameOver = false,
            isWon = false,
            isStarted = false
        )
    }

    fun onTap() {
        val current = _gameState.value
        if (current.isGameOver || current.isWon) return

        if (!current.isStarted) {
            _gameState.value = current.copy(isStarted = true)
            startGameLoop()
        }

        ColorSwitchEngine.applyJump(current.ball)
        soundManager.play(GameSound.JUMP)
        hapticManager.light()
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        lastFrameTime = System.nanoTime()

        gameLoopJob = viewModelScope.launch {
            while (isActive) {
                val now = System.nanoTime()
                val dt = ((now - lastFrameTime) / 1_000_000_000f).coerceIn(0.008f, 0.033f)
                lastFrameTime = now

                updateGame(dt)
                delay(16L) // ~60fps
            }
        }
    }

    private fun updateGame(dt: Float) {
        val current = _gameState.value
        if (current.isGameOver || current.isWon || !current.isStarted) return

        val ball = current.ball
        ColorSwitchEngine.updatePhysics(ball, dt)

        // Smooth camera following ball
        val targetCamY = ball.y
        val updatedCamY = min(current.cameraY, targetCamY)

        // Check if ball fell below screen
        if (ball.y > updatedCamY + 450f) {
            triggerGameOver()
            return
        }

        // Update obstacles
        val speedMult = if (current.mode == ColorSwitchGameMode.SPEED_CHALLENGE) 1.5f else 1.0f
        ColorSwitchEngine.updateObstacles(current.obstacles, dt, speedMult)

        // Update particles
        val updatedParticles = ColorSwitchEngine.updateParticles(current.particles, dt).toMutableList()

        var newScore = current.score
        var starsCollected = current.starsCollected

        // Check collisions & pickups with active obstacles
        for (obstacle in current.obstacles) {
            // Collision with non-matching color
            if (ColorSwitchEngine.checkCollision(ball, obstacle)) {
                updatedParticles.addAll(ColorSwitchEngine.createParticles(0f, ball.y, ball.color.color, 24))
                _gameState.value = current.copy(particles = updatedParticles)
                triggerGameOver()
                return
            }

            // Star pickup
            if (ColorSwitchEngine.checkStarPickup(ball, obstacle)) {
                newScore++
                starsCollected++
                soundManager.play(GameSound.COIN)
                hapticManager.light()
                updatedParticles.addAll(ColorSwitchEngine.createParticles(0f, obstacle.centerY, androidx.compose.ui.graphics.Color(0xFFFFD600), 16))
            }

            // Color Orb pickup
            if (ColorSwitchEngine.checkColorOrbPickup(ball, obstacle)) {
                soundManager.play(GameSound.COLOR_CHANGE)
                hapticManager.medium()
                updatedParticles.addAll(ColorSwitchEngine.createParticles(0f, obstacle.centerY - obstacle.radius - 40f, ball.color.color, 18))
            }
        }

        // Endless mode spawning
        var obstacles = current.obstacles
        if (current.mode == ColorSwitchGameMode.ENDLESS || current.mode == ColorSwitchGameMode.SPEED_CHALLENGE) {
            val highestObsY = obstacles.minOfOrNull { it.centerY } ?: 0f
            if (ball.y - highestObsY < 800f) {
                val nextId = obstacles.size
                val nextY = highestObsY - 280f
                obstacles = obstacles + ColorSwitchLevelManager.generateEndlessObstacle(nextId, nextY)
            }
        }

        // Campaign Win Condition
        if (current.mode == ColorSwitchGameMode.CAMPAIGN) {
            val config = ColorSwitchLevelManager.getLevel(current.levelNumber)
            if (ball.y <= config.targetHeight) {
                triggerWin(newScore)
                return
            }
        }

        _gameState.value = current.copy(
            cameraY = updatedCamY,
            score = newScore,
            starsCollected = starsCollected,
            particles = updatedParticles,
            obstacles = obstacles,
            maxHeightReached = maxOf(current.maxHeightReached, abs(ball.y))
        )
    }

    private fun triggerGameOver() {
        gameLoopJob?.cancel()
        soundManager.play(GameSound.HIT)
        hapticManager.strong()
        viewModelScope.launch {
            delay(150L)
            soundManager.play(GameSound.GAME_OVER)
        }

        val current = _gameState.value
        repository.recordGameSession(current.score, current.maxHeightReached, current.score)

        _gameState.value = current.copy(isGameOver = true)
    }

    private fun triggerWin(finalScore: Int) {
        gameLoopJob?.cancel()
        soundManager.play(GameSound.WIN)
        hapticManager.success()

        val current = _gameState.value
        val stars = 3 // Finished level
        viewModelScope.launch {
            repository.saveLevelCompletion(current.levelNumber, stars, finalScore)
        }

        _gameState.value = current.copy(isWon = true, score = finalScore)
    }

    fun restartCurrentGame() {
        val current = _gameState.value
        if (current.mode == ColorSwitchGameMode.CAMPAIGN) {
            startCampaignLevel(current.levelNumber)
        } else {
            startEndlessMode(current.mode)
        }
    }

    fun nextLevel() {
        val current = _gameState.value
        if (current.levelNumber < ColorSwitchRepository.TOTAL_LEVELS) {
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

    fun toggleColorBlind(enabled: Boolean) {
        repository.setColorBlindMode(enabled)
    }

    fun getGamesPlayed() = repository.getGamesPlayed()
    fun getBestScore() = repository.getBestScore()
    fun getBestHeight() = repository.getBestHeight()

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }

    class Factory(
        private val repository: ColorSwitchRepository,
        private val soundManager: SoundManager,
        private val hapticManager: HapticManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ColorSwitchViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
