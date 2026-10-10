package com.example.longrunner.game.player

import com.example.longrunner.game.graphics.Material
import com.example.longrunner.game.graphics.Matrix4
import com.example.longrunner.game.graphics.Mesh
import com.example.longrunner.game.graphics.MeshBuilder
import com.example.longrunner.game.graphics.Shader
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural 3D Character Renderer with articulated skeletal hierarchy.
 * Supports distinct silhouettes and accessories for Kai, Zara, Jax, Nova, and Mira.
 */
class PlayerVisual(initialData: CharacterData = CharacterData.JAKE) {

    var data: CharacterData = initialData
        private set

    private var torsoMesh: Mesh
    private var headMesh: Mesh
    private var visorMesh: Mesh
    private var armMesh: Mesh
    private var legMesh: Mesh

    // Unique accessory meshes for silhouettes
    private var accessoryMeshA: Mesh? = null
    private var accessoryMeshB: Mesh? = null

    // Power-up visual meshes
    private val shieldBubbleMesh: Mesh = MeshBuilder.createKineticShieldBubbleMesh()
    private val overdriveTrailMesh: Mesh = MeshBuilder.createOverdriveTrailMesh()
    private val magnetFieldMesh: Mesh = MeshBuilder.createMagnetFieldMesh()
    private var currentHoverboardId: String = ""
    private var hoverboardMesh: Mesh = MeshBuilder.createNeonHoverboardMesh()

    fun updateEquippedHoverboard(board: com.example.longrunner.game.powerups.HoverboardData) {
        if (currentHoverboardId == board.id) return
        currentHoverboardId = board.id
        hoverboardMesh.release()
        hoverboardMesh = MeshBuilder.createNeonHoverboardMesh(board.primaryMaterial, board.accentMaterial)
    }

    private val modelMatrix = Matrix4()
    private val mvpMatrix = Matrix4()

    private var accessoryAnimTime: Float = 0f

    init {
        val meshes = buildCharacterMeshes(initialData)
        torsoMesh = meshes.torso
        headMesh = meshes.head
        visorMesh = meshes.visor
        armMesh = meshes.arm
        legMesh = meshes.leg
        accessoryMeshA = meshes.accessoryA
        accessoryMeshB = meshes.accessoryB
    }

    fun setCharacter(newData: CharacterData) {
        if (data.id == newData.id) return
        release()
        data = newData
        val meshes = buildCharacterMeshes(newData)
        torsoMesh = meshes.torso
        headMesh = meshes.head
        visorMesh = meshes.visor
        armMesh = meshes.arm
        legMesh = meshes.leg
        accessoryMeshA = meshes.accessoryA
        accessoryMeshB = meshes.accessoryB
    }

    private class MeshBundle(
        val torso: Mesh,
        val head: Mesh,
        val visor: Mesh,
        val arm: Mesh,
        val leg: Mesh,
        val accessoryA: Mesh?,
        val accessoryB: Mesh?
    )

