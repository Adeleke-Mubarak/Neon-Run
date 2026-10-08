package com.example.longrunner

import com.example.longrunner.game.world.NullChaser
import com.example.longrunner.game.world.NullTensionLevel
import com.example.longrunner.game.world.WorldEventManager
import com.example.longrunner.game.world.WorldEventType
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class WorldEventAndNullTest {

    @Test
    fun testWorldEventManagerInitialState() {
        val manager = WorldEventManager()
        assertEquals(WorldEventType.NONE, manager.currentEvent)
        assertFalse(manager.isEventActive)
        assertFalse(manager.isWarningActive)
        assertEquals(0f, manager.eventProgress, 0.001f)
        assertEquals(0f, manager.blackoutFactor, 0.001f)
        assertEquals(0f, manager.glitchFactor, 0.001f)
        assertEquals(0f, manager.searchlightX, 0.001f)
    }

    @Test
    fun testWorldEventManualTriggerAndLifecycle() {
        val manager = WorldEventManager()
        var startedEvent: WorldEventType? = null
        var endedEvent: WorldEventType? = null

        manager.onEventStarted = { startedEvent = it }
        manager.onEventEnded = { endedEvent = it }

        manager.triggerEvent(WorldEventType.CITY_BLACKOUT, duration = 10.0f)
        assertTrue(manager.isEventActive)
        assertEquals(WorldEventType.CITY_BLACKOUT, manager.currentEvent)
        assertEquals(WorldEventType.CITY_BLACKOUT, startedEvent)
        assertEquals(10.0f, manager.totalDuration, 0.001f)
        assertEquals(1.0f, manager.progress, 0.001f)

        // Advance 4 seconds
        manager.update(playerZ = 0f, dt = 4.0f)
        assertTrue(manager.isEventActive)
        assertEquals(6.0f, manager.eventTimer, 0.01f)
        assertEquals(0.6f, manager.progress, 0.01f)

        // Advance past duration
        manager.update(playerZ = 0f, dt = 6.5f)
        assertFalse(manager.isEventActive)
        assertEquals(WorldEventType.NONE, manager.currentEvent)
        assertEquals(WorldEventType.CITY_BLACKOUT, endedEvent)
    }

    @Test
    fun testWorldEventTransitionsAndVisualFactors() {
        val manager = WorldEventManager()

        // Test Blackout factor ramping up
        manager.triggerEvent(WorldEventType.CITY_BLACKOUT, duration = 12.0f)
        manager.update(playerZ = 0f, dt = 0.5f)
        assertTrue("Blackout factor should ramp up", manager.blackoutFactor > 0.3f)

        // End event and verify factor decays towards 0
        manager.endEvent()
        manager.update(playerZ = 0f, dt = 2.0f)
        assertTrue("Blackout factor should decay towards 0", manager.blackoutFactor < 0.2f)

        // Test Drone Swarm searchlight oscillation
        manager.triggerEvent(WorldEventType.DRONE_SWARM, duration = 10.0f)
        manager.update(playerZ = 0f, dt = 0.4f)
        assertTrue("Searchlight X should oscillate", manager.searchlightX != 0f)

        // Test Reality Fracture glitch factor
        manager.triggerEvent(WorldEventType.REALITY_FRACTURE, duration = 10.0f)
        manager.update(playerZ = 0f, dt = 0.3f)
        assertTrue("Glitch factor should be active", manager.glitchFactor > 0f)
    }

    @Test
    fun testWorldEventDistanceWarningAndAutoTrigger() {
        val seededRandom = Random(12345)
        val manager = WorldEventManager(seededRandom)

        var warningTriggered: WorldEventType? = null
        manager.onEventWarning = { warningTriggered = it }

        // Distance 200m (threshold is 350 - 50 = 300m) -> no warning
        manager.update(playerZ = -200f, dt = 0.016f)
        assertFalse(manager.isWarningActive)
        assertNull(warningTriggered)

        // Player reaches -305m (distance = 305m > 300m) -> warning triggers!
        manager.update(playerZ = -305f, dt = 0.016f)
        assertTrue(manager.isWarningActive)
        assertNotNull(warningTriggered)
        assertTrue(manager.warningTitle.isNotEmpty())

        // Count down warning (warning timer is ~3.2s)
        manager.update(playerZ = -310f, dt = 3.5f)
        assertFalse(manager.isWarningActive)
        assertTrue("Event should now be actively running", manager.isEventActive)
        assertEquals(warningTriggered, manager.currentEvent)
    }

    @Test
    fun testNullChaserInitialStateAndTension() {
        val nullChaser = NullChaser()
        assertEquals(18.0f, nullChaser.distanceBehindPlayer, 0.001f)
        assertEquals(0.0f, nullChaser.tension, 0.001f)
        assertEquals(NullTensionLevel.SAFE, nullChaser.tensionLevel)
        assertFalse(nullChaser.isAlert)
        assertFalse(nullChaser.isCritical)
    }

    @Test
    fun testNullChaserPursuitDynamics() {
        val nullChaser = NullChaser()

        // At low speed (14 m/s) and combo 0, The Null advances
        val caught = nullChaser.update(playerSpeed = 14.0f, combo = 0, dt = 2.0f)
        assertFalse(caught)
        assertTrue("The Null should advance on slow speed", nullChaser.distanceBehindPlayer < 18.0f)
        assertTrue("Tension should increase", nullChaser.tension > 0.0f)

        // Force distance into ALERT (< 14m)
        while (nullChaser.distanceBehindPlayer > 12.0f) {
            nullChaser.update(playerSpeed = 12.0f, combo = 0, dt = 1.0f)
        }
        assertEquals(NullTensionLevel.ALERT, nullChaser.tensionLevel)
        assertTrue(nullChaser.isAlert)
        assertFalse(nullChaser.isCritical)

        // Force distance into CRITICAL (< 8m)
        while (nullChaser.distanceBehindPlayer > 6.0f) {
            nullChaser.update(playerSpeed = 10.0f, combo = 0, dt = 1.0f)
        }
        assertEquals(NullTensionLevel.CRITICAL, nullChaser.tensionLevel)
        assertTrue(nullChaser.isCritical)
        assertTrue("Tension should be high", nullChaser.tension > 0.6f)
    }

    @Test
    fun testNullChaserHeartbeatPulse() {
        val nullChaser = NullChaser()
        var pulseCount = 0
        nullChaser.onHeartbeatPulse = { pulseCount++ }

        // While safe, heartbeat does not pulse via callback
        nullChaser.update(playerSpeed = 22.0f, combo = 0, dt = 2.5f)
        assertEquals(0, pulseCount)

        // Force into critical state
        while (nullChaser.distanceBehindPlayer > 5.0f) {
            nullChaser.update(playerSpeed = 8.0f, combo = 0, dt = 1.0f)
        }

        // In critical, pulses trigger rapidly (~0.45s)
        nullChaser.update(playerSpeed = 10.0f, combo = 0, dt = 0.5f)
        assertTrue("Should have fired heartbeat pulse in critical tension", pulseCount >= 1)
    }

    @Test
    fun testNullChaserRepulsionAndStumble() {
        val nullChaser = NullChaser()

        // Player stumbles -> The Null surges forward
        var surgeFired = false
        nullChaser.onNullSurge = { surgeFired = true }
        val beforeStumble = nullChaser.distanceBehindPlayer
        nullChaser.onPlayerStumble()
        assertTrue(surgeFired)
        assertEquals(beforeStumble - 5.5f, nullChaser.distanceBehindPlayer, 0.01f)

        // Phase shift repel
        val beforePhase = nullChaser.distanceBehindPlayer
        nullChaser.onPhaseShiftRepel()
        assertEquals(beforePhase + 9.0f, nullChaser.distanceBehindPlayer, 0.01f)

        // Power-up blast
        nullChaser.onPlayerStumble()
        val beforeBlast = nullChaser.distanceBehindPlayer
        nullChaser.onPowerUpBlast()
        assertEquals(nullChaser.maxDistance, nullChaser.distanceBehindPlayer, 0.01f)

        // Energy collect nudge
        val beforeEnergy = 15.0f
        nullChaser.reset()
        nullChaser.update(playerSpeed = 15.0f, combo = 0, dt = 2.0f)
        val distBefore = nullChaser.distanceBehindPlayer
        nullChaser.onEnergyCollected()
        assertEquals(distBefore + 0.35f, nullChaser.distanceBehindPlayer, 0.01f)
    }

    @Test
    fun testNullChaserCatchLethalCondition() {
        val nullChaser = NullChaser()
        var caughtFired = false
        nullChaser.onPlayerCaught = { caughtFired = true }

        // Drive distance down to <= 0.8m
        var caught = false
        for (i in 0 until 50) {
            caught = nullChaser.update(playerSpeed = 6.0f, combo = 0, dt = 1.0f)
            if (caught) break
        }

        assertTrue("Update should return true when caught", caught)
        assertTrue("onPlayerCaught callback should have fired", caughtFired)
        assertEquals(NullTensionLevel.CAUGHT, nullChaser.tensionLevel)
        assertTrue(nullChaser.distanceBehindPlayer <= nullChaser.minDistance)
    }
}
