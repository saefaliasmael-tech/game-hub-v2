package com.example.colorswitch.presentation.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorswitch.core.level.ColorSwitchLevelManager
import com.example.colorswitch.core.model.ColorSwitchGameMode
import com.example.colorswitch.presentation.ColorSwitchViewModel
import com.example.watersort.core.database.GameProgressEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSwitchHomeScreen(
    viewModel: ColorSwitchViewModel,
    onNavigateBack: () -> Unit,
    onStartCampaignLevel: (Int) -> Unit,
    onStartEndless: (ColorSwitchGameMode) -> Unit
) {
    val progressList by viewModel.progressFlow.collectAsState(initial = emptyList())
    val totalStars by viewModel.totalStarsFlow.collectAsState(initial = 0)
    val completedLevels by viewModel.completedLevelsFlow.collectAsState(initial = 0)
    val soundEnabled by viewModel.soundEnabledFlow.collectAsState()
    val hapticEnabled by viewModel.hapticEnabledFlow.collectAsState()
    val colorBlindEnabled by viewModel.colorBlindFlow.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val progressMap = remember(progressList) {
        progressList.associateBy { it.levelId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.InvertColors,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Color Switch",
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
                    containerColor = Color(0xFF18181B)
                )
            )
        },
        bottomBar = {
            com.zubaluba.gamehub.ads.AdBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            )
        },
        containerColor = Color(0xFF09090B)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Stats Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color(0xFF27272A),
                shape = RoundedCornerShape(16.dp)
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
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$totalStars", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                        Text("Stars", fontSize = 12.sp, color = Color(0xFFA1A1AA))
                    }
                    Divider(modifier = Modifier.height(36.dp).width(1.dp), color = Color(0xFF3F3F46))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$completedLevels/100", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                        Text("Levels", fontSize = 12.sp, color = Color(0xFFA1A1AA))
                    }
                    Divider(modifier = Modifier.height(36.dp).width(1.dp), color = Color(0xFF3F3F46))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFF007F), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${viewModel.getBestScore()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                        Text("Best Score", fontSize = 12.sp, color = Color(0xFFA1A1AA))
                    }
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF18181B),
                contentColor = Color(0xFF00E5FF),
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Levels (1-100)", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Arcade Modes", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                0 -> ColorSwitchCampaignGrid(
                    progressMap = progressMap,
                    onSelectLevel = onStartCampaignLevel
                )
                1 -> ColorSwitchArcadeModes(onStartEndless = onStartEndless)
            }
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Color Switch Settings", color = Color.White) },
            containerColor = Color(0xFF27272A),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sound Effects", color = Color.White)
                        Switch(checked = soundEnabled, onCheckedChange = { viewModel.toggleSound(it) })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Haptic Feedback", color = Color.White)
                        Switch(checked = hapticEnabled, onCheckedChange = { viewModel.toggleHaptic(it) })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Color-Blind Symbols", color = Color.White)
                            Text("Draw ● ▲ ■ ◆ on colors", fontSize = 12.sp, color = Color(0xFFA1A1AA))
                        }
                        Switch(checked = colorBlindEnabled, onCheckedChange = { viewModel.toggleColorBlind(it) })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Close", color = Color(0xFF00E5FF))
                }
            }
        )
    }
}

@Composable
private fun ColorSwitchCampaignGrid(
    progressMap: Map<Int, GameProgressEntity>,
    onSelectLevel: (Int) -> Unit
) {
    val allLevels = remember { ColorSwitchLevelManager.getAllLevels() }

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

            val tierColor = when {
                config.levelNumber <= 20 -> Color(0xFF00E5FF)
                config.levelNumber <= 40 -> Color(0xFFFFD600)
                config.levelNumber <= 60 -> Color(0xFFFF007F)
                config.levelNumber <= 80 -> Color(0xFF8B5CF6)
                else -> Color(0xFFFF3366)
            }

            Surface(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = isUnlocked) {
                        onSelectLevel(config.levelNumber)
                    },
                color = if (isUnlocked) Color(0xFF27272A) else Color(0xFF18181B),
                shape = RoundedCornerShape(12.dp),
                border = if (isUnlocked) androidx.compose.foundation.BorderStroke(1.5.dp, tierColor.copy(alpha = 0.7f)) else null
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (!isUnlocked) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFF52525B), modifier = Modifier.size(20.dp))
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${config.levelNumber}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            if (isCompleted && stars > 0) {
                                Row(modifier = Modifier.padding(top = 2.dp)) {
                                    repeat(stars) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD600),
                                            modifier = Modifier.size(10.dp)
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
}

@Composable
private fun ColorSwitchArcadeModes(
    onStartEndless: (ColorSwitchGameMode) -> Unit
) {
    val modes = listOf(
        Triple(ColorSwitchGameMode.ENDLESS, "Classic Endless", "Ascend infinitely through procedural obstacles"),
        Triple(ColorSwitchGameMode.SPEED_CHALLENGE, "Speed Challenge", "1.5x speed multiplier for fast reflexes"),
        Triple(ColorSwitchGameMode.NO_COLOR_CHANGE, "Pure Instinct", "Navigate without color orbs")
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
                    .clickable { onStartEndless(mode) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A))
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
                            .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (mode) {
                                ColorSwitchGameMode.SPEED_CHALLENGE -> Icons.Default.Speed
                                ColorSwitchGameMode.NO_COLOR_CHANGE -> Icons.Default.ColorLens
                                else -> Icons.Default.AllInclusive
                            },
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Text(desc, fontSize = 13.sp, color = Color(0xFFA1A1AA))
                    }

                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF71717A))
                }
            }
        }
    }
}
