package com.example.mrbullet.core.model

import androidx.compose.ui.geometry.Offset

data class Enemy(
    val id: Int,
    var x: Float,
    var y: Float,
    val width: Float = 0.08f,
    val height: Float = 0.12f,
    var isDead: Boolean = false
)

data class Wall(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val isDestructible: Boolean = false,
    var isDestroyed: Boolean = false,
    val isTnt: Boolean = false
)

data class Bullet(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var bouncesLeft: Int = 4,
    var isAlive: Boolean = true
)

data class MrBulletLevelConfig(
    val levelNumber: Int,
    val heroX: Float,
    val heroY: Float,
    val maxBullets: Int,
    val enemies: List<Enemy>,
    val walls: List<Wall>
)

data class MrBulletGameState(
    val levelNumber: Int = 1,
    val heroX: Float = 0.15f,
    val heroY: Float = 0.75f,
    val bulletsLeft: Int = 3,
    val maxBullets: Int = 3,
    val activeBullets: List<Bullet> = emptyList(),
    val enemies: List<Enemy> = emptyList(),
    val walls: List<Wall> = emptyList(),
    val aimAngle: Float? = null,
    val isWon: Boolean = false,
    val isGameOver: Boolean = false,
    val stars: Int = 0
)
