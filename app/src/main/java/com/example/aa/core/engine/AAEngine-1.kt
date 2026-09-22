package com.example.aa.core.engine

import com.example.aa.core.model.AAGameState
import com.example.aa.core.model.AALevelConfig
import com.example.aa.core.model.PinnedBall
import com.example.aa.core.model.ShootingBall
import kotlin.math.abs
import kotlin.math.min

object AAEngine {

    const val MIN_ANGULAR_SEPARATION_DEGREES = 11.5f
    const val PIN_INSERTION_ANGLE = 90f // Bottom entry point into rotating wheel

    fun normalizeAngle(angle: Float): Float {
        var normalized = angle % 360f
        if (normalized < 0f) normalized += 360f
        return normalized
    }

    fun angularDistance(a: Float, b: Float): Float {
        val diff = abs(normalizeAngle(a) - normalizeAngle(b))
        return min(diff, 360f - diff)
    }

    fun updateRotation(
        currentAngle: Float,
        speed: Float,
        deltaTimeSec: Float
    ): Float {
        return normalizeAngle(currentAngle + speed * deltaTimeSec)
    }

    /**
     * Check if a ball arriving at PIN_INSERTION_ANGLE (90 degrees) collides with any pinned ball.
     * Returns true and the colliding angle if collision occurs.
     */
    fun checkCollision(
        pinnedBalls: List<PinnedBall>,
        currentWheelAngle: Float
    ): Pair<Boolean, Float?> {
        for (pin in pinnedBalls) {
            val pinScreenAngle = normalizeAngle(pin.angleDegrees + currentWheelAngle)
            val dist = angularDistance(pinScreenAngle, PIN_INSERTION_ANGLE)
            if (dist < MIN_ANGULAR_SEPARATION_DEGREES) {
                return true to pinScreenAngle
            }
        }
        return false to null
    }

    /**
     * Calculates the local angle on the rotating wheel for a ball pinned at PIN_INSERTION_ANGLE.
     */
    fun calculatePinAngleOnWheel(currentWheelAngle: Float): Float {
        return normalizeAngle(PIN_INSERTION_ANGLE - currentWheelAngle)
    }
}
