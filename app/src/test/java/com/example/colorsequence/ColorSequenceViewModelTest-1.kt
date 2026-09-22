package com.example.colorsequence

import com.example.colorsequence.core.engine.ColorSequenceEngine
import com.example.colorsequence.core.model.GamePhase
import com.example.colorsequence.core.model.SequenceColor
import com.example.colorsequence.core.repository.ColorSequenceRepository
import com.example.colorsequence.presentation.ColorSequenceViewModel
import com.example.watersort.core.database.AppDatabase
import com.example.watersort.core.database.GameProgressDao
import com.example.watersort.core.database.GameProgressEntity
import com.example.watersort.core.database.GameSaveDao
import com.example.watersort.core.database.GameSaveEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class ColorSequenceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockProgressDao: GameProgressDao
    private lateinit var mockSaveDao: GameSaveDao
    private lateinit var repository: ColorSequenceRepository
    private lateinit var viewModel: ColorSequenceViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val progressStorage = mutableMapOf<String, GameProgressEntity>()
        val saveStorage = mutableMapOf<String, GameSaveEntity>()

        val progressHandler = InvocationHandler { _, method, args ->
            when (method.name) {
                "getProgressForGame" -> flowOf(progressStorage.values.toList())
                "getTotalStarsForGame" -> flowOf(progressStorage.values.sumOf { it.stars })
                "getCompletedLevelsCount" -> flowOf(progressStorage.values.count { it.isCompleted })
                "getLevelProgress" -> {
                    val gameId = args[0] as String
                    val levelId = args[1] as Int
                    progressStorage["${gameId}_${levelId}"]
                }
                "saveProgress" -> {
                    val p = args[0] as GameProgressEntity
                    progressStorage["${p.gameId}_${p.levelId}"] = p
                    null
                }
                else -> null
            }
        }

        val saveHandler = InvocationHandler { _, method, args ->
            when (method.name) {
                "getSave" -> saveStorage[args[0] as String]
                "saveState" -> {
                    val s = args[0] as GameSaveEntity
                    saveStorage[s.gameId] = s
                    null
                }
                "deleteSave" -> {
                    saveStorage.remove(args[0] as String)
                    null
                }
                else -> null
            }
        }

        mockProgressDao = Proxy.newProxyInstance(
            GameProgressDao::class.java.classLoader,
            arrayOf(GameProgressDao::class.java),
            progressHandler
        ) as GameProgressDao

        mockSaveDao = Proxy.newProxyInstance(
            GameSaveDao::class.java.classLoader,
            arrayOf(GameSaveDao::class.java),
            saveHandler
        ) as GameSaveDao

        repository = ColorSequenceRepository(mockProgressDao, mockSaveDao)
        viewModel = ColorSequenceViewModel(repository, ColorSequenceEngine())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testStartLevelAndPlaybackTransition() = runTest(testDispatcher) {
        viewModel.startLevel(1)
        val state = viewModel.gameState.value
        assertNotNull(state)
        assertEquals(1, state!!.levelNumber)
        assertEquals(GamePhase.SHOWING_SEQUENCE, state.phase)

        // Advance coroutines through sequence presentation
        advanceUntilIdle()

        val readyState = viewModel.gameState.value
        assertNotNull(readyState)
        assertEquals(GamePhase.AWAITING_INPUT, readyState!!.phase)
        assertEquals(0, readyState.playerInput.size)
    }

    @Test
    fun testWinningSequenceRecordsProgress() = runTest(testDispatcher) {
        viewModel.startLevel(1)
        advanceUntilIdle()

        val state = viewModel.gameState.value!!
        for (targetColor in state.targetSequence) {
            viewModel.onPlayerColorTap(targetColor)
        }
        advanceUntilIdle()

        val wonState = viewModel.gameState.value!!
        assertEquals(GamePhase.LEVEL_WON, wonState.phase)
        assertTrue(wonState.starsAwarded > 0)
    }

    @Test
    fun testColorBlindModeToggle() {
        val initial = viewModel.isColorBlindMode.value
        viewModel.toggleColorBlindMode()
        assertEquals(!initial, viewModel.isColorBlindMode.value)
        viewModel.toggleColorBlindMode()
        assertEquals(initial, viewModel.isColorBlindMode.value)
    }
}
