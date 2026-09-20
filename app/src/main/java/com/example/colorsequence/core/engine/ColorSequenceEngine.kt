package com.example.colorsequence.core.engine

import com.example.colorsequence.core.model.ColorSequenceGameState
import com.example.colorsequence.core.model.ColorSequenceLevelConfig
import com.example.colorsequence.core.model.GamePhase
import com.example.colorsequence.core.model.SequenceColor
import kotlin.math.max
import kotlin.random.Random

class ColorSequenceEngine(
    private val randomProvider: (Long?) -> Random = { seed -> if (seed != null) Random(seed) else Random.Default }
) {

    fun generateLevelConfig(levelNumber: Int): ColorSequenceLevelConfig {
        val length = when {
            levelNumber <= 2 -> 3
            levelNumber <= 4 -> 4
            levelNumber <= 6 -> 5
            levelNumber <= 8 -> 6
            levelNumber <= 11 -> 7
            levelNumber <= 15 -> 8
            levelNumber <= 20 -> 9
            else -> 10
        }

        val poolSize = when {
            levelNumber <= 3 -> 4
            levelNumber <= 7 -> 6
            else -> 8
        }

        val displayInterval = max(500L, 1100L - (levelNumber * 40L))
        val targetStars3Time = (length * 1500L) + 2000L
        val targetStars2Time = (length * 2500L) + 4000L

        return ColorSequenceLevelConfig(
            levelNumber = levelNumber,
            sequenceLength = length,
            displayIntervalMs = displayInterval,
            poolSize = poolSize,
            targetStars3TimeMs = targetStars3Time,
            targetStars2TimeMs = targetStars2Time
        )
    }

    fun startLevel(levelNumber: Int, seed: Long? = null): ColorSequenceGameState {
        val config = generateLevelConfig(levelNumber)
        val rng = randomProvider(seed)

        val pool = SequenceColor.getAvailableColors(levelNumber)
        val selectedPool = pool.take(config.poolSize)

        // Generate non-trivial sequence (no 3 consecutive identical elements)
        val sequence = mutableListOf<SequenceColor>()
        var lastColor: SequenceColor? = null
        var repeatCount = 0

        for (i in 0 until config.sequenceLength) {
            val candidatePool = if (repeatCount >= 2 && lastColor != null) {
                selectedPool.filter { it != lastColor }
            } else {
                selectedPool
            }
            val picked = candidatePool[rng.nextInt(candidatePool.size)]
            if (picked == lastColor) {
                repeatCount++
            } else {
                lastColor = picked
                repeatCount = 1
            }
            sequence.add(picked)
        }

        return ColorSequenceGameState(
            levelNumber = levelNumber,
            targetSequence = sequence,
            availablePool = selectedPool,
            phase = GamePhase.SHOWING_SEQUENCE,
            currentStepIndex = 0,
            playerInput = emptyList(),
            startTimeMs = System.currentTimeMillis()
        )
    }

    fun onSequenceDisplayFinished(state: ColorSequenceGameState): ColorSequenceGameState {
        return state.copy(
            phase = GamePhase.AWAITING_INPUT,
            activeDisplayColor = null,
            activeDisplayIndex = -1,
            startTimeMs = System.currentTimeMillis()
        )
    }

    fun processPlayerTap(
        state: ColorSequenceGameState,
        color: SequenceColor,
        currentTimeMs: Long = System.currentTimeMillis()
    ): ColorSequenceGameState {
        if (state.phase != GamePhase.AWAITING_INPUT) return state

        val expected = state.targetSequence.getOrNull(state.currentStepIndex) ?: return state
        val updatedInput = state.playerInput + color

        if (color == expected) {
            val nextStep = state.currentStepIndex + 1
            if (nextStep >= state.targetSequence.size) {
                // Completed all steps correctly!
                val duration = max(0L, currentTimeMs - state.startTimeMs)
                val config = generateLevelConfig(state.levelNumber)
                val stars = calculateStars(state.errorsCount, duration, config)
                val timeBonus = max(0, ((config.targetStars3TimeMs - duration) / 100).toInt())
                val score = (state.targetSequence.size * 100) + (stars * 50) + timeBonus

                return state.copy(
                    playerInput = updatedInput,
                    currentStepIndex = nextStep,
                    phase = GamePhase.LEVEL_WON,
                    starsAwarded = stars,
                    score = score,
                    durationMs = duration
                )
            } else {
                return state.copy(
                    playerInput = updatedInput,
                    currentStepIndex = nextStep
                )
            }
        } else {
            // Wrong tap!
            val duration = max(0L, currentTimeMs - state.startTimeMs)
            return state.copy(
                playerInput = updatedInput,
                errorsCount = state.errorsCount + 1,
                phase = GamePhase.LEVEL_FAILED,
                durationMs = duration
            )
        }
    }

    fun calculateStars(errors: Int, durationMs: Long, config: ColorSequenceLevelConfig): Int {
        return when {
            errors == 0 && durationMs <= config.targetStars3TimeMs -> 3
            errors == 0 && durationMs <= config.targetStars2TimeMs -> 2
            errors == 0 -> 1
            else -> 1
        }
    }
}
