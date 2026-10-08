package com.example.longrunner.game.powerups

import android.content.Context
import android.content.SharedPreferences
import kotlin.math.max

enum class PowerUpType(
    val displayName: String,
    val description: String,
    val baseDuration: Float,
    val durationPerLevel: Float,
    val symbol: String
) {
    KINETIC_SHIELD(
        displayName = "Kinetic Shield",
        description = "Absorbs 1 collision impact and grants brief invulnerability",
        baseDuration = 5.0f,
        durationPerLevel = 1.0f,
        symbol = "🛡️"
    ),
    OVERDRIVE(
        displayName = "Overdrive Booster",
        description = "High-speed indestructible charge plowing through all obstacles",
        baseDuration = 6.0f,
        durationPerLevel = 1.2f,
        symbol = "⚡"
    ),
    PHASE_BATTERY(
        displayName = "Phase Battery",
        description = "Locks Phase Energy at 100% with infinite dual-reality duration",
        baseDuration = 8.0f,
        durationPerLevel = 1.5f,
        symbol = "🔋"
    ),
    SCORE_AMPLIFIER(
        displayName = "Score Amplifier",
        description = "Adds +3x score multiplier surge to all collected points",
        baseDuration = 10.0f,
        durationPerLevel = 2.0f,
        symbol = "✨"
    ),
    TIME_BRAKE(
        displayName = "Time Brake",
        description = "Dilates temporal flow, slowing the world to 0.50x speed",
        baseDuration = 6.5f,
        durationPerLevel = 1.5f,
        symbol = "⏱️"
    ),
    MAGNET(
        displayName = "Quantum Magnet",
        description = "Attracts all nearby gems, shards, and credits directly to your position",
        baseDuration = 10.0f,
        durationPerLevel = 2.0f,
        symbol = "🧲"
    ),
    HOVERBOARD(
        displayName = "Neon Hoverboard",
        description = "Mag-lev cyber deck with plasma thrusters that absorbs 1 fatal crash impact",
        baseDuration = 12.0f,
        durationPerLevel = 2.0f,
        symbol = "🛹"
    )
}

data class PowerUpSlot(
    val type: PowerUpType,
    var remainingTime: Float = 0f,
    var totalDuration: Float = 0f
) {
    val isActive: Boolean get() = remainingTime > 0f
    val progress: Float get() = if (totalDuration > 0f) (remainingTime / totalDuration).coerceIn(0f, 1f) else 0f
}

/**
 * Manages power-up activation, real-time timers, shield absorption,
 * and persistent vault upgrade levels.
 */
