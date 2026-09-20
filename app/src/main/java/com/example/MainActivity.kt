package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aa.core.repository.AARepository
import com.example.aa.presentation.AAViewModel
import com.example.aa.presentation.ui.AAGameScreen
import com.example.aa.presentation.ui.AAHomeScreen
import com.example.minitd.core.repository.MiniTDRepository
import com.example.minitd.presentation.MiniTDViewModel
import com.example.minitd.presentation.ui.MiniTDGameScreen
import com.example.minitd.presentation.ui.MiniTDHomeScreen
import com.example.ropearound.core.repository.RopeAroundRepository
import com.example.ropearound.presentation.RopeAroundViewModel
import com.example.ropearound.presentation.ui.RopeAroundGameScreen
import com.example.ropearound.presentation.ui.RopeAroundHomeScreen
import com.example.roperescue.core.repository.RopeRescueRepository
import com.example.roperescue.presentation.RopeRescueViewModel
import com.example.roperescue.presentation.ui.RopeRescueGameScreen
import com.example.roperescue.presentation.ui.RopeRescueHomeScreen
import com.example.picpuzzle.core.repository.PicPuzzleRepository
import com.example.picpuzzle.presentation.PicPuzzleViewModel
import com.example.picpuzzle.presentation.ui.PicPuzzleGameScreen
import com.example.picpuzzle.presentation.ui.PicPuzzleHomeScreen
import com.example.pullthepin.core.repository.PullThePinRepository
import com.example.pullthepin.presentation.PullThePinViewModel
import com.example.pullthepin.presentation.ui.PullThePinGameScreen
import com.example.pullthepin.presentation.ui.PullThePinHomeScreen
import com.example.colorsequence.core.repository.ColorSequenceRepository
import com.example.colorsequence.presentation.ColorSequenceViewModel
import com.example.colorsequence.presentation.ui.ColorSequenceGameScreen
import com.example.colorsequence.presentation.ui.ColorSequenceHomeScreen
import com.example.colorswitch.core.repository.ColorSwitchRepository
import com.example.colorswitch.presentation.ColorSwitchViewModel
import com.example.colorswitch.presentation.ui.ColorSwitchGameScreen
import com.example.colorswitch.presentation.ui.ColorSwitchHomeScreen
import com.example.game2048.core.repository.Game2048Repository
import com.example.game2048.presentation.Game2048ViewModel
import com.example.game2048.presentation.ui.Game2048GameScreen
import com.example.game2048.presentation.ui.Game2048HomeScreen
import com.example.hub.data.HubPreferences
import com.example.hub.registry.GameRegistry
import com.example.hub.ui.screens.GameDetailsScreen
import com.example.hub.ui.screens.HubHomeScreen
import com.example.hub.ui.screens.HubSettingsScreen
import com.example.knifehit.core.repository.KnifeHitRepository
import com.example.knifehit.presentation.KnifeHitViewModel
import com.example.knifehit.presentation.ui.KnifeHitGameScreen
import com.example.knifehit.presentation.ui.KnifeHitHomeScreen
import com.example.mastermind.core.model.MastermindDifficulty
import com.example.mastermind.core.model.MastermindGameMode
import com.example.mastermind.core.repository.MastermindRepository
import com.example.mastermind.presentation.MastermindViewModel
import com.example.mastermind.presentation.ui.MastermindGameScreen
import com.example.mastermind.presentation.ui.MastermindHomeScreen
import com.example.memory.core.repository.MemoryRepository
import com.example.memory.presentation.MemoryViewModel
import com.example.memory.presentation.ui.MemoryGameScreen
import com.example.memory.presentation.ui.MemoryHomeScreen
import com.example.stopthetime.core.repository.StopTheTimeRepository
import com.example.stopthetime.presentation.StopTheTimeViewModel
import com.example.stopthetime.presentation.ui.StopTheTimeGameScreen
import com.example.stopthetime.presentation.ui.StopTheTimeHomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.unblockme.core.repository.UnblockRepository
import com.example.unblockme.presentation.UnblockViewModel
import com.example.unblockme.presentation.ui.UnblockGameScreen
import com.example.unblockme.presentation.ui.UnblockHomeScreen
import com.example.watersort.core.audio.HapticManager
import com.example.watersort.core.audio.SoundManager
import com.example.watersort.core.database.AppDatabase
import com.example.watersort.core.repository.GameRepository
import com.example.watersort.ui.screens.dailychallenge.DailyChallengeScreen
import com.example.watersort.ui.screens.game.GameScreen
import com.example.watersort.ui.screens.game.GameViewModel
import com.example.watersort.ui.screens.home.HomeScreen
import com.example.watersort.ui.screens.profile.ProfileScreen
import com.example.watersort.ui.screens.settings.SettingsScreen
import com.example.watersort.ui.screens.shop.ShopScreen
import com.example.watersort.ui.screens.worlds.WorldsScreen

