package com.example.longrunner.game.progression

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * Manages active player missions, daily contracts, progress persistence,
 * completion validation, and reward claims.
 */
class MissionManager(
    context: Context? = null,
    private val random: Random = Random.Default
) {
    private val prefs: SharedPreferences? = context?.getSharedPreferences("neon_run_missions", Context.MODE_PRIVATE)

    private val _activeMissions = mutableListOf<Mission>()
    val activeMissions: List<Mission> get() = _activeMissions

    var totalMissionsCompleted: Int = prefs?.getInt("total_missions_completed", 0) ?: 0
        private set

    private var lastDailyDate: String = prefs?.getString("last_daily_date", "") ?: ""

    // Callbacks
    var onMissionCompleted: ((Mission) -> Unit)? = null

    init {
        loadOrInitializeMissions()
    }

    private fun loadOrInitializeMissions() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        _activeMissions.clear()

        // Slot 0: Bronze, Slot 1: Silver, Slot 2: Gold
        val savedSlot0 = loadMissionFromPrefs("slot_0")
        val savedSlot1 = loadMissionFromPrefs("slot_1")
        val savedSlot2 = loadMissionFromPrefs("slot_2")
        val savedDaily = loadMissionFromPrefs("slot_daily")

        _activeMissions.add(savedSlot0 ?: generateMissionForTier(MissionTier.BRONZE, "slot_0"))
        _activeMissions.add(savedSlot1 ?: generateMissionForTier(MissionTier.SILVER, "slot_1"))
        _activeMissions.add(savedSlot2 ?: generateMissionForTier(MissionTier.GOLD, "slot_2"))

        // Check daily reset
        if (savedDaily != null && lastDailyDate == today) {
            _activeMissions.add(savedDaily)
        } else {
            val newDaily = generateMissionForTier(MissionTier.DAILY, "daily_${System.currentTimeMillis()}")
            lastDailyDate = today
            prefs?.edit()?.putString("last_daily_date", today)?.apply()
            _activeMissions.add(newDaily)
        }

        saveAllMissions()
    }

    /**
     * Report progress for a specific mission type.
     * Returns list of missions completed by this update.
     */
    fun addProgress(type: MissionType, amount: Int): List<Mission> {
        if (amount <= 0) return emptyList()
        val completedNow = mutableListOf<Mission>()

        for (mission in _activeMissions) {
            if (mission.type == type && !mission.isCompleted) {
                val justCompleted = mission.updateProgress(amount)
                if (justCompleted) {
                    totalMissionsCompleted++
                    prefs?.edit()?.putInt("total_missions_completed", totalMissionsCompleted)?.apply()
                    completedNow.add(mission)
                    onMissionCompleted?.invoke(mission)
                }
            }
        }

        if (completedNow.isNotEmpty()) {
            saveAllMissions()
        }
        return completedNow
    }

    /**
     * Sets absolute progress (useful for single-run distance or combo peaks).
     */
    fun setPeakProgress(type: MissionType, peakValue: Int): List<Mission> {
        val completedNow = mutableListOf<Mission>()
        for (mission in _activeMissions) {
            if (mission.type == type && !mission.isCompleted) {
                if (peakValue > mission.currentProgress) {
                    val justCompleted = mission.setProgressValue(peakValue)
                    if (justCompleted) {
                        totalMissionsCompleted++
                        prefs?.edit()?.putInt("total_missions_completed", totalMissionsCompleted)?.apply()
                        completedNow.add(mission)
                        onMissionCompleted?.invoke(mission)
                    }
                }
            }
        }
        if (completedNow.isNotEmpty()) {
            saveAllMissions()
        }
        return completedNow
    }

    /**
     * Resets run-scoped progress for single-run missions at start of a run.
     */
    fun onRunStarted() {
        var changed = false
        for (mission in _activeMissions) {
            if (mission.type == MissionType.DISTANCE_SINGLE_RUN && !mission.isCompleted) {
                mission.currentProgress = 0
                changed = true
            }
        }
        if (changed) {
            saveAllMissions()
        }
    }

    /**
     * Claims rewards for a completed mission and rolls a new one into that slot.
     * Returns Pair(XP, Credits) or null if unable to claim.
     */
    fun claimReward(missionId: String): Pair<Int, Int>? {
        val index = _activeMissions.indexOfFirst { it.id == missionId }
        if (index == -1) return null

        val mission = _activeMissions[index]
        if (!mission.isCompleted || mission.isClaimed) return null

        mission.isClaimed = true
        val reward = Pair(mission.xpReward, mission.creditReward)

        // Replace with new mission of same tier
        val newMission = generateMissionForTier(mission.tier, "slot_${index}_${System.currentTimeMillis()}")
        _activeMissions[index] = newMission
        saveAllMissions()

        return reward
    }

    /**
     * Claims all currently completed missions at once.
     * Returns total Pair(totalXP, totalCredits).
     */
    fun claimAllCompleted(): Pair<Int, Int> {
        var totalXp = 0
        var totalCredits = 0

        val completedIds = _activeMissions.filter { it.isCompleted && !it.isClaimed }.map { it.id }
        for (id in completedIds) {
            val reward = claimReward(id)
            if (reward != null) {
                totalXp += reward.first
                totalCredits += reward.second
            }
        }
        return Pair(totalXp, totalCredits)
    }

    fun generateMissionForTier(tier: MissionTier, id: String): Mission {
        return when (tier) {
            MissionTier.BRONZE -> {
                val templates = listOf(
                    Mission(id, "Street Sprint", "Run 1,200 meters in total", MissionType.DISTANCE_TOTAL, tier, 1200, 0, false, false, 120, 50),
                    Mission(id, "Energy Sweep", "Collect 80 Energy Shards", MissionType.COLLECT_SHARDS, tier, 80, 0, false, false, 100, 45),
                    Mission(id, "Close Call", "Perform 5 Near Misses", MissionType.NEAR_MISS_COUNT, tier, 5, 0, false, false, 130, 55),
                    Mission(id, "Phase Shift Intake", "Activate Phase Shift 3 times", MissionType.ACTIVATE_PHASE_SHIFT, tier, 3, 0, false, false, 110, 40),
                    Mission(id, "Credit Seeker", "Collect 25 Credits", MissionType.COLLECT_CREDITS, tier, 25, 0, false, false, 120, 60)
                )
                templates[random.nextInt(templates.size)]
            }
            MissionTier.SILVER -> {
                val templates = listOf(
                    Mission(id, "Sector Marathon", "Run 1,000 meters in a single run", MissionType.DISTANCE_SINGLE_RUN, tier, 1000, 0, false, false, 250, 100),
                    Mission(id, "Rhythm Master", "Reach a 6x Combo Multiplier", MissionType.COMBO_TARGET, tier, 6, 0, false, false, 280, 110),
                    Mission(id, "Quantum Crossing", "Survive 2 Route Fractures", MissionType.SURVIVE_FRACTURES, tier, 2, 0, false, false, 300, 120),
                    Mission(id, "Core Harvester", "Collect 5 Phase Cores", MissionType.COLLECT_CORES, tier, 5, 0, false, false, 260, 95),
                    Mission(id, "Void Repulsion", "Repel The Null 3 times", MissionType.REPEL_NULL, tier, 3, 0, false, false, 270, 105),
                    Mission(id, "Demolition Run", "Smash 5 Breakable Crates", MissionType.SMASH_CRATES, tier, 5, 0, false, false, 240, 90)
                )
                templates[random.nextInt(templates.size)]
            }
            MissionTier.GOLD -> {
                val templates = listOf(
                    Mission(id, "Grand Circuit", "Cover 3,500 meters total across runs", MissionType.DISTANCE_TOTAL, tier, 3500, 0, false, false, 500, 220),
                    Mission(id, "Swarm Survivor", "Survive a Drone Swarm event", MissionType.SURVIVE_DRONE_SWARMS, tier, 1, 0, false, false, 480, 200),
                    Mission(id, "Acrobatic Ace", "Execute 15 Near Misses", MissionType.NEAR_MISS_COUNT, tier, 15, 0, false, false, 520, 230),
                    Mission(id, "Apex Multiplier", "Reach an 8x Combo Multiplier", MissionType.COMBO_TARGET, tier, 8, 0, false, false, 550, 250),
                    Mission(id, "Operative Overclock", "Activate character abilities 6 times", MissionType.ACTIVATE_ABILITY, tier, 6, 0, false, false, 460, 190)
                )
                templates[random.nextInt(templates.size)]
            }
            MissionTier.DAILY -> {
                val templates = listOf(
                    Mission(id, "Daily Protocol: Shard Torrent", "Collect 150 Energy Shards", MissionType.COLLECT_SHARDS, tier, 150, 0, false, false, 400, 160),
                    Mission(id, "Daily Protocol: Fracture Scout", "Clear 3 Route Fractures", MissionType.SURVIVE_FRACTURES, tier, 3, 0, false, false, 450, 180),
                    Mission(id, "Daily Protocol: Deep Sector Run", "Travel 1,500m in a single run", MissionType.DISTANCE_SINGLE_RUN, tier, 1500, 0, false, false, 420, 170),
                    Mission(id, "Daily Protocol: Bank Heist", "Collect 60 Credits in the field", MissionType.COLLECT_CREDITS, tier, 60, 0, false, false, 380, 200)
                )
                templates[random.nextInt(templates.size)]
            }
        }
    }

    private fun saveAllMissions() {
        val editor = prefs?.edit() ?: return
        for (i in 0 until _activeMissions.size) {
            val key = if (i == 3) "slot_daily" else "slot_$i"
            val m = _activeMissions[i]
            editor.putString("${key}_id", m.id)
            editor.putString("${key}_title", m.title)
            editor.putString("${key}_desc", m.description)
            editor.putString("${key}_type", m.type.name)
            editor.putString("${key}_tier", m.tier.name)
            editor.putInt("${key}_target", m.targetProgress)
            editor.putInt("${key}_current", m.currentProgress)
            editor.putBoolean("${key}_completed", m.isCompleted)
            editor.putBoolean("${key}_claimed", m.isClaimed)
            editor.putInt("${key}_xp", m.xpReward)
            editor.putInt("${key}_credits", m.creditReward)
        }
        editor.apply()
    }

    private fun loadMissionFromPrefs(key: String): Mission? {
        val p = prefs ?: return null
        val id = p.getString("${key}_id", null) ?: return null
        val title = p.getString("${key}_title", "") ?: ""
        val desc = p.getString("${key}_desc", "") ?: ""
        val typeStr = p.getString("${key}_type", "") ?: ""
        val tierStr = p.getString("${key}_tier", "") ?: ""
        val target = p.getInt("${key}_target", 100)
        val current = p.getInt("${key}_current", 0)
        val completed = p.getBoolean("${key}_completed", false)
        val claimed = p.getBoolean("${key}_claimed", false)
        val xp = p.getInt("${key}_xp", 100)
        val credits = p.getInt("${key}_credits", 50)

        val type = try { MissionType.valueOf(typeStr) } catch (_: Exception) { MissionType.DISTANCE_TOTAL }
        val tier = try { MissionTier.valueOf(tierStr) } catch (_: Exception) { MissionTier.BRONZE }

        return Mission(id, title, desc, type, tier, target, current, completed, claimed, xp, credits)
    }
}
