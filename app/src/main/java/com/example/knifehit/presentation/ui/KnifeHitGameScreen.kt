package com.example.knifehit.presentation.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.knifehit.core.engine.KnifeHitEngine
import com.example.knifehit.core.model.ALL_KNIFE_SKINS
import com.example.knifehit.core.model.KnifeHitGameMode
import com.example.knifehit.presentation.KnifeHitViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnifeHitGameScreen(
    viewModel: KnifeHitViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val apples by viewModel.applesFlow.collectAsState()
    val equippedSkinId by viewModel.equippedSkinFlow.collectAsState()

    val currentSkin = remember(equippedSkinId) {
        ALL_KNIFE_SKINS.find { it.id == equippedSkinId } ?: ALL_KNIFE_SKINS.first()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (state.mode == KnifeHitGameMode.CAMPAIGN) {
                                if (state.targetType.isBoss) "BOSS: ${state.targetType.displayName}" else "Stage ${state.levelNumber}"
                            } else {
                                "Boss Rush: Stage ${state.levelNumber}"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (state.targetType.isBoss) Color(0xFFFFB703) else Color.White
                        )
                        Text(
                            state.targetType.displayName,
                            fontSize = 12.sp,
                            color = Color(0xFFA8A29E)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Surface(
                        color = Color(0xFFDC2626).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("🍎", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "$apples",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1917)
                )
            )
        },
        containerColor = Color(0xFF0C0A09)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTapGestures {
                        viewModel.onThrowKnife()
                    }
                }
        ) {
            // Left side Knives remaining indicator
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(state.totalKnivesThisStage) { idx ->
                    val isAvailable = idx < state.knivesRemaining
                    Icon(
                        imageVector = Icons.Default.Straighten,
                        contentDescription = null,
                        tint = if (isAvailable) currentSkin.bladeColor else Color(0xFF44403C),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Main Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height * 0.35f + state.targetRecoilY

                // Draw Target or Shards
                if (state.targetShards.isNotEmpty()) {
                    state.targetShards.forEach { shard ->
                        drawCircle(
                            color = shard.color.copy(alpha = shard.alpha),
                            radius = 20f,
                            center = Offset(cx + shard.x, cy + shard.y)
                        )
                    }
                } else {
                    // Draw Main Rotating Target
                    val r = KnifeHitEngine.TARGET_RADIUS
                    drawCircle(
                        color = state.targetType.primaryColor,
                        radius = r,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = state.targetType.secondaryColor,
                        radius = r * 0.75f,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = state.targetType.primaryColor,
                        radius = r * 0.25f,
                        center = Offset(cx, cy)
                    )

                    // Draw Attached Knives
                    state.attachedKnives.forEach { knife ->
                        val globalAngleDeg = (knife.angle + state.targetAngle) % 360f
                        val rad = Math.toRadians(globalAngleDeg.toDouble())
                        val knifeHiltX = cx + ((r + 40f) * cos(rad)).toFloat()
                        val knifeHiltY = cy + ((r + 40f) * sin(rad)).toFloat()
                        val knifeTipX = cx + (r * cos(rad)).toFloat()
                        val knifeTipY = cy + (r * sin(rad)).toFloat()

                        drawLine(
                            color = currentSkin.bladeColor,
                            start = Offset(knifeTipX, knifeTipY),
                            end = Offset(knifeHiltX, knifeHiltY),
                            strokeWidth = 10f
                        )
                    }

                    // Draw Apples
                    state.attachedApples.forEach { apple ->
                        if (!apple.isSliced) {
                            val globalAngleDeg = (apple.angle + state.targetAngle) % 360f
                            val rad = Math.toRadians(globalAngleDeg.toDouble())
                            val appleX = cx + ((r + 14f) * cos(rad)).toFloat()
                            val appleY = cy + ((r + 14f) * sin(rad)).toFloat()

                            drawCircle(color = Color(0xFFEF4444), radius = 14f, center = Offset(appleX, appleY))
                            drawCircle(color = Color(0xFF10B981), radius = 4f, center = Offset(appleX, appleY - 10f))
                        }
                    }
                }

                // Draw Flying Knives
                state.flyingKnives.forEach { fk ->
                    val kx = cx
                    val ky = fk.y
                    drawLine(
                        color = currentSkin.bladeColor,
                        start = Offset(kx, ky),
                        end = Offset(kx, ky + 45f),
                        strokeWidth = 10f
                    )
                }

                // Draw Bottom Ready Knife
                if (state.knivesRemaining > 0 && state.flyingKnives.isEmpty() && !state.isGameOver && !state.isStageWon) {
                    val kx = cx
                    val ky = size.height * 0.85f
                    drawLine(
                        color = currentSkin.bladeColor,
                        start = Offset(kx, ky),
                        end = Offset(kx, ky + 45f),
                        strokeWidth = 10f
                    )
                }
            }
        }
    }

    // Stage Won Dialog
    if (state.isStageWon) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF292524),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = if (state.targetType.isBoss) Icons.Default.MilitaryTech else Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (state.targetType.isBoss) "BOSS DEFEATED!" else "STAGE CLEARED!",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = Color(0xFF10B981)
                    )
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Apples: +${state.applesCollectedThisSession}",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = onNavigateBack, shape = RoundedCornerShape(10.dp)) {
                        Text("Menu", color = Color.White)
                    }
                    Button(
                        onClick = { viewModel.nextStage() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Next Stage", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // Game Over Dialog
    if (state.isGameOver) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF292524),
            title = {
                Text(
                    "KNIFE DEFLECTED!",
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text("Your knife hit an existing blade. Try again!", color = Color(0xFFA8A29E))
            },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = onNavigateBack, shape = RoundedCornerShape(10.dp)) {
                        Text("Menu", color = Color.White)
                    }
                    Button(
                        onClick = { viewModel.retryStage() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Retry", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}
