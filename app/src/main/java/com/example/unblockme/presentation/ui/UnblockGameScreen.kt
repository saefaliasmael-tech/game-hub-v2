package com.example.unblockme.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unblockme.core.model.Block
import com.example.unblockme.core.model.Orientation
import com.example.unblockme.core.solver.UnblockSolver
import com.example.unblockme.presentation.UnblockViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnblockGameScreen(
    viewModel: UnblockViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsState()
    val density = LocalDensity.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Level ${state.levelNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${state.difficulty.displayName} • Optimal: ${state.minMoves} moves",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("unblock_game_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Timer
                    val mins = state.elapsedTimeSeconds / 60
                    val secs = state.elapsedTimeSeconds % 60
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
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

                    // Hint button
                    IconButton(
                        onClick = { viewModel.requestHint() },
                        enabled = !state.isWon
                    ) {
                        BadgedBox(
                            badge = {
                                if (state.hintsUsed > 0) {
                                    Badge { Text("${state.hintsUsed}") }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Filled.Lightbulb, contentDescription = "Hint")
                        }
                    }

                    // Reset button
                    IconButton(onClick = { viewModel.resetLevel() }) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Reset")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Hint Message Banner
            AnimatedVisibility(visible = state.hintMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.hintMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissHint() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Wooden Puzzle Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF3E2723), Color(0xFF271510))
                            )
                        )
                        .border(4.dp, Color(0xFF5D4037), RoundedCornerShape(20.dp))
                        .padding(8.dp)
                ) {
                    val boardWidthPx = constraints.maxWidth.toFloat()
                    val cellSizePx = boardWidthPx / 6f
                    val cellSizeDp = with(density) { cellSizePx.toDp() }

                    // Exit cutout on the right at row 2
                    Box(
                        modifier = Modifier
                            .offset(
                                x = with(density) { (boardWidthPx - 4.dp.toPx()).toDp() },
                                y = cellSizeDp * 2 + 2.dp
                            )
                            .width(8.dp)
                            .height(cellSizeDp - 4.dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                            .background(Color(0xFFFFB703)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Exit",
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Background Grid Subtle Lines
                    Column(modifier = Modifier.fillMaxSize()) {
                        repeat(6) { r ->
                            Row(modifier = Modifier.weight(1f)) {
                                repeat(6) { c ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .border(0.5.dp, Color(0xFF4E342E).copy(alpha = 0.4f))
                                    )
                                }
                            }
                        }
                    }

                    // Render Blocks
                    state.blocks.forEach { block ->
                        UnblockBlockView(
                            block = block,
                            allBlocks = state.blocks,
                            cellSizePx = cellSizePx,
                            isHighlighted = state.highlightedBlockId == block.id,
                            onMove = { newPos ->
                                viewModel.moveBlock(block.id, newPos)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Dashboard: Move count & Controls
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Moves: ${state.movesCount}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Target: ${state.minMoves}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Undo Button
                        OutlinedButton(
                            onClick = { viewModel.undoMove() },
                            enabled = state.moveHistory.isNotEmpty() && !state.isWon,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Undo (${state.moveHistory.size})", fontSize = 13.sp)
                        }

                        // Hint Button
                        Button(
                            onClick = { viewModel.requestHint() },
                            enabled = !state.isWon,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hint", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // Win Celebration Dialog
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
                    Text("Escaped!", fontWeight = FontWeight.Bold)
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
                        text = "Cleared in ${state.movesCount} moves!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Optimal Solution: ${state.minMoves} moves",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val mins = state.elapsedTimeSeconds / 60
                    val secs = state.elapsedTimeSeconds % 60
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Time: %02d:%02d".format(mins, secs),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                if (state.levelNumber < 100) {
                    Button(onClick = { viewModel.nextLevel() }) {
                        Text("Next Puzzle")
                    }
                } else {
                    Button(onClick = { viewModel.resetLevel() }) {
                        Text("Replay")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onNavigateBack) {
                    Text("Level Select")
                }
            }
        )
    }
}

@Composable
private fun UnblockBlockView(
    block: Block,
    allBlocks: List<Block>,
    cellSizePx: Float,
    isHighlighted: Boolean,
    onMove: (Int) -> Unit
) {
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    var minPos by remember { mutableIntStateOf(0) }
    var maxPos by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    val initialPos = if (block.orientation == Orientation.HORIZONTAL) block.col else block.row
    val isHorizontal = block.orientation == Orientation.HORIZONTAL

    // Block dimensions
    val widthDp = with(density) {
        (if (isHorizontal) (block.length * cellSizePx - 6.dp.toPx()) else (cellSizePx - 6.dp.toPx())).toDp()
    }
    val heightDp = with(density) {
        (if (isHorizontal) (cellSizePx - 6.dp.toPx()) else (block.length * cellSizePx - 6.dp.toPx())).toDp()
    }

    // Dynamic offset with clamped dragging
    val currentDragOffsetCells = dragOffsetPx / cellSizePx
    val clampedDragCells = currentDragOffsetCells.coerceIn(
        (minPos - initialPos).toFloat(),
        (maxPos - initialPos).toFloat()
    )

    val currentCol = if (isHorizontal) (block.col + clampedDragCells) else block.col.toFloat()
    val currentRow = if (isHorizontal) block.row.toFloat() else (block.row + clampedDragCells)

    val offsetXDp = with(density) { (currentCol * cellSizePx + 3.dp.toPx()).toDp() }
    val offsetYDp = with(density) { (currentRow * cellSizePx + 3.dp.toPx()).toDp() }

    // Wood / Ruby styling
    val blockColor = when {
        block.isTarget -> Brush.linearGradient(listOf(Color(0xFFE63946), Color(0xFFC1121F)))
        block.length == 3 -> Brush.linearGradient(listOf(Color(0xFFD4A373), Color(0xFFBC6C25)))
        else -> Brush.linearGradient(listOf(Color(0xFFE9C46A), Color(0xFFD4A373)))
    }

    val blockBorder = when {
        isHighlighted -> Color(0xFFFFB703)
        block.isTarget -> Color(0xFFFFD166)
        else -> Color(0xFFA67C52)
    }

    Box(
        modifier = Modifier
            .offset(x = offsetXDp, y = offsetYDp)
            .size(width = widthDp, height = heightDp)
            .shadow(if (block.isTarget) 6.dp else 3.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(blockColor)
            .border(if (isHighlighted || block.isTarget) 2.5.dp else 1.dp, blockBorder, RoundedCornerShape(10.dp))
            .pointerInput(block.id, initialPos) {
                detectDragGestures(
                    onDragStart = {
                        val bounds = UnblockSolver.getSlidingBounds(block, allBlocks)
                        minPos = bounds.first
                        maxPos = bounds.second
                        dragOffsetPx = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetPx += if (isHorizontal) dragAmount.x else dragAmount.y
                    },
                    onDragEnd = {
                        val finalCells = dragOffsetPx / cellSizePx
                        val targetPos = (initialPos + finalCells).roundToInt().coerceIn(minPos, maxPos)
                        dragOffsetPx = 0f
                        if (targetPos != initialPos) {
                            onMove(targetPos)
                        }
                    },
                    onDragCancel = {
                        dragOffsetPx = 0f
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (block.isTarget) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ESCAPE",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
        } else {
            // Subtle wood grain dots / grip
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(if (isHorizontal) block.length else 1) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.2f))
                    )
                }
            }
        }
    }
}
