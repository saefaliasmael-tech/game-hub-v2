package com.example.stopthetime.presentation.ui

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stopthetime.core.level.StopTheTimeLevelManager
import com.example.stopthetime.core.model.StopDifficulty
import com.example.stopthetime.core.model.StopGameMode
import com.example.stopthetime.presentation.StopTheTimeViewModel
import com.example.watersort.core.database.GameProgressEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopTheTimeHomeScreen(
    viewModel: StopTheTimeViewModel,
    onNavigateBack: () -> Unit,
    onStartCampaignLevel: (Int) -> Unit,
    onStartCustomGame: (StopGameMode, StopDifficulty, Long?, Int) -> Unit
) {
    val progressList by viewModel.progressFlow.collectAsState(initial = emptyList())
    val totalStars by viewModel.totalStarsFlow.collectAsState(initial = 0)
    val completedLevels by viewModel.completedLevelsFlow.collectAsState(initial = 0)
    val soundEnabled by viewModel.soundEnabledFlow.collectAsState()
    val hapticEnabled by viewModel.hapticEnabledFlow.collectAsState()
    val reduceMotion by viewModel.reduceMotionFlow.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var multiplayerCount by remember { mutableIntStateOf(2) }

    val progressMap = remember(progressList) {
        progressList.associateBy { it.levelId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Stop the Time",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
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
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
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
        ) {
            // Header Stats Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatBadge(
                        icon = Icons.Default.Star,
                        iconColor = Color(0xFFFFB703),
                        label = "Stars",
                        value = "$totalStars"
                    )
                    Divider(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp),
                        color = Color(0xFF334155)
                    )
                    StatBadge(
                        icon = Icons.Default.CheckCircle,
                        iconColor = Color(0xFF10B981),
                        label = "Levels",
                        value = "$completedLevels/100"
                    )
                    Divider(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp),
                        color = Color(0xFF334155)
                    )
                    StatBadge(
                        icon = Icons.Default.Bolt,
                        iconColor = Color(0xFF38BDF8),
                        label = "Best Diff",
                        value = if (viewModel.getBestAccuracyMs() == 99999L) "--" else "${viewModel.getBestAccuracyMs()}ms"
                    )
                }
            }

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0F172A),
                contentColor = Color(0xFF10B981),
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Campaign", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Modes", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Daily", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Pass & Play", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                0 -> CampaignLevelsView(
                    progressMap = progressMap,
                    onSelectLevel = onStartCampaignLevel
                )
                1 -> GameModesView(onStartCustomGame = onStartCustomGame)
                2 -> DailyChallengeView(
                    viewModel = viewModel,
                    onStartDaily = {
                        onStartCustomGame(StopGameMode.DAILY_CHALLENGE, StopDifficulty.NORMAL, null, 1)
                    }
                )
                3 -> LocalMultiplayerView(
                    playerCount = multiplayerCount,
                    onPlayerCountChange = { multiplayerCount = it },
                    onStartMultiplayer = {
                        onStartCustomGame(StopGameMode.LOCAL_MULTIPLAYER, StopDifficulty.NORMAL, 5000L, multiplayerCount)
                    }
                )
            }
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Game Settings", color = Color.White) },
            containerColor = Color(0xFF1E293B),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sound Effects", color = Color.White)
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { viewModel.toggleSound(it) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Haptic Feedback", color = Color.White)
                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = { viewModel.toggleHaptic(it) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reduce Motion", color = Color.White)
                        Switch(
                            checked = reduceMotion,
                            onCheckedChange = { viewModel.toggleReduceMotion(it) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Close", color = Color(0xFF10B981))
                }
            }
        )
    }
}

