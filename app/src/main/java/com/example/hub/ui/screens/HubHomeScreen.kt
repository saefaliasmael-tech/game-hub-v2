package com.example.hub.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.hub.data.HubPreferences
import com.example.hub.model.GameCategory
import com.example.hub.model.GameInfo
import com.example.hub.registry.GameRegistry
import com.example.hub.ui.components.GameCard
import com.example.hub.ui.components.HubBottomNavigation
import com.example.hub.ui.components.HubTab
import com.example.hub.ui.components.TopGameHeroCard
import com.example.hub.ui.components.UpdateDialog
import com.example.hub.ui.theme.HubColors
import com.example.update.UpdateChecker
import com.example.update.UpdateDownloader
import com.example.update.UpdateInstaller
import com.example.update.model.UpdateState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubHomeScreen(
    hubPreferences: HubPreferences,
    onNavigateToGameDetails: (String) -> Unit,
    onLaunchGame: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(HubTab.HOME) }

    val favorites by hubPreferences.favoritesFlow.collectAsState()
    val recentlyPlayedIds by hubPreferences.recentlyPlayedFlow.collectAsState()
    val soundEnabled by hubPreferences.soundEnabledFlow.collectAsState()
    val vibrationEnabled by hubPreferences.vibrationEnabledFlow.collectAsState()

    var selectedCategory by remember { mutableStateOf(GameCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    // Update state management
    val updateChecker = remember { UpdateChecker(context) }
    val updateDownloader = remember { UpdateDownloader(context) }
    val updateInstaller = remember { UpdateInstaller(context) }
    var updateState by remember { mutableStateOf<UpdateState>(UpdateState.Idle) }
    var isCheckingUpdates by remember { mutableStateOf(false) }

    // Check updates once in background on launch
    LaunchedEffect(Unit) {
        val result = updateChecker.checkForUpdates()
        if (result is UpdateState.UpdateAvailable) {
            updateState = result
        }
    }

    val allGames = remember { GameRegistry.getAllGames() }
    val topGames = remember { allGames.filter { it.isFeatured || it.isAvailable }.take(6) }

    val filteredGames = remember(selectedCategory, searchQuery) {
        var list = if (selectedCategory == GameCategory.ALL) allGames else allGames.filter { it.category == selectedCategory }
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.category.name.contains(searchQuery, ignoreCase = true)
            }
        }
        list
    }

    val favoriteGames = remember(favorites) {
        allGames.filter { favorites.contains(it.id) }
    }

    // Most recent game if any
    val recentGame = remember(recentlyPlayedIds) {
        recentlyPlayedIds.firstOrNull()?.let { GameRegistry.getGameById(it) }
    }

    val handleLaunch: (String) -> Unit = { gameId ->
        hubPreferences.recordGamePlayed(gameId)
        onLaunchGame(gameId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(HubColors.HeroGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SportsEsports,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = stringResource(R.string.game_hub_title),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = HubColors.HighText,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = stringResource(R.string.game_hub_subtitle),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = HubColors.LowText
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isSearchExpanded = !isSearchExpanded
                            if (!isSearchExpanded) searchQuery = ""
                        },
                        modifier = Modifier.testTag("hub_search_toggle")
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = HubColors.HighText
                        )
                    }

                    IconButton(
                        onClick = { currentTab = HubTab.FAVORITES },
                        modifier = Modifier.testTag("hub_filter_favorites")
                    ) {
                        Icon(
                            imageVector = if (currentTab == HubTab.FAVORITES) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorites",
                            tint = if (currentTab == HubTab.FAVORITES) HubColors.Magenta else HubColors.HighText
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("hub_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = HubColors.HighText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HubColors.Void
                )
            )
        },
        bottomBar = {
            HubBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { selected ->
                    currentTab = selected
                    if (selected == HubTab.HOME) {
                        searchQuery = ""
                        isSearchExpanded = false
                    }
                }
            )
        },
        containerColor = HubColors.Void,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HubColors.Void)
        ) {
            when (currentTab) {
                HubTab.HOME -> {
                    HomeScreenContent(
                        isSearchExpanded = isSearchExpanded,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        topGames = topGames,
                        recentGame = recentGame,
                        selectedCategory = selectedCategory,
                        onSelectCategory = { selectedCategory = it },
                        gamesList = filteredGames,
                        favorites = favorites,
                        onGameClick = onNavigateToGameDetails,
                        onPlayClick = handleLaunch,
                        onToggleFavorite = { hubPreferences.toggleFavorite(it) },
                        onViewAllClick = { currentTab = HubTab.GAMES }
                    )
                }

                HubTab.GAMES -> {
                    GamesCatalogContent(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        selectedCategory = selectedCategory,
                        onSelectCategory = { selectedCategory = it },
                        gamesList = filteredGames,
                        favorites = favorites,
                        onGameClick = onNavigateToGameDetails,
                        onPlayClick = handleLaunch,
                        onToggleFavorite = { hubPreferences.toggleFavorite(it) }
                    )
                }

                HubTab.FAVORITES -> {
                    FavoritesTabContent(
                        favoriteGames = favoriteGames,
                        onGameClick = onNavigateToGameDetails,
                        onPlayClick = handleLaunch,
                        onToggleFavorite = { hubPreferences.toggleFavorite(it) },
                        onExploreCatalogClick = { currentTab = HubTab.GAMES }
                    )
                }

                HubTab.SETTINGS -> {
                    EmbeddedSettingsContent(
                        soundEnabled = soundEnabled,
                        onToggleSound = { hubPreferences.setSoundEnabled(!soundEnabled) },
                        vibrationEnabled = vibrationEnabled,
                        onToggleVibration = { hubPreferences.setVibrationEnabled(!vibrationEnabled) },
                        isCheckingUpdates = isCheckingUpdates,
                        onCheckUpdates = {
                            coroutineScope.launch {
                                isCheckingUpdates = true
                                val result = updateChecker.checkForUpdates()
                                isCheckingUpdates = false
                                if (result is UpdateState.UpToDate) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.latest_version_message),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    updateState = result
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // OTA Update Dialog
    val currentUpdate = updateState
    if (currentUpdate !is UpdateState.Idle && currentUpdate !is UpdateState.UpToDate && currentUpdate !is UpdateState.Checking) {
        UpdateDialog(
            state = currentUpdate,
            onStartDownload = {
                val manifest = (currentUpdate as? UpdateState.UpdateAvailable)?.manifest
                if (manifest != null) {
                    coroutineScope.launch {
                        updateDownloader.downloadApk(manifest).collect { st ->
                            updateState = st
                        }
                    }
                }
            },
            onInstall = {
                val readyState = currentUpdate as? UpdateState.ReadyToInstall
                if (readyState != null) {
                    updateInstaller.installApk(readyState.apkFile)
                }
            },
            onDismiss = { updateState = UpdateState.Idle },
            onRetry = {
                coroutineScope.launch {
                    val res = updateChecker.checkForUpdates()
                    updateState = res
                }
            }
        )
    }
}

@Composable
private fun HomeScreenContent(
    isSearchExpanded: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    topGames: List<GameInfo>,
    recentGame: GameInfo?,
    selectedCategory: GameCategory,
    onSelectCategory: (GameCategory) -> Unit,
    gamesList: List<GameInfo>,
    favorites: Set<String>,
    onGameClick: (String) -> Unit,
    onPlayClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onViewAllClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("games_list"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Expandable or dynamic Search Field
        if (isSearchExpanded || searchQuery.isNotEmpty()) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(text = stringResource(R.string.search_games), color = HubColors.LowText) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = HubColors.SoftViolet)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear", tint = HubColors.LowText)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hub_search_field"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = HubColors.SurfaceMid,
                        unfocusedContainerColor = HubColors.SurfaceLow,
                        focusedBorderColor = HubColors.PrimaryViolet,
                        unfocusedBorderColor = HubColors.Hairline,
                        focusedTextColor = HubColors.HighText,
                        unfocusedTextColor = HubColors.HighText
                    ),
                    singleLine = true
                )
            }
        }

        // Zuba Luba Platform Showcase Hero Banner
        if (searchQuery.isEmpty()) {
            item {
                ZubaLubaHeroBanner(
                    onExploreClick = onViewAllClick
                )
            }
        }

        // Top Games / Featured Carousel (shown when not searching)
        if (searchQuery.isEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = HubColors.Magenta,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.top_games),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = HubColors.HighText,
                                    fontSize = 18.sp
                                )
                            )
                        }

                        TextButton(onClick = onViewAllClick) {
                            Text(
                                text = stringResource(R.string.view_all),
                                color = HubColors.Cyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(topGames, key = { it.id }) { game ->
                            TopGameHeroCard(
                                game = game,
                                onPlayClick = { onPlayClick(game.id) }
                            )
                        }
                    }
                }
            }

            // Continue Playing Strip (Only if there is a recently played game)
            if (recentGame != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, HubColors.CardBorder, RoundedCornerShape(18.dp))
                            .clickable { onPlayClick(recentGame.id) },
                        colors = CardDefaults.cardColors(containerColor = HubColors.SurfaceMid)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(HubColors.CyanGlowGradient),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.History,
                                        contentDescription = null,
                                        tint = HubColors.Void,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.continue_playing),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = HubColors.Cyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = recentGame.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = HubColors.HighText
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(HubColors.PlayButtonGradient)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.play),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Categories Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.categories),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = HubColors.HighText
                        )
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(GameCategory.entries) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectCategory(cat) },
                                label = {
                                    Text(
                                        text = when (cat) {
                                            GameCategory.ALL -> stringResource(R.string.category_all)
                                            GameCategory.PUZZLE -> stringResource(R.string.category_puzzle)
                                            GameCategory.ARCADE -> stringResource(R.string.category_arcade)
                                            GameCategory.LOGIC -> stringResource(R.string.category_logic)
                                            GameCategory.CASUAL -> stringResource(R.string.category_casual)
                                            GameCategory.BOARD -> stringResource(R.string.category_board)
                                            GameCategory.STRATEGY -> stringResource(R.string.category_strategy)
                                        },
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HubColors.PrimaryViolet,
                                    selectedLabelColor = Color.White,
                                    containerColor = HubColors.SurfaceMid,
                                    labelColor = HubColors.LowText
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) HubColors.SoftViolet else HubColors.Hairline
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section Title: All Games
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.GridView,
                        contentDescription = null,
                        tint = HubColors.Cyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.all_games),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = HubColors.HighText
                        )
                    )
                }

                Text(
                    text = "${gamesList.size} games",
                    style = MaterialTheme.typography.labelSmall.copy(color = HubColors.LowText)
                )
            }
        }

        // Games List items
        items(gamesList, key = { it.id }) { game ->
            GameCard(
                game = game,
                isFavorite = favorites.contains(game.id),
                onGameClick = { onGameClick(game.id) },
                onPlayClick = { onPlayClick(game.id) },
                onFavoriteToggle = { onToggleFavorite(game.id) }
            )
        }
    }
}

