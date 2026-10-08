package com.example.longrunner.game.camera

import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.graphics.Matrix4
import com.example.longrunner.game.graphics.Vector3
import com.example.longrunner.game.player.PlayerController
import kotlin.math.min

class GameCamera {

    val position = Vector3()
    val target = Vector3()

    val viewMatrix = Matrix4()
    val projectionMatrix = Matrix4()
    val viewProjectionMatrix = Matrix4()

    private var aspectRatio: Float = 1.0f

    fun onSurfaceChanged(width: Int, height: Int) {
        aspectRatio = if (height > 0) width.toFloat() / height.toFloat() else 1.0f
        updateProjection()
    }

    fun updateProjection(fovY: Float = GameConstants.CAMERA_FOV) {
        Matrix4.perspective(projectionMatrix, fovY, aspectRatio, 0.5f, 220.0f)
    }

    fun reset(player: PlayerController) {
        position.set(
            player.x * 0.4f,
            player.y + GameConstants.CAMERA_FOLLOW_HEIGHT,
            player.z + GameConstants.CAMERA_FOLLOW_DISTANCE
        )
        target.set(
            player.x * 0.6f,
            player.y + GameConstants.CAMERA_LOOK_HEIGHT,
            player.z - GameConstants.CAMERA_LOOK_AHEAD_Z
        )
        updateView()
    }

    var isScreenShakeEnabled: Boolean = true

    fun update(player: PlayerController, dt: Float) {
        // Desired camera position
        val desiredX = player.x * 0.45f
        // Dynamic camera damping on jump/slide so the camera doesn't violently jerk
        val jumpYOffset = if (isScreenShakeEnabled && player.isJumping) (player.y - player.surfaceY) * 0.35f else 0f
        val slideYOffset = if (isScreenShakeEnabled && player.isSliding) -0.4f else 0f
        val desiredY = player.surfaceY + GameConstants.CAMERA_FOLLOW_HEIGHT + jumpYOffset + slideYOffset
        val desiredZ = player.z + GameConstants.CAMERA_FOLLOW_DISTANCE

        // Smooth camera following (lerp)
        val lerpFactor = min(1.0f, dt * 10.0f)
        position.x += (desiredX - position.x) * lerpFactor
        position.y += (desiredY - position.y) * lerpFactor
        position.z = desiredZ // Maintain locked forward distance to avoid rubber-banding

        // Desired look target
        val desiredTargetX = player.x * 0.7f
        val targetYOffset = if (isScreenShakeEnabled) ((player.y - player.surfaceY) * 0.25f) else 0f
        val desiredTargetY = player.surfaceY + GameConstants.CAMERA_LOOK_HEIGHT + targetYOffset
        val desiredTargetZ = player.z - GameConstants.CAMERA_LOOK_AHEAD_Z

        target.x += (desiredTargetX - target.x) * lerpFactor
        target.y += (desiredTargetY - target.y) * lerpFactor
        target.z = desiredTargetZ

        // Slight speed-based FOV stretch
        val speedFactor = (player.speed - GameConstants.BASE_SPEED) / (GameConstants.MAX_SPEED - GameConstants.BASE_SPEED)
        val dynamicFov = if (isScreenShakeEnabled) {
            GameConstants.CAMERA_FOV + (speedFactor * 6.0f)
        } else {
            GameConstants.CAMERA_FOV
        }
        updateProjection(dynamicFov)

        updateView()
    }

    private fun updateView() {
        Matrix4.lookAt(
            viewMatrix,
            position.x, position.y, position.z,
            target.x, target.y, target.z,
            0f, 1f, 0f
        )
        Matrix4.multiply(viewProjectionMatrix, projectionMatrix, viewMatrix)
    }
}
