package com.example.unblockme.core.model

enum class Orientation {
    HORIZONTAL,
    VERTICAL
}

enum class UnblockDifficulty(val title: String) {
    EASY("Easy"),
    NORMAL("Normal"),
    HARD("Hard"),
    VERY_HARD("Very Hard"),
    EXPERT("Expert");

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
    fun occupies(r: Int, c: Int): Boolean {
        return if (orientation == Orientation.HORIZONTAL) {
            r == row && c >= col && c < col + length
        } else {
            c == col && r >= row && r < row + length
        }
    }
}

data class UnblockLevel(
    val levelNumber: Int,
    val difficulty: UnblockDifficulty,
    val blocks: List<Block>,
    val minMoves: Int
)

data class UnblockState(
    val levelNumber: Int = 1,
    val difficulty: UnblockDifficulty = UnblockDifficulty.EASY,
    val blocks: List<Block> = emptyList(),
    val moveCount: Int = 0,
    val minMoves: Int = 0,
    val isWon: Boolean = false,
    val stars: Int = 0,
    val moveHistory: List<List<Block>> = emptyList(),
    val hintMove: Pair<String, Int>? = null
)
