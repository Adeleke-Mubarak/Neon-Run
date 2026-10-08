package com.example.longrunner

import com.example.longrunner.game.collision.CollisionSystem
import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.obstacles.Obstacle
import com.example.longrunner.game.obstacles.ObstacleType
import com.example.longrunner.game.player.PlayerController
import com.example.longrunner.game.track.TrackGenerator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AdvancedObstaclesTest {

    private lateinit var collisionSystem: CollisionSystem
    private lateinit var player: PlayerController
    private lateinit var trackGenerator: TrackGenerator

    @Before
    fun setUp() {
        collisionSystem = CollisionSystem()
        player = PlayerController()
        player.reset()
        trackGenerator = TrackGenerator()
        trackGenerator.reset()
    }

    @Test
    fun testPatrolDroneMovementAndSlideClearance() {
        val segment = trackGenerator.segments[0]
        val drone = segment.obstacles[0]
        drone.setup(ObstacleType.PATROL_DRONE, GameConstants.LANE_CENTER, player.z)

        val initialX = drone.currentX
        // Advance drone time
        drone.update(0.3f, player.z)
        assertNotEquals("Drone should oscillate in X position", initialX, drone.currentX, 0.0001f)

        // Reset to center for collision test
        drone.currentX = 0f
        drone.collider.set(0f, 1.15f, player.z, 0.55f, 0.3f, 0.4f)

        // Standing runner should collide with chest-height drone
        var hit = collisionSystem.checkObstacleCollision(player, trackGenerator)
        assertNotNull("Standing runner should hit patrol drone", hit)
        assertEquals(ObstacleType.PATROL_DRONE, hit?.type)

        // Sliding runner (max height 0.75m) should duck under drone (min height 0.85m)
        player.slide()
        player.update(0.01f)
        hit = collisionSystem.checkObstacleCollision(player, trackGenerator)
        assertNull("Sliding runner should clear under patrol drone", hit)
    }

    @Test
    fun testSlidingGateLateralMovement() {
        val segment = trackGenerator.segments[0]
        val gate = segment.obstacles[0]
        gate.setup(ObstacleType.SLIDING_GATE, GameConstants.LANE_CENTER, player.z)

        val startX = gate.currentX
        gate.update(0.4f, player.z)
        assertNotEquals("Sliding gate should translate laterally over time", startX, gate.currentX, 0.0001f)
    }

    @Test
    fun testFallingDebrisTriggerAndTouchdown() {
        val segment = trackGenerator.segments[0]
        val debris = segment.obstacles[0]
        // Place debris 20m ahead of player
        val debrisZ = player.z - 20.0f
        debris.setup(ObstacleType.FALLING_DEBRIS, GameConstants.LANE_CENTER, debrisZ)

        // Initially suspended high up
        assertTrue("Debris should start suspended in the air", debris.currentY > 4.0f)
        assertFalse(debris.isGrounded)

        // Advance simulation as runner approaches
        for (i in 0 until 20) {
            debris.update(0.05f, player.z)
        }

        // Debris should have crashed to ground
        assertTrue("Debris should touch down and become grounded", debris.isGrounded)
        assertEquals(0.0f, debris.currentY, 0.01f)
    }

    @Test
    fun testBreakableCrateJumpClearance() {
        val segment = trackGenerator.segments[0]
        val crate = segment.obstacles[0]
        crate.setup(ObstacleType.BREAKABLE_CRATE, GameConstants.LANE_CENTER, player.z)

        // Standing runner hits crate
        var hit = collisionSystem.checkObstacleCollision(player, trackGenerator)
        assertNotNull("Standing runner should hit crate", hit)

        // Peak jump runner clears crate
        player.jump()
        // Peak of jump
        for (i in 0 until 12) player.update(0.03f)

        hit = collisionSystem.checkObstacleCollision(player, trackGenerator)
        assertNull("Jumping runner at peak should clear crate", hit)
    }
}
