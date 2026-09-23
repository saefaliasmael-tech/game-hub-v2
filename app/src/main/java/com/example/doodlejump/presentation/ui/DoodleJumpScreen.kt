package com.example.doodlejump.presentation.ui

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doodlejump.model.PlatformType
import com.example.doodlejump.presentation.DoodleJumpViewModel
import com.zubaluba.gamehub.ads.AdBanner
import com.zubaluba.gamehub.ads.UnifiedAdManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoodleJumpScreen(
    viewModel: DoodleJumpViewModel,
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
                            text = "Doodle Jump",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            fontSize = 18.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ALTITUDE: ${state.score}m",
                                color = Color(0xFF1B5E20),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "BEST: ${state.bestScore}m",
                                color = Color(0xFF558B2F),
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("doodlejump_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1B5E20))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.restart() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color(0xFF1B5E20))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE8F5E9))
            )
        },
        bottomBar = {
            AdBanner(modifier = Modifier.fillMaxWidth().navigationBarsPadding())
        },
        containerColor = Color(0xFFF1F8E9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Main Game Area with Drag / Tilt Steer
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {},
                            onDragEnd = { viewModel.horizontalInput = 0f },
                            onDragCancel = { viewModel.horizontalInput = 0f },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                viewModel.horizontalInput = (dragAmount.x / 15f).coerceIn(-1f, 1f)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. Draw Graph Notebook Background Grid
                    val gridStep = 30.dp.toPx()
                    var gx = 0f
                    while (gx < w) {
                        drawLine(Color(0xFFC8E6C9).copy(alpha = 0.5f), Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
                        gx += gridStep
                    }
                    var gy = 0f
                    while (gy < h) {
                        drawLine(Color(0xFFC8E6C9).copy(alpha = 0.5f), Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
                        gy += gridStep
                    }

                    // 2. Draw Platforms relative to camera
                    for (plat in state.platforms) {
                        if (plat.isBroken) continue
                        val screenY = plat.y - state.cameraY
                        if (screenY !in -50f..(h + 50f)) continue

                        val platW = plat.width * w
                        val platH = 14.dp.toPx()
                        val px = plat.x * w

                        val color = when (plat.type) {
                            PlatformType.STATIC -> Color(0xFF43A047)
                            PlatformType.MOVING -> Color(0xFF1E88E5)
                            PlatformType.FRAGILE -> Color(0xFF8D6E63)
                            PlatformType.SPRING -> Color(0xFFFDD835)
                        }

                        drawRoundRect(
                            color = color,
                            topLeft = Offset(px, screenY),
                            size = Size(platW, platH),
                            cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
                        )

                        // Highlight
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.35f),
                            topLeft = Offset(px + 4f, screenY + 2f),
                            size = Size(platW - 8f, platH * 0.35f),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        // Draw spring coil on spring platforms
                        if (plat.type == PlatformType.SPRING) {
                            val springX = px + platW / 2f
                            val springTop = screenY - 10.dp.toPx()
                            drawCircle(color = Color(0xFF757575), radius = 5.dp.toPx(), center = Offset(springX, springTop))
                        }
                    }

                    // 3. Draw The Doodler Character
                    val charScreenY = state.player.y - state.cameraY
                    val charX = state.player.x * w
                    val charRadius = 18.dp.toPx()

                    // Legs
                    drawRoundRect(
                        color = Color(0xFF388E3C),
                        topLeft = Offset(charX - 10.dp.toPx(), charScreenY + charRadius * 0.7f),
                        size = Size(6.dp.toPx(), 10.dp.toPx()),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFF388E3C),
                        topLeft = Offset(charX + 4.dp.toPx(), charScreenY + charRadius * 0.7f),
                        size = Size(6.dp.toPx(), 10.dp.toPx()),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )

                    // Body
                    drawCircle(
                        color = Color(0xFF8BC34A),
                        radius = charRadius,
                        center = Offset(charX, charScreenY)
                    )

                    // Snout / Nose
                    val snoutDir = if (state.player.facingRight) 1f else -1f
                    drawRoundRect(
                        color = Color(0xFF7CB342),
                        topLeft = Offset(charX + (snoutDir * charRadius * 0.4f), charScreenY - 4.dp.toPx()),
                        size = Size(14.dp.toPx() * snoutDir, 8.dp.toPx()),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Eyes
                    val eyeX = charX + (snoutDir * charRadius * 0.3f)
                    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(eyeX, charScreenY - 6.dp.toPx()))
                    drawCircle(color = Color.Black, radius = 2.5.dp.toPx(), center = Offset(eyeX + snoutDir * 1.5f, charScreenY - 6.dp.toPx()))
                }
            }

            // On-screen Navigation Touch Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = { viewModel.horizontalInput = -1f },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", modifier = Modifier.size(36.dp), tint = Color(0xFF1B5E20))
                }

                Text(
                    text = "👈 Tap or Drag to Steer 👉",
                    color = Color(0xFF558B2F),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                FilledTonalIconButton(
                    onClick = { viewModel.horizontalInput = 1f },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", modifier = Modifier.size(36.dp), tint = Color(0xFF1B5E20))
                }
            }
        }
    }

    // Game Over Dialog
    if (state.isGameOver) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("GAME OVER! 🍃", fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
            },
            text = {
                Column {
                    Text("Altitude Reached: ${state.score}m")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Best Record: ${state.bestScore}m", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.restart() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Jump Again", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFFE8F5E9)
        )
    }
}
