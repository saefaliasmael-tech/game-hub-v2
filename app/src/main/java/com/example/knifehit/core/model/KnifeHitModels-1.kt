package com.example.knifehit.core.model

import androidx.compose.ui.graphics.Color

enum class TargetType(
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val isBoss: Boolean = false
) {
    WOOD_LOG("Oak Log", Color(0xFF8B5A2B), Color(0xFF654321)),
    WATERMELON("Watermelon", Color(0xFF2E7D32), Color(0xFFC62828)),
    ORANGE("Orange", Color(0xFFFF9800), Color(0xFFF57C00)),
    LEMON("Lemon", Color(0xFFFFEB3B), Color(0xFFFBC02D)),
    CYBER_WHEEL("Cyber Wheel", Color(0xFF00E5FF), Color(0xFF7C4DFF)),
    BOSS_TOMATO_KING("Tomato King", Color(0xFFD32F2F), Color(0xFF388E3C), isBoss = true),
    BOSS_IRON_SHIELD("Iron Fortress", Color(0xFF78909C), Color(0xFF37474F), isBoss = true),
    BOSS_CHEESE_WHEEL("Big Cheese", Color(0xFFFFD54F), Color(0xFFFFA000), isBoss = true),
    BOSS_CYBER_CORE("Cyber Core", Color(0xFF7C4DFF), Color(0xFF00E5FF), isBoss = true),
    BOSS_TITAN("Titan Core", Color(0xFFE91E63), Color(0xFF880E4F), isBoss = true)
}

data class KnifeSkin(
    val id: String,
    val name: String,
    val costApples: Int,
    val bladeColor: Color,
    val handleColor: Color
)

val ALL_KNIFE_SKINS = listOf(
    KnifeSkin("classic", "Classic Dagger", 0, Color(0xFFE2E8F0), Color(0xFF64748B)),
    KnifeSkin("kunai", "Ninja Kunai", 10, Color(0xFF94A3B8), Color(0xFFDC2626)),
    KnifeSkin("machete", "Machete", 25, Color(0xFFCBD5E1), Color(0xFF78350F)),
    KnifeSkin("cyber", "Cyber Blade", 50, Color(0xFF00E5FF), Color(0xFF7C4DFF)),
    KnifeSkin("gold", "Golden Katana", 75, Color(0xFFFFD700), Color(0xFFB45309)),
    KnifeSkin("dragon", "Dragon Fang", 100, Color(0xFFEF4444), Color(0xFF1E293B)),
    KnifeSkin("obsidian", "Obsidian Edge", 125, Color(0xFF334155), Color(0xFF8B5CF6)),
    KnifeSkin("plasma", "Plasma Saber", 150, Color(0xFF10B981), Color(0xFF047857))
)

data class AttachedKnife(
    val angle: Float // angle on the target in degrees (0..360)
)

data class AttachedApple(
    val angle: Float,
    var isSliced: Boolean = false
)

data class FlyingKnife(
    var y: Float,
    val velocityY: Float = -2400f,
    var isCollided: Boolean = false,
    var deflectionVx: Float = 0f,
    var deflectionVy: Float = 0f
)

data class TargetShard(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var angle: Float,
    var vAngle: Float,
    var alpha: Float = 1f,
    val color: Color
)

data class KnifeHitLevelConfig(
    val levelNumber: Int,
    val knivesRequired: Int,
    val targetType: TargetType,
    val rotationSpeed: Float, // degrees/sec
    val hasOscillation: Boolean = false,
    val oscillationFrequency: Float = 1.5f,
    val initialKnives: List<Float> = emptyList(), // angles of pre-stuck knives
    val apples: List<Float> = emptyList() // angles of apples
)

enum class KnifeHitGameMode(val displayName: String, val description: String) {
    CAMPAIGN("Campaign", "100 Progressive levels with Boss fights every 5 stages"),
    ENDLESS("Endless Hit", "Continuous stage ascension without checkpoints"),
    BOSS_RUSH("Boss Rush", "Consecutive intense Boss encounters")
}

data class KnifeHitGameState(
    val mode: KnifeHitGameMode = KnifeHitGameMode.CAMPAIGN,
    val levelNumber: Int = 1,
    val targetType: TargetType = TargetType.WOOD_LOG,
    val targetAngle: Float = 0f,
    val rotationSpeed: Float = 90f,
    val knivesRemaining: Int = 7,
    val totalKnivesThisStage: Int = 7,
    val attachedKnives: List<AttachedKnife> = emptyList(),
    val attachedApples: List<AttachedApple> = emptyList(),
    val flyingKnives: List<FlyingKnife> = emptyList(),
    val targetShards: List<TargetShard> = emptyList(),
    val targetRecoilY: Float = 0f,
    val applesCollectedThisSession: Int = 0,
    val currentCombo: Int = 0,
    val isGameOver: Boolean = false,
    val isStageWon: Boolean = false,
    val stageShatterAnimation: Boolean = false
)
