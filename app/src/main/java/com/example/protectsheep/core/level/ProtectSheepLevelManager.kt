package com.example.protectsheep.core.level

import com.example.protectsheep.core.model.*
import kotlin.random.Random

object ProtectSheepLevelManager {

    const val MAX_LEVELS = 100

    fun getLevel(levelNumber: Int): ProtectSheepLevelConfig {
        val lvl = levelNumber.coerceIn(1, MAX_LEVELS)
        val rng = Random(lvl * 8311L + 47L)

        val sheepList = mutableListOf<Sheep>()
        val hives = mutableListOf<Hive>()
        val hazards = mutableListOf<Hazard>()

        when {
            lvl == 1 -> {
                // Intro: Hive directly overhead, single sheep
                sheepList.add(Sheep(1, 0.50f, 0.65f))
                hives.add(Hive(1, 0.50f, 0.20f, beeCount = 10))
                hazards.add(Hazard(0.30f, 0.72f, 0.70f, 0.72f))
            }
            lvl == 2 -> {
                // Off-center hive and ledge
                sheepList.add(Sheep(1, 0.35f, 0.60f))
                hives.add(Hive(1, 0.75f, 0.25f, beeCount = 12))
                hazards.add(Hazard(0.20f, 0.68f, 0.50f, 0.68f))
            }
            lvl == 3 -> {
                // Two flanking hives
                sheepList.add(Sheep(1, 0.50f, 0.65f))
                hives.add(Hive(1, 0.20f, 0.30f, beeCount = 8))
                hives.add(Hive(2, 0.80f, 0.30f, beeCount = 8))
                hazards.add(Hazard(0.35f, 0.72f, 0.65f, 0.72f))
            }
            lvl == 4 -> {
                // Platform with spikes underneath
                sheepList.add(Sheep(1, 0.50f, 0.50f))
                hives.add(Hive(1, 0.50f, 0.18f, beeCount = 12))
                hazards.add(Hazard(0.35f, 0.58f, 0.65f, 0.58f))
                hazards.add(Hazard(0.20f, 0.85f, 0.80f, 0.85f, isSpike = true))
            }
            lvl == 5 -> {
                // 2 Sheep to protect together
                sheepList.add(Sheep(1, 0.38f, 0.65f))
                sheepList.add(Sheep(2, 0.62f, 0.65f))
                hives.add(Hive(1, 0.50f, 0.22f, beeCount = 14))
                hazards.add(Hazard(0.25f, 0.72f, 0.75f, 0.72f))
            }
            else -> {
                val hasDualSheep = lvl % 3 == 0
                val sheepCount = if (hasDualSheep) 2 else 1

                for (i in 0 until sheepCount) {
                    val sx = if (sheepCount == 1) 0.50f else (0.35f + i * 0.30f)
                    val sy = 0.55f + (i * 0.05f)
                    sheepList.add(Sheep(i + 1, sx, sy))
                    // Platform
                    hazards.add(Hazard(sx - 0.15f, sy + 0.07f, sx + 0.15f, sy + 0.07f))
                }

                val hiveCount = (1 + (lvl % 3)).coerceIn(1, 3)
                for (h in 0 until hiveCount) {
                    val hx = 0.20f + h * 0.30f
                    val hy = 0.18f + (rng.nextFloat() * 0.12f)
                    hives.add(Hive(h + 1, hx, hy, beeCount = 8 + (lvl % 5)))
                }

                // Add ground hazard or spikes
                if (lvl % 2 == 0) {
                    hazards.add(Hazard(0.10f, 0.88f, 0.90f, 0.88f, isSpike = true))
                }
            }
        }

        val maxInk = (1.2f + (lvl % 4) * 0.2f).coerceAtMost(2.0f)

        return ProtectSheepLevelConfig(
            levelNumber = lvl,
            sheepList = sheepList,
            hives = hives,
            hazards = hazards,
            maxInk = maxInk
        )
    }
}
