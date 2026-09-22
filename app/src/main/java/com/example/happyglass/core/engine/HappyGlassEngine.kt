package com.example.happyglass.core.engine

import androidx.compose.ui.geometry.Offset
import com.example.happyglass.core.model.Glass
import com.example.happyglass.core.model.ObstacleLine
import com.example.happyglass.core.model.WaterParticle
import com.example.happyglass.core.model.WaterTap
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object HappyGlassEngine {
    const val DROP_RADIUS = 0.012f
    private const val GRAVITY = 0.0018f
    private const val DAMPING = 0.94f
    private const val BOUNCE = 0.35f

    fun calculatePathLength(points: List<Offset>): Float {
        if (points.size < 2) return 0f
        var sum = 0f
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            sum += hypot(p2.x - p1.x, p2.y - p1.y)
        }
        return sum
    }

    fun calculateStars(inkRemainingRatio: Float): Int {
        return when {
            inkRemainingRatio >= 0.60f -> 3
            inkRemainingRatio >= 0.25f -> 2
            else -> 1
        }
    }

    fun step(
        particles: List<WaterParticle>,
        tap: WaterTap,
        glass: Glass,
        obstacles: List<ObstacleLine>,
        drawnPoints: List<Offset>,
        dispensedCount: Int,
        isSimulating: Boolean
    ): Triple<List<WaterParticle>, Int, Int> {
        val updatedList = particles.toMutableList()
        var newDispensed = dispensedCount

        // Spawn new drops if simulating and drops left in tap
        if (isSimulating && newDispensed < tap.totalWater) {
            // Spawn 1 drop every few frames
            val rng = Random(System.nanoTime())
            val jitter = (rng.nextFloat() - 0.5f) * 0.02f
            updatedList.add(
                WaterParticle(
                    x = tap.x + jitter,
                    y = tap.y + 0.03f,
                    vx = (rng.nextFloat() - 0.5f) * 0.002f,
                    vy = 0.002f
                )
            )
            newDispensed++
        }

        val glassLeft = glass.x
        val glassRight = glass.x + glass.width
        val glassBottom = glass.y + glass.height
        val glassTop = glass.y

        var inGlassCount = 0

        for (p in updatedList) {
            if (p.isLost) continue

            // If already resting inside glass
            if (p.inGlass) {
                inGlassCount++
                continue
            }

            // Apply gravity
            p.vy += GRAVITY
            p.vx *= DAMPING
            p.vy *= DAMPING

            p.x += p.vx
            p.y += p.vy

            // Check screen bounds
            if (p.y > 1.05f || p.x < -0.1f || p.x > 1.1f) {
                p.isLost = true
                continue
            }

            // Collide with drawn stroke line segments
            if (drawnPoints.size >= 2) {
                for (i in 0 until drawnPoints.size - 1) {
                    val p1 = drawnPoints[i]
                    val p2 = drawnPoints[i + 1]
                    collidePointWithSegment(p, p1.x, p1.y, p2.x, p2.y, DROP_RADIUS + 0.008f)
                }
            }

            // Collide with obstacles
            for (obs in obstacles) {
                collidePointWithSegment(p, obs.x1, obs.y1, obs.x2, obs.y2, DROP_RADIUS + obs.strokeWidth / 2f)
            }

            // Check collision with glass
            // Glass interior
            if (p.x in (glassLeft + 0.015f)..(glassRight - 0.015f) && p.y in glassTop..glassBottom) {
                if (p.y >= glassBottom - DROP_RADIUS - 0.015f) {
                    p.y = glassBottom - DROP_RADIUS - 0.015f
                    p.vx = 0f
                    p.vy = 0f
                    p.inGlass = true
                    inGlassCount++
                }
            } else {
                // Glass outer walls collision
                // Left wall: (glassLeft, glassTop) to (glassLeft, glassBottom)
                collidePointWithSegment(p, glassLeft, glassTop, glassLeft, glassBottom, DROP_RADIUS + 0.008f)
                // Right wall: (glassRight, glassTop) to (glassRight, glassBottom)
                collidePointWithSegment(p, glassRight, glassTop, glassRight, glassBottom, DROP_RADIUS + 0.008f)
                // Bottom wall: (glassLeft, glassBottom) to (glassRight, glassBottom)
                collidePointWithSegment(p, glassLeft, glassBottom, glassRight, glassBottom, DROP_RADIUS + 0.008f)
            }
        }

        return Triple(updatedList, newDispensed, inGlassCount)
    }

    private fun collidePointWithSegment(
        p: WaterParticle,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        threshold: Float
    ) {
        val dx = x2 - x1
        val dy = y2 - y1
        val lenSq = dx * dx + dy * dy
        if (lenSq < 1e-6f) return

        val t = (((p.x - x1) * dx + (p.y - y1) * dy) / lenSq).coerceIn(0f, 1f)
        val projX = x1 + t * dx
        val projY = y1 + t * dy

        val distX = p.x - projX
        val distY = p.y - projY
        val dist = hypot(distX, distY)

        if (dist < threshold && dist > 1e-5f) {
            val nx = distX / dist
            val ny = distY / dist

            // Push out of line
            p.x = projX + nx * threshold
            p.y = projY + ny * threshold

            // Reflect velocity along normal
            val dot = p.vx * nx + p.vy * ny
            if (dot < 0) {
                p.vx = (p.vx - 2 * dot * nx) * BOUNCE
                p.vy = (p.vy - 2 * dot * ny) * BOUNCE
            }
        }
    }
}
