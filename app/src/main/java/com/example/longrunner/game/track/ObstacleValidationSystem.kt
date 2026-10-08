package com.example.longrunner.game.track

import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.obstacles.Obstacle
import com.example.longrunner.game.obstacles.ObstacleType
import kotlin.math.abs

/**
 * Validates and sanitizes generated obstacle sequences to ensure fairness.
 * Guarantees that every obstacle challenge is mechanically possible for the player.
 */
class ObstacleValidationSystem {

    companion object {
        const val MIN_OBSTACLE_SPACING_Z = 9.0f // Min longitudinal gap between hazards in same lane
        const val MIN_JUMP_TO_SLIDE_SPACING_Z = 11.0f // Min gap between a Jump and a subsequent Slide
        const val CO_OCCURRENCE_TOLERANCE_Z = 2.0f // Distance considered "simultaneous" across lanes
    }

    /**
     * Validates a list of active obstacles in a segment.
     * Returns true if valid, false if an impossible combination exists.
     */
    fun validate(obstacles: List<Obstacle>): Boolean {
        val active = obstacles.filter { it.isActive }
        if (active.isEmpty()) return true

        // 1. Check for simultaneous 3-lane blockade
        val zGroups = groupByZProximity(active, CO_OCCURRENCE_TOLERANCE_Z)
        for (group in zGroups) {
            val lanesBlocked = group.map { it.lane }.toSet()
            if (lanesBlocked.size >= GameConstants.LANE_COUNT) {
                // All 3 lanes have obstacles at the same Z.
                // Must ensure at least one is jumpable or slidable!
                val allImpassable = group.all { it.type == ObstacleType.CYBER_BLOCK }
                if (allImpassable) return false

                // If all lanes require conflicting actions simultaneously (e.g. must jump AND slide at same time), invalid!
                val hasJump = group.any { it.type == ObstacleType.LOW_HURDLE }
                val hasSlide = group.any { it.type == ObstacleType.HIGH_BEAM }
                val hasBlock = group.any { it.type == ObstacleType.CYBER_BLOCK }
                if (hasBlock && hasJump && hasSlide) {
                    // Conflicting mechanics with no clear neutral choice
                    return false
                }
            }
        }

        // 2. Check spacing between consecutive obstacles in the same lane
        for (i in active.indices) {
            for (j in i + 1 until active.size) {
                val o1 = active[i]
                val o2 = active[j]
                if (o1.lane == o2.lane) {
                    val gap = abs(o1.z - o2.z)
                    if (gap < MIN_OBSTACLE_SPACING_Z) return false

                    // If o1 is jump hurdle and o2 is high beam, ensure enough recovery room
                    val first = if (o1.z > o2.z) o1 else o2 // player moves from 0 to -Z, so larger Z is encountered first
                    val second = if (o1.z > o2.z) o2 else o1
                    if (first.type == ObstacleType.LOW_HURDLE && second.type == ObstacleType.HIGH_BEAM) {
                        if (gap < MIN_JUMP_TO_SLIDE_SPACING_Z) return false
                    }
                }
            }
        }

        return true
    }

    /**
     * Sanitizes obstacles in-place by removing or replacing invalid obstacles.
     */
    fun sanitize(obstacles: MutableList<Obstacle>) {
        val active = obstacles.filter { it.isActive }
        if (active.isEmpty()) return

        // 1. Check for 3-lane simultaneous blocks
        val zGroups = groupByZProximity(active, CO_OCCURRENCE_TOLERANCE_Z)
        for (group in zGroups) {
            val lanes = group.map { it.lane }.toSet()
            if (lanes.size >= GameConstants.LANE_COUNT) {
                // Ensure at least one lane is completely cleared
                val toDeactivate = group.randomOrNull()
                toDeactivate?.isActive = false
            }
        }

        // 2. Remove violating obstacles with insufficient spacing in same lane
        val remaining = obstacles.filter { it.isActive }
        for (i in remaining.indices) {
            val o1 = remaining[i]
            if (!o1.isActive) continue

            for (j in i + 1 until remaining.size) {
                val o2 = remaining[j]
                if (!o2.isActive || o1.lane != o2.lane) continue

                val gap = abs(o1.z - o2.z)
                if (gap < MIN_OBSTACLE_SPACING_Z) {
                    // Deactivate the second obstacle to keep track fair
                    o2.isActive = false
                } else {
                    val first = if (o1.z > o2.z) o1 else o2
                    val second = if (o1.z > o2.z) o2 else o1
                    if (first.type == ObstacleType.LOW_HURDLE && second.type == ObstacleType.HIGH_BEAM && gap < MIN_JUMP_TO_SLIDE_SPACING_Z) {
                        second.isActive = false
                    }
                }
            }
        }
    }

    private fun groupByZProximity(obstacles: List<Obstacle>, tolerance: Float): List<List<Obstacle>> {
        val groups = ArrayList<ArrayList<Obstacle>>()
        for (obs in obstacles) {
            var added = false
            for (group in groups) {
                if (abs(group[0].z - obs.z) <= tolerance) {
                    group.add(obs)
                    added = true
                    break
                }
            }
            if (!added) {
                groups.add(arrayListOf(obs))
            }
        }
        return groups
    }
}
