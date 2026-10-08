package com.example.longrunner.game.core

/**
 * Global game balance constants, dimensions, and gameplay tuning values.
 */
object GameConstants {
    // Lane configuration
    const val LANE_COUNT = 3
    const val LANE_WIDTH = 2.2f
    const val LANE_LEFT = -1
    const val LANE_CENTER = 0
    const val LANE_RIGHT = 1

    fun laneToX(lane: Int): Float = when (lane) {
        LANE_LEFT -> -LANE_WIDTH
        LANE_RIGHT -> LANE_WIDTH
        else -> 0.0f
    }

    // Player locomotion
    const val BASE_SPEED = 24.5f // units per second (tuned for heightened excitement)
    const val MAX_SPEED = 48.0f
    const val SPEED_ACCELERATION = 0.65f // acceleration per second (snappier speed ramp)
    const val LANE_SWITCH_SPEED = 15.0f // horizontal lerp speed

    // Jump & Slide physics
    const val JUMP_VELOCITY = 11.5f
    const val GRAVITY = -28.0f
    const val SLIDE_DURATION = 0.75f // seconds
    const val GROUND_Y = 0.0f

    // Player bounding box (AABB)
    const val PLAYER_WIDTH = 0.75f
    const val PLAYER_DEPTH = 0.75f
    const val PLAYER_STAND_HEIGHT = 1.8f
    const val PLAYER_SLIDE_HEIGHT = 0.75f

    // Track generation
    const val SEGMENT_LENGTH = 30.0f
    const val POOL_SEGMENT_COUNT = 10
    const val TRACK_WIDTH = 8.0f // total roadway width
    const val FOG_START = 60.0f
    const val FOG_END = 160.0f
    const val FOG_DENSITY = 0.012f

    // Camera parameters
    const val CAMERA_FOLLOW_DISTANCE = 6.8f
    const val CAMERA_FOLLOW_HEIGHT = 3.6f
    const val CAMERA_LOOK_AHEAD_Z = 12.0f
    const val CAMERA_LOOK_HEIGHT = 1.2f
    const val CAMERA_FOV = 65.0f

    // Scoring
    const val SCORE_PER_METER = 10
    const val SCORE_PER_SHARD = 50
}