    private fun buildCharacterMeshes(charData: CharacterData): MeshBundle {
        val primary = charData.primaryColor
        val accent = charData.accentColor

        // 1. Torso: Athletic cybernetic chest with glowing accent spine
        val torsoBuilder = MeshBuilder()
        when (charData.silhouetteType) {
            SilhouetteType.HEAVY -> {
                // Jax: Bulkier, wider reinforced exo-torso
                torsoBuilder.addBox(0f, 0f, 0f, 0.68f, 0.70f, 0.40f, primary)
                torsoBuilder.addBox(0f, 0.05f, 0.21f, 0.22f, 0.50f, 0.05f, accent)
                // Heavy breastplate chevron
                torsoBuilder.addBox(0f, 0.18f, 0.22f, 0.40f, 0.12f, 0.05f, accent)
            }
            SilhouetteType.AERODYNAMIC -> {
                // Zara: Lean, streamlined speedster chest
                torsoBuilder.addBox(0f, 0f, 0f, 0.48f, 0.62f, 0.28f, primary)
                torsoBuilder.addBox(0f, 0.05f, 0.15f, 0.12f, 0.45f, 0.04f, accent)
            }
            SilhouetteType.MYSTIC -> {
                // Nova: Technomancer tunic with conduit lines
                torsoBuilder.addBox(0f, 0f, 0f, 0.52f, 0.65f, 0.30f, primary)
                torsoBuilder.addBox(0f, 0.05f, 0.16f, 0.16f, 0.48f, 0.04f, accent)
                // Core chest orb
                torsoBuilder.addBox(0f, 0.12f, 0.17f, 0.16f, 0.16f, 0.06f, accent)
            }
            SilhouetteType.ACROBATIC -> {
                // Mira: Flexible parkour suit
                torsoBuilder.addBox(0f, 0f, 0f, 0.50f, 0.64f, 0.29f, primary)
                torsoBuilder.addBox(0f, 0.04f, 0.15f, 0.13f, 0.42f, 0.04f, accent)
            }
            SilhouetteType.BALANCED -> {
                // Kai: Balanced athletic cyber chassis
                torsoBuilder.addBox(0f, 0f, 0f, 0.55f, 0.65f, 0.32f, primary)
                torsoBuilder.addBox(0f, 0.05f, 0.17f, 0.14f, 0.45f, 0.04f, accent)
            }
        }
        val torso = torsoBuilder.build()

        // 2. Head: Cyber helmet
        val headBuilder = MeshBuilder()
        when (charData.silhouetteType) {
            SilhouetteType.HEAVY -> headBuilder.addBox(0f, 0f, 0f, 0.36f, 0.36f, 0.36f, primary)
            SilhouetteType.AERODYNAMIC -> headBuilder.addBox(0f, 0f, 0f, 0.30f, 0.32f, 0.30f, primary)
            else -> headBuilder.addBox(0f, 0f, 0f, 0.32f, 0.34f, 0.32f, primary)
        }
        val head = headBuilder.build()

        // 3. Visor: Glowing optical sensor array
        val visorBuilder = MeshBuilder()
        when (charData.silhouetteType) {
            SilhouetteType.HEAVY -> {
                // Narrow slit visor
                visorBuilder.addBox(0f, 0f, 0.19f, 0.32f, 0.08f, 0.05f, accent)
            }
            SilhouetteType.AERODYNAMIC -> {
                // Sharp wraparound visor
                visorBuilder.addBox(0f, 0f, 0.16f, 0.30f, 0.14f, 0.05f, accent)
            }
            SilhouetteType.MYSTIC -> {
                // Triple optical diode lenses
                visorBuilder.addBox(0f, 0f, 0.17f, 0.24f, 0.10f, 0.05f, accent)
                visorBuilder.addBox(0f, 0.10f, 0.17f, 0.08f, 0.08f, 0.05f, accent)
            }
            SilhouetteType.ACROBATIC -> {
                // Dual goggles
                visorBuilder.addBox(-0.08f, 0f, 0.16f, 0.12f, 0.12f, 0.05f, accent)
                visorBuilder.addBox(0.08f, 0f, 0.16f, 0.12f, 0.12f, 0.05f, accent)
            }
            SilhouetteType.BALANCED -> {
                visorBuilder.addBox(0f, 0f, 0.17f, 0.28f, 0.12f, 0.05f, accent)
            }
        }
        val visor = visorBuilder.build()

        // 4. Limbs: Arms and Legs
        val armBuilder = MeshBuilder()
        val legBuilder = MeshBuilder()
        when (charData.silhouetteType) {
            SilhouetteType.HEAVY -> {
                armBuilder.addBox(0f, -0.25f, 0f, 0.20f, 0.50f, 0.20f, primary)
                armBuilder.addBox(0f, -0.30f, 0.10f, 0.18f, 0.22f, 0.06f, accent) // Gauntlet

                legBuilder.addBox(0f, -0.32f, 0f, 0.22f, 0.64f, 0.22f, primary)
                legBuilder.addBox(0f, -0.15f, 0.12f, 0.16f, 0.18f, 0.05f, accent)
            }
            SilhouetteType.AERODYNAMIC -> {
                armBuilder.addBox(0f, -0.25f, 0f, 0.13f, 0.50f, 0.14f, primary)
                legBuilder.addBox(0f, -0.32f, 0f, 0.16f, 0.64f, 0.16f, primary)
                legBuilder.addBox(0f, -0.15f, 0.09f, 0.10f, 0.14f, 0.04f, accent)
            }
            else -> {
                armBuilder.addBox(0f, -0.25f, 0f, 0.15f, 0.50f, 0.16f, primary)
                legBuilder.addBox(0f, -0.32f, 0f, 0.18f, 0.64f, 0.18f, primary)
                legBuilder.addBox(0f, -0.15f, 0.10f, 0.12f, 0.14f, 0.04f, accent)
            }
        }
        val arm = armBuilder.build()
        val leg = legBuilder.build()

        // 5. Unique Accessories per Silhouette
        var accA: Mesh? = null
        var accB: Mesh? = null

        when (charData.silhouetteType) {
            SilhouetteType.AERODYNAMIC -> {
                // Zara: Twin aerodynamic back fins angled backwards
                val finBuilder = MeshBuilder()
                finBuilder.addBox(0f, 0f, 0f, 0.06f, 0.35f, 0.22f, accent)
                accA = finBuilder.build()
            }
            SilhouetteType.HEAVY -> {
                // Jax: Bulky shoulder pauldrons
                val pauldronBuilder = MeshBuilder()
                pauldronBuilder.addBox(0f, 0f, 0f, 0.26f, 0.16f, 0.24f, accent)
                accA = pauldronBuilder.build()
            }
            SilhouetteType.MYSTIC -> {
                // Nova: Floating quantum halo crown disc
                val haloBuilder = MeshBuilder()
                haloBuilder.addBox(0f, 0f, 0f, 0.38f, 0.04f, 0.38f, accent)
                haloBuilder.addBox(0f, 0f, 0f, 0.20f, 0.05f, 0.20f, primary)
                accA = haloBuilder.build()
            }
            SilhouetteType.ACROBATIC -> {
                // Mira: Dual flowing trailing streamers behind waist
                val ribbonBuilder = MeshBuilder()
                ribbonBuilder.addBox(0f, 0f, 0f, 0.06f, 0.40f, 0.04f, accent)
                accA = ribbonBuilder.build()
            }
            SilhouetteType.BALANCED -> {
                // Kai: Sleek back stabilizer module
                val ridgeBuilder = MeshBuilder()
                ridgeBuilder.addBox(0f, 0f, 0f, 0.08f, 0.45f, 0.10f, accent)
                accA = ridgeBuilder.build()
            }
        }

        return MeshBundle(torso, head, visor, arm, leg, accA, accB)
    }

