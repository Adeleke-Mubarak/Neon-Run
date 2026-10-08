package com.example.longrunner.game.graphics

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * 3-Dimensional Vector for spatial calculations.
 */
data class Vector3(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f) {
    fun set(x: Float, y: Float, z: Float): Vector3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun set(other: Vector3): Vector3 = set(other.x, other.y, other.z)

    operator fun plus(o: Vector3) = Vector3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vector3) = Vector3(x - o.x, y - o.y, z - o.z)
    operator fun times(scalar: Float) = Vector3(x * scalar, y * scalar, z * scalar)

    fun lengthSquared(): Float = x * x + y * y + z * z
    fun length(): Float = sqrt(lengthSquared())

    fun normalize(): Vector3 {
        val l = length()
        if (l > 0.00001f) {
            x /= l
            y /= l
            z /= l
        }
        return this
    }

    fun lerp(target: Vector3, t: Float): Vector3 {
        x += (target.x - x) * t
        y += (target.y - y) * t
        z += (target.z - z) * t
        return this
    }

    companion object {
        fun lerp(a: Vector3, b: Vector3, t: Float) = Vector3(
            a.x + (b.x - a.x) * t,
            a.y + (b.y - a.y) * t,
            a.z + (b.z - a.z) * t
        )
    }
}

/**
 * 3D Axis-Aligned Bounding Box (AABB) for collision detection.
 */
class AABB(
    var minX: Float = 0f,
    var minY: Float = 0f,
    var minZ: Float = 0f,
    var maxX: Float = 0f,
    var maxY: Float = 0f,
    var maxZ: Float = 0f
) {
    fun set(centerX: Float, centerY: Float, centerZ: Float, halfW: Float, halfH: Float, halfD: Float): AABB {
        minX = centerX - halfW
        maxX = centerX + halfW
        minY = centerY - halfH
        maxY = centerY + halfH
        minZ = centerZ - halfD
        maxZ = centerZ + halfD
        return this
    }

    fun intersects(other: AABB): Boolean {
        return (minX <= other.maxX && maxX >= other.minX) &&
                (minY <= other.maxY && maxY >= other.minY) &&
                (minZ <= other.maxZ && maxZ >= other.minZ)
    }

    fun intersectsWithTolerance(other: AABB, tolerance: Float): Boolean {
        return (minX + tolerance <= other.maxX - tolerance && maxX - tolerance >= other.minX + tolerance) &&
                (minY + tolerance <= other.maxY - tolerance && maxY - tolerance >= other.minY + tolerance) &&
                (minZ + tolerance <= other.maxZ - tolerance && maxZ - tolerance >= other.minZ + tolerance)
    }

    override fun toString(): String {
        return "AABB(X: [$minX, $maxX], Y: [$minY, $maxY], Z: [$minZ, $maxZ])"
    }
}

/**
 * 4x4 Transformation Matrix implemented in pure Kotlin.
 * Follows column-major layout standard for OpenGL ES shaders.
 */
class Matrix4 {
    val values = FloatArray(16)
    private val temp = FloatArray(16)

    init {
        identity()
    }

    fun identity(): Matrix4 {
        for (i in 0 until 16) values[i] = 0f
        values[0] = 1f
        values[5] = 1f
        values[10] = 1f
        values[15] = 1f
        return this
    }

    fun translate(x: Float, y: Float, z: Float): Matrix4 {
        // Translation in column-major:
        // [0..2] unaffected, values[12..14] += column combinations
        values[12] += values[0] * x + values[4] * y + values[8] * z
        values[13] += values[1] * x + values[5] * y + values[9] * z
        values[14] += values[2] * x + values[6] * y + values[10] * z
        values[15] += values[3] * x + values[7] * y + values[11] * z
        return this
    }

    fun scale(sx: Float, sy: Float, sz: Float): Matrix4 {
        values[0] *= sx; values[1] *= sx; values[2] *= sx; values[3] *= sx
        values[4] *= sy; values[5] *= sy; values[6] *= sy; values[7] *= sy
        values[8] *= sz; values[9] *= sz; values[10] *= sz; values[11] *= sz
        return this
    }

