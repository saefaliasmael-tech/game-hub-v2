package com.example.holeio.presentation.ui

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holeio.presentation.HoleIoViewModel
import com.zubaluba.gamehub.ads.AdBanner
import com.zubaluba.gamehub.ads.UnifiedAdManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoleIoScreen(
    viewModel: HoleIoViewModel,
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
                            text = "Hole.io",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF48CAE4),
                            fontSize = 18.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏱️ ${state.timeLeftSeconds}s",
                                color = if (state.timeLeftSeconds <= 10) Color(0xFFFF5252) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "SCORE: ${state.playerHole.score}",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("holeio_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF48CAE4))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.restart() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color(0xFF48CAE4))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        bottomBar = {
            AdBanner(modifier = Modifier.fillMaxWidth().navigationBarsPadding())
        },
        containerColor = Color(0xFF070B14)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Hole Radius / Growth Gauge
            LinearProgressIndicator(
                progress = { (state.playerHole.radius / 110f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF48CAE4),
                trackColor = Color(0xFF1E293B)
            )

            // Dynamic City Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                viewModel.dragTargetX = state.playerHole.x + (offset.x - cx) * 1.5f
                                viewModel.dragTargetY = state.playerHole.y + (offset.y - cy) * 1.5f
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                viewModel.dragTargetX = state.playerHole.x + (change.position.x - cx) * 1.5f
                                viewModel.dragTargetY = state.playerHole.y + (change.position.y - cy) * 1.5f
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val player = state.playerHole

                    // Camera translates world so player hole stays at screen center
                    val camX = w / 2f - player.x
                    val camY = h / 2f - player.y

                    // 1. Draw City Ground (Subtle asphalt blocks)
                    drawRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(camX, camY),
                        size = Size(1200f, 1200f)
                    )

                    // 2. Draw City Objects (Cones, Trees, Cars, Buildings)
                    for (obj in state.objects) {
                        if (obj.isEaten) continue
                        val ox = camX + obj.x
                        val oy = camY + obj.y

                        // Only draw if visible in screen bounds
                        if (ox in -80f..(w + 80f) && oy in -80f..(h + 80f)) {
                            drawCircle(
                                color = obj.color,
                                radius = obj.radius,
                                center = Offset(ox, oy)
                            )
                            // Highlight on object
                            drawCircle(
                                color = Color.White.copy(alpha = 0.3f),
                                radius = obj.radius * 0.4f,
                                center = Offset(ox - obj.radius * 0.25f, oy - obj.radius * 0.25f)
                            )
                        }
                    }

                    // 3. Draw AI Bot Holes
                    for (bot in state.botHoles) {
                        val bx = camX + bot.x
                        val by = camY + bot.y
                        if (bx in -150f..(w + 150f) && by in -150f..(h + 150f)) {
                            // Bot Hole Abyss
                            drawCircle(color = Color.Black, radius = bot.radius, center = Offset(bx, by))
                            // Bot Neon Rim
                            drawCircle(color = bot.color, radius = bot.radius, center = Offset(bx, by), style = Stroke(width = 3.dp.toPx()))
                        }
                    }

                    // 4. Draw Player Black Hole (Centered on screen)
                    val px = w / 2f
                    val py = h / 2f
                    val r = player.radius

                    // Gravitational warp shadow
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                        radius = r * 1.35f,
                        center = Offset(px, py)
                    )

                    // Deep Black Abyss
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF000000), Color(0xFF03071E), Color(0xFF0F172A)),
                            center = Offset(px, py),
                            radius = r
                        ),
                        radius = r,
                        center = Offset(px, py)
                    )

                    // Glowing Cyan Rim
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = r,
                        center = Offset(px, py),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
            }

            Surface(
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🕳️ Drag to move! Consume small objects first to grow bigger!",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    // Round Over Dialog
    if (state.isGameOver) {
        val allHoles = (listOf(state.playerHole) + state.botHoles).sortedByDescending { it.score }
        val playerRank = allHoles.indexOfFirst { it.id == 0 } + 1

        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("ROUND FINISHED! 🏆", fontWeight = FontWeight.Bold, color = Color(0xFF48CAE4))
            },
            text = {
                Column {
                    Text(
                        text = if (playerRank == 1) "🥇 1ST PLACE CHAMPION!" else "Place: #$playerRank",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F),
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Your Score: ${state.playerHole.score}")
                    Text("Best Score: ${state.bestScore}")
                    Spacer(modifier = Modifier.height(8.dp))
                    allHoles.forEachIndexed { idx, h ->
                        Text("#${idx + 1} ${h.name}: ${h.score} pts", color = h.color, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.restart() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF48CAE4))
                ) {
                    Text("Play Again", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }
}
