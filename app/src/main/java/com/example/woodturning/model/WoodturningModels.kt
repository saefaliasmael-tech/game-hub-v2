package com.example.woodturning.model

data class WoodShavingParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float = 1.0f
)

data class WoodLevel(
    val levelNumber: Int,
    val name: String,
    val targetProfile: FloatArray, // array of radii along 50 segments
    val woodType: String = "Oak"
)

data class WoodturningState(
    val levelNumber: Int = 1,
    val currentRadii: FloatArray = FloatArray(50) { 1.0f },
    val targetRadii: FloatArray = FloatArray(50) { 0.5f },
    val accuracy: Float = 0f,
    val isFinished: Boolean = false,
    val stars: Int = 0
)
