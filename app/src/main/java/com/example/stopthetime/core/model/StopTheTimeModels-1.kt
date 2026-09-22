package com.example.stopthetime.core.model

enum class StopAccuracy(val title: String, val maxDifferenceMs: Long, val baseScore: Int) {
    PERFECT("PERFECT!", 50L, 1000),
    EXCELLENT("EXCELLENT", 150L, 750),
    GREAT("GREAT", 300L, 500),
    GOOD("GOOD", 600L, 250),
    MISS("MISS", Long.MAX_VALUE, 0)
}

enum class StopDifficulty(
    val title: String,
    val thresholdMs3Stars: Long,
    val thresholdMs2Stars: Long,
    val thresholdMs1Star: Long
) {
    EASY("Easy", 120L, 300L, 600L),
    NORMAL("Normal", 80L, 200L, 400L),
    HARD("Hard", 50L, 150L, 300L),
    EXPERT("Expert", 30L, 90L, 200L),
    MASTER("Master", 15L, 50L, 120L);

    companion object {
        fun fromLevelNumber(levelNumber: Int): StopDifficulty {
            return when (levelNumber) {
                in 1..20 -> EASY
                in 21..40 -> NORMAL
                in 41..60 -> HARD
                in 61..80 -> EXPERT
                else -> MASTER
            }
        }
    }
}

enum class StopGameMode(val title: String) {
    CAMPAIGN("Campaign"),
    ENDLESS("Endless"),
    DAILY_CHALLENGE("Daily Challenge"),
    CUSTOM("Custom"),
    MULTIPLAYER("2 Players")
}

data class StopLevelConfig(
    val levelNumber: Int,
    val targetTimeMs: Long,
    val difficulty: StopDifficulty,
    val isBlind: Boolean = false,
    val blindHideAfterMs: Long = 0L,
    val hasSpeedVariation: Boolean = false,
    val hasDistractions: Boolean = false,
    val totalRounds: Int = 1,
    val maxAttempts: Int = 3
)

enum class StopStatePhase {
    IDLE,
    RUNNING,
    STOPPED,
    WON,
    LOST
}

data class StopTheTimeState(
    val mode: StopGameMode = StopGameMode.CAMPAIGN,
    val phase: StopStatePhase = StopStatePhase.IDLE,
    val levelNumber: Int = 1,
    val difficulty: StopDifficulty = StopDifficulty.EASY,
    val targetTimeMs: Long = 5000L,
    val currentTimeMs: Long = 0L,
    val stoppedTimeMs: Long = 0L,
    val differenceMs: Long = 0L,
    val accuracy: StopAccuracy? = null,
    val stars: Int = 0,
    val score: Int = 0,
    val combo: Int = 0,
    val isBlind: Boolean = false,
    val blindHideAfterMs: Long = 0L,
    val isTimerHidden: Boolean = false,
    val attemptsLeft: Int = 3,
    val currentRound: Int = 1,
    val totalRounds: Int = 1,
    val currentPlayer: Int = 1,
    val p1DiffMs: Long = 0L,
    val p2DiffMs: Long = 0L,
    val multiplayerWinner: Int? = null
)
