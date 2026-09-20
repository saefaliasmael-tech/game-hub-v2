package com.example.mastermind.core.model

import androidx.compose.ui.graphics.Color

/**
 * The 10 available peg colors in Mastermind, with distinct geometric symbols
 * for full Color-Blind Accessibility.
 */
enum class PegColor(
    val displayName: String,
    val color: Color,
    val symbol: String, // Accessible symbol for color-blind mode
    val textColor: Color
) {
    RED("Red", Color(0xFFEF4444), "●", Color.White),
    BLUE("Blue", Color(0xFF3B82F6), "■", Color.White),
    GREEN("Green", Color(0xFF10B981), "▲", Color.White),
    YELLOW("Yellow", Color(0xFFFBBF24), "◆", Color(0xFF1F2937)),
    PURPLE("Purple", Color(0xFF8B5CF6), "★", Color.White),
    ORANGE("Orange", Color(0xFFF97316), "♦", Color.White),
    CYAN("Cyan", Color(0xFF06B6D4), "⬟", Color(0xFF1F2937)),
    PINK("Pink", Color(0xFFEC4899), "✿", Color.White),
    WHITE("White", Color(0xFFF3F4F6), "✖", Color(0xFF1F2937)),
    BROWN("Brown", Color(0xFF92400E), "✚", Color.White);

    companion object {
        fun getPalette(count: Int): List<PegColor> {
            return entries.take(count.coerceIn(4, entries.size))
        }
    }
}

enum class MastermindDifficulty(
    val displayName: String,
    val codeLength: Int,
    val colorCount: Int,
    val maxAttempts: Int,
    val allowDuplicates: Boolean
) {
    EASY("Easy", 4, 5, 12, false),
    NORMAL("Normal", 4, 6, 10, true),
    HARD("Hard", 5, 7, 10, true),
    EXPERT("Expert", 6, 8, 9, true),
    MASTER("Master", 6, 10, 8, true);

    companion object {
        fun fromLevelNumber(level: Int): MastermindDifficulty = when {
            level <= 20 -> EASY
            level <= 45 -> NORMAL
            level <= 70 -> HARD
            level <= 85 -> EXPERT
            else -> MASTER
        }
    }
}

enum class MastermindGameMode {
    CAMPAIGN,
    QUICK_PLAY,
    DAILY_CHALLENGE,
    ENDLESS
}

data class Feedback(
    val exactMatches: Int, // Correct color in correct position (Red/Black peg)
    val colorMatches: Int  // Correct color in wrong position (White peg)
) {
    val totalMatches: Int get() = exactMatches + colorMatches
}

data class GuessRow(
    val guess: List<PegColor>,
    val feedback: Feedback? = null
)

data class MastermindLevelConfig(
    val levelNumber: Int,
    val difficulty: MastermindDifficulty,
    val codeLength: Int,
    val colorCount: Int,
    val maxAttempts: Int,
    val allowDuplicates: Boolean,
    val targetSecret: List<PegColor>? = null
)

data class MastermindGameState(
    val levelNumber: Int = 1,
    val mode: MastermindGameMode = MastermindGameMode.CAMPAIGN,
    val difficulty: MastermindDifficulty = MastermindDifficulty.EASY,
    val secretCode: List<PegColor> = emptyList(),
    val attempts: List<GuessRow> = emptyList(),
    val currentGuess: List<PegColor?> = emptyList(),
    val maxAttempts: Int = 10,
    val isWon: Boolean = false,
    val isGameOver: Boolean = false,
    val hintsUsed: Int = 0,
    val eliminatedColors: Set<PegColor> = emptySet(),
    val revealedPositions: Map<Int, PegColor> = emptyMap(),
    val hintMessage: String? = null,
    val startTimeMs: Long = System.currentTimeMillis(),
    val elapsedTimeSeconds: Int = 0,
    val starsAwarded: Int = 0,
    val score: Int = 0
) {
    val currentAttemptNumber: Int get() = attempts.size + 1
    val attemptsRemaining: Int get() = (maxAttempts - attempts.size).coerceAtLeast(0)
    val isCurrentGuessComplete: Boolean get() = currentGuess.none { it == null }
}
