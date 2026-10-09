package com.example.longrunner.game.core

import android.content.Context
import com.example.longrunner.game.audio.MusicManager
import com.example.longrunner.game.audio.SoundManager
import com.example.longrunner.game.collectibles.CollectibleType
import com.example.longrunner.game.collision.CollisionSystem
import com.example.longrunner.game.collision.NearMissTracker
import com.example.longrunner.game.graphics.ParticleSystem
import com.example.longrunner.game.graphics.PerformanceProfile
import com.example.longrunner.game.obstacles.ObstacleType
import com.example.longrunner.game.player.CharacterData
import com.example.longrunner.game.player.CharacterManager
import com.example.longrunner.game.player.PlayerController
import com.example.longrunner.game.powerups.PowerUpManager
import com.example.longrunner.game.powerups.PowerUpType
import com.example.longrunner.game.score.ComboManager
import com.example.longrunner.game.progression.AchievementId
import com.example.longrunner.game.progression.AchievementManager
import com.example.longrunner.game.progression.MissionManager
import com.example.longrunner.game.progression.MissionType
import com.example.longrunner.game.progression.ProgressionManager
import com.example.longrunner.game.score.ScoreManager
import com.example.longrunner.game.track.TrackGenerator
import com.example.longrunner.game.world.NullChaser
import com.example.longrunner.game.world.NullTensionLevel
import com.example.longrunner.game.world.WorldEventManager
import com.example.longrunner.game.world.WorldEventType
import kotlin.math.abs

class GameEngine(val context: Context? = null) {

    val characterManager = CharacterManager(context)
    val player = PlayerController(characterManager.getActiveCharacter())
    val biomeManager = com.example.longrunner.game.world.biomes.BiomeManager()
    val trackGenerator = TrackGenerator(biomeManager = biomeManager)
    val collisionSystem = CollisionSystem()
    val scoreManager = ScoreManager(context)
    val soundManager = SoundManager(context)
    val musicManager = MusicManager(context)
    val particleSystem = ParticleSystem()
    val comboManager = ComboManager()
    val nearMissTracker = NearMissTracker()
    val phaseEnergyManager = PhaseEnergyManager()
    val powerUpManager = PowerUpManager(context)
    val hoverboardManager = com.example.longrunner.game.powerups.HoverboardManager(context)
    val worldEventManager = WorldEventManager()
    val nullChaser = NullChaser()
    val missionManager = MissionManager(context)
    val progressionManager = ProgressionManager(context)
    val achievementManager = AchievementManager(context)
    val settingsManager = com.example.longrunner.game.settings.SettingsManager(context)

    var missionBannerMessage: String = ""
        private set
    var missionBannerTimer: Float = 0f

    var achievementBannerMessage: String = ""
        private set
    var achievementBannerTimer: Float = 0f

    var currentFps: Int = 60
        private set
    var currentFrameTimeMs: Float = 16.6f
        private set

    fun updateTelemetry(fps: Int, frameTimeMs: Float) {
        currentFps = fps
        currentFrameTimeMs = frameTimeMs
    }

    private var wasNullCriticalInRun: Boolean = false
    private var overdriveSmashesCount: Int = 0

    val fractureManager: com.example.longrunner.game.track.FractureManager
        get() = trackGenerator.fractureManager

    var state: GameState = GameState.READY
        private set

    var onStatsUpdated: ((GameStats) -> Unit)? = null

