package com.example.stopthetime.core.level

import com.example.stopthetime.core.model.StopDifficulty
import com.example.stopthetime.core.model.StopLevelConfig

object StopTheTimeLevelManager {

    private val levels: List<StopLevelConfig> = (1..100).map { levelNum ->
        when (levelNum) {
            in 1..20 -> {
                // Tier 1: Easy (1-20)
                val baseTargets = listOf(3000L, 4000L, 5000L, 6000L, 7000L, 8000L, 10000L, 12000L)
                val target = baseTargets[(levelNum - 1) % baseTargets.size]
                StopLevelConfig(
                    levelNumber = levelNum,
                    targetTimeMs = target,
                    difficulty = StopDifficulty.EASY,
                    isBlind = false,
                    hasSpeedVariation = false,
                    hasDistractions = false,
                    totalRounds = 1,
                    maxAttempts = 3
                )
            }
            in 21..40 -> {
                // Tier 2: Normal (21-40) - Quarter & irregular intervals
                val baseTargets = listOf(3500L, 4250L, 5750L, 6500L, 7250L, 8500L, 9750L, 11500L)
                val target = baseTargets[(levelNum - 21) % baseTargets.size]
                StopLevelConfig(
                    levelNumber = levelNum,
                    targetTimeMs = target,
                    difficulty = StopDifficulty.NORMAL,
                    isBlind = false,
                    hasSpeedVariation = false,
                    hasDistractions = false,
                    totalRounds = 1,
                    maxAttempts = 3
                )
            }
            in 41..60 -> {
                // Tier 3: Hard (41-60) - Blind Mode introduced
                val baseTargets = listOf(4000L, 5000L, 6000L, 7500L, 8000L, 9000L, 10000L)
                val target = baseTargets[(levelNum - 41) % baseTargets.size]
                val hideAfter = if (levelNum <= 50) 2000L else 1500L
                StopLevelConfig(
                    levelNumber = levelNum,
                    targetTimeMs = target,
                    difficulty = StopDifficulty.HARD,
                    isBlind = true,
                    blindHideAfterMs = hideAfter,
                    hasSpeedVariation = false,
                    hasDistractions = false,
                    totalRounds = 1,
                    maxAttempts = 3
                )
            }
            in 61..80 -> {
                // Tier 4: Expert (61-80) - Blind + Speed Variation illusion
                val baseTargets = listOf(4500L, 5500L, 7000L, 8250L, 9500L, 11000L, 13000L)
                val target = baseTargets[(levelNum - 61) % baseTargets.size]
                StopLevelConfig(
                    levelNumber = levelNum,
                    targetTimeMs = target,
                    difficulty = StopDifficulty.EXPERT,
                    isBlind = true,
                    blindHideAfterMs = 1200L,
                    hasSpeedVariation = true,
                    hasDistractions = (levelNum >= 70),
                    totalRounds = if (levelNum % 5 == 0) 2 else 1,
                    maxAttempts = 2
                )
            }
            else -> {
                // Tier 5: Master (81-100) - Tight precision, multi-round gauntlet
                val baseTargets = listOf(5000L, 6660L, 7770L, 8880L, 10000L, 12500L, 15000L)
                val target = baseTargets[(levelNum - 81) % baseTargets.size]
                val rounds = when {
                    levelNum == 100 -> 3
                    levelNum % 4 == 0 -> 2
                    else -> 1
                }
                StopLevelConfig(
                    levelNumber = levelNum,
                    targetTimeMs = target,
                    difficulty = StopDifficulty.MASTER,
                    isBlind = true,
                    blindHideAfterMs = 1000L,
                    hasSpeedVariation = true,
                    hasDistractions = true,
                    totalRounds = rounds,
                    maxAttempts = 2
                )
            }
        }
    }

    fun getLevel(levelNumber: Int): StopLevelConfig {
        val clamped = levelNumber.coerceIn(1, 100)
        return levels[clamped - 1]
    }

    fun getAllLevels(): List<StopLevelConfig> = levels

    fun getLevelsForDifficulty(difficulty: StopDifficulty): List<StopLevelConfig> {
        return levels.filter { it.difficulty == difficulty }
    }
}
