package com.example.appleworm.presentation.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appleworm.engine.AppleWormLevels
import com.example.appleworm.model.Direction
import com.example.appleworm.model.GridPos
import com.example.appleworm.presentation.AppleWormViewModel
import com.zubaluba.gamehub.ads.AdBanner
import com.zubaluba.gamehub.ads.UnifiedAdManager
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppleWormScreen(
    viewModel: AppleWormViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val state by viewModel.state.collectAsState()
    val level = viewModel.currentLevel
    val levelIndex by viewModel.currentLevelIndex.collectAsState()

    var showLevelSelect by remember { mutableStateOf(false) }

    LaunchedEffect(state.isWon) {
        if (state.isWon && activity != null) {
            UnifiedAdManager.getInstance(activity).showInterstitial(activity)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Apple Worm",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50))
                        ) {
                            Text(
                                text = "Level ${level.levelNumber}",
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("appleworm_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.undo() }) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.restart() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
                    }
                    IconButton(onClick = { showLevelSelect = true }) {
                        Icon(Icons.Default.FormatListNumbered, contentDescription = "Levels", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F1E13))
            )
        },
        bottomBar = {
            AdBanner(modifier = Modifier.fillMaxWidth().navigationBarsPadding())
        },
        containerColor = Color(0xFF0A140D)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Hint / Objective Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF162B1C),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🍎", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.portalOpen) "Portal unlocked! Reach the vortex!" else level.hint,
                        color = if (state.portalOpen) Color(0xFFFFD54F) else Color(0xFFB0BEC5),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Game Board Canvas with Swipe Gesture
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF102014))
                    .border(2.dp, Color(0xFF2E7D32).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .pointerInput(Unit) {
                        var totalDx = 0f
                        var totalDy = 0f
                        detectDragGestures(
                            onDragStart = { totalDx = 0f; totalDy = 0f },
                            onDragEnd = {
                                if (abs(totalDx) > abs(totalDy) && abs(totalDx) > 40f) {
                                    if (totalDx > 0) viewModel.move(Direction.RIGHT) else viewModel.move(Direction.LEFT)
                                } else if (abs(totalDy) > 40f) {
                                    if (totalDy > 0) viewModel.move(Direction.DOWN) else viewModel.move(Direction.UP)
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
                Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    val tileWidth = size.width / level.width
                    val tileHeight = size.height / level.height
                    val tileSize = minOf(tileWidth, tileHeight)

                    val startX = (size.width - tileSize * level.width) / 2f
                    val startY = (size.height - tileSize * level.height) / 2f

                    // Draw subtle grid
                    for (x in 0 until level.width) {
                        for (y in 0 until level.height) {
                            drawRoundRect(
                                color = Color(0xFF17301E).copy(alpha = 0.4f),
                                topLeft = Offset(startX + x * tileSize + 2f, startY + y * tileSize + 2f),
                                size = Size(tileSize - 4f, tileSize - 4f),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }
                    }

                    // Draw Walls (Stone Blocks)
                    for (wall in level.walls) {
                        val rx = startX + wall.x * tileSize
                        val ry = startY + wall.y * tileSize
                        drawRoundRect(
                            color = Color(0xFF388E3C),
                            topLeft = Offset(rx + 2f, ry + 2f),
                            size = Size(tileSize - 4f, tileSize - 4f),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                        // Stone highlight
                        drawRoundRect(
                            color = Color(0xFF4CAF50),
                            topLeft = Offset(rx + 4f, ry + 4f),
                            size = Size(tileSize - 8f, (tileSize - 8f) * 0.4f),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    // Draw Hazards (Spikes)
                    for (h in level.hazards) {
                        val hx = startX + h.x * tileSize
                        val hy = startY + h.y * tileSize
                        val spikePath = Path().apply {
                            moveTo(hx + 2f, hy + tileSize)
                            lineTo(hx + tileSize / 2f, hy + 4f)
                            lineTo(hx + tileSize - 2f, hy + tileSize)
                            close()
                        }
                        drawPath(spikePath, color = Color(0xFFE53935))
                    }

                    // Draw Apples
                    for (apple in state.applesRemaining) {
                        val ax = startX + apple.x * tileSize + tileSize / 2f
                        val ay = startY + apple.y * tileSize + tileSize / 2f
                        val radius = tileSize * 0.38f
                        drawCircle(color = Color(0xFFD32F2F), radius = radius, center = Offset(ax, ay + 2f))
                        drawCircle(color = Color(0xFFFF5252), radius = radius * 0.45f, center = Offset(ax - radius * 0.3f, ay - radius * 0.2f))
                        // Stem
                        drawCircle(color = Color(0xFF795548), radius = 2.5.dp.toPx(), center = Offset(ax, ay - radius))
                    }

                    // Draw Portal
                    val px = startX + level.portal.x * tileSize + tileSize / 2f
                    val py = startY + level.portal.y * tileSize + tileSize / 2f
                    val portalRadius = tileSize * 0.42f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = if (state.portalOpen)
                                listOf(Color(0xFFFFD54F), Color(0xFFFF6F00), Color(0xFF3E2723))
                            else
                                listOf(Color(0xFF546E7A), Color(0xFF263238)),
                            center = Offset(px, py),
                            radius = portalRadius
                        ),
                        radius = portalRadius,
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = portalRadius * 0.3f,
                        center = Offset(px, py)
                    )

                    // Draw Worm
                    state.worm.forEachIndexed { index, seg ->
                        val sx = startX + seg.x * tileSize + tileSize / 2f
                        val sy = startY + seg.y * tileSize + tileSize / 2f
                        val isHead = index == 0
                        val wormRadius = if (isHead) tileSize * 0.44f else tileSize * 0.36f

                        drawCircle(
                            color = if (isHead) Color(0xFF8BC34A) else Color(0xFF689F38),
                            radius = wormRadius,
                            center = Offset(sx, sy)
                        )

                        // Eyes on head
                        if (isHead) {
                            val eyeOffset = wormRadius * 0.35f
                            drawCircle(color = Color.White, radius = wormRadius * 0.25f, center = Offset(sx - eyeOffset, sy - eyeOffset))
                            drawCircle(color = Color.Black, radius = wormRadius * 0.12f, center = Offset(sx - eyeOffset, sy - eyeOffset))
                            drawCircle(color = Color.White, radius = wormRadius * 0.25f, center = Offset(sx + eyeOffset, sy - eyeOffset))
                            drawCircle(color = Color.Black, radius = wormRadius * 0.12f, center = Offset(sx + eyeOffset, sy - eyeOffset))
                        }
                    }
                }
            }

            // On-screen D-Pad Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                IconButton(
                    onClick = { viewModel.move(Direction.UP) },
                    modifier = Modifier.size(54.dp).background(Color(0xFF2E7D32), CircleShape)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.move(Direction.LEFT) },
                        modifier = Modifier.size(54.dp).background(Color(0xFF2E7D32), CircleShape)
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    IconButton(
                        onClick = { viewModel.move(Direction.DOWN) },
                        modifier = Modifier.size(54.dp).background(Color(0xFF2E7D32), CircleShape)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    IconButton(
                        onClick = { viewModel.move(Direction.RIGHT) },
                        modifier = Modifier.size(54.dp).background(Color(0xFF2E7D32), CircleShape)
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    }

    // Victory Dialog
    if (state.isWon) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("LEVEL COMPLETE! 🍏", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            },
            text = {
                Text("Great job navigating through the maze and reaching the portal in ${state.movesCount} moves!")
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
                    Text("Replay", color = Color.White)
                }
            },
            containerColor = Color(0xFF1B3822)
        )
    }

    // Game Over Dialog
    if (state.isGameOver) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("OUCH! 💥", fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
            },
            text = {
                Text("The worm fell into a hazard or fell out of the chamber. Try again!")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.restart() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Try Again", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF2C1616)
        )
    }

    // Level Select Bottom Sheet
    if (showLevelSelect) {
        AlertDialog(
            onDismissRequest = { showLevelSelect = false },
            title = { Text("Select Level", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    AppleWormLevels.levels.forEachIndexed { idx, lvl ->
                        Button(
                            onClick = {
                                viewModel.startLevel(idx)
                                showLevelSelect = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (idx == levelIndex) Color(0xFF4CAF50) else Color(0xFF263238)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("${lvl.levelNumber}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLevelSelect = false }) {
                    Text("Close", color = Color(0xFF81C784))
                }
            },
            containerColor = Color(0xFF162B1C)
        )
    }
}
