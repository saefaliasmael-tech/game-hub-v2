package com.example.protectsheep.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.example.protectsheep.core.engine.ProtectSheepEngine
import com.example.protectsheep.presentation.ProtectSheepViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectSheepGameScreen(
    viewModel: ProtectSheepViewModel,
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
                        // Survival countdown badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (state.isSimulating) Color(0xFFEF4444) else Color(0xFF1E3A1E))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1fs", state.survivalTimeLeftSec),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A1E))
            )
        },
        containerColor = Color(0xFF0F240F)
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
                    color = Color(0xFF86EFAC),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF143314))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(state.inkRemainingRatio)
                            .background(
                                when {
                                    state.inkRemainingRatio >= 0.55f -> Color(0xFF22C55E)
                                    state.inkRemainingRatio >= 0.20f -> Color(0xFFFFD166)
                                    else -> Color(0xFFEF4444)
                                }
                            )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row {
                    val stars = ProtectSheepEngine.calculateStars(state.inkRemainingRatio)
                    repeat(3) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < stars) Color(0xFFFFD166) else Color(0xFF386633),
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

                    // 1. Draw Hazards & Platforms
                    for (haz in state.hazards) {
                        val sx = haz.x1 * w
                        val sy = haz.y1 * h
                        val ex = haz.x2 * w
                        val ey = haz.y2 * h

                        if (haz.isSpike) {
                            // Spikes
                            drawLine(
                                color = Color(0xFFEF4444),
                                start = Offset(sx, sy),
                                end = Offset(ex, ey),
                                strokeWidth = 14.dp.toPx(),
                                cap = StrokeCap.Square
                            )
                        } else {
                            // Ground platform
                            drawLine(
                                color = Color(0xFF3B2412),
                                start = Offset(sx, sy + 4.dp.toPx()),
                                end = Offset(ex, ey + 4.dp.toPx()),
                                strokeWidth = 14.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            // Grass top
                            drawLine(
                                color = Color(0xFF22C55E),
                                start = Offset(sx, sy),
                                end = Offset(ex, ey),
                                strokeWidth = 6.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // 2. Draw Bee Hives
                    for (hive in state.hives) {
                        val hx = hive.x * w
                        val hy = hive.y * h

                        // Hanging string
                        drawLine(
                            color = Color(0xFF78350F),
                            start = Offset(hx, 0f),
                            end = Offset(hx, hy),
                            strokeWidth = 2.dp.toPx()
                        )
                        // Honeycomb hive layers
                        drawRoundRect(
                            color = Color(0xFFD97706),
                            topLeft = Offset(hx - 16.dp.toPx(), hy - 14.dp.toPx()),
                            size = Size(32.dp.toPx(), 28.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx(), 10.dp.toPx())
                        )
                        // Hive entrance hole
                        drawCircle(
                            color = Color(0xFF451A03),
                            radius = 6.dp.toPx(),
                            center = Offset(hx, hy + 2.dp.toPx())
                        )
                    }

                    // 3. Draw User Protective Line
                    if (state.drawnPoints.size >= 2) {
                        val path = Path().apply {
                            moveTo(state.drawnPoints[0].x * w, state.drawnPoints[0].y * h)
                            for (i in 1 until state.drawnPoints.size) {
                                lineTo(state.drawnPoints[i].x * w, state.drawnPoints[i].y * h)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF38BDF8),
                            style = Stroke(
                                width = 10.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }

                    // 4. Draw Sheep
                    for (sheep in state.sheepList) {
                        val sx = sheep.x * w
                        val sy = sheep.y * h
                        val r = sheep.radius * w

                        // Fluffy Wool Circles
                        val puffR = r * 0.45f
                        val offsets = listOf(
                            Offset(-puffR, -puffR * 0.5f),
                            Offset(puffR, -puffR * 0.5f),
                            Offset(-puffR * 0.8f, puffR * 0.5f),
                            Offset(puffR * 0.8f, puffR * 0.5f),
                            Offset(0f, -puffR * 0.8f),
                            Offset(0f, puffR * 0.8f)
                        )
                        for (off in offsets) {
                            drawCircle(
                                color = Color.White,
                                radius = puffR * 1.1f,
                                center = Offset(sx + off.x, sy + off.y)
                            )
                        }
                        // Core wool
                        drawCircle(
                            color = Color(0xFFF1F5F9),
                            radius = r * 0.85f,
                            center = Offset(sx, sy)
                        )

                        // Sheep Head (Dark charcoal)
                        drawRoundRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(sx - r * 0.4f, sy - r * 0.4f),
                            size = Size(r * 0.8f, r * 0.7f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Ears
                        drawCircle(
                            color = Color(0xFF1E293B),
                            radius = r * 0.18f,
                            center = Offset(sx - r * 0.45f, sy - r * 0.2f)
                        )
                        drawCircle(
                            color = Color(0xFF1E293B),
                            radius = r * 0.18f,
                            center = Offset(sx + r * 0.45f, sy - r * 0.2f)
                        )

                        if (sheep.isStung) {
                            // Swollen stung 'X' eyes
                            drawLine(
                                color = Color(0xFFEF4444),
                                start = Offset(sx - r * 0.25f, sy - r * 0.2f),
                                end = Offset(sx - r * 0.1f, sy - r * 0.05f),
                                strokeWidth = 2.dp.toPx()
                            )
                            drawLine(
                                color = Color(0xFFEF4444),
                                start = Offset(sx - r * 0.1f, sy - r * 0.2f),
                                end = Offset(sx - r * 0.25f, sy - r * 0.05f),
                                strokeWidth = 2.dp.toPx()
                            )
                            // Swollen red bump
                            drawCircle(
                                color = Color(0xFFEF4444).copy(alpha = 0.8f),
                                radius = r * 0.2f,
                                center = Offset(sx + r * 0.2f, sy - r * 0.1f)
                            )
                        } else {
                            // Cute Eyes
                            drawCircle(
                                color = Color.White,
                                radius = r * 0.12f,
                                center = Offset(sx - r * 0.18f, sy - r * 0.12f)
                            )
                            drawCircle(
                                color = Color.Black,
                                radius = r * 0.06f,
                                center = Offset(sx - r * 0.16f, sy - r * 0.12f)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = r * 0.12f,
                                center = Offset(sx + r * 0.18f, sy - r * 0.12f)
                            )
                            drawCircle(
                                color = Color.Black,
                                radius = r * 0.06f,
                                center = Offset(sx + r * 0.16f, sy - r * 0.12f)
                            )
                        }
                    }

                    // 5. Draw Bees
                    for (bee in state.bees) {
                        val bx = bee.x * w
                        val by = bee.y * h
                        val br = bee.radius * w

                        // Wings (fluttering white)
                        drawCircle(
                            color = Color.White.copy(alpha = 0.7f),
                            radius = br * 0.8f,
                            center = Offset(bx, by - br * 0.9f)
                        )
                        // Bee Body (Yellow)
                        drawCircle(
                            color = Color(0xFFFACC15),
                            radius = br,
                            center = Offset(bx, by)
                        )
                        // Black Stripes
                        drawLine(
                            color = Color.Black,
                            start = Offset(bx - br * 0.6f, by - br * 0.4f),
                            end = Offset(bx - br * 0.6f, by + br * 0.4f),
                            strokeWidth = 2.dp.toPx()
                        )
                        drawLine(
                            color = Color.Black,
                            start = Offset(bx + br * 0.2f, by - br * 0.4f),
                            end = Offset(bx + br * 0.2f, by + br * 0.4f),
                            strokeWidth = 2.dp.toPx()
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
                            text = "SHEEP STUNG!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFEF4444),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "The bees broke through the defense! Draw a closed protective dome around the sheep before the bees arrive.",
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
                            Text("LEVEL SELECT", color = Color(0xFF22C55E))
                        }
                    },
                    containerColor = Color(0xFF1E3A1E)
                )
            }

            // Victory Dialog
            if (state.isWon) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "FLOCK PROTECTED!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF22C55E),
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
                                        tint = if (index < state.stars) Color(0xFFFFD166) else Color(0xFF386633),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Outstanding defense! The sheep survived the 10s bee siege safely with ${((state.inkRemainingRatio) * 100).toInt()}% ink preserved.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.nextLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("NEXT LEVEL", fontWeight = FontWeight.Bold, color = Color(0xFF0F240F))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("LEVEL SELECT", color = Color(0xFF86EFAC))
                        }
                    },
                    containerColor = Color(0xFF1E3A1E)
                )
            }
        }
    }
}
