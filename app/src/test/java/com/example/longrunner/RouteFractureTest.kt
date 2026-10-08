package com.example.longrunner

import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.player.PlayerController
import com.example.longrunner.game.track.*
import org.junit.Assert.*
import org.junit.Test
import java.util.Random

class RouteFractureTest {

    @Test
    fun testFractureSchedulingAndAlternation() {
        val manager = FractureManager()
        val upcoming = manager.getUpcomingFractureForDistance(220f)
        assertNotNull("Should have scheduled fracture around 220m", upcoming)
        assertEquals(FractureType.BRANCH_SPLIT, upcoming!!.type)

        val secondFracture = manager.getUpcomingFractureForDistance(500f)
        assertNotNull("Should have scheduled second fracture around 500m", secondFracture)
        assertEquals(FractureType.ELEVATED_OVERPASS, secondFracture!!.type)
    }

    @Test
    fun testFractureWarningPhase() {
        val manager = FractureManager()
        var warningFired = false
        manager.onFractureWarningTriggered = {
            warningFired = true
        }

        val event = manager.getUpcomingFractureForDistance(220f)!!
        val startZ = event.startZ // -220m

        // Player is at -150m (70m away, warning distance is 48m) -> no warning yet
        manager.update(playerZ = -150f, playerLane = GameConstants.LANE_CENTER, dt = 0.016f)
        assertFalse("Warning should not be active at -150m", manager.isWarningActive)
        assertFalse(warningFired)

        // Player advances to -180m (40m away, inside warning distance) -> warning triggers!
        manager.update(playerZ = -180f, playerLane = GameConstants.LANE_CENTER, dt = 0.016f)
        assertTrue("Warning should be active at -180m", manager.isWarningActive)
        assertTrue(warningFired)
        assertTrue("Warning title should not be empty", manager.warningTitle.isNotEmpty())
    }

    @Test
    fun testFractureEntryAndLaneChoice() {
        val manager = FractureManager()
        var enteredChoice: RouteChoice? = null
        manager.onFractureEntered = { _, choice ->
            enteredChoice = choice
        }

        val event = manager.getUpcomingFractureForDistance(220f)!!
        val startZ = event.startZ // -220m

        // Player crosses startZ on RIGHT lane (High-Risk Bounty)
        manager.update(playerZ = startZ - 1.0f, playerLane = GameConstants.LANE_RIGHT, dt = 0.016f)
        assertTrue("Should be inside fracture zone", manager.isInFractureZone)
        assertEquals(RouteChoice.RIGHT_HIGH_RISK, event.chosenRoute)
        assertEquals(RouteChoice.RIGHT_HIGH_RISK, enteredChoice)
        assertTrue("Active route name should contain high risk", manager.activeRouteName.contains("HIGH-RISK"))
    }

    @Test
    fun testFractureClearedAndBonusAwarded() {
        val manager = FractureManager()
        var awardedBonus: Long = 0L
        manager.onFractureCleared = { _, bonus ->
            awardedBonus = bonus
        }

        val event = manager.getUpcomingFractureForDistance(220f)!!
        val startZ = event.startZ // -220m
        val endZ = event.endZ // -280m

        // Enter on High-Risk lane
        manager.update(playerZ = startZ - 2f, playerLane = GameConstants.LANE_RIGHT, dt = 0.016f)
        assertTrue(manager.isInFractureZone)

        // Clear fracture zone
        manager.update(playerZ = endZ - 5f, playerLane = GameConstants.LANE_RIGHT, dt = 0.016f)
        assertTrue("Event should be completed", event.isCompleted)
        assertFalse("Should no longer be in fracture zone", manager.isInFractureZone)
        assertEquals("Should award 1000 pts bonus for surviving high-risk route", 1000L, awardedBonus)
        assertTrue("Clear message should be active", manager.clearBannerMessage.contains("CONQUERED"))
    }

    @Test
    fun testTrackSegmentSurfaceElevation() {
        val rampUpSegment = TrackSegment(0)
        rampUpSegment.reset(startZ = 0f, type = SegmentType.FRACTURE_OVERPASS_RAMP_UP)
        assertEquals(0f, rampUpSegment.elevationStart, 0.001f)
        assertEquals(3.5f, rampUpSegment.elevationEnd, 0.001f)

        // Surface elevation at start of ramp
        assertEquals(0.0f, rampUpSegment.getSurfaceY(0f), 0.001f)
        // Surface elevation midway through ramp (z = -15m)
        assertEquals(1.75f, rampUpSegment.getSurfaceY(-15f), 0.001f)
        // Surface elevation at top of ramp (z = -30m)
        assertEquals(3.5f, rampUpSegment.getSurfaceY(-30f), 0.001f)

        val overpassSegment = TrackSegment(1)
        overpassSegment.reset(startZ = -30f, type = SegmentType.FRACTURE_OVERPASS)
        assertEquals(3.5f, overpassSegment.getSurfaceY(-30f), 0.001f)
        assertEquals(3.5f, overpassSegment.getSurfaceY(-45f), 0.001f)
        assertEquals(3.5f, overpassSegment.getSurfaceY(-60f), 0.001f)

        val rampDownSegment = TrackSegment(2)
        rampDownSegment.reset(startZ = -60f, type = SegmentType.FRACTURE_OVERPASS_RAMP_DOWN)
        assertEquals(3.5f, rampDownSegment.getSurfaceY(-60f), 0.001f)
        assertEquals(1.75f, rampDownSegment.getSurfaceY(-75f), 0.001f)
        assertEquals(0.0f, rampDownSegment.getSurfaceY(-90f), 0.001f)
    }

    @Test
    fun testPlayerControllerElevationPhysics() {
        val player = PlayerController()
        assertEquals(0f, player.y, 0.001f)

        // Ascend ramp: surfaceY rises to 3.5m
        player.updateSurfaceElevation(3.5f)
        assertEquals(3.5f, player.surfaceY, 0.001f)

        // Player is grounded and smoothly rides up
        player.update(0.1f)
        assertTrue("Player should climb towards surfaceY", player.y > 0f)

        // Test landing on elevated overpass during a jump
        player.jump()
        assertFalse(player.isGrounded)

        // Advance jump until landing on overpass
        var iterations = 0
        while (!player.isGrounded && iterations < 150) {
            player.update(0.016f)
            iterations++
        }

        assertTrue("Player should land grounded on elevated surface", player.isGrounded)
        assertEquals("Player should land at 3.5m", 3.5f, player.y, 0.01f)
    }

    @Test
    fun testTrackGeneratorFractureSplitPopulatesBounty() {
        val generator = TrackGenerator(random = Random(42))
        val testSegment = TrackSegment(0)
        testSegment.reset(startZ = -220f, type = SegmentType.FRACTURE_SPLIT)

        // Force populate
        val method = TrackGenerator::class.java.getDeclaredMethod("populateSegment", TrackSegment::class.java, Boolean::class.java)
        method.isAccessible = true
        method.invoke(generator, testSegment, false)

        val activeObstacles = testSegment.obstacles.filter { it.isActive }
        val activeCollectibles = testSegment.collectibles.filter { it.isActive }

        // Differentiated hazards
        assertTrue("Should have active obstacles", activeObstacles.isNotEmpty())
        assertTrue("Should have collectibles", activeCollectibles.isNotEmpty())

        // Right lane should contain high bounty collectibles
        val rightLaneCollectibles = activeCollectibles.filter { it.lane == GameConstants.LANE_RIGHT }
        assertTrue("Right lane should feature bounty collectibles", rightLaneCollectibles.isNotEmpty())
    }
}
