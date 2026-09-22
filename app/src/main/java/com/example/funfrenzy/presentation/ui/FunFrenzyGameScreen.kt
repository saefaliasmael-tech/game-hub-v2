package com.example.funfrenzy.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.funfrenzy.core.model.MicroGameType
import com.example.funfrenzy.core.model.MicroResult
import com.example.funfrenzy.presentation.FunFrenzyViewModel
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunFrenzyGameScreen(
    viewModel: FunFrenzyViewModel,
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
                            "Stage ${state.levelNumber} (${state.microIndex + 1}/${state.totalMicroGames})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        // Lives
                        Row {
                            repeat(state.maxLives) { index ->
                                Icon(
                                    imageVector = if (index < state.lives) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (index < state.lives) Color(0xFFEC4899) else Color(0xFF4C1D95),
                                    modifier = Modifier.size(20.dp)
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1035))
            )
        },
        containerColor = Color(0xFF0F061F)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Fuse timer progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color(0xFF1E1035))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(state.progressRatio)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    if (state.progressRatio > 0.3f) Color(0xFF06B6D4) else Color(0xFFEF4444),
                                    if (state.progressRatio > 0.3f) Color(0xFFEC4899) else Color(0xFFF59E0B)
                                )
                            )
                        )
                )
            }

            // Prompt Banner
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF261447)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = state.currentSpec.prompt,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    ),
                    color = Color(0xFFFBBF24),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }

            // Main Active MicroGame Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (state.currentSpec.type) {
                    MicroGameType.TAP_RUSH -> {
                        TapRushView(
                            currentTaps = state.subState.currentTaps,
                            requiredTaps = state.subState.requiredTaps,
                            onTap = { viewModel.onTapRush() }
                        )
                    }
                    MicroGameType.CATCH_FALLING -> {
                        CatchFallingView(
                            bucketX = state.subState.bucketX,
                            gemX = state.subState.gemX,
                            gemY = state.subState.gemY,
                            onBucketMove = { viewModel.onBucketDrag(it) }
                        )
                    }
                    MicroGameType.POP_BALLOONS -> {
                        PopBalloonsView(
                            balloons = state.subState.balloons,
                            poppedCount = state.subState.poppedCount,
                            requiredPops = state.subState.requiredPops,
                            onPop = { viewModel.onPopBalloon(it) }
                        )
                    }
                    MicroGameType.DODGE_ROCKS -> {
                        DodgeRocksView(
                            playerX = state.subState.playerX,
                            rocks = state.subState.rocks,
                            onPlayerMove = { viewModel.onDodgeMove(it) }
                        )
                    }
                    MicroGameType.STOP_NEEDLE -> {
                        StopNeedleView(
                            needleAngle = state.subState.needleAngle,
                            targetStart = state.subState.targetZoneStartAngle,
                            targetEnd = state.subState.targetZoneEndAngle,
                            onStop = { viewModel.onStopNeedle() }
                        )
                    }
                    MicroGameType.CUT_WIRE -> {
                        CutWireView(
                            wires = state.subState.wires,
                            targetColorName = state.subState.targetWireColorName,
                            onCut = { viewModel.onCutWire(it) }
                        )
                    }
                    MicroGameType.FIND_ODD_ONE -> {
                        FindOddOneView(
                            totalItems = state.subState.totalGridItems,
                            oddIndex = state.subState.oddIndex,
                            normalSym = state.subState.normalSymbol,
                            oddSym = state.subState.oddSymbol,
                            selectedIndex = state.subState.selectedIndex,
                            onSelect = { viewModel.onSelectOddItem(it) }
                        )
                    }
                }

                // Intermission Overlay
                if (state.isIntermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.75f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (state.microResult == MicroResult.SUCCESS) Color(0xFF065F46) else Color(0xFF7F1D1D)
                            ),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (state.microResult == MicroResult.SUCCESS) "AWESOME!" else "MISSED!",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (state.microResult == MicroResult.SUCCESS) "Next micro challenge coming..." else "-1 Life! Watch out!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // Defeat Dialog
            if (state.isStageOver) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "FRENZY OVER!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFEF4444),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "You ran out of lives during the gauntlet! Reflex speed is the key to surviving.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.restartLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("RETRY GAUNTLET", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("STAGE SELECT", color = Color(0xFFF472B6))
                        }
                    },
                    containerColor = Color(0xFF1E1035)
                )
            }

            // Victory Dialog
            if (state.isStageWon) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "STAGE CLEARED!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF22C55E),
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
                                        tint = if (index < state.stars) Color(0xFFFFD166) else Color(0xFF4C1D95),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Lightning reflexes! You conquered all ${state.totalMicroGames} micro-challenges with ${state.lives} lives remaining.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.nextLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("NEXT STAGE", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("STAGE SELECT", color = Color(0xFFF472B6))
                        }
                    },
                    containerColor = Color(0xFF1E1035)
                )
            }
        }
    }
}

