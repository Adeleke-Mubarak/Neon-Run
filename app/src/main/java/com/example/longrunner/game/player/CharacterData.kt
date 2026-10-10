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
    val modelAssetPath: String? = "models/runner.glb"
) {
    companion object {
        val KAI = CharacterData(
            id = "kai",
            name = "Kai",
            role = "Balanced Runner",
            personality = "Calm, observant, competitive street racer.",
            description = "Grounded urban runner with razor-sharp reflexes and tactical spatial awareness.",
            rarity = CharacterRarity.CORE,
            speedModifier = 1.0f,
            jumpModifier = 1.0f,
            slideModifier = 1.0f,
            phaseModifier = 1.0f,
            laneSwitchModifier = 1.0f,
            collisionTolerance = 0.08f,
            abilityName = "Quick Phase",
            abilityDescription = "Instantly phases into an intangible state for 3s, bypassing all physical obstacles.",
            abilityCooldown = 14.0f,
            abilityDuration = 3.0f,
            passiveName = "Tactical Focus",
            passiveDescription = "Rock-solid baseline handling with balanced recovery across all hazards.",
            unlockCostCredits = 0,
            unlockRequirementText = "Unlocked by default",
            primaryColor = Material(0.10f, 0.14f, 0.24f, 1f, 0.1f),
            accentColor = Material(0.0f, 0.95f, 1.0f, 1f, 0.95f),
            uiColorHex = 0xFF00F0FF,
            silhouetteType = SilhouetteType.BALANCED,
            modelAssetPath = "models/runner.glb"
        )

        val ZARA = CharacterData(
            id = "zara",
            name = "Zara",
            role = "Agile Courier",
            personality = "High-speed daredevil courier from the upper spires.",
            description = "Lightning-fast reflexes allow 30% snappier lane shifts and higher leaps, demanding precision.",
            rarity = CharacterRarity.RARE,
            speedModifier = 1.06f,
            jumpModifier = 1.15f,
            slideModifier = 1.10f,
            phaseModifier = 1.0f,
            laneSwitchModifier = 1.30f,
            collisionTolerance = 0.05f,
            abilityName = "Ghost Step",
            abilityDescription = "Primed intangible dodge that lets you safely phase straight through the next obstacle.",
            abilityCooldown = 12.0f,
            abilityDuration = 6.0f,
            passiveName = "Kinetic Drift",
            passiveDescription = "30% faster lane transition speed allows reactionary last-second dodges.",
            unlockCostCredits = 5000,
            unlockRequirementText = "5,000 Credits or reach 1,000m in one run",
            primaryColor = Material(0.18f, 0.06f, 0.16f, 1f, 0.1f),
            accentColor = Material(1.0f, 0.0f, 0.55f, 1f, 0.95f),
            uiColorHex = 0xFFFF007F,
            silhouetteType = SilhouetteType.AERODYNAMIC,
            modelAssetPath = "models/runner2.glb"
        )

        val JAX = CharacterData(
            id = "jax",
            name = "Jax",
            role = "Power Juggernaut",
            personality = "Ex-demolition cyber-enforcer built for brute force.",
            description = "Reinforced heavy exo-suit smashes through storage crates and resists hazard clipping.",
            rarity = CharacterRarity.EPIC,
            speedModifier = 0.94f,
            jumpModifier = 0.90f,
            slideModifier = 0.95f,
            phaseModifier = 0.85f,
            laneSwitchModifier = 0.90f,
            collisionTolerance = 0.12f,
            abilityName = "Impact Wave",
            abilityDescription = "Discharges a frontal kinetic blast annihilating all obstacles within 16 meters.",
            abilityCooldown = 18.0f,
            abilityDuration = 0.6f,
            passiveName = "Barricade Buster",
            passiveDescription = "Smashes Breakable Crates on direct impact (+200 pts) without suffering damage.",
            unlockCostCredits = 10000,
            unlockRequirementText = "10,000 Credits or smash 10 crates",
            primaryColor = Material(0.22f, 0.15f, 0.08f, 1f, 0.15f),
            accentColor = Material(1.0f, 0.60f, 0.0f, 1f, 0.95f),
            uiColorHex = 0xFFFF9900,
            silhouetteType = SilhouetteType.HEAVY
        )

        val NOVA = CharacterData(
            id = "nova",
            name = "Nova",
            role = "Energy Specialist",
            personality = "Quantum technician attuned to ambient neon frequencies.",
            description = "Ethereal runner capable of pulling energy from afar and supercharging phase capacity.",
            rarity = CharacterRarity.EPIC,
            speedModifier = 1.0f,
            jumpModifier = 1.0f,
            slideModifier = 1.0f,
            phaseModifier = 1.50f,
            laneSwitchModifier = 1.0f,
            collisionTolerance = 0.08f,
            abilityName = "Overcharge",
            abilityDescription = "Triggers a 22m electromagnetic siphon instantly pulling all track collectibles.",
            abilityCooldown = 15.0f,
            abilityDuration = 5.0f,
            passiveName = "Flux Siphon",
            passiveDescription = "Constant 6m passive magnetic aura pulls nearby energy shards and cores.",
            unlockCostCredits = 15000,
            unlockRequirementText = "15,000 Credits or collect 50 Phase Cores",
            primaryColor = Material(0.12f, 0.07f, 0.25f, 1f, 0.12f),
            accentColor = Material(0.75f, 0.20f, 1.0f, 1f, 0.95f),
            uiColorHex = 0xFFB026FF,
            silhouetteType = SilhouetteType.MYSTIC
        )

        val MIRA = CharacterData(
            id = "mira",
            name = "Mira",
            role = "Acrobatic Trickster",
            personality = "Aerialist and free-runner who treats the fractured city as an open playground.",
            description = "Unrivaled aerial hangtime and slide agility. Keeps combo chains active 40% longer.",
            rarity = CharacterRarity.LEGENDARY,
            speedModifier = 1.02f,
            jumpModifier = 1.12f,
            slideModifier = 1.20f,
            phaseModifier = 1.10f,
            laneSwitchModifier = 1.08f,
            collisionTolerance = 0.08f,
            abilityName = "Time Slip",
            abilityDescription = "Dilates temporal flow by 50% for 4.0s without speed penalty, freezing combo decay.",
            abilityCooldown = 16.0f,
            abilityDuration = 4.0f,
            passiveName = "Flow State",
            passiveDescription = "Combo retention window extended to 5.0s (+42%) and +25% bonus score on chained actions.",
            unlockCostCredits = 20000,
            unlockRequirementText = "20,000 Credits or achieve a 10x Combo",
            primaryColor = Material(0.06f, 0.18f, 0.14f, 1f, 0.1f),
            accentColor = Material(0.0f, 1.0f, 0.60f, 1f, 0.95f),
            uiColorHex = 0xFF00FF88,
            silhouetteType = SilhouetteType.ACROBATIC
        )

        val JAKE = CharacterData(
            id = "jake",
            name = "Jake",
            role = "Subway Surfer",
            personality = "Free-spirited graffiti artist and subway runner.",
            description = "Iconic subway surfer with smooth lane transitions and classic street style.",
            rarity = CharacterRarity.LEGENDARY,
            speedModifier = 1.05f,
            jumpModifier = 1.10f,
            slideModifier = 1.05f,
            phaseModifier = 1.0f,
            laneSwitchModifier = 1.15f,
            collisionTolerance = 0.08f,
            abilityName = "Super Sneakers",
            abilityDescription = "Leap high over trains and barriers with spring-loaded bounce.",
            abilityCooldown = 15.0f,
            abilityDuration = 4.0f,
            passiveName = "Subway Legend",
            passiveDescription = "High-agility street runner with smooth rail sliding.",
            unlockCostCredits = 0,
            unlockRequirementText = "Unlocked by default",
            primaryColor = Material(0.12f, 0.40f, 0.85f, 1f, 0.1f),
            accentColor = Material(1.0f, 0.85f, 0.0f, 1f, 0.95f),
            uiColorHex = 0xFFFFCC00,
            silhouetteType = SilhouetteType.BALANCED,
            modelAssetPath = "models/character_endless_runner.glb"
        )

        val ALL = listOf(KAI, JAKE, ZARA, JAX, NOVA, MIRA)

        fun findById(id: String): CharacterData = ALL.find { it.id == id } ?: KAI
    }
}
