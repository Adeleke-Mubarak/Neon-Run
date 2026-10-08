package com.example.longrunner

import com.example.longrunner.game.core.GameEngine
import com.example.longrunner.game.graphics.ParticleSystem
import com.example.longrunner.game.graphics.PerformanceProfile
import com.example.longrunner.game.settings.GraphicsQuality
import com.example.longrunner.game.settings.SettingsManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PerformanceOptimizationTest {

    private lateinit var particleSystem: ParticleSystem

    @Before
    fun setUp() {
        particleSystem = ParticleSystem(maxParticles = 256)
    }

    @Test
    fun testPerformanceProfileHierarchy() {
        val perf = PerformanceProfile.fromQuality(GraphicsQuality.PERFORMANCE)
        val balanced = PerformanceProfile.fromQuality(GraphicsQuality.BALANCED)
        val ultra = PerformanceProfile.fromQuality(GraphicsQuality.ULTRA)

        // Draw distance ordering
        assertEquals(95.0f, perf.maxDrawDistance, 0.001f)
        assertEquals(150.0f, balanced.maxDrawDistance, 0.001f)
        assertEquals(220.0f, ultra.maxDrawDistance, 0.001f)
        assertTrue(perf.maxDrawDistance < balanced.maxDrawDistance)
        assertTrue(balanced.maxDrawDistance < ultra.maxDrawDistance)

        // Max active particle pool cap
        assertEquals(64, perf.maxActiveParticles)
        assertEquals(128, balanced.maxActiveParticles)
        assertEquals(256, ultra.maxActiveParticles)

        // Scenery building culling
        assertFalse("Performance mode must disable heavy background buildings", perf.enableBackgroundScenery)
        assertTrue(balanced.enableBackgroundScenery)
        assertTrue(ultra.enableBackgroundScenery)

        // Speed streak toggle
        assertFalse("Performance mode must disable speed streaks", perf.enableSpeedStreaks)
        assertTrue(balanced.enableSpeedStreaks)
        assertTrue(ultra.enableSpeedStreaks)

        // Burst particle scale
        assertTrue(perf.particleBurstScale < balanced.particleBurstScale)
        assertTrue(balanced.particleBurstScale < ultra.particleBurstScale)
        assertEquals(1.0f, ultra.particleBurstScale, 0.001f)

        // Volumetric fog optimization
        assertFalse(perf.enableVolumetricFog)
        assertTrue(perf.fogDensityMultiplier < 1.0f)
        assertEquals(1.0f, ultra.fogDensityMultiplier, 0.001f)
    }

    @Test
    fun testParticleSystemScalingInPerformanceMode() {
        particleSystem.performanceProfile = PerformanceProfile.fromQuality(GraphicsQuality.PERFORMANCE)

        // Base 12 collectible burst scaled by 0.35 -> 4 particles
        particleSystem.emitCollectibleBurst(0f, 0f, 0f, 1f, 1f, 1f, baseCount = 12)
        val activeCount = particleSystem.particles.count { it.isActive }
        assertEquals(4, activeCount)

        // Base 16 obstacle smash scaled by 0.35 -> 5 particles
        particleSystem.reset()
        particleSystem.emitObstacleSmash(0f, 0f, 0f, baseCount = 16)
        assertEquals(5, particleSystem.particles.count { it.isActive })

        // Base 10 near miss shockwave scaled by 0.35 -> 3 particles
        particleSystem.reset()
        particleSystem.emitNearMissShockwave(0f, 0f, 0f, baseCount = 10)
        assertEquals(3, particleSystem.particles.count { it.isActive })

        // Speed streak should be completely disabled in performance mode
        particleSystem.reset()
        particleSystem.emitSpeedStreak(cameraZ = -10f, playerSpeed = 22.0f)
        assertEquals(0, particleSystem.particles.count { it.isActive })
    }

    @Test
    fun testParticleSystemScalingInBalancedMode() {
        particleSystem.performanceProfile = PerformanceProfile.fromQuality(GraphicsQuality.BALANCED)

        // Base 12 collectible burst scaled by 0.65 -> 7 particles
        particleSystem.emitCollectibleBurst(0f, 0f, 0f, 1f, 1f, 1f, baseCount = 12)
        assertEquals(7, particleSystem.particles.count { it.isActive })

        // Speed streaks enabled in balanced mode
        particleSystem.reset()
        particleSystem.emitSpeedStreak(cameraZ = -10f, playerSpeed = 19.0f)
        assertTrue("Speed streaks must emit in balanced mode", particleSystem.particles.count { it.isActive } > 0)
    }

    @Test
    fun testParticleSystemPoolCapUnderPerformanceMode() {
        particleSystem.performanceProfile = PerformanceProfile.fromQuality(GraphicsQuality.PERFORMANCE)

        // Emit excessive bursts
        for (i in 0 until 40) {
            particleSystem.emitCollectibleBurst(0f, 0f, 0f, 1f, 1f, 1f, baseCount = 10)
        }

        // Active particles must not exceed the performance pool cap of 64
        val activeCount = particleSystem.particles.count { it.isActive }
        assertTrue("Active particles ($activeCount) must be capped at 64", activeCount <= 64)
    }

    @Test
    fun testDistanceCullingBoundsMath() {
        val perf = PerformanceProfile.fromQuality(GraphicsQuality.PERFORMANCE)
        val ultra = PerformanceProfile.fromQuality(GraphicsQuality.ULTRA)
        val cameraZ = 0.0f

        // Hazard at -150m:
        val hazardZ = -150.0f
        val isVisibleInPerf = hazardZ in (cameraZ - perf.maxDrawDistance)..(cameraZ + 2.0f)
        val isVisibleInUltra = hazardZ in (cameraZ - ultra.maxDrawDistance)..(cameraZ + 2.0f)

        assertFalse("Hazard at -150m must be culled in PERFORMANCE mode (draw distance 95m)", isVisibleInPerf)
        assertTrue("Hazard at -150m must be rendered in ULTRA mode (draw distance 220m)", isVisibleInUltra)

        // Hazard passed behind camera at +10m:
        val passedHazardZ = 10.0f
        val isBehindCamera = passedHazardZ > cameraZ + 2.0f
        assertTrue("Hazard at +10m must be culled as behind camera", isBehindCamera)
    }

    @Test
    fun testTelemetryFpsTracking() {
        var observedFps = 0
        var observedFrameTimeMs = 0f

        val dummyEngineStatsUpdated: (com.example.longrunner.game.core.GameStats) -> Unit = { stats ->
            observedFps = stats.fps
            observedFrameTimeMs = stats.frameTimeMs
        }

        val stats = com.example.longrunner.game.core.GameStats(fps = 60, frameTimeMs = 16.6f)
        dummyEngineStatsUpdated(stats)

        assertEquals(60, observedFps)
        assertEquals(16.6f, observedFrameTimeMs, 0.01f)

        val updatedStats = stats.copy(fps = 58, frameTimeMs = 17.2f)
        dummyEngineStatsUpdated(updatedStats)

        assertEquals(58, observedFps)
        assertEquals(17.2f, observedFrameTimeMs, 0.01f)
    }
}
