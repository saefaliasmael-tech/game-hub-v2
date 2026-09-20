package com.example.ropearound.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ropearound.presentation.RopeAroundViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RopeAroundGameScreen(
    viewModel: RopeAroundViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val soundEnabled by viewModel.soundEnabledFlow.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val wrappedCount = state.pegs.count { it.isWrapped }
                    val totalPegs = state.pegs.size
                    Column {
                        Text(
                            text = "Level ${state.levelNumber}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Pegs Wrapped: $wrappedCount / $totalPegs",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (wrappedCount == totalPegs) Color(0xFF06D6A0) else Color(0xFFFF70A6)
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2B2D42))
            )
        },
        containerColor = Color(0xFF1A1B2F)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Interactive Dragging Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                viewModel.onTouchUpdate(offset.x / size.width, offset.y / size.height, true)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                viewModel.onTouchUpdate(change.position.x / size.width, change.position.y / size.height, true)
                            },
                            onDragEnd = {
                                // Keep last position
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            viewModel.onTouchUpdate(offset.x / size.width, offset.y / size.height, false)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // 1. Draw Obstacles (Red zones)
                for (obs in state.obstacles) {
                    val ox = obs.xPercent * w
                    val oy = obs.yPercent * h
                    val r = obs.radiusPercent * w

                    drawCircle(
                        color = Color(0xFFEF476F).copy(alpha = 0.25f),
                        radius = r * 1.2f,
                        center = Offset(ox, oy)
                    )
                    drawCircle(
                        color = Color(0xFFEF476F),
                        radius = r,
                        center = Offset(ox, oy)
                    )
                }

                // 2. Draw Rope
                val allPoints = state.pivotPoints + state.currentTouchPos
                if (allPoints.size >= 2) {
                    val ropePath = Path()
                    ropePath.moveTo(allPoints[0].x * w, allPoints[0].y * h)
                    for (i in 1 until allPoints.size) {
                        ropePath.lineTo(allPoints[i].x * w, allPoints[i].y * h)
                    }

                    // Outer rope glow
                    drawPath(
                        path = ropePath,
                        color = Color(0xFFFF70A6).copy(alpha = 0.35f),
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    // Core rope strand
                    drawPath(
                        path = ropePath,
                        color = Color(0xFFFF70A6),
                        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                // 3. Draw Pegs
                for (peg in state.pegs) {
                    val px = peg.xPercent * w
                    val py = peg.yPercent * h
                    val r = peg.radiusPercent * w

                    // Peg base
                    drawCircle(
                        color = Color(0xFF2B2D42),
                        radius = r,
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = if (peg.isWrapped) Color(0xFF06D6A0) else Color(0xFF8D99AE),
                        radius = r,
                        center = Offset(px, py),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Peg lit center
                    drawCircle(
                        color = if (peg.isWrapped) Color(0xFF06D6A0) else Color(0xFF4A4E69),
                        radius = r * 0.55f,
                        center = Offset(px, py)
                    )
                }

                // 4. Draw Rope Drag Handle
                val hx = state.currentTouchPos.x * w
                val hy = state.currentTouchPos.y * h
                drawCircle(
                    color = Color.White,
                    radius = 12.dp.toPx(),
                    center = Offset(hx, hy)
                )
                drawCircle(
                    color = Color(0xFFFF70A6),
                    radius = 8.dp.toPx(),
                    center = Offset(hx, hy)
                )
            }

            // Defeat Dialog
            if (state.isGameOver) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "ROPE SNAPPED!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFEF476F),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "The rope hit a hazard or ran out of length. Plan a clean wrapping path!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.restartLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF476F)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("RETRY PUZZLE", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("BACK TO LEVELS", color = Color(0xFF8D99AE))
                        }
                    },
                    containerColor = Color(0xFF2B2D42)
                )
            }

            // Victory Dialog
            if (state.isWon) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "ALL PEGS WRAPPED!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF06D6A0),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "Brilliant geometric winding! All pegs are brightly illuminated.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.nextLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06D6A0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("NEXT PUZZLE", fontWeight = FontWeight.Bold, color = Color(0xFF1A1B2F))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("PUZZLES MENU", color = Color(0xFF8D99AE))
                        }
                    },
                    containerColor = Color(0xFF2B2D42)
                )
            }
        }
    }
}
