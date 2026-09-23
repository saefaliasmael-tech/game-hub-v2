package com.example.paperio2.model

import androidx.compose.ui.graphics.Color

enum class OwnerId {
    NONE,
    PLAYER,
    BOT1,
    BOT2,
    BOT3
}

data class Entity(
    val id: OwnerId,
    val name: String,
    val color: Color,
    val trailColor: Color,
    var x: Float, // grid coords
    var y: Float,
    var vx: Float,
    var vy: Float,
    var trail: MutableList<Pair<Int, Int>> = mutableListOf(),
    var isAlive: Boolean = true,
    var territoryPercent: Float = 0f
)

data class PaperIoState(
    val player: Entity,
    val bots: List<Entity>,
    val gridOwner: Array<IntArray>, // 50x50 grid with OwnerId.ordinal
    val kills: Int = 0,
    val isGameOver: Boolean = false,
    val bestPercent: Float = 0f
)
