package com.example.colorswitch.core.engine

import com.example.colorswitch.core.model.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object ColorSwitchEngine {

    const val GRAVITY = 950f
    const val JUMP_IMPULSE = -380f
    const val MAX_FALL_SPEED = 550f
    const val OBSTACLE_SPACING = 280f

    fun applyJump(ball: ColorSwitchBall) {
        ball.velocityY = JUMP_IMPULSE
    }

    fun updatePhysics(ball: ColorSwitchBall, dt: Float) {
        ball.velocityY = (ball.velocityY + GRAVITY * dt).coerceAtMost(MAX_FALL_SPEED)
        ball.y += ball.velocityY * dt
    }

    fun updateObstacles(obstacles: List<ObstacleState>, dt: Float, speedMultiplier: Float = 1.0f) {
        obstacles.forEach { obs ->
            obs.currentAngle = (obs.currentAngle + obs.rotationSpeed * obs.rotationDirection * speedMultiplier * dt) % 360f
            if (obs.currentAngle < 0f) obs.currentAngle += 360f
        }
    }

    /**
     * Checks if the ball collides with an obstacle.
     * Returns true if ball hits a NON-MATCHING color (FATAL COLLISION).
     */
    fun checkCollision(ball: ColorSwitchBall, obstacle: ObstacleState): Boolean {
        val distY = abs(ball.y - obstacle.centerY)
        val r = obstacle.radius
        val thickness = 18f

        when (obstacle.type) {
            ObstacleType.ROTATING_CIRCLE, ObstacleType.CONCENTRIC_RINGS -> {
                // Circle touches ball at top crossing (y = centerY - r) or bottom crossing (y = centerY + r)
                val topCrossingY = obstacle.centerY - r
                val bottomCrossingY = obstacle.centerY + r

                val isTouchingBottom = abs(ball.y - bottomCrossingY) <= (thickness / 2 + ball.radius)
                val isTouchingTop = abs(ball.y - topCrossingY) <= (thickness / 2 + ball.radius)

                if (isTouchingBottom) {
                    // Contact point is at bottom (angle = 90 deg)
                    val effectiveAngle = ((90f - obstacle.currentAngle) % 360f + 360f) % 360f
                    val arcColor = getCircleArcColor(effectiveAngle)
                    if (arcColor != ball.color) return true
                }

                if (isTouchingTop) {
                    // Contact point is at top (angle = 270 deg)
                    val effectiveAngle = ((270f - obstacle.currentAngle) % 360f + 360f) % 360f
                    val arcColor = getCircleArcColor(effectiveAngle)
                    if (arcColor != ball.color) return true
                }
            }
            ObstacleType.ROTATING_CROSS -> {
                // 4 arms spinning around center. Arms are at angle 0, 90, 180, 270 offset by currentAngle
                for (i in 0 until 4) {
                    val armAngle = ((obstacle.currentAngle + i * 90f) % 360f + 360f) % 360f
                    // If arm is nearly vertical (crossing x = 0 at angle ~90 or ~270)
                    val armRad = Math.toRadians(armAngle.toDouble())
                    val armTipX = r * cos(armRad).toFloat()
                    val armTipY = obstacle.centerY + r * sin(armRad).toFloat()

                    // Check if ball is close to arm line segment
                    if (abs(armTipX) < ball.radius + 8f) {
                        val armMinY = minOf(obstacle.centerY, armTipY) - ball.radius
                        val armMaxY = maxOf(obstacle.centerY, armTipY) + ball.radius
                        if (ball.y in armMinY..armMaxY) {
                            val armColor = SwitchColor.fromIndex(i)
                            if (armColor != ball.color) return true
                        }
                    }
                }
            }
            ObstacleType.HORIZONTAL_BARS -> {
                // Bar at centerY with thickness 20f
                if (abs(ball.y - obstacle.centerY) <= (thickness / 2 + ball.radius)) {
                    // Offset moves horizontally
                    val offset = (obstacle.currentAngle * 2f) % 240f
                    val segmentIndex = ((offset / 60f).toInt()) % 4
                    val barColor = SwitchColor.fromIndex(segmentIndex)
                    if (barColor != ball.color) return true
                }
            }
            ObstacleType.SPINNING_TRIANGLE, ObstacleType.SPINNING_SQUARE -> {
                val topCrossingY = obstacle.centerY - r
                val bottomCrossingY = obstacle.centerY + r
                val isTouching = abs(ball.y - topCrossingY) <= (thickness / 2 + ball.radius) ||
                        abs(ball.y - bottomCrossingY) <= (thickness / 2 + ball.radius)
                if (isTouching) {
                    val effectiveAngle = ((270f - obstacle.currentAngle) % 360f + 360f) % 360f
                    val arcColor = getCircleArcColor(effectiveAngle)
                    if (arcColor != ball.color) return true
                }
            }
        }
        return false
    }

    private fun getCircleArcColor(angle: Float): SwitchColor {
        return when (angle) {
            in 0f..90f -> SwitchColor.CYAN
            in 90f..180f -> SwitchColor.YELLOW
            in 180f..270f -> SwitchColor.MAGENTA
            else -> SwitchColor.PURPLE
        }
    }

    fun checkStarPickup(ball: ColorSwitchBall, obstacle: ObstacleState): Boolean {
        if (!obstacle.hasStar || obstacle.isStarCollected) return false
        val distY = abs(ball.y - obstacle.centerY)
        if (distY <= ball.radius + 16f) {
            obstacle.isStarCollected = true
            return true
        }
        return false
    }

    fun checkColorOrbPickup(ball: ColorSwitchBall, obstacle: ObstacleState): Boolean {
        if (!obstacle.hasColorOrb || obstacle.isColorOrbCollected) return false
        val orbY = obstacle.centerY - obstacle.radius - 40f
        val distY = abs(ball.y - orbY)
        if (distY <= ball.radius + 14f) {
            obstacle.isColorOrbCollected = true
            ball.color = obstacle.nextColor
            return true
        }
        return false
    }

    fun createParticles(x: Float, y: Float, color: androidx.compose.ui.graphics.Color, count: Int = 16): List<Particle> {
        return (0 until count).map {
            val angle = Math.random() * 2 * Math.PI
            val speed = 80f + (Math.random().toFloat() * 160f)
            Particle(
                x = x,
                y = y,
                vx = (cos(angle) * speed).toFloat(),
                vy = (sin(angle) * speed).toFloat(),
                color = color,
                alpha = 1f,
                size = 5f + (Math.random().toFloat() * 6f)
            )
        }
    }

    fun updateParticles(particles: List<Particle>, dt: Float): List<Particle> {
        return particles.mapNotNull { p ->
            val newAlpha = p.alpha - (dt * 2.2f)
            if (newAlpha <= 0f) null
            else p.copy(
                x = p.x + p.vx * dt,
                y = p.y + p.vy * dt,
                alpha = newAlpha
            )
        }
    }
}
