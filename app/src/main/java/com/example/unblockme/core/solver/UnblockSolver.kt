package com.example.unblockme.core.solver

import com.example.unblockme.core.model.Block
import com.example.unblockme.core.model.Orientation
import com.example.unblockme.core.model.UnblockMove
import java.util.ArrayDeque

object UnblockSolver {

    const val GRID_SIZE = 6

    data class SolverNode(
        val blocks: List<Block>,
        val moves: List<UnblockMove>
    )

    /**
     * Solves the Unblock Me puzzle using Breadth-First Search (BFS).
     * Returns the shortest sequence of moves to escape the target block,
     * or null if no solution exists.
     */
    fun solve(initialBlocks: List<Block>, maxDepth: Int = 100): List<UnblockMove>? {
        val target = initialBlocks.find { it.isTarget } ?: return null
        if (target.isEscaped) return emptyList()

        val queue = ArrayDeque<SolverNode>()
        val visited = HashSet<String>()

        val initialKey = encodeState(initialBlocks)
        visited.add(initialKey)
        queue.add(SolverNode(initialBlocks, emptyList()))

        var nodesExplored = 0
        val maxExplored = 50_000 // Safeguard against combinatorial explosion

        while (queue.isNotEmpty() && nodesExplored < maxExplored) {
            nodesExplored++
            val current = queue.poll() ?: break

            if (current.moves.size >= maxDepth) continue

            // Check if solved
            val currentTarget = current.blocks.find { it.isTarget }
            if (currentTarget != null && currentTarget.isEscaped) {
                return current.moves
            }

            // Build occupancy grid for fast collision check
            val grid = buildOccupancyGrid(current.blocks)

            // Generate all valid moves for each block
            for (blockIndex in current.blocks.indices) {
                val block = current.blocks[blockIndex]
                val validMoves = getPossibleMovesForBlock(block, grid)

                for (destinationPos in validMoves) {
                    val updatedBlock = if (block.orientation == Orientation.HORIZONTAL) {
                        block.copy(col = destinationPos)
                    } else {
                        block.copy(row = destinationPos)
                    }

                    val updatedBlocks = current.blocks.toMutableList()
                    updatedBlocks[blockIndex] = updatedBlock

                    val key = encodeState(updatedBlocks)
                    if (visited.add(key)) {
                        val move = UnblockMove(
                            blockId = block.id,
                            fromRow = block.row,
                            fromCol = block.col,
                            toRow = updatedBlock.row,
                            toCol = updatedBlock.col
                        )
                        val nextMoves = current.moves + move
                        if (updatedBlock.isEscaped) {
                            return nextMoves
                        }
                        queue.add(SolverNode(updatedBlocks, nextMoves))
                    }
                }
            }
        }

        return null
    }

    /**
     * Builds a 6x6 occupancy matrix with block IDs. Empty cells are null.
     */
    fun buildOccupancyGrid(blocks: List<Block>): Array<Array<String?>> {
        val grid = Array(GRID_SIZE) { Array<String?>(GRID_SIZE) { null } }
        for (b in blocks) {
            for ((r, c) in b.getOccupiedCells()) {
                if (r in 0 until GRID_SIZE && c in 0 until GRID_SIZE) {
                    grid[r][c] = b.id
                }
            }
        }
        return grid
    }

    /**
     * Returns valid new coordinates (col if horizontal, row if vertical) for a block.
     */
    private fun getPossibleMovesForBlock(block: Block, grid: Array<Array<String?>>): List<Int> {
        val results = mutableListOf<Int>()

        if (block.orientation == Orientation.HORIZONTAL) {
            // Check slide Left
            var c = block.col - 1
            while (c >= 0 && grid[block.row][c] == null) {
                results.add(c)
                c--
            }

            // Check slide Right
            c = block.col + block.length
            while (c < GRID_SIZE && grid[block.row][c] == null) {
                results.add(c - block.length + 1)
                c++
            }
        } else {
            // Check slide Up
            var r = block.row - 1
            while (r >= 0 && grid[r][block.col] == null) {
                results.add(r)
                r--
            }

            // Check slide Down
            r = block.row + block.length
            while (r < GRID_SIZE && grid[r][block.col] == null) {
                results.add(r - block.length + 1)
                r++
            }
        }

        return results
    }

    /**
     * Calculates the minimum and maximum position (col or row) that a block can slide to right now.
     */
    fun getSlidingBounds(block: Block, blocks: List<Block>): Pair<Int, Int> {
        val grid = buildOccupancyGrid(blocks.filter { it.id != block.id })

        var minPos: Int
        var maxPos: Int

        if (block.orientation == Orientation.HORIZONTAL) {
            minPos = block.col
            while (minPos > 0 && grid[block.row][minPos - 1] == null) {
                minPos--
            }

            maxPos = block.col
            while (maxPos + block.length < GRID_SIZE && grid[block.row][maxPos + block.length] == null) {
                maxPos++
            }
        } else {
            minPos = block.row
            while (minPos > 0 && grid[minPos - 1][block.col] == null) {
                minPos--
            }

            maxPos = block.row
            while (maxPos + block.length < GRID_SIZE && grid[maxPos + block.length][block.col] == null) {
                maxPos++
            }
        }

        return Pair(minPos, maxPos)
    }

    private fun encodeState(blocks: List<Block>): String {
        return blocks.sortedBy { it.id }.joinToString(";") { "${it.id}:${it.row},${it.col}" }
    }
}
