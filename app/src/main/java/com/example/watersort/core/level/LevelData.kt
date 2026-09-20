package com.example.watersort.core.level

import com.example.watersort.core.model.Bottle
import com.example.watersort.core.model.Difficulty

data class LevelData(
    val levelId: Int,
    val seed: Long,
    val generatorVersion: Int,
    val difficulty: Difficulty,
    val bottles: List<Bottle>,
    val optimalMoves: Int = 0,
    val threeStarTargetMoves: Int = 0,
    val twoStarTargetMoves: Int = 0
)