class PowerUpManager(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("neon_run_upgrades", Context.MODE_PRIVATE)

    // Active power-up timers
    val slots = HashMap<PowerUpType, PowerUpSlot>().apply {
        for (type in PowerUpType.values()) {
            put(type, PowerUpSlot(type))
        }
    }

    // Direct active flags for high-frequency physics checks
    var hasShield: Boolean = false
        private set
    var isOverdriveActive: Boolean = false
        private set
    var isPhaseBatteryActive: Boolean = false
        private set
    var isScoreAmplifierActive: Boolean = false
        private set
    var isTimeBrakeActive: Boolean = false
        private set
    var isMagnetActive: Boolean = false
        private set
    var isHoverboardActive: Boolean = false
        private set

    var hoverboardCharges: Int = 1

    var invulnerabilityTimer: Float = 0f
        private set

    val isInvulnerable: Boolean
        get() = isOverdriveActive || invulnerabilityTimer > 0f

    // Callbacks
    var onPowerUpActivated: ((PowerUpType) -> Unit)? = null
    var onPowerUpExpired: ((PowerUpType) -> Unit)? = null
    var onShieldBroken: (() -> Unit)? = null
    var onHoverboardBroken: (() -> Unit)? = null

    fun resetRun() {
        for (slot in slots.values) {
            slot.remainingTime = 0f
            slot.totalDuration = 0f
        }
        hasShield = false
        isOverdriveActive = false
        isPhaseBatteryActive = false
        isScoreAmplifierActive = false
        isTimeBrakeActive = false
        isMagnetActive = false
        isHoverboardActive = false
        hoverboardCharges = 1
        invulnerabilityTimer = 0f
    }

    fun getUpgradeLevel(type: PowerUpType): Int {
        return prefs?.getInt("lvl_${type.name}", 1) ?: 1
    }

    fun getUpgradeCost(type: PowerUpType): Int {
        val currentLvl = getUpgradeLevel(type)
        if (currentLvl >= 5) return -1 // Max level
        return when (currentLvl) {
            1 -> 60
            2 -> 120
            3 -> 240
            4 -> 400
            else -> 600
        }
    }

    fun upgradePowerUp(type: PowerUpType, availableCredits: Int): Int {
        val cost = getUpgradeCost(type)
        if (cost in 1..availableCredits) {
            val nextLvl = getUpgradeLevel(type) + 1
            prefs?.edit()?.putInt("lvl_${type.name}", nextLvl)?.apply()
            return cost
        }
        return -1
    }

    fun getEffectiveDuration(type: PowerUpType): Float {
        val level = getUpgradeLevel(type)
        return type.baseDuration + (level - 1) * type.durationPerLevel
    }

    fun activatePowerUp(type: PowerUpType) {
        val duration = getEffectiveDuration(type)
        val slot = slots[type] ?: return
        slot.remainingTime = duration
        slot.totalDuration = duration

        when (type) {
            PowerUpType.KINETIC_SHIELD -> {
                hasShield = true
            }
            PowerUpType.OVERDRIVE -> {
                isOverdriveActive = true
            }
            PowerUpType.PHASE_BATTERY -> {
                isPhaseBatteryActive = true
            }
            PowerUpType.SCORE_AMPLIFIER -> {
                isScoreAmplifierActive = true
            }
            PowerUpType.TIME_BRAKE -> {
                isTimeBrakeActive = true
            }
            PowerUpType.MAGNET -> {
                isMagnetActive = true
            }
            PowerUpType.HOVERBOARD -> {
                isHoverboardActive = true
            }
        }

        onPowerUpActivated?.invoke(type)
    }

    fun activateHoverboard(customDuration: Float? = null) {
        val duration = customDuration ?: getEffectiveDuration(PowerUpType.HOVERBOARD)
        val slot = slots[PowerUpType.HOVERBOARD] ?: return
        slot.remainingTime = duration
        slot.totalDuration = duration
        isHoverboardActive = true
        onPowerUpActivated?.invoke(PowerUpType.HOVERBOARD)
    }

    fun update(dt: Float) {
        // Invulnerability frames decay
        if (invulnerabilityTimer > 0f) {
            invulnerabilityTimer -= dt
            if (invulnerabilityTimer < 0f) invulnerabilityTimer = 0f
        }

        // Update slots
        for (slot in slots.values) {
            if (slot.remainingTime > 0f) {
                slot.remainingTime -= dt
                if (slot.remainingTime <= 0f) {
                    slot.remainingTime = 0f
                    deactivateSlot(slot.type)
                    onPowerUpExpired?.invoke(slot.type)
                }
            }
        }
    }

    private fun deactivateSlot(type: PowerUpType) {
        when (type) {
            PowerUpType.KINETIC_SHIELD -> hasShield = false
            PowerUpType.OVERDRIVE -> isOverdriveActive = false
            PowerUpType.PHASE_BATTERY -> isPhaseBatteryActive = false
            PowerUpType.SCORE_AMPLIFIER -> isScoreAmplifierActive = false
            PowerUpType.TIME_BRAKE -> isTimeBrakeActive = false
            PowerUpType.MAGNET -> isMagnetActive = false
            PowerUpType.HOVERBOARD -> isHoverboardActive = false
        }
    }

    /**
     * Resolves collision interaction with active Hoverboard.
     * Absorbs fatal collision impact, destroys hoverboard, and grants 1.4s invulnerability frames.
     */
    fun absorbHoverboardCollision(): Boolean {
        if (isHoverboardActive) {
            isHoverboardActive = false
            slots[PowerUpType.HOVERBOARD]?.remainingTime = 0f
            invulnerabilityTimer = 1.4f
            onHoverboardBroken?.invoke()
            return true
        }
        return false
    }

    /**
     * Resolves collision interaction with active shields, hoverboard, or invulnerability.
     * Returns true if the impact was absorbed/negated, false if lethal.
     */
    fun absorbCollision(): Boolean {
        if (isOverdriveActive) {
            // Overdrive plows through obstacles without taking damage
            return true
        }

        if (invulnerabilityTimer > 0f) {
            // In grace invulnerability frames
            return true
        }

        if (isHoverboardActive) {
            return absorbHoverboardCollision()
        }

        if (hasShield) {
            // Shatter shield and grant 1.2s invulnerability frames
            hasShield = false
            slots[PowerUpType.KINETIC_SHIELD]?.remainingTime = 0f
            invulnerabilityTimer = 1.2f
            onShieldBroken?.invoke()
            return true
        }

        return false
    }

    fun getRemainingTime(type: PowerUpType): Float = slots[type]?.remainingTime ?: 0f
    fun getProgress(type: PowerUpType): Float = slots[type]?.progress ?: 0f
    fun isPowerUpActive(type: PowerUpType): Boolean = slots[type]?.isActive ?: false
}
