package com.example.sandloop.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.sandloop.model.SandLoopLevel
import com.example.sandloop.model.SandParticle
import kotlin.random.Random

object SandLoopLevels {
    val levels = listOf(
        // Level 1: Straight descent with side dirt
        SandLoopLevel(
            levelNumber = 1,
            initialDirtRects = listOf(
                Rect(0.1f, 0.25f, 0.9f, 0.7f)
            ),
            stoneBarriers = listOf(
                Rect(0.0f, 0.2f, 0.35f, 0.25f),
                Rect(0.65f, 0.2f, 1.0f, 0.25f)
            ),
            containerRect = Rect(0.35f, 0.8f, 0.65f, 0.95f),
            sandSpawnX = 0.5f,
            sandSpawnY = 0.15f,
            targetCount = 50
        ),
        // Level 2: Funnel around center rock
        SandLoopLevel(
            levelNumber = 2,
            initialDirtRects = listOf(
                Rect(0.05f, 0.2f, 0.95f, 0.75f)
            ),
            stoneBarriers = listOf(
                Rect(0.4f, 0.4f, 0.6f, 0.55f),
                Rect(0.0f, 0.65f, 0.25f, 0.7f),
                Rect(0.75f, 0.65f, 1.0f, 0.7f)
            ),
            containerRect = Rect(0.35f, 0.82f, 0.65f, 0.96f),
            sandSpawnX = 0.5f,
            sandSpawnY = 0.12f,
            targetCount = 60
        ),
        // Level 3: Dual channel zig-zag
        SandLoopLevel(
            levelNumber = 3,
            initialDirtRects = listOf(
                Rect(0.05f, 0.18f, 0.95f, 0.78f)
            ),
            stoneBarriers = listOf(
                Rect(0.0f, 0.35f, 0.65f, 0.4f),
                Rect(0.35f, 0.55f, 1.0f, 0.6f)
            ),
            containerRect = Rect(0.15f, 0.82f, 0.45f, 0.96f),
            sandSpawnX = 0.5f,
            sandSpawnY = 0.1f,
            targetCount = 60
        )
    )
}

class SandLoopEngine {
    companion object {
        const val MAX_PARTICLES = 120
        const val CARVE_RADIUS = 0.08f // relative to screen
    }

    val particles = mutableListOf<SandParticle>()
    val carvedHoles = mutableListOf<Offset>()

    fun reset(level: SandLoopLevel) {
        particles.clear()
        carvedHoles.clear()
        // Pre-carve initial spawn tunnel
        carvedHoles.add(Offset(level.sandSpawnX, level.sandSpawnY + 0.05f))
    }

    fun carve(normalizedPos: Offset) {
        carvedHoles.add(normalizedPos)
    }

    fun isPointInDirt(p: Offset, level: SandLoopLevel): Boolean {
        // If inside any dirt rect
        val inAnyDirt = level.initialDirtRects.any { it.contains(p) }
        if (!inAnyDirt) return false

        // Check if inside any carved hole
        val carved = carvedHoles.any { hole ->
            val dx = p.x - hole.x
            val dy = p.y - hole.y
            (dx * dx + dy * dy) < (CARVE_RADIUS * CARVE_RADIUS)
        }
        return !carved
    }

    fun update(
        level: SandLoopLevel,
        spawnAllowed: Boolean,
        onParticleCollected: () -> Unit
    ): Int {
        val random = Random.Default

        // Spawn new particles
        if (spawnAllowed && particles.size < MAX_PARTICLES) {
            val spawnPos = Offset(
                level.sandSpawnX + (random.nextFloat() - 0.5f) * 0.06f,
                level.sandSpawnY
            )
            particles.add(SandParticle(x = spawnPos.x, y = spawnPos.y, vy = 0.005f))
        }

        var newlyCollected = 0

        for (p in particles) {
            if (p.isCollected || p.isDead) continue

            // Gravity
            p.vy = (p.vy + 0.0008f).coerceAtMost(0.018f)

            // Random slight horizontal jitter for realistic fluid sand
            p.vx = (p.vx + (random.nextFloat() - 0.5f) * 0.001f).coerceIn(-0.008f, 0.008f)

            val nextX = p.x + p.vx
            val nextY = p.y + p.vy
            val nextPos = Offset(nextX, nextY)

            // Check container collection
            if (level.containerRect.contains(nextPos)) {
                p.isCollected = true
                newlyCollected++
                onParticleCollected()
                continue
            }

            // Check stone barriers
            val hitStone = level.stoneBarriers.any { it.contains(nextPos) }
            if (hitStone) {
                p.vy = 0f
                p.vx = (random.nextFloat() - 0.5f) * 0.008f
                p.x = (p.x + p.vx).coerceIn(0.02f, 0.98f)
                continue
            }

            // Check dirt collision
            if (isPointInDirt(nextPos, level)) {
                // Cannot pass through uncarved dirt, slide or rest
                p.vy = 0f
                p.vx = 0f
            } else {
                p.x = nextX.coerceIn(0.02f, 0.98f)
                p.y = nextY
            }

            // Fell out of bottom screen without container
            if (p.y > 1.0f) {
                p.isDead = true
            }
        }

        return newlyCollected
    }
}
