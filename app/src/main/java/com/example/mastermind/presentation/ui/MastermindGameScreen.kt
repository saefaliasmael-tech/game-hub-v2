package com.example.mastermind.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermind.core.model.*
import com.example.mastermind.presentation.MastermindViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MastermindGameScreen(
    viewModel: MastermindViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsState()
    val selectedSlot by viewModel.selectedSlotIndex.collectAsState()
    val colorBlindMode by viewModel.colorBlindMode.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to bottom when new attempt is added
    LaunchedEffect(state.attempts.size) {
        if (state.attempts.isNotEmpty()) {
            listState.animateScrollToItem(state.attempts.size - 1)
        }
    }

    val palette = remember(state.difficulty) {
        PegColor.getPalette(state.difficulty.colorCount)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (state.mode) {
                                MastermindGameMode.CAMPAIGN -> "Level ${state.levelNumber}"
                                MastermindGameMode.DAILY_CHALLENGE -> "Daily Challenge"
                                MastermindGameMode.QUICK_PLAY -> "${state.difficulty.displayName} Mode"
                                MastermindGameMode.ENDLESS -> "Endless"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${state.attemptsRemaining} attempts left",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("game_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Timer display
                    val mins = state.elapsedTimeSeconds / 60
                    val secs = state.elapsedTimeSeconds % 60
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
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
                        enabled = !state.isGameOver && !state.isWon
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

                    // Restart
                    IconButton(onClick = { viewModel.restartCurrentGame() }) {
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
            // Hint Banner if visible
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

            // Guess History List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (state.attempts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Select colors below to make your first deduction.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(state.attempts) { index, row ->
                            GuessRowItem(
                                attemptIndex = index + 1,
                                row = row,
                                codeLength = state.difficulty.codeLength,
                                colorBlindMode = colorBlindMode
                            )
                        }
                    }
                }
            }

            // Divider
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Current Active Guess Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "#${state.currentAttemptNumber}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp,
                        modifier = Modifier.width(36.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        state.currentGuess.forEachIndexed { index, peg ->
                            val isSelected = selectedSlot == index
                            val isRevealed = state.revealedPositions.containsKey(index)
                            val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

                            PegSlot(
                                peg = peg,
                                isSelected = isSelected,
                                isRevealed = isRevealed,
                                colorBlindMode = colorBlindMode,
                                onClick = { viewModel.selectSlot(index) },
                                onLongClick = { viewModel.clearSlot(index) }
                            )
                        }
                    }

                    // Clear button
                    IconButton(
                        onClick = { viewModel.clearCurrentRow() },
                        enabled = state.currentGuess.any { it != null } && !state.isGameOver
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Backspace,
                            contentDescription = "Clear Row",
                            tint = if (state.currentGuess.any { it != null }) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }

            // Color Palette Selector
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Palette Circles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        palette.forEach { color ->
                            val isEliminated = state.eliminatedColors.contains(color)
                            PalettePeg(
                                color = color,
                                isEliminated = isEliminated,
                                colorBlindMode = colorBlindMode,
                                onClick = { viewModel.selectColor(color) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Submit Guess Button
                    Button(
                        onClick = { viewModel.submitGuess() },
                        enabled = state.isCurrentGuessComplete && !state.isGameOver && !state.isWon,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_guess_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Submit Deduction",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
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
                    Text("Code Cracked!", fontWeight = FontWeight.Bold)
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Cracked in ${state.attempts.size} attempts!")
                    val mins = state.elapsedTimeSeconds / 60
                    val secs = state.elapsedTimeSeconds % 60
                    Text("Time: %02d:%02d".format(mins, secs), style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reveal Code
                    Text("Secret Code Was:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.secretCode.forEach { peg ->
                            PegSlot(
                                peg = peg,
                                isSelected = false,
                                isRevealed = false,
                                colorBlindMode = colorBlindMode,
                                onClick = {}
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (state.mode == MastermindGameMode.CAMPAIGN && state.levelNumber < 100) {
                    Button(onClick = { viewModel.nextLevel() }) {
                        Text("Next Level")
                    }
                } else {
                    Button(onClick = { viewModel.restartCurrentGame() }) {
                        Text("Play Again")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onNavigateBack) {
                    Text("Exit to Menu")
                }
            }
        )
    }

    // Game Over Dialog (Failed)
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
                    Text("Out of Attempts!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("The secret code remained uncracked this time.")
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Secret Code Was:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.secretCode.forEach { peg ->
                            PegSlot(
                                peg = peg,
                                isSelected = false,
                                isRevealed = false,
                                colorBlindMode = colorBlindMode,
                                onClick = {}
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.restartCurrentGame() }) {
                    Text("Try Again")
                }
            },
            dismissButton = {
                TextButton(onClick = onNavigateBack) {
                    Text("Exit to Menu")
                }
            }
        )
    }
}

@Composable
private fun GuessRowItem(
    attemptIndex: Int,
    row: GuessRow,
    codeLength: Int,
    colorBlindMode: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "#$attemptIndex",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(32.dp)
            )

            // Guessed Pegs
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.guess.forEach { peg ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(peg.color)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (colorBlindMode) {
                            Text(
                                text = peg.symbol,
                                color = peg.textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Feedback Pegs (Exact matches in black/red, Color matches in white)
            FeedbackPegs(
                feedback = row.feedback,
                codeLength = codeLength
            )
        }
    }
}

@Composable
private fun FeedbackPegs(
    feedback: Feedback?,
    codeLength: Int
) {
    if (feedback == null) return

    val exact = feedback.exactMatches
    val color = feedback.colorMatches
    val empty = (codeLength - exact - color).coerceAtLeast(0)

    val rows = if (codeLength <= 4) 2 else 2
    val cols = (codeLength + 1) / 2

    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(start = 6.dp)
    ) {
        var exactLeft = exact
        var colorLeft = color

        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(cols) {
                    val dotColor = when {
                        exactLeft > 0 -> {
                            exactLeft--
                            Color(0xFFDC2626) // Exact match: Vivid Red/Black peg
                        }
                        colorLeft > 0 -> {
                            colorLeft--
                            Color(0xFFF3F4F6) // Color match: White peg
                        }
                        else -> Color.Transparent
                    }

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .border(
                                width = 1.dp,
                                color = if (dotColor == Color.Transparent) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun PegSlot(
    peg: PegColor?,
    isSelected: Boolean,
    isRevealed: Boolean,
    colorBlindMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isRevealed -> Color(0xFFFFB703)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(peg?.color ?: MaterialTheme.colorScheme.surface)
            .border(if (isSelected) 2.5.dp else 1.dp, borderColor, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (peg != null && colorBlindMode) {
            Text(
                text = peg.symbol,
                color = peg.textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        if (peg == null && isSelected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun PalettePeg(
    color: PegColor,
    isEliminated: Boolean,
    colorBlindMode: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (isEliminated) color.color.copy(alpha = 0.2f) else color.color)
            .border(
                width = 1.dp,
                color = if (isEliminated) Color.Gray.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.35f),
                shape = CircleShape
            )
            .clickable(enabled = !isEliminated) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isEliminated) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Eliminated",
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )
        } else if (colorBlindMode) {
            Text(
                text = color.symbol,
                color = color.textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}
