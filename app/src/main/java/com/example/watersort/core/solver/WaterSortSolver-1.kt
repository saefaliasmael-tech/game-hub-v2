package com.example.watersort.core.solver

import com.example.watersort.core.model.Bottle
import com.example.watersort.core.model.LiquidColor
import com.example.watersort.core.model.PourMove
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

sealed interface SolverResult {
    data class Solved(
        val moves: List<PourMove>,
        val exploredStates: Int,
        val durationMs: Long
    ) : SolverResult

    data class Unsolvable(val exploredStates: Int, val durationMs: Long) : SolverResult
    data class Timeout(val exploredStates: Int, val durationMs: Long) : SolverResult
    object Cancelled : SolverResult
}

class WaterSortSolver(
    private val maxStates: Int = 30_000,
    private val maxDepth: Int = 75,
    private val timeoutMs: Long = 2_500L
) {
    /**
     * Solves the given bottle puzzle in the background using BFS with canonical state pruning.
     */
    suspend fun solve(
        initialBottles: List<Bottle>,
        capacity: Int = 4
    ): SolverResult = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()

        // Fast check: if already solved
        if (initialBottles.all { it.isSolved }) {
            return@withContext SolverResult.Solved(emptyList(), 0, System.currentTimeMillis() - startTime)
        }

        // Cache lookup
        val cacheKey = encodeState(initialBottles)
        SolutionCache.get(cacheKey)?.let { cachedMoves ->
            return@withContext SolverResult.Solved(cachedMoves, 0, System.currentTimeMillis() - startTime)
        }

        // BFS queue node
        class SearchNode(
            val bottles: List<Bottle>,
            val moves: List<PourMove>,
            val depth: Int
        )

        val queue = ArrayDeque<SearchNode>()
        val visited = HashSet<String>()

        val rootStateKey = encodeCanonicalState(initialBottles)
        visited.add(rootStateKey)
        queue.add(SearchNode(initialBottles, emptyList(), 0))

        var exploredCount = 0

        while (queue.isNotEmpty()) {
            coroutineContext.ensureActive()

            if (System.currentTimeMillis() - startTime > timeoutMs) {
                return@withContext SolverResult.Timeout(exploredCount, System.currentTimeMillis() - startTime)
            }

            if (exploredCount >= maxStates) {
                return@withContext SolverResult.Timeout(exploredCount, System.currentTimeMillis() - startTime)
            }

            val current = queue.removeFirst()
            exploredCount++

            // Check goal
            if (current.bottles.all { it.isSolved }) {
                val duration = System.currentTimeMillis() - startTime
                SolutionCache.put(cacheKey, current.moves)
                return@withContext SolverResult.Solved(current.moves, exploredCount, duration)
            }

            if (current.depth >= maxDepth) {
                continue
            }

            // Generate next moves with symmetry reduction
            val moves = generateMoves(current.bottles, capacity)

            for (move in moves) {
                val nextBottles = applyMove(current.bottles, move.fromBottleIndex, move.toBottleIndex, capacity)
                val canonicalKey = encodeCanonicalState(nextBottles)

                if (visited.add(canonicalKey)) {
                    queue.add(
                        SearchNode(
                            bottles = nextBottles,
                            moves = current.moves + move,
                            depth = current.depth + 1
                        )
                    )
                }
            }
        }

        val duration = System.currentTimeMillis() - startTime
        return@withContext SolverResult.Unsolvable(exploredCount, duration)
    }

    private fun generateMoves(bottles: List<Bottle>, capacity: Int): List<PourMove> {
        val result = ArrayList<PourMove>()
        var pouredIntoEmpty = false

        for (i in bottles.indices) {
            val src = bottles[i]
            if (src.isEmpty || src.isSolved) continue
            val srcTop = src.topColor ?: continue

            for (j in bottles.indices) {
                if (i == j) continue
                val dst = bottles[j]
                if (dst.isFull) continue

                if (dst.isEmpty) {
                    // Pouring into empty bottle:
                    // 1. Only consider the first empty bottle to eliminate permutation symmetry
                    // 2. Do not pour if source is already pure (pointless move)
                    if (!pouredIntoEmpty && !src.isPure) {
                        result.add(PourMove(i, j))
                        pouredIntoEmpty = true
                    }
                } else if (dst.topColor == srcTop) {
                    result.add(PourMove(i, j))
                }
            }
        }
        return result
    }

    private fun applyMove(bottles: List<Bottle>, from: Int, to: Int, capacity: Int): List<Bottle> {
        val src = bottles[from]
        val dst = bottles[to]
        val color = src.topColor ?: return bottles

        val pourAmount = minOf(src.topRunLength, dst.availableSpace)
        val newSrc = src.copy(layers = src.layers.dropLast(pourAmount))
        val newDst = dst.copy(layers = dst.layers + List(pourAmount) { color })

        val list = bottles.toMutableList()
        list[from] = newSrc
        list[to] = newDst
        return list
    }

    companion object {
        fun encodeState(bottles: List<Bottle>): String {
            return bottles.joinToString(";") { b ->
                b.layers.joinToString(",") { it.id.toString() }
            }
        }

        fun encodeCanonicalState(bottles: List<Bottle>): String {
            // Sort bottle signatures so permutations of identical bottles are treated as the same state
            return bottles.map { b ->
                b.layers.joinToString(",") { it.id.toString() }
            }.sorted().joinToString(";")
        }
    }
}
