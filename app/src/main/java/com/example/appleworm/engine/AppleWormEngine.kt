package com.example.appleworm.engine

import com.example.appleworm.model.AppleWormLevel
import com.example.appleworm.model.AppleWormState
import com.example.appleworm.model.Direction
import com.example.appleworm.model.GridPos

object AppleWormLevels {
    val levels = listOf(
        // Level 1: Intro to eating apple and exiting into portal
        AppleWormLevel(
            levelNumber = 1,
            width = 8,
            height = 6,
            initialWorm = listOf(GridPos(2, 4), GridPos(1, 4), GridPos(0, 4)),
            apples = listOf(GridPos(4, 4)),
            portal = GridPos(7, 4),
            walls = (0..7).map { GridPos(it, 5) }.toSet() +
                    setOf(GridPos(0, 3), GridPos(0, 2)),
            hint = "Eat the apple to unlock the vortex portal!"
        ),
        // Level 2: Climb a step
        AppleWormLevel(
            levelNumber = 2,
            width = 9,
            height = 7,
            initialWorm = listOf(GridPos(1, 5), GridPos(0, 5)),
            apples = listOf(GridPos(4, 3)),
            portal = GridPos(8, 4),
            walls = (0..3).map { GridPos(it, 6) }.toSet() +
                    (4..8).map { GridPos(it, 5) }.toSet() +
                    setOf(GridPos(3, 4), GridPos(3, 5)),
            hint = "Climb the step by lifting the worm's head."
        ),
        // Level 3: Pit with gravity
        AppleWormLevel(
            levelNumber = 3,
            width = 9,
            height = 8,
            initialWorm = listOf(GridPos(1, 4), GridPos(0, 4)),
            apples = listOf(GridPos(3, 3)),
            portal = GridPos(8, 4),
            walls = setOf(
                GridPos(0, 5), GridPos(1, 5), GridPos(2, 5),
                GridPos(6, 5), GridPos(7, 5), GridPos(8, 5),
                GridPos(3, 7), GridPos(4, 7), GridPos(5, 7)
            ),
            hazards = setOf(GridPos(3, 6), GridPos(4, 6), GridPos(5, 6)),
            hint = "Careful! Spikes line the deep pit. Bridge across."
        ),
        // Level 4: Eat apple to grow and bridge across a wide gap
        AppleWormLevel(
            levelNumber = 4,
            width = 10,
            height = 8,
            initialWorm = listOf(GridPos(2, 5), GridPos(1, 5), GridPos(0, 5)),
            apples = listOf(GridPos(2, 2)),
            portal = GridPos(9, 5),
            walls = setOf(
                GridPos(0, 6), GridPos(1, 6), GridPos(2, 6), GridPos(2, 3), GridPos(1, 3),
                GridPos(7, 6), GridPos(8, 6), GridPos(9, 6)
            ),
            hazards = setOf(GridPos(3, 7), GridPos(4, 7), GridPos(5, 7), GridPos(6, 7)),
            hint = "Reach up for the apple to grow longer, then bridge the chasm."
        ),
        // Level 5: Spiral descent
        AppleWormLevel(
            levelNumber = 5,
            width = 10,
            height = 9,
            initialWorm = listOf(GridPos(1, 2), GridPos(1, 1), GridPos(2, 1)),
            apples = listOf(GridPos(5, 2), GridPos(8, 5)),
            portal = GridPos(1, 7),
            walls = (0..9).map { GridPos(it, 0) }.toSet() +
                    (0..9).map { GridPos(it, 8) }.toSet() +
                    setOf(
                        GridPos(0, 1), GridPos(0, 2), GridPos(0, 3), GridPos(0, 4), GridPos(0, 5), GridPos(0, 6), GridPos(0, 7),
                        GridPos(9, 1), GridPos(9, 2), GridPos(9, 3), GridPos(9, 4), GridPos(9, 5), GridPos(9, 6), GridPos(9, 7),
                        GridPos(3, 3), GridPos(4, 3), GridPos(5, 3), GridPos(6, 3),
                        GridPos(3, 6), GridPos(4, 6), GridPos(5, 6), GridPos(6, 6)
                    ),
            hint = "Two apples to consume! Plan each turn."
        ),
        // Level 6: The Overhang
        AppleWormLevel(
            levelNumber = 6,
            width = 10,
            height = 8,
            initialWorm = listOf(GridPos(3, 5), GridPos(2, 5), GridPos(1, 5)),
            apples = listOf(GridPos(1, 2)),
            portal = GridPos(8, 2),
            walls = (0..4).map { GridPos(it, 6) }.toSet() +
                    (6..9).map { GridPos(it, 3) }.toSet() +
                    setOf(GridPos(0, 5), GridPos(0, 4), GridPos(0, 3)),
            hazards = (0..9).map { GridPos(it, 7) }.toSet(),
            hint = "Use your tail as an anchor to prevent falling."
        )
    )
}

