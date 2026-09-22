package com.example.mrbullet.presentation.ui

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mrbullet.core.level.MrBulletLevelManager
import com.example.mrbullet.core.model.BarrierType
import com.example.mrbullet.core.model.BulletGamePhase
import com.example.mrbullet.core.model.EnemyTarget
import com.example.mrbullet.core.model.HeroShooter
import com.example.mrbullet.core.model.MrBulletWall
import com.example.mrbullet.core.model.TntBarrel
import com.example.mrbullet.presentation.MrBulletViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MrBulletGameScreen(
    viewModel: MrBulletViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val soundEnabled by viewModel.soundEnabledFlow.collectAsState()

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
                            text = "Mission ${state.levelNumber}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Bullets: ",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            repeat(state.maxBullets) { index ->
                                val hasBullet = index < state.bulletsRemaining
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .size(8.dp, 14.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (hasBullet) Color(0xFFFBBF24) else Color(0xFF475569))
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("mrbullet_game_back")
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
                        modifier = Modifier.testTag("mrbullet_game_sound")
                    ) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Sound",
                            tint = Color(0xFFEF4444)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.restartLevel() },
                        modifier = Modifier.testTag("mrbullet_game_restart")
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
        containerColor = Color(0xFF080C14)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val config = remember(state.levelNumber) {
                MrBulletLevelManager.getLevel(state.levelNumber)
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("mrbullet_canvas")
                    .pointerInput(state.phase) {
                        if (state.phase == BulletGamePhase.AIMING) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val scaleX = size.width / MrBulletLevelManager.VIRTUAL_WIDTH
                                    val scaleY = size.height / MrBulletLevelManager.VIRTUAL_HEIGHT
                                    viewModel.onAim(offset.x / scaleX, offset.y / scaleY)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val scaleX = size.width / MrBulletLevelManager.VIRTUAL_WIDTH
                                    val scaleY = size.height / MrBulletLevelManager.VIRTUAL_HEIGHT
                                    viewModel.onAim(change.position.x / scaleX, change.position.y / scaleY)
                                },
                                onDragEnd = {
                                    viewModel.onReleaseAim()
                                },
                                onDragCancel = {
                                    viewModel.onReleaseAim()
                                }
                            )
                        }
                    }
            ) {
                val scaleX = size.width / MrBulletLevelManager.VIRTUAL_WIDTH
                val scaleY = size.height / MrBulletLevelManager.VIRTUAL_HEIGHT

                // 1. Background sky & city silhouettes
                drawBackgroundSky()

                // 2. Walls & Obstacles
                for (wall in config.walls) {
                    drawWall(wall, scaleX, scaleY)
                }

                // 3. TNT Barrels
                for (barrel in state.barrels) {
                    drawTntBarrel(barrel, scaleX, scaleY)
                }

                // 4. Enemies
                for (enemy in state.enemies) {
                    drawEnemy(enemy, scaleX, scaleY)
                }

                // 5. Hero Agent
                drawHero(config.hero, state.aimAngleRad, state.isAiming, scaleX, scaleY)

                // 6. Laser Aim Trajectory
                if (state.isAiming && state.trajectoryPoints.size >= 2) {
                    drawTrajectory(state.trajectoryPoints, scaleX, scaleY)
                }

                // 7. Active Flying Bullets & Trails
                for (bullet in state.activeBullets) {
                    if (bullet.isAlive) {
                        // Trail
                        for (i in 0 until bullet.trail.size - 1) {
                            val alpha = (i.toFloat() / bullet.trail.size).coerceIn(0.1f, 0.8f)
                            drawLine(
                                color = Color(0xFFF59E0B).copy(alpha = alpha),
                                start = Offset(bullet.trail[i].x * scaleX, bullet.trail[i].y * scaleY),
                                end = Offset(bullet.trail[i + 1].x * scaleX, bullet.trail[i + 1].y * scaleY),
                                strokeWidth = 3f * scaleX,
                                cap = StrokeCap.Round
                            )
                        }
                        // Bullet Head
                        drawCircle(
                            color = Color(0xFFFBBF24),
                            radius = bullet.radius * scaleX,
                            center = Offset(bullet.x * scaleX, bullet.y * scaleY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = bullet.radius * 0.5f * scaleX,
                            center = Offset(bullet.x * scaleX, bullet.y * scaleY)
                        )
                    }
                }
            }

            // Hint bar at bottom when aiming
            if (state.phase == BulletGamePhase.AIMING) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.85f))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Drag finger to aim laser, release to shoot!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }

            // Outcome Overlays: WON
            AnimatedVisibility(
                visible = state.phase == BulletGamePhase.WON,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                BulletOutcomeDialog(
                    isVictory = true,
                    stars = state.stars,
                    title = "Target Eliminated!",
                    subtitle = "All targets neutralized with surgical precision.",
                    primaryText = "Next Mission",
                    onPrimary = { viewModel.nextLevel() },
                    secondaryText = "Replay",
                    onSecondary = { viewModel.restartLevel() }
                )
            }

            // Outcome Overlays: LOST
            AnimatedVisibility(
                visible = state.phase == BulletGamePhase.LOST,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                BulletOutcomeDialog(
                    isVictory = false,
                    stars = 0,
                    title = "Out of Ammo!",
                    subtitle = "Hostiles remain standing. Aim ricochets to hit multiple targets.",
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
private fun BulletOutcomeDialog(
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
            .testTag(if (isVictory) "mrbullet_win_dialog" else "mrbullet_loss_dialog"),
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
                color = if (isVictory) Color(0xFF10B981) else Color(0xFFEF4444)
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
                    .testTag("mrbullet_dialog_primary"),
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
                    .testTag("mrbullet_dialog_secondary"),
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

private fun DrawScope.drawBackgroundSky() {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF0A0F1D), Color(0xFF161F33))
        )
    )
}

