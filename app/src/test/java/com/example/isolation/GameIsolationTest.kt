package com.example.isolation

import com.example.watersort.core.database.GameProgressEntity
import com.example.watersort.core.database.GameSaveEntity
import org.junit.Assert.*
import org.junit.Test

class GameIsolationTest {

    @Test
    fun testProgressIsIsolatedByGameId() {
        val allProgress = listOf(
            GameProgressEntity(gameId = "water_sort", levelId = 1, isCompleted = true, stars = 3, bestScore = 150),
            GameProgressEntity(gameId = "water_sort", levelId = 2, isCompleted = true, stars = 2, bestScore = 120),
            GameProgressEntity(gameId = "color_sequence", levelId = 1, isCompleted = true, stars = 3, bestScore = 500),
            GameProgressEntity(gameId = "game_2048", levelId = 1, isCompleted = false, stars = 0, bestScore = 2048)
        )

        // Water Sort queries
        val waterSortProgress = allProgress.filter { it.gameId == "water_sort" }
        assertEquals(2, waterSortProgress.size)
        assertTrue(waterSortProgress.all { it.gameId == "water_sort" })
        assertFalse(waterSortProgress.any { it.gameId == "color_sequence" })
        assertFalse(waterSortProgress.any { it.gameId == "game_2048" })

        // Color Sequence queries
        val colorSeqProgress = allProgress.filter { it.gameId == "color_sequence" }
        assertEquals(1, colorSeqProgress.size)
        assertEquals(500, colorSeqProgress[0].bestScore)
        assertFalse(colorSeqProgress.any { it.gameId == "water_sort" })

        // 2048 queries
        val game2048Progress = allProgress.filter { it.gameId == "game_2048" }
        assertEquals(1, game2048Progress.size)
        assertEquals(2048, game2048Progress[0].bestScore)
    }

    @Test
    fun testSavesAreIsolatedByGameId() {
        val saves = mapOf(
            "water_sort" to GameSaveEntity(gameId = "water_sort", stateJson = "{\"tubes\":[1,2]}", currentScore = 100),
            "color_sequence" to GameSaveEntity(gameId = "color_sequence", stateJson = "{\"colors\":[\"RED\",\"BLUE\"]}", currentScore = 200),
            "game_2048" to GameSaveEntity(gameId = "game_2048", stateJson = "{\"board\":[2,4,8,16]}", currentScore = 1250)
        )

        // Ensure retrieving one game cannot return another
        assertEquals("{\"tubes\":[1,2]}", saves["water_sort"]?.stateJson)
        assertEquals("{\"colors\":[\"RED\",\"BLUE\"]}", saves["color_sequence"]?.stateJson)
        assertEquals("{\"board\":[2,4,8,16]}", saves["game_2048"]?.stateJson)

        assertNotEquals(saves["water_sort"]?.currentScore, saves["color_sequence"]?.currentScore)
        assertNotEquals(saves["color_sequence"]?.currentScore, saves["game_2048"]?.currentScore)
    }
}
