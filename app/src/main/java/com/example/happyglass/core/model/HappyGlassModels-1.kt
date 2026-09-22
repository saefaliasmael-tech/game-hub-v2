package com.example.happyglass.core.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

data class WaterDrop(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val radius: Float = 6f,
    var inGlass: Boolean = false,
    var isLost: Boolean = false
)

data class DrawnStroke(
    val points: List<Offset>,
    val length: Float
)

data class LineSegment(
    val start: Offset,
    val end: Offset
)

enum class ObstacleType {
    SOLID_BLOCK,
    ROTATING_WHEEL,
    HAZARD_HOT_PLATE,
    BOUNCER_PAD
}

data class HappyObstacle(
    val bounds: Rect,
    val type: ObstacleType = ObstacleType.SOLID_BLOCK,
    val angleDegrees: Float = 0f
)

data class GlassContainer(
    val centerX: Float,
    val bottomY: Float,
    val width: Float = 140f,
    val height: Float = 160f,
    val wallThickness: Float = 12f
) {
    val leftWallX: Float get() = centerX - width / 2f
    val rightWallX: Float get() = centerX + width / 2f
    val topY: Float get() = bottomY - height
    val waterTargetY: Float get() = bottomY - height * 0.65f
}

data class Faucet(
    val x: Float,
    val y: Float,
    val totalDrops: Int = 80
)

data class HappyGlassLevelConfig(
    val levelNumber: Int,
    val title: String,
    val faucet: Faucet,
    val glass: GlassContainer,
    val obstacles: List<HappyObstacle> = emptyList(),
    val maxInkLength: Float = 1200f,
    val requiredDrops: Int = 35
)

enum class GamePhase {
    DRAWING,
    POURING,
    WON,
    LOST
}

data class HappyGlassState(
    val levelNumber: Int = 1,
    val phase: GamePhase = GamePhase.DRAWING,
    val strokes: List<DrawnStroke> = emptyList(),
    val currentStrokePoints: List<Offset> = emptyList(),
    val totalInkUsed: Float = 0f,
    val maxInkLength: Float = 1200f,
    val drops: List<WaterDrop> = emptyList(),
    val dropsInGlass: Int = 0,
    val dropsLost: Int = 0,
    val requiredDrops: Int = 35,
    val totalDropsToSpawn: Int = 80,
    val spawnedDropsCount: Int = 0,
    val stars: Int = 0,
    val isSadGlass: Boolean = true,
    val waterLevelRatio: Float = 0f
)
