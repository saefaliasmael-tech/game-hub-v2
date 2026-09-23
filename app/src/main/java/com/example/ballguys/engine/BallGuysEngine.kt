package com.example.ballguys.engine

import com.example.ballguys.model.BallGuy
import com.example.ballguys.model.BallGuysState
import com.example.ballguys.model.BallTiers
import kotlin.math.sqrt

class BallGuysEngine {
    companion object {
        const val GRAVITY = 0.55f
        const val DAMPING = 0.65f
        const val RESTITUTION = 0.4f
        const val DANGER_LINE_Y = 120f
    }

    private var nextBallId = 1L

    fun createBall(tierLevel: Int, x: Float, y: Float): BallGuy {
        val tier = BallTiers.tiers[tierLevel - 1]
        return BallGuy(
            id = nextBallId++,
            tierLevel = tierLevel,
            x = x,
            y = y,
            radius = tier.radius
        )
    }

    fun updatePhysics(
        state: BallGuysState,
        boxWidth: Float,
        boxHeight: Float,
        onMerge: (points: Int) -> Unit,
        onGameOver: () -> Unit
    ): BallGuysState {
        if (state.isGameOver) return state

        val balls = state.balls.toMutableList()
        var scoreAdd = 0

        // 1. Apply gravity & move
        for (b in balls) {
            b.vy += GRAVITY
            b.x += b.vx
            b.y += b.vy

            // Wall collisions (Left, Right, Bottom)
            if (b.x - b.radius < 0f) {
                b.x = b.radius
                b.vx = -b.vx * DAMPING
            } else if (b.x + b.radius > boxWidth) {
                b.x = boxWidth - b.radius
                b.vx = -b.vx * DAMPING
            }

            if (b.y + b.radius > boxHeight) {
                b.y = boxHeight - b.radius
                b.vy = -b.vy * RESTITUTION
                b.vx *= 0.9f
            }
        }

        // 2. Ball-to-ball collisions & Merges
        val toAdd = mutableListOf<BallGuy>()

        for (i in 0 until balls.size) {
            val b1 = balls[i]
            if (b1.isMerged) continue

            for (j in (i + 1) until balls.size) {
                val b2 = balls[j]
                if (b2.isMerged) continue

                val dx = b2.x - b1.x
                val dy = b2.y - b1.y
                val dist = sqrt(dx * dx + dy * dy)
                val minDist = b1.radius + b2.radius

                if (dist < minDist && dist > 0.001f) {
                    // Check if same tier for merge
                    if (b1.tierLevel == b2.tierLevel && b1.tierLevel < BallTiers.tiers.size) {
                        b1.isMerged = true
                        b2.isMerged = true
                        val newTierLevel = b1.tierLevel + 1
                        val midX = (b1.x + b2.x) / 2f
                        val midY = (b1.y + b2.y) / 2f
                        val newBall = createBall(newTierLevel, midX, midY)
                        toAdd.add(newBall)
                        val pts = BallTiers.tiers[newTierLevel - 1].points
                        scoreAdd += pts
                        onMerge(pts)
                        break
                    } else {
                        // Elastic push apart
                        val overlap = (minDist - dist) * 0.5f
                        val nx = dx / dist
                        val ny = dy / dist

                        b1.x -= nx * overlap
                        b1.y -= ny * overlap
                        b2.x += nx * overlap
                        b2.y += ny * overlap

                        val kx = b1.vx - b2.vx
                        val ky = b1.vy - b2.vy
                        val p = 2f * (nx * kx + ny * ky) / 2f

                        b1.vx -= p * nx * 0.6f
                        b1.vy -= p * ny * 0.6f
                        b2.vx += p * nx * 0.6f
                        b2.vy += p * ny * 0.6f
                    }
                }
            }
        }

        balls.removeAll { it.isMerged }
        balls.addAll(toAdd)

        // 3. Overflow check (balls resting above DANGER_LINE_Y)
        val overflow = balls.any { it.y - it.radius < DANGER_LINE_Y && kotlin.math.abs(it.vy) < 0.2f }
        if (overflow && balls.size > 5) {
            onGameOver()
            return state.copy(balls = balls, isGameOver = true)
        }

        return state.copy(
            balls = balls,
            score = state.score + scoreAdd
        )
    }
}
