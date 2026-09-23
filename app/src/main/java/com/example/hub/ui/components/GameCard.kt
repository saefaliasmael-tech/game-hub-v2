package com.example.hub.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.hub.model.GameInfo
import com.example.hub.registry.GameRegistry
import com.example.hub.ui.theme.HubColors

@Composable
fun GameCard(
    game: GameInfo,
    isFavorite: Boolean,
    onGameClick: () -> Unit,
    onPlayClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = if (game.isFeatured) HubColors.SoftViolet.copy(alpha = 0.5f) else HubColors.Hairline,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onGameClick() }
            .testTag("game_card_${game.id}"),
        colors = CardDefaults.cardColors(
            containerColor = HubColors.SurfaceLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Game Avatar + Title/Category + Favorite Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Game Art Thumbnail with dedicated artwork
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(game.primaryColor.copy(alpha = 0.35f), game.secondaryColor)
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (game.id == GameRegistry.WATER_SORT_ID) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_water_sort_icon),
                            contentDescription = game.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        GameArtwork(
                            gameId = game.id,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title, Category & Badges
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = game.name,
                            color = HubColors.HighText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (game.isFeatured) {
                            Badge(
                                containerColor = HubColors.Magenta,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = stringResource(R.string.badge_featured),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        } else if (game.isNew) {
                            Badge(
                                containerColor = HubColors.Lime,
                                contentColor = HubColors.Void
                            ) {
                                Text(
                                    text = stringResource(R.string.badge_new),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = game.category.name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = HubColors.Cyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Text(
                            text = "•",
                            color = HubColors.LowText,
                            fontSize = 12.sp
                        )

                        Text(
                            text = game.difficultyEstimate,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HubColors.LowText
                            )
                        )
                    }
                }

                // Favorite Toggle Button (Accessible minimum 48x48)
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("fav_btn_${game.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) HubColors.Magenta else HubColors.LowText
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Short Description
            Text(
                text = game.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = HubColors.LowText,
                    lineHeight = 20.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: Level Count Pill + Glowing Play Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = HubColors.SurfaceMid,
                    border = androidx.compose.foundation.BorderStroke(1.dp, HubColors.Hairline)
                ) {
                    Text(
                        text = "${game.totalLevelsEstimate} Levels",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = HubColors.SoftViolet
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                if (game.isAvailable) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(HubColors.PlayButtonGradient)
                            .clickable { onPlayClick() }
                            .padding(horizontal = 22.dp, vertical = 9.dp)
                            .defaultMinSize(minHeight = 44.dp)
                            .testTag("play_btn_${game.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.play),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onGameClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = HubColors.LowText
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HubColors.Hairline),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = HubColors.LowText
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.badge_coming_soon),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
