package com.example.hub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.hub.data.HubPreferences
import com.example.hub.model.GameInfo
import com.example.hub.registry.GameRegistry
import com.example.hub.ui.components.GameArtwork
import com.example.hub.ui.theme.HubColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailsScreen(
    gameId: String,
    hubPreferences: HubPreferences,
    onNavigateBack: () -> Unit,
    onLaunchGame: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val game: GameInfo? = GameRegistry.getGameById(gameId)
    val favorites by hubPreferences.favoritesFlow.collectAsState()
    val isFavorite = favorites.contains(gameId)

    if (game == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(HubColors.Void),
            contentAlignment = Alignment.Center
        ) {
            Text("Game not found", color = HubColors.HighText, style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.game_details),
                        color = HubColors.HighText,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = HubColors.HighText
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { hubPreferences.toggleFavorite(gameId) },
                        modifier = Modifier.testTag("details_fav_btn")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) HubColors.Magenta else HubColors.HighText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HubColors.Void
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HubColors.Void)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                if (game.isAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(HubColors.PlayButtonGradient)
                            .clickable {
                                hubPreferences.recordGamePlayed(game.id)
                                onLaunchGame(game.id)
                            }
                            .testTag("details_play_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.play),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HubColors.LowText),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HubColors.Hairline)
                    ) {
                        Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = HubColors.LowText)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.badge_coming_soon),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        },
        containerColor = HubColors.Void,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HubColors.Void)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Hero Banner Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(game.primaryColor, game.secondaryColor)
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GameArtwork(
                            gameId = game.id,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = game.category.name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = HubColors.Cyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = HubColors.HighText
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (game.isComingSoon) {
                    Badge(
                        containerColor = HubColors.SurfaceMid,
                        contentColor = HubColors.LowText
                    ) {
                        Text(
                            text = stringResource(R.string.badge_coming_soon),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Info Stat Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoPill(
                    title = "Levels",
                    value = game.totalLevelsEstimate,
                    modifier = Modifier.weight(1f)
                )
                InfoPill(
                    title = "Difficulty",
                    value = game.difficultyEstimate,
                    modifier = Modifier.weight(1f)
                )
                InfoPill(
                    title = "Version",
                    value = game.version,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Description Section
            Text(
                text = "About the Game",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = HubColors.HighText
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HubColors.SurfaceLow),
                border = androidx.compose.foundation.BorderStroke(1.dp, HubColors.Hairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = game.longDescription,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp,
                        color = HubColors.LowText
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Developer / Platform Specs
            Text(
                text = stringResource(R.string.information),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = HubColors.HighText
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HubColors.SurfaceLow),
                border = androidx.compose.foundation.BorderStroke(1.dp, HubColors.Hairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SpecRow(label = stringResource(R.string.developer), value = stringResource(R.string.game_hub_team))
                    HorizontalDivider(color = HubColors.Hairline)
                    SpecRow(label = stringResource(R.string.game_version), value = game.version)
                    HorizontalDivider(color = HubColors.Hairline)
                    SpecRow(label = "Architecture", value = "Isolated Clean Module")
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun InfoPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = HubColors.SurfaceMid),
        border = androidx.compose.foundation.BorderStroke(1.dp, HubColors.Hairline),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(color = HubColors.LowText)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = HubColors.HighText
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SpecRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = HubColors.LowText)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = HubColors.HighText
            )
        )
    }
}
