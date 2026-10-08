package com.example.longrunner

import com.example.longrunner.game.collectibles.Collectible
import com.example.longrunner.game.collectibles.CollectibleType
import com.example.longrunner.game.collision.NearMissTracker
import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.obstacles.ObstacleType
import com.example.longrunner.game.player.PlayerController
import com.example.longrunner.game.score.ComboManager
import com.example.longrunner.game.score.ScoreManager
import com.example.longrunner.game.track.TrackGenerator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CollectiblesAndComboTest {

    private lateinit var player: PlayerController
    private lateinit var trackGenerator: TrackGenerator
    private lateinit var comboManager: ComboManager
    private lateinit var scoreManager: ScoreManager
    private lateinit var nearMissTracker: NearMissTracker

    @Before
    fun setUp() {
        player = PlayerController()
        player.reset()
        trackGenerator = TrackGenerator()
        trackGenerator.reset()
        comboManager = ComboManager()
        comboManager.reset()
        scoreManager = ScoreManager(null)
        scoreManager.reset()
        nearMissTracker = NearMissTracker()
        nearMissTracker.reset()
    }

    @Test
    fun testCollectibleSetupAndMagnetAttraction() {
        val collectible = Collectible()
        collectible.setup(lane = GameConstants.LANE_LEFT, y = 0.8f, z = -10f, type = CollectibleType.ENERGY_SHARD)

        assertTrue(collectible.isActive)
        assertEquals(GameConstants.laneToX(GameConstants.LANE_LEFT), collectible.currentX, 0.001f)
        assertEquals(-10f, collectible.currentZ, 0.001f)

        // Player is at (0, 0, -10), simulate magnet pulling collectible
        val initialX = collectible.currentX
        collectible.attractTowards(targetX = 0f, targetY = 0.5f, targetZ = -10f, pullSpeed = 15f, dt = 0.1f)

        // Should have moved towards center lane
        assertTrue("Collectible X should move towards 0", collectible.currentX > initialX)

        // Full attraction test: step repeatedly until reaching target
        for (i in 0 until 50) {
            collectible.attractTowards(targetX = 0f, targetY = 0.5f, targetZ = -10f, pullSpeed = 20f, dt = 0.1f)
        }
        assertEquals(0f, collectible.currentX, 0.01f)
        assertEquals(0.5f, collectible.currentY, 0.01f)
        assertEquals(-10f, collectible.currentZ, 0.01f)
    }

    @Test
    fun testComboManagerProgressionAndDecay() {
        assertEquals(1, comboManager.currentCombo)
        assertEquals(0f, comboManager.comboTimer, 0.001f)

        // Register actions to build combo
        comboManager.registerAction()
        assertEquals(2, comboManager.currentCombo)
        assertEquals(comboManager.maxComboDuration, comboManager.comboTimer, 0.001f)

        // Build up to max limit
        for (i in 0 until 20) {
            comboManager.registerAction()
        }
        assertEquals(comboManager.maxComboLimit, comboManager.currentCombo)
        assertEquals(comboManager.maxComboLimit, comboManager.highestCombo)

        // Advance time partially; combo should persist
        comboManager.update(1.0f)
        assertEquals(comboManager.maxComboLimit, comboManager.currentCombo)
        assertTrue(comboManager.comboTimer > 0f)

        // Advance time beyond maxComboDuration; combo should expire to 1
        comboManager.update(comboManager.maxComboDuration + 1f)
        assertEquals(1, comboManager.currentCombo)
        assertEquals(0f, comboManager.comboTimer, 0.001f)
    }

    @Test
    fun testNearMissDetectionAndSlowMotion() {
        val segment = trackGenerator.segments[0]
        val obstacle = segment.obstacles[0]

        // Player switches to left lane
        player.moveLeft()
        for (i in 0 until 10) {
            player.update(0.05f)
        }

        // Place obstacle slightly to the right of the player (graze zone) at current player.z
        val grazeX = player.x + 1.2f // within 1.6m lateral distance
        obstacle.setup(ObstacleType.LOW_HURDLE, GameConstants.LANE_CENTER, player.z)
        obstacle.currentX = grazeX
        obstacle.collider.set(grazeX, 0.45f, player.z, 0.65f, 0.45f, 0.25f)

        val triggered = nearMissTracker.checkNearMiss(player, trackGenerator)
        assertTrue("Graze should register as near miss", triggered)
        assertEquals(1, nearMissTracker.totalNearMisses)
        assertTrue("Slow motion should be active", nearMissTracker.isSlowMotionActive)

        // Immediate next check on same obstacle should not duplicate trigger
        val duplicate = nearMissTracker.checkNearMiss(player, trackGenerator)
        assertFalse("Duplicate near miss on same obstacle should be prevented", duplicate)
        assertEquals(1, nearMissTracker.totalNearMisses)

        // Advance time to expire slow motion
        nearMissTracker.update(0.3f)
        assertFalse("Slow motion should expire", nearMissTracker.isSlowMotionActive)
    }

    @Test
    fun testScoreManagerFullScoringCalculation() {
        // Collect various items
        scoreManager.addShard()         // 50 pts
        scoreManager.addShard()         // 50 pts -> 100 pts
        scoreManager.addPhaseCore()     // 150 pts
        scoreManager.addCredit()        // 200 pts
        scoreManager.addNearMiss()      // 250 bonus pts
        scoreManager.addMultiplierToken() // 2x active for 10s

        assertTrue(scoreManager.isMultiplierBoostActive)

        // Update score: distance = 100m, speed = 24m/s (2x speedMult: 24/10=2), combo = 3x, token = 2x
        // Multiplier = 2 * 3 * 2 = 12x
        // Distance score: 100 * 10 = 1000
        // Shards: 2 * 50 = 100
        // Cores: 1 * 150 = 150
        // Credits: 1 * 200 = 200
        // Bonus: 250
        // Base score = 1000 + 100 + 150 + 200 + 250 = 1700
        // Total score = 1700 * 12 = 20400
        scoreManager.update(playerZ = -100f, speed = 24f, combo = 3, dt = 0.1f)

        assertEquals(12, scoreManager.multiplier)
        assertEquals(1700L * 12L, scoreManager.score)
        assertEquals(scoreManager.score, scoreManager.highScore)

        // Advance 11s to expire multiplier token
        scoreManager.update(playerZ = -100f, speed = 24f, combo = 3, dt = 11.0f)
        assertFalse(scoreManager.isMultiplierBoostActive)
        assertEquals(6, scoreManager.multiplier) // 2 * 3 * 1 = 6x
    }
}