private fun DrawScope.drawWall(wall: MrBulletWall, scaleX: Float, scaleY: Float) {
    val left = wall.bounds.left * scaleX
    val top = wall.bounds.top * scaleY
    val right = wall.bounds.right * scaleX
    val bottom = wall.bounds.bottom * scaleY
    val w = right - left
    val h = bottom - top

    when (wall.type) {
        BarrierType.STEEL_BEAM -> {
            drawRoundRect(
                color = Color(0xFF475569),
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color(0xFF94A3B8),
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(2f)
            )
        }
        BarrierType.WOOD_CRATE -> {
            drawRect(
                color = Color(0xFF78350F),
                topLeft = Offset(left, top),
                size = Size(w, h)
            )
            drawLine(
                color = Color(0xFFB45309),
                start = Offset(left, top),
                end = Offset(right, bottom),
                strokeWidth = 2f
            )
            drawLine(
                color = Color(0xFFB45309),
                start = Offset(right, top),
                end = Offset(left, bottom),
                strokeWidth = 2f
            )
        }
        else -> {
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = Color(0xFF334155),
                topLeft = Offset(left, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(2f)
            )
        }
    }
}

private fun DrawScope.drawTntBarrel(barrel: TntBarrel, scaleX: Float, scaleY: Float) {
    if (barrel.isExploded) {
        // Smoke puff
        drawCircle(
            color = Color(0xFFEF4444).copy(alpha = 0.3f),
            radius = barrel.explosionRadius * 0.4f * scaleX,
            center = Offset(barrel.x * scaleX, (barrel.y - barrel.height / 2f) * scaleY)
        )
        return
    }

    val left = (barrel.x - barrel.width / 2f) * scaleX
    val top = (barrel.y - barrel.height) * scaleY
    val w = barrel.width * scaleX
    val h = barrel.height * scaleY

    drawRoundRect(
        color = Color(0xFFDC2626),
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Warning yellow stripe
    drawRect(
        color = Color(0xFFFBBF24),
        topLeft = Offset(left, top + h * 0.35f),
        size = Size(w, h * 0.3f)
    )
    // "TNT" bar in center
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(left + 4f, top + h * 0.5f),
        end = Offset(left + w - 4f, top + h * 0.5f),
        strokeWidth = 3f
    )
}

