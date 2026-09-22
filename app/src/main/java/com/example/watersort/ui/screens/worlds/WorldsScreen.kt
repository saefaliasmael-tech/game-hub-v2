package com.example.watersort.ui.screens.worlds

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ads.ZubaLubaBannerAd
import com.example.watersort.core.database.LevelProgressEntity
import com.example.watersort.core.model.WorldConfig
import com.example.watersort.core.model.WorldInfo
import com.example.watersort.core.repository.GameRepository
import com.example.watersort.ui.components.AmbientEnvironment
import com.example.watersort.ui.components.GameCoinPill
import com.example.watersort.ui.components.TactileGameCard
import kotlin.math.sin

sealed class WorldMapItem {
    data class LevelNodeItem(
        val level: Int,
        val isCompleted: Boolean,
        val stars: Int,
        val isPlayable: Boolean,
        val isCurrent: Boolean,
        val xOffsetDp: Float,
        val prevXOffsetDp: Float?
    ) : WorldMapItem()

    data class LandmarkItem(
        val zoneIndex: Int,
        val name: String,
        val icon: String,
        val levelTrigger: Int,
        val isReached: Boolean
    ) : WorldMapItem()

    data class SecretPortalItem(
        val worldId: Int,
        val title: String,
        val unlockStars: Int,
        val isUnlocked: Boolean,
        val targetLevel: Int
    ) : WorldMapItem()
}

