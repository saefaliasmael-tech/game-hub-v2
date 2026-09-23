package com.example.knifehit.presentation.ui

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
import com.example.knifehit.core.level.KnifeHitLevelManager
import com.example.knifehit.core.model.ALL_KNIFE_SKINS
import com.example.knifehit.core.model.KnifeHitGameMode
import com.example.knifehit.core.model.KnifeSkin
import com.example.knifehit.presentation.KnifeHitViewModel
import com.example.watersort.core.database.GameProgressEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnifeHitHomeScreen(
    viewModel: KnifeHitViewModel,
    onNavigateBack: () -> Unit,
    onStartCampaignLevel: (Int) -> Unit,
    onStartBossRush: (Int) -> Unit
) {
    val progressList by viewModel.progressFlow.collectAsState(initial = emptyList())
    val totalStars by viewModel.totalStarsFlow.collectAsState(initial = 0)
    val completedLevels by viewModel.completedLevelsFlow.collectAsState(initial = 0)
    val soundEnabled by viewModel.soundEnabledFlow.collectAsState()
    val hapticEnabled by viewModel.hapticEnabledFlow.collectAsState()
    val apples by viewModel.applesFlow.collectAsState()
    val equippedSkin by viewModel.equippedSkinFlow.collectAsState()
    val unlockedSkins by viewModel.unlockedSkinsFlow.collectAsState()

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
                            imageVector = Icons.Default.Straighten,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Knife Hit",
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
                    Surface(
                        color = Color(0xFFDC2626).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("🍎", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "$apples",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1917)
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
        containerColor = Color(0xFF0C0A09)
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
                color = Color(0xFF292524),
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
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$completedLevels/100", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                        Text("Levels", fontSize = 12.sp, color = Color(0xFFA8A29E))
                    }
                    Divider(modifier = Modifier.height(36.dp).width(1.dp), color = Color(0xFF44403C))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Color(0xFFFFB703), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${viewModel.getBossesDefeated()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                        Text("Bosses", fontSize = 12.sp, color = Color(0xFFA8A29E))
                    }
                    Divider(modifier = Modifier.height(36.dp).width(1.dp), color = Color(0xFF44403C))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${viewModel.getHighestCombo()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                        Text("Max Combo", fontSize = 12.sp, color = Color(0xFFA8A29E))
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1C1917),
                contentColor = Color(0xFFEF4444),
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Campaign", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Boss Rush", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Armory", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                0 -> KnifeHitCampaignGrid(
                    progressMap = progressMap,
                    onSelectLevel = onStartCampaignLevel
                )
                1 -> KnifeHitBossRushView(
                    onStartBossRush = { onStartBossRush(1) }
                )
                2 -> KnifeHitShopView(
                    apples = apples,
                    equippedSkin = equippedSkin,
                    unlockedSkins = unlockedSkins,
                    onBuy = { viewModel.buySkin(it) },
                    onEquip = { viewModel.equipSkin(it) }
                )
            }
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Knife Hit Settings", color = Color.White) },
            containerColor = Color(0xFF292524),
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
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Close", color = Color(0xFFEF4444))
                }
            }
        )
    }
}

@Composable
private fun KnifeHitCampaignGrid(
    progressMap: Map<Int, GameProgressEntity>,
    onSelectLevel: (Int) -> Unit
) {
    val allLevels = remember { KnifeHitLevelManager.getAllLevels() }

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
            val isBoss = (config.levelNumber % 5 == 0)

            val borderColor = when {
                isBoss -> Color(0xFFFFB703)
                config.levelNumber <= 25 -> Color(0xFF8B5A2B)
                config.levelNumber <= 50 -> Color(0xFF2E7D32)
                config.levelNumber <= 75 -> Color(0xFFFF9800)
                else -> Color(0xFF00E5FF)
            }

            Surface(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = isUnlocked) {
                        onSelectLevel(config.levelNumber)
                    },
                color = if (isUnlocked) Color(0xFF292524) else Color(0xFF1C1917),
                shape = RoundedCornerShape(12.dp),
                border = if (isUnlocked) androidx.compose.foundation.BorderStroke(if (isBoss) 2.dp else 1.5.dp, borderColor) else null
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (!isUnlocked) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFF57534E), modifier = Modifier.size(20.dp))
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (isBoss) {
                                Icon(Icons.Default.MilitaryTech, contentDescription = "Boss", tint = Color(0xFFFFB703), modifier = Modifier.size(14.dp))
                            }
                            Text(
                                "${config.levelNumber}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isBoss) Color(0xFFFFB703) else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KnifeHitBossRushView(
    onStartBossRush: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFFFB703).copy(alpha = 0.15f),
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = Color(0xFFFFB703),
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Boss Rush Gauntlet",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Face consecutive powerful Boss targets without respite.\nDynamic speeds, armor plates, and high apple rewards!",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = Color(0xFFA8A29E),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartBossRush,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(54.dp)
        ) {
            Text("Enter Boss Rush", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        }
    }
}

@Composable
private fun KnifeHitShopView(
    apples: Int,
    equippedSkin: String,
    unlockedSkins: Set<String>,
    onBuy: (KnifeSkin) -> Unit,
    onEquip: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(ALL_KNIFE_SKINS) { skin ->
            val isUnlocked = unlockedSkins.contains(skin.id)
            val isEquipped = (skin.id == equippedSkin)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF292524))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = skin.bladeColor.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, skin.bladeColor)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Straighten, contentDescription = null, tint = skin.bladeColor)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(skin.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            if (!isUnlocked) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🍎 ${skin.costApples}", fontSize = 13.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    if (isEquipped) {
                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "EQUIPPED",
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    } else if (isUnlocked) {
                        OutlinedButton(
                            onClick = { onEquip(skin.id) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Equip", color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = { onBuy(skin) },
                            enabled = apples >= skin.costApples,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Unlock")
                        }
                    }
                }
            }
        }
    }
}
