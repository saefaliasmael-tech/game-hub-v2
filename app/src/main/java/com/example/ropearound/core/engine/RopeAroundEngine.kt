package com.example.ropearound.core.engine

import androidx.compose.ui.geometry.Offset
import com.example.ropearound.core.model.Peg
import com.example.ropearound.core.model.RopeObstacle
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

object RopeAroundEngine {

    fun distance(a: Offset, b: Offset): Float {
        return hypot(b.x - a.x, b.y - a.y)
    }

    /**
     * Distance from point P to line segment AB.
     */
    fun distanceToSegment(p: Offset, a: Offset, b: Offset): Float {
        val l2 = (b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y)
        if (l2 == 0f) return distance(p, a)
        var t = ((p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)) / l2
        t = max(0f, min(1f, t))
        val proj = Offset(a.x + t * (b.x - a.x), a.y + t * (b.y - a.y))
        return distance(p, proj)
    }

    fun calculateTotalLength(pivots: List<Offset>, currentEnd: Offset): Float {
        if (pivots.isEmpty()) return 0f
        var len = 0f
        for (i in 0 until pivots.size - 1) {
            len += distance(pivots[i], pivots[i + 1])
        }
        len += distance(pivots.last(), currentEnd)
        return len
    }

    /**
     * Checks if rope touches any obstacle.
     */
    fun checkObstacleCollision(
        pivots: List<Offset>,
        currentEnd: Offset,
        obstacles: List<RopeObstacle>
    ): Boolean {
        val allPoints = pivots + currentEnd
        for (obs in obstacles) {
            val obsPos = Offset(obs.xPercent, obs.yPercent)
            for (i in 0 until allPoints.size - 1) {
                val d = distanceToSegment(obsPos, allPoints[i], allPoints[i + 1])
                if (d < obs.radiusPercent) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Updates rope pivots when wrapping around pegs.
     */
    fun updatePivots(
        pivots: List<Offset>,
        touchPos: Offset,
        pegs: List<Peg>
    ): Pair<List<Offset>, List<Peg>> {
        if (pivots.isEmpty()) return pivots to pegs

        val lastPivot = pivots.last()
        var updatedPivots = pivots.toMutableList()
        val updatedPegs = pegs.map { it.copy() }.toMutableList()

        // 1. Check unwrapping if player pulled back past the second to last pivot
        if (updatedPivots.size >= 2) {
            val prevPivot = updatedPivots[updatedPivots.size - 2]
            val distToPrev = distance(touchPos, prevPivot)
            val pivotDist = distance(lastPivot, prevPivot)
            if (distToPrev < pivotDist * 0.75f) {
                // Unwrap last pivot
                updatedPivots.removeAt(updatedPivots.size - 1)
            }
        }

        // 2. Check if current segment wraps around any peg
        val currentLast = updatedPivots.last()
        for (i in updatedPegs.indices) {
            val peg = updatedPegs[i]
            val pegPos = Offset(peg.xPercent, peg.yPercent)

            // Don't re-test if already current last pivot
            if (distance(pegPos, currentLast) > 0.02f) {
                val d = distanceToSegment(pegPos, currentLast, touchPos)
                if (d < peg.radiusPercent) {
                    // Rope wrapped around this peg!
                    updatedPivots.add(pegPos)
                    updatedPegs[i] = peg.copy(isWrapped = true)
                    break
                }
            }
        }

        // Re-evaluate wrapped status for all pegs that have a pivot matching them
        for (i in updatedPegs.indices) {
            val pegPos = Offset(updatedPegs[i].xPercent, updatedPegs[i].yPercent)
            val isContained = updatedPivots.any { distance(it, pegPos) < 0.02f }
            if (isContained) {
                updatedPegs[i] = updatedPegs[i].copy(isWrapped = true)
            }
        }

        return updatedPivots to updatedPegs
    }
}
