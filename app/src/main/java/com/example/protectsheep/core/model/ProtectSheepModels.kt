package com.example.protectsheep.core.model

import androidx.compose.ui.geometry.Offset

data class Sheep(
    val id: Int,
    var x: Float,
    var y: Float,
    val radius: Float = 0.05f,
    var isStung: Boolean = false
)

data class Hive(
    val id: Int,
    val x: Float,
    val y: Float,
    val beeCount: Int = 12
)

data class Bee(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float = 0.015f
)

data class Hazard(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val isSpike: Boolean = false
)

data class ProtectSheepLevelConfig(
    val levelNumber: Int,
    val sheepList: List<Sheep>,
    val hives: List<Hive>,
    val hazards: List<Hazard>,
    val maxInk: Float = 1.5f
)

data class ProtectSheepGameState(
    val levelNumber: Int = 1,
    val sheepList: List<Sheep> = emptyList(),
    val hives: List<Hive> = emptyList(),
    val hazards: List<Hazard> = emptyList(),
    val bees: List<Bee> = emptyList(),
    val drawnPoints: List<Offset> = emptyList(),
    val maxInk: Float = 1.5f,
    val usedInk: Float = 0f,
    val isSimulating: Boolean = false,
    val survivalTimeLeftSec: Float = 10f,
    val isWon: Boolean = false,
    val isGameOver: Boolean = false,
    val stars: Int = 0
) {
    val inkRemainingRatio: Float
        get() = ((maxInk - usedInk) / maxInk).coerceIn(0f, 1f)
}
