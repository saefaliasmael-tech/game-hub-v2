package com.example.tombofthemask.model

enum class MaskTileType {
    EMPTY,
    WALL,
    SPIKE,
    DOT,
    COIN,
    EXIT
}

data class MaskLevel(
    val levelNumber: Int,
    val width: Int,
    val height: Int,
    val startX: Int,
    val startY: Int,
    val walls: Set<Pair<Int, Int>>,
    val spikes: Set<Pair<Int, Int>>,
    val dots: Set<Pair<Int, Int>>,
    val coins: Set<Pair<Int, Int>>,
    val exit: Pair<Int, Int>
)

data class TombOfTheMaskState(
    val levelNumber: Int = 1,
    val playerX: Int = 0,
    val playerY: Int = 0,
    val dotsRemaining: Set<Pair<Int, Int>> = emptySet(),
    val coinsRemaining: Set<Pair<Int, Int>> = emptySet(),
    val trail: List<Pair<Int, Int>> = emptyList(),
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val isWon: Boolean = false
)
