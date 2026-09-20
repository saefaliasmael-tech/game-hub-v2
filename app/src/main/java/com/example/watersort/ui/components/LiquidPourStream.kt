package com.example.watersort.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.watersort.core.model.LiquidColor
import kotlin.math.sin

@Composable
fun LiquidPourStream(
    fromOffset: Offset,
    toOffset: Offset,
    color: LiquidColor,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "stream_anim")
    val flowPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831855f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flow_phase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val baseColor = color.toComposeColor()
        val highlightColor = baseColor.copy(alpha = 0.95f)

        // Control point for smooth parabolic arc
        val midX = (fromOffset.x + toOffset.x) / 2f
        val arcApexY = minOf(fromOffset.y, toOffset.y) - 30.dp.toPx()

        val streamPath = Path().apply {
            moveTo(fromOffset.x, fromOffset.y)
            quadraticTo(
                x1 = midX,
                y1 = arcApexY,
                x2 = toOffset.x,
                y2 = toOffset.y
            )
        }

        // Outer glow
        drawPath(
            path = streamPath,
            color = baseColor.copy(alpha = 0.45f),
            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
        )

        // Main liquid core stream
        drawPath(
            path = streamPath,
            brush = Brush.verticalGradient(
                colors = listOf(highlightColor, baseColor),
                startY = arcApexY,
                endY = toOffset.y
            ),
            style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
        )

        // Inner specular highlight
        drawPath(
            path = streamPath,
            color = Color.White.copy(alpha = 0.65f),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Target splash ripple
        val rippleRadius = 8.dp.toPx() + (sin(flowPhase) * 3.dp.toPx())
        drawCircle(
            color = baseColor.copy(alpha = 0.7f),
            radius = rippleRadius,
            center = toOffset
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = rippleRadius * 0.4f,
            center = toOffset
        )

        // Micro-droplets falling along stream
        repeat(3) { i ->
            val t = ((flowPhase / 6.28f) + (i * 0.33f)) % 1f
            val dropX = (1 - t) * (1 - t) * fromOffset.x + 2 * (1 - t) * t * midX + t * t * toOffset.x
            val dropY = (1 - t) * (1 - t) * fromOffset.y + 2 * (1 - t) * t * arcApexY + t * t * toOffset.y
            drawCircle(
                color = baseColor.copy(alpha = 0.85f),
                radius = 3.dp.toPx(),
                center = Offset(dropX + sin(t * 10f) * 4f, dropY)
            )
        }
    }
}
