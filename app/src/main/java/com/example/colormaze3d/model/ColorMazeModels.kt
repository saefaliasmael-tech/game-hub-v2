package com.example.colormaze3d.model

import androidx.compose.ui.graphics.Color

data class MazeTile(
    val x: Int,
    val y: Int,
    val isWall: Boolean
)

data class ColorMazeLevel(
    val levelNumber: Int,
    val width: Int,
    val height: Int,
    val walls: Set<Pair<Int, Int>>,
    val startPos: Pair<Int, Int>,
    val paintColor: Color,
    val wallColor: Color
)

data class ColorMazeState(
    val levelNumber: Int = 1,
    val playerX: Int = 0,
    val playerY: Int = 0,
    val paintedTiles: Set<Pair<Int, Int>> = emptySet(),
    val totalFloorTiles: Int = 1,
    val movesCount: Int = 0,
    val isRolling: Boolean = false,
    val isWon: Boolean = false
) {
    val completionPercentage: Float
        get() = if (totalFloorTiles > 0) (paintedTiles.size.toFloat() / totalFloorTiles).coerceIn(0f, 1f) else 0f
}
