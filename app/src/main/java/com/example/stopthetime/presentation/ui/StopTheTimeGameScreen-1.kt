package com.example.stopthetime.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.stopthetime.core.engine.StopTheTimeEngine
import com.example.stopthetime.core.model.StopAccuracy
import com.example.stopthetime.core.model.StopGameMode
import com.example.stopthetime.core.model.StopStatePhase
import com.example.stopthetime.presentation.StopTheTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopTheTimeGameScreen(
    viewModel: StopTheTimeViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        val titleText = if (state.mode == StopGameMode.CAMPAIGN) {
                            "Level ${state.levelNumber} (${state.difficulty.title})"
                        } else if (state.mode == StopGameMode.MULTIPLAYER) {
                            "Player ${state.currentPlayer}'s Turn"
                        } else {
                            state.mode.title
                        }
                        Text(
                            text = titleText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (state.combo > 1) {
                            Text(
                                text = "Combo x${state.combo}!",
                                fontSize = 12.sp,
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("stoptime_game_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.retryLevel() },
                        modifier = Modifier.testTag("stoptime_restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Restart",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E88E5))
            )
        },
        containerColor = Color(0xFF0A192F)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Target Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF172A45)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TARGET TIME",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64FFDA)
                    )
                    Text(
                        text = StopTheTimeEngine.formatTime(state.targetTimeMs) + "s",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                    if (state.isBlind) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "BLIND MODE (Timer hides after ${(state.blindHideAfterMs / 1000f)}s)",
                            fontSize = 11.sp,
                            color = Color(0xFFFF8A80),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Big Central Stopwatch Display
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF1E3A5F), Color(0xFF0D1B2A))
                        )
                    )
                    .border(
                        width = 6.dp,
                        brush = Brush.sweepGradient(
                            listOf(Color(0xFF64FFDA), Color(0xFF00B0FF), Color(0xFF64FFDA))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val displayTime = when {
                        state.phase == StopStatePhase.IDLE -> "00.00"
                        state.isTimerHidden -> "??.??"
                        state.phase == StopStatePhase.RUNNING -> StopTheTimeEngine.formatTime(state.currentTimeMs)
                        else -> StopTheTimeEngine.formatTime(state.stoppedTimeMs)
                    }

                    Text(
                        text = displayTime,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (state.isTimerHidden) Color(0xFFFF8A80) else Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    state.accuracy?.let { acc ->
                        val accColor = when (acc) {
                            StopAccuracy.PERFECT -> Color(0xFF64FFDA)
                            StopAccuracy.EXCELLENT -> Color(0xFF00E676)
                            StopAccuracy.GREAT -> Color(0xFFFFEB3B)
                            StopAccuracy.GOOD -> Color(0xFFFF9800)
                            StopAccuracy.MISS -> Color(0xFFFF5252)
                        }
                        Text(
                            text = acc.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accColor
                        )
                        Text(
                            text = StopTheTimeEngine.formatDifference(
                                state.differenceMs,
                                state.stoppedTimeMs,
                                state.targetTimeMs
                            ),
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Big Tap Button (Start / Stop)
            val isRunning = state.phase == StopStatePhase.RUNNING
            Button(
                onClick = {
                    if (isRunning) {
                        viewModel.stopTimer()
                    } else if (state.phase == StopStatePhase.IDLE) {
                        viewModel.startTimer()
                    } else if (state.phase == StopStatePhase.STOPPED && state.mode == StopGameMode.MULTIPLAYER) {
                        // Switch to player 2
                        viewModel.startTimer()
                    } else {
                        viewModel.retryLevel()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFE53935) else Color(0xFF00C853)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("stoptime_action_button")
            ) {
                Text(
                    text = when {
                        isRunning -> "STOP!"
                        state.phase == StopStatePhase.IDLE -> "START"
                        state.phase == StopStatePhase.STOPPED && state.mode == StopGameMode.MULTIPLAYER -> "PLAYER 2 START"
                        else -> "TRY AGAIN"
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }

    // Win Dialog
    if (state.phase == StopStatePhase.WON && state.mode != StopGameMode.MULTIPLAYER) {
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF172A45)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("stoptime_victory_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Target Hit!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64FFDA)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Diff: ${StopTheTimeEngine.formatDifference(state.differenceMs, state.stoppedTimeMs, state.targetTimeMs)}",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 1..3) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (i <= state.stars) Color(0xFFFFD54F) else Color.Gray,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.retryLevel() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Replay", color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.nextLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("stoptime_next_level_button")
                        ) {
                            Text("Next Level", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Multiplayer Winner Dialog
    if (state.mode == StopGameMode.MULTIPLAYER && state.multiplayerWinner != null) {
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF172A45)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val winnerText = when (state.multiplayerWinner) {
                        1 -> "Player 1 Wins!"
                        2 -> "Player 2 Wins!"
                        else -> "It's a Tie!"
                    }
                    Text(
                        text = winnerText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64FFDA)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Player 1 diff: ${state.p1DiffMs}ms\nPlayer 2 diff: ${state.p2DiffMs}ms",
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Back to Menu", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
