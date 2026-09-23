package com.example.paperio2.presentation.ui

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paperio2.engine.PaperIoEngine
import com.example.paperio2.model.OwnerId
import com.example.paperio2.presentation.PaperIoViewModel
import com.zubaluba.gamehub.ads.AdBanner
import com.zubaluba.gamehub.ads.UnifiedAdManager
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaperIoScreen(
    viewModel: PaperIoViewModel,
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
                            text = "Paper.io 2",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF),
                            fontSize = 18.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "👑 %.1f%%".format(state.player.territoryPercent),
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "KILLS: ${state.kills}",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("paperio2_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF00E5FF))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.restart() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color(0xFF00E5FF))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF101726))
            )
        },
        bottomBar = {
            AdBanner(modifier = Modifier.fillMaxWidth().navigationBarsPadding())
        },
        containerColor = Color(0xFF0B0F19)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Live Mini-Leaderboard
            Surface(
                color = Color(0xFF141D30),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val entities = (listOf(state.player) + state.bots).sortedByDescending { it.territoryPercent }
                    entities.take(3).forEachIndexed { index, ent ->
                        Text(
                            text = "#${index + 1} ${ent.name}: %.1f%%".format(ent.territoryPercent),
                            color = ent.color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Main Arena Canvas with Swipe Steering
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF101827))
                    .pointerInput(Unit) {
                        var totalDx = 0f
                        var totalDy = 0f
                        detectDragGestures(
                            onDragStart = { totalDx = 0f; totalDy = 0f },
                            onDragEnd = {
                                if (abs(totalDx) > abs(totalDy) && abs(totalDx) > 25f) {
                                    if (totalDx > 0) viewModel.steer(1f, 0f) else viewModel.steer(-1f, 0f)
                                } else if (abs(totalDy) > 25f) {
                                    if (totalDy > 0) viewModel.steer(0f, 1f) else viewModel.steer(0f, -1f)
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
                    val cellSize = minOf(w, h) / PaperIoEngine.GRID_SIZE
                    val startX = (w - cellSize * PaperIoEngine.GRID_SIZE) / 2f
                    val startY = (h - cellSize * PaperIoEngine.GRID_SIZE) / 2f

                    // 1. Draw Arena Grid Borders
                    drawRect(
                        color = Color(0xFF1A2338),
                        topLeft = Offset(startX, startY),
                        size = Size(cellSize * PaperIoEngine.GRID_SIZE, cellSize * PaperIoEngine.GRID_SIZE)
                    )

                    // 2. Draw Territory Blocks
                    val colors = mapOf(
                        OwnerId.PLAYER.ordinal to Color(0xFF00B4D8).copy(alpha = 0.7f),
                        OwnerId.BOT1.ordinal to Color(0xFFE53935).copy(alpha = 0.6f),
                        OwnerId.BOT2.ordinal to Color(0xFFFFB300).copy(alpha = 0.6f),
                        OwnerId.BOT3.ordinal to Color(0xFF8E24AA).copy(alpha = 0.6f)
                    )

                    for (x in 0 until PaperIoEngine.GRID_SIZE) {
                        for (y in 0 until PaperIoEngine.GRID_SIZE) {
                            val owner = state.gridOwner[x][y]
                            val col = colors[owner]
                            if (col != null) {
                                drawRect(
                                    color = col,
                                    topLeft = Offset(startX + x * cellSize, startY + y * cellSize),
                                    size = Size(cellSize, cellSize)
                                )
                            }
                        }
                    }

                    // 3. Draw Ribbon Trails
                    val allEntities = listOf(state.player) + state.bots
                    for (ent in allEntities) {
                        if (!ent.isAlive) continue
                        for (trailCell in ent.trail) {
                            drawRect(
                                color = ent.trailColor,
                                topLeft = Offset(startX + trailCell.first * cellSize, startY + trailCell.second * cellSize),
                                size = Size(cellSize, cellSize)
                            )
                        }
                    }

                    // 4. Draw Heads / Cubes
                    for (ent in allEntities) {
                        if (!ent.isAlive) continue
                        val px = startX + ent.x * cellSize
                        val py = startY + ent.y * cellSize
                        val headSize = cellSize * 1.5f

                        drawRect(
                            color = ent.color,
                            topLeft = Offset(px - headSize * 0.25f, py - headSize * 0.25f),
                            size = Size(headSize, headSize)
                        )
                        // White highlight
                        drawRect(
                            color = Color.White.copy(alpha = 0.4f),
                            topLeft = Offset(px, py),
                            size = Size(headSize * 0.5f, headSize * 0.5f)
                        )
                    }
                }
            }

            // On-screen Directional Touch Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { viewModel.steer(0f, -1f) },
                    modifier = Modifier.size(52.dp).background(Color(0xFF1E283C), CircleShape)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = Color(0xFF00E5FF), modifier = Modifier.size(34.dp))
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                    IconButton(
                        onClick = { viewModel.steer(-1f, 0f) },
                        modifier = Modifier.size(52.dp).background(Color(0xFF1E283C), CircleShape)
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = Color(0xFF00E5FF), modifier = Modifier.size(34.dp))
                    }
                    IconButton(
                        onClick = { viewModel.steer(0f, 1f) },
                        modifier = Modifier.size(52.dp).background(Color(0xFF1E283C), CircleShape)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = Color(0xFF00E5FF), modifier = Modifier.size(34.dp))
                    }
                    IconButton(
                        onClick = { viewModel.steer(1f, 0f) },
                        modifier = Modifier.size(52.dp).background(Color(0xFF1E283C), CircleShape)
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = Color(0xFF00E5FF), modifier = Modifier.size(34.dp))
                    }
                }
            }
        }
    }

    // Game Over Dialog
    if (state.isGameOver) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("ELIMINATED! 💥", fontWeight = FontWeight.Bold, color = Color(0xFFFF1744))
            },
            text = {
                Column {
                    Text("Your trail was severed!")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Final Territory: %.1f%%".format(state.player.territoryPercent), fontWeight = FontWeight.SemiBold)
                    Text("Kills: ${state.kills}", fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.restart() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text("Play Again", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            containerColor = Color(0xFF141D30)
        )
    }
}
