package com.example.funfrenzy.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.funfrenzy.core.level.FunFrenzyLevelManager
import com.example.funfrenzy.presentation.FunFrenzyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunFrenzyHomeScreen(
    viewModel: FunFrenzyViewModel,
    onNavigateBack: () -> Unit,
    onStartLevel: (Int) -> Unit
) {
    val completedLevels by viewModel.completedLevelsFlow.collectAsState(initial = 0)
    val highestUnlocked by viewModel.highestLevelFlow.collectAsState()
    val progressList by viewModel.progressFlow.collectAsState(initial = emptyList())

    val progressMap = remember(progressList) {
        progressList.associateBy { it.levelId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Fun Frenzy",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Rapid-Fire Micro Minigame Madness",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF472B6)
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1035))
            )
        },
        containerColor = Color(0xFF0F061F)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Quick Play Hero Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF261447)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Stage $highestUnlocked",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$completedLevels / ${FunFrenzyLevelManager.MAX_LEVELS} Gauntlets Cleared",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF472B6)
                        )
                    }

                    Button(
                        onClick = { onStartLevel(highestUnlocked.coerceAtMost(FunFrenzyLevelManager.MAX_LEVELS)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("START", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Frenzy Gauntlets",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 100 Levels Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 64.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items((1..FunFrenzyLevelManager.MAX_LEVELS).toList()) { levelNum ->
                    val isUnlocked = levelNum <= highestUnlocked
                    val progress = progressMap[levelNum]
                    val isCompleted = progress?.isCompleted == true
                    val stars = progress?.stars ?: 0

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                when {
                                    isCompleted -> Color(0xFF4C1D95)
                                    isUnlocked -> Color(0xFF261447)
                                    else -> Color(0xFF140A26).copy(alpha = 0.6f)
                                }
                            )
                            .border(
                                width = if (levelNum == highestUnlocked) 2.dp else 1.dp,
                                color = when {
                                    levelNum == highestUnlocked -> Color(0xFFEC4899)
                                    isCompleted -> Color(0xFFFBBF24)
                                    isUnlocked -> Color(0xFF6D28D9)
                                    else -> Color(0xFF261447)
                                },
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable(enabled = isUnlocked) {
                                onStartLevel(levelNum)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUnlocked) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$levelNum",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isCompleted) Color(0xFFFBBF24) else Color.White
                                )
                                if (isCompleted) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        repeat(stars.coerceAtLeast(1)) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFFFD166),
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFF4C1D95),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
