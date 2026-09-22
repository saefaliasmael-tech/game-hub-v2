package com.example.unblockme.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.unblockme.core.engine.UnblockEngine
import com.example.unblockme.core.model.Block
import com.example.unblockme.core.model.Orientation
import com.example.unblockme.presentation.UnblockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnblockGameScreen(
    viewModel: UnblockViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Level ${state.levelNumber} - ${state.difficulty.title}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Moves: ${state.moveCount} (Min: ${state.minMoves})",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("unblock_game_back")
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
                        onClick = { viewModel.provideHint() },
                        modifier = Modifier.testTag("unblock_hint_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Hint",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { viewModel.resetLevel() },
                        modifier = Modifier.testTag("unblock_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reset",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF8D6E63))
            )
        },
        containerColor = Color(0xFFEFEBE9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Board Area
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFD7CCC8))
                    .border(4.dp, Color(0xFF8D6E63), RoundedCornerShape(16.dp))
                    .padding(8.dp)
                    .testTag("unblock_board"),
                contentAlignment = Alignment.CenterStart
            ) {
                val boardSize = maxWidth
                val cellSize = (boardSize - 8.dp) / UnblockEngine.GRID_SIZE

                // Grid background cells
                Column(modifier = Modifier.fillMaxSize()) {
                    for (r in 0 until UnblockEngine.GRID_SIZE) {
                        Row(modifier = Modifier.weight(1f)) {
                            for (c in 0 until UnblockEngine.GRID_SIZE) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(2.dp)
                                        .background(
                                            Color(0xFFBCAAA4).copy(alpha = 0.4f),
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
                    }
                }

                // Blocks rendering
                state.blocks.forEach { block ->
                    BlockItem(
                        block = block,
                        cellSize = cellSize,
                        isHinted = state.hintMove?.first == block.id,
                        onMove = { delta ->
                            viewModel.moveBlock(block.id, delta)
                        }
                    )
                }

                // Exit Indicator on Row 2 right edge
                Box(
                    modifier = Modifier
                        .offset(x = boardSize - 6.dp, y = (cellSize * 2) + 4.dp)
                        .size(width = 8.dp, height = cellSize - 8.dp)
                        .background(Color(0xFFE53935), RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                )
            }

            // Bottom Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.undo() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("unblock_undo_button")
                ) {
                    Text("Undo", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.resetLevel() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA1887F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Restart", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Victory Dialog
    if (state.isWon) {
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("unblock_victory_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Level Completed!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E342E)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Solved in ${state.moveCount} moves",
                        fontSize = 16.sp,
                        color = Color(0xFF6D4C41)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Stars
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 1..3) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (i <= state.stars) Color(0xFFFFB300) else Color.LightGray,
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
                            onClick = { viewModel.resetLevel() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Replay")
                        }

                        Button(
                            onClick = { viewModel.nextLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("unblock_next_level_button")
                        ) {
                            Text("Next Level", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockItem(
    block: Block,
    cellSize: Dp,
    isHinted: Boolean,
    onMove: (Int) -> Unit
) {
    val density = LocalDensity.current
    var dragAccumulator by remember { mutableStateOf(0f) }

    val width = if (block.orientation == Orientation.HORIZONTAL) cellSize * block.length else cellSize
    val height = if (block.orientation == Orientation.VERTICAL) cellSize * block.length else cellSize

    val xOffset = cellSize * block.col
    val yOffset = cellSize * block.row

    val blockColor = when {
        block.isTarget -> Color(0xFFE53935)
        isHinted -> Color(0xFFFFB300)
        block.length == 3 -> Color(0xFF5D4037)
        else -> Color(0xFF8D6E63)
    }

    Box(
        modifier = Modifier
            .offset(x = xOffset, y = yOffset)
            .size(width = width, height = height)
            .padding(2.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(blockColor, RoundedCornerShape(8.dp))
            .border(
                width = if (isHinted) 2.dp else 1.dp,
                color = if (isHinted) Color.Yellow else Color(0x40FFFFFF),
                shape = RoundedCornerShape(8.dp)
            )
            .pointerInput(block.id, block.row, block.col) {
                detectDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onDragEnd = { dragAccumulator = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val cellPx = with(density) { cellSize.toPx() }
                        if (block.orientation == Orientation.HORIZONTAL) {
                            dragAccumulator += dragAmount.x
                            if (dragAccumulator >= cellPx * 0.45f) {
                                onMove(1)
                                dragAccumulator = 0f
                            } else if (dragAccumulator <= -cellPx * 0.45f) {
                                onMove(-1)
                                dragAccumulator = 0f
                            }
                        } else {
                            dragAccumulator += dragAmount.y
                            if (dragAccumulator >= cellPx * 0.45f) {
                                onMove(1)
                                dragAccumulator = 0f
                            } else if (dragAccumulator <= -cellPx * 0.45f) {
                                onMove(-1)
                                dragAccumulator = 0f
                            }
                        }
                    }
                )
            }
            .testTag("block_${block.id}"),
        contentAlignment = Alignment.Center
    ) {
        if (block.isTarget) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Target",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
