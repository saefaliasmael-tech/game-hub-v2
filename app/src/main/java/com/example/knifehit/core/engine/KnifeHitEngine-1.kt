package com.example.knifehit.core.engine

import com.example.knifehit.core.model.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object KnifeHitEngine {

    const val MIN_KNIFE_SEPARATION_DEG = 15f
    const val APPLE_HIT_TOLERANCE_DEG = 14f
    const val TARGET_RADIUS = 90f
    const val KNIFE_FLY_SPEED = 2400f // pixels per second

    fun angularDistance(deg1: Float, deg2: Float): Float {
        val diff = abs(deg1 - deg2) % 360f
        return min(diff, 360f - diff)
    }

    /**
     * Calculates the local attachment angle on the target when a knife hits from the bottom.
     */
    fun calculateImpactAngle(targetAngle: Float): Float {
        return ((90f - targetAngle) % 360f + 360f) % 360f
    }

    /**
     * Checks if a knife at impactAngle collides with any already attached knife.
     */
    fun checkKnifeCollision(impactAngle: Float, attachedKnives: List<AttachedKnife>): Boolean {
        for (k in attachedKnives) {
            if (angularDistance(impactAngle, k.angle) < MIN_KNIFE_SEPARATION_DEG) {
                return true
            }
        }
        return false
    }

    /**
     * Checks if an apple was sliced by the hitting knife.
     */
    fun checkAppleHit(impactAngle: Float, apples: List<AttachedApple>): AttachedApple? {
        for (apple in apples) {
            if (!apple.isSliced && angularDistance(impactAngle, apple.angle) < APPLE_HIT_TOLERANCE_DEG) {
                return apple
            }
        }
        return null
    }

    /**
     * Shatters target into flying fragments on stage completion.
     */
    fun generateTargetShards(centerX: Float, centerY: Float, targetType: TargetType): List<TargetShard> {
        val shards = mutableListOf<TargetShard>()
        val shardCount = 10
        val colors = listOf(targetType.primaryColor, targetType.secondaryColor, targetType.primaryColor.copy(alpha = 0.8f))

        for (i in 0 until shardCount) {
            val angleRad = (i * (2 * Math.PI / shardCount)).toFloat()
            val speed = 250f + (Math.random().toFloat() * 200f)
            shards.add(
                TargetShard(
                    x = centerX + (TARGET_RADIUS * 0.5f * cos(angleRad)),
                    y = centerY + (TARGET_RADIUS * 0.5f * sin(angleRad)),
                    vx = (cos(angleRad) * speed),
                    vy = (sin(angleRad) * speed) - 100f,
                    angle = (Math.random().toFloat() * 360f),
                    vAngle = (Math.random().toFloat() * 360f - 180f),
                    color = colors[i % colors.size]
                )
            )
        }
        return shards
    }

    fun updateShards(shards: List<TargetShard>, dt: Float): List<TargetShard> {
        return shards.mapNotNull { s ->
            val newAlpha = s.alpha - (dt * 1.8f)
            if (newAlpha <= 0f) null
            else s.copy(
                x = s.x + s.vx * dt,
                y = s.y + (s.vy + 600f * dt) * dt, // with gravity
                angle = s.angle + s.vAngle * dt,
                alpha = newAlpha
            )
        }
    }
}
