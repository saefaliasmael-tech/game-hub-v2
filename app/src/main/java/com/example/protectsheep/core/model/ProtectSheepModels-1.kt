package com.example.protectsheep.core.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

data class SheepTarget(
    val id: Int,
    var x: Float,
    var y: Float,
    val radius: Float = 18f,
    var isAlive: Boolean = true
) {
    val bounds: Rect get() = Rect(x - radius, y - radius, x + radius, y + radius)
}

enum class HazardType {
    WOLF,
    BEE_SWARM,
    FALLING_BOULDER
}

data class WolfAttacker(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val speed: Float = 2.4f,
    val radius: Float = 16f,
    val type: HazardType = HazardType.WOLF
) {
    val bounds: Rect get() = Rect(x - radius, y - radius, x + radius, y + radius)
}

data class BarrierStroke(
    val points: List<Offset>,
    val length: Float
)

data class ProtectSheepLevelConfig(
    val levelNumber: Int,
    val title: String,
    val sheepList: List<SheepTarget>,
    val wolves: List<WolfAttacker>,
    val maxInkLength: Float = 950f,
    val surviveSeconds: Float = 8f
)

enum class SheepGamePhase {
    DRAWING_BARRIER,
    SURVIVING,
    WON,
    LOST
}

data class ProtectSheepState(
    val levelNumber: Int = 1,
    val phase: SheepGamePhase = SheepGamePhase.DRAWING_BARRIER,
    val strokes: List<BarrierStroke> = emptyList(),
    val currentStrokePoints: List<Offset> = emptyList(),
    val totalInkUsed: Float = 0f,
    val maxInkLength: Float = 950f,
    val sheepList: List<SheepTarget> = emptyList(),
    val wolves: List<WolfAttacker> = emptyList(),
    val timeRemainingSeconds: Float = 8f,
    val totalSurviveSeconds: Float = 8f,
    val stars: Int = 0
)
