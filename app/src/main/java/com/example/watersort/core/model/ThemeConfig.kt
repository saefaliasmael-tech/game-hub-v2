package com.example.watersort.core.model

import androidx.compose.ui.graphics.Color

enum class BottleShapeStyle {
    TUBE,
    FLASK,
    CRYSTAL,
    NEON,
    POTION,
    GOLD
}

data class SkinVisualConfig(
    val id: String,
    val name: String,
    val style: BottleShapeStyle,
    val glassTint: Color,
    val rimColor: Color,
    val strokeColor: Color,
    val glowColor: Color,
    val iconEmoji: String
)

data class ThemeVisualConfig(
    val id: String,
    val name: String,
    val backgroundColors: List<Color>,
    val surfaceColor: Color,
    val cardColor: Color,
    val primaryColor: Color,
    val accentColor: Color,
    val textColor: Color = Color.White,
    val textSecondaryColor: Color = Color(0xFF94A3B8)
)

object ThemeConfig {
    val SKINS = mapOf(
        "classic" to SkinVisualConfig(
            id = "classic",
            name = "Classic Tube",
            style = BottleShapeStyle.TUBE,
            glassTint = Color(0x18FFFFFF),
            rimColor = Color(0xAACBD5E1),
            strokeColor = Color(0x88CBD5E1),
            glowColor = Color(0x6638BDF8),
            iconEmoji = "🧪"
        ),
        "flask" to SkinVisualConfig(
            id = "flask",
            name = "Erlenmeyer Flask",
            style = BottleShapeStyle.FLASK,
            glassTint = Color(0x2038BDF8),
            rimColor = Color(0xFF38BDF8),
            strokeColor = Color(0xAA38BDF8),
            glowColor = Color(0x880284C7),
            iconEmoji = "⚗️"
        ),
        "crystal" to SkinVisualConfig(
            id = "crystal",
            name = "Crystal Vial",
            style = BottleShapeStyle.CRYSTAL,
            glassTint = Color(0x25A7F3D0),
            rimColor = Color(0xFF6EE7B7),
            strokeColor = Color(0xCC6EE7B7),
            glowColor = Color(0x9934D399),
            iconEmoji = "💎"
        ),
        "neon" to SkinVisualConfig(
            id = "neon",
            name = "Neon Glow",
            style = BottleShapeStyle.NEON,
            glassTint = Color(0x25F43F5E),
            rimColor = Color(0xFFFF2E93),
            strokeColor = Color(0xFFFF2E93),
            glowColor = Color(0xCCFF007F),
            iconEmoji = "⚡"
        ),
        "potion" to SkinVisualConfig(
            id = "potion",
            name = "Magic Potion",
            style = BottleShapeStyle.POTION,
            glassTint = Color(0x22C084FC),
            rimColor = Color(0xFFD8B4FE),
            strokeColor = Color(0xFFA855F7),
            glowColor = Color(0x889333EA),
            iconEmoji = "🔮"
        ),
        "gold" to SkinVisualConfig(
            id = "gold",
            name = "Royal Gilded",
            style = BottleShapeStyle.GOLD,
            glassTint = Color(0x25FDE047),
            rimColor = Color(0xFFFACC15),
            strokeColor = Color(0xFFEAB308),
            glowColor = Color(0xAAFFD700),
            iconEmoji = "👑"
        )
    )

    val THEMES = mapOf(
        "classic" to ThemeVisualConfig(
            id = "classic",
            name = "Classic Dark",
            backgroundColors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A)),
            surfaceColor = Color(0xFF1E293B),
            cardColor = Color(0xFF1E293B),
            primaryColor = Color(0xFF0284C7),
            accentColor = Color(0xFF38BDF8)
        ),
        "nature" to ThemeVisualConfig(
            id = "nature",
            name = "Crystal Garden",
            backgroundColors = listOf(Color(0xFF06281C), Color(0xFF0D4730), Color(0xFF051B13)),
            surfaceColor = Color(0xFF113D2D),
            cardColor = Color(0xFF113D2D),
            primaryColor = Color(0xFF059669),
            accentColor = Color(0xFF34D399)
        ),
        "ocean" to ThemeVisualConfig(
            id = "ocean",
            name = "Deep Ocean",
            backgroundColors = listOf(Color(0xFF031A2E), Color(0xFF073656), Color(0xFF021321)),
            surfaceColor = Color(0xFF0B2E48),
            cardColor = Color(0xFF0B2E48),
            primaryColor = Color(0xFF0284C7),
            accentColor = Color(0xFF38BDF8)
        ),
        "volcano" to ThemeVisualConfig(
            id = "volcano",
            name = "Ember Valley",
            backgroundColors = listOf(Color(0xFF240A0A), Color(0xFF451414), Color(0xFF170606)),
            surfaceColor = Color(0xFF3D1212),
            cardColor = Color(0xFF3D1212),
            primaryColor = Color(0xFFDC2626),
            accentColor = Color(0xFFF87171)
        ),
        "space" to ThemeVisualConfig(
            id = "space",
            name = "Cosmic Lab",
            backgroundColors = listOf(Color(0xFF120826), Color(0xFF261047), Color(0xFF0A0417)),
            surfaceColor = Color(0xFF22113D),
            cardColor = Color(0xFF22113D),
            primaryColor = Color(0xFF7C3AED),
            accentColor = Color(0xFFA78BFA)
        ),
        "frozen" to ThemeVisualConfig(
            id = "frozen",
            name = "Frozen Realm",
            backgroundColors = listOf(Color(0xFF082F49), Color(0xFF0C4A6E), Color(0xFF031926)),
            surfaceColor = Color(0xFF0F3E5E),
            cardColor = Color(0xFF0F3E5E),
            primaryColor = Color(0xFF06B6D4),
            accentColor = Color(0xFF67E8F9)
        ),
        "caverns" to ThemeVisualConfig(
            id = "caverns",
            name = "Crystal Caverns",
            backgroundColors = listOf(Color(0xFF2E1065), Color(0xFF4C1D95), Color(0xFF1E0741)),
            surfaceColor = Color(0xFF3B1378),
            cardColor = Color(0xFF3B1378),
            primaryColor = Color(0xFF9333EA),
            accentColor = Color(0xFFC084FC)
        ),
        "shadow" to ThemeVisualConfig(
            id = "shadow",
            name = "Shadow World",
            backgroundColors = listOf(Color(0xFF0B0F17), Color(0xFF1E293B), Color(0xFF06090E)),
            surfaceColor = Color(0xFF1E293B),
            cardColor = Color(0xFF1E293B),
            primaryColor = Color(0xFF64748B),
            accentColor = Color(0xFF94A3B8)
        ),
        "mystic" to ThemeVisualConfig(
            id = "mystic",
            name = "Mystic World",
            backgroundColors = listOf(Color(0xFF2E1A05), Color(0xFF54320A), Color(0xFF1A0E02)),
            surfaceColor = Color(0xFF422607),
            cardColor = Color(0xFF422607),
            primaryColor = Color(0xFFD97706),
            accentColor = Color(0xFFFBBF24)
        )
    )

    fun getSkin(id: String): SkinVisualConfig = SKINS[id] ?: SKINS["classic"]!!
    fun getTheme(id: String): ThemeVisualConfig = THEMES[id] ?: THEMES["classic"]!!
}
