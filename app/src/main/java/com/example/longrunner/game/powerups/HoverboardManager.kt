package com.example.longrunner.game.powerups

import android.content.Context
import android.content.SharedPreferences
import com.example.longrunner.game.player.CharacterManager

/**
 * Cyber Vault Hoverboard Manager.
 * Handles unlocking, selection, credit purchases, and equipped board retrieval
 * across all characters and gameplay sessions.
 */
class HoverboardManager(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("neon_run_hoverboards", Context.MODE_PRIVATE)

    var selectedHoverboardId: String = prefs?.getString("selected_hoverboard_id", HoverboardData.VECTOR_GLIDE.id) ?: HoverboardData.VECTOR_GLIDE.id
        private set

    private val unlockedBoardIds = HashSet<String>().apply {
        add(HoverboardData.VECTOR_GLIDE.id) // Vector Glide is the free starter deck (0 Credits)
        prefs?.getStringSet("unlocked_hoverboard_ids", null)?.let {
            addAll(it)
        }
    }

    var onHoverboardChanged: ((HoverboardData) -> Unit)? = null

    fun getEquippedHoverboard(): HoverboardData {
        return HoverboardData.findById(selectedHoverboardId)
    }

    fun isUnlocked(boardId: String): Boolean {
        if (boardId == HoverboardData.VECTOR_GLIDE.id) return true
        return unlockedBoardIds.contains(boardId)
    }

    fun equipHoverboard(boardId: String): Boolean {
        if (!isUnlocked(boardId)) return false
        selectedHoverboardId = boardId
        prefs?.edit()?.putString("selected_hoverboard_id", boardId)?.apply()
        onHoverboardChanged?.invoke(getEquippedHoverboard())
        return true
    }

    fun unlockBoard(boardId: String): Boolean {
        val board = HoverboardData.findById(boardId)
        unlockedBoardIds.add(board.id)
        prefs?.edit()?.putStringSet("unlocked_hoverboard_ids", unlockedBoardIds)?.apply()
        return true
    }

    fun unlockWithCredits(boardId: String, characterManager: CharacterManager): Boolean {
        val board = HoverboardData.findById(boardId)
        if (isUnlocked(board.id)) {
            equipHoverboard(board.id)
            return true
        }
        if (characterManager.totalBankCredits < board.costCredits) return false

        if (characterManager.deductBankCredits(board.costCredits)) {
            unlockBoard(board.id)
            equipHoverboard(board.id)
            return true
        }
        return false
    }

    fun getAllBoards(): List<HoverboardData> {
        return HoverboardData.ALL
    }

    fun resetAll() {
        unlockedBoardIds.clear()
        unlockedBoardIds.add(HoverboardData.VECTOR_GLIDE.id)
        selectedHoverboardId = HoverboardData.VECTOR_GLIDE.id
        prefs?.edit()?.clear()?.apply()
        onHoverboardChanged?.invoke(getEquippedHoverboard())
    }
}
