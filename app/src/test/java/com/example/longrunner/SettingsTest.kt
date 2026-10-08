package com.example.longrunner

import com.example.longrunner.game.core.GameStats
import com.example.longrunner.game.settings.GraphicsQuality
import com.example.longrunner.game.settings.LaneSensitivity
import com.example.longrunner.game.settings.SettingsManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SettingsTest {

    private lateinit var settingsManager: SettingsManager

    @Before
    fun setUp() {
        // Instantiate without Android Context for pure JVM in-memory testing
        settingsManager = SettingsManager(null)
    }

    @Test
    fun testDefaultSettingsValues() {
        assertEquals(1.0f, settingsManager.sfxVolume, 0.001f)
        assertEquals(0.8f, settingsManager.musicVolume, 0.001f)
        assertTrue(settingsManager.isSfxEnabled)
        assertTrue(settingsManager.isMusicEnabled)
        assertTrue(settingsManager.isScreenShakeEnabled)
        assertTrue(settingsManager.isGlitchShaderEnabled)
        assertTrue(settingsManager.isHapticsEnabled)
        assertEquals(GraphicsQuality.ULTRA, settingsManager.graphicsQuality)
        assertEquals(LaneSensitivity.NORMAL, settingsManager.laneSensitivity)
    }

    @Test
    fun testVolumeClamping() {
        // Over upper bound
        settingsManager.sfxVolume = 1.6f
        assertEquals(1.0f, settingsManager.sfxVolume, 0.001f)

        // Under lower bound
        settingsManager.sfxVolume = -0.4f
        assertEquals(0.0f, settingsManager.sfxVolume, 0.001f)

        // Normal range
        settingsManager.sfxVolume = 0.45f
        assertEquals(0.45f, settingsManager.sfxVolume, 0.001f)

        // Music volume clamping
        settingsManager.musicVolume = 2.5f
        assertEquals(1.0f, settingsManager.musicVolume, 0.001f)

        settingsManager.musicVolume = -1.2f
        assertEquals(0.0f, settingsManager.musicVolume, 0.001f)
    }

    @Test
    fun testSettingsChangeCallback() {
        var callbackFiredCount = 0
        settingsManager.onSettingsChanged = {
            callbackFiredCount++
        }

        settingsManager.sfxVolume = 0.5f
        assertEquals(1, callbackFiredCount)

        settingsManager.musicVolume = 0.2f
        assertEquals(2, callbackFiredCount)

        settingsManager.isSfxEnabled = false
        assertEquals(3, callbackFiredCount)

        settingsManager.graphicsQuality = GraphicsQuality.PERFORMANCE
        assertEquals(4, callbackFiredCount)

        settingsManager.laneSensitivity = LaneSensitivity.HYPER
        assertEquals(5, callbackFiredCount)
    }

    @Test
    fun testLaneSensitivityThresholdMultipliers() {
        assertEquals(1.0f, LaneSensitivity.NORMAL.thresholdMultiplier, 0.001f)
        assertEquals(0.75f, LaneSensitivity.FAST.thresholdMultiplier, 0.001f)
        assertEquals(0.5f, LaneSensitivity.HYPER.thresholdMultiplier, 0.001f)
    }

    @Test
    fun testGraphicsQualityTiers() {
        settingsManager.graphicsQuality = GraphicsQuality.PERFORMANCE
        assertEquals(GraphicsQuality.PERFORMANCE, settingsManager.graphicsQuality)

        settingsManager.graphicsQuality = GraphicsQuality.BALANCED
        assertEquals(GraphicsQuality.BALANCED, settingsManager.graphicsQuality)

        settingsManager.graphicsQuality = GraphicsQuality.ULTRA
        assertEquals(GraphicsQuality.ULTRA, settingsManager.graphicsQuality)
    }

    @Test
    fun testResetToDefaults() {
        // Mutate everything
        settingsManager.sfxVolume = 0.1f
        settingsManager.musicVolume = 0.2f
        settingsManager.isSfxEnabled = false
        settingsManager.isMusicEnabled = false
        settingsManager.isScreenShakeEnabled = false
        settingsManager.isGlitchShaderEnabled = false
        settingsManager.isHapticsEnabled = false
        settingsManager.graphicsQuality = GraphicsQuality.PERFORMANCE
        settingsManager.laneSensitivity = LaneSensitivity.HYPER

        // Reset
        settingsManager.resetToDefaults()

        // Verify restoration of all defaults
        assertEquals(1.0f, settingsManager.sfxVolume, 0.001f)
        assertEquals(0.8f, settingsManager.musicVolume, 0.001f)
        assertTrue(settingsManager.isSfxEnabled)
        assertTrue(settingsManager.isMusicEnabled)
        assertTrue(settingsManager.isScreenShakeEnabled)
        assertTrue(settingsManager.isGlitchShaderEnabled)
        assertTrue(settingsManager.isHapticsEnabled)
        assertEquals(GraphicsQuality.ULTRA, settingsManager.graphicsQuality)
        assertEquals(LaneSensitivity.NORMAL, settingsManager.laneSensitivity)
    }

    @Test
    fun testUnclaimedMissionsCountInGameStats() {
        val statsDefault = GameStats()
        assertEquals(0, statsDefault.unclaimedMissionsCount)

        val statsWithMissions = GameStats(unclaimedMissionsCount = 3)
        assertEquals(3, statsWithMissions.unclaimedMissionsCount)
    }
}
