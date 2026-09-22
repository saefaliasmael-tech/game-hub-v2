package com.example.unblockme.core.database

import com.example.unblockme.core.model.*
import org.json.JSONArray
import org.json.JSONObject

object UnblockStateSerializer {

    fun serialize(state: UnblockGameState): String {
        val root = JSONObject()
        root.put("levelNumber", state.levelNumber)
        root.put("difficulty", state.difficulty.name)
        root.put("minMoves", state.minMoves)
        root.put("movesCount", state.movesCount)
        root.put("isWon", state.isWon)
        root.put("hintsUsed", state.hintsUsed)
        root.put("elapsedTimeSeconds", state.elapsedTimeSeconds)
        root.put("starsAwarded", state.starsAwarded)

        val blocksArray = JSONArray()
        for (b in state.blocks) {
            val bo = JSONObject()
            bo.put("id", b.id)
            bo.put("row", b.row)
            bo.put("col", b.col)
            bo.put("length", b.length)
            bo.put("orientation", b.orientation.name)
            bo.put("isTarget", b.isTarget)
            blocksArray.put(bo)
        }
        root.put("blocks", blocksArray)

        return root.toString()
    }

    fun deserialize(json: String): UnblockGameState? {
        return try {
            val root = JSONObject(json)
            val levelNumber = root.optInt("levelNumber", 1)
            val diffName = root.optString("difficulty", "EASY")
            val difficulty = try { UnblockDifficulty.valueOf(diffName) } catch (_: Exception) { UnblockDifficulty.EASY }
            val minMoves = root.optInt("minMoves", 0)
            val movesCount = root.optInt("movesCount", 0)
            val isWon = root.optBoolean("isWon", false)
            val hintsUsed = root.optInt("hintsUsed", 0)
            val elapsedTimeSeconds = root.optInt("elapsedTimeSeconds", 0)
            val starsAwarded = root.optInt("starsAwarded", 0)

            val blocksArray = root.optJSONArray("blocks") ?: JSONArray()
            val blocks = mutableListOf<Block>()
            for (i in 0 until blocksArray.length()) {
                val bo = blocksArray.getJSONObject(i)
                val id = bo.getString("id")
                val row = bo.getInt("row")
                val col = bo.getInt("col")
                val length = bo.getInt("length")
                val ori = Orientation.valueOf(bo.getString("orientation"))
                val isTarget = bo.optBoolean("isTarget", false)
                blocks.add(Block(id, row, col, length, ori, isTarget))
            }

            UnblockGameState(
                levelNumber = levelNumber,
                difficulty = difficulty,
                blocks = blocks,
                minMoves = minMoves,
                movesCount = movesCount,
                isWon = isWon,
                hintsUsed = hintsUsed,
                elapsedTimeSeconds = elapsedTimeSeconds,
                starsAwarded = starsAwarded
            )
        } catch (_: Exception) {
            null
        }
    }
}
