package com.example.hub.registry

import androidx.compose.ui.graphics.Color
import com.example.hub.model.GameCategory
import com.example.hub.model.GameInfo

/**
 * Central registry of games inside Game Hub.
 * To add a new game in future updates:
 * 1. Build the game under com.example.games.<gamename>
 * 2. Add an entry here with a unique id, route, and metadata.
 * 3. Add the navigation composable in MainActivity.
 *
 * Notice: Zero game logic, state, or currencies exist here.
 */
object GameRegistry {

    const val WATER_SORT_ID = "water_sort"
    const val COLOR_SEQUENCE_ID = "color_sequence"
    const val PUZZLE_2048_ID = "game_2048"
    const val MASTERMIND_ID = "mastermind"
    const val UNBLOCK_ME_ID = "unblock_me"
    const val MEMORY_CARDS_ID = "memory_cards"
    const val STOP_THE_TIME_ID = "stop_the_time"
    const val COLOR_SWITCH_ID = "color_switch"
    const val KNIFE_HIT_ID = "knife_hit"
    const val AA_ID = "aa"
    const val MINI_TD_ID = "mini_tower_defense"
    const val ROPE_AROUND_ID = "rope_around"
    const val ROPE_RESCUE_ID = "rope_rescue"
    const val PIC_PUZZLE_ID = "pic_puzzle"
    const val PULL_THE_PIN_ID = "pull_the_pin"
    const val HAPPY_GLASS_ID = "happy_glass"
    const val MR_BULLET_ID = "mr_bullet"
    const val PROTECT_SHEEP_ID = "protect_sheep"
    const val FUN_FRENZY_ID = "fun_frenzy"

