package com.example.watersort.core.model

enum class WorldEnvType {
    GARDEN,
    OCEAN,
    EMBER,
    COSMIC,
    FROZEN,
    CAVERNS,
    SHADOW,
    MYSTIC
}

data class WorldZone(
    val zoneIndex: Int, // 1 to 5
    val name: String,
    val startLevel: Int,
    val endLevel: Int
) {
    val totalLevels: Int get() = (endLevel - startLevel + 1)
    val zoneNumber: Int get() = zoneIndex
}

data class WorldInfo(
    val id: Int,
    val name: String,
    val description: String,
    val startLevel: Int,
    val endLevel: Int,
    val requiredStars: Int,
    val themeColorHex: Long,
    val iconEmoji: String,
    val envType: WorldEnvType = WorldEnvType.GARDEN,
    val themeId: String = "classic"
) {
    val totalLevels: Int get() = (endLevel - startLevel + 1)
    val zones: List<WorldZone>
        get() {
            val zoneSize = 10
            val zoneNames = when (id) {
                1 -> listOf("Emerald Glade", "Dewdrop Path", "Sunlit Meadow", "Verdant Spring", "Crystal Sanctuary")
                2 -> listOf("Coral Shallows", "Azure Trench", "Sunken Ruins", "Abyssal Rift", "Triton's Palace")
                3 -> listOf("Ashen Flats", "Molten Crags", "Magma River", "Obsidian Gorge", "Volcanic Core")
                4 -> listOf("Orbit Entry", "Plasma Sector", "Gravity Chamber", "Starlight Array", "Singularity Hub")
                5 -> listOf("Frostbite Pass", "Glacial Peaks", "Blizzard Drift", "Aurora Valley", "Eternal Ice Spire")
                6 -> listOf("Amethyst Mine", "Prism Chasm", "Geode Haven", "Echoing Vaults", "Heart of the Caverns")
                7 -> listOf("Twilight Border", "Veil of Mist", "Phantom Ridge", "Umbral Void", "Eclipse Throne")
                else -> listOf("Astral Gate", "Ethereal Grove", "Sanctum of Light", "Celestial Ascent", "Prismatic Apex")
            }
            return (0 until 5).map { idx ->
                val zStart = startLevel + (idx * zoneSize)
                val zEnd = (zStart + zoneSize - 1).coerceAtMost(endLevel)
                WorldZone(
                    zoneIndex = idx + 1,
                    name = zoneNames.getOrElse(idx) { "Zone ${idx + 1}" },
                    startLevel = zStart,
                    endLevel = zEnd
                )
            }
        }
}

data class SecretLevelInfo(
    val id: Int,
    val name: String,
    val requiredTotalStars: Int,
    val difficultyTitle: String,
    val rewardCoins: Int,
    val iconEmoji: String
) {
    val worldId: Int get() = id
    val title: String get() = name
    val unlockStarsThreshold: Int get() = requiredTotalStars
    val levelNumber: Int get() = 500 + id
}

object WorldConfig {
    val WORLDS = listOf(
        WorldInfo(1, "Crystal Garden", "Lush emerald foliage and crystalline dew", 1, 50, 0, 0xFF059669, "🌿", WorldEnvType.GARDEN, "nature"),
        WorldInfo(2, "Deep Ocean", "Sunken coral mysteries in the azure depths", 51, 100, 60, 0xFF0284C7, "🌊", WorldEnvType.OCEAN, "ocean"),
        WorldInfo(3, "Ember Valley", "Scorching basalt cliffs and magma torrents", 101, 150, 130, 0xFFDC2626, "🔥", WorldEnvType.EMBER, "volcano"),
        WorldInfo(4, "Cosmic Lab", "Zero-gravity orbital conduits and plasma stellar rays", 151, 200, 210, 0xFF7C3AED, "🚀", WorldEnvType.COSMIC, "space"),
        WorldInfo(5, "Frozen Realm", "Glacial tundra, blizzards, and crystalline permafrost", 201, 250, 290, 0xFF06B6D4, "❄️", WorldEnvType.FROZEN, "frozen"),
        WorldInfo(6, "Crystal Caverns", "Shimmering amethyst geodes and luminescent stalactites", 251, 300, 370, 0xFF9333EA, "💎", WorldEnvType.CAVERNS, "caverns"),
        WorldInfo(7, "Shadow World", "Mysterious twilight wisps and obsidian monoliths", 301, 350, 450, 0xFF475569, "🌑", WorldEnvType.SHADOW, "shadow"),
        WorldInfo(8, "Mystic World", "Ethereal sanctum of celestial starlight and eternal gold", 351, 400, 530, 0xFFD97706, "✨", WorldEnvType.MYSTIC, "mystic")
    )

    val SECRET_LEVELS = listOf(
        SecretLevelInfo(1, "Prism Core", 35, "Challenge", 150, "💎"),
        SecretLevelInfo(2, "Abyssal Gate", 80, "Hard", 200, "🔱"),
        SecretLevelInfo(3, "Phoenix Hearth", 140, "Hard", 250, "🔥"),
        SecretLevelInfo(4, "Neutron Star", 200, "Expert", 300, "⚛️"),
        SecretLevelInfo(5, "Absolute Zero", 270, "Expert", 350, "🧊"),
        SecretLevelInfo(6, "Geode Heart", 340, "Master", 400, "🔮"),
        SecretLevelInfo(7, "Phantom Void", 420, "Master", 450, "👁️"),
        SecretLevelInfo(8, "Omni Prism", 500, "Grandmaster", 600, "👑")
    )

    fun forLevel(levelNumber: Int): WorldInfo {
        return WORLDS.find { levelNumber in it.startLevel..it.endLevel } ?: WORLDS.last()
    }
}

