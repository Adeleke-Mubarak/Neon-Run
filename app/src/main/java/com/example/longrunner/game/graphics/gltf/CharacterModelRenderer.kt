package com.example.longrunner.game.graphics.gltf

import android.content.Context
import android.util.Log
import com.example.longrunner.game.graphics.Matrix4

/**
 * High-level realistic 3D character model renderer.
 * Coordinates GLB loading, skeletal animation state machines, and GPU skinning.
 */
class CharacterModelRenderer(context: Context, modelAssetPath: String = "models/runner.glb") {

    private var model: GlbModel? = null
    private var controller: GlbAnimationController? = null
    private var shader: GlbSkinnedShader? = null

    private val modelMatrix = Matrix4()
    private val mvpMatrix = Matrix4()

    var currentAssetPath: String? = null
        private set

    val isLoaded: Boolean get() = model != null

    init {
        loadModel(context, modelAssetPath)
    }

    fun loadModel(context: Context, newAssetPath: String): Boolean {
        if (currentAssetPath == newAssetPath && model != null) return true
        release()
        return try {
            context.assets.open(newAssetPath).use { stream ->
                val loadedModel = GlbModel.load(stream)
                model = loadedModel
                controller = GlbAnimationController(loadedModel)
                shader = GlbSkinnedShader()
                currentAssetPath = newAssetPath
                Log.d("CharacterModelRenderer", "Successfully loaded 3D GLB character model: $newAssetPath with ${loadedModel.animations.keys} animations (${loadedModel.primitives.size} primitives)")
                true
            }
        } catch (e: Exception) {
            Log.e("CharacterModelRenderer", "Failed to load 3D character model $newAssetPath: ${e.message}", e)
            release()
            false
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
        isRunning: Boolean,
        dt: Float,
        cameraX: Float,
        cameraY: Float,
        cameraZ: Float,
        fogR: Float = 0.04f,
        fogG: Float = 0.03f,
        fogB: Float = 0.08f,
        fogDensity: Float = 0.008f,
        phaseTransition: Float = 0f
    ): Boolean {
        val activeModel = model ?: return false
        val activeController = controller ?: return false
        val activeShader = shader ?: return false

        // Select state-driven mocap animation
        when {
            !isRunning -> {
                activeController.play("Idle", loop = true, speed = 1.0f)
            }
            isSliding -> {
                activeController.play("Slide", loop = false, speed = 1.9f, startTime = 0.18f, blendDuration = 0.08f)
            }
            isJumping -> {
                activeController.play("Jump", loop = false, speed = 1.25f, startTime = 0.05f, blendDuration = 0.08f)
            }
            else -> {
                val runPace = (speed / 13.0f).coerceIn(0.85f, 2.2f)
                activeController.play("Run", loop = true, speed = runPace, blendDuration = 0.08f)
            }
        }

        activeController.update(dt)

        // Compute Transform Matrix
        modelMatrix.identity()
        // Position character: y is track surface (mocap skeleton drops to ground during slide naturally)
        modelMatrix.translate(x, y, z)
        // Mixamo character faces +Z by default; rotate 180 degrees around Y to face down -Z highway
        modelMatrix.rotate(180f, 0f, 1f, 0f)
        // Natural human scale
        modelMatrix.scale(1.0f, 1.0f, 1.0f)

        Matrix4.multiply(mvpMatrix, vpMatrix, modelMatrix)

        // Bind Shader & Uniforms
        activeShader.bind()
        activeShader.setModelMatrix(modelMatrix.values)
        activeShader.setMVPMatrix(mvpMatrix.values)

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

        // Draw skinned mesh
        activeModel.render(activeShader, activeController.boneMatrices)
        return true
    }

    fun release() {
        model?.release()
        model = null
        controller = null
        shader?.release()
        shader = null
        currentAssetPath = null
    }
}
