package com.example.longrunner.game.progression

import android.content.Context
import android.content.SharedPreferences

/**
 * Tracks permanent prestige achievements, evaluates unlock criteria,
 * and distributes prestige rewards.
 */
class AchievementManager(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("neon_run_achievements", Context.MODE_PRIVATE)

    private val _achievements = mutableListOf<Achievement>()
    val achievements: List<Achievement> get() = _achievements

    var onAchievementUnlocked: ((Achievement) -> Unit)? = null

    init {
        initializeAchievements()
    }

    private fun initializeAchievements() {
        val list = listOf(
            Achievement(AchievementId.FIRST_STEPS, "First Contact", "Run 500 meters in total", "👟", 150, 60),
            Achievement(AchievementId.FRACTURE_PIONEER, "Quantum Pioneer", "Choose and clear a Route Fracture", "⚡", 300, 100),
            Achievement(AchievementId.VOID_DODGER, "Void Dodger", "Push The Null back to safety from Critical range", "👁️", 450, 160),
            Achievement(AchievementId.OVERDRIVE_RAMPAGE, "Apex Breaker", "Plow through 5 obstacles while in Overdrive", "💥", 350, 120),
            Achievement(AchievementId.COMBO_MASTER, "Flow State", "Reach an 8x combo multiplier streak", "🔥", 500, 200),
            Achievement(AchievementId.CREDIT_TYCOON, "Neon Tycoon", "Accumulate 500 or more credits in the vault", "💰", 400, 150),
            Achievement(AchievementId.PHASE_ARCHITECT, "Phase Maestro", "Activate Phase Shift 15 times", "🌀", 450, 160),
            Achievement(AchievementId.NEAR_MISS_SURGEON, "Hair's Breadth", "Execute 10 Near Misses in a single run", "🎯", 450, 180),
            Achievement(AchievementId.ENDLESS_RUNNER, "Endless Instinct", "Survive past 2,000 meters in a single run", "🏆", 800, 350),
            Achievement(AchievementId.SWARM_EVADER, "Swarm Evader", "Survive a Drone Swarm event without collision", "🛸", 500, 200)
        )

        _achievements.clear()
        for (item in list) {
            val unlocked = prefs?.getBoolean("${item.id.name}_unlocked", false) ?: false
            val timestamp = prefs?.getLong("${item.id.name}_timestamp", 0L) ?: 0L
            item.isUnlocked = unlocked
            item.unlockedTimestamp = timestamp
            _achievements.add(item)
        }
    }

    fun isUnlocked(id: AchievementId): Boolean {
        return _achievements.find { it.id == id }?.isUnlocked ?: false
    }

    fun unlock(id: AchievementId): Achievement? {
        val ach = _achievements.find { it.id == id } ?: return null
        if (ach.isUnlocked) return null

        ach.isUnlocked = true
        ach.unlockedTimestamp = System.currentTimeMillis()

        prefs?.edit()
            ?.putBoolean("${ach.id.name}_unlocked", true)
            ?.putLong("${ach.id.name}_timestamp", ach.unlockedTimestamp)
            ?.apply()

        onAchievementUnlocked?.invoke(ach)
        return ach
    }

    /**
     * Checks criteria based on run metrics.
     * Returns list of newly unlocked achievements.
     */
    fun checkMetrics(
        distance: Float,
        maxCombo: Int,
        nearMisses: Int,
        vaultCredits: Int
    ): List<Achievement> {
        val unlocked = mutableListOf<Achievement>()

        if (distance >= 500f && !isUnlocked(AchievementId.FIRST_STEPS)) {
            unlock(AchievementId.FIRST_STEPS)?.let { unlocked.add(it) }
        }
        if (distance >= 2000f && !isUnlocked(AchievementId.ENDLESS_RUNNER)) {
            unlock(AchievementId.ENDLESS_RUNNER)?.let { unlocked.add(it) }
        }
        if (maxCombo >= 8 && !isUnlocked(AchievementId.COMBO_MASTER)) {
            unlock(AchievementId.COMBO_MASTER)?.let { unlocked.add(it) }
        }
        if (nearMisses >= 10 && !isUnlocked(AchievementId.NEAR_MISS_SURGEON)) {
            unlock(AchievementId.NEAR_MISS_SURGEON)?.let { unlocked.add(it) }
        }
        if (vaultCredits >= 500 && !isUnlocked(AchievementId.CREDIT_TYCOON)) {
            unlock(AchievementId.CREDIT_TYCOON)?.let { unlocked.add(it) }
        }

        return unlocked
    }
}
