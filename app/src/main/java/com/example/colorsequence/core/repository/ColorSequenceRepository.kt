package com.example.colorsequence.core.repository

import com.example.colorsequence.core.database.ColorSequenceStateSerializer
import com.example.colorsequence.core.model.ColorSequenceGameState
import com.example.watersort.core.database.AppDatabase
import com.example.watersort.core.database.GameProgressEntity
import com.example.watersort.core.database.GameSaveEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.math.max

class ColorSequenceRepository(
    private val progressDao: com.example.watersort.core.database.GameProgressDao,
    private val saveDao: com.example.watersort.core.database.GameSaveDao
) {
    constructor(database: AppDatabase) : this(
        database.gameProgressDao(),
        database.gameSaveDao()
    )

    companion object {
        const val GAME_ID = "color_sequence"
        const val TOTAL_LEVELS = 30
    }

    val progressFlow: Flow<List<GameProgressEntity>> = progressDao.getProgressForGame(GAME_ID)
    val totalStarsFlow: Flow<Int> = progressDao.getTotalStarsForGame(GAME_ID).map { it ?: 0 }
    val completedLevelsCountFlow: Flow<Int> = progressDao.getCompletedLevelsCount(GAME_ID)

    suspend fun getLevelProgress(levelNumber: Int): GameProgressEntity? = withContext(Dispatchers.IO) {
        progressDao.getLevelProgress(GAME_ID, levelNumber)
    }

    suspend fun saveLevelCompletion(levelNumber: Int, stars: Int, score: Int) = withContext(Dispatchers.IO) {
        val existing = progressDao.getLevelProgress(GAME_ID, levelNumber)
        val bestStars = max(existing?.stars ?: 0, stars)
        val bestScore = max(existing?.bestScore ?: 0, score)

        // Save progress for current level
        progressDao.saveProgress(
            GameProgressEntity(
                gameId = GAME_ID,
                levelId = levelNumber,
                isUnlocked = true,
                isCompleted = true,
                stars = bestStars,
                bestScore = bestScore,
                updatedAt = System.currentTimeMillis()
            )
        )

        // Unlock next level if available
        if (levelNumber < TOTAL_LEVELS) {
            val nextLevel = levelNumber + 1
            val nextExisting = progressDao.getLevelProgress(GAME_ID, nextLevel)
            if (nextExisting == null || !nextExisting.isUnlocked) {
                progressDao.saveProgress(
                    GameProgressEntity(
                        gameId = GAME_ID,
                        levelId = nextLevel,
                        isUnlocked = true,
                        isCompleted = nextExisting?.isCompleted ?: false,
                        stars = nextExisting?.stars ?: 0,
                        bestScore = nextExisting?.bestScore ?: 0,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        // Level is finished, clear mid-game save
        saveDao.deleteSave(GAME_ID)
    }

    suspend fun saveMidGameState(state: ColorSequenceGameState) = withContext(Dispatchers.IO) {
        val json = ColorSequenceStateSerializer.serialize(state)
        saveDao.saveState(
            GameSaveEntity(
                gameId = GAME_ID,
                stateJson = json,
                currentScore = state.score,
                bestScore = 0,
                isWon = false,
                isGameOver = false,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun loadMidGameState(): ColorSequenceGameState? = withContext(Dispatchers.IO) {
        val save = saveDao.getSave(GAME_ID) ?: return@withContext null
        ColorSequenceStateSerializer.deserialize(save.stateJson)
    }

    suspend fun clearMidGameState() = withContext(Dispatchers.IO) {
        saveDao.deleteSave(GAME_ID)
    }

    suspend fun getHighestScore(): Int = withContext(Dispatchers.IO) {
        progressDao.getHighestScore(GAME_ID) ?: 0
    }
}
