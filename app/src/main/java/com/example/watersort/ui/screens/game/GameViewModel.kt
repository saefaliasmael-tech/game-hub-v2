package com.example.watersort.ui.screens.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.watersort.core.audio.GameSound
import com.example.watersort.core.audio.HapticManager
import com.example.watersort.core.audio.SoundManager
import com.example.watersort.core.database.AppDatabase
import com.example.watersort.core.economy.EconomyConfig
import com.example.watersort.core.economy.TransactionType
import com.example.watersort.core.engine.GameEngine
import com.example.watersort.core.level.LevelData
import com.example.watersort.core.level.LevelGenerator
import com.example.watersort.core.model.GameState
import com.example.watersort.core.model.LiquidColor
import com.example.watersort.core.model.MoveResult
import com.example.watersort.core.model.PourMove
import com.example.watersort.core.repository.GameRepository
import com.example.watersort.core.solver.SolverResult
import com.example.watersort.core.solver.WaterSortSolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import android.app.Activity
import com.example.R
import com.example.watersort.core.model.InvalidReason
import com.zubaluba.gamehub.ads.NetworkUtils
import com.zubaluba.gamehub.ads.UnifiedAdManager

enum class AdRewardState {
    AVAILABLE,
    LOADING,
    SHOWING,
    CLAIMED
}

