package com.example.mrbullet.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mrbullet.core.engine.MrBulletEngine
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Mission ${state.levelNumber}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "AMMO: ",
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            repeat(state.maxBullets) { index ->
                                val hasAmmo = index < state.bulletsLeft
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .width(6.dp)
                                        .height(16.dp)
                                        .background(
                                            if (hasAmmo) Color(0xFFFFD166) else Color(0xFF475569),
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSound() }) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Sound",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { viewModel.restartLevel() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(state.bulletsLeft) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                viewModel.onAim(offset.x / size.width, offset.y / size.height)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                viewModel.onAim(change.position.x / size.width, change.position.y / size.height)
                            },
                            onDragEnd = {
                                state.aimAngle?.let { angle ->
                                    val tx = state.heroX + cos(angle) * 0.2f
                                    val ty = state.heroY + sin(angle) * 0.2f
                                    viewModel.onShoot(tx, ty)
                                }
                            }
                        )
                    }
            ) {
                val w = size.width
                val h = size.height

                // 1. Draw Walls & Platforms
                for (wall in state.walls) {
                    if (wall.isDestroyed) continue

                    if (wall.isTnt) {
                        // TNT crate
                        val left = (wall.x1.coerceAtMost(wall.x2) - 0.03f) * w
                        val top = (wall.y1.coerceAtMost(wall.y2) - 0.03f) * h
                        val sz = 0.06f * w
                        drawRect(
                            color = Color(0xFFDC2626),
                            topLeft = Offset(left, top),
                            size = Size(sz, sz)
                        )
                        drawRect(
                            color = Color(0xFFFEF08A),
                            topLeft = Offset(left + sz * 0.15f, top + sz * 0.35f),
                            size = Size(sz * 0.7f, sz * 0.3f)
                        )
                    } else {
                        // Steel wall / beam
                        drawLine(
                            color = if (wall.isDestructible) Color(0xFF94A3B8) else Color(0xFF475569),
                            start = Offset(wall.x1 * w, wall.y1 * h),
                            end = Offset(wall.x2 * w, wall.y2 * h),
                            strokeWidth = 12.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        // Metallic core line
                        drawLine(
                            color = Color(0xFF64748B),
                            start = Offset(wall.x1 * w, wall.y1 * h),
                            end = Offset(wall.x2 * w, wall.y2 * h),
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 2. Draw Spy Hero
                val hx = state.heroX * w
                val hy = state.heroY * h

                // Hero Body (Tuxedo)
                drawRoundRect(
                    color = Color(0xFF020617),
                    topLeft = Offset(hx - 12.dp.toPx(), hy - 10.dp.toPx()),
                    size = Size(24.dp.toPx(), 36.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
                // White shirt collar
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(hx, hy - 4.dp.toPx())
                )
                // Red bow tie
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = 3.dp.toPx(),
                    center = Offset(hx, hy - 4.dp.toPx())
                )
                // Head
                drawCircle(
                    color = Color(0xFFFED7AA),
                    radius = 10.dp.toPx(),
                    center = Offset(hx, hy - 20.dp.toPx())
                )
                // Black sunglasses
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(hx - 7.dp.toPx(), hy - 23.dp.toPx()),
                    size = Size(16.dp.toPx(), 6.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )

                // Hero Gun arm
                val angle = state.aimAngle ?: 0f
                val armLen = 22.dp.toPx()
                val gunMuzzleX = hx + cos(angle) * armLen
                val gunMuzzleY = hy - 6.dp.toPx() + sin(angle) * armLen

                drawLine(
                    color = Color(0xFF0F172A),
                    start = Offset(hx, hy - 6.dp.toPx()),
                    end = Offset(gunMuzzleX, gunMuzzleY),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Pistol barrel
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(gunMuzzleX, gunMuzzleY),
                    end = Offset(gunMuzzleX + cos(angle) * 8.dp.toPx(), gunMuzzleY + sin(angle) * 8.dp.toPx()),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Square
                )

                // 3. Draw Laser Sight if aiming
                state.aimAngle?.let { aimRad ->
                    val laserPoints = MrBulletEngine.calculateLaserPath(
                        startX = gunMuzzleX / w,
                        startY = gunMuzzleY / h,
                        angleRad = aimRad,
                        walls = state.walls
                    )

                    if (laserPoints.size >= 2) {
                        val path = Path().apply {
                            moveTo(laserPoints[0].x * w, laserPoints[0].y * h)
                            for (i in 1 until laserPoints.size) {
                                lineTo(laserPoints[i].x * w, laserPoints[i].y * h)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFFEF4444).copy(alpha = 0.8f),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            )
                        )
                        // Bounce dots
                        for (p in laserPoints.drop(1)) {
                            drawCircle(
                                color = Color(0xFFEF4444),
                                radius = 4.dp.toPx(),
                                center = Offset(p.x * w, p.y * h)
                            )
                        }
                    }
                }

                // 4. Draw Enemies
                for (enemy in state.enemies) {
                    val ex = enemy.x * w
                    val ey = enemy.y * h

                    if (enemy.isDead) {
                        // Fallen enemy
                        drawRoundRect(
                            color = Color(0xFFF97316).copy(alpha = 0.6f),
                            topLeft = Offset(ex - 20.dp.toPx(), ey + 10.dp.toPx()),
                            size = Size(40.dp.toPx(), 14.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        // 'X' Eyes
                        drawLine(
                            color = Color.White,
                            start = Offset(ex + 12.dp.toPx(), ey + 13.dp.toPx()),
                            end = Offset(ex + 18.dp.toPx(), ey + 19.dp.toPx()),
                            strokeWidth = 2.dp.toPx()
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(ex + 18.dp.toPx(), ey + 13.dp.toPx()),
                            end = Offset(ex + 12.dp.toPx(), ey + 19.dp.toPx()),
                            strokeWidth = 2.dp.toPx()
                        )
                    } else {
                        // Standing enemy
                        drawRoundRect(
                            color = Color(0xFFEA580C),
                            topLeft = Offset(ex - 12.dp.toPx(), ey - 10.dp.toPx()),
                            size = Size(24.dp.toPx(), 36.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                        // Enemy Head
                        drawCircle(
                            color = Color(0xFFFED7AA),
                            radius = 10.dp.toPx(),
                            center = Offset(ex, ey - 20.dp.toPx())
                        )
                        // Bandit red bandana
                        drawRect(
                            color = Color(0xFFDC2626),
                            topLeft = Offset(ex - 10.dp.toPx(), ey - 27.dp.toPx()),
                            size = Size(20.dp.toPx(), 7.dp.toPx())
                        )
                        // Angry eyes
                        drawCircle(
                            color = Color.Black,
                            radius = 2.dp.toPx(),
                            center = Offset(ex - 4.dp.toPx(), ey - 19.dp.toPx())
                        )
                        drawCircle(
                            color = Color.Black,
                            radius = 2.dp.toPx(),
                            center = Offset(ex + 4.dp.toPx(), ey - 19.dp.toPx())
                        )
                    }
                }

                // 5. Draw Bullets
                val bulletR = MrBulletEngine.BULLET_RADIUS * w
                for (b in state.activeBullets) {
                    val bx = b.x * w
                    val by = b.y * h

                    // Motion trail glow
                    drawCircle(
                        color = Color(0xFFF59E0B).copy(alpha = 0.4f),
                        radius = bulletR * 2f,
                        center = Offset(bx, by)
                    )
                    // Golden bullet core
                    drawCircle(
                        color = Color(0xFFFFD166),
                        radius = bulletR,
                        center = Offset(bx, by)
                    )
                }
            }

            // Defeat Dialog
            if (state.isGameOver) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "OUT OF AMMO!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFEF4444),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "Targets are still standing! Leverage ricochets against steel walls and ignite TNT crates to eliminate all enemies.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.restartLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("TRY AGAIN", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("MISSION SELECT", color = Color(0xFFF59E0B))
                        }
                    },
                    containerColor = Color(0xFF1E293B)
                )
            }

            // Victory Dialog
            if (state.isWon) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "TARGETS ELIMINATED!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF10B981),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                repeat(3) { index ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (index < state.stars) Color(0xFFFFD166) else Color(0xFF475569),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Flawless marksmanship, agent! Mission cleared with ${state.bulletsLeft} rounds remaining.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.nextLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("NEXT MISSION", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("MISSION SELECT", color = Color(0xFFF59E0B))
                        }
                    },
                    containerColor = Color(0xFF1E293B)
                )
            }
        }
    }
}
