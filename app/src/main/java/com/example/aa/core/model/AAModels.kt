package com.example.aa.core.model

data class PinnedBall(
    val id: Int,
    val angleDegrees: Float, // Angle around the center (0 - 360)
    val number: Int
)

data class ShootingBall(
    val id: Int,
    val number: Int,
    val progress: Float // 0f = launch position at bottom, 1f = reached center target edge
)

data class AALevelConfig(
    val levelNumber: Int,
    val ballsToShoot: Int,
    val initialPinnedAngles: List<Float>,
    val rotationSpeed: Float, // degrees per second (positive = clockwise, negative = counter-clockwise)
    val reversesDirection: Boolean = false,
    val reverseIntervalMs: Long = 0L,
    val oscillation: Boolean = false
)

data class AAGameState(
    val levelNumber: Int = 1,
    val currentAngle: Float = 0f,
    val pinnedBalls: List<PinnedBall> = emptyList(),
    val remainingBallNumbers: List<Int> = emptyList(),
    val shootingBall: ShootingBall? = null,
    val isGameOver: Boolean = false,
    val isWon: Boolean = false,
    val isPaused: Boolean = false,
    val collisionAngle: Float? = null,
    val score: Int = 0
)
