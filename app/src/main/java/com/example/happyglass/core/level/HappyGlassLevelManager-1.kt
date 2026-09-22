package com.example.happyglass.core.level

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.happyglass.core.model.Faucet
import com.example.happyglass.core.model.GlassContainer
import com.example.happyglass.core.model.HappyGlassLevelConfig
import com.example.happyglass.core.model.HappyObstacle
import com.example.happyglass.core.model.ObstacleType
import kotlin.math.cos
import kotlin.math.sin

object HappyGlassLevelManager {
    const val TOTAL_LEVELS = 100
    const val VIRTUAL_WIDTH = 400f
    const val VIRTUAL_HEIGHT = 700f

    fun getLevel(levelNumber: Int): HappyGlassLevelConfig {
        val clampedLevel = levelNumber.coerceIn(1, TOTAL_LEVELS)
        return generateLevel(clampedLevel)
    }

    private fun generateLevel(level: Int): HappyGlassLevelConfig {
        return when (level) {
            1 -> HappyGlassLevelConfig(
                levelNumber = 1,
                title = "Gentle Slope",
                faucet = Faucet(x = 100f, y = 120f, totalDrops = 80),
                glass = GlassContainer(centerX = 300f, bottomY = 560f, width = 120f, height = 140f),
                obstacles = emptyList(),
                maxInkLength = 1200f,
                requiredDrops = 30
            )
            2 -> HappyGlassLevelConfig(
                levelNumber = 2,
                title = "The High Divider",
                faucet = Faucet(x = 120f, y = 110f, totalDrops = 85),
                glass = GlassContainer(centerX = 300f, bottomY = 560f, width = 120f, height = 140f),
                obstacles = listOf(
                    HappyObstacle(Rect(180f, 260f, 220f, 480f), ObstacleType.SOLID_BLOCK)
                ),
                maxInkLength = 1100f,
                requiredDrops = 32
            )
            3 -> HappyGlassLevelConfig(
                levelNumber = 3,
                title = "Stepping Stones",
                faucet = Faucet(x = 200f, y = 100f, totalDrops = 90),
                glass = GlassContainer(centerX = 200f, bottomY = 600f, width = 120f, height = 140f),
                obstacles = listOf(
                    HappyObstacle(Rect(150f, 240f, 250f, 280f), ObstacleType.SOLID_BLOCK),
                    HappyObstacle(Rect(80f, 380f, 180f, 420f), ObstacleType.SOLID_BLOCK),
                    HappyObstacle(Rect(220f, 380f, 320f, 420f), ObstacleType.SOLID_BLOCK)
                ),
                maxInkLength = 1000f,
                requiredDrops = 35
            )
            4 -> HappyGlassLevelConfig(
                levelNumber = 4,
                title = "Hot Plate Danger",
                faucet = Faucet(x = 100f, y = 110f, totalDrops = 85),
                glass = GlassContainer(centerX = 310f, bottomY = 570f, width = 120f, height = 140f),
                obstacles = listOf(
                    HappyObstacle(Rect(160f, 340f, 260f, 380f), ObstacleType.HAZARD_HOT_PLATE)
                ),
                maxInkLength = 1000f,
                requiredDrops = 30
            )
            5 -> HappyGlassLevelConfig(
                levelNumber = 5,
                title = "Bouncer Trampoline",
                faucet = Faucet(x = 90f, y = 110f, totalDrops = 85),
                glass = GlassContainer(centerX = 310f, bottomY = 320f, width = 120f, height = 140f),
                obstacles = listOf(
                    HappyObstacle(Rect(60f, 450f, 180f, 490f), ObstacleType.BOUNCER_PAD)
                ),
                maxInkLength = 950f,
                requiredDrops = 28
            )
            else -> generateProceduralLevel(level)
        }
    }

    private fun generateProceduralLevel(level: Int): HappyGlassLevelConfig {
        val tier = (level - 1) / 10
        val subIndex = (level - 1) % 10

        val faucetX = when (subIndex % 4) {
            0 -> 80f + (level * 7 % 60)
            1 -> 200f + (level * 13 % 40) - 20f
            2 -> 310f - (level * 9 % 60)
            else -> 120f + (level * 11 % 80)
        }
        val faucetY = 90f + (subIndex * 5f)

        val glassX = when ((subIndex + tier) % 3) {
            0 -> 290f - (subIndex * 8f)
            1 -> 110f + (subIndex * 8f)
            else -> 200f + (if (subIndex % 2 == 0) 60f else -60f)
        }
        val glassY = 540f + (subIndex % 5) * 15f
        val glassWidth = 115f - (tier * 2f).coerceAtMost(25f)
        val glassHeight = 135f

        val obstacles = mutableListOf<HappyObstacle>()
        val obstacleCount = (1 + (level / 12)).coerceAtMost(5)

        for (i in 0 until obstacleCount) {
            val obsX = 50f + ((level * 37 + i * 83) % 250)
            val obsY = 220f + ((level * 53 + i * 71) % 240)
            val obsW = 60f + ((level + i * 19) % 70)
            val obsH = 25f + ((level + i * 11) % 35)

            val type = when {
                (level + i) % 5 == 0 -> ObstacleType.HAZARD_HOT_PLATE
                (level + i) % 7 == 0 -> ObstacleType.BOUNCER_PAD
                else -> ObstacleType.SOLID_BLOCK
            }

            obstacles.add(HappyObstacle(Rect(obsX, obsY, obsX + obsW, obsY + obsH), type))
        }

        val totalDrops = (80 + tier * 2).coerceAtMost(100)
        val requiredDrops = (28 + tier * 2).coerceAtMost(45)
        val maxInk = (1200f - tier * 35f).coerceAtLeast(650f)

        val titles = listOf(
            "Funnel Route", "Twin Pillars", "Steep Cascade", "Red Hot Ledge",
            "Gravity Slalom", "The Great Arch", "Springboard", "Zig-Zag Drop",
            "Chamber Divide", "Perilous Chute", "Water Wheel", "Bouncing Brook"
        )
        val title = titles[(level - 1) % titles.size] + " Lv.$level"

        return HappyGlassLevelConfig(
            levelNumber = level,
            title = title,
            faucet = Faucet(x = faucetX, y = faucetY, totalDrops = totalDrops),
            glass = GlassContainer(centerX = glassX, bottomY = glassY, width = glassWidth, height = glassHeight),
            obstacles = obstacles,
            maxInkLength = maxInk,
            requiredDrops = requiredDrops
        )
    }
}
