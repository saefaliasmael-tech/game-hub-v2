package com.example.tombofthemask.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TombOfTheMaskRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("tombofthemask_prefs", Context.MODE_PRIVATE)

    private val _highestLevel = MutableStateFlow(prefs.getInt("highest_level", 1))
    val highestLevel: StateFlow<Int> = _highestLevel.asStateFlow()

    fun saveLevel(level: Int) {
        if (level > _highestLevel.value) {
            prefs.edit().putInt("highest_level", level).apply()
            _highestLevel.value = level
        }
    }
}
