package com.example.roperescue.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roperescue.core.engine.RopeRescueEngine
import com.example.roperescue.presentation.RopeRescueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RopeRescueGameScreen(
    viewModel: RopeRescueViewModel,
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
                        // Saved / Required
                        Text(
                            "Saved: ${state.savedCount}/${state.requiredSaved} (Lost: ${state.lostCount})",
                            color = if (state.savedCount >= state.requiredSaved) Color(0xFF10B981) else Color(0xFF38BDF8),
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
            // Interactive Dragging and Rendering Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            viewModel.onDragRope(change.position.x / size.width, change.position.y / size.height)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // 1. Draw Start and Target Platforms
                val startPx = Offset(state.startPos.x * w, state.startPos.y * h)
                val targetPx = Offset(state.targetPos.x * w, state.targetPos.y * h)

                // Start platform
                drawCircle(color = Color(0xFF10B981).copy(alpha = 0.2f), radius = 28.dp.toPx(), center = startPx)
                drawCircle(color = Color(0xFF10B981), radius = 20.dp.toPx(), center = startPx)

                // Target platform
                drawCircle(color = Color(0xFF38BDF8).copy(alpha = 0.2f), radius = 28.dp.toPx(), center = targetPx)
                drawCircle(color = Color(0xFF38BDF8), radius = 20.dp.toPx(), center = targetPx)

                // 2. Draw Pulleys / Wheels
                for (wheel in state.wheels) {
                    val wx = wheel.xPercent * w
                    val wy = wheel.yPercent * h
                    val wr = wheel.radiusPercent * w

                    drawCircle(color = Color(0xFF334155), radius = wr, center = Offset(wx, wy))
                    drawCircle(color = Color(0xFF94A3B8), radius = wr, center = Offset(wx, wy), style = Stroke(width = 3.dp.toPx()))
                    drawCircle(color = Color(0xFF64748B), radius = wr * 0.4f, center = Offset(wx, wy))
                }

                // 3. Draw Hazard Saws
                for (hazard in state.hazards) {
                    val hx = hazard.xPercent * w
                    val hy = hazard.yPercent * h
                    val hr = hazard.radiusPercent * w

                    rotate(hazard.rotation, pivot = Offset(hx, hy)) {
                        // Saw teeth
                        val teethCount = 8
                        val toothPath = Path()
                        for (i in 0 until teethCount) {
                            val angle1 = Math.toRadians((i * (360.0 / teethCount)).toDouble())
                            val angle2 = Math.toRadians((i * (360.0 / teethCount) + (180.0 / teethCount)).toDouble())
                            val rOuter = hr
                            val rInner = hr * 0.7f
                            val p1 = Offset((hx + rOuter * kotlin.math.cos(angle1)).toFloat(), (hy + rOuter * kotlin.math.sin(angle1)).toFloat())
                            val p2 = Offset((hx + rInner * kotlin.math.cos(angle2)).toFloat(), (hy + rInner * kotlin.math.sin(angle2)).toFloat())
                            if (i == 0) toothPath.moveTo(p1.x, p1.y) else toothPath.lineTo(p1.x, p1.y)
                            toothPath.lineTo(p2.x, p2.y)
                        }
                        toothPath.close()
                        drawPath(toothPath, color = Color(0xFFEF4444))
                        drawCircle(color = Color(0xFF7F1D1D), radius = hr * 0.35f, center = Offset(hx, hy))
                    }
                }

                // 4. Draw Rescue Cable
                val fullPoints = state.ropePivots + if (state.isRopeAttached) state.targetPos else state.currentRopeEnd
                if (fullPoints.size >= 2) {
                    val cablePath = Path()
                    cablePath.moveTo(fullPoints[0].x * w, fullPoints[0].y * h)
                    for (i in 1 until fullPoints.size) {
                        cablePath.lineTo(fullPoints[i].x * w, fullPoints[i].y * h)
                    }

                    drawPath(
                        path = cablePath,
                        color = if (state.isRopeAttached) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                // 5. Draw Ziplining Characters
                if (state.isRopeAttached) {
                    for (zipliner in state.zipliners) {
                        if (zipliner.isAlive && !zipliner.isRescued) {
                            val pos = RopeRescueEngine.getPointAlongPolyline(fullPoints, zipliner.progress)
                            val zx = pos.x * w
                            val zy = pos.y * h

                            // Hostage body
                            drawCircle(color = Color(0xFFFBBF24), radius = 7.dp.toPx(), center = Offset(zx, zy))
                            // Handle bar
                            drawLine(
                                color = Color.White,
                                start = Offset(zx, zy - 7.dp.toPx()),
                                end = Offset(zx, zy),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                }
            }

            // Bottom Action Bar
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!state.isRopeAttached) {
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            "Drag the cable around pulleys into the cyan safe zone",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    }
                } else if (state.remainingAtStart > 0) {
                    Button(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        viewModel.startDeploying()
                                        tryAwaitRelease()
                                        viewModel.stopDeploying()
                                    }
                                )
                            },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "HOLD TO SEND HOSTAGES (${state.remainingAtStart} Waiting)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Defeat Dialog
            if (state.isGameOver) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "MISSION FAILED!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFEF4444),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "Too many casualties occurred along the line (${state.lostCount} lost). We needed at least ${state.requiredSaved} survivors!",
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
                            Text("RETRY MISSION", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("OPERATION MAP", color = Color(0xFF94A3B8))
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
                            text = "EVACUATION COMPLETE!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF10B981),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "Outstanding rescue operation! ${state.savedCount} hostages safely reached the evacuation point.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.nextLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("NEXT OPERATION", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("OPERATIONS MENU", color = Color(0xFF94A3B8))
                        }
                    },
                    containerColor = Color(0xFF1E293B)
                )
            }
        }
    }
}
