package com.example.hub.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Immutable metadata model for games registered within the Game Hub.
 * Strictly metadata only: DOES NOT hold any game engine, level state, or currencies.
 */
data class GameInfo(
    val id: String,
    val name: String,
    val description: String,
    val longDescription: String,
    val category: GameCategory,
    val version: String,
    val isAvailable: Boolean,
    val isFeatured: Boolean = false,
    val isNew: Boolean = false,
    val isComingSoon: Boolean = false,
    val primaryColor: Color = Color(0xFF00B4D8),
    val secondaryColor: Color = Color(0xFF03045E),
    val accentColor: Color = Color(0xFF90E0EF),
    val entryRoute: String,
    val order: Int = 0,
    val totalLevelsEstimate: String = "100+",
    val difficultyEstimate: String = "Easy → Hard"
)
