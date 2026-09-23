package com.example.paperio2.engine

import androidx.compose.ui.graphics.Color
import com.example.paperio2.model.Entity
import com.example.paperio2.model.OwnerId
import com.example.paperio2.model.PaperIoState
import kotlin.math.abs
import kotlin.random.Random

class PaperIoEngine {
    companion object {
        const val GRID_SIZE = 45
        const val SPEED = 0.45f
    }

    fun initState(): PaperIoState {
        val grid = Array(GRID_SIZE) { IntArray(GRID_SIZE) { OwnerId.NONE.ordinal } }

        // Spawn bases (3x3 blocks)
        claimInitialBase(grid, 10, 10, OwnerId.PLAYER.ordinal)
        claimInitialBase(grid, 35, 10, OwnerId.BOT1.ordinal)
        claimInitialBase(grid, 10, 35, OwnerId.BOT2.ordinal)
        claimInitialBase(grid, 35, 35, OwnerId.BOT3.ordinal)

        val player = Entity(
            id = OwnerId.PLAYER,
            name = "You",
            color = Color(0xFF00E5FF),
            trailColor = Color(0xFF80D8FF),
            x = 11.5f,
            y = 11.5f,
            vx = SPEED,
            vy = 0f
        )

        val bots = listOf(
            Entity(
                id = OwnerId.BOT1,
                name = "Viper",
                color = Color(0xFFFF1744),
                trailColor = Color(0xFFFF8A80),
                x = 36.5f,
                y = 11.5f,
                vx = -SPEED,
                vy = 0f
            ),
            Entity(
                id = OwnerId.BOT2,
                name = "Blaze",
                color = Color(0xFFFFEA00),
                trailColor = Color(0xFFFFFF8D),
                x = 11.5f,
                y = 36.5f,
                vx = SPEED,
                vy = 0f
            ),
            Entity(
                id = OwnerId.BOT3,
                name = "Phantom",
                color = Color(0xFFD500F9),
                trailColor = Color(0xFFEA80FC),
                x = 36.5f,
                y = 36.5f,
                vx = 0f,
                vy = -SPEED
            )
        )

        val state = PaperIoState(
            player = player,
            bots = bots,
            gridOwner = grid,
            kills = 0,
            isGameOver = false
        )
        updateTerritoryPercentages(state)
        return state
    }

    private fun claimInitialBase(grid: Array<IntArray>, startX: Int, startY: Int, ownerId: Int) {
        for (x in startX until startX + 4) {
            for (y in startY until startY + 4) {
                if (x in 0 until GRID_SIZE && y in 0 until GRID_SIZE) {
                    grid[x][y] = ownerId
                }
            }
        }
    }

