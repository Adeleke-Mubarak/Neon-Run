package com.example.longrunner.game.progression

enum class MissionType(val displayName: String) {
    DISTANCE_TOTAL("Distance Covered"),
    DISTANCE_SINGLE_RUN("Single Run Distance"),
    COLLECT_SHARDS("Energy Shards"),
    COLLECT_CORES("Phase Cores"),
    COLLECT_CREDITS("Credits Found"),
    COMBO_TARGET("Combo Multiplier"),
    NEAR_MISS_COUNT("Near Miss Dodges"),
    SURVIVE_FRACTURES("Fractures Cleared"),
    SURVIVE_BLACKOUTS("Blackouts Survived"),
    ACTIVATE_ABILITY("Ability Activations"),
    ACTIVATE_PHASE_SHIFT("Phase Shift Transitions"),
    REPEL_NULL("Repel The Null"),
    SMASH_CRATES("Crates Smashed")
}

enum class MissionTier(val displayName: String, val badgeColorHex: Long) {
    BRONZE("STANDARD PROTOCOL", 0xFFCD7F32),
    SILVER("TACTICAL DIRECTIVE", 0xFFC0C0C0),
    GOLD("ELITE BOUNTY", 0xFFFFD700),
    DAILY("DAILY OVERRIDE", 0xFF00F0FF)
}

data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val type: MissionType,
    val tier: MissionTier,
    val targetProgress: Int,
    var currentProgress: Int = 0,
    var isCompleted: Boolean = false,
    var isClaimed: Boolean = false,
    val xpReward: Int,
    val creditReward: Int
) {
    val progressRatio: Float
        get() = if (targetProgress > 0) (currentProgress.toFloat() / targetProgress).coerceIn(0f, 1f) else 1f

    val progressPercent: Int
        get() = (progressRatio * 100).toInt()

    fun updateProgress(amount: Int): Boolean {
        if (isCompleted) return false
        currentProgress = (currentProgress + amount).coerceAtMost(targetProgress)
        if (currentProgress >= targetProgress) {
            isCompleted = true
            return true
        }
        return false
    }

    fun setProgressValue(value: Int): Boolean {
        if (isCompleted) return false
        currentProgress = value.coerceIn(0, targetProgress)
        if (currentProgress >= targetProgress) {
            isCompleted = true
            return true
        }
        return false
    }
}
