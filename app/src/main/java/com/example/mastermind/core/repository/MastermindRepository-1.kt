package com.example.mastermind.core.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.mastermind.core.database.MastermindStateSerializer
import com.example.mastermind.core.model.MastermindGameState
import com.example.watersort.core.database.AppDatabase
import com.example.watersort.core.database.GameProgressDao
import com.example.watersort.core.database.GameProgressEntity
import com.example.watersort.core.database.GameSaveDao
import com.example.watersort.core.database.GameSaveEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.math.max

class MastermindRepository(
    private val progressDao: GameProgressDao,
    private val saveDao: GameSaveDao,
    context: Context? = null
) {
    constructor(database: AppDatabase, context: Context? = null) : this(
        database.gameProgressDao(),
        database.gameSaveDao(),
        context
    )

    companion object {
        const val GAME_ID = "mastermind"
        const val TOTAL_LEVELS = 100
        private const val PREFS_NAME = "mastermind_settings_prefs"
        private const val KEY_COLOR_BLIND = "pref_color_blind_mode"
        private const val KEY_SOUND = "pref_sound_enabled"
        private const val KEY_HAPTIC = "pref_haptic_enabled"
    }

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _colorBlindModeFlow = MutableStateFlow(prefs?.getBoolean(KEY_COLOR_BLIND, false) ?: false)
    val colorBlindModeFlow: StateFlow<Boolean> = _colorBlindModeFlow.asStateFlow()

    private val _soundEnabledFlow = MutableStateFlow(prefs?.getBoolean(KEY_SOUND, true) ?: true)
    val soundEnabledFlow: StateFlow<Boolean> = _soundEnabledFlow.asStateFlow()

    private val _hapticEnabledFlow = MutableStateFlow(prefs?.getBoolean(KEY_HAPTIC, true) ?: true)
    val hapticEnabledFlow: StateFlow<Boolean> = _hapticEnabledFlow.asStateFlow()

    val progressFlow: Flow<List<GameProgressEntity>> = progressDao.getProgressForGame(GAME_ID)
    val totalStarsFlow: Flow<Int> = progressDao.getTotalStarsForGame(GAME_ID).map { it ?: 0 }
    val completedLevelsCountFlow: Flow<Int> = progressDao.getCompletedLevelsCount(GAME_ID)

    fun setColorBlindMode(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_COLOR_BLIND, enabled)?.apply()
        _colorBlindModeFlow.value = enabled
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_SOUND, enabled)?.apply()
        _soundEnabledFlow.value = enabled
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_HAPTIC, enabled)?.apply()
        _hapticEnabledFlow.value = enabled
    }

    suspend fun getLevelProgress(levelNumber: Int): GameProgressEntity? = withContext(Dispatchers.IO) {
        progressDao.getLevelProgress(GAME_ID, levelNumber)
    }

    suspend fun saveLevelCompletion(levelNumber: Int, stars: Int, score: Int, attempts: Int) = withContext(Dispatchers.IO) {
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
                moves = attempts,
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

    suspend fun saveActiveGame(state: MastermindGameState) = withContext(Dispatchers.IO) {
        val json = MastermindStateSerializer.serialize(state)
        saveDao.saveState(
            GameSaveEntity(
                gameId = GAME_ID,
                stateJson = json,
                currentScore = state.score,
                bestScore = 0,
                isWon = state.isWon,
                isGameOver = state.isGameOver,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun loadActiveGame(): MastermindGameState? = withContext(Dispatchers.IO) {
        val save = saveDao.getSave(GAME_ID) ?: return@withContext null
        MastermindStateSerializer.deserialize(save.stateJson)
    }

    suspend fun clearActiveGame() = withContext(Dispatchers.IO) {
        saveDao.deleteSave(GAME_ID)
    }
}
