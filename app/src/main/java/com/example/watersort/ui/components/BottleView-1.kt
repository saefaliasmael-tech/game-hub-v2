package com.example.watersort.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.watersort.core.model.Bottle
import com.example.watersort.core.model.BottleShapeStyle
import com.example.watersort.core.model.LiquidColor
import com.example.watersort.core.model.ThemeConfig
import kotlin.math.sin

@Composable
fun BottleView(
    bottle: Bottle,
    isSelected: Boolean,
    isHighlighted: Boolean = false,
    isShaking: Boolean = false,
    colorBlindMode: Boolean = false,
    skinId: String = "classic",
    tiltAngle: Float = 0f,
    width: Dp = 64.dp,
    height: Dp = 180.dp,
    onBottleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val skin = remember(skinId) { ThemeConfig.getSkin(skinId) }

    val verticalOffset by animateDpAsState(
        targetValue = if (isSelected) (-28).dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "bottle_lift"
    )

    val shakeOffset by animateFloatAsState(
        targetValue = if (isShaking) 8f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = 1200f),
        label = "bottle_shake"
    )

    val rotation by animateFloatAsState(
        targetValue = tiltAngle,
        animationSpec = tween(durationMillis = 280),
        label = "bottle_tilt"
    )

    val waveTransition = rememberInfiniteTransition(label = "liquid_wave")
    val wavePhase by waveTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831855f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val sparkleScale by waveTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_scale"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isSolvedBottle = bottle.isSolved && !bottle.isEmpty

    Box(
        modifier = modifier
            .offset(x = shakeOffset.dp, y = verticalOffset)
            .graphicsLayer {
                rotationZ = rotation
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.1f)
            }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 40.dp),
                onClick = onBottleClick
            )
            .semantics {
                contentDescription = "Bottle ${bottle.id + 1}, contains ${bottle.layers.size} layers"
            }
            .testTag("bottle_${bottle.id}"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .width(width)
                .height(height)
        ) {
            val w = size.width
            val h = size.height
            val strokeW = when (skin.style) {
                BottleShapeStyle.NEON -> 5.dp.toPx()
                BottleShapeStyle.GOLD -> 4.5.dp.toPx()
                else -> 4.dp.toPx()
            }
            val cornerR = when (skin.style) {
                BottleShapeStyle.CRYSTAL -> 8.dp.toPx()
                BottleShapeStyle.POTION -> 32.dp.toPx()
                else -> 24.dp.toPx()
            }

            // Bottle interior clip path based on skin shape style
            val interiorPath = Path().apply {
                if (skin.style == BottleShapeStyle.FLASK) {
                    val neckW = w * 0.42f
                    val neckH = h * 0.28f
                    val neckLeft = (w - neckW) / 2f
                    val neckRight = neckLeft + neckW

                    moveTo(neckLeft, strokeW + 8.dp.toPx())
                    lineTo(neckRight, strokeW + 8.dp.toPx())
                    lineTo(neckRight, neckH)
                    lineTo(w - strokeW, h - strokeW - 12.dp.toPx())
                    quadraticTo(w - strokeW, h - strokeW, w - strokeW - 12.dp.toPx(), h - strokeW)
                    lineTo(strokeW + 12.dp.toPx(), h - strokeW)
                    quadraticTo(strokeW, h - strokeW, strokeW, h - strokeW - 12.dp.toPx())
                    lineTo(neckLeft, neckH)
                    close()
                } else {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(
                                left = strokeW,
                                top = strokeW + 8.dp.toPx(),
                                right = w - strokeW,
                                bottom = h - strokeW
                            ),
                            bottomLeft = CornerRadius(cornerR, cornerR),
                            bottomRight = CornerRadius(cornerR, cornerR),
                            topLeft = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                            topRight = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    )
                }
            }

            // Draw selection / highlight / solved aura
            if (isSelected || isHighlighted || isSolvedBottle) {
                val auraColor = when {
                    isSolvedBottle -> Color(0x77FACC15)
                    isSelected -> skin.glowColor
                    else -> Color(0x66FACC15)
                }
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(auraColor, Color.Transparent),
                        center = Offset(w / 2, h / 2),
                        radius = w * 0.95f
                    ),
                    topLeft = Offset(-10.dp.toPx(), -10.dp.toPx()),
                    size = Size(w + 20.dp.toPx(), h + 20.dp.toPx()),
                    cornerRadius = CornerRadius(cornerR + 8.dp.toPx())
                )
            }

            // Draw glass background with soft transparency & tint
            drawPath(
                path = interiorPath,
                color = skin.glassTint
            )

            // Draw liquid layers inside clipped glass path
            clipPath(interiorPath) {
                val layerCount = bottle.capacity
                val usableHeight = h - (strokeW * 2) - 8.dp.toPx()
                val layerHeight = usableHeight / layerCount

                bottle.layers.forEachIndexed { index, liquidColor ->
                    val layerBottom = h - strokeW - (index * layerHeight)
                    val layerTop = layerBottom - layerHeight

                    val isTopLiquid = (index == bottle.layers.size - 1)
                    val baseColor = liquidColor.toComposeColor()
                    val topSurfaceColor = baseColor.copy(alpha = 0.95f)
                    val bottomColor = baseColor.copy(alpha = 0.82f)

                    // Layer body gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(topSurfaceColor, bottomColor),
                            startY = layerTop,
                            endY = layerBottom
                        ),
                        topLeft = Offset(strokeW, layerTop),
                        size = Size(w - (strokeW * 2), layerHeight)
                    )

                    // Dynamic wave on top layer
                    val waveAmp = if (isTopLiquid) (2.dp.toPx() * sin(wavePhase)).coerceIn(-2.5.dp.toPx(), 2.5.dp.toPx()) else 0f
                    val meniscusPath = Path().apply {
                        moveTo(strokeW, layerTop)
                        quadraticTo(
                            x1 = w / 2,
                            y1 = layerTop + 3.dp.toPx() + waveAmp,
                            x2 = w - strokeW,
                            y2 = layerTop
                        )
                    }
                    drawPath(
                        path = meniscusPath,
                        color = Color(0x66FFFFFF),
                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Color blind indicator icon / pattern
                    if (colorBlindMode) {
                        drawIntoCanvas {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textAlign = android.graphics.Paint.Align.CENTER
                                textSize = 16.dp.toPx()
                                isAntiAlias = true
                                setShadowLayer(4f, 0f, 2f, android.graphics.Color.BLACK)
                            }
                            val textY = layerTop + (layerHeight / 2) + (paint.textSize / 3)
                            drawContext.canvas.nativeCanvas.drawText(
                                liquidColor.symbol,
                                w / 2,
                                textY,
                                paint
                            )
                        }
                    }
                }
            }

            // Draw outer neon glow if skin is NEON
            if (skin.style == BottleShapeStyle.NEON) {
                drawPath(
                    path = interiorPath,
                    color = skin.glowColor,
                    style = Stroke(width = strokeW + 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Draw glass bottle outline
            val outlineColor = when {
                isSolvedBottle -> Color(0xFFFDE047)
                isSelected -> skin.glowColor
                else -> skin.strokeColor
            }
            drawPath(
                path = interiorPath,
                color = outlineColor,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )

            // Bottle rim / lip at top
            drawRoundRect(
                color = if (isSolvedBottle) Color(0xFFFACC15) else if (isSelected) skin.glowColor else skin.rimColor,
                topLeft = Offset(0f, 0f),
                size = Size(w, 8.dp.toPx()),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )

            // Glass specular reflection highlight (vertical glossy sheen)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x66FFFFFF), Color(0x10FFFFFF)),
                    startY = 14.dp.toPx(),
                    endY = h - 20.dp.toPx()
                ),
                topLeft = Offset(8.dp.toPx(), 14.dp.toPx()),
                size = Size(3.5.dp.toPx(), h - 36.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx())
            )

            // Special gold rim filigree for GOLD skin
            if (skin.style == BottleShapeStyle.GOLD) {
                drawRoundRect(
                    color = Color(0xFFFACC15),
                    topLeft = Offset(w * 0.2f, 10.dp.toPx()),
                    size = Size(w * 0.6f, 2.dp.toPx()),
                    cornerRadius = CornerRadius(1.dp.toPx())
                )
            }

            // Solved celebratory sparkle star
            if (isSolvedBottle) {
                drawCircle(
                    color = Color(0xFFFDE047).copy(alpha = 0.85f),
                    radius = 4.dp.toPx() * sparkleScale,
                    center = Offset(w * 0.8f, 16.dp.toPx())
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx() * sparkleScale,
                    center = Offset(w * 0.8f, 16.dp.toPx())
                )
            }
        }
    }
}
