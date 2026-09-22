package com.example.ropearound.core.model

import androidx.compose.ui.geometry.Offset

data class Peg(
    val id: Int,
    val xPercent: Float,
    val yPercent: Float,
    val radiusPercent: Float = 0.045f,
    val isWrapped: Boolean = false
)

data class RopeObstacle(
    val id: Int,
    val xPercent: Float,
    val yPercent: Float,
    val radiusPercent: Float = 0.05f
)

data class RopeLevelConfig(
    val levelNumber: Int,
    val anchorXPercent: Float,
    val anchorYPercent: Float,
    val pegs: List<Peg>,
    val obstacles: List<RopeObstacle> = emptyList(),
    val maxRopeLengthRatio: Float = 3.5f // Ratio relative to screen width
)

data class RopeGameState(
    val levelNumber: Int = 1,
    val pegs: List<Peg> = emptyList(),
    val obstacles: List<RopeObstacle> = emptyList(),
    val pivotPoints: List<Offset> = emptyList(), // Normalized 0..1 coordinates of rope joints
    val currentTouchPos: Offset = Offset.Zero,
    val isDragging: Boolean = false,
    val isWon: Boolean = false,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val ropeLengthPercent: Float = 0f,
    val maxRopeLength: Float = 3.5f
)
