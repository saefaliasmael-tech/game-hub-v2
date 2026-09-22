package com.example.colorswitch.core.model

import androidx.compose.ui.graphics.Color

enum class SwitchColor(
    val color: Color,
    val symbolName: String,
    val symbolChar: String
) {
    CYAN(Color(0xFF00E5FF), "Circle", "●"),
    YELLOW(Color(0xFFFFD600), "Triangle", "▲"),
    MAGENTA(Color(0xFFFF007F), "Square", "■"),
    PURPLE(Color(0xFF8B5CF6), "Diamond", "◆");

    companion object {
        val ALL = entries
        fun fromIndex(index: Int): SwitchColor = entries[index.coerceIn(0, entries.size - 1)]
    }
}

enum class ObstacleType {
    ROTATING_CIRCLE,
    ROTATING_CROSS,
    HORIZONTAL_BARS,
    CONCENTRIC_RINGS,
    SPINNING_TRIANGLE,
    SPINNING_SQUARE
}

data class ObstacleState(
    val id: Int,
    val type: ObstacleType,
    val centerY: Float,
    val radius: Float = 90f,
    var currentAngle: Float = 0f,
    val rotationSpeed: Float = 60f, // degrees per second
    val rotationDirection: Float = 1f, // 1f = clockwise, -1f = counter-clockwise
    val hasStar: Boolean = true,
    var isStarCollected: Boolean = false,
    val hasColorOrb: Boolean = true,
    var isColorOrbCollected: Boolean = false,
    val nextColor: SwitchColor = SwitchColor.YELLOW,
    var isPassed: Boolean = false
)

data class ColorSwitchBall(
    var y: Float = 0f,
    var velocityY: Float = 0f,
    var color: SwitchColor = SwitchColor.CYAN,
    val radius: Float = 14f
)

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    var alpha: Float = 1f,
    var size: Float = 6f
)

enum class ColorSwitchGameMode(val displayName: String, val description: String) {
    CAMPAIGN("Levels", "100 Progressive levels to conquer"),
    ENDLESS("Endless", "Ascend as high as you can without falling"),
    DAILY_CHALLENGE("Daily Challenge", "Deterministic seed challenge for today"),
    SPEED_CHALLENGE("Speed Mode", "1.5x Speed for true reflex masters"),
    NO_COLOR_CHANGE("Pure Instinct", "Fixed color navigation without change orbs")
}

data class ColorSwitchLevelConfig(
    val levelNumber: Int,
    val targetScore: Int,
    val obstacleCount: Int,
    val speedMultiplier: Float = 1.0f,
    val obstacleTypes: List<ObstacleType>,
    val targetHeight: Float
)

data class ColorSwitchGameState(
    val mode: ColorSwitchGameMode = ColorSwitchGameMode.CAMPAIGN,
    val levelNumber: Int = 1,
    val ball: ColorSwitchBall = ColorSwitchBall(),
    val obstacles: List<ObstacleState> = emptyList(),
    val particles: List<Particle> = emptyList(),
    val cameraY: Float = 0f,
    val score: Int = 0,
    val maxHeightReached: Float = 0f,
    val currentCombo: Int = 0,
    val starsCollected: Int = 0,
    val isGameOver: Boolean = false,
    val isWon: Boolean = false,
    val isPaused: Boolean = false,
    val isStarted: Boolean = false
)
