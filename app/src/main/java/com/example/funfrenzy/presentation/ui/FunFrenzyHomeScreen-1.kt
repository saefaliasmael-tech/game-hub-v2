package com.example.funfrenzy.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.funfrenzy.core.level.FunFrenzyLevelManager
import com.example.funfrenzy.presentation.FunFrenzyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunFrenzyHomeScreen(
    viewModel: FunFrenzyViewModel,
    onNavigateBack: () -> Unit,
    onStartLevel: (Int) -> Unit
) {
    val completedCount by viewModel.completedLevelsFlow.collectAsState(initial = 0)
    val progressList by viewModel.progressFlow.collectAsState(initial = emptyList())
    val soundEnabled by viewModel.soundEnabledFlow.collectAsState()

    val progressMap = remember(progressList) {
        progressList.associateBy { it.levelId }
    }

    val totalStars = remember(progressList) {
        progressList.sumOf { it.stars }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Fun Frenzy Rescue",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFDF4FF)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("funfrenzy_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFDF4FF)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleSound() },
                        modifier = Modifier.testTag("funfrenzy_sound_toggle")
                    ) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Sound",
                            tint = Color(0xFFC084FC)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Stats Hero Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("funfrenzy_stats_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E293B)
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
                        Text(
                            text = "$completedCount / ${FunFrenzyLevelManager.TOTAL_LEVELS}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC084FC)
                        )
                        Text(
                            text = "Levels Rescued",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color(0xFF334155))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$totalStars",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                        }
                        Text(
                            text = "Stars Earned",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val nextLevel = (completedCount + 1).coerceAtMost(FunFrenzyLevelManager.TOTAL_LEVELS)
            Button(
                onClick = { onStartLevel(nextLevel) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("funfrenzy_play_next_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9333EA)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Play Rescue $nextLevel",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select Rescue Level (1 - 100)",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 64.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(FunFrenzyLevelManager.TOTAL_LEVELS) { index ->
                    val levelNum = index + 1
                    val entity = progressMap[levelNum]
                    val isUnlocked = levelNum == 1 || (entity?.isUnlocked == true) || levelNum <= (completedCount + 1)
                    val isCompleted = entity?.isCompleted == true
                    val stars = entity?.stars ?: 0

                    val bgBrush = when {
                        isCompleted -> Brush.verticalGradient(listOf(Color(0xFF6B21A8), Color(0xFF581C87)))
                        isUnlocked -> Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                        else -> Brush.verticalGradient(listOf(Color(0xFF1E293B).copy(alpha = 0.4f), Color(0xFF0F172A).copy(alpha = 0.4f)))
                    }

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgBrush)
                            .border(
                                width = if (isCompleted) 1.5.dp else 1.dp,
                                color = if (isCompleted) Color(0xFFC084FC) else if (isUnlocked) Color(0xFF334155) else Color(0xFF1E293B),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable(enabled = isUnlocked) { onStartLevel(levelNum) }
                            .testTag("funfrenzy_level_$levelNum"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isUnlocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
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
                                    text = "$levelNum",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompleted) Color.White else Color(0xFFE2E8F0)
                                )

                                if (isCompleted && stars > 0) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        repeat(3) { starIndex ->
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (starIndex < stars) Color(0xFFFBBF24) else Color(0xFF475569),
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
}
