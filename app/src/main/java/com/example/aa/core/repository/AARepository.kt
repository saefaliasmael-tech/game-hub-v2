package com.example.aa.core.repository

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

class AARepository(
    private val progressDao: GameProgressDao,
    context: Context? = null
) {
    constructor(database: AppDatabase, context: Context? = null) : this(
        database.gameProgressDao(),
        context
    )

    companion object {
        const val GAME_ID = "aa"
        const val TOTAL_LEVELS = 100
        private const val PREFS_NAME = "aa_prefs"

        private const val KEY_SOUND = "aa_sound_enabled"
        private const val KEY_HAPTIC = "aa_haptic_enabled"
        private const val KEY_HIGHEST_LEVEL = "aa_highest_level"
        private const val KEY_TOTAL_BALLS_PINNED = "aa_total_balls_pinned"
    }

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _soundEnabledFlow = MutableStateFlow(prefs?.getBoolean(KEY_SOUND, true) ?: true)
    val soundEnabledFlow: StateFlow<Boolean> = _soundEnabledFlow.asStateFlow()

    private val _hapticEnabledFlow = MutableStateFlow(prefs?.getBoolean(KEY_HAPTIC, true) ?: true)
    val hapticEnabledFlow: StateFlow<Boolean> = _hapticEnabledFlow.asStateFlow()

    private val _highestLevelFlow = MutableStateFlow(prefs?.getInt(KEY_HIGHEST_LEVEL, 1) ?: 1)
    val highestLevelFlow: StateFlow<Int> = _highestLevelFlow.asStateFlow()

    val progressFlow: Flow<List<GameProgressEntity>> = progressDao.getProgressForGame(GAME_ID)

    val completedLevelsFlow: Flow<Int> = progressDao.getCompletedLevelsCount(GAME_ID)

    suspend fun isLevelUnlocked(levelId: Int): Boolean = withContext(Dispatchers.IO) {
        if (levelId <= 1) return@withContext true
        val progress = progressDao.getLevelProgress(GAME_ID, levelId)
        if (progress?.isUnlocked == true) return@withContext true
        val prevProgress = progressDao.getLevelProgress(GAME_ID, levelId - 1)
        prevProgress?.isCompleted == true || levelId <= (_highestLevelFlow.value)
    }

    suspend fun saveLevelCompletion(levelId: Int, stars: Int = 3, score: Int = 100) = withContext(Dispatchers.IO) {
        val entity = GameProgressEntity(
            gameId = GAME_ID,
            levelId = levelId,
            isUnlocked = true,
            isCompleted = true,
            stars = stars,
            bestScore = score,
            updatedAt = System.currentTimeMillis()
        )
        progressDao.saveProgress(entity)

        // Unlock next level
        if (levelId < TOTAL_LEVELS) {
            val nextLevel = GameProgressEntity(
                gameId = GAME_ID,
                levelId = levelId + 1,
                isUnlocked = true,
                isCompleted = false,
                stars = 0,
                bestScore = 0,
                updatedAt = System.currentTimeMillis()
            )
            progressDao.saveProgress(nextLevel)
        }

        val newHighest = max(_highestLevelFlow.value, levelId + 1)
        _highestLevelFlow.value = newHighest
        prefs?.edit()?.putInt(KEY_HIGHEST_LEVEL, newHighest)?.apply()
    }

    fun incrementBallsPinned(count: Int = 1) {
        val current = prefs?.getInt(KEY_TOTAL_BALLS_PINNED, 0) ?: 0
        prefs?.edit()?.putInt(KEY_TOTAL_BALLS_PINNED, current + count)?.apply()
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabledFlow.value = enabled
        prefs?.edit()?.putBoolean(KEY_SOUND, enabled)?.apply()
    }

    fun setHapticEnabled(enabled: Boolean) {
        _hapticEnabledFlow.value = enabled
        prefs?.edit()?.putBoolean(KEY_HAPTIC, enabled)?.apply()
    }
}
