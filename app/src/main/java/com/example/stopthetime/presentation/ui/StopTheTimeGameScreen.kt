package com.example.stopthetime.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stopthetime.core.engine.StopTheTimeEngine
import com.example.stopthetime.core.model.GameStatus
import com.example.stopthetime.core.model.StopAccuracy
import com.example.stopthetime.core.model.StopGameMode
import com.example.stopthetime.presentation.StopTheTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopTheTimeGameScreen(
    viewModel: StopTheTimeViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()

    // Pulse animation for STOP button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (state.mode) {
                                StopGameMode.CAMPAIGN -> "Level ${state.levelNumber}"
                                StopGameMode.LOCAL_MULTIPLAYER -> "Turn: Player ${state.currentMultiplayerPlayer + 1}"
                                else -> state.mode.displayName
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        if (state.totalRounds > 1) {
                            Text(
                                "Round ${state.currentRound} of ${state.totalRounds}",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (state.currentCombo > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "x${state.currentCombo}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color(0xFF0B1120)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Target Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "TARGET TIME",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${StopTheTimeEngine.formatTime(state.targetTimeMs)}s",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }
            }

            // Central Timer Display
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (state.status) {
                    GameStatus.COUNTDOWN -> {
                        Text(
                            text = if (state.countdownNumber > 0) "${state.countdownNumber}" else "GO!",
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Black,
                            color = if (state.countdownNumber > 0) Color(0xFFF59E0B) else Color(0xFF10B981)
                        )
                    }
                    GameStatus.RUNNING, GameStatus.STOPPED, GameStatus.ROUND_COMPLETE, GameStatus.LEVEL_WON, GameStatus.GAME_OVER -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val displayTime = when {
                                state.isTimerHidden -> "??:??.??"
                                state.status == GameStatus.RUNNING -> StopTheTimeEngine.formatTime(state.elapsedTimeMs)
                                else -> StopTheTimeEngine.formatTime(state.stoppedTimeMs)
                            }

                            val timerColor = when {
                                state.isTimerHidden -> Color(0xFF64748B)
                                state.status == GameStatus.RUNNING -> Color(0xFF10B981)
                                state.accuracy != null -> state.accuracy!!.color
                                else -> Color.White
                            }

                            Text(
                                text = displayTime,
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = timerColor
                            )

                            if (state.isTimerHidden) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "BLIND MODE ACTIVE",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (state.accuracy != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = state.accuracy!!.color.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, state.accuracy!!.color)
                                ) {
                                    Text(
                                        text = "${state.accuracy!!.title} (${StopTheTimeEngine.formatDifference(state.differenceMs, state.stoppedTimeMs, state.targetTimeMs)})",
                                        color = state.accuracy!!.color,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        Text(
                            "Get Ready...",
                            fontSize = 28.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Big Tactile STOP Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .size(160.dp)
            ) {
                val isRunning = state.status == GameStatus.RUNNING

                // Glowing outer ring
                if (isRunning) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.25f))
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(CircleShape)
                        .clickable(enabled = isRunning) {
                            viewModel.onStopPressed()
                        },
                    color = if (isRunning) Color(0xFFEF4444) else Color(0xFF334155),
                    shape = CircleShape,
                    shadowElevation = if (isRunning) 12.dp else 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "STOP",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }
    }

    // Results Dialog (Level won, round complete, or multiplayer end)
    if (state.status == GameStatus.LEVEL_WON || state.status == GameStatus.GAME_OVER || state.status == GameStatus.ROUND_COMPLETE) {
        if (state.mode == StopGameMode.LOCAL_MULTIPLAYER && state.status == GameStatus.ROUND_COMPLETE) {
            MultiplayerPodiumDialog(
                results = state.multiplayerResults,
                onPlayAgain = { viewModel.retryCurrentGame() },
                onHome = onNavigateBack
            )
        } else if (state.status == GameStatus.ROUND_COMPLETE && state.currentRound < state.totalRounds) {
            // Inter-round dialog
            RoundTransitionDialog(
                round = state.currentRound,
                totalRounds = state.totalRounds,
                diffMs = state.differenceMs,
                accuracy = state.accuracy ?: StopAccuracy.GOOD,
                onNextRound = { viewModel.proceedToNextRound() }
            )
        } else {
            SinglePlayerResultDialog(
                state = state,
                onRetry = { viewModel.retryCurrentGame() },
                onNext = {
                    if (state.mode == StopGameMode.CAMPAIGN) {
                        viewModel.nextCampaignLevel()
                    } else {
                        viewModel.retryCurrentGame()
                    }
                },
                onHome = onNavigateBack
            )
        }
    }
}

@Composable
private fun SinglePlayerResultDialog(
    state: com.example.stopthetime.core.model.StopTheTimeGameState,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onHome: () -> Unit
) {
    val isWin = state.stars > 0

    AlertDialog(
        onDismissRequest = {},
        containerColor = Color(0xFF1E293B),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isWin) "STAGE CLEARED!" else "TRY AGAIN",
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = if (isWin) Color(0xFF10B981) else Color(0xFFEF4444)
                )

                if (isWin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.Center) {
                        repeat(3) { idx ->
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = if (idx < state.stars) Color(0xFFFFB703) else Color(0xFF475569),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                state.accuracy?.let { acc ->
                    Text(
                        text = acc.title,
                        color = acc.color,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                }

                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Target Time", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("${StopTheTimeEngine.formatTime(state.targetTimeMs)}s", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Your Time", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("${StopTheTimeEngine.formatTime(state.stoppedTimeMs)}s", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Difference", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text(
                                StopTheTimeEngine.formatDifference(state.differenceMs, state.stoppedTimeMs, state.targetTimeMs),
                                color = state.accuracy?.color ?: Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Score", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("${state.score} pts", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onRetry,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Retry")
                }
                if (isWin) {
                    Button(
                        onClick = onNext,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Next Level", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onHome,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Menu", color = Color.White)
                    }
                }
            }
        }
    )
}

@Composable
private fun RoundTransitionDialog(
    round: Int,
    totalRounds: Int,
    diffMs: Long,
    accuracy: StopAccuracy,
    onNextRound: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = Color(0xFF1E293B),
        title = {
            Text(
                "Round $round Complete!",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    accuracy.title,
                    color = accuracy.color,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Difference: ${diffMs}ms",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onNextRound,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Start Round ${round + 1}", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun MultiplayerPodiumDialog(
    results: List<com.example.stopthetime.core.model.PlayerTurnResult>,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = Color(0xFF1E293B),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFB703),
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Match Results",
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                results.forEachIndexed { rank, res ->
                    val rankColor = when (rank) {
                        0 -> Color(0xFFFFB703) // Gold
                        1 -> Color(0xFF94A3B8) // Silver
                        else -> Color(0xFFB45309) // Bronze
                    }
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(10.dp),
                        border = if (rank == 0) androidx.compose.foundation.BorderStroke(1.5.dp, rankColor) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "#${rank + 1}",
                                    fontWeight = FontWeight.Bold,
                                    color = rankColor,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    res.playerName,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                            Text(
                                "${res.differenceMs}ms",
                                fontWeight = FontWeight.Bold,
                                color = res.accuracy.color,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onHome,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Exit to Menu")
                }
                Button(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Rematch", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    )
}
