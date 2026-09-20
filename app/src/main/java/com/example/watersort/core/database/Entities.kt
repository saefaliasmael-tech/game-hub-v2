package com.example.watersort.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.watersort.core.economy.EconomyConfig

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int = EconomyConfig.STARTING_COINS,
    val currentLevel: Int = 1,
    val totalStars: Int = 0,
    val currentStreak: Int = 1,
    val longestStreak: Int = 1,
    val lastClaimDate: String = "",
    val equippedSkinId: String = "classic",
    val equippedThemeId: String = "classic",
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val colorBlindEnabled: Boolean = false,
    val languageCode: String = "en",
    val saveVersion: Int = 1,
    val xp: Int = 0,
    val playerLevel: Int = 1,
    val tutorialCompleted: Boolean = false,
    val totalMoves: Int = 0,
    val invalidMoves: Int = 0,
    val levelsCompleted: Int = 0,
    val perfectLevels: Int = 0,
    val hintsUsed: Int = 0,
    val undosUsed: Int = 0,
    val extraBottlesUsed: Int = 0,
    val totalCoinsEarned: Int = EconomyConfig.STARTING_COINS,
    val totalCoinsSpent: Int = 0,
    val dailyChallengesCompleted: Int = 0
)

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey val id: String,
    val type: String, // "DAILY" or "WEEKLY"
    val title: String,
    val description: String,
    val target: Int,
    val currentProgress: Int = 0,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    val rewardCoins: Int = 40,
    val rewardXp: Int = 50,
    val periodKey: String = ""
)

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelId: Int,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val stars: Int = 0,
    val bestMoves: Int = 0,
    val attempts: Int = 0
)

@Entity(tableName = "coin_transactions")
data class CoinTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Int,
    val type: String,
    val timestamp: Long = System.currentTimeMillis(),
    val reference: String = ""
)

@Entity(tableName = "daily_challenge")
data class DailyChallengeEntity(
    @PrimaryKey val dateString: String,
    val isCompleted: Boolean = false,
    val stars: Int = 0,
    val moves: Int = 0,
    val rewardClaimed: Boolean = false
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val target: Int,
    val currentProgress: Int = 0,
    val isCompleted: Boolean = false,
    val rewardCoins: Int = 50
)

@Entity(
    tableName = "inventory_items",
    primaryKeys = ["id", "category"]
)
data class InventoryItemEntity(
    val id: String,
    val category: String, // "SKIN" or "THEME"
    val name: String,
    val price: Int,
    val isOwned: Boolean = false
)

@Entity(tableName = "mid_level_save")
data class MidLevelSaveEntity(
    @PrimaryKey val levelId: Int,
    val bottleStateCompact: String,
    val movesCount: Int,
    val hasExtraBottle: Boolean,
    val undoDeltasCompact: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "game_progress",
    primaryKeys = ["gameId", "levelId"]
)
data class GameProgressEntity(
    val gameId: String,
    val levelId: Int,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val stars: Int = 0,
    val bestScore: Int = 0,
    val moves: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_save")
data class GameSaveEntity(
    @PrimaryKey val gameId: String,
    val stateJson: String,
    val currentScore: Int = 0,
    val bestScore: Int = 0,
    val isWon: Boolean = false,
    val isGameOver: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

