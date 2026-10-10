package com.example.longrunner.game.graphics.gltf

import android.content.Context
import android.util.Log
import com.example.longrunner.game.graphics.Matrix4

/**
 * High-performance 3D Subway Environment Renderer.
 * Loads and renders authentic textured Subway Surfers GLB assets:
 * - 3-Lane Railway Tracks (with rails, ties, ballast bed)
 * - Subway Surfers Train Carriage obstacles (blocking lanes)
 * - Subway Surfers Hurdle / Barricade obstacles (jump/slide hazards)
 * - Overhead Bridges & Gantry Archways
 * - City Background Skyline & Station Wall Scenery
 */
class SubwayEnvironmentRenderer(context: Context, modelAssetPath: String = "models/subway_surf_assets.glb") {

    private var model: GlbModel? = null
    private var shader: GlbSkinnedShader? = null

    private val modelMatrix = Matrix4()
    private val mvpMatrix = Matrix4()
    private val zeroBones = FloatArray(16) { 0f }

    // Primitives grouped by asset type
    private var trackPrimitives = emptyList<GlbPrimitive>()
    private var trainPrimitives = emptyList<GlbPrimitive>()
    private var hurdlePrimitives = emptyList<GlbPrimitive>()
    private var bridgePrimitives = emptyList<GlbPrimitive>()
    private var sceneryPrimitives = emptyList<GlbPrimitive>()

    val isLoaded: Boolean get() = model != null

    init {
        load(context, modelAssetPath)
    }

    fun load(context: Context, assetPath: String): Boolean {
        release()
        return try {
            context.assets.open(assetPath).use { stream ->
                val loadedModel = GlbModel.load(stream)
                model = loadedModel
                shader = GlbSkinnedShader()

                // Categorize primitives by mesh name and dimensions
                trackPrimitives = loadedModel.findPrimitives { prim ->
                    prim.name.contains("236") || prim.name.contains("237") || prim.name.contains("238")
                }

                trainPrimitives = loadedModel.findPrimitives { prim ->
                    prim.name.contains("Train", ignoreCase = true)
                }

                hurdlePrimitives = loadedModel.findPrimitives { prim ->
                    prim.name.contains("Obstacle", ignoreCase = true)
                }

                bridgePrimitives = loadedModel.findPrimitives { prim ->
                    prim.name.contains("260") || prim.name.contains("280")
                }

                sceneryPrimitives = loadedModel.findPrimitives { prim ->
                    prim.name.contains("247") || prim.name.contains("256")
                }

                Log.d(
                    "SubwayEnvRenderer",
                    "Loaded Subway Surfers assets: ${loadedModel.primitives.size} total primitives | " +
                            "Tracks: ${trackPrimitives.size}, Trains: ${trainPrimitives.size}, " +
                            "Hurdles: ${hurdlePrimitives.size}, Bridges: ${bridgePrimitives.size}, " +
                            "Scenery: ${sceneryPrimitives.size}"
                )
                true
            }
        } catch (e: Exception) {
            Log.e("SubwayEnvRenderer", "Failed to load subway assets from $assetPath: ${e.message}", e)
            release()
            false
        }
    }

    fun setLightingAndFog(
        lightDirX: Float, lightDirY: Float, lightDirZ: Float,
        lightR: Float, lightG: Float, lightB: Float,
        ambR: Float, ambG: Float, ambB: Float,
        fogR: Float, fogG: Float, fogB: Float,
        fogDensity: Float,
        cameraX: Float, cameraY: Float, cameraZ: Float,
        phaseTransition: Float = 0f
    ) {
        val s = shader ?: return
        s.bind()
        s.setLighting(lightDirX, lightDirY, lightDirZ, lightR, lightG, lightB, ambR, ambG, ambB)
        s.setFog(fogR, fogG, fogB, fogDensity)
        s.setCameraPosition(cameraX, cameraY, cameraZ)
        s.setPhaseFrequency(phaseTransition)
    }

