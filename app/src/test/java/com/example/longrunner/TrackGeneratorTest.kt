package com.example.longrunner

import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.player.PlayerController
import com.example.longrunner.game.track.TrackGenerator
import org.junit.Assert.*
import org.junit.Test

class TrackGeneratorTest {

    @Test
    fun testTrackPoolIntegrity() {
        val generator = TrackGenerator()
        generator.reset()

        assertEquals(GameConstants.POOL_SEGMENT_COUNT, generator.segments.size)

        // Check continuity of segments
        var currentZ = 0f
        for (segment in generator.segments) {
            assertEquals(currentZ, segment.startZ, 0.001f)
            currentZ = segment.endZ
        }
    }

    @Test
    fun testTrackRecyclingAheadOfPlayer() {
        val generator = TrackGenerator()
        generator.reset()

        val player = PlayerController()
        player.reset()

        val initialFirstSegmentStartZ = generator.segments[0].startZ

        // Player runs 100 meters forward (-100Z)
        while (player.z > -100f) {
            player.update(0.05f)
            generator.update(player, 0.05f)
        }

        // Segments should have recycled ahead
        val anyRecycled = generator.segments.any { it.startZ < -200f }
        assertTrue("Track generator should recycle past segments forward", anyRecycled)
    }
}
