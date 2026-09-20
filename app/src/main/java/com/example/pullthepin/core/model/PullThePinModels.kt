package com.example.pullthepin.core.model

import androidx.compose.ui.graphics.Color

enum class PinOrientation {
    HORIZONTAL,
    VERTICAL
}

data class Ball(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var isColored: Boolean,
    var color: Color = Color(0xFFFF5964),
    var isAlive: Boolean = true,
    var inBucket: Boolean = false
)

data class Pin(
    val id: Int,
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val orientation: PinOrientation,
    var isPulled: Boolean = false,
    var pullProgress: Float = 0f // 0.0 to 1.0 (fully removed)
)

data class Bomb(
    val id: Int,
    val x: Float,
    val y: Float,
    val radius: Float = 0.045f,
    var isExploded: Boolean = false
)

data class Wall(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float
)

data class Bucket(
    val x: Float,
    val y: Float,
    val width: Float = 0.3f,
    val height: Float = 0.12f
)

data class PinLevelConfig(
    val levelNumber: Int,
    val balls: List<Ball>,
    val pins: List<Pin>,
    val bombs: List<Bomb>,
    val walls: List<Wall>,
    val bucket: Bucket,
    val requiredBalls: Int
)

data class PinGameState(
    val levelNumber: Int = 1,
    val balls: List<Ball> = emptyList(),
    val pins: List<Pin> = emptyList(),
    val bombs: List<Bomb> = emptyList(),
    val walls: List<Wall> = emptyList(),
    val bucket: Bucket = Bucket(0.35f, 0.85f),
    val totalBalls: Int = 0,
    val collectedCount: Int = 0,
    val requiredCount: Int = 0,
    val isWon: Boolean = false,
    val isGameOver: Boolean = false
)
