package com.example.colorsequence

import com.example.colorsequence.core.engine.ColorSequenceEngine
import com.example.colorsequence.core.model.GamePhase
import com.example.colorsequence.core.model.SequenceColor
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ColorSequenceEngineTest {

    private lateinit var engine: ColorSequenceEngine

    @Before
    fun setUp() {
        engine = ColorSequenceEngine()
    }

    @Test
    fun testLevelConfigProgression() {
        val configL1 = engine.generateLevelConfig(1)
        assertEquals(3, configL1.sequenceLength)
        assertEquals(4, configL1.poolSize)

        val configL5 = engine.generateLevelConfig(5)
        assertEquals(5, configL5.sequenceLength)
        assertEquals(6, configL5.poolSize)

        val configL12 = engine.generateLevelConfig(12)
        assertEquals(8, configL12.sequenceLength)
        assertEquals(8, configL12.poolSize)
    }

    @Test
    fun testDeterministicSequenceGeneration() {
        val seed = 12345L
        val state1 = engine.startLevel(1, seed = seed)
        val state2 = engine.startLevel(1, seed = seed)

        assertEquals(3, state1.targetSequence.size)
        assertEquals(state1.targetSequence, state2.targetSequence)
        assertTrue(state1.targetSequence.all { it in state1.availablePool })
    }

    @Test
    fun testSuccessfulSequenceInputWinsGame() {
        val state = engine.startLevel(1, seed = 42L)
        val target = state.targetSequence
        var current = engine.onSequenceDisplayFinished(state)

        assertEquals(GamePhase.AWAITING_INPUT, current.phase)
        assertEquals(0, current.currentStepIndex)

        // Tap first correct color
        current = engine.processPlayerTap(current, target[0], currentTimeMs = current.startTimeMs + 100)
        assertEquals(1, current.currentStepIndex)
        assertEquals(GamePhase.AWAITING_INPUT, current.phase)

        // Tap second correct color
        current = engine.processPlayerTap(current, target[1], currentTimeMs = current.startTimeMs + 200)
        assertEquals(2, current.currentStepIndex)
        assertEquals(GamePhase.AWAITING_INPUT, current.phase)

        // Tap third (final) correct color
        current = engine.processPlayerTap(current, target[2], currentTimeMs = current.startTimeMs + 300)
        assertEquals(3, current.currentStepIndex)
        assertEquals(GamePhase.LEVEL_WON, current.phase)
        assertEquals(3, current.starsAwarded)
        assertTrue(current.score > 0)
    }

    @Test
    fun testWrongColorInputFailsLevel() {
        val state = engine.startLevel(1, seed = 42L)
        val target = state.targetSequence
        var current = engine.onSequenceDisplayFinished(state)

        // Pick a wrong color
        val wrongColor = SequenceColor.values().first { it != target[0] }
        current = engine.processPlayerTap(current, wrongColor)

        assertEquals(GamePhase.LEVEL_FAILED, current.phase)
        assertEquals(1, current.errorsCount)
        assertEquals(0, current.starsAwarded)
    }

    @Test
    fun testStarRatingBasedOnTime() {
        val config = engine.generateLevelConfig(1)
        // 0 errors, fast -> 3 stars
        assertEquals(3, engine.calculateStars(errors = 0, durationMs = config.targetStars3TimeMs - 500, config = config))
        // 0 errors, normal -> 2 stars
        assertEquals(2, engine.calculateStars(errors = 0, durationMs = config.targetStars2TimeMs - 500, config = config))
        // 0 errors, slow -> 1 star
        assertEquals(1, engine.calculateStars(errors = 0, durationMs = config.targetStars2TimeMs + 1000, config = config))
    }

    @Test
    fun testRetryResetsStateCleanly() {
        val state1 = engine.startLevel(1, seed = 99L)
        var failed = engine.onSequenceDisplayFinished(state1)
        val wrongColor = SequenceColor.values().first { it != state1.targetSequence[0] }
        failed = engine.processPlayerTap(failed, wrongColor)
        assertEquals(GamePhase.LEVEL_FAILED, failed.phase)

        // Retry
        val retried = engine.startLevel(1, seed = 99L)
        assertEquals(GamePhase.SHOWING_SEQUENCE, retried.phase)
        assertEquals(0, retried.playerInput.size)
        assertEquals(0, retried.errorsCount)
    }
}
