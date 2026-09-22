package com.example.funfrenzy.core.model

import androidx.compose.ui.graphics.Color

enum class MicroGameType {
    TAP_RUSH,
    CATCH_FALLING,
    POP_BALLOONS,
    DODGE_ROCKS,
    STOP_NEEDLE,
    CUT_WIRE,
    FIND_ODD_ONE
}

enum class MicroResult {
    PENDING,
    SUCCESS,
    FAIL
}

data class BalloonItem(
    val id: Int,
    val x: Float,
    val y: Float,
    val color: Color,
    val isPopped: Boolean = false
)

data class RockItem(
    val id: Int,
    val x: Float,
    var y: Float,
    val speed: Float = 0.6f
)

data class WireItem(
    val colorName: String,
    val color: Color,
    val isCut: Boolean = false
)

data class MicroSubState(
    // 1. Tap rush
    val currentTaps: Int = 0,
    val requiredTaps: Int = 10,

    // 2. Catch falling
    val bucketX: Float = 0.5f,
    val gemX: Float = 0.5f,
    val gemY: Float = 0.05f,
    val gemSpeed: Float = 0.45f,

    // 3. Pop balloons
    val balloons: List<BalloonItem> = emptyList(),
    val poppedCount: Int = 0,
    val requiredPops: Int = 3,

    // 4. Dodge rocks
    val playerX: Float = 0.5f,
    val rocks: List<RockItem> = emptyList(),

    // 5. Stop needle
    val needleAngle: Float = 0f,
    val needleSpeed: Float = 260f,
    val targetZoneStartAngle: Float = 140f,
    val targetZoneEndAngle: Float = 220f,
    val isNeedleStopped: Boolean = false,

    // 6. Cut wire
    val wires: List<WireItem> = emptyList(),
    val targetWireColorName: String = "Red",

    // 7. Find odd one
    val totalGridItems: Int = 9,
    val oddIndex: Int = 4,
    val normalSymbol: String = "🍎",
    val oddSymbol: String = "🍒",
    val selectedIndex: Int? = null
)

data class MicroGameSpec(
    val type: MicroGameType,
    val prompt: String,
    val durationSec: Float = 4f
)

data class FunFrenzyLevelConfig(
    val levelNumber: Int,
    val speedMultiplier: Float = 1f,
    val microGames: List<MicroGameSpec>
)

data class FunFrenzyGameState(
    val levelNumber: Int = 1,
    val microIndex: Int = 0,
    val totalMicroGames: Int = 5,
    val currentSpec: MicroGameSpec = MicroGameSpec(MicroGameType.TAP_RUSH, "TAP RAPIDLY!", 4f),
    val timeLeftSec: Float = 4f,
    val totalDurationSec: Float = 4f,
    val lives: Int = 3,
    val maxLives: Int = 3,
    val subState: MicroSubState = MicroSubState(),
    val microResult: MicroResult = MicroResult.PENDING,
    val isIntermission: Boolean = false,
    val isStageWon: Boolean = false,
    val isStageOver: Boolean = false,
    val stars: Int = 3
) {
    val progressRatio: Float
        get() = if (totalDurationSec > 0f) (timeLeftSec / totalDurationSec).coerceIn(0f, 1f) else 0f
}