@Composable
fun WorldsScreen(
    repository: GameRepository,
    onNavigateBack: () -> Unit,
    onSelectLevel: (Int) -> Unit
) {
    val totalStars by repository.totalStarsCountFlow.collectAsStateWithLifecycle(initialValue = 0)
    val profile by repository.profileFlow.collectAsStateWithLifecycle(initialValue = null)
    val allProgress by repository.allProgressFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val progressMap = remember(allProgress) {
        allProgress.associateBy { it.levelId }
    }

    val currentUnlockedLevel = profile?.currentLevel ?: 1
    val initialWorldIndex = remember(currentUnlockedLevel) {
        val w = WorldConfig.forLevel(currentUnlockedLevel)
        WorldConfig.WORLDS.indexOfFirst { it.id == w.id }.coerceAtLeast(0)
    }

    var selectedWorldIndex by remember { mutableIntStateOf(initialWorldIndex) }
    val currentWorld = WorldConfig.WORLDS.getOrElse(selectedWorldIndex) { WorldConfig.WORLDS.first() }
    val isWorldUnlocked = totalStars >= currentWorld.requiredStars
    val worldColor = Color(currentWorld.themeColorHex)

    // Calculate World completion progress
    val worldLevels = (currentWorld.startLevel..currentWorld.endLevel).toList()
    val completedInWorld = worldLevels.count { progressMap[it]?.isCompleted == true }
    val starsInWorld = worldLevels.sumOf { progressMap[it]?.stars ?: 0 }
    val maxStarsInWorld = worldLevels.size * 3

    // Build map items along organic winding path
    val mapItems = remember(currentWorld, progressMap, currentUnlockedLevel, totalStars) {
        val items = mutableListOf<WorldMapItem>()
        var prevX: Float? = null

        // Winding path curve generator: Sine wave
        for ((idx, level) in worldLevels.withIndex()) {
            val progress = progressMap[level]
            val isCompleted = progress?.isCompleted == true
            val stars = progress?.stars ?: 0
            val isPlayable = isWorldUnlocked && (level <= currentUnlockedLevel)
            val isCurrent = (level == currentUnlockedLevel)

            // Sine wave horizontal sway between -85dp and +85dp
            val angle = (idx * 0.75f)
            val xOffset = sin(angle) * 82f

            items.add(
                WorldMapItem.LevelNodeItem(
                    level = level,
                    isCompleted = isCompleted,
                    stars = stars,
                    isPlayable = isPlayable,
                    isCurrent = isCurrent,
                    xOffsetDp = xOffset,
                    prevXOffsetDp = prevX
                )
            )
            prevX = xOffset

            // Insert Zone Landmark every 10 levels
            if (level % 10 == 0 && level < currentWorld.endLevel) {
                val zoneIdx = (level - currentWorld.startLevel) / 10
                val landmarkNames = listOf(
                    "🌿 Whispering Glade Gate",
                    "🏰 Crystalline Checkpoint",
                    "🔮 Astral Mana Shrine",
                    "⚡ Resonance Chamber"
                )
                items.add(
                    WorldMapItem.LandmarkItem(
                        zoneIndex = zoneIdx + 1,
                        name = landmarkNames.getOrElse(zoneIdx) { "Zone Checkpoint" },
                        icon = when (zoneIdx % 4) {
                            0 -> "🌿"
                            1 -> "🏰"
                            2 -> "🔮"
                            else -> "⚡"
                        },
                        levelTrigger = level,
                        isReached = currentUnlockedLevel >= level
                    )
                )
                prevX = null // Reset connecting line after landmark
            }
        }

        // Add Secret Level Portal at the end of the world
        val secretLevel = WorldConfig.SECRET_LEVELS.find { it.worldId == currentWorld.id }
        if (secretLevel != null) {
            items.add(
                WorldMapItem.SecretPortalItem(
                    worldId = currentWorld.id,
                    title = secretLevel.title,
                    unlockStars = secretLevel.unlockStarsThreshold,
                    isUnlocked = totalStars >= secretLevel.unlockStarsThreshold,
                    targetLevel = secretLevel.levelNumber
                )
            )
        }

        items
    }

    val lazyListState = rememberLazyListState()

    // Auto-scroll camera to player's latest unlocked level in this world
    LaunchedEffect(selectedWorldIndex, currentUnlockedLevel) {
        val targetIdx = mapItems.indexOfFirst {
            it is WorldMapItem.LevelNodeItem && it.level == currentUnlockedLevel
        }
        if (targetIdx >= 0) {
            lazyListState.animateScrollToItem(targetIdx.coerceAtLeast(0))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF090E17),
                        Color(0xFF0F1A2C),
                        Color(0xFF0A1320),
                        Color(0xFF060B12)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // World-specific dynamic ambient particle immersion
        AmbientEnvironment(envType = currentWorld.envType)

        Column(modifier = Modifier.fillMaxSize()) {
            // ================= 1. TOP HEADER =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .testTag("worlds_back_button")
                        .background(Color(0x331E293B), CircleShape)
                        .border(1.dp, Color(0x33475569), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "WORLD ADVENTURE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                    )
                    Text(
                        text = "${currentWorld.iconEmoji} ${currentWorld.name}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = worldColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                GameCoinPill(coins = profile?.coins ?: 300)
            }

            // ================= 2. WORLD SELECTOR TABS =================
            ScrollableTabRow(
                selectedTabIndex = selectedWorldIndex,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    if (selectedWorldIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedWorldIndex]),
                            color = worldColor,
                            height = 3.dp
                        )
                    }
                }
            ) {
                WorldConfig.WORLDS.forEachIndexed { index, world ->
                    val isUnlocked = totalStars >= world.requiredStars
                    Tab(
                        selected = (selectedWorldIndex == index),
                        onClick = { selectedWorldIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${world.iconEmoji} ${world.name}",
                                    fontWeight = if (selectedWorldIndex == index) FontWeight.Black else FontWeight.Normal,
                                    color = if (selectedWorldIndex == index) Color.White else Color(0xFF94A3B8),
                                    fontSize = 13.sp
                                )
                                if (!isUnlocked) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // ================= 3. WORLD STATS SUMMARY BANNER =================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x331E293B))
                    .border(1.dp, worldColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress: $completedInWorld/${worldLevels.size} Levels",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Stars",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$starsInWorld / $maxStarsInWorld ★",
                            color = Color(0xFFFDE047),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // ================= 4. ORGANIC WINDING LEVEL PATH =================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (!isWorldUnlocked) {
                    // World Locked State
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TactileGameCard(
                            modifier = Modifier.fillMaxWidth(),
                            surfaceColor = Color(0xFF1E1528),
                            borderColor = Color(0xFFF59E0B),
                            shadowColor = Color(0xFF0F0818)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "World Expedition Locked",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Earn ${currentWorld.requiredStars} total stars across previous worlds to unlock!",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8)),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Current Stars: $totalStars / ${currentWorld.requiredStars} ★",
                                    color = Color(0xFFFDE047),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(mapItems) { index, item ->
                            when (item) {
                                is WorldMapItem.LevelNodeItem -> {
                                    OrganicLevelNodeRow(
                                        item = item,
                                        worldColor = worldColor,
                                        onSelectLevel = onSelectLevel
                                    )
                                }
                                is WorldMapItem.LandmarkItem -> {
                                    LandmarkCheckpointCard(
                                        item = item,
                                        worldColor = worldColor
                                    )
                                }
                                is WorldMapItem.SecretPortalItem -> {
                                    SecretPortalCard(
                                        item = item,
                                        totalStars = totalStars,
                                        onSelectLevel = onSelectLevel
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }

            // Non-intrusive Banner Ad docked safely at the bottom of the level select screen
            ZubaLubaBannerAd(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                backgroundColor = Color.Transparent
            )
        }
    }
}

/**
 * Organic Node placed along the winding path with connecting trail
 */
@Composable
fun OrganicLevelNodeRow(
    item: WorldMapItem.LevelNodeItem,
    worldColor: Color,
    onSelectLevel: (Int) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "current_node_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp),
        contentAlignment = Alignment.Center
    ) {
        // Organic Connecting Trail line to previous node
        if (item.prevXOffsetDp != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val startX = (w / 2f) + (item.prevXOffsetDp * density)
                val endX = (w / 2f) + (item.xOffsetDp * density)

                // Dashed path linking consecutive nodes
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(startX, -h * 0.4f)
                    cubicTo(
                        startX, h * 0.1f,
                        endX, h * 0.3f,
                        endX, h * 0.5f
                    )
                }

                drawPath(
                    path = path,
                    color = if (item.isPlayable) worldColor.copy(alpha = 0.55f) else Color(0x33475569),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                    )
                )
            }
        }

        // Node Button with horizontal displacement
        Column(
            modifier = Modifier
                .offset(x = item.xOffsetDp.dp)
                .clickable(enabled = item.isPlayable) { onSelectLevel(item.level) }
                .testTag("level_node_${item.level}"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "YOU ARE HERE" mascot marker above the current level
            if (item.isCurrent) {
                Box(
                    modifier = Modifier
                        .offset(y = (-6).dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0284C7))
                        .border(1.dp, Color(0xFFE0F2FE), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💧", fontSize = 10.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "YOU ARE HERE",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Node Circle
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .scale(if (item.isCurrent) pulseScale else 1f)
                    .shadow(
                        elevation = if (item.isCurrent) 10.dp else if (item.isCompleted) 6.dp else 2.dp,
                        shape = CircleShape,
                        spotColor = if (item.isCurrent) Color(0xFF38BDF8) else worldColor
                    )
                    .clip(CircleShape)
                    .background(
                        brush = when {
                            item.isCurrent -> Brush.radialGradient(
                                listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1))
                            )
                            item.isCompleted -> Brush.verticalGradient(
                                listOf(worldColor, worldColor.copy(alpha = 0.75f))
                            )
                            else -> Brush.verticalGradient(
                                listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        }
                    )
                    .border(
                        width = if (item.isCurrent) 2.5.dp else 1.5.dp,
                        color = when {
                            item.isCurrent -> Color.White
                            item.isCompleted -> Color(0xFFE0F2FE)
                            else -> Color(0xFF334155)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!item.isPlayable) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "${item.level}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }
            }

            // Stars earned under completed node
            if (item.isCompleted) {
                Row(
                    modifier = Modifier.offset(y = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    repeat(3) { starIdx ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (starIdx < item.stars) Color(0xFFFBBF24) else Color(0x55475569),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Illustrated Landmark Checkpoint
 */
@Composable
fun LandmarkCheckpointCard(
    item: WorldMapItem.LandmarkItem,
    worldColor: Color
) {
    TactileGameCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        surfaceColor = if (item.isReached) Color(0xFF162438) else Color(0xFF111827),
        borderColor = if (item.isReached) worldColor.copy(alpha = 0.6f) else Color(0xFF1E293B),
        shadowColor = Color(0xFF090E18)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.icon, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = if (item.isReached) Color.White else Color(0xFF94A3B8)
                        )
                    )
                    Text(
                        text = if (item.isReached) "Checkpoint Reached • Zone ${item.zoneIndex}" else "Locked • Reach Level ${item.levelTrigger}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (item.isReached) Color(0xFF38BDF8) else Color(0xFF64748B),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (item.isReached) Color(0x3310B981) else Color(0x22475569))
                    .padding(6.dp)
            ) {
                Text(
                    text = if (item.isReached) "✓" else "🔒",
                    fontSize = 12.sp,
                    color = if (item.isReached) Color(0xFF34D399) else Color(0xFF64748B)
                )
            }
        }
    }
}

/**
 * Secret Level Portal Card at the end of the world
 */
@Composable
fun SecretPortalCard(
    item: WorldMapItem.SecretPortalItem,
    totalStars: Int,
    onSelectLevel: (Int) -> Unit
) {
    TactileGameCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = item.isUnlocked) {
                if (item.isUnlocked) onSelectLevel(item.targetLevel)
            }
            .testTag("secret_portal_${item.worldId}"),
        surfaceColor = if (item.isUnlocked) Color(0xFF27153E) else Color(0xFF13101E),
        borderColor = if (item.isUnlocked) Color(0xFFA855F7) else Color(0xFF2E2342),
        shadowColor = Color(0xFF0A0512)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🌀", fontSize = 32.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ANCIENT TRIAL: ${item.title.uppercase()}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = if (item.isUnlocked) Color(0xFFF3E8FF) else Color(0xFF94A3B8)
                        )
                    )
                    Text(
                        text = if (item.isUnlocked) "Tap to enter secret trial master puzzle" else "Requires ${item.unlockStars}★ across all worlds ($totalStars/${item.unlockStars}★)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (item.isUnlocked) Color(0xFFD8B4FE) else Color(0xFF64748B)
                        )
                    )
                }
            }

            if (item.isUnlocked) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF9333EA))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "ENTER",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
