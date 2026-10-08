package com.example.longrunner.game.graphics

import android.opengl.GLES30
import com.example.longrunner.game.camera.GameCamera
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-performance, zero-allocation 3D particle system for Neon Run: Fracture.
 * Renders glowing neon sparks, collectible bursts, obstacle smash debris,
 * near-miss shockwaves, and high-speed warp streaks in OpenGL ES 3.0.
 */
class ParticleSystem(val maxParticles: Int = 256) {

    class Particle {
        var x = 0f
        var y = 0f
        var z = 0f
        var vx = 0f
        var vy = 0f
        var vz = 0f
        var r = 1f
        var g = 1f
        var b = 1f
        var a = 1f
        var size = 0.15f
        var life = 0f
        var maxLife = 1f
        var isActive = false
    }

    val particles = Array(maxParticles) { Particle() }

    // 10 floats per vertex (3 pos + 3 norm + 4 color)
    private val floatsPerVertex = 10
    private val verticesPerParticle = 4
    private val indicesPerParticle = 6
    private val strideBytes = floatsPerVertex * 4

    private val vertexData = FloatArray(maxParticles * verticesPerParticle * floatsPerVertex)
    private var vertexBuffer: FloatBuffer? = null
    private var indexBuffer: ShortBuffer? = null

    private var vboId = 0
    private var iboId = 0
    private var isGlInitialized = false

    private val modelMatrix = Matrix4()
    private val mvpMatrix = Matrix4()