    fun rotate(degrees: Float, ax: Float, ay: Float, az: Float): Matrix4 {
        val rad = Math.toRadians(degrees.toDouble()).toFloat()
        val c = kotlin.math.cos(rad)
        val s = kotlin.math.sin(rad)
        val nc = 1.0f - c

        var x = ax
        var y = ay
        var z = az
        val len = sqrt(x * x + y * y + z * z)
        if (len > 0.0001f) {
            x /= len
            y /= len
            z /= len
        }

        val rot = FloatArray(16)
        rot[0] = x * x * nc + c
        rot[1] = x * y * nc + z * s
        rot[2] = x * z * nc - y * s
        rot[3] = 0f

        rot[4] = y * x * nc - z * s
        rot[5] = y * y * nc + c
        rot[6] = y * z * nc + x * s
        rot[7] = 0f

        rot[8] = z * x * nc + y * s
        rot[9] = z * y * nc - x * s
        rot[10] = z * z * nc + c
        rot[11] = 0f

        rot[12] = 0f
        rot[13] = 0f
        rot[14] = 0f
        rot[15] = 1f

        multiply(this, this, rot)
        return this
    }

    fun multiply(other: Matrix4): Matrix4 {
        multiply(this, this, other.values)
        return this
    }

    fun copyFrom(other: Matrix4): Matrix4 {
        System.arraycopy(other.values, 0, values, 0, 16)
        return this
    }

    companion object {
        fun multiply(out: Matrix4, lhs: Matrix4, rhs: FloatArray) {
            val a = lhs.values
            val b = rhs
            val r = out.temp

            for (i in 0 until 4) {
                val ai0 = a[i]
                val ai1 = a[i + 4]
                val ai2 = a[i + 8]
                val ai3 = a[i + 12]

                r[i] = ai0 * b[0] + ai1 * b[1] + ai2 * b[2] + ai3 * b[3]
                r[i + 4] = ai0 * b[4] + ai1 * b[5] + ai2 * b[6] + ai3 * b[7]
                r[i + 8] = ai0 * b[8] + ai1 * b[9] + ai2 * b[10] + ai3 * b[11]
                r[i + 12] = ai0 * b[12] + ai1 * b[13] + ai2 * b[14] + ai3 * b[15]
            }

            System.arraycopy(r, 0, out.values, 0, 16)
        }

        fun multiply(out: Matrix4, lhs: Matrix4, rhs: Matrix4) {
            multiply(out, lhs, rhs.values)
        }

        fun perspective(out: Matrix4, fovY: Float, aspect: Float, near: Float, far: Float) {
            for (i in 0 until 16) out.values[i] = 0f
            val rad = Math.toRadians((fovY * 0.5f).toDouble()).toFloat()
            val f = 1.0f / kotlin.math.tan(rad)
            val rangeInv = 1.0f / (near - far)

            out.values[0] = f / aspect
            out.values[5] = f
            out.values[10] = (far + near) * rangeInv
            out.values[11] = -1.0f
            out.values[14] = (2.0f * far * near) * rangeInv
        }

        fun lookAt(
            out: Matrix4,
            eyeX: Float, eyeY: Float, eyeZ: Float,
            targetX: Float, targetY: Float, targetZ: Float,
            upX: Float, upY: Float, upZ: Float
        ) {
            // Forward vector (eye - target)
            var fx = eyeX - targetX
            var fy = eyeY - targetY
            var fz = eyeZ - targetZ
            var flen = sqrt(fx * fx + fy * fy + fz * fz)
            if (flen > 0.0001f) { fx /= flen; fy /= flen; fz /= flen }

            // Right vector (up x forward)
            var rx = upY * fz - upZ * fy
            var ry = upZ * fx - upX * fz
            var rz = upX * fy - upY * fx
            var rlen = sqrt(rx * rx + ry * ry + rz * rz)
            if (rlen > 0.0001f) { rx /= rlen; ry /= rlen; rz /= rlen }

            // Recomputed true up (forward x right)
            val ux = fy * rz - fz * ry
            val uy = fz * rx - fx * rz
            val uz = fx * ry - fy * rx

            out.values[0] = rx
            out.values[1] = ux
            out.values[2] = fx
            out.values[3] = 0f

            out.values[4] = ry
            out.values[5] = uy
            out.values[6] = fy
            out.values[7] = 0f

            out.values[8] = rz
            out.values[9] = uz
            out.values[10] = fz
            out.values[11] = 0f

            out.values[12] = -(rx * eyeX + ry * eyeY + rz * eyeZ)
            out.values[13] = -(ux * eyeX + uy * eyeY + uz * eyeZ)
            out.values[14] = -(fx * eyeX + fy * eyeY + fz * eyeZ)
            out.values[15] = 1f
        }
    }
}

