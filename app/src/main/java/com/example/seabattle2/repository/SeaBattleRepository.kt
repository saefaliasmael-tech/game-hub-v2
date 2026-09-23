package com.example.seabattle2.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SeaBattleRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("seabattle2_prefs", Context.MODE_PRIVATE)

    private val _wins = MutableStateFlow(prefs.getInt("wins", 0))
    val wins: StateFlow<Int> = _wins.asStateFlow()

    private val _losses = MutableStateFlow(prefs.getInt("losses", 0))
    val losses: StateFlow<Int> = _losses.asStateFlow()

    fun recordGame(isWin: Boolean) {
        if (isWin) {
            val w = _wins.value + 1
            prefs.edit().putInt("wins", w).apply()
            _wins.value = w
        } else {
            val l = _losses.value + 1
            prefs.edit().putInt("losses", l).apply()
            _losses.value = l
        }
    }
}
