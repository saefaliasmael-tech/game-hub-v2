package com.example.picpuzzle.core.model

import androidx.compose.ui.graphics.Color

enum class ArtworkType {
    SUNSET_MOUNTAINS,
    OCEAN_ISLAND,
    CYBERPUNK_CITY,
    COSMIC_GALAXY,
    EMERALD_FOREST,
    DESERT_PYRAMIDS,
    GEOMETRIC_MANDALA,
    NEON_AURORA
}

data class PuzzleTheme(
    val id: String,
    val name: String,
    val category: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val artworkType: ArtworkType
)

data class Tile(
    val id: Int,             // 0 to (gridSize*gridSize - 1)
    val correctPos: Int,     // target position
    val currentPos: Int,     // current grid index 0 until gridSize*gridSize
    val isEmpty: Boolean = false
)

data class PicLevelConfig(
    val levelNumber: Int,
    val gridSize: Int = 3,
    val theme: PuzzleTheme,
    val shuffleMoves: Int = 40
)

data class PicGameState(
    val levelNumber: Int = 1,
    val gridSize: Int = 3,
    val tiles: List<Tile> = emptyList(),
    val movesCount: Int = 0,
    val elapsedTimeSeconds: Long = 0L,
    val isSolved: Boolean = false,
    val isPaused: Boolean = false,
    val showNumberHints: Boolean = true,
    val theme: PuzzleTheme = PuzzleTheme(
        id = "sunset",
        name = "Sunset Peaks",
        category = "Nature",
        primaryColor = Color(0xFFFF5964),
        secondaryColor = Color(0xFF35A7FF),
        accentColor = Color(0xFFFFE74C),
        artworkType = ArtworkType.SUNSET_MOUNTAINS
    )
)
