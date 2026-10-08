package com.example.longrunner.game.core

/**
 * Phase Shift Energy and Reality State Manager.
 * Manages the runner's dual-frequency phase energy reserve, consumption,
 * recharge rates, transition smoothing, and reality shifting rules.
 */
class PhaseEnergyManager {

    var phaseEnergy: Float = 50.0f
        private set

    val maxPhaseEnergy: Float = 100.0f
    val minActivationEnergy: Float = 20.0f

    var isPhaseShiftActive: Boolean = false
        private set

    // Transition progress (0.0 = normal world, 1.0 = phase reality) for shader lerping
    var phaseFrequencyTransition: Float = 0.0f
        private set

    val consumptionRate: Float = 16.0f // consumes 16 energy per second (~6.25s duration)
    val rechargeRate: Float = 3.5f     // passively recharges 3.5 energy per second

    val energyProgress: Float
        get() = (phaseEnergy / maxPhaseEnergy).coerceIn(0f, 1f)

    val canActivate: Boolean
        get() = phaseEnergy >= minActivationEnergy

    fun reset() {
        phaseEnergy = 50.0f
        isPhaseShiftActive = false
        phaseFrequencyTransition = 0.0f
    }

    fun togglePhaseShift(): Boolean {
        return if (isPhaseShiftActive) {
            deactivatePhaseShift()
            false
        } else {
            activatePhaseShift()
        }
    }

    fun activatePhaseShift(): Boolean {
        if (!isPhaseShiftActive && canActivate) {
            isPhaseShiftActive = true
            return true
        }
        return false
    }

    fun deactivatePhaseShift() {
        isPhaseShiftActive = false
    }

    fun addEnergy(amount: Float, modifier: Float = 1.0f) {
        val effectiveAmount = amount * modifier
        phaseEnergy = (phaseEnergy + effectiveAmount).coerceIn(0f, maxPhaseEnergy)
    }

    fun update(dt: Float, rechargeModifier: Float = 1.0f) {
        if (isPhaseShiftActive) {
            // Consume phase energy
            phaseEnergy -= consumptionRate * dt
            if (phaseEnergy <= 0f) {
                phaseEnergy = 0f
                isPhaseShiftActive = false
            }
            // Lerp transition toward 1.0 (phase reality)
            phaseFrequencyTransition = (phaseFrequencyTransition + 5.0f * dt).coerceAtMost(1.0f)
        } else {
            // Passive recharge scaled by character phase modifier (e.g. Nova 1.5x)
            phaseEnergy = (phaseEnergy + rechargeRate * rechargeModifier * dt).coerceAtMost(maxPhaseEnergy)
            // Lerp transition back toward 0.0 (normal world)
            phaseFrequencyTransition = (phaseFrequencyTransition - 5.0f * dt).coerceAtLeast(0.0f)
        }
    }
}
