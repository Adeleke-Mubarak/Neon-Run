package com.example.longrunner.game.audio

import android.content.Context
import android.media.MediaPlayer
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural Dynamic Cyber Synthwave Soundtrack Engine.
 * Generates an algorithmic, loopable 128 BPM cyberpunk synthwave track
 * and streams it via MediaPlayer with seamless looping and dynamic volume scaling.
 */
class MusicManager(private val context: Context? = null) {

    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared = false

    var musicVolume: Float = 0.8f
        set(value) {
            field = value.coerceIn(0f, 1f)
            applyVolume()
        }

    var isEnabled: Boolean = true
        set(value) {
            field = value
            applyVolume()
        }

    var isPlaying: Boolean = false
        private set

    init {
        if (context != null) {
            try {
                prepareSoundtrack(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun prepareSoundtrack(ctx: Context) {
        val cacheDir = ctx.cacheDir
        val musicFile = File(cacheDir, "bgm_cyber_loop.wav")

        if (!musicFile.exists() || musicFile.length() < 1000) {
            generateSynthwaveLoop(musicFile)
        }

        mediaPlayer = MediaPlayer().apply {
            setDataSource(musicFile.absolutePath)
            isLooping = true
            setOnPreparedListener {
                isPrepared = true
                applyVolume()
            }
            prepareAsync()
        }
    }

    /**
     * Synthesizes a loopable 128 BPM 4-bar cyberpunk synthwave audio bed (7.5 seconds).
     * Features:
     * - Punchy 4-on-the-floor electro kicks with exponential pitch decay
     * - Cyber snares on beats 2 and 4
     * - Driving 16th-note rolling sub-bassline in A-minor
     * - Melodic cyber arpeggio lead with harmonic overtone
     */
    private fun generateSynthwaveLoop(file: File) {
        val sampleRate = 22050
        val bpm = 128.0
        val beatDuration = 60.0 / bpm // ~0.46875s
        val totalBeats = 16.0 // 4 bars = 16 beats
        val durationSec = (totalBeats * beatDuration).toFloat() // 7.5s
        val numSamples = (durationSec * sampleRate).toInt()
        val dataSize = numSamples * 2

        val buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put("RIFF".toByteArray())
        buffer.putInt(36 + dataSize)
        buffer.put("WAVE".toByteArray())

        buffer.put("fmt ".toByteArray())
        buffer.putInt(16)
        buffer.putShort(1) // PCM
        buffer.putShort(1) // Mono
        buffer.putInt(sampleRate)
        buffer.putInt(sampleRate * 2)
        buffer.putShort(2)
        buffer.putShort(16)
        buffer.put("data".toByteArray())
        buffer.putInt(dataSize)

        // Bass progression frequencies (A2 -> F2 -> C3 -> G2)
        val chordFreqs = doubleArrayOf(110.0, 87.31, 130.81, 98.0)
        // Arp frequencies (A4, C5, E5, G5, B5, A5, E5, C5)
        val arpNotes = doubleArrayOf(440.0, 523.25, 659.25, 783.99, 987.77, 880.0, 659.25, 523.25)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val currentBeat = (t / beatDuration) % totalBeats
            val barIndex = (currentBeat / 4.0).toInt().coerceIn(0, 3)

            // 1. Kick Drum (every beat: beatFraction == 0)
            val beatFrac = currentBeat % 1.0
            val kickTime = beatFrac * beatDuration
            var kickSample = 0.0
            if (kickTime < 0.22) {
                val kickEnv = (1.0 - (kickTime / 0.22)).coerceIn(0.0, 1.0)
                val kickPitch = 50.0 + 130.0 * (kickEnv * kickEnv)
                kickSample = sin(2.0 * PI * kickPitch * kickTime) * kickEnv * 0.45
            }

            // 2. Snare / Cyber Clap (beats 1 and 3 in 0-indexed, meaning beat 2 & 4)
            val snareBeat = (currentBeat % 2.0)
            val isSnareBeat = (currentBeat % 4.0) >= 1.0 && (currentBeat % 4.0) < 2.0 || (currentBeat % 4.0) >= 3.0
            var snareSample = 0.0
            if (isSnareBeat && snareBeat < 0.2) {
                val snareTime = snareBeat * beatDuration
                val snareEnv = (1.0 - (snareTime / 0.20)).coerceIn(0.0, 1.0)
                val noise = ((i * 1103515245 + 12345) % 65536) / 32768.0 - 1.0
                val tone = sin(2.0 * PI * 220.0 * snareTime) * 0.4
                snareSample = (noise * 0.6 + tone) * snareEnv * 0.35
            }

            // 3. 16th-Note Rolling Sub-Bassline
            val sixteenthFrac = (currentBeat * 4.0) % 1.0
            val bassTime = sixteenthFrac * (beatDuration / 4.0)
            val bassEnv = (1.0 - (bassTime / (beatDuration / 4.0))).coerceIn(0.0, 1.0)
            val baseFreq = chordFreqs[barIndex]
            val bassSample = (sin(2.0 * PI * baseFreq * t) * 0.65 +
                    sin(2.0 * PI * (baseFreq * 2.0) * t) * 0.25) * bassEnv * 0.35

            // 4. 8th-Note Melodic Cyber Arpeggio
            val eighthStep = ((currentBeat * 2.0).toInt()) % arpNotes.size
            val eighthFrac = (currentBeat * 2.0) % 1.0
            val arpTime = eighthFrac * (beatDuration / 2.0)
            val arpEnv = (1.0 - (arpTime / (beatDuration / 2.0))).coerceIn(0.0, 1.0)
            val arpFreq = arpNotes[eighthStep]
            val arpSample = (sin(2.0 * PI * arpFreq * t) * 0.7 +
                    sin(2.0 * PI * (arpFreq * 2.0) * t) * 0.3) * arpEnv * 0.20

            // Mix components
            val totalMix = (kickSample + snareSample + bassSample + arpSample).coerceIn(-1.0, 1.0)
            val pcm = (totalMix * 32767.0).toInt().toShort()
            buffer.putShort(pcm)
        }

        FileOutputStream(file).use {
            it.write(buffer.array())
        }
    }

    fun start() {
        if (!isPlaying) {
            isPlaying = true
            try {
                if (isPrepared && isEnabled && musicVolume > 0f) {
                    mediaPlayer?.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun pause() {
        if (isPlaying) {
            isPlaying = false
            try {
                mediaPlayer?.pause()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resume() {
        if (!isPlaying) {
            isPlaying = true
            try {
                if (isPrepared && isEnabled && musicVolume > 0f) {
                    mediaPlayer?.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        isPlaying = false
        try {
            mediaPlayer?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyVolume() {
        val effectiveVol = if (isEnabled) musicVolume else 0f
        try {
            mediaPlayer?.setVolume(effectiveVol, effectiveVol)
            if (isPlaying && isPrepared) {
                if (effectiveVol > 0f && mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.start()
                } else if (effectiveVol == 0f && mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        isPlaying = false
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
