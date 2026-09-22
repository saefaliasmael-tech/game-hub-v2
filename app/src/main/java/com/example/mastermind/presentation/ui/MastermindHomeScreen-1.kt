package com.example.mastermind.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mastermind.core.model.MastermindDifficulty
import com.example.mastermind.presentation.MastermindViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MastermindHomeScreen(
    viewModel: MastermindViewModel,
    onNavigateBack: () -> Unit,
    onStartCampaignLevel: (Int) -> Unit,
    onStartQuickPlay: (MastermindDifficulty) -> Unit,
    onStartDailyChallenge: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressList by viewModel.progressFlow.collectAsState(initial = emptyList())
    val totalStars by viewModel.totalStarsFlow.collectAsState(initial = 0)
    val completedCount by viewModel.completedLevelsCountFlow.collectAsState(initial = 0)
    val colorBlindMode by viewModel.colorBlindMode.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Campaign, 1: Quick Play

    val progressMap = remember(progressList) {
        progressList.associateBy { it.levelId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mastermind",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("mastermind_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings")
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
            // Stats Header Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB703),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$totalStars",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Text(
                            text = "Stars Earned",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$completedCount / 100",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Text(
                            text = "Levels Cleared",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Daily Challenge Strip
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onStartDailyChallenge() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF7209B7).copy(alpha = 0.15f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF7209B7), Color(0xFF3B82F6)))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7209B7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Today,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Daily Code Challenge",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Cracked by deduction experts today",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Button(
                        onClick = onStartDailyChallenge,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Crack", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Tabs: Campaign vs Quick Play
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Campaign (100 Levels)") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Quick Play") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedTab == 0) {
                // Campaign 100 Levels Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 64.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(100) { index ->
                        val levelNum = index + 1
                        val progress = progressMap[levelNum]
                        val isUnlocked = levelNum == 1 || (progress?.isUnlocked == true)
                        val isCompleted = progress?.isCompleted == true
                        val stars = progress?.stars ?: 0
                        val diff = MastermindDifficulty.fromLevelNumber(levelNum)

                        LevelGridCell(
                            levelNumber = levelNum,
                            isUnlocked = isUnlocked,
                            isCompleted = isCompleted,
                            stars = stars,
                            difficulty = diff,
                            onClick = {
                                if (isUnlocked) {
                                    onStartCampaignLevel(levelNum)
                                }
                            }
                        )
                    }
                }
            } else {
                // Quick Play Modes
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(MastermindDifficulty.entries) { diff ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStartQuickPlay(diff) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = diff.displayName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = "${diff.codeLength} Pegs",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${diff.colorCount} Colors • ${diff.maxAttempts} Attempts • Duplicates ${if (diff.allowDuplicates) "ON" else "OFF"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = { onStartQuickPlay(diff) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Play")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Settings Modal
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Mastermind Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Color-Blind Mode", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Display distinct symbols on pegs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = colorBlindMode,
                            onCheckedChange = { viewModel.toggleColorBlindMode() }
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sound Effects", fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { viewModel.toggleSound() }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Haptic Feedback", fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = { viewModel.toggleHaptic() }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun LevelGridCell(
    levelNumber: Int,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    stars: Int,
    difficulty: MastermindDifficulty,
    onClick: () -> Unit
) {
    val borderColor = when {
        !isUnlocked -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        isCompleted -> Color(0xFF10B981)
        else -> MaterialTheme.colorScheme.primary
    }

    val backgroundColor = when {
        !isUnlocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        isCompleted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = isUnlocked) { onClick() }
            .testTag("level_cell_$levelNumber"),
        contentAlignment = Alignment.Center
    ) {
        if (!isUnlocked) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Locked",
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$levelNumber",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isCompleted) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { starIndex ->
                            val active = starIndex < stars
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (active) Color(0xFFFFB703) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
