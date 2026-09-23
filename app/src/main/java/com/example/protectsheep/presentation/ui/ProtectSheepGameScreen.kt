package com.example.protectsheep.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.protectsheep.core.level.ProtectSheepLevelManager
import com.example.protectsheep.core.model.HazardType
import com.example.protectsheep.core.model.SheepGamePhase
import com.example.protectsheep.core.model.SheepTarget
import com.example.protectsheep.core.model.WolfAttacker
import com.example.protectsheep.presentation.ProtectSheepViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectSheepGameScreen(
    viewModel: ProtectSheepViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val soundEnabled by viewModel.soundEnabledFlow.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopSimulation()
        }
    }

    val inkRemainingRatio = remember(state.totalInkUsed, state.maxInkLength) {
        (1f - (state.totalInkUsed / state.maxInkLength)).coerceIn(0f, 1f)
    }
    val animatedInk by animateFloatAsState(targetValue = inkRemainingRatio, label = "ink")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Level ${state.levelNumber}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (state.phase == SheepGamePhase.SURVIVING)
                                "Survive: ${String.format("%.1f", state.timeRemainingSeconds)}s"
                            else
                                "Ink: ${(inkRemainingRatio * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = if (state.phase == SheepGamePhase.SURVIVING) Color(0xFFFBBF24) else Color(0xFF34D399)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("protectsheep_game_back")
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
                        modifier = Modifier.testTag("protectsheep_game_sound")
                    ) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Sound",
                            tint = Color(0xFF10B981)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.restartLevel() },
                        modifier = Modifier.testTag("protectsheep_game_restart")
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
        containerColor = Color(0xFF091410)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Ink Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { animatedInk },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = when {
                            animatedInk > 0.55f -> Color(0xFF10B981)
                            animatedInk > 0.25f -> Color(0xFFFBBF24)
                            else -> Color(0xFFEF4444)
                        },
                        trackColor = Color(0xFF1E293B)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.width(1.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (animatedInk > 0.25f) Color(0xFFFBBF24) else Color(0xFF475569),
                            modifier = Modifier.size(12.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (animatedInk > 0.55f) Color(0xFFFBBF24) else Color(0xFF475569),
                            modifier = Modifier.size(12.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (animatedInk > 0.85f) Color(0xFFFBBF24) else Color(0xFF475569),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Main Game Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1B382B))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("protectsheep_canvas")
                            .pointerInput(state.phase) {
                                if (state.phase == SheepGamePhase.DRAWING_BARRIER) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val scaleX = size.width / ProtectSheepLevelManager.VIRTUAL_WIDTH
                                            val scaleY = size.height / ProtectSheepLevelManager.VIRTUAL_HEIGHT
                                            viewModel.onTouchDown(Offset(offset.x / scaleX, offset.y / scaleY))
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val scaleX = size.width / ProtectSheepLevelManager.VIRTUAL_WIDTH
                                            val scaleY = size.height / ProtectSheepLevelManager.VIRTUAL_HEIGHT
                                            viewModel.onTouchMove(Offset(change.position.x / scaleX, change.position.y / scaleY))
                                        },
                                        onDragEnd = { viewModel.onTouchUp() },
                                        onDragCancel = { viewModel.onTouchUp() }
                                    )
                                }
                            }
                    ) {
                        val scaleX = size.width / ProtectSheepLevelManager.VIRTUAL_WIDTH
                        val scaleY = size.height / ProtectSheepLevelManager.VIRTUAL_HEIGHT

                        // 1. Meadow Pasture Background
                        drawMeadowBackground()

                        // 2. Sheep
                        for (sheep in state.sheepList) {
                            drawSheep(sheep, state.phase == SheepGamePhase.SURVIVING, scaleX, scaleY)
                        }

                        // 3. Wolves / Hazards
                        for (wolf in state.wolves) {
                            drawWolfOrHazard(wolf, scaleX, scaleY)
                        }

                        // 4. Drawn Barriers
                        for (stroke in state.strokes) {
                            drawBarrier(stroke.points, scaleX, scaleY, Color(0xFFD97706))
                        }

                        // Current active stroke
                        if (state.currentStrokePoints.size >= 2) {
                            drawBarrier(state.currentStrokePoints, scaleX, scaleY, Color(0xFFFBBF24))
                        }
                    }
                }

                // Bottom Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.undoLastStroke() },
                        enabled = state.phase == SheepGamePhase.DRAWING_BARRIER && state.strokes.isNotEmpty(),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("protectsheep_undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo",
                            tint = if (state.strokes.isNotEmpty()) Color(0xFFE2E8F0) else Color(0xFF475569)
                        )
                    }

                    Button(
                        onClick = {
                            if (state.phase == SheepGamePhase.DRAWING_BARRIER) {
                                viewModel.startSurviving()
                            }
                        },
                        enabled = state.phase == SheepGamePhase.DRAWING_BARRIER,
                        modifier = Modifier
                            .height(50.dp)
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                            .testTag("protectsheep_start_defense_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "START DEFENSE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = { viewModel.restartLevel() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("protectsheep_restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            // Outcome: WON
            AnimatedVisibility(
                visible = state.phase == SheepGamePhase.WON,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                SheepOutcomeDialog(
                    isVictory = true,
                    stars = state.stars,
                    title = "Sheep Protected!",
                    subtitle = "The flock survived the attack safe and sound!",
                    primaryText = "Next Level",
                    onPrimary = { viewModel.nextLevel() },
                    secondaryText = "Replay",
                    onSecondary = { viewModel.restartLevel() }
                )
            }

            // Outcome: LOST
            AnimatedVisibility(
                visible = state.phase == SheepGamePhase.LOST,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                SheepOutcomeDialog(
                    isVictory = false,
                    stars = 0,
                    title = "The Wolves Breached!",
                    subtitle = "Close all gaps completely when drawing the protective fence.",
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
private fun SheepOutcomeDialog(
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
            .testTag(if (isVictory) "protectsheep_win_dialog" else "protectsheep_loss_dialog"),
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
                color = if (isVictory) Color(0xFF34D399) else Color(0xFFF87171)
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
                    .testTag("protectsheep_dialog_primary"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVictory) Color(0xFF059669) else Color(0xFFDC2626)
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
                    .testTag("protectsheep_dialog_secondary"),
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

private fun DrawScope.drawMeadowBackground() {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF143320), Color(0xFF1B432B))
        )
    )
}

private fun DrawScope.drawSheep(
    sheep: SheepTarget,
    isPanicking: Boolean,
    scaleX: Float,
    scaleY: Float
) {
    val cx = sheep.x * scaleX
    val cy = sheep.y * scaleY
    val r = sheep.radius * scaleX

    if (!sheep.isAlive) {
        // Sheep defeated
        drawCircle(color = Color(0xFFEF4444).copy(alpha = 0.5f), radius = r, center = Offset(cx, cy))
        return
    }

    // Fluffy cloud wool body
    val offsets = listOf(
        Offset(-r * 0.4f, -r * 0.3f),
        Offset(r * 0.4f, -r * 0.3f),
        Offset(-r * 0.5f, r * 0.2f),
        Offset(r * 0.5f, r * 0.2f),
        Offset(0f, -r * 0.4f),
        Offset(0f, r * 0.3f),
        Offset(0f, 0f)
    )

    for (off in offsets) {
        drawCircle(
            color = Color(0xFFF1F5F9),
            radius = r * 0.55f,
            center = Offset(cx + off.x, cy + off.y)
        )
    }

    // Black sheep face
    drawOval(
        color = Color(0xFF1E293B),
        topLeft = Offset(cx - r * 0.35f, cy - r * 0.1f),
        size = androidx.compose.ui.geometry.Size(r * 0.7f, r * 0.6f)
    )

    // Pink ears
    drawCircle(color = Color(0xFFFDA4AF), radius = r * 0.15f, center = Offset(cx - r * 0.4f, cy - r * 0.1f))
    drawCircle(color = Color(0xFFFDA4AF), radius = r * 0.15f, center = Offset(cx + r * 0.4f, cy - r * 0.1f))

    // Eyes
    val eyeRadius = if (isPanicking) r * 0.14f else r * 0.1f
    drawCircle(color = Color.White, radius = eyeRadius, center = Offset(cx - r * 0.15f, cy + r * 0.1f))
    drawCircle(color = Color.White, radius = eyeRadius, center = Offset(cx + r * 0.15f, cy + r * 0.1f))
    drawCircle(color = Color.Black, radius = eyeRadius * 0.5f, center = Offset(cx - r * 0.15f, cy + r * 0.1f))
    drawCircle(color = Color.Black, radius = eyeRadius * 0.5f, center = Offset(cx + r * 0.15f, cy + r * 0.1f))
}

private fun DrawScope.drawWolfOrHazard(wolf: WolfAttacker, scaleX: Float, scaleY: Float) {
    val cx = wolf.x * scaleX
    val cy = wolf.y * scaleY
    val r = wolf.radius * scaleX

    when (wolf.type) {
        HazardType.FALLING_BOULDER -> {
            drawCircle(color = Color(0xFF475569), radius = r, center = Offset(cx, cy))
            drawCircle(color = Color(0xFF64748B), radius = r * 0.8f, center = Offset(cx - 2f, cy - 2f))
            drawCircle(color = Color(0xFF334155), radius = r * 0.3f, center = Offset(cx + 3f, cy + 3f))
        }
        HazardType.BEE_SWARM -> {
            // Yellow body with black stripes
            drawOval(
                color = Color(0xFFFBBF24),
                topLeft = Offset(cx - r, cy - r * 0.7f),
                size = androidx.compose.ui.geometry.Size(r * 2f, r * 1.4f)
            )
            drawLine(
                color = Color(0xFF1E293B),
                start = Offset(cx - 2f, cy - r * 0.6f),
                end = Offset(cx - 2f, cy + r * 0.6f),
                strokeWidth = 3f
            )
            drawLine(
                color = Color(0xFF1E293B),
                start = Offset(cx + 3f, cy - r * 0.6f),
                end = Offset(cx + 3f, cy + r * 0.6f),
                strokeWidth = 3f
            )
            // Translucent wings
            drawCircle(color = Color.White.copy(alpha = 0.7f), radius = r * 0.5f, center = Offset(cx, cy - r * 0.8f))
        }
        else -> {
            // Wolf
            drawCircle(color = Color(0xFF334155), radius = r, center = Offset(cx, cy))
            // Pointed ears
            val leftEar = Path().apply {
                moveTo(cx - r * 0.8f, cy - r * 0.2f)
                lineTo(cx - r * 0.6f, cy - r * 1.2f)
                lineTo(cx - r * 0.2f, cy - r * 0.6f)
            }
            drawPath(leftEar, color = Color(0xFF334155))

            val rightEar = Path().apply {
                moveTo(cx + r * 0.2f, cy - r * 0.6f)
                lineTo(cx + r * 0.6f, cy - r * 1.2f)
                lineTo(cx + r * 0.8f, cy - r * 0.2f)
            }
            drawPath(rightEar, color = Color(0xFF334155))

            // Glowing yellow eyes
            drawCircle(color = Color(0xFFFBBF24), radius = r * 0.16f, center = Offset(cx - r * 0.35f, cy - r * 0.1f))
            drawCircle(color = Color(0xFFFBBF24), radius = r * 0.16f, center = Offset(cx + r * 0.35f, cy - r * 0.1f))
            // White fangs
            val fangs = Path().apply {
                moveTo(cx - r * 0.2f, cy + r * 0.3f)
                lineTo(cx, cy + r * 0.6f)
                lineTo(cx + r * 0.2f, cy + r * 0.3f)
            }
            drawPath(fangs, color = Color.White)
        }
    }
}

private fun DrawScope.drawBarrier(
    points: List<Offset>,
    scaleX: Float,
    scaleY: Float,
    color: Color
) {
    if (points.size < 2) return

    val path = Path().apply {
        moveTo(points[0].x * scaleX, points[0].y * scaleY)
        for (i in 1 until points.size) {
            lineTo(points[i].x * scaleX, points[i].y * scaleY)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 12f * scaleX,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Inner highlight for wooden beam look
    drawPath(
        path = path,
        color = Color(0xFFFDE68A).copy(alpha = 0.4f),
        style = Stroke(
            width = 4f * scaleX,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
