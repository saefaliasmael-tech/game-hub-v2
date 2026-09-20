package com.example.watersort.core.events

import com.example.watersort.core.economy.TransactionType
import com.example.watersort.core.model.InvalidReason
import com.example.watersort.core.model.LiquidColor

sealed interface GameEvent {
    data class LevelStarted(val levelId: Int) : GameEvent
    data class MoveExecuted(
        val levelId: Int,
        val fromIndex: Int,
        val toIndex: Int,
        val color: LiquidColor,
        val amount: Int
    ) : GameEvent
    data class InvalidMove(val levelId: Int, val reason: InvalidReason) : GameEvent
    data class LevelCompleted(
        val levelId: Int,
        val stars: Int,
        val moves: Int,
        val withoutUndo: Boolean,
        val withoutInvalidMoves: Boolean
    ) : GameEvent
    data class HintUsed(val levelId: Int) : GameEvent
    data class UndoUsed(val levelId: Int) : GameEvent
    data class ExtraBottleUsed(val levelId: Int) : GameEvent
    data class CoinsEarned(val amount: Int, val type: TransactionType, val reference: String) : GameEvent
    data class CoinsSpent(val amount: Int, val type: TransactionType, val reference: String) : GameEvent
    data class DailyChallengeCompleted(val date: String, val stars: Int, val moves: Int) : GameEvent
    data class MissionCompleted(val missionId: String) : GameEvent
    data class MissionClaimed(val missionId: String, val rewardCoins: Int, val rewardXp: Int) : GameEvent
    data class AchievementUnlocked(val achievementId: String) : GameEvent
    data class LevelUp(val newLevel: Int, val bonusCoins: Int) : GameEvent
}
