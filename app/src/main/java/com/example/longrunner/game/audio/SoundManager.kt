package com.example.longrunner.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

class SoundManager(private val context: Context? = null) {

    private val soundPool: SoundPool?
    private var swipeSoundId = 0
    private var jumpSoundId = 0
    private var slideSoundId = 0
    private var collectSoundId = 0
    private var crashSoundId = 0
    private var coreSoundId = 0
    private var creditSoundId = 0
    private var nearMissSoundId = 0
    private var boostSoundId = 0
    private var abilitySoundId = 0
    private var unlockSoundId = 0
    private var phaseEnterSoundId = 0
    private var phaseExitSoundId = 0
    private var fractureWarningSoundId = 0
    private var fractureClearSoundId = 0
    private var shieldPickupSoundId = 0
    private var shieldBreakSoundId = 0
    private var overdriveSoundId = 0
    private var timeBrakeSoundId = 0
    private var droneAlarmSoundId = 0
    private var realityGlitchSoundId = 0
    private var nullHeartbeatSoundId = 0
    private var nullSurgeSoundId = 0
    private var nullCatchSoundId = 0
    private var missionCompleteSoundId = 0
    private var rankUpSoundId = 0
    private var uiClickSoundId = 0

    var isEnabled = true
    var sfxVolume = 1.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }
    var musicVolume = 0.8f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    init {
        if (context != null) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(16)
                .setAudioAttributes(audioAttributes)
                .build()

            generateAndLoadSounds()
        } else {
            soundPool = null
        }
    }

    private fun generateAndLoadSounds() {
        val context = this.context ?: return
        val soundPool = this.soundPool ?: return
        try {
            val cacheDir = context.cacheDir

            // 1. Swipe: Fast dynamic frequency rise (300Hz -> 650Hz, 0.08s)
            val swipeFile = File(cacheDir, "sfx_swipe.wav")
            generateWav(swipeFile, 0.08f) { t ->
                val freq = 300.0 + 350.0 * (t / 0.08)
                sin(2.0 * PI * freq * t).toFloat() * (1.0f - (t / 0.08f))
            }
            swipeSoundId = soundPool.load(swipeFile.absolutePath, 1)

            // 2. Jump: Energetic chirp (400Hz -> 900Hz, 0.15s)
            val jumpFile = File(cacheDir, "sfx_jump.wav")
            generateWav(jumpFile, 0.15f) { t ->
                val freq = 400.0 + 500.0 * (t / 0.15)
                val env = 1.0f - (t / 0.15f)
                sin(2.0 * PI * freq * t).toFloat() * env
            }
            jumpSoundId = soundPool.load(jumpFile.absolutePath, 1)

            // 3. Slide: Low filtered whoosh (240Hz -> 140Hz, 0.22s)
            val slideFile = File(cacheDir, "sfx_slide.wav")
            generateWav(slideFile, 0.22f) { t ->
                val freq = 240.0 - 100.0 * (t / 0.22)
                val env = 1.0f - (t / 0.22f)
                (sin(2.0 * PI * freq * t) + 0.3 * sin(2.0 * PI * (freq * 0.5) * t)).toFloat() * env
            }
            slideSoundId = soundPool.load(slideFile.absolutePath, 1)

            // 4. Collect Shard: Bright crystal chime (880Hz + 1760Hz, 0.18s)
            val collectFile = File(cacheDir, "sfx_collect.wav")
            generateWav(collectFile, 0.18f) { t ->
                val env = 1.0f - (t / 0.18f)
                (0.6f * sin(2.0 * PI * 880.0 * t).toFloat() + 0.4f * sin(2.0 * PI * 1760.0 * t).toFloat()) * env
            }
            collectSoundId = soundPool.load(collectFile.absolutePath, 1)

            // 5. Crash: Low impact burst (120Hz down to 40Hz with noise, 0.35s)
            val crashFile = File(cacheDir, "sfx_crash.wav")
            generateWav(crashFile, 0.35f) { t ->
                val freq = 120.0 * (1.0 - t / 0.35)
                val noise = (Math.random().toFloat() * 2f - 1f) * 0.4f
                val env = (1.0f - (t / 0.35f)) * (1.0f - (t / 0.35f))
                (sin(2.0 * PI * freq * t).toFloat() * 0.6f + noise) * env
            }
            crashSoundId = soundPool.load(crashFile.absolutePath, 1)

            // 6. Phase Core: Resonant dual chord chime (528Hz + 1056Hz, 0.25s)
            val coreFile = File(cacheDir, "sfx_core.wav")
            generateWav(coreFile, 0.25f) { t ->
                val env = 1.0f - (t / 0.25f)
                (0.5f * sin(2.0 * PI * 528.0 * t).toFloat() + 0.5f * sin(2.0 * PI * 1056.0 * t).toFloat()) * env
            }
            coreSoundId = soundPool.load(coreFile.absolutePath, 1)

            // 7. Credit: High crisp double ping (1320Hz + 1980Hz, 0.12s)
            val creditFile = File(cacheDir, "sfx_credit.wav")
            generateWav(creditFile, 0.12f) { t ->
                val env = 1.0f - (t / 0.12f)
                (0.6f * sin(2.0 * PI * 1320.0 * t).toFloat() + 0.4f * sin(2.0 * PI * 1980.0 * t).toFloat()) * env
            }
            creditSoundId = soundPool.load(creditFile.absolutePath, 1)

            // 8. Near Miss: Cinematic riser whoosh (250Hz -> 800Hz, 0.22s)
            val nearMissFile = File(cacheDir, "sfx_near_miss.wav")
            generateWav(nearMissFile, 0.22f) { t ->
                val freq = 250.0 + 550.0 * (t / 0.22)
                val env = sin(PI * (t / 0.22)).toFloat()
                sin(2.0 * PI * freq * t).toFloat() * env
            }
            nearMissSoundId = soundPool.load(nearMissFile.absolutePath, 1)

            // 9. Multiplier Boost: Ascending synth sweep (440Hz -> 1200Hz, 0.3s)
            val boostFile = File(cacheDir, "sfx_boost.wav")
            generateWav(boostFile, 0.3f) { t ->
                val freq = 440.0 + 760.0 * (t / 0.3)
                val env = 1.0f - (t / 0.3f)
                sin(2.0 * PI * freq * t).toFloat() * env
            }
            boostSoundId = soundPool.load(boostFile.absolutePath, 1)

            // 10. Ability Activation: Sci-fi surge sweep (480Hz -> 1350Hz, 0.28s)
            val abilityFile = File(cacheDir, "sfx_ability.wav")
            generateWav(abilityFile, 0.28f) { t ->
                val freq = 480.0 + 870.0 * (t / 0.28)
                val env = 1.0f - (t / 0.28f)
                (0.7f * sin(2.0 * PI * freq * t).toFloat() + 0.3f * sin(2.0 * PI * (freq * 1.5) * t).toFloat()) * env
            }
            abilitySoundId = soundPool.load(abilityFile.absolutePath, 1)

            // 11. Unlock Fanfare: Major chord harmonic burst (587Hz + 880Hz + 1174Hz, 0.35s)
            val unlockFile = File(cacheDir, "sfx_unlock.wav")
            generateWav(unlockFile, 0.35f) { t ->
                val env = 1.0f - (t / 0.35f)
                (0.4f * sin(2.0 * PI * 587.33 * t).toFloat() +
                 0.35f * sin(2.0 * PI * 880.0 * t).toFloat() +
                 0.25f * sin(2.0 * PI * 1174.66 * t).toFloat()) * env
            }
            unlockSoundId = soundPool.load(unlockFile.absolutePath, 1)

            // 12. Phase Shift Enter: Resonant low-to-high frequency warp (180Hz -> 580Hz, 0.24s)
            val phaseEnterFile = File(cacheDir, "sfx_phase_enter.wav")
            generateWav(phaseEnterFile, 0.24f) { t ->
                val freq = 180.0 + 400.0 * (t / 0.24)
                val env = sin(PI * (t / 0.24)).toFloat()
                (sin(2.0 * PI * freq * t) + 0.35 * sin(2.0 * PI * (freq * 2.0) * t)).toFloat() * env
            }
            phaseEnterSoundId = soundPool.load(phaseEnterFile.absolutePath, 1)

            // 13. Phase Shift Exit: Dimensional return chime (580Hz -> 200Hz, 0.20s)
            val phaseExitFile = File(cacheDir, "sfx_phase_exit.wav")
            generateWav(phaseExitFile, 0.20f) { t ->
                val freq = 580.0 - 380.0 * (t / 0.20)
                val env = (1.0f - (t / 0.20f))
                (0.6f * sin(2.0 * PI * freq * t).toFloat() + 0.4f * sin(2.0 * PI * (freq * 0.5) * t).toFloat()) * env
            }
            phaseExitSoundId = soundPool.load(phaseExitFile.absolutePath, 1)

            // 14. Route Fracture Warning: Urgent pulsing sci-fi alert (680Hz -> 420Hz, 0.32s)
            val fractureWarnFile = File(cacheDir, "sfx_fracture_warn.wav")
            generateWav(fractureWarnFile, 0.32f) { t ->
                val pulse = sin(2.0 * PI * 14.0 * t).toFloat()
                val freq = if (pulse > 0f) 680.0 else 440.0
                val env = (1.0f - (t / 0.32f))
                (0.7f * sin(2.0 * PI * freq * t).toFloat() + 0.3f * sin(2.0 * PI * (freq * 1.5) * t).toFloat()) * env
            }
            fractureWarningSoundId = soundPool.load(fractureWarnFile.absolutePath, 1)

            // 15. Route Fracture Cleared: Triumphant harmonic resolution chime (440Hz + 660Hz + 880Hz + 1320Hz, 0.45s)
            val fractureClearFile = File(cacheDir, "sfx_fracture_clear.wav")
            generateWav(fractureClearFile, 0.45f) { t ->
                val env = (1.0f - (t / 0.45f)) * (1.0f - (t / 0.45f))
                (0.35f * sin(2.0 * PI * 440.0 * t).toFloat() +
                 0.30f * sin(2.0 * PI * 660.0 * t).toFloat() +
                 0.20f * sin(2.0 * PI * 880.0 * t).toFloat() +
                 0.15f * sin(2.0 * PI * 1320.0 * t).toFloat()) * env
            }
            fractureClearSoundId = soundPool.load(fractureClearFile.absolutePath, 1)

            // 16. Shield Pickup: High crystalline chime charge (520Hz -> 1040Hz, 0.22s)
            val shieldPickFile = File(cacheDir, "sfx_shield_pickup.wav")
            generateWav(shieldPickFile, 0.22f) { t ->
                val freq = 520.0 + 520.0 * (t / 0.22)
                val env = (1.0f - (t / 0.22f))
                sin(2.0 * PI * freq * t).toFloat() * env
            }
            shieldPickupSoundId = soundPool.load(shieldPickFile.absolutePath, 1)

            // 17. Shield Break: Glass shattering kinetic dispersion (1200Hz -> 200Hz with noise, 0.28s)
            val shieldBreakFile = File(cacheDir, "sfx_shield_break.wav")
            generateWav(shieldBreakFile, 0.28f) { t ->
                val freq = 1200.0 - 1000.0 * (t / 0.28)
                val noise = (Math.random().toFloat() * 2f - 1f) * 0.45f
                val env = (1.0f - (t / 0.28f))
                (sin(2.0 * PI * freq * t).toFloat() * 0.55f + noise) * env
            }
            shieldBreakSoundId = soundPool.load(shieldBreakFile.absolutePath, 1)

            // 18. Overdrive Ignition: Hypersonic afterburner surge (220Hz -> 880Hz, 0.35s)
            val overdriveFile = File(cacheDir, "sfx_overdrive.wav")
            generateWav(overdriveFile, 0.35f) { t ->
                val freq = 220.0 + 660.0 * (t / 0.35)
                val env = sin(PI * (t / 0.35)).toFloat()
                (0.6f * sin(2.0 * PI * freq * t).toFloat() + 0.4f * sin(2.0 * PI * (freq * 1.5) * t).toFloat()) * env
            }
            overdriveSoundId = soundPool.load(overdriveFile.absolutePath, 1)

            // 19. Time Brake: Chrono warp temporal dilation (440Hz -> 180Hz smooth glide, 0.30s)
            val timeBrakeFile = File(cacheDir, "sfx_time_brake.wav")
            generateWav(timeBrakeFile, 0.30f) { t ->
                val freq = 440.0 - 260.0 * (t / 0.30)
                val env = (1.0f - (t / 0.30f))
                (0.7f * sin(2.0 * PI * freq * t).toFloat() + 0.3f * sin(2.0 * PI * (freq * 0.5) * t).toFloat()) * env
            }
            timeBrakeSoundId = soundPool.load(timeBrakeFile.absolutePath, 1)


            // 21. Drone Swarm Alert: Radar scanner alarm chirp (1100Hz <-> 1600Hz alternating, 0.25s)
            val droneAlarmFile = File(cacheDir, "sfx_drone_alarm.wav")
            generateWav(droneAlarmFile, 0.25f) { t ->
                val freq = if ((t * 20.0).toInt() % 2 == 0) 1100.0 else 1600.0
                val env = sin(PI * (t / 0.25)).toFloat()
                sin(2.0 * PI * freq * t).toFloat() * env
            }
            droneAlarmSoundId = soundPool.load(droneAlarmFile.absolutePath, 1)

            // 22. Reality Glitch: Digital phase distortion buzz with noise (0.28s)
            val glitchFile = File(cacheDir, "sfx_glitch.wav")
            generateWav(glitchFile, 0.28f) { t ->
                val carrier = sin(2.0 * PI * (380.0 + 300.0 * sin(2.0 * PI * 40.0 * t)) * t).toFloat()
                val noise = (Math.random().toFloat() * 2f - 1f) * 0.4f
                val env = (1.0f - (t / 0.28f))
                (carrier * 0.6f + noise) * env
            }
            realityGlitchSoundId = soundPool.load(glitchFile.absolutePath, 1)

            // 23. The Null Heartbeat: Deep sub-bass pulse (55Hz -> 38Hz, 0.22s)
            val heartbeatFile = File(cacheDir, "sfx_null_heartbeat.wav")
            generateWav(heartbeatFile, 0.22f) { t ->
                val freq = 55.0 - 17.0 * (t / 0.22)
                val env = sin(PI * (t / 0.22)).toFloat()
                sin(2.0 * PI * freq * t).toFloat() * env
            }
            nullHeartbeatSoundId = soundPool.load(heartbeatFile.absolutePath, 1)

            // 24. The Null Surge: Distorted void roar lunging forward (130Hz -> 65Hz, 0.38s)
            val nullSurgeFile = File(cacheDir, "sfx_null_surge.wav")
            generateWav(nullSurgeFile, 0.38f) { t ->
                val freq = 130.0 - 65.0 * (t / 0.38)
                val sub = sin(2.0 * PI * (freq * 0.5) * t).toFloat() * 0.4f
                val noise = (Math.random().toFloat() * 2f - 1f) * 0.35f
                val env = (1.0f - (t / 0.38f))
                (sin(2.0 * PI * freq * t).toFloat() * 0.5f + sub + noise) * env
            }
            nullSurgeSoundId = soundPool.load(nullSurgeFile.absolutePath, 1)

            // 25. The Null Catch: Catastrophic void implosion (180Hz -> 30Hz with heavy noise, 0.55s)
            val nullCatchFile = File(cacheDir, "sfx_null_catch.wav")
            generateWav(nullCatchFile, 0.55f) { t ->
                val freq = 180.0 - 150.0 * (t / 0.55)
                val noise = (Math.random().toFloat() * 2f - 1f) * 0.65f
                val env = (1.0f - (t / 0.55f))
                (sin(2.0 * PI * freq * t).toFloat() * 0.45f + noise) * env
            }
            nullCatchSoundId = soundPool.load(nullCatchFile.absolutePath, 1)

            // 26. Mission Complete: Ascending celebratory arpeggio chime (0.36s)
            val missionFile = File(cacheDir, "sfx_mission_complete.wav")
            generateWav(missionFile, 0.36f) { t ->
                val step = (t / 0.09f).toInt().coerceIn(0, 3)
                val freq = when (step) {
                    0 -> 523.25
                    1 -> 659.25
                    2 -> 783.99
                    else -> 1046.50
                }
                val localT = t - (step * 0.09f)
                val env = (1.0f - (localT / 0.09f)).coerceIn(0f, 1f)
                (sin(2.0 * PI * freq * t).toFloat() * 0.7f + 0.3f * sin(2.0 * PI * (freq * 2.0) * t).toFloat()) * env
            }
            missionCompleteSoundId = soundPool.load(missionFile.absolutePath, 1)

            // 27. Rank Up: Resonant cyber fanfare triad with harmonic sparkle (0.55s)
            val rankUpFile = File(cacheDir, "sfx_rank_up.wav")
            generateWav(rankUpFile, 0.55f) { t ->
                val env = (1.0f - (t / 0.55f))
                val wave = 0.35f * sin(2.0 * PI * 440.0 * t).toFloat() +
                           0.30f * sin(2.0 * PI * 554.37 * t).toFloat() +
                           0.25f * sin(2.0 * PI * 659.25 * t).toFloat() +
                           0.20f * sin(2.0 * PI * 880.0 * t).toFloat()
                wave * env
            }
            rankUpSoundId = soundPool.load(rankUpFile.absolutePath, 1)

            // 28. UI Click: High-tech cyber blip (1200Hz -> 1800Hz, 0.04s)
            val uiClickFile = File(cacheDir, "sfx_ui_click.wav")
            generateWav(uiClickFile, 0.04f) { t ->
                val freq = 1200.0 + 600.0 * (t / 0.04)
                val env = (1.0f - (t / 0.04f)).coerceIn(0f, 1f)
                sin(2.0 * PI * freq * t).toFloat() * env
            }
            uiClickSoundId = soundPool.load(uiClickFile.absolutePath, 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateWav(file: File, durationSec: Float, sampleFunc: (Float) -> Float) {
        val sampleRate = 22050
        val numSamples = (durationSec * sampleRate).toInt()
        val dataSize = numSamples * 2

        val buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put("RIFF".toByteArray())
        buffer.putInt(36 + dataSize)
        buffer.put("WAVE".toByteArray())

        buffer.put("fmt ".toByteArray())
        buffer.putInt(16)
        buffer.putShort(1)
        buffer.putShort(1)
        buffer.putInt(sampleRate)
        buffer.putInt(sampleRate * 2)
        buffer.putShort(2)
        buffer.putShort(16)
        buffer.put("data".toByteArray())
        buffer.putInt(dataSize)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val sample = sampleFunc(t).coerceIn(-1.0f, 1.0f)
            val pcm = (sample * 32767.0f).toInt().toShort()
            buffer.putShort(pcm)
        }

        FileOutputStream(file).use {
            it.write(buffer.array())
        }
    }

    private fun play(soundId: Int, baseVol: Float, priority: Int, rate: Float = 1.0f) {
        if (!isEnabled || soundId == 0) return
        val vol = (baseVol * sfxVolume).coerceIn(0f, 1f)
        if (vol > 0f) {
            soundPool?.play(soundId, vol, vol, priority, 0, rate)
        }
    }

    fun playSwipe() = play(swipeSoundId, 0.8f, 1)
    fun playJump() = play(jumpSoundId, 1.0f, 1)
    fun playSlide() = play(slideSoundId, 0.9f, 1)
    fun playCollect() = play(collectSoundId, 0.85f, 1)
    fun playCore() = play(coreSoundId, 1.0f, 2)
    fun playCredit() = play(creditSoundId, 0.95f, 2)
    fun playNearMiss() = play(nearMissSoundId, 1.0f, 3)
    fun playBoost() = play(boostSoundId, 1.0f, 3)
    fun playCrash() = play(crashSoundId, 1.0f, 4)
    fun playAbility() = play(abilitySoundId, 1.0f, 5)
    fun playUnlock() = play(unlockSoundId, 1.0f, 5)
    fun playPhaseShiftEnter() = play(phaseEnterSoundId, 1.0f, 6)
    fun playPhaseShiftExit() = play(phaseExitSoundId, 0.9f, 6)
    fun playFractureWarning() = play(fractureWarningSoundId, 1.0f, 7)
    fun playFractureClear() = play(fractureClearSoundId, 1.0f, 7)
    fun playShieldPickup() = play(shieldPickupSoundId, 1.0f, 8)
    fun playShieldBreak() = play(shieldBreakSoundId, 1.0f, 8)
    fun playOverdrive() = play(overdriveSoundId, 1.0f, 9)
    fun playTimeBrake() = play(timeBrakeSoundId, 1.0f, 9)
    fun playDroneAlarm() = play(droneAlarmSoundId, 0.9f, 8)
    fun playRealityGlitch() = play(realityGlitchSoundId, 0.85f, 7)
    fun playNullHeartbeat() = play(nullHeartbeatSoundId, 1.0f, 10)
    fun playNullSurge() = play(nullSurgeSoundId, 1.0f, 10)
    fun playNullCatch() = play(nullCatchSoundId, 1.0f, 10)
    fun playMissionComplete() = play(missionCompleteSoundId, 1.0f, 11)
    fun playRankUp() = play(rankUpSoundId, 1.0f, 11)
    fun playUiClick() = play(uiClickSoundId, 0.9f, 2)

    fun release() {
        soundPool?.release()
    }
}
