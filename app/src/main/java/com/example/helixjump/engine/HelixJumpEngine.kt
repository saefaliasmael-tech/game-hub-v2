package com.example.helixjump.engine

import com.example.helixjump.model.*
import kotlin.random.Random

class HelixJumpEngine {
    companion object {
        const val TOTAL_FLOORS = 15
        const val BOUNCE_VELOCITY = -9.5f
        const val GRAVITY = 0.5f
        const val FLOOR_SPACING = 100f
    }

    fun generateFloors(level: Int): List<HelixFloor> {
        val random = Random(level * 31 + 7)
        val floors = mutableListOf<HelixFloor>()

        for (i in 0 until TOTAL_FLOORS) {
            if (i == TOTAL_FLOORS - 1) {
                // Final Finish Floor
                floors.add(
                    HelixFloor(
                        floorIndex = i,
                        sectors = listOf(HelixSector(0f, 360f, SectorType.FINISH))
                    )
                )
            } else {
                val sectors = mutableListOf<HelixSector>()
                val gapStart = random.nextFloat() * 360f
                val gapSweep = 60f + random.nextFloat() * 20f

                // Empty Gap
                sectors.add(HelixSector(gapStart, gapSweep, SectorType.EMPTY))

                val remainingAngle = 360f - gapSweep
                val numHazardSectors = if (i > 1) minOf(1 + i / 4, 3) else 0

                val hazardStart = (gapStart + gapSweep + 40f + random.nextFloat() * (remainingAngle - 80f)) % 360f
                val hazardSweep = 30f + minOf(i * 3f, 35f)

                // Fill remaining with Normal and Hazard
                var currentAngle = (gapStart + gapSweep) % 360f
                var angleLeft = remainingAngle

                while (angleLeft > 1f) {
                    val step = minOf(angleLeft, 45f)
                    val isHazard = numHazardSectors > 0 &&
                            absAngleDiff(currentAngle, hazardStart) < hazardSweep

                    sectors.add(
                        HelixSector(
                            startAngle = currentAngle,
                            sweepAngle = step,
                            type = if (isHazard) SectorType.HAZARD else SectorType.NORMAL
                        )
                    )
                    currentAngle = (currentAngle + step) % 360f
                    angleLeft -= step
                }

                floors.add(HelixFloor(floorIndex = i, sectors = sectors))
            }
        }
        return floors
    }

    private fun absAngleDiff(a1: Float, a2: Float): Float {
        val diff = kotlin.math.abs(a1 - a2) % 360f
        return if (diff > 180f) 360f - diff else diff
    }

    fun updatePhysics(
        state: HelixJumpState,
        onBounce: (Boolean) -> Unit,
        onPassFloor: (Int) -> Unit,
        onHazardHit: () -> Unit,
        onWin: () -> Unit
    ): HelixJumpState {
        if (state.isGameOver || state.isWon) return state

        val ball = state.ball
        var newY = ball.y + ball.velocityY
        var newVelY = ball.velocityY + GRAVITY
        var streak = state.comboStreak
        var score = state.score
        var currentFloor = ball.currentFloor

        // Check floor collision
        val nextFloorY = currentFloor * FLOOR_SPACING
        if (ball.y <= nextFloorY && newY >= nextFloorY) {
            val floor = state.floors.getOrNull(currentFloor)
            if (floor != null) {
                // Calculate sector hit at the front of the tower
                // The front angle is (360 - towerRotation % 360) % 360
                val hitAngle = ((360f - (state.towerRotation % 360f)) % 360f + 360f) % 360f
                val hitSector = floor.sectors.find { s ->
                    val normStart = (s.startAngle % 360f + 360f) % 360f
                    val endAngle = normStart + s.sweepAngle
                    if (endAngle <= 360f) {
                        hitAngle in normStart..endAngle
                    } else {
                        hitAngle >= normStart || hitAngle <= (endAngle % 360f)
                    }
                }

                when (hitSector?.type) {
                    SectorType.EMPTY -> {
                        // Passed through the gap!
                        streak++
                        val points = 10 * streak
                        score += points
                        currentFloor++
                        onPassFloor(streak)

                        if (currentFloor >= TOTAL_FLOORS - 1) {
                            onWin()
                            return state.copy(
                                ball = ball.copy(y = newY, velocityY = newVelY, currentFloor = currentFloor),
                                score = score,
                                isWon = true,
                                comboStreak = streak
                            )
                        }
                    }
                    SectorType.HAZARD -> {
                        if (streak >= 3) {
                            // Smash through with fireball streak!
                            streak = 0
                            newVelY = BOUNCE_VELOCITY
                            newY = nextFloorY
                            score += 50
                            onBounce(true)
                        } else {
                            // Deadly hazard
                            onHazardHit()
                            return state.copy(
                                ball = ball.copy(y = nextFloorY, velocityY = 0f),
                                isGameOver = true
                            )
                        }
                    }
                    SectorType.FINISH -> {
                        onWin()
                        return state.copy(
                            ball = ball.copy(y = nextFloorY, velocityY = 0f),
                            isWon = true
                        )
                    }
                    else -> {
                        // Safe bounce
                        streak = 0
                        newVelY = BOUNCE_VELOCITY
                        newY = nextFloorY
                        onBounce(false)
                    }
                }
            }
        }

        return state.copy(
            ball = ball.copy(
                y = newY,
                velocityY = newVelY,
                currentFloor = currentFloor,
                isSmashing = streak >= 3
            ),
            score = score,
            comboStreak = streak
        )
    }
}
