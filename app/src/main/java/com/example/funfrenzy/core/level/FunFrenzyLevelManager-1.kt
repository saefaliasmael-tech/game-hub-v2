package com.example.funfrenzy.core.level

import androidx.compose.ui.geometry.Rect
import com.example.funfrenzy.core.model.ExitPortal
import com.example.funfrenzy.core.model.FrenzyHazard
import com.example.funfrenzy.core.model.FrenzyHazardType
import com.example.funfrenzy.core.model.FunFrenzyLevelConfig
import com.example.funfrenzy.core.model.RescueBuddy
import com.example.funfrenzy.core.model.RescueRope
import com.example.funfrenzy.core.model.RopeAnchor

object FunFrenzyLevelManager {
    const val TOTAL_LEVELS = 100
    const val VIRTUAL_WIDTH = 400f
    const val VIRTUAL_HEIGHT = 700f

    fun getLevel(levelNumber: Int): FunFrenzyLevelConfig {
        val clampedLevel = levelNumber.coerceIn(1, TOTAL_LEVELS)
        return generateLevel(clampedLevel)
    }

    private fun generateLevel(level: Int): FunFrenzyLevelConfig {
        return when (level) {
            1 -> FunFrenzyLevelConfig(
                levelNumber = 1,
                title = "First Cut",
                buddy = RescueBuddy(x = 200f, y = 280f),
                anchors = listOf(
                    RopeAnchor(id = 1, x = 200f, y = 100f)
                ),
                ropes = listOf(
                    RescueRope(id = 1, anchorId = 1, restLength = 180f, optimalCutOrder = 1)
                ),
                hazards = emptyList(),
                portal = ExitPortal(Rect(140f, 540f, 260f, 600f)),
                timeLimitSeconds = 15f
            )
            2 -> FunFrenzyLevelConfig(
                levelNumber = 2,
                title = "Swing Over Spikes",
                buddy = RescueBuddy(x = 180f, y = 300f),
                anchors = listOf(
                    RopeAnchor(id = 1, x = 100f, y = 120f),
                    RopeAnchor(id = 2, x = 300f, y = 120f)
                ),
                ropes = listOf(
                    RescueRope(id = 1, anchorId = 1, restLength = 200f, optimalCutOrder = 1),
                    RescueRope(id = 2, anchorId = 2, restLength = 220f, optimalCutOrder = 2)
                ),
                hazards = listOf(
                    FrenzyHazard(id = 10, bounds = Rect(120f, 560f, 240f, 600f), type = FrenzyHazardType.SPIKES)
                ),
                portal = ExitPortal(Rect(270f, 540f, 370f, 600f)),
                timeLimitSeconds = 15f
            )
            3 -> FunFrenzyLevelConfig(
                levelNumber = 3,
                title = "Sawblade Gauntlet",
                buddy = RescueBuddy(x = 200f, y = 250f),
                anchors = listOf(
                    RopeAnchor(id = 1, x = 80f, y = 110f),
                    RopeAnchor(id = 2, x = 200f, y = 90f),
                    RopeAnchor(id = 3, x = 320f, y = 110f)
                ),
                ropes = listOf(
                    RescueRope(id = 1, anchorId = 1, restLength = 190f, optimalCutOrder = 2),
                    RescueRope(id = 2, anchorId = 2, restLength = 160f, optimalCutOrder = 1),
                    RescueRope(id = 3, anchorId = 3, restLength = 190f, optimalCutOrder = 3)
                ),
                hazards = listOf(
                    FrenzyHazard(id = 10, bounds = Rect(170f, 380f, 230f, 440f), type = FrenzyHazardType.SAWBLADE),
                    FrenzyHazard(id = 11, bounds = Rect(20f, 580f, 180f, 620f), type = FrenzyHazardType.SPIKES)
                ),
                portal = ExitPortal(Rect(240f, 540f, 360f, 600f)),
                timeLimitSeconds = 16f
            )
            4 -> FunFrenzyLevelConfig(
                levelNumber = 4,
                title = "Lava Cavern",
                buddy = RescueBuddy(x = 150f, y = 280f),
                anchors = listOf(
                    RopeAnchor(id = 1, x = 80f, y = 100f),
                    RopeAnchor(id = 2, x = 280f, y = 100f)
                ),
                ropes = listOf(
                    RescueRope(id = 1, anchorId = 1, restLength = 190f, optimalCutOrder = 1),
                    RescueRope(id = 2, anchorId = 2, restLength = 230f, optimalCutOrder = 2)
                ),
                hazards = listOf(
                    FrenzyHazard(id = 10, bounds = Rect(40f, 580f, 360f, 640f), type = FrenzyHazardType.LAVA_PIT)
                ),
                portal = ExitPortal(Rect(290f, 480f, 380f, 540f)),
                timeLimitSeconds = 14f
            )
            else -> generateProceduralLevel(level)
        }
    }

    private fun generateProceduralLevel(level: Int): FunFrenzyLevelConfig {
        val tier = (level - 1) / 10
        val subIndex = (level - 1) % 10

        val buddyX = 160f + (subIndex % 3) * 30f
        val buddyY = 260f + (subIndex % 2) * 30f

        val anchorCount = when {
            subIndex >= 6 -> 3
            subIndex >= 2 -> 2
            else -> 2
        }

        val anchors = mutableListOf<RopeAnchor>()
        val ropes = mutableListOf<RescueRope>()

        for (i in 0 until anchorCount) {
            val ax = when (i) {
                0 -> 80f + (subIndex * 4f)
                1 -> 310f - (subIndex * 4f)
                else -> 200f + (if (subIndex % 2 == 0) 20f else -20f)
            }
            val ay = 80f + (i * 25f)
            anchors.add(RopeAnchor(id = i + 1, x = ax, y = ay))

            val dist = kotlin.math.hypot(buddyX - ax, buddyY - ay)
            ropes.add(
                RescueRope(
                    id = i + 1,
                    anchorId = i + 1,
                    restLength = dist,
                    optimalCutOrder = i + 1
                )
            )
        }

        val hazards = mutableListOf<FrenzyHazard>()
        val hazardType = if (level % 3 == 0) FrenzyHazardType.SAWBLADE else FrenzyHazardType.SPIKES

        val hx = 100f + ((level * 37) % 140)
        val hy = 420f + ((level * 23) % 100)
        hazards.add(FrenzyHazard(id = 100 + level, bounds = Rect(hx, hy, hx + 70f, hy + 45f), type = hazardType))

        if (level % 2 == 0) {
            hazards.add(FrenzyHazard(id = 200 + level, bounds = Rect(30f, 590f, 210f, 630f), type = FrenzyHazardType.SPIKES))
        }

        val portalX = if (level % 2 == 0) 260f else 60f
        val portalY = 530f + (subIndex % 3) * 15f
        val portal = ExitPortal(Rect(portalX, portalY, portalX + 100f, portalY + 50f))

        val titles = listOf(
            "Precision Cut", "Tension Release", "Pendulum Swing", "Saw Dodge",
            "Gravity Drop", "Safety Net", "Critical Slice", "Hazard Chasm",
            "Momentum Pivot", "Triple Tether", "Aerial Evacuation", "Perfect Timing"
        )
        val title = titles[(level - 1) % titles.size] + " Lv.$level"

        return FunFrenzyLevelConfig(
            levelNumber = level,
            title = title,
            buddy = RescueBuddy(x = buddyX, y = buddyY),
            anchors = anchors,
            ropes = ropes,
            hazards = hazards,
            portal = portal,
            timeLimitSeconds = 15f
        )
    }
}
