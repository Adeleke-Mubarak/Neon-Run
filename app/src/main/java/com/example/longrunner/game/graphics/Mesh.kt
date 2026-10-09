package com.example.longrunner.game.graphics

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

data class Material(
    val r: Float = 1f,
    val g: Float = 1f,
    val b: Float = 1f,
    val a: Float = 1f,
    val emissive: Float = 0f
) {
    companion object {
        val ROAD_ASPHALT = Material(0.08f, 0.08f, 0.12f, 1f, 0.0f)
        val ROAD_LANE_CYAN = Material(0.0f, 0.95f, 1.0f, 1f, 0.85f)
        val ROAD_CURB_MAGENTA = Material(1.0f, 0.08f, 0.7f, 1f, 0.85f)
        val BUILDING_DARK = Material(0.05f, 0.04f, 0.08f, 1f, 0.0f)
        val BUILDING_WINDOW_GLOW = Material(0.95f, 0.2f, 0.8f, 1f, 0.9f)
        val HURDLE_AMBER = Material(1.0f, 0.65f, 0.05f, 1f, 0.7f)
        val LASER_CYAN = Material(0.1f, 0.9f, 1.0f, 1f, 0.95f)
        val BLOCK_RED = Material(0.95f, 0.15f, 0.25f, 1f, 0.6f)
        val SHARD_GOLD = Material(1.0f, 0.85f, 0.15f, 1f, 0.9f)
        val PLAYER_BODY = Material(0.12f, 0.14f, 0.22f, 1f, 0.1f)
        val PLAYER_NEON = Material(0.0f, 1.0f, 0.9f, 1f, 0.95f)
        val TUNNEL_FRAME = Material(0.06f, 0.05f, 0.10f, 1f, 0.0f)
        val TUNNEL_NEON_MAGENTA = Material(1.0f, 0.05f, 0.75f, 1f, 0.95f)
        val BRIDGE_GUARD_CYAN = Material(0.0f, 0.85f, 1.0f, 1f, 0.9f)
        val GANTRY_STRUCTURE = Material(0.08f, 0.09f, 0.14f, 1f, 0.05f)
        val BILLBOARD_DISPLAY = Material(0.1f, 0.95f, 0.85f, 1f, 0.95f)
        val SOLAR_GOLD = Material(1.0f, 0.75f, 0.1f, 1f, 0.85f)
        val DRONE_BODY = Material(0.12f, 0.12f, 0.16f, 1f, 0.1f)
        val DRONE_SCANNER_RED = Material(1.0f, 0.1f, 0.1f, 1f, 0.95f)
        val GATE_HAZARD_STRIPE = Material(1.0f, 0.8f, 0.0f, 1f, 0.85f)
        val CRATE_ENERGY = Material(0.0f, 0.85f, 0.95f, 1f, 0.65f)
        val PHASE_CORE_CYAN = Material(0.1f, 1.0f, 0.95f, 1f, 0.95f)
        val CREDIT_MAGENTA = Material(1.0f, 0.15f, 0.85f, 1f, 0.9f)
        val TOKEN_VIOLET = Material(0.75f, 0.2f, 1.0f, 1f, 0.95f)
        val FRACTURE_SAFE_GREEN = Material(0.1f, 0.95f, 0.45f, 1f, 0.95f)
        val FRACTURE_HAZARD_AMBER = Material(1.0f, 0.35f, 0.05f, 1f, 0.95f)
        val FRACTURE_SIGN_BG = Material(0.04f, 0.03f, 0.09f, 1f, 0.35f)
        val POWERUP_SHIELD = Material(0.1f, 0.9f, 1.0f, 1f, 0.95f)
        val POWERUP_OVERDRIVE = Material(1.0f, 0.45f, 0.05f, 1f, 0.95f)
        val POWERUP_BATTERY = Material(0.85f, 0.1f, 1.0f, 1f, 0.95f)
        val POWERUP_CHRONO = Material(0.1f, 1.0f, 0.5f, 1f, 0.95f)
        val POWERUP_MAGNET = Material(0.0f, 0.9f, 1.0f, 1f, 0.95f)
        val MAGNET_POLE_RED = Material(1.0f, 0.15f, 0.25f, 1f, 0.95f)
        val SHIELD_BUBBLE = Material(0.0f, 0.85f, 1.0f, 0.45f, 0.85f)
        val NULL_VOID_DARK = Material(0.03f, 0.01f, 0.05f, 0.98f, 0.2f)
        val NULL_CORE_CRIMSON = Material(1.0f, 0.05f, 0.22f, 1.0f, 0.98f)
        val NULL_TENDRIL_VIOLET = Material(0.6f, 0.05f, 0.95f, 0.85f, 0.9f)
        val DRONE_SEARCHLIGHT_RED = Material(1.0f, 0.12f, 0.12f, 0.45f, 0.95f)
        val POWERUP_HOVERBOARD_CYAN = Material(0.0f, 0.95f, 1.0f, 1f, 0.95f)
        val POWERUP_HOVERBOARD_MAGENTA = Material(1.0f, 0.05f, 0.75f, 1f, 0.95f)
        val POWERUP_HOVERBOARD_DECK = Material(0.06f, 0.05f, 0.12f, 1f, 0.1f)
        val POWERUP_HOVERBOARD_THRUSTER = Material(0.0f, 1.0f, 0.85f, 1f, 0.98f)

        // Biome 1: Metro Transit & Rail Depot
        val SUBWAY_BALLAST = Material(0.12f, 0.12f, 0.16f, 1f, 0.0f)
        val SUBWAY_RAIL_STEEL = Material(0.78f, 0.82f, 0.92f, 1f, 0.7f)
        val SUBWAY_SLEEPER_WOOD = Material(0.22f, 0.16f, 0.12f, 1f, 0.05f)
        val SUBWAY_PLATFORM_WALL = Material(0.18f, 0.20f, 0.24f, 1f, 0.0f)
        val SUBWAY_SIGNAL_AMBER = Material(1.0f, 0.65f, 0.05f, 1f, 0.95f)
        val SUBWAY_TRAIN_BODY = Material(0.15f, 0.28f, 0.45f, 1f, 0.15f)
        val SUBWAY_TRAIN_ACCENT = Material(1.0f, 0.20f, 0.10f, 1f, 0.9f)

        // Biome 2: Rustfall Desert Canyon
        val CANYON_SANDSTONE_DARK = Material(0.55f, 0.28f, 0.14f, 1f, 0.0f)
        val CANYON_SANDSTONE_LIGHT = Material(0.85f, 0.48f, 0.22f, 1f, 0.15f)
        val CANYON_TRESTLE_WOOD = Material(0.32f, 0.18f, 0.10f, 1f, 0.0f)
        val CANYON_CACTUS_GREEN = Material(0.16f, 0.48f, 0.18f, 1f, 0.2f)
        val CANYON_RUST_PIPE = Material(0.50f, 0.22f, 0.12f, 1f, 0.1f)
        val CANYON_SUNSET_AMBER = Material(1.0f, 0.50f, 0.15f, 1f, 0.95f)

        // Biome 3: Bioluminescent Jungle Ruins
        val RUINS_STONE_ANCIENT = Material(0.18f, 0.22f, 0.20f, 1f, 0.05f)
        val RUINS_MOSS_GREEN = Material(0.08f, 0.58f, 0.25f, 1f, 0.4f)
        val RUINS_TREE_BARK = Material(0.24f, 0.16f, 0.12f, 1f, 0.0f)
        val RUINS_GLOW_FLORA = Material(0.20f, 1.0f, 0.65f, 1f, 0.95f)
        val RUINS_VIOLET_SPORE = Material(0.78f, 0.18f, 1.0f, 1f, 0.95f)

        // Biome 4: Orbital Skydeck
        val ORBITAL_GLASS_FLOOR = Material(0.08f, 0.16f, 0.30f, 0.88f, 0.45f)
        val ORBITAL_GOLD_TRIM = Material(1.0f, 0.84f, 0.12f, 1f, 0.95f)
        val ORBITAL_SOLAR_BLUE = Material(0.06f, 0.38f, 0.90f, 1f, 0.90f)
        val ORBITAL_WHITE_CHASSIS = Material(0.92f, 0.94f, 0.98f, 1f, 0.35f)
        val ORBITAL_HOLOGRAM_CYAN = Material(0.0f, 0.95f, 1.0f, 1f, 0.95f)
    }
}

