package com.example

import com.example.watersort.core.engine.GameEngine
import com.example.watersort.core.level.LevelGenerator
import com.example.watersort.core.model.Bottle
import com.example.watersort.core.model.GameState
import com.example.watersort.core.model.InvalidReason
import com.example.watersort.core.model.LiquidColor
import com.example.watersort.core.model.MoveResult
import com.example.watersort.core.solver.SolverResult
import com.example.watersort.core.solver.WaterSortSolver
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WaterSortGameTest {

    @Test
    fun testPourExecutionAndRunLength() {
        val engine = GameEngine()
        val bottle0 = Bottle(
            id = 0,
            capacity = 4,
            layers = listOf(LiquidColor.BLUE, LiquidColor.RED, LiquidColor.RED)
        )
        val bottle1 = Bottle(
            id = 1,
            capacity = 4,
            layers = listOf(LiquidColor.RED)
        )
        val state = GameState(levelNumber = 1, bottles = listOf(bottle0, bottle1), maxCapacity = 4)

        val result = engine.executePour(state, fromIndex = 0, toIndex = 1)
        assertTrue("Pour should succeed", result is MoveResult.Success)

        val success = result as MoveResult.Success
        // Poured 2 RED layers from bottle0 into bottle1
        assertEquals(2, success.delta.amountPoured)
        assertEquals(LiquidColor.RED, success.delta.color)
        assertEquals(listOf(LiquidColor.BLUE), success.newGameState.bottles[0].layers)
        assertEquals(listOf(LiquidColor.RED, LiquidColor.RED, LiquidColor.RED), success.newGameState.bottles[1].layers)
    }

    @Test
    fun testColorMismatchValidation() {
        val engine = GameEngine()
        val bottle0 = Bottle(
            id = 0,
            capacity = 4,
            layers = listOf(LiquidColor.BLUE)
        )
        val bottle1 = Bottle(
            id = 1,
            capacity = 4,
            layers = listOf(LiquidColor.GREEN)
        )
        val state = GameState(levelNumber = 1, bottles = listOf(bottle0, bottle1))

        val result = engine.executePour(state, 0, 1)
        assertTrue(result is MoveResult.Invalid)
        assertEquals(InvalidReason.COLOR_MISMATCH, (result as MoveResult.Invalid).reason)
    }

    @Test
    fun testUndoAtomicReversal() {
        val engine = GameEngine()
        val bottle0 = Bottle(0, 4, listOf(LiquidColor.BLUE, LiquidColor.RED))
        val bottle1 = Bottle(1, 4, emptyList())
        val initialState = GameState(levelNumber = 1, bottles = listOf(bottle0, bottle1))

        val pourResult = engine.executePour(initialState, 0, 1) as MoveResult.Success
        val newState = pourResult.newGameState

        assertEquals(1, newState.movesCount)
        assertEquals(listOf(LiquidColor.BLUE), newState.bottles[0].layers)
        assertEquals(listOf(LiquidColor.RED), newState.bottles[1].layers)

        val undoneState = engine.undo(newState)
        assertNotNull(undoneState)
        assertEquals(0, undoneState!!.movesCount)
        assertEquals(listOf(LiquidColor.BLUE, LiquidColor.RED), undoneState.bottles[0].layers)
        assertTrue(undoneState.bottles[1].isEmpty)
    }

    @Test
    fun testSolverSolvesSimplePuzzle() = runBlocking {
        val solver = WaterSortSolver()
        val bottle0 = Bottle(0, 4, listOf(LiquidColor.BLUE, LiquidColor.RED, LiquidColor.BLUE, LiquidColor.RED))
        val bottle1 = Bottle(1, 4, listOf(LiquidColor.RED, LiquidColor.BLUE, LiquidColor.RED, LiquidColor.BLUE))
        val bottle2 = Bottle(2, 4, emptyList())
        val bottle3 = Bottle(3, 4, emptyList())

        val result = solver.solve(listOf(bottle0, bottle1, bottle2, bottle3), capacity = 4)
        assertTrue("Puzzle must be solved by solver", result is SolverResult.Solved)
        val solved = result as SolverResult.Solved
        assertTrue(solved.moves.isNotEmpty())
    }

    @Test
    fun testDeterministicLevelGeneration() = runBlocking {
        val generator = LevelGenerator()
        val levelA = generator.generateLevel(levelId = 42, seed = 42L)
        val levelB = generator.generateLevel(levelId = 42, seed = 42L)

        assertEquals(levelA.bottles.size, levelB.bottles.size)
        assertEquals(levelA.difficulty, levelB.difficulty)
        for (i in levelA.bottles.indices) {
            assertEquals(levelA.bottles[i].layers, levelB.bottles[i].layers)
        }
    }

    @Test
    fun testDeadlockDetection() {
        val engine = GameEngine()
        // Two full bottles with different colors at top, no empty bottle
        val b0 = Bottle(0, 4, listOf(LiquidColor.RED, LiquidColor.BLUE, LiquidColor.RED, LiquidColor.BLUE))
        val b1 = Bottle(1, 4, listOf(LiquidColor.BLUE, LiquidColor.RED, LiquidColor.BLUE, LiquidColor.GREEN))
        assertTrue(engine.isDeadlocked(listOf(b0, b1)))
    }
}
