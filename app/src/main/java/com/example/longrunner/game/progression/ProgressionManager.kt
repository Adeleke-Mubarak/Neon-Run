package com.example.longrunner.game.progression

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages player operative rank, experience (XP) curves,
 * level milestone rewards, and permanent prestige perks.
 */
class ProgressionManager(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("neon_run_progression", Context.MODE_PRIVATE)

    var totalXp: Long = prefs?.getLong("total_xp", 0L) ?: 0L
        private set

    var currentRank: Int = prefs?.getInt("current_rank", 1) ?: 1
        private set

    var currentRankXp: Int = prefs?.getInt("current_rank_xp", 0) ?: 0
        private set

    val maxRank: Int = 20

    val rankTitle: String
        get() = getTitleForRank(currentRank)

    val xpToNextRank: Int
        get() = getXpRequiredForRank(currentRank)

    val rankProgress: Float
        get() = if (currentRank >= maxRank) 1.0f else (currentRankXp.toFloat() / xpToNextRank).coerceIn(0f, 1f)

    /**
     * Permanent passive score multiplier bonus earned from operative rank progression.
     * (+1x at Rank 4, Rank 8, Rank 12, Rank 16)
     */
    val permanentMultiplierBonus: Int
        get() = (currentRank - 1) / 4

    // Callbacks
    var onRankUp: ((newRank: Int, title: String, rewardCredits: Int) -> Unit)? = null

    /**
     * Grants XP to the operative and evaluates rank-ups.
     * Returns true if one or more rank-ups occurred.
     */
    fun addXp(amount: Int): Boolean {
        if (amount <= 0) return false
        totalXp += amount
        currentRankXp += amount

        var rankedUp = false
        while (currentRank < maxRank && currentRankXp >= xpToNextRank) {
            currentRankXp -= xpToNextRank
            currentRank++
            rankedUp = true

            val creditBounty = currentRank * 150
            onRankUp?.invoke(currentRank, rankTitle, creditBounty)
        }

        if (currentRank >= maxRank) {
            currentRankXp = 0
        }

        save()
        return rankedUp
    }

    fun getXpRequiredForRank(rank: Int): Int {
        return 500 + (rank - 1) * 300
    }

    private fun getTitleForRank(rank: Int): String {
        return when (rank) {
            1 -> "CYBER CADET"
            2 -> "STREET RUNNER"
            3 -> "NEON DRIFTER"
            4 -> "GRID RUNNER"
            5 -> "PHANTOM OPERATIVE"
            6 -> "QUANTUM STRIDER"
            7 -> "VOID GLIDER"
            8 -> "APEX STALKER"
            9 -> "SECTOR ARCHITECT"
            10 -> "FRACTURE LEGEND"
            11 -> "CHRONO DRIFTER"
            12 -> "CYBER REAPER"
            13 -> "NEON VANGUARD"
            14 -> "DIMENSION WALKER"
            15 -> "NEXUS OVERLORD"
            16 -> "ECLIPSE SPECIALIST"
            17 -> "NULL BREAKER"
            18 -> "INFINITY OPERATIVE"
            19 -> "ASCENDED RUNNER"
            else -> "GRAND ARCHITECT"
        }
    }

    private fun save() {
        prefs?.edit()
            ?.putLong("total_xp", totalXp)
            ?.putInt("current_rank", currentRank)
            ?.putInt("current_rank_xp", currentRankXp)
            ?.apply()
    }
}
