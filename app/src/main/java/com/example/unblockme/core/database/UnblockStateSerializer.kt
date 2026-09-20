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
        root.put("bestMoves", state.bestMoves)

        val blocksArr = JSONArray()
        state.blocks.forEach { b ->
            val bObj = JSONObject()
            bObj.put("id", b.id)
            bObj.put("row", b.row)
            bObj.put("col", b.col)
            bObj.put("length", b.length)
            bObj.put("orientation", b.orientation.name)
            bObj.put("isTarget", b.isTarget)
            blocksArr.put(bObj)
        }
        root.put("blocks", blocksArr)

        val histArr = JSONArray()
        state.moveHistory.forEach { m ->
            val mObj = JSONObject()
            mObj.put("blockId", m.blockId)
            mObj.put("fromRow", m.fromRow)
            mObj.put("fromCol", m.fromCol)
            mObj.put("toRow", m.toRow)
            mObj.put("toCol", m.toCol)
            histArr.put(mObj)
        }
        root.put("moveHistory", histArr)

        return root.toString()
    }

    fun deserialize(jsonString: String): UnblockGameState? {
        return try {
            val root = JSONObject(jsonString)
            val levelNumber = root.getInt("levelNumber")
            val difficulty = UnblockDifficulty.valueOf(root.getString("difficulty"))
            val minMoves = root.getInt("minMoves")
            val movesCount = root.getInt("movesCount")
            val isWon = root.getBoolean("isWon")
            val hintsUsed = root.getInt("hintsUsed")
            val elapsedTimeSeconds = root.optInt("elapsedTimeSeconds", 0)
            val starsAwarded = root.optInt("starsAwarded", 0)
            val bestMoves = root.optInt("bestMoves", 0)

            val blocksArr = root.getJSONArray("blocks")
            val blocks = mutableListOf<Block>()
            for (i in 0 until blocksArr.length()) {
                val bObj = blocksArr.getJSONObject(i)
                blocks.add(
                    Block(
                        id = bObj.getString("id"),
                        row = bObj.getInt("row"),
                        col = bObj.getInt("col"),
                        length = bObj.getInt("length"),
                        orientation = Orientation.valueOf(bObj.getString("orientation")),
                        isTarget = bObj.optBoolean("isTarget", false)
                    )
                )
            }

            val histArr = root.getJSONArray("moveHistory")
            val history = mutableListOf<UnblockMove>()
            for (i in 0 until histArr.length()) {
                val mObj = histArr.getJSONObject(i)
                history.add(
                    UnblockMove(
                        blockId = mObj.getString("blockId"),
                        fromRow = mObj.getInt("fromRow"),
                        fromCol = mObj.getInt("fromCol"),
                        toRow = mObj.getInt("toRow"),
                        toCol = mObj.getInt("toCol")
                    )
                )
            }

            UnblockGameState(
                levelNumber = levelNumber,
                difficulty = difficulty,
                blocks = blocks,
                minMoves = minMoves,
                movesCount = movesCount,
                moveHistory = history,
                isWon = isWon,
                hintsUsed = hintsUsed,
                elapsedTimeSeconds = elapsedTimeSeconds,
                starsAwarded = starsAwarded,
                bestMoves = bestMoves
            )
        } catch (_: Exception) {
            null
        }
    }
}
