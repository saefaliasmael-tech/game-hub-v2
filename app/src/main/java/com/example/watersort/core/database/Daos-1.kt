package com.example.watersort.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerProfileDao {
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun getProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun getProfileSync(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: PlayerProfileEntity)

    @Query("UPDATE player_profile SET coins = :coins WHERE id = 1")
    suspend fun updateCoins(coins: Int)

    @Query("UPDATE player_profile SET lastClaimDate = :date WHERE id = 1 AND (lastClaimDate != :date OR lastClaimDate IS NULL)")
    suspend fun claimDailyLoginRewardAtomically(date: String): Int
}

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress WHERE levelId = :levelId")
    fun getLevelProgress(levelId: Int): Flow<LevelProgressEntity?>

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId")
    suspend fun getLevelProgressSync(levelId: Int): LevelProgressEntity?

    @Query("SELECT * FROM level_progress ORDER BY levelId ASC")
    fun getAllProgress(): Flow<List<LevelProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLevelProgress(progress: LevelProgressEntity)

    @Query("SELECT COUNT(*) FROM level_progress WHERE isCompleted = 1")
    fun getCompletedLevelsCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(stars), 0) FROM level_progress")
    fun getTotalStarsCount(): Flow<Int>
}

@Dao
interface CoinTransactionDao {
    @Query("SELECT * FROM coin_transactions ORDER BY timestamp DESC LIMIT 50")
    fun getRecentTransactions(): Flow<List<CoinTransactionEntity>>

    @Insert
    suspend fun insertTransaction(transaction: CoinTransactionEntity)
}

@Dao
interface DailyChallengeDao {
    @Query("SELECT * FROM daily_challenge WHERE dateString = :date")
    fun getChallenge(date: String): Flow<DailyChallengeEntity?>

    @Query("SELECT * FROM daily_challenge WHERE dateString = :date")
    suspend fun getChallengeSync(date: String): DailyChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChallenge(challenge: DailyChallengeEntity)

    @Query("UPDATE daily_challenge SET rewardClaimed = 1 WHERE dateString = :date AND isCompleted = 1 AND rewardClaimed = 0")
    suspend fun claimRewardAtomically(date: String): Int
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievement(id: String): AchievementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAchievement(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveAll(achievements: List<AchievementEntity>)

    @Query("UPDATE achievements SET isCompleted = 1, currentProgress = :progress WHERE id = :id AND isCompleted = 0")
    suspend fun completeAchievementAtomically(id: String, progress: Int): Int
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items WHERE category = :category")
    fun getItemsByCategory(category: String): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items")
    fun getAllItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE id = :id AND category = :category")
    suspend fun getItem(id: String, category: String): InventoryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveItem(item: InventoryItemEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialItems(items: List<InventoryItemEntity>)

    @Query("UPDATE inventory_items SET isOwned = 1 WHERE id = :id AND category = :category AND isOwned = 0")
    suspend fun buyItemAtomically(id: String, category: String): Int
}

@Dao
interface MidLevelSaveDao {
    @Query("SELECT * FROM mid_level_save WHERE levelId = :levelId")
    suspend fun getMidLevelSave(levelId: Int): MidLevelSaveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMidLevel(save: MidLevelSaveEntity)

    @Query("DELETE FROM mid_level_save WHERE levelId = :levelId")
    suspend fun deleteMidLevelSave(levelId: Int)

    @Query("DELETE FROM mid_level_save")
    suspend fun clearAllMidLevelSaves()
}

@Dao
interface MissionDao {
    @Query("SELECT * FROM missions WHERE type = :type ORDER BY isClaimed ASC, isCompleted DESC")
    fun getMissionsByType(type: String): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions ORDER BY isClaimed ASC, isCompleted DESC")
    fun getAllMissions(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE id = :id")
    suspend fun getMission(id: String): MissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMission(mission: MissionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveAllMissions(missions: List<MissionEntity>)

    @Query("UPDATE missions SET currentProgress = :progress, isCompleted = :completed WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int, completed: Boolean)

    @Query("UPDATE missions SET isClaimed = 1 WHERE id = :id AND isCompleted = 1 AND isClaimed = 0")
    suspend fun claimMissionAtomically(id: String): Int
}

@Dao
interface GameProgressDao {
    @Query("SELECT * FROM game_progress WHERE gameId = :gameId ORDER BY levelId ASC")
    fun getProgressForGame(gameId: String): Flow<List<GameProgressEntity>>

    @Query("SELECT * FROM game_progress WHERE gameId = :gameId AND levelId = :levelId")
    suspend fun getLevelProgress(gameId: String, levelId: Int): GameProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: GameProgressEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialProgress(progress: GameProgressEntity)

    @Query("SELECT SUM(stars) FROM game_progress WHERE gameId = :gameId")
    fun getTotalStarsForGame(gameId: String): Flow<Int?>

    @Query("SELECT COUNT(*) FROM game_progress WHERE gameId = :gameId AND isCompleted = 1")
    fun getCompletedLevelsCount(gameId: String): Flow<Int>

    @Query("SELECT MAX(levelId) FROM game_progress WHERE gameId = :gameId AND isCompleted = 1")
    suspend fun getMaxCompletedLevel(gameId: String): Int?

    @Query("SELECT MAX(bestScore) FROM game_progress WHERE gameId = :gameId")
    suspend fun getHighestScore(gameId: String): Int?
}

@Dao
interface GameSaveDao {
    @Query("SELECT * FROM game_save WHERE gameId = :gameId")
    suspend fun getSave(gameId: String): GameSaveEntity?

    @Query("SELECT * FROM game_save WHERE gameId = :gameId")
    fun getSaveFlow(gameId: String): Flow<GameSaveEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveState(save: GameSaveEntity)

    @Query("DELETE FROM game_save WHERE gameId = :gameId")
    suspend fun deleteSave(gameId: String)
}


