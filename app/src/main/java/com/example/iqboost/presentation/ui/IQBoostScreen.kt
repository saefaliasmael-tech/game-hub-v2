package com.example.iqboost.presentation.ui

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iqboost.model.MiniGameType
import com.example.iqboost.model.ReflexPhase
import com.example.iqboost.presentation.IQBoostViewModel
import com.zubaluba.gamehub.ads.AdBanner
import com.zubaluba.gamehub.ads.UnifiedAdManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IQBoostScreen(
    viewModel: IQBoostViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isGameFinished) {
        if (state.isGameFinished && activity != null) {
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
                            text = "IQ Boost",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF72EFDD),
                            fontSize = 18.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ROUND ${state.roundIndex}/5",
                                color = Color(0xFF4EA8DE),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "IQ: ${100 + state.score / 5}",
                                color = Color(0xFFFFD166),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("iqboost_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.restart() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E38))
            )
        },
        bottomBar = {
            AdBanner(modifier = Modifier.fillMaxWidth().navigationBarsPadding())
        },
        containerColor = Color(0xFF121224)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Round Progress
            LinearProgressIndicator(
                progress = { (state.roundIndex.toFloat() / state.totalRounds).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF72EFDD),
                trackColor = Color(0xFF2C2C4D)
            )

            // Dynamic Mini Game Screen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E1E38))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (state.currentMiniGame) {
                    MiniGameType.MEMORY_MATRIX -> {
                        MemoryMatrixView(
                            isShowingPattern = state.isMatrixShowingPattern,
                            highlighted = state.matrixHighlighted,
                            selected = state.matrixSelected,
                            onCellClicked = { viewModel.onMatrixCellClicked(it) }
                        )
                    }
                    MiniGameType.SPEED_MATH -> {
                        state.mathQuestion?.let { q ->
                            SpeedMathView(question = q, onSelect = { viewModel.onMathOptionSelected(it) })
                        }
                    }
                    MiniGameType.STROOP_COLOR -> {
                        state.stroopQuestion?.let { q ->
                            StroopColorView(question = q, onAnswer = { viewModel.onStroopAnswer(it) })
                        }
                    }
                    MiniGameType.NUMBER_PATTERN -> {
                        state.patternQuestion?.let { q ->
                            NumberPatternView(question = q, onSelect = { viewModel.onPatternOptionSelected(it) })
                        }
                    }
                    MiniGameType.REFLEX_SPEED -> {
                        ReflexSpeedView(
                            phase = state.reflexState,
                            elapsedMs = state.reflexTimeMs,
                            onTap = { viewModel.onReflexTapped() }
                        )
                    }
                }
            }
        }
    }

    // Game Finished IQ Summary Dialog
    if (state.isGameFinished) {
        val estimatedIQ = 100 + (state.score / 5)
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("BRAIN ASSESSMENT COMPLETE! 🧠", fontWeight = FontWeight.Bold, color = Color(0xFF72EFDD))
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Your Estimated Brain Index:", color = Color(0xFFB0BEC5), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$estimatedIQ IQ",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD166)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when {
                            estimatedIQ >= 140 -> "Genius Tier Performance! 🌟"
                            estimatedIQ >= 125 -> "Superior Cognitive Speed! ⚡"
                            else -> "Above Average Sharpness! 👍"
                        },
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.restart() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF72EFDD))
                ) {
                    Text("Train Again", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            containerColor = Color(0xFF1E1E38)
        )
    }
}

@Composable
private fun MemoryMatrixView(
    isShowingPattern: Boolean,
    highlighted: Set<Int>,
    selected: Set<Int>,
    onCellClicked: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (isShowingPattern) "MEMORIZE THE PATTERN! 🧠" else "TAP THE TILES YOU SAW! 👆",
            color = if (isShowingPattern) Color(0xFFFFD166) else Color(0xFF72EFDD),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        for (row in 0 until 4) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (col in 0 until 4) {
                    val idx = row * 4 + col
                    val isLit = if (isShowingPattern) highlighted.contains(idx) else selected.contains(idx)

                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isLit) Color(0xFF00F5D4) else Color(0xFF2C2C54))
                            .clickable(enabled = !isShowingPattern) { onCellClicked(idx) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun SpeedMathView(
    question: com.example.iqboost.model.SpeedMathQuestion,
    onSelect: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("CALCULATION SPEED ⚡", color = Color(0xFF4EA8DE), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(question.equation, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(32.dp))
        for (row in 0..1) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (col in 0..1) {
                    val idx = row * 2 + col
                    Button(
                        onClick = { onSelect(idx) },
                        modifier = Modifier.size(width = 120.dp, height = 60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C54)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("${question.options[idx]}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StroopColorView(
    question: com.example.iqboost.model.StroopQuestion,
    onAnswer: (Boolean) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("COLOR MATCH TEST 🎨", color = Color(0xFFFFD166), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Does the meaning match the text color?", color = Color(0xFFB0BEC5), fontSize = 13.sp)
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = question.wordText,
            color = question.colorValue,
            fontSize = 44.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Button(
                onClick = { onAnswer(false) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                modifier = Modifier.size(width = 120.dp, height = 54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("NO ❌", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Button(
                onClick = { onAnswer(true) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                modifier = Modifier.size(width = 120.dp, height = 54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("YES ✔️", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun NumberPatternView(
    question: com.example.iqboost.model.NumberPatternQuestion,
    onSelect: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("LOGICAL SEQUENCE 🔢", color = Color(0xFF72EFDD), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(question.sequence, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        for (row in 0..1) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (col in 0..1) {
                    val idx = row * 2 + col
                    Button(
                        onClick = { onSelect(idx) },
                        modifier = Modifier.size(width = 120.dp, height = 58.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C54)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("${question.options[idx]}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReflexSpeedView(
    phase: ReflexPhase,
    elapsedMs: Long,
    onTap: () -> Unit
) {
    val bgColor = when (phase) {
        ReflexPhase.WAITING -> Color(0xFFE53935)
        ReflexPhase.READY_TO_TAP -> Color(0xFF00E676)
        ReflexPhase.RESULT -> Color(0xFF2979FF)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable { onTap() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (phase) {
                ReflexPhase.WAITING -> {
                    Text("WAIT FOR GREEN...", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                ReflexPhase.READY_TO_TAP -> {
                    Text("TAP NOW! 💥", color = Color.Black, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                }
                ReflexPhase.RESULT -> {
                    Text("${elapsedMs} ms", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Reaction Time", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
            }
        }
    }
}
