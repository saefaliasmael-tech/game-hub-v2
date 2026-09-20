package com.example.unblockme.core.model

enum class Orientation {
    HORIZONTAL,
    VERTICAL
}

data class Block(
    val id: String,
    val row: Int,       // 0..5
    val col: Int,       // 0..5
    val length: Int,    // 2 or 3
    val orientation: Orientation,
    val isTarget: Boolean = false
) {
    /**
     * All (r, c) cells occupied by this block.
     */
    fun getOccupiedCells(): List<Pair<Int, Int>> {
        return if (orientation == Orientation.HORIZONTAL) {
            (0 until length).map { Pair(row, col + it) }
        } else {
            (0 until length).map { Pair(row + it, col) }
        }
    }

    /**
     * Checks if target block has reached the right exit (row 2, col 4 or 5).
     */
    val isEscaped: Boolean
        get() = isTarget && orientation == Orientation.HORIZONTAL && row == 2 && (col + length >= 6)
}

enum class UnblockDifficulty(val displayName: String, val levelRange: IntRange) {
    EASY("Easy", 1..20),
    NORMAL("Normal", 21..40),
    HARD("Hard", 41..60),
    VERY_HARD("Very Hard", 61..80),
    EXPERT("Expert", 81..100);

    companion object {
        fun fromLevelNumber(level: Int): UnblockDifficulty = when {
            level <= 20 -> EASY
            level <= 40 -> NORMAL
            level <= 60 -> HARD
            level <= 80 -> VERY_HARD
            else -> EXPERT
        }
    }
}

data class UnblockLevel(
    val levelNumber: Int,
    val difficulty: UnblockDifficulty,
    val blocks: List<Block>,
    val minMoves: Int
)

data class UnblockMove(
    val blockId: String,
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int
)

data class UnblockGameState(
    val levelNumber: Int = 1,
    val difficulty: UnblockDifficulty = UnblockDifficulty.EASY,
    val blocks: List<Block> = emptyList(),
    val minMoves: Int = 0,
    val movesCount: Int = 0,
    val moveHistory: List<UnblockMove> = emptyList(),
    val isWon: Boolean = false,
    val hintsUsed: Int = 0,
    val highlightedBlockId: String? = null,
    val hintMessage: String? = null,
    val elapsedTimeSeconds: Int = 0,
    val starsAwarded: Int = 0,
    val bestMoves: Int = 0
)
