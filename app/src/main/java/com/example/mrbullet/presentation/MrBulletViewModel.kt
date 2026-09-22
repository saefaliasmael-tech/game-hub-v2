package com.example.mrbullet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mrbullet.core.engine.MrBulletEngine
import com.example.mrbullet.core.level.MrBulletLevelManager
import com.example.mrbullet.core.model.Bullet
import com.example.mrbullet.core.model.MrBulletGameState
import com.example.mrbullet.core.repository.MrBulletRepository
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MrBulletViewModel(
    val repository: MrBulletRepository,
    val soundManager: SoundManager,
    val hapticManager: HapticManager
) : ViewModel() {

    private val _gameState = MutableStateFlow(MrBulletGameState())
    val gameState: StateFlow<MrBulletGameState> = _gameState.asStateFlow()

    private var physicsJob: Job? = null

    val soundEnabledFlow = repository.soundEnabledFlow
    val hapticEnabledFlow = repository.hapticEnabledFlow
    val highestLevelFlow = repository.highestLevelFlow
    val progressFlow = repository.progressFlow
    val completedLevelsFlow = repository.completedLevelsFlow

    init {
        startLevel(1)
    }

    fun startLevel(levelNumber: Int) {
        physicsJob?.cancel()

        val config = MrBulletLevelManager.getLevel(levelNumber)

        _gameState.value = MrBulletGameState(
            levelNumber = levelNumber,
            heroX = config.heroX,
            heroY = config.heroY,
            bulletsLeft = config.maxBullets,
            maxBullets = config.maxBullets,
            activeBullets = emptyList(),
            enemies = config.enemies,
            walls = config.walls,
            aimAngle = null,
            isWon = false,
            isGameOver = false,
            stars = 0
        )

        startPhysicsLoop()
    }

    fun onAim(touchX: Float, touchY: Float) {
        val current = _gameState.value
        if (current.isWon || current.isGameOver || current.bulletsLeft <= 0) return

        val dx = touchX - current.heroX
        val dy = touchY - current.heroY
        val angle = atan2(dy, dx)
        _gameState.value = current.copy(aimAngle = angle)
    }

    fun onShoot(touchX: Float, touchY: Float) {
        val current = _gameState.value
        if (current.isWon || current.isGameOver || current.bulletsLeft <= 0) {
            _gameState.value = current.copy(aimAngle = null)
            return
        }

        val dx = touchX - current.heroX
        val dy = touchY - current.heroY
        val angle = atan2(dy, dx)

        val speed = MrBulletEngine.BULLET_SPEED
        val newBullet = Bullet(
            x = current.heroX + cos(angle) * 0.05f,
            y = current.heroY + sin(angle) * 0.05f,
            vx = cos(angle) * speed,
            vy = sin(angle) * speed
        )

        if (soundEnabledFlow.value) soundManager.play(GameSound.KNIFE_THROW)
        if (hapticEnabledFlow.value) hapticManager.strong()

        val bullets = current.activeBullets + newBullet
        _gameState.value = current.copy(
            bulletsLeft = current.bulletsLeft - 1,
            activeBullets = bullets,
            aimAngle = null
        )
    }

    private fun startPhysicsLoop() {
        physicsJob?.cancel()
        physicsJob = viewModelScope.launch {
            while (isActive) {
                updatePhysics()
                delay(16)
            }
        }
    }

    private fun updatePhysics() {
        val current = _gameState.value
        if (current.isWon || current.isGameOver) return

        if (current.activeBullets.isEmpty()) {
            // Check if game over condition met
            val allDead = current.enemies.all { it.isDead }
            if (!allDead && current.bulletsLeft == 0) {
                _gameState.value = current.copy(isGameOver = true)
                if (soundEnabledFlow.value) soundManager.play(GameSound.INVALID)
                if (hapticEnabledFlow.value) hapticManager.error()
            }
            return
        }

        val (updatedBullets, updatedEnemies, updatedWalls) = MrBulletEngine.stepPhysics(
            bullets = current.activeBullets,
            enemies = current.enemies,
            walls = current.walls
        )

        // Check if all enemies eliminated
        val allDead = updatedEnemies.all { it.isDead }
        var isWon = false
        var isGameOver = false
        var stars = current.stars

        if (allDead) {
            isWon = true
            stars = MrBulletEngine.calculateStars(current.bulletsLeft, current.maxBullets)
            if (soundEnabledFlow.value) soundManager.play(GameSound.WIN)
            if (hapticEnabledFlow.value) hapticManager.success()

            viewModelScope.launch {
                repository.saveLevelCompletion(current.levelNumber, stars)
            }
        } else if (updatedBullets.isEmpty() && current.bulletsLeft == 0) {
            isGameOver = true
            if (soundEnabledFlow.value) soundManager.play(GameSound.INVALID)
            if (hapticEnabledFlow.value) hapticManager.error()
        }

        _gameState.value = current.copy(
            activeBullets = updatedBullets,
            enemies = updatedEnemies,
            walls = updatedWalls,
            isWon = isWon,
            isGameOver = isGameOver,
            stars = stars
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
        physicsJob?.cancel()
    }

    class Factory(
        private val repository: MrBulletRepository,
        private val soundManager: SoundManager,
        private val hapticManager: HapticManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MrBulletViewModel(repository, soundManager, hapticManager) as T
        }
    }
}
