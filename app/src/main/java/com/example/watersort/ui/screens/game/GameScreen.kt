package com.example.watersort.ui.screens.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import android.app.Activity
import com.example.ads.AdManager
import com.example.ads.ZubaLubaRewardedButton
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.watersort.core.economy.EconomyConfig
import com.example.watersort.core.model.LiquidColor
import com.example.watersort.core.model.ThemeConfig
import com.example.watersort.core.model.WorldConfig
import com.example.watersort.ui.components.AmbientEnvironment
import com.example.watersort.ui.components.BottleView
import com.example.watersort.ui.components.GameButtonColor
import com.example.watersort.ui.components.GameCoinPill
import com.example.watersort.ui.components.LiquidPourStream
import com.example.watersort.ui.components.TactileGameButton
import com.example.watersort.ui.components.TactileIconButton
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameScreen(
    levelId: Int,
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    onNavigateNextLevel: (Int) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val theme = remember(uiState.themeId) { ThemeConfig.getTheme(uiState.themeId) }
    val worldInfo = remember(levelId) { WorldConfig.forLevel(levelId) }

    // Bottle position coordinates for dynamic liquid pour stream
    val bottlePositions = remember { mutableStateMapOf<Int, Offset>() }

    LaunchedEffect(levelId) {
        viewModel.loadLevel(levelId)
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissUserMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = theme.backgroundColors
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Ambient particles matching current world environment
        AmbientEnvironment(envType = worldInfo.envType)

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF38BDF8))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Game Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .testTag("back_button")
                            .background(Color(0x331E293B), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    // Level & World Badge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0x440284C7), Color(0x440F172A))
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .border(1.dp, Color(0x4438BDF8), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${worldInfo.iconEmoji} ${worldInfo.name.uppercase()}",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = stringResource(R.string.level_title, levelId),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                        uiState.gameState?.let { state ->
                            Text(
                                text = stringResource(R.string.moves, state.movesCount),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFCBD5E1),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    // Coins Pill
                    GameCoinPill(coins = uiState.coins)
                }

                // Middle: Bottles Area
                val bottles = uiState.gameState?.bottles ?: emptyList()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(28.dp),
                        maxItemsInEachRow = if (bottles.size > 8) 6 else 4
                    ) {
                        bottles.forEachIndexed { index, bottle ->
                            val isSelected = uiState.selectedBottleIndex == index
                            val isHighlighted = uiState.hintMove?.toBottleIndex == index
                            val isTilting = uiState.tiltingBottleIndex == index
                            val isShaking = uiState.shakingBottleIndex == index
                            val tilt = if (isTilting) uiState.tiltAngle else 0f

                            BottleView(
                                bottle = bottle,
                                isSelected = isSelected,
                                isHighlighted = isHighlighted,
                                isShaking = isShaking,
                                colorBlindMode = uiState.colorBlindMode,
                                skinId = uiState.skinId,
                                tiltAngle = tilt,
                                width = if (bottles.size > 8) 48.dp else 56.dp,
                                height = if (bottles.size > 8) 150.dp else 172.dp,
                                onBottleClick = { viewModel.onBottleClicked(index) },
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .onGloballyPositioned { coordinates ->
                                        val bounds = coordinates.boundsInWindow()
                                        // Store center mouth offset of this bottle
                                        bottlePositions[index] = Offset(
                                            x = bounds.left + (bounds.width / 2f),
                                            y = bounds.top + 16f
                                        )
                                    }
                            )
                        }
                    }
                }

                // Bottom Tactile Game Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TactileIconButton(
                        icon = Icons.Default.Undo,
                        label = stringResource(R.string.undo),
                        badgeText = "${EconomyConfig.COST_UNDO}🪙",
                        color = GameButtonColor.DARK,
                        enabled = viewModel.canUndo(),
                        onClick = { viewModel.onUndoClicked() },
                        testTag = "undo_button"
                    )

                    TactileIconButton(
                        icon = Icons.Default.Lightbulb,
                        label = stringResource(R.string.hint),
                        badgeText = "${EconomyConfig.COST_HINT}🪙",
                        color = GameButtonColor.GOLD,
                        enabled = true,
                        onClick = { viewModel.onHintClicked() },
                        testTag = "hint_button"
                    )

                    TactileIconButton(
                        icon = Icons.Default.Add,
                        label = stringResource(R.string.extra_bottle),
                        badgeText = "${EconomyConfig.COST_EXTRA_BOTTLE}🪙",
                        color = GameButtonColor.BLUE,
                        enabled = uiState.gameState?.hasExtraBottle == false,
                        onClick = { viewModel.onExtraBottleClicked() },
                        testTag = "extra_bottle_button"
                    )

                    TactileIconButton(
                        icon = Icons.Default.Refresh,
                        label = stringResource(R.string.restart),
                        color = GameButtonColor.RED,
                        enabled = true,
                        onClick = { viewModel.onRestartClicked() },
                        testTag = "restart_button"
                    )
                }
            }
        }

        // Active Liquid Pour Stream Overlay
        val activePourColor = uiState.pouringColor
        if (uiState.isPouringActive && uiState.pouringSourceIndex != null && uiState.pouringTargetIndex != null && activePourColor != null) {
            val from = bottlePositions[uiState.pouringSourceIndex]
            val to = bottlePositions[uiState.pouringTargetIndex]
            if (from != null && to != null) {
                LiquidPourStream(
                    fromOffset = from,
                    toOffset = to,
                    color = activePourColor
                )
            }
        }

        // Restart Dialog
        if (uiState.showRestartDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissRestart() },
                title = { Text(stringResource(R.string.restart_confirm_title), color = Color.White) },
                text = { Text(stringResource(R.string.restart_confirm_desc), color = Color(0xFFCBD5E1)) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.dismissRestart()
                            AdManager.recordGameLoss("watersort", activity) {
                                viewModel.confirmRestart()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                    ) {
                        Text(stringResource(R.string.yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissRestart() }) {
                        Text(stringResource(R.string.no), color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E293B)
            )
        }

        // Stuck / Deadlock Dialog
        if (uiState.showStuckDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissStuck() },
                title = { Text(stringResource(R.string.stuck_title), color = Color(0xFFFBBF24)) },
                text = { Text(stringResource(R.string.stuck_desc), color = Color(0xFFCBD5E1)) },
                confirmButton = {
                    Button(onClick = {
                        viewModel.dismissStuck()
                        AdManager.recordGameLoss("watersort", activity) {
                            viewModel.confirmRestart()
                        }
                    }) {
                        Text(stringResource(R.string.restart))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissStuck() }) {
                        Text(stringResource(R.string.got_it), color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E293B)
            )
        }

        // Win Dialog & Studio Victory Celebration
        AnimatedVisibility(
            visible = uiState.showWinDialog,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut()
        ) {
            VictoryDialog(
                starsEarned = uiState.gameState?.starsEarned ?: 3,
                coinsEarned = uiState.coinsEarnedOnWin,
                isPerfectRun = uiState.isPerfectRun,
                levelId = levelId,
                onNextLevel = {
                    AdManager.recordGameWin("watersort", activity) {
                        onNavigateNextLevel(levelId + 1)
                    }
                },
                onHome = {
                    AdManager.recordGameWin("watersort", activity) {
                        onNavigateBack()
                    }
                },
                onRewardEarned = {
                    viewModel.onAdRewardEarned(50)
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun VictoryDialog(
    starsEarned: Int,
    coinsEarned: Int,
    isPerfectRun: Boolean,
    levelId: Int,
    onNextLevel: () -> Unit,
    onHome: () -> Unit,
    onRewardEarned: () -> Unit = {}
) {
    val star1Scale = remember { Animatable(0f) }
    val star2Scale = remember { Animatable(0f) }
    val star3Scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(150)
        star1Scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        if (starsEarned >= 2) {
            delay(120)
            star2Scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        if (starsEarned >= 3) {
            delay(120)
            star3Scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD090D16)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0x44FACC15), RoundedCornerShape(26.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Perfect Run Badge
                if (isPerfectRun) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFEAB308), Color(0xFFF59E0B), Color(0xFFD97706))
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "★ PERFECT RUN! ★",
                            color = Color(0xFF451A03),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Text(
                    text = "🏆",
                    fontSize = 44.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.level_complete),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Animated Stars
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (starsEarned >= 1) Color(0xFFFACC15) else Color(0xFF334155),
                        modifier = Modifier
                            .size(46.dp)
                            .scale(star1Scale.value)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (starsEarned >= 2) Color(0xFFFACC15) else Color(0xFF334155),
                        modifier = Modifier
                            .size(56.dp)
                            .scale(star2Scale.value)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (starsEarned >= 3) Color(0xFFFACC15) else Color(0xFF334155),
                        modifier = Modifier
                            .size(46.dp)
                            .scale(star3Scale.value)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reward Callout
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x33FBBF24))
                        .border(1.dp, Color(0x66FBBF24), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "🪙 +$coinsEarned Coins Earned!",
                        color = Color(0xFFFDE047),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Optional Rewarded Ad: Bonus Coins
                ZubaLubaRewardedButton(
                    rewardDescription = "+50 🪙",
                    onRewardEarned = { _, _ -> onRewardEarned() },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Next Level Tactile Button
                TactileGameButton(
                    text = stringResource(R.string.next_level),
                    onClick = onNextLevel,
                    color = GameButtonColor.GREEN,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "next_level_button"
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("home_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8))
                ) {
                    Text(text = stringResource(R.string.home), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
