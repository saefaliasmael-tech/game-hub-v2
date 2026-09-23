package com.example.woodturning.presentation.ui

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.woodturning.engine.WoodLevels
import com.example.woodturning.presentation.WoodturningViewModel
import com.zubaluba.gamehub.ads.AdBanner
import com.zubaluba.gamehub.ads.UnifiedAdManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WoodturningScreen(
    viewModel: WoodturningViewModel,
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
            viewModel.stopLoop()
        }
    }

    LaunchedEffect(state.isFinished) {
        if (state.isFinished && activity != null) {
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
                                text = "Woodturning 3D",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Level ${level.levelNumber} - ${level.name}",
                                color = Color(0xFFD4A373),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "Accuracy: ${(state.accuracy * 100).toInt()}%",
                            color = if (state.accuracy >= 0.85f) Color(0xFF4CAF50) else Color(0xFFFFD166),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("woodturning_back_btn")
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF261C14))
            )
        },
        bottomBar = {
            AdBanner(modifier = Modifier.fillMaxWidth().navigationBarsPadding())
        },
        containerColor = Color(0xFF140E0A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Accuracy Bar
            LinearProgressIndicator(
                progress = { state.accuracy },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (state.accuracy >= 0.85f) Color(0xFF4CAF50) else Color(0xFFD4A373),
                trackColor = Color(0xFF3E2723)
            )

            // Wood Lathe Canvas with Drag-to-Chisel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E1712))
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val nx = (change.position.x / size.width).coerceIn(0f, 1f)
                            // Distance from horizontal center line
                            val cy = size.height / 2f
                            val distY = kotlin.math.abs(change.position.y - cy)
                            val maxRadiusPx = size.height * 0.35f
                            val normRadius = (distY / maxRadiusPx).coerceIn(0.1f, 1.0f)
                            viewModel.carve(nx, normRadius)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    val w = size.width
                    val h = size.height
                    val cy = h / 2f
                    val maxR = h * 0.35f

                    // 1. Draw Lathe Spindle at Left and Right
                    drawRect(
                        brush = Brush.verticalGradient(listOf(Color(0xFF757575), Color(0xFF424242))),
                        topLeft = Offset(0f, cy - 25.dp.toPx()),
                        size = Size(20.dp.toPx(), 50.dp.toPx())
                    )
                    drawRect(
                        brush = Brush.verticalGradient(listOf(Color(0xFF757575), Color(0xFF424242))),
                        topLeft = Offset(w - 20.dp.toPx(), cy - 25.dp.toPx()),
                        size = Size(20.dp.toPx(), 50.dp.toPx())
                    )

                    // 2. Draw Target Silhouette (dashed cyan line)
                    val targetPathTop = Path()
                    val targetPathBottom = Path()
                    val segWidth = w / WoodLevels.SEGMENTS

                    for (i in 0 until WoodLevels.SEGMENTS) {
                        val px = i * segWidth
                        val tr = state.targetRadii[i] * maxR
                        if (i == 0) {
                            targetPathTop.moveTo(px, cy - tr)
                            targetPathBottom.moveTo(px, cy + tr)
                        } else {
                            targetPathTop.lineTo(px, cy - tr)
                            targetPathBottom.lineTo(px, cy + tr)
                        }
                    }

                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    drawPath(targetPathTop, color = Color(0xFF4DD0E1), style = Stroke(width = 2.dp.toPx(), pathEffect = dashEffect))
                    drawPath(targetPathBottom, color = Color(0xFF4DD0E1), style = Stroke(width = 2.dp.toPx(), pathEffect = dashEffect))

                    // 3. Draw Carved Wood Piece (Top & Bottom mirrored symmetrical polygons)
                    val woodPath = Path()
                    // Top curve
                    for (i in 0 until WoodLevels.SEGMENTS) {
                        val px = i * segWidth
                        val r = state.currentRadii[i] * maxR
                        if (i == 0) woodPath.moveTo(px, cy - r) else woodPath.lineTo(px, cy - r)
                    }
                    // Bottom curve (in reverse)
                    for (i in (WoodLevels.SEGMENTS - 1) downTo 0) {
                        val px = i * segWidth
                        val r = state.currentRadii[i] * maxR
                        woodPath.lineTo(px, cy + r)
                    }
                    woodPath.close()

                    // Wood Grain Gradient
                    drawPath(
                        path = woodPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFD4A373),
                                Color(0xFFBC6C25),
                                Color(0xFF8C4A1B),
                                Color(0xFFBC6C25),
                                Color(0xFFD4A373)
                            )
                        )
                    )

                    // 4. Draw Flying Wood Shavings
                    for (p in viewModel.engine.particles) {
                        drawCircle(
                            color = Color(0xFFFFE082).copy(alpha = p.life),
                            radius = 3.dp.toPx(),
                            center = Offset(p.x * w, p.y * h)
                        )
                    }
                }
            }

            // Finish button or Instructions
            if (state.accuracy >= 0.85f && !state.isFinished) {
                Button(
                    onClick = { viewModel.finishPiece() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SAND & FINISH MASTERPIECE! ✨", fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF261C14),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "👆 Drag your chisel along the spinning wood to carve the silhouette!",
                        color = Color(0xFFBCAAA4),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    // Victory Dialog
    if (state.isFinished) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("MASTERPIECE CRAFTED! 🪵", fontWeight = FontWeight.Bold, color = Color(0xFFD4A373))
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Precision Match: ${(state.accuracy * 100).toInt()}%")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        repeat(state.stars) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.nextLevel() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4A373))
                ) {
                    Text("Next Wood Level", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.restart() }) {
                    Text("Carve Again", color = Color.White)
                }
            },
            containerColor = Color(0xFF261C14)
        )
    }

    // Level Select Dialog
    if (showLevelSelect) {
        AlertDialog(
            onDismissRequest = { showLevelSelect = false },
            title = { Text("Select Project", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    WoodLevels.levels.forEachIndexed { idx, lvl ->
                        Button(
                            onClick = {
                                viewModel.startLevel(idx)
                                showLevelSelect = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (idx == levelIndex) Color(0xFFD4A373) else Color(0xFF3E2723)
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
            containerColor = Color(0xFF261C14)
        )
    }
}
