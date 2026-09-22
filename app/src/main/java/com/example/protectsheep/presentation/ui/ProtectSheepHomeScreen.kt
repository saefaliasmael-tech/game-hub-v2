package com.example.protectsheep.presentation.ui

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
import com.example.protectsheep.core.level.ProtectSheepLevelManager
import com.example.protectsheep.presentation.ProtectSheepViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectSheepHomeScreen(
    viewModel: ProtectSheepViewModel,
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
                            text = "Protect Sheep",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Shield the Flock from Angry Bees",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A1E))
            )
        },
        containerColor = Color(0xFF0F240F)
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A1E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(Color(0xFF22C55E), Color(0xFFEAB308))),
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
                            text = "Pasture $highestUnlocked",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$completedLevels / ${ProtectSheepLevelManager.MAX_LEVELS} Flocks Saved",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF22C55E)
                        )
                    }

                    Button(
                        onClick = { onStartLevel(highestUnlocked.coerceAtMost(ProtectSheepLevelManager.MAX_LEVELS)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF0F240F))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PLAY", fontWeight = FontWeight.Bold, color = Color(0xFF0F240F))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Pasture Puzzles",
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
                items((1..ProtectSheepLevelManager.MAX_LEVELS).toList()) { levelNum ->
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
                                    isCompleted -> Color(0xFF2D5A27)
                                    isUnlocked -> Color(0xFF1E3A1E)
                                    else -> Color(0xFF0F240F).copy(alpha = 0.6f)
                                }
                            )
                            .border(
                                width = if (levelNum == highestUnlocked) 2.dp else 1.dp,
                                color = when {
                                    levelNum == highestUnlocked -> Color(0xFF22C55E)
                                    isCompleted -> Color(0xFFEAB308)
                                    isUnlocked -> Color(0xFF386633)
                                    else -> Color(0xFF1E3A1E)
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
                                    color = if (isCompleted) Color(0xFFEAB308) else Color.White
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
                                tint = Color(0xFF386633),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
