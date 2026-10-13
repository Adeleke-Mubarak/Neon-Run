package com.example.longrunner

import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.player.CharacterData
import com.example.longrunner.game.player.PlayerController
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PlayerControllerTest {

    private lateinit var player: PlayerController

    @Before
    fun setUp() {
        player = PlayerController(CharacterData.KAI)
        player.reset()
    }

    @Test
    fun testInitialState() {
        assertEquals(0f, player.x, 0.001f)
        assertEquals(0f, player.y, 0.001f)
        assertEquals(0f, player.z, 0.001f)
        assertEquals(GameConstants.LANE_CENTER, player.currentLane)
        assertTrue(player.isGrounded)
        assertFalse(player.isJumping)
        assertFalse(player.isSliding)
    }

    @Test
    fun testLaneSwitchingBounds() {
        // Start center
        assertEquals(GameConstants.LANE_CENTER, player.targetLane)

        // Move Left -> Lane Left (-1)
        assertTrue(player.moveLeft())
        assertEquals(GameConstants.LANE_LEFT, player.targetLane)

        // Try Move Left again -> should be clamped at -1
        assertFalse(player.moveLeft())
        assertEquals(GameConstants.LANE_LEFT, player.targetLane)

        // Move Right -> Lane Center (0)
        assertTrue(player.moveRight())
        assertEquals(GameConstants.LANE_CENTER, player.targetLane)

        // Move Right -> Lane Right (1)
        assertTrue(player.moveRight())
        assertEquals(GameConstants.LANE_RIGHT, player.targetLane)

        // Try Move Right again -> clamped at 1
        assertFalse(player.moveRight())
        assertEquals(GameConstants.LANE_RIGHT, player.targetLane)
    }

    @Test
    fun testJumpPhysics() {
        assertTrue(player.jump())
        assertFalse(player.isGrounded)
        assertTrue(player.isJumping)
        assertTrue(player.velocityY > 0f)

        // Simulate flight for 0.2s
        player.update(0.2f)
        assertTrue(player.y > 0.5f)

        // Simulate flight until landing (1.5 seconds)
        for (i in 0 until 50) {
            player.update(0.03f)
        }
        assertTrue(player.isGrounded)
        assertFalse(player.isJumping)
        assertEquals(GameConstants.GROUND_Y, player.y, 0.001f)
    }

    @Test
    fun testSlideDurationAndHitbox() {
        assertTrue(player.slide())
        assertTrue(player.isSliding)

        // Collider height should be compressed during slide
        val slideHeight = player.collider.maxY - player.collider.minY
        assertEquals(GameConstants.PLAYER_SLIDE_HEIGHT, slideHeight, 0.01f)

        // Advance time past slide duration
        player.update(GameConstants.SLIDE_DURATION + 0.1f)
        assertFalse(player.isSliding)

        // Collider height should return to standard standing height
        val standHeight = player.collider.maxY - player.collider.minY
        assertEquals(GameConstants.PLAYER_STAND_HEIGHT, standHeight, 0.01f)
    }

    @Test
    fun testForwardMovementAndSpeedAcceleration() {
        val initialSpeed = player.speed
        player.update(1.0f)

        // Player should move down negative Z
        assertTrue(player.z < 0f)
        // Speed should accelerate
        assertTrue(player.speed > initialSpeed)
    }

    @Test
    fun testJumpAirTimeCeilingAndSnappyLanding() {
        assertTrue(player.jump())
        assertTrue(player.isJumping)
        assertFalse(player.isGrounded)

        // Simulate flight: within 0.55s, runner should have peaked and returned near ground
        var totalDt = 0f
        while (!player.isGrounded && totalDt < 0.65f) {
            player.update(0.016f)
            totalDt += 0.016f
        }

        // Runner must be safely grounded and not suspended
        assertTrue("Runner should have landed within 0.60s without mid-air hang", player.isGrounded)
        assertFalse(player.isJumping)
        assertEquals(0f, player.jumpAirTimer, 0.001f)
    }

    @Test
    fun testMidAirSlideDiveRoll() {
        assertTrue(player.jump())
        player.update(0.1f) // Ascend slightly
        assertTrue(player.isJumping)

        // Mid-air swipe down should trigger dive roll
        assertTrue(player.slide())
        assertTrue(player.isSliding)
        assertTrue("Dive roll velocity should be sharply negative", player.velocityY <= -26.0f)
    }

    @Test
    fun testSlideDurationWithTimeDilation() {
        assertTrue(player.slide())
        assertTrue(player.isSliding)

        // Under 0.50x time dilation, effectiveDt is halved, but verticalDt is real frame time
        val timeDilation = 0.50f
        val frameDt = 0.016f
        var totalRealTime = 0f
        while (player.isSliding && totalRealTime < 0.70f) {
            player.update(dt = frameDt * timeDilation, verticalDt = frameDt)
            totalRealTime += frameDt
        }

        // Slide must complete in normal arcade time (~0.52s) and NEVER drag for 2 seconds
        assertFalse("Slide should have completed without 2-second drag", player.isSliding)
        assertTrue(totalRealTime <= 0.55f)
    }
}
