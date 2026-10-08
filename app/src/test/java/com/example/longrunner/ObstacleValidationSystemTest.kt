package com.example.longrunner

import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.obstacles.Obstacle
import com.example.longrunner.game.obstacles.ObstacleType
import com.example.longrunner.game.track.ObstacleValidationSystem
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ObstacleValidationSystemTest {

    private lateinit var validator: ObstacleValidationSystem

    @Before
    fun setUp() {
        validator = ObstacleValidationSystem()
    }

    @Test
    fun testValidObstacleSequence() {
        val o1 = Obstacle().apply { setup(ObstacleType.LOW_HURDLE, GameConstants.LANE_LEFT, -10f) }
        val o2 = Obstacle().apply { setup(ObstacleType.HIGH_BEAM, GameConstants.LANE_RIGHT, -24f) }

        assertTrue("Spaced out obstacles in different lanes must be valid", validator.validate(listOf(o1, o2)))
    }

    @Test
    fun testTripleCyberBlockIsRejected() {
        // Block all 3 lanes with solid cyber blocks at same Z = -15m
        val o1 = Obstacle().apply { setup(ObstacleType.CYBER_BLOCK, GameConstants.LANE_LEFT, -15f) }
        val o2 = Obstacle().apply { setup(ObstacleType.CYBER_BLOCK, GameConstants.LANE_CENTER, -15f) }
        val o3 = Obstacle().apply { setup(ObstacleType.CYBER_BLOCK, GameConstants.LANE_RIGHT, -15f) }

        assertFalse("Blocking all 3 lanes with solid walls must fail validation", validator.validate(listOf(o1, o2, o3)))
    }

    @Test
    fun testInsufficientSpacingInSameLaneIsRejected() {
        // Two obstacles placed 4m apart in the center lane (minimum is 9m)
        val o1 = Obstacle().apply { setup(ObstacleType.LOW_HURDLE, GameConstants.LANE_CENTER, -10f) }
        val o2 = Obstacle().apply { setup(ObstacleType.LOW_HURDLE, GameConstants.LANE_CENTER, -14f) }

        assertFalse("Obstacles under 9m apart in same lane must fail validation", validator.validate(listOf(o1, o2)))
    }

    @Test
    fun testJumpThenSlideCloseTogetherIsRejected() {
        // Hurdle at -10, then high laser gate at -16 (gap is 6m, less than required 11m)
        val o1 = Obstacle().apply { setup(ObstacleType.LOW_HURDLE, GameConstants.LANE_CENTER, -10f) }
        val o2 = Obstacle().apply { setup(ObstacleType.HIGH_BEAM, GameConstants.LANE_CENTER, -16f) }

        assertFalse("Jump hurdle immediately followed by slide beam must fail validation", validator.validate(listOf(o1, o2)))
    }

    @Test
    fun testSanitizeResolvesTripleBlockade() {
        val o1 = Obstacle().apply { setup(ObstacleType.CYBER_BLOCK, GameConstants.LANE_LEFT, -15f) }
        val o2 = Obstacle().apply { setup(ObstacleType.CYBER_BLOCK, GameConstants.LANE_CENTER, -15f) }
        val o3 = Obstacle().apply { setup(ObstacleType.CYBER_BLOCK, GameConstants.LANE_RIGHT, -15f) }

        val list = arrayListOf(o1, o2, o3)
        assertFalse(validator.validate(list))

        validator.sanitize(list)
        assertTrue("Sanitized list must be valid with an open lane", validator.validate(list))
        val activeCount = list.count { it.isActive }
        assertTrue("At least one obstacle must have been deactivated", activeCount <= 2)
    }

    @Test
    fun testSanitizeResolvesSpacingConflict() {
        val o1 = Obstacle().apply { setup(ObstacleType.LOW_HURDLE, GameConstants.LANE_CENTER, -10f) }
        val o2 = Obstacle().apply { setup(ObstacleType.LOW_HURDLE, GameConstants.LANE_CENTER, -13f) }

        val list = arrayListOf(o1, o2)
        assertFalse(validator.validate(list))

        validator.sanitize(list)
        assertTrue("Sanitized list must resolve spacing conflict", validator.validate(list))
    }
}
