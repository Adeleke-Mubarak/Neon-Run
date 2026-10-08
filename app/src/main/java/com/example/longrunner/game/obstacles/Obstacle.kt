package com.example.longrunner.game.obstacles

import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.graphics.AABB
import com.example.longrunner.game.graphics.Matrix4
import com.example.longrunner.game.graphics.Mesh
import com.example.longrunner.game.graphics.Shader
import kotlin.math.sin

enum class ObstacleType {
    LOW_HURDLE,     // Jump required (height ~0.65m)
    HIGH_BEAM,      // Slide required (suspended beam from 0.85m to 2.0m)
    CYBER_BLOCK,    // Lane change required (full solid block up to 2.5m)
    PATROL_DRONE,   // Moving drone oscillating laterally at chest height (~1.15m)
    SLIDING_GATE,   // Heavy tracked gate sliding across lanes
    BREAKABLE_CRATE,// Holographic cargo container (height ~1.1m, jumpable)
    FALLING_DEBRIS  // Telegraphed girder dropping from overhead when runner approaches
}

class Obstacle(
    var type: ObstacleType = ObstacleType.LOW_HURDLE,
    var lane: Int = GameConstants.LANE_CENTER,
    var z: Float = 0f
) {
    var isActive: Boolean = false
    var currentX: Float = 0f
    var currentY: Float = 0f
    var baseX: Float = 0f

    // Dynamic movement parameters
    var oscillationPhase: Float = 0f
    var oscillationSpeed: Float = 3.2f
    var oscillationAmplitude: Float = 0.85f
    var fallVelocity: Float = 0f
    var isGrounded: Boolean = false
    var isPhasePermeable: Boolean = false

    var baseY: Float = 0f

    val collider = AABB()

    private val modelMatrix = Matrix4()
    private val mvpMatrix = Matrix4()

    fun setup(type: ObstacleType, lane: Int, z: Float, baseY: Float = 0f) {
        this.type = type
        this.lane = lane
        this.z = z
        this.baseY = baseY
        this.isActive = true
        this.baseX = GameConstants.laneToX(lane)
        this.currentX = baseX
        this.oscillationPhase = (z * 0.2f) % 6.28f
        this.isPhasePermeable = (type == ObstacleType.HIGH_BEAM || type == ObstacleType.CYBER_BLOCK || type == ObstacleType.PATROL_DRONE)

        when (type) {
            ObstacleType.LOW_HURDLE -> {
                currentY = baseY
                collider.set(currentX, baseY + 0.325f, z, 0.85f, 0.325f, 0.55f)
            }
            ObstacleType.HIGH_BEAM -> {
                currentY = baseY
                collider.set(currentX, baseY + 1.475f, z, 0.85f, 0.625f, 0.55f)
            }
            ObstacleType.CYBER_BLOCK -> {
                currentY = baseY
                collider.set(currentX, baseY + 1.25f, z, 0.85f, 1.25f, 0.65f)
            }
            ObstacleType.PATROL_DRONE -> {
                currentY = baseY + 1.15f
                collider.set(currentX, currentY, z, 0.55f, 0.3f, 0.55f)
            }
            ObstacleType.SLIDING_GATE -> {
                currentY = baseY
                collider.set(currentX, baseY + 0.9f, z, 0.9f, 0.9f, 0.55f)
            }
            ObstacleType.BREAKABLE_CRATE -> {
                currentY = baseY
                collider.set(currentX, baseY + 0.55f, z, 0.7f, 0.55f, 0.65f)
            }
            ObstacleType.FALLING_DEBRIS -> {
                currentY = baseY + 6.5f // starts suspended high above
                fallVelocity = 0f
                isGrounded = false
                collider.set(currentX, currentY + 0.25f, z, 0.9f, 0.25f, 0.60f)
            }
        }
    }

    fun update(dt: Float, playerZ: Float) {
        if (!isActive) return

        when (type) {
            ObstacleType.PATROL_DRONE -> {
                oscillationPhase += oscillationSpeed * dt
                currentX = baseX + sin(oscillationPhase) * oscillationAmplitude
                val hoverY = 1.15f + sin(oscillationPhase * 2.0f) * 0.12f
                currentY = baseY + hoverY
                collider.set(currentX, currentY, z, 0.55f, 0.3f, 0.55f)
            }
            ObstacleType.SLIDING_GATE -> {
                oscillationPhase += 2.6f * dt
                currentX = baseX + sin(oscillationPhase) * 1.05f
                currentY = baseY
                collider.set(currentX, baseY + 0.9f, z, 0.9f, 0.9f, 0.55f)
            }
            ObstacleType.FALLING_DEBRIS -> {
                // If player is within 34 meters, initiate rapid descent
                val distanceAhead = playerZ - z // player runs in -Z direction
                if (!isGrounded && distanceAhead in 0.0f..34.0f) {
                    fallVelocity -= 52.0f * dt
                    currentY += fallVelocity * dt
                    if (currentY <= baseY) {
                        currentY = baseY
                        isGrounded = true
                        fallVelocity = 0f
                    }
                }
                collider.set(currentX, currentY + 0.25f, z, 0.9f, 0.25f, 0.60f)
            }
            else -> {
                // Static obstacles maintain current collider
            }
        }
    }

    fun render(
        shader: Shader,
        vpMatrix: Matrix4,
        lowHurdleMesh: Mesh,
        highBeamMesh: Mesh,
        cyberBlockMesh: Mesh,
        patrolDroneMesh: Mesh,
        slidingGateMesh: Mesh,
        breakableCrateMesh: Mesh,
        fallingDebrisMesh: Mesh
    ) {
        if (!isActive) return

        modelMatrix.identity()
        modelMatrix.translate(currentX, currentY, z)
        Matrix4.multiply(mvpMatrix, vpMatrix, modelMatrix)

        shader.setModelMatrix(modelMatrix.values)
        shader.setMVPMatrix(mvpMatrix.values)

        when (type) {
            ObstacleType.LOW_HURDLE -> lowHurdleMesh.render(shader)
            ObstacleType.HIGH_BEAM -> highBeamMesh.render(shader)
            ObstacleType.CYBER_BLOCK -> cyberBlockMesh.render(shader)
            ObstacleType.PATROL_DRONE -> patrolDroneMesh.render(shader)
            ObstacleType.SLIDING_GATE -> slidingGateMesh.render(shader)
            ObstacleType.BREAKABLE_CRATE -> breakableCrateMesh.render(shader)
            ObstacleType.FALLING_DEBRIS -> fallingDebrisMesh.render(shader)
        }
    }
}
