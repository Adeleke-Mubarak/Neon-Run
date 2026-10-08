package com.example.longrunner

import com.example.longrunner.game.core.GameEngine
import com.example.longrunner.game.core.GameState
import com.example.longrunner.game.player.CharacterData
import com.example.longrunner.game.powerups.HoverboardData
import com.example.longrunner.game.powerups.HoverboardManager
import com.example.longrunner.game.powerups.HoverboardRarity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HoverboardSystemTest {

    private lateinit var engine: GameEngine
    private lateinit var hoverboardManager: HoverboardManager

    @Before
    fun setUp() {
        engine = GameEngine(null)
        hoverboardManager = HoverboardManager(null)
        hoverboardManager.resetAll()
    }

    @Test
    fun testHoverboardsOrderedLowestToHighestValue() {
        val boards = HoverboardData.ALL
        assertEquals(5, boards.size)

        // Verify sorted strictly in ascending order by costCredits
        for (i in 0 until boards.size - 1) {
            assertTrue(
                "Board ${boards[i].name} (${boards[i].costCredits} CR) must cost less than or equal to ${boards[i+1].name} (${boards[i+1].costCredits} CR)",
                boards[i].costCredits <= boards[i + 1].costCredits
            )
        }

        // Check specific tier progression
        assertEquals(HoverboardData.VECTOR_GLIDE, boards[0])
        assertEquals(0, boards[0].costCredits)
        assertEquals(HoverboardRarity.CORE, boards[0].rarity)

        assertEquals(HoverboardData.PULSE_STRIKER, boards[1])
        assertEquals(1500, boards[1].costCredits)

        assertEquals(HoverboardData.VORTEX_MIRAGE, boards[2])
        assertEquals(4000, boards[2].costCredits)

        assertEquals(HoverboardData.APEX_PHANTOM, boards[3])
        assertEquals(8500, boards[3].costCredits)

        assertEquals(HoverboardData.HYPERION_PRIME, boards[4])
        assertEquals(16000, boards[4].costCredits)
        assertEquals(HoverboardRarity.LEGENDARY, boards[4].rarity)
    }

    @Test
    fun testDefaultEquippedHoverboardIsFreeStarter() {
        val equipped = hoverboardManager.getEquippedHoverboard()
        assertEquals(HoverboardData.VECTOR_GLIDE.id, equipped.id)
        assertTrue(hoverboardManager.isUnlocked(HoverboardData.VECTOR_GLIDE.id))
        assertEquals(0, equipped.costCredits)
    }

    @Test
    fun testHoverboardUnlockAndEquipWithCredits() {
        val cm = engine.characterManager
        cm.resetAll()

        // Attempting to unlock Hyperion Prime with 0 credits fails
        assertFalse(hoverboardManager.isUnlocked(HoverboardData.HYPERION_PRIME.id))
        val failed = hoverboardManager.unlockWithCredits(HoverboardData.HYPERION_PRIME.id, cm)
        assertFalse(failed)
        assertFalse(hoverboardManager.isUnlocked(HoverboardData.HYPERION_PRIME.id))

        // Grant 20,000 bank credits
        cm.addBankCredits(20000)
        assertEquals(20000, cm.totalBankCredits)

        // Unlock Hyperion Prime
        val success = hoverboardManager.unlockWithCredits(HoverboardData.HYPERION_PRIME.id, cm)
        assertTrue(success)
        assertTrue(hoverboardManager.isUnlocked(HoverboardData.HYPERION_PRIME.id))
        assertEquals(HoverboardData.HYPERION_PRIME.id, hoverboardManager.getEquippedHoverboard().id)
        // Bank balance reduced: 20000 - 16000 = 4000
        assertEquals(4000, cm.totalBankCredits)

        // Equipping previously unlocked starter works
        assertTrue(hoverboardManager.equipHoverboard(HoverboardData.VECTOR_GLIDE.id))
        assertEquals(HoverboardData.VECTOR_GLIDE.id, hoverboardManager.getEquippedHoverboard().id)

        // Re-equipping Hyperion Prime works without spending credits
        assertTrue(hoverboardManager.equipHoverboard(HoverboardData.HYPERION_PRIME.id))
        assertEquals(HoverboardData.HYPERION_PRIME.id, hoverboardManager.getEquippedHoverboard().id)
        assertEquals(4000, cm.totalBankCredits)
    }

    @Test
    fun testAllCharactersCanActivateHoverboard() {
        val characters = listOf(
            CharacterData.KAI,
            CharacterData.ZARA,
            CharacterData.JAX,
            CharacterData.NOVA,
            CharacterData.MIRA
        )

        for (charData in characters) {
            engine.reset()
            engine.characterManager.unlockCharacter(charData.id)
            engine.characterManager.selectCharacter(charData.id)
            engine.applyCharacterConfiguration(charData)
            engine.startRun()
            assertEquals(GameState.RUNNING, engine.state)
            assertEquals(charData.id, engine.player.characterData.id)

            // Double tap / activate hoverboard
            assertFalse(engine.powerUpManager.isHoverboardActive)
            val activated = engine.activateHoverboard()
            assertTrue("Character ${charData.name} must be able to activate hoverboard", activated)
            assertTrue(engine.powerUpManager.isHoverboardActive)

            // Breaking the board via obstacle collision
            engine.powerUpManager.absorbHoverboardCollision()
            assertFalse(engine.powerUpManager.isHoverboardActive)

            // Re-activating hoverboard works without artificial charge depletion!
            val reActivated = engine.activateHoverboard()
            assertTrue("Character ${charData.name} must be able to deploy hoverboard again after break", reActivated)
            assertTrue(engine.powerUpManager.isHoverboardActive)
        }
    }

    @Test
    fun testEquippedHoverboardPerksApplied() {
        engine.reset()
        engine.startRun()

        // Equip Hyperion Prime
        engine.hoverboardManager.unlockBoard(HoverboardData.HYPERION_PRIME.id)
        engine.hoverboardManager.equipHoverboard(HoverboardData.HYPERION_PRIME.id)
        assertEquals(HoverboardData.HYPERION_PRIME.id, engine.hoverboardManager.getEquippedHoverboard().id)

        // Activate hoverboard
        engine.activateHoverboard()
        assertTrue(engine.powerUpManager.isHoverboardActive)

        // Hyperion Prime duration: 22s
        assertEquals(22.0f, engine.powerUpManager.getRemainingTime(com.example.longrunner.game.powerups.PowerUpType.HOVERBOARD), 0.1f)

        // Hyperion Prime magnet bonus: 12m
        assertEquals(12.0f, engine.hoverboardManager.getEquippedHoverboard().magnetRadiusBonus, 0.01f)

        // Hyperion Prime score bonus: 2.0x
        assertEquals(2.0f, engine.hoverboardManager.getEquippedHoverboard().scoreMultiplierBonus, 0.01f)
    }

    @Test
    fun testTrackGeneratorMagnetSpawnReduced() {
        val tg = engine.trackGenerator
        tg.reset()

        var magnetCount = 0
        var shardCount = 0
        val sampleSize = 200

        for (seg in 0 until sampleSize) {
            tg.update(engine.player, 0.1f)
            for (segment in tg.segments) {
                for (col in segment.collectibles) {
                    if (col.isActive) {
                        if (col.type == com.example.longrunner.game.collectibles.CollectibleType.MAGNET_ORB) {
                            magnetCount++
                        } else if (col.type == com.example.longrunner.game.collectibles.CollectibleType.ENERGY_SHARD) {
                            shardCount++
                        }
                    }
                }
            }
        }

        // Shards should drastically outnumber magnets (>15x)
        assertTrue("Energy shards ($shardCount) should vastly outnumber rare magnets ($magnetCount)", shardCount > magnetCount * 5)
    }
}
