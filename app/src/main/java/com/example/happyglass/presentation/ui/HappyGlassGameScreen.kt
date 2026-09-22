package com.example.happyglass.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.happyglass.core.engine.HappyGlassEngine
import com.example.happyglass.presentation.HappyGlassViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HappyGlassGameScreen(
    viewModel: HappyGlassViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val soundEnabled by viewModel.soundEnabledFlow.collectAsState()

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
                            "Water: ${state.waterInGlassCount}/${state.glass.requiredWater}",
                            color = if (state.isGlassHappy) Color(0xFF38BDF8) else Color(0xFFFFD166),
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
                    IconButton(onClick = { viewModel.clearDrawing() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F2B48))
            )
        },
        containerColor = Color(0xFF0A192F)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Ink gauge bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INK",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(state.inkRemainingRatio)
                            .background(
                                when {
                                    state.inkRemainingRatio >= 0.60f -> Color(0xFF38BDF8)
                                    state.inkRemainingRatio >= 0.25f -> Color(0xFFFFD166)
                                    else -> Color(0xFFEF4444)
                                }
                            )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row {
                    val stars = HappyGlassEngine.calculateStars(state.inkRemainingRatio)
                    repeat(3) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < stars) Color(0xFFFFD166) else Color(0xFF475569),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(state.isSimulating) {
                            if (!state.isSimulating) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        viewModel.onDrawingStart(
                                            Offset(offset.x / size.width, offset.y / size.height)
                                        )
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        viewModel.onDrawingMove(
                                            Offset(
                                                change.position.x / size.width,
                                                change.position.y / size.height
                                            )
                                        )
                                    },
                                    onDragEnd = {
                                        viewModel.onDrawingEnd()
                                    }
                                )
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Draw Tap
                    val tap = state.tap
                    val tx = tap.x * w
                    val ty = tap.y * h

                    // Tap pipe body
                    drawRect(
                        color = Color(0xFF64748B),
                        topLeft = Offset(tx - 16.dp.toPx(), ty - 24.dp.toPx()),
                        size = Size(32.dp.toPx(), 24.dp.toPx())
                    )
                    // Tap nozzle
                    drawRect(
                        color = Color(0xFF38BDF8),
                        topLeft = Offset(tx - 8.dp.toPx(), ty),
                        size = Size(16.dp.toPx(), 10.dp.toPx())
                    )

                    // 2. Draw Obstacles
                    for (obs in state.obstacles) {
                        drawLine(
                            color = Color(0xFFF97316),
                            start = Offset(obs.x1 * w, obs.y1 * h),
                            end = Offset(obs.x2 * w, obs.y2 * h),
                            strokeWidth = 10.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // 3. Draw User Drawn Line
                    if (state.drawnPoints.size >= 2) {
                        val path = Path().apply {
                            moveTo(state.drawnPoints[0].x * w, state.drawnPoints[0].y * h)
                            for (i in 1 until state.drawnPoints.size) {
                                lineTo(state.drawnPoints[i].x * w, state.drawnPoints[i].y * h)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF22C55E),
                            style = Stroke(
                                width = 8.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }

                    // 4. Draw Glass
                    val gl = state.glass
                    val gx = gl.x * w
                    val gy = gl.y * h
                    val gw = gl.width * w
                    val gh = gl.height * h

                    // Glass water target fill line (dotted/dashed)
                    val waterLinePx = gl.waterLineY * h
                    drawLine(
                        color = Color(0xFF38BDF8).copy(alpha = 0.5f),
                        start = Offset(gx + 4.dp.toPx(), waterLinePx),
                        end = Offset(gx + gw - 4.dp.toPx(), waterLinePx),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Glass walls and bottom
                    val glassPath = Path().apply {
                        moveTo(gx, gy)
                        lineTo(gx, gy + gh)
                        lineTo(gx + gw, gy + gh)
                        lineTo(gx + gw, gy)
                    }
                    drawPath(
                        path = glassPath,
                        color = Color.White.copy(alpha = 0.85f),
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Glass Face (Smiling if happy, Sad if not)
                    val faceCenterX = gx + gw / 2f
                    val faceCenterY = gy + gh * 0.55f

                    // Eyes
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = Offset(faceCenterX - 12.dp.toPx(), faceCenterY - 4.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = Offset(faceCenterX + 12.dp.toPx(), faceCenterY - 4.dp.toPx())
                    )

                    // Mouth
                    val mouthPath = Path()
                    if (state.isGlassHappy) {
                        // Happy Smile Curve
                        mouthPath.moveTo(faceCenterX - 10.dp.toPx(), faceCenterY + 6.dp.toPx())
                        mouthPath.quadraticTo(
                            faceCenterX, faceCenterY + 16.dp.toPx(),
                            faceCenterX + 10.dp.toPx(), faceCenterY + 6.dp.toPx()
                        )
                    } else {
                        // Sad Frown Curve
                        mouthPath.moveTo(faceCenterX - 10.dp.toPx(), faceCenterY + 14.dp.toPx())
                        mouthPath.quadraticTo(
                            faceCenterX, faceCenterY + 4.dp.toPx(),
                            faceCenterX + 10.dp.toPx(), faceCenterY + 14.dp.toPx()
                        )
                    }
                    drawPath(
                        path = mouthPath,
                        color = Color.White,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 5. Draw Water Particles
                    val rPx = HappyGlassEngine.DROP_RADIUS * w
                    for (drop in state.waterParticles) {
                        if (!drop.isLost) {
                            val center = Offset(drop.x * w, drop.y * h)
                            drawCircle(
                                color = Color(0xFF38BDF8),
                                radius = rPx,
                                center = center
                            )
                            // Specular reflection
                            drawCircle(
                                color = Color.White.copy(alpha = 0.6f),
                                radius = rPx * 0.4f,
                                center = Offset(center.x - rPx * 0.25f, center.y - rPx * 0.25f)
                            )
                        }
                    }
                }

                // Play Button if not yet simulating and line is drawn
                if (!state.isSimulating && state.drawnPoints.size >= 2) {
                    FloatingActionButton(
                        onClick = { viewModel.startSimulation() },
                        containerColor = Color(0xFF38BDF8),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Water",
                            tint = Color(0xFF0A192F)
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
                            text = "GLASS IS THIRSTY!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFEF4444),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "Water reached ${state.waterInGlassCount}/${state.glass.requiredWater}. Draw a smoother guide line to channel more water into the glass!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.restartLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
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
                            Text("LEVEL SELECT", color = Color(0xFF38BDF8))
                        }
                    },
                    containerColor = Color(0xFF0F2B48)
                )
            }

            // Victory Dialog
            if (state.isWon) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "HAPPY GLASS FILLED!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF10B981),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                repeat(3) { index ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (index < state.stars) Color(0xFFFFD166) else Color(0xFF475569),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Superb line architecture! You conserved ${((state.inkRemainingRatio) * 100).toInt()}% ink and satisfied the glass!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.nextLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("NEXT LEVEL", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("LEVEL SELECT", color = Color(0xFF38BDF8))
                        }
                    },
                    containerColor = Color(0xFF0F2B48)
                )
            }
        }
    }
}
