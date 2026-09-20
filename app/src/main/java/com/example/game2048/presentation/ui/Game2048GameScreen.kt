package com.example.game2048.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game2048.core.model.MoveDirection
import com.example.game2048.presentation.Game2048ViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Game2048GameScreen(
    viewModel: Game2048ViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("2048", fontWeight = FontWeight.Bold, color = Color.White)
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("game2048_game_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Menu",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.startNewGame() },
                        modifier = Modifier.testTag("game2048_restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "New Game",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B2A32)
                )
            )
        },
        containerColor = Color(0xFF0F171C)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Score Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScoreCard(
                    title = "SCORE",
                    value = "${state.score}",
                    modifier = Modifier.weight(1f).testTag("game2048_current_score")
                )
                ScoreCard(
                    title = "BEST",
                    value = "${state.bestScore}",
                    modifier = Modifier.weight(1f).testTag("game2048_best_score")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4x4 Grid Board with Swipe Detection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFBBADA0))
                    .padding(8.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                totalDragX = 0f
                                totalDragY = 0f
                            },
                            onDragEnd = {
                                val minSwipeDistance = 50f
                                if (abs(totalDragX) > abs(totalDragY)) {
                                    if (totalDragX > minSwipeDistance) {
                                        viewModel.onMove(MoveDirection.RIGHT)
                                    } else if (totalDragX < -minSwipeDistance) {
                                        viewModel.onMove(MoveDirection.LEFT)
                                    }
                                } else {
                                    if (totalDragY > minSwipeDistance) {
                                        viewModel.onMove(MoveDirection.DOWN)
                                    } else if (totalDragY < -minSwipeDistance) {
                                        viewModel.onMove(MoveDirection.UP)
                                    }
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                totalDragX += dragAmount.x
                                totalDragY += dragAmount.y
                            }
                        )
                    }
                    .testTag("game2048_board"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (row in 0 until 4) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0 until 4) {
                                val tileValue = state.getTile(row, col)
                                TileCell(
                                    value = tileValue,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Accessibility Directional Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("game2048_directional_pad")
            ) {
                IconButton(
                    onClick = { viewModel.onMove(MoveDirection.UP) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF1E2E38), RoundedCornerShape(12.dp))
                        .testTag("btn_move_up")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Move Up",
                        tint = Color.White
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.onMove(MoveDirection.LEFT) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF1E2E38), RoundedCornerShape(12.dp))
                            .testTag("btn_move_left")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Move Left",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { viewModel.onMove(MoveDirection.DOWN) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF1E2E38), RoundedCornerShape(12.dp))
                            .testTag("btn_move_down")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move Down",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { viewModel.onMove(MoveDirection.RIGHT) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF1E2E38), RoundedCornerShape(12.dp))
                            .testTag("btn_move_right")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Move Right",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Win Dialog
        if (state.isWon) {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(
                        text = "You Win! 2048 Reached!",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE9C46A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = "Congratulations! You reached the legendary 2048 tile! Do you want to keep playing for a higher score?",
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.continuePlayingAfterWin() },
                        modifier = Modifier.testTag("win_continue_button")
                    ) {
                        Text("Keep Going")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.startNewGame() },
                        modifier = Modifier.testTag("win_restart_button")
                    ) {
                        Text("New Game")
                    }
                }
            )
        }

        // Game Over Dialog
        if (state.isGameOver) {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(
                        text = "Game Over!",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No moves left on the board.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Final Score: ${state.score}", fontWeight = FontWeight.Bold)
                        Text("Best Score: ${state.bestScore}", color = Color(0xFFE9C46A))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.startNewGame() },
                        modifier = Modifier.testTag("game_over_restart_button")
                    ) {
                        Text("Try Again")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("game_over_menu_button")
                    ) {
                        Text("Main Menu")
                    }
                }
            )
        }
    }
}

@Composable
fun ScoreCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2E38))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun TileCell(
    value: Int,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = getTileColors(value)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (value > 0) {
            Text(
                text = "$value",
                fontWeight = FontWeight.Black,
                fontSize = when {
                    value < 100 -> 24.sp
                    value < 1000 -> 20.sp
                    value < 10000 -> 16.sp
                    else -> 13.sp
                },
                color = textColor
            )
        }
    }
}

fun getTileColors(value: Int): Pair<Color, Color> {
    return when (value) {
        0 -> Pair(Color(0xFFCDC1B4), Color.Transparent)
        2 -> Pair(Color(0xFFEEE4DA), Color(0xFF776E65))
        4 -> Pair(Color(0xFFEDE0C8), Color(0xFF776E65))
        8 -> Pair(Color(0xFFF2B179), Color.White)
        16 -> Pair(Color(0xFFF59563), Color.White)
        32 -> Pair(Color(0xFFF67C5F), Color.White)
        64 -> Pair(Color(0xFFF65E3B), Color.White)
        128 -> Pair(Color(0xFFEDCF72), Color.White)
        256 -> Pair(Color(0xFFEDCC61), Color.White)
        512 -> Pair(Color(0xFFEDC850), Color.White)
        1024 -> Pair(Color(0xFFEDC53F), Color.White)
        2048 -> Pair(Color(0xFFEDC22E), Color.White)
        else -> Pair(Color(0xFF3C3A32), Color.White)
    }
}
