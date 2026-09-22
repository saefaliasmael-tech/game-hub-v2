package com.example.watersort.core.model

data class GameState(
    val levelNumber: Int,
    val bottles: List<Bottle>,
    val movesCount: Int = 0,
    val maxCapacity: Int = 4,
    val isSolved: Boolean = false,
    val hasExtraBottle: Boolean = false,
    val selectedBottleIndex: Int? = null,
    val lastDelta: PourDelta? = null,
    val isDeadlocked: Boolean = false,
    val optimalMoves: Int = 0,
    val starsEarned: Int = 0
) {
    val totalBottles: Int get() = bottles.size
    val isWon: Boolean get() = isSolved || bottles.all { it.isSolved }
}

sealed interface MoveResult {
    data class Success(val delta: PourDelta, val newGameState: GameState) : MoveResult
    data class Invalid(val reason: InvalidReason) : MoveResult
}

enum class InvalidReason {
    SAME_BOTTLE,
    SOURCE_EMPTY,
    TARGET_FULL,
    COLOR_MISMATCH,
    BOTTLE_ALREADY_SOLVED
}
