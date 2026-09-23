package com.example.tombofthemask.engine

import com.example.tombofthemask.model.MaskLevel
import com.example.tombofthemask.model.TombOfTheMaskState

object MaskLevels {
    val levels = listOf(
        // Level 1: Intro to dashing
        MaskLevel(
            levelNumber = 1,
            width = 6,
            height = 8,
            startX = 1,
            startY = 6,
            walls = (0..5).map { it to 0 }.toSet() +
                    (0..5).map { it to 7 }.toSet() +
                    (0..7).map { 0 to it }.toSet() +
                    (0..7).map { 5 to it }.toSet() +
                    setOf(2 to 2, 3 to 2, 2 to 4, 3 to 4),
            spikes = emptySet(),
            dots = setOf(1 to 1, 2 to 1, 3 to 1, 4 to 1, 1 to 3, 4 to 3, 1 to 5, 4 to 5),
            coins = setOf(2 to 3, 3 to 3),
            exit = 4 to 1
        ),
        // Level 2: Spikes introduction
        MaskLevel(
            levelNumber = 2,
            width = 7,
            height = 9,
            startX = 1,
            startY = 7,
            walls = (0..6).map { it to 0 }.toSet() +
                    (0..6).map { it to 8 }.toSet() +
                    (0..8).map { 0 to it }.toSet() +
                    (0..8).map { 6 to it }.toSet() +
                    setOf(2 to 2, 4 to 2, 2 to 5, 4 to 5, 3 to 3),
            spikes = setOf(3 to 4, 3 to 6),
            dots = setOf(1 to 1, 2 to 1, 3 to 1, 4 to 1, 5 to 1, 1 to 4, 5 to 4, 1 to 6, 5 to 6),
            coins = setOf(3 to 2, 3 to 5),
            exit = 5 to 1
        ),
        // Level 3: Chamber of Traps
        MaskLevel(
            levelNumber = 3,
            width = 8,
            height = 10,
            startX = 1,
            startY = 8,
            walls = (0..7).map { it to 0 }.toSet() +
                    (0..7).map { it to 9 }.toSet() +
                    (0..9).map { 0 to it }.toSet() +
                    (0..9).map { 7 to it }.toSet() +
                    setOf(2 to 2, 3 to 2, 5 to 2, 2 to 4, 4 to 4, 5 to 4, 3 to 6, 4 to 6),
            spikes = setOf(4 to 2, 2 to 6, 5 to 6, 3 to 7),
            dots = setOf(1 to 1, 3 to 1, 6 to 1, 1 to 3, 6 to 3, 1 to 5, 6 to 5, 1 to 7, 6 to 7),
            coins = setOf(3 to 3, 4 to 5),
            exit = 6 to 1
        )
    )
}

class TombOfTheMaskEngine {
    fun initState(level: MaskLevel): TombOfTheMaskState {
        return TombOfTheMaskState(
            levelNumber = level.levelNumber,
            playerX = level.startX,
            playerY = level.startY,
            dotsRemaining = level.dots,
            coinsRemaining = level.coins,
            trail = listOf(level.startX to level.startY),
            score = 0,
            isGameOver = false,
            isWon = false
        )
    }

    fun dash(
        state: TombOfTheMaskState,
        level: MaskLevel,
        dx: Int,
        dy: Int,
        onDotCollected: () -> Unit,
        onCoinCollected: () -> Unit
    ): TombOfTheMaskState {
        if (state.isGameOver || state.isWon) return state

        var curX = state.playerX
        var curY = state.playerY
        val newTrail = state.trail.toMutableList()
        val remainingDots = state.dotsRemaining.toMutableSet()
        val remainingCoins = state.coinsRemaining.toMutableSet()
        var scoreAdd = 0
        var hitSpike = false

        while (true) {
            val nextX = curX + dx
            val nextY = curY + dy

            // Wall check
            if (nextX !in 0 until level.width || nextY !in 0 until level.height) break
            if (level.walls.contains(nextX to nextY)) break

            curX = nextX
            curY = nextY
            newTrail.add(curX to curY)

            // Spike check
            if (level.spikes.contains(curX to curY)) {
                hitSpike = true
                break
            }

            // Dot collection
            if (remainingDots.remove(curX to curY)) {
                scoreAdd += 10
                onDotCollected()
            }

            // Coin collection
            if (remainingCoins.remove(curX to curY)) {
                scoreAdd += 50
                onCoinCollected()
            }
        }

        if (hitSpike) {
            return state.copy(
                playerX = curX,
                playerY = curY,
                trail = newTrail,
                isGameOver = true
            )
        }

        val reachedExit = curX == level.exit.first && curY == level.exit.second
        val won = reachedExit && remainingDots.isEmpty()

        return state.copy(
            playerX = curX,
            playerY = curY,
            trail = newTrail,
            dotsRemaining = remainingDots,
            coinsRemaining = remainingCoins,
            score = state.score + scoreAdd,
            isWon = won
        )
    }
}
