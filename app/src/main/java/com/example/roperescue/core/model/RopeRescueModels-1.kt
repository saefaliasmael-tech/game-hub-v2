package com.example.roperescue.core.model

import androidx.compose.ui.geometry.Offset

data class Wheel(
    val id: Int,
    val xPercent: Float,
    val yPercent: Float,
    val radiusPercent: Float = 0.045f
)

data class Hazard(
    val id: Int,
    val xPercent: Float,
    val yPercent: Float,
    val radiusPercent: Float = 0.05f,
    val rotation: Float = 0f
)

data class Zipliner(
    val id: Int,
    val progress: Float = 0f, // 0.0 (start platform) to 1.0 (safe zone)
    val isAlive: Boolean = true,
    val isRescued: Boolean = false
)

data class RescueLevelConfig(
    val levelNumber: Int,
    val startX: Float = 0.15f,
    val startY: Float = 0.18f,
    val targetX: Float = 0.85f,
    val targetY: Float = 0.82f,
    val wheels: List<Wheel> = emptyList(),
    val hazards: List<Hazard> = emptyList(),
    val totalHostages: Int = 10,
    val requiredSaved: Int = 7
)

data class RescueGameState(
    val levelNumber: Int = 1,
    val startPos: Offset = Offset(0.15f, 0.18f),
    val targetPos: Offset = Offset(0.85f, 0.82f),
    val wheels: List<Wheel> = emptyList(),
    val hazards: List<Hazard> = emptyList(),
    val ropePivots: List<Offset> = emptyList(),
    val currentRopeEnd: Offset = Offset(0.15f, 0.18f),
    val isRopeAttached: Boolean = false,
    val isDeploying: Boolean = false,
    val totalHostages: Int = 10,
    val remainingAtStart: Int = 10,
    val zipliners: List<Zipliner> = emptyList(),
    val savedCount: Int = 0,
    val lostCount: Int = 0,
    val requiredSaved: Int = 7,
    val isWon: Boolean = false,
    val isGameOver: Boolean = false
)