    fun render(
        shader: Shader,
        vpMatrix: Matrix4,
        x: Float,
        y: Float,
        z: Float,
        isJumping: Boolean,
        isSliding: Boolean,
        stridePhase: Float,
        dt: Float = 0.016f,
        hasShield: Boolean = false,
        isOverdrive: Boolean = false,
        isMagnet: Boolean = false,
        isHoverboard: Boolean = false
    ) {
        accessoryAnimTime += dt

        val runAngle = sin(stridePhase) * 35.0f
        val oppAngle = -runAngle

        if (isSliding) {
            // Sliding posture
            val baseY = y + 0.35f

            renderPart(shader, vpMatrix, x, baseY, z, torsoMesh, pitch = -40f)
            renderPart(shader, vpMatrix, x, baseY + 0.30f, z + 0.25f, headMesh, pitch = -25f)
            renderPart(shader, vpMatrix, x, baseY + 0.30f, z + 0.25f, visorMesh, pitch = -25f)

            // Arms back for balance
            renderPart(shader, vpMatrix, x - 0.35f, baseY + 0.1f, z - 0.2f, armMesh, pitch = 45f)
            renderPart(shader, vpMatrix, x + 0.35f, baseY + 0.1f, z - 0.2f, armMesh, pitch = 45f)

            // Legs extended forward
            renderPart(shader, vpMatrix, x - 0.16f, baseY - 0.1f, z - 0.35f, legMesh, pitch = -75f)
            renderPart(shader, vpMatrix, x + 0.16f, baseY - 0.1f, z - 0.35f, legMesh, pitch = -70f)

            // Render accessories in slide posture
            renderAccessories(shader, vpMatrix, x, baseY, z, pitchOffset = -40f)

        } else if (isJumping) {
            // Jumping posture: Leaping tuck
            val baseY = y + 0.75f

            renderPart(shader, vpMatrix, x, baseY + 0.35f, z, torsoMesh)
            renderPart(shader, vpMatrix, x, baseY + 0.82f, z, headMesh)
            renderPart(shader, vpMatrix, x, baseY + 0.82f, z, visorMesh)

            // Arms raised forward/up
            renderPart(shader, vpMatrix, x - 0.38f, baseY + 0.45f, z, armMesh, pitch = -60f)
            renderPart(shader, vpMatrix, x + 0.38f, baseY + 0.45f, z, armMesh, pitch = -60f)

            // Legs bent/tucked
            renderPart(shader, vpMatrix, x - 0.16f, baseY + 0.05f, z, legMesh, pitch = 35f)
            renderPart(shader, vpMatrix, x + 0.16f, baseY + 0.05f, z, legMesh, pitch = 25f)

            renderAccessories(shader, vpMatrix, x, baseY, z, pitchOffset = 0f)

        } else {
            // Running posture: Full dynamic stride
            val bobbing = sin(stridePhase * 2.0f) * 0.06f
            val baseY = y + 0.65f + bobbing

            renderPart(shader, vpMatrix, x, baseY + 0.35f, z, torsoMesh, pitch = 8f)
            renderPart(shader, vpMatrix, x, baseY + 0.82f, z + 0.05f, headMesh, pitch = 4f)
            renderPart(shader, vpMatrix, x, baseY + 0.82f, z + 0.05f, visorMesh, pitch = 4f)

            // Arms swing opposing legs
            val armOffsetX = if (data.silhouetteType == SilhouetteType.HEAVY) 0.44f else 0.38f
            renderPart(shader, vpMatrix, x - armOffsetX, baseY + 0.55f, z, armMesh, pitch = oppAngle)
            renderPart(shader, vpMatrix, x + armOffsetX, baseY + 0.55f, z, armMesh, pitch = runAngle)

            // Legs swing
            val legOffsetX = if (data.silhouetteType == SilhouetteType.HEAVY) 0.19f else 0.16f
            renderPart(shader, vpMatrix, x - legOffsetX, baseY + 0.1f, z, legMesh, pitch = runAngle)
            renderPart(shader, vpMatrix, x + legOffsetX, baseY + 0.1f, z, legMesh, pitch = oppAngle)

            renderAccessories(shader, vpMatrix, x, baseY, z, pitchOffset = 8f)
        }

        renderPowerUpVfx(
            shader = shader,
            vpMatrix = vpMatrix,
            x = x,
            y = y,
            z = z,
            dt = dt,
            hasShield = hasShield,
            isOverdrive = isOverdrive,
            isMagnet = isMagnet,
            isHoverboard = isHoverboard,
            isSliding = isSliding,
            isJumping = isJumping,
            stridePhase = stridePhase
        )
    }

