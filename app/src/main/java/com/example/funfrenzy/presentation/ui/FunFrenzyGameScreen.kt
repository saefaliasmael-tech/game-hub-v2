package com.example.funfrenzy.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.funfrenzy.core.level.FunFrenzyLevelManager
import com.example.funfrenzy.core.model.ExitPortal
import com.example.funfrenzy.core.model.FrenzyGamePhase
import com.example.funfrenzy.core.model.FrenzyHazard
import com.example.funfrenzy.core.model.FrenzyHazardType
import com.example.funfrenzy.core.model.RescueBuddy
import com.example.funfrenzy.core.model.RescueRope
import com.example.funfrenzy.core.model.RopeAnchor
import com.example.funfrenzy.presentation.FunFrenzyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunFrenzyGameScreen(
    viewModel: FunFrenzyViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val soundEnabled by viewModel.soundEnabledFlow.collectAsState()

    var swipeTrail by remember { mutableStateOf<List<Offset>>(emptyList()) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopSimulation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Rescue Mission ${state.levelNumber}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Time: ${String.format("%.1f", state.timeRemainingSeconds)}s",
                            fontSize = 12.sp,
                            color = if (state.timeRemainingSeconds < 5f) Color(0xFFEF4444) else Color(0xFFC084FC)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("funfrenzy_game_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleSound() },
                        modifier = Modifier.testTag("funfrenzy_game_sound")
                    ) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Sound",
                            tint = Color(0xFFC084FC)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.restartLevel() },
                        modifier = Modifier.testTag("funfrenzy_game_restart")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart",
                            tint = Color(0xFFE2E8F0)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color(0xFF0D0B14)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Main Game Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF13111C))
                ) {
                    val anchorsMap = remember(state.anchors) {
                        state.anchors.associateBy { it.id }
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("funfrenzy_canvas")
                            .pointerInput(state.phase) {
                                if (state.phase == FrenzyGamePhase.PLAYING) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            swipeTrail = listOf(offset)
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val currentTrail = swipeTrail
                                            if (currentTrail.isNotEmpty()) {
                                                val prev = currentTrail.last()
                                                val curr = change.position
                                                val scaleX = size.width / FunFrenzyLevelManager.VIRTUAL_WIDTH
                                                val scaleY = size.height / FunFrenzyLevelManager.VIRTUAL_HEIGHT

                                                val p1 = Offset(prev.x / scaleX, prev.y / scaleY)
                                                val p2 = Offset(curr.x / scaleX, curr.y / scaleY)
                                                viewModel.onSwipeSlice(p1, p2)
                                            }
                                            swipeTrail = (currentTrail + change.position).takeLast(10)
                                        },
                                        onDragEnd = { swipeTrail = emptyList() },
                                        onDragCancel = { swipeTrail = emptyList() }
                                    )
                                }
                            }
                    ) {
                        val scaleX = size.width / FunFrenzyLevelManager.VIRTUAL_WIDTH
                        val scaleY = size.height / FunFrenzyLevelManager.VIRTUAL_HEIGHT

                        // 1. Atmospheric Cavern Background
                        drawCavernBackground()

                        // 2. Anchors
                        for (anchor in state.anchors) {
                            drawAnchor(anchor, scaleX, scaleY)
                        }

                        // 3. Ropes
                        for (rope in state.ropes) {
                            val anchor = anchorsMap[rope.anchorId]
                            if (anchor != null) {
                                val isHinted = state.hintRopeId == rope.id
                                drawRope(anchor, state.buddy, rope, isHinted, scaleX, scaleY)
                            }
                        }

                        // 4. Hazards
                        for (hazard in state.hazards) {
                            drawHazard(hazard, scaleX, scaleY)
                        }

                        // 5. Exit Portal / Mattress
                        drawExitPortal(state.portal, scaleX, scaleY)

                        // 6. Buddy
                        drawBuddy(state.buddy, scaleX, scaleY)

                        // 7. Swipe slash trail
                        if (swipeTrail.size >= 2) {
                            for (i in 0 until swipeTrail.size - 1) {
                                val alpha = (i.toFloat() / swipeTrail.size).coerceIn(0.2f, 0.9f)
                                drawLine(
                                    color = Color(0xFFE879F9).copy(alpha = alpha),
                                    start = swipeTrail[i],
                                    end = swipeTrail[i + 1],
                                    strokeWidth = 5f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }

                // Powerups Bottom Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.useHint() },
                        enabled = state.phase == FrenzyGamePhase.PLAYING && state.hintsRemaining > 0,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7E22CE)
                        ),
                        modifier = Modifier.testTag("funfrenzy_hint_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Hint (${state.hintsRemaining})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { viewModel.useExtraTime() },
                        enabled = state.phase == FrenzyGamePhase.PLAYING && !state.extraTimeUsed,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669)
                        ),
                        modifier = Modifier.testTag("funfrenzy_extra_time_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+10s Time",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Outcome: WON
            AnimatedVisibility(
                visible = state.phase == FrenzyGamePhase.WON,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                FrenzyOutcomeDialog(
                    isVictory = true,
                    stars = state.stars,
                    title = "Buddy Rescued!",
                    subtitle = "Safe landing! Swift thinking and precise cuts.",
                    primaryText = "Next Level",
                    onPrimary = { viewModel.nextLevel() },
                    secondaryText = "Replay",
                    onSecondary = { viewModel.restartLevel() }
                )
            }

            // Outcome: LOST
            AnimatedVisibility(
                visible = state.phase == FrenzyGamePhase.LOST,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                FrenzyOutcomeDialog(
                    isVictory = false,
                    stars = 0,
                    title = "Mission Failed!",
                    subtitle = "Buddy hit a hazard or time ran out. Cut ropes in the right order!",
                    primaryText = "Try Again",
                    onPrimary = { viewModel.restartLevel() },
                    secondaryText = "Back to Menu",
                    onSecondary = onNavigateBack
                )
            }
        }
    }
}