@Composable
private fun CampaignLevelsView(
    progressMap: Map<Int, GameProgressEntity>,
    onSelectLevel: (Int) -> Unit
) {
    val allLevels = remember { StopTheTimeLevelManager.getAllLevels() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(allLevels) { config ->
            val progress = progressMap[config.levelNumber]
            val isUnlocked = progress?.isUnlocked ?: (config.levelNumber == 1)
            val isCompleted = progress?.isCompleted ?: false
            val stars = progress?.stars ?: 0

            val tierColor = when (config.difficulty) {
                StopDifficulty.EASY -> Color(0xFF10B981)
                StopDifficulty.NORMAL -> Color(0xFF38BDF8)
                StopDifficulty.HARD -> Color(0xFFF59E0B)
                StopDifficulty.EXPERT -> Color(0xFFEC4899)
                StopDifficulty.MASTER -> Color(0xFF8B5CF6)
            }

            Surface(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = isUnlocked) {
                        onSelectLevel(config.levelNumber)
                    },
                color = if (isUnlocked) Color(0xFF1E293B) else Color(0xFF0F172A),
                shape = RoundedCornerShape(12.dp),
                border = if (isUnlocked) androidx.compose.foundation.BorderStroke(1.5.dp, tierColor.copy(alpha = 0.6f)) else null
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (!isUnlocked) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "${config.levelNumber}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            if (isCompleted && stars > 0) {
                                Row(
                                    modifier = Modifier.padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    repeat(stars) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB703),
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            } else if (config.isBlind) {
                                Text(
                                    "BLIND",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameModesView(
    onStartCustomGame: (StopGameMode, StopDifficulty, Long?, Int) -> Unit
) {
    val modes = listOf(
        Triple(StopGameMode.CLASSIC, "Classic Target", "Choose your target time and stop right on the dot."),
        Triple(StopGameMode.BLIND, "Blind Reflex", "Timer blanks out after 1.5 seconds! Trust your internal pulse."),
        Triple(StopGameMode.SPEED_VARIATION, "Speed Illusion", "Digits visually fluctuate while internal clock stays true."),
        Triple(StopGameMode.RANDOM, "Random Target", "Dynamic unexpected target times each attempt."),
        Triple(StopGameMode.ONE_ATTEMPT, "Sudden Death", "Single attempt to score maximum accuracy."),
        Triple(StopGameMode.MULTI_ROUND, "Multi-Round Match", "3 progressive rounds combined absolute difference.")
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(modes) { (mode, title, desc) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onStartCustomGame(mode, StopDifficulty.NORMAL, null, 1)
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (mode) {
                                StopGameMode.BLIND -> Icons.Default.VisibilityOff
                                StopGameMode.SPEED_VARIATION -> Icons.Default.Speed
                                StopGameMode.RANDOM -> Icons.Default.Casino
                                StopGameMode.ONE_ATTEMPT -> Icons.Default.Dangerous
                                StopGameMode.MULTI_ROUND -> Icons.Default.Repeat
                                else -> Icons.Default.PlayArrow
                            },
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            desc,
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyChallengeView(
    viewModel: StopTheTimeViewModel,
    onStartDaily: () -> Unit
) {
    val isCompleted = viewModel.isDailyCompletedToday()
    val targets = remember { com.example.stopthetime.core.engine.StopTheTimeEngine.getDailyChallengeTargets() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF10B981).copy(alpha = 0.15f),
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Today's Challenge",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "3 Deterministic Consecutive Rounds\nTargets: ${targets.map { "${it / 1000}s" }.joinToString(", ")}",
            textAlign = TextAlign.Center,
            color = Color(0xFF94A3B8),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartDaily,
            enabled = !isCompleted,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981),
                disabledContainerColor = Color(0xFF334155)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(54.dp)
        ) {
            Text(
                if (isCompleted) "Completed Today ✓" else "Play Daily Challenge",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isCompleted) Color(0xFF94A3B8) else Color.White
            )
        }
    }
}

@Composable
private fun LocalMultiplayerView(
    playerCount: Int,
    onPlayerCountChange: (Int) -> Unit,
    onStartMultiplayer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF38BDF8).copy(alpha = 0.15f),
            modifier = Modifier.size(90.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Pass & Play Multiplayer",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.White
        )

        Text(
            "Take turns stopping the timer at the same target.\nPlayer with the lowest absolute difference wins!",
            textAlign = TextAlign.Center,
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Select Number of Players", color = Color.White, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(2, 3, 4).forEach { count ->
                val isSelected = count == playerCount
                Surface(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPlayerCountChange(count) },
                    color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "$count",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onStartMultiplayer,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(54.dp)
        ) {
            Text("Start Match", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
        }
    }
}

@Composable
private fun StatBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        }
        Text(label, fontSize = 12.sp, color = Color(0xFF94A3B8))
    }
}
