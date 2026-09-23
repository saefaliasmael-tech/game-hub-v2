package com.example.seabattle2.engine

import com.example.seabattle2.model.BattlePhase
import com.example.seabattle2.model.CellStatus
import com.example.seabattle2.model.SeaBattleState
import com.example.seabattle2.model.Ship
import kotlin.random.Random

class SeaBattleEngine {
    companion object {
        const val GRID_SIZE = 10
        val FLEET_SIZES = listOf(4, 3, 3, 2, 2, 2, 1, 1, 1, 1)
    }

    fun initGame(): SeaBattleState {
        val playerShips = generateFleet()
        val enemyShips = generateFleet()

        val playerGrid = Array(GRID_SIZE) { IntArray(GRID_SIZE) { CellStatus.EMPTY.ordinal } }
        for (ship in playerShips) {
            for (c in ship.cells) {
                playerGrid[c.first][c.second] = CellStatus.SHIP.ordinal
            }
        }

        val enemyGrid = Array(GRID_SIZE) { IntArray(GRID_SIZE) { CellStatus.EMPTY.ordinal } }

        return SeaBattleState(
            phase = BattlePhase.PLACEMENT,
            playerGrid = playerGrid,
            enemyGrid = enemyGrid,
            playerShips = playerShips,
            enemyShips = enemyShips,
            isPlayerWinner = false,
            shotsFired = 0,
            hitsScored = 0
        )
    }

    fun generateFleet(): List<Ship> {
        val random = Random.Default
        val occupied = mutableSetOf<Pair<Int, Int>>()
        val ships = mutableListOf<Ship>()
        var shipId = 0

        for (size in FLEET_SIZES) {
            var placed = false
            var attempts = 0
            while (!placed && attempts < 200) {
                attempts++
                val horizontal = random.nextBoolean()
                val x = if (horizontal) random.nextInt(GRID_SIZE - size + 1) else random.nextInt(GRID_SIZE)
                val y = if (!horizontal) random.nextInt(GRID_SIZE - size + 1) else random.nextInt(GRID_SIZE)

                val cells = mutableListOf<Pair<Int, Int>>()
                for (i in 0 until size) {
                    val cx = if (horizontal) x + i else x
                    val cy = if (!horizontal) y + i else y
                    cells.add(cx to cy)
                }

                // Check collision and 1-cell buffer margin
                val valid = cells.none { c ->
                    for (dx in -1..1) {
                        for (dy in -1..1) {
                            if (occupied.contains((c.first + dx) to (c.second + dy))) return@none true
                        }
                    }
                    false
                }

                if (valid) {
                    occupied.addAll(cells)
                    ships.add(Ship(id = shipId++, size = size, cells = cells))
                    placed = true
                }
            }
        }
        return ships
    }

    fun playerFire(
        state: SeaBattleState,
        x: Int,
        y: Int,
        onHit: () -> Unit,
        onMiss: () -> Unit,
        onShipSunk: () -> Unit
    ): SeaBattleState {
        if (state.phase != BattlePhase.PLAYER_TURN) return state
        if (x !in 0 until GRID_SIZE || y !in 0 until GRID_SIZE) return state

        val curStatus = state.enemyGrid[x][y]
        if (curStatus != CellStatus.EMPTY.ordinal) return state // already shot

        // Check if enemy ship is at this cell
        val hitShip = state.enemyShips.find { ship -> ship.cells.contains(x to y) }

        var hitsAdd = 0
        val isHit = hitShip != null

        if (isHit) {
            state.enemyGrid[x][y] = CellStatus.HIT.ordinal
            hitShip.hits++
            hitsAdd = 1
            onHit()

            if (hitShip.isSunk) {
                for (c in hitShip.cells) {
                    state.enemyGrid[c.first][c.second] = CellStatus.SUNK.ordinal
                }
                onShipSunk()
            }
        } else {
            state.enemyGrid[x][y] = CellStatus.MISS.ordinal
            onMiss()
        }

        val allEnemySunk = state.enemyShips.all { it.isSunk }
        if (allEnemySunk) {
            return state.copy(
                phase = BattlePhase.GAME_OVER,
                isPlayerWinner = true,
                shotsFired = state.shotsFired + 1,
                hitsScored = state.hitsScored + hitsAdd
            )
        }

        return state.copy(
            phase = BattlePhase.AI_TURN,
            shotsFired = state.shotsFired + 1,
            hitsScored = state.hitsScored + hitsAdd
        )
    }

    fun aiFire(
        state: SeaBattleState,
        onHit: () -> Unit,
        onMiss: () -> Unit,
        onShipSunk: () -> Unit
    ): SeaBattleState {
        val random = Random.Default

        // Pick target: from target queue (hunting around hits) or random unshot
        var target: Pair<Int, Int>? = null
        while (state.aiTargetQueue.isNotEmpty()) {
            val candidate = state.aiTargetQueue.removeAt(0)
            if (candidate.first in 0 until GRID_SIZE &&
                candidate.second in 0 until GRID_SIZE &&
                (state.playerGrid[candidate.first][candidate.second] == CellStatus.EMPTY.ordinal ||
                        state.playerGrid[candidate.first][candidate.second] == CellStatus.SHIP.ordinal)
            ) {
                target = candidate
                break
            }
        }

        if (target == null) {
            // Find all unshot coordinates
            val unshot = mutableListOf<Pair<Int, Int>>()
            for (x in 0 until GRID_SIZE) {
                for (y in 0 until GRID_SIZE) {
                    val status = state.playerGrid[x][y]
                    if (status == CellStatus.EMPTY.ordinal || status == CellStatus.SHIP.ordinal) {
                        unshot.add(x to y)
                    }
                }
            }
            if (unshot.isNotEmpty()) {
                target = unshot[random.nextInt(unshot.size)]
            }
        }

        if (target == null) return state

        val (tx, ty) = target
        val hitShip = state.playerShips.find { it.cells.contains(tx to ty) }
        val isHit = hitShip != null

        if (isHit) {
            state.playerGrid[tx][ty] = CellStatus.HIT.ordinal
            hitShip.hits++
            onHit()

            if (hitShip.isSunk) {
                for (c in hitShip.cells) {
                    state.playerGrid[c.first][c.second] = CellStatus.SUNK.ordinal
                }
                onShipSunk()
            } else {
                // Add adjacent cells to hunting queue
                val adj = listOf(tx - 1 to ty, tx + 1 to ty, tx to ty - 1, tx to ty + 1)
                state.aiTargetQueue.addAll(adj)
            }
        } else {
            state.playerGrid[tx][ty] = CellStatus.MISS.ordinal
            onMiss()
        }

        val allPlayerSunk = state.playerShips.all { it.isSunk }
        if (allPlayerSunk) {
            return state.copy(
                phase = BattlePhase.GAME_OVER,
                isPlayerWinner = false
            )
        }

        return state.copy(phase = BattlePhase.PLAYER_TURN)
    }
}
