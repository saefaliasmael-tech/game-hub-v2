package com.example.watersort.ui.screens.dailychallenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.watersort.core.database.DailyChallengeEntity
import com.example.watersort.core.database.MissionEntity
import com.example.watersort.core.level.LevelGenerator
import com.example.watersort.core.model.GameState
import com.example.watersort.core.model.MoveResult
import com.example.watersort.core.repository.GameRepository
import com.example.watersort.ui.components.BottleView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyChallengeScreen(
    repository: GameRepository,
    onNavigateBack: () -> Unit
) {
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val scope = rememberCoroutineScope()
    val levelGenerator = remember { LevelGenerator() }
    val profile by repository.profileFlow.collectAsStateWithLifecycle(initialValue = null)
    val missions by repository.allMissionsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }
    var challengeEntity by remember { mutableStateOf<DailyChallengeEntity?>(null) }
    var gameState by remember { mutableStateOf<GameState?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedBottleIndex by remember { mutableStateOf<Int?>(null) }
    var tiltingBottleIndex by remember { mutableStateOf<Int?>(null) }
    var tiltAngle by remember { mutableStateOf(0f) }
    var rewardEarned by remember { mutableStateOf(0) }

    LaunchedEffect(today) {
        isLoading = true
        val entity = repository.getDailyChallenge(today)
        challengeEntity = entity

        if (!entity.isCompleted) {
            val midSave = repository.loadMidLevel(9999)
            if (midSave != null && midSave.first.bottles.isNotEmpty()) {
                gameState = midSave.first
            } else {
                val dailySeed = today.hashCode().toLong()
                val levelData = levelGenerator.generateLevel(levelId = 9999, seed = dailySeed)
                gameState = GameState(
                    levelNumber = 9999,
                    bottles = levelData.bottles,
                    maxCapacity = levelData.difficulty.capacity
                )
            }
        }
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("daily_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = stringResource(R.string.daily_challenge),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Surface(
                    shape = CircleShape,
                    color = Color(0x33FBBF24)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "🪙", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+100",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color(0xFFFDE047),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Navigation: Daily Puzzle vs Missions
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1E293B),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF38BDF8)
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Daily Puzzle",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        val claimableCount = missions.count { it.isCompleted && !it.isClaimed }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Missions",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 1) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                            )
                            if (claimableCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$claimableCount",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF38BDF8))
                    }
                } else if (challengeEntity?.isCompleted == true) {
                    // Completed State
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .padding(16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Today's Challenge Completed!",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Come back tomorrow for a brand new puzzle and reward.",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8))
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = onNavigateBack,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(stringResource(R.string.home))
                                }
                            }
                        }
                    }
                } else {
                    // Puzzle Active State
                    val state = gameState
                    if (state != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = today,
                                style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFF38BDF8))
                            )
                            Text(
                                text = stringResource(R.string.moves, state.movesCount),
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8))
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalArrangement = Arrangement.spacedBy(24.dp),
                                maxItemsInEachRow = 4
                            ) {
                                state.bottles.forEachIndexed { index, bottle ->
                                    val isSelected = selectedBottleIndex == index
                                    val isTilting = tiltingBottleIndex == index
                                    val tilt = if (isTilting) tiltAngle else 0f

                                    BottleView(
                                        bottle = bottle,
                                        isSelected = isSelected,
                                        colorBlindMode = profile?.colorBlindEnabled ?: false,
                                        skinId = profile?.equippedSkinId ?: "classic",
                                        tiltAngle = tilt,
                                        onBottleClick = {
                                            if (selectedBottleIndex == null) {
                                                if (!bottle.isEmpty && !bottle.isSolved) {
                                                    selectedBottleIndex = index
                                                }
                                            } else if (selectedBottleIndex == index) {
                                                selectedBottleIndex = null
                                            } else {
                                                val srcIdx = selectedBottleIndex!!
                                                val dstIdx = index
                                                val engine = com.example.watersort.core.engine.GameEngine()
                                                val res = engine.executePour(state, srcIdx, dstIdx)
                                                if (res is MoveResult.Success) {
                                                    scope.launch {
                                                        val tiltDir = if (dstIdx > srcIdx) 40f else -40f
                                                        tiltingBottleIndex = srcIdx
                                                        tiltAngle = tiltDir
                                                        selectedBottleIndex = null

                                                        delay(250)

                                                        tiltingBottleIndex = null
                                                        tiltAngle = 0f
                                                        gameState = res.newGameState
                                                        repository.saveMidLevel(res.newGameState, emptyList())

                                                        if (res.newGameState.isWon) {
                                                            repository.clearMidLevel(9999)
                                                            val reward = repository.completeDailyChallenge(
                                                                today,
                                                                stars = 3,
                                                                moves = res.newGameState.movesCount
                                                            )
                                                            rewardEarned = reward
                                                            challengeEntity = challengeEntity?.copy(isCompleted = true)
                                                        }
                                                    }
                                                } else {
                                                    selectedBottleIndex = null
                                                }
                                            }
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            } else {
                // MISSIONS TAB
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val dailyMissions = missions.filter { it.type == "DAILY" }
                    val weeklyMissions = missions.filter { it.type == "WEEKLY" }

                    Text(
                        text = "📅 DAILY MISSIONS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    dailyMissions.forEach { mission ->
                        MissionCard(
                            mission = mission,
                            onClaim = {
                                scope.launch { repository.claimMission(mission.id) }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "🏆 WEEKLY MISSIONS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFFFBBF24),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    weeklyMissions.forEach { mission ->
                        MissionCard(
                            mission = mission,
                            onClaim = {
                                scope.launch { repository.claimMission(mission.id) }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun MissionCard(
    mission: MissionEntity,
    onClaim: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mission_card_${mission.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mission.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = mission.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                when {
                    mission.isClaimed -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x3310B981)
                        ) {
                            Text(
                                text = "CLAIMED",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    mission.isCompleted -> {
                        Button(
                            onClick = onClaim,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("claim_mission_${mission.id}")
                        ) {
                            Text(text = "CLAIM", fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "+${mission.rewardCoins} 🪙",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFFDE047),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${mission.rewardXp} XP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF60A5FA),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val progressRatio = (mission.currentProgress.toFloat() / mission.target.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progressRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (mission.isCompleted) Color(0xFF10B981) else Color(0xFF0284C7),
                trackColor = Color(0xFF334155),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${mission.currentProgress} / ${mission.target}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                )
                Text(
                    text = "+${mission.rewardCoins} 🪙  +${mission.rewardXp} XP",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1))
                )
            }
        }
    }
}
