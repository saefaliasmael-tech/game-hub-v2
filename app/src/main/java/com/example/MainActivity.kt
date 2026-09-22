package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ads.AdManager
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
import com.example.happyglass.core.repository.HappyGlassRepository
import com.example.happyglass.presentation.HappyGlassViewModel
import com.example.happyglass.presentation.ui.HappyGlassGameScreen
import com.example.happyglass.presentation.ui.HappyGlassHomeScreen
import com.example.mrbullet.core.repository.MrBulletRepository
import com.example.mrbullet.presentation.MrBulletViewModel
import com.example.mrbullet.presentation.ui.MrBulletGameScreen
import com.example.mrbullet.presentation.ui.MrBulletHomeScreen
import com.example.protectsheep.core.repository.ProtectSheepRepository
import com.example.protectsheep.presentation.ProtectSheepViewModel
import com.example.protectsheep.presentation.ui.ProtectSheepGameScreen
import com.example.protectsheep.presentation.ui.ProtectSheepHomeScreen
import com.example.funfrenzy.core.repository.FunFrenzyRepository
import com.example.funfrenzy.presentation.FunFrenzyViewModel
import com.example.funfrenzy.presentation.ui.FunFrenzyGameScreen
import com.example.funfrenzy.presentation.ui.FunFrenzyHomeScreen
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
import com.example.hub.ui.screens.ZubaLubaSplashScreen
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
        AdManager.initialize(this)
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
        val happyGlassRepository = HappyGlassRepository(database, this)
        val mrBulletRepository = MrBulletRepository(database, this)
        val protectSheepRepository = ProtectSheepRepository(database, this)
        val funFrenzyRepository = FunFrenzyRepository(database, this)
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
                        happyGlassRepository = happyGlassRepository,
                        mrBulletRepository = mrBulletRepository,
                        protectSheepRepository = protectSheepRepository,
                        funFrenzyRepository = funFrenzyRepository,
                        soundManager = soundManager,
                        hapticManager = hapticManager,
                        gameViewModelProvider = { gameViewModel },
                        hubPreferences = hubPreferences
                    )
                }
            }
        }
    }
}

