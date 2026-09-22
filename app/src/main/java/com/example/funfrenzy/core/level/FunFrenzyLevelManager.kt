package com.example.funfrenzy.core.level

import com.example.funfrenzy.core.model.FunFrenzyLevelConfig
import com.example.funfrenzy.core.model.MicroGameSpec
import com.example.funfrenzy.core.model.MicroGameType

object FunFrenzyLevelManager {
    const val MAX_LEVELS = 100

    fun getLevel(levelNumber: Int): FunFrenzyLevelConfig {
        val clamped = levelNumber.coerceIn(1, MAX_LEVELS)
        val speedMultiplier = 1f + (clamped - 1) * 0.015f

        val microGameSequence = when {
            clamped <= 3 -> listOf(
                MicroGameSpec(MicroGameType.TAP_RUSH, "TAP RAPIDLY!", 4f),
                MicroGameSpec(MicroGameType.POP_BALLOONS, "POP ALL BALLOONS!", 4.5f),
                MicroGameSpec(MicroGameType.STOP_NEEDLE, "STOP IN THE GREEN!", 4f)
            )
            clamped <= 10 -> listOf(
                MicroGameSpec(MicroGameType.CATCH_FALLING, "CATCH THE GEM!", 3.8f),
                MicroGameSpec(MicroGameType.TAP_RUSH, "TAP TO SURVIVE!", 3.5f),
                MicroGameSpec(MicroGameType.CUT_WIRE, "CUT THE RIGHT WIRE!", 4f),
                MicroGameSpec(MicroGameType.FIND_ODD_ONE, "SPOT THE ODD ONE!", 4f)
            )
            clamped <= 30 -> listOf(
                MicroGameSpec(MicroGameType.DODGE_ROCKS, "DODGE FALLING ROCKS!", 4f),
                MicroGameSpec(MicroGameType.POP_BALLOONS, "BURST THEM ALL!", 3.5f),
                MicroGameSpec(MicroGameType.STOP_NEEDLE, "TARGET LOCK!", 3.5f),
                MicroGameSpec(MicroGameType.CUT_WIRE, "DEFUSE WIRE!", 3.5f),
                MicroGameSpec(MicroGameType.TAP_RUSH, "TURBO TAP!", 3f)
            )
            else -> {
                // Procedural mix for higher levels
                val pool = listOf(
                    MicroGameSpec(MicroGameType.TAP_RUSH, "TAP FRENZY!", 3f),
                    MicroGameSpec(MicroGameType.CATCH_FALLING, "FAST CATCH!", 3f),
                    MicroGameSpec(MicroGameType.POP_BALLOONS, "SPEED POP!", 3.2f),
                    MicroGameSpec(MicroGameType.DODGE_ROCKS, "METEOR DODGE!", 3.5f),
                    MicroGameSpec(MicroGameType.STOP_NEEDLE, "PRECISION STOP!", 3f),
                    MicroGameSpec(MicroGameType.CUT_WIRE, "HAZARD WIRE!", 3.2f),
                    MicroGameSpec(MicroGameType.FIND_ODD_ONE, "RAPID SPOT!", 3.2f)
                ).shuffled()
                pool.take(5)
            }
        }

        return FunFrenzyLevelConfig(
            levelNumber = clamped,
            speedMultiplier = speedMultiplier,
            microGames = microGameSequence
        )
    }
}
