package com.example.longrunner.game.graphics

import com.example.longrunner.game.settings.GraphicsQuality

/**
 * Performance profile parameters tailored for mobile GPU and battery efficiency.
 * Scales draw distance, particle counts, scenery LOD, and lighting complexity.
 */
data class PerformanceProfile(
    val quality: GraphicsQuality,
    val maxDrawDistance: Float,
    val maxActiveParticles: Int,
    val particleBurstScale: Float,
    val enableBackgroundScenery: Boolean,
    val enableSpeedStreaks: Boolean,
    val enableVolumetricFog: Boolean,
    val fogDensityMultiplier: Float
) {
    companion object {
        fun fromQuality(quality: GraphicsQuality): PerformanceProfile = when (quality) {
            GraphicsQuality.PERFORMANCE -> PerformanceProfile(
                quality = GraphicsQuality.PERFORMANCE,
                maxDrawDistance = 95.0f,
                maxActiveParticles = 64,
                particleBurstScale = 0.35f,
                enableBackgroundScenery = false,
                enableSpeedStreaks = false,
                enableVolumetricFog = false,
                fogDensityMultiplier = 0.75f
            )
            GraphicsQuality.BALANCED -> PerformanceProfile(
                quality = GraphicsQuality.BALANCED,
                maxDrawDistance = 150.0f,
                maxActiveParticles = 128,
                particleBurstScale = 0.65f,
                enableBackgroundScenery = true,
                enableSpeedStreaks = true,
                enableVolumetricFog = true,
                fogDensityMultiplier = 1.0f
            )
            GraphicsQuality.ULTRA -> PerformanceProfile(
                quality = GraphicsQuality.ULTRA,
                maxDrawDistance = 220.0f,
                maxActiveParticles = 256,
                particleBurstScale = 1.0f,
                enableBackgroundScenery = true,
                enableSpeedStreaks = true,
                enableVolumetricFog = true,
                fogDensityMultiplier = 1.0f
            )
        }
    }
}