// 1. TAP RUSH VIEW
@Composable
fun TapRushView(currentTaps: Int, requiredTaps: Int, onTap: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "$currentTaps / $requiredTaps",
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
            color = Color(0xFFFBBF24)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFEC4899), Color(0xFFBE185D))
                    )
                )
                .border(4.dp, Color(0xFFF472B6), CircleShape)
                .clickable { onTap() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "TAP!",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                color = Color.White
            )
        }
    }
}

// 2. CATCH FALLING VIEW
@Composable
fun CatchFallingView(bucketX: Float, gemX: Float, gemY: Float, onBucketMove: (Float) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    onBucketMove(change.position.x / size.width)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Falling Gem
            drawCircle(
                color = Color(0xFF38BDF8),
                radius = 18.dp.toPx(),
                center = Offset(gemX * w, gemY * h)
            )
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = Offset(gemX * w - 4.dp.toPx(), gemY * h - 4.dp.toPx())
            )

            // Bucket Basket at y = 0.85
            val bx = bucketX * w
            val by = 0.85f * h
            drawRoundRect(
                color = Color(0xFFF59E0B),
                topLeft = Offset(bx - 36.dp.toPx(), by),
                size = Size(72.dp.toPx(), 24.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )
        }
    }
}

// 3. POP BALLOONS VIEW
@Composable
fun PopBalloonsView(
    balloons: List<com.example.funfrenzy.core.model.BalloonItem>,
    poppedCount: Int,
    requiredPops: Int,
    onPop: (Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        for (b in balloons) {
            if (!b.isPopped) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = (b.x * 280).dp,
                            top = (b.y * 360).dp
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(b.color)
                            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            .clickable { onPop(b.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("POP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// 4. DODGE ROCKS VIEW
@Composable
fun DodgeRocksView(
    playerX: Float,
    rocks: List<com.example.funfrenzy.core.model.RockItem>,
    onPlayerMove: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    onPlayerMove(change.position.x / size.width)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Rocks
            for (r in rocks) {
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = 16.dp.toPx(),
                    center = Offset(r.x * w, r.y * h)
                )
            }

            // Player character at y = 0.85
            val px = playerX * w
            val py = 0.85f * h
            drawCircle(
                color = Color(0xFF22C55E),
                radius = 20.dp.toPx(),
                center = Offset(px, py)
            )
            // Eyes
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(px - 6.dp.toPx(), py - 4.dp.toPx())
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(px + 6.dp.toPx(), py - 4.dp.toPx())
            )
        }
    }
}

// 5. STOP NEEDLE VIEW
@Composable
fun StopNeedleView(needleAngle: Float, targetStart: Float, targetEnd: Float, onStop: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2 - 20.dp.toPx()

            // Dial Background Track
            drawCircle(
                color = Color(0xFF261447),
                radius = radius,
                style = Stroke(width = 24.dp.toPx())
            )

            // Target Arc in Green
            drawArc(
                color = Color(0xFF22C55E),
                startAngle = targetStart,
                sweepAngle = targetEnd - targetStart,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
            )

            // Rotating Needle
            val rad = Math.toRadians(needleAngle.toDouble())
            val needleEnd = Offset(
                (center.x + cos(rad) * (radius - 10.dp.toPx())).toFloat(),
                (center.y + sin(rad) * (radius - 10.dp.toPx())).toFloat()
            )
            drawLine(
                color = Color(0xFFEF4444),
                start = center,
                end = needleEnd,
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = Color.White, radius = 10.dp.toPx(), center = center)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStop,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.size(width = 160.dp, height = 50.dp)
        ) {
            Text("STOP!", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF0F240F))
        }
    }
}

// 6. CUT WIRE VIEW
@Composable
fun CutWireView(
    wires: List<com.example.funfrenzy.core.model.WireItem>,
    targetColorName: String,
    onCut: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "TARGET: $targetColorName WIRE",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))

        wires.forEach { wire ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(wire.color)
                    .clickable { onCut(wire.colorName) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SNIP ${wire.colorName}",
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// 7. FIND ODD ONE VIEW
@Composable
fun FindOddOneView(
    totalItems: Int,
    oddIndex: Int,
    normalSym: String,
    oddSym: String,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        for (row in 0 until 3) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                for (col in 0 until 3) {
                    val idx = row * 3 + col
                    val isOdd = idx == oddIndex
                    val symbol = if (isOdd) oddSym else normalSym

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF261447))
                            .border(2.dp, Color(0xFF6D28D9), RoundedCornerShape(16.dp))
                            .clickable { onSelect(idx) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = symbol,
                            fontSize = 32.sp
                        )
                    }
                }
            }
        }
    }
}
