package com.example.protectsheep.core.engine

import androidx.compose.ui.geometry.Offset
import com.example.protectsheep.core.model.Bee
import com.example.protectsheep.core.model.Hazard
import com.example.protectsheep.core.model.Sheep
import kotlin.math.hypot
import kotlin.random.Random

object ProtectSheepEngine {

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
            inkRemainingRatio >= 0.55f -> 3
            inkRemainingRatio >= 0.20f -> 2
            else -> 1
        }
    }

    fun stepBees(
        bees: List<Bee>,
        sheepList: List<Sheep>,
        drawnPoints: List<Offset>,
        hazards: List<Hazard>,
        dt: Float
    ): Pair<List<Bee>, Boolean> {
        if (sheepList.isEmpty()) return Pair(bees, false)

        val updatedBees = mutableListOf<Bee>()
        var anyStung = false
        val rng = Random(System.nanoTime())

        val targetSheep = sheepList.first()

        for (bee in bees) {
            // Vector towards sheep
            val dx = targetSheep.x - bee.x
            val dy = targetSheep.y - bee.y
            val distToSheep = hypot(dx, dy)

            // Check if bee stings sheep
            if (distToSheep < targetSheep.radius + bee.radius) {
                anyStung = true
            }

            // Seek acceleration with slight swarm noise
            val speed = 0.24f
            val steerForce = 0.5f
            val normX = if (distToSheep > 1e-4f) dx / distToSheep else 0f
            val normY = if (distToSheep > 1e-4f) dy / distToSheep else 0f

            val jitterX = (rng.nextFloat() - 0.5f) * 0.15f
            val jitterY = (rng.nextFloat() - 0.5f) * 0.15f

            bee.vx += (normX * speed - bee.vx + jitterX) * steerForce * dt * 60f
            bee.vy += (normY * speed - bee.vy + jitterY) * steerForce * dt * 60f

            var nextX = bee.x + bee.vx * dt
            var nextY = bee.y + bee.vy * dt

            // Collide with drawn stroke barriers
            if (drawnPoints.size >= 2) {
                for (i in 0 until drawnPoints.size - 1) {
                    val p1 = drawnPoints[i]
                    val p2 = drawnPoints[i + 1]
                    val lineDist = pointToSegmentDist(nextX, nextY, p1.x, p1.y, p2.x, p2.y)
                    val threshold = bee.radius + 0.02f
                    if (lineDist.first < threshold) {
                        // Push bee outside barrier
                        nextX += lineDist.second.first * (threshold - lineDist.first) * 1.5f
                        nextY += lineDist.second.second * (threshold - lineDist.first) * 1.5f
                        bee.vx *= -0.5f
                        bee.vy *= -0.5f
                    }
                }
            }

            // Collide with hazards
            for (h in hazards) {
                val lineDist = pointToSegmentDist(nextX, nextY, h.x1, h.y1, h.x2, h.y2)
                val threshold = bee.radius + 0.025f
                if (lineDist.first < threshold) {
                    nextX += lineDist.second.first * (threshold - lineDist.first) * 1.5f
                    nextY += lineDist.second.second * (threshold - lineDist.first) * 1.5f
                    bee.vx *= -0.5f
                    bee.vy *= -0.5f
                }
            }

            bee.x = nextX.coerceIn(0.02f, 0.98f)
            bee.y = nextY.coerceIn(0.02f, 0.98f)

            updatedBees.add(bee)
        }

        return Pair(updatedBees, anyStung)
    }

    private fun pointToSegmentDist(
        px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float
    ): Pair<Float, Pair<Float, Float>> {
        val dx = x2 - x1
        val dy = y2 - y1
        val lenSq = dx * dx + dy * dy
        if (lenSq < 1e-6f) {
            val d = hypot(px - x1, py - y1)
            val nx = if (d > 1e-5f) (px - x1) / d else 0f
            val ny = if (d > 1e-5f) (py - y1) / d else 1f
            return Pair(d, Pair(nx, ny))
        }

        val t = (((px - x1) * dx + (py - y1) * dy) / lenSq).coerceIn(0f, 1f)
        val projX = x1 + t * dx
        val projY = y1 + t * dy

        val distX = px - projX
        val distY = py - projY
        val d = hypot(distX, distY)
        val nx = if (d > 1e-5f) distX / d else 0f
        val ny = if (d > 1e-5f) distY / d else 1f

        return Pair(d, Pair(nx, ny))
    }
}
