package com.example.sandloop.model

import androidx.compose.ui.geometry.Rect

data class SandParticle(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var isCollected: Boolean = false,
    var isDead: Boolean = false
)

data class SandLoopLevel(
    val levelNumber: Int,
    val initialDirtRects: List<Rect>,
    val stoneBarriers: List<Rect>,
    val containerRect: Rect,
    val sandSpawnX: Float,
    val sandSpawnY: Float,
    val targetCount: Int = 60
)

data class SandLoopState(
    val levelNumber: Int = 1,
    val collectedCount: Int = 0,
    val targetCount: Int = 60,
    val isSpawning: Boolean = true,
    val isWon: Boolean = false,
    val isGameOver: Boolean = false
)
