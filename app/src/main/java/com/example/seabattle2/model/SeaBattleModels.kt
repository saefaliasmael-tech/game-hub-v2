package com.example.seabattle2.model

enum class CellStatus {
    EMPTY,
    SHIP,
    MISS,
    HIT,
    SUNK
}

data class Ship(
    val id: Int,
    val size: Int,
    val cells: List<Pair<Int, Int>>,
    var hits: Int = 0
) {
    val isSunk: Boolean get() = hits >= size
}

enum class BattlePhase {
    PLACEMENT,
    PLAYER_TURN,
    AI_TURN,
    GAME_OVER
}

data class SeaBattleState(
    val phase: BattlePhase = BattlePhase.PLACEMENT,
    val playerGrid: Array<IntArray> = Array(10) { IntArray(10) { CellStatus.EMPTY.ordinal } },
    val enemyGrid: Array<IntArray> = Array(10) { IntArray(10) { CellStatus.EMPTY.ordinal } },
    val playerShips: List<Ship> = emptyList(),
    val enemyShips: List<Ship> = emptyList(),
    val isPlayerWinner: Boolean = false,
    val shotsFired: Int = 0,
    val hitsScored: Int = 0,
    val aiTargetQueue: MutableList<Pair<Int, Int>> = mutableListOf()
)
