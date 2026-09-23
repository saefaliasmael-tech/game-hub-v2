package com.example.minitd.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minitd.core.level.MiniTDLevelManager
import com.example.minitd.core.model.EnemyType
import com.example.minitd.core.model.TowerType
import com.example.minitd.presentation.MiniTDViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniTDGameScreen(
    viewModel: MiniTDViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val soundEnabled by viewModel.soundEnabledFlow.collectAsState()
    val levelConfig = remember(state.levelNumber) { MiniTDLevelManager.getLevel(state.levelNumber) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(Unit) {
        if (state.slots.isEmpty()) {
            viewModel.startLevel(state.levelNumber.coerceAtLeast(1))
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.pauseGame()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Lives
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, contentDescription = "Lives", tint = Color(0xFFE63946))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${state.lives}", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Gold
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = "Gold", tint = Color(0xFFFFD166))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${state.gold}", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Wave
                        Text(
                            "Wave ${state.waveIndex}/${state.totalWaves}",
                            color = Color(0xFF00B4D8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
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
                    IconButton(onClick = { viewModel.toggleGameSpeed() }) {
                        Text(
                            "${state.gameSpeedMultiplier.toInt()}x",
                            color = if (state.gameSpeedMultiplier > 1f) Color(0xFFFFD166) else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = { viewModel.toggleSound() }) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Sound",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B2A4A))
            )
        },
        containerColor = Color(0xFF0D1B2A)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Battlefield Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(interactionSource = interactionSource, indication = null) {
                        viewModel.selectSlot(null)
                        viewModel.selectTower(null)
                    }
            ) {
                val w = size.width
                val h = size.height

                // 1. Draw Winding Path
                val waypoints = levelConfig.waypoints
                if (waypoints.isNotEmpty()) {
                    val path = Path()
                    path.moveTo(waypoints[0].xPercent * w, waypoints[0].yPercent * h)
                    for (i in 1 until waypoints.size) {
                        path.lineTo(waypoints[i].xPercent * w, waypoints[i].yPercent * h)
                    }
                    // Border of road
                    drawPath(
                        path = path,
                        color = Color(0xFF33415C),
                        style = Stroke(width = 36.dp.toPx(), pathEffect = null)
                    )
                    // Road center
                    drawPath(
                        path = path,
                        color = Color(0xFF415A77),
                        style = Stroke(width = 30.dp.toPx(), pathEffect = null)
                    )
                }

                // 2. Draw Tower Build Slots
                for (slot in state.slots) {
                    val slotX = slot.xPercent * w
                    val slotY = slot.yPercent * h
                    if (!slot.isOccupied) {
                        drawCircle(
                            color = Color(0xFF778DA9).copy(alpha = 0.4f),
                            radius = 20.dp.toPx(),
                            center = Offset(slotX, slotY),
                            style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                        )
                    }
                }

                // 3. Draw Selected Tower Range
                val selectedTower = state.selectedTower
                if (selectedTower != null) {
                    val rangePx = selectedTower.range * w
                    drawCircle(
                        color = Color(0xFF00B4D8).copy(alpha = 0.15f),
                        radius = rangePx,
                        center = Offset(selectedTower.xPercent * w, selectedTower.yPercent * h)
                    )
                    drawCircle(
                        color = Color(0xFF00B4D8).copy(alpha = 0.5f),
                        radius = rangePx,
                        center = Offset(selectedTower.xPercent * w, selectedTower.yPercent * h),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                // 4. Draw Towers
                for (tower in state.towers) {
                    val tx = tower.xPercent * w
                    val ty = tower.yPercent * h
                    val towerColor = when (tower.type) {
                        TowerType.ARCHER -> Color(0xFF52B788)
                        TowerType.CANNON -> Color(0xFFE63946)
                        TowerType.MAGIC -> Color(0xFF9D4EDD)
                    }

                    // Base platform
                    drawCircle(
                        color = Color(0xFF1B2A4A),
                        radius = 18.dp.toPx(),
                        center = Offset(tx, ty)
                    )
                    // Core gem/turret
                    drawCircle(
                        color = towerColor,
                        radius = 14.dp.toPx(),
                        center = Offset(tx, ty)
                    )
                }

                // 5. Draw Enemies
                for (enemy in state.enemies) {
                    val ex = enemy.xPercent * w
                    val ey = enemy.yPercent * h
                    val enemyRadius = enemy.type.radiusPercent * w

                    val enemyColor = when (enemy.type) {
                        EnemyType.NORMAL -> Color(0xFF48CAE4)
                        EnemyType.FAST -> Color(0xFFFFB703)
                        EnemyType.TANK -> Color(0xFF6C757D)
                        EnemyType.BOSS -> Color(0xFFD00000)
                    }

                    // Enemy body
                    drawCircle(
                        color = enemyColor,
                        radius = enemyRadius,
                        center = Offset(ex, ey)
                    )

                    // Health bar
                    val barWidth = enemyRadius * 2.2f
                    val barHeight = 4.dp.toPx()
                    val barLeft = ex - barWidth / 2f
                    val barTop = ey - enemyRadius - 8.dp.toPx()
                    val hpRatio = (enemy.currentHp / enemy.maxHp).coerceIn(0f, 1f)

                    drawRect(
                        color = Color(0xFF1B2A4A),
                        topLeft = Offset(barLeft, barTop),
                        size = Size(barWidth, barHeight)
                    )
                    drawRect(
                        color = if (hpRatio > 0.4f) Color(0xFF52B788) else Color(0xFFE63946),
                        topLeft = Offset(barLeft, barTop),
                        size = Size(barWidth * hpRatio, barHeight)
                    )
                }

                // 6. Draw Projectiles
                for (proj in state.projectiles) {
                    val px = proj.currentX * w
                    val py = proj.currentY * h
                    val projColor = when (proj.towerType) {
                        TowerType.ARCHER -> Color(0xFF52B788)
                        TowerType.CANNON -> Color(0xFFFFD166)
                        TowerType.MAGIC -> Color(0xFFC77DFF)
                    }
                    drawCircle(
                        color = projColor,
                        radius = 4.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }

            // Clickable Overlays for Slots and Towers
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val w = maxWidth
                val h = maxHeight

                // Slot Click Targets
                for (slot in state.slots) {
                    if (!slot.isOccupied) {
                        val size = 44.dp
                        val x = w * slot.xPercent - size / 2
                        val y = h * slot.yPercent - size / 2

                        Box(
                            modifier = Modifier
                                .offset(x = x, y = y)
                                .size(size)
                                .clip(CircleShape)
                                .clickable {
                                    viewModel.selectSlot(slot)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Build Slot",
                                tint = Color(0xFF00B4D8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Tower Click Targets
                for (tower in state.towers) {
                    val size = 44.dp
                    val x = w * tower.xPercent - size / 2
                    val y = h * tower.yPercent - size / 2

                    Box(
                        modifier = Modifier
                            .offset(x = x, y = y)
                            .size(size)
                            .clip(CircleShape)
                            .clickable {
                                viewModel.selectTower(tower)
                            }
                    )
                }
            }

            // Bottom Floating Controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Build Tower Card (When slot is selected)
                val selectedSlot = state.selectedSlot
                if (selectedSlot != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A4A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF00B4D8), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Deploy Tower",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TowerType.values().forEach { type ->
                                    val canAfford = state.gold >= type.baseCost
                                    Button(
                                        onClick = { viewModel.buildTower(type) },
                                        enabled = canAfford,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = when (type) {
                                                TowerType.ARCHER -> Color(0xFF52B788)
                                                TowerType.CANNON -> Color(0xFFE63946)
                                                TowerType.MAGIC -> Color(0xFF9D4EDD)
                                            }
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(4.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(type.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("$${type.baseCost}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Upgrade Tower Card (When tower is selected)
                val selectedTower = state.selectedTower
                if (selectedTower != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A4A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF00B4D8), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "${selectedTower.type.displayName} (Lv ${selectedTower.level})",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "Dmg: ${selectedTower.damage.toInt()} | Range: ${(selectedTower.range * 100).toInt()}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE0E1DD)
                                )
                            }

                            if (selectedTower.level < 3) {
                                val canAfford = state.gold >= selectedTower.upgradeCost
                                Button(
                                    onClick = { viewModel.upgradeTower() },
                                    enabled = canAfford,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("UPGRADE ($${selectedTower.upgradeCost})", color = Color(0xFF0D1B2A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            } else {
                                Text("MAX LEVEL", color = Color(0xFF52B788), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // 3. Call Wave Button
                if (!state.isWaveInProgress && state.waveIndex < state.totalWaves && selectedSlot == null && selectedTower == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.startNextWave() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF0D1B2A))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("START WAVE ${state.waveIndex + 1}", fontWeight = FontWeight.Bold, color = Color(0xFF0D1B2A))
                    }
                }
            }

            // Defeat Dialog
            if (state.isGameOver) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "KINGDOM FALLEN!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE63946),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "Enemies breached the castle gates. Re-strategize your tower placements and try again!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.restartLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("RETRY BATTLE", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("RETURN TO MAP", color = Color(0xFFE0E1DD))
                        }
                    },
                    containerColor = Color(0xFF1B2A4A)
                )
            }

            // Victory Dialog
            if (state.isVictory) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "VICTORY ACHIEVED!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF52B788),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "All enemy waves were crushed! Territory secured.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.nextLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF52B788)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ADVANCE TO NEXT TERRITORY", fontWeight = FontWeight.Bold, color = Color(0xFF0D1B2A))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("LEVELS MAP", color = Color(0xFFE0E1DD))
                        }
                    },
                    containerColor = Color(0xFF1B2A4A)
                )
            }
        }
    }
}
