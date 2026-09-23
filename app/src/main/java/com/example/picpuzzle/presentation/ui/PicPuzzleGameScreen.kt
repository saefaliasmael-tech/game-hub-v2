package com.example.picpuzzle.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.picpuzzle.presentation.PicPuzzleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PicPuzzleGameScreen(
    viewModel: PicPuzzleViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val soundEnabled by viewModel.soundEnabledFlow.collectAsState()

    LaunchedEffect(Unit) {
        if (state.tiles.isEmpty() && !state.isSolved) {
            viewModel.startLevel(state.levelNumber.coerceAtLeast(1))
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopTimer()
        }
    }

    val formattedTime = remember(state.elapsedTimeSeconds) {
        val mins = state.elapsedTimeSeconds / 60
        val secs = state.elapsedTimeSeconds % 60
        String.format("%02d:%02d", mins, secs)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Level ${state.levelNumber}: ${state.theme.name}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Moves: ${state.movesCount} | Time: $formattedTime",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF74C69D)
                        )
                    }
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
                    // Number hint toggle
                    IconButton(onClick = { viewModel.toggleHints() }) {
                        Icon(
                            imageVector = Icons.Default.Numbers,
                            contentDescription = "Number Hints",
                            tint = if (state.showNumberHints) Color(0xFFFFD166) else Color(0xFF74C69D)
                        )
                    }
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1F2421))
            )
        },
        containerColor = Color(0xFF141815)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Target Thumbnail Preview
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2421)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF40916C), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Target Artwork",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "${state.gridSize}x${state.gridSize} Sliding Mosaic",
                            fontSize = 12.sp,
                            color = Color(0xFF95D5B2)
                        )
                    }

                    // Mini preview canvas
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        FullArtworkView(theme = state.theme, modifier = Modifier.fillMaxSize())
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Puzzle Grid Board
            val tilesByPosition = remember(state.tiles) {
                state.tiles.associateBy { it.currentPos }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0B0E0C))
                    .border(2.dp, Color(0xFF2D6A4F), RoundedCornerShape(18.dp))
                    .padding(6.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (row in 0 until state.gridSize) {
                        Row(modifier = Modifier.weight(1f)) {
                            for (col in 0 until state.gridSize) {
                                val pos = row * state.gridSize + col
                                val tile = tilesByPosition[pos]

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(3.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (tile?.isEmpty == true) Color(0xFF141815) else Color.Transparent)
                                        .clickable(enabled = tile?.isEmpty == false) {
                                            tile?.let { viewModel.onTileClick(it.id) }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (tile != null && !tile.isEmpty) {
                                        // Render tile slice of artwork
                                        TileArtworkView(
                                            theme = state.theme,
                                            correctPos = tile.correctPos,
                                            gridSize = state.gridSize
                                        )

                                        // Subtle border around tile
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                        )

                                        // Optional Numeric Hint badge
                                        if (state.showNumberHints) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(4.dp)
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Black.copy(alpha = 0.65f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${tile.correctPos + 1}",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Solved Celebration Alert Dialog
            if (state.isSolved) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "MASTERPIECE RESTORED!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF74C69D),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "You assembled ${state.theme.name} in ${state.movesCount} moves and $formattedTime!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(2.dp, Color(0xFF52B788), RoundedCornerShape(12.dp))
                            ) {
                                FullArtworkView(theme = state.theme, modifier = Modifier.fillMaxSize())
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.nextLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF52B788)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("NEXT MASTERPIECE", fontWeight = FontWeight.Bold, color = Color(0xFF141815))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("GALLERY MENU", color = Color(0xFF95D5B2))
                        }
                    },
                    containerColor = Color(0xFF1F2421)
                )
            }
        }
    }
}
