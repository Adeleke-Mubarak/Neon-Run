package com.example.longrunner

import com.example.longrunner.game.core.GameEngine
import com.example.longrunner.game.core.GameState
import com.example.longrunner.game.world.biomes.BiomeData
import com.example.longrunner.game.world.biomes.BiomeManager
import com.example.longrunner.game.world.biomes.BiomeType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class BiomeSystemTest {

    private lateinit var biomeManager: BiomeManager
    private lateinit var engine: GameEngine

    @Before
    fun setUp() {
        biomeManager = BiomeManager()
        engine = GameEngine(null)
    }

    @Test
    fun testAllBiomesConfiguredAndOrdered() {
        val biomes = BiomeData.ALL
        assertEquals(4, biomes.size)

        // Zone 1: Metro Transit & Rail Depot
        assertEquals(BiomeType.SUBWAY_DEPOT, biomes[0].type)
        assertEquals(1, biomes[0].zoneNumber)
        assertEquals("METRO TRANSIT", biomes[0].name)
        assertEquals(0f, biomes[0].minDistance, 0.01f)
        assertEquals(900f, biomes[0].maxDistance, 0.01f)

        // Zone 2: Rustfall Desert Canyon
        assertEquals(BiomeType.DESERT_CANYON, biomes[1].type)
        assertEquals(2, biomes[1].zoneNumber)
        assertEquals("RUSTFALL CANYON", biomes[1].name)
        assertEquals(900f, biomes[1].minDistance, 0.01f)
        assertEquals(1800f, biomes[1].maxDistance, 0.01f)

        // Zone 3: Bioluminescent Jungle Ruins
        assertEquals(BiomeType.OVERGROWN_RUINS, biomes[2].type)
        assertEquals(3, biomes[2].zoneNumber)
        assertEquals("BIOLUMINESCENT RUINS", biomes[2].name)
        assertEquals(1800f, biomes[2].minDistance, 0.01f)
        assertEquals(2800f, biomes[2].maxDistance, 0.01f)

        // Zone 4: Orbital Skydeck
        assertEquals(BiomeType.ORBITAL_SKYDECK, biomes[3].type)
        assertEquals(4, biomes[3].zoneNumber)
        assertEquals(2800f, biomes[3].minDistance, 0.01f)
    }

    @Test
    fun testDistanceToBiomeMapping() {
        assertEquals(BiomeType.SUBWAY_DEPOT, BiomeData.getByDistance(0f).type)
        assertEquals(BiomeType.SUBWAY_DEPOT, BiomeData.getByDistance(450f).type)
        assertEquals(BiomeType.SUBWAY_DEPOT, BiomeData.getByDistance(899.9f).type)

        assertEquals(BiomeType.DESERT_CANYON, BiomeData.getByDistance(900f).type)
        assertEquals(BiomeType.DESERT_CANYON, BiomeData.getByDistance(1350f).type)
        assertEquals(BiomeType.DESERT_CANYON, BiomeData.getByDistance(1799.9f).type)

        assertEquals(BiomeType.OVERGROWN_RUINS, BiomeData.getByDistance(1800f).type)
        assertEquals(BiomeType.OVERGROWN_RUINS, BiomeData.getByDistance(2300f).type)
        assertEquals(BiomeType.OVERGROWN_RUINS, BiomeData.getByDistance(2799.9f).type)

        assertEquals(BiomeType.ORBITAL_SKYDECK, BiomeData.getByDistance(2800f).type)
        assertEquals(BiomeType.ORBITAL_SKYDECK, BiomeData.getByDistance(5000f).type)
    }

    @Test
    fun testAtmosphereInterpolation() {
        // Deep inside Zone 1 (Metro): pure Zone 1 fog & clear color
        val subwayAtmosphere = BiomeData.SUBWAY_DEPOT.atmosphere
        val at400 = biomeManager.getInterpolatedAtmosphere(400f)
        assertEquals(subwayAtmosphere.clearR, at400.clearR, 0.001f)
        assertEquals(subwayAtmosphere.clearG, at400.clearG, 0.001f)
        assertEquals(subwayAtmosphere.clearB, at400.clearB, 0.001f)
        assertEquals(subwayAtmosphere.fogDensity, at400.fogDensity, 0.001f)

        // Boundary midpoint at 900m: exact 50% blend between Subway and Desert Canyon
        val canyonAtmosphere = BiomeData.DESERT_CANYON.atmosphere
        val at900 = biomeManager.getInterpolatedAtmosphere(900f)
        val expectedMidClearR = (subwayAtmosphere.clearR + canyonAtmosphere.clearR) * 0.5f
        val expectedMidFogDensity = (subwayAtmosphere.fogDensity + canyonAtmosphere.fogDensity) * 0.5f
        assertEquals(expectedMidClearR, at900.clearR, 0.01f)
        assertEquals(expectedMidFogDensity, at900.fogDensity, 0.01f)

        // Deep inside Zone 2 (Desert): pure Desert Canyon atmosphere
        val at1350 = biomeManager.getInterpolatedAtmosphere(1350f)
        assertEquals(canyonAtmosphere.clearR, at1350.clearR, 0.001f)
        assertEquals(canyonAtmosphere.fogR, at1350.fogR, 0.001f)
    }

    @Test
    fun testTransitionGatewayDetection() {
        // Within 35m of 900m boundary:
        assertTrue(biomeManager.isTransitionGateway(900f))
        assertTrue(biomeManager.isTransitionGateway(880f))
        assertTrue(biomeManager.isTransitionGateway(920f))

        // Outside boundary transition zone:
        assertFalse(biomeManager.isTransitionGateway(500f))
        assertFalse(biomeManager.isTransitionGateway(850f))
        assertFalse(biomeManager.isTransitionGateway(950f))

        // Within 35m of 1800m boundary:
        assertTrue(biomeManager.isTransitionGateway(1800f))
        assertFalse(biomeManager.isTransitionGateway(1500f))

        // Within 35m of 2800m boundary:
        assertTrue(biomeManager.isTransitionGateway(2800f))
    }

    @Test
    fun testBiomeManagerBannerTriggersOnZoneEntry() {
        // Start in Zone 1
        biomeManager.update(0f, 0.1f)
        assertEquals("METRO TRANSIT", biomeManager.currentBiomeName)
        assertEquals("", biomeManager.biomeBannerMessage)

        // Cross into Zone 2 (Rustfall Canyon)
        biomeManager.update(920f, 0.1f)
        assertEquals("RUSTFALL CANYON", biomeManager.currentBiomeName)
        assertTrue(biomeManager.biomeBannerMessage.contains("ZONE 2: RUSTFALL CANYON"))
        assertTrue(biomeManager.biomeBannerSubtitle.isNotEmpty())
        assertTrue(biomeManager.biomeBannerTimer > 0f)

        // Banner decays after timer runs down
        biomeManager.update(930f, 4.5f)
        assertEquals(0f, biomeManager.biomeBannerTimer, 0.001f)
        assertEquals("", biomeManager.biomeBannerMessage)
    }

    @Test
    fun testTrackGeneratorAssignsBiomesToSegments() {
        engine.trackGenerator.reset()

        // First segment should be in Zone 1 (Metro Transit)
        val firstSegment = engine.trackGenerator.segments.first()
        assertEquals(BiomeType.SUBWAY_DEPOT, firstSegment.biomeType)

        // Recycle segments until track extends past 900m into Zone 2
        engine.player.teleport(0f, 0f, -1400f)
        repeat(6) {
            engine.trackGenerator.update(engine.player, 0.1f)
        }

        // At least one segment should now be assigned to Zone 2 (Desert Canyon)
        val hasDesertSegment = engine.trackGenerator.segments.any { it.biomeType == BiomeType.DESERT_CANYON }
        assertTrue("Track generator should generate desert canyon segments past 900m", hasDesertSegment)
    }

    @Test
    fun testGameEngineStatsReflectActiveBiome() {
        engine.reset()
        var observedZone = 0
        var observedName = ""

        engine.onStatsUpdated = { stats ->
            observedZone = stats.currentBiomeZone
            observedName = stats.currentBiomeName
        }

        engine.notifyStats()
        assertEquals(1, observedZone)
        assertEquals("METRO TRANSIT", observedName)

        // Advance distance into Zone 2
        engine.scoreManager.addBonusScore(100L)
        // Simulate distance update in engine
        engine.biomeManager.update(950f, 0.1f)
        engine.notifyStats()

        assertEquals(2, observedZone)
        assertEquals("RUSTFALL CANYON", observedName)

        // Engine reset resets back to Zone 1
        engine.reset()
        assertEquals(1, engine.biomeManager.getActiveBiome().zoneNumber)
        assertEquals("METRO TRANSIT", engine.biomeManager.currentBiomeName)
    }
}
