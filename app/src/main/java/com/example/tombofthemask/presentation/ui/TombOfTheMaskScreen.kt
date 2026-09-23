package com.example.tombofthemask.presentation.ui

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tombofthemask.engine.MaskLevels
import com.example.tombofthemask.presentation.TombOfTheMaskViewModel
import com.zubaluba.gamehub.ads.AdBanner
import com.zubaluba.gamehub.ads.UnifiedAdManager
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TombOfTheMaskScreen(
    viewModel: TombOfTheMaskViewModel,
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
                        Text(
                            text = "Tomb of the Mask",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFE600),
                            fontSize = 18.sp
                        )
                        Text(
                            text = "⭐ ${state.score}",
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("tombofthemask_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFFFFE600))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.restart() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color(0xFFFFE600))
                    }
                    IconButton(onClick = { showLevelSelect = true }) {
                        Icon(Icons.Default.FormatListNumbered, contentDescription = "Levels", tint = Color(0xFFFFE600))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        bottomBar = {
            AdBanner(modifier = Modifier.fillMaxWidth().navigationBarsPadding())
        },
        containerColor = Color(0xFF080808)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E1E1E),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Level ${level.levelNumber} - Dots left: ${state.dotsRemaining.size}",
                        color = Color(0xFFFFE600),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Reach the Relic 👑",
                        color = Color(0xFFB0BEC5),
                        fontSize = 12.sp
                    )
                }
            }

            // Neon Grid Canvas with 4-way Swipe Controls
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F0F12))
                    .border(2.dp, Color(0xFF9D4EDD).copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .pointerInput(Unit) {
                        var totalDx = 0f
                        var totalDy = 0f
                        detectDragGestures(
                            onDragStart = { totalDx = 0f; totalDy = 0f },
                            onDragEnd = {
                                if (abs(totalDx) > abs(totalDy) && abs(totalDx) > 30f) {
                                    if (totalDx > 0) viewModel.dash(1, 0) else viewModel.dash(-1, 0)
                                } else if (abs(totalDy) > 30f) {
                                    if (totalDy > 0) viewModel.dash(0, 1) else viewModel.dash(0, -1)
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

                    // 1. Draw Neon Walls
                    for (wall in level.walls) {
                        val rx = startX + wall.first * tileSize
                        val ry = startY + wall.second * tileSize

                        drawRoundRect(
                            color = Color(0xFF3A0CA3),
                            topLeft = Offset(rx + 2f, ry + 2f),
                            size = Size(tileSize - 4f, tileSize - 4f),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        // Neon border outline
                        drawRoundRect(
                            color = Color(0xFF4CC9F0),
                            topLeft = Offset(rx + 2f, ry + 2f),
                            size = Size(tileSize - 4f, tileSize - 4f),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    // 2. Draw Luminescent Trail
                    for (p in state.trail) {
                        val tx = startX + p.first * tileSize + tileSize / 2f
                        val ty = startY + p.second * tileSize + tileSize / 2f
                        drawCircle(
                            color = Color(0xFFFFE600).copy(alpha = 0.25f),
                            radius = tileSize * 0.35f,
                            center = Offset(tx, ty)
                        )
                    }

                    // 3. Draw Dots
                    for (dot in state.dotsRemaining) {
                        val dx = startX + dot.first * tileSize + tileSize / 2f
                        val dy = startY + dot.second * tileSize + tileSize / 2f
                        drawCircle(color = Color(0xFFFFEB3B), radius = tileSize * 0.12f, center = Offset(dx, dy))
                    }

                    // 4. Draw Coins
                    for (coin in state.coinsRemaining) {
                        val cx = startX + coin.first * tileSize + tileSize / 2f
                        val cy = startY + coin.second * tileSize + tileSize / 2f
                        drawCircle(color = Color(0xFFFFB300), radius = tileSize * 0.25f, center = Offset(cx, cy))
                        drawCircle(color = Color(0xFFFFD54F), radius = tileSize * 0.15f, center = Offset(cx, cy))
                    }

                    // 5. Draw Spikes
                    for (spk in level.spikes) {
                        val sx = startX + spk.first * tileSize
                        val sy = startY + spk.second * tileSize
                        val path = Path().apply {
                            moveTo(sx + 3f, sy + tileSize - 3f)
                            lineTo(sx + tileSize / 2f, sy + 3f)
                            lineTo(sx + tileSize - 3f, sy + tileSize - 3f)
                            close()
                        }
                        drawPath(path, color = Color(0xFFFF0055))
                    }

                    // 6. Draw Exit Relic
                    val ex = startX + level.exit.first * tileSize + tileSize / 2f
                    val ey = startY + level.exit.second * tileSize + tileSize / 2f
                    drawCircle(color = Color(0xFF9D4EDD), radius = tileSize * 0.4f, center = Offset(ex, ey))
                    drawCircle(color = Color(0xFFFFE600), radius = tileSize * 0.25f, center = Offset(ex, ey))

                    // 7. Draw Player Mask
                    val px = startX + state.playerX * tileSize + tileSize / 2f
                    val py = startY + state.playerY * tileSize + tileSize / 2f
                    val maskR = tileSize * 0.4f

                    // Golden square mask
                    drawRoundRect(
                        color = Color(0xFFFFD700),
                        topLeft = Offset(px - maskR, py - maskR),
                        size = Size(maskR * 2f, maskR * 2f),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    // Mask Eyes (Black slits)
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = Offset(px - maskR * 0.6f, py - maskR * 0.3f),
                        size = Size(maskR * 0.4f, maskR * 0.25f),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = Offset(px + maskR * 0.2f, py - maskR * 0.3f),
                        size = Size(maskR * 0.4f, maskR * 0.25f),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E1E1E),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "⚡ Swipe to dash instantly wall-to-wall! Collect dots & avoid spikes!",
                    color = Color(0xFFB0BEC5),
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
                Text("TOMB CONQUERED! 👑", fontWeight = FontWeight.Bold, color = Color(0xFFFFE600))
            },
            text = {
                Text("All dots collected and relic retrieved! Score: ${state.score}")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.nextLevel() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE600))
                ) {
                    Text("Next Tomb", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.restart() }) {
                    Text("Replay", color = Color.White)
                }
            },
            containerColor = Color(0xFF1B1B1B)
        )
    }

    // Game Over Dialog
    if (state.isGameOver) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("IMPALED! 💀", fontWeight = FontWeight.Bold, color = Color(0xFFFF0055))
            },
            text = {
                Text("The mask struck a spike trap! Watch your trajectory.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.restart() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055))
                ) {
                    Text("Try Again", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF241016)
        )
    }

    // Level Select Dialog
    if (showLevelSelect) {
        AlertDialog(
            onDismissRequest = { showLevelSelect = false },
            title = { Text("Select Tomb", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    MaskLevels.levels.forEachIndexed { idx, lvl ->
                        Button(
                            onClick = {
                                viewModel.startLevel(idx)
                                showLevelSelect = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (idx == levelIndex) Color(0xFFFFE600) else Color(0xFF2E2E2E)
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
            containerColor = Color(0xFF1E1E1E)
        )
    }
}
