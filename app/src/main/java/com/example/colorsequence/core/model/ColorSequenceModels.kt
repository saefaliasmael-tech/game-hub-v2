package com.example.colorsequence.core.model

import androidx.compose.ui.graphics.Color

enum class SequenceColor(
    val id: String,
    val displayName: String,
    val color: Color,
    val symbol: String,
    val symbolName: String
) {
    RED("RED", "Red", Color(0xFFEF4444), "●", "Circle"),
    GREEN("GREEN", "Green", Color(0xFF22C55E), "■", "Square"),
    BLUE("BLUE", "Blue", Color(0xFF3B82F6), "▲", "Triangle"),
    YELLOW("YELLOW", "Yellow", Color(0xFFEAB308), "★", "Star"),
    ORANGE("ORANGE", "Orange", Color(0xFFF97316), "◆", "Diamond"),
    PURPLE("PURPLE", "Purple", Color(0xFFA855F7), "⬡", "Hexagon"),
    CYAN("CYAN", "Cyan", Color(0xFF06B6D4), "✚", "Cross"),
    PINK("PINK", "Pink", Color(0xFFEC4899), "✦", "Sparkle");

    companion object {
        fun getAvailableColors(level: Int): List<SequenceColor> {
            return when {
                level <= 3 -> listOf(RED, GREEN, BLUE, YELLOW)
                level <= 7 -> listOf(RED, GREEN, BLUE, YELLOW, ORANGE, PURPLE)
                else -> values().toList()
            }
        }
    }
}

enum class GamePhase {
    PREPARING,
    SHOWING_SEQUENCE,
    AWAITING_INPUT,
    LEVEL_WON,
    LEVEL_FAILED
}

data class ColorSequenceLevelConfig(
    val levelNumber: Int,
    val sequenceLength: Int,
    val displayIntervalMs: Long,
    val poolSize: Int,
    val targetStars3TimeMs: Long,
    val targetStars2TimeMs: Long
)

data class ColorSequenceGameState(
    val levelNumber: Int,
    val targetSequence: List<SequenceColor>,
    val availablePool: List<SequenceColor>,
    val playerInput: List<SequenceColor> = emptyList(),
    val currentStepIndex: Int = 0,
    val phase: GamePhase = GamePhase.PREPARING,
    val activeDisplayColor: SequenceColor? = null,
    val activeDisplayIndex: Int = -1,
    val errorsCount: Int = 0,
    val starsAwarded: Int = 0,
    val score: Int = 0,
    val startTimeMs: Long = 0,
    val durationMs: Long = 0
)
