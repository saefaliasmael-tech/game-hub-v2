package com.example.holeio.model

import androidx.compose.ui.graphics.Color

data class CityObject(
    val id: Int,
    var x: Float,
    var y: Float,
    val radius: Float,
    val points: Int,
    val color: Color,
    val name: String,
    var isEaten: Boolean = false,
    var sinkScale: Float = 1.0f
)

data class HoleEntity(
    val id: Int,
    val name: String,
    val color: Color,
    var x: Float,
    var y: Float,
    var radius: Float = 28f,
    var score: Int = 0,
    var targetX: Float = 0f,
    var targetY: Float = 0f
)

data class HoleIoState(
    val playerHole: HoleEntity,
    val botHoles: List<HoleEntity>,
    val objects: List<CityObject>,
    val timeLeftSeconds: Int = 60,
    val isGameOver: Boolean = false,
    val bestScore: Int = 0
)
