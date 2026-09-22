package com.example.unblockme.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unblockme.core.model.UnblockDifficulty
import com.example.unblockme.core.repository.UnblockRepository
import com.example.unblockme.presentation.UnblockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnblockHomeScreen(
    viewModel: UnblockViewModel,
    onNavigateBack: () -> Unit,
    onStartLevel: (Int) -> Unit
) {
    val highestLevel by viewModel.highestLevelFlow.collectAsState()
    val progressList by viewModel.progressFlow.collectAsState(initial = emptyList())
    val progressMap = remember(progressList) { progressList.associateBy { it.levelId } }

    var selectedTab by remember { mutableStateOf(UnblockDifficulty.EASY) }

    val levelsForTab = remember(selectedTab) {
        when (selectedTab) {
            UnblockDifficulty.EASY -> (1..20).toList()
            UnblockDifficulty.NORMAL -> (21..40).toList()
            UnblockDifficulty.HARD -> (41..60).toList()
            UnblockDifficulty.VERY_HARD -> (61..80).toList()
            UnblockDifficulty.EXPERT -> (81..100).toList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Unblock Me",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("unblock_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF8D6E63)
                )
            )
        },
        containerColor = Color(0xFFFBE9E7)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Quick Play Button
            Button(
                onClick = { onStartLevel(highestLevel.coerceIn(1, UnblockRepository.TOTAL_LEVELS)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("unblock_play_button")
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

            Spacer(modifier = Modifier.height(16.dp))

            // Difficulty Tabs
            ScrollableTabRow(
                selectedTabIndex = UnblockDifficulty.values().indexOf(selectedTab),
                containerColor = Color.Transparent,
                contentColor = Color(0xFFD84315),
                edgePadding = 0.dp
            ) {
                UnblockDifficulty.values().forEach { diff ->
                    Tab(
                        selected = selectedTab == diff,
                        onClick = { selectedTab = diff },
                        text = {
                            Text(
                                diff.title,
                                fontWeight = if (selectedTab == diff) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == diff) Color(0xFFD84315) else Color(0xFF8D6E63)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Levels Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("unblock_levels_grid"),
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
                                onStartLevel(levelNum)
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
            .testTag("level_card_$levelNumber"),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFFFFCCBC) else Color(0xFFE0E0E0)
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
                    color = Color(0xFF4E342E)
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