class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = AppDatabase.getDatabase(this)
        val repository = GameRepository(database)
        val colorSequenceRepository = ColorSequenceRepository(database)
        val game2048Repository = Game2048Repository(database)
        val mastermindRepository = MastermindRepository(database, this)
        val unblockRepository = UnblockRepository(database, this)
        val memoryRepository = MemoryRepository(database, this)
        val stopTheTimeRepository = StopTheTimeRepository(database, this)
        val colorSwitchRepository = ColorSwitchRepository(database, this)
        val knifeHitRepository = KnifeHitRepository(database, this)
        val aaRepository = AARepository(database, this)
        val miniTDRepository = MiniTDRepository(database, this)
        val ropeAroundRepository = RopeAroundRepository(database, this)
        val ropeRescueRepository = RopeRescueRepository(database, this)
        val picPuzzleRepository = PicPuzzleRepository(database, this)
        val pullThePinRepository = PullThePinRepository(database, this)
        val soundManager = SoundManager(this)
        val hapticManager = HapticManager(this)
        val hubPreferences = HubPreferences.getInstance(this)

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GameHubApp(
                        repository = repository,
                        colorSequenceRepository = colorSequenceRepository,
                        game2048Repository = game2048Repository,
                        mastermindRepository = mastermindRepository,
                        unblockRepository = unblockRepository,
                        memoryRepository = memoryRepository,
                        stopTheTimeRepository = stopTheTimeRepository,
                        colorSwitchRepository = colorSwitchRepository,
                        knifeHitRepository = knifeHitRepository,
                        aaRepository = aaRepository,
                        miniTDRepository = miniTDRepository,
                        ropeAroundRepository = ropeAroundRepository,
                        ropeRescueRepository = ropeRescueRepository,
                        picPuzzleRepository = picPuzzleRepository,
                        pullThePinRepository = pullThePinRepository,
                        soundManager = soundManager,
                        hapticManager = hapticManager,
                        gameViewModel = gameViewModel,
                        hubPreferences = hubPreferences
                    )
                }
            }
        }
    }
}

