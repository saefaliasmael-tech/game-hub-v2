package com.example.iqboost.model

enum class MiniGameType {
    MEMORY_MATRIX,
    SPEED_MATH,
    STROOP_COLOR,
    NUMBER_PATTERN,
    REFLEX_SPEED
}

data class SpeedMathQuestion(
    val equation: String,
    val options: List<Int>,
    val correctIndex: Int
)

data class StroopQuestion(
    val wordText: String,
    val textColorName: String,
    val colorValue: androidx.compose.ui.graphics.Color,
    val isMatch: Boolean
)

data class NumberPatternQuestion(
    val sequence: String,
    val options: List<Int>,
    val correctIndex: Int
)

data class IQBoostState(
    val currentMiniGame: MiniGameType = MiniGameType.MEMORY_MATRIX,
    val roundIndex: Int = 1,
    val totalRounds: Int = 5,
    val score: Int = 0,
    val bestScore: Int = 0,
    val isRoundComplete: Boolean = false,
    val isGameFinished: Boolean = false,

    // Memory Matrix state
    val matrixHighlighted: Set<Int> = emptySet(),
    val matrixSelected: Set<Int> = emptySet(),
    val isMatrixShowingPattern: Boolean = true,

    // Speed Math state
    val mathQuestion: SpeedMathQuestion? = null,

    // Stroop state
    val stroopQuestion: StroopQuestion? = null,

    // Number Pattern state
    val patternQuestion: NumberPatternQuestion? = null,

    // Reflex state
    val reflexState: ReflexPhase = ReflexPhase.WAITING,
    val reflexTimeMs: Long = 0L
)

enum class ReflexPhase {
    WAITING,
    READY_TO_TAP,
    RESULT
}
