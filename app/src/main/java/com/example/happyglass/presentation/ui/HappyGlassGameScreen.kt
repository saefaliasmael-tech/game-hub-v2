package com.example.happyglass.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.happyglass.core.level.HappyGlassLevelManager
import com.example.happyglass.core.model.GamePhase
import com.example.happyglass.core.model.GlassContainer
import com.example.happyglass.core.model.HappyObstacle
import com.example.happyglass.core.model.ObstacleType
import com.example.happyglass.presentation.HappyGlassViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HappyGlassGameScreen(
    viewModel: HappyGlassViewModel,
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

    val animatedInk by animateFloatAsState(targetValue = inkRemainingRatio, label = "inkRatio")

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
                            text = "Ink: ${(inkRemainingRatio * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("happyglass_game_back")
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
                        modifier = Modifier.testTag("happyglass_game_sound")
                    ) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Sound",
                            tint = Color(0xFF38BDF8)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.restartLevel() },
                        modifier = Modifier.testTag("happyglass_game_restart_top")
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
        containerColor = Color(0xFF090D16)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Ink Bar with 3 Star Indicators
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

                    // Star markers over ink bar
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
                        .background(Color(0xFF0F172A))
                ) {
                    val config = remember(state.levelNumber) {
                        HappyGlassLevelManager.getLevel(state.levelNumber)
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("happyglass_canvas")
                            .pointerInput(state.phase) {
                                if (state.phase == GamePhase.DRAWING) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val scaleX = size.width / HappyGlassLevelManager.VIRTUAL_WIDTH
                                            val scaleY = size.height / HappyGlassLevelManager.VIRTUAL_HEIGHT
                                            viewModel.onTouchDown(Offset(offset.x / scaleX, offset.y / scaleY))
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val scaleX = size.width / HappyGlassLevelManager.VIRTUAL_WIDTH
                                            val scaleY = size.height / HappyGlassLevelManager.VIRTUAL_HEIGHT
                                            viewModel.onTouchMove(Offset(change.position.x / scaleX, change.position.y / scaleY))
                                        },
                                        onDragEnd = {
                                            viewModel.onTouchUp()
                                        },
                                        onDragCancel = {
                                            viewModel.onTouchUp()
                                        }
                                    )
                                }
                            }
                    ) {
                        val scaleX = size.width / HappyGlassLevelManager.VIRTUAL_WIDTH
                        val scaleY = size.height / HappyGlassLevelManager.VIRTUAL_HEIGHT

                        // 1. Subtle background grid pattern
                        drawBackgroundGrid()

                        // 2. Obstacles
                        for (obs in config.obstacles) {
                            drawObstacle(obs, scaleX, scaleY)
                        }

                        // 3. Faucet
                        drawFaucet(config.faucet.x * scaleX, config.faucet.y * scaleY, scaleX)

                        // 4. Glass Container & Face
                        drawGlassCup(
                            glass = config.glass,
                            scaleX = scaleX,
                            scaleY = scaleY,
                            waterRatio = state.waterLevelRatio,
                            isSad = state.isSadGlass
                        )

                        // 5. Drawn Strokes
                        for (stroke in state.strokes) {
                            drawStrokeLine(stroke.points, scaleX, scaleY, Color(0xFF38BDF8))
                        }

                        // Current in-progress stroke
                        if (state.currentStrokePoints.size >= 2) {
                            drawStrokeLine(state.currentStrokePoints, scaleX, scaleY, Color(0xFF67E8F9))
                        }

                        // 6. Water Drops
                        for (drop in state.drops) {
                            if (!drop.isLost) {
                                val dropColor = if (drop.inGlass) Color(0xFF38BDF8) else Color(0xFF60A5FA)
                                drawCircle(
                                    color = dropColor,
                                    radius = drop.radius * scaleX,
                                    center = Offset(drop.x * scaleX, drop.y * scaleY)
                                )
                                // Specular highlight
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.6f),
                                    radius = drop.radius * 0.35f * scaleX,
                                    center = Offset((drop.x - 1.5f) * scaleX, (drop.y - 1.5f) * scaleY)
                                )
                            }
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
                        enabled = state.phase == GamePhase.DRAWING && state.strokes.isNotEmpty(),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("happyglass_undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo Stroke",
                            tint = if (state.strokes.isNotEmpty()) Color(0xFFE2E8F0) else Color(0xFF475569)
                        )
                    }

                    Button(
                        onClick = {
                            if (state.phase == GamePhase.DRAWING) {
                                viewModel.startPouring()
                            }
                        },
                        enabled = state.phase == GamePhase.DRAWING,
                        modifier = Modifier
                            .height(50.dp)
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                            .testTag("happyglass_pour_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "POUR WATER",
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
                            .testTag("happyglass_restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Level",
                            tint = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            // Outcome Overlays: WON
            AnimatedVisibility(
                visible = state.phase == GamePhase.WON,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                OutcomeDialog(
                    isVictory = true,
                    stars = state.stars,
                    title = "Level Cleared!",
                    subtitle = "The glass is full and smiling happily!",
                    primaryActionText = "Next Level",
                    onPrimaryAction = { viewModel.nextLevel() },
                    secondaryActionText = "Replay",
                    onSecondaryAction = { viewModel.restartLevel() }
                )
            }

            // Outcome Overlays: LOST
            AnimatedVisibility(
                visible = state.phase == GamePhase.LOST,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                OutcomeDialog(
                    isVictory = false,
                    stars = 0,
                    title = "Water Spilled!",
                    subtitle = "Draw ramps or funnels to guide the water straight into the glass.",
                    primaryActionText = "Try Again",
                    onPrimaryAction = { viewModel.restartLevel() },
                    secondaryActionText = "Undo Line",
                    onSecondaryAction = {
                        viewModel.startLevel(state.levelNumber)
                    }
                )
            }
        }
    }
}

