package com.example.longrunner.game.graphics.gltf

import com.example.longrunner.game.graphics.Matrix4
import com.example.longrunner.game.graphics.Quaternion

class GlbAnimationController(private val model: GlbModel) {

    private var currentAnim: GlbAnimation? = null
    var currentTime: Float = 0f
        private set
    var isLooping: Boolean = true
    var speedMultiplier: Float = 1.0f

    private val jointCount = model.skin?.joints?.size ?: 0
    val boneMatrices: FloatArray = FloatArray(jointCount * 12) // 3x4 affine rows per joint

    // Per-node evaluated local transforms
    private val nodeTranslations = Array(model.nodes.size) { FloatArray(3) }
    private val nodeRotations = Array(model.nodes.size) { Quaternion() }
    private val nodeScales = Array(model.nodes.size) { FloatArray(3) }

    // Snapshot of previous pose for smooth cross-fading
    private val prevNodeTranslations = Array(model.nodes.size) { FloatArray(3) }
    private val prevNodeRotations = Array(model.nodes.size) { Quaternion() }
    private val prevNodeScales = Array(model.nodes.size) { FloatArray(3) }
    private var isBlending: Boolean = false
    private var blendTimer: Float = 0f
    private var blendDuration: Float = 0.08f

    private val localMatrices = Array(model.nodes.size) { Matrix4() }
    private val globalMatrices = Array(model.nodes.size) { Matrix4() }
    private val tempInvBind = FloatArray(16)
    private val tempJointSkin = Matrix4()
    private val tempRotMat = FloatArray(16)

    init {
        // Default to Idle if present, or first available animation
        play("Idle", loop = true, blendDuration = 0f)
    }

    fun play(
        animName: String,
        loop: Boolean = true,
        speed: Float = 1.0f,
        startTime: Float = 0f,
        blendDuration: Float = 0.08f
    ) {
        val anim = model.animations[animName] ?: model.animations.values.firstOrNull() ?: return
        if (currentAnim?.name == anim.name) {
            isLooping = loop
            speedMultiplier = speed
            return
        }

        // Snapshot current pose for smooth cross-fading
        if (currentAnim != null && blendDuration > 0f) {
            for (i in model.nodes.indices) {
                prevNodeTranslations[i][0] = nodeTranslations[i][0]
                prevNodeTranslations[i][1] = nodeTranslations[i][1]
                prevNodeTranslations[i][2] = nodeTranslations[i][2]

                prevNodeRotations[i].set(nodeRotations[i])

                prevNodeScales[i][0] = nodeScales[i][0]
                prevNodeScales[i][1] = nodeScales[i][1]
                prevNodeScales[i][2] = nodeScales[i][2]
            }
            isBlending = true
            this.blendDuration = blendDuration
            this.blendTimer = 0f
        } else {
            isBlending = false
        }

        currentAnim = anim
        currentTime = startTime.coerceAtLeast(0f)
        isLooping = loop
        speedMultiplier = speed
    }

    fun update(dt: Float) {
        val anim = currentAnim
        if (anim != null && anim.duration > 0f) {
            currentTime += dt * speedMultiplier
            if (currentTime >= anim.duration) {
                if (isLooping) {
                    currentTime %= anim.duration
                } else {
                    currentTime = anim.duration
                }
            }
        }

        if (isBlending) {
            blendTimer += dt
            if (blendTimer >= blendDuration) {
                isBlending = false
            }
        }

        evaluatePose()
    }

