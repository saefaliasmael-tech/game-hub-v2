package com.example.mastermind.core.database

import com.example.mastermind.core.model.*
import org.json.JSONArray
import org.json.JSONObject

object MastermindStateSerializer {

    fun serialize(state: MastermindGameState): String {
        val root = JSONObject()
        root.put("levelNumber", state.levelNumber)
        root.put("mode", state.mode.name)
        root.put("difficulty", state.difficulty.name)
        root.put("maxAttempts", state.maxAttempts)
        root.put("isWon", state.isWon)
        root.put("isGameOver", state.isGameOver)
        root.put("hintsUsed", state.hintsUsed)
        root.put("elapsedTimeSeconds", state.elapsedTimeSeconds)
        root.put("starsAwarded", state.starsAwarded)
        root.put("score", state.score)

        // Secret code
        val secretArray = JSONArray()
        state.secretCode.forEach { secretArray.put(it.name) }
        root.put("secretCode", secretArray)

        // Attempts
        val attemptsArray = JSONArray()
        state.attempts.forEach { row ->
            val rowObj = JSONObject()
            val guessArr = JSONArray()
            row.guess.forEach { guessArr.put(it.name) }
            rowObj.put("guess", guessArr)

            row.feedback?.let { fb ->
                val fbObj = JSONObject()
                fbObj.put("exact", fb.exactMatches)
                fbObj.put("color", fb.colorMatches)
                rowObj.put("feedback", fbObj)
            }
            attemptsArray.put(rowObj)
        }
        root.put("attempts", attemptsArray)

        // Current guess
        val currentGuessArr = JSONArray()
        state.currentGuess.forEach { peg ->
            currentGuessArr.put(peg?.name ?: "NULL")
        }
        root.put("currentGuess", currentGuessArr)

        // Eliminated colors
        val elimArr = JSONArray()
        state.eliminatedColors.forEach { elimArr.put(it.name) }
        root.put("eliminatedColors", elimArr)

        // Revealed positions
        val revealedObj = JSONObject()
        state.revealedPositions.forEach { (pos, color) ->
            revealedObj.put(pos.toString(), color.name)
        }
        root.put("revealedPositions", revealedObj)

        return root.toString()
    }

    fun deserialize(jsonString: String): MastermindGameState? {
        return try {
            val root = JSONObject(jsonString)
            val levelNumber = root.getInt("levelNumber")
            val mode = MastermindGameMode.valueOf(root.getString("mode"))
            val difficulty = MastermindDifficulty.valueOf(root.getString("difficulty"))
            val maxAttempts = root.getInt("maxAttempts")
            val isWon = root.getBoolean("isWon")
            val isGameOver = root.getBoolean("isGameOver")
            val hintsUsed = root.getInt("hintsUsed")
            val elapsedTimeSeconds = root.optInt("elapsedTimeSeconds", 0)
            val starsAwarded = root.optInt("starsAwarded", 0)
            val score = root.optInt("score", 0)

            val secretArr = root.getJSONArray("secretCode")
            val secretCode = mutableListOf<PegColor>()
            for (i in 0 until secretArr.length()) {
                PegColor.entries.find { it.name == secretArr.getString(i) }?.let { secretCode.add(it) }
            }

            val attemptsArr = root.getJSONArray("attempts")
            val attempts = mutableListOf<GuessRow>()
            for (i in 0 until attemptsArr.length()) {
                val rowObj = attemptsArr.getJSONObject(i)
                val guessArr = rowObj.getJSONArray("guess")
                val guess = mutableListOf<PegColor>()
                for (j in 0 until guessArr.length()) {
                    PegColor.entries.find { it.name == guessArr.getString(j) }?.let { guess.add(it) }
                }
                var feedback: Feedback? = null
                if (rowObj.has("feedback")) {
                    val fbObj = rowObj.getJSONObject("feedback")
                    feedback = Feedback(
                        exactMatches = fbObj.getInt("exact"),
                        colorMatches = fbObj.getInt("color")
                    )
                }
                attempts.add(GuessRow(guess, feedback))
            }

            val currentGuessArr = root.getJSONArray("currentGuess")
            val currentGuess = mutableListOf<PegColor?>()
            for (i in 0 until currentGuessArr.length()) {
                val str = currentGuessArr.getString(i)
                if (str == "NULL") {
                    currentGuess.add(null)
                } else {
                    currentGuess.add(PegColor.entries.find { it.name == str })
                }
            }

            val elimArr = root.getJSONArray("eliminatedColors")
            val eliminatedColors = mutableSetOf<PegColor>()
            for (i in 0 until elimArr.length()) {
                PegColor.entries.find { it.name == elimArr.getString(i) }?.let { eliminatedColors.add(it) }
            }

            val revealedObj = root.getJSONObject("revealedPositions")
            val revealedPositions = mutableMapOf<Int, PegColor>()
            val keys = revealedObj.keys()
            while (keys.hasNext()) {
                val posStr = keys.next()
                val colorStr = revealedObj.getString(posStr)
                val color = PegColor.entries.find { it.name == colorStr }
                if (color != null) {
                    revealedPositions[posStr.toInt()] = color
                }
            }

            MastermindGameState(
                levelNumber = levelNumber,
                mode = mode,
                difficulty = difficulty,
                secretCode = secretCode,
                attempts = attempts,
                currentGuess = currentGuess,
                maxAttempts = maxAttempts,
                isWon = isWon,
                isGameOver = isGameOver,
                hintsUsed = hintsUsed,
                eliminatedColors = eliminatedColors,
                revealedPositions = revealedPositions,
                elapsedTimeSeconds = elapsedTimeSeconds,
                starsAwarded = starsAwarded,
                score = score
            )
        } catch (_: Exception) {
            null
        }
    }
}
