package com.example.longrunner.game.track

import com.example.longrunner.game.collectibles.Collectible
import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.graphics.Matrix4
import com.example.longrunner.game.graphics.Mesh
import com.example.longrunner.game.graphics.PerformanceProfile
import com.example.longrunner.game.graphics.Shader
import com.example.longrunner.game.obstacles.Obstacle
import com.example.longrunner.game.world.biomes.BiomeType

enum class SegmentType {
    METRO_STRAIGHT,
    NEON_TUNNEL,
    SKY_BRIDGE,
    OVERPASS_GANTRY,
    SOLAR_DISTRICT,
    FRACTURE_SPLIT,
    FRACTURE_OVERPASS_RAMP_UP,
    FRACTURE_OVERPASS,
    FRACTURE_OVERPASS_RAMP_DOWN,
    FRACTURE_MERGE
}

class TrackSegment(val segmentIndex: Int) {
    var startZ: Float = 0f
    val length: Float = GameConstants.SEGMENT_LENGTH
    val endZ: Float get() = startZ - length

    var type: SegmentType = SegmentType.METRO_STRAIGHT
    var fractureType: FractureType = FractureType.NONE
    var biomeType: BiomeType = BiomeType.CITY_STREETS
    var isTransitionGateway: Boolean = false

    var elevationStart: Float = 0.0f
    var elevationEnd: Float = 0.0f

    val obstacles = ArrayList<Obstacle>()
    val collectibles = ArrayList<Collectible>()

    init {
        for (j in 0 until 6) obstacles.add(Obstacle())
        for (j in 0 until 12) collectibles.add(Collectible())
    }

    private val modelMatrix = Matrix4()
    private val mvpMatrix = Matrix4()

    fun reset(
        startZ: Float,
        type: SegmentType = SegmentType.METRO_STRAIGHT,
        fractureType: FractureType = FractureType.NONE,
        biomeType: BiomeType = BiomeType.CITY_STREETS,
        isTransitionGateway: Boolean = false
    ) {
        this.startZ = startZ
        this.type = type
        this.fractureType = fractureType
        this.biomeType = biomeType
        this.isTransitionGateway = isTransitionGateway

        when (type) {
            SegmentType.FRACTURE_OVERPASS_RAMP_UP -> {
                elevationStart = 0.0f
                elevationEnd = 3.5f
            }
            SegmentType.FRACTURE_OVERPASS -> {
                elevationStart = 3.5f
                elevationEnd = 3.5f
            }
            SegmentType.FRACTURE_OVERPASS_RAMP_DOWN -> {
                elevationStart = 3.5f
                elevationEnd = 0.0f
            }
            else -> {
                elevationStart = 0.0f
                elevationEnd = 0.0f
            }
        }

        for (obs in obstacles) obs.isActive = false
        for (col in collectibles) col.isActive = false
    }

    fun getSurfaceY(z: Float): Float {
        if (elevationStart == elevationEnd) return elevationStart
        val t = ((startZ - z) / length).coerceIn(0.0f, 1.0f)
        return elevationStart + t * (elevationEnd - elevationStart)
    }

    fun update(dt: Float, playerZ: Float) {
        for (obs in obstacles) {
            if (obs.isActive) obs.update(dt, playerZ)
        }
        for (col in collectibles) {
            if (col.isActive) col.update(dt)
        }
    }

