package com.example.watersort.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.watersort.core.economy.EconomyConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PlayerProfileEntity::class,
        LevelProgressEntity::class,
        CoinTransactionEntity::class,
        DailyChallengeEntity::class,
        AchievementEntity::class,
        InventoryItemEntity::class,
        MidLevelSaveEntity::class,
        MissionEntity::class,
        GameProgressEntity::class,
        GameSaveEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerProfileDao(): PlayerProfileDao
    abstract fun levelProgressDao(): LevelProgressDao
    abstract fun coinTransactionDao(): CoinTransactionDao
    abstract fun dailyChallengeDao(): DailyChallengeDao
    abstract fun achievementDao(): AchievementDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun midLevelSaveDao(): MidLevelSaveDao
    abstract fun missionDao(): MissionDao
    abstract fun gameProgressDao(): GameProgressDao
    abstract fun gameSaveDao(): GameSaveDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS missions (
                        id TEXT NOT NULL PRIMARY KEY,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        target INTEGER NOT NULL,
                        currentProgress INTEGER NOT NULL DEFAULT 0,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        isClaimed INTEGER NOT NULL DEFAULT 0,
                        rewardCoins INTEGER NOT NULL DEFAULT 40,
                        rewardXp INTEGER NOT NULL DEFAULT 50,
                        periodKey TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Recreate inventory_items table with composite primary key (id, category)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS inventory_items_new (
                        id TEXT NOT NULL,
                        category TEXT NOT NULL,
                        name TEXT NOT NULL,
                        price INTEGER NOT NULL,
                        isOwned INTEGER NOT NULL,
                        PRIMARY KEY(id, category)
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT OR IGNORE INTO inventory_items_new (id, category, name, price, isOwned)
                    SELECT id, category, name, price, isOwned FROM inventory_items
                """.trimIndent())
                db.execSQL("DROP TABLE IF EXISTS inventory_items")
                db.execSQL("ALTER TABLE inventory_items_new RENAME TO inventory_items")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS game_progress (
                        gameId TEXT NOT NULL,
                        levelId INTEGER NOT NULL,
                        isUnlocked INTEGER NOT NULL DEFAULT 0,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        stars INTEGER NOT NULL DEFAULT 0,
                        bestScore INTEGER NOT NULL DEFAULT 0,
                        moves INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(gameId, levelId)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS game_save (
                        gameId TEXT NOT NULL PRIMARY KEY,
                        stateJson TEXT NOT NULL,
                        currentScore INTEGER NOT NULL DEFAULT 0,
                        bestScore INTEGER NOT NULL DEFAULT 0,
                        isWon INTEGER NOT NULL DEFAULT 0,
                        isGameOver INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "water_sort_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch {
                            INSTANCE?.populateInitialData()
                        }
                    }
                })
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun populateInitialData() {
        // Default Player Profile
        if (playerProfileDao().getProfileSync() == null) {
            playerProfileDao().saveProfile(
                PlayerProfileEntity(
                    id = 1,
                    coins = EconomyConfig.STARTING_COINS,
                    currentLevel = 1,
                    totalStars = 0,
                    currentStreak = 1,
                    longestStreak = 1,
                    totalCoinsEarned = EconomyConfig.STARTING_COINS
                )
            )
        }

        // Default Level 1 unlocked
        if (levelProgressDao().getLevelProgressSync(1) == null) {
            levelProgressDao().saveLevelProgress(
                LevelProgressEntity(levelId = 1, isUnlocked = true, isCompleted = false)
            )
        }

        // Default Level 1 unlocked for Color Sequence
        if (gameProgressDao().getLevelProgress("color_sequence", 1) == null) {
            gameProgressDao().insertInitialProgress(
                GameProgressEntity(gameId = "color_sequence", levelId = 1, isUnlocked = true, isCompleted = false)
            )
        }

        // Initial Achievements (IGNORE on conflict so progress is never wiped)
        achievementDao().saveAll(
            listOf(
                AchievementEntity("first_drop", target = 1, rewardCoins = 50),
                AchievementEntity("getting_started", target = 10, rewardCoins = 100),
                AchievementEntity("sort_master", target = 50, rewardCoins = 250),
                AchievementEntity("bottle_expert", target = 100, rewardCoins = 500),
                AchievementEntity("perfect_sort", target = 1, rewardCoins = 100),
                AchievementEntity("no_mistakes", target = 1, rewardCoins = 75),
                AchievementEntity("no_undo", target = 1, rewardCoins = 75),
                AchievementEntity("coin_collector", target = 1000, rewardCoins = 200)
            )
        )

        // Initial Skins & Themes (IGNORE on conflict to preserve purchase state)
        inventoryDao().insertInitialItems(
            listOf(
                InventoryItemEntity("classic", "SKIN", "Classic Tube", price = 0, isOwned = true),
                InventoryItemEntity("flask", "SKIN", "Erlenmeyer Flask", price = 250, isOwned = false),
                InventoryItemEntity("crystal", "SKIN", "Crystal Vial", price = 500, isOwned = false),
                InventoryItemEntity("neon", "SKIN", "Neon Glow", price = 750, isOwned = false),
                InventoryItemEntity("potion", "SKIN", "Magic Potion", price = 1000, isOwned = false),
                InventoryItemEntity("gold", "SKIN", "Royal Gilded", price = 1500, isOwned = false),

                // Initial Themes
                InventoryItemEntity("classic", "THEME", "Classic Dark", price = 0, isOwned = true),
                InventoryItemEntity("nature", "THEME", "Crystal Garden", price = 250, isOwned = false),
                InventoryItemEntity("ocean", "THEME", "Deep Ocean", price = 500, isOwned = false),
                InventoryItemEntity("volcano", "THEME", "Ember Valley", price = 750, isOwned = false),
                InventoryItemEntity("space", "THEME", "Cosmic Lab", price = 1000, isOwned = false),
                InventoryItemEntity("frozen", "THEME", "Frozen Realm", price = 1250, isOwned = false),
                InventoryItemEntity("caverns", "THEME", "Crystal Caverns", price = 1500, isOwned = false),
                InventoryItemEntity("shadow", "THEME", "Shadow World", price = 1750, isOwned = false),
                InventoryItemEntity("mystic", "THEME", "Mystic World", price = 2000, isOwned = false)
            )
        )

        // Initial Missions (IGNORE on conflict to preserve claimed/completed state)
        missionDao().saveAllMissions(
            listOf(
                MissionEntity("d_levels", "DAILY", "Level Conqueror", "Complete 3 levels", target = 3, rewardCoins = 50, rewardXp = 60),
                MissionEntity("d_coins", "DAILY", "Treasure Hunter", "Earn 100 coins", target = 100, rewardCoins = 40, rewardXp = 50),
                MissionEntity("d_stars", "DAILY", "Star Chaser", "Earn 6 stars", target = 6, rewardCoins = 60, rewardXp = 75),
                MissionEntity("d_undo", "DAILY", "Sharp Mind", "Complete a level without using undo", target = 1, rewardCoins = 50, rewardXp = 50),
                MissionEntity("w_levels", "WEEKLY", "Weekly Champion", "Complete 15 levels", target = 15, rewardCoins = 200, rewardXp = 250),
                MissionEntity("w_perfect", "WEEKLY", "Perfectionist", "Earn 3 stars on 5 levels", target = 5, rewardCoins = 250, rewardXp = 300),
                MissionEntity("w_coins", "WEEKLY", "Gold Hoarder", "Earn 500 coins", target = 500, rewardCoins = 300, rewardXp = 350)
            )
        )
    }
}
