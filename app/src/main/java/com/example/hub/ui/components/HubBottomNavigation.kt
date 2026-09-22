package com.example.hub.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.hub.ui.theme.HubColors

enum class HubTab {
    HOME,
    GAMES,
    FAVORITES,
    SETTINGS
}

@Composable
fun HubBottomNavigation(
    currentTab: HubTab,
    onTabSelected: (HubTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(HubColors.BottomNavGradient)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(HubColors.SurfaceLow)
                .border(
                    width = 1.dp,
                    color = HubColors.Hairline,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HubTabItem(
                tab = HubTab.HOME,
                isSelected = currentTab == HubTab.HOME,
                label = stringResource(R.string.tab_home),
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                testTag = "hub_tab_home",
                onClick = { onTabSelected(HubTab.HOME) }
            )

            HubTabItem(
                tab = HubTab.GAMES,
                isSelected = currentTab == HubTab.GAMES,
                label = stringResource(R.string.tab_games),
                selectedIcon = Icons.Filled.GridView,
                unselectedIcon = Icons.Outlined.GridView,
                testTag = "hub_tab_games",
                onClick = { onTabSelected(HubTab.GAMES) }
            )

            HubTabItem(
                tab = HubTab.FAVORITES,
                isSelected = currentTab == HubTab.FAVORITES,
                label = stringResource(R.string.tab_favorites),
                selectedIcon = Icons.Filled.Favorite,
                unselectedIcon = Icons.Outlined.FavoriteBorder,
                testTag = "hub_tab_favorites",
                onClick = { onTabSelected(HubTab.FAVORITES) }
            )

            HubTabItem(
                tab = HubTab.SETTINGS,
                isSelected = currentTab == HubTab.SETTINGS,
                label = stringResource(R.string.tab_settings),
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                testTag = "hub_tab_settings",
                onClick = { onTabSelected(HubTab.SETTINGS) }
            )
        }
    }
}

@Composable
private fun HubTabItem(
    tab: HubTab,
    isSelected: Boolean,
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) HubColors.HighText else HubColors.LowText,
        animationSpec = tween(durationMillis = 200),
        label = "tabIconTint"
    )

    val backgroundModifier = if (isSelected) {
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(HubColors.PrimaryViolet.copy(alpha = 0.85f))
            .border(1.dp, HubColors.SoftViolet.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
    } else {
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
    }

    Box(
        modifier = Modifier
            .then(backgroundModifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = HubColors.PrimaryViolet),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .defaultMinSize(minHeight = 48.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )

            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = HubColors.HighText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}
