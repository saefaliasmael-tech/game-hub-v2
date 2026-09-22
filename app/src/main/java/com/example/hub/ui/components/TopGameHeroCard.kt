package com.example.hub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.hub.model.GameInfo
import com.example.hub.ui.theme.HubColors

@Composable
fun TopGameHeroCard(
    game: GameInfo,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(260.dp)
            .height(170.dp)
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(HubColors.PrimaryViolet, HubColors.Cyan)
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onPlayClick() }
            .testTag("top_game_hero_${game.id}"),
        colors = CardDefaults.cardColors(containerColor = HubColors.SurfaceMid),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Atmospheric background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                game.secondaryColor.copy(alpha = 0.85f),
                                HubColors.Void.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // Game Artwork Accent in right background
            Box(
                modifier = Modifier
                    .size(115.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 12.dp)
                    .padding(12.dp)
            ) {
                GameArtwork(
                    gameId = game.id,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Content Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: Category tag + Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HubColors.SurfaceHigh.copy(alpha = 0.8f),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, HubColors.Cyan.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = game.category.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HubColors.Cyan,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB703),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "4.9",
                            color = HubColors.HighText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Bottom row: Title + Play button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = game.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = HubColors.HighText,
                                fontSize = 17.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = game.totalLevelsEstimate,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HubColors.LowText
                            ),
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(HubColors.PlayButtonGradient)
                            .clickable { onPlayClick() }
                            .testTag("play_hero_btn_${game.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = stringResource(R.string.play),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
