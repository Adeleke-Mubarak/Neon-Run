package com.example.longrunner.game.collectibles

import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.graphics.AABB
import com.example.longrunner.game.graphics.Matrix4
import com.example.longrunner.game.graphics.Mesh
import com.example.longrunner.game.graphics.Shader
import kotlin.math.sin
import kotlin.math.sqrt

enum class CollectibleType {
    ENERGY_SHARD,       // Standard collectible (+50 pts)
    PHASE_CORE,         // Restores Phase Energy (+150 pts)
    CREDIT,             // Progression currency (+200 pts)
    MULTIPLIER_TOKEN,   // Multiplier boost (+2x for 10s)
    SHIELD_ORB,         // Kinetic Shield power-up
    OVERDRIVE_ORB,      // Overdrive Booster power-up
    PHASE_BATTERY_ORB,  // Phase Battery power-up
    TIME_BRAKE_ORB,     // Time Brake power-up
    MAGNET_ORB,         // Quantum Magnet power-up
    HOVERBOARD_ORB      // Neon Hoverboard power-up
}

class Collectible(
    var type: CollectibleType = CollectibleType.ENERGY_SHARD,
    var lane: Int = GameConstants.LANE_CENTER,
    var baseY: Float = 0.8f,
    var z: Float = 0f
) {
    var isActive: Boolean = false
    var currentX: Float = 0f
    var currentY: Float = baseY
    var currentZ: Float = z
    var rotationAngle: Float = 0f
    var isPhaseExclusive: Boolean = false

    val collider = AABB()

    private val modelMatrix = Matrix4()
    private val mvpMatrix = Matrix4()

    fun setup(
        lane: Int,
        y: Float,
        z: Float,
        type: CollectibleType = CollectibleType.ENERGY_SHARD,
        isPhaseExclusive: Boolean = false
    ) {
        this.type = type
        this.lane = lane
        this.baseY = y
        this.currentY = y
        this.z = z
        this.currentZ = z
        this.isActive = true
        this.isPhaseExclusive = isPhaseExclusive
        this.currentX = GameConstants.laneToX(lane)
        this.rotationAngle = (z * 15f) % 360f

        updateCollider()
    }

    fun update(dt: Float) {
        if (!isActive) return

        rotationAngle = (rotationAngle + 200f * dt) % 360f
        val bob = sin(rotationAngle * 0.04f) * 0.12f
        currentY = baseY + bob

        updateCollider()
    }

    /**
     * Magnet attraction physics: Smoothly pulls collectible toward player position
     */
    fun attractTowards(targetX: Float, targetY: Float, targetZ: Float, pullSpeed: Float, dt: Float) {
        if (!isActive) return

        val dx = targetX - currentX
        val dy = targetY - currentY
        val dz = targetZ - currentZ
        val distSq = dx * dx + dy * dy + dz * dz

        if (distSq > 0.001f) {
            val dist = sqrt(distSq)
            val step = pullSpeed * dt
            if (dist <= step) {
                currentX = targetX
                currentY = targetY
                currentZ = targetZ
            } else {
                currentX += (dx / dist) * step
                currentY += (dy / dist) * step
                currentZ += (dz / dist) * step
            }
            updateCollider()
        }
    }

    private fun updateCollider() {
        collider.set(currentX, currentY, currentZ, 0.45f, 0.45f, 0.45f)
    }

    fun render(
        shader: Shader,
        vpMatrix: Matrix4,
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
        if (!isActive) return
        if (isPhaseExclusive && !isPhaseShiftActive) return

        modelMatrix.identity()
        modelMatrix.translate(currentX, currentY, currentZ)
        modelMatrix.rotate(rotationAngle, 0f, 1f, 0f)
        Matrix4.multiply(mvpMatrix, vpMatrix, modelMatrix)

        shader.setModelMatrix(modelMatrix.values)
        shader.setMVPMatrix(mvpMatrix.values)

        when (type) {
            CollectibleType.ENERGY_SHARD -> shardMesh.render(shader)
            CollectibleType.PHASE_CORE -> phaseCoreMesh.render(shader)
            CollectibleType.CREDIT -> creditMesh.render(shader)
            CollectibleType.MULTIPLIER_TOKEN -> multiplierMesh.render(shader)
            CollectibleType.SHIELD_ORB -> shieldOrbMesh.render(shader)
            CollectibleType.OVERDRIVE_ORB -> overdriveOrbMesh.render(shader)
            CollectibleType.PHASE_BATTERY_ORB -> phaseBatteryOrbMesh.render(shader)
            CollectibleType.TIME_BRAKE_ORB -> timeBrakeOrbMesh.render(shader)
            CollectibleType.MAGNET_ORB -> magnetOrbMesh.render(shader)
            CollectibleType.HOVERBOARD_ORB -> hoverboardOrbMesh.render(shader)
        }
    }
}
