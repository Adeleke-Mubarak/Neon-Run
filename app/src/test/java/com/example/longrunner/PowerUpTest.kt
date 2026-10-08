package com.example.longrunner

import com.example.longrunner.game.powerups.PowerUpManager
import com.example.longrunner.game.powerups.PowerUpType
import com.example.longrunner.game.score.ScoreManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PowerUpTest {

    private lateinit var powerUpManager: PowerUpManager
    private lateinit var scoreManager: ScoreManager

    @Before
    fun setUp() {
        // Initialize without Android Context for pure JVM unit testing
        powerUpManager = PowerUpManager(null)
        powerUpManager.resetRun()
        scoreManager = ScoreManager(null)
        scoreManager.reset()
    }

    @Test
    fun testPowerUpInitialStateAndActivation() {
        assertFalse(powerUpManager.hasShield)
        assertFalse(powerUpManager.isOverdriveActive)
        assertFalse(powerUpManager.isPhaseBatteryActive)
        assertFalse(powerUpManager.isScoreAmplifierActive)
        assertFalse(powerUpManager.isTimeBrakeActive)
        assertFalse(powerUpManager.isMagnetActive)
        assertFalse(powerUpManager.isHoverboardActive)
        assertEquals(0f, powerUpManager.invulnerabilityTimer, 0.001f)

        // Activate Kinetic Shield
        powerUpManager.activatePowerUp(PowerUpType.KINETIC_SHIELD)
        assertTrue(powerUpManager.hasShield)
        assertTrue(powerUpManager.isPowerUpActive(PowerUpType.KINETIC_SHIELD))
        assertEquals(PowerUpType.KINETIC_SHIELD.baseDuration, powerUpManager.getRemainingTime(PowerUpType.KINETIC_SHIELD), 0.01f)

        // Activate Overdrive
        powerUpManager.activatePowerUp(PowerUpType.OVERDRIVE)
        assertTrue(powerUpManager.isOverdriveActive)
        assertTrue(powerUpManager.isInvulnerable)

        // Activate Phase Battery
        powerUpManager.activatePowerUp(PowerUpType.PHASE_BATTERY)
        assertTrue(powerUpManager.isPhaseBatteryActive)

        // Activate Score Amplifier
        powerUpManager.activatePowerUp(PowerUpType.SCORE_AMPLIFIER)
        assertTrue(powerUpManager.isScoreAmplifierActive)

        // Activate Time Brake
        powerUpManager.activatePowerUp(PowerUpType.TIME_BRAKE)
        assertTrue(powerUpManager.isTimeBrakeActive)

        // Activate Magnet
        powerUpManager.activatePowerUp(PowerUpType.MAGNET)
        assertTrue(powerUpManager.isMagnetActive)
        assertTrue(powerUpManager.isPowerUpActive(PowerUpType.MAGNET))
        assertEquals(PowerUpType.MAGNET.baseDuration, powerUpManager.getRemainingTime(PowerUpType.MAGNET), 0.01f)

        // Activate Hoverboard
        powerUpManager.activatePowerUp(PowerUpType.HOVERBOARD)
        assertTrue(powerUpManager.isHoverboardActive)
        assertTrue(powerUpManager.isPowerUpActive(PowerUpType.HOVERBOARD))
        assertEquals(PowerUpType.HOVERBOARD.baseDuration, powerUpManager.getRemainingTime(PowerUpType.HOVERBOARD), 0.01f)
    }

    @Test
    fun testPowerUpCountdownAndExpirationCallback() {
        var expiredType: PowerUpType? = null
        powerUpManager.onPowerUpExpired = { type ->
            expiredType = type
        }

        powerUpManager.activatePowerUp(PowerUpType.OVERDRIVE)
        val baseDuration = PowerUpType.OVERDRIVE.baseDuration
        assertEquals(baseDuration, powerUpManager.getRemainingTime(PowerUpType.OVERDRIVE), 0.01f)

        // Advance 2 seconds
        powerUpManager.update(2.0f)
        assertTrue(powerUpManager.isOverdriveActive)
        assertEquals(baseDuration - 2.0f, powerUpManager.getRemainingTime(PowerUpType.OVERDRIVE), 0.01f)

        // Advance remaining time + 0.1s to expire
        powerUpManager.update(baseDuration - 1.9f)
        assertFalse(powerUpManager.isOverdriveActive)
        assertEquals(PowerUpType.OVERDRIVE, expiredType)
    }

    @Test
    fun testKineticShieldCollisionAbsorptionAndInvulnerabilityFrames() {
        var shieldBrokenTriggered = false
        powerUpManager.onShieldBroken = {
            shieldBrokenTriggered = true
        }

        // Before shield: fatal collision
        assertFalse("Without shield or overdrive, collision cannot be absorbed", powerUpManager.absorbCollision())

        // Activate shield
        powerUpManager.activatePowerUp(PowerUpType.KINETIC_SHIELD)
        assertTrue(powerUpManager.hasShield)

        // First collision: absorbed!
        val absorbedFirst = powerUpManager.absorbCollision()
        assertTrue("Kinetic shield must absorb first collision", absorbedFirst)
        assertTrue("Callback must fire when shield breaks", shieldBrokenTriggered)
        assertFalse("Shield must shatter after absorbing hit", powerUpManager.hasShield)
        assertTrue("Player should gain invulnerability frames", powerUpManager.invulnerabilityTimer > 1.0f)
        assertTrue("Player is invulnerable during i-frames", powerUpManager.isInvulnerable)

        // Second immediate collision during grace frames: absorbed by i-frames
        val absorbedSecond = powerUpManager.absorbCollision()
        assertTrue("Grace invulnerability frames should absorb follow-up hit", absorbedSecond)

        // Advance past invulnerability duration (1.2s)
        powerUpManager.update(1.3f)
        assertEquals(0f, powerUpManager.invulnerabilityTimer, 0.001f)
        assertFalse("Invulnerability should expire after timer ends", powerUpManager.isInvulnerable)

        // Third collision after i-frames expire: lethal!
        val absorbedThird = powerUpManager.absorbCollision()
        assertFalse("Collision after shield break and i-frame expiry must be lethal", absorbedThird)
    }

    @Test
    fun testOverdrivePlowingIndestructible() {
        powerUpManager.activatePowerUp(PowerUpType.OVERDRIVE)
        assertTrue(powerUpManager.isOverdriveActive)
        assertTrue(powerUpManager.isInvulnerable)

        // Overdrive absorbs multiple consecutive impacts without depleting
        for (i in 1..5) {
            val absorbed = powerUpManager.absorbCollision()
            assertTrue("Overdrive must plow through obstacle $i", absorbed)
            assertTrue("Overdrive must remain active after impact $i", powerUpManager.isOverdriveActive)
        }
    }

    @Test
    fun testDurationCalculationPerUpgradeLevel() {
        for (type in PowerUpType.values()) {
            val lvl1Duration = type.baseDuration
            val lvl2Duration = type.baseDuration + type.durationPerLevel
            val lvl5Duration = type.baseDuration + 4 * type.durationPerLevel

            assertTrue("Level 2 duration must be strictly greater than level 1", lvl2Duration > lvl1Duration)
            assertTrue("Level 5 duration must be strictly greater than level 2", lvl5Duration > lvl2Duration)
        }

        // Specific test for Kinetic Shield
        val shieldLvl1 = PowerUpType.KINETIC_SHIELD.baseDuration // 5.0s
        val shieldLvl3 = PowerUpType.KINETIC_SHIELD.baseDuration + 2 * PowerUpType.KINETIC_SHIELD.durationPerLevel // 5 + 2 = 7.0s
        assertEquals(5.0f, shieldLvl1, 0.001f)
        assertEquals(7.0f, shieldLvl3, 0.001f)
    }

    @Test
    fun testScoreAmplifierMultiplierSurge() {
        // Distance 100m at speed 20 m/s with 1x combo
        scoreManager.update(playerZ = -100f, speed = 20f, combo = 1, dt = 1f, extraMultiplier = 0)
        val normalMultiplier = scoreManager.multiplier // (20/10) * 1 * 1 = 2
        assertEquals(2, normalMultiplier)

        // With Score Amplifier active (+3 surge)
        scoreManager.update(playerZ = -100f, speed = 20f, combo = 1, dt = 1f, extraMultiplier = 3)
        val amplifiedMultiplier = scoreManager.multiplier // 2 + 3 = 5
        assertEquals(5, amplifiedMultiplier)
        assertTrue(scoreManager.score > 0)
    }

    @Test
    fun testResetRunClearsAllActivePowerUps() {
        powerUpManager.activatePowerUp(PowerUpType.KINETIC_SHIELD)
        powerUpManager.activatePowerUp(PowerUpType.OVERDRIVE)
        powerUpManager.activatePowerUp(PowerUpType.PHASE_BATTERY)
        powerUpManager.activatePowerUp(PowerUpType.SCORE_AMPLIFIER)
        powerUpManager.activatePowerUp(PowerUpType.TIME_BRAKE)
        powerUpManager.activatePowerUp(PowerUpType.MAGNET)
        powerUpManager.activatePowerUp(PowerUpType.HOVERBOARD)

        assertTrue(powerUpManager.hasShield)
        assertTrue(powerUpManager.isOverdriveActive)
        assertTrue(powerUpManager.isMagnetActive)
        assertTrue(powerUpManager.isHoverboardActive)

        powerUpManager.resetRun()

        assertFalse(powerUpManager.hasShield)
        assertFalse(powerUpManager.isOverdriveActive)
        assertFalse(powerUpManager.isPhaseBatteryActive)
        assertFalse(powerUpManager.isScoreAmplifierActive)
        assertFalse(powerUpManager.isTimeBrakeActive)
        assertFalse(powerUpManager.isMagnetActive)
        assertFalse(powerUpManager.isHoverboardActive)
        assertEquals(0f, powerUpManager.invulnerabilityTimer, 0.001f)
    }

    @Test
    fun testHoverboardCollisionAbsorptionAndInvulnerabilityFrames() {
        var brokenTriggered = false
        powerUpManager.onHoverboardBroken = {
            brokenTriggered = true
        }

        // Before hoverboard: lethal
        assertFalse(powerUpManager.absorbCollision())

        // Activate Hoverboard
        powerUpManager.activatePowerUp(PowerUpType.HOVERBOARD)
        assertTrue(powerUpManager.isHoverboardActive)

        // First impact: absorbed by hoverboard!
        val absorbedFirst = powerUpManager.absorbCollision()
        assertTrue("Hoverboard must absorb fatal collision impact", absorbedFirst)
        assertTrue("Callback must fire when hoverboard breaks", brokenTriggered)
        assertFalse("Hoverboard must shatter after absorbing hit", powerUpManager.isHoverboardActive)
        assertTrue("Player should gain invulnerability frames", powerUpManager.invulnerabilityTimer > 1.0f)
        assertTrue("Player is invulnerable during grace frames", powerUpManager.isInvulnerable)

        // Second immediate impact during grace frames: absorbed by i-frames!
        val absorbedSecond = powerUpManager.absorbCollision()
        assertTrue("Grace invulnerability frames should absorb follow-up hit", absorbedSecond)

        // Advance past invulnerability duration (1.4s)
        powerUpManager.update(1.5f)
        assertEquals(0f, powerUpManager.invulnerabilityTimer, 0.001f)
        assertFalse("Invulnerability should expire after timer ends", powerUpManager.isInvulnerable)

        // Third collision after i-frames expire: lethal!
        val absorbedThird = powerUpManager.absorbCollision()
        assertFalse("Collision after hoverboard break and i-frame expiry must be lethal", absorbedThird)
    }

    @Test
    fun testMagnetCountdownAndExpiration() {
        var expiredType: PowerUpType? = null
        powerUpManager.onPowerUpExpired = { type ->
            expiredType = type
        }

        powerUpManager.activatePowerUp(PowerUpType.MAGNET)
        assertTrue(powerUpManager.isMagnetActive)
        val duration = PowerUpType.MAGNET.baseDuration

        // Tick partial time
        powerUpManager.update(duration * 0.5f)
        assertTrue(powerUpManager.isMagnetActive)

        // Tick past remaining time
        powerUpManager.update(duration * 0.5f + 0.1f)
        assertFalse(powerUpManager.isMagnetActive)
        assertEquals(PowerUpType.MAGNET, expiredType)
    }
}
