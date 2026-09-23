package com.example.seabattle2.presentation.ui

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seabattle2.engine.SeaBattleEngine
import com.example.seabattle2.model.BattlePhase
import com.example.seabattle2.model.CellStatus
import com.example.seabattle2.presentation.SeaBattleViewModel
import com.zubaluba.gamehub.ads.AdBanner
import com.zubaluba.gamehub.ads.UnifiedAdManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeaBattleScreen(
    viewModel: SeaBattleViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val state by viewModel.state.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Enemy Radar (Target), 1 = My Fleet

    LaunchedEffect(state.phase) {
        if (state.phase == BattlePhase.GAME_OVER && activity != null) {
            UnifiedAdManager.getInstance(activity).showInterstitial(activity)
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
                        Text(
                            text = "Sea Battle 2",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1FAEE),
                            fontSize = 18.sp
                        )
                        val statusText = when (state.phase) {
                            BattlePhase.PLACEMENT -> "DEPLOY FLEET"
                            BattlePhase.PLAYER_TURN -> "YOUR TURN 🎯"
                            BattlePhase.AI_TURN -> "ENEMY FIRING..."
                            BattlePhase.GAME_OVER -> if (state.isPlayerWinner) "VICTORY! 🏆" else "DEFEAT! 💥"
                        }
                        Text(
                            text = statusText,
                            color = if (state.phase == BattlePhase.PLAYER_TURN) Color(0xFF48CAE4) else Color(0xFFFFB703),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("seabattle2_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.restart() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1D3557))
            )
        },
        bottomBar = {
            AdBanner(modifier = Modifier.fillMaxWidth().navigationBarsPadding())
        },
        containerColor = Color(0xFF0F1E33)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Fleet Status Counts
            val enemySunk = state.enemyShips.count { it.isSunk }
            val playerSunk = state.playerShips.count { it.isSunk }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enemy Sunk: $enemySunk / 10", color = Color(0xFFE63946), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("My Sunk: $playerSunk / 10", color = Color(0xFF457B9D), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // Radar Tabs (during battle)
            if (state.phase != BattlePhase.PLACEMENT) {
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color(0xFF1D3557),
                    contentColor = Color.White,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Enemy Radar (Fire)", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("My Fleet", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // 10x10 Graph Paper Grid Canvas
            val displayGrid = if (state.phase == BattlePhase.PLACEMENT || activeTab == 1) state.playerGrid else state.enemyGrid

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE9F1F7))
                    .border(2.dp, Color(0xFF457B9D), RoundedCornerShape(16.dp))
                    .pointerInput(activeTab, state.phase) {
                        detectTapGestures { offset ->
                            if (state.phase == BattlePhase.PLAYER_TURN && activeTab == 0) {
                                val cellSize = size.width / SeaBattleEngine.GRID_SIZE
                                val gx = (offset.x / cellSize).toInt().coerceIn(0, 9)
                                val gy = (offset.y / cellSize).toInt().coerceIn(0, 9)
                                viewModel.fireAtEnemy(gx, gy)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cellSize = w / SeaBattleEngine.GRID_SIZE

                    // Graph paper grid lines (Blue ink sketch)
                    for (i in 0..SeaBattleEngine.GRID_SIZE) {
                        val pos = i * cellSize
                        drawLine(Color(0xFFB0C4DE), Offset(pos, 0f), Offset(pos, h), strokeWidth = 1.dp.toPx())
                        drawLine(Color(0xFFB0C4DE), Offset(0f, pos), Offset(w, pos), strokeWidth = 1.dp.toPx())
                    }

                    // Render cells
                    for (x in 0 until SeaBattleEngine.GRID_SIZE) {
                        for (y in 0 until SeaBattleEngine.GRID_SIZE) {
                            val status = displayGrid[x][y]
                            val cx = x * cellSize + cellSize / 2f
                            val cy = y * cellSize + cellSize / 2f

                            when (status) {
                                CellStatus.SHIP.ordinal -> {
                                    // Navy blue ship hull
                                    drawRoundRect(
                                        color = Color(0xFF1D3557),
                                        topLeft = Offset(x * cellSize + 2f, y * cellSize + 2f),
                                        size = Size(cellSize - 4f, cellSize - 4f),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                }
                                CellStatus.MISS.ordinal -> {
                                    // Small ballpoint pen dot
                                    drawCircle(color = Color(0xFF457B9D), radius = 3.dp.toPx(), center = Offset(cx, cy))
                                }
                                CellStatus.HIT.ordinal -> {
                                    // Red ink explosion cross
                                    val r = cellSize * 0.35f
                                    drawLine(Color(0xFFE63946), Offset(cx - r, cy - r), Offset(cx + r, cy + r), strokeWidth = 3.dp.toPx())
                                    drawLine(Color(0xFFE63946), Offset(cx + r, cy - r), Offset(cx - r, cy + r), strokeWidth = 3.dp.toPx())
                                }
                                CellStatus.SUNK.ordinal -> {
                                    // Sunken ship hull with red X
                                    drawRoundRect(
                                        color = Color(0xFF6C757D).copy(alpha = 0.6f),
                                        topLeft = Offset(x * cellSize + 2f, y * cellSize + 2f),
                                        size = Size(cellSize - 4f, cellSize - 4f),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                    val r = cellSize * 0.35f
                                    drawLine(Color(0xFFD00000), Offset(cx - r, cy - r), Offset(cx + r, cy + r), strokeWidth = 3.5.dp.toPx())
                                    drawLine(Color(0xFFD00000), Offset(cx + r, cy - r), Offset(cx - r, cy + r), strokeWidth = 3.5.dp.toPx())
                                }
                            }
                        }
                    }
                }
            }

            // Controls row
            if (state.phase == BattlePhase.PLACEMENT) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.randomizePlacement() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Casino, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Randomize")
                    }

                    Button(
                        onClick = { viewModel.startBattle() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946))
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BATTLE!", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1D3557),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = if (state.phase == BattlePhase.PLAYER_TURN)
                            "🎯 Tap any enemy coordinate to launch artillery strike!"
                        else
                            "⏳ Enemy is calculating target coordinates...",
                        color = Color(0xFFF1FAEE),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    // Game Over Dialog
    if (state.phase == BattlePhase.GAME_OVER) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = if (state.isPlayerWinner) "FLEET VICTORY! 🎖️" else "FLEET DESTROYED! 🌊",
                    fontWeight = FontWeight.Bold,
                    color = if (state.isPlayerWinner) Color(0xFF48CAE4) else Color(0xFFE63946)
                )
            },
            text = {
                Column {
                    Text(if (state.isPlayerWinner) "You sank all 10 enemy warships!" else "All your ships have been sent to the ocean floor.")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Total Shots: ${state.shotsFired}")
                    Text("Hits: ${state.hitsScored}")
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.restart() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557))
                ) {
                    Text("New Battle", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            containerColor = Color(0xFF0F1E33)
        )
    }
}