    /**
     * Renders the 3-rail railway track bed for a 40-meter segment.
     */
    fun renderTrack(vpMatrix: Matrix4, startZ: Float, segmentLength: Float = 40.0f) {
        val activeModel = model ?: return
        val activeShader = shader ?: return
        val prim = trackPrimitives.firstOrNull() ?: return

        // Center and length scaling
        // Raw Track: length dx=4222.2, width dy=2807.2, height dz=1333.6
        // Raw center: cx = -3414.6f, cy = -1275.0f (exact center of 3 rails), czBase = 135.0f (rail top)
        val cx = -3414.6f
        val cy = -1275.0f
        val czBase = 135.0f

        val scaleLen = segmentLength / 4222.2f
        val scaleWidth = 0.00978f
        val scaleHeight = 0.00978f
        val centerZ = startZ - segmentLength * 0.5f

        // Standard column-major OpenGL transform:
        // worldX = (rawY - cy) * scaleWidth
        // worldY = (rawZ - czBase) * scaleHeight
        // worldZ = -(rawX - cx) * scaleLen + centerZ
        val m = FloatArray(16)
        // Col 0: raw X maps to -worldZ (length along track)
        m[0] = 0f
        m[1] = 0f
        m[2] = -scaleLen
        m[3] = 0f

        // Col 1: raw Y maps to +worldX (width across tracks)
        m[4] = scaleWidth
        m[5] = 0f
        m[6] = 0f
        m[7] = 0f

        // Col 2: raw Z maps to +worldY (vertical rail elevation)
        m[8] = 0f
        m[9] = scaleHeight
        m[10] = 0f
        m[11] = 0f

        // Col 3: Translation with center offsets
        m[12] = 0f - cy * scaleWidth
        m[13] = 0f - czBase * scaleHeight
        m[14] = centerZ + cx * scaleLen
        m[15] = 1.0f

        val customModelMatrix = Matrix4(m)
        Matrix4.multiply(mvpMatrix, vpMatrix, customModelMatrix)

        android.opengl.GLES30.glDisable(android.opengl.GLES30.GL_CULL_FACE)
        activeShader.bind()
        activeShader.setModelMatrix(customModelMatrix.values)
        activeShader.setMVPMatrix(mvpMatrix.values)

        activeModel.renderPrimitive(prim, activeShader)
    }

    /**
     * Renders an authentic Subway Surfers train carriage blocking a lane.
     */
    fun renderTrain(vpMatrix: Matrix4, laneX: Float, y: Float, z: Float) {
        val activeModel = model ?: return
        val activeShader = shader ?: return
        val prim = trainPrimitives.firstOrNull() ?: return

        // Raw Train: cx = -26715.4f, cy = -1333.1f, czBase = 119.3f
        // Dimensions: dx = 702.3, dy = 240.7, dz = 353.6
        val cx = -26715.4f
        val cy = -1333.1f
        val czBase = 119.3f
        val s = 0.0090f // Centimeters to meters

        // worldX = (rawY - cy) * s + laneX
        // worldY = (rawZ - czBase) * s + y
        // worldZ = -(rawX - cx) * s + z
        val m = FloatArray(16)
        // Col 0: raw X maps to -Z (train length along track)
        m[0] = 0f
        m[1] = 0f
        m[2] = -s
        m[3] = 0f

        // Col 1: raw Y maps to +X (train width across lane)
        m[4] = s
        m[5] = 0f
        m[6] = 0f
        m[7] = 0f

        // Col 2: raw Z maps to +Y (train height)
        m[8] = 0f
        m[9] = s
        m[10] = 0f
        m[11] = 0f

        // Col 3: World position + offset
        m[12] = laneX - cy * s
        m[13] = y - czBase * s
        m[14] = z + cx * s
        m[15] = 1.0f

        val customModelMatrix = Matrix4(m)
        Matrix4.multiply(mvpMatrix, vpMatrix, customModelMatrix)

        android.opengl.GLES30.glDisable(android.opengl.GLES30.GL_CULL_FACE)
        activeShader.bind()
        activeShader.setModelMatrix(customModelMatrix.values)
        activeShader.setMVPMatrix(mvpMatrix.values)

        activeModel.renderPrimitive(prim, activeShader)
    }

