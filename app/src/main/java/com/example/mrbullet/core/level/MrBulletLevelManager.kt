package com.example.mrbullet.core.level

import com.example.mrbullet.core.model.*
import kotlin.random.Random

object MrBulletLevelManager {

    const val MAX_LEVELS = 100

    fun getLevel(levelNumber: Int): MrBulletLevelConfig {
        val lvl = levelNumber.coerceIn(1, MAX_LEVELS)
        val rng = Random(lvl * 7129L + 31L)

        val heroX = 0.15f
        val heroY = 0.72f

        val enemies = mutableListOf<Enemy>()
        val walls = mutableListOf<Wall>()

        // Floor / platform under hero
        walls.add(Wall(0.05f, 0.78f, 0.28f, 0.78f))

        when {
            lvl == 1 -> {
                // Direct line of sight target
                enemies.add(Enemy(1, 0.80f, 0.72f))
                walls.add(Wall(0.70f, 0.78f, 0.92f, 0.78f))
            }
            lvl == 2 -> {
                // Enemy on high platform
                enemies.add(Enemy(1, 0.82f, 0.35f))
                walls.add(Wall(0.70f, 0.42f, 0.95f, 0.42f))
            }
            lvl == 3 -> {
                // Wall in between requiring ceiling or ground bounce
                enemies.add(Enemy(1, 0.82f, 0.72f))
                walls.add(Wall(0.70f, 0.78f, 0.95f, 0.78f))
                // Middle barrier
                walls.add(Wall(0.50f, 0.50f, 0.50f, 0.85f))
            }
            lvl == 4 -> {
                // TNT crate above enemy
                enemies.add(Enemy(1, 0.80f, 0.72f))
                walls.add(Wall(0.70f, 0.78f, 0.92f, 0.78f))
                // TNT crate hanging
                walls.add(Wall(0.78f, 0.35f, 0.84f, 0.35f, isTnt = true))
            }
            lvl == 5 -> {
                // 2 Enemies on multiple levels
                enemies.add(Enemy(1, 0.75f, 0.32f))
                enemies.add(Enemy(2, 0.80f, 0.72f))
                walls.add(Wall(0.65f, 0.38f, 0.88f, 0.38f))
                walls.add(Wall(0.70f, 0.78f, 0.92f, 0.78f))
            }
            else -> {
                // 2 to 4 enemies
                val enemyCount = (1 + (lvl % 3)).coerceIn(1, 3)
                for (i in 0 until enemyCount) {
                    val ex = 0.65f + (i * 0.10f).coerceAtMost(0.25f)
                    val ey = 0.25f + (i * 0.22f)
                    enemies.add(Enemy(i + 1, ex, ey))
                    // Platform
                    walls.add(Wall(ex - 0.08f, ey + 0.06f, ex + 0.08f, ey + 0.06f))
                }

                // Intermediate obstacles and reflectors
                val numObstacles = 1 + (lvl % 3)
                for (o in 0 until numObstacles) {
                    val ox = 0.35f + o * 0.15f
                    val oy1 = 0.20f + rng.nextFloat() * 0.20f
                    val oy2 = oy1 + 0.25f + rng.nextFloat() * 0.15f
                    val isTnt = (lvl + o) % 4 == 0
                    walls.add(Wall(ox, oy1, ox, oy2, isTnt = isTnt))
                }
            }
        }

        val maxBullets = when {
            enemies.size == 1 -> 3
            enemies.size == 2 -> 4
            else -> 5
        }

        return MrBulletLevelConfig(
            levelNumber = lvl,
            heroX = heroX,
            heroY = heroY,
            maxBullets = maxBullets,
            enemies = enemies,
            walls = walls
        )
    }
}
