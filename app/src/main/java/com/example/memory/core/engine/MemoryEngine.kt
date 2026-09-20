package com.example.memory.core.engine

import com.example.memory.core.model.MemoryBoardSize
import com.example.memory.core.model.MemoryCard
import com.example.memory.core.model.MemoryGameMode
import com.example.memory.core.model.MemoryTheme
import java.util.Random

object MemoryEngine {

    /**
     * Creates and shuffles a new deck of matching card pairs.
     */
    fun createBoard(
        boardSize: MemoryBoardSize,
        theme: MemoryTheme,
        random: Random = Random()
    ): List<MemoryCard> {
        val pairsNeeded = boardSize.totalPairs
        val iconList = theme.getIconList().take(pairsNeeded)

        val cards = mutableListOf<MemoryCard>()
        var cardId = 0

        for (pairIndex in 0 until pairsNeeded) {
            val iconName = iconList[pairIndex % iconList.size].first
            // Card A of pair
            cards.add(
                MemoryCard(
                    id = cardId++,
                    pairId = pairIndex,
                    iconName = iconName,
                    isFlipped = false,
                    isMatched = false
                )
            )
            // Card B of pair
            cards.add(
                MemoryCard(
                    id = cardId++,
                    pairId = pairIndex,
                    iconName = iconName,
                    isFlipped = false,
                    isMatched = false
                )
            )
        }

        return cards.shuffled(random)
    }

    /**
     * Determines stars (1..3) based on accuracy and board size.
     */
    fun calculateStars(movesCount: Int, pairsCount: Int): Int {
        val ratio = movesCount.toFloat() / pairsCount.toFloat()
        return when {
            ratio <= 1.4f -> 3 // Outstanding memory!
            ratio <= 2.0f -> 2 // Good memory
            else -> 1
        }
    }

    /**
     * Calculates score.
     */
    fun calculateScore(movesCount: Int, pairsCount: Int, timeSeconds: Int, mode: MemoryGameMode): Int {
        val baseScore = pairsCount * 500
        val movesBonus = ((pairsCount * 2.5 - movesCount) * 100).toInt().coerceAtLeast(0)
        val timePenalty = (timeSeconds * 5).coerceAtMost(1000)
        val modeMultiplier = when (mode) {
            MemoryGameMode.CLASSIC -> 1.0
            MemoryGameMode.TIMED -> 1.5
            MemoryGameMode.LIMITED_MOVES -> 1.3
            MemoryGameMode.DAILY_CHALLENGE -> 2.0
        }
        return ((baseScore + movesBonus - timePenalty) * modeMultiplier).toInt().coerceAtLeast(100)
    }
}
