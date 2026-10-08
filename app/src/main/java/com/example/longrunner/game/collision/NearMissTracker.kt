package com.example.longrunner.game.collision

import com.example.longrunner.game.obstacles.Obstacle
import com.example.longrunner.game.player.PlayerController
import com.example.longrunner.game.track.TrackGenerator
import kotlin.math.abs

class NearMissTracker {

    private val triggeredObstacleIds = HashSet<Int>()

    var totalNearMisses: Int = 0
        private set

    var slowMotionTimer: Float = 0f
        private set
    val isSlowMotionActive: Boolean get() = slowMotionTimer > 0f

    fun reset() {
        triggeredObstacleIds.clear()
        totalNearMisses = 0
        slowMotionTimer = 0f
    }

    fun update(dt: Float) {
        if (slowMotionTimer > 0f) {
            slowMotionTimer -= dt
            if (slowMotionTimer < 0f) slowMotionTimer = 0f
        }
    }

    /**
     * Checks if the player narrowly avoided an obstacle.
     * Returns true if a new near-miss occurred this frame.
     */
    fun checkNearMiss(player: PlayerController, trackGenerator: TrackGenerator): Boolean {
        val playerBox = player.collider

        for (segment in trackGenerator.segments) {
            if (player.z < segment.endZ - 2.0f || player.z > segment.startZ + 2.0f) continue

            for (obstacle in segment.obstacles) {
                if (!obstacle.isActive) continue

                val obsKey = (segment.segmentIndex * 100) + obstacle.hashCode()
                if (triggeredObstacleIds.contains(obsKey)) continue

                // Check longitudinal proximity: player is passing through the obstacle's Z slice
                val zDist = abs(player.z - obstacle.z)
                if (zDist <= 1.2f) {
                    // Check if player did NOT hit the obstacle
                    if (!playerBox.intersectsWithTolerance(obstacle.collider, player.collisionTolerance)) {
                        // Check if player was close enough for a near-miss
                        val xDist = abs(player.x - obstacle.currentX)
                        val yDist = abs((player.y + 0.9f) - obstacle.collider.maxY)

                        // Close lateral graze (< 1.6m) or close vertical graze (< 0.75m)
                        val isNear = (xDist <= 1.6f && yDist <= 1.8f)

                        if (isNear) {
                            triggeredObstacleIds.add(obsKey)
                            totalNearMisses++
                            // Trigger subtle slow-mo effect for 0.25 seconds
                            slowMotionTimer = 0.25f
                            return true
                        }
                    }
                }
            }
        }
        return false
    }
}