@Composable
private fun GamesCatalogContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: GameCategory,
    onSelectCategory: (GameCategory) -> Unit,
    gamesList: List<GameInfo>,
    favorites: Set<String>,
    onGameClick: (String) -> Unit,
    onPlayClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("games_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(text = stringResource(R.string.search_games), color = HubColors.LowText) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = HubColors.SoftViolet)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear", tint = HubColors.LowText)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hub_search_field"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = HubColors.SurfaceMid,
                    unfocusedContainerColor = HubColors.SurfaceLow,
                    focusedBorderColor = HubColors.PrimaryViolet,
                    unfocusedBorderColor = HubColors.Hairline,
                    focusedTextColor = HubColors.HighText,
                    unfocusedTextColor = HubColors.HighText
                ),
                singleLine = true
            )
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(GameCategory.entries) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCategory(cat) },
                        label = {
                            Text(
                                text = when (cat) {
                                    GameCategory.ALL -> stringResource(R.string.category_all)
                                    GameCategory.PUZZLE -> stringResource(R.string.category_puzzle)
                                    GameCategory.ARCADE -> stringResource(R.string.category_arcade)
                                    GameCategory.LOGIC -> stringResource(R.string.category_logic)
                                    GameCategory.CASUAL -> stringResource(R.string.category_casual)
                                    GameCategory.BOARD -> stringResource(R.string.category_board)
                                    GameCategory.STRATEGY -> stringResource(R.string.category_strategy)
                                },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HubColors.PrimaryViolet,
                            selectedLabelColor = Color.White,
                            containerColor = HubColors.SurfaceMid,
                            labelColor = HubColors.LowText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) HubColors.SoftViolet else HubColors.Hairline
                        )
                    )
                }
            }
        }

        // Items count
        item {
            Text(
                text = "${gamesList.size} games available",
                style = MaterialTheme.typography.labelMedium.copy(color = HubColors.LowText)
            )
        }

        // Games items
        items(gamesList, key = { it.id }) { game ->
            GameCard(
                game = game,
                isFavorite = favorites.contains(game.id),
                onGameClick = { onGameClick(game.id) },
                onPlayClick = { onPlayClick(game.id) },
                onFavoriteToggle = { onToggleFavorite(game.id) }
            )
        }
    }
}

