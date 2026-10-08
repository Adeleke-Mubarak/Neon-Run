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

class CollisionSystemTest {

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
    fun testStandingHitsLowHurdle() {
        // Place a low hurdle in center lane right in front of player
        val segment = trackGenerator.segments[0]
        val hurdle = segment.obstacles[0]
        hurdle.setup(ObstacleType.LOW_HURDLE, GameConstants.LANE_CENTER, player.z)

        val hit = collisionSystem.checkObstacleCollision(player, trackGenerator)
        assertNotNull(hit)
        assertEquals(ObstacleType.LOW_HURDLE, hit?.type)
    }

    @Test
    fun testJumpingClearsLowHurdle() {
        val segment = trackGenerator.segments[0]
        val hurdle = segment.obstacles[0]
        hurdle.setup(ObstacleType.LOW_HURDLE, GameConstants.LANE_CENTER, player.z)

        // Player jumps high enough (e.g. Y = 1.2m)
        player.jump()
        player.update(0.18f) // In peak of jump

        val hit = collisionSystem.checkObstacleCollision(player, trackGenerator)
        assertNull("Jumping runner should clear low hurdle", hit)
    }

    @Test
    fun testStandingHitsHighLaserGate() {
        val segment = trackGenerator.segments[0]
        val laser = segment.obstacles[0]
        laser.setup(ObstacleType.HIGH_BEAM, GameConstants.LANE_CENTER, player.z)

        val hit = collisionSystem.checkObstacleCollision(player, trackGenerator)
        assertNotNull("Standing runner should hit high laser gate", hit)
        assertEquals(ObstacleType.HIGH_BEAM, hit?.type)
    }

    @Test
    fun testSlidingClearsHighLaserGate() {
        val segment = trackGenerator.segments[0]
        val laser = segment.obstacles[0]
        laser.setup(ObstacleType.HIGH_BEAM, GameConstants.LANE_CENTER, player.z)

        // Slide under
        player.slide()
        player.update(0.01f)

        val hit = collisionSystem.checkObstacleCollision(player, trackGenerator)
        assertNull("Sliding runner should clear under high laser beam", hit)
    }

    @Test
    fun testCyberBlockCollisionInLaneAndMissInDifferentLane() {
        val segment = trackGenerator.segments[0]
        val block = segment.obstacles[0]
        // Place block in Left lane
        block.setup(ObstacleType.CYBER_BLOCK, GameConstants.LANE_LEFT, player.z)

        // Player is currently in Center lane
        var hit = collisionSystem.checkObstacleCollision(player, trackGenerator)
        assertNull("Player in center should not hit block in left lane", hit)

        // Move player to Left lane
        player.moveLeft()
        for (i in 0 until 10) player.update(0.05f) // wait for lane lerp to complete

        // Update block Z to match current player Z
        block.setup(ObstacleType.CYBER_BLOCK, GameConstants.LANE_LEFT, player.z)

        hit = collisionSystem.checkObstacleCollision(player, trackGenerator)
        assertNotNull("Player in left lane should hit cyber block", hit)
    }
}
