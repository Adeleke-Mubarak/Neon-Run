package com.example.longrunner

import com.example.longrunner.game.collectibles.Collectible
import com.example.longrunner.game.collectibles.CollectibleType
import com.example.longrunner.game.collision.CollisionSystem
import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.core.PhaseEnergyManager
import com.example.longrunner.game.obstacles.ObstacleType
import com.example.longrunner.game.player.PlayerController
import com.example.longrunner.game.track.TrackGenerator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PhaseShiftTest {

    private lateinit var phaseManager: PhaseEnergyManager
    private lateinit var player: PlayerController
    private lateinit var collisionSystem: CollisionSystem
    private lateinit var trackGenerator: TrackGenerator

    @Before
    fun setUp() {
        phaseManager = PhaseEnergyManager()
        phaseManager.reset()
        player = PlayerController()
        player.reset()
        collisionSystem = CollisionSystem()
        trackGenerator = TrackGenerator()
        trackGenerator.reset()
    }

    @Test
    fun testPhaseEnergyConsumptionAndAutoDeactivation() {
        assertEquals(50f, phaseManager.phaseEnergy, 0.001f)
        assertTrue(phaseManager.canActivate)
        assertFalse(phaseManager.isPhaseShiftActive)

        // Activate Phase Shift
        val activated = phaseManager.activatePhaseShift()
        assertTrue(activated)
        assertTrue(phaseManager.isPhaseShiftActive)

        // Advance 2 seconds: consumes 2 * 16 = 32 energy -> 50 - 32 = 18 energy
        phaseManager.update(2.0f)
        assertTrue(phaseManager.isPhaseShiftActive)
        assertEquals(18f, phaseManager.phaseEnergy, 0.1f)
        assertTrue("Phase transition should approach 1.0", phaseManager.phaseFrequencyTransition > 0.8f)

        // Advance 2 more seconds: energy depletes and auto-deactivates
        phaseManager.update(2.0f)
        assertFalse("Phase Shift should auto-deactivate when energy hits 0", phaseManager.isPhaseShiftActive)
        assertEquals(0f, phaseManager.phaseEnergy, 0.001f)
    }

    @Test
    fun testPhaseEnergyRechargeAndCoreCollection() {
        phaseManager.activatePhaseShift()
        phaseManager.update(5.0f) // Drain completely while active (50 - 5 * 16 <= 0)
        assertEquals(0f, phaseManager.phaseEnergy, 0.001f)
        assertFalse(phaseManager.isPhaseShiftActive)

        // Passive recharge at 3.5 energy/sec
        phaseManager.update(2.0f, rechargeModifier = 1.0f)
        assertEquals(7.0f, phaseManager.phaseEnergy, 0.1f)

        // Nova's 1.5x passive recharge modifier
        phaseManager.update(2.0f, rechargeModifier = 1.5f)
        assertEquals(17.5f, phaseManager.phaseEnergy, 0.1f)

        // Collecting Phase Core adds +35 energy
        phaseManager.addEnergy(35.0f)
        assertEquals(52.5f, phaseManager.phaseEnergy, 0.1f)
        assertTrue("Should be able to activate with > 20 energy", phaseManager.canActivate)

        // Max energy ceiling at 100
        phaseManager.addEnergy(100.0f)
        assertEquals(phaseManager.maxPhaseEnergy, phaseManager.phaseEnergy, 0.001f)
    }

    @Test
    fun testPhasePermeableObstaclesPassThrough() {
        val segment = trackGenerator.segments[0]
        val cyberBlock = segment.obstacles[0]
        cyberBlock.setup(ObstacleType.CYBER_BLOCK, GameConstants.LANE_CENTER, player.z)
        assertTrue("Cyber block should be phase-permeable", cyberBlock.isPhasePermeable)

        // In normal world (isPhaseShiftActive = false), player collides
        var hit = collisionSystem.checkObstacleCollision(player, trackGenerator, isPhaseShiftActive = false)
        assertNotNull("Standing runner should hit cyber block in normal world", hit)
        assertEquals(ObstacleType.CYBER_BLOCK, hit?.type)

        // In Phase Shift reality (isPhaseShiftActive = true), permeable obstacle is ethereal
        hit = collisionSystem.checkObstacleCollision(player, trackGenerator, isPhaseShiftActive = true)
        assertNull("Runner should pass safely through cyber block during Phase Shift", hit)
    }

    @Test
    fun testNonPermeableObstaclesStillCollideDuringPhaseShift() {
        val segment = trackGenerator.segments[0]
        val hurdle = segment.obstacles[0]
        hurdle.setup(ObstacleType.LOW_HURDLE, GameConstants.LANE_CENTER, player.z)
        assertFalse("Low hurdle should NOT be phase-permeable", hurdle.isPhasePermeable)

        // Low hurdle must still be jumped even during Phase Shift
        val hit = collisionSystem.checkObstacleCollision(player, trackGenerator, isPhaseShiftActive = true)
        assertNotNull("Runner must still jump over low hurdle in Phase Shift", hit)
        assertEquals(ObstacleType.LOW_HURDLE, hit?.type)
    }

    @Test
    fun testPhaseExclusiveCollectiblesHarvesting() {
        val segment = trackGenerator.segments[0]
        val secretCore = segment.collectibles[0]
        secretCore.setup(
            lane = GameConstants.LANE_CENTER,
            y = 0.8f,
            z = player.z,
            type = CollectibleType.PHASE_CORE,
            isPhaseExclusive = true
        )
        assertTrue(secretCore.isActive)
        assertTrue(secretCore.isPhaseExclusive)

        var collectedCount = 0

        // In normal reality, exclusive collectible cannot be picked up
        collisionSystem.checkCollectiblePickups(player, trackGenerator, isPhaseShiftActive = false) {
            collectedCount++
        }
        assertEquals(0, collectedCount)
        assertTrue(secretCore.isActive)

        // In Phase Reality, collectible is successfully harvested
        collisionSystem.checkCollectiblePickups(player, trackGenerator, isPhaseShiftActive = true) {
            collectedCount++
        }
        assertEquals(1, collectedCount)
        assertFalse(secretCore.isActive)
    }
}
