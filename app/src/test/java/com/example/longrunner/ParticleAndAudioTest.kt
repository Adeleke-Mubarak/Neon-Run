package com.example.longrunner

import com.example.longrunner.game.audio.MusicManager
import com.example.longrunner.game.graphics.ParticleSystem
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ParticleAndAudioTest {

    private lateinit var particleSystem: ParticleSystem
    private lateinit var musicManager: MusicManager

    @Before
    fun setUp() {
        particleSystem = ParticleSystem(maxParticles = 256)
        // MusicManager with null context for pure JVM unit testing
        musicManager = MusicManager(null)
    }

    @Test
    fun testParticleSystemInitialization() {
        assertEquals(256, particleSystem.maxParticles)
        assertEquals(256, particleSystem.particles.size)
        for (p in particleSystem.particles) {
            assertFalse("All particles must initialize inactive", p.isActive)
            assertEquals(0f, p.life, 0.0001f)
        }
    }

    @Test
    fun testEmitFootstepSpark() {
        val colorHex = 0x00FFCCL // Cyan teal
        particleSystem.emitFootstepSpark(1.5f, 0.0f, -20.0f, colorHex)

        val activeCount = particleSystem.particles.count { it.isActive }
        assertEquals(1, activeCount)

        val p = particleSystem.particles.first { it.isActive }
        assertTrue("Spark life must be positive", p.life > 0f)
        assertTrue("Spark maxLife must be positive", p.maxLife > 0f)
        assertEquals(1.5f, p.x, 0.5f)
        assertEquals(0.05f, p.y, 0.01f)
        assertEquals(-19.8f, p.z, 0.1f)
        assertTrue("Spark should have forward momentum", p.vz > 0f)
        // Hex extraction check
        assertEquals(0.0f, p.r, 0.01f)
        assertEquals(1.0f, p.g, 0.01f)
        assertEquals(0.8f, p.b, 0.01f)
    }

    @Test
    fun testEmitCollectibleBurst() {
        val burstCount = 12
        particleSystem.emitCollectibleBurst(0f, 1.2f, -50f, 1.0f, 0.85f, 0.0f, burstCount)

        val activeCount = particleSystem.particles.count { it.isActive }
        assertEquals(burstCount, activeCount)

        for (p in particleSystem.particles.filter { it.isActive }) {
            assertEquals(0f, p.x, 0.001f)
            assertEquals(1.2f, p.y, 0.001f)
            assertEquals(-50f, p.z, 0.001f)
            assertEquals(1.0f, p.r, 0.001f)
            assertEquals(0.85f, p.g, 0.001f)
            assertEquals(0.0f, p.b, 0.001f)
            assertTrue("Particle velocity magnitude must be non-zero", p.vx * p.vx + p.vy * p.vy + p.vz * p.vz > 0.1f)
        }
    }

    @Test
    fun testEmitObstacleSmash() {
        val debrisCount = 16
        particleSystem.emitObstacleSmash(-2.0f, 0.5f, -30f, debrisCount)

        val activeCount = particleSystem.particles.count { it.isActive }
        assertEquals(debrisCount, activeCount)

        for (p in particleSystem.particles.filter { it.isActive }) {
            assertEquals(1.0f, p.r, 0.001f) // Amber/orange destruction
            assertTrue("Green channel within destruction tint", p.g in 0.35f..0.95f)
            assertEquals(0.05f, p.b, 0.001f)
            assertTrue("Debris has upward launch velocity", p.vy > 1.5f)
        }
    }

    @Test
    fun testEmitNearMissShockwave() {
        val count = 10
        particleSystem.emitNearMissShockwave(1.0f, 0.0f, -15.0f, count)

        val activeCount = particleSystem.particles.count { it.isActive }
        assertEquals(count, activeCount)

        for (p in particleSystem.particles.filter { it.isActive }) {
            assertEquals(1.0f, p.r, 0.001f) // Golden yellow
            assertEquals(0.85f, p.g, 0.001f)
            assertEquals(0.0f, p.b, 0.001f)
            assertEquals(0.5f, p.y, 0.001f)
        }
    }

    @Test
    fun testEmitSpeedStreakThreshold() {
        // Under threshold: 15 m/s -> no streak
        particleSystem.emitSpeedStreak(cameraZ = -10f, playerSpeed = 15.0f)
        assertEquals(0, particleSystem.particles.count { it.isActive })

        // At/above threshold: 18 m/s -> emits streaks
        particleSystem.emitSpeedStreak(cameraZ = -10f, playerSpeed = 18.0f)
        assertTrue(particleSystem.particles.count { it.isActive } > 0)
    }

    @Test
    fun testParticlePhysicsAndDecay() {
        particleSystem.emitFootstepSpark(0f, 1f, 0f, 0xFF0000L)
        val p = particleSystem.particles.first { it.isActive }
        val initialVy = p.vy
        val initialLife = p.life
        val initialZ = p.z

        val dt = 0.1f
        particleSystem.update(dt)

        assertEquals(initialLife - dt, p.life, 0.001f)
        assertTrue("Z position should increase with positive vz", p.z > initialZ)
        assertTrue("Gravity should pull down vy", p.vy < initialVy)
    }

    @Test
    fun testParticleLifetimeExpirationAndRecycling() {
        particleSystem.emitFootstepSpark(0f, 0f, 0f, 0x00FF00L)
        assertEquals(1, particleSystem.particles.count { it.isActive })

        // Advance time beyond max particle lifetime (0.7 seconds)
        particleSystem.update(1.5f)

        assertEquals(0, particleSystem.particles.count { it.isActive })
    }

    @Test
    fun testParticleSystemReset() {
        particleSystem.emitCollectibleBurst(0f, 0f, 0f, 1f, 1f, 1f, 20)
        assertEquals(20, particleSystem.particles.count { it.isActive })

        particleSystem.reset()
        assertEquals(0, particleSystem.particles.count { it.isActive })
    }

    @Test
    fun testParticlePoolExhaustionSafeWrapping() {
        // Emit more than 256 particles to test zero-allocation ring recycling
        for (i in 0 until 30) {
            particleSystem.emitCollectibleBurst(0f, 0f, 0f, 1f, 1f, 1f, 10)
        }
        // Total particles active capped at maxParticles (256)
        assertEquals(256, particleSystem.particles.count { it.isActive })
    }

    @Test
    fun testMusicManagerInitialValues() {
        assertEquals(0.8f, musicManager.musicVolume, 0.001f)
        assertTrue(musicManager.isEnabled)
        assertFalse(musicManager.isPlaying)
    }

    @Test
    fun testMusicManagerVolumeClamping() {
        musicManager.musicVolume = 1.75f
        assertEquals(1.0f, musicManager.musicVolume, 0.001f)

        musicManager.musicVolume = -0.5f
        assertEquals(0.0f, musicManager.musicVolume, 0.001f)

        musicManager.musicVolume = 0.65f
        assertEquals(0.65f, musicManager.musicVolume, 0.001f)
    }

    @Test
    fun testMusicManagerMuteToggle() {
        musicManager.isEnabled = false
        assertFalse(musicManager.isEnabled)

        musicManager.isEnabled = true
        assertTrue(musicManager.isEnabled)
    }

    @Test
    fun testMusicManagerPlaybackStateTracking() {
        assertFalse(musicManager.isPlaying)

        musicManager.start()
        assertTrue(musicManager.isPlaying)

        musicManager.pause()
        assertFalse(musicManager.isPlaying)

        musicManager.resume()
        assertTrue(musicManager.isPlaying)

        musicManager.stop()
        assertFalse(musicManager.isPlaying)

        musicManager.release()
        assertFalse(musicManager.isPlaying)
    }
}