@Composable
private fun FavoritesTabContent(
    favoriteGames: List<GameInfo>,
    onGameClick: (String) -> Unit,
    onPlayClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onExploreCatalogClick: () -> Unit
) {
    if (favoriteGames.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(HubColors.SurfaceMid)
                        .border(1.dp, HubColors.Hairline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        tint = HubColors.Magenta,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.no_favorites_yet),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = HubColors.HighText
                    )
                )

                Text(
                    text = stringResource(R.string.no_favorites_desc),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = HubColors.LowText
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(HubColors.PlayButtonGradient)
                        .clickable { onExploreCatalogClick() }
                        .padding(horizontal = 28.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.explore_catalog),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("games_list"),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Text(
                    text = "${favoriteGames.size} Favorites",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = HubColors.HighText
                    )
                )
            }

            items(favoriteGames, key = { it.id }) { game ->
                GameCard(
                    game = game,
                    isFavorite = true,
                    onGameClick = { onGameClick(game.id) },
                    onPlayClick = { onPlayClick(game.id) },
                    onFavoriteToggle = { onToggleFavorite(game.id) }
                )
            }
        }
    }
}

@Composable
private fun EmbeddedSettingsContent(
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    vibrationEnabled: Boolean,
    onToggleVibration: () -> Unit,
    isCheckingUpdates: Boolean,
    onCheckUpdates: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App identity card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = HubColors.SurfaceMid),
                border = androidx.compose.foundation.BorderStroke(1.dp, HubColors.Hairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(HubColors.HeroGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SportsEsports,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.game_hub_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = HubColors.HighText
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.app_version_format,
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(color = HubColors.LowText)
                    )
                }
            }
        }

        // Preferences section
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = HubColors.SurfaceLow),
                border = androidx.compose.foundation.BorderStroke(1.dp, HubColors.Hairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = HubColors.Cyan
                        )
                    )

                    // Sound Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.VolumeUp, contentDescription = null, tint = HubColors.SoftViolet)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = stringResource(R.string.sound_effects), color = HubColors.HighText)
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { onToggleSound() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = HubColors.PrimaryViolet
                            )
                        )
                    }

                    HorizontalDivider(color = HubColors.Hairline)

                    // Vibration Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Vibration, contentDescription = null, tint = HubColors.SoftViolet)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = stringResource(R.string.haptics), color = HubColors.HighText)
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { onToggleVibration() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = HubColors.PrimaryViolet
                            )
                        )
                    }
                }
            }
        }

        // OTA Update section
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = HubColors.SurfaceLow),
                border = androidx.compose.foundation.BorderStroke(1.dp, HubColors.Hairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "OTA Full APK Updates",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = HubColors.Cyan)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Check for full app APK updates without losing any saved progress or high scores.",
                        style = MaterialTheme.typography.bodySmall.copy(color = HubColors.LowText)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(HubColors.PlayButtonGradient)
                            .clickable(enabled = !isCheckingUpdates) { onCheckUpdates() }
                            .padding(vertical = 12.dp)
                            .testTag("check_updates_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCheckingUpdates) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Filled.CloudSync, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.check_for_updates),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Architecture specs
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = HubColors.SurfaceLow),
                border = androidx.compose.foundation.BorderStroke(1.dp, HubColors.Hairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.about_hub),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = HubColors.Cyan)
                    )
                    Text(
                        text = "• Package: ${BuildConfig.APPLICATION_ID}",
                        style = MaterialTheme.typography.bodySmall.copy(color = HubColors.LowText)
                    )
                    Text(
                        text = "• Standalone Game Architecture: Zero shared logic or state across games.",
                        style = MaterialTheme.typography.bodySmall.copy(color = HubColors.LowText)
                    )
                    Text(
                        text = "• Offline-First: All games run locally without network dependencies.",
                        style = MaterialTheme.typography.bodySmall.copy(color = HubColors.LowText)
                    )
                }
            }
        }
    }
}

@Composable
fun ZubaLubaHeroBanner(
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(HubColors.PrimaryViolet, HubColors.Cyan)
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onExploreClick() }
            .testTag("hub_hero_banner"),
        colors = CardDefaults.cardColors(containerColor = HubColors.SurfaceMid),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 8.5f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_zuba_luba_hero),
                contentDescription = "Zuba Luba Universe",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Bottom gradient overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                HubColors.Void.copy(alpha = 0.45f),
                                HubColors.Void.copy(alpha = 0.94f)
                            )
                        )
                    )
            )

            // Overlay content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HubColors.SurfaceHigh.copy(alpha = 0.85f),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, HubColors.Cyan.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "19 STANDALONE GAMES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HubColors.Cyan,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HubColors.PrimaryViolet.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = "v1.0.0",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Bottom Titles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.game_hub_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = stringResource(R.string.game_hub_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = HubColors.HighText.copy(alpha = 0.85f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = HubColors.Cyan,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = HubColors.Void,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.explore_catalog),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = HubColors.Void,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
