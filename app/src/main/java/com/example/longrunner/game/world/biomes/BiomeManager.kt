package com.example.longrunner.game.world.biomes

/**
 * Manages dynamic 3D environment transitions, real-time atmospheric blending (fog & lighting),
 * transition gateway detection, and on-screen zone announcement banners.
 */
class BiomeManager {

    private var activeBiome: BiomeData = BiomeData.SUBWAY_DEPOT
    private var lastAnnouncedZone: Int = 1

    var currentBiomeName: String = activeBiome.name
        private set

    var biomeBannerMessage: String = ""
        private set
    var biomeBannerSubtitle: String = ""
        private set
    var biomeBannerTimer: Float = 0f
        private set
    var biomeAccentColorHex: Long = activeBiome.accentColorHex
        private set

    var onBiomeChanged: ((BiomeData) -> Unit)? = null

    fun reset() {
        activeBiome = BiomeData.SUBWAY_DEPOT
        lastAnnouncedZone = 1
        currentBiomeName = activeBiome.name
        biomeBannerMessage = ""
        biomeBannerSubtitle = ""
        biomeBannerTimer = 0f
        biomeAccentColorHex = activeBiome.accentColorHex
    }

    fun getActiveBiome(): BiomeData = activeBiome

    fun getBiomeAtDistance(distance: Float): BiomeData = BiomeData.getByDistance(distance)

    fun update(distance: Float, dt: Float) {
        val current = BiomeData.getByDistance(distance)
        if (current.zoneNumber != activeBiome.zoneNumber) {
            activeBiome = current
            currentBiomeName = current.name
            biomeAccentColorHex = current.accentColorHex
            onBiomeChanged?.invoke(current)

            if (current.zoneNumber > lastAnnouncedZone) {
                lastAnnouncedZone = current.zoneNumber
                triggerBanner("ZONE ${current.zoneNumber}: ${current.name}", current.subtitle)
            }
        }

        if (biomeBannerTimer > 0f) {
            biomeBannerTimer -= dt
            if (biomeBannerTimer <= 0f) {
                biomeBannerTimer = 0f
                biomeBannerMessage = ""
                biomeBannerSubtitle = ""
            }
        }
    }

    fun triggerBanner(title: String, subtitle: String) {
        biomeBannerMessage = title
        biomeBannerSubtitle = subtitle
        biomeBannerTimer = 4.0f
    }

    /**
     * Determines whether a given segment distance falls within a transition gateway zone.
     * Transition gateways span 40m before and after each boundary (e.g. 880m-920m, 1780m-1820m, 2780m-2820m).
     */
    fun isTransitionGateway(distance: Float): Boolean {
        val boundaries = floatArrayOf(900f, 1800f, 2800f)
        for (b in boundaries) {
            if (distance in (b - 35f)..(b + 35f)) {
                return true
            }
        }
        return false
    }

    /**
     * Calculates smoothly blended atmospheric vectors (fog, lighting, sky clear color)
     * during transition zones so the world morphs without abrupt pop-in.
     */
    fun getInterpolatedAtmosphere(distance: Float): BiomeAtmosphere {
        val boundaries = floatArrayOf(900f, 1800f, 2800f)
        val transitionHalfWidth = 45.0f

        for (i in boundaries.indices) {
            val b = boundaries[i]
            if (distance in (b - transitionHalfWidth)..(b + transitionHalfWidth)) {
                val fromBiome = BiomeData.ALL[i]
                val toBiome = BiomeData.ALL[i + 1]
                val progress = ((distance - (b - transitionHalfWidth)) / (transitionHalfWidth * 2f)).coerceIn(0f, 1f)
                return fromBiome.atmosphere.lerp(toBiome.atmosphere, progress)
            }
        }

        return BiomeData.getByDistance(distance).atmosphere
    }
}
