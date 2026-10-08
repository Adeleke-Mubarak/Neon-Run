package com.example.longrunner.game.powerups

import com.example.longrunner.game.graphics.Material

enum class HoverboardRarity(val displayName: String, val colorHex: Long) {
    CORE("CORE", 0xFF00F0FF),
    RARE("RARE", 0xFF00FF88),
    EPIC("EPIC", 0xFFB026FF),
    LEGENDARY("LEGENDARY", 0xFFFFD700)
}

/**
 * Data model for individual cyber hoverboard models in the Cyber Vault.
 * Ordered from lowest value (free starter deck) to highest value (legendary flagship).
 */
data class HoverboardData(
    val id: String,
    val name: String,
    val modelCode: String,
    val description: String,
    val rarity: HoverboardRarity,
    val costCredits: Int,
    val durationSeconds: Float,
    val scoreMultiplierBonus: Float,
    val magnetRadiusBonus: Float,
    val perkName: String,
    val perkDescription: String,
    val primaryColorHex: Long,
    val accentColorHex: Long,
    val primaryMaterial: Material,
    val accentMaterial: Material
) {
    companion object {
        val VECTOR_GLIDE = HoverboardData(
            id = "vector_glide",
            name = "Vector Glide",
            modelCode = "VG-01",
            description = "Standard-issue agile cyber deck with dual cyan plasma thrusters.",
            rarity = HoverboardRarity.CORE,
            costCredits = 0, // Lowest Value: Unlocked by default from start
            durationSeconds = 12.0f,
            scoreMultiplierBonus = 1.0f,
            magnetRadiusBonus = 0f,
            perkName = "Crash Barrier",
            perkDescription = "Absorbs 1 fatal obstacle impact before detonating safely.",
            primaryColorHex = 0xFF00F0FF, // Cyan
            accentColorHex = 0xFFFFFFFF,
            primaryMaterial = Material.POWERUP_HOVERBOARD_CYAN,
            accentMaterial = Material.POWERUP_HOVERBOARD_MAGENTA
        )

        val PULSE_STRIKER = HoverboardData(
            id = "pulse_striker",
            name = "Pulse Striker",
            modelCode = "PS-09",
            description = "Aerodynamic emerald cruiser with reinforced kinetic stabilizers.",
            rarity = HoverboardRarity.RARE,
            costCredits = 1500, // Value: 1,500 Credits
            durationSeconds = 14.0f,
            scoreMultiplierBonus = 1.15f,
            magnetRadiusBonus = 4.5f,
            perkName = "Ion Attraction",
            perkDescription = "+15% score bonus & passive 4.5m magnet aura while surfing.",
            primaryColorHex = 0xFF00FF88, // Neon Green
            accentColorHex = 0xFF00F0FF,
            primaryMaterial = Material(0.0f, 1.0f, 0.55f, 1f, 0.95f),
            accentMaterial = Material(0.0f, 0.95f, 1.0f, 1f, 0.95f)
        )

        val VORTEX_MIRAGE = HoverboardData(
            id = "vortex_mirage",
            name = "Vortex Mirage",
            modelCode = "VM-X4",
            description = "High-octane plasma deck leaving a blinding magenta wake trail.",
            rarity = HoverboardRarity.RARE,
            costCredits = 4000, // Value: 4,000 Credits
            durationSeconds = 16.0f,
            scoreMultiplierBonus = 1.30f,
            magnetRadiusBonus = 6.0f,
            perkName = "Plasma Surge",
            perkDescription = "+30% score multiplier & extended 16s ride duration.",
            primaryColorHex = 0xFFFF007F, // Neon Magenta
            accentColorHex = 0xFFFFD700,
            primaryMaterial = Material(1.0f, 0.0f, 0.55f, 1f, 0.95f),
            accentMaterial = Material(1.0f, 0.84f, 0.0f, 1f, 0.95f)
        )

        val APEX_PHANTOM = HoverboardData(
            id = "apex_phantom",
            name = "Apex Phantom",
            modelCode = "AP-99",
            description = "Military stealth hovercraft with dual antimatter warp turbines.",
            rarity = HoverboardRarity.EPIC,
            costCredits = 8500, // Value: 8,500 Credits
            durationSeconds = 18.0f,
            scoreMultiplierBonus = 1.50f,
            magnetRadiusBonus = 8.5f,
            perkName = "Warp Dynamo",
            perkDescription = "18s duration, +50% score boost & disintegrates nearby hazards upon impact.",
            primaryColorHex = 0xFFB026FF, // Neon Violet
            accentColorHex = 0xFF00F0FF,
            primaryMaterial = Material(0.70f, 0.15f, 1.0f, 1f, 0.95f),
            accentMaterial = Material(0.0f, 0.95f, 1.0f, 1f, 0.95f)
        )

        val HYPERION_PRIME = HoverboardData(
            id = "hyperion_prime",
            name = "Hyperion Prime",
            modelCode = "HP-OMEGA",
            description = "The pinnacle of Cyber Vault tech. Golden solar plasma deck with omni-siphon field.",
            rarity = HoverboardRarity.LEGENDARY,
            costCredits = 16000, // Highest Value: 16,000 Credits
            durationSeconds = 22.0f,
            scoreMultiplierBonus = 2.0f,
            magnetRadiusBonus = 12.0f,
            perkName = "Solar Singularity",
            perkDescription = "Huge 22s duration, +2.0x Double Score, and powerful 12m magnetic vacuum.",
            primaryColorHex = 0xFFFFD700, // Neon Gold
            accentColorHex = 0xFFFF007F,
            primaryMaterial = Material(1.0f, 0.85f, 0.0f, 1f, 0.95f),
            accentMaterial = Material(1.0f, 0.0f, 0.55f, 1f, 0.95f)
        )

        val ALL = listOf(VECTOR_GLIDE, PULSE_STRIKER, VORTEX_MIRAGE, APEX_PHANTOM, HYPERION_PRIME)

        fun findById(id: String): HoverboardData = ALL.find { it.id == id } ?: VECTOR_GLIDE
    }
}
