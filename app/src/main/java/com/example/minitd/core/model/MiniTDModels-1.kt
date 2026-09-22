package com.example.minitd.core.model

enum class TowerType(
    val displayName: String,
    val baseCost: Int,
    val baseDamage: Float,
    val baseRange: Float, // percentage of screen width (e.g. 0.28f)
    val attackIntervalSec: Float,
    val slowsEnemy: Boolean = false,
    val splashRadius: Float = 0f
) {
    ARCHER("Archer", 100, 25f, 0.28f, 0.55f),
    CANNON("Cannon", 150, 75f, 0.24f, 1.4f, splashRadius = 0.08f),
    MAGIC("Magic", 125, 35f, 0.26f, 0.9f, slowsEnemy = true)
}

enum class EnemyType(
    val displayName: String,
    val baseHp: Float,
    val baseSpeed: Float, // normalized units per second
    val goldReward: Int,
    val radiusPercent: Float
) {
    NORMAL("Goblin", 90f, 0.12f, 15, 0.035f),
    FAST("Scout", 50f, 0.20f, 12, 0.028f),
    TANK("Golem", 240f, 0.07f, 30, 0.045f),
    BOSS("Dragon", 700f, 0.06f, 100, 0.065f)
}

data class TowerSlot(
    val id: Int,
    val xPercent: Float,
    val yPercent: Float,
    val isOccupied: Boolean = false
)

data class Waypoint(
    val xPercent: Float,
    val yPercent: Float
)

data class Tower(
    val id: Int,
    val slotId: Int,
    val type: TowerType,
    val level: Int = 1,
    val xPercent: Float,
    val yPercent: Float,
    val damage: Float,
    val range: Float,
    val attackIntervalSec: Float,
    val timeSinceLastShotSec: Float = 0f
) {
    val upgradeCost: Int get() = (type.baseCost * 0.8f * level).toInt()
}

data class Enemy(
    val id: Int,
    val type: EnemyType,
    val maxHp: Float,
    var currentHp: Float,
    var speed: Float,
    var slowTimerSec: Float = 0f,
    var waypointIndex: Int = 0,
    var xPercent: Float,
    var yPercent: Float,
    var isDead: Boolean = false,
    var reachedEnd: Boolean = false
)

data class Projectile(
    val id: Long,
    val startX: Float,
    val startY: Float,
    var currentX: Float,
    var currentY: Float,
    val targetEnemyId: Int,
    val targetX: Float,
    val targetY: Float,
    val speed: Float,
    val damage: Float,
    val towerType: TowerType
)

data class EnemySpawn(
    val type: EnemyType,
    val delaySec: Float
)

data class WaveConfig(
    val waveNumber: Int,
    val spawns: List<EnemySpawn>
)

data class TDLevelConfig(
    val levelNumber: Int,
    val waypoints: List<Waypoint>,
    val slots: List<TowerSlot>,
    val waves: List<WaveConfig>,
    val startingGold: Int = 300,
    val startingLives: Int = 10
)

data class TDGameState(
    val levelNumber: Int = 1,
    val waveIndex: Int = 0,
    val totalWaves: Int = 3,
    val gold: Int = 300,
    val lives: Int = 10,
    val maxLives: Int = 10,
    val towers: List<Tower> = emptyList(),
    val enemies: List<Enemy> = emptyList(),
    val projectiles: List<Projectile> = emptyList(),
    val slots: List<TowerSlot> = emptyList(),
    val selectedSlot: TowerSlot? = null,
    val selectedTower: Tower? = null,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val isWaveInProgress: Boolean = false,
    val isPaused: Boolean = false,
    val gameSpeedMultiplier: Float = 1f
)
