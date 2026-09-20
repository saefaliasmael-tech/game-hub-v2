package com.example.watersort.core.engine

import com.example.watersort.core.model.Bottle
import com.example.watersort.core.model.GameState
import com.example.watersort.core.model.InvalidReason
import com.example.watersort.core.model.MoveResult
import com.example.watersort.core.model.PourDelta
import com.example.watersort.core.model.PourMove

class GameEngine(
    private val undoStack: UndoStack = UndoStack()
) {
    /**
     * Checks whether pouring from [fromIndex] to [toIndex] is a valid move according to game rules.
     */
    fun validateMove(state: GameState, fromIndex: Int, toIndex: Int): InvalidReason? {
        if (fromIndex == toIndex) return InvalidReason.SAME_BOTTLE
        if (fromIndex !in state.bottles.indices || toIndex !in state.bottles.indices) {
            return InvalidReason.SOURCE_EMPTY
        }

        val source = state.bottles[fromIndex]
        val target = state.bottles[toIndex]

        if (source.isEmpty) return InvalidReason.SOURCE_EMPTY
        if (source.isSolved) return InvalidReason.BOTTLE_ALREADY_SOLVED
        if (target.isFull) return InvalidReason.TARGET_FULL

        val sourceColor = source.topColor ?: return InvalidReason.SOURCE_EMPTY
        if (!target.isEmpty && target.topColor != sourceColor) {
            return InvalidReason.COLOR_MISMATCH
        }

        return null
    }

    /**
     * Executes a pour move atomically and returns MoveResult.
     */
    fun executePour(state: GameState, fromIndex: Int, toIndex: Int): MoveResult {
        val error = validateMove(state, fromIndex, toIndex)
        if (error != null) {
            return MoveResult.Invalid(error)
        }

        val source = state.bottles[fromIndex]
        val target = state.bottles[toIndex]
        val color = source.topColor!!

        val pourAmount = minOf(source.topRunLength, target.availableSpace)

        // Create new layers for source
        val newSourceLayers = source.layers.dropLast(pourAmount)
        val newSource = source.copy(layers = newSourceLayers)

        // Create new layers for target
        val newTargetLayers = target.layers + List(pourAmount) { color }
        val newTarget = target.copy(layers = newTargetLayers)

        val updatedBottles = state.bottles.toMutableList()
        updatedBottles[fromIndex] = newSource
        updatedBottles[toIndex] = newTarget

        val delta = PourDelta(
            fromBottleIndex = fromIndex,
            toBottleIndex = toIndex,
            color = color,
            amountPoured = pourAmount
        )
        undoStack.push(delta)

        val isWon = updatedBottles.all { it.isSolved }
        val movesCount = state.movesCount + 1

        val newState = state.copy(
            bottles = updatedBottles,
            movesCount = movesCount,
            isSolved = isWon,
            selectedBottleIndex = null,
            lastDelta = delta,
            isDeadlocked = !isWon && isDeadlocked(updatedBottles)
        )

        return MoveResult.Success(delta, newState)
    }

    /**
     * Undoes the last move using Action Delta without storing full GameState copies.
     * Validates bounds, amounts, colors, and capacity to prevent state corruption.
     */
    fun undo(state: GameState): GameState? {
        val delta = undoStack.peekLast() ?: return null

        val fromIndex = delta.fromBottleIndex
        val toIndex = delta.toBottleIndex
        val amount = delta.amountPoured
        val color = delta.color

        // Strict Delta Consistency Validation
        if (fromIndex !in state.bottles.indices || toIndex !in state.bottles.indices) {
            return null
        }
        if (amount <= 0) {
            return null
        }

        val currentTarget = state.bottles[toIndex]
        val currentSource = state.bottles[fromIndex]

        // Validate target has enough layers and they match the expected color
        if (currentTarget.layers.size < amount) {
            return null
        }
        val targetTopLayers = currentTarget.layers.takeLast(amount)
        if (targetTopLayers.any { it != color }) {
            return null
        }

        // Validate source has capacity to receive the layers back
        if (currentSource.layers.size + amount > currentSource.capacity) {
            return null
        }

        // State is fully validated to match delta. Safely consume delta from stack.
        undoStack.pop()

        // Reverse: remove amount from target, add to source
        val reversedTargetLayers = currentTarget.layers.dropLast(amount)
        val reversedSourceLayers = currentSource.layers + List(amount) { color }

        val updatedBottles = state.bottles.toMutableList()
        updatedBottles[toIndex] = currentTarget.copy(layers = reversedTargetLayers)
        updatedBottles[fromIndex] = currentSource.copy(layers = reversedSourceLayers)

        val isWon = updatedBottles.all { it.isSolved }

        return state.copy(
            bottles = updatedBottles,
            movesCount = (state.movesCount - 1).coerceAtLeast(0),
            isSolved = isWon,
            selectedBottleIndex = null,
            lastDelta = null,
            isDeadlocked = !isWon && isDeadlocked(updatedBottles)
        )
    }

    /**
     * Adds an extra empty bottle.
     */
    fun addExtraBottle(state: GameState): GameState {
        if (state.hasExtraBottle) return state
        val newId = state.bottles.size
        val extraBottle = Bottle(id = newId, capacity = state.maxCapacity, layers = emptyList())
        val newBottles = state.bottles + extraBottle
        return state.copy(
            bottles = newBottles,
            hasExtraBottle = true,
            isDeadlocked = isDeadlocked(newBottles)
        )
    }

    /**
     * Detects if no useful or legal moves remain in the given bottle state.
     */
    fun isDeadlocked(bottles: List<Bottle>): Boolean {
        if (bottles.all { it.isSolved }) return false
        val validMoves = getLegalMoves(bottles)
        return validMoves.isEmpty()
    }

    /**
     * Returns all legal moves between bottles.
     */
    fun getLegalMoves(bottles: List<Bottle>): List<PourMove> {
        val moves = mutableListOf<PourMove>()
        for (i in bottles.indices) {
            val source = bottles[i]
            if (source.isEmpty) continue
            // If the bottle is already completely solved (full with 1 color), skip pouring out of it
            if (source.isSolved) continue

            val sourceTop = source.topColor ?: continue

            for (j in bottles.indices) {
                if (i == j) continue
                val target = bottles[j]
                if (target.isFull) continue

                if (target.isEmpty) {
                    // Pouring pure run into an empty bottle is useless if source was already only that color
                    if (!source.isPure) {
                        moves.add(PourMove(i, j))
                    }
                } else if (target.topColor == sourceTop) {
                    moves.add(PourMove(i, j))
                }
            }
        }
        return moves
    }

    fun canUndo(): Boolean = undoStack.canUndo

    fun clearUndo() {
        undoStack.clear()
    }

    fun getUndoDeltas(): List<PourDelta> = undoStack.toList()

    fun restoreUndo(deltas: List<PourDelta>) {
        undoStack.restore(deltas)
    }
}
