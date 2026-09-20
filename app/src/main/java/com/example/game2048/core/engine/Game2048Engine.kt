package com.example.game2048.core.engine

import com.example.game2048.core.model.Game2048State
import com.example.game2048.core.model.MoveDirection
import kotlin.math.max
import kotlin.random.Random

class Game2048Engine(
    private val randomProvider: (Long?) -> Random = { seed -> if (seed != null) Random(seed) else Random.Default }
) {

    data class MoveResult(
        val newBoard: List<Int>,
        val pointsEarned: Int,
        val hasChanged: Boolean
    )

    fun createInitialState(bestScore: Int = 0, seed: Long? = null): Game2048State {
        val emptyBoard = List(16) { 0 }
        val rng = randomProvider(seed)

        val firstBoard = spawnRandomTile(emptyBoard, rng)
        val secondBoard = spawnRandomTile(firstBoard, rng)

        return Game2048State(
            board = secondBoard,
            score = 0,
            bestScore = bestScore,
            isWon = false,
            hasContinuedAfterWin = false,
            isGameOver = false,
            moveCount = 0
        )
    }

    fun makeMove(state: Game2048State, direction: MoveDirection, seed: Long? = null): Game2048State {
        if (state.isGameOver) return state

        val moveResult = executeMove(state.board, direction)
        if (!moveResult.hasChanged) {
            // Move was invalid (no tiles moved or merged)
            return state
        }

        val rng = randomProvider(seed)
        val boardWithSpawn = spawnRandomTile(moveResult.newBoard, rng)
        val newScore = state.score + moveResult.pointsEarned
        val newBestScore = max(state.bestScore, newScore)

        val reached2048 = boardWithSpawn.any { it >= 2048 }
        val isWon = reached2048 && !state.hasContinuedAfterWin
        val isGameOver = !canMakeAnyMove(boardWithSpawn)

        return state.copy(
            board = boardWithSpawn,
            score = newScore,
            bestScore = newBestScore,
            isWon = isWon,
            isGameOver = isGameOver,
            moveCount = state.moveCount + 1
        )
    }

    fun continuePlayingAfterWin(state: Game2048State): Game2048State {
        return state.copy(
            isWon = false,
            hasContinuedAfterWin = true
        )
    }

    fun executeMove(board: List<Int>, direction: MoveDirection): MoveResult {
        var pointsGained = 0
        val newBoardArray = IntArray(16)

        for (i in 0 until 4) {
            val line = when (direction) {
                MoveDirection.LEFT -> (0 until 4).map { col -> board[i * 4 + col] }
                MoveDirection.RIGHT -> (3 downTo 0).map { col -> board[i * 4 + col] }
                MoveDirection.UP -> (0 until 4).map { row -> board[row * 4 + i] }
                MoveDirection.DOWN -> (3 downTo 0).map { row -> board[row * 4 + i] }
            }

            val (mergedLine, linePoints) = mergeLine(line)
            pointsGained += linePoints

            for (j in 0 until 4) {
                val value = mergedLine[j]
                when (direction) {
                    MoveDirection.LEFT -> newBoardArray[i * 4 + j] = value
                    MoveDirection.RIGHT -> newBoardArray[i * 4 + (3 - j)] = value
                    MoveDirection.UP -> newBoardArray[j * 4 + i] = value
                    MoveDirection.DOWN -> newBoardArray[(3 - j) * 4 + i] = value
                }
            }
        }

        val newBoard = newBoardArray.toList()
        val hasChanged = newBoard != board
        return MoveResult(newBoard, pointsGained, hasChanged)
    }

    fun mergeLine(line: List<Int>): Pair<List<Int>, Int> {
        val nonZero = line.filter { it != 0 }
        val result = mutableListOf<Int>()
        var points = 0
        var i = 0

        while (i < nonZero.size) {
            if (i + 1 < nonZero.size && nonZero[i] == nonZero[i + 1]) {
                val mergedVal = nonZero[i] * 2
                result.add(mergedVal)
                points += mergedVal
                i += 2 // skip both merged tiles - double merge in one move is forbidden
            } else {
                result.add(nonZero[i])
                i += 1
            }
        }

        while (result.size < 4) {
            result.add(0)
        }

        return Pair(result, points)
    }

    fun spawnRandomTile(board: List<Int>, rng: Random): List<Int> {
        val emptyIndices = board.indices.filter { board[it] == 0 }
        if (emptyIndices.isEmpty()) return board

        val pickIndex = emptyIndices[rng.nextInt(emptyIndices.size)]
        // 90% chance of 2, 10% chance of 4
        val tileValue = if (rng.nextFloat() < 0.9f) 2 else 4

        val newBoard = board.toMutableList()
        newBoard[pickIndex] = tileValue
        return newBoard
    }

    fun canMakeAnyMove(board: List<Int>): Boolean {
        // If there are any empty cells, moves are definitely available
        if (board.any { it == 0 }) return true

        // Check horizontal merges
        for (row in 0 until 4) {
            for (col in 0 until 3) {
                if (board[row * 4 + col] == board[row * 4 + col + 1]) {
                    return true
                }
            }
        }

        // Check vertical merges
        for (col in 0 until 4) {
            for (row in 0 until 3) {
                if (board[row * 4 + col] == board[(row + 1) * 4 + col]) {
                    return true
                }
            }
        }

        return false
    }
}
