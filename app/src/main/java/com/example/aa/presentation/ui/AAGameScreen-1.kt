package com.example.aa.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aa.core.engine.AAEngine
import com.example.aa.presentation.AAViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AAGameScreen(
    viewModel: AAViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val soundEnabled by viewModel.soundEnabledFlow.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(Unit) {
        if (state.remainingBallNumbers.isEmpty() && !state.isGameOver && !state.isWon) {
            viewModel.startLevel(state.levelNumber.coerceAtLeast(1))
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.pauseGame()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Level ${state.levelNumber}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
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
                    IconButton(onClick = { viewModel.restartLevel() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF11111B)
                )
            )
        },
        containerColor = Color(0xFF11111B)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    viewModel.shootBall()
                }
        ) {
            // Main Game Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(size.width / 2f, size.height * 0.38f)
                val targetRadius = 52.dp.toPx()
                val needleLength = 115.dp.toPx()
                val pinBallRadius = 11.dp.toPx()
                val launchY = size.height * 0.85f
                val targetEdgeY = centerOffset.y + targetRadius + needleLength

                // 1. Draw Pinned Needles and Balls
                for (pin in state.pinnedBalls) {
                    val absAngleDeg = AAEngine.normalizeAngle(pin.angleDegrees + state.currentAngle)
                    val rad = Math.toRadians(absAngleDeg.toDouble())
                    val endX = (centerOffset.x + (targetRadius + needleLength) * cos(rad)).toFloat()
                    val endY = (centerOffset.y + (targetRadius + needleLength) * sin(rad)).toFloat()
                    val startX = (centerOffset.x + targetRadius * cos(rad)).toFloat()
                    val startY = (centerOffset.y + targetRadius * sin(rad)).toFloat()

                    // Needle Line
                    drawLine(
                        color = Color(0xFFA6ADC8),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Pin Ball
                    drawCircle(
                        color = if (state.isGameOver) Color(0xFFF38BA8) else Color(0xFFCDD6F4),
                        radius = pinBallRadius,
                        center = Offset(endX, endY)
                    )
                }

                // 2. Center Rotating Wheel
                drawCircle(
                    color = if (state.isGameOver) Color(0xFFF38BA8) else Color.White,
                    radius = targetRadius,
                    center = centerOffset
                )

                // 3. Shooting Ball in flight
                val flying = state.shootingBall
                if (flying != null) {
                    val currentY = launchY - flying.progress * (launchY - targetEdgeY)
                    drawCircle(
                        color = Color(0xFF89B4FA),
                        radius = pinBallRadius,
                        center = Offset(centerOffset.x, currentY)
                    )
                }
            }

            // Central Level Number on Wheel
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Approximate position matching size.height * 0.38f
            }

            // Remaining Balls Counter at Bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Ready-to-Shoot Ball
                val readyBall = state.remainingBallNumbers.firstOrNull()
                if (readyBall != null) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF89B4FA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$readyBall",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF11111B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Next 5 upcoming balls in queue
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.remainingBallNumbers.drop(1).take(5).forEach { num ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF313244)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$num",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    val extraCount = state.remainingBallNumbers.size - 6
                    if (extraCount > 0) {
                        Text(
                            text = "+$extraCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA6ADC8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "TAP TO SHOOT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFA6ADC8).copy(alpha = 0.8f)
                )
            }

            // Game Over Dialog
            if (state.isGameOver) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "COLLISION!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF38BA8),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "A ball collided with an existing needle. Don't give up!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.restartLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF38BA8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("RETRY", fontWeight = FontWeight.Bold, color = Color(0xFF11111B))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("BACK TO LEVELS", color = Color(0xFFA6ADC8))
                        }
                    },
                    containerColor = Color(0xFF1E1E2E)
                )
            }

            // Victory Dialog
            if (state.isWon) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "LEVEL COMPLETE!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFA6E3A1),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "Fantastic precision! All needles pinned perfectly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.nextLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA6E3A1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("NEXT LEVEL", fontWeight = FontWeight.Bold, color = Color(0xFF11111B))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("LEVELS MENU", color = Color(0xFFA6ADC8))
                        }
                    },
                    containerColor = Color(0xFF1E1E2E)
                )
            }
        }
    }
}
