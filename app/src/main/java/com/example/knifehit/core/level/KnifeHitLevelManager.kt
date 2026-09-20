package com.example.knifehit.core.level

import com.example.knifehit.core.model.*

object KnifeHitLevelManager {

    private val levels: List<KnifeHitLevelConfig> = (1..100).map { levelNum ->
        val isBoss = (levelNum % 5 == 0)

        val targetType = when {
            isBoss -> when ((levelNum / 5) % 5) {
                1 -> TargetType.BOSS_TOMATO_KING
                2 -> TargetType.BOSS_IRON_SHIELD
                3 -> TargetType.BOSS_CHEESE_WHEEL
                4 -> TargetType.BOSS_CYBER_CORE
                else -> TargetType.BOSS_TITAN
            }
            levelNum <= 20 -> if (levelNum % 2 == 0) TargetType.WOOD_LOG else TargetType.ORANGE
            levelNum <= 40 -> if (levelNum % 2 == 0) TargetType.WATERMELON else TargetType.LEMON
            levelNum <= 70 -> if (levelNum % 2 == 0) TargetType.WOOD_LOG else TargetType.CYBER_WHEEL
            else -> TargetType.CYBER_WHEEL
        }

        val knivesCount = when {
            isBoss -> (6 + (levelNum / 10)).coerceAtMost(10)
            levelNum <= 10 -> (4 + (levelNum % 3))
            levelNum <= 30 -> (5 + (levelNum % 4))
            levelNum <= 60 -> (6 + (levelNum % 4))
            else -> (7 + (levelNum % 4))
        }

        val baseSpeed = 80f + (levelNum * 1.5f).coerceAtMost(140f)

        // Pre-stuck knives
        val preStuckKnives = if (isBoss) {
            when (levelNum) {
                5 -> listOf(0f, 180f)
                10 -> listOf(45f, 135f, 225f)
                else -> listOf(30f, 120f, 210f, 300f)
            }
        } else if (levelNum > 20 && levelNum % 3 == 0) {
            listOf(0f, 180f)
        } else emptyList()

        // Apples placed on target
        val apples = if (isBoss) {
            listOf(90f, 270f)
        } else if (levelNum % 2 == 1) {
            listOf((Math.random().toFloat() * 360f))
        } else emptyList()

        KnifeHitLevelConfig(
            levelNumber = levelNum,
            knivesRequired = knivesCount,
            targetType = targetType,
            rotationSpeed = baseSpeed,
            hasOscillation = isBoss || (levelNum > 15 && levelNum % 4 == 0),
            oscillationFrequency = if (isBoss) 2.2f else 1.4f,
            initialKnives = preStuckKnives,
            apples = apples
        )
    }

    fun getLevel(levelNumber: Int): KnifeHitLevelConfig {
        val clamped = levelNumber.coerceIn(1, 100)
        return levels[clamped - 1]
    }

    fun getAllLevels(): List<KnifeHitLevelConfig> = levels

    fun generateBossRushLevel(stage: Int): KnifeHitLevelConfig {
        val bossTypes = listOf(
            TargetType.BOSS_TOMATO_KING,
            TargetType.BOSS_IRON_SHIELD,
            TargetType.BOSS_CHEESE_WHEEL,
            TargetType.BOSS_CYBER_CORE,
            TargetType.BOSS_TITAN
        )
        val boss = bossTypes[(stage - 1) % bossTypes.size]
        return KnifeHitLevelConfig(
            levelNumber = stage,
            knivesRequired = (7 + (stage % 4)).coerceAtMost(11),
            targetType = boss,
            rotationSpeed = 110f + (stage * 5f).coerceAtMost(80f),
            hasOscillation = true,
            oscillationFrequency = 2.0f,
            initialKnives = listOf(0f, 120f, 240f),
            apples = listOf(60f, 180f)
        )
    }
}
