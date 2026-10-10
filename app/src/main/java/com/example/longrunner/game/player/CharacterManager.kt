package com.example.longrunner.game.player

import android.content.Context
import android.content.SharedPreferences

/**
 * Character Roster and Progression Manager.
 * Manages player character selection, persistent unlock tracking,
 * bank credit purchases, and milestone-based character grants.
 */
class CharacterManager(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("neon_run_characters", Context.MODE_PRIVATE)
    private val bankPrefs: SharedPreferences? = context?.getSharedPreferences("neon_run_save", Context.MODE_PRIVATE)

    var selectedCharacterId: String = prefs?.getString("selected_character_id", CharacterData.JAKE.id) ?: CharacterData.JAKE.id
        private set

    private val unlockedCharacterIds = HashSet<String>().apply {
        add(CharacterData.KAI.id)
        add(CharacterData.JAKE.id)
        prefs?.getStringSet("unlocked_character_ids", null)?.let {
            addAll(it)
        }
    }

    var totalBankCredits: Int = bankPrefs?.getInt("bank_credits", 0) ?: 0
        private set

    var onCharacterChanged: ((CharacterData) -> Unit)? = null

    fun getActiveCharacter(): CharacterData {
        return CharacterData.findById(selectedCharacterId)
    }

    fun isUnlocked(characterId: String): Boolean {
        if (characterId == CharacterData.KAI.id || characterId == CharacterData.JAKE.id) return true
        return unlockedCharacterIds.contains(characterId)
    }

    fun selectCharacter(characterId: String): Boolean {
        if (!isUnlocked(characterId)) return false

        selectedCharacterId = characterId
        prefs?.edit()?.putString("selected_character_id", characterId)?.apply()
        onCharacterChanged?.invoke(getActiveCharacter())
        return true
    }

    fun unlockCharacter(characterId: String): Boolean {
        val character = CharacterData.findById(characterId)
        unlockedCharacterIds.add(character.id)
        prefs?.edit()?.putStringSet("unlocked_character_ids", unlockedCharacterIds)?.apply()
        return true
    }

    fun unlockWithCredits(characterId: String): Boolean {
        val character = CharacterData.findById(characterId)
        if (isUnlocked(character.id)) return true
        if (totalBankCredits < character.unlockCostCredits) return false

        totalBankCredits -= character.unlockCostCredits
        bankPrefs?.edit()?.putInt("bank_credits", totalBankCredits)?.apply()
        unlockCharacter(character.id)
        return true
    }

    fun addBankCredits(credits: Int) {
        if (credits <= 0) return
        totalBankCredits += credits
        bankPrefs?.edit()?.putInt("bank_credits", totalBankCredits)?.apply()
    }

    fun deductBankCredits(credits: Int): Boolean {
        if (credits <= 0) return false
        if (totalBankCredits >= credits) {
            totalBankCredits -= credits
            bankPrefs?.edit()?.putInt("bank_credits", totalBankCredits)?.apply()
            return true
        }
        return false
    }

    /**
     * Checks gameplay milestone criteria for alternative character unlocks.
     */
    fun checkMilestoneUnlocks(bestDistance: Float, maxCombo: Int, smashedCrates: Int = 0): List<CharacterData> {
        val newlyUnlocked = mutableListOf<CharacterData>()

        // Zara unlock milestone: 1000m single run
        if (bestDistance >= 1000.0f && !isUnlocked(CharacterData.ZARA.id)) {
            unlockCharacter(CharacterData.ZARA.id)
            newlyUnlocked.add(CharacterData.ZARA)
        }

        // Jax unlock milestone: smash 10 crates
        if (smashedCrates >= 10 && !isUnlocked(CharacterData.JAX.id)) {
            unlockCharacter(CharacterData.JAX.id)
            newlyUnlocked.add(CharacterData.JAX)
        }

        // Mira unlock milestone: 10x combo achieved
        if (maxCombo >= 10 && !isUnlocked(CharacterData.MIRA.id)) {
            unlockCharacter(CharacterData.MIRA.id)
            newlyUnlocked.add(CharacterData.MIRA)
        }

        return newlyUnlocked
    }

    fun resetAll() {
        unlockedCharacterIds.clear()
        unlockedCharacterIds.add(CharacterData.KAI.id)
        selectedCharacterId = CharacterData.KAI.id
        totalBankCredits = 0
        prefs?.edit()?.clear()?.apply()
        bankPrefs?.edit()?.clear()?.apply()
        onCharacterChanged?.invoke(getActiveCharacter())
    }
}
