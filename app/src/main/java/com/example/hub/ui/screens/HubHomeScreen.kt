package com.example.hub.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.hub.data.HubPreferences
import com.example.hub.model.GameCategory
import com.example.hub.model.GameInfo
import com.example.hub.registry.GameRegistry
import com.example.hub.ui.components.GameCard
import com.example.hub.ui.components.UpdateDialog
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

    val favorites by hubPreferences.favoritesFlow.collectAsState()
    val recentlyPlayedIds by hubPreferences.recentlyPlayedFlow.collectAsState()

    var selectedCategory by remember { mutableStateOf(GameCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    // Update state management
    val updateChecker = remember { UpdateChecker(context) }
    val updateDownloader = remember { UpdateDownloader(context) }
    val updateInstaller = remember { UpdateInstaller(context) }
    var updateState by remember { mutableStateOf<UpdateState>(UpdateState.Idle) }

    // Check updates once in background on launch (graceful failure)
    LaunchedEffect(Unit) {
        val result = updateChecker.checkForUpdates()
        if (result is UpdateState.UpdateAvailable) {
            updateState = result
        }
    }

    val allGames = remember { GameRegistry.getAllGames() }
    val featuredGame = remember { GameRegistry.getFeaturedGame() }

    val filteredGames = remember(selectedCategory, searchQuery, showOnlyFavorites, favorites) {
        var list = if (selectedCategory == GameCategory.ALL) allGames else allGames.filter { it.category == selectedCategory }
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.category.name.contains(searchQuery, ignoreCase = true)
            }
        }
        if (showOnlyFavorites) {
            list = list.filter { favorites.contains(it.id) }
        }
        list
    }

    // Most recent game if any
    val recentGame = remember(recentlyPlayedIds) {
        recentlyPlayedIds.firstOrNull()?.let { GameRegistry.getGameById(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF00B4D8), Color(0xFF7209B7))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SportsEsports,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.game_hub_title),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = stringResource(R.string.game_hub_subtitle),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showOnlyFavorites = !showOnlyFavorites },
                        modifier = Modifier.testTag("hub_filter_favorites")
                    ) {
                        Icon(
                            imageVector = if (showOnlyFavorites) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Filter Favorites",
                            tint = if (showOnlyFavorites) Color(0xFFE63946) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("hub_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Search Field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(text = stringResource(R.string.search_games)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hub_search_field"),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            // Categories Carousel
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(GameCategory.entries) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(
                                    text = when (cat) {
                                        GameCategory.ALL -> stringResource(R.string.category_all)
                                        GameCategory.PUZZLE -> stringResource(R.string.category_puzzle)
                                        GameCategory.ARCADE -> stringResource(R.string.category_arcade)
                                        GameCategory.LOGIC -> stringResource(R.string.category_logic)
                                        GameCategory.CASUAL -> stringResource(R.string.category_casual)
                                        GameCategory.BOARD -> stringResource(R.string.category_board)
                                    }
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Continue Playing Strip (if recent game exists)
            if (recentGame != null && searchQuery.isEmpty() && !showOnlyFavorites) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                hubPreferences.recordGamePlayed(recentGame.id)
                                onLaunchGame(recentGame.id)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
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
                                Icon(
                                    imageVector = Icons.Filled.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.continue_playing),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = recentGame.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    hubPreferences.recordGamePlayed(recentGame.id)
                                    onLaunchGame(recentGame.id)
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.play), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Featured Hero Card (if available and not searching)
            if (featuredGame != null && searchQuery.isEmpty() && !showOnlyFavorites && selectedCategory == GameCategory.ALL) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .border(1.dp, Color(0xFFFFB703).copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                            .clickable { onNavigateToGameDetails(featuredGame.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF03045E)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Badge(
                                    containerColor = Color(0xFFFFB703),
                                    contentColor = Color(0xFF03045E)
                                ) {
                                    Text(
                                        text = "★ " + stringResource(R.string.featured_game).uppercase(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { hubPreferences.toggleFavorite(featuredGame.id) }
                                ) {
                                    Icon(
                                        imageVector = if (favorites.contains(featuredGame.id)) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (favorites.contains(featuredGame.id)) Color(0xFFE63946) else Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = featuredGame.name,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = featuredGame.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.85f),
                                    lineHeight = 20.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "8 Worlds • Daily Puzzles • 100+ Levels",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF90E0EF),
                                        fontWeight = FontWeight.Medium
                                    )
                                )

                                Button(
                                    onClick = {
                                        hubPreferences.recordGamePlayed(featuredGame.id)
                                        onLaunchGame(featuredGame.id)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00B4D8),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.play), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Section Header: All Games
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showOnlyFavorites) stringResource(R.string.favorites) else stringResource(R.string.all_games),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${filteredGames.size}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Games List
            if (filteredGames.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (showOnlyFavorites) Icons.Outlined.FavoriteBorder else Icons.Filled.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (showOnlyFavorites) stringResource(R.string.no_favorites_yet) else stringResource(R.string.no_games_found),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }
                }
            } else {
                items(filteredGames, key = { it.id }) { game ->
                    GameCard(
                        game = game,
                        isFavorite = favorites.contains(game.id),
                        onGameClick = { onNavigateToGameDetails(game.id) },
                        onPlayClick = {
                            if (game.isAvailable) {
                                hubPreferences.recordGamePlayed(game.id)
                                onLaunchGame(game.id)
                            } else {
                                onNavigateToGameDetails(game.id)
                            }
                        },
                        onFavoriteToggle = { hubPreferences.toggleFavorite(game.id) }
                    )
                }
            }
        }
    }

    // Update Dialog handler
    UpdateDialog(
        state = updateState,
        onStartDownload = {
            val manifest = (updateState as? UpdateState.UpdateAvailable)?.manifest
            if (manifest != null) {
                coroutineScope.launch {
                    updateDownloader.downloadApk(manifest).collect { state ->
                        updateState = state
                    }
                }
            }
        },
        onInstall = {
            val readyState = updateState as? UpdateState.ReadyToInstall
            if (readyState != null) {
                updateInstaller.installApk(readyState.apkFile)
            }
        },
        onDismiss = {
            updateState = UpdateState.Idle
        },
        onRetry = {
            coroutineScope.launch {
                updateState = UpdateState.Checking
                updateState = updateChecker.checkForUpdates()
            }
        }
    )
}
