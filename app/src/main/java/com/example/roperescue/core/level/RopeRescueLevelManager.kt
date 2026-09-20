package com.example.roperescue.core.level

import com.example.roperescue.core.model.Hazard
import com.example.roperescue.core.model.RescueLevelConfig
import com.example.roperescue.core.model.Wheel

object RopeRescueLevelManager {

    const val MAX_LEVELS = 100

    fun getLevel(levelNumber: Int): RescueLevelConfig {
        val level = levelNumber.coerceIn(1, MAX_LEVELS)

        val totalHostages = (8 + (level % 6) * 2).coerceAtMost(20)
        val requiredSaved = (totalHostages * 0.75f).toInt().coerceAtLeast(4)

        val startX = 0.15f
        val startY = 0.18f
        val targetX = 0.85f
        val targetY = 0.82f

        val wheels = mutableListOf<Wheel>()
        val hazards = mutableListOf<Hazard>()

        val layoutType = (level - 1) % 6

        when (layoutType) {
            0 -> {
                // Single detour wheel with central saw
                wheels.add(Wheel(id = 1, xPercent = 0.75f, yPercent = 0.30f))
                wheels.add(Wheel(id = 2, xPercent = 0.25f, yPercent = 0.65f))
                hazards.add(Hazard(id = 1, xPercent = 0.50f, yPercent = 0.50f, radiusPercent = 0.08f))
            }
            1 -> {
                // Top & Bottom bypass
                wheels.add(Wheel(id = 1, xPercent = 0.50f, yPercent = 0.22f))
                wheels.add(Wheel(id = 2, xPercent = 0.80f, yPercent = 0.45f))
                hazards.add(Hazard(id = 1, xPercent = 0.45f, yPercent = 0.45f, radiusPercent = 0.07f))
                hazards.add(Hazard(id = 2, xPercent = 0.60f, yPercent = 0.70f, radiusPercent = 0.07f))
            }
            2 -> {
                // Triple Zig-Zag
                wheels.add(Wheel(id = 1, xPercent = 0.30f, yPercent = 0.35f))
                wheels.add(Wheel(id = 2, xPercent = 0.70f, yPercent = 0.45f))
                wheels.add(Wheel(id = 3, xPercent = 0.30f, yPercent = 0.65f))
                hazards.add(Hazard(id = 1, xPercent = 0.50f, yPercent = 0.35f, radiusPercent = 0.06f))
                hazards.add(Hazard(id = 2, xPercent = 0.50f, yPercent = 0.55f, radiusPercent = 0.06f))
            }
            3 -> {
                // Diamond corridor
                wheels.add(Wheel(id = 1, xPercent = 0.50f, yPercent = 0.25f))
                wheels.add(Wheel(id = 2, xPercent = 0.20f, yPercent = 0.50f))
                wheels.add(Wheel(id = 3, xPercent = 0.80f, yPercent = 0.50f))
                wheels.add(Wheel(id = 4, xPercent = 0.50f, yPercent = 0.75f))
                hazards.add(Hazard(id = 1, xPercent = 0.50f, yPercent = 0.50f, radiusPercent = 0.10f))
            }
            4 -> {
                // Two saw blockade
                wheels.add(Wheel(id = 1, xPercent = 0.65f, yPercent = 0.20f))
                wheels.add(Wheel(id = 2, xPercent = 0.85f, yPercent = 0.50f))
                wheels.add(Wheel(id = 3, xPercent = 0.35f, yPercent = 0.75f))
                hazards.add(Hazard(id = 1, xPercent = 0.35f, yPercent = 0.38f, radiusPercent = 0.07f))
                hazards.add(Hazard(id = 2, xPercent = 0.65f, yPercent = 0.62f, radiusPercent = 0.07f))
            }
            else -> {
                // High density puzzle
                wheels.add(Wheel(id = 1, xPercent = 0.40f, yPercent = 0.25f))
                wheels.add(Wheel(id = 2, xPercent = 0.75f, yPercent = 0.35f))
                wheels.add(Wheel(id = 3, xPercent = 0.25f, yPercent = 0.60f))
                wheels.add(Wheel(id = 4, xPercent = 0.60f, yPercent = 0.70f))
                hazards.add(Hazard(id = 1, xPercent = 0.50f, yPercent = 0.45f, radiusPercent = 0.08f))
                hazards.add(Hazard(id = 2, xPercent = 0.45f, yPercent = 0.80f, radiusPercent = 0.06f))
            }
        }

        return RescueLevelConfig(
            levelNumber = level,
            startX = startX,
            startY = startY,
            targetX = targetX,
            targetY = targetY,
            wheels = wheels,
            hazards = hazards,
            totalHostages = totalHostages,
            requiredSaved = requiredSaved
        )
    }
}
