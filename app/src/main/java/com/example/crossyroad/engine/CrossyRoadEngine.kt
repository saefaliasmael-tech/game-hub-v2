package com.example.crossyroad.engine

import com.example.crossyroad.model.*
import kotlin.random.Random

class CrossyRoadEngine {
    companion object {
        const val COLS = 9
    }

    fun generateInitialRows(): List<CrossyRow> {
        val rows = mutableListOf<CrossyRow>()
        // First 3 rows safe grass
        for (i in 0..2) {
            rows.add(CrossyRow(rowIndex = i, type = RowType.GRASS))
        }
        for (i in 3..25) {
            rows.add(generateRow(i))
        }
        return rows
    }

    fun generateRow(index: Int): CrossyRow {
        val random = Random.Default
        val type = when {
            index % 6 == 0 -> RowType.GRASS
            index % 7 == 0 -> RowType.RAIL
            random.nextFloat() < 0.45f -> RowType.ROAD
            else -> RowType.RIVER
        }

        val row = CrossyRow(rowIndex = index, type = type)
        val speed = (0.005f + random.nextFloat() * 0.008f) * if (random.nextBoolean()) 1f else -1f

        when (type) {
            RowType.ROAD -> {
                // Spawn 2-3 cars
                row.obstacles.add(Obstacle(x = 0.1f, width = 0.18f, speed = speed))
                row.obstacles.add(Obstacle(x = 0.6f, width = 0.18f, speed = speed))
            }
            RowType.RIVER -> {
                // Spawn 2-3 logs
                val logSpeed = (0.004f + random.nextFloat() * 0.006f) * if (random.nextBoolean()) 1f else -1f
                row.obstacles.add(Obstacle(x = 0.1f, width = 0.28f, speed = logSpeed))
                row.obstacles.add(Obstacle(x = 0.65f, width = 0.28f, speed = logSpeed))
            }
            RowType.RAIL -> {
                // Trains spawn periodically
            }
            RowType.GRASS -> {}
        }
        return row
    }

    fun update(
        state: CrossyRoadState,
        onCollision: (String) -> Unit
    ): CrossyRoadState {
        if (state.isGameOver) return state

        val rows = state.rows.toMutableList()
        val player = state.player
        val random = Random.Default

        // Ensure rows ahead of player exist
        val maxRow = rows.maxOfOrNull { it.rowIndex } ?: 0
        if (player.y + 15 > maxRow) {
            for (i in (maxRow + 1)..(player.y + 20)) {
                rows.add(generateRow(i))
            }
        }

        // Update obstacles across all active rows
        for (row in rows) {
            for (obs in row.obstacles) {
                obs.x += obs.speed
                if (obs.speed > 0 && obs.x > 1.2f) {
                    obs.x = -0.3f
                } else if (obs.speed < 0 && obs.x < -0.3f) {
                    obs.x = 1.2f
                }
            }

            // Rail logic
            if (row.type == RowType.RAIL) {
                if (row.obstacles.isEmpty() && random.nextFloat() < 0.015f) {
                    // Spawn high-speed train
                    val dir = if (random.nextBoolean()) 1f else -1f
                    val startX = if (dir > 0) -0.5f else 1.5f
                    row.obstacles.add(Obstacle(x = startX, width = 0.5f, speed = dir * 0.035f))
                    row.trainWarning = true
                } else if (row.obstacles.isNotEmpty()) {
                    val train = row.obstacles.first()
                    if (train.x < -0.6f || train.x > 1.6f) {
                        row.obstacles.clear()
                        row.trainWarning = false
                    }
                }
            }
        }

        // Collision Check for current player row
        val currentRow = rows.find { it.rowIndex == player.y }
        val playerNormX = (player.x + 0.5f) / COLS

        if (currentRow != null) {
            when (currentRow.type) {
                RowType.ROAD -> {
                    val hitCar = currentRow.obstacles.any { obs ->
                        playerNormX in (obs.x - 0.04f)..(obs.x + obs.width + 0.04f)
                    }
                    if (hitCar) {
                        onCollision("Hit by a car!")
                        return state.copy(isGameOver = true, causeOfDeath = "Flattened by traffic!")
                    }
                }
                RowType.RAIL -> {
                    val hitTrain = currentRow.obstacles.any { obs ->
                        playerNormX in (obs.x - 0.05f)..(obs.x + obs.width + 0.05f)
                    }
                    if (hitTrain) {
                        onCollision("Run over by train!")
                        return state.copy(isGameOver = true, causeOfDeath = "Zapped by high-speed express train!")
                    }
                }
                RowType.RIVER -> {
                    // Must be on a log!
                    val onLog = currentRow.obstacles.find { obs ->
                        playerNormX in (obs.x - 0.02f)..(obs.x + obs.width + 0.02f)
                    }
                    if (onLog == null) {
                        // Sunk in water!
                        onCollision("Drowned!")
                        return state.copy(isGameOver = true, causeOfDeath = "Splashed into the roaring river!")
                    }
                }
                RowType.GRASS -> {
                    // Safe
                }
            }
        }

        return state.copy(rows = rows)
    }

    fun hop(state: CrossyRoadState, dx: Int, dy: Int): CrossyRoadState {
        if (state.isGameOver) return state

        val newX = (state.player.x + dx).coerceIn(0, COLS - 1)
        val newY = (state.player.y + dy).coerceAtLeast(0)
        val newScore = maxOf(state.score, newY)

        return state.copy(
            player = state.player.copy(x = newX, y = newY),
            score = newScore
        )
    }
}
