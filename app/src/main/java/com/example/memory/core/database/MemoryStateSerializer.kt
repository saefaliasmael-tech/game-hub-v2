package com.example.memory.core.database

import com.example.memory.core.model.*
import org.json.JSONArray
import org.json.JSONObject

object MemoryStateSerializer {

    fun serialize(state: MemoryGameState): String {
        val root = JSONObject()
        root.put("boardSize", state.boardSize.name)
        root.put("mode", state.mode.name)
        root.put("theme", state.theme.name)
        root.put("movesCount", state.movesCount)
        root.put("matchesCount", state.matchesCount)
        root.put("mistakesCount", state.mistakesCount)
        root.put("timeLimitSeconds", state.timeLimitSeconds)
        root.put("elapsedTimeSeconds", state.elapsedTimeSeconds)
        root.put("maxMoves", state.maxMoves)
        root.put("isWon", state.isWon)
        root.put("isGameOver", state.isGameOver)
        root.put("starsAwarded", state.starsAwarded)
        root.put("score", state.score)

        val cardsArr = JSONArray()
        state.cards.forEach { c ->
            val cObj = JSONObject()
            cObj.put("id", c.id)
            cObj.put("pairId", c.pairId)
            cObj.put("iconName", c.iconName)
            cObj.put("isFlipped", c.isFlipped)
            cObj.put("isMatched", c.isMatched)
            cardsArr.put(cObj)
        }
        root.put("cards", cardsArr)

        return root.toString()
    }

    fun deserialize(jsonString: String): MemoryGameState? {
        return try {
            val root = JSONObject(jsonString)
            val boardSize = MemoryBoardSize.valueOf(root.getString("boardSize"))
            val mode = MemoryGameMode.valueOf(root.getString("mode"))
            val theme = MemoryTheme.valueOf(root.getString("theme"))
            val movesCount = root.getInt("movesCount")
            val matchesCount = root.getInt("matchesCount")
            val mistakesCount = root.getInt("mistakesCount")
            val timeLimitSeconds = root.optInt("timeLimitSeconds", 0)
            val elapsedTimeSeconds = root.optInt("elapsedTimeSeconds", 0)
            val maxMoves = root.optInt("maxMoves", 0)
            val isWon = root.getBoolean("isWon")
            val isGameOver = root.getBoolean("isGameOver")
            val starsAwarded = root.optInt("starsAwarded", 0)
            val score = root.optInt("score", 0)

            val cardsArr = root.getJSONArray("cards")
            val cards = mutableListOf<MemoryCard>()
            for (i in 0 until cardsArr.length()) {
                val cObj = cardsArr.getJSONObject(i)
                cards.add(
                    MemoryCard(
                        id = cObj.getInt("id"),
                        pairId = cObj.getInt("pairId"),
                        iconName = cObj.getString("iconName"),
                        isFlipped = cObj.getBoolean("isFlipped"),
                        isMatched = cObj.getBoolean("isMatched")
                    )
                )
            }

            MemoryGameState(
                boardSize = boardSize,
                mode = mode,
                theme = theme,
                cards = cards,
                movesCount = movesCount,
                matchesCount = matchesCount,
                mistakesCount = mistakesCount,
                timeLimitSeconds = timeLimitSeconds,
                elapsedTimeSeconds = elapsedTimeSeconds,
                maxMoves = maxMoves,
                isWon = isWon,
                isGameOver = isGameOver,
                starsAwarded = starsAwarded,
                score = score
            )
        } catch (_: Exception) {
            null
        }
    }
}
