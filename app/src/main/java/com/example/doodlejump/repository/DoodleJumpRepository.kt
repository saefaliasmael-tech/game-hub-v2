package com.example.doodlejump.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DoodleJumpRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("doodlejump_prefs", Context.MODE_PRIVATE)

    private val _bestScore = MutableStateFlow(prefs.getInt("best_score", 0))
    val bestScore: StateFlow<Int> = _bestScore.asStateFlow()

    fun saveScore(score: Int) {
        if (score > _bestScore.value) {
            prefs.edit().putInt("best_score", score).apply()
            _bestScore.value = score
        }
    }
}
