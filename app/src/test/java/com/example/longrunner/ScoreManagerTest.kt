package com.example.longrunner

import com.example.longrunner.game.score.ScoreManager
import org.junit.Assert.*
import org.junit.Test

class ScoreManagerTest {

    @Test
    fun testScoreCalculation() {
        val scoreManager = ScoreManager(null)
        scoreManager.reset()

        assertEquals(0L, scoreManager.score)
        assertEquals(0f, scoreManager.distance, 0.001f)
        assertEquals(0, scoreManager.shardsCollected)

        // Player runs 50m at speed 20.0f
        scoreManager.update(-50.0f, 20.0f)
        assertEquals(50.0f, scoreManager.distance, 0.001f)
        assertEquals(2, scoreManager.multiplier) // 20 / 10 = 2

        // Base distance score = 50 * 10 = 500; with multiplier x2 = 1000
        assertEquals(1000L, scoreManager.score)

        // Collect 3 shards
        scoreManager.addShard()
        scoreManager.addShard()
        scoreManager.addShard()

        scoreManager.update(-50.0f, 20.0f)
        // (500 + 3 * 50) * 2 = 650 * 2 = 1300
        assertEquals(1300L, scoreManager.score)
        assertTrue(scoreManager.highScore >= 1300L)
    }
}