private fun DrawScope.drawEnemy(enemy: EnemyTarget, scaleX: Float, scaleY: Float) {
    val cx = enemy.x * scaleX
    val cy = enemy.y * scaleY

    if (!enemy.isAlive) {
        // Defeated ragdoll silhouette on ground
        drawOval(
            color = Color(0xFFEF4444).copy(alpha = 0.5f),
            topLeft = Offset(cx - 20f * scaleX, cy - 8f * scaleY),
            size = Size(40f * scaleX, 10f * scaleY)
        )
        return
    }

    val headY = cy - 50f * scaleY
    val bodyY = cy - 30f * scaleY

    // Body (Bandit striped jumpsuit)
    drawRoundRect(
        color = Color(0xFFEA580C),
        topLeft = Offset(cx - 12f * scaleX, bodyY),
        size = Size(24f * scaleX, 30f * scaleY),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Black stripes
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(cx - 12f * scaleX, bodyY + 8f),
        end = Offset(cx + 12f * scaleX, bodyY + 8f),
        strokeWidth = 3f
    )
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(cx - 12f * scaleX, bodyY + 18f),
        end = Offset(cx + 12f * scaleX, bodyY + 18f),
        strokeWidth = 3f
    )

    // Head
    drawCircle(
        color = Color(0xFFFBBF24),
        radius = 12f * scaleX,
        center = Offset(cx, headY)
    )
    // Eye mask
    drawRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(cx - 10f * scaleX, headY - 4f),
        size = Size(20f * scaleX, 8f)
    )
    // White eye dots
    drawCircle(color = Color.White, radius = 2f, center = Offset(cx - 4f * scaleX, headY))
    drawCircle(color = Color.White, radius = 2f, center = Offset(cx + 4f * scaleX, headY))
}

private fun DrawScope.drawHero(
    hero: HeroShooter,
    aimAngleRad: Float,
    isAiming: Boolean,
    scaleX: Float,
    scaleY: Float
) {
    val cx = hero.x * scaleX
    val cy = hero.y * scaleY

    val headY = cy - 52f * scaleY
    val bodyY = cy - 32f * scaleY

    // Tuxedo Body
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(cx - 12f * scaleX, bodyY),
        size = Size(24f * scaleX, 32f * scaleY),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // White shirt V-shape
    val shirtPath = Path().apply {
        moveTo(cx - 6f * scaleX, bodyY)
        lineTo(cx, bodyY + 16f * scaleY)
        lineTo(cx + 6f * scaleX, bodyY)
    }
    drawPath(shirtPath, color = Color.White)
    // Red Tie
    drawLine(
        color = Color(0xFFDC2626),
        start = Offset(cx, bodyY + 4f),
        end = Offset(cx, bodyY + 18f * scaleY),
        strokeWidth = 3f
    )

    // Head
    drawCircle(
        color = Color(0xFFFED7AA),
        radius = 12f * scaleX,
        center = Offset(cx, headY)
    )
    // Sunglasses
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(cx - 10f * scaleX, headY - 3f),
        size = Size(20f * scaleX, 7f),
        cornerRadius = CornerRadius(2f, 2f)
    )

    // Aiming Arm & Pistol
    val shoulderX = cx + 4f * scaleX
    val shoulderY = cy - 26f * scaleY
    val armLen = 22f * scaleX

    val angle = if (isAiming) aimAngleRad else 0f
    val handX = shoulderX + cos(angle) * armLen
    val handY = shoulderY + sin(angle) * armLen

    // Arm line
    drawLine(
        color = Color(0xFF0F172A),
        start = Offset(shoulderX, shoulderY),
        end = Offset(handX, handY),
        strokeWidth = 6f * scaleX,
        cap = StrokeCap.Round
    )

    // Gun barrel
    val gunLen = 10f * scaleX
    val muzzleX = handX + cos(angle) * gunLen
    val muzzleY = handY + sin(angle) * gunLen
    drawLine(
        color = Color(0xFF64748B),
        start = Offset(handX, handY),
        end = Offset(muzzleX, muzzleY),
        strokeWidth = 4f * scaleX,
        cap = StrokeCap.Square
    )
}

private fun DrawScope.drawTrajectory(points: List<Offset>, scaleX: Float, scaleY: Float) {
    val path = Path().apply {
        moveTo(points[0].x * scaleX, points[0].y * scaleY)
        for (i in 1 until points.size) {
            lineTo(points[i].x * scaleX, points[i].y * scaleY)
        }
    }

    drawPath(
        path = path,
        color = Color(0xFFEF4444),
        style = Stroke(
            width = 2.5f * scaleX,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
        )
    )

    // Endpoint crosshair circle
    val last = points.last()
    drawCircle(
        color = Color(0xFFEF4444).copy(alpha = 0.8f),
        radius = 8f * scaleX,
        center = Offset(last.x * scaleX, last.y * scaleY),
        style = Stroke(2f)
    )
}
