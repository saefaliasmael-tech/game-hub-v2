package com.example.unblockme.core.model

enum class Orientation {
    HORIZONTAL,
    VERTICAL
}

enum class UnblockDifficulty(val displayName: String, val levelRange: IntRange) {
    EASY("Easy", 1..20),
    NORMAL("Normal", 21..40),
    HARD("Hard", 41..60),
    VERY_HARD("Very Hard", 61..80),
    EXPERT("Expert", 81..100);

    companion object {
        fun fromLevelNumber(levelNumber: Int): UnblockDifficulty {
            return when (levelNumber) {
                in 1..20 -> EASY
                in 21..40 -> NORMAL
                in 41..60 -> HARD
                in 61..80 -> VERY_HARD
                else -> EXPERT
            }
        }
    }
}

data class Block(
    val id: String,
    val row: Int,
    val col: Int,
    val length: Int,
    val orientation: Orientation,
    val isTarget: Boolean = false
) {
    val isEscaped: Boolean get() = isTarget && col >= 4

    fun occupies(r: Int, c: Int): Boolean {
        return if (orientation == Orientation.HORIZONTAL) {
            r == row && c >= col && c < col + length
        } else {
            c == col && r >= row && r < row + length
        }
    }

    fun getOccupiedCells(): List<Pair<Int, Int>> {
        return (0 until length).map { i ->
            if (orientation == Orientation.HORIZONTAL) Pair(row, col + i)
            else Pair(row + i, col)
        }
    }
}

data class UnblockMove(
    val blockId: String,
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int
)

data class UnblockLevel(
    val levelNumber: Int,
    val difficulty: UnblockDifficulty,
    val blocks: List<Block>,
    val minMoves: Int
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
