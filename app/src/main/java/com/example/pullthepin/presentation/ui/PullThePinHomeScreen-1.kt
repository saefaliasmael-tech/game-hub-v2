package com.example.pullthepin.presentation.ui

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
import com.example.pullthepin.core.level.PullThePinLevelManager
import com.example.pullthepin.presentation.PullThePinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullThePinHomeScreen(
    viewModel: PullThePinViewModel,
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
                            text = "Pull the Pin",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Physics Ball Color Cascade",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF261C14))
            )
        },
        containerColor = Color(0xFF19120D)
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF261C14)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(Color(0xFFFF9F1C), Color(0xFFFFBF69))),
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
                            text = "Chamber $highestUnlocked",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$completedLevels / ${PullThePinLevelManager.MAX_LEVELS} Completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFBF69)
                        )
                    }

                    Button(
                        onClick = { onStartLevel(highestUnlocked.coerceAtMost(PullThePinLevelManager.MAX_LEVELS)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F1C)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF19120D))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PLAY", fontWeight = FontWeight.Bold, color = Color(0xFF19120D))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Puzzle Chambers",
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
                items((1..PullThePinLevelManager.MAX_LEVELS).toList()) { levelNum ->
                    val isUnlocked = levelNum <= highestUnlocked
                    val progress = progressMap[levelNum]
                    val isCompleted = progress?.isCompleted == true

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                when {
                                    isCompleted -> Color(0xFF5A3E2B)
                                    isUnlocked -> Color(0xFF261C14)
                                    else -> Color(0xFF19120D).copy(alpha = 0.6f)
                                }
                            )
                            .border(
                                width = if (levelNum == highestUnlocked) 2.dp else 1.dp,
                                color = when {
                                    levelNum == highestUnlocked -> Color(0xFFFF9F1C)
                                    isCompleted -> Color(0xFFFFBF69)
                                    isUnlocked -> Color(0xFF433022)
                                    else -> Color(0xFF261C14)
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
                                    color = if (isCompleted) Color(0xFFFFBF69) else Color.White
                                )
                                if (isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD166),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFF70523C),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
