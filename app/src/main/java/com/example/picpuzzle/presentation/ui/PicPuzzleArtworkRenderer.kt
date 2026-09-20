package com.example.picpuzzle.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import com.example.picpuzzle.core.model.ArtworkType
import com.example.picpuzzle.core.model.PuzzleTheme

@Composable
fun FullArtworkView(
    theme: PuzzleTheme,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawArtwork(theme, size.width, size.height)
    }
}

@Composable
fun TileArtworkView(
    theme: PuzzleTheme,
    correctPos: Int,
    gridSize: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val tileW = size.width
        val tileH = size.height

        val totalW = tileW * gridSize
        val totalH = tileH * gridSize

        val col = correctPos % gridSize
        val row = correctPos / gridSize

        clipRect(0f, 0f, tileW, tileH) {
            // Translate draw scope so this tile shows its part of the whole artwork
            val offsetX = -col * tileW
            val offsetY = -row * tileH

            translate(left = offsetX, top = offsetY) {
                drawArtwork(theme, totalW, totalH)
            }
        }
    }
}

private fun DrawScope.drawArtwork(theme: PuzzleTheme, w: Float, h: Float) {
    // 1. Base Gradient Background
    drawRect(
        brush = Brush.verticalGradient(
            listOf(theme.primaryColor, theme.secondaryColor)
        ),
        size = Size(w, h)
    )

    when (theme.artworkType) {
        ArtworkType.SUNSET_MOUNTAINS -> {
            // Sun
            drawCircle(
                color = theme.accentColor,
                radius = w * 0.18f,
                center = Offset(w * 0.5f, h * 0.35f)
            )
            // Back mountain
            val m1 = Path().apply {
                moveTo(0f, h)
                lineTo(w * 0.35f, h * 0.45f)
                lineTo(w * 0.75f, h)
                close()
            }
            drawPath(m1, color = theme.secondaryColor.copy(alpha = 0.8f))

            // Front mountain
            val m2 = Path().apply {
                moveTo(w * 0.25f, h)
                lineTo(w * 0.7f, h * 0.52f)
                lineTo(w, h)
                close()
            }
            drawPath(m2, color = Color(0xFF1B263B))
        }

        ArtworkType.OCEAN_ISLAND -> {
            // Sun / Moon
            drawCircle(
                color = theme.accentColor,
                radius = w * 0.15f,
                center = Offset(w * 0.8f, h * 0.25f)
            )
            // Island
            drawCircle(
                color = Color(0xFF2D6A4F),
                radius = w * 0.35f,
                center = Offset(w * 0.3f, h * 0.85f)
            )
            // Waves
            drawRect(
                color = theme.secondaryColor.copy(alpha = 0.6f),
                topLeft = Offset(0f, h * 0.7f),
                size = Size(w, h * 0.3f)
            )
        }

        ArtworkType.CYBERPUNK_CITY -> {
            // Neon buildings skyline
            val buildingCount = 6
            for (i in 0 until buildingCount) {
                val bw = w / buildingCount
                val bh = h * (0.35f + (i % 3) * 0.18f)
                val bx = i * bw
                val by = h - bh

                drawRect(
                    color = Color(0xFF10002B),
                    topLeft = Offset(bx, by),
                    size = Size(bw, bh)
                )
                drawRect(
                    color = theme.accentColor,
                    topLeft = Offset(bx + 4f, by + 4f),
                    size = Size(bw - 8f, 10f)
                )
            }
        }

        ArtworkType.COSMIC_GALAXY -> {
            // Stars and nebula ring
            drawCircle(
                color = theme.accentColor.copy(alpha = 0.4f),
                radius = w * 0.3f,
                center = Offset(w * 0.5f, h * 0.5f)
            )
            drawCircle(
                color = theme.secondaryColor,
                radius = w * 0.12f,
                center = Offset(w * 0.5f, h * 0.5f)
            )
            for (i in 0 until 12) {
                val sx = (w * (0.1f + (i * 0.08f) % 0.8f))
                val sy = (h * (0.15f + (i * 0.13f) % 0.7f))
                drawCircle(color = Color.White, radius = 4f, center = Offset(sx, sy))
            }
        }

        ArtworkType.EMERALD_FOREST -> {
            // Layered pine tree silhouettes
            for (i in 0 until 5) {
                val cx = w * (0.15f + i * 0.18f)
                val treePath = Path().apply {
                    moveTo(cx, h * 0.35f)
                    lineTo(cx - w * 0.12f, h)
                    lineTo(cx + w * 0.12f, h)
                    close()
                }
                drawPath(treePath, color = Color(0xFF1B4332).copy(alpha = 0.9f))
            }
        }

        ArtworkType.DESERT_PYRAMIDS -> {
            // Giant sun
            drawCircle(
                color = theme.accentColor,
                radius = w * 0.22f,
                center = Offset(w * 0.5f, h * 0.3f)
            )
            // Pyramid 1
            val p1 = Path().apply {
                moveTo(w * 0.1f, h)
                lineTo(w * 0.4f, h * 0.5f)
                lineTo(w * 0.75f, h)
                close()
            }
            drawPath(p1, color = Color(0xFFBC6C25))
            // Dunes
            drawCircle(color = Color(0xFFDDA15E), radius = w * 0.6f, center = Offset(w * 0.8f, h * 1.1f))
        }

        ArtworkType.GEOMETRIC_MANDALA -> {
            // Concentric shapes
            drawCircle(color = theme.accentColor, radius = w * 0.35f, center = Offset(w * 0.5f, h * 0.5f))
            drawCircle(color = theme.secondaryColor, radius = w * 0.22f, center = Offset(w * 0.5f, h * 0.5f))
            drawCircle(color = theme.primaryColor, radius = w * 0.1f, center = Offset(w * 0.5f, h * 0.5f))
        }

        ArtworkType.NEON_AURORA -> {
            // Aurora waves
            val wave = Path().apply {
                moveTo(0f, h * 0.4f)
                quadraticBezierTo(w * 0.3f, h * 0.2f, w * 0.6f, h * 0.45f)
                quadraticBezierTo(w * 0.85f, h * 0.6f, w, h * 0.35f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(wave, color = theme.accentColor.copy(alpha = 0.7f))
        }
    }
}