    /**
     * Renders an authentic Subway Surfers hurdle / barrier.
     */
    fun renderHurdle(vpMatrix: Matrix4, laneX: Float, y: Float, z: Float, scaleMultiplier: Float = 0.45f) {
        val activeModel = model ?: return
        val activeShader = shader ?: return
        val prim = hurdlePrimitives.firstOrNull() ?: return

        // Raw Hurdle: cx = -6374.9f, cy = -1268.2f, czBase = 121.4f
        // dx = 616.2 (width across lane), dy = 212.4 (depth along track), dz = 310.7 (height)
        val cx = -6374.9f
        val cy = -1268.2f
        val czBase = 121.4f

        // Scaled to fit 2.2-meter lane (width ~2.15m)
        val s = 0.0035f * (scaleMultiplier / 0.45f)

        // worldX = (rawX - cx) * s + laneX
        // worldY = (rawZ - czBase) * s + y
        // worldZ = -(rawY - cy) * s + z
        val m = FloatArray(16)
        // Col 0: raw X maps to +X (hurdle width across lane)
        m[0] = s
        m[1] = 0f
        m[2] = 0f
        m[3] = 0f

        // Col 1: raw Y maps to -Z (hurdle depth along track)
        m[4] = 0f
        m[5] = 0f
        m[6] = -s
        m[7] = 0f

        // Col 2: raw Z maps to +Y (hurdle height)
        m[8] = 0f
        m[9] = s
        m[10] = 0f
        m[11] = 0f

        // Col 3: World position + offset
        m[12] = laneX - cx * s
        m[13] = y - czBase * s
        m[14] = z + cy * s
        m[15] = 1.0f

        val customModelMatrix = Matrix4(m)
        Matrix4.multiply(mvpMatrix, vpMatrix, customModelMatrix)

        android.opengl.GLES30.glDisable(android.opengl.GLES30.GL_CULL_FACE)
        activeShader.bind()
        activeShader.setModelMatrix(customModelMatrix.values)
        activeShader.setMVPMatrix(mvpMatrix.values)

        activeModel.renderPrimitive(prim, activeShader)
    }

    /**
     * Renders overhead bridge / viaduct archway across the track.
     */
    fun renderBridge(vpMatrix: Matrix4, z: Float) {
        val activeModel = model ?: return
        val activeShader = shader ?: return
        val prim = bridgePrimitives.firstOrNull() ?: return

        val cx = -16146.4f
        val cy = -1177.7f
        val czBase = 67.2f
        val s = 0.009f

        val m = FloatArray(16)
        // Col 0: raw X maps to -Z
        m[0] = 0f
        m[1] = 0f
        m[2] = -s
        m[3] = 0f

        // Col 1: raw Y maps to +X
        m[4] = s
        m[5] = 0f
        m[6] = 0f
        m[7] = 0f

        // Col 2: raw Z maps to +Y
        m[8] = 0f
        m[9] = s
        m[10] = 0f
        m[11] = 0f

        // Col 3: World position + offset
        m[12] = 0f - cy * s
        m[13] = 0f - czBase * s
        m[14] = z + cx * s
        m[15] = 1.0f

        val customModelMatrix = Matrix4(m)
        Matrix4.multiply(mvpMatrix, vpMatrix, customModelMatrix)

        android.opengl.GLES30.glDisable(android.opengl.GLES30.GL_CULL_FACE)
        activeShader.bind()
        activeShader.setModelMatrix(customModelMatrix.values)
        activeShader.setMVPMatrix(mvpMatrix.values)

        activeModel.renderPrimitive(prim, activeShader)
    }

    /**
     * Renders city background skyline and station walls flanking the tracks.
     */
    fun renderScenery(vpMatrix: Matrix4, z: Float) {
        val activeModel = model ?: return
        val activeShader = shader ?: return
        val prim = sceneryPrimitives.firstOrNull() ?: return

        val cx = -26710.7f
        val cy = -1113.1f
        val czBase = -724.1f
        val s = 0.008f

        val m = FloatArray(16)
        // Col 0: raw X maps to -Z
        m[0] = 0f
        m[1] = 0f
        m[2] = -s
        m[3] = 0f

        // Col 1: raw Y maps to +X
        m[4] = s
        m[5] = 0f
        m[6] = 0f
        m[7] = 0f

        // Col 2: raw Z maps to +Y
        m[8] = 0f
        m[9] = s
        m[10] = 0f
        m[11] = 0f

        // Col 3: World position + offset
        m[12] = 0f - cy * s
        m[13] = 0f - czBase * s
        m[14] = z + cx * s
        m[15] = 1.0f

        val customModelMatrix = Matrix4(m)
        Matrix4.multiply(mvpMatrix, vpMatrix, customModelMatrix)

        android.opengl.GLES30.glDisable(android.opengl.GLES30.GL_CULL_FACE)
        activeShader.bind()
        activeShader.setModelMatrix(customModelMatrix.values)
        activeShader.setMVPMatrix(mvpMatrix.values)

        activeModel.renderPrimitive(prim, activeShader)
    }

    fun release() {
        model?.release()
        model = null
        shader = null
    }
}
