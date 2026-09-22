package com.example.funfrenzy.core.engine

import androidx.compose.ui.geometry.Offset
import com.example.funfrenzy.core.model.ExitPortal
import com.example.funfrenzy.core.model.FrenzyHazard
import com.example.funfrenzy.core.model.RescueBuddy
import com.example.funfrenzy.core.model.RescueRope
import com.example.funfrenzy.core.model.RopeAnchor
import kotlin.math.hypot
import kotlin.math.max

object FunFrenzyPhysicsEngine {
    private const val GRAVITY = 0.26f
    private const val DAMPING = 0.982f
    private const val ROPE_STIFFNESS = 0.45f

    fun updatePhysics(
        buddy: RescueBuddy,
        ropes: List<RescueRope>,
        anchors: Map<Int, RopeAnchor>,
        hazards: List<FrenzyHazard>,
        portal: ExitPortal
    ) {
        if (buddy.isSaved || buddy.isDead) return

        buddy.vy += GRAVITY
        buddy.vx *= DAMPING
        buddy.vy *= DAMPING

        // Rope tension constraints
        for (rope in ropes) {
            if (!rope.isCut) {
                val anchor = anchors[rope.anchorId] ?: continue
                val dx = buddy.x - anchor.x
                val dy = buddy.y - anchor.y
                val dist = hypot(dx, dy)

                if (dist > rope.restLength) {
                    val excess = dist - rope.restLength
                    val nx = dx / max(dist, 1e-4f)
                    val ny = dy / max(dist, 1e-4f)

                    // Pull buddy along rope normal towards anchor
                    buddy.vx -= nx * excess * ROPE_STIFFNESS
                    buddy.vy -= ny * excess * ROPE_STIFFNESS
                }
            }
        }

        buddy.x += buddy.vx
        buddy.y += buddy.vy

        // Collision with hazards
        for (hazard in hazards) {
            val b = hazard.bounds
            if (buddy.x + buddy.radius >= b.left && buddy.x - buddy.radius <= b.right &&
                buddy.y + buddy.radius >= b.top && buddy.y - buddy.radius <= b.bottom
            ) {
                buddy.isDead = true
                return
            }
        }

        // Collision with exit portal
        val pb = portal.bounds
        if (buddy.x >= pb.left && buddy.x <= pb.right &&
            buddy.y >= pb.top && buddy.y <= pb.bottom + 20f
        ) {
            buddy.isSaved = true
            buddy.vx = 0f
            buddy.vy = 0f
            return
        }

        // Fall out of bounds
        if (buddy.y > 750f || buddy.x < -60f || buddy.x > 460f) {
            buddy.isDead = true
        }
    }

    fun checkSwipeCut(
        p1: Offset,
        p2: Offset,
        ropes: List<RescueRope>,
        anchors: Map<Int, RopeAnchor>,
        buddy: RescueBuddy
    ): RescueRope? {
        for (rope in ropes) {
            if (!rope.isCut) {
                val anchor = anchors[rope.anchorId] ?: continue
                val r1 = Offset(anchor.x, anchor.y)
                val r2 = Offset(buddy.x, buddy.y)

                if (linesIntersect(p1, p2, r1, r2)) {
                    rope.isCut = true
                    return rope
                }
            }
        }
        return null
    }

    private fun linesIntersect(
        p1: Offset, p2: Offset,
        p3: Offset, p4: Offset
    ): Boolean {
        fun ccw(a: Offset, b: Offset, c: Offset): Boolean {
            return (c.y - a.y) * (b.x - a.x) > (b.y - a.y) * (c.x - a.x)
        }
        return ccw(p1, p3, p4) != ccw(p2, p3, p4) && ccw(p1, p2, p3) != ccw(p1, p2, p4)
    }
}
