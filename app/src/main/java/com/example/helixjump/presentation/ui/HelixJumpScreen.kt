package com.example.helixjump.presentation.ui

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helixjump.engine.HelixJumpEngine
import com.example.helixjump.model.SectorType
import com.example.helixjump.presentation.HelixJumpViewModel
import com.zubaluba.gamehub.ads.AdBanner
import com.zubaluba.gamehub.ads.UnifiedAdManager
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelixJumpScreen(
    viewModel: HelixJumpViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val state by viewModel.state.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startGame()
        onDispose {
            viewModel.stopGameLoop()
        }
    }

    LaunchedEffect(state.isGameOver, state.isWon) {
        if ((state.isGameOver || state.isWon) && activity != null) {
            UnifiedAdManager.getInstance(activity).showInterstitial(activity)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Helix Jump",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Level ${state.currentLevel}",
                                color = Color(0xFFFF9800),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⭐ ${state.score}",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "BEST: ${state.bestScore}",
                                color = Color(0xFFB0BEC5),
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("helixjump_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.restart() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF141324))
            )
        },
        bottomBar = {
            AdBanner(modifier = Modifier.fillMaxWidth().navigationBarsPadding())
        },
        containerColor = Color(0xFF0C0B17)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        viewModel.rotateTower(dragAmount.x)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height * 0.42f
                val towerRadius = size.width * 0.35f
                val innerRadius = size.width * 0.12f

                // Draw central pillar
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF23214A), Color(0xFF4C478C), Color(0xFF1B1A3A)),
                        startX = cx - innerRadius,
                        endX = cx + innerRadius
                    ),
                    topLeft = Offset(cx - innerRadius, 0f),
                    size = Size(innerRadius * 2f, size.height)
                )

                // Visible floors around the ball
                val ballY = state.ball.y
                val currentFloorIdx = state.ball.currentFloor

                // Render floors relative to ball camera
                for (floor in state.floors) {
                    val floorYWorld = floor.floorIndex * HelixJumpEngine.FLOOR_SPACING
                    val screenY = cy + (floorYWorld - ballY) * 2.2f

                    // Draw floor if within visible screen bounds
                    if (screenY in -100f..(size.height + 100f)) {
                        val isFinalFloor = floor.floorIndex == HelixJumpEngine.TOTAL_FLOORS - 1

                        // Draw floor sectors (elliptical 3D perspective)
                        val rx = towerRadius
                        val ry = towerRadius * 0.3f

                        for (sector in floor.sectors) {
                            if (sector.type == SectorType.EMPTY) continue

                            val color = when (sector.type) {
                                SectorType.HAZARD -> Color(0xFFE53935)
                                SectorType.FINISH -> Color(0xFF4CAF50)
                                else -> Color(0xFFFF9800)
                            }

                            // Compute visual angle shifted by tower rotation
                            val rotStart = (sector.startAngle + state.towerRotation) % 360f
                            val radStart = Math.toRadians(rotStart.toDouble()).toFloat()
                            val radEnd = Math.toRadians((rotStart + sector.sweepAngle).toDouble()).toFloat()

                            // Check front visibility (facing viewer between 0 and 180 degrees)
                            val midAngle = (rotStart + sector.sweepAngle / 2f) % 360f
                            val isFacingFront = midAngle in 0f..180f

                            val sectorAlpha = if (isFacingFront) 1f else 0.45f

                            val path = Path().apply {
                                val x1 = cx + rx * cos(radStart)
                                val y1 = screenY + ry * sin(radStart)
                                val x2 = cx + rx * cos(radEnd)
                                val y2 = screenY + ry * sin(radEnd)
                                val ix1 = cx + innerRadius * cos(radStart)
                                val iy1 = screenY + (innerRadius * 0.3f) * sin(radStart)
                                val ix2 = cx + innerRadius * cos(radEnd)
                                val iy2 = screenY + (innerRadius * 0.3f) * sin(radEnd)

                                moveTo(ix1, iy1)
                                lineTo(x1, y1)
                                lineTo(x2, y2)
                                lineTo(ix2, iy2)
                                close()
                            }
                            drawPath(path, color = color.copy(alpha = sectorAlpha))
                            drawPath(path, color = Color.White.copy(alpha = 0.2f), style = Stroke(width = 1.5.dp.toPx()))
                        }
                    }
                }

                // Draw bouncing ball at center
                val ballRadius = 14.dp.toPx()
                val ballColor = if (state.ball.isSmashing) Color(0xFFFF1744) else Color(0xFFFFEB3B)

                // Ball drop shadow on floor
                drawOval(
                    color = Color.Black.copy(alpha = 0.35f),
                    topLeft = Offset(cx - ballRadius * 1.2f, cy + ballRadius * 0.6f),
                    size = Size(ballRadius * 2.4f, ballRadius * 0.8f)
                )

                // Ball sphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, ballColor, Color(0xFFE65100)),
                        center = Offset(cx - ballRadius * 0.3f, cy - ballRadius * 0.3f),
                        radius = ballRadius
                    ),
                    radius = ballRadius,
                    center = Offset(cx, cy)
                )

                // Fireball aura if combo
                if (state.ball.isSmashing) {
                    drawCircle(
                        color = Color(0xFFFF5722).copy(alpha = 0.4f),
                        radius = ballRadius * 1.7f,
                        center = Offset(cx, cy)
                    )
                }
            }

            // Streak indicator overlay
            if (state.comboStreak >= 2) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFF1744).copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
                ) {
                    Text(
                        text = "🔥 ${state.comboStreak}X STREAK! SMASH READY!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    // Victory Dialog
    if (state.isWon) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("LEVEL CLEARED! 🏆", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            },
            text = {
                Text("Incredible descent! You completed Level ${state.currentLevel} with ${state.score} points.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.nextLevel() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Next Level", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.restart() }) {
                    Text("Play Again", color = Color.White)
                }
            },
            containerColor = Color(0xFF1B1A3A)
        )
    }

    // Game Over Dialog
    if (state.isGameOver) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("SPLAT! 💥", fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
            },
            text = {
                Text("You landed on a red hazard zone! Score: ${state.score}")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.restart() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Try Again", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF26121C)
        )
    }
}
