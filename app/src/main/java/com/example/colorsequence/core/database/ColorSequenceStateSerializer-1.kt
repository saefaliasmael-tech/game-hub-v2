package com.example.colorsequence.core.database

import com.example.colorsequence.core.model.ColorSequenceGameState
import com.example.colorsequence.core.model.GamePhase
import com.example.colorsequence.core.model.SequenceColor
import org.json.JSONArray
import org.json.JSONObject

object ColorSequenceStateSerializer {

    fun serialize(state: ColorSequenceGameState): String {
        val root = JSONObject()
        root.put("levelNumber", state.levelNumber)
        root.put("phase", state.phase.name)
        root.put("currentStepIndex", state.currentStepIndex)
        root.put("errorsCount", state.errorsCount)
        root.put("starsAwarded", state.starsAwarded)
        root.put("score", state.score)
        root.put("startTimeMs", state.startTimeMs)
        root.put("durationMs", state.durationMs)

        val targetSeqArray = JSONArray()
        state.targetSequence.forEach { targetSeqArray.put(it.name) }
        root.put("targetSequence", targetSeqArray)

        val poolArray = JSONArray()
        state.availablePool.forEach { poolArray.put(it.name) }
        root.put("availablePool", poolArray)

        val inputArray = JSONArray()
        state.playerInput.forEach { inputArray.put(it.name) }
        root.put("playerInput", inputArray)

        return root.toString()
    }

    fun deserialize(jsonString: String): ColorSequenceGameState? {
        return try {
            val root = JSONObject(jsonString)
            val levelNumber = root.getInt("levelNumber")
            val phaseStr = root.optString("phase", GamePhase.AWAITING_INPUT.name)
            val phase = try { GamePhase.valueOf(phaseStr) } catch (e: Exception) { GamePhase.AWAITING_INPUT }
            val currentStepIndex = root.getInt("currentStepIndex")
            val errorsCount = root.getInt("errorsCount")
            val starsAwarded = root.getInt("starsAwarded")
            val score = root.getInt("score")
            val startTimeMs = root.optLong("startTimeMs", System.currentTimeMillis())
            val durationMs = root.optLong("durationMs", 0L)

            val targetSeqArray = root.getJSONArray("targetSequence")
            val targetSequence = mutableListOf<SequenceColor>()
            for (i in 0 until targetSeqArray.length()) {
                val colorName = targetSeqArray.getString(i)
                SequenceColor.values().find { it.name == colorName }?.let { targetSequence.add(it) }
            }

            val poolArray = root.getJSONArray("availablePool")
            val availablePool = mutableListOf<SequenceColor>()
            for (i in 0 until poolArray.length()) {
                val colorName = poolArray.getString(i)
                SequenceColor.values().find { it.name == colorName }?.let { availablePool.add(it) }
            }

            val inputArray = root.getJSONArray("playerInput")
            val playerInput = mutableListOf<SequenceColor>()
            for (i in 0 until inputArray.length()) {
                val colorName = inputArray.getString(i)
                SequenceColor.values().find { it.name == colorName }?.let { playerInput.add(it) }
            }

            ColorSequenceGameState(
                levelNumber = levelNumber,
                targetSequence = targetSequence,
                availablePool = availablePool,
                playerInput = playerInput,
                currentStepIndex = currentStepIndex,
                phase = phase,
                errorsCount = errorsCount,
                starsAwarded = starsAwarded,
                score = score,
                startTimeMs = startTimeMs,
                durationMs = durationMs
            )
        } catch (e: Exception) {
            null
        }
    }
}
