package com.example.stopthetime.core.engine

import com.example.stopthetime.core.model.StopAccuracy
import com.example.stopthetime.core.model.StopDifficulty
import java.util.Calendar
import java.util.Random
import kotlin.math.abs

object StopTheTimeEngine {

    fun calculateDifference(playerTimeMs: Long, targetTimeMs: Long): Long {
        return abs(playerTimeMs - targetTimeMs)
    }

    fun evaluateAccuracy(differenceMs: Long): StopAccuracy {
        return when {
            differenceMs <= StopAccuracy.PERFECT.maxDifferenceMs -> StopAccuracy.PERFECT
            differenceMs <= StopAccuracy.EXCELLENT.maxDifferenceMs -> StopAccuracy.EXCELLENT
            differenceMs <= StopAccuracy.GREAT.maxDifferenceMs -> StopAccuracy.GREAT
            differenceMs <= StopAccuracy.GOOD.maxDifferenceMs -> StopAccuracy.GOOD
            else -> StopAccuracy.MISS
        }
    }

    fun calculateStars(differenceMs: Long, difficulty: StopDifficulty): Int {
        return when {
            differenceMs <= difficulty.thresholdMs3Stars -> 3
            differenceMs <= difficulty.thresholdMs2Stars -> 2
            differenceMs <= difficulty.thresholdMs1Star -> 1
            else -> 0
        }
    }

    fun calculateScore(accuracy: StopAccuracy, combo: Int, differenceMs: Long): Int {
        val precisionBonus = (300L - differenceMs.coerceAtMost(300L)) * 2
        val comboMultiplier = 1.0 + (combo * 0.25).coerceAtMost(3.0)
        return ((accuracy.baseScore + precisionBonus) * comboMultiplier).toInt()
    }

    fun updateCombo(currentCombo: Int, accuracy: StopAccuracy): Int {
        return when (accuracy) {
            StopAccuracy.PERFECT, StopAccuracy.EXCELLENT -> currentCombo + 1
            StopAccuracy.GREAT -> currentCombo + 1
            StopAccuracy.GOOD -> currentCombo // hold combo
            StopAccuracy.MISS -> 0 // reset
        }
    }

    fun generateRandomTargetMs(minSeconds: Float = 3.0f, maxSeconds: Float = 12.0f): Long {
        val range = (maxSeconds - minSeconds)
        val randSec = minSeconds + (Math.random().toFloat() * range)
        // Round to nearest quarter or tenth of a second for clean display
        val roundedQuarter = (Math.round(randSec * 4) / 4.0f)
        return (roundedQuarter * 1000).toLong()
    }

    fun getDailyChallengeTargets(): List<Long> {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        val seed = (year * 365 + dayOfYear).toLong()
        val rng = Random(seed)

        val baseTargets = listOf(
            listOf(4000L, 7500L, 10000L),
            listOf(3500L, 6250L, 9000L),
            listOf(5000L, 8000L, 12000L),
            listOf(4500L, 7000L, 11500L),
            listOf(3000L, 6000L, 10500L)
        )
        val chosenIndex = (rng.nextInt(baseTargets.size)).coerceIn(0, baseTargets.size - 1)
        return baseTargets[chosenIndex]
    }

    fun formatTime(timeMs: Long): String {
        val seconds = timeMs / 1000
        val millisPart = (timeMs % 1000) / 10 // two digits
        return String.format("%02d.%02d", seconds, millisPart)
    }

    fun formatDifference(diffMs: Long, stoppedMs: Long, targetMs: Long): String {
        val sign = if (stoppedMs >= targetMs) "+" else "-"
        val seconds = diffMs / 1000
        val hundredths = (diffMs % 1000) / 10
        val thousandths = (diffMs % 10)
        return if (seconds > 0) {
            String.format("%s%d.%02d%ds", sign, seconds, hundredths, thousandths)
        } else {
            String.format("%s0.%02d%ds", sign, hundredths, thousandths)
        }
    }
}
