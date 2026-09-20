package com.example.minitd.core.engine

import com.example.minitd.core.model.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object MiniTDEngine {

    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Advances enemy towards its next waypoint. Returns true if reached the end of path.
     */
    fun moveEnemy(
        enemy: Enemy,
        waypoints: List<Waypoint>,
        deltaTimeSec: Float
    ): Boolean {
        if (enemy.waypointIndex >= waypoints.size) {
            enemy.reachedEnd = true
            return true
        }

        val targetWp = waypoints[enemy.waypointIndex]
        val dist = distance(enemy.xPercent, enemy.yPercent, targetWp.xPercent, targetWp.yPercent)

        val actualSpeed = if (enemy.slowTimerSec > 0f) {
            enemy.speed * 0.6f // 40% slow
        } else {
            enemy.speed
        }

        val moveDist = actualSpeed * deltaTimeSec

        if (dist <= moveDist) {
            enemy.xPercent = targetWp.xPercent
            enemy.yPercent = targetWp.yPercent
            enemy.waypointIndex++
            if (enemy.waypointIndex >= waypoints.size) {
                enemy.reachedEnd = true
                return true
            }
        } else {
            val angle = atan2(targetWp.yPercent - enemy.yPercent, targetWp.xPercent - enemy.xPercent)
            enemy.xPercent += cos(angle) * moveDist
            enemy.yPercent += sin(angle) * moveDist
        }

        if (enemy.slowTimerSec > 0f) {
            enemy.slowTimerSec = (enemy.slowTimerSec - deltaTimeSec).coerceAtLeast(0f)
        }

        return false
    }

    /**
     * Finds the best target for a tower (the enemy within range with the highest path progress).
     */
    fun findTarget(
        tower: Tower,
        enemies: List<Enemy>
    ): Enemy? {
        val aliveEnemiesInRange = enemies.filter { enemy ->
            !enemy.isDead && !enemy.reachedEnd &&
                    distance(tower.xPercent, tower.yPercent, enemy.xPercent, enemy.yPercent) <= tower.range
        }

        return aliveEnemiesInRange.maxByOrNull { enemy ->
            enemy.waypointIndex * 1000f + (1f - distance(enemy.xPercent, enemy.yPercent, 0.5f, 0.5f))
        }
    }
}
