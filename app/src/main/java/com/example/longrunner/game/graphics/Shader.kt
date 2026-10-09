package com.example.longrunner.game.graphics

import android.opengl.GLES30
import android.util.Log

class Shader(vertexSource: String, fragmentSource: String) {
    val programId: Int

    val aPositionLocation: Int
    val aNormalLocation: Int
    val aColorLocation: Int

    val uMVPMatrixLocation: Int
    val uModelMatrixLocation: Int
    val uLightDirLocation: Int
    val uLightColorLocation: Int
    val uAmbientColorLocation: Int
    val uEmissiveLocation: Int
    val uFogColorLocation: Int
    val uFogDensityLocation: Int
    val uCameraPosLocation: Int
    val uPhaseFrequencyLocation: Int
    val uGlitchFactorLocation: Int

    init {
        val vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)

        programId = GLES30.glCreateProgram()
        GLES30.glAttachShader(programId, vertexShader)
        GLES30.glAttachShader(programId, fragmentShader)
        GLES30.glLinkProgram(programId)

        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val error = GLES30.glGetProgramInfoLog(programId)
            GLES30.glDeleteProgram(programId)
            throw RuntimeException("Shader link failed: $error")
        }

        GLES30.glDeleteShader(vertexShader)
        GLES30.glDeleteShader(fragmentShader)

        // Attribute locations
        aPositionLocation = GLES30.glGetAttribLocation(programId, "a_Position")
        aNormalLocation = GLES30.glGetAttribLocation(programId, "a_Normal")
        aColorLocation = GLES30.glGetAttribLocation(programId, "a_Color")

        // Uniform locations
        uMVPMatrixLocation = GLES30.glGetUniformLocation(programId, "u_MVPMatrix")
        uModelMatrixLocation = GLES30.glGetUniformLocation(programId, "u_ModelMatrix")
        uLightDirLocation = GLES30.glGetUniformLocation(programId, "u_LightDir")
        uLightColorLocation = GLES30.glGetUniformLocation(programId, "u_LightColor")
        uAmbientColorLocation = GLES30.glGetUniformLocation(programId, "u_AmbientColor")
        uEmissiveLocation = GLES30.glGetUniformLocation(programId, "u_Emissive")
        uFogColorLocation = GLES30.glGetUniformLocation(programId, "u_FogColor")
        uFogDensityLocation = GLES30.glGetUniformLocation(programId, "u_FogDensity")
        uCameraPosLocation = GLES30.glGetUniformLocation(programId, "u_CameraPos")
        uPhaseFrequencyLocation = GLES30.glGetUniformLocation(programId, "u_PhaseFrequency")
        uGlitchFactorLocation = GLES30.glGetUniformLocation(programId, "u_GlitchFactor")
    }

    fun bind() {
        GLES30.glUseProgram(programId)
    }

    fun setMVPMatrix(matrix: FloatArray) {
        GLES30.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, matrix, 0)
    }

    fun setModelMatrix(matrix: FloatArray) {
        GLES30.glUniformMatrix4fv(uModelMatrixLocation, 1, false, matrix, 0)
    }

    fun setLighting(
        lightDirX: Float, lightDirY: Float, lightDirZ: Float,
        lightR: Float, lightG: Float, lightB: Float,
        ambientR: Float, ambientG: Float, ambientB: Float
    ) {
        GLES30.glUniform3f(uLightDirLocation, lightDirX, lightDirY, lightDirZ)
        GLES30.glUniform3f(uLightColorLocation, lightR, lightG, lightB)
        GLES30.glUniform3f(uAmbientColorLocation, ambientR, ambientG, ambientB)
    }

    fun setEmissive(emissive: Float) {
        GLES30.glUniform1f(uEmissiveLocation, emissive)
    }

    fun setFog(r: Float, g: Float, b: Float, density: Float) {
        GLES30.glUniform3f(uFogColorLocation, r, g, b)
        GLES30.glUniform1f(uFogDensityLocation, density)
    }

    fun setCameraPosition(x: Float, y: Float, z: Float) {
        GLES30.glUniform3f(uCameraPosLocation, x, y, z)
    }

    fun setPhaseFrequency(frequency: Float) {
        GLES30.glUniform1f(uPhaseFrequencyLocation, frequency)
    }

    fun setGlitchFactor(glitch: Float) {
        GLES30.glUniform1f(uGlitchFactorLocation, glitch)
    }

    companion object {
        private const val TAG = "Shader"

        private fun compileShader(type: Int, source: String): Int {
            val shader = GLES30.glCreateShader(type)
            GLES30.glShaderSource(shader, source)
            GLES30.glCompileShader(shader)

            val compileStatus = IntArray(1)
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                val error = GLES30.glGetShaderInfoLog(shader)
                GLES30.glDeleteShader(shader)
                throw RuntimeException("Compilation failed for shader $type: $error")
            }
            return shader
        }

        const val VERTEX_SHADER_SRC = """#version 300 es
precision highp float;
precision highp int;

layout(location = 0) in vec3 a_Position;
layout(location = 1) in vec3 a_Normal;
layout(location = 2) in vec4 a_Color;

uniform mat4 u_MVPMatrix;
uniform mat4 u_ModelMatrix;
uniform float u_GlitchFactor;

out vec3 v_Position;
out vec3 v_Normal;
out vec4 v_Color;

void main() {
    vec3 pos = a_Position;
    if (u_GlitchFactor > 0.001) {
        float jitter = sin(pos.y * 14.0 + u_GlitchFactor * 30.0) * (u_GlitchFactor * 0.15);
        pos.x += jitter;
    }
    vec4 worldPos = u_ModelMatrix * vec4(pos, 1.0);
    v_Position = worldPos.xyz;
    v_Normal = normalize(mat3(u_ModelMatrix) * a_Normal);
    v_Color = a_Color;
    gl_Position = u_MVPMatrix * vec4(pos, 1.0);
}
"""

        const val FRAGMENT_SHADER_SRC = """#version 300 es
precision highp float;
precision highp int;

in vec3 v_Position;
in vec3 v_Normal;
in vec4 v_Color;

uniform vec3 u_LightDir;
uniform vec3 u_LightColor;
uniform vec3 u_AmbientColor;
uniform float u_Emissive;
uniform vec3 u_FogColor;
uniform float u_FogDensity;
uniform vec3 u_CameraPos;
uniform float u_PhaseFrequency;
uniform float u_GlitchFactor;

out vec4 fragColor;

void main() {
    // Directional + Ambient lighting
    vec3 normal = normalize(v_Normal);
    vec3 lightDir = normalize(u_LightDir);
    float diff = max(dot(normal, lightDir), 0.0);
    
    vec3 lighting = u_AmbientColor + (u_LightColor * diff);
    
    vec3 baseColor = v_Color.rgb;
    vec3 litColor = mix(baseColor * lighting, baseColor * 1.15, u_Emissive);
    
    // Phase Reality chromatic frequency shift
    vec3 phaseColor = vec3(litColor.b * 1.15, litColor.r * 0.35 + litColor.g * 0.25, litColor.r * 0.85 + litColor.b * 0.85);
    litColor = mix(litColor, phaseColor, u_PhaseFrequency * 0.82);
    
    // Reality Glitch chromatic distortion
    if (u_GlitchFactor > 0.001) {
        float glitchStripe = step(0.60, sin(v_Position.y * 18.0 + u_GlitchFactor * 25.0));
        vec3 glitchColor = vec3(litColor.g * 1.2, litColor.b * 1.4, litColor.r * 1.3);
        litColor = mix(litColor, glitchColor, glitchStripe * u_GlitchFactor * 0.75);
    }
    
    // Distance Fog
    float dist = length(v_Position - u_CameraPos);
    float fogFactor = exp(-pow(dist * u_FogDensity, 2.0));
    fogFactor = clamp(fogFactor, 0.0, 1.0);
    
    vec3 finalColor = mix(u_FogColor, litColor, fogFactor);
    fragColor = vec4(finalColor, v_Color.a);
}
"""
    }
}