data class GameUiState(
    val isLoading: Boolean = true,
    val levelData: LevelData? = null,
    val gameState: GameState? = null,
    val selectedBottleIndex: Int? = null,
    val tiltingBottleIndex: Int? = null,
    val tiltAngle: Float = 0f,
    val shakingBottleIndex: Int? = null,
    val pouringSourceIndex: Int? = null,
    val pouringTargetIndex: Int? = null,
    val pouringColor: LiquidColor? = null,
    val isPouringActive: Boolean = false,
    val hintMove: PourMove? = null,
    val showRestartDialog: Boolean = false,
    val showStuckDialog: Boolean = false,
    val showWinDialog: Boolean = false,
    val isPerfectRun: Boolean = false,
    val coinsEarnedOnWin: Int = 0,
    val userMessage: String? = null,
    val coins: Int = 0,
    val colorBlindMode: Boolean = false,
    val skinId: String = "classic",
    val themeId: String = "classic",
    val showTutorial: Boolean = false,
    val adRewardState: AdRewardState = AdRewardState.AVAILABLE
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = GameRepository(database)
    private val engine = GameEngine()
    private val levelGenerator = LevelGenerator()
    private val solver = WaterSortSolver()

    val soundManager = SoundManager(application)
    val hapticManager = HapticManager(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var usedUndoInLevel = false
    private var hadInvalidMovesInLevel = false
    private var isTutorialCompleted = false
    private val moveMutex = Mutex()
    @Volatile private var isMoveInProgress = false

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
            repository.profileFlow.collect { profile ->
                profile?.let { p ->
                    soundManager.soundEnabled = p.soundEnabled
                    soundManager.musicEnabled = p.musicEnabled
                    hapticManager.hapticsEnabled = p.hapticEnabled
                    isTutorialCompleted = p.tutorialCompleted
                    _uiState.update {
                        it.copy(
                            coins = p.coins,
                            colorBlindMode = p.colorBlindEnabled,
                            skinId = p.equippedSkinId,
                            themeId = p.equippedThemeId
                        )
                    }
                }
            }
        }
    }

    fun loadLevel(levelId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, hintMove = null, showWinDialog = false, adRewardState = AdRewardState.AVAILABLE) }
            usedUndoInLevel = false
            hadInvalidMovesInLevel = false
            engine.clearUndo()

            val level = levelGenerator.generateLevel(levelId)

            // Check mid-level saved state
            val savedMid = repository.loadMidLevel(levelId)
            val initialGameState = if (savedMid != null) {
                engine.restoreUndo(savedMid.second)
                savedMid.first.copy(
                    levelNumber = levelId,
                    optimalMoves = level.optimalMoves,
                    isDeadlocked = engine.isDeadlocked(savedMid.first.bottles)
                )
            } else {
                GameState(
                    levelNumber = levelId,
                    bottles = level.bottles,
                    maxCapacity = level.difficulty.capacity,
                    optimalMoves = level.optimalMoves
                )
            }

            val shouldShowTutorial = !isTutorialCompleted && levelId == 1

            _uiState.update {
                it.copy(
                    isLoading = false,
                    levelData = level,
                    gameState = initialGameState,
                    selectedBottleIndex = null,
                    tiltingBottleIndex = null,
                    tiltAngle = 0f,
                    showStuckDialog = initialGameState.isDeadlocked,
                    showTutorial = shouldShowTutorial
                )
            }
        }
    }

    fun onBottleClicked(clickedIndex: Int) {
        if (isMoveInProgress || _uiState.value.isPouringActive || _uiState.value.tiltingBottleIndex != null) return
        val current = _uiState.value.gameState ?: return
        if (current.isWon) return

        val selected = _uiState.value.selectedBottleIndex

        if (selected == null) {
            // First tap: select source
            val bottle = current.bottles.getOrNull(clickedIndex) ?: return
            if (bottle.isEmpty) {
                hapticManager.tap()
                val message = getApplication<android.app.Application>().getString(R.string.invalid_move_source_empty)
                _uiState.update { it.copy(userMessage = message) }
                return
            }
            if (bottle.isSolved) {
                // Already completed
                hapticManager.tap()
                val message = getApplication<android.app.Application>().getString(R.string.invalid_move_already_solved)
                _uiState.update { it.copy(userMessage = message) }
                return
            }

            soundManager.play(GameSound.SELECT)
            hapticManager.tap()
            _uiState.update {
                it.copy(
                    selectedBottleIndex = clickedIndex,
                    hintMove = null,
                    userMessage = null
                )
            }
        } else if (selected == clickedIndex) {
            // Deselect
            soundManager.play(GameSound.BUTTON)
            _uiState.update { it.copy(selectedBottleIndex = null, userMessage = null) }
        } else {
            // Second tap: attempt pour from 'selected' into 'clickedIndex'
            val sourceIndex = selected
            val targetIndex = clickedIndex

            val result = engine.executePour(current, sourceIndex, targetIndex)
            when (result) {
                is MoveResult.Invalid -> {
                    hadInvalidMovesInLevel = true
                    soundManager.play(GameSound.INVALID)
                    hapticManager.error()
                    val msgRes = when (result.reason) {
                        InvalidReason.SAME_BOTTLE -> R.string.invalid_move_same_bottle
                        InvalidReason.SOURCE_EMPTY -> R.string.invalid_move_source_empty
                        InvalidReason.TARGET_FULL -> R.string.invalid_move_target_full
                        InvalidReason.COLOR_MISMATCH -> R.string.invalid_move_color_mismatch
                        InvalidReason.BOTTLE_ALREADY_SOLVED -> R.string.invalid_move_already_solved
                        else -> R.string.invalid_move_general
                    }
                    val feedbackText = getApplication<android.app.Application>().getString(msgRes)
                    viewModelScope.launch {
                        repository.recordMove(isInvalid = true)
                        _uiState.update {
                            it.copy(
                                selectedBottleIndex = null,
                                shakingBottleIndex = clickedIndex,
                                userMessage = feedbackText
                            )
                        }
                        delay(220)
                        _uiState.update { it.copy(shakingBottleIndex = null) }
                    }
                }
                is MoveResult.Success -> {
                    viewModelScope.launch {
                        if (!moveMutex.tryLock()) return@launch
                        isMoveInProgress = true
                        try {
                            repository.recordMove(isInvalid = false)
                            // Step 1: Tilt source toward target + start stream
                            val tiltDirection = if (targetIndex > sourceIndex) 42f else -42f
                            _uiState.update {
                                it.copy(
                                    tiltingBottleIndex = sourceIndex,
                                    tiltAngle = tiltDirection,
                                    pouringSourceIndex = sourceIndex,
                                    pouringTargetIndex = targetIndex,
                                    pouringColor = result.delta.color,
                                    isPouringActive = true,
                                    selectedBottleIndex = null,
                                    userMessage = null
                                )
                            }
                            soundManager.play(GameSound.POUR)
                            hapticManager.tap()

                            // Step 2: Liquid stream flow duration
                            delay(350)

                            // Step 3: Target fills and source updates
                            val newState = result.newGameState
                            _uiState.update {
                                it.copy(
                                    gameState = newState,
                                    pouringSourceIndex = null,
                                    pouringTargetIndex = null,
                                    pouringColor = null,
                                    isPouringActive = false
                                )
                            }

                            // Step 4: Settle & return source bottle
                            delay(160)
                            _uiState.update {
                                it.copy(
                                    tiltingBottleIndex = null,
                                    tiltAngle = 0f,
                                    showStuckDialog = newState.isDeadlocked && !newState.isWon
                                )
                            }

                            // Atomic mid-level save
                            repository.saveMidLevel(newState, engine.getUndoDeltas())

                            if (newState.isWon) {
                                handleWin(newState)
                            }
                        } finally {
                            isMoveInProgress = false
                            moveMutex.unlock()
                        }
                    }
                }
            }
        }
    }

    private suspend fun handleWin(state: GameState) {
        soundManager.play(GameSound.WIN)
        hapticManager.success()

        val level = _uiState.value.levelData
        val moves = state.movesCount
        val optimal = level?.optimalMoves ?: 10

        val stars = when {
            moves <= (level?.threeStarTargetMoves ?: (optimal + 2)) -> 3
            moves <= (level?.twoStarTargetMoves ?: (optimal + 6)) -> 2
            else -> 1
        }

        val isPerfect = !usedUndoInLevel && !hadInvalidMovesInLevel && stars == 3

        val coinsReward = repository.saveLevelCompletion(
            levelId = state.levelNumber,
            starsEarned = stars,
            movesCount = moves,
            withoutUndo = !usedUndoInLevel,
            withoutInvalidMoves = !hadInvalidMovesInLevel
        )

        _uiState.update {
            it.copy(
                showWinDialog = true,
                isPerfectRun = isPerfect,
                coinsEarnedOnWin = coinsReward,
                gameState = state.copy(starsEarned = stars),
                adRewardState = AdRewardState.AVAILABLE
            )
        }
    }

    fun onUndoClicked() {
        val current = _uiState.value.gameState ?: return
        if (!engine.canUndo()) return

        viewModelScope.launch {
            if (!repository.spendCoins(EconomyConfig.COST_UNDO, TransactionType.SPEND_UNDO, "Level ${current.levelNumber}")) {
                _uiState.update { it.copy(userMessage = "Not enough coins (${EconomyConfig.COST_UNDO} needed)") }
                return@launch
            }

            usedUndoInLevel = true
            repository.recordPowerup("UNDO")
            val undone = engine.undo(current)
            if (undone != null) {
                soundManager.play(GameSound.BUTTON)
                hapticManager.tap()
                repository.saveMidLevel(undone, engine.getUndoDeltas())
                _uiState.update {
                    it.copy(
                        gameState = undone,
                        selectedBottleIndex = null,
                        hintMove = null,
                        showStuckDialog = false
                    )
                }
            }
        }
    }

    fun onHintClicked() {
        val current = _uiState.value.gameState ?: return
        if (current.isWon) return

        viewModelScope.launch(Dispatchers.Default) {
            if (!repository.spendCoins(EconomyConfig.COST_HINT, TransactionType.SPEND_HINT, "Level ${current.levelNumber}")) {
                _uiState.update { it.copy(userMessage = "Not enough coins (${EconomyConfig.COST_HINT} needed)") }
                return@launch
            }

            soundManager.play(GameSound.BUTTON)
            repository.recordPowerup("HINT")
            val solveResult = solver.solve(current.bottles, current.maxCapacity)
            if (solveResult is SolverResult.Solved && solveResult.moves.isNotEmpty()) {
                val nextMove = solveResult.moves.first()
                _uiState.update {
                    it.copy(
                        hintMove = nextMove,
                        selectedBottleIndex = nextMove.fromBottleIndex
                    )
                }
                soundManager.play(GameSound.SELECT)
                hapticManager.tap()
            } else {
                _uiState.update { it.copy(userMessage = "No hint available for current state. Try Undo or Restart!") }
            }
        }
    }

    fun onExtraBottleClicked() {
        val current = _uiState.value.gameState ?: return
        if (current.hasExtraBottle) {
            _uiState.update { it.copy(userMessage = "Extra bottle already added!") }
            return
        }

        viewModelScope.launch {
            if (!repository.spendCoins(EconomyConfig.COST_EXTRA_BOTTLE, TransactionType.SPEND_EXTRA_BOTTLE, "Level ${current.levelNumber}")) {
                _uiState.update { it.copy(userMessage = "Not enough coins (${EconomyConfig.COST_EXTRA_BOTTLE} needed)") }
                return@launch
            }

            repository.recordPowerup("EXTRA_BOTTLE")
            soundManager.play(GameSound.COIN)
            hapticManager.success()

            val stateWithExtra = engine.addExtraBottle(current)
            repository.saveMidLevel(stateWithExtra, engine.getUndoDeltas())
            _uiState.update {
                it.copy(
                    gameState = stateWithExtra,
                    showStuckDialog = false
                )
            }
        }
    }

    fun dismissTutorial() {
        viewModelScope.launch {
            repository.setTutorialCompleted()
            isTutorialCompleted = true
            _uiState.update { it.copy(showTutorial = false) }
        }
    }

    fun onRestartClicked() {
        _uiState.update { it.copy(showRestartDialog = true) }
    }

    fun confirmRestart() {
        val current = _uiState.value.gameState ?: return
        viewModelScope.launch {
            repository.clearMidLevel(current.levelNumber)
            loadLevel(current.levelNumber)
            _uiState.update { it.copy(showRestartDialog = false, showStuckDialog = false) }
        }
    }

    fun dismissRestart() {
        _uiState.update { it.copy(showRestartDialog = false) }
    }

    fun dismissStuck() {
        _uiState.update { it.copy(showStuckDialog = false) }
    }

    fun dismissUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun claimRewardedAd(activity: Activity) {
        val currentState = _uiState.value.adRewardState
        if (currentState != AdRewardState.AVAILABLE) {
            // Already claimed or in-progress - prevent rapid clicks / duplicate awards
            return
        }

        // Check if offline first
        if (!NetworkUtils.isOnline(activity)) {
            _uiState.update { it.copy(userMessage = activity.getString(R.string.ad_offline)) }
            return
        }

        val adManager = UnifiedAdManager.getInstance(activity)
        if (!adManager.isRewardedAdReady()) {
            _uiState.update { it.copy(userMessage = activity.getString(R.string.ad_not_available)) }
            adManager.preloadRewarded()
            return
        }

        // Mark as SHOWING to lock out rapid clicks
        _uiState.update { it.copy(adRewardState = AdRewardState.SHOWING) }

        adManager.showRewarded(
            activity = activity,
            onUserEarnedReward = { _ ->
                viewModelScope.launch {
                    val currentLevel = _uiState.value.gameState?.levelNumber ?: 1
                    repository.addCoins(50, TransactionType.REWARD_ACHIEVEMENT, "Water Sort Rewarded Ad L$currentLevel")
                    soundManager.play(GameSound.WIN)
                    hapticManager.success()
                    _uiState.update {
                        it.copy(
                            adRewardState = AdRewardState.CLAIMED,
                            userMessage = activity.getString(R.string.ad_reward_claimed)
                        )
                    }
                }
            },
            onAdClosed = {
                // If ad closed without earning reward, revert to AVAILABLE if not claimed
                _uiState.update { current ->
                    if (current.adRewardState != AdRewardState.CLAIMED) {
                        current.copy(adRewardState = AdRewardState.AVAILABLE)
                    } else {
                        current
                    }
                }
            }
        )
    }

    fun canUndo(): Boolean = engine.canUndo()
}
