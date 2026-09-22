package com.example.pullthepin.core.level

import androidx.compose.ui.graphics.Color
import com.example.pullthepin.core.model.*
import kotlin.random.Random

object PullThePinLevelManager {

    const val MAX_LEVELS = 100

    private val COLORS = listOf(
        Color(0xFFFF5964),
        Color(0xFF35A7FF),
        Color(0xFFFFE74C),
        Color(0xFF10B981),
        Color(0xFF8B5CF6)
    )

    fun getLevel(levelNumber: Int): PinLevelConfig {
        val level = levelNumber.coerceIn(1, MAX_LEVELS)
        val rng = Random(level * 4243L)

        val totalBallCount = (12 + (level % 15)).coerceAtMost(28)
        val hasGreyBalls = level >= 2
        val hasBombs = level >= 3

        val balls = mutableListOf<Ball>()

        // Split balls into colored and grey
        val coloredCount = if (hasGreyBalls) (totalBallCount * 0.45f).toInt().coerceAtLeast(4) else totalBallCount
        val greyCount = totalBallCount - coloredCount

        var ballId = 0

        // Colored balls in left or top chamber
        val leftChamberX = 0.32f
        val leftChamberY = 0.22f
        for (i in 0 until coloredCount) {
            val ox = (rng.nextFloat() - 0.5f) * 0.12f
            val oy = (rng.nextFloat() - 0.5f) * 0.08f
            balls.add(
                Ball(
                    id = ballId++,
                    x = leftChamberX + ox,
                    y = leftChamberY + oy,
                    isColored = true,
                    color = COLORS[(ballId + level) % COLORS.size]
                )
            )
        }

        // Grey balls in right or middle chamber
        val rightChamberX = 0.68f
        val rightChamberY = 0.36f
        for (i in 0 until greyCount) {
            val ox = (rng.nextFloat() - 0.5f) * 0.12f
            val oy = (rng.nextFloat() - 0.5f) * 0.08f
            balls.add(
                Ball(
                    id = ballId++,
                    x = rightChamberX + ox,
                    y = rightChamberY + oy,
                    isColored = false,
                    color = Color(0xFF9E9E9E)
                )
            )
        }

        // Chamber walls
        val walls = mutableListOf<Wall>()

        // Outer pipe walls
        walls.add(Wall(0.2f, 0.12f, 0.2f, 0.62f))   // Left outer wall
        walls.add(Wall(0.8f, 0.12f, 0.8f, 0.62f))   // Right outer wall

        // Funnel toward bucket
        walls.add(Wall(0.2f, 0.62f, 0.35f, 0.82f))  // Left funnel slope
        walls.add(Wall(0.8f, 0.62f, 0.65f, 0.82f))  // Right funnel slope

        // Divider wall between left and right upper chambers
        walls.add(Wall(0.5f, 0.12f, 0.5f, 0.42f))

        // Pins
        val pins = mutableListOf<Pin>()
        // Pin 1: Under left chamber
        pins.add(
            Pin(
                id = 1,
                x1 = 0.18f,
                y1 = 0.32f,
                x2 = 0.52f,
                y2 = 0.32f,
                orientation = PinOrientation.HORIZONTAL
            )
        )

        // Pin 2: Under right chamber
        pins.add(
            Pin(
                id = 2,
                x1 = 0.48f,
                y1 = 0.46f,
                x2 = 0.82f,
                y2 = 0.46f,
                orientation = PinOrientation.HORIZONTAL
            )
        )

        // Pin 3: Gate before the funnel/bucket
        pins.add(
            Pin(
                id = 3,
                x1 = 0.2f,
                y1 = 0.62f,
                x2 = 0.8f,
                y2 = 0.62f,
                orientation = PinOrientation.HORIZONTAL
            )
        )

        // Additional vertical pin for higher levels
        if (level >= 5) {
            pins.add(
                Pin(
                    id = 4,
                    x1 = 0.5f,
                    y1 = 0.42f,
                    x2 = 0.5f,
                    y2 = 0.62f,
                    orientation = PinOrientation.VERTICAL
                )
            )
        }

        // Bombs in isolated pockets
        val bombs = mutableListOf<Bomb>()
        if (hasBombs) {
            val bombX = if (level % 2 == 0) 0.35f else 0.65f
            val bombY = 0.52f
            bombs.add(Bomb(id = 1, x = bombX, y = bombY))
        }

        val bucket = Bucket(x = 0.35f, y = 0.84f, width = 0.3f, height = 0.12f)
        val requiredBalls = (totalBallCount * 0.75f).toInt().coerceAtLeast(3)

        return PinLevelConfig(
            levelNumber = level,
            balls = balls,
            pins = pins,
            bombs = bombs,
            walls = walls,
            bucket = bucket,
            requiredBalls = requiredBalls
        )
    }
}
