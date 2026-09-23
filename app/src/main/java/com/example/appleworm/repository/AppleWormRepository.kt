package com.example.appleworm.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppleWormRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("appleworm_prefs", Context.MODE_PRIVATE)

    private val _highestCompletedLevel = MutableStateFlow(prefs.getInt("highest_completed_level", 0))
    val highestCompletedLevel: StateFlow<Int> = _highestCompletedLevel.asStateFlow()

    fun markLevelCompleted(level: Int) {
        val currentMax = _highestCompletedLevel.value
        if (level > currentMax) {
            prefs.edit().putInt("highest_completed_level", level).apply()
            _highestCompletedLevel.value = level
        }
    }
}
