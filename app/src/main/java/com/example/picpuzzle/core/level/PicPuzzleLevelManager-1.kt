package com.example.picpuzzle.core.level

import androidx.compose.ui.graphics.Color
import com.example.picpuzzle.core.model.ArtworkType
import com.example.picpuzzle.core.model.PicLevelConfig
import com.example.picpuzzle.core.model.PuzzleTheme

object PicPuzzleLevelManager {

    const val MAX_LEVELS = 100

    private val THEMES = listOf(
        PuzzleTheme(
            id = "sunset",
            name = "Sunset Peaks",
            category = "Landscape",
            primaryColor = Color(0xFFFF5964),
            secondaryColor = Color(0xFF35A7FF),
            accentColor = Color(0xFFFFE74C),
            artworkType = ArtworkType.SUNSET_MOUNTAINS
        ),
        PuzzleTheme(
            id = "ocean",
            name = "Tropical Lagoon",
            category = "Ocean",
            primaryColor = Color(0xFF0077B6),
            secondaryColor = Color(0xFF00B4D8),
            accentColor = Color(0xFFFFD166),
            artworkType = ArtworkType.OCEAN_ISLAND
        ),
        PuzzleTheme(
            id = "cyberpunk",
            name = "Neon Metropolis",
            category = "Sci-Fi",
            primaryColor = Color(0xFF7209B7),
            secondaryColor = Color(0xFF4CC9F0),
            accentColor = Color(0xFFF72585),
            artworkType = ArtworkType.CYBERPUNK_CITY
        ),
        PuzzleTheme(
            id = "galaxy",
            name = "Cosmic Nebula",
            category = "Space",
            primaryColor = Color(0xFF3A0CA3),
            secondaryColor = Color(0xFF4361EE),
            accentColor = Color(0xFF4CC9F0),
            artworkType = ArtworkType.COSMIC_GALAXY
        ),
        PuzzleTheme(
            id = "forest",
            name = "Emerald Grove",
            category = "Nature",
            primaryColor = Color(0xFF2D6A4F),
            secondaryColor = Color(0xFF52B788),
            accentColor = Color(0xFFD8F3DC),
            artworkType = ArtworkType.EMERALD_FOREST
        ),
        PuzzleTheme(
            id = "desert",
            name = "Golden Pyramids",
            category = "Landmarks",
            primaryColor = Color(0xFFD4A373),
            secondaryColor = Color(0xFFE76F51),
            accentColor = Color(0xFFFAEDCD),
            artworkType = ArtworkType.DESERT_PYRAMIDS
        ),
        PuzzleTheme(
            id = "mandala",
            name = "Kaleido Matrix",
            category = "Abstract",
            primaryColor = Color(0xFFE63946),
            secondaryColor = Color(0xFF457B9D),
            accentColor = Color(0xFFA8DADC),
            artworkType = ArtworkType.GEOMETRIC_MANDALA
        ),
        PuzzleTheme(
            id = "aurora",
            name = "Boreal Aurora",
            category = "Atmosphere",
            primaryColor = Color(0xFF06D6A0),
            secondaryColor = Color(0xFF118AB2),
            accentColor = Color(0xFFFFD166),
            artworkType = ArtworkType.NEON_AURORA
        )
    )

    fun getLevel(levelNumber: Int): PicLevelConfig {
        val level = levelNumber.coerceIn(1, MAX_LEVELS)

        val gridSize = when {
            level <= 30 -> 3
            level <= 70 -> 4
            else -> 5
        }

        val theme = THEMES[(level - 1) % THEMES.size]
        val shuffleMoves = 25 + level

        return PicLevelConfig(
            levelNumber = level,
            gridSize = gridSize,
            theme = theme,
            shuffleMoves = shuffleMoves
        )
    }
}
