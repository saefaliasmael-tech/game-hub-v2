package com.example.picpuzzle.core.engine

import com.example.picpuzzle.core.model.Tile
import kotlin.math.abs
import kotlin.random.Random

object PicPuzzleEngine {

    fun generateBoard(gridSize: Int, shuffleMoves: Int, seed: Long): List<Tile> {
        val total = gridSize * gridSize
        val tiles = (0 until total).map { idx ->
            Tile(
                id = idx,
                correctPos = idx,
                currentPos = idx,
                isEmpty = (idx == total - 1)
            )
        }.toMutableList()

        val random = Random(seed)
        var lastMovedId = -1

        repeat(shuffleMoves) {
            val emptyTile = tiles.first { it.isEmpty }
            val emptyPos = emptyTile.currentPos
            val emptyRow = emptyPos / gridSize
            val emptyCol = emptyPos % gridSize

            val validNeighbors = tiles.filter { tile ->
                if (tile.isEmpty) return@filter false
                val r = tile.currentPos / gridSize
                val c = tile.currentPos % gridSize
                val isAdj = (abs(r - emptyRow) + abs(c - emptyCol)) == 1
                isAdj && tile.id != lastMovedId
            }

            val chosen = if (validNeighbors.isNotEmpty()) {
                validNeighbors.random(random)
            } else {
                tiles.filter { !it.isEmpty && (abs(it.currentPos / gridSize - emptyRow) + abs(it.currentPos % gridSize - emptyCol)) == 1 }.random(random)
            }

            // Swap positions
            val chosenIdx = tiles.indexOf(chosen)
            val emptyIdx = tiles.indexOf(emptyTile)

            val oldChosenPos = chosen.currentPos
            tiles[chosenIdx] = chosen.copy(currentPos = emptyPos)
            tiles[emptyIdx] = emptyTile.copy(currentPos = oldChosenPos)

            lastMovedId = chosen.id
        }

        return tiles
    }

    fun canMove(tile: Tile, emptyTile: Tile, gridSize: Int): Boolean {
        if (tile.isEmpty) return false
        val r1 = tile.currentPos / gridSize
        val c1 = tile.currentPos % gridSize
        val r2 = emptyTile.currentPos / gridSize
        val c2 = emptyTile.currentPos % gridSize
        return (abs(r1 - r2) + abs(c1 - c2)) == 1
    }

    fun slideTile(tiles: List<Tile>, clickedTileId: Int, gridSize: Int): Pair<List<Tile>, Boolean> {
        val clicked = tiles.firstOrNull { it.id == clickedTileId } ?: return tiles to false
        val empty = tiles.firstOrNull { it.isEmpty } ?: return tiles to false

        if (!canMove(clicked, empty, gridSize)) {
            return tiles to false
        }

        val updated = tiles.map {
            when (it.id) {
                clicked.id -> it.copy(currentPos = empty.currentPos)
                empty.id -> it.copy(currentPos = clicked.currentPos)
                else -> it
            }
        }

        return updated to true
    }

    fun isSolved(tiles: List<Tile>): Boolean {
        return tiles.all { it.currentPos == it.correctPos }
    }
}
