package com.example.hub.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Dedicated Hub preferences for storing Hub-level favorites, recently played history,
 * and general platform configurations.
 * Completely separate from any game's internal Room database or save state.
 */
class HubPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _favoritesFlow = MutableStateFlow<Set<String>>(emptySet())
    val favoritesFlow: StateFlow<Set<String>> = _favoritesFlow.asStateFlow()

    private val _recentlyPlayedFlow = MutableStateFlow<List<String>>(emptyList())
    val recentlyPlayedFlow: StateFlow<List<String>> = _recentlyPlayedFlow.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val favs = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
        _favoritesFlow.value = favs

        val recentRaw = prefs.getString(KEY_RECENTLY_PLAYED, "") ?: ""
        val recents = if (recentRaw.isBlank()) {
            emptyList()
        } else {
            recentRaw.split(",").filter { it.isNotBlank() }
        }
        _recentlyPlayedFlow.value = recents
    }

    fun isFavorite(gameId: String): Boolean {
        return _favoritesFlow.value.contains(gameId)
    }

    fun toggleFavorite(gameId: String) {
        val current = _favoritesFlow.value.toMutableSet()
        if (current.contains(gameId)) {
            current.remove(gameId)
        } else {
            current.add(gameId)
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        _favoritesFlow.value = current
    }

    fun recordGamePlayed(gameId: String) {
        val current = _recentlyPlayedFlow.value.toMutableList()
        current.remove(gameId)
        current.add(0, gameId) // Most recent first
        val trimmed = current.take(10)
        prefs.edit().putString(KEY_RECENTLY_PLAYED, trimmed.joinToString(",")).apply()
        _recentlyPlayedFlow.value = trimmed
    }

    fun getMostRecentlyPlayedGameId(): String? {
        return _recentlyPlayedFlow.value.firstOrNull()
    }

    companion object {
        private const val PREFS_NAME = "game_hub_platform_prefs"
        private const val KEY_FAVORITES = "hub_favorites_set"
        private const val KEY_RECENTLY_PLAYED = "hub_recent_games_list"

        @Volatile
        private var INSTANCE: HubPreferences? = null

        fun getInstance(context: Context): HubPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HubPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
