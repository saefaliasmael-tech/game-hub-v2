package com.example.colorswitch.core.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.watersort.core.database.AppDatabase
import com.example.watersort.core.database.GameProgressDao
import com.example.watersort.core.database.GameProgressEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.math.max

class ColorSwitchRepository(
    private val progressDao: GameProgressDao,
    context: Context? = null
) {
    constructor(database: AppDatabase, context: Context? = null) : this(
        database.gameProgressDao(),
        context
    )

    companion object {
        const val GAME_ID = "color_switch"
        const val TOTAL_LEVELS = 100
        private const val PREFS_NAME = "color_switch_prefs"

        private const val KEY_SOUND = "pref_sound_enabled"
        private const val KEY_HAPTIC = "pref_haptic_enabled"
        private const val KEY_COLOR_BLIND = "pref_color_blind_symbols"

        private const val KEY_GAMES_PLAYED = "stat_games_played"
        private const val KEY_BEST_SCORE = "stat_best_score"
        private const val KEY_BEST_HEIGHT = "stat_best_height"
        private const val KEY_OBSTACLES_PASSED = "stat_obstacles_passed"
    }

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _soundEnabledFlow = MutableStateFlow(prefs?.getBoolean(KEY_SOUND, true) ?: true)
    val soundEnabledFlow: StateFlow<Boolean> = _soundEnabledFlow.asStateFlow()

    private val _hapticEnabledFlow = MutableStateFlow(prefs?.getBoolean(KEY_HAPTIC, true) ?: true)
    val hapticEnabledFlow: StateFlow<Boolean> = _hapticEnabledFlow.asStateFlow()

    private val _colorBlindFlow = MutableStateFlow(prefs?.getBoolean(KEY_COLOR_BLIND, false) ?: false)
    val colorBlindFlow: StateFlow<Boolean> = _colorBlindFlow.asStateFlow()

    val progressFlow: Flow<List<GameProgressEntity>> = progressDao.getProgressForGame(GAME_ID)
    val totalStarsFlow: Flow<Int> = progressDao.getTotalStarsForGame(GAME_ID).map { it ?: 0 }
    val completedLevelsCountFlow: Flow<Int> = progressDao.getCompletedLevelsCount(GAME_ID)

    fun setSoundEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_SOUND, enabled)?.apply()
        _soundEnabledFlow.value = enabled
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_HAPTIC, enabled)?.apply()
        _hapticEnabledFlow.value = enabled
    }

    fun setColorBlindMode(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_COLOR_BLIND, enabled)?.apply()
        _colorBlindFlow.value = enabled
    }

    fun getGamesPlayed(): Int = prefs?.getInt(KEY_GAMES_PLAYED, 0) ?: 0
    fun getBestScore(): Int = prefs?.getInt(KEY_BEST_SCORE, 0) ?: 0
    fun getBestHeight(): Float = prefs?.getFloat(KEY_BEST_HEIGHT, 0f) ?: 0f
    fun getTotalObstaclesPassed(): Int = prefs?.getInt(KEY_OBSTACLES_PASSED, 0) ?: 0

    fun recordGameSession(score: Int, heightReached: Float, obstaclesPassed: Int) {
        prefs?.let { p ->
            val editor = p.edit()
            val totalGames = p.getInt(KEY_GAMES_PLAYED, 0) + 1
            editor.putInt(KEY_GAMES_PLAYED, totalGames)

            val curBestScore = p.getInt(KEY_BEST_SCORE, 0)
            editor.putInt(KEY_BEST_SCORE, max(curBestScore, score))

            val curBestHeight = p.getFloat(KEY_BEST_HEIGHT, 0f)
            editor.putFloat(KEY_BEST_HEIGHT, max(curBestHeight, heightReached))

            val curObstacles = p.getInt(KEY_OBSTACLES_PASSED, 0) + obstaclesPassed
            editor.putInt(KEY_OBSTACLES_PASSED, curObstacles)

            editor.apply()
        }
    }

    suspend fun getLevelProgress(levelNumber: Int): GameProgressEntity? = withContext(Dispatchers.IO) {
        progressDao.getLevelProgress(GAME_ID, levelNumber)
    }

    suspend fun saveLevelCompletion(levelNumber: Int, stars: Int, score: Int) = withContext(Dispatchers.IO) {
        val existing = progressDao.getLevelProgress(GAME_ID, levelNumber)
        val bestStars = max(existing?.stars ?: 0, stars)
        val bestScore = max(existing?.bestScore ?: 0, score)

        progressDao.saveProgress(
            GameProgressEntity(
                gameId = GAME_ID,
                levelId = levelNumber,
                isUnlocked = true,
                isCompleted = true,
                stars = bestStars,
                bestScore = bestScore,
                moves = score,
                updatedAt = System.currentTimeMillis()
            )
        )

        if (levelNumber < TOTAL_LEVELS) {
            val nextLevel = levelNumber + 1
            val nextExisting = progressDao.getLevelProgress(GAME_ID, nextLevel)
            if (nextExisting == null || !nextExisting.isUnlocked) {
                progressDao.saveProgress(
                    GameProgressEntity(
                        gameId = GAME_ID,
                        levelId = nextLevel,
                        isUnlocked = true,
                        isCompleted = false,
                        stars = 0,
                        bestScore = 0,
                        moves = 0,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun initializeFirstLevel() = withContext(Dispatchers.IO) {
        val level1 = progressDao.getLevelProgress(GAME_ID, 1)
        if (level1 == null) {
            progressDao.saveProgress(
                GameProgressEntity(
                    gameId = GAME_ID,
                    levelId = 1,
                    isUnlocked = true,
                    isCompleted = false,
                    stars = 0,
                    bestScore = 0,
                    moves = 0,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
