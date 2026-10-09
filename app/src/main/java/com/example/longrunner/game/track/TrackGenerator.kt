package com.example.longrunner.game.track

import com.example.longrunner.game.collectibles.Collectible
import com.example.longrunner.game.collectibles.CollectibleType
import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.graphics.Matrix4
import com.example.longrunner.game.graphics.Mesh
import com.example.longrunner.game.graphics.PerformanceProfile
import com.example.longrunner.game.graphics.Shader
import com.example.longrunner.game.obstacles.Obstacle
import com.example.longrunner.game.obstacles.ObstacleType
import com.example.longrunner.game.player.PlayerController
import com.example.longrunner.game.world.biomes.BiomeManager
import java.util.Random
import kotlin.math.abs

class TrackGenerator(
    val random: Random = Random(),
    val validationSystem: ObstacleValidationSystem = ObstacleValidationSystem(),
    val fractureManager: FractureManager = FractureManager(),
    val biomeManager: BiomeManager = BiomeManager()
) {

    val segments = ArrayList<TrackSegment>()
    private var furthestZ: Float = 0f
    private var totalSegmentsGenerated: Int = 0

    init {
        // Initialize pooled segments
        for (i in 0 until GameConstants.POOL_SEGMENT_COUNT) {
            segments.add(TrackSegment(i))
        }
    }

    fun reset() {
        totalSegmentsGenerated = 0
        furthestZ = 0f
        fractureManager.reset()
        biomeManager.reset()

        for (segment in segments) {
            val isWarmup = totalSegmentsGenerated < 1
            val dist = abs(furthestZ)
            val segmentType = if (isWarmup) SegmentType.METRO_STRAIGHT else selectSegmentType(dist)
            val biome = biomeManager.getBiomeAtDistance(dist)
            val isGateway = biomeManager.isTransitionGateway(dist)
            segment.reset(
                furthestZ,
                segmentType,
                biomeType = biome.type,
                isTransitionGateway = isGateway
            )
            populateSegment(segment, isWarmup = isWarmup)
            furthestZ = segment.endZ
            totalSegmentsGenerated++
        }
    }

    fun getSurfaceY(z: Float): Float {
        for (segment in segments) {
            if (z <= segment.startZ && z >= segment.endZ) {
                return segment.getSurfaceY(z)
            }
        }
        return GameConstants.GROUND_Y
    }

    fun update(player: PlayerController, dt: Float) {
        // Update fracture manager with player's real-time position and lane
        fractureManager.update(player.z, player.currentLane, dt)

        // Adapt player surface elevation to track geometry
        val currentTrackY = getSurfaceY(player.z)
        player.updateSurfaceElevation(currentTrackY)

        // Update segment dynamic obstacles and collectibles
        for (segment in segments) {
            segment.update(dt, player.z)
        }

        // Check if oldest segment has passed behind player + camera
        val recycleThresholdZ = player.z + GameConstants.CAMERA_FOLLOW_DISTANCE + 5.0f

        for (segment in segments) {
            if (segment.startZ > recycleThresholdZ && segment.endZ > recycleThresholdZ) {
                // Recycle this segment to the front with dynamic environment selection
                val dist = abs(furthestZ)
                val newType = selectSegmentType(dist)
                val biome = biomeManager.getBiomeAtDistance(dist)
                val isGateway = biomeManager.isTransitionGateway(dist)
                segment.reset(
                    furthestZ,
                    newType,
                    biomeType = biome.type,
                    isTransitionGateway = isGateway
                )
                populateSegment(segment, isWarmup = false)
                furthestZ = segment.endZ
                totalSegmentsGenerated++
            }
        }
    }

    /**
     * Selects modular segment environment based on distance, procedural rhythm, and Route Fractures.
     */
    fun selectSegmentType(distanceMeters: Float): SegmentType {
        val targetZ = -distanceMeters
        val upcomingFracture = fractureManager.getUpcomingFractureForDistance(distanceMeters)

        if (upcomingFracture != null && targetZ <= upcomingFracture.startZ && targetZ >= upcomingFracture.endZ - GameConstants.SEGMENT_LENGTH) {
            val offset = upcomingFracture.startZ - targetZ
            return when (upcomingFracture.type) {
                FractureType.BRANCH_SPLIT -> {
                    if (offset < 50.0f) SegmentType.FRACTURE_SPLIT else SegmentType.FRACTURE_MERGE
                }
                FractureType.ELEVATED_OVERPASS -> {
                    when {
                        offset < 30.0f -> SegmentType.FRACTURE_OVERPASS_RAMP_UP
                        offset < 60.0f -> SegmentType.FRACTURE_OVERPASS
                        else -> SegmentType.FRACTURE_OVERPASS_RAMP_DOWN
                    }
                }
                FractureType.COLLAPSING_HIGHWAY -> {
                    if (offset < 45.0f) SegmentType.FRACTURE_SPLIT else SegmentType.FRACTURE_MERGE
                }
                FractureType.NONE -> selectStandardSegmentType(distanceMeters)
            }
        }

        return selectStandardSegmentType(distanceMeters)
    }

    private fun selectStandardSegmentType(distanceMeters: Float): SegmentType {
        // Tier 1: Intro (< 80m) -> mostly Metro Straight
        if (distanceMeters < 80f) {
            return if (random.nextFloat() < 0.25f) SegmentType.OVERPASS_GANTRY else SegmentType.METRO_STRAIGHT
        }

        // Tier 2: District Core (80m - 200m) -> Tunnels & Gantries appear
        if (distanceMeters < 200f) {
            return when (random.nextInt(4)) {
                0 -> SegmentType.NEON_TUNNEL
                1 -> SegmentType.OVERPASS_GANTRY
                2 -> SegmentType.SOLAR_DISTRICT
                else -> SegmentType.METRO_STRAIGHT
            }
        }

        // Tier 3 & 4: Skyways & Outer Viaducts (> 200m) -> Full diversity with Sky Bridges
        return when (random.nextInt(5)) {
            0 -> SegmentType.SKY_BRIDGE
            1 -> SegmentType.NEON_TUNNEL
            2 -> SegmentType.OVERPASS_GANTRY
            3 -> SegmentType.SOLAR_DISTRICT
            else -> SegmentType.METRO_STRAIGHT
        }
    }

    private fun populateSegment(segment: TrackSegment, isWarmup: Boolean) {
        if (isWarmup) {
            // Warm-up segments have only gentle shard pickups
            spawnShardLine(segment, lane = GameConstants.LANE_CENTER, startOffset = -8f, count = 4)
            return
        }

        var obsIdx = 0
        var colIdx = 0

        // Handle Specialized Fracture Segments
        when (segment.type) {
            SegmentType.FRACTURE_SPLIT -> {
                // LEFT LANE (SAFE PROTOCOL): Single low hurdle, safe passage
                segment.obstacles[obsIdx++].setup(ObstacleType.LOW_HURDLE, GameConstants.LANE_LEFT, segment.startZ - 18f)
                spawnShardLine(segment, GameConstants.LANE_LEFT, -8f, count = 4, colOffset = colIdx)
                colIdx += 4

                // CENTER LANE (BALANCED TRANSIT): High beam
                segment.obstacles[obsIdx++].setup(ObstacleType.HIGH_BEAM, GameConstants.LANE_CENTER, segment.startZ - 15f)
                spawnShardLine(segment, GameConstants.LANE_CENTER, -8f, count = 3, colOffset = colIdx)
                colIdx += 3

                // RIGHT LANE (HIGH-RISK BOUNTY SECTOR): Patrol drone + falling debris + dense credits and tokens!
                segment.obstacles[obsIdx++].setup(ObstacleType.PATROL_DRONE, GameConstants.LANE_RIGHT, segment.startZ - 11f)
                segment.obstacles[obsIdx++].setup(ObstacleType.FALLING_DEBRIS, GameConstants.LANE_RIGHT, segment.startZ - 22f)

                // High reward bounty in right lane
                if (colIdx < segment.collectibles.size) {
                    segment.collectibles[colIdx++].setup(GameConstants.LANE_RIGHT, 0.8f, segment.startZ - 6f, CollectibleType.CREDIT)
                }
                if (colIdx < segment.collectibles.size) {
                    segment.collectibles[colIdx++].setup(GameConstants.LANE_RIGHT, 0.8f, segment.startZ - 14f, CollectibleType.MULTIPLIER_TOKEN)
                }
                if (colIdx < segment.collectibles.size) {
                    segment.collectibles[colIdx++].setup(GameConstants.LANE_RIGHT, 0.8f, segment.startZ - 18f, CollectibleType.CREDIT)
                }
                if (colIdx < segment.collectibles.size) {
                    segment.collectibles[colIdx++].setup(GameConstants.LANE_RIGHT, 0.8f, segment.startZ - 26f, CollectibleType.PHASE_CORE)
                }

                validationSystem.sanitize(segment.obstacles)
                return
            }

            SegmentType.FRACTURE_OVERPASS_RAMP_UP -> {
                // Ascending runway shards climbing up the ramp incline along center lane
                for (i in 0 until 5) {
                    val zPos = segment.startZ - 4f - (i * 5f)
                    val surfY = segment.getSurfaceY(zPos)
                    if (colIdx < segment.collectibles.size) {
                        val type = if (i == 4) CollectibleType.PHASE_CORE else CollectibleType.ENERGY_SHARD
                        segment.collectibles[colIdx++].setup(GameConstants.LANE_CENTER, surfY + 0.8f, zPos, type)
                    }
                }
                return
            }

            SegmentType.FRACTURE_OVERPASS -> {
                // Elevated Skyway deck at Y = 3.5m!
                val baseY = 3.5f
                segment.obstacles[obsIdx++].setup(ObstacleType.LOW_HURDLE, GameConstants.LANE_LEFT, segment.startZ - 12f, baseY = baseY)
                segment.obstacles[obsIdx++].setup(ObstacleType.BREAKABLE_CRATE, GameConstants.LANE_RIGHT, segment.startZ - 18f, baseY = baseY)

                // High-altitude reward arc across center lane
                for (i in 0 until 4) {
                    val zPos = segment.startZ - 8f - (i * 3.5f)
                    if (colIdx < segment.collectibles.size) {
                        val type = if (i == 2) CollectibleType.MULTIPLIER_TOKEN else CollectibleType.ENERGY_SHARD
                        segment.collectibles[colIdx++].setup(GameConstants.LANE_CENTER, baseY + 0.85f, zPos, type)
                    }
                }
                validationSystem.sanitize(segment.obstacles)
                return
            }

            SegmentType.FRACTURE_OVERPASS_RAMP_DOWN -> {
                // Descending ramp shards
                for (i in 0 until 4) {
                    val zPos = segment.startZ - 5f - (i * 5f)
                    val surfY = segment.getSurfaceY(zPos)
                    if (colIdx < segment.collectibles.size) {
                        segment.collectibles[colIdx++].setup(GameConstants.LANE_CENTER, surfY + 0.8f, zPos, CollectibleType.ENERGY_SHARD)
                    }
                }
                return
            }

            SegmentType.FRACTURE_MERGE -> {
                // Clean exit merge gantry with apex Credit
                if (colIdx < segment.collectibles.size) {
                    segment.collectibles[colIdx++].setup(GameConstants.LANE_CENTER, 0.8f, segment.startZ - 15f, CollectibleType.CREDIT)
                }
                return
            }

            else -> {
                // Standard procedural generation based on distance tier
            }
        }

        val distanceMeters = abs(segment.startZ)

        // Tiered Difficulty Selection for standard segments
        if (distanceMeters < 65f) {
            // TIER 1 (Init): Single reactive obstacle
            val lane = randomLane()
            val pick = random.nextInt(3)
            when (pick) {
                0 -> {
                    segment.obstacles[obsIdx++].setup(ObstacleType.LOW_HURDLE, lane, segment.startZ - 14f)
                    spawnShardArcOverHurdle(segment, lane, segment.startZ - 14f, colIdx)
                    colIdx += 3
                }
                1 -> {
                    segment.obstacles[obsIdx++].setup(ObstacleType.HIGH_BEAM, lane, segment.startZ - 14f)
                    spawnShardLine(segment, lane, startOffset = -12f, count = 3, colOffset = colIdx, height = 0.4f)
                    colIdx += 3
                }
                else -> {
                    segment.obstacles[obsIdx++].setup(ObstacleType.BREAKABLE_CRATE, lane, segment.startZ - 14f)
                    spawnShardArcOverHurdle(segment, lane, segment.startZ - 14f, colIdx)
                    colIdx += 3
                }
            }
        } else if (distanceMeters < 120f) {
            // TIER 2 (Moderate): 2 staggered obstacles requiring fast lane transitions
            val pattern = random.nextInt(4)
            when (pattern) {
                0 -> {
                    val droneLane = randomLane()
                    val hurdleLane = otherLane(droneLane)
                    segment.obstacles[obsIdx++].setup(ObstacleType.PATROL_DRONE, droneLane, segment.startZ - 10f)
                    segment.obstacles[obsIdx++].setup(ObstacleType.LOW_HURDLE, hurdleLane, segment.startZ - 20f)
                    val freeLane = otherLane(droneLane, hurdleLane)
                    spawnShardLine(segment, freeLane, -8f, count = 4, colOffset = colIdx)
                    colIdx += 4
                }
                1 -> {
                    val blockLane = randomLane()
                    val hurdleLane = otherLane(blockLane)
                    val safeLane = otherLane(blockLane, hurdleLane)
                    segment.obstacles[obsIdx++].setup(ObstacleType.CYBER_BLOCK, blockLane, segment.startZ - 12f)
                    segment.obstacles[obsIdx++].setup(ObstacleType.LOW_HURDLE, hurdleLane, segment.startZ - 20f)
                    spawnShardLine(segment, safeLane, -10f, count = 4, colOffset = colIdx)
                    colIdx += 4
                }
                2 -> {
                    val debrisLane = randomLane()
                    val laserLane = otherLane(debrisLane)
                    segment.obstacles[obsIdx++].setup(ObstacleType.FALLING_DEBRIS, debrisLane, segment.startZ - 12f)
                    segment.obstacles[obsIdx++].setup(ObstacleType.HIGH_BEAM, laserLane, segment.startZ - 22f)
                    val safeLane = otherLane(debrisLane, laserLane)
                    spawnShardLine(segment, safeLane, -9f, count = 4, colOffset = colIdx)
                    colIdx += 4
                }
                else -> {
                    val l1 = randomLane()
                    val l2 = otherLane(l1)
                    segment.obstacles[obsIdx++].setup(ObstacleType.LOW_HURDLE, l1, segment.startZ - 8f)
                    segment.obstacles[obsIdx++].setup(ObstacleType.HIGH_BEAM, l2, segment.startZ - 19f)
                    spawnShardLine(segment, otherLane(l1, l2), -6f, count = 4, colOffset = colIdx)
                    colIdx += 4
                }
            }
        } else {
            // TIER 3 & 4 (High Intensity): Rapid multi-hazard sequences
            val pattern = random.nextInt(5)
            when (pattern) {
                0 -> {
                    // Double barrier with only 1 safe escape lane
                    val safeLane = randomLane()
                    for (lane in listOf(GameConstants.LANE_LEFT, GameConstants.LANE_CENTER, GameConstants.LANE_RIGHT)) {
                        if (lane != safeLane && obsIdx < segment.obstacles.size) {
                            val obsType = if (obsIdx == 0) ObstacleType.CYBER_BLOCK else ObstacleType.LOW_HURDLE
                            segment.obstacles[obsIdx++].setup(obsType, lane, segment.startZ - 14f)
                        }
                    }
                    spawnShardLine(segment, safeLane, -11f, count = 4, colOffset = colIdx)
                    colIdx += 4
                }
                1 -> {
                    // Sliding gate followed immediately by falling debris
                    val gateLane = randomLane()
                    val debrisLane = otherLane(gateLane)
                    segment.obstacles[obsIdx++].setup(ObstacleType.SLIDING_GATE, gateLane, segment.startZ - 11f)
                    segment.obstacles[obsIdx++].setup(ObstacleType.FALLING_DEBRIS, debrisLane, segment.startZ - 21f)
                    spawnShardLine(segment, otherLane(gateLane, debrisLane), -9f, count = 4, colOffset = colIdx)
                    colIdx += 4
                }
                2 -> {
                    // Rapid drone oscillation + high beam
                    val droneLane = randomLane()
                    val laserLane = otherLane(droneLane)
                    segment.obstacles[obsIdx++].setup(ObstacleType.PATROL_DRONE, droneLane, segment.startZ - 9f)
                    segment.obstacles[obsIdx++].setup(ObstacleType.HIGH_BEAM, laserLane, segment.startZ - 18f)
                    spawnShardLine(segment, otherLane(droneLane, laserLane), -7f, count = 4, colOffset = colIdx)
                    colIdx += 4
                }
                3 -> {
                    // High beam and low hurdle combo
                    val l1 = randomLane()
                    val l2 = otherLane(l1)
                    segment.obstacles[obsIdx++].setup(ObstacleType.HIGH_BEAM, l1, segment.startZ - 10f)
                    segment.obstacles[obsIdx++].setup(ObstacleType.CYBER_BLOCK, l2, segment.startZ - 20f)
                    spawnShardLine(segment, otherLane(l1, l2), -8f, count = 4, colOffset = colIdx)
                    colIdx += 4
                }
                else -> {
                    // Sliding gate + Cyber block
                    val gateLane = randomLane()
                    val blockLane = otherLane(gateLane)
                    segment.obstacles[obsIdx++].setup(ObstacleType.SLIDING_GATE, gateLane, segment.startZ - 10f)
                    segment.obstacles[obsIdx++].setup(ObstacleType.CYBER_BLOCK, blockLane, segment.startZ - 22f)
                    spawnShardLine(segment, otherLane(gateLane, blockLane), -10f, count = 4, colOffset = colIdx)
                    colIdx += 4
                }
            }
        }

        // Validate and sanitize the generated segment obstacles
        validationSystem.sanitize(segment.obstacles)
    }

    private fun spawnShardLine(
        segment: TrackSegment,
        lane: Int,
        startOffset: Float,
        count: Int,
        colOffset: Int = 0,
        height: Float = 0.8f
    ) {
        for (i in 0 until count) {
            val idx = colOffset + i
            if (idx < segment.collectibles.size) {
                val roll = random.nextFloat()
                val type = when {
                    roll < 0.0025f -> CollectibleType.SHIELD_ORB // Drastically reduced (was 0.012f, ~80% reduction)
                    roll < 0.0065f -> CollectibleType.MAGNET_ORB // Drastically reduced (was 0.0270f, ~85% reduction)
                    roll < 0.0180f -> CollectibleType.HOVERBOARD_ORB // Neon Hoverboard power-up pickup
                    roll < 0.0380f -> CollectibleType.OVERDRIVE_ORB
                    roll < 0.0650f -> CollectibleType.PHASE_BATTERY_ORB
                    roll < 0.0920f -> CollectibleType.TIME_BRAKE_ORB
                    roll < 0.2100f -> CollectibleType.CREDIT
                    roll < 0.2800f -> CollectibleType.PHASE_CORE
                    roll < 0.3400f -> CollectibleType.MULTIPLIER_TOKEN
                    else -> CollectibleType.ENERGY_SHARD
                }
                val isPhaseSecret = (roll < 0.25f && random.nextFloat() < 0.40f)
                val zPos = segment.startZ + startOffset - (i * 2.5f)
                val surfY = segment.getSurfaceY(zPos)
                segment.collectibles[idx].setup(
                    lane,
                    surfY + height,
                    zPos,
                    type,
                    isPhaseSecret
                )
            }
        }
    }

    private fun spawnShardArcOverHurdle(
        segment: TrackSegment,
        lane: Int,
        hurdleZ: Float,
        colOffset: Int
    ) {
        val surfY = segment.getSurfaceY(hurdleZ)
        if (colOffset < segment.collectibles.size) {
            segment.collectibles[colOffset].setup(lane, surfY + 1.1f, hurdleZ + 2.0f, CollectibleType.ENERGY_SHARD)
        }
        if (colOffset + 1 < segment.collectibles.size) {
            val apexType = if (random.nextFloat() < 0.35f) CollectibleType.CREDIT else CollectibleType.ENERGY_SHARD
            segment.collectibles[colOffset + 1].setup(lane, surfY + 1.8f, hurdleZ, apexType)
        }
        if (colOffset + 2 < segment.collectibles.size) {
            segment.collectibles[colOffset + 2].setup(lane, surfY + 1.1f, hurdleZ - 2.0f, CollectibleType.ENERGY_SHARD)
        }
    }

    private fun randomLane(): Int {
        val lanes = intArrayOf(GameConstants.LANE_LEFT, GameConstants.LANE_CENTER, GameConstants.LANE_RIGHT)
        return lanes[random.nextInt(lanes.size)]
    }

    private fun otherLane(lane1: Int): Int {
        val candidates = listOf(GameConstants.LANE_LEFT, GameConstants.LANE_CENTER, GameConstants.LANE_RIGHT).filter { it != lane1 }
        return candidates[random.nextInt(candidates.size)]
    }

    private fun otherLane(lane1: Int, lane2: Int): Int {
        return listOf(GameConstants.LANE_LEFT, GameConstants.LANE_CENTER, GameConstants.LANE_RIGHT).first { it != lane1 && it != lane2 }
    }

    fun render(
        shader: Shader,
        vpMatrix: Matrix4,
        cameraZ: Float = 0f,
        performanceProfile: PerformanceProfile = PerformanceProfile.fromQuality(com.example.longrunner.game.settings.GraphicsQuality.ULTRA),
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
        isPhaseShiftActive: Boolean = false
    ) {
        for (segment in segments) {
            segment.render(
                shader,
                vpMatrix,
                cameraZ,
                performanceProfile,
                roadMesh,
                rampUpRoadMesh,
                elevatedRoadMesh,
                rampDownRoadMesh,
                subwayRoadMesh,
                canyonRoadMesh,
                ruinsRoadMesh,
                orbitalRoadMesh,
                subwaySceneryMesh,
                canyonSceneryMesh,
                ruinsSceneryMesh,
                orbitalSceneryMesh,
                transitGatewayMesh,
                buildingMesh,
                tunnelMesh,
                bridgeMesh,
                gantryMesh,
                solarCanopyMesh,
                fractureSplitDecorMesh,
                lowHurdleMesh,
                highBeamMesh,
                cyberBlockMesh,
                patrolDroneMesh,
                slidingGateMesh,
                breakableCrateMesh,
                fallingDebrisMesh,
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
