package com.example.longrunner.game.player

import com.example.longrunner.game.graphics.Material

enum class SilhouetteType {
    BALANCED,
    AERODYNAMIC,
    HEAVY,
    MYSTIC,
    ACROBATIC
}

enum class CharacterRarity {
    CORE,
    RARE,
    EPIC,
    LEGENDARY
}

/**
 * Data-driven character configuration system.
 * Every runner features distinct physical attributes, abilities, passives,
 * unlock milestones, and procedural 3D silhouettes.
 */
data class CharacterData(
    val id: String,
    val name: String,
    val role: String,
    val personality: String,
    val description: String,
    val rarity: CharacterRarity = CharacterRarity.CORE,
    val speedModifier: Float = 1.0f,
    val jumpModifier: Float = 1.0f,
    val slideModifier: Float = 1.0f,
    val phaseModifier: Float = 1.0f,
    val laneSwitchModifier: Float = 1.0f,
    val collisionTolerance: Float = 0.08f,
    val abilityName: String = "Quick Phase",
    val abilityDescription: String = "Activates a short intangible phase burst.",
    val abilityCooldown: Float = 14.0f,
    val abilityDuration: Float = 3.0f,
    val passiveName: String = "Steady Stride",
    val passiveDescription: String = "Balanced acceleration with zero stat penalties.",
    val unlockCostCredits: Int = 0,
    val unlockRequirementText: String = "Available from start",
    val primaryColor: Material = Material(0.12f, 0.14f, 0.22f, 1f, 0.1f),
    val accentColor: Material = Material(0.0f, 1.0f, 0.9f, 1f, 0.95f),
    val uiColorHex: Long = 0xFF00F0FF,
    val silhouetteType: SilhouetteType = SilhouetteType.BALANCED,
    val modelAssetPath: String? = "models/runner3.glb"
) {
    companion object {
        val RUNNER = CharacterData(
            id = "runner",
            name = "Runner",
            role = "Subway Surfer",
            personality = "Dynamic, unstoppable runner navigating subway tracks and dodging trains.",
            description = "High-agility subway runner with realistic mocap running, jumping, and sliding animations.",
            rarity = CharacterRarity.LEGENDARY,
            speedModifier = 1.0f,
            jumpModifier = 1.0f,
            slideModifier = 1.0f,
            phaseModifier = 1.0f,
            laneSwitchModifier = 1.0f,
            collisionTolerance = 0.08f,
            abilityName = "Super Sneakers",
            abilityDescription = "Leap high over trains and barriers with spring-loaded bounce.",
            abilityCooldown = 14.0f,
            abilityDuration = 4.0f,
            passiveName = "Subway Agility",
            passiveDescription = "Rock-solid baseline handling with balanced recovery across all hazards.",
            unlockCostCredits = 0,
            unlockRequirementText = "Unlocked by default",
            primaryColor = Material(0.95f, 0.40f, 0.15f, 1f, 0.1f),
            accentColor = Material(0.0f, 0.95f, 1.0f, 1f, 0.95f),
            uiColorHex = 0xFFFF7700,
            silhouetteType = SilhouetteType.BALANCED,
            modelAssetPath = "models/runner3.glb"
        )

        // Compatibility aliases redirecting to the active runner
        val JAKE: CharacterData get() = RUNNER
        val KAI: CharacterData get() = RUNNER
        val ZARA: CharacterData get() = RUNNER
        val JAX: CharacterData get() = RUNNER
        val NOVA: CharacterData get() = RUNNER
        val MIRA: CharacterData get() = RUNNER

        val ALL = listOf(RUNNER)

        fun findById(id: String): CharacterData = ALL.find { it.id == id } ?: RUNNER
    }
}
