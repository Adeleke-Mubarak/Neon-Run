package com.example.longrunner.game.score

import android.content.Context
import com.example.longrunner.game.core.GameConstants
import kotlin.math.abs
import kotlin.math.max

class ScoreManager(context: Context? = null) {

    private val prefs = context?.getSharedPreferences("neon_run_save", Context.MODE_PRIVATE)

    var distance: Float = 0f
        private set

    var shardsCollected: Int = 0
        private set

    var phaseCoresCollected: Int = 0
        private set

    var creditsCollected: Int = 0
        private set

    var nearMissCount: Int = 0
        private set

    var bonusScore: Long = 0
        private set

    var multiplierTokenTimer: Float = 0f
        private set
    val isMultiplierBoostActive: Boolean get() = multiplierTokenTimer > 0f

    var multiplier: Int = 1
        private set

    var score: Long = 0
        private set

    var highScore: Long = prefs?.getLong("high_score", 0L) ?: 0L
        private set

    fun reset() {
        distance = 0f
        shardsCollected = 0
        phaseCoresCollected = 0
        creditsCollected = 0
        nearMissCount = 0
        bonusScore = 0
        multiplierTokenTimer = 0f
        score = 0
        multiplier = 1
    }

    fun update(playerZ: Float, speed: Float, combo: Int = 1, dt: Float = 0f, extraMultiplier: Int = 0) {
        distance = abs(playerZ)

        // Decrement token boost timer
        if (multiplierTokenTimer > 0f) {
            multiplierTokenTimer -= dt
            if (multiplierTokenTimer < 0f) multiplierTokenTimer = 0f
        }

        // Base speed multiplier (1x at 18m/s, up to 3x)
        val speedMult = max(1, (speed / 10f).toInt())
        val tokenMult = if (isMultiplierBoostActive) 2 else 1
        multiplier = (speedMult * combo * tokenMult) + extraMultiplier

        val distanceScore = (distance * GameConstants.SCORE_PER_METER).toLong()
        val shardScore = (shardsCollected.toLong() * GameConstants.SCORE_PER_SHARD)
        val coreScore = (phaseCoresCollected.toLong() * 150L)
        val creditScore = (creditsCollected.toLong() * 200L)

        score = (distanceScore + shardScore + coreScore + creditScore + bonusScore) * multiplier

        if (score > highScore) {
            highScore = score
        }
    }

    fun addShard() {
        shardsCollected++
    }

    fun addPhaseCore() {
        phaseCoresCollected++
    }

    fun addCredit() {
        creditsCollected++
    }

    fun addMultiplierToken() {
        multiplierTokenTimer = 10.0f
    }

    fun addNearMiss() {
        nearMissCount++
        bonusScore += 250L
    }

    fun addBonusScore(points: Long) {
        bonusScore += points
    }

    fun persistHighScore() {
        prefs?.edit()?.putLong("high_score", highScore)?.apply()
    }
}
