package com.example.watersort.core.model

import androidx.compose.ui.graphics.Color

enum class LiquidColor(
    val id: Int,
    val hexColor: Long,
    val symbol: String,
    val colorName: String
) {
    RED(1, 0xFFE53935, "▲", "Red"),
    BLUE(2, 0xFF1E88E5, "●", "Blue"),
    YELLOW(3, 0xFFFDD835, "★", "Yellow"),
    GREEN(4, 0xFF43A047, "◆", "Green"),
    PURPLE(5, 0xFF8E24AA, "✦", "Purple"),
    ORANGE(6, 0xFFFB8C00, "■", "Orange"),
    CYAN(7, 0xFF00ACC1, "▼", "Cyan"),
    PINK(8, 0xFFD81B60, "♥", "Pink"),
    LIME(9, 0xFFC0CA33, "✿", "Lime"),
    TEAL(10, 0xFF00897B, "✶", "Teal"),
    AMBER(11, 0xFFFFB300, "❖", "Amber"),
    INDIGO(12, 0xFF3949AB, "✪", "Indigo");

    fun toComposeColor(): Color = Color(hexColor)

    companion object {
        fun fromId(id: Int): LiquidColor = entries.find { it.id == id } ?: RED
    }
}