class AppleWormEngine {
    fun initState(level: AppleWormLevel): AppleWormState {
        return AppleWormState(
            levelNumber = level.levelNumber,
            worm = level.initialWorm,
            applesRemaining = level.apples.toSet(),
            portalPos = level.portal,
            portalOpen = level.apples.isEmpty(),
            isWon = false,
            isGameOver = false,
            movesCount = 0,
            history = listOf(level.initialWorm)
        )
    }

    fun move(current: AppleWormState, level: AppleWormLevel, direction: Direction): AppleWormState {
        if (current.isWon || current.isGameOver) return current

        val head = current.worm.first()
        val newHead = GridPos(head.x + direction.dx, head.y + direction.dy)

        // Out of level bounds check (horizontal)
        if (newHead.x < 0 || newHead.x >= level.width || newHead.y < 0) {
            return current
        }

        // Check wall collision
        if (level.walls.contains(newHead)) {
            return current
        }

        // Cannot reverse directly into neck
        if (current.worm.size > 1 && newHead == current.worm[1]) {
            return current
        }

        // Self collision check (excluding current tail if not growing)
        val willEat = current.applesRemaining.contains(newHead)
        val bodyToCheck = if (willEat) current.worm else current.worm.dropLast(1)
        if (bodyToCheck.contains(newHead)) {
            return current
        }

        // Build new worm segments
        val newWorm = if (willEat) {
            listOf(newHead) + current.worm
        } else {
            listOf(newHead) + current.worm.dropLast(1)
        }

        val remainingApples = current.applesRemaining - setOf(newHead)
        val portalNowOpen = remainingApples.isEmpty()

        // Apply gravity if unsupported
        val settledWorm = applyGravity(newWorm, level)

        // Check hazard / fall out of bottom
        val touchedHazard = settledWorm.any { level.hazards.contains(it) || it.y >= level.height }
        if (touchedHazard) {
            return current.copy(
                worm = settledWorm,
                applesRemaining = remainingApples,
                portalOpen = portalNowOpen,
                isGameOver = true,
                movesCount = current.movesCount + 1
            )
        }

        // Check if head reached portal
        val reachedPortal = portalNowOpen && settledWorm.first() == level.portal
        val isWon = reachedPortal

        return current.copy(
            worm = settledWorm,
            applesRemaining = remainingApples,
            portalOpen = portalNowOpen,
            isWon = isWon,
            isGameOver = false,
            movesCount = current.movesCount + 1,
            history = current.history + listOf(settledWorm)
        )
    }

    private fun applyGravity(worm: List<GridPos>, level: AppleWormLevel): List<GridPos> {
        var currentWorm = worm
        while (true) {
            // Check if ANY segment is supported by ground/wall or bottom border
            val isSupported = currentWorm.any { seg ->
                val below = GridPos(seg.x, seg.y + 1)
                level.walls.contains(below) || (!currentWorm.contains(below) && level.hazards.contains(below))
            }
            if (isSupported) break

            // Fall one tile down
            val fallen = currentWorm.map { GridPos(it.x, it.y + 1) }
            currentWorm = fallen

            // If completely fallen out of screen, stop
            if (currentWorm.all { it.y >= level.height }) break
        }
        return currentWorm
    }

    fun undo(current: AppleWormState): AppleWormState {
        if (current.history.size <= 1) return current
        val prevWorm = current.history[current.history.size - 2]
        return current.copy(
            worm = prevWorm,
            isWon = false,
            isGameOver = false,
            history = current.history.dropLast(1)
        )
    }
}