    fun render(
        shader: Shader,
        vpMatrix: Matrix4,
        cameraZ: Float,
        performanceProfile: PerformanceProfile,
        roadMesh: Mesh,
        rampUpRoadMesh: Mesh,
        elevatedRoadMesh: Mesh,
        rampDownRoadMesh: Mesh,
        subwayRoadMesh: Mesh,
        canyonRoadMesh: Mesh,
        ruinsRoadMesh: Mesh,
        orbitalRoadMesh: Mesh,
        subwaySceneryMesh: Mesh,
        canyonSceneryMesh: Mesh,
        ruinsSceneryMesh: Mesh,
        orbitalSceneryMesh: Mesh,
        transitGatewayMesh: Mesh,
        buildingMesh: Mesh,
        tunnelMesh: Mesh,
        bridgeMesh: Mesh,
        gantryMesh: Mesh,
        solarCanopyMesh: Mesh,
        fractureSplitDecorMesh: Mesh,
        lowHurdleMesh: Mesh,
        highBeamMesh: Mesh,
        cyberBlockMesh: Mesh,
        patrolDroneMesh: Mesh,
        slidingGateMesh: Mesh,
        breakableCrateMesh: Mesh,
        fallingDebrisMesh: Mesh,
        shardMesh: Mesh,
        phaseCoreMesh: Mesh,
        creditMesh: Mesh,
        multiplierMesh: Mesh,
        shieldOrbMesh: Mesh,
        overdriveOrbMesh: Mesh,
        phaseBatteryOrbMesh: Mesh,
        timeBrakeOrbMesh: Mesh,
        magnetOrbMesh: Mesh,
        hoverboardOrbMesh: Mesh,
        isPhaseShiftActive: Boolean = false,
        subwayEnvRenderer: com.example.longrunner.game.graphics.gltf.SubwayEnvironmentRenderer? = null
    ) {
        // Occlusion & Horizon Distance Culling
        val endZ = startZ - length
        if (endZ > cameraZ + 5.0f) return // Passed entirely behind camera
        if (startZ < cameraZ - performanceProfile.maxDrawDistance) return // Beyond horizon draw distance

        // Render road section
        modelMatrix.identity()
        modelMatrix.translate(0f, 0f, startZ)
        Matrix4.multiply(mvpMatrix, vpMatrix, modelMatrix)

        shader.setModelMatrix(modelMatrix.values)
        shader.setMVPMatrix(mvpMatrix.values)

        // Select road geometry
        if (subwayEnvRenderer != null && subwayEnvRenderer.isLoaded) {
            subwayEnvRenderer.renderTrack(vpMatrix, startZ, length)
            shader.bind()
        } else {
            when (type) {
                SegmentType.FRACTURE_OVERPASS_RAMP_UP -> rampUpRoadMesh.render(shader)
                SegmentType.FRACTURE_OVERPASS -> elevatedRoadMesh.render(shader)
                SegmentType.FRACTURE_OVERPASS_RAMP_DOWN -> rampDownRoadMesh.render(shader)
                else -> subwayRoadMesh.render(shader)
            }

            val renderSceneryBuildings = performanceProfile.enableBackgroundScenery
            if (renderSceneryBuildings) {
                subwaySceneryMesh.render(shader)
            }
            if (type == SegmentType.SKY_BRIDGE || type == SegmentType.FRACTURE_OVERPASS) {
                bridgeMesh.render(shader)
            } else if (type == SegmentType.OVERPASS_GANTRY) {
                gantryMesh.render(shader)
            } else if (type == SegmentType.NEON_TUNNEL) {
                tunnelMesh.render(shader)
            }
        }

        // Render segment obstacles with distance culling
        val minZ = cameraZ - performanceProfile.maxDrawDistance
        val maxZ = cameraZ + 2.0f
        for (obs in obstacles) {
            if (obs.isActive && obs.z in minZ..maxZ) {
                obs.render(
                    shader,
                    vpMatrix,
                    lowHurdleMesh,
                    highBeamMesh,
                    cyberBlockMesh,
                    patrolDroneMesh,
                    slidingGateMesh,
                    breakableCrateMesh,
                    fallingDebrisMesh,
                    subwayEnvRenderer = subwayEnvRenderer
                )
            }
        }

        // Render segment collectibles with distance culling
        for (col in collectibles) {
            if (col.isActive && col.currentZ in minZ..maxZ) {
                col.render(
                    shader,
                    vpMatrix,
                    shardMesh,
                    phaseCoreMesh,
                    creditMesh,
                    multiplierMesh,
                    shieldOrbMesh,
                    overdriveOrbMesh,
                    phaseBatteryOrbMesh,
                    timeBrakeOrbMesh,
                    magnetOrbMesh,
                    hoverboardOrbMesh,
                    isPhaseShiftActive
                )
            }
        }
    }
}
