package com.example.watersort.ui.screens.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.watersort.core.model.WorldConfig
import com.example.watersort.core.repository.GameRepository
import com.example.watersort.ui.components.AmbientEnvironment
import com.example.watersort.ui.components.GameButtonColor
import com.example.watersort.ui.components.GameCoinPill
import com.example.watersort.ui.components.TactileGameButton
import com.example.watersort.ui.components.TactileGameCard
import com.example.watersort.ui.components.WaterSortMascot
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    repository: GameRepository,
    onNavigateToGame: (Int) -> Unit,
    onNavigateToWorlds: () -> Unit,
    onNavigateToDailyChallenge: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateBackToHub: (() -> Unit)? = null
) {
    val profile by repository.profileFlow.collectAsStateWithLifecycle(initialValue = null)
    val totalStars by repository.totalStarsCountFlow.collectAsStateWithLifecycle(initialValue = 0)
    val scope = rememberCoroutineScope()

    val currentLevel = profile?.currentLevel ?: 1
    val currentWorld = WorldConfig.forLevel(currentLevel)

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val canClaimDaily = profile?.lastClaimDate != todayStr

    // Gentle cloud drifting animation
    val infiniteTransition = rememberInfiniteTransition(label = "clouds")
    val cloudOffset by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_drift"
    )

    // Pulse animation for Play button glow
    val playPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "play_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0C192E), // Deep Twilight Sky
                        Color(0xFF0A2540), // Horizon Blue
                        Color(0xFF081C30), // Mountain Indigo
                        Color(0xFF061320)  // Deep Terrain Floor
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Living Ambient Environment (World Particles)
        AmbientEnvironment(envType = currentWorld.envType)

        // Illustrated environmental clouds backdrop
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Distant soft clouds
            drawCircle(
                color = Color(0x1838BDF8),
                radius = 160f,
                center = Offset(w * 0.25f + cloudOffset, h * 0.16f)
            )
            drawCircle(
                color = Color(0x1538BDF8),
                radius = 120f,
                center = Offset(w * 0.40f + cloudOffset * 1.2f, h * 0.15f)
            )
            drawCircle(
                color = Color(0x100284C7),
                radius = 200f,
                center = Offset(w * 0.85f - cloudOffset, h * 0.22f)
            )

            // Distant horizon mountain silhouettes
            val mountainPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, h * 0.38f)
                lineTo(w * 0.28f, h * 0.31f)
                lineTo(w * 0.55f, h * 0.36f)
                lineTo(w * 0.82f, h * 0.29f)
                lineTo(w, h * 0.35f)
                lineTo(w, h * 0.5f)
                lineTo(0f, h * 0.5f)
                close()
            }
            drawPath(
                path = mountainPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x221E293B), Color(0x110F172A)),
                    startY = h * 0.29f,
                    endY = h * 0.5f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ================= 1. TOP BAR =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile & Level Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0x331E293B))
                        .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(24.dp))
                        .clickable(onClick = onNavigateToProfile)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("profile_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💧", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Alchemist",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Level ${profile?.playerLevel ?: 1}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }

                // Currency & Settings
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameCoinPill(coins = profile?.coins ?: 300)

                    if (onNavigateBackToHub != null) {
                        IconButton(
                            onClick = onNavigateBackToHub,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("back_to_hub_button")
                                .background(Color(0x331E293B), CircleShape)
                                .border(1.dp, Color(0x4438BDF8), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = "Game Hub",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("settings_button")
                            .background(Color(0x331E293B), CircleShape)
                            .border(1.dp, Color(0x33475569), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ================= 2. LIVING GAME ENVIRONMENT & MASCOT =================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                contentAlignment = Alignment.Center
            ) {
                // Floating Pedestal / Island
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h * 0.72f

                    // Glowing magical aura under island
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x6638BDF8), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = w * 0.42f
                        ),
                        topLeft = Offset(cx - w * 0.42f, cy - 35f),
                        size = androidx.compose.ui.geometry.Size(w * 0.84f, 70f)
                    )

                    // Floating Island Top Rim (Lush Crystal Pedestal)
                    drawOval(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF0C4A6E)),
                            start = Offset(cx - w * 0.35f, cy - 18f),
                            end = Offset(cx + w * 0.35f, cy + 18f)
                        ),
                        topLeft = Offset(cx - w * 0.35f, cy - 18f),
                        size = androidx.compose.ui.geometry.Size(w * 0.7f, 36f)
                    )
                    // Inner bright highlight rim
                    drawOval(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                        ),
                        topLeft = Offset(cx - w * 0.33f, cy - 16f),
                        size = androidx.compose.ui.geometry.Size(w * 0.66f, 30f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                    )
                }

                // Lively Mascot "Aqua"
                WaterSortMascot(
                    size = 118.dp,
                    modifier = Modifier.offset(y = (-16).dp)
                )

                // Current World Expedition Badge (Pill above pedestal)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xEE0F172A))
                        .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = currentWorld.iconEmoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${currentWorld.name.uppercase()} • LEVEL $currentLevel",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // ================= 3. MAIN ACTION: LARGE 3D PLAY BUTTON =================
            // Requirement 22: PLAY OPENS WORLD MAP
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Tactile 3D Game Play CTA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .scale(playPulseScale)
                        .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF0284C7))
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1))
                            )
                        )
                        .border(2.dp, Color(0xFFE0F2FE), RoundedCornerShape(20.dp))
                        .clickable(onClick = onNavigateToWorlds)
                        .testTag("play_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.play).uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 3.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ================= 4. COMPACT DAILY STREAK STRIP =================
            // Requirement 19: Daily Reward remains compact strip/card (8-10dp padding, flame icon)
            TactileGameCard(
                modifier = Modifier.fillMaxWidth(),
                surfaceColor = Color(0xFF141D2C),
                borderColor = if (canClaimDaily) Color(0xFFF59E0B) else Color(0xFF334155),
                shadowColor = Color(0xFF080D15)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔥", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.streak_fire, profile?.currentStreak ?: 1),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = if (canClaimDaily) "Daily gift ready (+50🪙)" else "Claimed today!",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (canClaimDaily) Color(0xFFFDE047) else Color(0xFF94A3B8),
                                    fontWeight = if (canClaimDaily) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }

                    TactileGameButton(
                        text = if (canClaimDaily) stringResource(R.string.claim) else stringResource(R.string.claimed),
                        onClick = {
                            scope.launch {
                                repository.claimDailyReward()
                            }
                        },
                        color = if (canClaimDaily) GameButtonColor.GOLD else GameButtonColor.DARK,
                        enabled = canClaimDaily,
                        testTag = "claim_daily_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ================= 5. SECONDARY NAVIGATION 2x2 HUB =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HubTactileCard(
                    title = stringResource(R.string.worlds),
                    subtitle = "8 Worlds • 40 Zones",
                    icon = Icons.Default.Explore,
                    accentColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToWorlds,
                    testTag = "worlds_nav_card"
                )
                HubTactileCard(
                    title = stringResource(R.string.daily_challenge),
                    subtitle = "Unique Puzzle",
                    icon = Icons.Default.DateRange,
                    accentColor = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToDailyChallenge,
                    testTag = "daily_challenge_nav_card"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HubTactileCard(
                    title = stringResource(R.string.shop),
                    subtitle = "Themes & Skins",
                    icon = Icons.Default.ShoppingBag,
                    accentColor = Color(0xFFEC4899),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToShop,
                    testTag = "shop_nav_card"
                )
                HubTactileCard(
                    title = stringResource(R.string.achievements),
                    subtitle = "$totalStars Stars Earned",
                    icon = Icons.Default.EmojiEvents,
                    accentColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToProfile,
                    testTag = "profile_nav_card"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
fun HubTactileCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    TactileGameCard(
        modifier = modifier
            .height(112.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        surfaceColor = Color(0xFF151F30),
        borderColor = accentColor.copy(alpha = 0.45f),
        shadowColor = Color(0xFF090E18)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                )
            }
        }
    }
}
