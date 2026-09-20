package com.example.memory.presentation.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memory.core.model.MemoryCard
import com.example.memory.core.model.MemoryGameMode
import com.example.memory.core.model.MemoryTheme
import com.example.memory.presentation.MemoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryGameScreen(
    viewModel: MemoryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsState()

    val iconMap = remember(state.theme) {
        state.theme.getIconList().toMap()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${state.theme.displayName} Memory",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${state.mode.displayName} • ${state.boardSize.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("memory_game_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Timer or Moves countdown
                    if (state.mode == MemoryGameMode.TIMED) {
                        val mins = state.timeRemainingSeconds / 60
                        val secs = state.timeRemainingSeconds % 60
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = null,
                                tint = if (state.timeRemainingSeconds <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%02d:%02d".format(mins, secs),
                                fontWeight = FontWeight.Bold,
                                color = if (state.timeRemainingSeconds <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else if (state.mode == MemoryGameMode.LIMITED_MOVES) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Sync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${state.movesRemaining} left",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        val mins = state.elapsedTimeSeconds / 60
                        val secs = state.elapsedTimeSeconds % 60
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%02d:%02d".format(mins, secs),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Restart
                    IconButton(onClick = { viewModel.restartGame() }) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Restart")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Live Stats Ribbon
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.matchesCount} / ${state.totalPairs}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Pairs", style = MaterialTheme.typography.labelSmall)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.movesCount}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text("Moves", style = MaterialTheme.typography.labelSmall)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%.0f%%".format(state.accuracy),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF10B981)
                        )
                        Text("Accuracy", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dynamic Cards Grid
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(state.boardSize.cols),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(state.cards) { index, card ->
                        FlipCardView(
                            card = card,
                            iconMap = iconMap,
                            onClick = { viewModel.onCardClicked(index) }
                        )
                    }
                }
            }
        }
    }

    // Win Dialog
    if (state.isWon) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("All Pairs Matched!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        repeat(3) { starIndex ->
                            val active = starIndex < state.starsAwarded
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (active) Color(0xFFFFB703) else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        text = "Score: ${state.score}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Total Moves: ${state.movesCount}")
                    Text("Accuracy: %.1f%%".format(state.accuracy), color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold)

                    val mins = state.elapsedTimeSeconds / 60
                    val secs = state.elapsedTimeSeconds % 60
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Time: %02d:%02d".format(mins, secs), style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.restartGame() }) {
                    Text("Play Again")
                }
            },
            dismissButton = {
                TextButton(onClick = onNavigateBack) {
                    Text("Menu")
                }
            }
        )
    }

    // Game Over Dialog (For Timed / Moves Fail)
    if (state.isGameOver && !state.isWon) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Cancel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Game Over", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = if (state.mode == MemoryGameMode.TIMED) "Time ran out before finding all pairs!" else "Out of moves!"
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.restartGame() }) {
                    Text("Try Again")
                }
            },
            dismissButton = {
                TextButton(onClick = onNavigateBack) {
                    Text("Menu")
                }
            }
        )
    }
}

@Composable
private fun FlipCardView(
    card: MemoryCard,
    iconMap: Map<String, androidx.compose.ui.graphics.vector.ImageVector>,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "card_flip"
    )

    val isFrontVisible = rotation > 90f

    Box(
        modifier = Modifier
            .aspectRatio(0.85f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !card.isFlipped && !card.isMatched) { onClick() }
            .testTag("memory_card_${card.id}"),
        contentAlignment = Alignment.Center
    ) {
        if (!isFrontVisible) {
            // Card Back Face
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    )
                    .border(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.HelpOutline,
                    contentDescription = "Hidden Card",
                    tint = Color(0xFF38BDF8).copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            // Card Front Face (Mirror rotation applied so icon is right-side up)
            val frontBg = when {
                card.isMatched -> Brush.linearGradient(listOf(Color(0xFF065F46), Color(0xFF047857)))
                else -> Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF2563EB)))
            }
            val frontBorder = if (card.isMatched) Color(0xFF34D399) else Color(0xFF60A5FA)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .background(frontBg)
                    .border(2.dp, frontBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val iconVector = iconMap[card.iconName] ?: Icons.Filled.Star
                Icon(
                    imageVector = iconVector,
                    contentDescription = card.iconName,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
