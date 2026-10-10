package com.example.longrunner

import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.obstacles.ObstacleType
import com.example.longrunner.game.player.CharacterData
import com.example.longrunner.game.player.CharacterManager
import com.example.longrunner.game.player.PlayerController
import com.example.longrunner.game.player.SilhouetteType
import com.example.longrunner.game.score.ComboManager
import com.example.longrunner.game.track.TrackGenerator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CharacterSystemTest {

    private lateinit var characterManager: CharacterManager
    private lateinit var player: PlayerController
    private lateinit var comboManager: ComboManager
    private lateinit var trackGenerator: TrackGenerator

    @Before
    fun setUp() {
        characterManager = CharacterManager(null)
        characterManager.resetAll()
        player = PlayerController()
        player.reset()
        comboManager = ComboManager()
        comboManager.reset()
        trackGenerator = TrackGenerator()
        trackGenerator.reset()
    }

    @Test
    fun testRosterDataCompletenessAndUniqueness() {
        assertEquals(6, CharacterData.ALL.size)

        val ids = CharacterData.ALL.map { it.id }.toSet()
        assertEquals(6, ids.size)

        // Verify Kai
        val kai = CharacterData.KAI
        assertEquals("Kai", kai.name)
        assertEquals(1.0f, kai.speedModifier, 0.001f)
        assertEquals(SilhouetteType.BALANCED, kai.silhouetteType)
        assertEquals(0, kai.unlockCostCredits)
        assertEquals("models/runner.glb", kai.modelAssetPath)

        // Verify Jake
        val jake = CharacterData.JAKE
        assertEquals("Jake", jake.name)
        assertEquals("models/character_endless_runner.glb", jake.modelAssetPath)
        assertEquals(0, jake.unlockCostCredits)

        // Verify Zara
        val zara = CharacterData.ZARA
        assertEquals("Zara", zara.name)
        assertTrue("Zara should be faster than baseline", zara.speedModifier > 1.0f)
        assertTrue("Zara should jump higher", zara.jumpModifier > 1.0f)
        assertTrue("Zara should have faster lane switch", zara.laneSwitchModifier > 1.0f)
        assertEquals(SilhouetteType.AERODYNAMIC, zara.silhouetteType)
        assertEquals(5000, zara.unlockCostCredits)
        assertEquals("models/runner2.glb", zara.modelAssetPath)

        // Verify Jax
        val jax = CharacterData.JAX
        assertEquals("Jax", jax.name)
        assertTrue("Jax should be heavier / slower", jax.speedModifier < 1.0f)
        assertTrue("Jax should have larger collision tolerance", jax.collisionTolerance > 0.08f)
        assertEquals(SilhouetteType.HEAVY, jax.silhouetteType)
        assertEquals(10000, jax.unlockCostCredits)

        // Verify Nova
        val nova = CharacterData.NOVA
        assertEquals("Nova", nova.name)
        assertTrue("Nova should have high phase affinity", nova.phaseModifier >= 1.5f)
        assertEquals(SilhouetteType.MYSTIC, nova.silhouetteType)
        assertEquals(15000, nova.unlockCostCredits)

        // Verify Mira
        val mira = CharacterData.MIRA
        assertEquals("Mira", mira.name)
        assertTrue("Mira should have high slide agility", mira.slideModifier >= 1.2f)
        assertEquals(SilhouetteType.ACROBATIC, mira.silhouetteType)
        assertEquals(20000, mira.unlockCostCredits)
    }

    @Test
    fun testCharacterManagerSelectionAndPersistence() {
        // Kai is selected by default and unlocked
        assertEquals(CharacterData.KAI.id, characterManager.selectedCharacterId)
        assertTrue(characterManager.isUnlocked(CharacterData.KAI.id))

        // Locked characters cannot be selected
        assertFalse(characterManager.isUnlocked(CharacterData.ZARA.id))
        val selectLocked = characterManager.selectCharacter(CharacterData.ZARA.id)
        assertFalse("Cannot select locked character", selectLocked)
        assertEquals(CharacterData.KAI.id, characterManager.selectedCharacterId)

        // Unlock with credits
        characterManager.addBankCredits(6000)
        assertEquals(6000, characterManager.totalBankCredits)

        val unlockSuccess = characterManager.unlockWithCredits(CharacterData.ZARA.id)
        assertTrue("Zara should unlock with sufficient credits", unlockSuccess)
        assertTrue(characterManager.isUnlocked(CharacterData.ZARA.id))
        assertEquals(1000, characterManager.totalBankCredits) // 6000 - 5000 = 1000

        // Now selection succeeds
        var callbackFired = false
        characterManager.onCharacterChanged = { char ->
            assertEquals(CharacterData.ZARA.id, char.id)
            callbackFired = true
        }
        val selectSuccess = characterManager.selectCharacter(CharacterData.ZARA.id)
        assertTrue("Zara should now be selectable", selectSuccess)
        assertTrue(callbackFired)
        assertEquals(CharacterData.ZARA.id, characterManager.selectedCharacterId)
    }

    @Test
    fun testMilestoneAutoUnlocks() {
        // Milestone 1: 1000m run unlocks Zara
        assertFalse(characterManager.isUnlocked(CharacterData.ZARA.id))
        val unlockedDist = characterManager.checkMilestoneUnlocks(bestDistance = 1200f, maxCombo = 3, smashedCrates = 0)
        assertTrue(characterManager.isUnlocked(CharacterData.ZARA.id))
        assertTrue(unlockedDist.any { it.id == CharacterData.ZARA.id })

        // Milestone 2: Smashing 10 crates unlocks Jax
        assertFalse(characterManager.isUnlocked(CharacterData.JAX.id))
        val unlockedCrates = characterManager.checkMilestoneUnlocks(bestDistance = 500f, maxCombo = 3, smashedCrates = 12)
        assertTrue(characterManager.isUnlocked(CharacterData.JAX.id))
        assertTrue(unlockedCrates.any { it.id == CharacterData.JAX.id })

        // Milestone 3: 10x combo unlocks Mira
        assertFalse(characterManager.isUnlocked(CharacterData.MIRA.id))
        val unlockedCombo = characterManager.checkMilestoneUnlocks(bestDistance = 500f, maxCombo = 10, smashedCrates = 0)
        assertTrue(characterManager.isUnlocked(CharacterData.MIRA.id))
        assertTrue(unlockedCombo.any { it.id == CharacterData.MIRA.id })
    }

    @Test
    fun testPlayerControllerAttributesAdaptToCharacter() {
        // Test Zara speed & lane switch responsiveness
        player.setCharacter(CharacterData.ZARA)
        assertEquals(GameConstants.BASE_SPEED * CharacterData.ZARA.speedModifier, player.speed, 0.001f)
        assertEquals(CharacterData.ZARA.collisionTolerance, player.collisionTolerance, 0.001f)

        // Move left with Zara's 1.30x lane switch modifier
        player.moveLeft()
        val initialX = player.x
        player.update(0.05f)
        val zaraStep = Math.abs(player.x - initialX)

        // Compare with Jax's 0.90x lane switch step
        player.reset()
        player.setCharacter(CharacterData.JAX)
        player.moveLeft()
        val initialJaxX = player.x
        player.update(0.05f)
        val jaxStep = Math.abs(player.x - initialJaxX)

        assertTrue("Zara's lane change should be faster than Jax's", zaraStep > jaxStep)
    }

    @Test
    fun testAbilityActivationAndCooldownFlow() {
        player.setCharacter(CharacterData.KAI)
        assertTrue(player.isAbilityReady)
        assertFalse(player.isAbilityActive)

        // Trigger ability
        val activated = player.activateAbility()
        assertTrue(activated)
        assertTrue(player.isAbilityActive)
        assertFalse(player.isAbilityReady)
        assertEquals(CharacterData.KAI.abilityCooldown, player.abilityCooldownTimer, 0.001f)
        assertEquals(CharacterData.KAI.abilityDuration, player.abilityDurationTimer, 0.001f)

        // Cannot activate while on cooldown
        assertFalse("Cannot activate ability while cooling down", player.activateAbility())

        // Advance past duration but within cooldown
        player.update(CharacterData.KAI.abilityDuration + 0.1f)
        assertFalse(player.isAbilityActive)
        assertFalse(player.isAbilityReady)

        // Advance to full cooldown expiry
        player.update(CharacterData.KAI.abilityCooldown)
        assertTrue(player.isAbilityReady)
    }

    @Test
    fun testZaraGhostStepMechanic() {
        player.setCharacter(CharacterData.ZARA)
        assertFalse(player.hasGhostStepCharge)

        player.activateAbility()
        assertTrue("Zara should have Ghost Step charge active", player.hasGhostStepCharge)

        // Consuming charge
        player.hasGhostStepCharge = false
        assertFalse(player.hasGhostStepCharge)
    }
}
