package com.example.ropearound.core.level

import com.example.ropearound.core.model.Peg
import com.example.ropearound.core.model.RopeLevelConfig
import com.example.ropearound.core.model.RopeObstacle
import kotlin.math.cos
import kotlin.math.sin

object RopeAroundLevelManager {

    const val MAX_LEVELS = 100

    fun getLevel(levelNumber: Int): RopeLevelConfig {
        val level = levelNumber.coerceIn(1, MAX_LEVELS)

        val pegCount = when {
            level <= 5 -> 3 + level
            level <= 20 -> 5 + (level - 5) / 4
            level <= 50 -> 7 + (level - 20) / 6
            else -> 9 + (level - 50) / 8
        }.coerceAtMost(14)

        val pegs = mutableListOf<Peg>()
        val obstacles = mutableListOf<RopeObstacle>()

        val layoutType = (level - 1) % 5
        val centerX = 0.5f
        val centerY = 0.5f

        when (layoutType) {
            0 -> {
                // Polygon ring
                val radius = 0.30f
                for (i in 0 until pegCount) {
                    val angle = Math.toRadians((i * (360.0 / pegCount)).toDouble())
                    pegs.add(
                        Peg(
                            id = i + 1,
                            xPercent = (centerX + radius * cos(angle)).toFloat(),
                            yPercent = (centerY + radius * sin(angle)).toFloat()
                        )
                    )
                }
                if (level >= 10) {
                    obstacles.add(RopeObstacle(id = 1, xPercent = centerX, yPercent = centerY, radiusPercent = 0.07f))
                }
            }
            1 -> {
                // Dual Columns
                val half = pegCount / 2
                for (i in 0 until half) {
                    val y = 0.25f + (i.toFloat() / (half - 1).coerceAtLeast(1)) * 0.50f
                    pegs.add(Peg(id = i * 2 + 1, xPercent = 0.25f, yPercent = y))
                    pegs.add(Peg(id = i * 2 + 2, xPercent = 0.75f, yPercent = y))
                }
                if (level >= 15) {
                    obstacles.add(RopeObstacle(id = 1, xPercent = 0.50f, yPercent = 0.50f, radiusPercent = 0.06f))
                }
            }
            2 -> {
                // Star pattern
                for (i in 0 until pegCount) {
                    val isOuter = i % 2 == 0
                    val r = if (isOuter) 0.34f else 0.18f
                    val angle = Math.toRadians((i * (360.0 / pegCount)).toDouble())
                    pegs.add(
                        Peg(
                            id = i + 1,
                            xPercent = (centerX + r * cos(angle)).toFloat(),
                            yPercent = (centerY + r * sin(angle)).toFloat()
                        )
                    )
                }
                if (level >= 25) {
                    obstacles.add(RopeObstacle(id = 1, xPercent = 0.50f, yPercent = 0.35f, radiusPercent = 0.05f))
                    obstacles.add(RopeObstacle(id = 2, xPercent = 0.50f, yPercent = 0.65f, radiusPercent = 0.05f))
                }
            }
            3 -> {
                // Triangle / Pyramidal cluster
                var idx = 1
                val rows = 4
                for (r in 0 until rows) {
                    val countInRow = r + 1
                    for (c in 0 until countInRow) {
                        if (pegs.size < pegCount) {
                            val x = 0.5f + (c - countInRow / 2.0f + 0.5f) * 0.20f
                            val y = 0.20f + r * 0.18f
                            pegs.add(Peg(id = idx++, xPercent = x, yPercent = y))
                        }
                    }
                }
            }
            else -> {
                // S-Curve or Spiral
                for (i in 0 until pegCount) {
                    val t = i.toFloat() / (pegCount - 1).coerceAtLeast(1)
                    val x = 0.20f + 0.60f * t
                    val y = 0.50f + 0.25f * sin(t * Math.PI * 2.0).toFloat()
                    pegs.add(Peg(id = i + 1, xPercent = x, yPercent = y))
                }
                if (level >= 30) {
                    obstacles.add(RopeObstacle(id = 1, xPercent = 0.50f, yPercent = 0.50f, radiusPercent = 0.06f))
                }
            }
        }

        val anchorX = pegs.firstOrNull()?.xPercent ?: 0.5f
        val anchorY = (pegs.firstOrNull()?.yPercent ?: 0.2f) - 0.08f

        val maxLen = 2.5f + pegCount * 0.35f

        return RopeLevelConfig(
            levelNumber = level,
            anchorXPercent = anchorX,
            anchorYPercent = anchorY,
            pegs = pegs,
            obstacles = obstacles,
            maxRopeLengthRatio = maxLen
        )
    }
}
