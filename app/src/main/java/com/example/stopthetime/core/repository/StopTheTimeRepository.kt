package com.example.stopthetime.core.repository

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
import kotlin.math.min

class StopTheTimeRepository(
    private val progressDao: GameProgressDao,
    context: Context? = null
) {
    constructor(database: AppDatabase, context: Context? = null) : this(
        database.gameProgressDao(),
        context
    )

    companion object {
        const val GAME_ID = "stop_the_time"
        const val TOTAL_LEVELS = 100
        private const val PREFS_NAME = "stop_the_time_prefs"

        private const val KEY_SOUND = "pref_sound_enabled"
        private const val KEY_HAPTIC = "pref_haptic_enabled"
        private const val KEY_REDUCE_MOTION = "pref_reduce_motion"

        private const val KEY_GAMES_PLAYED = "stat_games_played"
        private const val KEY_BEST_ACCURACY = "stat_best_accuracy_ms"
        private const val KEY_TOTAL_DIFF_SUM = "stat_total_diff_sum"
        private const val KEY_TOTAL_STOPS = "stat_total_stops"
        private const val KEY_PERFECT_STOPS = "stat_perfect_stops"
        private const val KEY_HIGHEST_COMBO = "stat_highest_combo"
        private const val KEY_HIGHEST_SCORE = "stat_highest_score"
        private const val KEY_DAILY_COMPLETED_DATE = "stat_daily_completed_date"
    }

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _soundEnabledFlow = MutableStateFlow(prefs?.getBoolean(KEY_SOUND, true) ?: true)
    val soundEnabledFlow: StateFlow<Boolean> = _soundEnabledFlow.asStateFlow()

    private val _hapticEnabledFlow = MutableStateFlow(prefs?.getBoolean(KEY_HAPTIC, true) ?: true)
    val hapticEnabledFlow: StateFlow<Boolean> = _hapticEnabledFlow.asStateFlow()

    private val _reduceMotionFlow = MutableStateFlow(prefs?.getBoolean(KEY_REDUCE_MOTION, false) ?: false)
    val reduceMotionFlow: StateFlow<Boolean> = _reduceMotionFlow.asStateFlow()

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

    fun setReduceMotion(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_REDUCE_MOTION, enabled)?.apply()
        _reduceMotionFlow.value = enabled
    }

    // Statistics Accessors
    fun getGamesPlayed(): Int = prefs?.getInt(KEY_GAMES_PLAYED, 0) ?: 0
    fun getBestAccuracyMs(): Long = prefs?.getLong(KEY_BEST_ACCURACY, 99999L) ?: 99999L
    fun getAverageDifferenceMs(): Long {
        val totalStops = prefs?.getInt(KEY_TOTAL_STOPS, 0) ?: 0
        if (totalStops == 0) return 0L
        val totalDiff = prefs?.getLong(KEY_TOTAL_DIFF_SUM, 0L) ?: 0L
        return totalDiff / totalStops
    }
    fun getPerfectStops(): Int = prefs?.getInt(KEY_PERFECT_STOPS, 0) ?: 0
    fun getHighestCombo(): Int = prefs?.getInt(KEY_HIGHEST_COMBO, 0) ?: 0
    fun getHighestScore(): Int = prefs?.getInt(KEY_HIGHEST_SCORE, 0) ?: 0
    fun getDailyCompletedDate(): String? = prefs?.getString(KEY_DAILY_COMPLETED_DATE, null)

    fun recordStopResult(differenceMs: Long, isPerfect: Boolean, combo: Int, score: Int) {
        prefs?.let { p ->
            val editor = p.edit()
            val totalGames = p.getInt(KEY_GAMES_PLAYED, 0) + 1
            editor.putInt(KEY_GAMES_PLAYED, totalGames)

            val currentBestAcc = p.getLong(KEY_BEST_ACCURACY, 99999L)
            editor.putLong(KEY_BEST_ACCURACY, min(currentBestAcc, differenceMs))

            val currentDiffSum = p.getLong(KEY_TOTAL_DIFF_SUM, 0L) + differenceMs
            val currentTotalStops = p.getInt(KEY_TOTAL_STOPS, 0) + 1
            editor.putLong(KEY_TOTAL_DIFF_SUM, currentDiffSum)
            editor.putInt(KEY_TOTAL_STOPS, currentTotalStops)

            if (isPerfect) {
                editor.putInt(KEY_PERFECT_STOPS, p.getInt(KEY_PERFECT_STOPS, 0) + 1)
            }

            val maxCombo = p.getInt(KEY_HIGHEST_COMBO, 0)
            editor.putInt(KEY_HIGHEST_COMBO, max(maxCombo, combo))

            val maxScore = p.getInt(KEY_HIGHEST_SCORE, 0)
            editor.putInt(KEY_HIGHEST_SCORE, max(maxScore, score))

            editor.apply()
        }
    }

    fun markDailyChallengeCompleted(dateStr: String) {
        prefs?.edit()?.putString(KEY_DAILY_COMPLETED_DATE, dateStr)?.apply()
    }

    suspend fun getLevelProgress(levelNumber: Int): GameProgressEntity? = withContext(Dispatchers.IO) {
        progressDao.getLevelProgress(GAME_ID, levelNumber)
    }

    suspend fun saveLevelCompletion(levelNumber: Int, stars: Int, score: Int, diffMs: Long) = withContext(Dispatchers.IO) {
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
                moves = diffMs.toInt(),
                updatedAt = System.currentTimeMillis()
            )
        )

        // Unlock next level if in campaign
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
