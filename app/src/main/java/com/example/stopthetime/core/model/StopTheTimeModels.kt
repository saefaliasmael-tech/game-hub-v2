package com.example.stopthetime.core.model

import androidx.compose.ui.graphics.Color

enum class StopAccuracy(
    val title: String,
    val titleAr: String,
    val maxDifferenceMs: Long,
    val baseScore: Int,
    val defaultStars: Int,
    val color: Color
) {
    PERFECT("PERFECT!", "مثالي!", 20L, 1000, 3, Color(0xFF10B981)),
    EXCELLENT("EXCELLENT!", "ممتاز!", 50L, 800, 3, Color(0xFF06B6D4)),
    GREAT("GREAT!", "رائع!", 150L, 600, 2, Color(0xFF3B82F6)),
    GOOD("GOOD", "جيد", 300L, 400, 1, Color(0xFFF59E0B)),
    MISS("MISS", "فائت", Long.MAX_VALUE, 50, 0, Color(0xFFEF4444))
}

enum class StopDifficulty(val displayName: String, val thresholdMs3Stars: Long, val thresholdMs2Stars: Long, val thresholdMs1Star: Long) {
    EASY("Easy", 40L, 120L, 250L),
    NORMAL("Normal", 30L, 90L, 200L),
    HARD("Hard", 20L, 60L, 150L),
    EXPERT("Expert", 15L, 45L, 100L),
    MASTER("Master", 10L, 30L, 60L)
}

enum class StopGameMode(val displayName: String, val description: String) {
    CAMPAIGN("Campaign", "100 Progressive levels of precision timing"),
    CLASSIC("Classic", "Select target time with full timer display"),
    BLIND("Blind Mode", "Timer disappears after 1.5s - test internal clock"),
    SPEED_VARIATION("Speed Illusion", "Display pulse alters perception, time stays real"),
    RANDOM("Random Target", "Dynamic random target time each attempt"),
    ONE_ATTEMPT("Sudden Death", "Single attempt to achieve perfection"),
    DAILY_CHALLENGE("Daily Challenge", "Unique deterministic 3-round daily challenge"),
    MULTI_ROUND("Multi-Round", "3 to 5 rounds combined total difference"),
    LOCAL_MULTIPLAYER("Pass & Play", "2-4 Players compete locally on the same device")
}

data class PlayerTurnResult(
    val playerIndex: Int,
    val playerName: String,
    val targetMs: Long,
    val stoppedMs: Long,
    val differenceMs: Long,
    val accuracy: StopAccuracy,
    val score: Int
)

data class StopLevelConfig(
    val levelNumber: Int,
    val targetTimeMs: Long,
    val difficulty: StopDifficulty,
    val isBlind: Boolean = false,
    val blindHideAfterMs: Long = 1500L,
    val hasSpeedVariation: Boolean = false,
    val hasDistractions: Boolean = false,
    val totalRounds: Int = 1,
    val maxAttempts: Int = 3
)

enum class GameStatus {
    IDLE,
    COUNTDOWN,
    RUNNING,
    STOPPED,
    ROUND_COMPLETE,
    LEVEL_WON,
    GAME_OVER
}

data class StopTheTimeGameState(
    val levelNumber: Int = 1,
    val mode: StopGameMode = StopGameMode.CAMPAIGN,
    val difficulty: StopDifficulty = StopDifficulty.EASY,
    val targetTimeMs: Long = 5000L,
    val elapsedTimeMs: Long = 0L,
    val stoppedTimeMs: Long = 0L,
    val differenceMs: Long = 0L,
    val accuracy: StopAccuracy? = null,
    val currentCombo: Int = 0,
    val score: Int = 0,
    val stars: Int = 0,
    val status: GameStatus = GameStatus.IDLE,
    val countdownNumber: Int = 3,
    val isTimerHidden: Boolean = false,
    val currentRound: Int = 1,
    val totalRounds: Int = 1,
    val roundDifferences: List<Long> = emptyList(),
    val multiplayerPlayerCount: Int = 2,
    val currentMultiplayerPlayer: Int = 0,
    val multiplayerResults: List<PlayerTurnResult> = emptyList(),
    val attemptsRemaining: Int = 3
)
