package com.example.longrunner.game.world

import kotlin.math.max
import kotlin.math.min

enum class NullTensionLevel {
    SAFE,       // > 14m behind
    ALERT,      // 8m - 14m behind
    CRITICAL,   // < 8m behind
    CAUGHT      // <= 0.8m behind (Game Over)
}

/**
 * "The Null": A sinister pursuing void anomaly that follows the operative.
 * Closing speed increases when player stumbles or runs slowly;
 * The Null is repelled by high combos, speed, abilities, and power-up explosions.
 */
class NullChaser {

    // Relative distance behind the player in meters (+Z relative to player)
    var distanceBehindPlayer: Float = 18.0f
        private set

    val minDistance: Float = 0.8f   // Caught threshold
    val maxDistance: Float = 24.0f  // Max lag distance

    val tension: Float
        get() = ((18.0f - distanceBehindPlayer) / 16.0f).coerceIn(0f, 1f)

    val tensionLevel: NullTensionLevel
        get() = when {
            distanceBehindPlayer <= minDistance -> NullTensionLevel.CAUGHT
            distanceBehindPlayer < 8.0f -> NullTensionLevel.CRITICAL
            distanceBehindPlayer <= 14.0f -> NullTensionLevel.ALERT
            else -> NullTensionLevel.SAFE
        }

    val isAlert: Boolean get() = tensionLevel == NullTensionLevel.ALERT || tensionLevel == NullTensionLevel.CRITICAL
    val isCritical: Boolean get() = tensionLevel == NullTensionLevel.CRITICAL

    // Heartbeat audio rate timer
    var heartbeatTimer: Float = 0f
        private set

    // Callbacks
    var onHeartbeatPulse: (() -> Unit)? = null
    var onNullSurge: (() -> Unit)? = null
    var onPlayerCaught: (() -> Unit)? = null

    fun reset() {
        distanceBehindPlayer = 18.0f
        heartbeatTimer = 0f
    }

    /**
     * Updates The Null's relative distance.
     * Returns true if The Null catches the player (Game Over).
     */
    fun update(playerSpeed: Float, combo: Int, dt: Float): Boolean {
        // Dynamic closing speed:
        // High speed (>= 22) and high combo (>= 3) pushes The Null back
        // Low speed (< 17) or 0 combo allows The Null to advance
        val speedFactor = when {
            playerSpeed >= 28.0f -> -1.4f   // Falling behind at high sprint
            playerSpeed >= 24.0f -> -0.6f   // Steady pace slightly repels
            playerSpeed >= 20.0f -> 0.7f    // Stumbling or low speed creeps closer
            else -> 2.0f                    // Stumbling/stopped rapidly loses ground!
        }

        val comboRepel = if (combo >= 4) -0.6f else 0.0f
        val deltaDistance = (speedFactor + comboRepel) * dt

        distanceBehindPlayer = (distanceBehindPlayer - deltaDistance).coerceIn(minDistance, maxDistance)

        // Heartbeat timer frequency scales with tension:
        // At safe: heartbeat every ~2.0s
        // At alert: heartbeat every ~1.0s
        // At critical: rapid heartbeat every ~0.45s
        val heartbeatInterval = when {
            isCritical -> 0.40f
            isAlert -> 0.85f
            else -> 1.8f
        }

        heartbeatTimer += dt
        if (heartbeatTimer >= heartbeatInterval) {
            heartbeatTimer = 0f
            if (isAlert || isCritical) {
                onHeartbeatPulse?.invoke()
            }
        }

        // Caught condition
        if (distanceBehindPlayer <= minDistance) {
            onPlayerCaught?.invoke()
            return true
        }

        return false
    }

    /**
     * Called when the player stumbles into a hazard (even if saved by shield/ability).
     * The Null lunges forward instantly!
     */
    fun onPlayerStumble() {
        distanceBehindPlayer = max(minDistance + 0.5f, distanceBehindPlayer - 5.5f)
        onNullSurge?.invoke()
    }

    /**
     * Called when Overdrive or Kinetic Shield detonates.
     * Blasts The Null back by 14m!
     */
    fun onPowerUpBlast() {
        distanceBehindPlayer = min(maxDistance, distanceBehindPlayer + 14.0f)
    }

    /**
     * Called when Phase Shift is activated.
     * Phase vibration repels The Null back by 9m!
     */
    fun onPhaseShiftRepel() {
        distanceBehindPlayer = min(maxDistance, distanceBehindPlayer + 9.0f)
    }

    /**
     * Small pushback when collecting energy/cores.
     */
    fun onEnergyCollected() {
        distanceBehindPlayer = min(maxDistance, distanceBehindPlayer + 0.35f)
    }
}
