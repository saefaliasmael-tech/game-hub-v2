package com.example.funfrenzy.core.engine

import androidx.compose.ui.graphics.Color
import com.example.funfrenzy.core.model.*
import kotlin.math.abs
import kotlin.random.Random

object FunFrenzyEngine {

    fun initSubState(type: MicroGameType, seed: Long): MicroSubState {
        val rng = Random(seed)
        return when (type) {
            MicroGameType.TAP_RUSH -> {
                val req = rng.nextInt(7, 13)
                MicroSubState(
                    currentTaps = 0,
                    requiredTaps = req
                )
            }
            MicroGameType.CATCH_FALLING -> {
                MicroSubState(
                    bucketX = 0.5f,
                    gemX = rng.nextFloat() * 0.7f + 0.15f,
                    gemY = 0.05f,
                    gemSpeed = rng.nextFloat() * 0.1f + 0.35f
                )
            }
            MicroGameType.POP_BALLOONS -> {
                val colors = listOf(
                    Color(0xFFEF4444),
                    Color(0xFF3B82F6),
                    Color(0xFF10B981),
                    Color(0xFFF59E0B),
                    Color(0xFF8B5CF6)
                )
                val balloons = (0 until 4).map { i ->
                    BalloonItem(
                        id = i,
                        x = 0.1f + (i % 2) * 0.5f + rng.nextFloat() * 0.1f,
                        y = 0.1f + (i / 2) * 0.45f + rng.nextFloat() * 0.1f,
                        color = colors[i % colors.size]
                    )
                }
                MicroSubState(
                    balloons = balloons,
                    poppedCount = 0,
                    requiredPops = 4
                )
            }
            MicroGameType.DODGE_ROCKS -> {
                val rocks = (0 until 4).map { i ->
                    RockItem(
                        id = i,
                        x = rng.nextFloat() * 0.75f + 0.12f,
                        y = -0.1f - i * 0.3f,
                        speed = 0.45f + rng.nextFloat() * 0.2f
                    )
                }
                MicroSubState(
                    playerX = 0.5f,
                    rocks = rocks
                )
            }
            MicroGameType.STOP_NEEDLE -> {
                val targetStart = rng.nextInt(100, 240).toFloat()
                MicroSubState(
                    needleAngle = 0f,
                    needleSpeed = 220f + rng.nextInt(0, 80),
                    targetZoneStartAngle = targetStart,
                    targetZoneEndAngle = targetStart + 60f,
                    isNeedleStopped = false
                )
            }
            MicroGameType.CUT_WIRE -> {
                val wirePalette = listOf(
                    WireItem("Red", Color(0xFFEF4444)),
                    WireItem("Blue", Color(0xFF3B82F6)),
                    WireItem("Yellow", Color(0xFFF59E0B)),
                    WireItem("Green", Color(0xFF10B981))
                ).shuffled(rng)
                val target = wirePalette.random(rng).colorName
                MicroSubState(
                    wires = wirePalette,
                    targetWireColorName = target
                )
            }
            MicroGameType.FIND_ODD_ONE -> {
                val pairs = listOf(
                    Pair("🍎", "🍒"),
                    Pair("⭐", "🌟"),
                    Pair("🐱", "🐶"),
                    Pair("🔷", "🔶"),
                    Pair("🚀", "🛸")
                )
                val pair = pairs.random(rng)
                val oddIdx = rng.nextInt(0, 9)
                MicroSubState(
                    totalGridItems = 9,
                    oddIndex = oddIdx,
                    normalSymbol = pair.first,
                    oddSymbol = pair.second,
                    selectedIndex = null
                )
            }
        }
    }

    fun stepDynamic(
        type: MicroGameType,
        state: MicroSubState,
        dt: Float,
        speedMult: Float
    ): Pair<MicroSubState, MicroResult> {
        return when (type) {
            MicroGameType.CATCH_FALLING -> {
                val newY = state.gemY + state.gemSpeed * speedMult * dt
                if (newY >= 0.82f && newY <= 0.88f) {
                    if (abs(state.gemX - state.bucketX) < 0.12f) {
                        Pair(state.copy(gemY = newY), MicroResult.SUCCESS)
                    } else {
                        Pair(state.copy(gemY = newY), MicroResult.PENDING)
                    }
                } else if (newY > 0.92f) {
                    Pair(state.copy(gemY = newY), MicroResult.FAIL)
                } else {
                    Pair(state.copy(gemY = newY), MicroResult.PENDING)
                }
            }
            MicroGameType.DODGE_ROCKS -> {
                var collided = false
                val updatedRocks = state.rocks.map { r ->
                    val nextY = r.y + r.speed * speedMult * dt
                    if (nextY in 0.78f..0.92f && abs(r.x - state.playerX) < 0.10f) {
                        collided = true
                    }
                    r.copy(y = nextY)
                }
                if (collided) {
                    Pair(state.copy(rocks = updatedRocks), MicroResult.FAIL)
                } else {
                    Pair(state.copy(rocks = updatedRocks), MicroResult.PENDING)
                }
            }
            MicroGameType.STOP_NEEDLE -> {
                if (state.isNeedleStopped) {
                    Pair(state, MicroResult.PENDING)
                } else {
                    val nextAngle = (state.needleAngle + state.needleSpeed * speedMult * dt) % 360f
                    Pair(state.copy(needleAngle = nextAngle), MicroResult.PENDING)
                }
            }
            else -> Pair(state, MicroResult.PENDING)
        }
    }
}