    fun update(
        state: PaperIoState,
        playerInputVx: Float,
        playerInputVy: Float,
        onKill: () -> Unit,
        onPlayerDied: () -> Unit
    ): PaperIoState {
        if (state.isGameOver) return state

        val grid = state.gridOwner
        val player = state.player
        val bots = state.bots
        val random = Random.Default

        // Apply player steering
        if (playerInputVx != 0f || playerInputVy != 0f) {
            // Cannot instantly reverse into self
            if (!(playerInputVx == -player.vx && playerInputVy == -player.vy)) {
                player.vx = playerInputVx * SPEED
                player.vy = playerInputVy * SPEED
            }
        }

        val allEntities = listOf(player) + bots
        var killsAdd = 0
        var playerDied = false

        // Update each entity position
        for (entity in allEntities) {
            if (!entity.isAlive) continue

            // Bot simple AI: steer towards empty space or loop back to base
            if (entity.id != OwnerId.PLAYER) {
                if (random.nextFloat() < 0.08f) {
                    val turn = if (random.nextBoolean()) 1 else -1
                    if (entity.vx != 0f) {
                        entity.vy = turn * SPEED
                        entity.vx = 0f
                    } else {
                        entity.vx = turn * SPEED
                        entity.vy = 0f
                    }
                }
            }

            entity.x = (entity.x + entity.vx).coerceIn(1f, (GRID_SIZE - 2).toFloat())
            entity.y = (entity.y + entity.vy).coerceIn(1f, (GRID_SIZE - 2).toFloat())

            val cellX = entity.x.toInt()
            val cellY = entity.y.toInt()

            val currentTileOwner = grid[cellX][cellY]

            if (currentTileOwner == entity.id.ordinal) {
                // Inside own territory
                if (entity.trail.isNotEmpty()) {
                    // Closed loop! Claim territory enclosed by trail
                    claimLoopTerritory(grid, entity.trail, entity.id.ordinal)
                    entity.trail.clear()
                    if (entity.id == OwnerId.PLAYER) {
                        onKill()
                    }
                }
            } else {
                // Outside territory: extend trail
                val lastPos = entity.trail.lastOrNull()
                if (lastPos == null || lastPos != (cellX to cellY)) {
                    // Check self collision with own trail
                    if (entity.trail.contains(cellX to cellY)) {
                        entity.isAlive = false
                        clearTerritory(grid, entity.id.ordinal)
                        if (entity.id == OwnerId.PLAYER) playerDied = true
                        continue
                    }
                    entity.trail.add(cellX to cellY)
                }
            }
        }

        // Trail cutting collisions (entities cutting other entities' trails)
        for (attacker in allEntities) {
            if (!attacker.isAlive) continue
            val aCell = attacker.x.toInt() to attacker.y.toInt()

            for (victim in allEntities) {
                if (!victim.isAlive || victim.id == attacker.id) continue
                if (victim.trail.contains(aCell)) {
                    // Victim eliminated!
                    victim.isAlive = false
                    victim.trail.clear()
                    clearTerritory(grid, victim.id.ordinal)
                    if (attacker.id == OwnerId.PLAYER) {
                        killsAdd++
                        onKill()
                    }
                    if (victim.id == OwnerId.PLAYER) {
                        playerDied = true
                    }
                }
            }
        }

        if (playerDied || !player.isAlive) {
            onPlayerDied()
            return state.copy(isGameOver = true)
        }

        updateTerritoryPercentages(state)

        return state.copy(kills = state.kills + killsAdd)
    }

    private fun claimLoopTerritory(grid: Array<IntArray>, trail: List<Pair<Int, Int>>, ownerOrdinal: Int) {
        if (trail.isEmpty()) return
        // Find bounding box of the trail and fill
        var minX = GRID_SIZE
        var maxX = 0
        var minY = GRID_SIZE
        var maxY = 0

        for (pt in trail) {
            grid[pt.first][pt.second] = ownerOrdinal
            minX = minOf(minX, pt.first)
            maxX = maxOf(maxX, pt.first)
            minY = minOf(minY, pt.second)
            maxY = maxOf(maxY, pt.second)
        }

        // Fill bounding box interior
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                grid[x][y] = ownerOrdinal
            }
        }
    }

    private fun clearTerritory(grid: Array<IntArray>, ownerOrdinal: Int) {
        for (x in 0 until GRID_SIZE) {
            for (y in 0 until GRID_SIZE) {
                if (grid[x][y] == ownerOrdinal) {
                    grid[x][y] = OwnerId.NONE.ordinal
                }
            }
        }
    }

    private fun updateTerritoryPercentages(state: PaperIoState) {
        val totalCells = (GRID_SIZE * GRID_SIZE).toFloat()
        val counts = IntArray(OwnerId.values().size)

        for (x in 0 until GRID_SIZE) {
            for (y in 0 until GRID_SIZE) {
                counts[state.gridOwner[x][y]]++
            }
        }

        state.player.territoryPercent = (counts[OwnerId.PLAYER.ordinal] / totalCells) * 100f
        for (bot in state.bots) {
            bot.territoryPercent = (counts[bot.id.ordinal] / totalCells) * 100f
        }
    }
}
