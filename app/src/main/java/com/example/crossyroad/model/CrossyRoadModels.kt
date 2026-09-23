package com.example.crossyroad.model

enum class RowType {
    GRASS,
    ROAD,
    RIVER,
    RAIL
}

data class Obstacle(
    var x: Float, // 0..1
    val width: Float,
    val speed: Float // positive = right, negative = left
)

data class CrossyRow(
    val rowIndex: Int,
    val type: RowType,
    val obstacles: MutableList<Obstacle> = mutableListOf(),
    var trainWarning: Boolean = false
)

data class CrossyPlayer(
    var x: Int = 4, // 0..8 grid columns
    var y: Int = 0  // row index (progress)
)

data class CrossyRoadState(
    val player: CrossyPlayer = CrossyPlayer(),
    val rows: List<CrossyRow> = emptyList(),
    val score: Int = 0,
    val bestScore: Int = 0,
    val isGameOver: Boolean = false,
    val causeOfDeath: String = ""
)
