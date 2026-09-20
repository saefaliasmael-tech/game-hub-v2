package com.example.knifehit.core.repository

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.math.max

class KnifeHitRepository(
    private val progressDao: GameProgressDao,
    context: Context? = null
) {
    constructor(database: AppDatabase, context: Context? = null) : this(
        database.gameProgressDao(),
        context
    )

    companion object {
        const val GAME_ID = "knife_hit"
        const val TOTAL_LEVELS = 100
        private const val PREFS_NAME = "knife_hit_prefs"

        private const val KEY_SOUND = "pref_sound_enabled"
        private const val KEY_HAPTIC = "pref_haptic_enabled"

        private const val KEY_APPLES = "user_apples"
        private const val KEY_EQUIPPED_SKIN = "user_equipped_skin"
        private const val KEY_UNLOCKED_SKINS = "user_unlocked_skins"

        private const val KEY_GAMES_PLAYED = "stat_games_played"
        private const val KEY_BOSSES_DEFEATED = "stat_bosses_defeated"
        private const val KEY_HIGHEST_COMBO = "stat_highest_combo"
    }

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _soundEnabledFlow = MutableStateFlow(prefs?.getBoolean(KEY_SOUND, true) ?: true)
    val soundEnabledFlow: StateFlow<Boolean> = _soundEnabledFlow.asStateFlow()

    private val _hapticEnabledFlow = MutableStateFlow(prefs?.getBoolean(KEY_HAPTIC, true) ?: true)
    val hapticEnabledFlow: StateFlow<Boolean> = _hapticEnabledFlow.asStateFlow()

    private val _applesFlow = MutableStateFlow(prefs?.getInt(KEY_APPLES, 0) ?: 0)
    val applesFlow: StateFlow<Int> = _applesFlow.asStateFlow()

    private val _equippedSkinFlow = MutableStateFlow(prefs?.getString(KEY_EQUIPPED_SKIN, "classic") ?: "classic")
    val equippedSkinFlow: StateFlow<String> = _equippedSkinFlow.asStateFlow()

    private val _unlockedSkinsFlow = MutableStateFlow(
        prefs?.getString(KEY_UNLOCKED_SKINS, "classic")?.split(",")?.toSet() ?: setOf("classic")
    )
    val unlockedSkinsFlow: StateFlow<Set<String>> = _unlockedSkinsFlow.asStateFlow()

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

    fun addApples(amount: Int) {
        val current = _applesFlow.value
        val updated = current + amount
        prefs?.edit()?.putInt(KEY_APPLES, updated)?.apply()
        _applesFlow.value = updated
    }

    fun spendApples(amount: Int): Boolean {
        val current = _applesFlow.value
        if (current < amount) return false
        val updated = current - amount
        prefs?.edit()?.putInt(KEY_APPLES, updated)?.apply()
        _applesFlow.value = updated
        return true
    }

    fun unlockSkin(skinId: String) {
        val current = _unlockedSkinsFlow.value.toMutableSet()
        current.add(skinId)
        prefs?.edit()?.putString(KEY_UNLOCKED_SKINS, current.joinToString(","))?.apply()
        _unlockedSkinsFlow.value = current
    }

    fun equipSkin(skinId: String) {
        prefs?.edit()?.putString(KEY_EQUIPPED_SKIN, skinId)?.apply()
        _equippedSkinFlow.value = skinId
    }

    fun getGamesPlayed(): Int = prefs?.getInt(KEY_GAMES_PLAYED, 0) ?: 0
    fun getBossesDefeated(): Int = prefs?.getInt(KEY_BOSSES_DEFEATED, 0) ?: 0
    fun getHighestCombo(): Int = prefs?.getInt(KEY_HIGHEST_COMBO, 0) ?: 0

    fun recordGameSession(isBossDefeated: Boolean, combo: Int) {
        prefs?.let { p ->
            val editor = p.edit()
            editor.putInt(KEY_GAMES_PLAYED, p.getInt(KEY_GAMES_PLAYED, 0) + 1)
            if (isBossDefeated) {
                editor.putInt(KEY_BOSSES_DEFEATED, p.getInt(KEY_BOSSES_DEFEATED, 0) + 1)
            }
            editor.putInt(KEY_HIGHEST_COMBO, max(p.getInt(KEY_HIGHEST_COMBO, 0), combo))
            editor.apply()
        }
    }

    suspend fun getLevelProgress(levelNumber: Int): GameProgressEntity? = withContext(Dispatchers.IO) {
        progressDao.getLevelProgress(GAME_ID, levelNumber)
    }

    suspend fun saveLevelCompletion(levelNumber: Int, stars: Int, score: Int) = withContext(Dispatchers.IO) {
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
                moves = score,
                updatedAt = System.currentTimeMillis()
            )
        )

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
}
