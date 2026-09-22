package com.example.protectsheep.core.level

import com.example.protectsheep.core.model.HazardType
import com.example.protectsheep.core.model.ProtectSheepLevelConfig
import com.example.protectsheep.core.model.SheepTarget
import com.example.protectsheep.core.model.WolfAttacker

object ProtectSheepLevelManager {
    const val TOTAL_LEVELS = 100
    const val VIRTUAL_WIDTH = 400f
    const val VIRTUAL_HEIGHT = 700f

    fun getLevel(levelNumber: Int): ProtectSheepLevelConfig {
        val clampedLevel = levelNumber.coerceIn(1, TOTAL_LEVELS)
        return generateLevel(clampedLevel)
    }

    private fun generateLevel(level: Int): ProtectSheepLevelConfig {
        return when (level) {
            1 -> ProtectSheepLevelConfig(
                levelNumber = 1,
                title = "Lone Wolf",
                sheepList = listOf(
                    SheepTarget(id = 1, x = 200f, y = 420f)
                ),
                wolves = listOf(
                    WolfAttacker(id = 1, x = 200f, y = 140f, speed = 2.2f)
                ),
                maxInkLength = 1000f,
                surviveSeconds = 7f
            )
            2 -> ProtectSheepLevelConfig(
                levelNumber = 2,
                title = "Pincer Ambush",
                sheepList = listOf(
                    SheepTarget(id = 1, x = 200f, y = 420f)
                ),
                wolves = listOf(
                    WolfAttacker(id = 1, x = 60f, y = 420f, speed = 2.4f),
                    WolfAttacker(id = 2, x = 340f, y = 420f, speed = 2.4f)
                ),
                maxInkLength = 950f,
                surviveSeconds = 8f
            )
            3 -> ProtectSheepLevelConfig(
                levelNumber = 3,
                title = "Falling Rocks",
                sheepList = listOf(
                    SheepTarget(id = 1, x = 200f, y = 500f)
                ),
                wolves = listOf(
                    WolfAttacker(id = 1, x = 160f, y = 120f, speed = 3.2f, type = HazardType.FALLING_BOULDER),
                    WolfAttacker(id = 2, x = 240f, y = 100f, speed = 3.5f, type = HazardType.FALLING_BOULDER)
                ),
                maxInkLength = 900f,
                surviveSeconds = 7f
            )
            4 -> ProtectSheepLevelConfig(
                levelNumber = 4,
                title = "Twin Lambs",
                sheepList = listOf(
                    SheepTarget(id = 1, x = 140f, y = 420f),
                    SheepTarget(id = 2, x = 260f, y = 420f)
                ),
                wolves = listOf(
                    WolfAttacker(id = 1, x = 200f, y = 160f, speed = 2.5f),
                    WolfAttacker(id = 2, x = 200f, y = 600f, speed = 2.5f)
                ),
                maxInkLength = 1100f,
                surviveSeconds = 8f
            )
            5 -> ProtectSheepLevelConfig(
                levelNumber = 5,
                title = "Angry Bee Swarm",
                sheepList = listOf(
                    SheepTarget(id = 1, x = 200f, y = 400f)
                ),
                wolves = listOf(
                    WolfAttacker(id = 1, x = 50f, y = 250f, speed = 2.8f, type = HazardType.BEE_SWARM),
                    WolfAttacker(id = 2, x = 350f, y = 250f, speed = 2.8f, type = HazardType.BEE_SWARM),
                    WolfAttacker(id = 3, x = 200f, y = 620f, speed = 2.2f, type = HazardType.WOLF)
                ),
                maxInkLength = 950f,
                surviveSeconds = 9f
            )
            else -> generateProceduralLevel(level)
        }
    }

    private fun generateProceduralLevel(level: Int): ProtectSheepLevelConfig {
        val tier = (level - 1) / 10
        val subIndex = (level - 1) % 10

        val sheepCount = if (level % 6 == 0) 2 else 1
        val sheepList = mutableListOf<SheepTarget>()

        if (sheepCount == 1) {
            val sx = 200f + (if (subIndex % 2 == 0) 20f else -20f)
            val sy = 380f + (subIndex % 3) * 25f
            sheepList.add(SheepTarget(1, sx, sy))
        } else {
            val sy = 400f + (subIndex % 2) * 20f
            sheepList.add(SheepTarget(1, 140f, sy))
            sheepList.add(SheepTarget(2, 260f, sy))
        }

        val wolfCount = (2 + (level / 15)).coerceIn(2, 5)
        val wolves = mutableListOf<WolfAttacker>()

        for (i in 0 until wolfCount) {
            val type = when {
                (level + i) % 4 == 0 -> HazardType.FALLING_BOULDER
                (level + i) % 5 == 0 -> HazardType.BEE_SWARM
                else -> HazardType.WOLF
            }

            val (wx, wy) = when (i % 4) {
                0 -> Pair(50f + (i * 40f), 120f)
                1 -> Pair(350f - (i * 30f), 150f)
                2 -> Pair(50f, 540f - (i * 30f))
                else -> Pair(350f, 540f - (i * 30f))
            }

            val speed = (2.2f + (tier * 0.15f) + (i * 0.15f)).coerceAtMost(4.2f)
            wolves.add(WolfAttacker(id = i + 1, x = wx, y = wy, speed = speed, type = type))
        }

        val surviveSeconds = (7f + (tier * 0.5f)).coerceAtMost(11f)
        val maxInk = (1050f - tier * 30f).coerceAtLeast(650f)

        val titles = listOf(
            "Forest Perimeter", "Wolf Territory", "Meadow Guard", "Shield the Flock",
            "Swarm Defense", "Night Howlers", "Valley of Rocks", "Pasture Fortress",
            "Midnight Raid", "Great Shepherd", "Alpha Predator", "Sanctuary"
        )
        val title = titles[(level - 1) % titles.size] + " Lv.$level"

        return ProtectSheepLevelConfig(
            levelNumber = level,
            title = title,
            sheepList = sheepList,
            wolves = wolves,
            maxInkLength = maxInk,
            surviveSeconds = surviveSeconds
        )
    }
}
