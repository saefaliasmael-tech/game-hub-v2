package com.example.watersort.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class MascotMood {
    IDLE,
    HAPPY,
    CELEBRATING
}

/**
 * Animated Vector Mascot "Aqua" — A lively water-spirit companion.
 * Has organic bobbing, eye blinking, smiling expression, specular reflections,
 * and decorative ambient water particles.
 */
@Composable
fun WaterSortMascot(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    mood: MascotMood = MascotMood.IDLE
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_anim")

    // Bobbing float
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_float"
    )

    // Breathing squish
    val scaleY by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_scale_y"
    )

    // Eye blinking loop
    val blinkProgress by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mascot_blink"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val centerX = w / 2f
            val centerY = (h / 2f) + floatOffset

            // 1. Soft Shadow on the ground
            val shadowWidth = w * 0.55f * (2f - scaleY)
            val shadowHeight = h * 0.12f * (2f - scaleY)
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x550284C7), Color.Transparent),
                    center = Offset(centerX, h * 0.92f),
                    radius = shadowWidth / 2f
                ),
                topLeft = Offset(centerX - shadowWidth / 2f, h * 0.92f - shadowHeight / 2f),
                size = Size(shadowWidth, shadowHeight)
            )

            // 2. Teardrop body path
            val bodyWidth = w * 0.68f
            val bodyHeight = h * 0.72f * scaleY
            val topX = centerX
            val topY = centerY - (bodyHeight * 0.55f)
            val bottomY = centerY + (bodyHeight * 0.45f)

            val bodyPath = Path().apply {
                moveTo(topX, topY)
                // Right curve down
                cubicTo(
                    topX + bodyWidth * 0.35f, topY + bodyHeight * 0.35f,
                    centerX + bodyWidth * 0.5f, bottomY - bodyHeight * 0.2f,
                    centerX + bodyWidth * 0.5f, bottomY - bodyHeight * 0.15f
                )
                // Bottom curve
                cubicTo(
                    centerX + bodyWidth * 0.5f, bottomY + bodyHeight * 0.1f,
                    centerX - bodyWidth * 0.5f, bottomY + bodyHeight * 0.1f,
                    centerX - bodyWidth * 0.5f, bottomY - bodyHeight * 0.15f
                )
                // Left curve back to top
                cubicTo(
                    centerX - bodyWidth * 0.5f, bottomY - bodyHeight * 0.2f,
                    topX - bodyWidth * 0.35f, topY + bodyHeight * 0.35f,
                    topX, topY
                )
                close()
            }

            // Body Gradient Fill (Shining Crystal Cyan & Azure)
            val bodyBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF67E8F9), // Light cyan top
                    Color(0xFF38BDF8), // Vibrant blue
                    Color(0xFF0284C7)  // Deep ocean azure
                ),
                startY = topY,
                endY = bottomY
            )
            drawPath(path = bodyPath, brush = bodyBrush, style = Fill)

            // Body Outline Rim
            drawPath(
                path = bodyPath,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFE0F2FE), Color(0xFF0369A1))
                ),
                style = Stroke(width = w * 0.035f)
            )

            // Specular Top-Left Highlight
            val highlightPath = Path().apply {
                moveTo(topX - bodyWidth * 0.12f, topY + bodyHeight * 0.15f)
                cubicTo(
                    topX - bodyWidth * 0.28f, topY + bodyHeight * 0.3f,
                    centerX - bodyWidth * 0.35f, centerY,
                    centerX - bodyWidth * 0.35f, centerY + bodyHeight * 0.1f
                )
            }
            drawPath(
                path = highlightPath,
                color = Color.White.copy(alpha = 0.55f),
                style = Stroke(width = w * 0.045f)
            )

            // 3. Eyes
            val eyeSpacing = bodyWidth * 0.24f
            val eyeY = centerY - (bodyHeight * 0.02f)
            val eyeRadius = w * 0.055f

            // Left eye
            drawCircle(
                color = Color(0xFF082F49),
                radius = eyeRadius,
                center = Offset(centerX - eyeSpacing, eyeY)
            )
            // Left eye specular dot
            drawCircle(
                color = Color.White,
                radius = eyeRadius * 0.45f,
                center = Offset(centerX - eyeSpacing - eyeRadius * 0.25f, eyeY - eyeRadius * 0.25f)
            )

            // Right eye
            drawCircle(
                color = Color(0xFF082F49),
                radius = eyeRadius,
                center = Offset(centerX + eyeSpacing, eyeY)
            )
            // Right eye specular dot
            drawCircle(
                color = Color.White,
                radius = eyeRadius * 0.45f,
                center = Offset(centerX + eyeSpacing - eyeRadius * 0.25f, eyeY - eyeRadius * 0.25f)
            )

            // 4. Rosy Cheeks
            val cheekY = eyeY + eyeRadius * 1.5f
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0x88F472B6), Color.Transparent),
                    center = Offset(centerX - eyeSpacing - eyeRadius * 0.8f, cheekY),
                    radius = eyeRadius * 0.9f
                ),
                radius = eyeRadius * 0.9f,
                center = Offset(centerX - eyeSpacing - eyeRadius * 0.8f, cheekY)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0x88F472B6), Color.Transparent),
                    center = Offset(centerX + eyeSpacing + eyeRadius * 0.8f, cheekY),
                    radius = eyeRadius * 0.9f
                ),
                radius = eyeRadius * 0.9f,
                center = Offset(centerX + eyeSpacing + eyeRadius * 0.8f, cheekY)
            )

            // 5. Smiling Mouth
            val mouthY = eyeY + eyeRadius * 1.6f
            val mouthPath = Path().apply {
                moveTo(centerX - eyeRadius * 1.1f, mouthY)
                quadraticTo(
                    centerX, mouthY + (eyeRadius * 1.5f),
                    centerX + eyeRadius * 1.1f, mouthY
                )
            }
            drawPath(
                path = mouthPath,
                color = Color(0xFF082F49),
                style = Stroke(width = w * 0.035f)
            )
        }
    }
}
