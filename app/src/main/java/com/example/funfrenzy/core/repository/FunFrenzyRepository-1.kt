package com.example.funfrenzy.core.repository

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

class FunFrenzyRepository(
    private val progressDao: GameProgressDao,
    context: Context? = null
) {
    constructor(database: AppDatabase, context: Context? = null) : this(
        database.gameProgressDao(),
        context
    )

    companion object {
        const val GAME_ID = "fun_frenzy"
        const val TOTAL_LEVELS = 100
        private const val PREFS_NAME = "fun_frenzy_prefs"

        private const val KEY_SOUND = "frenzy_sound_enabled"
        private const val KEY_HAPTIC = "frenzy_haptic_enabled"
        private const val KEY_HIGHEST_LEVEL = "frenzy_highest_level"
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

    suspend fun saveLevelCompletion(levelId: Int, timeRemaining: Int, stars: Int) = withContext(Dispatchers.IO) {
        val entity = GameProgressEntity(
            gameId = GAME_ID,
            levelId = levelId,
            isUnlocked = true,
            isCompleted = true,
            stars = stars,
            bestScore = timeRemaining,
            updatedAt = System.currentTimeMillis()
        )
        progressDao.saveProgress(entity)

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

        val currentHighest = _highestLevelFlow.value
        if (levelId >= currentHighest && levelId < TOTAL_LEVELS) {
            val newHighest = levelId + 1
            _highestLevelFlow.value = newHighest
            prefs?.edit()?.putInt(KEY_HIGHEST_LEVEL, newHighest)?.apply()
        }
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
