package com.example.pullthepin.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pullthepin.core.engine.PullThePinEngine
import com.example.pullthepin.presentation.PullThePinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullThePinGameScreen(
    viewModel: PullThePinViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val soundEnabled by viewModel.soundEnabledFlow.collectAsState()

    LaunchedEffect(Unit) {
        if (state.balls.isEmpty() && state.pins.isEmpty() && !state.isGameOver && !state.isWon) {
            viewModel.startLevel(state.levelNumber.coerceAtLeast(1))
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPhysicsLoop()
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
                            "Level ${state.levelNumber}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Collected: ${state.collectedCount}/${state.requiredCount}",
                            color = if (state.collectedCount >= state.requiredCount) Color(0xFF10B981) else Color(0xFFFF9F1C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSound() }) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Sound",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { viewModel.restartLevel() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF261C14))
            )
        },
        containerColor = Color(0xFF19120D)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            viewModel.onTouchScreen(
                                offset.x / size.width,
                                offset.y / size.height
                            )
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // 1. Draw Glass Pipe Walls
                for (wall in state.walls) {
                    drawLine(
                        color = Color(0xFF5A3E2B),
                        start = Offset(wall.x1 * w, wall.y1 * h),
                        end = Offset(wall.x2 * w, wall.y2 * h),
                        strokeWidth = 8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Inner highlight
                    drawLine(
                        color = Color(0xFFFFBF69).copy(alpha = 0.4f),
                        start = Offset(wall.x1 * w, wall.y1 * h),
                        end = Offset(wall.x2 * w, wall.y2 * h),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // 2. Draw Collection Bucket
                val b = state.bucket
                val bx = b.x * w
                val by = b.y * h
                val bw = b.width * w
                val bh = b.height * h

                // Bucket background
                drawRect(
                    color = Color(0xFF261C14),
                    topLeft = Offset(bx, by),
                    size = Size(bw, bh)
                )
                // Bucket rim
                drawRect(
                    color = Color(0xFFFF9F1C),
                    topLeft = Offset(bx, by),
                    size = Size(bw, bh),
                    style = Stroke(width = 4.dp.toPx())
                )

                // 3. Draw Pins
                for (pin in state.pins) {
                    if (!pin.isPulled) {
                        val p1x = pin.x1 * w
                        val p1y = pin.y1 * h
                        val p2x = pin.x2 * w
                        val p2y = pin.y2 * h

                        // Pin rod (Steel/Gold)
                        drawLine(
                            color = Color(0xFFFFD166),
                            start = Offset(p1x, p1y),
                            end = Offset(p2x, p2y),
                            strokeWidth = 10.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.6f),
                            start = Offset(p1x, p1y),
                            end = Offset(p2x, p2y),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Pin Pull Ring on the edge
                        val ringCenter = Offset(p1x, p1y)
                        drawCircle(
                            color = Color(0xFFE76F51),
                            radius = 16.dp.toPx(),
                            center = ringCenter
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 8.dp.toPx(),
                            center = ringCenter,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }

                // 4. Draw Bombs
                for (bomb in state.bombs) {
                    if (!bomb.isExploded) {
                        val bpx = bomb.x * w
                        val bpy = bomb.y * h
                        val br = bomb.radius * w

                        drawCircle(color = Color(0xFF222222), radius = br, center = Offset(bpx, bpy))
                        drawCircle(color = Color(0xFFE63946), radius = br * 0.35f, center = Offset(bpx, bpy))
                        // Fuse
                        drawLine(
                            color = Color(0xFFFF9F1C),
                            start = Offset(bpx, bpy - br),
                            end = Offset(bpx + 8.dp.toPx(), bpy - br - 10.dp.toPx()),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }

                // 5. Draw Balls
                val brPx = PullThePinEngine.BALL_RADIUS * w
                for (ball in state.balls) {
                    if (ball.isAlive) {
                        val ballCenter = Offset(ball.x * w, ball.y * h)
                        drawCircle(
                            color = if (ball.isColored) ball.color else Color(0xFF757575),
                            radius = brPx,
                            center = ballCenter
                        )
                        // Specular highlight
                        drawCircle(
                            color = Color.White.copy(alpha = 0.5f),
                            radius = brPx * 0.35f,
                            center = Offset(ballCenter.x - brPx * 0.3f, ballCenter.y - brPx * 0.3f)
                        )
                    }
                }
            }

            // Defeat Dialog
            if (state.isGameOver) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "CHAMBER FAILED!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE63946),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "You collected ${state.collectedCount} colored balls, but this chamber needed ${state.requiredCount}!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.restartLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("TRY AGAIN", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("CHAMBER SELECT", color = Color(0xFFFFBF69))
                        }
                    },
                    containerColor = Color(0xFF261C14)
                )
            }

            // Victory Dialog
            if (state.isWon) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "CASCADE COMPLETE!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF10B981),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "Great physics mastery! All ${state.collectedCount} colored spheres cascaded into the collection vat.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.nextLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("NEXT CHAMBER", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("CHAMBERS MENU", color = Color(0xFFFFBF69))
                        }
                    },
                    containerColor = Color(0xFF261C14)
                )
            }
        }
    }
}
