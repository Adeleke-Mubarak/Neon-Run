package com.example.longrunner.game.progression

enum class AchievementId {
    FIRST_STEPS,         // Run 500m total
    FRACTURE_PIONEER,    // Survive 1st Route Fracture
    VOID_DODGER,         // Push The Null from Critical (<8m) back to Safe (>14m)
    OVERDRIVE_RAMPAGE,   // Destroy 5 obstacles with Overdrive
    COMBO_MASTER,        // Reach an 8x combo streak
    CREDIT_TYCOON,       // Accumulate 500+ credits in the vault
    PHASE_ARCHITECT,     // Spend 30s total in Phase Shift
    NEAR_MISS_SURGEON,   // Perform 10 near misses in one run
    ENDLESS_RUNNER,      // Travel 2,000m in a single run
    SWARM_EVADER;        // Survive a Drone Swarm without colliding

    companion object {
        @JvmField
        val BLACKOUT_NINJA = SWARM_EVADER
    }
}

data class Achievement(
    val id: AchievementId,
    val title: String,
    val description: String,
    val icon: String,
    val xpReward: Int,
    val creditReward: Int,
    var isUnlocked: Boolean = false,
    var unlockedTimestamp: Long = 0L
)
