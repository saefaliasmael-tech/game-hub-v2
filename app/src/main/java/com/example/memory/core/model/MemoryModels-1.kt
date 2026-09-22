package com.example.memory.core.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class MemoryBoardSize(
    val displayName: String,
    val rows: Int,
    val cols: Int
) {
    EASY("3x4 (Easy)", 3, 4),           // 12 cards, 6 pairs
    NORMAL("4x4 (Normal)", 4, 4),       // 16 cards, 8 pairs
    HARD("4x6 (Hard)", 4, 6),           // 24 cards, 12 pairs
    EXPERT("6x6 (Expert)", 6, 6),       // 36 cards, 18 pairs
    MASTER("6x8 (Master)", 6, 8);       // 48 cards, 24 pairs

    val totalCards: Int get() = rows * cols
    val totalPairs: Int get() = totalCards / 2
}

enum class MemoryTheme(val displayName: String) {
    ANIMALS("Animals"),
    FOOD("Food & Drink"),
    SPACE("Space & Galaxy"),
    SPORTS("Sports"),
    NATURE("Nature"),
    TECH("Tech & Gadgets");

    fun getIconList(): List<Pair<String, ImageVector>> = when (this) {
        ANIMALS -> listOf(
            "Pets" to Icons.Filled.Pets,
            "CrueltyFree" to Icons.Filled.CrueltyFree,
            "FlutterDash" to Icons.Filled.FlutterDash,
            "BugReport" to Icons.Filled.BugReport,
            "Forest" to Icons.Filled.Forest,
            "PestControl" to Icons.Filled.PestControl,
            "Egg" to Icons.Filled.Egg,
            "SetMeal" to Icons.Filled.SetMeal,
            "WaterDrop" to Icons.Filled.WaterDrop,
            "Air" to Icons.Filled.Air,
            "Terrain" to Icons.Filled.Terrain,
            "Park" to Icons.Filled.Park,
            "Grass" to Icons.Filled.Grass,
            "Nature" to Icons.Filled.Nature,
            "FilterVintage" to Icons.Filled.FilterVintage,
            "Spa" to Icons.Filled.Spa,
            "Psychology" to Icons.Filled.Psychology,
            "Coronavirus" to Icons.Filled.Coronavirus,
            "Face" to Icons.Filled.Face,
            "Mood" to Icons.Filled.Mood,
            "EmojiNature" to Icons.Filled.EmojiNature,
            "Waves" to Icons.Filled.Waves,
            "LocalFlorist" to Icons.Filled.LocalFlorist,
            "Compost" to Icons.Filled.Compost
        )
        FOOD -> listOf(
            "Restaurant" to Icons.Filled.Restaurant,
            "Fastfood" to Icons.Filled.Fastfood,
            "LocalPizza" to Icons.Filled.LocalPizza,
            "Coffee" to Icons.Filled.Coffee,
            "Icecream" to Icons.Filled.Icecream,
            "BakeryDining" to Icons.Filled.BakeryDining,
            "LunchDining" to Icons.Filled.LunchDining,
            "LocalCafe" to Icons.Filled.LocalCafe,
            "LocalBar" to Icons.Filled.LocalBar,
            "Cake" to Icons.Filled.Cake,
            "RamenDining" to Icons.Filled.RamenDining,
            "EggAlt" to Icons.Filled.EggAlt,
            "SoupKitchen" to Icons.Filled.SoupKitchen,
            "BreakfastDining" to Icons.Filled.BreakfastDining,
            "DinnerDining" to Icons.Filled.DinnerDining,
            "LocalDining" to Icons.Filled.LocalDining,
            "Kitchen" to Icons.Filled.Kitchen,
            "Liquor" to Icons.Filled.Liquor,
            "BrunchDining" to Icons.Filled.BrunchDining,
            "Tapas" to Icons.Filled.Tapas,
            "TakeoutDining" to Icons.Filled.TakeoutDining,
            "SetMeal" to Icons.Filled.SetMeal,
            "LocalDrink" to Icons.Filled.LocalDrink,
            "Bento" to Icons.Filled.Bento
        )
        SPACE -> listOf(
            "RocketLaunch" to Icons.Filled.RocketLaunch,
            "Public" to Icons.Filled.Public,
            "Brightness3" to Icons.Filled.Brightness3,
            "WbSunny" to Icons.Filled.WbSunny,
            "Star" to Icons.Filled.Star,
            "AutoAwesome" to Icons.Filled.AutoAwesome,
            "Flare" to Icons.Filled.Flare,
            "DarkMode" to Icons.Filled.DarkMode,
            "LightMode" to Icons.Filled.LightMode,
            "Explore" to Icons.Filled.Explore,
            "Radar" to Icons.Filled.Radar,
            "Satellite" to Icons.Filled.Satellite,
            "Navigation" to Icons.Filled.Navigation,
            "LocationSearching" to Icons.Filled.LocationSearching,
            "GpsFixed" to Icons.Filled.GpsFixed,
            "TrackChanges" to Icons.Filled.TrackChanges,
            "Language" to Icons.Filled.Language,
            "Grade" to Icons.Filled.Grade,
            "Stars" to Icons.Filled.Stars,
            "FlashOn" to Icons.Filled.FlashOn,
            "BlurOn" to Icons.Filled.BlurOn,
            "TravelExplore" to Icons.Filled.TravelExplore,
            "Science" to Icons.Filled.Science,
            "Hub" to Icons.Filled.Hub
        )
        SPORTS -> listOf(
            "SportsSoccer" to Icons.Filled.SportsSoccer,
            "SportsBasketball" to Icons.Filled.SportsBasketball,
            "SportsTennis" to Icons.Filled.SportsTennis,
            "SportsBaseball" to Icons.Filled.SportsBaseball,
            "SportsEsports" to Icons.Filled.SportsEsports,
            "EmojiEvents" to Icons.Filled.EmojiEvents,
            "MilitaryTech" to Icons.Filled.MilitaryTech,
            "FitnessCenter" to Icons.Filled.FitnessCenter,
            "Pool" to Icons.Filled.Pool,
            "DirectionsRun" to Icons.Filled.DirectionsRun,
            "DirectionsBike" to Icons.Filled.DirectionsBike,
            "DirectionsWalk" to Icons.Filled.DirectionsWalk,
            "Kayaking" to Icons.Filled.Kayaking,
            "Rowing" to Icons.Filled.Rowing,
            "Snowboarding" to Icons.Filled.Snowboarding,
            "Skateboarding" to Icons.Filled.Skateboarding,
            "Sailing" to Icons.Filled.Sailing,
            "SportsMotorsports" to Icons.Filled.SportsMotorsports,
            "SportsFootball" to Icons.Filled.SportsFootball,
            "SportsGolf" to Icons.Filled.SportsGolf,
            "SportsCricket" to Icons.Filled.SportsCricket,
            "Timer" to Icons.Filled.Timer,
            "Flag" to Icons.Filled.Flag,
            "Speed" to Icons.Filled.Speed
        )
        NATURE -> listOf(
            "Forest" to Icons.Filled.Forest,
            "Grass" to Icons.Filled.Grass,
            "Park" to Icons.Filled.Park,
            "LocalFlorist" to Icons.Filled.LocalFlorist,
            "WaterDrop" to Icons.Filled.WaterDrop,
            "WbSunny" to Icons.Filled.WbSunny,
            "Cloud" to Icons.Filled.Cloud,
            "Thunderstorm" to Icons.Filled.Thunderstorm,
            "AcUnit" to Icons.Filled.AcUnit,
            "Thermostat" to Icons.Filled.Thermostat,
            "Air" to Icons.Filled.Air,
            "Terrain" to Icons.Filled.Terrain,
            "Landscape" to Icons.Filled.Landscape,
            "Waves" to Icons.Filled.Waves,
            "Volcano" to Icons.Filled.Volcano,
            "Flare" to Icons.Filled.Flare,
            "Bedtime" to Icons.Filled.Bedtime,
            "Tsunami" to Icons.Filled.Tsunami,
            "Cyclone" to Icons.Filled.Cyclone,
            "Eco" to Icons.Filled.Eco,
            "Recycling" to Icons.Filled.Recycling,
            "Grain" to Icons.Filled.Grain,
            "Fireplace" to Icons.Filled.Fireplace,
            "Compost" to Icons.Filled.Compost
        )
        TECH -> listOf(
            "Laptop" to Icons.Filled.Laptop,
            "Smartphone" to Icons.Filled.Smartphone,
            "Headphones" to Icons.Filled.Headphones,
            "CameraAlt" to Icons.Filled.CameraAlt,
            "Watch" to Icons.Filled.Watch,
            "Tv" to Icons.Filled.Tv,
            "Memory" to Icons.Filled.Memory,
            "DeveloperBoard" to Icons.Filled.DeveloperBoard,
            "Gamepad" to Icons.Filled.Gamepad,
            "Keyboard" to Icons.Filled.Keyboard,
            "Mouse" to Icons.Filled.Mouse,
            "Router" to Icons.Filled.Router,
            "BatteryChargingFull" to Icons.Filled.BatteryChargingFull,
            "Wifi" to Icons.Filled.Wifi,
            "Bluetooth" to Icons.Filled.Bluetooth,
            "Fingerprint" to Icons.Filled.Fingerprint,
            "QrCode" to Icons.Filled.QrCode,
            "Code" to Icons.Filled.Code,
            "Terminal" to Icons.Filled.Terminal,
            "Sensors" to Icons.Filled.Sensors,
            "Mic" to Icons.Filled.Mic,
            "Speaker" to Icons.Filled.Speaker,
            "Print" to Icons.Filled.Print,
            "Computer" to Icons.Filled.Computer
        )
    }
}

