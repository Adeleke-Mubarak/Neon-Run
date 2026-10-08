package com.example.longrunner

import com.example.longrunner.game.collectibles.CollectibleType
import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.core.GameEngine
import com.example.longrunner.game.core.GameState
import com.example.longrunner.game.obstacles.ObstacleType
import com.example.longrunner.game.player.CharacterData
import com.example.longrunner.game.powerups.PowerUpType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameEngineIntegrationSimulationTest {

    private lateinit var engine: GameEngine

    @Before
    fun setUp() {
        // Instantiate without Android Context for pure JVM in-memory test execution
        engine = GameEngine(null)
    }

    @Test
    fun testInitialEngineState() {
        assertEquals(GameState.READY, engine.state)
        assertEquals(0, engine.scoreManager.score)
        assertEquals(0f, engine.scoreManager.distance, 0.001f)
        assertEquals(1, engine.comboManager.currentCombo)
        assertEquals(CharacterData.KAI.id, engine.player.characterData.id)
        assertFalse(engine.musicManager.isPlaying)
    }

    @Test
    fun testStartRunAndMovementInputs() {
        engine.startRun()
        assertEquals(GameState.RUNNING, engine.state)
        assertTrue(engine.musicManager.isPlaying)

        // Swipe left
        engine.onSwipeLeft()
        assertEquals(GameConstants.LANE_LEFT, engine.player.targetLane)

        // Swipe right twice
        engine.onSwipeRight()
        assertEquals(GameConstants.LANE_CENTER, engine.player.targetLane)
        engine.onSwipeRight()
        assertEquals(GameConstants.LANE_RIGHT, engine.player.targetLane)

        // Swipe up (jump)
        engine.onSwipeUp()
        assertTrue(engine.player.isJumping)

        // Advance simulation
        engine.update(0.1f)
        assertTrue(engine.scoreManager.distance > 0f)

        // Swipe down (slide)
        engine.onSwipeDown()
        assertTrue(engine.player.isSliding)
    }

    @Test
    fun testAbilityAndPhaseShiftExecution() {
        engine.startRun()

        // Activate Kai's ability
        val activated = engine.activateAbility()
        assertTrue(activated)
        assertTrue(engine.player.isAbilityActive)

        // Toggle phase shift
        engine.phaseEnergyManager.addEnergy(50f)
        val phaseActive = engine.togglePhaseShift()
        assertTrue(phaseActive)
        assertTrue(engine.phaseEnergyManager.isPhaseShiftActive)

        // Update simulation
        engine.update(0.2f)
        assertTrue(engine.phaseEnergyManager.energyProgress > 0f)
    }

    @Test
    fun testPowerUpOverdriveRampageSimulation() {
        engine.startRun()

        // Activate Overdrive
        engine.powerUpManager.activatePowerUp(PowerUpType.OVERDRIVE)
        assertTrue(engine.powerUpManager.isOverdriveActive)

        // Place obstacle directly in front of player
        val segment = engine.trackGenerator.segments[0]
        val obs = segment.obstacles[0]
        obs.setup(ObstacleType.CYBER_BLOCK, engine.player.currentLane, engine.player.z - 0.9f)
        assertTrue(obs.isActive)

        // Update simulation -> Overdrive should smash through obstacle without causing Game Over
        engine.update(0.05f)
        assertEquals(GameState.RUNNING, engine.state)
        assertFalse("Overdrive must smash obstacle", obs.isActive)
        assertTrue(engine.scoreManager.score > 0L)
    }

    @Test
    fun testShieldAbsorbsCollisionGracefully() {
        engine.startRun()

        // Activate Kinetic Shield
        engine.powerUpManager.activatePowerUp(PowerUpType.KINETIC_SHIELD)
        assertTrue(engine.powerUpManager.hasShield)

        // Place low hurdle in front of player
        val segment = engine.trackGenerator.segments[0]
        val obs = segment.obstacles[0]
        obs.setup(ObstacleType.LOW_HURDLE, engine.player.currentLane, engine.player.z - 0.9f)

        // Update simulation -> Shield absorbs collision
        engine.update(0.05f)
        assertEquals(GameState.RUNNING, engine.state)
        assertFalse("Shield must be consumed on collision", engine.powerUpManager.hasShield)
        assertTrue("Player receives grace invulnerability period", engine.powerUpManager.isInvulnerable)
    }

    @Test
    fun testCollectiblePickupSimulation() {
        engine.startRun()

        // Place Energy Shard and Credit at player's position
        val segment = engine.trackGenerator.segments[0]
        segment.collectibles[0].setup(engine.player.currentLane, engine.player.y + 0.5f, engine.player.z - 0.5f, CollectibleType.ENERGY_SHARD)
        segment.collectibles[1].setup(engine.player.currentLane, engine.player.y + 0.5f, engine.player.z - 0.5f, CollectibleType.CREDIT)

        engine.update(0.1f)

        assertTrue("Shards collected count incremented", engine.scoreManager.shardsCollected > 0)
        assertTrue("Credits collected count incremented", engine.scoreManager.creditsCollected > 0)
    }

    @Test
    fun testGameOverAndRestartCycle() {
        engine.startRun()
        val initialVaultCredits = engine.characterManager.totalBankCredits

        // Force obstacle collision without shield or overdrive
        val segment = engine.trackGenerator.segments[0]
        val obs = segment.obstacles[0]
        obs.setup(ObstacleType.CYBER_BLOCK, engine.player.currentLane, engine.player.z - 0.9f)

        // Award some credits before crash
        engine.scoreManager.addCredit()
        engine.scoreManager.addCredit()

        engine.update(0.05f)

        // State transitions to GAME_OVER
        assertEquals(GameState.GAME_OVER, engine.state)
        assertFalse(engine.musicManager.isPlaying)

        // Credits must be banked into permanent vault
        assertEquals(initialVaultCredits + 2, engine.characterManager.totalBankCredits)

        // Restart run
        engine.restartRun()
        assertEquals(GameState.RUNNING, engine.state)
        assertTrue(engine.musicManager.isPlaying)
        assertEquals(0, engine.scoreManager.creditsCollected)
        assertEquals(0f, engine.scoreManager.distance, 0.001f)
    }

    @Test
    fun testGameOverAndReturnToHomeCycle() {
        engine.startRun()
        val segment = engine.trackGenerator.segments[0]
        val obs = segment.obstacles[0]
        obs.setup(ObstacleType.CYBER_BLOCK, engine.player.currentLane, engine.player.z - 0.9f)
        engine.update(0.05f)
        assertEquals(GameState.GAME_OVER, engine.state)

        // User chooses to return to Main Menu / Home
        engine.returnToHome()
        assertEquals(GameState.READY, engine.state)
        assertFalse(engine.musicManager.isPlaying)
        assertEquals(0, engine.scoreManager.creditsCollected)
        assertEquals(0f, engine.scoreManager.distance, 0.001f)
    }

    @Test
    fun testHoverboardEmergencyDeployAndCollisionProtection() {
        engine.startRun()
        assertEquals(GameState.RUNNING, engine.state)
        assertFalse(engine.powerUpManager.isHoverboardActive)

        // Double tap deploys hoverboard
        engine.onDoubleTap()
        assertTrue("Hoverboard should be active after double tap", engine.powerUpManager.isHoverboardActive)

        // Setup obstacle in player's path
        val segment = engine.trackGenerator.segments[0]
        val obs = segment.obstacles[0]
        obs.setup(ObstacleType.CYBER_BLOCK, engine.player.currentLane, engine.player.z - 0.9f)
        assertTrue(obs.isActive)

        // Collision occurs: Hoverboard absorbs impact, destroys obstacle, and saves run!
        engine.update(0.05f)
        assertEquals(GameState.RUNNING, engine.state)
        assertFalse("Obstacle must be destroyed by hoverboard", obs.isActive)
        assertFalse("Hoverboard should be consumed", engine.powerUpManager.isHoverboardActive)
        assertTrue("Player should have invulnerability frames", engine.powerUpManager.invulnerabilityTimer > 0f)
    }

    @Test
    fun testHoverboardPickupAndDrasticShieldRarity() {
        engine.startRun()
        val segment = engine.trackGenerator.segments[0]

        // Setup hoverboard pickup orb
        segment.collectibles[0].setup(engine.player.currentLane, engine.player.y + 0.5f, engine.player.z - 0.5f, CollectibleType.HOVERBOARD_ORB)
        engine.update(0.05f)

        assertTrue(engine.powerUpManager.isHoverboardActive)
        assertTrue(engine.scoreManager.score >= 200)

        // Verify shield spawn probability is drastically lower than before
        var shieldCount = 0
        val testGen = com.example.longrunner.game.track.TrackGenerator()
        for (seg in testGen.segments) {
            for (col in seg.collectibles) {
                if (col.type == CollectibleType.SHIELD_ORB) shieldCount++
            }
        }
        // Across standard segments, shields should be exceptionally low/rare
        assertTrue("Shield spawn count should be exceptionally low", shieldCount <= 3)
    }
}
