package com.example.happyglass.core.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.happyglass.core.model.DrawnStroke
import com.example.happyglass.core.model.GlassContainer
import com.example.happyglass.core.model.HappyObstacle
import com.example.happyglass.core.model.ObstacleType
import com.example.happyglass.core.model.WaterDrop
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object HappyGlassPhysicsEngine {
    private const val GRAVITY = 0.32f
    private const val DAMPING_X = 0.985f
    private const val DAMPING_Y = 0.992f
    private const val BOUNCE_RESTITUTION = 0.42f
    private const val LINE_HALF_THICKNESS = 4.5f

    fun updateDrop(
        drop: WaterDrop,
        strokes: List<DrawnStroke>,
        glass: GlassContainer,
        obstacles: List<HappyObstacle>
    ) {
        if (drop.inGlass || drop.isLost) return

        drop.vy += GRAVITY
        drop.vx *= DAMPING_X
        drop.vy *= DAMPING_Y

        // Sub-stepping for smooth physics collision
        val steps = 2
        val stepVx = drop.vx / steps
        val stepVy = drop.vy / steps

        for (s in 0 until steps) {
            drop.x += stepVx
            drop.y += stepVy

            // Check drawn stroke collisions
            collideWithStrokes(drop, strokes)

            // Check obstacle collisions
            collideWithObstacles(drop, obstacles)
            if (drop.isLost) return

            // Check glass interactions
            checkGlassInteraction(drop, glass)
            if (drop.inGlass || drop.isLost) return
        }

        // Out of screen bounds check
        if (drop.y > 750f || drop.x < -60f || drop.x > 460f) {
            drop.isLost = true
        }
    }

    private fun collideWithStrokes(drop: WaterDrop, strokes: List<DrawnStroke>) {
        val dropRadius = drop.radius
        val totalThickness = dropRadius + LINE_HALF_THICKNESS

        for (stroke in strokes) {
            val points = stroke.points
            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]

                val segDx = p2.x - p1.x
                val segDy = p2.y - p1.y
                val segLenSq = segDx * segDx + segDy * segDy
                if (segLenSq < 1e-4f) continue

                val t = (((drop.x - p1.x) * segDx + (drop.y - p1.y) * segDy) / segLenSq).coerceIn(0f, 1f)
                val projX = p1.x + t * segDx
                val projY = p1.y + t * segDy

                val distDx = drop.x - projX
                val distDy = drop.y - projY
                val distSq = distDx * distDx + distDy * distDy

                if (distSq < totalThickness * totalThickness) {
                    val dist = sqrt(max(distSq, 1e-4f))
                    val nx = distDx / dist
                    val ny = distDy / dist

                    // Separate drop from stroke
                    drop.x = projX + nx * totalThickness
                    drop.y = projY + ny * totalThickness

                    // Reflect velocity
                    val dot = drop.vx * nx + drop.vy * ny
                    if (dot < 0f) {
                        drop.vx = (drop.vx - (1f + BOUNCE_RESTITUTION) * dot * nx)
                        drop.vy = (drop.vy - (1f + BOUNCE_RESTITUTION) * dot * ny)
                    }
                }
            }
        }
    }

    private fun collideWithObstacles(drop: WaterDrop, obstacles: List<HappyObstacle>) {
        val r = drop.radius
        for (obs in obstacles) {
            val b = obs.bounds
            // Check if drop is near or intersecting the rectangle
            if (drop.x + r >= b.left && drop.x - r <= b.right &&
                drop.y + r >= b.top && drop.y - r <= b.bottom
            ) {
                if (obs.type == ObstacleType.HAZARD_HOT_PLATE) {
                    drop.isLost = true
                    return
                }

                val restitution = if (obs.type == ObstacleType.BOUNCER_PAD) 1.25f else BOUNCE_RESTITUTION

                // Determine closest edge
                val overlapLeft = (drop.x + r) - b.left
                val overlapRight = b.right - (drop.x - r)
                val overlapTop = (drop.y + r) - b.top
                val overlapBottom = b.bottom - (drop.y - r)

                val minOverlap = min(min(overlapLeft, overlapRight), min(overlapTop, overlapBottom))

                when (minOverlap) {
                    overlapTop -> {
                        drop.y = b.top - r
                        if (drop.vy > 0f) drop.vy = -drop.vy * restitution
                    }
                    overlapBottom -> {
                        drop.y = b.bottom + r
                        if (drop.vy < 0f) drop.vy = -drop.vy * restitution
                    }
                    overlapLeft -> {
                        drop.x = b.left - r
                        if (drop.vx > 0f) drop.vx = -drop.vx * restitution
                    }
                    overlapRight -> {
                        drop.x = b.right + r
                        if (drop.vx < 0f) drop.vx = -drop.vx * restitution
                    }
                }
            }
        }
    }

    private fun checkGlassInteraction(drop: WaterDrop, glass: GlassContainer) {
        val r = drop.radius
        val innerLeft = glass.leftWallX + glass.wallThickness
        val innerRight = glass.rightWallX - glass.wallThickness
        val bottom = glass.bottomY - glass.wallThickness

        // Inside the glass column
        if (drop.x >= innerLeft && drop.x <= innerRight && drop.y >= glass.topY) {
            // Drop reached bottom of cup or resting water pool
            if (drop.y + r >= bottom) {
                drop.y = bottom - r
                drop.vy = 0f
                drop.vx *= 0.5f
                drop.inGlass = true
                return
            }
            // Inner wall bounces
            if (drop.x - r <= innerLeft && drop.vx < 0f) {
                drop.x = innerLeft + r
                drop.vx = -drop.vx * 0.3f
            } else if (drop.x + r >= innerRight && drop.vx > 0f) {
                drop.x = innerRight - r
                drop.vx = -drop.vx * 0.3f
            }
            return
        }

        // Exterior glass wall deflection
        if (drop.y >= glass.topY && drop.y <= glass.bottomY) {
            // Left outer wall
            if (drop.x + r >= glass.leftWallX && drop.x < innerLeft && drop.vx > 0f) {
                drop.x = glass.leftWallX - r
                drop.vx = -drop.vx * 0.4f
            }
            // Right outer wall
            else if (drop.x - r <= glass.rightWallX && drop.x > innerRight && drop.vx < 0f) {
                drop.x = glass.rightWallX + r
                drop.vx = -drop.vx * 0.4f
            }
        }
    }
}
