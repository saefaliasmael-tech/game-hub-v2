package com.example.watersort.ui.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.watersort.core.database.InventoryItemEntity
import com.example.watersort.core.model.Bottle
import com.example.watersort.core.model.LiquidColor
import com.example.watersort.core.model.ThemeConfig
import com.example.watersort.core.repository.GameRepository
import com.example.watersort.ui.components.BottleView
import com.example.watersort.ui.components.GameCoinPill
import kotlinx.coroutines.launch

@Composable
fun ShopScreen(
    repository: GameRepository,
    onNavigateBack: () -> Unit
) {
    val profile by repository.profileFlow.collectAsStateWithLifecycle(initialValue = null)
    val inventory by repository.allInventoryFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }
    val categories = listOf("SKIN", "THEME")

    var previewSkinId by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var previewThemeId by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var isProcessing by remember { androidx.compose.runtime.mutableStateOf(false) }

    val activeSkinId = previewSkinId ?: profile?.equippedSkinId ?: "classic"
    val activeThemeId = previewThemeId ?: profile?.equippedThemeId ?: "classic"
    val activeTheme = remember(activeThemeId) { ThemeConfig.getTheme(activeThemeId) }

    val sampleBottle = remember {
        Bottle(
            id = 0,
            capacity = 4,
            layers = listOf(
                LiquidColor.BLUE,
                LiquidColor.CYAN,
                LiquidColor.PURPLE,
                LiquidColor.AMBER
            )
        )
    }

    val currentItems = inventory.filter { it.category == categories[selectedTab] }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = activeTheme.backgroundColors
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("shop_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = stringResource(R.string.shop),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                GameCoinPill(coins = profile?.coins ?: 0)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live Preview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_preview_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = activeTheme.surfaceColor.copy(alpha = 0.85f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LIVE PREVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = activeTheme.accentColor,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BottleView(
                        bottle = sampleBottle,
                        isSelected = true,
                        skinId = activeSkinId,
                        width = 56.dp,
                        height = 140.dp,
                        onBottleClick = {}
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${ThemeConfig.getSkin(activeSkinId).name} • ${activeTheme.name}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = activeTheme.surfaceColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = activeTheme.accentColor
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = stringResource(R.string.skins),
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) activeTheme.accentColor else Color(0xFF94A3B8)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = stringResource(R.string.themes),
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) activeTheme.accentColor else Color(0xFF94A3B8)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Item Cards List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currentItems) { item ->
                    val isEquipped = if (item.category == "SKIN") {
                        profile?.equippedSkinId == item.id
                    } else {
                        profile?.equippedThemeId == item.id
                    }
                    val isPreviewing = if (item.category == "SKIN") {
                        activeSkinId == item.id
                    } else {
                        activeThemeId == item.id
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shop_item_${item.id}"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPreviewing) activeTheme.surfaceColor else Color(0xFF1E293B)
                        ),
                        onClick = {
                            if (item.category == "SKIN") {
                                previewSkinId = item.id
                            } else {
                                previewThemeId = item.id
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        val iconEmoji = if (item.category == "SKIN") {
                                            ThemeConfig.SKINS[item.id]?.iconEmoji ?: "🧪"
                                        } else {
                                            "🎨"
                                        }
                                        Text(text = iconEmoji, fontSize = 22.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    if (!item.isOwned) {
                                        Text(
                                            text = "${item.price} 🪙",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFFFDE047),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    } else if (isPreviewing && !isEquipped) {
                                        Text(
                                            text = "Previewing",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = activeTheme.accentColor,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }

                            when {
                                isEquipped -> {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0x3310B981)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF34D399),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = stringResource(R.string.equipped),
                                                color = Color(0xFF34D399),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                                item.isOwned -> {
                                    OutlinedButton(
                                        onClick = {
                                            if (isProcessing) return@OutlinedButton
                                            scope.launch {
                                                isProcessing = true
                                                try {
                                                    if (item.category == "SKIN") {
                                                        repository.equipSkin(item.id)
                                                        previewSkinId = item.id
                                                    } else {
                                                        repository.equipTheme(item.id)
                                                        previewThemeId = item.id
                                                    }
                                                } finally {
                                                    isProcessing = false
                                                }
                                            }
                                        },
                                        enabled = !isProcessing,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(text = stringResource(R.string.equip), color = activeTheme.accentColor)
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = {
                                            if (isProcessing) return@Button
                                            scope.launch {
                                                isProcessing = true
                                                try {
                                                    val success = repository.buyItem(item)
                                                    if (!success) {
                                                        snackbarHostState.showSnackbar("Not enough coins!")
                                                    } else {
                                                        if (item.category == "SKIN") {
                                                            repository.equipSkin(item.id)
                                                            previewSkinId = item.id
                                                        } else {
                                                            repository.equipTheme(item.id)
                                                            previewThemeId = item.id
                                                        }
                                                    }
                                                } finally {
                                                    isProcessing = false
                                                }
                                            }
                                        },
                                        enabled = !isProcessing,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = activeTheme.primaryColor)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.unlock_for, item.price),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
