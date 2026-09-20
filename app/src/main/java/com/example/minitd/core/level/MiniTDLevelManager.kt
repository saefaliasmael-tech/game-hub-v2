package com.example.minitd.core.level

import com.example.minitd.core.model.*

object MiniTDLevelManager {

    const val MAX_LEVELS = 100

    fun getLevel(levelNumber: Int): TDLevelConfig {
        val level = levelNumber.coerceIn(1, MAX_LEVELS)

        val mapStyle = (level - 1) % 4
        val (waypoints, slots) = when (mapStyle) {
            0 -> getSCurveMap()
            1 -> getZigzagMap()
            2 -> getLoopMap()
            else -> getSpiralMap()
        }

        val waveCount = when {
            level <= 5 -> 3
            level <= 20 -> 4
            level <= 50 -> 5
            else -> 6
        }

        val waves = (1..waveCount).map { waveNum ->
            generateWave(level, waveNum)
        }

        return TDLevelConfig(
            levelNumber = level,
            waypoints = waypoints,
            slots = slots,
            waves = waves,
            startingGold = 250 + (level % 5) * 25,
            startingLives = 10
        )
    }

    private fun generateWave(level: Int, waveNum: Int): WaveConfig {
        val spawns = mutableListOf<EnemySpawn>()
        val count = 4 + waveNum * 2 + (level / 10)

        for (i in 0 until count) {
            val type = when {
                waveNum == 6 || (level % 5 == 0 && waveNum >= 3 && i == count - 1) -> EnemyType.BOSS
                i % 4 == 0 && level >= 3 -> EnemyType.TANK
                i % 3 == 0 -> EnemyType.FAST
                else -> EnemyType.NORMAL
            }
            spawns.add(EnemySpawn(type, delaySec = 1.0f + i * 1.2f))
        }

        return WaveConfig(waveNumber = waveNum, spawns = spawns)
    }

    private fun getSCurveMap(): Pair<List<Waypoint>, List<TowerSlot>> {
        val waypoints = listOf(
            Waypoint(0.05f, 0.20f),
            Waypoint(0.85f, 0.20f),
            Waypoint(0.85f, 0.50f),
            Waypoint(0.15f, 0.50f),
            Waypoint(0.15f, 0.80f),
            Waypoint(0.95f, 0.80f)
        )

        val slots = listOf(
            TowerSlot(1, 0.35f, 0.33f),
            TowerSlot(2, 0.65f, 0.33f),
            TowerSlot(3, 0.35f, 0.65f),
            TowerSlot(4, 0.65f, 0.65f),
            TowerSlot(5, 0.50f, 0.08f),
            TowerSlot(6, 0.50f, 0.92f)
        )

        return waypoints to slots
    }

    private fun getZigzagMap(): Pair<List<Waypoint>, List<TowerSlot>> {
        val waypoints = listOf(
            Waypoint(0.10f, 0.12f),
            Waypoint(0.85f, 0.30f),
            Waypoint(0.15f, 0.55f),
            Waypoint(0.85f, 0.75f),
            Waypoint(0.50f, 0.92f)
        )

        val slots = listOf(
            TowerSlot(1, 0.40f, 0.22f),
            TowerSlot(2, 0.60f, 0.42f),
            TowerSlot(3, 0.40f, 0.65f),
            TowerSlot(4, 0.65f, 0.85f),
            TowerSlot(5, 0.20f, 0.35f),
            TowerSlot(6, 0.80f, 0.60f)
        )

        return waypoints to slots
    }

    private fun getLoopMap(): Pair<List<Waypoint>, List<TowerSlot>> {
        val waypoints = listOf(
            Waypoint(0.50f, 0.08f),
            Waypoint(0.85f, 0.25f),
            Waypoint(0.85f, 0.75f),
            Waypoint(0.50f, 0.88f),
            Waypoint(0.15f, 0.75f),
            Waypoint(0.15f, 0.25f),
            Waypoint(0.50f, 0.48f)
        )

        val slots = listOf(
            TowerSlot(1, 0.50f, 0.28f),
            TowerSlot(2, 0.50f, 0.68f),
            TowerSlot(3, 0.35f, 0.48f),
            TowerSlot(4, 0.65f, 0.48f),
            TowerSlot(5, 0.88f, 0.50f),
            TowerSlot(6, 0.12f, 0.50f)
        )

        return waypoints to slots
    }

    private fun getSpiralMap(): Pair<List<Waypoint>, List<TowerSlot>> {
        val waypoints = listOf(
            Waypoint(0.08f, 0.10f),
            Waypoint(0.92f, 0.10f),
            Waypoint(0.92f, 0.88f),
            Waypoint(0.25f, 0.88f),
            Waypoint(0.25f, 0.32f),
            Waypoint(0.72f, 0.32f),
            Waypoint(0.72f, 0.68f),
            Waypoint(0.48f, 0.68f)
        )

        val slots = listOf(
            TowerSlot(1, 0.50f, 0.20f),
            TowerSlot(2, 0.78f, 0.50f),
            TowerSlot(3, 0.50f, 0.78f),
            TowerSlot(4, 0.38f, 0.50f),
            TowerSlot(5, 0.60f, 0.50f)
        )

        return waypoints to slots
    }
}
