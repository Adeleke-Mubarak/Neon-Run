package com.example.longrunner.game.graphics

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.example.longrunner.game.camera.GameCamera
import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.core.GameEngine
import com.example.longrunner.game.player.PlayerVisual
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Renderer3D(
    private val engine: GameEngine,
    val camera: GameCamera = GameCamera()
) : GLSurfaceView.Renderer {

    private var shader: Shader? = null
    private var roadMesh: Mesh? = null
    private var rampUpRoadMesh: Mesh? = null
    private var elevatedRoadMesh: Mesh? = null
    private var rampDownRoadMesh: Mesh? = null
    private var buildingMesh: Mesh? = null
    private var tunnelMesh: Mesh? = null
    private var bridgeMesh: Mesh? = null
    private var gantryMesh: Mesh? = null
    private var solarCanopyMesh: Mesh? = null
    private var fractureSplitDecorMesh: Mesh? = null

    private var lowHurdleMesh: Mesh? = null
    private var highBeamMesh: Mesh? = null
    private var cyberBlockMesh: Mesh? = null
    private var patrolDroneMesh: Mesh? = null
    private var slidingGateMesh: Mesh? = null
    private var breakableCrateMesh: Mesh? = null
    private var fallingDebrisMesh: Mesh? = null

    // Multi-Biome Shifting Environment Meshes
    private var subwayRoadMesh: Mesh? = null
    private var canyonRoadMesh: Mesh? = null
    private var ruinsRoadMesh: Mesh? = null
    private var orbitalRoadMesh: Mesh? = null
    private var subwaySceneryMesh: Mesh? = null
    private var canyonSceneryMesh: Mesh? = null
    private var ruinsSceneryMesh: Mesh? = null
    private var orbitalSceneryMesh: Mesh? = null
    private var transitGatewayMesh: Mesh? = null

    private var shardMesh: Mesh? = null
    private var phaseCoreMesh: Mesh? = null
    private var creditMesh: Mesh? = null
    private var multiplierMesh: Mesh? = null
    private var shieldOrbMesh: Mesh? = null
    private var overdriveOrbMesh: Mesh? = null
    private var phaseBatteryOrbMesh: Mesh? = null
    private var timeBrakeOrbMesh: Mesh? = null
    private var magnetOrbMesh: Mesh? = null
    private var hoverboardOrbMesh: Mesh? = null
    private var nullChaserMesh: Mesh? = null
    private var droneSearchlightMesh: Mesh? = null

    private var playerVisual: PlayerVisual? = null
    private var characterModelRenderer: com.example.longrunner.game.graphics.gltf.CharacterModelRenderer? = null
    private var subwayEnvRenderer: com.example.longrunner.game.graphics.gltf.SubwayEnvironmentRenderer? = null

    private val modelMatrix = Matrix4()
    private val mvpMatrix = Matrix4()

    private var lastFrameTimeNs: Long = 0L

    var currentFps: Int = 60
        private set
    var currentFrameTimeMs: Float = 16.6f
        private set
    private var frameCount: Int = 0
    private var fpsTimerNs: Long = 0L

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Deep cyber night sky clear color
        GLES30.glClearColor(0.04f, 0.03f, 0.08f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glDepthFunc(GLES30.GL_LEQUAL)

        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        // Compile Shader
        shader = Shader(Shader.VERTEX_SHADER_SRC, Shader.FRAGMENT_SHADER_SRC)

        // Generate procedural 3D Meshes for Environments & Fractures
        roadMesh = MeshBuilder.createRoadSegment(GameConstants.TRACK_WIDTH, GameConstants.SEGMENT_LENGTH)
        rampUpRoadMesh = MeshBuilder.createRampRoadMesh(GameConstants.TRACK_WIDTH, GameConstants.SEGMENT_LENGTH, 0.0f, 3.5f)
        elevatedRoadMesh = MeshBuilder.createElevatedRoadMesh(GameConstants.TRACK_WIDTH, GameConstants.SEGMENT_LENGTH, 3.5f)
        rampDownRoadMesh = MeshBuilder.createRampRoadMesh(GameConstants.TRACK_WIDTH, GameConstants.SEGMENT_LENGTH, 3.5f, 0.0f)

        // Multi-Biome Shifting Environment Road & Scenery Meshes
        subwayRoadMesh = MeshBuilder.createSubwayRoadMesh(GameConstants.TRACK_WIDTH, GameConstants.SEGMENT_LENGTH)
        canyonRoadMesh = MeshBuilder.createDesertCanyonRoadMesh(GameConstants.TRACK_WIDTH, GameConstants.SEGMENT_LENGTH)
        ruinsRoadMesh = MeshBuilder.createRuinsViaductRoadMesh(GameConstants.TRACK_WIDTH, GameConstants.SEGMENT_LENGTH)
        orbitalRoadMesh = MeshBuilder.createOrbitalSkydeckRoadMesh(GameConstants.TRACK_WIDTH, GameConstants.SEGMENT_LENGTH)

        subwaySceneryMesh = MeshBuilder.createSubwaySceneryMesh()
        canyonSceneryMesh = MeshBuilder.createDesertCanyonSceneryMesh()
        ruinsSceneryMesh = MeshBuilder.createOvergrownRuinsSceneryMesh()
        orbitalSceneryMesh = MeshBuilder.createOrbitalSkydeckSceneryMesh()
        transitGatewayMesh = MeshBuilder.createTransitGatewayMesh()

        buildingMesh = MeshBuilder.createBuildingScenery()
        tunnelMesh = MeshBuilder.createTunnelRibMesh()
        bridgeMesh = MeshBuilder.createBridgePylonMesh()
        gantryMesh = MeshBuilder.createOverpassGantryMesh()
        solarCanopyMesh = MeshBuilder.createSolarCanopyMesh()
        fractureSplitDecorMesh = MeshBuilder.createFractureSplitDecorMesh()

        // Generate procedural 3D Meshes for Hazards
        lowHurdleMesh = MeshBuilder.createLowHurdle()
        highBeamMesh = MeshBuilder.createHighLaserGate()
        cyberBlockMesh = MeshBuilder.createCyberBlock()
        patrolDroneMesh = MeshBuilder.createPatrolDroneMesh()
        slidingGateMesh = MeshBuilder.createSlidingGateMesh()
        breakableCrateMesh = MeshBuilder.createBreakableCrateMesh()
        fallingDebrisMesh = MeshBuilder.createFallingDebrisMesh()

        // Collectibles & Power-Ups
        shardMesh = MeshBuilder.createEnergyShard()
        phaseCoreMesh = MeshBuilder.createPhaseCoreMesh()
        creditMesh = MeshBuilder.createCreditMesh()
        multiplierMesh = MeshBuilder.createMultiplierTokenMesh()
        shieldOrbMesh = MeshBuilder.createShieldOrbMesh()
        overdriveOrbMesh = MeshBuilder.createOverdriveOrbMesh()
        phaseBatteryOrbMesh = MeshBuilder.createPhaseBatteryOrbMesh()
        timeBrakeOrbMesh = MeshBuilder.createTimeBrakeOrbMesh()
        magnetOrbMesh = MeshBuilder.createMagnetOrbMesh()
        hoverboardOrbMesh = MeshBuilder.createHoverboardOrbMesh()
        nullChaserMesh = MeshBuilder.createNullChaserMesh()
        droneSearchlightMesh = MeshBuilder.createDroneSearchlightConeMesh()

        playerVisual = PlayerVisual(engine.player.characterData)
        val ctx = engine.context
        if (ctx != null) {
            val initialModelPath = engine.player.characterData.modelAssetPath ?: "models/runner.glb"
            try {
                characterModelRenderer = com.example.longrunner.game.graphics.gltf.CharacterModelRenderer(ctx, initialModelPath)
            } catch (e: Exception) {
                android.util.Log.e("Renderer3D", "Error initializing CharacterModelRenderer", e)
            }
            try {
                subwayEnvRenderer = com.example.longrunner.game.graphics.gltf.SubwayEnvironmentRenderer(ctx)
            } catch (e: Exception) {
                android.util.Log.e("Renderer3D", "Error initializing SubwayEnvironmentRenderer", e)
            }
        }
        engine.particleSystem.initGl()

        lastFrameTimeNs = System.nanoTime()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        camera.onSurfaceChanged(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        val currentNs = System.nanoTime()
        val dtSec = if (lastFrameTimeNs > 0L) {
            ((currentNs - lastFrameTimeNs) / 1_000_000_000.0f).coerceIn(0.001f, 0.05f)
        } else {
            0.016f
        }
        lastFrameTimeNs = currentNs

        frameCount++
        if (fpsTimerNs == 0L) fpsTimerNs = currentNs
        val elapsedNs = currentNs - fpsTimerNs
        if (elapsedNs >= 500_000_000L) {
            currentFps = ((frameCount.toFloat() * 1_000_000_000.0f) / elapsedNs.toFloat()).toInt()
            currentFrameTimeMs = dtSec * 1000f
            frameCount = 0
            fpsTimerNs = currentNs
            engine.updateTelemetry(currentFps, currentFrameTimeMs)
        }

        // Active Performance & Optimization Profile
        val performanceProfile = PerformanceProfile.fromQuality(engine.settingsManager.graphicsQuality)
        engine.particleSystem.performanceProfile = performanceProfile

        // Update gameplay physics & state
        engine.update(dtSec)

        // Update Camera
        camera.isScreenShakeEnabled = engine.settingsManager.isScreenShakeEnabled
        camera.update(engine.player, dtSec)

        val phaseTransition = engine.phaseEnergyManager.phaseFrequencyTransition
        val playerDistance = engine.scoreManager.distance
        val atmosphere = if (subwayEnvRenderer?.isLoaded == true) {
            com.example.longrunner.game.world.biomes.BiomeData.CITY_STREETS.atmosphere
        } else {
            engine.biomeManager.getInterpolatedAtmosphere(playerDistance)
        }

        // Clear screen with dynamic dual-reality palette modulated by interpolated biome atmosphere
        val clearR = (atmosphere.clearR + (0.10f - atmosphere.clearR) * phaseTransition).coerceIn(0f, 1f)
        val clearG = (atmosphere.clearG * (1f - phaseTransition * 0.5f)).coerceIn(0f, 1f)
        val clearB = (atmosphere.clearB + (0.20f - atmosphere.clearB) * phaseTransition).coerceIn(0f, 1f)
        GLES30.glClearColor(clearR, clearG, clearB, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        val activeShader = shader ?: return
        val activeRoad = roadMesh ?: return
        val activeRampUp = rampUpRoadMesh ?: return
        val activeElevated = elevatedRoadMesh ?: return
        val activeRampDown = rampDownRoadMesh ?: return
        val activeSubwayRoad = subwayRoadMesh ?: return
        val activeCanyonRoad = canyonRoadMesh ?: return
        val activeRuinsRoad = ruinsRoadMesh ?: return
        val activeOrbitalRoad = orbitalRoadMesh ?: return
        val activeSubwayScenery = subwaySceneryMesh ?: return
        val activeCanyonScenery = canyonSceneryMesh ?: return
        val activeRuinsScenery = ruinsSceneryMesh ?: return
        val activeOrbitalScenery = orbitalSceneryMesh ?: return
        val activeGateway = transitGatewayMesh ?: return
        val activeBuilding = buildingMesh ?: return
        val activeTunnel = tunnelMesh ?: return
        val activeBridge = bridgeMesh ?: return
        val activeGantry = gantryMesh ?: return
        val activeSolar = solarCanopyMesh ?: return
        val activeFractureSplit = fractureSplitDecorMesh ?: return

        val activeHurdle = lowHurdleMesh ?: return
        val activeLaser = highBeamMesh ?: return
        val activeBlock = cyberBlockMesh ?: return
        val activeDrone = patrolDroneMesh ?: return
        val activeGate = slidingGateMesh ?: return
        val activeCrate = breakableCrateMesh ?: return
        val activeDebris = fallingDebrisMesh ?: return

        val activeShard = shardMesh ?: return
        val activePhaseCore = phaseCoreMesh ?: return
        val activeCredit = creditMesh ?: return
        val activeMultiplier = multiplierMesh ?: return
        val activeShieldOrb = shieldOrbMesh ?: return
        val activeOverdriveOrb = overdriveOrbMesh ?: return
        val activePhaseBatteryOrb = phaseBatteryOrbMesh ?: return
        val activeTimeBrakeOrb = timeBrakeOrbMesh ?: return
        val activeMagnetOrb = magnetOrbMesh ?: return
        val activeHoverboardOrb = hoverboardOrbMesh ?: return

        val activePlayerVisual = playerVisual ?: return

        activeShader.bind()

        // Set global lighting and cyber fog with phase shift chromatic distortion & biome atmosphere
        activeShader.setPhaseFrequency(phaseTransition)
        activeShader.setGlitchFactor(
            if (engine.settingsManager.isGlitchShaderEnabled) engine.worldEventManager.glitchFactor else 0f
        )
        val lightR = (atmosphere.lightColorR + 0.15f * phaseTransition).coerceIn(0f, 1f)
        val lightG = (atmosphere.lightColorG - 0.20f * phaseTransition).coerceIn(0f, 1f)
        val lightB = (atmosphere.lightColorB + 0.20f * phaseTransition).coerceIn(0f, 1f)
        val ambR = atmosphere.ambientR
        val ambG = atmosphere.ambientG
        val ambB = (atmosphere.ambientB + 0.15f * phaseTransition).coerceIn(0f, 1f)
        activeShader.setLighting(
            atmosphere.lightDirX, atmosphere.lightDirY, atmosphere.lightDirZ,
            lightR, lightG, lightB,
            ambR, ambG, ambB
        )
        activeShader.setEmissive(0.2f * phaseTransition)
        val fogR = (atmosphere.fogR + 0.08f * phaseTransition).coerceIn(0f, 1f)
        val fogG = (atmosphere.fogG - 0.01f * phaseTransition).coerceIn(0f, 1f)
        val fogB = (atmosphere.fogB + 0.14f * phaseTransition).coerceIn(0f, 1f)
        val scaledFogDensity = atmosphere.fogDensity * (1.0f + 0.35f * phaseTransition) * performanceProfile.fogDensityMultiplier
        activeShader.setFog(fogR, fogG, fogB, scaledFogDensity)
        activeShader.setCameraPosition(camera.position.x, camera.position.y, camera.position.z)

        // Update SubwayEnvironmentRenderer lighting & fog parameters
        subwayEnvRenderer?.setLightingAndFog(
            lightDirX = atmosphere.lightDirX,
            lightDirY = atmosphere.lightDirY,
            lightDirZ = atmosphere.lightDirZ,
            lightR = lightR,
            lightG = lightG,
            lightB = lightB,
            ambR = ambR,
            ambG = ambG,
            ambB = ambB,
            fogR = fogR,
            fogG = fogG,
            fogB = fogB,
            fogDensity = scaledFogDensity,
            cameraX = camera.position.x,
            cameraY = camera.position.y,
            cameraZ = camera.position.z,
            phaseTransition = phaseTransition
        )

        // Render Track Segments, Obstacles, and Collectibles with distance culling
        engine.trackGenerator.render(
            activeShader,
            camera.viewProjectionMatrix,
            camera.position.z,
            performanceProfile,
            activeRoad,
            activeRampUp,
            activeElevated,
            activeRampDown,
            subwayRoadMesh = activeSubwayRoad,
            canyonRoadMesh = activeCanyonRoad,
            ruinsRoadMesh = activeRuinsRoad,
            orbitalRoadMesh = activeOrbitalRoad,
            subwaySceneryMesh = activeSubwayScenery,
            canyonSceneryMesh = activeCanyonScenery,
            ruinsSceneryMesh = activeRuinsScenery,
            orbitalSceneryMesh = activeOrbitalScenery,
            transitGatewayMesh = activeGateway,
            buildingMesh = activeBuilding,
            tunnelMesh = activeTunnel,
            bridgeMesh = activeBridge,
            gantryMesh = activeGantry,
            solarCanopyMesh = activeSolar,
            fractureSplitDecorMesh = activeFractureSplit,
            lowHurdleMesh = activeHurdle,
            highBeamMesh = activeLaser,
            cyberBlockMesh = activeBlock,
            patrolDroneMesh = activeDrone,
            slidingGateMesh = activeGate,
            breakableCrateMesh = activeCrate,
            fallingDebrisMesh = activeDebris,
            shardMesh = activeShard,
            phaseCoreMesh = activePhaseCore,
            creditMesh = activeCredit,
            multiplierMesh = activeMultiplier,
            shieldOrbMesh = activeShieldOrb,
            overdriveOrbMesh = activeOverdriveOrb,
            phaseBatteryOrbMesh = activePhaseBatteryOrb,
            timeBrakeOrbMesh = activeTimeBrakeOrb,
            magnetOrbMesh = activeMagnetOrb,
            hoverboardOrbMesh = activeHoverboardOrb,
            isPhaseShiftActive = engine.phaseEnergyManager.isPhaseShiftActive,
            subwayEnvRenderer = subwayEnvRenderer
        )

        // Render Drone Swarm Searchlight Cone on Track if Swarm Event Active
        if (engine.worldEventManager.currentEvent == com.example.longrunner.game.world.WorldEventType.DRONE_SWARM) {
            val droneMesh = droneSearchlightMesh
            if (droneMesh != null) {
                modelMatrix.identity()
                modelMatrix.translate(
                    engine.worldEventManager.searchlightX,
                    0.0f,
                    engine.player.z - 12.0f
                )
                Matrix4.multiply(mvpMatrix, camera.viewProjectionMatrix, modelMatrix)
                activeShader.setMVPMatrix(mvpMatrix.values)
                activeShader.setModelMatrix(modelMatrix.values)
                activeShader.setEmissive(0.9f)
                droneMesh.render(activeShader)
            }
        }

        // Check character swap
        if (activePlayerVisual.data.id != engine.player.characterData.id) {
            activePlayerVisual.setCharacter(engine.player.characterData)
        }

        // Synchronize active hoverboard visual model & neon palette
        activePlayerVisual.updateEquippedHoverboard(engine.hoverboardManager.getEquippedHoverboard())

        val targetModelPath = engine.player.characterData.modelAssetPath ?: "models/runner.glb"
        val modelRenderer = characterModelRenderer
        if (modelRenderer != null && modelRenderer.currentAssetPath != targetModelPath) {
            val ctx = engine.context
            if (ctx != null) {
                modelRenderer.loadModel(ctx, targetModelPath)
            }
        }

        // Render Realistic 3D Skinned Model (with procedural fallback)
        val rendered3DModel = if (modelRenderer != null && modelRenderer.isLoaded) {
            val rendered = modelRenderer.render(
                vpMatrix = camera.viewProjectionMatrix,
                x = engine.player.x,
                y = engine.player.y,
                z = engine.player.z,
                speed = engine.player.speed,
                isJumping = engine.player.isJumping,
                isSliding = engine.player.isSliding,
                isRunning = engine.state == com.example.longrunner.game.core.GameState.RUNNING,
                dt = dtSec,
                cameraX = camera.position.x,
                cameraY = camera.position.y,
                cameraZ = camera.position.z,
                fogR = fogR,
                fogG = fogG,
                fogB = fogB,
                fogDensity = scaledFogDensity,
                phaseTransition = phaseTransition
            )
            if (rendered) {
                // Re-bind activeShader for power-up VFX and following meshes
                activeShader.bind()
                activeShader.setPhaseFrequency(phaseTransition)
                activeShader.setFog(fogR, fogG, fogB, scaledFogDensity)
                activeShader.setLighting(
                    0.35f, 0.9f, -0.4f,
                    0.95f + 0.15f * phaseTransition, 0.9f - 0.2f * phaseTransition, 1.0f + 0.2f * phaseTransition,
                    0.4f, 0.4f, 0.55f
                )
                activePlayerVisual.renderPowerUpVfx(
                    shader = activeShader,
                    vpMatrix = camera.viewProjectionMatrix,
                    x = engine.player.x,
                    y = engine.player.y,
                    z = engine.player.z,
                    dt = dtSec,
                    hasShield = engine.powerUpManager.hasShield,
                    isOverdrive = engine.powerUpManager.isOverdriveActive,
                    isMagnet = engine.powerUpManager.isMagnetActive,
                    isHoverboard = engine.powerUpManager.isHoverboardActive,
                    isSliding = engine.player.isSliding,
                    isJumping = engine.player.isJumping,
                    stridePhase = engine.player.stridePhase
                )
            }
            rendered
        } else {
            false
        }

        if (!rendered3DModel) {
            // Procedural fallback
            activePlayerVisual.render(
                activeShader,
                camera.viewProjectionMatrix,
                engine.player.x,
                engine.player.y,
                engine.player.z,
                engine.player.isJumping,
                engine.player.isSliding,
                engine.player.stridePhase,
                dtSec,
                hasShield = engine.powerUpManager.hasShield,
                isOverdrive = engine.powerUpManager.isOverdriveActive,
                isMagnet = engine.powerUpManager.isMagnetActive,
                isHoverboard = engine.powerUpManager.isHoverboardActive
            )
        }

        // Render "The Null" Shadow Chaser pursuing from behind
        val chaserMesh = nullChaserMesh
        if (chaserMesh != null) {
            val nullDist = engine.nullChaser.distanceBehindPlayer
            if (nullDist < 25.0f) {
                modelMatrix.identity()
                val hoverOffset = kotlin.math.sin(engine.scoreManager.distance * 0.15f) * 0.25f
                val nullX = engine.player.x * 0.55f
                val nullY = engine.player.y + 0.65f + hoverOffset
                val nullZ = engine.player.z + nullDist
                modelMatrix.translate(nullX, nullY, nullZ)
                modelMatrix.rotate(
                    kotlin.math.sin(engine.scoreManager.distance * 0.1f) * 6.0f,
                    0f, 1f, 0f
                )
                Matrix4.multiply(mvpMatrix, camera.viewProjectionMatrix, modelMatrix)
                activeShader.setMVPMatrix(mvpMatrix.values)
                activeShader.setModelMatrix(modelMatrix.values)
                val tensionPulse = 0.35f + engine.nullChaser.tension * 0.65f
                activeShader.setEmissive(tensionPulse)
                chaserMesh.render(activeShader)
            }
        }

        // Render 3D dynamic particle system (footstep sparks, debris, bursts, shockwaves, streaks)
        engine.particleSystem.render(camera, activeShader)
    }

    fun release() {
        roadMesh?.release()
        rampUpRoadMesh?.release()
        elevatedRoadMesh?.release()
        rampDownRoadMesh?.release()
        buildingMesh?.release()
        tunnelMesh?.release()
        bridgeMesh?.release()
        gantryMesh?.release()
        solarCanopyMesh?.release()
        fractureSplitDecorMesh?.release()

        lowHurdleMesh?.release()
        highBeamMesh?.release()
        cyberBlockMesh?.release()
        patrolDroneMesh?.release()
        slidingGateMesh?.release()
        breakableCrateMesh?.release()
        fallingDebrisMesh?.release()

        shardMesh?.release()
        phaseCoreMesh?.release()
        creditMesh?.release()
        multiplierMesh?.release()
        shieldOrbMesh?.release()
        overdriveOrbMesh?.release()
        phaseBatteryOrbMesh?.release()
        timeBrakeOrbMesh?.release()
        magnetOrbMesh?.release()
        hoverboardOrbMesh?.release()
        nullChaserMesh?.release()
        droneSearchlightMesh?.release()

        characterModelRenderer?.release()
        subwayEnvRenderer?.release()
        playerVisual?.release()
        engine.particleSystem.release()
    }
}
