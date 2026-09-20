package com.example.watersort.core.level

import com.example.watersort.core.model.Bottle
import com.example.watersort.core.solver.SolverResult
import com.example.watersort.core.solver.WaterSortSolver

data class ValidationReport(
    val totalTested: Int,
    val validCount: Int,
    val tooEasyCount: Int,
    val timeoutCount: Int,
    val unsolvableCount: Int
)

class LevelValidator(
    private val generator: LevelGenerator = LevelGenerator(),
    private val solver: WaterSortSolver = WaterSortSolver()
) {
    suspend fun validateBatch(startLevel: Int, count: Int): ValidationReport {
        var valid = 0
        var tooEasy = 0
        var timeouts = 0
        var unsolvables = 0

        for (id in startLevel until (startLevel + count)) {
            val level = generator.generateLevel(id)
            when (val res = solver.solve(level.bottles, level.difficulty.capacity)) {
                is SolverResult.Solved -> {
                    if (res.moves.size < 3) {
                        tooEasy++
                    } else {
                        valid++
                    }
                }
                is SolverResult.Timeout -> timeouts++
                is SolverResult.Unsolvable -> unsolvables++
                SolverResult.Cancelled -> {}
            }
        }

        return ValidationReport(
            totalTested = count,
            validCount = valid,
            tooEasyCount = tooEasy,
            timeoutCount = timeouts,
            unsolvableCount = unsolvables
        )
    }

    fun isTrivial(bottles: List<Bottle>): Boolean {
        return bottles.all { it.isEmpty || it.isSolved }
    }
}