    fun renderPowerUpVfx(
        shader: Shader,
        vpMatrix: Matrix4,
        x: Float,
        y: Float,
        z: Float,
        dt: Float = 0.016f,
        hasShield: Boolean = false,
        isOverdrive: Boolean = false,
        isMagnet: Boolean = false,
        isHoverboard: Boolean = false,
        isSliding: Boolean = false,
        isJumping: Boolean = false,
        stridePhase: Float = 0f
    ) {
        accessoryAnimTime += dt

        // Render Kinetic Aegis Drone: Miniature floating deflector drone orbiting runner
        if (hasShield) {
            val shieldOrbitAngle = accessoryAnimTime * 3.2f + 3.14159f // opposite phase to magnet drone
            val shieldOrbitRadius = 0.68f
            val shieldOrbitX = x + kotlin.math.cos(shieldOrbitAngle) * shieldOrbitRadius
            val shieldOrbitZ = z + kotlin.math.sin(shieldOrbitAngle) * shieldOrbitRadius
            val shieldBobY = kotlin.math.cos(accessoryAnimTime * 5.5f) * 0.07f
            val shieldOrbitY = y + 1.45f + shieldBobY
            val shieldSpinYaw = (accessoryAnimTime * 150f) % 360f
            val shieldTiltPitch = kotlin.math.sin(accessoryAnimTime * 3.5f) * 10f
            renderPart(shader, vpMatrix, shieldOrbitX, shieldOrbitY, shieldOrbitZ, shieldBubbleMesh, pitch = shieldTiltPitch, yaw = shieldSpinYaw)
        }

        // Render Overdrive Thruster Micro Plasma Jets: Compact exhaust flames tight to runner's upper back
        if (isOverdrive) {
            renderPart(shader, vpMatrix, x, y + 0.15f, z, overdriveTrailMesh)
        }

        // Render Quantum Magnet Satellite Drone: Miniature floating magnet revolving around runner
        if (isMagnet) {
            val orbitAngle = accessoryAnimTime * 3.5f
            val orbitRadius = 0.70f
            val orbitX = x + kotlin.math.cos(orbitAngle) * orbitRadius
            val orbitZ = z + kotlin.math.sin(orbitAngle) * orbitRadius
            val bobY = kotlin.math.sin(accessoryAnimTime * 6.0f) * 0.08f
            val orbitY = y + 1.55f + bobY
            val selfSpinYaw = (accessoryAnimTime * 180f) % 360f
            val selfTiltPitch = kotlin.math.sin(accessoryAnimTime * 4.0f) * 12f
            renderPart(shader, vpMatrix, orbitX, orbitY, orbitZ, magnetFieldMesh, pitch = selfTiltPitch, yaw = selfSpinYaw)
        }

        // Render Neon Mag-Lev Hoverboard: Futuristic cyber hoverboard deck gliding under runner's feet
        if (isHoverboard) {
            val boardFloatY = kotlin.math.sin(accessoryAnimTime * 8.0f) * 0.025f
            val boardY = y + 0.08f + boardFloatY
            val boardPitch = if (isSliding) -20f else (if (isJumping) -8f else 3f)
            val boardRoll = kotlin.math.sin(stridePhase * 1.0f) * 3.5f
            renderPart(shader, vpMatrix, x, boardY, z, hoverboardMesh, pitch = boardPitch, roll = boardRoll)
        }
    }

