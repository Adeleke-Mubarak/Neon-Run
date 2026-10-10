package com.example.longrunner.game.graphics.gltf

import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import com.example.longrunner.game.graphics.Quaternion
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

data class GlbNode(
    val id: Int,
    val name: String,
    val translation: FloatArray = floatArrayOf(0f, 0f, 0f),
    val rotation: Quaternion = Quaternion(0f, 0f, 0f, 1f),
    val scale: FloatArray = floatArrayOf(1f, 1f, 1f),
    val children: IntArray = IntArray(0),
    var parent: Int = -1
)

data class GlbSampler(
    val times: FloatArray,
    val values: FloatArray,
    val interpolation: String = "LINEAR"
)

data class GlbChannel(
    val targetNode: Int,
    val targetPath: String, // "translation", "rotation", "scale"
    val samplerIndex: Int
)

data class GlbAnimation(
    val name: String,
    val duration: Float,
    val channels: List<GlbChannel>,
    val samplers: List<GlbSampler>
)

data class GlbSkin(
    val joints: IntArray,
    val inverseBindMatrices: FloatArray // joints.size * 16 floats
)

data class GlbPrimitive(
    val vaoId: Int,
    val indexCount: Int,
    val diffuseTexId: Int,
    val glowTexId: Int,
    val vboIds: IntArray,
    val name: String = "",
    val minX: Float = 0f,
    val minY: Float = 0f,
    val minZ: Float = 0f,
    val maxX: Float = 0f,
    val maxY: Float = 0f,
    val maxZ: Float = 0f
)