    init {
        // Synchronize initial audio settings
        soundManager.sfxVolume = settingsManager.sfxVolume
        soundManager.musicVolume = settingsManager.musicVolume
        soundManager.isEnabled = settingsManager.isSfxEnabled

        musicManager.musicVolume = settingsManager.musicVolume
        musicManager.isEnabled = settingsManager.isMusicEnabled

        particleSystem.performanceProfile = PerformanceProfile.fromQuality(settingsManager.graphicsQuality)

        settingsManager.onSettingsChanged = {
            soundManager.sfxVolume = settingsManager.sfxVolume
            soundManager.musicVolume = settingsManager.musicVolume
            soundManager.isEnabled = settingsManager.isSfxEnabled

            musicManager.musicVolume = settingsManager.musicVolume
            musicManager.isEnabled = settingsManager.isMusicEnabled

            particleSystem.performanceProfile = PerformanceProfile.fromQuality(settingsManager.graphicsQuality)
            notifyStats()
        }

        fractureManager.onFractureWarningTriggered = {
            soundManager.playFractureWarning()
            notifyStats()
        }
        fractureManager.onFractureEntered = { _, _ ->
            notifyStats()
        }
        fractureManager.onFractureCleared = { _, bonus ->
            scoreManager.addBonusScore(bonus)
            comboManager.registerAction()
            soundManager.playFractureClear()
            missionManager.addProgress(MissionType.SURVIVE_FRACTURES, 1)
            achievementManager.unlock(AchievementId.FRACTURE_PIONEER)
            notifyStats()
        }

        powerUpManager.onPowerUpActivated = { _ ->
            notifyStats()
        }
        powerUpManager.onPowerUpExpired = { _ ->
            notifyStats()
        }
        powerUpManager.onShieldBroken = {
            soundManager.playShieldBreak()
            notifyStats()
        }
        powerUpManager.onHoverboardBroken = {
            soundManager.playShieldBreak()
            notifyStats()
        }

        hoverboardManager.onHoverboardChanged = {
            notifyStats()
        }

        worldEventManager.onEventWarning = { event ->
            when (event) {
                WorldEventType.DRONE_SWARM -> soundManager.playDroneAlarm()
                WorldEventType.REALITY_FRACTURE -> soundManager.playRealityGlitch()
                WorldEventType.NONE -> {}
            }
            notifyStats()
        }
        worldEventManager.onEventStarted = { _ ->
            notifyStats()
        }
        worldEventManager.onEventEnded = { _ ->
            notifyStats()
        }

        nullChaser.onHeartbeatPulse = {
            soundManager.playNullHeartbeat()
        }
        nullChaser.onNullSurge = {
            soundManager.playNullSurge()
            notifyStats()
        }
        nullChaser.onPlayerCaught = {
            soundManager.playNullCatch()
            onGameOver()
        }

        missionManager.onMissionCompleted = { mission ->
            soundManager.playMissionComplete()
            progressionManager.addXp(mission.xpReward)
            characterManager.addBankCredits(mission.creditReward)
            missionBannerMessage = "MISSION: ${mission.title} (+${mission.xpReward} XP, +${mission.creditReward} C)"
            missionBannerTimer = 3.5f
            notifyStats()
        }

        progressionManager.onRankUp = { rank, title, bounty ->
            soundManager.playRankUp()
            characterManager.addBankCredits(bounty)
            achievementBannerMessage = "PROMOTED TO RANK $rank: $title (+${bounty} C)!"
            achievementBannerTimer = 4.0f
            notifyStats()
        }

        achievementManager.onAchievementUnlocked = { ach ->
            soundManager.playUnlock()
            progressionManager.addXp(ach.xpReward)
            characterManager.addBankCredits(ach.creditReward)
            achievementBannerMessage = "ACHIEVEMENT: ${ach.title} (+${ach.xpReward} XP, +${ach.creditReward} C)"
            achievementBannerTimer = 4.0f
            notifyStats()
        }

        applyCharacterConfiguration(characterManager.getActiveCharacter())
        reset()
    }

    fun applyCharacterConfiguration(character: CharacterData) {
        player.setCharacter(character)
        comboManager.maxComboDuration = if (character.id == CharacterData.MIRA.id) 5.0f else 3.5f
    }

    fun selectCharacter(characterId: String): Boolean {
        if (characterManager.selectCharacter(characterId)) {
            val char = characterManager.getActiveCharacter()
            applyCharacterConfiguration(char)
            soundManager.playUnlock()
            notifyStats()
            return true
        }
        return false
    }

    fun unlockCharacterWithCredits(characterId: String): Boolean {
        if (characterManager.unlockWithCredits(characterId)) {
            soundManager.playUnlock()
            notifyStats()
            return true
        }
        return false
    }

    fun upgradePowerUp(type: PowerUpType): Boolean {
        val cost = powerUpManager.getUpgradeCost(type)
        if (cost in 1..characterManager.totalBankCredits) {
            val spent = powerUpManager.upgradePowerUp(type, characterManager.totalBankCredits)
            if (spent > 0) {
                characterManager.deductBankCredits(spent)
                soundManager.playUnlock()
                notifyStats()
                return true
            }
        }
        return false
    }

    fun reset() {
        state = GameState.READY
        player.reset()
        trackGenerator.reset()
        biomeManager.reset()
        scoreManager.reset()
        comboManager.reset()
        nearMissTracker.reset()
        phaseEnergyManager.reset()
        powerUpManager.resetRun()
        worldEventManager.reset()
        nullChaser.reset()
        particleSystem.reset()
        applyCharacterConfiguration(characterManager.getActiveCharacter())
        notifyStats()
    }

