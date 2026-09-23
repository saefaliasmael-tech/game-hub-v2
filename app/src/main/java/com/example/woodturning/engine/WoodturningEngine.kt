package com.example.woodturning.engine

import com.example.woodturning.model.WoodLevel
import com.example.woodturning.model.WoodShavingParticle
import com.example.woodturning.model.WoodturningState
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

object WoodLevels {
    const val SEGMENTS = 50

    val levels = listOf(
        // Level 1: Baseball Bat / Club
        WoodLevel(
            levelNumber = 1,
            name = "Classic Bat",
            targetProfile = FloatArray(SEGMENTS) { i ->
                val t = i.toFloat() / SEGMENTS
                if (t < 0.25f) 0.35f else 0.35f + (t - 0.25f) * 0.7f
            }
        ),
        // Level 2: Hourglass / Goblet
        WoodLevel(
            levelNumber = 2,
            name = "Royal Goblet",
            targetProfile = FloatArray(SEGMENTS) { i ->
                val t = i.toFloat() / SEGMENTS
                if (t < 0.2f) 0.85f // Base
                else if (t < 0.45f) 0.3f // Stem
                else 0.3f + sin((t - 0.45f) * 3.1415f / 0.55f) * 0.55f // Cup
            }
        ),
        // Level 3: Chess Pawn
        WoodLevel(
            levelNumber = 3,
            name = "Chess Pawn",
            targetProfile = FloatArray(SEGMENTS) { i ->
                val t = i.toFloat() / SEGMENTS
                when {
                    t < 0.2f -> 0.9f
                    t < 0.6f -> 0.45f + (0.6f - t) * 0.5f
                    t < 0.75f -> 0.5f
                    else -> 0.75f - abs(t - 0.88f) * 2.5f
                }
            }
        )
    )
}

class WoodturningEngine {
    val particles = mutableListOf<WoodShavingParticle>()

    fun initState(level: WoodLevel): WoodturningState {
        particles.clear()
        val initialRadii = FloatArray(WoodLevels.SEGMENTS) { 1.0f }
        return WoodturningState(
            levelNumber = level.levelNumber,
            currentRadii = initialRadii,
            targetRadii = level.targetProfile.copyOf(),
            accuracy = calculateAccuracy(initialRadii, level.targetProfile)
        )
    }

    fun carve(
        state: WoodturningState,
        normalizedX: Float, // 0..1 along log length
        normalizedDepth: Float, // 0..1 radius to cut to
        onCarveHit: () -> Unit
    ): WoodturningState {
        val segment = (normalizedX * WoodLevels.SEGMENTS).toInt().coerceIn(0, WoodLevels.SEGMENTS - 1)
        val radii = state.currentRadii.copyOf()
        val targetRadius = state.targetRadii[segment]
        var didCut = false

        // Carve neighboring segments with a slight curved chisel profile
        for (offset in -2..2) {
            val idx = segment + offset
            if (idx in 0 until WoodLevels.SEGMENTS) {
                val falloff = 1f - (abs(offset) * 0.2f)
                val cutTo = normalizedDepth.coerceAtLeast(targetRadius) * falloff
                if (radii[idx] > cutTo) {
                    radii[idx] = maxOf(radii[idx] - 0.05f, cutTo)
                    didCut = true
                }
            }
        }

        if (didCut) {
            onCarveHit()
            // Spawn wood shavings
            val random = Random.Default
            for (k in 0..3) {
                particles.add(
                    WoodShavingParticle(
                        x = normalizedX,
                        y = 0.5f - radii[segment] * 0.25f,
                        vx = (random.nextFloat() - 0.5f) * 0.015f,
                        vy = -random.nextFloat() * 0.02f - 0.01f,
                        life = 1.0f
                    )
                )
            }
        }

        val accuracy = calculateAccuracy(radii, state.targetRadii)
        return state.copy(currentRadii = radii, accuracy = accuracy)
    }

    fun updateParticles() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx
            p.y += p.vy
            p.vy += 0.001f // gravity
            p.life -= 0.04f
            if (p.life <= 0f) {
                iterator.remove()
            }
        }
    }

    private fun calculateAccuracy(current: FloatArray, target: FloatArray): Float {
        var totalDiff = 0f
        for (i in current.indices) {
            totalDiff += abs(current[i] - target[i])
        }
        val avgDiff = totalDiff / current.size
        return (1.0f - avgDiff).coerceIn(0f, 1f)
    }
}
