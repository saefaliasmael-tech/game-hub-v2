package com.example.colorswitch.presentation.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorswitch.core.model.*
import com.example.colorswitch.presentation.ColorSwitchViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSwitchGameScreen(
    viewModel: ColorSwitchViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val colorBlind by viewModel.colorBlindFlow.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (state.mode == ColorSwitchGameMode.CAMPAIGN) "Level ${state.levelNumber}" else state.mode.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD600),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${state.score}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF18181B)
                )
            )
        },
        containerColor = Color(0xFF09090B)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTapGestures {
                        viewModel.onTap()
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val centerY = size.height * 0.75f - state.cameraY

                // Draw Finish Portal (Campaign)
                if (state.mode == ColorSwitchGameMode.CAMPAIGN) {
                    val finishY = centerY - (state.obstacles.size * 280f) - 100f
                    drawCircle(
                        color = Color(0xFF10B981).copy(alpha = 0.3f),
                        radius = 45f,
                        center = Offset(centerX, finishY)
                    )
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 25f,
                        center = Offset(centerX, finishY),
                        style = Stroke(width = 6f)
                    )
                }

                // Draw Obstacles
                state.obstacles.forEach { obs ->
                    val obsScreenY = centerY + obs.centerY
                    drawObstacle(obs, centerX, obsScreenY, colorBlind)
                }

                // Draw Particles
                state.particles.forEach { p ->
                    drawCircle(
                        color = p.color.copy(alpha = p.alpha),
                        radius = p.size,
                        center = Offset(centerX + p.x, centerY + p.y)
                    )
                }

                // Draw Ball
                val ballScreenY = centerY + state.ball.y
                drawCircle(
                    color = state.ball.color.color,
                    radius = state.ball.radius,
                    center = Offset(centerX, ballScreenY)
                )

                // Draw Symbol inside ball if color blind mode is on
                if (colorBlind) {
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 28f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                        drawText(state.ball.color.symbolChar, centerX, ballScreenY + 10f, paint)
                    }
                }
            }

            // Start Prompt
            if (!state.isStarted && !state.isGameOver && !state.isWon) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        color = Color(0xFF27272A).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            "TAP TO JUMP",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }

    // Game Over Dialog
    if (state.isGameOver) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF27272A),
            title = {
                Text(
                    "GAME OVER",
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = Color(0xFFFF007F),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Stars Collected: ${state.score}", color = Color.White, fontSize = 16.sp)
                    Text("Best Score: ${viewModel.getBestScore()}", color = Color(0xFFA1A1AA), fontSize = 14.sp)
                }
            },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = onNavigateBack, shape = RoundedCornerShape(10.dp)) {
                        Text("Menu", color = Color.White)
                    }
                    Button(
                        onClick = { viewModel.restartCurrentGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Try Again", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // Win Dialog
    if (state.isWon) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF27272A),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("STAGE CLEARED!", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFF10B981))
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.Center) {
                        repeat(3) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Stars Collected: ${state.score}", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = onNavigateBack, shape = RoundedCornerShape(10.dp)) {
                        Text("Menu", color = Color.White)
                    }
                    Button(
                        onClick = { viewModel.nextLevel() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Next Level", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}

private fun DrawScope.drawObstacle(
    obs: ObstacleState,
    cx: Float,
    cy: Float,
    colorBlind: Boolean
) {
    val r = obs.radius
    val thickness = 18f

    when (obs.type) {
        ObstacleType.ROTATING_CIRCLE -> {
            val colors = SwitchColor.ALL
            for (i in 0 until 4) {
                val startAngle = obs.currentAngle + i * 90f
                drawArc(
                    color = colors[i].color,
                    startAngle = startAngle,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(width = thickness)
                )

                if (colorBlind) {
                    val midAngle = Math.toRadians((startAngle + 45f).toDouble())
                    val textX = cx + (r * cos(midAngle)).toFloat()
                    val textY = cy + (r * sin(midAngle)).toFloat()
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 22f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawText(colors[i].symbolChar, textX, textY + 7f, paint)
                    }
                }
            }
        }
        ObstacleType.CONCENTRIC_RINGS -> {
            // Outer ring
            val colors = SwitchColor.ALL
            for (i in 0 until 4) {
                drawArc(
                    color = colors[i].color,
                    startAngle = obs.currentAngle + i * 90f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(width = thickness)
                )
            }
            // Inner ring rotating opposite
            val innerR = r * 0.65f
            for (i in 0 until 4) {
                drawArc(
                    color = colors[i].color,
                    startAngle = -obs.currentAngle + i * 90f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx - innerR, cy - innerR),
                    size = Size(innerR * 2, innerR * 2),
                    style = Stroke(width = thickness * 0.8f)
                )
            }
        }
        ObstacleType.ROTATING_CROSS -> {
            val colors = SwitchColor.ALL
            for (i in 0 until 4) {
                val angle = Math.toRadians((obs.currentAngle + i * 90f).toDouble())
                val endX = cx + (r * cos(angle)).toFloat()
                val endY = cy + (r * sin(angle)).toFloat()
                drawLine(
                    color = colors[i].color,
                    start = Offset(cx, cy),
                    end = Offset(endX, endY),
                    strokeWidth = thickness
                )
            }
        }
        else -> {
            val colors = SwitchColor.ALL
            for (i in 0 until 4) {
                drawArc(
                    color = colors[i].color,
                    startAngle = obs.currentAngle + i * 90f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(width = thickness)
                )
            }
        }
    }

    // Draw Star
    if (obs.hasStar && !obs.isStarCollected) {
        drawCircle(color = Color(0xFFFFD600), radius = 12f, center = Offset(cx, cy))
        drawCircle(color = Color.White, radius = 5f, center = Offset(cx, cy))
    }

    // Draw Color Switch Orb
    if (obs.hasColorOrb && !obs.isColorOrbCollected) {
        val orbY = cy - r - 40f
        val orbR = 14f
        val colors = SwitchColor.ALL
        for (i in 0 until 4) {
            drawArc(
                color = colors[i].color,
                startAngle = i * 90f,
                sweepAngle = 90f,
                useCenter = true,
                topLeft = Offset(cx - orbR, orbY - orbR),
                size = Size(orbR * 2, orbR * 2)
            )
        }
    }
}
