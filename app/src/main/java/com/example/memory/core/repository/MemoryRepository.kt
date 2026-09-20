package com.example.memory.core.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.memory.core.database.MemoryStateSerializer
import com.example.memory.core.model.MemoryBoardSize
import com.example.memory.core.model.MemoryGameState
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

class MemoryRepository(
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
        const val GAME_ID = "memory_cards"
        private const val PREFS_NAME = "memory_cards_settings_prefs"
        private const val KEY_SOUND = "pref_sound_enabled"
        private const val KEY_HAPTIC = "pref_haptic_enabled"
    }

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _soundEnabledFlow = MutableStateFlow(prefs?.getBoolean(KEY_SOUND, true) ?: true)
    val soundEnabledFlow: StateFlow<Boolean> = _soundEnabledFlow.asStateFlow()

    private val _hapticEnabledFlow = MutableStateFlow(prefs?.getBoolean(KEY_HAPTIC, true) ?: true)
    val hapticEnabledFlow: StateFlow<Boolean> = _hapticEnabledFlow.asStateFlow()

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

    suspend fun saveGameCompletion(boardSize: MemoryBoardSize, stars: Int, moves: Int, score: Int) = withContext(Dispatchers.IO) {
        val levelId = boardSize.ordinal + 1
        val existing = progressDao.getLevelProgress(GAME_ID, levelId)
        val bestStars = max(existing?.stars ?: 0, stars)
        val bestScore = max(existing?.bestScore ?: 0, score)
        val bestMoves = if (existing?.moves != null && existing.moves > 0) minOf(existing.moves, moves) else moves

        progressDao.saveProgress(
            GameProgressEntity(
                gameId = GAME_ID,
                levelId = levelId,
                isUnlocked = true,
                isCompleted = true,
                stars = bestStars,
                bestScore = bestScore,
                moves = bestMoves,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun initializeProgress() = withContext(Dispatchers.IO) {
        MemoryBoardSize.entries.forEach { size ->
            val levelId = size.ordinal + 1
            val existing = progressDao.getLevelProgress(GAME_ID, levelId)
            if (existing == null) {
                progressDao.saveProgress(
                    GameProgressEntity(
                        gameId = GAME_ID,
                        levelId = levelId,
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

    suspend fun saveActiveGame(state: MemoryGameState) = withContext(Dispatchers.IO) {
        val json = MemoryStateSerializer.serialize(state)
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

    suspend fun loadActiveGame(): MemoryGameState? = withContext(Dispatchers.IO) {
        val save = saveDao.getSave(GAME_ID) ?: return@withContext null
        MemoryStateSerializer.deserialize(save.stateJson)
    }

    suspend fun clearActiveGame() = withContext(Dispatchers.IO) {
        saveDao.deleteSave(GAME_ID)
    }
}
