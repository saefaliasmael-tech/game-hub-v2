package com.example.game2048

import com.example.game2048.core.engine.Game2048Engine
import com.example.game2048.core.model.MoveDirection
import com.example.game2048.core.repository.Game2048Repository
import com.example.game2048.presentation.Game2048ViewModel
import com.example.watersort.core.database.AppDatabase
import com.example.watersort.core.database.GameProgressDao
import com.example.watersort.core.database.GameProgressEntity
import com.example.watersort.core.database.GameSaveDao
import com.example.watersort.core.database.GameSaveEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class Game2048ViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockProgressDao: GameProgressDao
    private lateinit var mockSaveDao: GameSaveDao
    private lateinit var repository: Game2048Repository
    private lateinit var viewModel: Game2048ViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val progressStorage = mutableMapOf<String, GameProgressEntity>()
        val saveStorage = mutableMapOf<String, GameSaveEntity>()

        val progressHandler = InvocationHandler { _, method, args ->
            when (method.name) {
                "getLevelProgress" -> progressStorage["${args[0]}_${args[1]}"]
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

        repository = Game2048Repository(mockSaveDao, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialGameCreation() = runTest(testDispatcher) {
        viewModel = Game2048ViewModel(repository, Game2048Engine())
        advanceUntilIdle()

        val state = viewModel.gameState.value
        assertEquals(16, state.board.size)
        val nonZeroCount = state.board.count { it > 0 }
        assertEquals(2, nonZeroCount)
        assertFalse(state.isGameOver)
    }

    @Test
    fun testMovesAndRestart() = runTest(testDispatcher) {
        viewModel = Game2048ViewModel(repository, Game2048Engine())
        advanceUntilIdle()

        // Perform moves
        viewModel.onMove(MoveDirection.DOWN)
        viewModel.onMove(MoveDirection.RIGHT)
        advanceUntilIdle()

        val stateAfterMoves = viewModel.gameState.value
        assertNotNull(stateAfterMoves)

        // Restart
        viewModel.startNewGame()
        advanceUntilIdle()

        val freshState = viewModel.gameState.value
        assertEquals(0, freshState.score)
        assertEquals(2, freshState.board.count { it > 0 })
    }
}
