package com.example.colormaze3d.engine

import androidx.compose.ui.graphics.Color
import com.example.colormaze3d.model.ColorMazeLevel
import com.example.colormaze3d.model.ColorMazeState

object ColorMazeLevels {
    val levels = listOf(
        // Level 1: Simple Cross
        ColorMazeLevel(
            levelNumber = 1,
            width = 5,
            height = 5,
            walls = setOf(
                0 to 0, 0 to 1, 0 to 3, 0 to 4,
                4 to 0, 4 to 1, 4 to 3, 4 to 4,
                2 to 0, 2 to 4
            ),
            startPos = 2 to 2,
            paintColor = Color(0xFFFF007F),
            wallColor = Color(0xFF3A0CA3)
        ),
        // Level 2: Box Circuit
        ColorMazeLevel(
            levelNumber = 2,
            width = 6,
            height = 6,
            walls = setOf(
                2 to 2, 2 to 3, 3 to 2, 3 to 3,
                0 to 2, 5 to 3
            ),
            startPos = 0 to 0,
            paintColor = Color(0xFF00F5D4),
            wallColor = Color(0xFF7209B7)
        ),
        // Level 3: Zigzag & Islands
        ColorMazeLevel(
            levelNumber = 3,
            width = 7,
            height = 7,
            walls = setOf(
                1 to 1, 1 to 2, 1 to 4, 1 to 5,
                3 to 2, 3 to 4,
                5 to 1, 5 to 2, 5 to 4, 5 to 5
            ),
            startPos = 0 to 0,
            paintColor = Color(0xFFFFBE0B),
            wallColor = Color(0xFFFB5607)
        ),
        // Level 4: Spiral Labyrinth
        ColorMazeLevel(
            levelNumber = 4,
            width = 7,
            height = 7,
            walls = setOf(
                1 to 1, 2 to 1, 3 to 1, 4 to 1, 5 to 1,
                5 to 2, 5 to 3, 5 to 4, 5 to 5,
                1 to 5, 2 to 5, 3 to 5, 4 to 5,
                1 to 3, 2 to 3, 3 to 3
            ),
            startPos = 0 to 0,
            paintColor = Color(0xFF8338EC),
            wallColor = Color(0xFF3A86FF)
        ),
        // Level 5: Complex Grid
        ColorMazeLevel(
            levelNumber = 5,
            width = 8,
            height = 8,
            walls = setOf(
                1 to 1, 2 to 1, 4 to 1, 5 to 1, 6 to 1,
                1 to 3, 3 to 3, 4 to 3, 6 to 3,
                1 to 5, 3 to 5, 4 to 5, 6 to 5,
                1 to 6, 2 to 6, 5 to 6, 6 to 6
            ),
            startPos = 0 to 0,
            paintColor = Color(0xFFFF5400),
            wallColor = Color(0xFF9D0208)
        )
    )
}

class ColorMazeEngine {
    fun initState(level: ColorMazeLevel): ColorMazeState {
        var floorCount = 0
        for (x in 0 until level.width) {
            for (y in 0 until level.height) {
                if (!level.walls.contains(x to y)) {
                    floorCount++
                }
            }
        }

        return ColorMazeState(
            levelNumber = level.levelNumber,
            playerX = level.startPos.first,
            playerY = level.startPos.second,
            paintedTiles = setOf(level.startPos),
            totalFloorTiles = floorCount,
            movesCount = 0,
            isRolling = false,
            isWon = false
        )
    }

    fun roll(state: ColorMazeState, level: ColorMazeLevel, dx: Int, dy: Int): ColorMazeState {
        if (state.isWon) return state

        var curX = state.playerX
        var curY = state.playerY
        val newlyPainted = state.paintedTiles.toMutableSet()
        var moved = false

        while (true) {
            val nextX = curX + dx
            val nextY = curY + dy

            // Check boundaries
            if (nextX !in 0 until level.width || nextY !in 0 until level.height) break
            // Check walls
            if (level.walls.contains(nextX to nextY)) break

            curX = nextX
            curY = nextY
            newlyPainted.add(curX to curY)
            moved = true
        }

        if (!moved) return state

        val isWon = newlyPainted.size >= state.totalFloorTiles

        return state.copy(
            playerX = curX,
            playerY = curY,
            paintedTiles = newlyPainted,
            movesCount = state.movesCount + 1,
            isWon = isWon
        )
    }
}
