package com.example.mastermind.core.engine

import com.example.mastermind.core.model.*
import java.util.Random

object MastermindEngine {

    /**
     * Mathematically sound Mastermind Feedback calculation algorithm.
     * Accurately prevents over-counting of duplicate colors.
     */
    fun calculateFeedback(secret: List<PegColor>, guess: List<PegColor>): Feedback {
        require(secret.size == guess.size) { "Secret and guess must be of equal length" }

        var exactMatches = 0
        val unmatchedSecretFreq = mutableMapOf<PegColor, Int>()
        val unmatchedGuess = mutableListOf<PegColor>()

        // 1st pass: find exact position matches
        for (i in secret.indices) {
            if (secret[i] == guess[i]) {
                exactMatches++
            } else {
                unmatchedSecretFreq[secret[i]] = (unmatchedSecretFreq[secret[i]] ?: 0) + 1
                unmatchedGuess.add(guess[i])
            }
        }

        // 2nd pass: find color matches in non-matching positions
        var colorMatches = 0
        for (color in unmatchedGuess) {
            val count = unmatchedSecretFreq[color] ?: 0
            if (count > 0) {
                colorMatches++
                unmatchedSecretFreq[color] = count - 1
            }
        }

        return Feedback(exactMatches = exactMatches, colorMatches = colorMatches)
    }

    /**
     * Generates a random secret code according to difficulty or level configuration.
     */
    fun generateSecretCode(
        codeLength: Int,
        colorCount: Int,
        allowDuplicates: Boolean,
        random: Random = Random()
    ): List<PegColor> {
        val palette = PegColor.getPalette(colorCount)
        return if (allowDuplicates) {
            List(codeLength) { palette[random.nextInt(palette.size)] }
        } else {
            val shuffled = palette.shuffled(random)
            shuffled.take(codeLength)
        }
    }

    /**
     * Generate 100 progressive campaign levels with deterministic secrets.
     */
    fun getLevelConfig(levelNumber: Int): MastermindLevelConfig {
        val boundedLevel = levelNumber.coerceIn(1, 100)
        val difficulty = MastermindDifficulty.fromLevelNumber(boundedLevel)
        val random = Random((boundedLevel * 7919L) xor 0x5EED)

        val targetSecret = generateSecretCode(
            codeLength = difficulty.codeLength,
            colorCount = difficulty.colorCount,
            allowDuplicates = difficulty.allowDuplicates,
            random = random
        )

        return MastermindLevelConfig(
            levelNumber = boundedLevel,
            difficulty = difficulty,
            codeLength = difficulty.codeLength,
            colorCount = difficulty.colorCount,
            maxAttempts = difficulty.maxAttempts,
            allowDuplicates = difficulty.allowDuplicates,
            targetSecret = targetSecret
        )
    }

    /**
     * Daily challenge code generator based on epoch day.
     */
    fun getDailyChallengeConfig(epochDay: Long): MastermindLevelConfig {
        val random = Random(epochDay * 1234567L + 42L)
        val difficulty = MastermindDifficulty.HARD
        val targetSecret = generateSecretCode(
            codeLength = difficulty.codeLength,
            colorCount = difficulty.colorCount,
            allowDuplicates = difficulty.allowDuplicates,
            random = random
        )
        return MastermindLevelConfig(
            levelNumber = 9999,
            difficulty = difficulty,
            codeLength = difficulty.codeLength,
            colorCount = difficulty.colorCount,
            maxAttempts = difficulty.maxAttempts,
            allowDuplicates = difficulty.allowDuplicates,
            targetSecret = targetSecret
        )
    }

    /**
     * Calculates star performance (1..3 stars) based on attempts and hints used.
     */
    fun calculateStars(attemptsCount: Int, maxAttempts: Int, hintsUsed: Int): Int {
        val ratio = attemptsCount.toFloat() / maxAttempts.toFloat()
        return when {
            ratio <= 0.5f && hintsUsed == 0 -> 3
            ratio <= 0.8f && hintsUsed <= 1 -> 2
            else -> 1
        }
    }

    /**
     * Calculates score.
     */
    fun calculateScore(attemptsCount: Int, maxAttempts: Int, timeSeconds: Int, difficulty: MastermindDifficulty, hintsUsed: Int): Int {
        val baseScore = when (difficulty) {
            MastermindDifficulty.EASY -> 1000
            MastermindDifficulty.NORMAL -> 2000
            MastermindDifficulty.HARD -> 3500
            MastermindDifficulty.EXPERT -> 5000
            MastermindDifficulty.MASTER -> 7500
        }
        val attemptsBonus = (maxAttempts - attemptsCount) * 150
        val timePenalty = (timeSeconds * 2).coerceAtMost(500)
        val hintPenalty = hintsUsed * 300
        return (baseScore + attemptsBonus - timePenalty - hintPenalty).coerceAtLeast(100)
    }

    /**
     * Generates a progressive hint:
     * 1. Eliminate an unused color.
     * 2. Reveal a correct position.
     * 3. Inform of color frequency.
     */
    fun provideHint(state: MastermindGameState): HintResult {
        val secret = state.secretCode
        val palette = PegColor.getPalette(state.difficulty.colorCount)

        // 1st Hint Type: Eliminate a color that is not in the secret code
        val absentColors = palette.filter { color -> !secret.contains(color) && !state.eliminatedColors.contains(color) }
        if (absentColors.isNotEmpty() && state.hintsUsed == 0) {
            val eliminated = absentColors.first()
            return HintResult.ColorEliminated(eliminated, "The color ${eliminated.displayName} is NOT in the secret code!")
        }

        // 2nd Hint Type: Reveal a specific position
        val unrevealedPositions = secret.indices.filter { !state.revealedPositions.containsKey(it) }
        if (unrevealedPositions.isNotEmpty()) {
            val targetPos = unrevealedPositions.first()
            val correctColor = secret[targetPos]
            return HintResult.PositionRevealed(targetPos, correctColor, "Position ${targetPos + 1} is ${correctColor.displayName}!")
        }

        // 3rd Hint Type: Color frequency clue
        val randomColor = secret.random()
        val count = secret.count { it == randomColor }
        return HintResult.GeneralClue("The code contains $count ${randomColor.displayName} peg(s).")
    }
}

sealed class HintResult {
    data class ColorEliminated(val color: PegColor, val message: String) : HintResult()
    data class PositionRevealed(val position: Int, val color: PegColor, val message: String) : HintResult()
    data class GeneralClue(val message: String) : HintResult()
}
