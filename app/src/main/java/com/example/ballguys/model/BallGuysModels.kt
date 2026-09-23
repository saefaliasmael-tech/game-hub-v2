package com.example.ballguys.model

import androidx.compose.ui.graphics.Color

data class BallTier(
    val level: Int,
    val name: String,
    val radius: Float,
    val color: Color,
    val points: Int,
    val eyeEmoji: String = "👀"
)

object BallTiers {
    val tiers = listOf(
        BallTier(1, "Cherry", 16f, Color(0xFFFF1744), 2, "🍒"),
        BallTier(2, "Berry", 23f, Color(0xFFE91E63), 4, "🍓"),
        BallTier(3, "Grape", 31f, Color(0xFF9C27B0), 8, "🍇"),
        BallTier(4, "Orange", 40f, Color(0xFFFF9800), 16, "🍊"),
        BallTier(5, "Apple", 50f, Color(0xFF4CAF50), 32, "🍏"),
        BallTier(6, "Peach", 60f, Color(0xFFFF8A80), 64, "🍑"),
        BallTier(7, "Pineapple", 72f, Color(0xFFFFEB3B), 128, "🍍"),
        BallTier(8, "Watermelon", 85f, Color(0xFF2E7D32), 256, "🍉"),
        BallTier(9, "King Ball", 100f, Color(0xFFFFD700), 512, "👑")
    )
}

data class BallGuy(
    val id: Long,
    var tierLevel: Int,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var radius: Float,
    var isMerged: Boolean = false
)

data class BallGuysState(
    val balls: List<BallGuy> = emptyList(),
    val currentDropTier: Int = 1,
    val nextDropTier: Int = 1,
    val dropAimX: Float = 0.5f,
    val score: Int = 0,
    val bestScore: Int = 0,
    val isGameOver: Boolean = false
)
