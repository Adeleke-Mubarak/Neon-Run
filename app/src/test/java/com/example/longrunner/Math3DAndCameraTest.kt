package com.example.longrunner

import com.example.longrunner.game.camera.GameCamera
import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.graphics.AABB
import com.example.longrunner.game.graphics.Matrix4
import com.example.longrunner.game.graphics.Vector3
import com.example.longrunner.game.player.PlayerController
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.sqrt

class Math3DAndCameraTest {

    @Test
    fun testVector3BasicOperations() {
        val v1 = Vector3(1f, 2f, 3f)
        val v2 = Vector3(4f, 5f, 6f)

        val sum = v1 + v2
        assertEquals(5f, sum.x, 0.0001f)
        assertEquals(7f, sum.y, 0.0001f)
        assertEquals(9f, sum.z, 0.0001f)

        val diff = v2 - v1
        assertEquals(3f, diff.x, 0.0001f)
        assertEquals(3f, diff.y, 0.0001f)
        assertEquals(3f, diff.z, 0.0001f)

        val scaled = v1 * 2.5f
        assertEquals(2.5f, scaled.x, 0.0001f)
        assertEquals(5.0f, scaled.y, 0.0001f)
        assertEquals(7.5f, scaled.z, 0.0001f)

        val lenSq = v1.lengthSquared() // 1 + 4 + 9 = 14
        assertEquals(14f, lenSq, 0.0001f)
        assertEquals(sqrt(14f), v1.length(), 0.0001f)
    }

    @Test
    fun testVector3NormalizeAndZeroGuard() {
        val v = Vector3(3f, 0f, 4f)
        v.normalize()
        assertEquals(1.0f, v.length(), 0.0001f)
        assertEquals(0.6f, v.x, 0.0001f)
        assertEquals(0.0f, v.y, 0.0001f)
        assertEquals(0.8f, v.z, 0.0001f)

        // Zero vector guard must not produce NaN or Infinity
        val zero = Vector3(0f, 0f, 0f)
        zero.normalize()
        assertEquals(0f, zero.x, 0.0001f)
        assertEquals(0f, zero.y, 0.0001f)
        assertEquals(0f, zero.z, 0.0001f)
        assertFalse(zero.x.isNaN())
    }

    @Test
    fun testVector3Lerp() {
        val a = Vector3(0f, 10f, 20f)
        val b = Vector3(10f, 20f, 40f)

        val mid = Vector3.lerp(a, b, 0.5f)
        assertEquals(5f, mid.x, 0.0001f)
        assertEquals(15f, mid.y, 0.0001f)
        assertEquals(30f, mid.z, 0.0001f)

        a.lerp(b, 0.25f)
        assertEquals(2.5f, a.x, 0.0001f)
        assertEquals(12.5f, a.y, 0.0001f)
        assertEquals(25.0f, a.z, 0.0001f)
    }

    @Test
    fun testAABBSetAndIntersects() {
        val boxA = AABB().set(centerX = 0f, centerY = 1f, centerZ = -10f, halfW = 0.5f, halfH = 1.0f, halfD = 0.5f)
        assertEquals(-0.5f, boxA.minX, 0.0001f)
        assertEquals(0.5f, boxA.maxX, 0.0001f)
        assertEquals(0.0f, boxA.minY, 0.0001f)
        assertEquals(2.0f, boxA.maxY, 0.0001f)
        assertEquals(-10.5f, boxA.minZ, 0.0001f)
        assertEquals(-9.5f, boxA.maxZ, 0.0001f)

        // Overlapping box
        val boxB = AABB().set(centerX = 0.4f, centerY = 1f, centerZ = -10f, halfW = 0.5f, halfH = 1.0f, halfD = 0.5f)
        assertTrue(boxA.intersects(boxB))

        // Disjoint box on X
        val boxC = AABB().set(centerX = 3.0f, centerY = 1f, centerZ = -10f, halfW = 0.5f, halfH = 1.0f, halfD = 0.5f)
        assertFalse(boxA.intersects(boxC))

        // Disjoint box on Y
        val boxD = AABB().set(centerX = 0f, centerY = 5.0f, centerZ = -10f, halfW = 0.5f, halfH = 1.0f, halfD = 0.5f)
        assertFalse(boxA.intersects(boxD))

        // Disjoint box on Z
        val boxE = AABB().set(centerX = 0f, centerY = 1f, centerZ = -20f, halfW = 0.5f, halfH = 1.0f, halfD = 0.5f)
        assertFalse(boxA.intersects(boxE))
    }