class Mesh(
    val vertexData: FloatArray,
    val indexData: ShortArray
) {
    // 3 pos + 3 norm + 4 color = 10 floats per vertex
    val strideBytes = 10 * 4
    val indexCount = indexData.size

    private val vboId: Int
    private val iboId: Int

    init {
        val buffers = IntArray(2)
        GLES30.glGenBuffers(2, buffers, 0)
        vboId = buffers[0]
        iboId = buffers[1]

        val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertexData)
                position(0)
            }

        val indexBuffer: ShortBuffer = ByteBuffer.allocateDirect(indexData.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(indexData)
                position(0)
            }

        // Upload vertex buffer
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            vertexData.size * 4,
            vertexBuffer,
            GLES30.GL_STATIC_DRAW
        )

        // Upload index buffer
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, iboId)
        GLES30.glBufferData(
            GLES30.GL_ELEMENT_ARRAY_BUFFER,
            indexData.size * 2,
            indexBuffer,
            GLES30.GL_STATIC_DRAW
        )

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun render(shader: Shader) {
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, iboId)

        // a_Position (offset 0)
        GLES30.glEnableVertexAttribArray(shader.aPositionLocation)
        GLES30.glVertexAttribPointer(
            shader.aPositionLocation,
            3,
            GLES30.GL_FLOAT,
            false,
            strideBytes,
            0
        )

        // a_Normal (offset 3 * 4 = 12)
        GLES30.glEnableVertexAttribArray(shader.aNormalLocation)
        GLES30.glVertexAttribPointer(
            shader.aNormalLocation,
            3,
            GLES30.GL_FLOAT,
            false,
            strideBytes,
            12
        )

        // a_Color (offset 6 * 4 = 24)
        GLES30.glEnableVertexAttribArray(shader.aColorLocation)
        GLES30.glVertexAttribPointer(
            shader.aColorLocation,
            4,
            GLES30.GL_FLOAT,
            false,
            strideBytes,
            24
        )

        GLES30.glDrawElements(
            GLES30.GL_TRIANGLES,
            indexCount,
            GLES30.GL_UNSIGNED_SHORT,
            0
        )

        GLES30.glDisableVertexAttribArray(shader.aPositionLocation)
        GLES30.glDisableVertexAttribArray(shader.aNormalLocation)
        GLES30.glDisableVertexAttribArray(shader.aColorLocation)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun release() {
        GLES30.glDeleteBuffers(2, intArrayOf(vboId, iboId), 0)
    }
}
