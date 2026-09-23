package com.example.doodlejump.engine

import com.example.doodlejump.model.*
import kotlin.random.Random

class DoodleJumpEngine {
    companion object {
        const val GRAVITY = 0.45f
        const val BOUNCE_VELOCITY = -13.5f
        const val SPRING_VELOCITY = -21.0f
        const val PLATFORM_GAP = 65f
    }

    private var highestGeneratedY = 0f

    fun initGame(): Pair<DoodlePlayer, List<DoodlePlatform>> {
        highestGeneratedY = 800f
        val platforms = mutableListOf<DoodlePlatform>()

        // Starting floor platform right below player
        platforms.add(DoodlePlatform(x = 0.4f, y = 250f, type = PlatformType.STATIC))

        var currentY = 250f
        val random = Random.Default

        for (i in 1..25) {
            currentY -= PLATFORM_GAP + random.nextFloat() * 20f
            val x = random.nextFloat() * 0.75f + 0.05f
            val type = when {
                i > 10 && random.nextFloat() < 0.25f -> PlatformType.MOVING
                i > 15 && random.nextFloat() < 0.2f -> PlatformType.FRAGILE
                random.nextFloat() < 0.15f -> PlatformType.SPRING
                else -> PlatformType.STATIC
            }
            platforms.add(DoodlePlatform(x = x, y = currentY, type = type))
        }
        highestGeneratedY = currentY

        val player = DoodlePlayer(x = 0.5f, y = 200f, vy = BOUNCE_VELOCITY)
        return player to platforms
    }

    fun update(
        state: DoodleJumpState,
        moveDirectionX: Float, // -1..1
        onBounce: (isSpring: Boolean) -> Unit,
        onGameOver: () -> Unit
    ): DoodleJumpState {
        if (state.isGameOver) return state

        val player = state.player
        val platforms = state.platforms.toMutableList()
        val random = Random.Default

        // Horizontal movement with wrap-around
        var newX = player.x + moveDirectionX * 0.022f
        if (newX < 0f) newX += 1.0f
        if (newX > 1.0f) newX -= 1.0f

        val facingRight = if (moveDirectionX != 0f) moveDirectionX > 0 else player.facingRight

        // Vertical movement
        val prevY = player.y
        var newVy = player.vy + GRAVITY
        val newY = player.y + newVy

        // Update moving platforms
        for (plat in platforms) {
            if (plat.type == PlatformType.MOVING) {
                plat.x += plat.vx
                if (plat.x < 0.05f || plat.x > 0.75f) {
                    plat.vx = -plat.vx
                }
            }
        }

        // Platform collision (only when falling downward: prevY <= plat.y and newY >= plat.y)
        if (newVy > 0f) {
            for (plat in platforms) {
                if (plat.isBroken) continue
                // Check if foot is within horizontal platform bounds
                val hitX = newX in (plat.x - 0.08f)..(plat.x + plat.width + 0.02f)
                val hitY = prevY <= plat.y && newY >= plat.y

                if (hitX && hitY) {
                    if (plat.type == PlatformType.FRAGILE) {
                        plat.isBroken = true
                    } else if (plat.type == PlatformType.SPRING) {
                        newVy = SPRING_VELOCITY
                        onBounce(true)
                    } else {
                        newVy = BOUNCE_VELOCITY
                        onBounce(false)
                    }
                    break
                }
            }
        }

        // Update camera position (moves up only, never down)
        val cameraTargetY = newY - 350f
        val newCameraY = minOf(state.cameraY, cameraTargetY)

        // Generate more platforms ahead
        while (highestGeneratedY > newCameraY - 500f) {
            highestGeneratedY -= PLATFORM_GAP + random.nextFloat() * 25f
            val x = random.nextFloat() * 0.75f + 0.05f
            val type = when {
                random.nextFloat() < 0.3f -> PlatformType.MOVING
                random.nextFloat() < 0.2f -> PlatformType.FRAGILE
                random.nextFloat() < 0.15f -> PlatformType.SPRING
                else -> PlatformType.STATIC
            }
            platforms.add(DoodlePlatform(x = x, y = highestGeneratedY, type = type))
        }

        // Remove platforms far below camera
        platforms.removeAll { it.y > newCameraY + 800f }

        // Check fall off bottom
        val isDead = newY > newCameraY + 750f
        if (isDead) {
            onGameOver()
            return state.copy(isGameOver = true)
        }

        // Score based on highest climbed altitude
        val score = (-newCameraY / 10f).toInt().coerceAtLeast(state.score)

        return state.copy(
            player = player.copy(x = newX, y = newY, vy = newVy, facingRight = facingRight),
            platforms = platforms,
            cameraY = newCameraY,
            score = score
        )
    }
}
