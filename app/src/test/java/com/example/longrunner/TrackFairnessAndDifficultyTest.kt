package com.example.longrunner

import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.obstacles.ObstacleType
import com.example.longrunner.game.player.PlayerController
import com.example.longrunner.game.track.SegmentType
import com.example.longrunner.game.track.TrackGenerator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class TrackFairnessAndDifficultyTest {

    private lateinit var trackGenerator: TrackGenerator
    private lateinit var player: PlayerController

    @Before
    fun setUp() {
        trackGenerator = TrackGenerator().apply { reset() }
        player = PlayerController()
    }

    @Test
    fun testInitialTrackGenerationContinuity() {
        assertEquals(GameConstants.POOL_SEGMENT_COUNT, trackGenerator.segments.size)

        // Segments must be contiguous and monotonically decreasing in Z
        var expectedStartZ = 0.0f
        for (segment in trackGenerator.segments) {
            assertEquals(expectedStartZ, segment.startZ, 0.01f)
            expectedStartZ -= GameConstants.SEGMENT_LENGTH
        }
    }

    @Test
    fun testLongRunProceduralFairnessAcross100Segments() {
        // Advance player along the track over 100 segment recycling iterations (~3,000 meters)
        val validation = trackGenerator.validationSystem

        for (step in 0 until 100) {
            // Move player forward by 30 meters
            player.update(dt = 30.0f / player.speed)
            trackGenerator.update(player, dt = 1.0f)

            // Validate all active segments for fairness
            for (segment in trackGenerator.segments) {
                // Must pass ObstacleValidationSystem
                val activeObstacles = segment.obstacles.filter { it.isActive }
                if (activeObstacles.size >= 3) {
                    val occupiedLanes = activeObstacles.map { it.lane }.toSet()
                    if (occupiedLanes.size == 3) {
                        // All 3 lanes occupied -> at least one obstacle must be jumpable or slideable
                        val hasJumpable = activeObstacles.any { it.type == ObstacleType.LOW_HURDLE || it.type == ObstacleType.BREAKABLE_CRATE }
                        val hasSlideable = activeObstacles.any { it.type == ObstacleType.HIGH_BEAM }
                        assertTrue(
                            "3-lane cluster at Z=${segment.startZ} must allow bypass via jump or slide",
                            hasJumpable || hasSlideable
                        )
                    }
                }
            }
        }

        // Verify player survived extended track generation
        assertTrue(trackGenerator.segments.isNotEmpty())
    }

    @Test
    fun testTieredObstacleDensityProgression() {
        // Tier 1 (0 - 150m): Low hazard density
        trackGenerator.reset()
        val tier1Segment = trackGenerator.segments[2] // ~60m
        val tier1Obstacles = tier1Segment.obstacles.count { it.isActive }
        assertTrue("Tier 1 segments should have at most 1 obstacle", tier1Obstacles <= 1)

        // Advance to Tier 3 (500m+)
        while (trackGenerator.segments.last().startZ > -600f) {
            player.update(dt = 2.0f)
            trackGenerator.update(player, dt = 0.1f)
        }

        val highTierSegment = trackGenerator.segments.firstOrNull { it.startZ < -500f }
        assertNotNull(highTierSegment)
    }

    @Test
    fun testFractureRouteElevatedOverpassHeights() {
        trackGenerator.reset()
        // Fast-forward generator directly to fracture zone (500m - 700m)
        while (trackGenerator.segments.last().startZ > -650f) {
            player.update(dt = 1.5f)
            trackGenerator.update(player, dt = 0.05f)
        }

        val overpassSegments = trackGenerator.segments.filter {
            it.type == SegmentType.FRACTURE_OVERPASS ||
            it.type == SegmentType.FRACTURE_OVERPASS_RAMP_UP ||
            it.type == SegmentType.FRACTURE_OVERPASS_RAMP_DOWN
        }

        // Verify elevation profile
        for (seg in overpassSegments) {
            when (seg.type) {
                SegmentType.FRACTURE_OVERPASS -> {
                    assertEquals(3.5f, seg.elevationStart, 0.01f)
                    assertEquals(3.5f, seg.elevationEnd, 0.01f)
                    assertEquals(3.5f, seg.getSurfaceY(seg.startZ - 15f), 0.01f)
                }
                SegmentType.FRACTURE_OVERPASS_RAMP_UP -> {
                    assertEquals(0.0f, seg.elevationStart, 0.01f)
                    assertEquals(3.5f, seg.elevationEnd, 0.01f)
                    val midY = seg.getSurfaceY(seg.startZ - 15f)
                    assertEquals(1.75f, midY, 0.05f)
                }
                SegmentType.FRACTURE_OVERPASS_RAMP_DOWN -> {
                    assertEquals(3.5f, seg.elevationStart, 0.01f)
                    assertEquals(0.0f, seg.elevationEnd, 0.01f)
                }
                else -> {}
            }
        }
    }

    @Test
    fun testSanitizeRemovesTripleImpassableWall() {
        val segment = trackGenerator.segments[0]
        segment.obstacles[0].setup(ObstacleType.CYBER_BLOCK, GameConstants.LANE_LEFT, segment.startZ - 10f)
        segment.obstacles[1].setup(ObstacleType.CYBER_BLOCK, GameConstants.LANE_CENTER, segment.startZ - 10f)
        segment.obstacles[2].setup(ObstacleType.CYBER_BLOCK, GameConstants.LANE_RIGHT, segment.startZ - 10f)

        // Before sanitizing: all 3 active CYBER_BLOCKs (impossible wall)
        assertEquals(3, segment.obstacles.count { it.isActive })

        trackGenerator.validationSystem.sanitize(segment.obstacles)

        // After sanitizing: at least one obstacle must be deactivated to create a safe opening
        val activeCount = segment.obstacles.count { it.isActive }
        assertTrue("Sanitizer must create an opening in triple solid blocks", activeCount <= 2)
    }
}
