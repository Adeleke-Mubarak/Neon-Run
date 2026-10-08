package com.example.longrunner.game.settings

import android.content.Context
import android.content.SharedPreferences

enum class GraphicsQuality(val displayName: String, val description: String) {
    PERFORMANCE("Performance", "Optimized framerate, essential neon lighting"),
    BALANCED("Balanced", "Smooth reflections, ambient neon glow"),
    ULTRA("Ultra Neon", "Full volumetric bloom, cyber shaders & reflections")
}

enum class LaneSensitivity(val displayName: String, val thresholdMultiplier: Float) {
    NORMAL("Normal", 1.0f),
    FAST("Fast", 0.75f),
    HYPER("Hyper", 0.5f)
}

/**
 * Manages player audio, graphics, and gameplay customization settings
 * with persistent SharedPreferences storage.
 */
class SettingsManager(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("neon_run_settings", Context.MODE_PRIVATE)

    var sfxVolume: Float = prefs?.getFloat("sfx_volume", 1.0f) ?: 1.0f
        set(value) {
            field = value.coerceIn(0.0f, 1.0f)
            save()
            onSettingsChanged?.invoke()
        }

    var musicVolume: Float = prefs?.getFloat("music_volume", 0.8f) ?: 0.8f
        set(value) {
            field = value.coerceIn(0.0f, 1.0f)
            save()
            onSettingsChanged?.invoke()
        }

    var isSfxEnabled: Boolean = prefs?.getBoolean("sfx_enabled", true) ?: true
        set(value) {
            field = value
            save()
            onSettingsChanged?.invoke()
        }

    var isMusicEnabled: Boolean = prefs?.getBoolean("music_enabled", true) ?: true
        set(value) {
            field = value
            save()
            onSettingsChanged?.invoke()
        }

    var isScreenShakeEnabled: Boolean = prefs?.getBoolean("screen_shake_enabled", true) ?: true
        set(value) {
            field = value
            save()
            onSettingsChanged?.invoke()
        }

    var isGlitchShaderEnabled: Boolean = prefs?.getBoolean("glitch_shader_enabled", true) ?: true
        set(value) {
            field = value
            save()
            onSettingsChanged?.invoke()
        }

    var isHapticsEnabled: Boolean = prefs?.getBoolean("haptics_enabled", true) ?: true
        set(value) {
            field = value
            save()
            onSettingsChanged?.invoke()
        }

    var graphicsQuality: GraphicsQuality = try {
        val name = prefs?.getString("graphics_quality", GraphicsQuality.ULTRA.name) ?: GraphicsQuality.ULTRA.name
        GraphicsQuality.valueOf(name)
    } catch (_: Exception) {
        GraphicsQuality.ULTRA
    }
        set(value) {
            field = value
            save()
            onSettingsChanged?.invoke()
        }

    var laneSensitivity: LaneSensitivity = try {
        val name = prefs?.getString("lane_sensitivity", LaneSensitivity.NORMAL.name) ?: LaneSensitivity.NORMAL.name
        LaneSensitivity.valueOf(name)
    } catch (_: Exception) {
        LaneSensitivity.NORMAL
    }
        set(value) {
            field = value
            save()
            onSettingsChanged?.invoke()
        }

    var onSettingsChanged: (() -> Unit)? = null

    fun resetToDefaults() {
        sfxVolume = 1.0f
        musicVolume = 0.8f
        isSfxEnabled = true
        isMusicEnabled = true
        isScreenShakeEnabled = true
        isGlitchShaderEnabled = true
        isHapticsEnabled = true
        graphicsQuality = GraphicsQuality.ULTRA
        laneSensitivity = LaneSensitivity.NORMAL
        save()
        onSettingsChanged?.invoke()
    }

    private fun save() {
        prefs?.edit()?.apply {
            putFloat("sfx_volume", sfxVolume)
            putFloat("music_volume", musicVolume)
            putBoolean("sfx_enabled", isSfxEnabled)
            putBoolean("music_enabled", isMusicEnabled)
            putBoolean("screen_shake_enabled", isScreenShakeEnabled)
            putBoolean("glitch_shader_enabled", isGlitchShaderEnabled)
            putBoolean("haptics_enabled", isHapticsEnabled)
            putString("graphics_quality", graphicsQuality.name)
            putString("lane_sensitivity", laneSensitivity.name)
            apply()
        }
    }
}
