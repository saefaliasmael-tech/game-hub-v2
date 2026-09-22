package com.example.mrbullet.core.level

import androidx.compose.ui.geometry.Rect
import com.example.mrbullet.core.model.BarrierType
import com.example.mrbullet.core.model.EnemyTarget
import com.example.mrbullet.core.model.HeroShooter
import com.example.mrbullet.core.model.MrBulletLevelConfig
import com.example.mrbullet.core.model.MrBulletWall
import com.example.mrbullet.core.model.TntBarrel

object MrBulletLevelManager {
    const val TOTAL_LEVELS = 100
    const val VIRTUAL_WIDTH = 400f
    const val VIRTUAL_HEIGHT = 700f

    fun getLevel(levelNumber: Int): MrBulletLevelConfig {
        val clampedLevel = levelNumber.coerceIn(1, TOTAL_LEVELS)
        return generateLevel(clampedLevel)
    }

    private fun generateLevel(level: Int): MrBulletLevelConfig {
        return when (level) {
            1 -> MrBulletLevelConfig(
                levelNumber = 1,
                title = "Direct Line of Sight",
                hero = HeroShooter(x = 60f, y = 520f),
                maxBullets = 3,
                enemies = listOf(
                    EnemyTarget(id = 1, x = 320f, y = 520f)
                ),
                walls = listOf(
                    MrBulletWall(Rect(20f, 520f, 380f, 560f), BarrierType.METAL_WALL)
                )
            )
            2 -> MrBulletLevelConfig(
                levelNumber = 2,
                title = "The High Ground",
                hero = HeroShooter(x = 60f, y = 540f),
                maxBullets = 3,
                enemies = listOf(
                    EnemyTarget(id = 1, x = 330f, y = 320f),
                    EnemyTarget(id = 2, x = 330f, y = 540f)
                ),
                walls = listOf(
                    MrBulletWall(Rect(20f, 540f, 380f, 570f), BarrierType.METAL_WALL),
                    MrBulletWall(Rect(280f, 320f, 380f, 350f), BarrierType.METAL_WALL)
                )
            )
            3 -> MrBulletLevelConfig(
                levelNumber = 3,
                title = "First Ricochet",
                hero = HeroShooter(x = 60f, y = 520f),
                maxBullets = 3,
                enemies = listOf(
                    EnemyTarget(id = 1, x = 320f, y = 520f)
                ),
                walls = listOf(
                    // Shield wall directly blocking direct line of sight
                    MrBulletWall(Rect(180f, 380f, 210f, 530f), BarrierType.STEEL_BEAM),
                    // Ceiling bounce pad
                    MrBulletWall(Rect(100f, 160f, 300f, 190f), BarrierType.METAL_WALL),
                    MrBulletWall(Rect(20f, 520f, 380f, 550f), BarrierType.METAL_WALL)
                )
            )
            4 -> MrBulletLevelConfig(
                levelNumber = 4,
                title = "Explosive Barrel",
                hero = HeroShooter(x = 60f, y = 500f),
                maxBullets = 3,
                enemies = listOf(
                    EnemyTarget(id = 1, x = 310f, y = 340f),
                    EnemyTarget(id = 2, x = 350f, y = 500f)
                ),
                barrels = listOf(
                    TntBarrel(id = 101, x = 310f, y = 500f)
                ),
                walls = listOf(
                    MrBulletWall(Rect(20f, 500f, 380f, 540f), BarrierType.METAL_WALL),
                    MrBulletWall(Rect(270f, 340f, 360f, 370f), BarrierType.WOOD_CRATE)
                )
            )
            5 -> MrBulletLevelConfig(
                levelNumber = 5,
                title = "Corridor Bounce",
                hero = HeroShooter(x = 70f, y = 240f),
                maxBullets = 4,
                enemies = listOf(
                    EnemyTarget(id = 1, x = 320f, y = 540f),
                    EnemyTarget(id = 2, x = 70f, y = 540f)
                ),
                walls = listOf(
                    MrBulletWall(Rect(20f, 240f, 140f, 270f), BarrierType.METAL_WALL),
                    MrBulletWall(Rect(20f, 540f, 380f, 570f), BarrierType.METAL_WALL),
                    MrBulletWall(Rect(180f, 280f, 210f, 480f), BarrierType.STEEL_BEAM),
                    MrBulletWall(Rect(370f, 100f, 390f, 540f), BarrierType.METAL_WALL)
                )
            )
            else -> generateProceduralLevel(level)
        }
    }

    private fun generateProceduralLevel(level: Int): MrBulletLevelConfig {
        val tier = (level - 1) / 10
        val subIndex = (level - 1) % 10

        val heroX = 60f + (subIndex % 3) * 15f
        val heroY = 480f + (subIndex % 2) * 40f

        val enemyCount = (1 + (level / 15)).coerceIn(1, 4)
        val enemies = mutableListOf<EnemyTarget>()

        for (i in 0 until enemyCount) {
            val ex = 240f + ((level * 31 + i * 53) % 110)
            val ey = when ((subIndex + i) % 3) {
                0 -> 520f
                1 -> 340f - (i * 40f)
                else -> 220f + (i * 60f)
            }
            enemies.add(EnemyTarget(id = i + 1, x = ex, y = ey))
        }

        val barrels = mutableListOf<TntBarrel>()
        if (level % 2 == 0) {
            val bx = 280f + ((level * 19) % 60)
            val by = 520f
            barrels.add(TntBarrel(id = 100 + level, x = bx, y = by))
        }

        val walls = mutableListOf<MrBulletWall>()
        // Ground platform
        walls.add(MrBulletWall(Rect(10f, 520f, 390f, 560f), BarrierType.METAL_WALL))

        // Elevated ledges for enemies
        for (i in 0 until enemyCount) {
            val ey = enemies[i].y
            if (ey < 500f) {
                val ex = enemies[i].x
                walls.add(MrBulletWall(Rect(ex - 45f, ey, ex + 45f, ey + 25f), BarrierType.METAL_WALL))
            }
        }

        // Bouncing walls / dividers
        if (level >= 3) {
            val wallX = 170f + ((level * 23) % 40)
            val wallY = 280f + ((level * 17) % 60)
            walls.add(MrBulletWall(Rect(wallX, wallY, wallX + 24f, wallY + 140f), BarrierType.STEEL_BEAM))
        }

        // Ceiling ricochet reflector
        walls.add(MrBulletWall(Rect(30f, 90f, 370f, 115f), BarrierType.METAL_WALL))

        val maxBullets = when {
            enemyCount >= 3 -> 4
            level % 5 == 0 -> 4
            else -> 3
        }

        val titles = listOf(
            "Trick Shot", "Double Ricochet", "Angle of Impact", "Shatter Crate",
            "Target Acquired", "High Velocity", "Bank Shot", "Crossfire Alley",
            "Deflection Matrix", "Clean Sweep", "Master Marksman", "Deadly Arc"
        )
        val title = titles[(level - 1) % titles.size] + " Lv.$level"

        return MrBulletLevelConfig(
            levelNumber = level,
            title = title,
            hero = HeroShooter(heroX, heroY),
            maxBullets = maxBullets,
            enemies = enemies,
            barrels = barrels,
            walls = walls
        )
    }
}
