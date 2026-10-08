package com.example.longrunner.game.score

import kotlin.math.max

/**
 * Skill-based Combo System.
 * Chaining jumps, slides, lane dodges, near-misses, and collectibles builds the combo multiplier.
 */
class ComboManager {

    var currentCombo: Int = 1
        private set

    var comboTimer: Float = 0f
        private set

    var highestCombo: Int = 1
        private set

    var maxComboDuration: Float = 3.5f // seconds before combo decays
    val maxComboLimit: Int = 15
    var isFrozen: Boolean = false

    val decayProgress: Float
        get() = if (currentCombo > 1 && maxComboDuration > 0f) {
            (comboTimer / maxComboDuration).coerceIn(0f, 1f)
        } else {
            0f
        }

    fun reset() {
        currentCombo = 1
        comboTimer = 0f
        highestCombo = 1
    }

    fun registerAction() {
        if (currentCombo < maxComboLimit) {
            currentCombo++
        }
        comboTimer = maxComboDuration
        highestCombo = max(highestCombo, currentCombo)
    }

    fun update(dt: Float) {
        if (currentCombo > 1 && !isFrozen) {
            comboTimer -= dt
            if (comboTimer <= 0f) {
                // Combo expired, reset to baseline
                currentCombo = 1
                comboTimer = 0f
            }
        }
    }
}