    fun startRun() {
        if (state == GameState.READY || state == GameState.GAME_OVER) {
            reset()
            state = GameState.RUNNING
            musicManager.start()
            missionManager.onRunStarted()
            wasNullCriticalInRun = false
            overdriveSmashesCount = 0
            missionBannerMessage = ""
            achievementBannerMessage = ""
            notifyStats()
        }
    }

    fun pauseRun() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED
            musicManager.pause()
            notifyStats()
        }
    }

    fun resumeRun() {
        if (state == GameState.PAUSED) {
            state = GameState.RUNNING
            musicManager.resume()
            notifyStats()
        }
    }

    fun restartRun() {
        reset()
        state = GameState.RUNNING
        musicManager.start()
        missionManager.onRunStarted()
        wasNullCriticalInRun = false
        overdriveSmashesCount = 0
        missionBannerMessage = ""
        achievementBannerMessage = ""
        notifyStats()
    }

    fun returnToHome() {
        musicManager.stop()
        reset()
    }

    fun claimMissionReward(missionId: String): Boolean {
        val reward = missionManager.claimReward(missionId) ?: return false
        progressionManager.addXp(reward.first)
        characterManager.addBankCredits(reward.second)
        soundManager.playUnlock()
        notifyStats()
        return true
    }

    fun claimAllMissions(): Pair<Int, Int> {
        val rewards = missionManager.claimAllCompleted()
        if (rewards.first > 0 || rewards.second > 0) {
            progressionManager.addXp(rewards.first)
            characterManager.addBankCredits(rewards.second)
            soundManager.playUnlock()
            notifyStats()
        }
        return rewards
    }

    fun playUiClick() {
        soundManager.playUiClick()
    }

    fun togglePhaseShift(): Boolean {
        if (state != GameState.RUNNING) return false
        val wasActive = phaseEnergyManager.isPhaseShiftActive
        val res = phaseEnergyManager.togglePhaseShift()
        if (!wasActive && res) {
            soundManager.playPhaseShiftEnter()
            nullChaser.onPhaseShiftRepel()
            missionManager.addProgress(MissionType.ACTIVATE_PHASE_SHIFT, 1)
            missionManager.addProgress(MissionType.REPEL_NULL, 1)
        } else if (wasActive && !res) {
            soundManager.playPhaseShiftExit()
        }
        notifyStats()
        return res
    }

    fun activateAbility(): Boolean {
        if (state != GameState.RUNNING) return false
        if (!player.activateAbility()) return false

        soundManager.playAbility()
        missionManager.addProgress(MissionType.ACTIVATE_ABILITY, 1)

        // Character-specific ability execution
        when (player.characterData.id) {
            CharacterData.KAI.id -> {
                // Kai: Quick Phase triggers immediate short phase shift and energy replenishment
                phaseEnergyManager.addEnergy(35.0f)
            }
            CharacterData.JAX.id -> {
                // Jax: Impact Wave destroys all obstacles within 16m ahead
                for (segment in trackGenerator.segments) {
                    for (obs in segment.obstacles) {
                        if (obs.isActive && obs.z < player.z && obs.z > player.z - 16.0f) {
                            obs.isActive = false
                            particleSystem.emitObstacleSmash(obs.currentX, obs.currentY, obs.z)
                        }
                    }
                }
                scoreManager.addBonusScore(500L)
            }
            CharacterData.NOVA.id -> {
                // Nova: Overcharge immediately vacuums all items within 22m & adds Phase Energy
                phaseEnergyManager.addEnergy(50.0f)
                for (segment in trackGenerator.segments) {
                    for (col in segment.collectibles) {
                        if (col.isActive && col.z < player.z + 4f && col.z > player.z - 22.0f) {
                            col.attractTowards(player.x, player.y + 0.8f, player.z, pullSpeed = 42.0f, dt = 0.5f)
                        }
                    }
                }
            }
        }

        notifyStats()
        return true
    }

    fun update(dt: Float) {
        if (state != GameState.RUNNING) return

        // Time dilation: Time Brake, Mira's Time Slip, or Near-miss
        nearMissTracker.update(dt)
        val isMiraTimeSlip = player.characterData.id == CharacterData.MIRA.id && player.isAbilityActive
        val isTimeBrake = powerUpManager.isTimeBrakeActive
        val timeDilation = when {
            isTimeBrake -> 0.50f
            isMiraTimeSlip -> 0.50f
            nearMissTracker.isSlowMotionActive -> 0.65f
            else -> 1.0f
        }
        val effectiveDt = dt * timeDilation

        // Freeze combo timer during Mira's Time Slip
        comboManager.isFrozen = isMiraTimeSlip

        // Update active power-up timers
        powerUpManager.update(effectiveDt)

        // Phase Battery keeps Phase Energy topped up & active
        if (powerUpManager.isPhaseBatteryActive) {
            phaseEnergyManager.addEnergy(100.0f)
            if (!phaseEnergyManager.isPhaseShiftActive) {
                phaseEnergyManager.activatePhaseShift()
            }
        }

        // Update Phase Energy & Reality frequency
        val wasPhaseActive = phaseEnergyManager.isPhaseShiftActive
        phaseEnergyManager.update(effectiveDt, player.characterData.phaseModifier)
        if (wasPhaseActive && !phaseEnergyManager.isPhaseShiftActive) {
            soundManager.playPhaseShiftExit()
        }

        // Update player kinematics
        player.update(effectiveDt)

        // Update procedural track
        trackGenerator.update(player, effectiveDt)

        // Update dynamic world events (Drone Swarms, Glitches)
        worldEventManager.update(player.z, effectiveDt)

        // Update The Null pursuit entity
        val caughtByNull = nullChaser.update(player.speed, comboManager.currentCombo, effectiveDt)
        if (caughtByNull) {
            soundManager.playNullCatch()
            onGameOver()
            return
        }

        // Check for obstacle collision with Phase Shift permeability consideration
        val hitObstacle = collisionSystem.checkObstacleCollision(
            player = player,
            trackGenerator = trackGenerator,
            isPhaseShiftActive = phaseEnergyManager.isPhaseShiftActive
        )
        if (hitObstacle != null) {
            val isKaiPhasing = player.characterData.id == CharacterData.KAI.id && player.isAbilityActive
            val hasZaraGhostStep = player.characterData.id == CharacterData.ZARA.id && player.hasGhostStepCharge
            val isJaxSmashingCrate = player.characterData.id == CharacterData.JAX.id && hitObstacle.type == ObstacleType.BREAKABLE_CRATE

            when {
                powerUpManager.isOverdriveActive -> {
                    // Overdrive: Plows effortlessly through all obstacles & repels Null
                    hitObstacle.isActive = false
                    soundManager.playCrash()
                    particleSystem.emitObstacleSmash(hitObstacle.currentX, hitObstacle.currentY, hitObstacle.z)
                    scoreManager.addBonusScore(300L)
                    comboManager.registerAction()
                    nullChaser.onPowerUpBlast()
                    overdriveSmashesCount++
                    if (overdriveSmashesCount >= 5) {
                        achievementManager.unlock(AchievementId.OVERDRIVE_RAMPAGE)
                    }
                }
                powerUpManager.isInvulnerable -> {
                    // Grace invulnerability period (e.g. after shield break or hoverboard crash)
                    hitObstacle.isActive = false
                }
                powerUpManager.isHoverboardActive -> {
                    // Neon Hoverboard: Absorbs collision, detonates, saves player from crash!
                    powerUpManager.absorbHoverboardCollision()
                    hitObstacle.isActive = false
                    soundManager.playCrash()
                    soundManager.playShieldBreak()
                    particleSystem.emitObstacleSmash(hitObstacle.currentX, hitObstacle.currentY, hitObstacle.z)
                    particleSystem.emitNearMissShockwave(player.x, player.y, player.z)
                    scoreManager.addBonusScore(250L)
                    comboManager.registerAction()
                    nullChaser.onPlayerStumble()
                }
                powerUpManager.hasShield -> {
                    // Kinetic Shield: Absorbs impact, shatters, gives i-frames, and causes Null stumble surge
                    powerUpManager.absorbCollision()
                    hitObstacle.isActive = false
                    particleSystem.emitObstacleSmash(hitObstacle.currentX, hitObstacle.currentY, hitObstacle.z)
                    scoreManager.addBonusScore(100L)
                    comboManager.registerAction()
                    nullChaser.onPlayerStumble()
                }
                isKaiPhasing -> {
                    // Kai: Invulnerable during Quick Phase
                    scoreManager.addBonusScore(100L)
                }
                hasZaraGhostStep -> {
                    // Zara: Consume Ghost Step charge to bypass 1 obstacle
                    player.hasGhostStepCharge = false
                    soundManager.playAbility()
                    scoreManager.addBonusScore(200L)
                }
                isJaxSmashingCrate -> {
                    // Jax: Smashes breakable crate on contact
                    hitObstacle.isActive = false
                    player.incrementCratesSmashed()
                    soundManager.playCrash()
                    particleSystem.emitObstacleSmash(hitObstacle.currentX, hitObstacle.currentY, hitObstacle.z)
                    scoreManager.addBonusScore(200L)
                    comboManager.registerAction()
                    missionManager.addProgress(MissionType.SMASH_CRATES, 1)
                }
                else -> {
                    onGameOver()
                    return
                }
            }
        }

        // Check for near-miss narrowly avoiding an obstacle
        if (nearMissTracker.checkNearMiss(player, trackGenerator)) {
            val nearMissBonus = if (player.characterData.id == CharacterData.MIRA.id) 312L else 250L
            scoreManager.addNearMiss()
            if (player.characterData.id == CharacterData.MIRA.id) {
                scoreManager.addBonusScore(62L) // Mira +25% bonus
            }
            comboManager.registerAction()
            soundManager.playNearMiss()
            particleSystem.emitNearMissShockwave(player.x, player.y, player.z)
            missionManager.addProgress(MissionType.NEAR_MISS_COUNT, 1)
        }

        // Magnet attraction physics (active with Quantum Magnet, Overdrive, Nova overcharge, or equipped Hoverboard perks)
        val isMagnet = powerUpManager.isMagnetActive
        val isOverdrive = powerUpManager.isOverdriveActive
        val isNova = player.characterData.id == CharacterData.NOVA.id
        val isNovaOvercharging = isNova && player.isAbilityActive
        val equippedBoard = hoverboardManager.getEquippedHoverboard()
        val isBoardActive = powerUpManager.isHoverboardActive
        val boardMagnetRadius = if (isBoardActive) equippedBoard.magnetRadiusBonus else 0f

        val magnetRadius = when {
            isMagnet -> 24.0f
            isOverdrive -> 28.0f
            isNovaOvercharging -> 18.0f
            boardMagnetRadius > 0f -> boardMagnetRadius
            else -> 0.0f
        }
        val magnetSpeed = when {
            isMagnet -> 35.0f
            isOverdrive -> 45.0f
            isNovaOvercharging -> 30.0f
            boardMagnetRadius > 0f -> 26.0f
            else -> 0.0f
        }

        if (magnetRadius > 0.0f) {
            for (segment in trackGenerator.segments) {
                for (col in segment.collectibles) {
                    if (col.isActive) {
                        val zDist = abs(col.currentZ - player.z)
                        if (zDist <= magnetRadius) {
                            col.attractTowards(player.x, player.y + 0.8f, player.z, pullSpeed = magnetSpeed, dt = effectiveDt)
                        }
                    }
                }
            }
        }

        // Pickups (respects phase-exclusive items & powerups)
        collisionSystem.checkCollectiblePickups(
            player = player,
            trackGenerator = trackGenerator,
            isPhaseShiftActive = phaseEnergyManager.isPhaseShiftActive
        ) { item ->
            comboManager.registerAction()
            nullChaser.onEnergyCollected()
            when (item.type) {
                CollectibleType.ENERGY_SHARD -> {
                    scoreManager.addShard()
                    phaseEnergyManager.addEnergy(4.0f, player.characterData.phaseModifier)
                    missionManager.addProgress(MissionType.COLLECT_SHARDS, 1)
                    soundManager.playCollect()
                    particleSystem.emitCollectibleBurst(item.currentX, item.currentY, item.currentZ, 0.0f, 0.95f, 1.0f)
                }
                CollectibleType.PHASE_CORE -> {
                    scoreManager.addPhaseCore()
                    phaseEnergyManager.addEnergy(35.0f, player.characterData.phaseModifier)
                    missionManager.addProgress(MissionType.COLLECT_CORES, 1)
                    soundManager.playCore()
                    particleSystem.emitCollectibleBurst(item.currentX, item.currentY, item.currentZ, 0.95f, 0.1f, 0.95f)
                }
                CollectibleType.CREDIT -> {
                    scoreManager.addCredit()
                    missionManager.addProgress(MissionType.COLLECT_CREDITS, 1)
                    soundManager.playCredit()
                    particleSystem.emitCollectibleBurst(item.currentX, item.currentY, item.currentZ, 1.0f, 0.85f, 0.1f)
                }
                CollectibleType.MULTIPLIER_TOKEN -> {
                    scoreManager.addMultiplierToken()
                    powerUpManager.activatePowerUp(PowerUpType.SCORE_AMPLIFIER)
                    soundManager.playBoost()
                    particleSystem.emitCollectibleBurst(item.currentX, item.currentY, item.currentZ, 0.2f, 0.95f, 0.4f)
                }
                CollectibleType.SHIELD_ORB -> {
                    powerUpManager.activatePowerUp(PowerUpType.KINETIC_SHIELD)
                    soundManager.playShieldPickup()
                    scoreManager.addBonusScore(150L)
                    nullChaser.onPowerUpBlast()
                    missionManager.addProgress(MissionType.REPEL_NULL, 1)
                    particleSystem.emitCollectibleBurst(item.currentX, item.currentY, item.currentZ, 0.3f, 0.6f, 1.0f)
                }
                CollectibleType.OVERDRIVE_ORB -> {
                    powerUpManager.activatePowerUp(PowerUpType.OVERDRIVE)
                    soundManager.playOverdrive()
                    scoreManager.addBonusScore(200L)
                    nullChaser.onPowerUpBlast()
                    missionManager.addProgress(MissionType.REPEL_NULL, 1)
                    particleSystem.emitCollectibleBurst(item.currentX, item.currentY, item.currentZ, 1.0f, 0.4f, 0.1f)
                }
                CollectibleType.PHASE_BATTERY_ORB -> {
                    powerUpManager.activatePowerUp(PowerUpType.PHASE_BATTERY)
                    soundManager.playCore()
                    scoreManager.addBonusScore(150L)
                    particleSystem.emitCollectibleBurst(item.currentX, item.currentY, item.currentZ, 0.8f, 0.2f, 1.0f)
                }
                CollectibleType.TIME_BRAKE_ORB -> {
                    powerUpManager.activatePowerUp(PowerUpType.TIME_BRAKE)
                    soundManager.playTimeBrake()
                    scoreManager.addBonusScore(150L)
                    particleSystem.emitCollectibleBurst(item.currentX, item.currentY, item.currentZ, 0.2f, 0.95f, 0.9f)
                }
                CollectibleType.MAGNET_ORB -> {
                    powerUpManager.activatePowerUp(PowerUpType.MAGNET)
                    soundManager.playBoost()
                    scoreManager.addBonusScore(150L)
                    nullChaser.onPowerUpBlast()
                    missionManager.addProgress(MissionType.REPEL_NULL, 1)
                    particleSystem.emitCollectibleBurst(item.currentX, item.currentY, item.currentZ, 0.0f, 0.9f, 1.0f)
                }
                CollectibleType.HOVERBOARD_ORB -> {
                    powerUpManager.activatePowerUp(PowerUpType.HOVERBOARD)
                    soundManager.playBoost()
                    scoreManager.addBonusScore(200L)
                    nullChaser.onPowerUpBlast()
                    missionManager.addProgress(MissionType.REPEL_NULL, 1)
                    particleSystem.emitCollectibleBurst(item.currentX, item.currentY, item.currentZ, 0.0f, 0.95f, 1.0f)
                }
            }
        }

        // Update combo decay
        comboManager.update(effectiveDt)
        missionManager.setPeakProgress(MissionType.COMBO_TARGET, comboManager.currentCombo)

        // Particle system simulation & runner emission
        particleSystem.update(effectiveDt)
        if (player.isGrounded && player.speed > 8f) {
            particleSystem.emitFootstepSpark(player.x, player.y, player.z, player.characterData.uiColorHex)
        }
        if (player.speed >= 17f) {
            particleSystem.emitSpeedStreak(player.z + GameConstants.CAMERA_FOLLOW_DISTANCE, player.speed)
        }

        // Update score and distance metrics (Score Amplifier provides +3x multiplier surge, Hoverboards grant tier perks)
        missionManager.setPeakProgress(MissionType.DISTANCE_SINGLE_RUN, scoreManager.distance.toInt())
        val boardScoreBonus = if (isBoardActive && equippedBoard.scoreMultiplierBonus > 1.0f) {
            ((equippedBoard.scoreMultiplierBonus - 1.0f) * 4).toInt().coerceAtLeast(1)
        } else 0
        scoreManager.update(
            player.z,
            player.speed,
            comboManager.currentCombo,
            effectiveDt,
            extraMultiplier = (if (powerUpManager.isScoreAmplifierActive) 3 else 0) + progressionManager.permanentMultiplierBonus + boardScoreBonus
        )

        // Update active dynamic biome environment and atmospheric transition
        biomeManager.update(scoreManager.distance, effectiveDt)

        // Evaluate Void Dodger achievement (escaped critical back to safe)
        if (nullChaser.isCritical) {
            wasNullCriticalInRun = true
        } else if (wasNullCriticalInRun && nullChaser.tensionLevel == NullTensionLevel.SAFE) {
            achievementManager.unlock(AchievementId.VOID_DODGER)
        }

        // Decay in-game mission and achievement banners
        if (missionBannerTimer > 0f) {
            missionBannerTimer -= effectiveDt
            if (missionBannerTimer <= 0f) missionBannerMessage = ""
        }
        if (achievementBannerTimer > 0f) {
            achievementBannerTimer -= effectiveDt
            if (achievementBannerTimer <= 0f) achievementBannerMessage = ""
        }

        notifyStats()
    }

    private fun onGameOver() {
        state = GameState.GAME_OVER
        musicManager.pause()
        phaseEnergyManager.deactivatePhaseShift()
        soundManager.playCrash()
        scoreManager.persistHighScore()

        // Bank collected credits into persistent player balance
        characterManager.addBankCredits(scoreManager.creditsCollected)

        // Distribute run XP and update missions & achievements
        missionManager.addProgress(MissionType.DISTANCE_TOTAL, scoreManager.distance.toInt())
        val runXp = (scoreManager.distance / 8.0f).toInt() + scoreManager.shardsCollected * 2 + scoreManager.creditsCollected * 5
        progressionManager.addXp(runXp)
        achievementManager.checkMetrics(
            distance = scoreManager.distance,
            maxCombo = comboManager.highestCombo,
            nearMisses = scoreManager.nearMissCount,
            vaultCredits = characterManager.totalBankCredits
        )

        // Check milestone unlocks
        val unlocked = characterManager.checkMilestoneUnlocks(
            bestDistance = scoreManager.distance,
            maxCombo = comboManager.highestCombo,
            smashedCrates = player.cratesSmashed
        )
        if (unlocked.isNotEmpty()) {
            soundManager.playUnlock()
        }

        notifyStats()
    }

    fun notifyStats() {
        val maxCd = player.characterData.abilityCooldown
        val cdProgress = if (maxCd > 0f) (player.abilityCooldownTimer / maxCd).coerceIn(0f, 1f) else 0f

        val stats = GameStats(
            score = scoreManager.score,
            distance = scoreManager.distance,
            shards = scoreManager.shardsCollected,
            credits = scoreManager.creditsCollected,
            combo = comboManager.currentCombo,
            comboProgress = comboManager.decayProgress,
            nearMisses = scoreManager.nearMissCount,
            multiplier = scoreManager.multiplier,
            isBoostActive = scoreManager.isMultiplierBoostActive,
            isNearMissActive = nearMissTracker.isSlowMotionActive,
            speed = player.speed,
            highScore = scoreManager.highScore,
            state = state,
            character = player.characterData,
            isAbilityReady = player.isAbilityReady,
            isAbilityActive = player.isAbilityActive,
            abilityCooldownProgress = cdProgress,
            abilityName = player.characterData.abilityName,
            bankCredits = characterManager.totalBankCredits,
            phaseEnergyProgress = phaseEnergyManager.energyProgress,
            isPhaseShiftActive = phaseEnergyManager.isPhaseShiftActive,
            canActivatePhaseShift = phaseEnergyManager.canActivate,
            isFractureWarning = fractureManager.isWarningActive,
            fractureWarningTitle = fractureManager.warningTitle,
            fractureWarningSubtitle = fractureManager.warningSubtitle,
            isInFractureZone = fractureManager.isInFractureZone,
            activeRouteName = fractureManager.activeRouteName,
            fractureClearMessage = fractureManager.clearBannerMessage,
            hasShield = powerUpManager.hasShield,
            shieldProgress = powerUpManager.getProgress(PowerUpType.KINETIC_SHIELD),
            isOverdriveActive = powerUpManager.isOverdriveActive,
            overdriveProgress = powerUpManager.getProgress(PowerUpType.OVERDRIVE),
            isPhaseBatteryActive = powerUpManager.isPhaseBatteryActive,
            phaseBatteryProgress = powerUpManager.getProgress(PowerUpType.PHASE_BATTERY),
            isTimeBrakeActive = powerUpManager.isTimeBrakeActive,
            timeBrakeProgress = powerUpManager.getProgress(PowerUpType.TIME_BRAKE),
            isScoreAmplifierActive = powerUpManager.isScoreAmplifierActive,
            scoreAmplifierProgress = powerUpManager.getProgress(PowerUpType.SCORE_AMPLIFIER),
            isMagnetActive = powerUpManager.isMagnetActive,
            magnetProgress = powerUpManager.getProgress(PowerUpType.MAGNET),
            isHoverboardActive = powerUpManager.isHoverboardActive,
            hoverboardProgress = powerUpManager.getProgress(PowerUpType.HOVERBOARD),
            hoverboardCharges = 99,
            equippedHoverboard = hoverboardManager.getEquippedHoverboard(),
            worldEventType = worldEventManager.currentEvent,
            isWorldEventWarning = worldEventManager.isWarningActive,
            worldEventTitle = worldEventManager.warningTitle,
            worldEventSubtitle = worldEventManager.warningSubtitle,
            worldEventProgress = worldEventManager.eventProgress,
            nullDistance = nullChaser.distanceBehindPlayer,
            nullTension = nullChaser.tension,
            isNullAlert = nullChaser.isAlert,
            isNullCritical = nullChaser.isCritical,
            operativeRank = progressionManager.currentRank,
            rankTitle = progressionManager.rankTitle,
            rankProgress = progressionManager.rankProgress,
            currentRankXp = progressionManager.currentRankXp,
            xpToNextRank = progressionManager.xpToNextRank,
            totalXp = progressionManager.totalXp,
            activeMissions = missionManager.activeMissions,
            totalMissionsCompleted = missionManager.totalMissionsCompleted,
            missionBannerMessage = missionBannerMessage,
            achievementsUnlockedCount = achievementManager.achievements.count { it.isUnlocked },
            totalAchievementsCount = achievementManager.achievements.size,
            achievementBannerMessage = achievementBannerMessage,
            unclaimedMissionsCount = missionManager.activeMissions.count { it.isCompleted && !it.isClaimed },
            fps = currentFps,
            frameTimeMs = currentFrameTimeMs,
            currentBiomeName = biomeManager.currentBiomeName,
            currentBiomeZone = biomeManager.getActiveBiome().zoneNumber,
            biomeBannerMessage = biomeManager.biomeBannerMessage,
            biomeBannerSubtitle = biomeManager.biomeBannerSubtitle,
            biomeAccentColorHex = biomeManager.biomeAccentColorHex
        )
        onStatsUpdated?.invoke(stats)
    }

    // Input actions
    fun onSwipeLeft() {
        if (state == GameState.RUNNING) {
            if (player.moveLeft()) {
                soundManager.playSwipe()
                comboManager.registerAction()
            }
        }
    }

    fun onSwipeRight() {
        if (state == GameState.RUNNING) {
            if (player.moveRight()) {
                soundManager.playSwipe()
                comboManager.registerAction()
            }
        }
    }

    fun onSwipeUp() {
        if (state == GameState.RUNNING) {
            if (player.jump()) {
                soundManager.playJump()
                comboManager.registerAction()
            }
        }
    }

    fun onSwipeDown() {
        if (state == GameState.RUNNING) {
            if (player.slide()) {
                soundManager.playSlide()
                comboManager.registerAction()
            }
        }
    }

    fun activateHoverboard(): Boolean {
        if (state != GameState.RUNNING) return false
        if (powerUpManager.isHoverboardActive) return false

        val equippedBoard = hoverboardManager.getEquippedHoverboard()
        powerUpManager.activateHoverboard(equippedBoard.durationSeconds)
        soundManager.playBoost()
        particleSystem.emitNearMissShockwave(player.x, player.y, player.z)
        notifyStats()
        return true
    }

    fun equipHoverboard(boardId: String): Boolean {
        val ok = hoverboardManager.equipHoverboard(boardId)
        if (ok) {
            soundManager.playUiClick()
            notifyStats()
        }
        return ok
    }

    fun unlockHoverboardWithCredits(boardId: String): Boolean {
        val ok = hoverboardManager.unlockWithCredits(boardId, characterManager)
        if (ok) {
            soundManager.playUnlock()
            notifyStats()
        }
        return ok
    }

    fun onDoubleTap() {
        if (state == GameState.RUNNING) {
            activateHoverboard()
        }
    }

    fun onTap() {
        when (state) {
            GameState.READY -> startRun()
            GameState.RUNNING -> activateAbility()
            else -> {}
        }
    }

    fun release() {
        soundManager.release()
        musicManager.release()
        particleSystem.release()
    }
}
