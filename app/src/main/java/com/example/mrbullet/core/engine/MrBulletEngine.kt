package com.example.mrbullet.core.engine

import androidx.compose.ui.geometry.Offset
import com.example.mrbullet.core.model.Bullet
import com.example.mrbullet.core.model.Enemy
import com.example.mrbullet.core.model.Wall
import kotlin.math.*

object MrBulletEngine {
    const val BULLET_SPEED = 0.022f
    const val BULLET_RADIUS = 0.012f

    fun calculateStars(bulletsLeft: Int, maxBullets: Int): Int {
        val used = maxBullets - bulletsLeft
        return when {
            used <= 1 -> 3
            used <= 2 -> 2
            else -> 1
        }
    }

    fun calculateLaserPath(
        startX: Float,
        startY: Float,
        angleRad: Float,
        walls: List<Wall>
    ): List<Offset> {
        val points = mutableListOf(Offset(startX, startY))
        var curX = startX
        var curY = startY
        var dirX = cos(angleRad)
        var dirY = sin(angleRad)

        var bounces = 2
        while (bounces >= 0) {
            // Find closest collision with walls or screen boundary
            var closestDist = 2.0f
            var hitWall: Wall? = null
            var hitNormalX = 0f
            var hitNormalY = 0f

            for (w in walls) {
                if (w.isDestroyed) continue
                // Ray-segment intersection
                val intersection = raySegmentIntersection(curX, curY, dirX, dirY, w.x1, w.y1, w.x2, w.y2)
                if (intersection != null && intersection.first in 0.001f..closestDist) {
                    closestDist = intersection.first
                    hitWall = w
                    val (nx, ny) = getSegmentNormal(w.x1, w.y1, w.x2, w.y2, dirX, dirY)
                    hitNormalX = nx
                    hitNormalY = ny
                }
            }

            // Screen boundaries
            val boundT = rayBoundsIntersection(curX, curY, dirX, dirY)
            if (boundT != null && boundT.first < closestDist) {
                closestDist = boundT.first
                hitWall = null
                hitNormalX = boundT.second.first
                hitNormalY = boundT.second.second
            }

            curX += dirX * closestDist
            curY += dirY * closestDist
            points.add(Offset(curX, curY))

            if (closestDist >= 1.5f || bounces == 0) break

            // Reflect dir
            val dot = dirX * hitNormalX + dirY * hitNormalY
            dirX -= 2 * dot * hitNormalX
            dirY -= 2 * dot * hitNormalY
            bounces--
        }

        return points
    }

