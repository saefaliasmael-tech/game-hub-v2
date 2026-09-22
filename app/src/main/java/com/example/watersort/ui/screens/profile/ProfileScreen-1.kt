package com.example.watersort.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.watersort.core.database.AchievementEntity
import com.example.watersort.core.repository.GameRepository

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    repository: GameRepository,
    onNavigateBack: () -> Unit
) {
    val profile by repository.profileFlow.collectAsStateWithLifecycle(initialValue = null)
    val completedLevels by repository.completedLevelsCountFlow.collectAsStateWithLifecycle(initialValue = 0)
    val totalStars by repository.totalStarsCountFlow.collectAsStateWithLifecycle(initialValue = 0)
    val achievements by repository.allAchievementsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val p = profile
    val playerLevel = p?.playerLevel ?: 1
    val currentXp = p?.xp ?: 0
    val xpInCurrentLevel = currentXp % 100
    val xpProgress = xpInCurrentLevel / 100f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = stringResource(R.string.profile),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Player Stats & Level Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0284C7).copy(alpha = 0.25f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🧪", fontSize = 38.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Sort Master",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        // Player Level Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x3338BDF8),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "LEVEL $playerLevel",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // XP Progress
                        Column(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "XP Progress",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                                )
                                Text(
                                    text = "$xpInCurrentLevel / 100 XP",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { xpProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = Color(0xFF38BDF8),
                                trackColor = Color(0xFF334155),
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Summary Counters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(label = stringResource(R.string.total_stars), value = "$totalStars ★", accentColor = Color(0xFFFACC15))
                            StatItem(label = stringResource(R.string.levels_completed), value = "$completedLevels", accentColor = Color(0xFF38BDF8))
                            StatItem(label = "Streak", value = "${p?.currentStreak ?: 1}d 🔥", accentColor = Color(0xFFF97316))
                            StatItem(label = "Coins", value = "${p?.coins ?: 300} 🪙", accentColor = Color(0xFFFDE047))
                        }
                    }
                }
            }

            // Equipped Customization Showcase Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "EQUIPPED COSMETICS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CosmeticBadge(
                                label = "Skin",
                                value = p?.equippedSkinId?.replaceFirstChar { it.uppercase() } ?: "Classic",
                                icon = Icons.Default.WorkspacePremium,
                                color = Color(0xFFEC4899),
                                modifier = Modifier.weight(1f)
                            )
                            CosmeticBadge(
                                label = "Theme",
                                value = p?.equippedThemeId?.replaceFirstChar { it.uppercase() } ?: "Classic",
                                icon = Icons.Default.Palette,
                                color = Color(0xFF8B5CF6),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Detailed Gameplay Statistics Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "GAMEPLAY STATISTICS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            maxItemsInEachRow = 2
                        ) {
                            DetailedStatRow(label = "Total Moves", value = "${p?.totalMoves ?: 0}")
                            DetailedStatRow(label = "Perfect Levels", value = "${p?.perfectLevels ?: 0} 🌟")
                            DetailedStatRow(label = "Undos Used", value = "${p?.undosUsed ?: 0}")
                            DetailedStatRow(label = "Hints Used", value = "${p?.hintsUsed ?: 0}")
                            DetailedStatRow(label = "Extra Bottles Used", value = "${p?.extraBottlesUsed ?: 0}")
                            DetailedStatRow(label = "Daily Challenges Won", value = "${p?.dailyChallengesCompleted ?: 0}")
                            DetailedStatRow(label = "Longest Streak", value = "${p?.longestStreak ?: 1} days")
                            DetailedStatRow(label = "Total Coins Earned", value = "${p?.totalCoinsEarned ?: 300} 🪙")
                        }
                    }
                }
            }

            // Achievements Header
            item {
                Text(
                    text = stringResource(R.string.achievements),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            // Achievements List
            items(achievements) { ach ->
                val progressRatio = (ach.currentProgress.toFloat() / ach.target).coerceIn(0f, 1f)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (ach.isCompleted) Color(0x3310B981) else Color(0xFF334155),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (ach.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF34D399),
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = ach.id.replace("_", " ").replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = if (ach.isCompleted) "COMPLETED" else "${ach.currentProgress}/${ach.target}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (ach.isCompleted) Color(0xFF34D399) else Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { progressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = if (ach.isCompleted) Color(0xFF10B981) else Color(0xFF38BDF8),
                                trackColor = Color(0xFF334155),
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Reward: +${ach.rewardCoins} 🪙",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFDE047))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CosmeticBadge(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun DetailedStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.width(150.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}

@Composable
fun StatItem(label: String, value: String, accentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
        )
    }
}
