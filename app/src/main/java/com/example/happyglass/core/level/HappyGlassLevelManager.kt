package com.example.happyglass.core.level

import com.example.happyglass.core.model.*
import kotlin.random.Random

object HappyGlassLevelManager {

    const val MAX_LEVELS = 100

    fun getLevel(levelNumber: Int): HappyGlassLevelConfig {
        val lvl = levelNumber.coerceIn(1, MAX_LEVELS)
        val rng = Random(lvl * 5923L + 17L)

        // Determine layout archetypes based on level number
        val tapX = when (lvl % 4) {
            0 -> 0.25f
            1 -> 0.50f
            2 -> 0.75f
            else -> 0.35f
        }
        val tapY = 0.12f

        val glassX = when (lvl % 5) {
            0 -> 0.65f
            1 -> 0.38f
            2 -> 0.15f
            3 -> 0.72f
            else -> 0.50f
        }
        val glassY = 0.72f

        val glass = Glass(
            x = glassX,
            y = glassY,
            width = 0.22f,
            height = 0.20f,
            requiredWater = 20
        )

        val tap = WaterTap(
            x = tapX,
            y = tapY,
            totalWater = 40
        )

        val obstacles = mutableListOf<ObstacleLine>()

        when {
            lvl == 1 -> {
                // Intro: Open space, simple guide
                // No obstacle, player simply draws a funnel line
            }
            lvl == 2 -> {
                // Single central barrier between tap and glass
                obstacles.add(ObstacleLine(0.35f, 0.45f, 0.65f, 0.45f))
            }
            lvl == 3 -> {
                // Slanted slide obstacle
                obstacles.add(ObstacleLine(0.2f, 0.35f, 0.6f, 0.5f))
            }
            lvl == 4 -> {
                // V-shaped obstacle directing water away
                obstacles.add(ObstacleLine(0.5f, 0.35f, 0.3f, 0.5f))
                obstacles.add(ObstacleLine(0.5f, 0.35f, 0.7f, 0.5f))
            }
            lvl == 5 -> {
                // Split platforms
                obstacles.add(ObstacleLine(0.15f, 0.38f, 0.45f, 0.42f))
                obstacles.add(ObstacleLine(0.55f, 0.52f, 0.85f, 0.48f))
            }
            else -> {
                // Procedural generation according to level tiers
                val numObstacles = (1 + (lvl % 5)).coerceAtMost(5)
                val baseY = 0.30f
                val spacingY = 0.10f

                for (i in 0 until numObstacles) {
                    val y = baseY + i * spacingY
                    val isSlanted = rng.nextBoolean()
                    val startX = 0.12f + rng.nextFloat() * 0.35f
                    val length = 0.20f + rng.nextFloat() * 0.25f
                    val endX = (startX + length).coerceAtMost(0.88f)
                    val endY = if (isSlanted) y + (rng.nextFloat() - 0.5f) * 0.10f else y

                    obstacles.add(ObstacleLine(startX, y, endX, endY))
                }
            }
        }

        // Ink budget scales with puzzle complexity
        val maxInk = (1.1f + (lvl % 3) * 0.25f).coerceAtMost(2.0f)

        return HappyGlassLevelConfig(
            levelNumber = lvl,
            tap = tap,
            glass = glass,
            obstacles = obstacles,
            maxInkLength = maxInk
        )
    }
}