    fun stepPhysics(
        bullets: List<Bullet>,
        enemies: List<Enemy>,
        walls: List<Wall>
    ): Triple<List<Bullet>, List<Enemy>, List<Wall>> {
        val updatedBullets = mutableListOf<Bullet>()
        val updatedEnemies = enemies.map { it.copy() }
        val updatedWalls = walls.map { it.copy() }

        for (b in bullets) {
            if (!b.isAlive) continue

            b.x += b.vx
            b.y += b.vy

            // Check enemy hits
            for (e in updatedEnemies) {
                if (!e.isDead) {
                    if (b.x >= e.x - e.width / 2 && b.x <= e.x + e.width / 2 &&
                        b.y >= e.y - e.height / 2 && b.y <= e.y + e.height / 2
                    ) {
                        e.isDead = true
                        b.isAlive = false
                        break
                    }
                }
            }

            if (!b.isAlive) continue

            // Screen boundaries
            if (b.x <= BULLET_RADIUS) {
                b.x = BULLET_RADIUS
                b.vx = abs(b.vx)
                b.bouncesLeft--
            } else if (b.x >= 1f - BULLET_RADIUS) {
                b.x = 1f - BULLET_RADIUS
                b.vx = -abs(b.vx)
                b.bouncesLeft--
            }

            if (b.y <= BULLET_RADIUS) {
                b.y = BULLET_RADIUS
                b.vy = abs(b.vy)
                b.bouncesLeft--
            } else if (b.y >= 1f - BULLET_RADIUS) {
                b.y = 1f - BULLET_RADIUS
                b.vy = -abs(b.vy)
                b.bouncesLeft--
            }

            // Wall collisions
            for (w in updatedWalls) {
                if (w.isDestroyed) continue
                val dist = pointToSegmentDistance(b.x, b.y, w.x1, w.y1, w.x2, w.y2)
                if (dist < BULLET_RADIUS + 0.015f) {
                    if (w.isTnt) {
                        w.isDestroyed = true
                        // Explode TNT: kill nearby enemies
                        for (e in updatedEnemies) {
                            val ed = hypot(e.x - (w.x1 + w.x2) / 2, e.y - (w.y1 + w.y2) / 2)
                            if (ed < 0.28f) {
                                e.isDead = true
                            }
                        }
                        b.isAlive = false
                        break
                    } else if (w.isDestructible) {
                        w.isDestroyed = true
                        b.bouncesLeft--
                    } else {
                        val (nx, ny) = getSegmentNormal(w.x1, w.y1, w.x2, w.y2, b.vx, b.vy)
                        val dot = b.vx * nx + b.vy * ny
                        if (dot < 0) {
                            b.vx -= 2 * dot * nx
                            b.vy -= 2 * dot * ny
                        }
                        b.bouncesLeft--
                    }
                }
            }

            if (b.bouncesLeft <= 0) {
                b.isAlive = false
            }

            if (b.isAlive) {
                updatedBullets.add(b)
            }
        }

        return Triple(updatedBullets, updatedEnemies, updatedWalls)
    }

    private fun pointToSegmentDistance(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        val lenSq = dx * dx + dy * dy
        if (lenSq < 1e-6f) return hypot(px - x1, py - y1)
        val t = (((px - x1) * dx + (py - y1) * dy) / lenSq).coerceIn(0f, 1f)
        return hypot(px - (x1 + t * dx), py - (y1 + t * dy))
    }

    private fun raySegmentIntersection(
        rx: Float, ry: Float, dx: Float, dy: Float,
        x1: Float, y1: Float, x2: Float, y2: Float
    ): Pair<Float, Unit>? {
        val sx = x2 - x1
        val sy = y2 - y1
        val denom = dx * sy - dy * sx
        if (abs(denom) < 1e-6f) return null

        val t = ((x1 - rx) * sy - (y1 - ry) * sx) / denom
        val u = ((x1 - rx) * dy - (y1 - ry) * dx) / denom

        return if (t > 0.001f && u in 0f..1f) Pair(t, Unit) else null
    }

    private fun rayBoundsIntersection(
        rx: Float, ry: Float, dx: Float, dy: Float
    ): Pair<Float, Pair<Float, Float>>? {
        var minT = Float.MAX_VALUE
        var normal = Pair(0f, 0f)

        if (dx > 1e-5f) {
            val t = (1f - rx) / dx
            if (t > 0 && t < minT) { minT = t; normal = Pair(-1f, 0f) }
        } else if (dx < -1e-5f) {
            val t = -rx / dx
            if (t > 0 && t < minT) { minT = t; normal = Pair(1f, 0f) }
        }

        if (dy > 1e-5f) {
            val t = (1f - ry) / dy
            if (t > 0 && t < minT) { minT = t; normal = Pair(0f, -1f) }
        } else if (dy < -1e-5f) {
            val t = -ry / dy
            if (t > 0 && t < minT) { minT = t; normal = Pair(0f, 1f) }
        }

        return if (minT < 10f) Pair(minT, normal) else null
    }

    private fun getSegmentNormal(x1: Float, y1: Float, x2: Float, y2: Float, inVx: Float, inVy: Float): Pair<Float, Float> {
        val dx = x2 - x1
        val dy = y2 - y1
        val len = hypot(dx, dy).coerceAtLeast(1e-4f)
        var nx = -dy / len
        var ny = dx / len
        if (inVx * nx + inVy * ny > 0) {
            nx = -nx
            ny = -ny
        }
        return Pair(nx, ny)
    }
}