/**
 * Quaternion for 3D skeletal rotation calculations and slerp interpolation.
 */
data class Quaternion(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,
    var w: Float = 1f
) {
    fun set(x: Float, y: Float, z: Float, w: Float): Quaternion {
        this.x = x; this.y = y; this.z = z; this.w = w
        return this
    }

    fun set(o: Quaternion): Quaternion = set(o.x, o.y, o.z, o.w)

    fun identity(): Quaternion = set(0f, 0f, 0f, 1f)

    fun normalize(): Quaternion {
        val len = sqrt(x * x + y * y + z * z + w * w)
        if (len > 0.00001f) {
            x /= len; y /= len; z /= len; w /= len
        } else {
            identity()
        }
        return this
    }

    fun toMatrix(out: FloatArray, offset: Int = 0) {
        val xx = x * x; val yy = y * y; val zz = z * z
        val xy = x * y; val xz = x * z; val yz = y * z
        val wx = w * x; val wy = w * y; val wz = w * z

        out[offset + 0] = 1f - 2f * (yy + zz)
        out[offset + 1] = 2f * (xy + wz)
        out[offset + 2] = 2f * (xz - wy)
        out[offset + 3] = 0f

        out[offset + 4] = 2f * (xy - wz)
        out[offset + 5] = 1f - 2f * (xx + zz)
        out[offset + 6] = 2f * (yz + wx)
        out[offset + 7] = 0f

        out[offset + 8] = 2f * (xz + wy)
        out[offset + 9] = 2f * (yz - wx)
        out[offset + 10] = 1f - 2f * (xx + yy)
        out[offset + 11] = 0f

        out[offset + 12] = 0f
        out[offset + 13] = 0f
        out[offset + 14] = 0f
        out[offset + 15] = 1f
    }

    companion object {
        fun slerp(q1: Quaternion, q2: Quaternion, t: Float, out: Quaternion) {
            var cosHalfTheta = q1.x * q2.x + q1.y * q2.y + q1.z * q2.z + q1.w * q2.w
            var q2x = q2.x; var q2y = q2.y; var q2z = q2.z; var q2w = q2.w

            if (cosHalfTheta < 0f) {
                q2x = -q2x; q2y = -q2y; q2z = -q2z; q2w = -q2w
                cosHalfTheta = -cosHalfTheta
            }

            if (cosHalfTheta >= 0.9995f) {
                out.x = q1.x + (q2x - q1.x) * t
                out.y = q1.y + (q2y - q1.y) * t
                out.z = q1.z + (q2z - q1.z) * t
                out.w = q1.w + (q2w - q1.w) * t
                out.normalize()
                return
            }

            val halfTheta = kotlin.math.acos(cosHalfTheta.coerceIn(-1f, 1f))
            val sinHalfTheta = sqrt(1.0f - cosHalfTheta * cosHalfTheta)

            if (sinHalfTheta < 0.001f) {
                out.x = q1.x * 0.5f + q2x * 0.5f
                out.y = q1.y * 0.5f + q2y * 0.5f
                out.z = q1.z * 0.5f + q2z * 0.5f
                out.w = q1.w * 0.5f + q2w * 0.5f
                out.normalize()
                return
            }

            val ratioA = kotlin.math.sin((1f - t) * halfTheta) / sinHalfTheta
            val ratioB = kotlin.math.sin(t * halfTheta) / sinHalfTheta

            out.x = q1.x * ratioA + q2x * ratioB
            out.y = q1.y * ratioA + q2y * ratioB
            out.z = q1.z * ratioA + q2z * ratioB
            out.w = q1.w * ratioA + q2w * ratioB
            out.normalize()
        }
    }
}
