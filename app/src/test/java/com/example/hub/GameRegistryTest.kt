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
    }

    @Test
    fun testZubaLubaGameCount() {
        val allGames = GameRegistry.getAllGames()
        assertEquals(19, allGames.size)
        assertTrue(allGames.all { it.isAvailable })
    }

    @Test
    fun testSearchFunctionality() {
        val searchWater = GameRegistry.searchGames("Water")
        assertTrue(searchWater.any { it.id == GameRegistry.WATER_SORT_ID })

        val searchPuzzle = GameRegistry.searchGames("Puzzle")
        assertTrue(searchPuzzle.any { it.id == GameRegistry.WATER_SORT_ID })
    }

    @Test
    fun testCategoryFiltering() {
        val puzzleGames = GameRegistry.getGamesByCategory(GameCategory.PUZZLE)
        assertTrue(puzzleGames.any { it.id == GameRegistry.WATER_SORT_ID })

        val allGames = GameRegistry.getGamesByCategory(GameCategory.ALL)
        assertEquals(GameRegistry.getAllGames().size, allGames.size)
    }
}
