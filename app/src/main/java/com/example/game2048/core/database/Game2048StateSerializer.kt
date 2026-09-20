package com.example.game2048.core.database

import com.example.game2048.core.model.Game2048State
import org.json.JSONArray
import org.json.JSONObject

object Game2048StateSerializer {

    fun serialize(state: Game2048State): String {
        val root = JSONObject()
        root.put("score", state.score)
        root.put("bestScore", state.bestScore)
        root.put("isWon", state.isWon)
        root.put("hasContinuedAfterWin", state.hasContinuedAfterWin)
        root.put("isGameOver", state.isGameOver)
        root.put("moveCount", state.moveCount)

        val boardArray = JSONArray()
        state.board.forEach { boardArray.put(it) }
        root.put("board", boardArray)

        return root.toString()
    }

    fun deserialize(jsonString: String): Game2048State? {
        return try {
            val root = JSONObject(jsonString)
            val score = root.getInt("score")
            val bestScore = root.getInt("bestScore")
            val isWon = root.getBoolean("isWon")
            val hasContinuedAfterWin = root.optBoolean("hasContinuedAfterWin", false)
            val isGameOver = root.getBoolean("isGameOver")
            val moveCount = root.optInt("moveCount", 0)

            val boardArray = root.getJSONArray("board")
            val board = mutableListOf<Int>()
            for (i in 0 until boardArray.length()) {
                board.add(boardArray.getInt(i))
            }

            if (board.size != 16) return null

            Game2048State(
                board = board,
                score = score,
                bestScore = bestScore,
                isWon = isWon,
                hasContinuedAfterWin = hasContinuedAfterWin,
                isGameOver = isGameOver,
                moveCount = moveCount
            )
        } catch (e: Exception) {
            null
        }
    }
}
