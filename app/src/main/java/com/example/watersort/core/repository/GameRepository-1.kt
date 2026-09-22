package com.example.watersort.core.repository

import com.example.watersort.core.database.AchievementEntity
import com.example.watersort.core.database.AppDatabase
import com.example.watersort.core.database.CoinTransactionEntity
import com.example.watersort.core.database.DailyChallengeEntity
import com.example.watersort.core.database.InventoryItemEntity
import com.example.watersort.core.database.LevelProgressEntity
import com.example.watersort.core.database.MidLevelSaveEntity
import com.example.watersort.core.database.MissionEntity
import com.example.watersort.core.database.PlayerProfileEntity
import com.example.watersort.core.economy.EconomyConfig
import com.example.watersort.core.economy.TransactionType
import com.example.watersort.core.model.Bottle
import com.example.watersort.core.model.Difficulty
import com.example.watersort.core.model.GameState
import com.example.watersort.core.model.LiquidColor
import com.example.watersort.core.model.PourDelta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameRepository(
    private val database: AppDatabase
) {
    private val economyMutex = Mutex()
    private val profileDao = database.playerProfileDao()
    private val levelDao = database.levelProgressDao()
    private val coinDao = database.coinTransactionDao()
    private val challengeDao = database.dailyChallengeDao()
    private val achievementDao = database.achievementDao()
    private val inventoryDao = database.inventoryDao()
    private val midLevelDao = database.midLevelSaveDao()
    private val missionDao = database.missionDao()

    val profileFlow: Flow<PlayerProfileEntity?> = profileDao.getProfile()
    val allAchievementsFlow: Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()
    val allInventoryFlow: Flow<List<InventoryItemEntity>> = inventoryDao.getAllItems()
    val allMissionsFlow: Flow<List<MissionEntity>> = missionDao.getAllMissions()
    val completedLevelsCountFlow: Flow<Int> = levelDao.getCompletedLevelsCount()
    val totalStarsCountFlow: Flow<Int> = levelDao.getTotalStarsCount()
    val allProgressFlow: Flow<List<LevelProgressEntity>> = levelDao.getAllProgress()

    fun getMissionsByType(type: String): Flow<List<MissionEntity>> = missionDao.getMissionsByType(type)

    suspend fun ensureInitialized() = withContext(Dispatchers.IO) {
        database.populateInitialData()
    }

    suspend fun getProfile(): PlayerProfileEntity = withContext(Dispatchers.IO) {
        profileDao.getProfileSync() ?: PlayerProfileEntity().also {
            profileDao.saveProfile(it)
        }
    }

    suspend fun updateSettings(
        sound: Boolean? = null,
        music: Boolean? = null,
        haptic: Boolean? = null,
        colorBlind: Boolean? = null,
        language: String? = null
    ) = withContext(Dispatchers.IO) {
        val current = getProfile()
        val updated = current.copy(
            soundEnabled = sound ?: current.soundEnabled,
            musicEnabled = music ?: current.musicEnabled,
            hapticEnabled = haptic ?: current.hapticEnabled,
            colorBlindEnabled = colorBlind ?: current.colorBlindEnabled,
            languageCode = language ?: current.languageCode
        )
        profileDao.saveProfile(updated)
    }

    private suspend fun addCoinsInternal(amount: Int, type: TransactionType, reference: String): Int {
        val current = getProfile()
        val newBalance = current.coins + amount
        val newEarned = current.totalCoinsEarned + amount
        profileDao.saveProfile(current.copy(coins = newBalance, totalCoinsEarned = newEarned))
        coinDao.insertTransaction(
            CoinTransactionEntity(
                amount = amount,
                type = type.name,
                reference = reference
            )
        )
        return newEarned
    }

    suspend fun addCoins(amount: Int, type: TransactionType, reference: String = ""): Boolean = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext false
        val newEarned = economyMutex.withLock {
            addCoinsInternal(amount, type, reference)
        }
        // Invoked outside the economyMutex to prevent recursive re-entrant deadlocks
        checkAchievementProgress("coin_collector", newEarned)
        incrementMissionProgress("d_coins", amount)
        incrementMissionProgress("w_coins", amount)
        com.example.watersort.core.events.GameEventBus.emit(
            com.example.watersort.core.events.GameEvent.CoinsEarned(amount, type, reference)
        )
        true
    }

    private suspend fun spendCoinsInternal(amount: Int, type: TransactionType, reference: String): Boolean {
        val current = getProfile()
        if (current.coins < amount) return false
        val newBalance = (current.coins - amount).coerceAtLeast(0)
        val newSpent = current.totalCoinsSpent + amount
        profileDao.saveProfile(current.copy(coins = newBalance, totalCoinsSpent = newSpent))
        coinDao.insertTransaction(
            CoinTransactionEntity(
                amount = -amount,
                type = type.name,
                reference = reference
            )
        )
        return true
    }

    suspend fun spendCoins(amount: Int, type: TransactionType, reference: String = ""): Boolean = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext false
        val success = economyMutex.withLock {
            spendCoinsInternal(amount, type, reference)
        }
        if (success) {
            com.example.watersort.core.events.GameEventBus.emit(
                com.example.watersort.core.events.GameEvent.CoinsSpent(amount, type, reference)
            )
        }
        success
    }

    suspend fun addXp(amount: Int): Boolean = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext false
        val current = getProfile()
        val newXp = current.xp + amount
        val calculatedLevel = (newXp / 100) + 1
        val leveledUp = calculatedLevel > current.playerLevel

        profileDao.saveProfile(
            current.copy(
                xp = newXp,
                playerLevel = calculatedLevel
            )
        )

        if (leveledUp) {
            val levelUpBonus = 50
            addCoins(levelUpBonus, TransactionType.REWARD_ACHIEVEMENT, "Player Level $calculatedLevel")
            com.example.watersort.core.events.GameEventBus.emit(
                com.example.watersort.core.events.GameEvent.LevelUp(calculatedLevel, levelUpBonus)
            )
        }
        leveledUp
    }

    suspend fun recordMove(isInvalid: Boolean) = withContext(Dispatchers.IO) {
        val current = getProfile()
        val total = current.totalMoves + 1
        val invalid = if (isInvalid) current.invalidMoves + 1 else current.invalidMoves
        profileDao.saveProfile(current.copy(totalMoves = total, invalidMoves = invalid))
    }

    suspend fun recordPowerup(type: String) = withContext(Dispatchers.IO) {
        val current = getProfile()
        val updated = when (type) {
            "HINT" -> current.copy(hintsUsed = current.hintsUsed + 1)
            "UNDO" -> current.copy(undosUsed = current.undosUsed + 1)
            "EXTRA_BOTTLE" -> current.copy(extraBottlesUsed = current.extraBottlesUsed + 1)
            else -> current
        }
        profileDao.saveProfile(updated)
    }

    suspend fun setTutorialCompleted() = withContext(Dispatchers.IO) {
        val current = getProfile()
        profileDao.saveProfile(current.copy(tutorialCompleted = true))
    }

    suspend fun claimMission(missionId: String): Pair<Int, Int>? = withContext(Dispatchers.IO) {
        val mission = missionDao.getMission(missionId) ?: return@withContext null
        if (!mission.isCompleted || mission.isClaimed) return@withContext null

        val claimedRows = missionDao.claimMissionAtomically(missionId)
        if (claimedRows == 0) return@withContext null

        addCoins(mission.rewardCoins, TransactionType.REWARD_ACHIEVEMENT, "Mission ${mission.title}")
        addXp(mission.rewardXp)

        com.example.watersort.core.events.GameEventBus.emit(
            com.example.watersort.core.events.GameEvent.MissionClaimed(
                missionId,
                mission.rewardCoins,
                mission.rewardXp
            )
        )
        Pair(mission.rewardCoins, mission.rewardXp)
    }

    private suspend fun incrementMissionProgress(missionId: String, amount: Int = 1) {
        val mission = missionDao.getMission(missionId) ?: return
        val currentPeriod = getCurrentPeriodKey(mission.type)
        val workingMission = if (mission.periodKey != currentPeriod && currentPeriod.isNotEmpty()) {
            val reset = mission.copy(currentProgress = 0, isCompleted = false, isClaimed = false, periodKey = currentPeriod)
            missionDao.saveMission(reset)
            reset
        } else {
            mission
        }
        if (workingMission.isCompleted) return
        val newProgress = workingMission.currentProgress + amount
        val isNowCompleted = newProgress >= workingMission.target
        missionDao.saveMission(
            workingMission.copy(
                currentProgress = minOf(newProgress, workingMission.target),
                isCompleted = isNowCompleted
            )
        )
        if (isNowCompleted) {
            com.example.watersort.core.events.GameEventBus.emit(
                com.example.watersort.core.events.GameEvent.MissionCompleted(missionId)
            )
        }
    }

    private fun getCurrentPeriodKey(type: String): String {
        return if (type == "DAILY") {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        } else {
            SimpleDateFormat("yyyy-'W'ww", Locale.US).format(Date())
        }
    }

    suspend fun saveLevelCompletion(
        levelId: Int,
        starsEarned: Int,
        movesCount: Int,
        withoutUndo: Boolean,
        withoutInvalidMoves: Boolean
    ): Int = withContext(Dispatchers.IO) {
        clearMidLevel(levelId)

        val existing = levelDao.getLevelProgressSync(levelId)
        val isFirstCompletion = existing == null || !existing.isCompleted
        val previousStars = existing?.stars ?: 0
        val isFirstThreeStars = previousStars < 3 && starsEarned == 3
        val bestMoves = if (existing == null || existing.bestMoves == 0) movesCount else minOf(existing.bestMoves, movesCount)
        val bestStars = maxOf(previousStars, starsEarned)

        levelDao.saveLevelProgress(
            LevelProgressEntity(
                levelId = levelId,
                isUnlocked = true,
                isCompleted = true,
                stars = bestStars,
                bestMoves = bestMoves,
                attempts = (existing?.attempts ?: 0) + 1
            )
        )

        // Unlock next level
        val nextLevelId = levelId + 1
        val nextExisting = levelDao.getLevelProgressSync(nextLevelId)
        if (nextExisting == null || !nextExisting.isUnlocked) {
            levelDao.saveLevelProgress(
                LevelProgressEntity(
                    levelId = nextLevelId,
                    isUnlocked = true,
                    isCompleted = nextExisting?.isCompleted ?: false,
                    stars = nextExisting?.stars ?: 0,
                    bestMoves = nextExisting?.bestMoves ?: 0
                )
            )
        }

        // Award Coins & XP: prevent exploit on replay
        var coinsReward = 0
        var xpReward = 0
        if (isFirstCompletion) {
            coinsReward += EconomyConfig.REWARD_LEVEL_COMPLETE
            xpReward += 50
            if (starsEarned == 3) {
                coinsReward += EconomyConfig.REWARD_THREE_STARS
                xpReward += 25
            }
        } else {
            // Upgrade bonus if player reached 3 stars for the first time
            if (previousStars < 3 && starsEarned == 3) {
                coinsReward += EconomyConfig.REWARD_THREE_STARS
                xpReward += 25
            }
        }

        if (coinsReward > 0) {
            addCoins(coinsReward, TransactionType.REWARD_LEVEL, "Level $levelId")
        }
        if (xpReward > 0) {
            addXp(xpReward)
        }

        // Update Player Profile total stars, completed levels, and perfect levels
        // Only increment completed/perfect count on first occurrence (prevent replay inflation)
        val currentProfile = getProfile()
        val totalStars = levelDao.getTotalStarsCount().firstOrNull() ?: 0
        val newLevelsCompleted = if (isFirstCompletion) currentProfile.levelsCompleted + 1 else currentProfile.levelsCompleted
        val newPerfect = if (isFirstThreeStars) currentProfile.perfectLevels + 1 else currentProfile.perfectLevels
        profileDao.saveProfile(
            currentProfile.copy(
                currentLevel = maxOf(currentProfile.currentLevel, nextLevelId),
                totalStars = totalStars,
                levelsCompleted = newLevelsCompleted,
                perfectLevels = newPerfect
            )
        )

        // Update Missions
        incrementMissionProgress("d_levels", 1)
        incrementMissionProgress("w_levels", 1)
        incrementMissionProgress("d_stars", starsEarned)
        if (starsEarned == 3) {
            incrementMissionProgress("w_perfect", 1)
        }
        if (withoutUndo) {
            incrementMissionProgress("d_undo", 1)
        }

        // Update Achievements
        val completedCount = levelDao.getCompletedLevelsCount().firstOrNull() ?: 1
        checkAchievementProgress("first_drop", 1)
        checkAchievementProgress("getting_started", completedCount)
        checkAchievementProgress("sort_master", completedCount)
        checkAchievementProgress("bottle_expert", completedCount)
        if (starsEarned == 3) {
            checkAchievementProgress("perfect_sort", 1)
        }
        if (withoutUndo) {
            checkAchievementProgress("no_undo", 1)
        }
        if (withoutInvalidMoves) {
            checkAchievementProgress("no_mistakes", 1)
        }

        com.example.watersort.core.events.GameEventBus.emit(
            com.example.watersort.core.events.GameEvent.LevelCompleted(
                levelId, starsEarned, movesCount, withoutUndo, withoutInvalidMoves
            )
        )

        coinsReward
    }

    private suspend fun checkAchievementProgress(achId: String, value: Int) {
        val ach = achievementDao.getAchievement(achId) ?: return
        if (ach.isCompleted) return
        val newProgress = maxOf(ach.currentProgress, value)
        if (newProgress >= ach.target) {
            val updated = achievementDao.completeAchievementAtomically(achId, newProgress)
            if (updated == 1) {
                addCoins(ach.rewardCoins, TransactionType.REWARD_ACHIEVEMENT, ach.id)
            }
        } else {
            achievementDao.saveAchievement(ach.copy(currentProgress = newProgress))
        }
    }

    suspend fun saveMidLevel(state: GameState, undoDeltas: List<PourDelta>) = withContext(Dispatchers.IO) {
        if (state.isWon) {
            clearMidLevel(state.levelNumber)
            return@withContext
        }

        // Compact bottle serialization: "cap:col1,col2|cap:col1,col2"
        val bottleStr = state.bottles.joinToString("|") { b ->
            val layersStr = b.layers.joinToString(",") { it.id.toString() }
            "${b.capacity}:$layersStr"
        }

        // Compact delta serialization: "from>to>colId>amount;..."
        val deltaStr = undoDeltas.joinToString(";") { d ->
            "${d.fromBottleIndex}>${d.toBottleIndex}>${d.color.id}>${d.amountPoured}"
        }

        val timestamp = System.currentTimeMillis()
        val rawPayload = "$bottleStr###$deltaStr"
        val checksum = rawPayload.hashCode().toString()
        val stateCompactWithMeta = "v1|${state.levelNumber}|$timestamp|$checksum|$bottleStr"

        midLevelDao.saveMidLevel(
            MidLevelSaveEntity(
                levelId = state.levelNumber,
                bottleStateCompact = stateCompactWithMeta,
                movesCount = state.movesCount,
                hasExtraBottle = state.hasExtraBottle,
                undoDeltasCompact = deltaStr,
                timestamp = timestamp
            )
        )
    }

    suspend fun loadMidLevel(levelId: Int): Pair<GameState, List<PourDelta>>? = withContext(Dispatchers.IO) {
        try {
            val save = midLevelDao.getMidLevelSave(levelId) ?: return@withContext null

            val parts = save.bottleStateCompact.split("|", limit = 5)
            val bottlePayload: String
            if (parts.size == 5 && parts[0] == "v1") {
                val savedLevelId = parts[1].toIntOrNull()
                val savedChecksum = parts[3]
                bottlePayload = parts[4]

                if (savedLevelId != levelId) {
                    clearMidLevel(levelId)
                    return@withContext null
                }

                val computedChecksum = "$bottlePayload###${save.undoDeltasCompact}".hashCode().toString()
                if (savedChecksum != computedChecksum) {
                    clearMidLevel(levelId)
                    return@withContext null
                }
            } else {
                bottlePayload = save.bottleStateCompact
            }

            val bottles = bottlePayload.split("|").mapIndexed { idx, item ->
                val bParts = item.split(":")
                val cap = bParts[0].toIntOrNull() ?: 4
                if (cap <= 0 || cap > 8) throw IllegalArgumentException("Invalid capacity $cap")
                val layerPart = if (bParts.size > 1) bParts[1] else ""
                val layers = if (layerPart.isBlank()) {
                    emptyList()
                } else {
                    layerPart.split(",").map { idStr ->
                        val id = idStr.toIntOrNull() ?: throw IllegalArgumentException("Invalid color id")
                        LiquidColor.fromId(id) ?: throw IllegalArgumentException("Unknown color $id")
                    }
                }
                if (layers.size > cap) throw IllegalArgumentException("Layers exceed capacity")
                Bottle(id = idx, capacity = cap, layers = layers)
            }

            if (bottles.isEmpty()) {
                clearMidLevel(levelId)
                return@withContext null
            }

            val deltas = if (save.undoDeltasCompact.isBlank()) {
                emptyList()
            } else {
                save.undoDeltasCompact.split(";").mapNotNull { deltaStr ->
                    val p = deltaStr.split(">")
                    if (p.size == 4) {
                        val from = p[0].toIntOrNull() ?: return@mapNotNull null
                        val to = p[1].toIntOrNull() ?: return@mapNotNull null
                        val colorId = p[2].toIntOrNull() ?: return@mapNotNull null
                        val amount = p[3].toIntOrNull() ?: return@mapNotNull null
                        val color = LiquidColor.fromId(colorId) ?: return@mapNotNull null
                        if (from !in bottles.indices || to !in bottles.indices || amount <= 0) return@mapNotNull null
                        PourDelta(from, to, color, amount)
                    } else null
                }
            }

            val gameState = GameState(
                levelNumber = levelId,
                bottles = bottles,
                movesCount = save.movesCount.coerceAtLeast(0),
                hasExtraBottle = save.hasExtraBottle,
                isSolved = false
            )
            Pair(gameState, deltas)
        } catch (e: Exception) {
            clearMidLevel(levelId)
            null
        }
    }

    suspend fun clearMidLevel(levelId: Int) = withContext(Dispatchers.IO) {
        midLevelDao.deleteMidLevelSave(levelId)
    }

    suspend fun claimDailyReward(): Int? = withContext(Dispatchers.IO) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val profile = getProfile()

        if (profile.lastClaimDate == today) {
            return@withContext null // Already claimed today
        }

        // Detect backward time manipulation
        if (profile.lastClaimDate.isNotEmpty() && today < profile.lastClaimDate) {
            return@withContext null // Device clock moved backwards
        }

        val rowsUpdated = profileDao.claimDailyLoginRewardAtomically(today)
        if (rowsUpdated == 0) {
            return@withContext null // Concurrently claimed
        }

        // Calculate streak based on calendar days
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        var streak = profile.currentStreak
        if (profile.lastClaimDate.isNotEmpty()) {
            try {
                val lastDate = sdf.parse(profile.lastClaimDate)
                val todayDate = sdf.parse(today)
                if (lastDate != null && todayDate != null) {
                    val diffDays = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(todayDate.time - lastDate.time)
                    if (diffDays == 1L) {
                        streak += 1
                    } else if (diffDays > 1L) {
                        streak = 1
                    }
                } else {
                    streak = 1
                }
            } catch (_: Exception) {
                streak = 1
            }
        } else {
            streak = 1
        }

        val dayIndex = ((streak - 1) % 7).coerceIn(0, 6)
        val rewardAmount = EconomyConfig.DAILY_REWARD_AMOUNTS[dayIndex]

        addCoins(rewardAmount, TransactionType.REWARD_DAILY, "Daily Day $streak")

        val longest = maxOf(profile.longestStreak, streak)
        val freshProfile = getProfile()
        profileDao.saveProfile(
            freshProfile.copy(
                currentStreak = streak,
                longestStreak = longest,
                lastClaimDate = today
            )
        )

        rewardAmount
    }

    suspend fun getDailyChallenge(date: String): DailyChallengeEntity = withContext(Dispatchers.IO) {
        challengeDao.getChallengeSync(date) ?: DailyChallengeEntity(dateString = date).also {
            challengeDao.saveChallenge(it)
        }
    }

    suspend fun completeDailyChallenge(date: String, stars: Int, moves: Int): Int = withContext(Dispatchers.IO) {
        val rowsClaimed = challengeDao.claimRewardAtomically(date)
        if (rowsClaimed == 0) {
            val existing = getDailyChallenge(date)
            if (existing.rewardClaimed) {
                return@withContext 0
            }
            challengeDao.saveChallenge(
                existing.copy(
                    isCompleted = true,
                    stars = stars,
                    moves = moves
                )
            )
            val secondTry = challengeDao.claimRewardAtomically(date)
            if (secondTry == 0) return@withContext 0
        }

        val reward = EconomyConfig.REWARD_DAILY_CHALLENGE
        addCoins(reward, TransactionType.REWARD_CHALLENGE, "Daily Challenge $date")
        addXp(100)

        val profile = getProfile()
        profileDao.saveProfile(profile.copy(dailyChallengesCompleted = profile.dailyChallengesCompleted + 1))

        com.example.watersort.core.events.GameEventBus.emit(
            com.example.watersort.core.events.GameEvent.DailyChallengeCompleted(date, stars, moves)
        )
        reward
    }

    suspend fun equipSkin(skinId: String): Boolean = withContext(Dispatchers.IO) {
        if (skinId == "classic") {
            val profile = getProfile()
            profileDao.saveProfile(profile.copy(equippedSkinId = skinId))
            return@withContext true
        }
        val item = inventoryDao.getItem(skinId, "SKIN")
        if (item != null && item.isOwned) {
            val profile = getProfile()
            profileDao.saveProfile(profile.copy(equippedSkinId = skinId))
            true
        } else {
            false
        }
    }

    suspend fun equipTheme(themeId: String): Boolean = withContext(Dispatchers.IO) {
        if (themeId == "classic") {
            val profile = getProfile()
            profileDao.saveProfile(profile.copy(equippedThemeId = themeId))
            return@withContext true
        }
        val item = inventoryDao.getItem(themeId, "THEME")
        if (item != null && item.isOwned) {
            val profile = getProfile()
            profileDao.saveProfile(profile.copy(equippedThemeId = themeId))
            true
        } else {
            false
        }
    }

    suspend fun buyItem(item: InventoryItemEntity): Boolean = withContext(Dispatchers.IO) {
        economyMutex.withLock {
            val fresh = inventoryDao.getItem(item.id, item.category) ?: item
            if (fresh.isOwned) return@withLock false
            val profile = getProfile()
            if (profile.coins < item.price) return@withLock false

            val spent = spendCoinsInternal(item.price, if (item.category == "SKIN") TransactionType.SPEND_SKIN else TransactionType.SPEND_THEME, item.id)
            if (spent) {
                inventoryDao.saveItem(fresh.copy(isOwned = true))
                true
            } else {
                false
            }
        }
    }

    suspend fun resetAllProgress() = withContext(Dispatchers.IO) {
        database.clearAllTables()
        database.populateInitialData()
    }
}
