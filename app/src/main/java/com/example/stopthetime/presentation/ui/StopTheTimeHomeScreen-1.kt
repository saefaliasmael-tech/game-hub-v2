package com.example.stopthetime.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
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
import com.example.stopthetime.core.model.StopDifficulty
import com.example.stopthetime.core.model.StopGameMode
import com.example.stopthetime.core.repository.StopTheTimeRepository
import com.example.stopthetime.presentation.StopTheTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopTheTimeHomeScreen(
    viewModel: StopTheTimeViewModel,
    onNavigateBack: () -> Unit,
    onStartCampaignLevel: (Int) -> Unit,
    onStartCustomGame: (StopGameMode, StopDifficulty, Long?, Int) -> Unit
) {
    val highestLevel by viewModel.highestLevelFlow.collectAsState()
    val highScore by viewModel.highScoreFlow.collectAsState()
    val progressList by viewModel.progressFlow.collectAsState(initial = emptyList())
    val progressMap = remember(progressList) { progressList.associateBy { it.levelId } }

    var selectedDiffTab by remember { mutableStateOf(StopDifficulty.EASY) }

    val levelsForTab = remember(selectedDiffTab) {
        when (selectedDiffTab) {
            StopDifficulty.EASY -> (1..20).toList()
            StopDifficulty.NORMAL -> (21..40).toList()
            StopDifficulty.HARD -> (41..60).toList()
            StopDifficulty.EXPERT -> (61..80).toList()
            StopDifficulty.MASTER -> (81..100).toList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Stop The Time",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("stoptime_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E88E5))
            )
        },
        containerColor = Color(0xFFE3F2FD)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Main Play Button
            Button(
                onClick = { onStartCampaignLevel(highestLevel.coerceIn(1, StopTheTimeRepository.TOTAL_LEVELS)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("stoptime_play_button")
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Play Level $highestLevel",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Modes: 2 Players / Daily Challenge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onStartCustomGame(StopGameMode.MULTIPLAYER, StopDifficulty.NORMAL, 5000L, 2)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("2 Players (Duel)", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        onStartCustomGame(StopGameMode.DAILY_CHALLENGE, StopDifficulty.HARD, null, 1)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Daily Challenge", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Difficulty Tabs
            ScrollableTabRow(
                selectedTabIndex = StopDifficulty.values().indexOf(selectedDiffTab),
                containerColor = Color.Transparent,
                contentColor = Color(0xFF1565C0),
                edgePadding = 0.dp
            ) {
                StopDifficulty.values().forEach { diff ->
                    Tab(
                        selected = selectedDiffTab == diff,
                        onClick = { selectedDiffTab = diff },
                        text = {
                            Text(
                                diff.title,
                                fontWeight = if (selectedDiffTab == diff) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedDiffTab == diff) Color(0xFF0D47A1) else Color(0xFF546E7A)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Level Selection Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("stoptime_levels_grid"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(levelsForTab.size) { index ->
                    val levelNum = levelsForTab[index]
                    val isUnlocked = levelNum <= highestLevel
                    val entity = progressMap[levelNum]
                    val stars = entity?.stars ?: 0

                    LevelCard(
                        levelNumber = levelNum,
                        isUnlocked = isUnlocked,
                        stars = stars,
                        onClick = {
                            if (isUnlocked) {
                                onStartCampaignLevel(levelNum)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelCard(
    levelNumber: Int,
    isUnlocked: Boolean,
    stars: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = isUnlocked, onClick = onClick)
            .testTag("stoptime_card_$levelNumber"),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFFBBDEFB) else Color(0xFFECEFF1)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isUnlocked) {
                Text(
                    text = "$levelNumber",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0D47A1)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    for (i in 1..3) {
                        Icon(
                            imageVector = if (i <= stars) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (i <= stars) Color(0xFFFFB300) else Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Locked",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
