package com.example.ballguys.presentation.ui

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ballguys.engine.BallGuysEngine
import com.example.ballguys.model.BallTiers
import com.example.ballguys.presentation.BallGuysViewModel
import com.zubaluba.gamehub.ads.AdBanner
import com.zubaluba.gamehub.ads.UnifiedAdManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BallGuysScreen(
    viewModel: BallGuysViewModel,
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
                            text = "Ball Guys",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD166),
                            fontSize = 18.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⭐ ${state.score}",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "BEST: ${state.bestScore}",
                                color = Color(0xFFB0BEC5),
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("ballguys_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.restart() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2B1D3A))
            )
        },
        bottomBar = {
            AdBanner(modifier = Modifier.fillMaxWidth().navigationBarsPadding())
        },
        containerColor = Color(0xFF191024)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Next Ball Preview Banner
            val nextTier = BallTiers.tiers[state.nextDropTier - 1]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2B1D3A))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NEXT GUY:",
                    color = Color(0xFFB0BEC5),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(nextTier.color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${nextTier.name} ${nextTier.eyeEmoji}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Glass Container Canvas with Aim & Drop
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF221630))
                    .border(3.dp, Color(0xFF4A3468), RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                change.consume()
                                viewModel.setAimX(change.position.x / size.width)
                            },
                            onDragEnd = {
                                viewModel.dropBall()
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            viewModel.setAimX(offset.x / size.width)
                            viewModel.dropBall()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    viewModel.boxWidth = size.width
                    viewModel.boxHeight = size.height

                    val w = size.width
                    val h = size.height

                    // 1. Danger Line
                    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    drawLine(
                        color = Color(0xFFFF1744).copy(alpha = 0.5f),
                        start = Offset(0f, BallGuysEngine.DANGER_LINE_Y),
                        end = Offset(w, BallGuysEngine.DANGER_LINE_Y),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = dash
                    )

                    // 2. Drop Aim Guide Line & Ready Ball
                    val aimPx = state.dropAimX * w
                    drawLine(
                        color = Color.White.copy(alpha = 0.25f),
                        start = Offset(aimPx, 60f),
                        end = Offset(aimPx, h),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = dash
                    )

                    val curTier = BallTiers.tiers[state.currentDropTier - 1]
                    drawCircle(
                        color = curTier.color,
                        radius = curTier.radius,
                        center = Offset(aimPx, 60f)
                    )

                    // 3. Draw All Balls in Container
                    for (b in state.balls) {
                        val tier = BallTiers.tiers[b.tierLevel - 1]

                        // Ball Body
                        drawCircle(
                            color = tier.color,
                            radius = b.radius,
                            center = Offset(b.x, b.y)
                        )

                        // Highlight sheen
                        drawCircle(
                            color = Color.White.copy(alpha = 0.35f),
                            radius = b.radius * 0.35f,
                            center = Offset(b.x - b.radius * 0.3f, b.y - b.radius * 0.3f)
                        )

                        // Expressive Eyes
                        val eyeR = b.radius * 0.16f
                        val eyeOffset = b.radius * 0.3f
                        // Left eye
                        drawCircle(color = Color.White, radius = eyeR, center = Offset(b.x - eyeOffset, b.y - b.radius * 0.1f))
                        drawCircle(color = Color.Black, radius = eyeR * 0.55f, center = Offset(b.x - eyeOffset, b.y - b.radius * 0.1f))
                        // Right eye
                        drawCircle(color = Color.White, radius = eyeR, center = Offset(b.x + eyeOffset, b.y - b.radius * 0.1f))
                        drawCircle(color = Color.Black, radius = eyeR * 0.55f, center = Offset(b.x + eyeOffset, b.y - b.radius * 0.1f))

                        // Cheerful Smile
                        val mouthY = b.y + b.radius * 0.25f
                        drawLine(
                            color = Color.Black.copy(alpha = 0.7f),
                            start = Offset(b.x - b.radius * 0.18f, mouthY),
                            end = Offset(b.x + b.radius * 0.18f, mouthY),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF2B1D3A),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text(
                    text = "👇 Drag & release to drop! Merge matching balls to reach King Ball! 👑",
                    color = Color(0xFFD1C4E9),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }

    // Game Over Dialog
    if (state.isGameOver) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("CONTAINER OVERFLOW! 💥", fontWeight = FontWeight.Bold, color = Color(0xFFFF1744))
            },
            text = {
                Column {
                    Text("The ball guys spilled above the danger line!")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Final Score: ${state.score}", fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                    Text("Best Score: ${state.bestScore}")
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.restart() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166))
                ) {
                    Text("Drop Again", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            containerColor = Color(0xFF2B1D3A)
        )
    }
}
