package com.example.longrunner.game.graphics.gltf

import android.content.Context
import android.opengl.GLES30
import android.util.Log
import com.example.longrunner.game.graphics.Matrix4
import com.example.longrunner.game.powerups.HoverboardData

/**
 * High-performance 3D Skateboard Model Renderer.
 * Loads and renders authentic 3D GLB skateboards directly under the runner's feet:
 * - Classic Street Skateboard (models/skateboard_classic.glb)
 * - Cyber Cruiser Skateboard (models/skateboard_cruiser.glb)
 * - Speed Demon Pro Skateboard (models/skateboard_pro.glb)
 * - Supreme Red/Gold Edition Skateboard (models/skateboard_supreme.glb)
 */
class SkateboardModelRenderer(private val context: Context) {

    private val modelCache = HashMap<String, GlbModel>()
    private var shader: GlbSkinnedShader? = null

    private val modelMatrix = Matrix4()
    private val mvpMatrix = Matrix4()
    private val zeroBones = FloatArray(16) { 0f }

    private var animTime: Float = 0f

    init {
        try {
            shader = GlbSkinnedShader()
            // Preload starter skateboard
            loadModel(HoverboardData.VECTOR_GLIDE.modelAssetPath)
        } catch (e: Exception) {
            Log.e("SkateboardRenderer", "Failed to initialize SkateboardModelRenderer: ${e.message}", e)
        }
    }

    fun loadModel(assetPath: String): GlbModel? {
        modelCache[assetPath]?.let { return it }
        return try {
            context.assets.open(assetPath).use { stream ->
                val loaded = GlbModel.load(stream)
                modelCache[assetPath] = loaded
                Log.d("SkateboardRenderer", "Successfully loaded skateboard GLB: $assetPath (${loaded.primitives.size} primitives)")
                loaded
            }
        } catch (e: Exception) {
            Log.e("SkateboardRenderer", "Failed to load skateboard GLB $assetPath: ${e.message}", e)
            null
        }
    }

    fun render(
        vpMatrix: Matrix4,
        x: Float,
        y: Float,
        z: Float,
        speed: Float,
        isJumping: Boolean,
        isSliding: Boolean,
        stridePhase: Float,
        dt: Float,
        cameraX: Float,
        cameraY: Float,
        cameraZ: Float,
        fogR: Float = 0.04f,
        fogG: Float = 0.03f,
        fogB: Float = 0.08f,
        fogDensity: Float = 0.008f,
        phaseTransition: Float = 0f,
        boardData: HoverboardData
    ): Boolean {
        animTime += dt

        val activeShader = shader ?: return false
        val model = modelCache[boardData.modelAssetPath] ?: loadModel(boardData.modelAssetPath) ?: return false

        // Compute skateboard position and tilt under runner's feet
        val boardFloatY = kotlin.math.sin(animTime * 6.0f) * 0.008f
        val boardY = y + 0.01f + boardFloatY
        val boardPitch = if (isJumping) -6.0f else 0.0f
        val boardRoll = kotlin.math.sin(animTime * 3.5f) * 1.5f

        modelMatrix.identity()
        modelMatrix.translate(x, boardY, z)
        if (boardPitch != 0f) modelMatrix.rotate(boardPitch, 1f, 0f, 0f)
        if (boardRoll != 0f) modelMatrix.rotate(boardRoll, 0f, 0f, 1f)

        Matrix4.multiply(mvpMatrix, vpMatrix, modelMatrix)

        activeShader.bind()
        activeShader.setModelMatrix(modelMatrix.values)
        activeShader.setMVPMatrix(mvpMatrix.values)
        activeShader.setBones(zeroBones, 0)

        activeShader.setLighting(
            dirX = 0.35f, dirY = 0.9f, dirZ = -0.4f,
            lr = 0.95f + 0.15f * phaseTransition,
            lg = 0.9f - 0.2f * phaseTransition,
            lb = 1.0f + 0.2f * phaseTransition,
            ar = 0.45f, ag = 0.45f, ab = 0.60f
        )
        activeShader.setFog(fogR, fogG, fogB, fogDensity)
        activeShader.setCameraPosition(cameraX, cameraY, cameraZ)
        activeShader.setPhaseFrequency(phaseTransition)

        GLES30.glDisable(GLES30.GL_CULL_FACE)
        for (prim in model.primitives) {
            model.renderPrimitive(prim, activeShader)
        }

        return true
    }

    fun release() {
        for ((_, model) in modelCache) {
            model.release()
        }
        modelCache.clear()
        shader?.release()
        shader = null
    }
}
