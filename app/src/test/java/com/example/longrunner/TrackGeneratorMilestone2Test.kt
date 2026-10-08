package com.example.longrunner

import com.example.longrunner.game.player.PlayerController
import com.example.longrunner.game.track.SegmentType
import com.example.longrunner.game.track.TrackGenerator
import org.junit.Assert.*
import org.junit.Test
import java.util.Random

class TrackGeneratorMilestone2Test {

    @Test
    fun testEnvironmentDiversity() {
        val generator = TrackGenerator(Random(42))

        val observedTypes = HashSet<SegmentType>()
        // Sample 100 distance increments up to 1000m
        for (dist in 0..1000 step 10) {
            observedTypes.add(generator.selectSegmentType(dist.toFloat()))
        }

        assertTrue("Metro Straight should be present", observedTypes.contains(SegmentType.METRO_STRAIGHT))
        assertTrue("Neon Tunnel should be present", observedTypes.contains(SegmentType.NEON_TUNNEL))
        assertTrue("Sky Bridge should be present", observedTypes.contains(SegmentType.SKY_BRIDGE))
        assertTrue("Overpass Gantry should be present", observedTypes.contains(SegmentType.OVERPASS_GANTRY))
        assertTrue("Solar District should be present", observedTypes.contains(SegmentType.SOLAR_DISTRICT))
    }

    @Test
    fun testGeneratedSegmentsPassFairnessValidation() {
        val generator = TrackGenerator(Random(1234))
        generator.reset()

        val player = PlayerController()
        player.reset()

        // Simulate a 500-meter continuous run
        while (player.z > -500f) {
            player.update(0.05f)
            generator.update(player, 0.05f)

            // Verify all active segments satisfy obstacle fairness rules
            for (segment in generator.segments) {
                val isValid = generator.validationSystem.validate(segment.obstacles)
                assertTrue("Segment ${segment.segmentIndex} must satisfy fairness rules", isValid)
            }
        }
    }
}
