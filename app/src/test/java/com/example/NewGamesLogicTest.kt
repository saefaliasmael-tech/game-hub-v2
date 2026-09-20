package com.example

import com.example.mastermind.core.engine.MastermindEngine
import com.example.mastermind.core.model.MastermindDifficulty
import com.example.mastermind.core.model.PegColor
import com.example.memory.core.engine.MemoryEngine
import com.example.memory.core.model.MemoryBoardSize
import com.example.memory.core.model.MemoryGameMode
import com.example.memory.core.model.MemoryTheme
import com.example.unblockme.core.level.UnblockLevelManager
import com.example.unblockme.core.solver.UnblockSolver
import org.junit.Assert.*
import org.junit.Test

class NewGamesLogicTest {

    @Test
    fun testMastermindFeedback_ExactMatch() {
        val secret = listOf(PegColor.RED, PegColor.BLUE, PegColor.GREEN, PegColor.YELLOW)
        val guess = listOf(PegColor.RED, PegColor.BLUE, PegColor.GREEN, PegColor.YELLOW)

        val feedback = MastermindEngine.evaluateGuess(secret, guess)
        assertEquals(4, feedback.exactMatches)
        assertEquals(0, feedback.colorMatches)
        assertTrue(feedback.isSolved(4))
    }

    @Test
    fun testMastermindFeedback_PartialAndColorMatches() {
        val secret = listOf(PegColor.RED, PegColor.BLUE, PegColor.GREEN, PegColor.YELLOW)
        val guess = listOf(PegColor.RED, PegColor.GREEN, PegColor.BLUE, PegColor.PURPLE)

        val feedback = MastermindEngine.evaluateGuess(secret, guess)
        assertEquals(1, feedback.exactMatches) // RED
        assertEquals(2, feedback.colorMatches) // GREEN, BLUE
        assertFalse(feedback.isSolved(4))
    }

    @Test
    fun testMastermindSecretCodeLength() {
        val secret = MastermindEngine.generateSecretCode(
            codeLength = 5,
            colorCount = 7,
            allowDuplicates = false
        )
        assertEquals(5, secret.size)
        assertEquals(5, secret.toSet().size) // No duplicates
    }

    @Test
    fun testUnblockMeSolver_EscapesSimplePuzzle() {
        val level1 = UnblockLevelManager.getLevel(1)
        assertNotNull(level1)
        assertTrue(level1.blocks.any { it.isTarget })

        // Target block starts at row 2
        val target = level1.blocks.first { it.isTarget }
        assertEquals(2, target.row)

        // BFS Solver finds a valid sequence of moves
        val solution = UnblockSolver.solve(level1.blocks)
        assertNotNull("Puzzle 1 must be solvable", solution)
        assertTrue(solution!!.isNotEmpty())
    }

    @Test
    fun testMemoryEngineDeckCreation() {
        val board = MemoryEngine.createBoard(
            boardSize = MemoryBoardSize.NORMAL, // 16 cards, 8 pairs
            theme = MemoryTheme.ANIMALS
        )

        assertEquals(16, board.size)
        val pairGroups = board.groupBy { it.pairId }
        assertEquals(8, pairGroups.size)
        pairGroups.values.forEach { pairCards ->
            assertEquals(2, pairCards.size)
            assertEquals(pairCards[0].iconName, pairCards[1].iconName)
        }
    }

    @Test
    fun testMemoryEngineStarRating() {
        // 8 pairs matched in 9 moves -> 3 stars
        val stars = MemoryEngine.calculateStars(movesCount = 9, pairsCount = 8)
        assertEquals(3, stars)
    }
}
