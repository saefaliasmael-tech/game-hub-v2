package com.example.helixjump.model

enum class SectorType {
    NORMAL,
    HAZARD,
    EMPTY,
    FINISH
}

data class HelixSector(
    val startAngle: Float,
    val sweepAngle: Float,
    val type: SectorType
)

data class HelixFloor(
    val floorIndex: Int,
    val sectors: List<HelixSector>
)

data class HelixBall(
    val y: Float = 0f,
    val velocityY: Float = 0f,
    val isSmashing: Boolean = false,
    val currentFloor: Int = 0
)

data class HelixJumpState(
    val ball: HelixBall = HelixBall(),
    val towerRotation: Float = 0f,
    val floors: List<HelixFloor> = emptyList(),
    val score: Int = 0,
    val bestScore: Int = 0,
    val currentLevel: Int = 1,
    val comboStreak: Int = 0,
    val isGameOver: Boolean = false,
    val isWon: Boolean = false
)
