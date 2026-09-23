package com.example.crossyroad.presentation.ui

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crossyroad.engine.CrossyRoadEngine
import com.example.crossyroad.model.RowType
import com.example.crossyroad.presentation.CrossyRoadViewModel
import com.zubaluba.gamehub.ads.AdBanner
import com.zubaluba.gamehub.ads.UnifiedAdManager
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossyRoadScreen(
    viewModel: CrossyRoadViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val state by viewModel.state.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startGame()
        onDispose {
            viewModel.stopGame()
        }
    }

    LaunchedEffect(state.isGameOver) {
        if (state.isGameOver && activity != null) {
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
                        Text(
                            text = "Crossy Road",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DISTANCE: ${state.score}m",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "BEST: ${state.bestScore}m",
                                color = Color(0xFFB0BEC5),
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("crossyroad_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.restart() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E2832))
            )
        },
        bottomBar = {
            AdBanner(modifier = Modifier.fillMaxWidth().navigationBarsPadding())
        },
        containerColor = Color(0xFF10171E)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Isometric Canvas with Swipes & Tap
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        var totalDx = 0f
                        var totalDy = 0f
                        detectDragGestures(
                            onDragStart = { totalDx = 0f; totalDy = 0f },
                            onDragEnd = {
                                if (abs(totalDx) > abs(totalDy) && abs(totalDx) > 30f) {
                                    if (totalDx > 0) viewModel.hop(1, 0) else viewModel.hop(-1, 0)
                                } else if (abs(totalDy) > 30f) {
                                    if (totalDy < 0) viewModel.hop(0, 1) else viewModel.hop(0, -1)
                                } else {
                                    // Tap to hop forward
                                    viewModel.hop(0, 1)
                                }
                            },
                            onDrag = { _, dragAmount ->
                                totalDx += dragAmount.x
                                totalDy += dragAmount.y
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val rowHeight = h * 0.085f
                    val playerY = state.player.y
                    // Center camera on player
                    val cameraCenterY = h * 0.65f

                    // Draw visible rows
                    for (row in state.rows) {
                        val screenY = cameraCenterY - (row.rowIndex - playerY) * rowHeight

                        if (screenY in -rowHeight..(h + rowHeight)) {
                            // Row Background
                            val rowColor = when (row.type) {
                                RowType.GRASS -> Color(0xFF43A047)
                                RowType.ROAD -> Color(0xFF37474F)
                                RowType.RIVER -> Color(0xFF0288D1)
                                RowType.RAIL -> Color(0xFF5D4037)
                            }
                            drawRect(
                                color = rowColor,
                                topLeft = Offset(0f, screenY),
                                size = Size(w, rowHeight)
                            )

                            // Lane lines for road
                            if (row.type == RowType.ROAD) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.4f),
                                    start = Offset(0f, screenY + rowHeight / 2f),
                                    end = Offset(w, screenY + rowHeight / 2f),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }

                            // Rail tracks & Warning light
                            if (row.type == RowType.RAIL) {
                                drawLine(Color(0xFF8D6E63), Offset(0f, screenY + 4f), Offset(w, screenY + 4f), 3f)
                                drawLine(Color(0xFF8D6E63), Offset(0f, screenY + rowHeight - 4f), Offset(w, screenY + rowHeight - 4f), 3f)
                                if (row.trainWarning) {
                                    // Flashing warning red light at edge
                                    drawCircle(Color.Red, radius = 6.dp.toPx(), center = Offset(16.dp.toPx(), screenY + rowHeight / 2f))
                                }
                            }

                            // Obstacles (Cars, Trucks, Logs, Trains)
                            for (obs in row.obstacles) {
                                val ox = obs.x * w
                                val ow = obs.width * w
                                val oh = rowHeight * 0.7f
                                val oy = screenY + (rowHeight - oh) / 2f

                                when (row.type) {
                                    RowType.ROAD -> {
                                        // Blocky Car
                                        drawRoundRect(
                                            color = if (obs.speed > 0) Color(0xFFE53935) else Color(0xFFFFB300),
                                            topLeft = Offset(ox, oy),
                                            size = Size(ow, oh),
                                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                        )
                                        // Windshield
                                        drawRoundRect(
                                            color = Color(0xFF81D4FA),
                                            topLeft = Offset(ox + ow * 0.25f, oy + 2f),
                                            size = Size(ow * 0.5f, oh * 0.4f),
                                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                        )
                                    }
                                    RowType.RIVER -> {
                                        // Floating Wooden Log
                                        drawRoundRect(
                                            color = Color(0xFF795548),
                                            topLeft = Offset(ox, oy),
                                            size = Size(ow, oh),
                                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                        )
                                    }
                                    RowType.RAIL -> {
                                        // High-speed Train
                                        drawRoundRect(
                                            color = Color(0xFFECEFF1),
                                            topLeft = Offset(ox, oy),
                                            size = Size(ow, oh),
                                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                        )
                                    }
                                    RowType.GRASS -> {}
                                }
                            }
                        }
                    }

                    // Draw Chicken Hopper
                    val colWidth = w / CrossyRoadEngine.COLS
                    val px = state.player.x * colWidth + colWidth / 2f
                    val py = cameraCenterY + rowHeight / 2f
                    val chickenRadius = 14.dp.toPx()

                    // Chicken Blocky Body
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(px - chickenRadius, py - chickenRadius),
                        size = Size(chickenRadius * 2f, chickenRadius * 2f),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    // Yellow Beak
                    drawRoundRect(
                        color = Color(0xFFFFC107),
                        topLeft = Offset(px - 4.dp.toPx(), py - chickenRadius - 5.dp.toPx()),
                        size = Size(8.dp.toPx(), 6.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                    // Red Comb on head
                    drawCircle(
                        color = Color(0xFFD32F2F),
                        radius = 4.dp.toPx(),
                        center = Offset(px, py - chickenRadius + 1.dp.toPx())
                    )
                    // Eyes
                    drawCircle(Color.Black, radius = 2.dp.toPx(), center = Offset(px - 5.dp.toPx(), py - 3.dp.toPx()))
                    drawCircle(Color.Black, radius = 2.dp.toPx(), center = Offset(px + 5.dp.toPx(), py - 3.dp.toPx()))
                }
            }

            // On-screen Buttons for Accessible Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.hop(-1, 0) },
                    modifier = Modifier.size(54.dp).background(Color(0xFF2E7D32), CircleShape)
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = Color.White, modifier = Modifier.size(32.dp))
                }

                Button(
                    onClick = { viewModel.hop(0, 1) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    modifier = Modifier.height(54.dp).padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("HOP FORWARD", fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = { viewModel.hop(1, 0) },
                    modifier = Modifier.size(54.dp).background(Color(0xFF2E7D32), CircleShape)
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }
    }

    // Game Over Dialog
    if (state.isGameOver) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("CRUNCH! 💥", fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
            },
            text = {
                Column {
                    Text(state.causeOfDeath)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Distance: ${state.score} meters", fontWeight = FontWeight.SemiBold)
                    Text("Best: ${state.bestScore} meters", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.restart() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text("Hop Again", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            containerColor = Color(0xFF1E2832)
        )
    }
}