enum class MemoryGameMode(val displayName: String) {
    CLASSIC("Classic"),
    TIMED("Time Attack"),
    LIMITED_MOVES("Limited Moves"),
    DAILY_CHALLENGE("Daily Challenge")
}

data class MemoryCard(
    val id: Int,
    val pairId: Int,
    val iconName: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

data class MemoryGameState(
    val boardSize: MemoryBoardSize = MemoryBoardSize.NORMAL,
    val mode: MemoryGameMode = MemoryGameMode.CLASSIC,
    val theme: MemoryTheme = MemoryTheme.ANIMALS,
    val cards: List<MemoryCard> = emptyList(),
    val firstSelectedIndex: Int? = null,
    val secondSelectedIndex: Int? = null,
    val isBusyChecking: Boolean = false,
    val movesCount: Int = 0,
    val matchesCount: Int = 0,
    val mistakesCount: Int = 0,
    val timeLimitSeconds: Int = 0,
    val elapsedTimeSeconds: Int = 0,
    val maxMoves: Int = 0,
    val isWon: Boolean = false,
    val isGameOver: Boolean = false,
    val starsAwarded: Int = 0,
    val score: Int = 0
) {
    val totalPairs: Int get() = boardSize.totalPairs
    val pairsRemaining: Int get() = totalPairs - matchesCount
    val accuracy: Float get() = if (movesCount > 0) (matchesCount.toFloat() / movesCount.toFloat()) * 100f else 100f
    val timeRemainingSeconds: Int get() = (timeLimitSeconds - elapsedTimeSeconds).coerceAtLeast(0)
    val movesRemaining: Int get() = (maxMoves - movesCount).coerceAtLeast(0)
}