    init {
        // Pre-allocate direct buffers
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        val indices = ShortArray(maxParticles * indicesPerParticle)
        for (i in 0 until maxParticles) {
            val vBase = (i * 4).toShort()
            val iBase = i * 6
            indices[iBase + 0] = vBase
            indices[iBase + 1] = (vBase + 1).toShort()
            indices[iBase + 2] = (vBase + 2).toShort()
            indices[iBase + 3] = (vBase + 2).toShort()
            indices[iBase + 4] = (vBase + 3).toShort()
            indices[iBase + 5] = vBase
        }

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(indices)
                position(0)
            }
    }

    fun initGl() {
        if (isGlInitialized) return
        val buffers = IntArray(2)
        GLES30.glGenBuffers(2, buffers, 0)
        vboId = buffers[0]
        iboId = buffers[1]

        // Allocate dynamic VBO
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            vertexData.size * 4,
            null,
            GLES30.GL_DYNAMIC_DRAW
        )

        // Allocate static IBO
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, iboId)
        GLES30.glBufferData(
            GLES30.GL_ELEMENT_ARRAY_BUFFER,
            maxParticles * indicesPerParticle * 2,
            indexBuffer,
            GLES30.GL_STATIC_DRAW
        )

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0)
        isGlInitialized = true
    }

    var performanceProfile: PerformanceProfile = PerformanceProfile.fromQuality(com.example.longrunner.game.settings.GraphicsQuality.ULTRA)

    /**
     * Finds an inactive particle or reclaims the oldest particle within quality limits.
     */
    private fun obtainParticle(): Particle {
        val limit = performanceProfile.maxActiveParticles.coerceIn(1, maxParticles)
        for (i in 0 until limit) {
            val p = particles[i]
            if (!p.isActive) return p
        }
        // Fallback: reclaim first particle
        return particles[0]
    }

    /**
     * Emits cyber exhaust sparks trailing the runner's feet.
     */
    fun emitFootstepSpark(x: Float, y: Float, z: Float, colorHex: Long) {
        val p = obtainParticle()
        p.isActive = true
        p.x = x + (Random.nextFloat() - 0.5f) * 0.35f
        p.y = y + 0.05f
        p.z = z + 0.2f
        p.vx = (Random.nextFloat() - 0.5f) * 1.5f
        p.vy = 0.5f + Random.nextFloat() * 1.2f
        p.vz = 2.0f + Random.nextFloat() * 2.5f

        // Extract color from hex
        p.r = ((colorHex shr 16) and 0xFF) / 255.0f
        p.g = ((colorHex shr 8) and 0xFF) / 255.0f
        p.b = (colorHex and 0xFF) / 255.0f
        p.a = 0.95f
        p.size = 0.12f + Random.nextFloat() * 0.08f
        p.maxLife = 0.35f + Random.nextFloat() * 0.25f
        p.life = p.maxLife
    }

    /**
     * Radial explosion of glittering particles upon collecting shards, cores, or credits.
     */
    fun emitCollectibleBurst(x: Float, y: Float, z: Float, r: Float, g: Float, b: Float, baseCount: Int = 12) {
        val count = (baseCount * performanceProfile.particleBurstScale).toInt().coerceAtLeast(1)
        for (i in 0 until count) {
            val p = obtainParticle()
            p.isActive = true
            p.x = x
            p.y = y
            p.z = z

            val angle = (i.toFloat() / count) * 2f * Math.PI.toFloat() + (Random.nextFloat() * 0.4f)
            val speed = 2.5f + Random.nextFloat() * 3.5f
            p.vx = cos(angle) * speed
            p.vy = sin(angle) * speed * 0.8f + 1.2f
            p.vz = (Random.nextFloat() - 0.5f) * speed

            p.r = r
            p.g = g
            p.b = b
            p.a = 1.0f
            p.size = 0.16f + Random.nextFloat() * 0.1f
            p.maxLife = 0.45f + Random.nextFloat() * 0.35f
            p.life = p.maxLife
        }
    }

    /**
     * Kinetic debris fragments scattered when an obstacle is smashed.
     */
    fun emitObstacleSmash(x: Float, y: Float, z: Float, baseCount: Int = 16) {
        val count = (baseCount * performanceProfile.particleBurstScale).toInt().coerceAtLeast(1)
        for (i in 0 until count) {
            val p = obtainParticle()
            p.isActive = true
            p.x = x + (Random.nextFloat() - 0.5f) * 0.6f
            p.y = y + Random.nextFloat() * 0.8f
            p.z = z + (Random.nextFloat() - 0.5f) * 0.5f

            p.vx = (Random.nextFloat() - 0.5f) * 6.5f
            p.vy = 2.0f + Random.nextFloat() * 5.0f
            p.vz = -2.0f + (Random.nextFloat() - 0.5f) * 4.0f

            // Cyber orange / amber destruction palette
            p.r = 1.0f
            p.g = 0.4f + Random.nextFloat() * 0.5f
            p.b = 0.05f
            p.a = 0.95f
            p.size = 0.22f + Random.nextFloat() * 0.15f
            p.maxLife = 0.55f + Random.nextFloat() * 0.4f
            p.life = p.maxLife
        }
    }

    /**
     * Horizontal golden shockwave ring emitted on a Near Miss dodge.
     */
    fun emitNearMissShockwave(x: Float, y: Float, z: Float, baseCount: Int = 10) {
        val count = (baseCount * performanceProfile.particleBurstScale).toInt().coerceAtLeast(1)
        for (i in 0 until count) {
            val p = obtainParticle()
            p.isActive = true
            p.x = x
            p.y = y + 0.5f
            p.z = z

            val angle = (i.toFloat() / count) * 2f * Math.PI.toFloat()
            val speed = 3.5f + Random.nextFloat() * 2.0f
            p.vx = cos(angle) * speed
            p.vy = (Random.nextFloat() - 0.5f) * 0.8f
            p.vz = sin(angle) * speed

            p.r = 1.0f
            p.g = 0.85f
            p.b = 0.0f
            p.a = 0.95f
            p.size = 0.18f
            p.maxLife = 0.4f
            p.life = p.maxLife
        }
    }

    /**
     * Atmospheric speed streaks rushing past the camera at high velocity.
     */
    fun emitSpeedStreak(cameraZ: Float, playerSpeed: Float) {
        if (!performanceProfile.enableSpeedStreaks || playerSpeed < 17.0f) return
        val count = if (playerSpeed > 22.0f) 3 else 1
        for (i in 0 until count) {
            val p = obtainParticle()
            p.isActive = true
            p.x = (Random.nextFloat() - 0.5f) * 10.0f
            p.y = 0.5f + Random.nextFloat() * 4.0f
            p.z = cameraZ - 15.0f - Random.nextFloat() * 15.0f

            p.vx = 0f
            p.vy = 0f
            p.vz = playerSpeed * 1.8f // Rush past camera

            p.r = 0.0f
            p.g = 0.95f
            p.b = 1.0f
            p.a = 0.75f
            p.size = 0.14f
            p.maxLife = 0.35f
            p.life = p.maxLife
        }
    }

    /**
     * Updates positions, gravity, and lifetime of all active particles.
     */
    fun update(dt: Float) {
        for (p in particles) {
            if (!p.isActive) continue
            p.life -= dt
            if (p.life <= 0f) {
                p.isActive = false
                continue
            }

            p.x += p.vx * dt
            p.y += p.vy * dt
            p.z += p.vz * dt

            // Slight gravity on heavier debris
            p.vy -= 9.8f * 0.6f * dt
        }
    }

    /**
     * Zero-allocation OpenGL ES 3.0 billboard particle batch rendering.
     */
    fun render(camera: GameCamera, shader: Shader) {
        if (!isGlInitialized) return

        var activeCount = 0
        var offset = 0

        // Billboard orientation vectors aligned with camera view
        val camRightX = camera.viewMatrix.values[0]
        val camRightY = camera.viewMatrix.values[4]
        val camRightZ = camera.viewMatrix.values[8]

        val camUpX = camera.viewMatrix.values[1]
        val camUpY = camera.viewMatrix.values[5]
        val camUpZ = camera.viewMatrix.values[9]

        val activeCap = performanceProfile.maxActiveParticles.coerceIn(1, maxParticles)
        for (p in particles) {
            if (!p.isActive) continue
            if (activeCount >= activeCap) break

            val alpha = (p.a * (p.life / p.maxLife)).coerceIn(0f, 1f)
            val halfSize = p.size * 0.5f

            // Quad vertex offsets relative to particle center
            // V0: (-halfSize, -halfSize)
            val v0x = p.x - (camRightX + camUpX) * halfSize
            val v0y = p.y - (camRightY + camUpY) * halfSize
            val v0z = p.z - (camRightZ + camUpZ) * halfSize

            // V1: (+halfSize, -halfSize)
            val v1x = p.x + (camRightX - camUpX) * halfSize
            val v1y = p.y + (camRightY - camUpY) * halfSize
            val v1z = p.z + (camRightZ - camUpZ) * halfSize

            // V2: (+halfSize, +halfSize)
            val v2x = p.x + (camRightX + camUpX) * halfSize
            val v2y = p.y + (camRightY + camUpY) * halfSize
            val v2z = p.z + (camRightZ + camUpZ) * halfSize

            // V3: (-halfSize, +halfSize)
            val v3x = p.x - (camRightX - camUpX) * halfSize
            val v3y = p.y - (camRightY - camUpY) * halfSize
            val v3z = p.z - (camRightZ - camUpZ) * halfSize

            // Write V0
            writeVertex(offset, v0x, v0y, v0z, p.r, p.g, p.b, alpha)
            offset += floatsPerVertex
            // Write V1
            writeVertex(offset, v1x, v1y, v1z, p.r, p.g, p.b, alpha)
            offset += floatsPerVertex
            // Write V2
            writeVertex(offset, v2x, v2y, v2z, p.r, p.g, p.b, alpha)
            offset += floatsPerVertex
            // Write V3
            writeVertex(offset, v3x, v3y, v3z, p.r, p.g, p.b, alpha)
            offset += floatsPerVertex

            activeCount++
        }

        if (activeCount == 0) return

        // Upload active vertices to dynamic VBO
        vertexBuffer?.position(0)
        vertexBuffer?.put(vertexData, 0, offset)
        vertexBuffer?.position(0)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, offset * 4, vertexBuffer)

        // Identity model matrix for world-space particle coordinates
        modelMatrix.identity()
        Matrix4.multiply(mvpMatrix, camera.viewProjectionMatrix, modelMatrix)

        shader.setMVPMatrix(mvpMatrix.values)
        shader.setModelMatrix(modelMatrix.values)

        // Additive blending & disable depth writes for radiant neon glow
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE)
        GLES30.glDepthMask(false)

        // Setup vertex attributes
        GLES30.glEnableVertexAttribArray(shader.aPositionLocation)
        GLES30.glVertexAttribPointer(shader.aPositionLocation, 3, GLES30.GL_FLOAT, false, strideBytes, 0)

        GLES30.glEnableVertexAttribArray(shader.aNormalLocation)
        GLES30.glVertexAttribPointer(shader.aNormalLocation, 3, GLES30.GL_FLOAT, false, strideBytes, 3 * 4)

        GLES30.glEnableVertexAttribArray(shader.aColorLocation)
        GLES30.glVertexAttribPointer(shader.aColorLocation, 4, GLES30.GL_FLOAT, false, strideBytes, 6 * 4)

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, iboId)
        GLES30.glDrawElements(
            GLES30.GL_TRIANGLES,
            activeCount * indicesPerParticle,
            GLES30.GL_UNSIGNED_SHORT,
            0
        )

        // Restore default state
        GLES30.glDepthMask(true)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    private fun writeVertex(
        offset: Int,
        x: Float, y: Float, z: Float,
        r: Float, g: Float, b: Float, a: Float
    ) {
        vertexData[offset + 0] = x
        vertexData[offset + 1] = y
        vertexData[offset + 2] = z
        // Normal facing camera (+Y or +Z placeholder)
        vertexData[offset + 3] = 0f
        vertexData[offset + 4] = 1f
        vertexData[offset + 5] = 0f
        // Color
        vertexData[offset + 6] = r
        vertexData[offset + 7] = g
        vertexData[offset + 8] = b
        vertexData[offset + 9] = a
    }

    fun reset() {
        for (p in particles) {
            p.isActive = false
        }
    }

    fun release() {
        if (isGlInitialized) {
            val buffers = intArrayOf(vboId, iboId)
            GLES30.glDeleteBuffers(2, buffers, 0)
            isGlInitialized = false
        }
    }
}
