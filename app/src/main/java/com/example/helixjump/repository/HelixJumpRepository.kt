package com.example.helixjump.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HelixJumpRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("helixjump_prefs", Context.MODE_PRIVATE)

    private val _bestScore = MutableStateFlow(prefs.getInt("best_score", 0))
    val bestScore: StateFlow<Int> = _bestScore.asStateFlow()

    private val _highestLevel = MutableStateFlow(prefs.getInt("highest_level", 1))
    val highestLevel: StateFlow<Int> = _highestLevel.asStateFlow()

    fun saveScore(score: Int) {
        if (score > _bestScore.value) {
            prefs.edit().putInt("best_score", score).apply()
            _bestScore.value = score
        }
    }

    fun saveLevel(level: Int) {
        if (level > _highestLevel.value) {
            prefs.edit().putInt("highest_level", level).apply()
            _highestLevel.value = level
        }
    }
}
