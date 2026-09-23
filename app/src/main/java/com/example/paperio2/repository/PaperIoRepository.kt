package com.example.paperio2.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PaperIoRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("paperio2_prefs", Context.MODE_PRIVATE)

    private val _bestPercent = MutableStateFlow(prefs.getFloat("best_percent", 0f))
    val bestPercent: StateFlow<Float> = _bestPercent.asStateFlow()

    fun savePercent(percent: Float) {
        if (percent > _bestPercent.value) {
            prefs.edit().putFloat("best_percent", percent).apply()
            _bestPercent.value = percent
        }
    }
}