@Composable
private fun FrenzyOutcomeDialog(
    isVictory: Boolean,
    stars: Int,
    title: String,
    subtitle: String,
    primaryText: String,
    onPrimary: () -> Unit,
    secondaryText: String,
    onSecondary: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .testTag(if (isVictory) "funfrenzy_win_dialog" else "funfrenzy_loss_dialog"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        ),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (isVictory) Color(0xFFC084FC) else Color(0xFFEF4444)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (isVictory) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < stars) Color(0xFFFBBF24) else Color(0xFF475569),
                            modifier = Modifier
                                .size(36.dp)
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("funfrenzy_dialog_primary"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVictory) Color(0xFF9333EA) else Color(0xFFDC2626)
                )
            ) {
                Text(
                    text = primaryText,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("funfrenzy_dialog_secondary"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = secondaryText,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

private fun DrawScope.drawCavernBackground() {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF0F0E1A), Color(0xFF1B192A))
        )
    )
}

private fun DrawScope.drawAnchor(anchor: RopeAnchor, scaleX: Float, scaleY: Float) {
    val cx = anchor.x * scaleX
    val cy = anchor.y * scaleY

    // Swivel ring
    drawCircle(color = Color(0xFF475569), radius = 10f * scaleX, center = Offset(cx, cy))
    drawCircle(color = Color(0xFF0F172A), radius = 5f * scaleX, center = Offset(cx, cy))
}

