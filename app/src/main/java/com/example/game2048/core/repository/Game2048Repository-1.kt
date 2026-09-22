package com.example.game2048.core.repository

import com.example.game2048.core.database.Game2048StateSerializer
import com.example.game2048.core.model.Game2048State
import com.example.watersort.core.database.AppDatabase
import com.example.watersort.core.database.GameSaveEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.math.max

class Game2048Repository(
    private val saveDao: com.example.watersort.core.database.GameSaveDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    constructor(database: AppDatabase) : this(
        database.gameSaveDao(),
        Dispatchers.IO
    )

    companion object {
        const val GAME_ID = "game_2048"
    }

    val saveStateFlow: Flow<Game2048State?> = saveDao.getSaveFlow(GAME_ID).map { entity ->
        entity?.let { Game2048StateSerializer.deserialize(it.stateJson) }
    }

    suspend fun loadState(): Game2048State? = withContext(dispatcher) {
        val entity = saveDao.getSave(GAME_ID) ?: return@withContext null
        Game2048StateSerializer.deserialize(entity.stateJson)
    }

    suspend fun saveGameState(state: Game2048State) = withContext(dispatcher) {
        val existing = saveDao.getSave(GAME_ID)
        val highestBest = max(existing?.bestScore ?: 0, state.bestScore)
        val stateToSave = state.copy(bestScore = highestBest)

        val json = Game2048StateSerializer.serialize(stateToSave)
        saveDao.saveState(
            GameSaveEntity(
                gameId = GAME_ID,
                stateJson = json,
                currentScore = stateToSave.score,
                bestScore = highestBest,
                isWon = stateToSave.isWon,
                isGameOver = stateToSave.isGameOver,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getBestScore(): Int = withContext(dispatcher) {
        saveDao.getSave(GAME_ID)?.bestScore ?: 0
    }

    suspend fun clearActiveGame(preserveBestScore: Boolean = true) = withContext(dispatcher) {
        val existingBest = if (preserveBestScore) getBestScore() else 0
        saveDao.deleteSave(GAME_ID)
        if (preserveBestScore && existingBest > 0) {
            val emptyState = Game2048State(bestScore = existingBest)
            val json = Game2048StateSerializer.serialize(emptyState)
            saveDao.saveState(
                GameSaveEntity(
                    gameId = GAME_ID,
                    stateJson = json,
                    currentScore = 0,
                    bestScore = existingBest,
                    isWon = false,
                    isGameOver = false,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
