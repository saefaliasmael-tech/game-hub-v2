package com.example.holeio.engine

import androidx.compose.ui.graphics.Color
import com.example.holeio.model.CityObject
import com.example.holeio.model.HoleEntity
import com.example.holeio.model.HoleIoState
import kotlin.math.sqrt
import kotlin.random.Random

class HoleIoEngine {
    companion object {
        const val ARENA_WIDTH = 1200f
        const val ARENA_HEIGHT = 1200f
    }

    fun initState(): HoleIoState {
        val random = Random(42)
        val objects = mutableListOf<CityObject>()
        var objId = 0

        // Small items (traffic cones, trash cans, hydrants)
        repeat(70) {
            objects.add(
                CityObject(
                    id = objId++,
                    x = random.nextFloat() * (ARENA_WIDTH - 80f) + 40f,
                    y = random.nextFloat() * (ARENA_HEIGHT - 80f) + 40f,
                    radius = 12f,
                    points = 5,
                    color = Color(0xFFFF9800),
                    name = "Cone"
                )
            )
        }

        // Medium items (park benches, street trees, streetlamps)
        repeat(45) {
            objects.add(
                CityObject(
                    id = objId++,
                    x = random.nextFloat() * (ARENA_WIDTH - 100f) + 50f,
                    y = random.nextFloat() * (ARENA_HEIGHT - 100f) + 50f,
                    radius = 24f,
                    points = 15,
                    color = Color(0xFF4CAF50),
                    name = "Tree"
                )
            )
        }

        // Large items (parked cars, hot dog stands)
        repeat(25) {
            objects.add(
                CityObject(
                    id = objId++,
                    x = random.nextFloat() * (ARENA_WIDTH - 120f) + 60f,
                    y = random.nextFloat() * (ARENA_HEIGHT - 120f) + 60f,
                    radius = 42f,
                    points = 40,
                    color = Color(0xFF2196F3),
                    name = "Car"
                )
            )
        }

        // Huge items (buildings, buses)
        repeat(12) {
            objects.add(
                CityObject(
                    id = objId++,
                    x = random.nextFloat() * (ARENA_WIDTH - 160f) + 80f,
                    y = random.nextFloat() * (ARENA_HEIGHT - 160f) + 80f,
                    radius = 70f,
                    points = 100,
                    color = Color(0xFF9C27B0),
                    name = "Building"
                )
            )
        }

        val player = HoleEntity(
            id = 0,
            name = "You",
            color = Color(0xFF00E5FF),
            x = ARENA_WIDTH / 2f,
            y = ARENA_HEIGHT / 2f,
            radius = 26f
        )

        val bots = listOf(
            HoleEntity(
                id = 1,
                name = "VoidBot",
                color = Color(0xFFFF1744),
                x = 250f,
                y = 250f,
                radius = 26f
            ),
            HoleEntity(
                id = 2,
                name = "AbyssBot",
                color = Color(0xFFFFEA00),
                x = 950f,
                y = 950f,
                radius = 26f
            )
        )

        return HoleIoState(
            playerHole = player,
            botHoles = bots,
            objects = objects,
            timeLeftSeconds = 60,
            isGameOver = false
        )
    }

    fun updatePhysics(
        state: HoleIoState,
        playerTargetX: Float,
        playerTargetY: Float,
        onItemEaten: () -> Unit
    ): HoleIoState {
        if (state.isGameOver) return state

        val player = state.playerHole
        val bots = state.botHoles
        val random = Random.Default

        // Move player towards target drag
        val pDx = playerTargetX - player.x
        val pDy = playerTargetY - player.y
        val pDist = sqrt(pDx * pDx + pDy * pDy)
        if (pDist > 4f) {
            val speed = 5.5f
            player.x = (player.x + (pDx / pDist) * speed).coerceIn(player.radius, ARENA_WIDTH - player.radius)
            player.y = (player.y + (pDy / pDist) * speed).coerceIn(player.radius, ARENA_HEIGHT - player.radius)
        }

        // Move bots toward nearest small edible objects
        for (bot in bots) {
            val nearest = state.objects.find { !it.isEaten && it.radius < bot.radius }
            if (nearest != null) {
                val bDx = nearest.x - bot.x
                val bDy = nearest.y - bot.y
                val bDist = sqrt(bDx * bDx + bDy * bDy)
                if (bDist > 4f) {
                    val bSpeed = 4.2f
                    bot.x = (bot.x + (bDx / bDist) * bSpeed).coerceIn(bot.radius, ARENA_WIDTH - bot.radius)
                    bot.y = (bot.y + (bDy / bDist) * bSpeed).coerceIn(bot.radius, ARENA_HEIGHT - bot.radius)
                }
            }
        }

        val allHoles = listOf(player) + bots

        // Check object consumption
        for (obj in state.objects) {
            if (obj.isEaten) continue

            for (hole in allHoles) {
                val dx = obj.x - hole.x
                val dy = obj.y - hole.y
                val dist = sqrt(dx * dx + dy * dy)

                if (dist < (hole.radius - obj.radius * 0.4f) && obj.radius < hole.radius) {
                    obj.isEaten = true
                    hole.score += obj.points
                    // Hole grows in radius as it consumes
                    hole.radius = (hole.radius + obj.radius * 0.08f).coerceAtMost(110f)

                    if (hole.id == 0) {
                        onItemEaten()
                    }
                    break
                }
            }
        }

        return state
    }
}