    private val gamesList: List<GameInfo> = listOf(
        GameInfo(
            id = WATER_SORT_ID,
            name = "Water Sort Puzzle",
            description = "Sort colored waters into matching glass tubes with pure logic and soothing physics.",
            longDescription = "Experience a relaxing and brain-challenging puzzle adventure! Carefully pour water between bottles until every tube contains only a single uniform color. Features 8 distinct fantasy worlds, daily challenge puzzles, customizable vial skins, atmospheric sound effects, and intelligent move undo.",
            category = GameCategory.PUZZLE,
            version = "4.0",
            isAvailable = true,
            isFeatured = true,
            isNew = false,
            isComingSoon = false,
            primaryColor = Color(0xFF00B4D8),
            secondaryColor = Color(0xFF03045E),
            accentColor = Color(0xFF48CAE4),
            entryRoute = "watersort_home",
            order = 1,
            totalLevelsEstimate = "100+ Levels",
            difficultyEstimate = "Casual → Master"
        ),
        GameInfo(
            id = COLOR_SEQUENCE_ID,
            name = "Color Sequence",
            description = "Memorize the sequence of glowing colors and repeat the pattern without mistakes.",
            longDescription = "A vibrant pattern recognition and memory challenge! Watch the glowing colored shapes appear in sequence, remember the exact order, and tap them back accurately as the sequence grows longer and faster.",
            category = GameCategory.CASUAL,
            version = "1.0",
            isAvailable = true,
            isFeatured = false,
            isNew = false,
            isComingSoon = false,
            primaryColor = Color(0xFF8B5CF6),
            secondaryColor = Color(0xFF4C1D95),
            accentColor = Color(0xFFA78BFA),
            entryRoute = "colorsequence_home",
            order = 2,
            totalLevelsEstimate = "30 Levels",
            difficultyEstimate = "Memory & Focus"
        ),
        GameInfo(
            id = PUZZLE_2048_ID,
            name = "2048 Infinite",
            description = "Slide and merge matching number tiles to reach 2048 and beyond.",
            longDescription = "Swipe up, down, left, and right to merge identical number tiles. Plan your moves ahead to unlock the legendary 2048 tile, and continue beyond for highest score records.",
            category = GameCategory.LOGIC,
            version = "1.0",
            isAvailable = true,
            isFeatured = false,
            isNew = false,
            isComingSoon = false,
            primaryColor = Color(0xFFE76F51),
            secondaryColor = Color(0xFF264653),
            accentColor = Color(0xFFF4A261),
            entryRoute = "game2048_home",
            order = 3,
            totalLevelsEstimate = "Endless Mode",
            difficultyEstimate = "Tactical"
        ),
        GameInfo(
            id = MASTERMIND_ID,
            name = "Mastermind",
            description = "Crack the secret color code through logic, deduction, and feedback pegs.",
            longDescription = "The classic code-breaking game! Deduce the hidden sequence of colors using precise feedback indicators for exact positions and matching colors across 100 progressive campaign levels, daily challenges, and custom difficulties.",
            category = GameCategory.LOGIC,
            version = "1.0",
            isAvailable = true,
            isFeatured = false,
            isNew = false,
            isComingSoon = false,
            primaryColor = Color(0xFF6366F1),
            secondaryColor = Color(0xFF312E81),
            accentColor = Color(0xFF818CF8),
            entryRoute = "mastermind_home",
            order = 4,
            totalLevelsEstimate = "100 Levels",
            difficultyEstimate = "Deduction & Logic"
        ),
        GameInfo(
            id = UNBLOCK_ME_ID,
            name = "Unblock Me",
            description = "Slide wooden blocks to clear the path for the red block to escape.",
            longDescription = "A timeless sliding block puzzle on a 6x6 grid. Strategically maneuver horizontal and vertical wooden obstacles to free the crimson wood block. Features 100 crafted puzzles across 5 difficulties, BFS optimal hint solver, and undo support.",
            category = GameCategory.PUZZLE,
            version = "1.0",
            isAvailable = true,
            isFeatured = false,
            isNew = false,
            isComingSoon = false,
            primaryColor = Color(0xFFE63946),
            secondaryColor = Color(0xFF3E2723),
            accentColor = Color(0xFFFFB703),
            entryRoute = "unblock_home",
            order = 5,
            totalLevelsEstimate = "100 Puzzles",
            difficultyEstimate = "Tactical Sliding"
        ),
        GameInfo(
            id = MEMORY_CARDS_ID,
            name = "Memory Card Match",
            description = "Flip and match thematic pairs of cards across multiple board sizes.",
            longDescription = "Train and sharpen your visual memory with smooth 3D card flipping, 6 rich icon themes (Animals, Food, Space, Sports, Nature, Tech), 5 board sizes (up to 6x8 Master), and time attack & limited move modes.",
            category = GameCategory.CASUAL,
            version = "1.0",
            isAvailable = true,
            isFeatured = false,
            isNew = false,
            isComingSoon = false,
            primaryColor = Color(0xFF06B6D4),
            secondaryColor = Color(0xFF164E63),
            accentColor = Color(0xFF22D3EE),
            entryRoute = "memory_home",
            order = 6,
            totalLevelsEstimate = "5 Grid Sizes",
            difficultyEstimate = "Visual Recall"
        ),
        GameInfo(
            id = STOP_THE_TIME_ID,
            name = "Stop the Time",
            description = "Stop the clock at the exact target time with millisecond precision.",
            longDescription = "Test your inner clock and reflex precision! Stop the timer at exact millisecond marks across 100 Campaign levels, Blind timing mode, speed illusions, daily deterministic challenges, and pass-and-play local multiplayer.",
            category = GameCategory.ARCADE,
            version = "1.0",
            isAvailable = true,
            isFeatured = true,
            isNew = true,
            isComingSoon = false,
            primaryColor = Color(0xFF10B981),
            secondaryColor = Color(0xFF064E3B),
            accentColor = Color(0xFF34D399),
            entryRoute = "stoptime_home",
            order = 7,
            totalLevelsEstimate = "100 Levels",
            difficultyEstimate = "Precision & Reflex"
        ),
        GameInfo(
            id = COLOR_SWITCH_ID,
            name = "Color Switch",
            description = "Bounce through multi-colored obstacles matching your ball's color.",
            longDescription = "Arcade reflex sensation! Tap to keep your ball airborne and pass safely through matching colored segments of spinning circles, crosses, and concentric rings. Includes 100 campaign levels, endless climb, and color-blind symbol support.",
            category = GameCategory.ARCADE,
            version = "1.0",
            isAvailable = true,
            isFeatured = true,
            isNew = true,
            isComingSoon = false,
            primaryColor = Color(0xFF00E5FF),
            secondaryColor = Color(0xFF0F172A),
            accentColor = Color(0xFFFF007F),
            entryRoute = "colorswitch_home",
            order = 8,
            totalLevelsEstimate = "100 Levels",
            difficultyEstimate = "Reflex & Timing"
        ),
        GameInfo(
            id = KNIFE_HIT_ID,
            name = "Knife Hit",
            description = "Throw knives into rotating targets, slice apples, and shatter epic bosses.",
            longDescription = "Precision knife-throwing arcade action! Launch daggers into spinning logs, fruits, and shields without hitting existing blades. Conquer 100 stages, epic Boss battles every 5 levels, apple rewards, and unlock 8 unique cosmetic blades.",
            category = GameCategory.ARCADE,
            version = "1.0",
            isAvailable = true,
            isFeatured = true,
            isNew = true,
            isComingSoon = false,
            primaryColor = Color(0xFFEF4444),
            secondaryColor = Color(0xFF450A0A),
            accentColor = Color(0xFFFFB703),
            entryRoute = "knifehit_home",
            order = 9,
            totalLevelsEstimate = "100 Stages",
            difficultyEstimate = "Timing & Bosses"
        ),
        GameInfo(
            id = AA_ID,
            name = "AA",
            description = "Pin needles onto the rotating wheel without colliding with existing needles.",
            longDescription = "A masterclass in rhythm, focus, and precision! Tap to shoot numbered pins into a spinning central sphere. Master 100 handcrafted levels featuring reversing rotations, speed shifts, and dynamic obstacles.",
            category = GameCategory.ARCADE,
            version = "1.0",
            isAvailable = true,
            isFeatured = true,
            isNew = true,
            isComingSoon = false,
            primaryColor = Color(0xFF89B4FA),
            secondaryColor = Color(0xFF1E1E2E),
            accentColor = Color(0xFFA6E3A1),
            entryRoute = "aa_home",
            order = 10,
            totalLevelsEstimate = "100 Levels",
            difficultyEstimate = "Precision & Speed"
        ),
        GameInfo(
            id = MINI_TD_ID,
            name = "Mini Tower Defense",
            description = "Build Archer, Cannon, and Magic towers to stop incoming waves of mythical beasts.",
            longDescription = "Command tactical bastions across varied strategic paths! Deploy Archer, Cannon, and Magic towers, upgrade firepower and range, manage your battle gold, and defend the castle gates across 100 tactical missions.",
            category = GameCategory.STRATEGY,
            version = "1.0",
            isAvailable = true,
            isFeatured = true,
            isNew = true,
            isComingSoon = false,
            primaryColor = Color(0xFF00B4D8),
            secondaryColor = Color(0xFF1B2A4A),
            accentColor = Color(0xFFFFD166),
            entryRoute = "minitd_home",
            order = 11,
            totalLevelsEstimate = "100 Levels",
            difficultyEstimate = "Tactical & Strategic"
        ),
        GameInfo(
            id = ROPE_AROUND_ID,
            name = "Rope Around",
            description = "Wrap the glowing elastic rope around all pegs to illuminate them without hitting hazards.",
            longDescription = "A satisfying geometric tactile puzzle! Drag your neon rope smoothly around pins to light them all up. Navigate intricate polygons, stars, and tight corridors while avoiding danger zones across 100 levels.",
            category = GameCategory.PUZZLE,
            version = "1.0",
            isAvailable = true,
            isFeatured = true,
            isNew = true,
            isComingSoon = false,
            primaryColor = Color(0xFFFF70A6),
            secondaryColor = Color(0xFF2B2D42),
            accentColor = Color(0xFF06D6A0),
            entryRoute = "ropearound_home",
            order = 12,
            totalLevelsEstimate = "100 Levels",
            difficultyEstimate = "Spatial Logic"
        ),
        GameInfo(
            id = ROPE_RESCUE_ID,
            name = "Rope Rescue",
            description = "Route zipline cables around pulleys and past spinning sawblades to evacuate hostages.",
            longDescription = "A thrilling physics evacuation puzzle! Guide the rescue tension cable past deadly rotating sawblades, weave securely through mechanical pulleys, and anchor directly into the ambulance station. Press and hold to slide your stranded civilians to safety across 100 hazardous rescue missions.",
            category = GameCategory.PUZZLE,
            version = "1.0",
            isAvailable = true,
            isFeatured = true,
            isNew = true,
            isComingSoon = false,
            primaryColor = Color(0xFF38BDF8),
            secondaryColor = Color(0xFF1E293B),
            accentColor = Color(0xFF10B981),
            entryRoute = "roperescue_home",
            order = 13,
            totalLevelsEstimate = "100 Levels",
            difficultyEstimate = "Tactical Rescue"
        ),
        GameInfo(
            id = PIC_PUZZLE_ID,
            name = "Pic Puzzle",
            description = "Slide and reconstruct vibrant mosaic artworks across 3x3, 4x4, and 5x5 grid masteries.",
            longDescription = "An artistic sliding tile puzzle game! Reassemble shattered vibrant landscapes, cyberpunk metropolises, cosmic nebulae, and ancient landmarks. Features move counters, precision timers, dynamic number hints, and thumbnail preview across 100 unique levels.",
            category = GameCategory.PUZZLE,
            version = "1.0",
            isAvailable = true,
            isFeatured = true,
            isNew = true,
            isComingSoon = false,
            primaryColor = Color(0xFF52B788),
            secondaryColor = Color(0xFF1F2421),
            accentColor = Color(0xFFFFD166),
            entryRoute = "picpuzzle_home",
            order = 14,
            totalLevelsEstimate = "100 Levels",
            difficultyEstimate = "Visual & Logic"
        ),
        GameInfo(
            id = PULL_THE_PIN_ID,
            name = "Pull the Pin",
            description = "Slide pins strategically, colorize neutral spheres, and guide balls safely into the collection vat.",
            longDescription = "A clever physics puzzle where precision pulling is key! Navigate intricate gravitational chambers, diffuse vibrant color to dull spheres, avoid destructive explosive ordnance, and funnel your target count safely into the collection basket across 100 tactical levels.",
            category = GameCategory.PUZZLE,
            version = "1.0",
            isAvailable = true,
            isFeatured = true,
            isNew = true,
            isComingSoon = false,
            primaryColor = Color(0xFFFF9F1C),
            secondaryColor = Color(0xFF261C14),
            accentColor = Color(0xFFFFBF69),
            entryRoute = "pullthepin_home",
            order = 15,
            totalLevelsEstimate = "100 Levels",
            difficultyEstimate = "Physics & Gravity"
        ),
        GameInfo(
            id = HAPPY_GLASS_ID,
            name = "Happy Glass",
            description = "Draw lines with limited ink to direct flowing water drops into the empty glass.",
            longDescription = "An ingenious physics puzzle! Draw physical lines to guide water streams from taps into sad empty glasses. Turn their frowns upside down across 100 tactical levels featuring obstacles, ramps, and ink conservation scoring.",
            category = GameCategory.PUZZLE,
            version = "1.0",
            isAvailable = true,
            isFeatured = true,
            isNew = true,
            isComingSoon = false,
            primaryColor = Color(0xFF0EA5E9),
            secondaryColor = Color(0xFF0C4A6E),
            accentColor = Color(0xFF38BDF8),
            entryRoute = "happyglass_home",
            order = 16,
            totalLevelsEstimate = "100 Levels",
            difficultyEstimate = "Physics & Drawing"
        ),
        GameInfo(
            id = MR_BULLET_ID,
            name = "Mr Bullet",
            description = "Aim laser-guided ricochet bullets to eliminate all bandit targets and ignite TNT barrels.",
            longDescription = "A legendary tactical trick-shot shooter! Play as a suave secret agent, calculate geometric laser ricochets off steel girders, explode TNT barrels, and neutralize all criminal targets with minimum ammo across 100 missions.",
            category = GameCategory.ARCADE,
            version = "1.0",
            isAvailable = true,
            isFeatured = true,
            isNew = true,
            isComingSoon = false,
            primaryColor = Color(0xFFEF4444),
            secondaryColor = Color(0xFF450A0A),
            accentColor = Color(0xFFFBBF24),
            entryRoute = "mrbullet_home",
            order = 17,
            totalLevelsEstimate = "100 Levels",
            difficultyEstimate = "Trajectory & Aim"
        ),
        GameInfo(
            id = PROTECT_SHEEP_ID,
            name = "Protect Sheep",
            description = "Draw protective fences to shield cute pasture sheep from attacking wolves and falling boulders.",
            longDescription = "An adorable and tense defensive drawing puzzle! Trace barriers and pens around innocent sheep to hold off ravenous wolves, angry bee swarms, and rolling boulders until the countdown timer expires across 100 levels.",
            category = GameCategory.PUZZLE,
            version = "1.0",
            isAvailable = true,
            isFeatured = true,
            isNew = true,
            isComingSoon = false,
            primaryColor = Color(0xFF10B981),
            secondaryColor = Color(0xFF064E3B),
            accentColor = Color(0xFF34D399),
            entryRoute = "protectsheep_home",
            order = 18,
            totalLevelsEstimate = "100 Levels",
            difficultyEstimate = "Drawing & Defense"
        ),
        GameInfo(
            id = FUN_FRENZY_ID,
            name = "Fun Frenzy",
            description = "Swipe and cut suspension ropes in the right order to safely rescue your stranded buddy.",
            longDescription = "A high-stakes physics rope-cutting rescue adventure! Swipe your finger to slice tension ropes, control pendulum swings, avoid razor-sharp sawblades and lethal lava pits, and safely drop into the rescue portal across 100 levels.",
            category = GameCategory.PUZZLE,
            version = "1.0",
            isAvailable = true,
            isFeatured = true,
            isNew = true,
            isComingSoon = false,
            primaryColor = Color(0xFFA855F7),
            secondaryColor = Color(0xFF3B0764),
            accentColor = Color(0xFFC084FC),
            entryRoute = "funfrenzy_home",
            order = 19,
            totalLevelsEstimate = "100 Levels",
            difficultyEstimate = "Physics & Rescue"
        )
    )

    fun getAllGames(): List<GameInfo> = gamesList.sortedBy { it.order }

    fun getGameById(id: String): GameInfo? = gamesList.find { it.id == id }

    fun getFeaturedGame(): GameInfo? = gamesList.find { it.isFeatured } ?: gamesList.firstOrNull()

    fun getGamesByCategory(category: GameCategory): List<GameInfo> {
        if (category == GameCategory.ALL) return getAllGames()
        return gamesList.filter { it.category == category }
    }

    fun searchGames(query: String): List<GameInfo> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return getAllGames()
        return gamesList.filter {
            it.name.contains(trimmed, ignoreCase = true) ||
            it.description.contains(trimmed, ignoreCase = true) ||
            it.category.name.contains(trimmed, ignoreCase = true)
        }
    }
}
