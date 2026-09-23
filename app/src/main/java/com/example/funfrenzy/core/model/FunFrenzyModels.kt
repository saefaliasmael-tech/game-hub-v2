package com.example.funfrenzy.core.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

data class RescueBuddy(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val radius: Float = 20f,
    var isSaved: Boolean = false,
    var isDead: Boolean = false
) {
    val bounds: Rect get() = Rect(x - radius, y - radius, x + radius, y + radius)
}

data class RopeAnchor(
    val id: Int,
    val x: Float,
    val y: Float
)

data class RescueRope(
    val id: Int,
    val anchorId: Int,
    val restLength: Float,
    var isCut: Boolean = false,
    val optimalCutOrder: Int = 1
)

enum class FrenzyHazardType {
    SPIKES,
    SAWBLADE,
    LAVA_PIT
}

data class FrenzyHazard(
    val id: Int,
    val bounds: Rect,
    val type: FrenzyHazardType = FrenzyHazardType.SPIKES
)

data class ExitPortal(
    val bounds: Rect
)

data class FunFrenzyLevelConfig(
    val levelNumber: Int,
    val title: String,
    val buddy: RescueBuddy,
    val anchors: List<RopeAnchor>,
    val ropes: List<RescueRope>,
    val hazards: List<FrenzyHazard>,
    val portal: ExitPortal,
    val timeLimitSeconds: Float = 15f
)

enum class FrenzyGamePhase {
    PLAYING,
    WON,
    LOST
}

data class FunFrenzyState(
    val levelNumber: Int = 1,
    val phase: FrenzyGamePhase = FrenzyGamePhase.PLAYING,
    val buddy: RescueBuddy,
    val anchors: List<RopeAnchor> = emptyList(),
    val ropes: List<RescueRope> = emptyList(),
    val hazards: List<FrenzyHazard> = emptyList(),
    val portal: ExitPortal,
    val timeRemainingSeconds: Float = 15f,
    val hintRopeId: Int? = null,
    val hintsRemaining: Int = 3,
    val extraTimeUsed: Boolean = false,
    val stars: Int = 0
)