class GameViewModelHolder(
    private val colorSequenceRepository: ColorSequenceRepository,
    private val game2048Repository: Game2048Repository,
    private val mastermindRepository: MastermindRepository,
    private val unblockRepository: UnblockRepository,
    private val memoryRepository: MemoryRepository,
    private val stopTheTimeRepository: StopTheTimeRepository,
    private val colorSwitchRepository: ColorSwitchRepository,
    private val knifeHitRepository: KnifeHitRepository,
    private val aaRepository: AARepository,
    private val miniTDRepository: MiniTDRepository,
    private val ropeAroundRepository: RopeAroundRepository,
    private val ropeRescueRepository: RopeRescueRepository,
    private val picPuzzleRepository: PicPuzzleRepository,
    private val pullThePinRepository: PullThePinRepository,
    private val happyGlassRepository: HappyGlassRepository,
    private val mrBulletRepository: MrBulletRepository,
    private val protectSheepRepository: ProtectSheepRepository,
    private val funFrenzyRepository: FunFrenzyRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) {
    val colorSequenceViewModel by lazy { ColorSequenceViewModel(colorSequenceRepository) }
    val game2048ViewModel by lazy { Game2048ViewModel(game2048Repository) }
    val mastermindViewModel by lazy { MastermindViewModel(mastermindRepository, soundManager, hapticManager) }
    val unblockViewModel by lazy { UnblockViewModel(unblockRepository, soundManager, hapticManager) }
    val memoryViewModel by lazy { MemoryViewModel(memoryRepository, soundManager, hapticManager) }
    val stopTheTimeViewModel by lazy { StopTheTimeViewModel(stopTheTimeRepository, soundManager, hapticManager) }
    val colorSwitchViewModel by lazy { ColorSwitchViewModel(colorSwitchRepository, soundManager, hapticManager) }
    val knifeHitViewModel by lazy { KnifeHitViewModel(knifeHitRepository, soundManager, hapticManager) }
    val aaViewModel by lazy { AAViewModel(aaRepository, soundManager, hapticManager) }
    val miniTDViewModel by lazy { MiniTDViewModel(miniTDRepository, soundManager, hapticManager) }
    val ropeAroundViewModel by lazy { RopeAroundViewModel(ropeAroundRepository, soundManager, hapticManager) }
    val ropeRescueViewModel by lazy { RopeRescueViewModel(ropeRescueRepository, soundManager, hapticManager) }
    val picPuzzleViewModel by lazy { PicPuzzleViewModel(picPuzzleRepository, soundManager, hapticManager) }
    val pullThePinViewModel by lazy { PullThePinViewModel(pullThePinRepository, soundManager, hapticManager) }
    val happyGlassViewModel by lazy { HappyGlassViewModel(happyGlassRepository, soundManager, hapticManager) }
    val mrBulletViewModel by lazy { MrBulletViewModel(mrBulletRepository, soundManager, hapticManager) }
    val protectSheepViewModel by lazy { ProtectSheepViewModel(protectSheepRepository, soundManager, hapticManager) }
    val funFrenzyViewModel by lazy { FunFrenzyViewModel(funFrenzyRepository, soundManager, hapticManager) }
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
    happyGlassRepository: HappyGlassRepository,
    mrBulletRepository: MrBulletRepository,
    protectSheepRepository: ProtectSheepRepository,
    funFrenzyRepository: FunFrenzyRepository,
    soundManager: SoundManager,
    hapticManager: HapticManager,
    gameViewModelProvider: () -> GameViewModel,
    hubPreferences: HubPreferences
) {
    val navController = rememberNavController()

    // Lazy ViewModels holder - ensures no heavy game loops or database operations run at startup
    val viewModelHolder = remember {
        GameViewModelHolder(
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
            happyGlassRepository = happyGlassRepository,
            mrBulletRepository = mrBulletRepository,
            protectSheepRepository = protectSheepRepository,
            funFrenzyRepository = funFrenzyRepository,
            soundManager = soundManager,
            hapticManager = hapticManager
        )
    }

    val launchGame: (String) -> Unit = { gameId ->
        val game = GameRegistry.getGameById(gameId)
        if (game != null && game.isAvailable) {
            navController.navigate(game.entryRoute)
        } else {
            navController.navigate("game_details/$gameId")
        }
    }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // --- SPLASH SCREEN ---
        composable("splash") {
            ZubaLubaSplashScreen(
                onSplashFinished = {
                    navController.navigate("hub_home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // --- GAME HUB SCREENS ---
        composable("hub_home") {
            HubHomeScreen(
                hubPreferences = hubPreferences,
                onNavigateToGameDetails = { gameId ->
                    navController.navigate("game_details/$gameId")
                },
                onLaunchGame = launchGame,
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
                onLaunchGame = launchGame
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
            val vm = viewModelHolder.mastermindViewModel
            MastermindHomeScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartCampaignLevel = { levelNum ->
                    vm.startCampaignLevel(levelNum)
                    navController.navigate("mastermind_game")
                },
                onStartQuickPlay = { difficulty ->
                    vm.startQuickPlay(difficulty)
                    navController.navigate("mastermind_game")
                },
                onStartDailyChallenge = {
                    vm.startDailyChallenge()
                    navController.navigate("mastermind_game")
                }
            )
        }

        composable("mastermind_game") {
            MastermindGameScreen(
                viewModel = viewModelHolder.mastermindViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- UNBLOCK ME GAME SCREENS ---
        composable("unblock_home") {
            val vm = viewModelHolder.unblockViewModel
            UnblockHomeScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    vm.startLevel(levelNum)
                    navController.navigate("unblock_game/$levelNum")
                }
            )
        }

        composable(
            route = "unblock_game/{levelNum}",
            arguments = listOf(navArgument("levelNum") { type = NavType.IntType })
        ) {
            UnblockGameScreen(
                viewModel = viewModelHolder.unblockViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- MEMORY CARDS GAME SCREENS ---
        composable("memory_home") {
            val vm = viewModelHolder.memoryViewModel
            MemoryHomeScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartGame = { boardSize, mode, theme ->
                    vm.startNewGame(boardSize, mode, theme)
                    navController.navigate("memory_game")
                }
            )
        }

        composable("memory_game") {
            MemoryGameScreen(
                viewModel = viewModelHolder.memoryViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- COLOR SEQUENCE GAME SCREENS ---
        composable("colorsequence_home") {
            ColorSequenceHomeScreen(
                viewModel = viewModelHolder.colorSequenceViewModel,
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
                viewModel = viewModelHolder.colorSequenceViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- 2048 GAME SCREENS ---
        composable("game2048_home") {
            Game2048HomeScreen(
                viewModel = viewModelHolder.game2048ViewModel,
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
                viewModel = viewModelHolder.game2048ViewModel,
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
                viewModel = gameViewModelProvider(),
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
            val vm = viewModelHolder.stopTheTimeViewModel
            StopTheTimeHomeScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartCampaignLevel = { levelNum ->
                    vm.startCampaignLevel(levelNum)
                    navController.navigate("stoptime_game")
                },
                onStartCustomGame = { mode, difficulty, customTarget, playerCount ->
                    vm.startCustomGame(mode, difficulty, customTarget, playerCount)
                    navController.navigate("stoptime_game")
                }
            )
        }

        composable("stoptime_game") {
            StopTheTimeGameScreen(
                viewModel = viewModelHolder.stopTheTimeViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- COLOR SWITCH SCREENS ---
        composable("colorswitch_home") {
            val vm = viewModelHolder.colorSwitchViewModel
            ColorSwitchHomeScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartCampaignLevel = { levelNum ->
                    vm.startCampaignLevel(levelNum)
                    navController.navigate("colorswitch_game")
                },
                onStartEndless = {
                    vm.startEndlessMode()
                    navController.navigate("colorswitch_game")
                }
            )
        }

        composable("colorswitch_game") {
            ColorSwitchGameScreen(
                viewModel = viewModelHolder.colorSwitchViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- KNIFE HIT SCREENS ---
        composable("knifehit_home") {
            val vm = viewModelHolder.knifeHitViewModel
            KnifeHitHomeScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartCampaignLevel = { stage ->
                    vm.startCampaignLevel(stage)
                    navController.navigate("knifehit_game")
                },
                onStartBossRush = { stage ->
                    vm.startBossRushStage(stage)
                    navController.navigate("knifehit_game")
                }
            )
        }

        composable("knifehit_game") {
            KnifeHitGameScreen(
                viewModel = viewModelHolder.knifeHitViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- AA SCREENS ---
        composable("aa_home") {
            val vm = viewModelHolder.aaViewModel
            AAHomeScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    vm.startLevel(levelNum)
                    navController.navigate("aa_game")
                }
            )
        }

        composable("aa_game") {
            AAGameScreen(
                viewModel = viewModelHolder.aaViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- MINI TOWER DEFENSE SCREENS ---
        composable("minitd_home") {
            val vm = viewModelHolder.miniTDViewModel
            MiniTDHomeScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    vm.startLevel(levelNum)
                    navController.navigate("minitd_game")
                }
            )
        }

        composable("minitd_game") {
            MiniTDGameScreen(
                viewModel = viewModelHolder.miniTDViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- ROPE AROUND SCREENS ---
        composable("ropearound_home") {
            val vm = viewModelHolder.ropeAroundViewModel
            RopeAroundHomeScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    vm.startLevel(levelNum)
                    navController.navigate("ropearound_game")
                }
            )
        }

        composable("ropearound_game") {
            RopeAroundGameScreen(
                viewModel = viewModelHolder.ropeAroundViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- ROPE RESCUE SCREENS ---
        composable("roperescue_home") {
            val vm = viewModelHolder.ropeRescueViewModel
            RopeRescueHomeScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    vm.startLevel(levelNum)
                    navController.navigate("roperescue_game")
                }
            )
        }

        composable("roperescue_game") {
            RopeRescueGameScreen(
                viewModel = viewModelHolder.ropeRescueViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- PIC PUZZLE SCREENS ---
        composable("picpuzzle_home") {
            val vm = viewModelHolder.picPuzzleViewModel
            PicPuzzleHomeScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    vm.startLevel(levelNum)
                    navController.navigate("picpuzzle_game")
                }
            )
        }

        composable("picpuzzle_game") {
            PicPuzzleGameScreen(
                viewModel = viewModelHolder.picPuzzleViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- PULL THE PIN SCREENS ---
        composable("pullthepin_home") {
            val vm = viewModelHolder.pullThePinViewModel
            PullThePinHomeScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    vm.startLevel(levelNum)
                    navController.navigate("pullthepin_game")
                }
            )
        }

        composable("pullthepin_game") {
            PullThePinGameScreen(
                viewModel = viewModelHolder.pullThePinViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- HAPPY GLASS SCREENS ---
        composable("happyglass_home") {
            val vm = viewModelHolder.happyGlassViewModel
            HappyGlassHomeScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    vm.startLevel(levelNum)
                    navController.navigate("happyglass_game")
                }
            )
        }

        composable("happyglass_game") {
            HappyGlassGameScreen(
                viewModel = viewModelHolder.happyGlassViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- MR BULLET SCREENS ---
        composable("mrbullet_home") {
            val vm = viewModelHolder.mrBulletViewModel
            MrBulletHomeScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    vm.startLevel(levelNum)
                    navController.navigate("mrbullet_game")
                }
            )
        }

        composable("mrbullet_game") {
            MrBulletGameScreen(
                viewModel = viewModelHolder.mrBulletViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- PROTECT SHEEP SCREENS ---
        composable("protectsheep_home") {
            val vm = viewModelHolder.protectSheepViewModel
            ProtectSheepHomeScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    vm.startLevel(levelNum)
                    navController.navigate("protectsheep_game")
                }
            )
        }

        composable("protectsheep_game") {
            ProtectSheepGameScreen(
                viewModel = viewModelHolder.protectSheepViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- FUN FRENZY RESCUE SCREENS ---
        composable("funfrenzy_home") {
            val vm = viewModelHolder.funFrenzyViewModel
            FunFrenzyHomeScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelNum ->
                    vm.startLevel(levelNum)
                    navController.navigate("funfrenzy_game")
                }
            )
        }

        composable("funfrenzy_game") {
            FunFrenzyGameScreen(
                viewModel = viewModelHolder.funFrenzyViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