    @Test
    fun testAABBIntersectsWithTolerance() {
        val boxA = AABB().set(0f, 0f, 0f, 1f, 1f, 1f)
        // Overlaps by 0.05 on X boundary
        val boxB = AABB().set(1.95f, 0f, 0f, 1f, 1f, 1f)

        // Standard intersects considers it colliding
        assertTrue(boxA.intersects(boxB))

        // With tolerance 0.10f, the boundary overlap is ignored
        assertFalse(boxA.intersectsWithTolerance(boxB, tolerance = 0.10f))
    }

    @Test
    fun testMatrix4IdentityAndTransforms() {
        val mat = Matrix4()
        // Default constructor calls identity()
        assertEquals(1f, mat.values[0], 0.0001f)
        assertEquals(1f, mat.values[5], 0.0001f)
        assertEquals(1f, mat.values[10], 0.0001f)
        assertEquals(1f, mat.values[15], 0.0001f)
        assertEquals(0f, mat.values[1], 0.0001f)
        assertEquals(0f, mat.values[12], 0.0001f)

        // Translation
        mat.translate(3f, -4f, 5f)
        assertEquals(3f, mat.values[12], 0.0001f)
        assertEquals(-4f, mat.values[13], 0.0001f)
        assertEquals(5f, mat.values[14], 0.0001f)

        // Scale
        val scaleMat = Matrix4().scale(2f, 3f, 4f)
        assertEquals(2f, scaleMat.values[0], 0.0001f)
        assertEquals(3f, scaleMat.values[5], 0.0001f)
        assertEquals(4f, scaleMat.values[10], 0.0001f)
    }

    @Test
    fun testMatrix4Multiplication() {
        val m1 = Matrix4().translate(1f, 2f, 3f)
        val m2 = Matrix4().translate(4f, 5f, 6f)

        val out = Matrix4()
        Matrix4.multiply(out, m1, m2)

        // Consecutive translations combine: (1+4, 2+5, 3+6) = (5, 7, 9)
        assertEquals(5f, out.values[12], 0.0001f)
        assertEquals(7f, out.values[13], 0.0001f)
        assertEquals(9f, out.values[14], 0.0001f)
    }

    @Test
    fun testMatrix4PerspectiveAndLookAt() {
        val proj = Matrix4()
        Matrix4.perspective(proj, fovY = 60f, aspect = 1.777f, near = 0.5f, far = 200f)
        assertTrue("Projection m00 non-zero", proj.values[0] > 0f)
        assertTrue("Projection m11 non-zero", proj.values[5] > 0f)
        assertEquals(-1.0f, proj.values[11], 0.0001f) // Column-major perspective sign

        val view = Matrix4()
        Matrix4.lookAt(
            out = view,
            eyeX = 0f, eyeY = 5f, eyeZ = 10f,
            targetX = 0f, targetY = 1f, targetZ = 0f,
            upX = 0f, upY = 1f, upZ = 0f
        )
        // Camera looking down negative Z has identity-like right vector on X
        assertEquals(1.0f, view.values[0], 0.05f)
    }

    @Test
    fun testGameCameraFollowDynamics() {
        val camera = GameCamera()
        camera.onSurfaceChanged(1080, 1920)
        camera.updateProjection(GameConstants.CAMERA_FOV)

        val player = PlayerController()
        camera.reset(player)

        // Initial camera position relative to player
        assertEquals(player.x * 0.4f, camera.position.x, 0.01f)
        assertEquals(player.y + GameConstants.CAMERA_FOLLOW_HEIGHT, camera.position.y, 0.01f)
        assertEquals(player.z + GameConstants.CAMERA_FOLLOW_DISTANCE, camera.position.z, 0.01f)

        // Camera follow update smoothly lerps towards player
        camera.update(player, dt = 0.016f)
        assertTrue("Camera Z tracks follow distance", camera.position.z > player.z)
    }
}
