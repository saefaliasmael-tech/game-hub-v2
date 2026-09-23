package com.example.sandloop.presentation.ui

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
import com.example.sandloop.engine.SandLoopEngine
import com.example.sandloop.engine.SandLoopLevels
import com.example.sandloop.presentation.SandLoopViewModel
import com.zubaluba.gamehub.ads.AdBanner
import com.zubaluba.gamehub.ads.UnifiedAdManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SandLoopScreen(
    viewModel: SandLoopViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val state by viewModel.state.collectAsState()
    val level = viewModel.currentLevel
    val levelIndex by viewModel.currentLevelIndex.collectAsState()

    var showLevelSelect by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        viewModel.startLevel(levelIndex)
        onDispose {
            viewModel.stopGameLoop()
        }
    }

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
                                text = "Sand Loop",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Level ${level.levelNumber} - Dig a path!",
                                color = Color(0xFFF4A261),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "⏳ ${state.collectedCount} / ${state.targetCount}",
                            color = Color(0xFFFFD166),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("sandloop_back_btn")
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B232A))
            )
        },
        bottomBar = {
            AdBanner(modifier = Modifier.fillMaxWidth().navigationBarsPadding())
        },
        containerColor = Color(0xFF0F151B)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Collection Progress Bar
            val progress = (state.collectedCount.toFloat() / state.targetCount).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFF4A261),
                trackColor = Color(0xFF263238)
            )

            // Canvas with touch carving
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF141C24))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val nx = offset.x / size.width
                                val ny = offset.y / size.height
                                viewModel.carve(Offset(nx, ny))
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val nx = change.position.x / size.width
                                val ny = change.position.y / size.height
                                viewModel.carve(Offset(nx, ny))
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. Draw Soft Dirt (Brown background with carving)
                    for (dirt in level.initialDirtRects) {
                        drawRoundRect(
                            color = Color(0xFF6D4C41),
                            topLeft = Offset(dirt.left * w, dirt.top * h),
                            size = Size(dirt.width * w, dirt.height * h),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }

                    // 2. Draw Carved Holes
                    val carveRadiusPx = SandLoopEngine.CARVE_RADIUS * w
                    for (hole in viewModel.engine.carvedHoles) {
                        drawCircle(
                            color = Color(0xFF141C24),
                            radius = carveRadiusPx,
                            center = Offset(hole.x * w, hole.y * h)
                        )
                    }

                    // 3. Draw Stone Barriers (Grey Granite)
                    for (stone in level.stoneBarriers) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF78909C), Color(0xFF455A64))
                            ),
                            topLeft = Offset(stone.left * w, stone.top * h),
                            size = Size(stone.width * w, stone.height * h),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }

                    // 4. Draw Sand Dispenser at top
                    val spawnPx = Offset(level.sandSpawnX * w, level.sandSpawnY * h)
                    drawCircle(
                        color = Color(0xFFE76F51),
                        radius = 16.dp.toPx(),
                        center = spawnPx
                    )
                    drawCircle(
                        color = Color(0xFFF4A261),
                        radius = 10.dp.toPx(),
                        center = spawnPx
                    )

                    // 5. Draw Target Funnel/Vat at bottom
                    val cont = level.containerRect
                    val contTopLeft = Offset(cont.left * w, cont.top * h)
                    val contSize = Size(cont.width * w, cont.height * h)

                    drawRoundRect(
                        color = Color(0xFF37474F),
                        topLeft = contTopLeft,
                        size = contSize,
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )
                    // Liquid / sand level in container
                    val fillHeight = contSize.height * progress
                    drawRoundRect(
                        color = Color(0xFFF4A261),
                        topLeft = Offset(contTopLeft.x + 4.dp.toPx(), contTopLeft.y + contSize.height - fillHeight),
                        size = Size(contSize.width - 8.dp.toPx(), fillHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )

                    // 6. Draw Falling Sand Particles
                    val grainRadius = 3.5.dp.toPx()
                    for (p in viewModel.engine.particles) {
                        if (!p.isCollected && !p.isDead) {
                            val px = p.x * w
                            val py = p.y * h
                            drawCircle(
                                color = Color(0xFFFFD166),
                                radius = grainRadius,
                                center = Offset(px, py)
                            )
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1B232A),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "👆 Drag your finger through the dirt to carve channels for the sand!",
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
                Text("VAT FILLED! 🏖️", fontWeight = FontWeight.Bold, color = Color(0xFFF4A261))
            },
            text = {
                Text("Excellent channel design! All ${state.collectedCount} sand grains flowed safely into the container.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.nextLevel() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4A261))
                ) {
                    Text("Next Level", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.restart() }) {
                    Text("Replay", color = Color.White)
                }
            },
            containerColor = Color(0xFF1B232A)
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
                    SandLoopLevels.levels.forEachIndexed { idx, lvl ->
                        Button(
                            onClick = {
                                viewModel.startLevel(idx)
                                showLevelSelect = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (idx == levelIndex) Color(0xFFF4A261) else Color(0xFF263238)
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
            containerColor = Color(0xFF1B232A)
        )
    }
}
