package com.example.hub.model

enum class GameCategory(val id: String, val titleResName: String) {
    ALL("all", "category_all"),
    PUZZLE("puzzle", "category_puzzle"),
    ARCADE("arcade", "category_arcade"),
    LOGIC("logic", "category_logic"),
    CASUAL("casual", "category_casual"),
    BOARD("board", "category_board");

    companion object {
        fun fromId(id: String): GameCategory = entries.find { it.id.equals(id, ignoreCase = true) } ?: ALL
    }
}
