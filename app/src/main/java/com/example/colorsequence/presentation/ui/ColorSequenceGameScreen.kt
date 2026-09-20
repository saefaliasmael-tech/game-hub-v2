package com.example.colorsequence.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorsequence.core.model.GamePhase
import com.example.colorsequence.core.model.SequenceColor
import com.example.colorsequence.presentation.ColorSequenceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSequenceGameScreen(
    viewModel: ColorSequenceViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val isColorBlindMode by viewModel.isColorBlindMode.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.exitGame()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Level ${state?.levelNumber ?: 1}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("color_seq_game_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Level Select",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.retryCurrentLevel() },
                        modifier = Modifier.testTag("color_seq_retry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart Level",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1035)
                )
            )
        },
        containerColor = Color(0xFF0F071D)
    ) { paddingValues ->
        val currentState = state
        if (currentState == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF8B5CF6))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Instruction Status Banner
            val statusText = when (currentState.phase) {
                GamePhase.SHOWING_SEQUENCE -> "Memorize the sequence..."
                GamePhase.AWAITING_INPUT -> "Your turn! Repeat the pattern"
                GamePhase.LEVEL_WON -> "Level Completed!"
                GamePhase.LEVEL_FAILED -> "Incorrect Sequence!"
                else -> "Get Ready..."
            }

            val statusColor = when (currentState.phase) {
                GamePhase.SHOWING_SEQUENCE -> Color(0xFFA78BFA)
                GamePhase.AWAITING_INPUT -> Color(0xFF38BDF8)
                GamePhase.LEVEL_WON -> Color(0xFF4ADE80)
                GamePhase.LEVEL_FAILED -> Color(0xFFF87171)
                else -> Color.White
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = statusColor.copy(alpha = 0.15f),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(statusColor)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                )
            }

            // Sequence Step Progress Indicators
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                for (i in 0 until currentState.targetSequence.size) {
                    val isDone = i < currentState.playerInput.size
                    val isCurrentShowing = currentState.phase == GamePhase.SHOWING_SEQUENCE && currentState.activeDisplayIndex == i

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (isCurrentShowing) 18.dp else 14.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCurrentShowing -> currentState.activeDisplayColor?.color ?: Color.White
                                    isDone -> Color(0xFF22C55E)
                                    else -> Color.White.copy(alpha = 0.25f)
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (isCurrentShowing) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                    )
                }
            }

            // Sequence Showcase Display Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1E1035))
                    .border(
                        width = 2.dp,
                        color = currentState.activeDisplayColor?.color ?: Color(0xFF3B1E6D),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("sequence_display_box"),
                contentAlignment = Alignment.Center
            ) {
                val activeColor = currentState.activeDisplayColor
                if (activeColor != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(activeColor.color)
                                .border(3.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isColorBlindMode) {
                                Text(
                                    text = activeColor.symbol,
                                    fontSize = 42.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = activeColor.displayName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                } else {
                    Text(
                        text = if (currentState.phase == GamePhase.SHOWING_SEQUENCE) "..." else "Tap the colors below",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 16.sp
                    )
                }
            }

            // Color Input Grid for Player
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val pool = currentState.availablePool
                val isInputEnabled = currentState.phase == GamePhase.AWAITING_INPUT

                // 2 columns grid of large interactive buttons
                val rows = pool.chunked(2)
                for (row in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (colorItem in row) {
                            Button(
                                onClick = { viewModel.onPlayerColorTap(colorItem) },
                                enabled = isInputEnabled,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .padding(vertical = 6.dp)
                                    .testTag("color_btn_${colorItem.name.lowercase()}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorItem.color,
                                    disabledContainerColor = colorItem.color.copy(alpha = 0.35f)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isColorBlindMode) {
                                        Text(
                                            text = colorItem.symbol,
                                            fontSize = 24.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Black
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(
                                        text = colorItem.displayName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Win Dialog
        if (state?.phase == GamePhase.LEVEL_WON) {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(
                        text = "Level Complete!",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            val stars = state?.starsAwarded ?: 0
                            repeat(3) { starIndex ->
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (starIndex < stars) Color(0xFFFFD166) else Color.Gray,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Score: ${state?.score ?: 0}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.playNextLevel() },
                        modifier = Modifier.testTag("win_next_level_button")
                    ) {
                        Text("Next Level")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("win_menu_button")
                    ) {
                        Text("Levels")
                    }
                }
            )
        }

        // Failed Dialog
        if (state?.phase == GamePhase.LEVEL_FAILED) {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(
                        text = "Pattern Missed!",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = "You tapped an incorrect color in the sequence. Would you like to try again?",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.retryCurrentLevel() },
                        modifier = Modifier.testTag("fail_retry_button")
                    ) {
                        Text("Try Again")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("fail_menu_button")
                    ) {
                        Text("Exit")
                    }
                }
            )
        }
    }
}