private fun DrawScope.drawRope(
    anchor: RopeAnchor,
    buddy: RescueBuddy,
    rope: RescueRope,
    isHinted: Boolean,
    scaleX: Float,
    scaleY: Float
) {
    if (rope.isCut) return

    val start = Offset(anchor.x * scaleX, anchor.y * scaleY)
    val end = Offset(buddy.x * scaleX, buddy.y * scaleY)

    val color = if (isHinted) Color(0xFF34D399) else Color(0xFFD97706)
    val width = if (isHinted) 6f * scaleX else 4f * scaleX

    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = width,
        cap = StrokeCap.Round
    )

    if (isHinted) {
        // Glowing hint pulse
        drawLine(
            color = Color.White.copy(alpha = 0.6f),
            start = start,
            end = end,
            strokeWidth = 2f * scaleX,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawBuddy(buddy: RescueBuddy, scaleX: Float, scaleY: Float) {
    val cx = buddy.x * scaleX
    val cy = buddy.y * scaleY
    val r = buddy.radius * scaleX

    if (buddy.isDead) {
        drawCircle(color = Color(0xFFEF4444).copy(alpha = 0.6f), radius = r, center = Offset(cx, cy))
        return
    }

    // Body (cute blue/purple round buddy)
    drawCircle(color = Color(0xFF8B5CF6), radius = r, center = Offset(cx, cy))

    // Expressive Eyes
    val eyeOffset = 6f * scaleX
    val eyeY = cy - 4f * scaleY
    drawCircle(color = Color.White, radius = 6f * scaleX, center = Offset(cx - eyeOffset, eyeY))
    drawCircle(color = Color.White, radius = 6f * scaleX, center = Offset(cx + eyeOffset, eyeY))
    drawCircle(color = Color.Black, radius = 3f * scaleX, center = Offset(cx - eyeOffset, eyeY))
    drawCircle(color = Color.Black, radius = 3f * scaleX, center = Offset(cx + eyeOffset, eyeY))

    // Cheerful or shocked mouth
    drawCircle(color = Color(0xFF6D28D9), radius = 3f * scaleX, center = Offset(cx, cy + 6f * scaleY))
}

private fun DrawScope.drawHazard(hazard: FrenzyHazard, scaleX: Float, scaleY: Float) {
    val b = hazard.bounds
    val left = b.left * scaleX
    val top = b.top * scaleY
    val right = b.right * scaleX
    val bottom = b.bottom * scaleY
    val w = right - left
    val h = bottom - top

    when (hazard.type) {
        FrenzyHazardType.SPIKES -> {
            val spikeCount = (w / 18f).toInt().coerceAtLeast(2)
            val spikeWidth = w / spikeCount

            val path = Path().apply {
                moveTo(left, bottom)
                for (i in 0 until spikeCount) {
                    val sx = left + i * spikeWidth
                    lineTo(sx + spikeWidth * 0.5f, top)
                    lineTo(sx + spikeWidth, bottom)
                }
                close()
            }
            drawPath(path, color = Color(0xFFEF4444))
        }
        FrenzyHazardType.SAWBLADE -> {
            val cx = (left + right) / 2f
            val cy = (top + bottom) / 2f
            val r = (w.coerceAtMost(h)) / 2f

            drawCircle(color = Color(0xFF94A3B8), radius = r, center = Offset(cx, cy))
            drawCircle(color = Color(0xFFE2E8F0), radius = r * 0.7f, center = Offset(cx, cy))
            drawCircle(color = Color(0xFF1E293B), radius = r * 0.25f, center = Offset(cx, cy))
        }
        FrenzyHazardType.LAVA_PIT -> {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFEA580C), Color(0xFFDC2626))
                ),
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(w, h)
            )
        }
    }
}

private fun DrawScope.drawExitPortal(portal: ExitPortal, scaleX: Float, scaleY: Float) {
    val b = portal.bounds
    val left = b.left * scaleX
    val top = b.top * scaleY
    val w = (b.right - b.left) * scaleX
    val h = (b.bottom - b.top) * scaleY

    // Safety Mattress
    drawRoundRect(
        color = Color(0xFF10B981),
        topLeft = Offset(left, top),
        size = androidx.compose.ui.geometry.Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
    )

    // Inner bright zone
    drawRoundRect(
        color = Color(0xFF34D399),
        topLeft = Offset(left + 6f, top + 4f),
        size = androidx.compose.ui.geometry.Size(w - 12f, h - 8f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
}
