package com.example.longrunner.game.collision

import com.example.longrunner.game.collectibles.Collectible
import com.example.longrunner.game.obstacles.Obstacle
import com.example.longrunner.game.player.PlayerController
import com.example.longrunner.game.track.TrackGenerator

class CollisionSystem {

    fun checkObstacleCollision(
        player: PlayerController,
        trackGenerator: TrackGenerator,
        isPhaseShiftActive: Boolean = false
    ): Obstacle? {
        val playerBox = player.collider
        val tolerance = player.collisionTolerance

        for (segment in trackGenerator.segments) {
            for (obstacle in segment.obstacles) {
                if (!obstacle.isActive) continue

                // Only evaluate obstacles in active vicinity of player
                if (kotlin.math.abs(obstacle.z - player.z) > 4.5f) continue

                // During Phase Shift, phase-permeable obstacles (beams, cyber blocks, drones) are ethereal
                if (isPhaseShiftActive && obstacle.isPhasePermeable) {
                    continue
                }

                if (playerBox.intersectsWithTolerance(obstacle.collider, tolerance)) {
                    return obstacle
                }
            }
        }
        return null
    }

    fun checkCollectiblePickups(
        player: PlayerController,
        trackGenerator: TrackGenerator,
        isPhaseShiftActive: Boolean = false,
        onCollect: (Collectible) -> Unit
    ) {
        val playerBox = player.collider

        for (segment in trackGenerator.segments) {
            for (collectible in segment.collectibles) {
                if (!collectible.isActive) continue

                if (kotlin.math.abs(collectible.z - player.z) > 4.0f) continue

                // Phase-exclusive collectibles can only be harvested in Phase Reality
                if (collectible.isPhaseExclusive && !isPhaseShiftActive) {
                    continue
                }

                if (playerBox.intersects(collectible.collider)) {
                    collectible.isActive = false
                    onCollect(collectible)
                }
            }
        }
    }
}
