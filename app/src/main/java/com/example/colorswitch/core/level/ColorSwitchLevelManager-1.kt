package com.example.colorswitch.core.level

import com.example.colorswitch.core.model.*

object ColorSwitchLevelManager {

    private val levels: List<ColorSwitchLevelConfig> = (1..100).map { levelNum ->
        val count = when (levelNum) {
            in 1..10 -> 2
            in 11..25 -> 3
            in 26..50 -> 4
            in 51..75 -> 5
            in 76..90 -> 6
            else -> 7
        }

        val speed = 0.8f + (levelNum * 0.009f).coerceAtMost(1.2f)

        val types = when {
            levelNum <= 15 -> listOf(ObstacleType.ROTATING_CIRCLE)
            levelNum <= 30 -> listOf(ObstacleType.ROTATING_CIRCLE, ObstacleType.ROTATING_CROSS)
            levelNum <= 50 -> listOf(ObstacleType.ROTATING_CIRCLE, ObstacleType.ROTATING_CROSS, ObstacleType.HORIZONTAL_BARS)
            levelNum <= 75 -> listOf(ObstacleType.ROTATING_CIRCLE, ObstacleType.CONCENTRIC_RINGS, ObstacleType.SPINNING_TRIANGLE)
            else -> listOf(ObstacleType.CONCENTRIC_RINGS, ObstacleType.SPINNING_SQUARE, ObstacleType.ROTATING_CROSS)
        }

        val totalHeight = -(count * 280f) - 120f

        ColorSwitchLevelConfig(
            levelNumber = levelNum,
            targetScore = count,
            obstacleCount = count,
            speedMultiplier = speed,
            obstacleTypes = types,
            targetHeight = totalHeight
        )
    }

    fun getLevel(levelNumber: Int): ColorSwitchLevelConfig {
        val clamped = levelNumber.coerceIn(1, 100)
        return levels[clamped - 1]
    }

    fun getAllLevels(): List<ColorSwitchLevelConfig> = levels

    fun generateObstaclesForLevel(config: ColorSwitchLevelConfig): List<ObstacleState> {
        val obstacles = mutableListOf<ObstacleState>()
        var currentY = -280f

        for (i in 0 until config.obstacleCount) {
            val type = config.obstacleTypes[i % config.obstacleTypes.size]
            val direction = if (i % 2 == 0) 1f else -1f
            val baseSpeed = 50f + (i * 5f)
            val nextCol = SwitchColor.fromIndex((i + 1) % SwitchColor.ALL.size)

            obstacles.add(
                ObstacleState(
                    id = i,
                    type = type,
                    centerY = currentY,
                    radius = if (type == ObstacleType.CONCENTRIC_RINGS) 95f else 85f,
                    currentAngle = (i * 45f) % 360f,
                    rotationSpeed = baseSpeed * config.speedMultiplier,
                    rotationDirection = direction,
                    hasStar = true,
                    hasColorOrb = (i < config.obstacleCount - 1),
                    nextColor = nextCol
                )
            )
            currentY -= 280f
        }
        return obstacles
    }

    fun generateEndlessObstacle(id: Int, centerY: Float): ObstacleState {
        val allTypes = ObstacleType.entries
        val type = allTypes[(Math.random() * allTypes.size).toInt()]
        val direction = if (Math.random() > 0.5) 1f else -1f
        val speed = 55f + (Math.random().toFloat() * 45f)
        val nextCol = SwitchColor.fromIndex((Math.random() * SwitchColor.ALL.size).toInt())

        return ObstacleState(
            id = id,
            type = type,
            centerY = centerY,
            radius = 85f,
            currentAngle = (Math.random().toFloat() * 360f),
            rotationSpeed = speed,
            rotationDirection = direction,
            hasStar = true,
            hasColorOrb = true,
            nextColor = nextCol
        )
    }
}