    private fun evaluatePose() {
        // Reset to node default rest pose
        for (i in model.nodes.indices) {
            val node = model.nodes[i]
            nodeTranslations[i][0] = node.translation[0]
            nodeTranslations[i][1] = node.translation[1]
            nodeTranslations[i][2] = node.translation[2]

            nodeRotations[i].set(node.rotation)

            nodeScales[i][0] = node.scale[0]
            nodeScales[i][1] = node.scale[1]
            nodeScales[i][2] = node.scale[2]
        }

        // Apply animation channels
        val anim = currentAnim
        if (anim != null) {
            for (channel in anim.channels) {
                val nodeIdx = channel.targetNode
                if (nodeIdx !in model.nodes.indices) continue
                val sampler = anim.samplers[channel.samplerIndex]
                val times = sampler.times
                if (times.isEmpty()) continue

                // Find keyframe interval
                val t = currentTime.coerceIn(0f, times.last())
                var k0 = 0
                var k1 = times.size - 1
                if (t <= times[0]) {
                    k0 = 0
                    k1 = 0
                } else if (t >= times.last()) {
                    k0 = times.size - 1
                    k1 = times.size - 1
                } else {
                    for (i in 0 until times.size - 1) {
                        if (t >= times[i] && t <= times[i + 1]) {
                            k0 = i
                            k1 = i + 1
                            break
                        }
                    }
                }

                val factor = if (k0 == k1 || times[k1] == times[k0]) 0f else (t - times[k0]) / (times[k1] - times[k0])

                when (channel.targetPath) {
                    "translation" -> {
                        val v0x = sampler.values[k0 * 3]
                        val v0y = sampler.values[k0 * 3 + 1]
                        val v0z = sampler.values[k0 * 3 + 2]
                        val v1x = sampler.values[k1 * 3]
                        val v1y = sampler.values[k1 * 3 + 1]
                        val v1z = sampler.values[k1 * 3 + 2]

                        nodeTranslations[nodeIdx][0] = v0x + (v1x - v0x) * factor
                        nodeTranslations[nodeIdx][1] = v0y + (v1y - v0y) * factor
                        nodeTranslations[nodeIdx][2] = v0z + (v1z - v0z) * factor
                    }
                    "rotation" -> {
                        val q0 = Quaternion(
                            sampler.values[k0 * 4],
                            sampler.values[k0 * 4 + 1],
                            sampler.values[k0 * 4 + 2],
                            sampler.values[k0 * 4 + 3]
                        )
                        val q1 = Quaternion(
                            sampler.values[k1 * 4],
                            sampler.values[k1 * 4 + 1],
                            sampler.values[k1 * 4 + 2],
                            sampler.values[k1 * 4 + 3]
                        )
                        Quaternion.slerp(q0, q1, factor, nodeRotations[nodeIdx])
                    }
                    "scale" -> {
                        val s0x = sampler.values[k0 * 3]
                        val s0y = sampler.values[k0 * 3 + 1]
                        val s0z = sampler.values[k0 * 3 + 2]
                        val s1x = sampler.values[k1 * 3]
                        val s1y = sampler.values[k1 * 3 + 1]
                        val s1z = sampler.values[k1 * 3 + 2]

                        nodeScales[nodeIdx][0] = s0x + (s1x - s0x) * factor
                        nodeScales[nodeIdx][1] = s0y + (s1y - s0y) * factor
                        nodeScales[nodeIdx][2] = s0z + (s1z - s0z) * factor
                    }
                }
            }
        }

        // Apply crossfade blending if active
        if (isBlending && blendDuration > 0.0001f) {
            val factor = (blendTimer / blendDuration).coerceIn(0f, 1f)
            for (i in model.nodes.indices) {
                val pTrans = prevNodeTranslations[i]
                val cTrans = nodeTranslations[i]
                cTrans[0] = pTrans[0] + (cTrans[0] - pTrans[0]) * factor
                cTrans[1] = pTrans[1] + (cTrans[1] - pTrans[1]) * factor
                cTrans[2] = pTrans[2] + (cTrans[2] - pTrans[2]) * factor

                Quaternion.slerp(prevNodeRotations[i], nodeRotations[i], factor, nodeRotations[i])

                val pScale = prevNodeScales[i]
                val cScale = nodeScales[i]
                cScale[0] = pScale[0] + (cScale[0] - pScale[0]) * factor
                cScale[1] = pScale[1] + (cScale[1] - pScale[1]) * factor
                cScale[2] = pScale[2] + (cScale[2] - pScale[2]) * factor
            }
        }

        // Build local matrix: M = Translate * Rotate * Scale
        for (i in model.nodes.indices) {
            val m = localMatrices[i]
            m.identity()
            m.translate(nodeTranslations[i][0], nodeTranslations[i][1], nodeTranslations[i][2])
            nodeRotations[i].toMatrix(tempRotMat)
            Matrix4.multiply(m, m, tempRotMat)
            m.scale(nodeScales[i][0], nodeScales[i][1], nodeScales[i][2])
        }

        // Build global hierarchy: root nodes first, children inherit
        for (i in model.nodes.indices) {
            updateGlobalMatrix(i)
        }

        // Compute final bone palette: JointGlobal * InverseBind
        val skin = model.skin ?: return
        for (i in skin.joints.indices) {
            val jointNodeIdx = skin.joints[i]
            val jointGlobal = globalMatrices[jointNodeIdx]

            System.arraycopy(skin.inverseBindMatrices, i * 16, tempInvBind, 0, 16)
            Matrix4.multiply(tempJointSkin, jointGlobal, tempInvBind)

            // Extract 3x4 affine matrix rows into boneMatrices uniform
            val v = tempJointSkin.values
            val outIdx = i * 12

            // Row 0: v[0], v[4], v[8], v[12]
            boneMatrices[outIdx + 0] = v[0]
            boneMatrices[outIdx + 1] = v[4]
            boneMatrices[outIdx + 2] = v[8]
            boneMatrices[outIdx + 3] = v[12]

            // Row 1: v[1], v[5], v[9], v[13]
            boneMatrices[outIdx + 4] = v[1]
            boneMatrices[outIdx + 5] = v[5]
            boneMatrices[outIdx + 6] = v[9]
            boneMatrices[outIdx + 7] = v[13]

            // Row 2: v[2], v[6], v[10], v[14]
            boneMatrices[outIdx + 8] = v[2]
            boneMatrices[outIdx + 9] = v[6]
            boneMatrices[outIdx + 10] = v[10]
            boneMatrices[outIdx + 11] = v[14]
        }
    }

    private fun updateGlobalMatrix(nodeIdx: Int) {
        val parent = model.nodes[nodeIdx].parent
        if (parent >= 0) {
            Matrix4.multiply(globalMatrices[nodeIdx], globalMatrices[parent], localMatrices[nodeIdx])
        } else {
            globalMatrices[nodeIdx].copyFrom(localMatrices[nodeIdx])
        }
    }
}