@Composable
private fun OutcomeDialog(
    isVictory: Boolean,
    stars: Int,
    title: String,
    subtitle: String,
    primaryActionText: String,
    onPrimaryAction: () -> Unit,
    secondaryActionText: String,
    onSecondaryAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .testTag(if (isVictory) "happyglass_win_dialog" else "happyglass_loss_dialog"),
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
                color = if (isVictory) Color(0xFF38BDF8) else Color(0xFFF87171)
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onPrimaryAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("happyglass_dialog_primary"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVictory) Color(0xFF0284C7) else Color(0xFFEF4444)
                )
            ) {
                Text(
                    text = primaryActionText,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onSecondaryAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("happyglass_dialog_secondary"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = secondaryActionText,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

private fun DrawScope.drawBackgroundGrid() {
    val step = 32.dp.toPx()
    var x = 0f
    while (x < size.width) {
        drawLine(
            color = Color(0xFF1E293B).copy(alpha = 0.35f),
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
        x += step
    }
    var y = 0f
    while (y < size.height) {
        drawLine(
            color = Color(0xFF1E293B).copy(alpha = 0.35f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += step
    }
}

private fun DrawScope.drawObstacle(obs: HappyObstacle, scaleX: Float, scaleY: Float) {
    val left = obs.bounds.left * scaleX
    val top = obs.bounds.top * scaleY
    val right = obs.bounds.right * scaleX
    val bottom = obs.bounds.bottom * scaleY
    val w = right - left
    val h = bottom - top

    when (obs.type) {
        ObstacleType.SOLID_BLOCK -> {
            drawRoundRect(
                color = Color(0xFF334155),
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = Color(0xFF64748B),
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 2f)
            )
        }
        ObstacleType.BOUNCER_PAD -> {
            drawRoundRect(
                color = Color(0xFF059669),
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = Color(0xFF34D399),
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(width = 3f)
            )
        }
        ObstacleType.HAZARD_HOT_PLATE -> {
            drawRoundRect(
                color = Color(0xFFDC2626),
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Warning stripe
            drawLine(
                color = Color(0xFFFCA5A5),
                start = Offset(left + 6f, top + h / 2f),
                end = Offset(right - 6f, top + h / 2f),
                strokeWidth = 3f
            )
        }
        else -> {}
    }
}

private fun DrawScope.drawFaucet(x: Float, y: Float, scaleX: Float) {
    // Pipe coming from top
    drawRect(
        color = Color(0xFF475569),
        topLeft = Offset(x - 8f * scaleX, 0f),
        size = Size(16f * scaleX, y)
    )
    // Nozzle spout
    drawRoundRect(
        color = Color(0xFF94A3B8),
        topLeft = Offset(x - 14f * scaleX, y - 6f),
        size = Size(28f * scaleX, 16f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Water drip preview
    drawCircle(
        color = Color(0xFF38BDF8),
        radius = 5f * scaleX,
        center = Offset(x, y + 14f)
    )
}

private fun DrawScope.drawGlassCup(
    glass: GlassContainer,
    scaleX: Float,
    scaleY: Float,
    waterRatio: Float,
    isSad: Boolean
) {
    val left = glass.leftWallX * scaleX
    val right = glass.rightWallX * scaleX
    val top = glass.topY * scaleY
    val bottom = glass.bottomY * scaleY
    val wall = glass.wallThickness * scaleX

    // 1. Water Pool inside cup
    if (waterRatio > 0f) {
        val waterHeight = (bottom - wall - top) * waterRatio
        val waterTop = (bottom - wall) - waterHeight
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.85f), Color(0xFF0284C7).copy(alpha = 0.9f)),
                startY = waterTop,
                endY = bottom - wall
            ),
            topLeft = Offset(left + wall, waterTop),
            size = Size((right - wall) - (left + wall), waterHeight)
        )
    }

    // 2. Glass Outline (U-shape)
    val glassPath = Path().apply {
        moveTo(left, top)
        lineTo(left, bottom - 12f)
        quadraticTo(left, bottom, left + 12f, bottom)
        lineTo(right - 12f, bottom)
        quadraticTo(right, bottom, right, bottom - 12f)
        lineTo(right, top)
    }

    drawPath(
        path = glassPath,
        color = Color(0xFFE2E8F0),
        style = Stroke(width = wall, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Target fill dashed line
    val targetY = glass.waterTargetY * scaleY
    drawLine(
        color = Color(0xFFFBBF24).copy(alpha = 0.7f),
        start = Offset(left + wall + 4f, targetY),
        end = Offset(right - wall - 4f, targetY),
        strokeWidth = 2.5f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
    )

    // 3. Cute Animated Face on Glass
    val faceCenterX = (left + right) / 2f
    val faceCenterY = (top + bottom) / 2f - 4f

    if (isSad) {
        // Sad Eyes
        drawCircle(color = Color(0xFF475569), radius = 4.5f * scaleX, center = Offset(faceCenterX - 18f * scaleX, faceCenterY - 8f))
        drawCircle(color = Color(0xFF475569), radius = 4.5f * scaleX, center = Offset(faceCenterX + 18f * scaleX, faceCenterY - 8f))
        // Down-turned mouth
        val sadMouth = Path().apply {
            moveTo(faceCenterX - 14f * scaleX, faceCenterY + 16f)
            quadraticTo(faceCenterX, faceCenterY + 6f, faceCenterX + 14f * scaleX, faceCenterY + 16f)
        }
        drawPath(sadMouth, color = Color(0xFF475569), style = Stroke(width = 3.5f, cap = StrokeCap.Round))
    } else {
        // Happy Eyes (curved joyful lines)
        val leftEye = Path().apply {
            moveTo(faceCenterX - 24f * scaleX, faceCenterY - 6f)
            quadraticTo(faceCenterX - 18f * scaleX, faceCenterY - 14f, faceCenterX - 12f * scaleX, faceCenterY - 6f)
        }
        val rightEye = Path().apply {
            moveTo(faceCenterX + 12f * scaleX, faceCenterY - 6f)
            quadraticTo(faceCenterX + 18f * scaleX, faceCenterY - 14f, faceCenterX + 24f * scaleX, faceCenterY - 6f)
        }
        drawPath(leftEye, color = Color(0xFF0369A1), style = Stroke(width = 3.5f, cap = StrokeCap.Round))
        drawPath(rightEye, color = Color(0xFF0369A1), style = Stroke(width = 3.5f, cap = StrokeCap.Round))

        // Big Smile
        val smile = Path().apply {
            moveTo(faceCenterX - 18f * scaleX, faceCenterY + 8f)
            quadraticTo(faceCenterX, faceCenterY + 22f, faceCenterX + 18f * scaleX, faceCenterY + 8f)
        }
        drawPath(smile, color = Color(0xFF0369A1), style = Stroke(width = 3.5f, cap = StrokeCap.Round))

        // Cute blush cheeks
        drawCircle(color = Color(0xFFF472B6).copy(alpha = 0.6f), radius = 5f * scaleX, center = Offset(faceCenterX - 24f * scaleX, faceCenterY + 10f))
        drawCircle(color = Color(0xFFF472B6).copy(alpha = 0.6f), radius = 5f * scaleX, center = Offset(faceCenterX + 24f * scaleX, faceCenterY + 10f))
    }
}

private fun DrawScope.drawStrokeLine(
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
            width = 8f * scaleX,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
