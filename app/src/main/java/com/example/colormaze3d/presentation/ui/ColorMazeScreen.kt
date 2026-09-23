package com.example.colormaze3d.presentation.ui

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Refresh
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
import com.example.colormaze3d.engine.ColorMazeLevels
import com.example.colormaze3d.presentation.ColorMazeViewModel
import com.zubaluba.gamehub.ads.AdBanner
import com.zubaluba.gamehub.ads.UnifiedAdManager
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorMazeScreen(
    viewModel: ColorMazeViewModel,
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Color Maze 3D",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Level ${level.levelNumber} - Moves: ${state.movesCount}",
                                color = level.paintColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "${(state.completionPercentage * 100).toInt()}%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("colormaze_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.restart() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
                    }
                    IconButton(onClick = { showLevelSelect = true }) {
                        Icon(Icons.Default.FormatListNumbered, contentDescription = "Levels", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F1424))
            )
        },
        bottomBar = {
            AdBanner(modifier = Modifier.fillMaxWidth().navigationBarsPadding())
        },
        containerColor = Color(0xFF0A0D18)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { state.completionPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = level.paintColor,
                trackColor = Color(0xFF1E2640)
            )

            // Maze Canvas with Swipe Controls
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF131A2E))
                    .pointerInput(Unit) {
                        var totalDx = 0f
                        var totalDy = 0f
                        detectDragGestures(
                            onDragStart = { totalDx = 0f; totalDy = 0f },
                            onDragEnd = {
                                if (abs(totalDx) > abs(totalDy) && abs(totalDx) > 30f) {
                                    if (totalDx > 0) viewModel.swipe(1, 0) else viewModel.swipe(-1, 0)
                                } else if (abs(totalDy) > 30f) {
                                    if (totalDy > 0) viewModel.swipe(0, 1) else viewModel.swipe(0, -1)
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
                Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    val tileWidth = size.width / level.width
                    val tileHeight = size.height / level.height
                    val tileSize = minOf(tileWidth, tileHeight)

                    val startX = (size.width - tileSize * level.width) / 2f
                    val startY = (size.height - tileSize * level.height) / 2f

                    // Draw floor tiles
                    for (x in 0 until level.width) {
                        for (y in 0 until level.height) {
                            val isWall = level.walls.contains(x to y)
                            val rx = startX + x * tileSize
                            val ry = startY + y * tileSize

                            if (isWall) {
                                // 3D Isometric Wall Block
                                val blockDepth = tileSize * 0.18f
                                // Front face shadow
                                drawRoundRect(
                                    color = level.wallColor.copy(alpha = 0.6f),
                                    topLeft = Offset(rx + 2f, ry + 2f + blockDepth),
                                    size = Size(tileSize - 4f, tileSize - 4f),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                                // Top face
                                drawRoundRect(
                                    color = level.wallColor,
                                    topLeft = Offset(rx + 2f, ry + 2f),
                                    size = Size(tileSize - 4f, tileSize - 4f),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                            } else {
                                val isPainted = state.paintedTiles.contains(x to y)
                                val tileColor = if (isPainted) level.paintColor else Color(0xFF1E2846)

                                drawRoundRect(
                                    color = tileColor,
                                    topLeft = Offset(rx + 2f, ry + 2f),
                                    size = Size(tileSize - 4f, tileSize - 4f),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                                if (isPainted) {
                                    // Paint sheen
                                    drawRoundRect(
                                        color = Color.White.copy(alpha = 0.25f),
                                        topLeft = Offset(rx + 4f, ry + 4f),
                                        size = Size((tileSize - 8f) * 0.5f, (tileSize - 8f) * 0.5f),
                                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                                    )
                                }
                            }
                        }
                    }

                    // Draw Roller Ball
                    val px = startX + state.playerX * tileSize + tileSize / 2f
                    val py = startY + state.playerY * tileSize + tileSize / 2f
                    val ballRadius = tileSize * 0.42f

                    // Shadow
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.4f),
                        radius = ballRadius,
                        center = Offset(px, py + ballRadius * 0.25f)
                    )

                    // Sphere
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, level.paintColor, Color.Black.copy(alpha = 0.5f)),
                            center = Offset(px - ballRadius * 0.3f, py - ballRadius * 0.3f),
                            radius = ballRadius
                        ),
                        radius = ballRadius,
                        center = Offset(px, py)
                    )
                }
            }

            // Swipe instructions
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF161E34),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "👆 Swipe in any direction to roll and paint all tiles!",
                    color = Color(0xFF90A4AE),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }

    // Victory Dialog
    if (state.isWon) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("MAZE PAINTED! 🎨", fontWeight = FontWeight.Bold, color = level.paintColor)
            },
            text = {
                Text("100% covered! Completed in ${state.movesCount} rolls.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.nextLevel() },
                    colors = ButtonDefaults.buttonColors(containerColor = level.paintColor)
                ) {
                    Text("Next Level", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.restart() }) {
                    Text("Replay", color = Color.White)
                }
            },
            containerColor = Color(0xFF131A2E)
        )
    }

    // Level Select Dialog
    if (showLevelSelect) {
        AlertDialog(
            onDismissRequest = { showLevelSelect = false },
            title = { Text("Select Level", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    ColorMazeLevels.levels.forEachIndexed { idx, lvl ->
                        Button(
                            onClick = {
                                viewModel.startLevel(idx)
                                showLevelSelect = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (idx == levelIndex) lvl.paintColor else Color(0xFF263238)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("${lvl.levelNumber}", color = if (idx == levelIndex) Color.Black else Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLevelSelect = false }) {
                    Text("Close", color = Color.White)
                }
            },
            containerColor = Color(0xFF131A2E)
        )
    }
}
