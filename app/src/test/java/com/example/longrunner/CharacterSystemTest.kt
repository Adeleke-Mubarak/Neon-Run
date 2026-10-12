package com.example.longrunner

import com.example.longrunner.game.core.GameConstants
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
        assertEquals(1, CharacterData.ALL.size)

        val runner = CharacterData.RUNNER
        assertEquals("runner", runner.id)
        assertEquals("Runner", runner.name)
        assertEquals(1.0f, runner.speedModifier, 0.001f)
        assertEquals(1.0f, runner.jumpModifier, 0.001f)
        assertEquals(1.0f, runner.slideModifier, 0.001f)
        assertEquals(SilhouetteType.BALANCED, runner.silhouetteType)
        assertEquals(0, runner.unlockCostCredits)
        assertEquals("models/runner3.glb", runner.modelAssetPath)
    }

    @Test
    fun testCharacterManagerSelectionAndPersistence() {
        // Runner is selected by default and unlocked
        assertEquals(CharacterData.RUNNER.id, characterManager.selectedCharacterId)
        assertTrue(characterManager.isUnlocked(CharacterData.RUNNER.id))

        // Callback fires when character is selected
        var callbackFired = false
        characterManager.onCharacterChanged = { char ->
            assertEquals(CharacterData.RUNNER.id, char.id)
            callbackFired = true
        }
        val selectSuccess = characterManager.selectCharacter(CharacterData.RUNNER.id)
        assertTrue("Runner should be selectable", selectSuccess)
        assertTrue(callbackFired)
        assertEquals(CharacterData.RUNNER.id, characterManager.selectedCharacterId)
    }

    @Test
    fun testPlayerControllerAttributesAdaptToCharacter() {
        player.setCharacter(CharacterData.RUNNER)
        assertEquals(GameConstants.BASE_SPEED * CharacterData.RUNNER.speedModifier, player.speed, 0.001f)
        assertEquals(CharacterData.RUNNER.collisionTolerance, player.collisionTolerance, 0.001f)

        // Lane movement functions smoothly
        player.moveLeft()
        val initialX = player.x
        player.update(0.05f)
        val step = Math.abs(player.x - initialX)
        assertTrue("Runner lane change should progress", step > 0f)
    }

    @Test
    fun testAbilityActivationAndCooldownFlow() {
        player.setCharacter(CharacterData.RUNNER)
        assertTrue(player.isAbilityReady)
        assertFalse(player.isAbilityActive)

        // Trigger ability
        val activated = player.activateAbility()
        assertTrue(activated)
        assertTrue(player.isAbilityActive)
        assertFalse(player.isAbilityReady)
        assertEquals(CharacterData.RUNNER.abilityCooldown, player.abilityCooldownTimer, 0.001f)
        assertEquals(CharacterData.RUNNER.abilityDuration, player.abilityDurationTimer, 0.001f)

        // Cannot activate while on cooldown
        assertFalse("Cannot activate ability while cooling down", player.activateAbility())

        // Advance past duration but within cooldown
        player.update(CharacterData.RUNNER.abilityDuration + 0.1f)
        assertFalse(player.isAbilityActive)
        assertFalse(player.isAbilityReady)

        // Advance to full cooldown expiry
        player.update(CharacterData.RUNNER.abilityCooldown)
        assertTrue(player.isAbilityReady)
    }
}
