package com.example.watersort.core.model

data class PourMove(
    val fromBottleIndex: Int,
    val toBottleIndex: Int
)

data class PourDelta(
    val fromBottleIndex: Int,
    val toBottleIndex: Int,
    val color: LiquidColor,
    val amountPoured: Int
)

enum class Difficulty(
    val title: String,
    val colorCount: Int,
    val bottleCount: Int,
    val emptyBottleCount: Int,
    val capacity: Int
) {
    EASY("Easy", colorCount = 4, bottleCount = 6, emptyBottleCount = 2, capacity = 4),
    NORMAL("Normal", colorCount = 6, bottleCount = 8, emptyBottleCount = 2, capacity = 4),
    HARD("Hard", colorCount = 8, bottleCount = 10, emptyBottleCount = 2, capacity = 4),
    EXPERT("Expert", colorCount = 10, bottleCount = 12, emptyBottleCount = 2, capacity = 4),
    MASTER("Master", colorCount = 11, bottleCount = 14, emptyBottleCount = 3, capacity = 4);

    companion object {
        fun forLevel(levelNumber: Int): Difficulty {
            return when {
                levelNumber <= 5 -> EASY
                levelNumber <= 25 -> NORMAL
                levelNumber <= 75 -> HARD
                levelNumber <= 150 -> EXPERT
                else -> MASTER
            }
        }
    }
}

enum class GameMode {
    STORY,
    DAILY_CHALLENGE
}
