package com.example.game2048.core.model

enum class MoveDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT
}

data class Game2048State(
    val board: List<Int> = List(16) { 0 },
    val score: Int = 0,
    val bestScore: Int = 0,
    val isWon: Boolean = false,
    val hasContinuedAfterWin: Boolean = false,
    val isGameOver: Boolean = false,
    val moveCount: Int = 0
) {
    fun getTile(row: Int, col: Int): Int = board[row * 4 + col]
}
