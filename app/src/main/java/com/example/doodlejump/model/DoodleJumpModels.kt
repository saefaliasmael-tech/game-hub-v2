package com.example.doodlejump.model

enum class PlatformType {
    STATIC,
    MOVING,
    FRAGILE,
    SPRING
}

data class DoodlePlatform(
    var x: Float, // 0..1
    var y: Float, // world Y
    val width: Float = 0.22f,
    val type: PlatformType = PlatformType.STATIC,
    var vx: Float = 0.004f,
    var isBroken: Boolean = false
)

data class DoodlePlayer(
    var x: Float = 0.5f,
    var y: Float = 200f,
    var vy: Float = -12f,
    var facingRight: Boolean = true
)

data class DoodleJumpState(
    val player: DoodlePlayer = DoodlePlayer(),
    val platforms: List<DoodlePlatform> = emptyList(),
    val cameraY: Float = 0f,
    val score: Int = 0,
    val bestScore: Int = 0,
    val isGameOver: Boolean = false
)
