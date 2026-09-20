package com.example.watersort.core.economy

object EconomyConfig {
    const val STARTING_COINS = 250

    const val COST_HINT = 50
    const val COST_UNDO = 30
    const val COST_EXTRA_BOTTLE = 150

    const val REWARD_LEVEL_COMPLETE = 25
    const val REWARD_THREE_STARS = 15
    const val REWARD_DAILY_CHALLENGE = 100
    const val REWARD_REWARDED_AD = 100

    val DAILY_REWARD_AMOUNTS = listOf(50, 75, 100, 150, 200, 300, 500)
}

enum class TransactionType {
    REWARD_LEVEL,
    REWARD_STARS,
    REWARD_DAILY,
    REWARD_CHALLENGE,
    REWARD_ACHIEVEMENT,
    REWARD_AD,
    SPEND_HINT,
    SPEND_UNDO,
    SPEND_EXTRA_BOTTLE,
    SPEND_SKIN,
    SPEND_THEME
}

data class CoinTransaction(
    val id: Long = 0,
    val amount: Int,
    val type: TransactionType,
    val timestamp: Long = System.currentTimeMillis(),
    val reference: String = ""
)
