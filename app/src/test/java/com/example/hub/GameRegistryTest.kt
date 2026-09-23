package com.example.hub

import com.example.hub.model.GameCategory
import com.example.hub.registry.GameRegistry
import org.junit.Assert.*
import org.junit.Test

class GameRegistryTest {

    @Test
    fun testWaterSortIsRegisteredAndAvailable() {
        val waterSort = GameRegistry.getGameById(GameRegistry.WATER_SORT_ID)
        assertNotNull(waterSort)
        assertEquals("Water Sort Puzzle", waterSort!!.name)
        assertTrue(waterSort.isAvailable)
        assertEquals(GameCategory.PUZZLE, waterSort.category)
    }

    @Test
    fun testNoDuplicateOrEmptyIds() {
        val allGames = GameRegistry.getAllGames()
        val ids = allGames.map { it.id }
        assertEquals("Duplicate IDs found in GameRegistry!", ids.size, ids.toSet().size)
        assertTrue(ids.all { it.isNotBlank() })
        assertTrue(allGames.all { it.name.isNotBlank() })
        assertTrue(allGames.all { it.entryRoute.isNotBlank() })
    }

    @Test
    fun testAvailableGamesHaveCorrectRoutes() {
        val waterSort = GameRegistry.getGameById(GameRegistry.WATER_SORT_ID)
        assertNotNull(waterSort)
        assertTrue(waterSort!!.isAvailable)
        assertEquals("watersort_home", waterSort.entryRoute)

        val colorSequence = GameRegistry.getGameById(GameRegistry.COLOR_SEQUENCE_ID)
        assertNotNull(colorSequence)
        assertTrue(colorSequence!!.isAvailable)
        assertEquals("colorsequence_home", colorSequence.entryRoute)

        val game2048 = GameRegistry.getGameById(GameRegistry.PUZZLE_2048_ID)
        assertNotNull(game2048)
        assertTrue(game2048!!.isAvailable)
        assertEquals("game2048_home", game2048.entryRoute)

        val happyGlass = GameRegistry.getGameById(GameRegistry.HAPPY_GLASS_ID)
        assertNotNull(happyGlass)
        assertTrue(happyGlass!!.isAvailable)
        assertEquals("happyglass_home", happyGlass.entryRoute)

        val mrBullet = GameRegistry.getGameById(GameRegistry.MR_BULLET_ID)
        assertNotNull(mrBullet)
        assertTrue(mrBullet!!.isAvailable)
        assertEquals("mrbullet_home", mrBullet.entryRoute)

        val protectSheep = GameRegistry.getGameById(GameRegistry.PROTECT_SHEEP_ID)
        assertNotNull(protectSheep)
        assertTrue(protectSheep!!.isAvailable)
        assertEquals("protectsheep_home", protectSheep.entryRoute)

        val funFrenzy = GameRegistry.getGameById(GameRegistry.FUN_FRENZY_ID)
        assertNotNull(funFrenzy)
        assertTrue(funFrenzy!!.isAvailable)
        assertEquals("funfrenzy_home", funFrenzy.entryRoute)
    }

    @Test
    fun testUpcomingGamesAreFlaggedAsComingSoon() {
        val ballSort = GameRegistry.getGameById(GameRegistry.BALL_SORT_ID)
        assertNotNull(ballSort)
        assertFalse(ballSort!!.isAvailable)
        assertTrue(ballSort.isComingSoon)

        val blockPuzzle = GameRegistry.getGameById(GameRegistry.BLOCK_PUZZLE_ID)
        assertNotNull(blockPuzzle)
        assertFalse(blockPuzzle!!.isAvailable)
        assertTrue(blockPuzzle.isComingSoon)
    }

    @Test
    fun testSearchFunctionality() {
        val searchWater = GameRegistry.searchGames("Water")
        assertTrue(searchWater.isNotEmpty())
        assertTrue(searchWater.any { it.id == GameRegistry.WATER_SORT_ID })

        val searchPuzzle = GameRegistry.searchGames("Puzzle")
        assertTrue(searchPuzzle.any { it.id == GameRegistry.WATER_SORT_ID })
        assertTrue(searchPuzzle.any { it.id == GameRegistry.BLOCK_PUZZLE_ID })
    }

    @Test
    fun testCategoryFiltering() {
        val puzzleGames = GameRegistry.getGamesByCategory(GameCategory.PUZZLE)
        assertTrue(puzzleGames.any { it.id == GameRegistry.WATER_SORT_ID })
        assertTrue(puzzleGames.any { it.id == GameRegistry.BALL_SORT_ID })

        val allGames = GameRegistry.getGamesByCategory(GameCategory.ALL)
        assertEquals(GameRegistry.getAllGames().size, allGames.size)
    }
}
