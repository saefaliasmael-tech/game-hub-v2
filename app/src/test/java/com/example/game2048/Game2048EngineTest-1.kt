package com.example.game2048

import com.example.game2048.core.engine.Game2048Engine
import com.example.game2048.core.model.Game2048State
import com.example.game2048.core.model.MoveDirection
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class Game2048EngineTest {

    private lateinit var engine: Game2048Engine

    @Before
    fun setUp() {
        engine = Game2048Engine()
    }

    @Test
    fun testMergeLineBasic() {
        // [2, 2, 0, 0] -> [4, 0, 0, 0], points: 4
        val (line1, points1) = engine.mergeLine(listOf(2, 2, 0, 0))
        assertEquals(listOf(4, 0, 0, 0), line1)
        assertEquals(4, points1)

        // [2, 2, 4, 4] -> [4, 8, 0, 0], points: 12
        val (line2, points2) = engine.mergeLine(listOf(2, 2, 4, 4))
        assertEquals(listOf(4, 8, 0, 0), line2)
        assertEquals(12, points2)
    }

    @Test
    fun testDoubleMergePrevention() {
        // [2, 2, 2, 0] -> [4, 2, 0, 0], points: 4 (first pair merges, 3rd tile cannot merge into it)
        val (line1, points1) = engine.mergeLine(listOf(2, 2, 2, 0))
        assertEquals(listOf(4, 2, 0, 0), line1)
        assertEquals(4, points1)

        // [4, 2, 2, 0] -> [4, 4, 0, 0] (not [8, 0, 0, 0])
        val (line2, points2) = engine.mergeLine(listOf(4, 2, 2, 0))
        assertEquals(listOf(4, 4, 0, 0), line2)
        assertEquals(4, points2)
    }

    @Test
    fun testMoveExecutionLeft() {
        val board = listOf(
            2, 2, 4, 4,
            0, 2, 0, 2,
            0, 0, 0, 0,
            8, 8, 8, 8
        )
        val result = engine.executeMove(board, MoveDirection.LEFT)
        assertTrue(result.hasChanged)
        assertEquals(listOf(
            4, 8, 0, 0,
            4, 0, 0, 0,
            0, 0, 0, 0,
            16, 16, 0, 0
        ), result.newBoard)
        assertEquals(4 + 8 + 4 + 16 + 16, result.pointsEarned)
    }

    @Test
    fun testInvalidMoveDoesNotChangeBoard() {
        val board = listOf(
            2, 4, 8, 16,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0
        )
        val result = engine.executeMove(board, MoveDirection.UP)
        assertFalse(result.hasChanged)
        assertEquals(board, result.newBoard)
        assertEquals(0, result.pointsEarned)
    }

    @Test
    fun testGameOverDetection() {
        // Full board with no adjacent matches
        val fullBoard = listOf(
            2, 4, 2, 4,
            4, 2, 4, 2,
            2, 4, 2, 4,
            4, 2, 4, 2
        )
        assertFalse(engine.canMakeAnyMove(fullBoard))

        // Full board with one adjacent match
        val playableBoard = listOf(
            2, 2, 2, 4,
            4, 2, 4, 2,
            2, 4, 2, 4,
            4, 2, 4, 2
        )
        assertTrue(engine.canMakeAnyMove(playableBoard))
    }

    @Test
    fun testWinConditionAt2048() {
        val board = listOf(
            1024, 1024, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0
        )
        val state = Game2048State(board = board, score = 0, bestScore = 0)
        val newState = engine.makeMove(state, MoveDirection.LEFT, seed = 1L)

        assertTrue(newState.board.any { it >= 2048 })
        assertTrue(newState.isWon)
        assertEquals(2048, newState.score)

        // Player chooses to continue after win
        val continuedState = engine.continuePlayingAfterWin(newState)
        assertFalse(continuedState.isWon)
        assertTrue(continuedState.hasContinuedAfterWin)
    }
}
