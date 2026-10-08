package com.example.longrunner

import com.example.longrunner.game.progression.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProgressionAndMissionsTest {

    private lateinit var progressionManager: ProgressionManager
    private lateinit var missionManager: MissionManager
    private lateinit var achievementManager: AchievementManager

    @Before
    fun setUp() {
        // Run with null context for pure in-memory JVM test execution
        progressionManager = ProgressionManager(null)
        missionManager = MissionManager(null)
        achievementManager = AchievementManager(null)
    }

    @Test
    fun testProgressionInitialState() {
        assertEquals(1, progressionManager.currentRank)
        assertEquals("CYBER CADET", progressionManager.rankTitle)
        assertEquals(0L, progressionManager.totalXp)
        assertEquals(0, progressionManager.currentRankXp)
        assertEquals(500, progressionManager.xpToNextRank)
        assertEquals(0f, progressionManager.rankProgress, 0.001f)
        assertEquals(0, progressionManager.permanentMultiplierBonus)
    }

    @Test
    fun testProgressionXpGainAndSingleRankUp() {
        var promotedToRank = 0
        var promotedTitle = ""
        var creditBounty = 0
        progressionManager.onRankUp = { newRank, title, bounty ->
            promotedToRank = newRank
            promotedTitle = title
            creditBounty = bounty
        }

        // Add partial XP
        val rankedUp1 = progressionManager.addXp(300)
        assertFalse(rankedUp1)
        assertEquals(1, progressionManager.currentRank)
        assertEquals(300L, progressionManager.totalXp)
        assertEquals(300, progressionManager.currentRankXp)
        assertEquals(500, progressionManager.xpToNextRank)
        assertEquals(0.6f, progressionManager.rankProgress, 0.001f)
        assertEquals(0, promotedToRank)

        // Add remaining XP to trigger rank up (300 + 250 = 550 XP, overflow 50 XP into Rank 2)
        val rankedUp2 = progressionManager.addXp(250)
        assertTrue(rankedUp2)
        assertEquals(2, progressionManager.currentRank)
        assertEquals("STREET RUNNER", progressionManager.rankTitle)
        assertEquals(550L, progressionManager.totalXp)
        assertEquals(50, progressionManager.currentRankXp)
        // Rank 2 XP required = 500 + (2 - 1) * 300 = 800
        assertEquals(800, progressionManager.xpToNextRank)
        assertEquals(50f / 800f, progressionManager.rankProgress, 0.001f)

        // Promotion callback verification
        assertEquals(2, promotedToRank)
        assertEquals("STREET RUNNER", promotedTitle)
        assertEquals(300, creditBounty) // bounty = 2 * 150 = 300 credits
    }

    @Test
    fun testProgressionMultiplierBonusProgression() {
        // Rank 1..4: bonus = (rank - 1) / 4 -> R1: 0, R2: 0, R3: 0, R4: 0, R5: 1
        assertEquals(0, progressionManager.permanentMultiplierBonus)

        // Promote to Rank 5: need R1(500) + R2(800) + R3(1100) + R4(1400) = 3800 XP
        progressionManager.addXp(3800)
        assertEquals(5, progressionManager.currentRank)
        assertEquals("PHANTOM OPERATIVE", progressionManager.rankTitle)
        assertEquals(1, progressionManager.permanentMultiplierBonus) // (5 - 1) / 4 = 1

        // Promote to Rank 9 (bonus = 2)
        // R5(1700) + R6(2000) + R7(2300) + R8(2600) = 8600 XP
        progressionManager.addXp(8600)
        assertEquals(9, progressionManager.currentRank)
        assertEquals("SECTOR ARCHITECT", progressionManager.rankTitle)
        assertEquals(2, progressionManager.permanentMultiplierBonus) // (9 - 1) / 4 = 2
    }

    @Test
    fun testProgressionMaxRankCap() {
        // Add massive XP to max out
        progressionManager.addXp(200_000)
        assertEquals(progressionManager.maxRank, progressionManager.currentRank)
        assertEquals(20, progressionManager.currentRank)
        assertEquals("GRAND ARCHITECT", progressionManager.rankTitle)
        assertEquals(4, progressionManager.permanentMultiplierBonus) // (20 - 1) / 4 = 4
        assertEquals(1f, progressionManager.rankProgress, 0.001f)
    }

    @Test
    fun testMissionManagerInitialActiveSlots() {
        val missions = missionManager.activeMissions
        assertEquals(4, missions.size)

        val tiers = missions.map { it.tier }
        assertTrue(tiers.contains(MissionTier.BRONZE))
        assertTrue(tiers.contains(MissionTier.SILVER))
        assertTrue(tiers.contains(MissionTier.GOLD))
        assertTrue(tiers.contains(MissionTier.DAILY))

        // Initial progress is 0
        missions.forEach { mission ->
            assertFalse(mission.isCompleted)
            assertFalse(mission.isClaimed)
            assertEquals(0, mission.currentProgress)
        }
    }

    @Test
    fun testMissionProgressAndCompletion() {
        var completedMission: Mission? = null
        missionManager.onMissionCompleted = { mission ->
            completedMission = mission
        }

        // Find the active bronze mission
        val bronze = missionManager.activeMissions.first { it.tier == MissionTier.BRONZE }
        val targetType = bronze.type
        val targetGoal = bronze.targetProgress

        // Advance progress partially
        missionManager.addProgress(targetType, targetGoal / 2)
        val partiallyUpdated = missionManager.activeMissions.first { it.id == bronze.id }
        assertFalse(partiallyUpdated.isCompleted)
        assertEquals(targetGoal / 2, partiallyUpdated.currentProgress)
        assertNull(completedMission)

        // Advance progress to completion
        missionManager.addProgress(targetType, targetGoal)
        val completed = missionManager.activeMissions.first { it.id == bronze.id }
        assertTrue(completed.isCompleted)
        assertNotNull(completedMission)
        assertEquals(bronze.id, completedMission?.id)
    }

    @Test
    fun testMissionRewardClaimingAndReplenishment() {
        val initialMissions = missionManager.activeMissions
        val silver = initialMissions.first { it.tier == MissionTier.SILVER }

        // Complete the mission directly
        missionManager.addProgress(silver.type, silver.targetProgress)
        assertTrue(missionManager.activeMissions.first { it.id == silver.id }.isCompleted)

        // Claim reward
        val reward = missionManager.claimReward(silver.id)
        assertNotNull(reward)
        assertEquals(silver.xpReward, reward?.first)
        assertEquals(silver.creditReward, reward?.second)

        // Verify total completed count incremented
        assertEquals(1, missionManager.totalMissionsCompleted)

        // Verify slot was replenished with a fresh mission of the same tier
        val replenishedMissions = missionManager.activeMissions
        assertEquals(4, replenishedMissions.size)
        val newSilver = replenishedMissions.first { it.tier == MissionTier.SILVER }
        assertNotEquals(silver.id, newSilver.id)
        assertFalse(newSilver.isCompleted)
        assertFalse(newSilver.isClaimed)
    }

    @Test
    fun testMissionClaimAllCompleted() {
        // Complete both Bronze and Silver
        val bronze = missionManager.activeMissions.first { it.tier == MissionTier.BRONZE }
        val silver = missionManager.activeMissions.first { it.tier == MissionTier.SILVER }

        missionManager.addProgress(bronze.type, bronze.targetProgress)
        missionManager.addProgress(silver.type, silver.targetProgress)

        val expectedXp = bronze.xpReward + silver.xpReward
        val expectedCredits = bronze.creditReward + silver.creditReward

        val claimed = missionManager.claimAllCompleted()
        assertEquals(expectedXp, claimed.first)
        assertEquals(expectedCredits, claimed.second)
        assertEquals(2, missionManager.totalMissionsCompleted)

        // All active missions should now be freshly uncompleted
        assertEquals(4, missionManager.activeMissions.size)
        missionManager.activeMissions.forEach {
            assertFalse(it.isCompleted)
        }
    }

    @Test
    fun testAchievementEvaluationAndUnlock() {
        val unlockedList = mutableListOf<Achievement>()
        achievementManager.onAchievementUnlocked = { ach ->
            unlockedList.add(ach)
        }

        assertEquals(10, achievementManager.achievements.size)
        assertEquals(0, achievementManager.achievements.count { it.isUnlocked })

        // Check metrics that satisfy FIRST_STEPS (distance >= 500) and COMBO_MASTER (combo >= 8)
        val unlocked = achievementManager.checkMetrics(
            distance = 600f,
            maxCombo = 10,
            nearMisses = 2,
            vaultCredits = 100
        )

        assertEquals(2, unlocked.size)
        assertEquals(2, achievementManager.achievements.count { it.isUnlocked })
        val unlockedIds = unlocked.map { it.id }
        assertTrue(unlockedIds.contains(AchievementId.FIRST_STEPS))
        assertTrue(unlockedIds.contains(AchievementId.COMBO_MASTER))

        // Check achievement model properties
        assertTrue(achievementManager.isUnlocked(AchievementId.FIRST_STEPS))
        val firstSteps = achievementManager.achievements.find { it.id == AchievementId.FIRST_STEPS }
        assertNotNull(firstSteps)
        assertTrue(firstSteps!!.isUnlocked)
        assertTrue(firstSteps.unlockedTimestamp > 0L)
    }

    @Test
    fun testAchievementManualUnlock() {
        // Manual unlock of Void Dodger
        val firstUnlock = achievementManager.unlock(AchievementId.VOID_DODGER)
        assertNotNull(firstUnlock)
        assertTrue(achievementManager.isUnlocked(AchievementId.VOID_DODGER))
        assertEquals(1, achievementManager.achievements.count { it.isUnlocked })

        // Attempting to unlock again should return null (idempotent)
        val secondUnlock = achievementManager.unlock(AchievementId.VOID_DODGER)
        assertNull(secondUnlock)
        assertEquals(1, achievementManager.achievements.count { it.isUnlocked })
    }
}
