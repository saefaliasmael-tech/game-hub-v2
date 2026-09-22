package com.example.unblockme.core.repository

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
import kotlinx.coroutines.withContext
import kotlin.math.max

class UnblockRepository(
    private val progressDao: GameProgressDao,
    context: Context? = null
) {
    constructor(database: AppDatabase, context: Context? = null) : this(
        database.gameProgressDao(),
        context
    )

    companion object {
        const val GAME_ID = "unblock_me"
        const val TOTAL_LEVELS = 100
        private const val PREFS_NAME = "unblock_me_prefs"

        private const val KEY_SOUND = "unblock_sound_enabled"
        private const val KEY_HAPTIC = "unblock_haptic_enabled"
        private const val KEY_HIGHEST_LEVEL = "unblock_highest_level"
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

    suspend fun saveLevelCompletion(levelId: Int, moves: Int, stars: Int = 3) = withContext(Dispatchers.IO) {
        val entity = GameProgressEntity(
            gameId = GAME_ID,
            levelId = levelId,
            isUnlocked = true,
            isCompleted = true,
            stars = stars,
            bestScore = moves,
            updatedAt = System.currentTimeMillis()
        )
        progressDao.saveProgress(entity)

        val nextLevelId = levelId + 1
        if (nextLevelId <= TOTAL_LEVELS) {
            val nextEntity = GameProgressEntity(
                gameId = GAME_ID,
                levelId = nextLevelId,
                isUnlocked = true,
                isCompleted = false,
                stars = 0,
                bestScore = 0,
                updatedAt = System.currentTimeMillis()
            )
            progressDao.saveProgress(nextEntity)
        }

        val currentHighest = _highestLevelFlow.value
        val newHighest = max(currentHighest, max(levelId, nextLevelId.coerceAtMost(TOTAL_LEVELS)))
        if (newHighest != currentHighest) {
            _highestLevelFlow.value = newHighest
            prefs?.edit()?.putInt(KEY_HIGHEST_LEVEL, newHighest)?.apply()
        }
    }

    suspend fun getProgressForLevel(levelId: Int): GameProgressEntity? = withContext(Dispatchers.IO) {
        progressDao.getLevelProgress(GAME_ID, levelId)
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
