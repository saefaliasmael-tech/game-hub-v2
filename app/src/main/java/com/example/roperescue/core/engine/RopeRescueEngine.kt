package com.example.roperescue.core.engine

import androidx.compose.ui.geometry.Offset
import com.example.roperescue.core.model.Hazard
import com.example.roperescue.core.model.Wheel
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

object RopeRescueEngine {

    fun distance(a: Offset, b: Offset): Float {
        return hypot(b.x - a.x, b.y - a.y)
    }

    fun distanceToSegment(p: Offset, a: Offset, b: Offset): Float {
        val l2 = (b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y)
        if (l2 == 0f) return distance(p, a)
        var t = ((p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)) / l2
        t = max(0f, min(1f, t))
        val proj = Offset(a.x + t * (b.x - a.x), a.y + t * (b.y - a.y))
        return distance(p, proj)
    }

    fun updateRopePath(
        pivots: List<Offset>,
        touchPos: Offset,
        wheels: List<Wheel>,
        targetPos: Offset
    ): Pair<List<Offset>, Boolean> {
        var updated = pivots.toMutableList()

        // Check unwrapping
        if (updated.size >= 2) {
            val prev = updated[updated.size - 2]
            val last = updated.last()
            if (distance(touchPos, prev) < distance(last, prev) * 0.7f) {
                updated.removeAt(updated.size - 1)
            }
        }

        // Check wrapping around wheels
        val currentLast = updated.last()
        for (wheel in wheels) {
            val wheelPos = Offset(wheel.xPercent, wheel.yPercent)
            if (distance(wheelPos, currentLast) > 0.03f) {
                val d = distanceToSegment(wheelPos, currentLast, touchPos)
                if (d < wheel.radiusPercent) {
                    updated.add(wheelPos)
                    break
                }
            }
        }

        val isAttached = distance(touchPos, targetPos) < 0.08f
        return updated to isAttached
    }

    fun getPointAlongPolyline(points: List<Offset>, progress: Float): Offset {
        if (points.isEmpty()) return Offset.Zero
        if (points.size == 1) return points.first()

        val clamped = progress.coerceIn(0f, 1f)
        var totalLength = 0f
        val segmentLengths = mutableListOf<Float>()

        for (i in 0 until points.size - 1) {
            val segLen = distance(points[i], points[i + 1])
            segmentLengths.add(segLen)
            totalLength += segLen
        }

        if (totalLength == 0f) return points.first()

        val targetDistance = clamped * totalLength
        var accumulated = 0f

        for (i in 0 until segmentLengths.size) {
            val segLen = segmentLengths[i]
            if (accumulated + segLen >= targetDistance || i == segmentLengths.size - 1) {
                val segT = if (segLen > 0f) (targetDistance - accumulated) / segLen else 0f
                val pA = points[i]
                val pB = points[i + 1]
                return Offset(
                    pA.x + segT * (pB.x - pA.x),
                    pA.y + segT * (pB.y - pA.y)
                )
            }
            accumulated += segLen
        }

        return points.last()
    }

    fun checkHazardCollision(position: Offset, hazards: List<Hazard>): Boolean {
        for (h in hazards) {
            val hPos = Offset(h.xPercent, h.yPercent)
            if (distance(position, hPos) < h.radiusPercent) {
                return true
            }
        }
        return false
    }
}