@Composable
fun GameHubApp(
    repository: GameRepository,
    colorSequenceRepository: ColorSequenceRepository,
    game2048Repository: Game2048Repository,
    mastermindRepository: MastermindRepository,
    unblockRepository: UnblockRepository,
    memoryRepository: MemoryRepository,
    stopTheTimeRepository: StopTheTimeRepository,
    colorSwitchRepository: ColorSwitchRepository,
    knifeHitRepository: KnifeHitRepository,
    aaRepository: AARepository,
    miniTDRepository: MiniTDRepository,
    ropeAroundRepository: RopeAroundRepository,
    ropeRescueRepository: RopeRescueRepository,
    picPuzzleRepository: PicPuzzleRepository,
    pullThePinRepository: PullThePinRepository,
    soundManager: SoundManager,
    hapticManager: HapticManager,
    gameViewModel: GameViewModel,
    hubPreferences: HubPreferences
) {
    val navController = rememberNavController()

    // Scoped ViewModels for all games
    val colorSequenceViewModel: ColorSequenceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ColorSequenceViewModel.Factory(colorSequenceRepository)
    )
    val game2048ViewModel: Game2048ViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = Game2048ViewModel.Factory(game2048Repository)
    )
    val mastermindViewModel: MastermindViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = MastermindViewModel.Factory(mastermindRepository, soundManager, hapticManager)
    )
    val unblockViewModel: UnblockViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = UnblockViewModel.Factory(unblockRepository, soundManager, hapticManager)
    )
    val memoryViewModel: MemoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = MemoryViewModel.Factory(memoryRepository, soundManager, hapticManager)
    )
    val stopTheTimeViewModel: StopTheTimeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = StopTheTimeViewModel.Factory(stopTheTimeRepository, soundManager, hapticManager)
    )
    val colorSwitchViewModel: ColorSwitchViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ColorSwitchViewModel.Factory(colorSwitchRepository, soundManager, hapticManager)
    )
    val knifeHitViewModel: KnifeHitViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = KnifeHitViewModel.Factory(knifeHitRepository, soundManager, hapticManager)
    )
    val aaViewModel: AAViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = AAViewModel.Factory(aaRepository, soundManager, hapticManager)
    )
    val miniTDViewModel: MiniTDViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = MiniTDViewModel.Factory(miniTDRepository, soundManager, hapticManager)
    )
    val ropeAroundViewModel: RopeAroundViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = RopeAroundViewModel.Factory(ropeAroundRepository, soundManager, hapticManager)
    )
    val ropeRescueViewModel: RopeRescueViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = RopeRescueViewModel.Factory(ropeRescueRepository, soundManager, hapticManager)
    )
    val picPuzzleViewModel: PicPuzzleViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = PicPuzzleViewModel.Factory(picPuzzleRepository, soundManager, hapticManager)
    )
    val pullThePinViewModel: PullThePinViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = PullThePinViewModel.Factory(pullThePinRepository, soundManager, hapticManager)
    )

    NavHost(
        navController = navController,
        startDestination = "hub_home"
    ) {
        // --- GAME HUB SCREENS ---
        composable("hub_home") {
            HubHomeScreen(
                hubPreferences = hubPreferences,
                onNavigateToGameDetails = { gameId ->
                    navController.navigate("game_details/$gameId")
                },
                onLaunchGame = { gameId ->
                    when (gameId) {
                        GameRegistry.WATER_SORT_ID -> {
                            navController.navigate("watersort_home")
                        }
                        GameRegistry.COLOR_SEQUENCE_ID -> {
                            navController.navigate("colorsequence_home")
                        }
                        GameRegistry.PUZZLE_2048_ID -> {
                            navController.navigate("game2048_home")
                        }
                        GameRegistry.MASTERMIND_ID -> {
                            navController.navigate("mastermind_home")
                        }
                        GameRegistry.UNBLOCK_ME_ID -> {
                            navController.navigate("unblock_home")
                        }
                        GameRegistry.MEMORY_CARDS_ID -> {
                            navController.navigate("memory_home")
                        }
                        GameRegistry.STOP_THE_TIME_ID -> {
                            navController.navigate("stoptime_home")
                        }
                        GameRegistry.COLOR_SWITCH_ID -> {
                            navController.navigate("colorswitch_home")
                        }
                        GameRegistry.KNIFE_HIT_ID -> {
                            navController.navigate("knifehit_home")
                        }
                        GameRegistry.AA_ID -> {
                            navController.navigate("aa_home")
                        }
                        GameRegistry.MINI_TD_ID -> {
                            navController.navigate("minitd_home")
                        }
                        GameRegistry.ROPE_AROUND_ID -> {
                            navController.navigate("ropearound_home")
                        }
                        GameRegistry.ROPE_RESCUE_ID -> {
                            navController.navigate("roperescue_home")
                        }
                        GameRegistry.PIC_PUZZLE_ID -> {
                            navController.navigate("picpuzzle_home")
                        }
                        GameRegistry.PULL_THE_PIN_ID -> {
                            navController.navigate("pullthepin_home")
                        }
                        else -> {
                            navController.navigate("game_details/$gameId")
                        }
                    }
                },
                onNavigateToSettings = {
                    navController.navigate("hub_settings")
                }
            )
        }

        composable(
            route = "game_details/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: GameRegistry.WATER_SORT_ID
            GameDetailsScreen(
                gameId = gameId,
                hubPreferences = hubPreferences,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLaunchGame = { targetGameId ->
                    when (targetGameId) {
                        GameRegistry.WATER_SORT_ID -> navController.navigate("watersort_home")
                        GameRegistry.COLOR_SEQUENCE_ID -> navController.navigate("colorsequence_home")
                        GameRegistry.PUZZLE_2048_ID -> navController.navigate("game2048_home")
                        GameRegistry.MASTERMIND_ID -> navController.navigate("mastermind_home")
                        GameRegistry.UNBLOCK_ME_ID -> navController.navigate("unblock_home")
                        GameRegistry.MEMORY_CARDS_ID -> navController.navigate("memory_home")
                        GameRegistry.STOP_THE_TIME_ID -> navController.navigate("stoptime_home")
                        GameRegistry.COLOR_SWITCH_ID -> navController.navigate("colorswitch_home")
                        GameRegistry.KNIFE_HIT_ID -> navController.navigate("knifehit_home")
                        GameRegistry.AA_ID -> navController.navigate("aa_home")
                        GameRegistry.MINI_TD_ID -> navController.navigate("minitd_home")
                        GameRegistry.ROPE_AROUND_ID -> navController.navigate("ropearound_home")
                        GameRegistry.ROPE_RESCUE_ID -> navController.navigate("roperescue_home")
                        GameRegistry.PIC_PUZZLE_ID -> navController.navigate("picpuzzle_home")
                        GameRegistry.PULL_THE_PIN_ID -> navController.navigate("pullthepin_home")
                    }
                }
            )
        }

        composable("hub_settings") {
            HubSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- MASTERMIND GAME SCREENS ---
        composable("mastermind_home") {
            MastermindHomeScreen(
                viewModel = mastermindViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartCampaignLevel = { levelNum ->
                    mastermindViewModel.startCampaignLevel(levelNum)
                    navController.navigate("mastermind_game")
                },
                onStartQuickPlay = { difficulty ->
                    mastermindViewModel.startQuickPlay(difficulty)
                    navController.navigate("mastermind_game")
                },
                onStartDailyChallenge = {
                    mastermindViewModel.startDailyChallenge()
                    navController.navigate("mastermind_game")
                }
            )
        }

        composable("mastermind_game") {
            MastermindGameScreen(
                viewModel = mastermindViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- UNBLOCK ME GAME SCREENS ---
        composable("unblock_home") {
            UnblockHomeScreen(
                viewModel = unblockViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    unblockViewModel.startLevel(levelNum)
                    navController.navigate("unblock_game/$levelNum")
                }
            )
        }

        composable(
            route = "unblock_game/{levelNum}",
            arguments = listOf(navArgument("levelNum") { type = NavType.IntType })
        ) {
            UnblockGameScreen(
                viewModel = unblockViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- MEMORY CARDS GAME SCREENS ---
        composable("memory_home") {
            MemoryHomeScreen(
                viewModel = memoryViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartGame = { boardSize, mode, theme ->
                    memoryViewModel.startNewGame(boardSize, mode, theme)
                    navController.navigate("memory_game")
                }
            )
        }

        composable("memory_game") {
            MemoryGameScreen(
                viewModel = memoryViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- COLOR SEQUENCE GAME SCREENS ---
        composable("colorsequence_home") {
            ColorSequenceHomeScreen(
                viewModel = colorSequenceViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelId ->
                    navController.navigate("colorsequence_game/$levelId")
                }
            )
        }

        composable(
            route = "colorsequence_game/{levelId}",
            arguments = listOf(navArgument("levelId") { type = NavType.IntType })
        ) {
            ColorSequenceGameScreen(
                viewModel = colorSequenceViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- 2048 GAME SCREENS ---
        composable("game2048_home") {
            Game2048HomeScreen(
                viewModel = game2048ViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartGame = {
                    navController.navigate("game2048_game")
                }
            )
        }

        composable("game2048_game") {
            Game2048GameScreen(
                viewModel = game2048ViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- WATER SORT GAME SCREENS (COMPLETELY PRESERVED & INDEPENDENT) ---
        composable("watersort_home") {
            HomeScreen(
                repository = repository,
                onNavigateToGame = { levelId ->
                    navController.navigate("game/$levelId")
                },
                onNavigateToWorlds = {
                    navController.navigate("worlds")
                },
                onNavigateToDailyChallenge = {
                    navController.navigate("daily_challenge")
                },
                onNavigateToShop = {
                    navController.navigate("shop")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateBackToHub = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "game/{levelId}",
            arguments = listOf(navArgument("levelId") { type = NavType.IntType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getInt("levelId") ?: 1
            GameScreen(
                levelId = levelId,
                viewModel = gameViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateNextLevel = { nextLevelId ->
                    navController.navigate("game/$nextLevelId") {
                        popUpTo("watersort_home")
                    }
                }
            )
        }

        composable("worlds") {
            WorldsScreen(
                repository = repository,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSelectLevel = { levelId ->
                    navController.navigate("game/$levelId")
                }
            )
        }

        composable("daily_challenge") {
            DailyChallengeScreen(
                repository = repository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("shop") {
            ShopScreen(
                repository = repository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                repository = repository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                repository = repository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- STOP THE TIME SCREENS ---
        composable("stoptime_home") {
            StopTheTimeHomeScreen(
                viewModel = stopTheTimeViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartCampaignLevel = { levelNum ->
                    stopTheTimeViewModel.startCampaignLevel(levelNum)
                    navController.navigate("stoptime_game")
                },
                onStartCustomGame = { mode, difficulty, customTarget, playerCount ->
                    stopTheTimeViewModel.startCustomGame(mode, difficulty, customTarget, playerCount)
                    navController.navigate("stoptime_game")
                }
            )
        }

        composable("stoptime_game") {
            StopTheTimeGameScreen(
                viewModel = stopTheTimeViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- COLOR SWITCH SCREENS ---
        composable("colorswitch_home") {
            ColorSwitchHomeScreen(
                viewModel = colorSwitchViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartCampaignLevel = { levelNum ->
                    colorSwitchViewModel.startCampaignLevel(levelNum)
                    navController.navigate("colorswitch_game")
                },
                onStartEndless = {
                    colorSwitchViewModel.startEndlessMode()
                    navController.navigate("colorswitch_game")
                }
            )
        }

        composable("colorswitch_game") {
            ColorSwitchGameScreen(
                viewModel = colorSwitchViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- KNIFE HIT SCREENS ---
        composable("knifehit_home") {
            KnifeHitHomeScreen(
                viewModel = knifeHitViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartCampaignLevel = { stage ->
                    knifeHitViewModel.startCampaignLevel(stage)
                    navController.navigate("knifehit_game")
                },
                onStartBossRush = { stage ->
                    knifeHitViewModel.startBossRushStage(stage)
                    navController.navigate("knifehit_game")
                }
            )
        }

        composable("knifehit_game") {
            KnifeHitGameScreen(
                viewModel = knifeHitViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- AA SCREENS ---
        composable("aa_home") {
            AAHomeScreen(
                viewModel = aaViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    aaViewModel.startLevel(levelNum)
                    navController.navigate("aa_game")
                }
            )
        }

        composable("aa_game") {
            AAGameScreen(
                viewModel = aaViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- MINI TOWER DEFENSE SCREENS ---
        composable("minitd_home") {
            MiniTDHomeScreen(
                viewModel = miniTDViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    miniTDViewModel.startLevel(levelNum)
                    navController.navigate("minitd_game")
                }
            )
        }

        composable("minitd_game") {
            MiniTDGameScreen(
                viewModel = miniTDViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- ROPE AROUND SCREENS ---
        composable("ropearound_home") {
            RopeAroundHomeScreen(
                viewModel = ropeAroundViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    ropeAroundViewModel.startLevel(levelNum)
                    navController.navigate("ropearound_game")
                }
            )
        }

        composable("ropearound_game") {
            RopeAroundGameScreen(
                viewModel = ropeAroundViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- ROPE RESCUE SCREENS ---
        composable("roperescue_home") {
            RopeRescueHomeScreen(
                viewModel = ropeRescueViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    ropeRescueViewModel.startLevel(levelNum)
                    navController.navigate("roperescue_game")
                }
            )
        }

        composable("roperescue_game") {
            RopeRescueGameScreen(
                viewModel = ropeRescueViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- PIC PUZZLE SCREENS ---
        composable("picpuzzle_home") {
            PicPuzzleHomeScreen(
                viewModel = picPuzzleViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    picPuzzleViewModel.startLevel(levelNum)
                    navController.navigate("picpuzzle_game")
                }
            )
        }

        composable("picpuzzle_game") {
            PicPuzzleGameScreen(
                viewModel = picPuzzleViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- PULL THE PIN SCREENS ---
        composable("pullthepin_home") {
            PullThePinHomeScreen(
                viewModel = pullThePinViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    pullThePinViewModel.startLevel(levelNum)
                    navController.navigate("pullthepin_game")
                }
            )
        }

        composable("pullthepin_game") {
            PullThePinGameScreen(
                viewModel = pullThePinViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
