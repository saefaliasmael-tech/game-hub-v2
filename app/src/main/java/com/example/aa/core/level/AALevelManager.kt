package com.example.aa.core.level

import com.example.aa.core.model.AALevelConfig

object AALevelManager {

    const val MAX_LEVELS = 100

    fun getLevel(levelNumber: Int): AALevelConfig {
        val level = levelNumber.coerceIn(1, MAX_LEVELS)

        val ballsToShoot = when {
            level <= 5 -> 6 + level
            level <= 20 -> 10 + (level - 5) / 2
            level <= 50 -> 16 + (level - 20) / 4
            level <= 80 -> 18 + (level - 50) / 5
            else -> 20 + (level - 80) / 4
        }

        // Initial pins pre-attached to the wheel
        val initialPinCount = when {
            level <= 3 -> 2
            level <= 10 -> 3
            level <= 25 -> 4
            level <= 50 -> 5
            level <= 75 -> 6
            else -> 7
        }

        // Generate evenly distributed initial angles with slight level-specific offsets
        val step = 360f / initialPinCount
        val initialAngles = (0 until initialPinCount).map { i ->
            val offset = (level * 13f) % 360f
            (i * step + offset) % 360f
        }

        val baseSpeed = when {
            level <= 10 -> 55f + level * 3f
            level <= 30 -> 80f + (level - 10) * 2f
            level <= 60 -> 120f + (level - 30) * 1.5f
            level <= 85 -> 155f + (level - 60) * 1.8f
            else -> 190f + (level - 85) * 2.5f
        }

        val direction = if (level % 2 == 0) 1f else -1f
        val rotationSpeed = baseSpeed * direction

        val reverses = level >= 15
        val reverseIntervalMs = when {
            level >= 80 -> 2200L
            level >= 50 -> 2800L
            level >= 25 -> 3500L
            reverses -> 4200L
            else -> 0L
        }

        val oscillation = level % 5 == 0 && level >= 20

        return AALevelConfig(
            levelNumber = level,
            ballsToShoot = ballsToShoot,
            initialPinnedAngles = initialAngles,
            rotationSpeed = rotationSpeed,
            reversesDirection = reverses,
            reverseIntervalMs = reverseIntervalMs,
            oscillation = oscillation
        )
    }
}
