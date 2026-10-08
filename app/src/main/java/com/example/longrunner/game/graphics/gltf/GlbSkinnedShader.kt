package com.example.longrunner.game.graphics.gltf

import android.opengl.GLES30

/**
 * OpenGL ES 3.0 Skinned Mesh Shader with GPU skeletal skinning (3x4 affine bone matrix palette),
 * diffuse texture mapping, emissive neon glow mapping, directional lighting, and cyber fog.
 */
class GlbSkinnedShader {

    val programId: Int

    private val uMVPMatrixLoc: Int
    private val uModelMatrixLoc: Int
    private val uBonesLoc: Int

    private val uDiffuseTexLoc: Int
    private val uGlowTexLoc: Int
    private val uHasGlowTexLoc: Int

    private val uLightDirLoc: Int
    private val uLightColorLoc: Int
    private val uAmbientColorLoc: Int
    private val uFogColorLoc: Int
    private val uFogDensityLoc: Int
    private val uCameraPosLoc: Int
    private val uPhaseFreqLoc: Int

    init {
        val vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, VERTEX_SHADER_SRC)
        val fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_SRC)

        programId = GLES30.glCreateProgram()
        GLES30.glAttachShader(programId, vertexShader)
        GLES30.glAttachShader(programId, fragmentShader)
        GLES30.glLinkProgram(programId)

        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val error = GLES30.glGetProgramInfoLog(programId)
            GLES30.glDeleteProgram(programId)
            throw RuntimeException("GlbSkinnedShader link failed: $error")
        }

        GLES30.glDeleteShader(vertexShader)
        GLES30.glDeleteShader(fragmentShader)

        uMVPMatrixLoc = GLES30.glGetUniformLocation(programId, "u_MVPMatrix")
        uModelMatrixLoc = GLES30.glGetUniformLocation(programId, "u_ModelMatrix")
        uBonesLoc = GLES30.glGetUniformLocation(programId, "u_Bones")

        uDiffuseTexLoc = GLES30.glGetUniformLocation(programId, "u_DiffuseTexture")
        uGlowTexLoc = GLES30.glGetUniformLocation(programId, "u_GlowTexture")
        uHasGlowTexLoc = GLES30.glGetUniformLocation(programId, "u_HasGlowTexture")

        uLightDirLoc = GLES30.glGetUniformLocation(programId, "u_LightDir")
        uLightColorLoc = GLES30.glGetUniformLocation(programId, "u_LightColor")
        uAmbientColorLoc = GLES30.glGetUniformLocation(programId, "u_AmbientColor")
        uFogColorLoc = GLES30.glGetUniformLocation(programId, "u_FogColor")
        uFogDensityLoc = GLES30.glGetUniformLocation(programId, "u_FogDensity")
        uCameraPosLoc = GLES30.glGetUniformLocation(programId, "u_CameraPos")
        uPhaseFreqLoc = GLES30.glGetUniformLocation(programId, "u_PhaseFrequency")
    }

    fun bind() {
        GLES30.glUseProgram(programId)
    }

    fun setMVPMatrix(matrix: FloatArray) {
        GLES30.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, matrix, 0)
    }

    fun setModelMatrix(matrix: FloatArray) {
        GLES30.glUniformMatrix4fv(uModelMatrixLoc, 1, false, matrix, 0)
    }

    /**
     * Uploads the 3x4 affine bone matrix palette (up to 68 bones * 3 rows = 204 vec4s).
     */
    fun setBones(boneVectors: FloatArray, count: Int) {
        GLES30.glUniform4fv(uBonesLoc, count, boneVectors, 0)
    }

    fun setTextures(diffuseTexId: Int, glowTexId: Int) {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, diffuseTexId)
        GLES30.glUniform1i(uDiffuseTexLoc, 0)

        if (glowTexId != 0) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, glowTexId)
            GLES30.glUniform1i(uGlowTexLoc, 1)
            GLES30.glUniform1i(uHasGlowTexLoc, 1)
        } else {
            GLES30.glUniform1i(uHasGlowTexLoc, 0)
        }
    }

    fun setLighting(
        dirX: Float, dirY: Float, dirZ: Float,
        lr: Float, lg: Float, lb: Float,
        ar: Float, ag: Float, ab: Float
    ) {
        GLES30.glUniform3f(uLightDirLoc, dirX, dirY, dirZ)
        GLES30.glUniform3f(uLightColorLoc, lr, lg, lb)
        GLES30.glUniform3f(uAmbientColorLoc, ar, ag, ab)
    }

    fun setFog(r: Float, g: Float, b: Float, density: Float) {
        GLES30.glUniform3f(uFogColorLoc, r, g, b)
        GLES30.glUniform1f(uFogDensityLoc, density)
    }

    fun setCameraPosition(x: Float, y: Float, z: Float) {
        GLES30.glUniform3f(uCameraPosLoc, x, y, z)
    }

    fun setPhaseFrequency(phase: Float) {
        GLES30.glUniform1f(uPhaseFreqLoc, phase)
    }

    fun release() {
        GLES30.glDeleteProgram(programId)
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)

        val compiled = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            val error = GLES30.glGetShaderInfoLog(shader)
            GLES30.glDeleteShader(shader)
            throw RuntimeException("GlbSkinnedShader compile failed (type $type): $error")
        }
        return shader
    }

    companion object {
        private const val VERTEX_SHADER_SRC = """#version 300 es
layout(location = 0) in vec3 a_Position;
layout(location = 1) in vec3 a_Normal;
layout(location = 2) in vec2 a_TexCoord;
layout(location = 3) in vec4 a_Joints;
layout(location = 4) in vec4 a_Weights;

uniform mat4 u_MVPMatrix;
uniform mat4 u_ModelMatrix;
uniform vec4 u_Bones[204]; // 68 bones * 3 rows (3x4 affine matrices)

out vec3 v_Position;
out vec3 v_Normal;
out vec2 v_TexCoord;

mat4 getBoneMatrix(int boneIndex) {
    int idx = boneIndex * 3;
    vec4 r0 = u_Bones[idx];
    vec4 r1 = u_Bones[idx + 1];
    vec4 r2 = u_Bones[idx + 2];
    return mat4(
        r0.x, r1.x, r2.x, 0.0,
        r0.y, r1.y, r2.y, 0.0,
        r0.z, r1.z, r2.z, 0.0,
        r0.w, r1.w, r2.w, 1.0
    );
}

void main() {
    mat4 skinMatrix = 
        getBoneMatrix(int(a_Joints.x)) * a_Weights.x +
        getBoneMatrix(int(a_Joints.y)) * a_Weights.y +
        getBoneMatrix(int(a_Joints.z)) * a_Weights.z +
        getBoneMatrix(int(a_Joints.w)) * a_Weights.w;

    vec4 localPos = skinMatrix * vec4(a_Position, 1.0);
    vec4 worldPos = u_ModelMatrix * localPos;
    gl_Position = u_MVPMatrix * localPos;

    v_Position = worldPos.xyz;
    v_Normal = normalize(mat3(u_ModelMatrix) * mat3(skinMatrix) * a_Normal);
    v_TexCoord = a_TexCoord;
}
"""

        private const val FRAGMENT_SHADER_SRC = """#version 300 es
precision mediump float;

in vec3 v_Position;
in vec3 v_Normal;
in vec2 v_TexCoord;

uniform sampler2D u_DiffuseTexture;
uniform sampler2D u_GlowTexture;
uniform int u_HasGlowTexture;

uniform vec3 u_LightDir;
uniform vec3 u_LightColor;
uniform vec3 u_AmbientColor;
uniform vec3 u_FogColor;
uniform float u_FogDensity;
uniform vec3 u_CameraPos;
uniform float u_PhaseFrequency;

out vec4 fragColor;

void main() {
    vec4 diffuse = texture(u_DiffuseTexture, v_TexCoord);
    if (diffuse.a < 0.05) discard;

    vec3 norm = normalize(v_Normal);
    float diff = max(dot(norm, u_LightDir), 0.0);
    vec3 litColor = diffuse.rgb * (u_AmbientColor + u_LightColor * diff);

    if (u_HasGlowTexture == 1) {
        vec4 glow = texture(u_GlowTexture, v_TexCoord);
        litColor += glow.rgb * (1.2 + 0.4 * u_PhaseFrequency);
    }

    float dist = length(u_CameraPos - v_Position);
    float fogFactor = clamp(exp(-dist * u_FogDensity), 0.0, 1.0);
    vec3 finalColor = mix(u_FogColor, litColor, fogFactor);

    fragColor = vec4(finalColor, diffuse.a);
}
"""
    }
}