    private fun renderAccessories(
        shader: Shader,
        vpMatrix: Matrix4,
        x: Float,
        baseY: Float,
        z: Float,
        pitchOffset: Float
    ) {
        val acc = accessoryMeshA ?: return

        when (data.silhouetteType) {
            SilhouetteType.AERODYNAMIC -> {
                // Zara: Left and right aerodynamic shoulder fins
                renderPart(shader, vpMatrix, x - 0.22f, baseY + 0.42f, z - 0.16f, acc, pitch = pitchOffset + 25f, yaw = -15f)
                renderPart(shader, vpMatrix, x + 0.22f, baseY + 0.42f, z - 0.16f, acc, pitch = pitchOffset + 25f, yaw = 15f)
            }
            SilhouetteType.HEAVY -> {
                // Jax: Bulky shoulder pauldrons
                renderPart(shader, vpMatrix, x - 0.42f, baseY + 0.62f, z, acc, pitch = pitchOffset)
                renderPart(shader, vpMatrix, x + 0.42f, baseY + 0.62f, z, acc, pitch = pitchOffset)
            }
            SilhouetteType.MYSTIC -> {
                // Nova: Hovering floating halo disc orbiting above head
                val haloBob = sin(accessoryAnimTime * 3.5f) * 0.04f
                val haloSpin = (accessoryAnimTime * 90f) % 360f
                renderPart(shader, vpMatrix, x, baseY + 1.08f + haloBob, z + 0.05f, acc, yaw = haloSpin)
            }
            SilhouetteType.ACROBATIC -> {
                // Mira: Dual trailing streamers fluttering behind waist
                val flutter1 = sin(accessoryAnimTime * 12f) * 15f
                val flutter2 = cos(accessoryAnimTime * 12f) * 15f
                renderPart(shader, vpMatrix, x - 0.18f, baseY + 0.20f, z - 0.18f, acc, pitch = pitchOffset + 35f + flutter1)
                renderPart(shader, vpMatrix, x + 0.18f, baseY + 0.20f, z - 0.18f, acc, pitch = pitchOffset + 35f + flutter2)
            }
            SilhouetteType.BALANCED -> {
                // Kai: Back stabilizer spine
                renderPart(shader, vpMatrix, x, baseY + 0.35f, z - 0.18f, acc, pitch = pitchOffset)
            }
        }
    }

    private fun renderPart(
        shader: Shader,
        vpMatrix: Matrix4,
        px: Float, py: Float, pz: Float,
        mesh: Mesh,
        pitch: Float = 0f,
        yaw: Float = 0f,
        roll: Float = 0f
    ) {
        modelMatrix.identity()
        modelMatrix.translate(px, py, pz)
        if (pitch != 0f) modelMatrix.rotate(pitch, 1f, 0f, 0f)
        if (yaw != 0f) modelMatrix.rotate(yaw, 0f, 1f, 0f)
        if (roll != 0f) modelMatrix.rotate(roll, 0f, 0f, 1f)

        Matrix4.multiply(mvpMatrix, vpMatrix, modelMatrix)

        shader.setModelMatrix(modelMatrix.values)
        shader.setMVPMatrix(mvpMatrix.values)
        mesh.render(shader)
    }

    fun release() {
        torsoMesh.release()
        headMesh.release()
        visorMesh.release()
        armMesh.release()
        legMesh.release()
        accessoryMeshA?.release()
        accessoryMeshB?.release()
        shieldBubbleMesh.release()
        overdriveTrailMesh.release()
        magnetFieldMesh.release()
        hoverboardMesh.release()
    }
}
