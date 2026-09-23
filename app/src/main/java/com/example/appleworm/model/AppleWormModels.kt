package com.example.appleworm.model

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0)
}

enum class TileType {
    EMPTY,
    WALL,
    APPLE,
    PORTAL,
    HAZARD
}

data class GridPos(val x: Int, val y: Int)

data class AppleWormLevel(
    val levelNumber: Int,
    val width: Int,
    val height: Int,
    val initialWorm: List<GridPos>,
    val apples: List<GridPos>,
    val portal: GridPos,
    val walls: Set<GridPos>,
    val hazards: Set<GridPos> = emptySet(),
    val hint: String = ""
)

data class AppleWormState(
    val levelNumber: Int = 1,
    val worm: List<GridPos> = emptyList(),
    val applesRemaining: Set<GridPos> = emptySet(),
    val portalPos: GridPos = GridPos(0, 0),
    val portalOpen: Boolean = false,
    val isWon: Boolean = false,
    val isGameOver: Boolean = false,
    val movesCount: Int = 0,
    val history: List<List<GridPos>> = emptyList()
)
