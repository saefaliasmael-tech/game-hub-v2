package com.example.unblockme.core.engine

import com.example.unblockme.core.model.Block
import com.example.unblockme.core.model.Orientation

object UnblockEngine {
    const val GRID_SIZE = 6

    fun canMove(block: Block, delta: Int, allBlocks: List<Block>): Boolean {
        if (delta == 0) return false
        val step = if (delta > 0) 1 else -1
        val steps = kotlin.math.abs(delta)

        var currentRow = block.row
        var currentCol = block.col

        for (s in 1..steps) {
            val nextRow = if (block.orientation == Orientation.VERTICAL) currentRow + step else currentRow
            val nextCol = if (block.orientation == Orientation.HORIZONTAL) currentCol + step else currentCol

            // Check grid bounds
            if (block.orientation == Orientation.HORIZONTAL) {
                if (nextCol < 0 || nextCol + block.length > GRID_SIZE) return false
            } else {
                if (nextRow < 0 || nextRow + block.length > GRID_SIZE) return false
            }

            // Check collision with other blocks
            val testBlock = block.copy(row = nextRow, col = nextCol)
            for (other in allBlocks) {
                if (other.id == block.id) continue
                if (blocksOverlap(testBlock, other)) return false
            }

            currentRow = nextRow
            currentCol = nextCol
        }

        return true
    }

    fun moveBlock(block: Block, delta: Int, allBlocks: List<Block>): List<Block>? {
        if (!canMove(block, delta, allBlocks)) return null

        val updatedBlock = if (block.orientation == Orientation.HORIZONTAL) {
            block.copy(col = block.col + delta)
        } else {
            block.copy(row = block.row + delta)
        }

        return allBlocks.map { if (it.id == block.id) updatedBlock else it }
    }

    fun blocksOverlap(b1: Block, b2: Block): Boolean {
        for (i in 0 until b1.length) {
            val r1 = if (b1.orientation == Orientation.VERTICAL) b1.row + i else b1.row
            val c1 = if (b1.orientation == Orientation.HORIZONTAL) b1.col + i else b1.col
            if (b2.occupies(r1, c1)) return true
        }
        return false
    }

    fun checkWin(blocks: List<Block>): Boolean {
        val target = blocks.find { it.isTarget } ?: return false
        // Win when target block reaches col 4 on row 2 (occupying col 4 and 5)
        return target.row == 2 && target.col >= 4
    }
}