class GlbModel(
    val nodes: List<GlbNode>,
    val skin: GlbSkin?,
    val animations: Map<String, GlbAnimation>,
    val primitives: List<GlbPrimitive>,
    private val allLoadedTextures: List<Int> = emptyList()
) {
    val vaoId: Int get() = primitives.firstOrNull()?.vaoId ?: 0
    val indexCount: Int get() = primitives.firstOrNull()?.indexCount ?: 0
    val diffuseTexId: Int get() = primitives.firstOrNull()?.diffuseTexId ?: 0
    val glowTexId: Int get() = primitives.firstOrNull()?.glowTexId ?: 0

    fun render(shader: GlbSkinnedShader, boneMatrices: FloatArray) {
        val boneVecCount = (skin?.joints?.size ?: 0) * 3
        if (boneVecCount > 0) {
            shader.setBones(boneMatrices, boneVecCount)
        }

        for (prim in primitives) {
            shader.setTextures(prim.diffuseTexId, prim.glowTexId)
            GLES30.glBindVertexArray(prim.vaoId)
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, prim.indexCount, GLES30.GL_UNSIGNED_SHORT, 0)
        }
        GLES30.glBindVertexArray(0)
    }

    fun renderPrimitive(prim: GlbPrimitive, shader: GlbSkinnedShader) {
        shader.setTextures(prim.diffuseTexId, prim.glowTexId)
        GLES30.glBindVertexArray(prim.vaoId)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, prim.indexCount, GLES30.GL_UNSIGNED_SHORT, 0)
        GLES30.glBindVertexArray(0)
    }

    fun findPrimitives(predicate: (GlbPrimitive) -> Boolean): List<GlbPrimitive> {
        return primitives.filter(predicate)
    }

    fun release() {
        for (prim in primitives) {
            GLES30.glDeleteVertexArrays(1, intArrayOf(prim.vaoId), 0)
            GLES30.glDeleteBuffers(prim.vboIds.size, prim.vboIds, 0)
        }
        if (allLoadedTextures.isNotEmpty()) {
            val texArr = allLoadedTextures.toIntArray()
            GLES30.glDeleteTextures(texArr.size, texArr, 0)
        }
    }

    companion object {
        private var defaultWhiteTexId: Int = 0

        fun getDefaultWhiteTexture(): Int {
            if (defaultWhiteTexId != 0) return defaultWhiteTexId
            val tex = IntArray(1)
            GLES30.glGenTextures(1, tex, 0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, tex[0])
            val whitePixels = ByteBuffer.allocateDirect(4).put(byteArrayOf(-1, -1, -1, -1)).position(0)
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, 1, 1, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, whitePixels)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST)
            defaultWhiteTexId = tex[0]
            return defaultWhiteTexId
        }

        fun load(inputStream: InputStream): GlbModel {
            val bytes = inputStream.readBytes()
            val byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

            // Header (12 bytes)
            val magic = byteBuffer.int
            if (magic != 0x46546C67) {
                throw IllegalArgumentException("Not a valid glTF/GLB file! Magic: $magic")
            }
            val version = byteBuffer.int
            val totalLength = byteBuffer.int

            // Chunk 0: JSON
            val jsonLength = byteBuffer.int
            val jsonType = byteBuffer.int
            val jsonBytes = ByteArray(jsonLength)
            byteBuffer.get(jsonBytes)
            val jsonStr = String(jsonBytes, Charsets.UTF_8)
            val json = JSONObject(jsonStr)

            // Chunk 1: BIN
            val binLength = byteBuffer.int
            val binType = byteBuffer.int
            val binOffset = byteBuffer.position()

            // Helper to get buffer slice
            fun getBufferSlice(byteOffset: Int, byteLength: Int): ByteBuffer {
                val slice = ByteBuffer.wrap(bytes, binOffset + byteOffset, byteLength).order(ByteOrder.LITTLE_ENDIAN)
                return slice
            }

            // Parse BufferViews
            class BufferView(val byteOffset: Int, val byteLength: Int, val byteStride: Int)
            val bufferViewsList = ArrayList<BufferView>()
            val bufferViewsJson = json.optJSONArray("bufferViews") ?: JSONArray()
            for (i in 0 until bufferViewsJson.length()) {
                val bv = bufferViewsJson.getJSONObject(i)
                bufferViewsList.add(
                    BufferView(
                        bv.optInt("byteOffset", 0),
                        bv.getInt("byteLength"),
                        bv.optInt("byteStride", 0)
                    )
                )
            }

            // Parse Accessors
            class Accessor(val bufferViewIndex: Int, val byteOffset: Int, val componentType: Int, val count: Int, val type: String)
            val accessorsList = ArrayList<Accessor>()
            val accessorsJson = json.optJSONArray("accessors") ?: JSONArray()
            for (i in 0 until accessorsJson.length()) {
                val acc = accessorsJson.getJSONObject(i)
                accessorsList.add(
                    Accessor(
                        acc.getInt("bufferView"),
                        acc.optInt("byteOffset", 0),
                        acc.getInt("componentType"),
                        acc.getInt("count"),
                        acc.getString("type")
                    )
                )
            }

            // Helper: Read FloatArray from Accessor
            fun readFloatArray(accessorIndex: Int, componentsPerItem: Int): FloatArray {
                val acc = accessorsList[accessorIndex]
                val bv = bufferViewsList[acc.bufferViewIndex]
                val buf = getBufferSlice(bv.byteOffset + acc.byteOffset, bv.byteLength)
                val totalFloats = acc.count * componentsPerItem
                val out = FloatArray(totalFloats)
                buf.asFloatBuffer().get(out)
                return out
            }

            // Helper: Read ShortArray from Accessor
            fun readShortArray(accessorIndex: Int): ShortArray {
                val acc = accessorsList[accessorIndex]
                val bv = bufferViewsList[acc.bufferViewIndex]
                val buf = getBufferSlice(bv.byteOffset + acc.byteOffset, bv.byteLength)
                val out = ShortArray(acc.count)
                if (acc.componentType == 5123) { // UNSIGNED_SHORT
                    buf.asShortBuffer().get(out)
                } else if (acc.componentType == 5121) { // UNSIGNED_BYTE
                    for (i in 0 until acc.count) {
                        out[i] = (buf.get().toInt() and 0xFF).toShort()
                    }
                } else if (acc.componentType == 5125) { // UNSIGNED_INT
                    val ib = buf.asIntBuffer()
                    for (i in 0 until acc.count) {
                        out[i] = ib.get().toShort()
                    }
                }
                return out
            }

            // Helper: Read Joints as FloatArray (converting UBYTE or USHORT to 4 floats per vertex)
            fun readJointsFloatArray(accessorIndex: Int): FloatArray {
                val acc = accessorsList[accessorIndex]
                val bv = bufferViewsList[acc.bufferViewIndex]
                val buf = getBufferSlice(bv.byteOffset + acc.byteOffset, bv.byteLength)
                val out = FloatArray(acc.count * 4)

                if (acc.componentType == 5121) { // UNSIGNED_BYTE
                    for (i in 0 until acc.count * 4) {
                        out[i] = (buf.get().toInt() and 0xFF).toFloat()
                    }
                } else if (acc.componentType == 5123) { // UNSIGNED_SHORT
                    for (i in 0 until acc.count * 4) {
                        out[i] = (buf.short.toInt() and 0xFFFF).toFloat()
                    }
                }
                return out
            }

            // Parse Textures & Images
            val loadedImageTexIds = HashMap<Int, Int>()
            val allLoadedTextures = ArrayList<Int>()

            val imagesJson = json.optJSONArray("images")
            if (imagesJson != null) {
                for (i in 0 until imagesJson.length()) {
                    val imgObj = imagesJson.getJSONObject(i)
                    val bvIdx = imgObj.optInt("bufferView", -1)
                    if (bvIdx >= 0) {
                        val bv = bufferViewsList[bvIdx]
                        val imgSlice = getBufferSlice(bv.byteOffset, bv.byteLength)
                        val imgBytes = ByteArray(bv.byteLength)
                        imgSlice.get(imgBytes)

                        val bmp = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.size)
                        if (bmp != null) {
                            val tex = IntArray(1)
                            GLES30.glGenTextures(1, tex, 0)
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, tex[0])
                            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
                            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
                            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT)
                            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT)
                            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bmp, 0)
                            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
                            bmp.recycle()

                            loadedImageTexIds[i] = tex[0]
                            allLoadedTextures.add(tex[0])
                        }
                    }
                }
            }

            // Texture sources map (texture index -> image index)
            val textureSourceMap = HashMap<Int, Int>()
            val texturesJson = json.optJSONArray("textures")
            if (texturesJson != null) {
                for (i in 0 until texturesJson.length()) {
                    val tObj = texturesJson.getJSONObject(i)
                    val src = tObj.optInt("source", -1)
                    if (src >= 0) {
                        textureSourceMap[i] = src
                    }
                }
            }

            // Material textures map (material index -> (diffuse, glow))
            class MatTex(val diffuseTexId: Int, val glowTexId: Int)
            val materialTexMap = HashMap<Int, MatTex>()
            val materialsJson = json.optJSONArray("materials")
            if (materialsJson != null) {
                for (i in 0 until materialsJson.length()) {
                    val mObj = materialsJson.getJSONObject(i)
                    var diffTex = 0
                    var glowTex = 0

                    val pbr = mObj.optJSONObject("pbrMetallicRoughness")
                    var baseColorTexObj = pbr?.optJSONObject("baseColorTexture")
                    if (baseColorTexObj == null) {
                        val specGloss = mObj.optJSONObject("extensions")?.optJSONObject("KHR_materials_pbrSpecularGlossiness")
                        baseColorTexObj = specGloss?.optJSONObject("diffuseTexture")
                    }
                    val baseTexIdx = baseColorTexObj?.optInt("index", -1) ?: -1
                    if (baseTexIdx >= 0) {
                        val imgIdx = textureSourceMap[baseTexIdx] ?: baseTexIdx
                        diffTex = loadedImageTexIds[imgIdx] ?: 0
                    }

                    val emissiveTexObj = mObj.optJSONObject("emissiveTexture")
                    val emissiveTexIdx = emissiveTexObj?.optInt("index", -1) ?: -1
                    if (emissiveTexIdx >= 0) {
                        val imgIdx = textureSourceMap[emissiveTexIdx] ?: emissiveTexIdx
                        glowTex = loadedImageTexIds[imgIdx] ?: 0
                    }

                    materialTexMap[i] = MatTex(diffTex, glowTex)
                }
            }

            // Parse Mesh Primitives
            val primitivesList = ArrayList<GlbPrimitive>()
            val meshesJson = json.optJSONArray("meshes")
            if (meshesJson != null) {
                for (mIdx in 0 until meshesJson.length()) {
                    val meshObj = meshesJson.getJSONObject(mIdx)
                    val meshName = meshObj.optString("name", "Mesh_$mIdx")
                    val primsArr = meshObj.getJSONArray("primitives")
                    for (pIdx in 0 until primsArr.length()) {
                        val prim = primsArr.getJSONObject(pIdx)
                        val attributes = prim.getJSONObject("attributes")

                        val posAccIdx = attributes.getInt("POSITION")
                        val normAccIdx = attributes.optInt("NORMAL", -1)
                        val uvAccIdx = attributes.optInt("TEXCOORD_0", -1)
                        val jointsAccIdx = attributes.optInt("JOINTS_0", -1)
                        val weightsAccIdx = attributes.optInt("WEIGHTS_0", -1)
                        val indicesAccIdx = prim.optInt("indices", -1)

                        val posAccObj = accessorsJson.optJSONObject(posAccIdx)
                        val minArr = posAccObj?.optJSONArray("min")
                        val maxArr = posAccObj?.optJSONArray("max")
                        val minX = minArr?.optDouble(0, 0.0)?.toFloat() ?: 0f
                        val minY = minArr?.optDouble(1, 0.0)?.toFloat() ?: 0f
                        val minZ = minArr?.optDouble(2, 0.0)?.toFloat() ?: 0f
                        val maxX = maxArr?.optDouble(0, 0.0)?.toFloat() ?: 0f
                        val maxY = maxArr?.optDouble(1, 0.0)?.toFloat() ?: 0f
                        val maxZ = maxArr?.optDouble(2, 0.0)?.toFloat() ?: 0f

                        val posData = readFloatArray(posAccIdx, 3)
                        val normData = if (normAccIdx >= 0) readFloatArray(normAccIdx, 3) else FloatArray(posData.size) { 0f }
                        val uvData = if (uvAccIdx >= 0) readFloatArray(uvAccIdx, 2) else FloatArray(posData.size / 3 * 2) { 0f }
                        val jointsData = if (jointsAccIdx >= 0) readJointsFloatArray(jointsAccIdx) else FloatArray(posData.size / 3 * 4) { 0f }
                        val weightsData = if (weightsAccIdx >= 0) readFloatArray(weightsAccIdx, 4) else FloatArray(posData.size / 3 * 4) { 0f }
                        val indicesData = if (indicesAccIdx >= 0) readShortArray(indicesAccIdx) else ShortArray(posData.size / 3) { it.toShort() }

                        // Setup OpenGL VAO & VBOs
                        val vao = IntArray(1)
                        GLES30.glGenVertexArrays(1, vao, 0)
                        GLES30.glBindVertexArray(vao[0])

                        val vbo = IntArray(6)
                        GLES30.glGenBuffers(6, vbo, 0)

                        fun uploadFloatVbo(index: Int, location: Int, size: Int, data: FloatArray) {
                            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[index])
                            val fb = ByteBuffer.allocateDirect(data.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
                            fb.put(data).position(0)
                            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, data.size * 4, fb, GLES30.GL_STATIC_DRAW)
                            GLES30.glEnableVertexAttribArray(location)
                            GLES30.glVertexAttribPointer(location, size, GLES30.GL_FLOAT, false, 0, 0)
                        }

                        uploadFloatVbo(0, 0, 3, posData)
                        uploadFloatVbo(1, 1, 3, normData)
                        uploadFloatVbo(2, 2, 2, uvData)
                        uploadFloatVbo(3, 3, 4, jointsData)
                        uploadFloatVbo(4, 4, 4, weightsData)

                        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, vbo[5])
                        val ib = ByteBuffer.allocateDirect(indicesData.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer()
                        ib.put(indicesData).position(0)
                        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indicesData.size * 2, ib, GLES30.GL_STATIC_DRAW)

                        GLES30.glBindVertexArray(0)

                        val matIdx = prim.optInt("material", -1)
                        val matTex = if (matIdx >= 0) materialTexMap[matIdx] else null
                        val primDiffTex = when {
                            matTex != null && matTex.diffuseTexId != 0 -> matTex.diffuseTexId
                            allLoadedTextures.isNotEmpty() -> allLoadedTextures.first()
                            else -> getDefaultWhiteTexture()
                        }
                        val primGlowTex = matTex?.glowTexId ?: 0

                        primitivesList.add(
                            GlbPrimitive(
                                vaoId = vao[0],
                                indexCount = indicesData.size,
                                diffuseTexId = primDiffTex,
                                glowTexId = primGlowTex,
                                vboIds = vbo,
                                name = meshName,
                                minX = minX,
                                minY = minY,
                                minZ = minZ,
                                maxX = maxX,
                                maxY = maxY,
                                maxZ = maxZ
                            )
                        )
                    }
                }
            }

            // Parse Nodes
            val nodesList = ArrayList<GlbNode>()
            val nodesJson = json.getJSONArray("nodes")
            for (i in 0 until nodesJson.length()) {
                val nObj = nodesJson.getJSONObject(i)
                val name = nObj.optString("name", "Node_$i")

                val trans = FloatArray(3) { 0f }
                val tArr = nObj.optJSONArray("translation")
                if (tArr != null) {
                    trans[0] = tArr.getDouble(0).toFloat()
                    trans[1] = tArr.getDouble(1).toFloat()
                    trans[2] = tArr.getDouble(2).toFloat()
                }

                val rot = Quaternion(0f, 0f, 0f, 1f)
                val rArr = nObj.optJSONArray("rotation")
                if (rArr != null) {
                    rot.x = rArr.getDouble(0).toFloat()
                    rot.y = rArr.getDouble(1).toFloat()
                    rot.z = rArr.getDouble(2).toFloat()
                    rot.w = rArr.getDouble(3).toFloat()
                }

                val sca = FloatArray(3) { 1f }
                val sArr = nObj.optJSONArray("scale")
                if (sArr != null) {
                    sca[0] = sArr.getDouble(0).toFloat()
                    sca[1] = sArr.getDouble(1).toFloat()
                    sca[2] = sArr.getDouble(2).toFloat()
                }

                val cArr = nObj.optJSONArray("children")
                val children = if (cArr != null) {
                    IntArray(cArr.length()) { idx -> cArr.getInt(idx) }
                } else {
                    IntArray(0)
                }

                nodesList.add(GlbNode(i, name, trans, rot, sca, children))
            }

            // Assign parents
            for (node in nodesList) {
                for (childId in node.children) {
                    if (childId in nodesList.indices) {
                        nodesList[childId].parent = node.id
                    }
                }
            }

            // Parse Skin
            var skin: GlbSkin? = null
            val skinsJson = json.optJSONArray("skins")
            if (skinsJson != null && skinsJson.length() > 0) {
                val skinObj = skinsJson.getJSONObject(0)
                val jointsArr = skinObj.getJSONArray("joints")
                val joints = IntArray(jointsArr.length()) { idx -> jointsArr.getInt(idx) }

                val invBindIdx = skinObj.getInt("inverseBindMatrices")
                val invBind = readFloatArray(invBindIdx, 16)
                skin = GlbSkin(joints, invBind)
            }

            // Parse Animations
            val animsMap = HashMap<String, GlbAnimation>()
            val animsJson = json.optJSONArray("animations")
            if (animsJson != null) {
                for (i in 0 until animsJson.length()) {
                    val aObj = animsJson.getJSONObject(i)
                    val rawName = aObj.optString("name", "Anim_$i")
                    val cleanName = when {
                        rawName.contains("run", ignoreCase = true) -> "Run"
                        rawName.contains("jump", ignoreCase = true) -> "Jump"
                        rawName.contains("slide", ignoreCase = true) -> "Slide"
                        rawName.contains("idle", ignoreCase = true) -> "Idle"
                        else -> rawName
                    }

                    // Samplers
                    val samplersList = ArrayList<GlbSampler>()
                    val sArr = aObj.getJSONArray("samplers")
                    var maxDuration = 0f
                    for (sIdx in 0 until sArr.length()) {
                        val sObj = sArr.getJSONObject(sIdx)
                        val inputAccIdx = sObj.getInt("input")
                        val outputAccIdx = sObj.getInt("output")
                        val interp = sObj.optString("interpolation", "LINEAR")

                        val times = readFloatArray(inputAccIdx, 1)
                        if (times.isNotEmpty() && times.last() > maxDuration) {
                            maxDuration = times.last()
                        }
                        val outAcc = accessorsList[outputAccIdx]
                        val compCount = when (outAcc.type) {
                            "SCALAR" -> 1
                            "VEC2" -> 2
                            "VEC3" -> 3
                            "VEC4" -> 4
                            "MAT4" -> 16
                            else -> 1
                        }
                        val values = readFloatArray(outputAccIdx, compCount)
                        samplersList.add(GlbSampler(times, values, interp))
                    }

                    // Channels
                    val channelsList = ArrayList<GlbChannel>()
                    val cArr = aObj.getJSONArray("channels")
                    for (cIdx in 0 until cArr.length()) {
                        val cObj = cArr.getJSONObject(cIdx)
                        val samplerIdx = cObj.getInt("sampler")
                        val targetObj = cObj.getJSONObject("target")
                        val targetNode = targetObj.getInt("node")
                        val path = targetObj.getString("path")
                        channelsList.add(GlbChannel(targetNode, path, samplerIdx))
                    }

                    animsMap[cleanName] = GlbAnimation(cleanName, maxDuration, channelsList, samplersList)
                }
            }

            return GlbModel(
                nodes = nodesList,
                skin = skin,
                animations = animsMap,
                primitives = primitivesList,
                allLoadedTextures = allLoadedTextures
            )
        }
    }
}
