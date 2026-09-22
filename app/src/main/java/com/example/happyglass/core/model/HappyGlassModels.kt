package com.example.happyglass.core.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class WaterParticle(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var inGlass: Boolean = false,
    var isLost: Boolean = false
)

data class WaterTap(
    val x: Float,
    val y: Float,
    val totalWater: Int = 45
)

data class Glass(
    val x: Float, // Top-left x
    val y: Float, // Top-left y
    val width: Float = 0.24f,
    val height: Float = 0.22f,
    val requiredWater: Int = 22
) {
    val fillTargetRatio: Float get() = 0.70f
    val waterLineY: Float get() = y + height * (1f - fillTargetRatio)
}

data class ObstacleLine(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val strokeWidth: Float = 0.02f
)

data class HappyGlassLevelConfig(
    val levelNumber: Int,
    val tap: WaterTap,
    val glass: Glass,
    val obstacles: List<ObstacleLine>,
    val maxInkLength: Float = 1.2f
)

data class HappyGlassGameState(
    val levelNumber: Int = 1,
    val tap: WaterTap = WaterTap(0.5f, 0.15f),
    val glass: Glass = Glass(0.5f, 0.75f),
    val obstacles: List<ObstacleLine> = emptyList(),
    val waterParticles: List<WaterParticle> = emptyList(),
    val drawnPoints: List<Offset> = emptyList(),
    val maxInkLength: Float = 1.2f,
    val usedInkLength: Float = 0f,
    val isSimulating: Boolean = false,
    val dispensedCount: Int = 0,
    val waterInGlassCount: Int = 0,
    val isWon: Boolean = false,
    val isGameOver: Boolean = false,
    val stars: Int = 0
) {
    val inkRemainingRatio: Float
        get() = (1f - (usedInkLength / maxInkLength)).coerceIn(0f, 1f)

    val isGlassHappy: Boolean
        get() = waterInGlassCount >= glass.requiredWater
}
