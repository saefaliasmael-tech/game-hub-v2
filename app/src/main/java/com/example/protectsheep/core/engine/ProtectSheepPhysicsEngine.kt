package com.example.protectsheep.core.engine

import androidx.compose.ui.geometry.Offset
import com.example.protectsheep.core.model.BarrierStroke
import com.example.protectsheep.core.model.HazardType
import com.example.protectsheep.core.model.SheepTarget
import com.example.protectsheep.core.model.WolfAttacker
import kotlin.math.max
import kotlin.math.sqrt

object ProtectSheepPhysicsEngine {
    private const val BARRIER_HALF_THICKNESS = 6f

    fun updateSimulation(
        wolves: List<WolfAttacker>,
        sheepList: List<SheepTarget>,
        barriers: List<BarrierStroke>,
        onSheepTouched: (SheepTarget) -> Unit
    ) {
        for (wolf in wolves) {
            // Find closest living sheep
            var closestDistSq = Float.MAX_VALUE
            var targetSheep: SheepTarget? = null

            for (sheep in sheepList) {
                if (sheep.isAlive) {
                    val dSq = (sheep.x - wolf.x) * (sheep.x - wolf.x) + (sheep.y - wolf.y) * (sheep.y - wolf.y)
                    if (dSq < closestDistSq) {
                        closestDistSq = dSq
                        targetSheep = sheep
                    }
                }
            }

            if (targetSheep == null) continue

            // Determine steering vector
            if (wolf.type == HazardType.FALLING_BOULDER) {
                wolf.vy = wolf.speed
                wolf.vx = ((targetSheep.x - wolf.x) * 0.02f).coerceIn(-1.5f, 1.5f)
            } else {
                val dx = targetSheep.x - wolf.x
                val dy = targetSheep.y - wolf.y
                val dist = sqrt(max(dx * dx + dy * dy, 1e-4f))
                wolf.vx = (dx / dist) * wolf.speed
                wolf.vy = (dy / dist) * wolf.speed
            }

            // Sub-step movement
            val steps = 2
            val stepVx = wolf.vx / steps
            val stepVy = wolf.vy / steps

            for (s in 0 until steps) {
                wolf.x += stepVx
                wolf.y += stepVy

                // Collide against drawn barrier strokes
                collideWithBarriers(wolf, barriers)

                // Check collision with sheep
                for (sheep in sheepList) {
                    if (sheep.isAlive) {
                        val touchDist = wolf.radius + sheep.radius
                        val dSq = (sheep.x - wolf.x) * (sheep.x - wolf.x) + (sheep.y - wolf.y) * (sheep.y - wolf.y)
                        if (dSq < touchDist * touchDist) {
                            sheep.isAlive = false
                            onSheepTouched(sheep)
                            return
                        }
                    }
                }
            }
        }
    }

    private fun collideWithBarriers(wolf: WolfAttacker, barriers: List<BarrierStroke>) {
        val totalThickness = wolf.radius + BARRIER_HALF_THICKNESS

        for (stroke in barriers) {
            val points = stroke.points
            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]

                val segDx = p2.x - p1.x
                val segDy = p2.y - p1.y
                val segLenSq = segDx * segDx + segDy * segDy
                if (segLenSq < 1e-4f) continue

                val t = (((wolf.x - p1.x) * segDx + (wolf.y - p1.y) * segDy) / segLenSq).coerceIn(0f, 1f)
                val projX = p1.x + t * segDx
                val projY = p1.y + t * segDy

                val distDx = wolf.x - projX
                val distDy = wolf.y - projY
                val distSq = distDx * distDx + distDy * distDy

                if (distSq < totalThickness * totalThickness) {
                    val dist = sqrt(max(distSq, 1e-4f))
                    val nx = distDx / dist
                    val ny = distDy / dist

                    // Push wolf away from barrier
                    wolf.x = projX + nx * totalThickness
                    wolf.y = projY + ny * totalThickness

                    // Slide along tangent
                    val dot = wolf.vx * nx + wolf.vy * ny
                    if (dot < 0f) {
                        wolf.vx -= dot * nx
                        wolf.vy -= dot * ny
                    }
                }
            }
        }
    }
}
