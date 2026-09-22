package com.example.mrbullet.core.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

data class HeroShooter(
    val x: Float,
    val y: Float,
    val aimAngleRad: Float = 0f
)

data class EnemyTarget(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Float = 40f,
    val height: Float = 65f,
    var isAlive: Boolean = true,
    val isArmored: Boolean = false
) {
    val bounds: Rect get() = Rect(x - width / 2f, y - height, x + width / 2f, y)
}

data class TntBarrel(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Float = 36f,
    val height: Float = 44f,
    var isExploded: Boolean = false,
    val explosionRadius: Float = 110f
) {
    val bounds: Rect get() = Rect(x - width / 2f, y - height, x + width / 2f, y)
}

enum class BarrierType {
    METAL_WALL,
    WOOD_CRATE,
    STEEL_BEAM
}

data class MrBulletWall(
    val bounds: Rect,
    val type: BarrierType = BarrierType.METAL_WALL
)

data class ActiveBullet(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float = 5f,
    var bouncesLeft: Int = 6,
    var isAlive: Boolean = true,
    val trail: MutableList<Offset> = mutableListOf()
)

data class MrBulletLevelConfig(
    val levelNumber: Int,
    val title: String,
    val hero: HeroShooter,
    val maxBullets: Int = 3,
    val enemies: List<EnemyTarget>,
    val barrels: List<TntBarrel> = emptyList(),
    val walls: List<MrBulletWall> = emptyList()
)

enum class BulletGamePhase {
    AIMING,
    BULLET_FLYING,
    WON,
    LOST
}

data class MrBulletState(
    val levelNumber: Int = 1,
    val phase: BulletGamePhase = BulletGamePhase.AIMING,
    val bulletsRemaining: Int = 3,
    val maxBullets: Int = 3,
    val aimAngleRad: Float = 0f,
    val isAiming: Boolean = false,
    val trajectoryPoints: List<Offset> = emptyList(),
    val activeBullets: List<ActiveBullet> = emptyList(),
    val enemies: List<EnemyTarget> = emptyList(),
    val barrels: List<TntBarrel> = emptyList(),
    val stars: Int = 0,
    val score: Int = 0
)
