package com.example.mrbullet.core.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.mrbullet.core.model.ActiveBullet
import com.example.mrbullet.core.model.EnemyTarget
import com.example.mrbullet.core.model.MrBulletWall
import com.example.mrbullet.core.model.TntBarrel
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

object MrBulletPhysicsEngine {
    private const val BULLET_SPEED = 18f
    private const val GRAVITY = 0.04f

    fun calculateTrajectory(
        startX: Float,
        startY: Float,
        angleRad: Float,
        walls: List<MrBulletWall>
    ): List<Offset> {
        val points = mutableListOf<Offset>()
        points.add(Offset(startX, startY))

        var curX = startX
        var curY = startY
        var vx = cos(angleRad) * 12f
        var vy = sin(angleRad) * 12f
        var bounces = 2

        for (step in 0 until 120) {
            curX += vx
            curY += vy

            var hit = false
            for (wall in walls) {
                val b = wall.bounds
                if (curX >= b.left && curX <= b.right && curY >= b.top && curY <= b.bottom) {
                    points.add(Offset(curX, curY))
                    val overlapLeft = curX - b.left
                    val overlapRight = b.right - curX
                    val overlapTop = curY - b.top
                    val overlapBottom = b.bottom - curY
                    val minOverlap = min(min(overlapLeft, overlapRight), min(overlapTop, overlapBottom))

                    if (minOverlap == overlapLeft || minOverlap == overlapRight) {
                        vx = -vx
                    } else {
                        vy = -vy
                    }

                    bounces--
                    hit = true
                    break
                }
            }

            if (hit && bounces < 0) break
            if (curX < 0f || curX > 400f || curY < 0f || curY > 700f) {
                points.add(Offset(curX, curY))
                break
            }

            if (step % 8 == 0) {
                points.add(Offset(curX, curY))
            }
        }

        return points
    }

    fun updateBullet(
        bullet: ActiveBullet,
        walls: List<MrBulletWall>,
        enemies: List<EnemyTarget>,
        barrels: List<TntBarrel>,
        onEnemyKilled: (EnemyTarget) -> Unit,
        onTntExploded: (TntBarrel) -> Unit
    ) {
        if (!bullet.isAlive) return

        bullet.vy += GRAVITY
        val steps = 3
        val dx = bullet.vx / steps
        val dy = bullet.vy / steps

        for (s in 0 until steps) {
            bullet.x += dx
            bullet.y += dy

            // Check enemy hits
            for (enemy in enemies) {
                if (enemy.isAlive && enemy.bounds.contains(Offset(bullet.x, bullet.y))) {
                    enemy.isAlive = false
                    onEnemyKilled(enemy)
                }
            }

            // Check TNT hits
            for (barrel in barrels) {
                if (!barrel.isExploded && barrel.bounds.contains(Offset(bullet.x, bullet.y))) {
                    barrel.isExploded = true
                    onTntExploded(barrel)

                    // Explode nearby enemies
                    val bx = barrel.x
                    val by = barrel.y - barrel.height / 2f
                    for (enemy in enemies) {
                        if (enemy.isAlive) {
                            val distSq = (enemy.x - bx) * (enemy.x - bx) + (enemy.y - by) * (enemy.y - by)
                            if (distSq <= barrel.explosionRadius * barrel.explosionRadius) {
                                enemy.isAlive = false
                                onEnemyKilled(enemy)
                            }
                        }
                    }
                }
            }

            // Check wall bounces
            for (wall in walls) {
                val b = wall.bounds
                if (bullet.x + bullet.radius >= b.left && bullet.x - bullet.radius <= b.right &&
                    bullet.y + bullet.radius >= b.top && bullet.y - bullet.radius <= b.bottom
                ) {
                    val overlapLeft = (bullet.x + bullet.radius) - b.left
                    val overlapRight = b.right - (bullet.x - bullet.radius)
                    val overlapTop = (bullet.y + bullet.radius) - b.top
                    val overlapBottom = b.bottom - (bullet.y - bullet.radius)
                    val minOverlap = min(min(overlapLeft, overlapRight), min(overlapTop, overlapBottom))

                    when (minOverlap) {
                        overlapTop -> {
                            bullet.y = b.top - bullet.radius
                            bullet.vy = -bullet.vy
                        }
                        overlapBottom -> {
                            bullet.y = b.bottom + bullet.radius
                            bullet.vy = -bullet.vy
                        }
                        overlapLeft -> {
                            bullet.x = b.left - bullet.radius
                            bullet.vx = -bullet.vx
                        }
                        overlapRight -> {
                            bullet.x = b.right + bullet.radius
                            bullet.vx = -bullet.vx
                        }
                    }

                    bullet.bouncesLeft--
                    if (bullet.bouncesLeft <= 0) {
                        bullet.isAlive = false
                        return
                    }
                }
            }

            // Screen boundary bounds
            if (bullet.x < -20f || bullet.x > 420f || bullet.y < -20f || bullet.y > 720f) {
                bullet.isAlive = false
                return
            }
        }

        // Record trail
        bullet.trail.add(Offset(bullet.x, bullet.y))
        if (bullet.trail.size > 14) {
            bullet.trail.removeAt(0)
        }
    }
}
