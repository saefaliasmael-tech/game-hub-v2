package com.example.pullthepin.core.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.pullthepin.core.model.*
import kotlin.math.*

object PullThePinEngine {

    const val BALL_RADIUS = 0.022f
    const val GRAVITY = 0.00055f
    const val BOUNCE = 0.4f

    private val VIBRANT_COLORS = listOf(
        Color(0xFFFF5964),
        Color(0xFF35A7FF),
        Color(0xFFFFE74C),
        Color(0xFF10B981),
        Color(0xFF8B5CF6)
    )

    fun stepPhysics(
        balls: List<Ball>,
        pins: List<Pin>,
        bombs: List<Bomb>,
        walls: List<Wall>,
        bucket: Bucket
    ): Quadruple<List<Ball>, List<Bomb>, Int, Boolean> {
        var newlyCollected = 0
        var bombExploded = false

        val updatedBombs = bombs.map { it.copy() }.toMutableList()
        val updatedBalls = balls.map { it.copy() }.toMutableList()

        // 1. Process active bombs collision with balls
        for (bomb in updatedBombs) {
            if (!bomb.isExploded) {
                val hit = updatedBalls.any { it.isAlive && !it.inBucket && distance(it.x, it.y, bomb.x, bomb.y) < (bomb.radius + BALL_RADIUS) }
                if (hit) {
                    bomb.isExploded = true
                    bombExploded = true
                    // Destroy balls in blast radius
                    for (b in updatedBalls) {
                        if (b.isAlive && distance(b.x, b.y, bomb.x, bomb.y) < 0.14f) {
                            b.isAlive = false
                        }
                    }
                }
            }
        }

        // 2. Color diffusion between balls
        for (i in updatedBalls.indices) {
            val b1 = updatedBalls[i]
            if (!b1.isAlive || b1.inBucket || !b1.isColored) continue

            for (j in updatedBalls.indices) {
                val b2 = updatedBalls[j]
                if (!b2.isAlive || b2.inBucket || b2.isColored) continue

                if (distance(b1.x, b1.y, b2.x, b2.y) < BALL_RADIUS * 2.4f) {
                    b2.isColored = true
                    b2.color = b1.color
                }
            }
        }

        // 3. Move balls and handle collisions
        val activePins = pins.filter { !it.isPulled }

        for (ball in updatedBalls) {
            if (!ball.isAlive || ball.inBucket) continue

            // Gravity
            ball.vy += GRAVITY
            ball.x += ball.vx
            ball.y += ball.vy

            // Air resistance
            ball.vx *= 0.98f
            ball.vy *= 0.99f

            // Screen boundary clamping
            if (ball.x < 0.05f) {
                ball.x = 0.05f
                ball.vx = -ball.vx * BOUNCE
            } else if (ball.x > 0.95f) {
                ball.x = 0.95f
                ball.vx = -ball.vx * BOUNCE
            }

            // Wall collisions
            for (wall in walls) {
                resolveSegmentCollision(ball, wall.x1, wall.y1, wall.x2, wall.y2)
            }

            // Pin collisions
            for (pin in activePins) {
                val p1 = Offset(pin.x1, pin.y1)
                val p2 = Offset(pin.x2, pin.y2)
                // If partially pulled, shorten segment
                val activeEnd = Offset(
                    p1.x + (p2.x - p1.x) * (1f - pin.pullProgress),
                    p1.y + (p2.y - p1.y) * (1f - pin.pullProgress)
                )
                resolveSegmentCollision(ball, p1.x, p1.y, activeEnd.x, activeEnd.y)
            }

            // Check bucket entry
            if (ball.x >= bucket.x && ball.x <= (bucket.x + bucket.width) && ball.y >= bucket.y) {
                ball.inBucket = true
                ball.vx = 0f
                ball.vy = 0f
                if (ball.isColored) {
                    newlyCollected++
                } else {
                    // Gray ball in bucket counts as loss / destroyed
                    ball.isAlive = false
                }
            }

            // Fell below screen
            if (ball.y > 0.98f) {
                ball.isAlive = false
            }
        }

        return Quadruple(updatedBalls, updatedBombs, newlyCollected, bombExploded)
    }

    private fun resolveSegmentCollision(ball: Ball, x1: Float, y1: Float, x2: Float, y2: Float) {
        val dx = x2 - x1
        val dy = y2 - y1
        val lengthSq = dx * dx + dy * dy
        if (lengthSq == 0f) return

        val t = max(0f, min(1f, ((ball.x - x1) * dx + (ball.y - y1) * dy) / lengthSq))
        val closestX = x1 + t * dx
        val closestY = y1 + t * dy

        val dist = distance(ball.x, ball.y, closestX, closestY)
        if (dist < BALL_RADIUS && dist > 0.0001f) {
            val normalX = (ball.x - closestX) / dist
            val normalY = (ball.y - closestY) / dist

            // Push out
            ball.x = closestX + normalX * BALL_RADIUS
            ball.y = closestY + normalY * BALL_RADIUS

            // Reflect velocity
            val dot = ball.vx * normalX + ball.vy * normalY
            if (dot < 0) {
                ball.vx -= (1f + BOUNCE) * dot * normalX
                ball.vy -= (1f + BOUNCE) * dot * normalY
            }
        }
    }

    fun pullPin(pins: List<Pin>, pinId: Int): List<Pin> {
        return pins.map { pin ->
            if (pin.id == pinId) {
                pin.copy(isPulled = true, pullProgress = 1f)
            } else {
                pin
            }
        }
    }

    fun isPointNearPinHandle(pin: Pin, px: Float, py: Float): Boolean {
        if (pin.isPulled) return false
        val d1 = distance(px, py, pin.x1, pin.y1)
        val d2 = distance(px, py, pin.x2, pin.y2)
        return min(d1, d2) < 0.08f
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
