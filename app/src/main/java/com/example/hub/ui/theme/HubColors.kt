package com.example.hub.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Dark Premium Gaming Design System Color Tokens.
 * Tailored for GAME HUB to provide a modern, futuristic, smooth gaming dashboard experience.
 */
object HubColors {
    // Canvas & Surfaces
    val Void = Color(0xFF0A0918)
    val SurfaceLow = Color(0xFF14122A)
    val SurfaceMid = Color(0xFF1C1936)
    val SurfaceHigh = Color(0xFF272253)

    // Primary Accents
    val PrimaryViolet = Color(0xFF7C5CFF)
    val SoftViolet = Color(0xFFA98BFF)
    val Cyan = Color(0xFF34E3E0)
    val Magenta = Color(0xFFFF4D8D)
    val Lime = Color(0xFFB6FF3C)

    // Typography
    val HighText = Color(0xFFF6F5FF)
    val LowText = Color(0xFF9B97C6)

    // Borders & Overlays
    val Hairline = Color.White.copy(alpha = 0.12f)
    val CardBorder = Color(0xFF7C5CFF).copy(alpha = 0.25f)
    val GlowBorder = Color(0xFF34E3E0).copy(alpha = 0.35f)

    // Gradients
    val HeroGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF6A4BFF), Color(0xFFB06CFF))
    )

    val PlayButtonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF7C5CFF), Color(0xFFA98BFF))
    )

    val CyanGlowGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF34E3E0), Color(0xFF7C5CFF))
    )

    val TopBarGradient = Brush.verticalGradient(
        colors = listOf(Void, SurfaceLow.copy(alpha = 0.95f))
    )

    val BottomNavGradient = Brush.verticalGradient(
        colors = listOf(SurfaceLow.copy(alpha = 0.96f), Void)
    )

    val CardDarkGradient = Brush.verticalGradient(
        colors = listOf(SurfaceMid, SurfaceLow)
    )
}
