package com.example.longrunner.game.world.biomes

import com.example.longrunner.game.core.GameConstants

enum class BiomeType {
    SUBWAY_DEPOT,
    DESERT_CANYON,
    OVERGROWN_RUINS,
    ORBITAL_SKYDECK
}

/**
 * Lighting, fog, and atmospheric color vectors for an environment zone.
 */
data class BiomeAtmosphere(
    val clearR: Float,
    val clearG: Float,
    val clearB: Float,
    val fogR: Float,
    val fogG: Float,
    val fogB: Float,
    val fogDensity: Float,
    val lightDirX: Float,
    val lightDirY: Float,
    val lightDirZ: Float,
    val lightColorR: Float,
    val lightColorG: Float,
    val lightColorB: Float,
    val ambientR: Float,
    val ambientG: Float,
    val ambientB: Float
) {
    fun lerp(target: BiomeAtmosphere, t: Float): BiomeAtmosphere {
        val factor = t.coerceIn(0f, 1f)
        return BiomeAtmosphere(
            clearR = clearR + (target.clearR - clearR) * factor,
            clearG = clearG + (target.clearG - clearG) * factor,
            clearB = clearB + (target.clearB - clearB) * factor,
            fogR = fogR + (target.fogR - fogR) * factor,
            fogG = fogG + (target.fogG - fogG) * factor,
            fogB = fogB + (target.fogB - fogB) * factor,
            fogDensity = fogDensity + (target.fogDensity - fogDensity) * factor,
            lightDirX = lightDirX + (target.lightDirX - lightDirX) * factor,
            lightDirY = lightDirY + (target.lightDirY - lightDirY) * factor,
            lightDirZ = lightDirZ + (target.lightDirZ - lightDirZ) * factor,
            lightColorR = lightColorR + (target.lightColorR - lightColorR) * factor,
            lightColorG = lightColorG + (target.lightColorG - lightColorG) * factor,
            lightColorB = lightColorB + (target.lightColorB - lightColorB) * factor,
            ambientR = ambientR + (target.ambientR - ambientR) * factor,
            ambientG = ambientG + (target.ambientG - ambientG) * factor,
            ambientB = ambientB + (target.ambientB - ambientB) * factor
        )
    }
}

/**
 * Configuration and metadata for a 3D environment biome zone.
 */
data class BiomeData(
    val type: BiomeType,
    val zoneNumber: Int,
    val name: String,
    val subtitle: String,
    val minDistance: Float,
    val maxDistance: Float,
    val accentColorHex: Long,
    val atmosphere: BiomeAtmosphere
) {
    companion object {
        val SUBWAY_DEPOT = BiomeData(
            type = BiomeType.SUBWAY_DEPOT,
            zoneNumber = 1,
            name = "METRO TRANSIT",
            subtitle = "MAG-LEV RAIL DEPOT",
            minDistance = 0f,
            maxDistance = 900f,
            accentColorHex = 0xFF00F0FF, // Cyan & steel blue
            atmosphere = BiomeAtmosphere(
                clearR = 0.03f,
                clearG = 0.04f,
                clearB = 0.08f,
                fogR = 0.04f,
                fogG = 0.06f,
                fogB = 0.10f,
                fogDensity = GameConstants.FOG_DENSITY * 1.0f,
                lightDirX = 0.35f,
                lightDirY = 0.90f,
                lightDirZ = -0.40f,
                lightColorR = 0.95f,
                lightColorG = 0.92f,
                lightColorB = 1.0f,
                ambientR = 0.38f,
                ambientG = 0.40f,
                ambientB = 0.52f
            )
        )

        val DESERT_CANYON = BiomeData(
            type = BiomeType.DESERT_CANYON,
            zoneNumber = 2,
            name = "RUSTFALL CANYON",
            subtitle = "ARID SANDSTONE WASTELAND",
            minDistance = 900f,
            maxDistance = 1800f,
            accentColorHex = 0xFFFF7700, // Sunset amber & warm ochre
            atmosphere = BiomeAtmosphere(
                clearR = 0.20f,
                clearG = 0.08f,
                clearB = 0.04f,
                fogR = 0.24f,
                fogG = 0.11f,
                fogB = 0.05f,
                fogDensity = GameConstants.FOG_DENSITY * 1.15f,
                lightDirX = 0.65f,
                lightDirY = 0.55f,
                lightDirZ = -0.35f, // Low golden desert sun
                lightColorR = 1.0f,
                lightColorG = 0.70f,
                lightColorB = 0.45f,
                ambientR = 0.45f,
                ambientG = 0.28f,
                ambientB = 0.20f
            )
        )

        val OVERGROWN_RUINS = BiomeData(
            type = BiomeType.OVERGROWN_RUINS,
            zoneNumber = 3,
            name = "BIOLUMINESCENT RUINS",
            subtitle = "ANCIENT OVERGROWN SANCTUARY",
            minDistance = 1800f,
            maxDistance = 2800f,
            accentColorHex = 0xFF00FF88, // Emerald green & mystic violet
            atmosphere = BiomeAtmosphere(
                clearR = 0.02f,
                clearG = 0.09f,
                clearB = 0.06f,
                fogR = 0.03f,
                fogG = 0.12f,
                fogB = 0.08f,
                fogDensity = GameConstants.FOG_DENSITY * 1.1f,
                lightDirX = 0.20f,
                lightDirY = 0.95f,
                lightDirZ = -0.30f,
                lightColorR = 0.60f,
                lightColorG = 1.0f,
                lightColorB = 0.75f,
                ambientR = 0.25f,
                ambientG = 0.45f,
                ambientB = 0.35f
            )
        )

        val ORBITAL_SKYDECK = BiomeData(
            type = BiomeType.ORBITAL_SKYDECK,
            zoneNumber = 4,
            name = "ORBITAL SKYDECK",
            subtitle = "STRATOSPHERE SOLAR SPIRE",
            minDistance = 2800f,
            maxDistance = Float.MAX_VALUE,
            accentColorHex = 0xFFFFD700, // Solar Gold & Starfield Indigo
            atmosphere = BiomeAtmosphere(
                clearR = 0.01f,
                clearG = 0.01f,
                clearB = 0.05f,
                fogR = 0.03f,
                fogG = 0.05f,
                fogB = 0.15f,
                fogDensity = GameConstants.FOG_DENSITY * 0.75f, // Thin crystal atmosphere
                lightDirX = 0.10f,
                lightDirY = 0.98f,
                lightDirZ = -0.20f,
                lightColorR = 1.0f,
                lightColorG = 0.95f,
                lightColorB = 0.85f,
                ambientR = 0.40f,
                ambientG = 0.45f,
                ambientB = 0.65f
            )
        )

        val ALL = listOf(SUBWAY_DEPOT, DESERT_CANYON, OVERGROWN_RUINS, ORBITAL_SKYDECK)

        fun getByDistance(distance: Float): BiomeData {
            val d = distance.coerceAtLeast(0f)
            return ALL.find { d >= it.minDistance && d < it.maxDistance } ?: ORBITAL_SKYDECK
        }
    }
}
