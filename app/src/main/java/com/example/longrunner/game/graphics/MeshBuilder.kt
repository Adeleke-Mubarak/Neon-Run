package com.example.longrunner.game.graphics

class MeshBuilder {
    private val vertices = ArrayList<Float>()
    private val indices = ArrayList<Short>()
    private var vertexCount = 0

    fun clear() {
        vertices.clear()
        indices.clear()
        vertexCount = 0
    }

    fun addVertex(
        x: Float, y: Float, z: Float,
        nx: Float, ny: Float, nz: Float,
        r: Float, g: Float, b: Float, a: Float
    ): Short {
        vertices.add(x)
        vertices.add(y)
        vertices.add(z)
        vertices.add(nx)
        vertices.add(ny)
        vertices.add(nz)
        vertices.add(r)
        vertices.add(g)
        vertices.add(b)
        vertices.add(a)

        val idx = vertexCount.toShort()
        vertexCount++
        return idx
    }

    fun addTriangle(i0: Short, i1: Short, i2: Short) {
        indices.add(i0)
        indices.add(i1)
        indices.add(i2)
    }

    fun addQuad(i0: Short, i1: Short, i2: Short, i3: Short) {
        addTriangle(i0, i1, i2)
        addTriangle(i2, i3, i0)
    }

    fun addBox(
        cx: Float, cy: Float, cz: Float,
        w: Float, h: Float, d: Float,
        mat: Material
    ) {
        val hw = w * 0.5f
        val hh = h * 0.5f
        val hd = d * 0.5f

        val r = mat.r
        val g = mat.g
        val b = mat.b
        val a = mat.a

        // Front Face (+Z)
        var i0 = addVertex(cx - hw, cy - hh, cz + hd, 0f, 0f, 1f, r, g, b, a)
        var i1 = addVertex(cx + hw, cy - hh, cz + hd, 0f, 0f, 1f, r, g, b, a)
        var i2 = addVertex(cx + hw, cy + hh, cz + hd, 0f, 0f, 1f, r, g, b, a)
        var i3 = addVertex(cx - hw, cy + hh, cz + hd, 0f, 0f, 1f, r, g, b, a)
        addQuad(i0, i1, i2, i3)

        // Back Face (-Z)
        i0 = addVertex(cx + hw, cy - hh, cz - hd, 0f, 0f, -1f, r, g, b, a)
        i1 = addVertex(cx - hw, cy - hh, cz - hd, 0f, 0f, -1f, r, g, b, a)
        i2 = addVertex(cx - hw, cy + hh, cz - hd, 0f, 0f, -1f, r, g, b, a)
        i3 = addVertex(cx + hw, cy + hh, cz - hd, 0f, 0f, -1f, r, g, b, a)
        addQuad(i0, i1, i2, i3)

        // Top Face (+Y)
        i0 = addVertex(cx - hw, cy + hh, cz + hd, 0f, 1f, 0f, r, g, b, a)
        i1 = addVertex(cx + hw, cy + hh, cz + hd, 0f, 1f, 0f, r, g, b, a)
        i2 = addVertex(cx + hw, cy + hh, cz - hd, 0f, 1f, 0f, r, g, b, a)
        i3 = addVertex(cx - hw, cy + hh, cz - hd, 0f, 1f, 0f, r, g, b, a)
        addQuad(i0, i1, i2, i3)

        // Bottom Face (-Y)
        i0 = addVertex(cx - hw, cy - hh, cz - hd, 0f, -1f, 0f, r, g, b, a)
        i1 = addVertex(cx + hw, cy - hh, cz - hd, 0f, -1f, 0f, r, g, b, a)
        i2 = addVertex(cx + hw, cy - hh, cz + hd, 0f, -1f, 0f, r, g, b, a)
        i3 = addVertex(cx - hw, cy - hh, cz + hd, 0f, -1f, 0f, r, g, b, a)
        addQuad(i0, i1, i2, i3)

        // Right Face (+X)
        i0 = addVertex(cx + hw, cy - hh, cz + hd, 1f, 0f, 0f, r, g, b, a)
        i1 = addVertex(cx + hw, cy - hh, cz - hd, 1f, 0f, 0f, r, g, b, a)
        i2 = addVertex(cx + hw, cy + hh, cz - hd, 1f, 0f, 0f, r, g, b, a)
        i3 = addVertex(cx + hw, cy + hh, cz + hd, 1f, 0f, 0f, r, g, b, a)
        addQuad(i0, i1, i2, i3)

        // Left Face (-X)
        i0 = addVertex(cx - hw, cy - hh, cz - hd, -1f, 0f, 0f, r, g, b, a)
        i1 = addVertex(cx - hw, cy - hh, cz + hd, -1f, 0f, 0f, r, g, b, a)
        i2 = addVertex(cx - hw, cy + hh, cz + hd, -1f, 0f, 0f, r, g, b, a)
        i3 = addVertex(cx - hw, cy + hh, cz - hd, -1f, 0f, 0f, r, g, b, a)
        addQuad(i0, i1, i2, i3)
    }

    /**
     * Builds an Octahedron (Diamond / Energy Shard)
     */
    fun addOctahedron(cx: Float, cy: Float, cz: Float, radius: Float, mat: Material) {
        val r = mat.r
        val g = mat.g
        val b = mat.b
        val a = mat.a

        val top = addVertex(cx, cy + radius, cz, 0f, 1f, 0f, r, g, b, a)
        val bottom = addVertex(cx, cy - radius, cz, 0f, -1f, 0f, r, g, b, a)

        val f = addVertex(cx, cy, cz + radius * 0.7f, 0f, 0f, 1f, r, g, b, a)
        val rt = addVertex(cx + radius * 0.7f, cy, cz, 1f, 0f, 0f, r, g, b, a)
        val bk = addVertex(cx, cy, cz - radius * 0.7f, 0f, 0f, -1f, r, g, b, a)
        val lt = addVertex(cx - radius * 0.7f, cy, cz, -1f, 0f, 0f, r, g, b, a)

        // Top pyramid
        addTriangle(top, f, rt)
        addTriangle(top, rt, bk)
        addTriangle(top, bk, lt)
        addTriangle(top, lt, f)

        // Bottom pyramid
        addTriangle(bottom, rt, f)
        addTriangle(bottom, bk, rt)
        addTriangle(bottom, lt, bk)
        addTriangle(bottom, f, lt)
    }

    fun build(): Mesh {
        val vArray = FloatArray(vertices.size)
        for (i in vertices.indices) vArray[i] = vertices[i]

        val iArray = ShortArray(indices.size)
        for (i in indices.indices) iArray[i] = indices[i]

        return Mesh(vArray, iArray)
    }

    companion object {
        fun createBox(w: Float, h: Float, d: Float, mat: Material): Mesh {
            val builder = MeshBuilder()
            builder.addBox(0f, 0f, 0f, w, h, d, mat)
            return builder.build()
        }

        fun createCityStreetRoadMesh(width: Float, length: Float): Mesh {
            val builder = MeshBuilder()
            // Main dark textured asphalt road surface (center at Z = -length/2)
            builder.addBox(0f, -0.1f, -length * 0.5f, width, 0.2f, length, Material.ROAD_ASPHALT)

            // Painted white dashed lane markings separating the 3 lanes (at X = -1.1 and X = 1.1)
            val dashLength = 4.0f
            val gapLength = 3.0f
            var curZ = 0.0f
            while (curZ > -length) {
                val zCenter = curZ - dashLength * 0.5f
                builder.addBox(-1.1f, 0.02f, zCenter, 0.14f, 0.04f, dashLength, Material.ROAD_MARKING_WHITE)
                builder.addBox(1.1f, 0.02f, zCenter, 0.14f, 0.04f, dashLength, Material.ROAD_MARKING_WHITE)
                curZ -= (dashLength + gapLength)
            }

            // Road edge solid white fog lines at the outer lane boundaries
            builder.addBox(-width * 0.48f, 0.015f, -length * 0.5f, 0.12f, 0.03f, length, Material.ROAD_MARKING_WHITE)
            builder.addBox(width * 0.48f, 0.015f, -length * 0.5f, 0.12f, 0.03f, length, Material.ROAD_MARKING_WHITE)

            // Raised concrete curbs
            val curbWidth = 0.35f
            val curbHeight = 0.28f
            builder.addBox(-width * 0.5f - curbWidth * 0.5f, curbHeight * 0.5f, -length * 0.5f, curbWidth, curbHeight, length, Material.SIDEWALK_CURB)
            builder.addBox(width * 0.5f + curbWidth * 0.5f, curbHeight * 0.5f, -length * 0.5f, curbWidth, curbHeight, length, Material.SIDEWALK_CURB)

            // Broad concrete pedestrian sidewalks flanking the roadway
            val sidewalkWidth = 3.2f
            val sidewalkHeight = 0.22f
            val leftSidewalkCenter = -width * 0.5f - curbWidth - sidewalkWidth * 0.5f
            val rightSidewalkCenter = width * 0.5f + curbWidth + sidewalkWidth * 0.5f
            builder.addBox(leftSidewalkCenter, sidewalkHeight * 0.5f, -length * 0.5f, sidewalkWidth, sidewalkHeight, length, Material.SIDEWALK_CONCRETE)
            builder.addBox(rightSidewalkCenter, sidewalkHeight * 0.5f, -length * 0.5f, sidewalkWidth, sidewalkHeight, length, Material.SIDEWALK_CONCRETE)

            // Sidewalk details: Trees in square planters, modern lampposts, and fire hydrants
            val leftPavementX = -width * 0.5f - curbWidth - 1.2f
            val rightPavementX = width * 0.5f + curbWidth + 1.2f

            // Sidewalk Trees with trunks, leafy crowns, and stone planters at Z = -6.0f and Z = -22.0f
            val treeZ = floatArrayOf(-6.0f, -22.0f)
            for (z in treeZ) {
                // Left tree: Planter box
                builder.addBox(leftPavementX, 0.32f, z, 1.1f, 0.22f, 1.1f, Material.SIDEWALK_CURB)
                // Tree trunk
                builder.addBox(leftPavementX, 1.6f, z, 0.28f, 2.6f, 0.28f, Material.STREET_TREE_TRUNK)
                // Tree leafy canopy foliage
                builder.addBox(leftPavementX, 3.4f, z, 1.8f, 1.8f, 1.8f, Material.STREET_TREE_LEAVES)
                builder.addBox(leftPavementX, 4.4f, z, 1.2f, 0.8f, 1.2f, Material.STREET_TREE_LEAVES)

                // Right tree: Planter box
                builder.addBox(rightPavementX, 0.32f, z, 1.1f, 0.22f, 1.1f, Material.SIDEWALK_CURB)
                // Tree trunk
                builder.addBox(rightPavementX, 1.6f, z, 0.28f, 2.6f, 0.28f, Material.STREET_TREE_TRUNK)
                // Tree canopy
                builder.addBox(rightPavementX, 3.4f, z, 1.8f, 1.8f, 1.8f, Material.STREET_TREE_LEAVES)
                builder.addBox(rightPavementX, 4.4f, z, 1.2f, 0.8f, 1.2f, Material.STREET_TREE_LEAVES)
            }

            // Modern municipal streetlamps at Z = -14.0f
            val lampZ = -14.0f
            // Left streetlamp
            builder.addBox(leftPavementX - 0.4f, 2.4f, lampZ, 0.16f, 4.8f, 0.16f, Material.STREET_METAL_DARK)
            builder.addBox(leftPavementX, 4.8f, lampZ, 0.8f, 0.12f, 0.12f, Material.STREET_METAL_DARK)
            builder.addBox(leftPavementX + 0.35f, 4.65f, lampZ, 0.35f, 0.18f, 0.24f, Material.STREET_LAMP_WARM)

            // Right streetlamp
            builder.addBox(rightPavementX + 0.4f, 2.4f, lampZ, 0.16f, 4.8f, 0.16f, Material.STREET_METAL_DARK)
            builder.addBox(rightPavementX, 4.8f, lampZ, 0.8f, 0.12f, 0.12f, Material.STREET_METAL_DARK)
            builder.addBox(rightPavementX - 0.35f, 4.65f, lampZ, 0.35f, 0.18f, 0.24f, Material.STREET_LAMP_WARM)

            // Cast iron fire hydrant on right sidewalk at Z = -18.0f
            builder.addBox(rightPavementX - 0.2f, 0.55f, -18.0f, 0.32f, 0.65f, 0.32f, Material.FIRE_HYDRANT_RED)
            builder.addBox(rightPavementX - 0.2f, 0.82f, -18.0f, 0.46f, 0.14f, 0.22f, Material.FIRE_HYDRANT_RED)

            return builder.build()
        }

        fun createRoadSegment(width: Float, length: Float): Mesh = createCityStreetRoadMesh(width, length)

        fun createCityBuildingsSceneryMesh(): Mesh {
            val builder = MeshBuilder()

            // LEFT SIDE: Multi-Story Red-Brick Brownstone Apartment & Concrete Commercial Mid-Rise
            // Building 1: 4-Story Red-Brick Brownstone Apartment (Z = -8.0f, X = -9.8f)
            builder.addBox(-9.8f, 7.5f, -8.0f, 6.2f, 15.0f, 14.0f, Material.BUILDING_RED_BRICK)
            // Flat rooftop parapet and tar roof
            builder.addBox(-9.8f, 15.2f, -8.0f, 6.4f, 0.4f, 14.2f, Material.BUILDING_ROOF_TAR)
            // Rooftop cedar water cistern tower
            builder.addBox(-8.5f, 16.6f, -10.0f, 1.8f, 2.4f, 1.8f, Material.BUILDING_WATER_TOWER)
            builder.addBox(-8.5f, 15.4f, -10.0f, 1.4f, 0.8f, 1.4f, Material.STREET_METAL_DARK) // Tank stilts

            // Storefront on ground floor with blue canvas awning
            builder.addBox(-6.6f, 2.8f, -8.0f, 0.8f, 0.25f, 6.0f, Material.BUILDING_AWNING_BLUE)
            builder.addBox(-6.65f, 1.4f, -8.0f, 0.1f, 1.8f, 5.6f, Material.BUILDING_GLASS_WINDOW)

            // Brownstone window rows (floors 2, 3, 4)
            val windowZ = floatArrayOf(-12.0f, -9.5f, -6.5f, -4.0f)
            val windowY = floatArrayOf(5.5f, 8.8f, 12.0f)
            for (wy in windowY) {
                for (wz in windowZ) {
                    builder.addBox(-6.65f, wy, wz, 0.12f, 1.5f, 1.1f, Material.BUILDING_WINDOW_FRAME)
                    builder.addBox(-6.62f, wy, wz, 0.10f, 1.3f, 0.9f, Material.BUILDING_GLASS_WINDOW)
                }
            }

            // Building 2: Concrete Office High-Rise (Z = -22.0f, X = -10.5f)
            builder.addBox(-10.5f, 13.0f, -22.0f, 7.5f, 26.0f, 13.0f, Material.BUILDING_CONCRETE_GREY)
            val officeFloorY = floatArrayOf(4.0f, 8.5f, 13.0f, 17.5f, 22.0f)
            for (fy in officeFloorY) {
                builder.addBox(-6.7f, fy, -22.0f, 0.15f, 1.6f, 11.5f, Material.BUILDING_GLASS_WINDOW)
            }

            // RIGHT SIDE: Limestone Apartment, Bus Stop Shelter, and Commercial Tower
            // Building 3: Beige Limestone Apartment (Z = -9.0f, X = 9.8f)
            builder.addBox(9.8f, 8.0f, -9.0f, 6.2f, 16.0f, 13.0f, Material.BUILDING_STONE_BEIGE)
            builder.addBox(9.8f, 16.2f, -9.0f, 6.4f, 0.4f, 13.2f, Material.BUILDING_ROOF_TAR)
            // Red storefront canvas awning
            builder.addBox(6.6f, 2.8f, -9.0f, 0.8f, 0.25f, 5.5f, Material.BUILDING_AWNING_RED)
            builder.addBox(6.65f, 1.4f, -9.0f, 0.1f, 1.8f, 5.0f, Material.BUILDING_GLASS_WINDOW)

            // Limestone apartment windows
            val rWindowZ = floatArrayOf(-12.5f, -9.0f, -5.5f)
            val rWindowY = floatArrayOf(5.5f, 9.0f, 12.5f)
            for (wy in rWindowY) {
                for (wz in rWindowZ) {
                    builder.addBox(6.65f, wy, wz, 0.12f, 1.5f, 1.1f, Material.BUILDING_WINDOW_FRAME)
                    builder.addBox(6.62f, wy, wz, 0.10f, 1.3f, 0.9f, Material.BUILDING_GLASS_WINDOW)
                }
            }

            // Realistic Bus Stop Passenger Shelter on right sidewalk (at Z = -16.0f, X = 5.6f)
            builder.addBox(5.6f, 2.4f, -16.0f, 1.8f, 0.15f, 3.6f, Material.STREET_METAL_DARK)
            builder.addBox(6.4f, 1.2f, -16.0f, 0.1f, 2.4f, 3.4f, Material.BUS_SHELTER_GLASS)
            builder.addBox(5.6f, 1.2f, -17.7f, 1.6f, 2.4f, 0.1f, Material.BUS_SHELTER_GLASS)
            builder.addBox(5.6f, 0.5f, -16.0f, 0.5f, 0.1f, 2.4f, Material.STREET_TREE_TRUNK)

            // Building 4: Modern Commercial Tower (Z = -24.0f, X = 10.5f)
            builder.addBox(10.5f, 15.0f, -24.0f, 7.5f, 30.0f, 14.0f, Material.BUILDING_CONCRETE_GREY)
            builder.addBox(8.5f, 30.8f, -22.0f, 2.4f, 1.4f, 3.0f, Material.STREET_METAL_DARK) // HVAC
            val towerFloors = floatArrayOf(5.0f, 10.0f, 15.0f, 20.0f, 25.0f)
            for (tf in towerFloors) {
                builder.addBox(6.7f, tf, -24.0f, 0.15f, 1.8f, 12.0f, Material.BUILDING_GLASS_WINDOW)
            }

            return builder.build()
        }

        fun createBuildingScenery(): Mesh = createCityBuildingsSceneryMesh()

        /**
         * Obstacle 1: Municipal Construction Roadblock Barrier (Requires JUMP)
         * Total height ~0.65m. Player jumps over.
         */
        fun createLowHurdle(): Mesh {
            val builder = MeshBuilder()
            // Sturdy black steel A-frame base support legs on left & right
            builder.addBox(-0.85f, 0.32f, 0f, 0.16f, 0.64f, 0.16f, Material.STREET_METAL_DARK)
            builder.addBox(-0.85f, 0.05f, 0f, 0.18f, 0.10f, 0.45f, Material.STREET_METAL_DARK)

            builder.addBox(0.85f, 0.32f, 0f, 0.16f, 0.64f, 0.16f, Material.STREET_METAL_DARK)
            builder.addBox(0.85f, 0.05f, 0f, 0.18f, 0.10f, 0.45f, Material.STREET_METAL_DARK)

            // Main horizontal barricade panel (width 1.8m, height 0.40m, center Y = 0.42m)
            builder.addBox(0f, 0.42f, 0f, 1.8f, 0.40f, 0.12f, Material.BARRIER_ORANGE)

            // Reflective white diagonal warning stripe blocks
            val stripeX = floatArrayOf(-0.60f, -0.20f, 0.20f, 0.60f)
            for (sx in stripeX) {
                builder.addBox(sx, 0.42f, 0.065f, 0.20f, 0.36f, 0.02f, Material.BARRIER_WHITE)
                builder.addBox(sx, 0.42f, -0.065f, 0.20f, 0.36f, 0.02f, Material.BARRIER_WHITE)
            }

            // Top flashing amber construction hazard beacon
            builder.addBox(0f, 0.70f, 0f, 0.18f, 0.16f, 0.18f, Material.BARRIER_WARNING_LIGHT)
            builder.addBox(0f, 0.62f, 0f, 0.22f, 0.06f, 0.22f, Material.STREET_METAL_DARK)
            return builder.build()
        }

        /**
         * Obstacle 2: Overhead Highway Clearance Girder & Traffic Sign (Requires SLIDE)
         * Frame height = 2.7m, clearance bar Y = 1.05m to 1.65m.
         * Normal runner (height 1.8m) hits clearance bar, sliding runner (height 0.75m) clears under it!
         */
        fun createHighLaserGate(): Mesh {
            val builder = MeshBuilder()
            // Heavy galvanized steel vertical I-beam support pylons on road edges
            builder.addBox(-0.95f, 1.35f, 0f, 0.22f, 2.7f, 0.24f, Material.STREET_METAL_DARK)
            builder.addBox(0.95f, 1.35f, 0f, 0.22f, 2.7f, 0.24f, Material.STREET_METAL_DARK)

            // Heavy structural steel overhead cross girder at top
            builder.addBox(0f, 2.65f, 0f, 2.12f, 0.30f, 0.28f, Material.STREET_METAL_DARK)

            // Hanging green highway clearance lane sign
            builder.addBox(0f, 2.2f, 0.14f, 1.7f, 0.55f, 0.06f, Material.HIGHWAY_SIGN_GREEN)
            builder.addBox(0f, 2.2f, 0.17f, 1.55f, 0.08f, 0.02f, Material.ROAD_MARKING_WHITE)

            // Low-hanging yellow/black hazard clearance bar (Y = 1.05m to 1.65m)
            builder.addBox(0f, 1.35f, 0f, 1.85f, 0.55f, 0.14f, Material.BARRIER_WARNING_LIGHT)
            val stripeX = floatArrayOf(-0.65f, -0.22f, 0.22f, 0.65f)
            for (sx in stripeX) {
                builder.addBox(sx, 1.35f, 0.075f, 0.20f, 0.50f, 0.02f, Material.STREET_METAL_DARK)
                builder.addBox(sx, 1.35f, -0.075f, 0.20f, 0.50f, 0.02f, Material.STREET_METAL_DARK)
            }
            return builder.build()
        }

        /**
         * Obstacle 3: Concrete Jersey Road Barrier & Municipal Dumpster (Requires LANE SWITCH)
         * Solid roadblock blocking entire lane (height 2.4m, width 1.8m).
         * Cannot be jumped or slid under.
         */
        fun createCyberBlock(): Mesh {
            val builder = MeshBuilder()
            // Bottom heavy concrete Jersey safety barrier base
            builder.addBox(0f, 0.42f, 0f, 1.8f, 0.84f, 0.90f, Material.BUILDING_CONCRETE_GREY)
            builder.addBox(0f, 0.88f, 0f, 1.7f, 0.12f, 0.65f, Material.SIDEWALK_CURB)

            // High-visibility orange & white reflective chevron band across jersey barrier
            builder.addBox(0f, 0.45f, 0.46f, 1.65f, 0.24f, 0.04f, Material.BARRIER_ORANGE)
            builder.addBox(-0.45f, 0.45f, 0.485f, 0.25f, 0.24f, 0.02f, Material.BARRIER_WHITE)
            builder.addBox(0.45f, 0.45f, 0.485f, 0.25f, 0.24f, 0.02f, Material.BARRIER_WHITE)

            // Upper heavy industrial dark green steel roadblock dumpster (full height 2.4m)
            builder.addBox(0f, 1.65f, 0f, 1.75f, 1.45f, 0.85f, Material.DUMPSTER_GREEN)
            builder.addBox(0f, 2.42f, 0f, 1.78f, 0.12f, 0.88f, Material.STREET_METAL_DARK)
            builder.addBox(-0.85f, 1.4f, 0f, 0.12f, 0.22f, 0.80f, Material.STREET_METAL_DARK)
            builder.addBox(0.85f, 1.4f, 0f, 0.12f, 0.22f, 0.80f, Material.STREET_METAL_DARK)

            return builder.build()
        }

        /**
         * Energy Shard Collectible (Floating Gold Octahedron)
         */
        fun createEnergyShard(): Mesh {
            val builder = MeshBuilder()
            builder.addOctahedron(0f, 0f, 0f, 0.38f, Material.SHARD_GOLD)
            return builder.build()
        }

        /**
         * Highway Underpass / Concrete Arch Tunnel: Overhead concrete bridge arches with underpass lights
         */
        fun createTunnelRibMesh(): Mesh {
            val builder = MeshBuilder()
            val archZOffsets = floatArrayOf(-4.0f, -12.0f, -20.0f, -28.0f)
            val archWidth = 9.4f
            val archHeight = 5.2f
            val colThick = 0.55f

            for (z in archZOffsets) {
                builder.addBox(-archWidth * 0.5f, archHeight * 0.5f, z, colThick, archHeight, colThick, Material.BUILDING_CONCRETE_GREY)
                builder.addBox(archWidth * 0.5f, archHeight * 0.5f, z, colThick, archHeight, colThick, Material.BUILDING_CONCRETE_GREY)
                builder.addBox(0f, archHeight, z, archWidth + colThick, colThick, colThick, Material.BUILDING_CONCRETE_GREY)
                builder.addBox(-1.8f, archHeight - colThick * 0.55f, z, 0.9f, 0.10f, 0.22f, Material.STREET_LAMP_WARM)
                builder.addBox(1.8f, archHeight - colThick * 0.55f, z, 0.9f, 0.10f, 0.22f, Material.STREET_LAMP_WARM)
            }
            return builder.build()
        }

        /**
         * Highway Suspension Bridge & Viaduct: Elevated highway bridge with steel guardrails and piers
         */
        fun createBridgePylonMesh(): Mesh {
            val builder = MeshBuilder()
            val railWidth = 0.28f
            val railHeight = 1.15f

            // Left & Right highway W-beam galvanized steel guardrails
            builder.addBox(-4.6f, railHeight * 0.5f, -15.0f, railWidth, railHeight, 30.0f, Material.HIGHWAY_GUARDRAIL_STEEL)
            builder.addBox(4.6f, railHeight * 0.5f, -15.0f, railWidth, railHeight, 30.0f, Material.HIGHWAY_GUARDRAIL_STEEL)

            // Massive concrete viaduct piers supporting the elevated highway deck
            builder.addBox(-4.8f, -6.0f, -8.0f, 1.4f, 12.0f, 1.4f, Material.BUILDING_CONCRETE_GREY)
            builder.addBox(4.8f, -6.0f, -8.0f, 1.4f, 12.0f, 1.4f, Material.BUILDING_CONCRETE_GREY)
            builder.addBox(-4.8f, -6.0f, -22.0f, 1.4f, 12.0f, 1.4f, Material.BUILDING_CONCRETE_GREY)
            builder.addBox(4.8f, -6.0f, -22.0f, 1.4f, 12.0f, 1.4f, Material.BUILDING_CONCRETE_GREY)

            // Bridge highway lamppost towers
            builder.addBox(-4.7f, 3.2f, -15.0f, 0.16f, 5.0f, 0.16f, Material.STREET_METAL_DARK)
            builder.addBox(-4.4f, 5.6f, -15.0f, 0.6f, 0.18f, 0.25f, Material.STREET_LAMP_WARM)

            builder.addBox(4.7f, 3.2f, -15.0f, 0.16f, 5.0f, 0.16f, Material.STREET_METAL_DARK)
            builder.addBox(4.4f, 5.6f, -15.0f, 0.6f, 0.18f, 0.25f, Material.STREET_LAMP_WARM)

            return builder.build()
        }

        /**
         * Interstate Overhead Green Highway Sign Gantry
         */
        fun createOverpassGantryMesh(): Mesh {
            val builder = MeshBuilder()
            val gantryZ = -15.0f

            builder.addBox(-4.8f, 3.4f, gantryZ, 0.55f, 6.8f, 0.55f, Material.STREET_METAL_DARK)
            builder.addBox(4.8f, 3.4f, gantryZ, 0.55f, 6.8f, 0.55f, Material.STREET_METAL_DARK)

            builder.addBox(0f, 6.4f, gantryZ, 10.4f, 0.75f, 0.55f, Material.STREET_METAL_DARK)

            builder.addBox(-1.8f, 5.1f, gantryZ, 3.4f, 1.7f, 0.12f, Material.HIGHWAY_SIGN_GREEN)
            builder.addBox(-1.8f, 5.1f, gantryZ - 0.08f, 3.2f, 0.10f, 0.02f, Material.ROAD_MARKING_WHITE)
            builder.addBox(1.8f, 5.1f, gantryZ, 3.4f, 1.7f, 0.12f, Material.HIGHWAY_SIGN_GREEN)
            builder.addBox(1.8f, 5.1f, gantryZ - 0.08f, 3.2f, 0.10f, 0.02f, Material.ROAD_MARKING_WHITE)

            builder.addBox(-1.8f, 6.0f, gantryZ - 0.4f, 0.45f, 0.12f, 0.35f, Material.STREET_LAMP_WARM)
            builder.addBox(1.8f, 6.0f, gantryZ - 0.4f, 0.45f, 0.12f, 0.35f, Material.STREET_LAMP_WARM)

            return builder.build()
        }

        /**
         * Solar Canopy District: Golden solar wing collectors along the road perimeter
         */
        fun createSolarCanopyMesh(): Mesh {
            val builder = MeshBuilder()
            val canopyZOffsets = floatArrayOf(-6.0f, -18.0f)

            for (z in canopyZOffsets) {
                // Left solar wing
                builder.addBox(-6.0f, 3.8f, z, 2.5f, 0.12f, 7.0f, Material.SOLAR_GOLD)
                builder.addBox(-7.2f, 2.0f, z, 0.4f, 4.0f, 0.4f, Material.TUNNEL_FRAME)

                // Right solar wing
                builder.addBox(6.0f, 3.8f, z, 2.5f, 0.12f, 7.0f, Material.SOLAR_GOLD)
                builder.addBox(7.2f, 2.0f, z, 0.4f, 4.0f, 0.4f, Material.TUNNEL_FRAME)
            }

            return builder.build()
        }

        /**
         * Autonomous Patrol Drone: Aerodynamic hovering chassis with twin thruster pods and red scanner beam
         */
        fun createPatrolDroneMesh(): Mesh {
            val builder = MeshBuilder()
            builder.addBox(0f, 0f, 0f, 0.75f, 0.35f, 0.65f, Material.DRONE_BODY)
            builder.addBox(-0.55f, 0.05f, 0f, 0.32f, 0.28f, 0.55f, Material.STREET_METAL_DARK)
            builder.addBox(-0.55f, 0.05f, 0.28f, 0.22f, 0.2f, 0.05f, Material.ROAD_MARKING_WHITE)
            builder.addBox(0.55f, 0.05f, 0.28f, 0.22f, 0.2f, 0.05f, Material.ROAD_MARKING_WHITE)
            builder.addBox(0f, -0.05f, 0.34f, 0.45f, 0.14f, 0.08f, Material.DRONE_SCANNER_RED)
            return builder.build()
        }

        /**
         * Sliding Hazard Gate: Motorized heavy industrial construction barrier moving horizontally across lanes
         */
        fun createSlidingGateMesh(): Mesh {
            val builder = MeshBuilder()
            builder.addBox(0f, 0.95f, 0f, 1.8f, 1.9f, 0.24f, Material.STREET_METAL_DARK)
            builder.addBox(0f, 1.45f, 0f, 1.65f, 0.35f, 0.28f, Material.BARRIER_WARNING_LIGHT)
            builder.addBox(0f, 0.55f, 0f, 1.65f, 0.35f, 0.28f, Material.BARRIER_WARNING_LIGHT)
            builder.addBox(0f, 2.0f, 0f, 0.22f, 0.22f, 0.22f, Material.BARRIER_WARNING_LIGHT)
            return builder.build()
        }

        /**
         * Wooden Shipping Crate: Jumpable or breakable obstacle
         */
        fun createBreakableCrateMesh(): Mesh {
            val builder = MeshBuilder()
            builder.addBox(0f, 0.55f, 0f, 1.35f, 1.1f, 1.1f, Material.STREET_TREE_TRUNK)
            builder.addBox(0f, 0.55f, 0.56f, 1.1f, 0.16f, 0.04f, Material.STREET_METAL_DARK)
            builder.addBox(0f, 0.55f, -0.56f, 1.1f, 0.16f, 0.04f, Material.STREET_METAL_DARK)
            builder.addBox(0f, 1.12f, 0f, 1.1f, 0.04f, 0.85f, Material.STREET_METAL_DARK)
            return builder.build()
        }

        /**
         * Construction Girder Debris: Heavy structural steel I-beam with caution markings
         */
        fun createFallingDebrisMesh(): Mesh {
            val builder = MeshBuilder()
            builder.addBox(0f, 0.25f, 0f, 1.8f, 0.5f, 0.5f, Material.STREET_METAL_DARK)
            builder.addBox(0f, 0.48f, 0f, 1.6f, 0.05f, 0.4f, Material.BARRIER_WARNING_LIGHT)
            return builder.build()
        }

        /**
         * Phase Core: Glowing cyan crystal with outer stabilizing orbital ring
         */
        fun createPhaseCoreMesh(): Mesh {
            val builder = MeshBuilder()
            // Inner cyan core (compact 0.30m)
            builder.addOctahedron(0f, 0f, 0f, 0.30f, Material.PHASE_CORE_CYAN)
            // Orbital ring (compact 0.44m)
            builder.addBox(0f, 0f, 0f, 0.44f, 0.04f, 0.44f, Material.ROAD_LANE_CYAN)
            return builder.build()
        }

        /**
         * Credit: High-luster magenta and gold cybernetic currency coin
         */
        fun createCreditMesh(): Mesh {
            val builder = MeshBuilder()
            // Octagonal cyber coin
            builder.addBox(0f, 0f, 0f, 0.55f, 0.55f, 0.12f, Material.CREDIT_MAGENTA)
            // Golden center core
            builder.addBox(0f, 0f, 0f, 0.28f, 0.28f, 0.16f, Material.SHARD_GOLD)
            return builder.build()
        }

        /**
         * Multiplier Token: Violet prismatic multiplier star
         */
        fun createMultiplierTokenMesh(): Mesh {
            val builder = MeshBuilder()
            builder.addOctahedron(0f, 0f, 0f, 0.5f, Material.TOKEN_VIOLET)
            builder.addBox(0f, 0f, 0f, 0.7f, 0.1f, 0.7f, Material.ROAD_CURB_MAGENTA)
            return builder.build()
        }

        /**
         * Inclined Highway Ramp: Slopes road deck smoothly between startY and endY
         */
        fun createRampRoadMesh(width: Float, length: Float, startY: Float, endY: Float): Mesh {
            val builder = MeshBuilder()
            val slices = 6
            val sliceLength = length / slices
            val curbWidth = 0.40f
            val curbHeight = 0.35f

            for (i in 0 until slices) {
                val zNear = -(i * sliceLength)
                val zFar = -((i + 1) * sliceLength)
                val zCenter = (zNear + zFar) * 0.5f

                val tCenter = (i + 0.5f) / slices
                val yCenter = startY + tCenter * (endY - startY)
                val rampThickness = 0.35f

                // Main sloping asphalt road deck
                builder.addBox(0f, yCenter - 0.1f, zCenter, width, rampThickness, sliceLength, Material.ROAD_ASPHALT)

                // Substructure concrete ramp support fill down to ground
                val underY = (yCenter - 0.1f - rampThickness * 0.5f) * 0.5f
                val underHeight = (yCenter - 0.1f - rampThickness * 0.5f)
                if (underHeight > 0.1f) {
                    builder.addBox(0f, underY, zCenter, width - 0.2f, underHeight, sliceLength, Material.BUILDING_CONCRETE_GREY)
                }

                // White painted lane divider markings
                builder.addBox(-1.1f, yCenter + 0.08f, zCenter, 0.14f, 0.04f, sliceLength * 0.6f, Material.ROAD_MARKING_WHITE)
                builder.addBox(1.1f, yCenter + 0.08f, zCenter, 0.14f, 0.04f, sliceLength * 0.6f, Material.ROAD_MARKING_WHITE)

                // Concrete outer curbs
                builder.addBox(-width * 0.5f - curbWidth * 0.5f, yCenter + curbHeight * 0.5f, zCenter, curbWidth, curbHeight, sliceLength, Material.SIDEWALK_CURB)
                builder.addBox(width * 0.5f + curbWidth * 0.5f, yCenter + curbHeight * 0.5f, zCenter, curbWidth, curbHeight, sliceLength, Material.SIDEWALK_CURB)
            }

            return builder.build()
        }

        /**
         * Elevated Skyway Viaduct: Flat highway deck elevated at elevationY (3.5m)
         */
        fun createElevatedRoadMesh(width: Float, length: Float, elevationY: Float = 3.5f): Mesh {
            val builder = MeshBuilder()

            // Main elevated deck
            builder.addBox(0f, elevationY - 0.15f, -length * 0.5f, width, 0.35f, length, Material.ROAD_ASPHALT)

            // White lane divider markings
            val dashLength = 4.0f
            val gapLength = 3.0f
            var curZ = 0.0f
            while (curZ > -length) {
                val zCenter = curZ - dashLength * 0.5f
                builder.addBox(-1.1f, elevationY + 0.04f, zCenter, 0.14f, 0.04f, dashLength, Material.ROAD_MARKING_WHITE)
                builder.addBox(1.1f, elevationY + 0.04f, zCenter, 0.14f, 0.04f, dashLength, Material.ROAD_MARKING_WHITE)
                curZ -= (dashLength + gapLength)
            }

            // Outer concrete highway barriers & galvanized steel safety guardrails
            val curbWidth = 0.40f
            val railHeight = 1.15f
            builder.addBox(-width * 0.5f - curbWidth * 0.5f, elevationY + railHeight * 0.5f, -length * 0.5f, curbWidth, railHeight, length, Material.SIDEWALK_CURB)
            builder.addBox(-width * 0.5f - curbWidth * 0.5f, elevationY + railHeight + 0.05f, -length * 0.5f, 0.15f, 0.1f, length, Material.HIGHWAY_GUARDRAIL_STEEL)

            builder.addBox(width * 0.5f + curbWidth * 0.5f, elevationY + railHeight * 0.5f, -length * 0.5f, curbWidth, railHeight, length, Material.SIDEWALK_CURB)
            builder.addBox(width * 0.5f + curbWidth * 0.5f, elevationY + railHeight + 0.05f, -length * 0.5f, 0.15f, 0.1f, length, Material.HIGHWAY_GUARDRAIL_STEEL)

            // Concrete foundation piers extending to the ground
            val pierHeight = elevationY + 4.0f
            val pierCenterY = (elevationY - pierHeight) * 0.5f
            builder.addBox(-width * 0.4f, pierCenterY, -8.0f, 1.2f, pierHeight, 1.2f, Material.BUILDING_CONCRETE_GREY)
            builder.addBox(width * 0.4f, pierCenterY, -8.0f, 1.2f, pierHeight, 1.2f, Material.BUILDING_CONCRETE_GREY)
            builder.addBox(-width * 0.4f, pierCenterY, -22.0f, 1.2f, pierHeight, 1.2f, Material.BUILDING_CONCRETE_GREY)
            builder.addBox(width * 0.4f, pierCenterY, -22.0f, 1.2f, pierHeight, 1.2f, Material.BUILDING_CONCRETE_GREY)

            return builder.build()
        }

        /**
         * Fracture Split Holographic Sign & Divider Decor:
         * Overhead signpost with green (safe left) and amber (hazard bounty right) guidance displays
         */
        fun createFractureSplitDecorMesh(): Mesh {
            val builder = MeshBuilder()
            val signZ = -8.0f

            // Overhead portal truss
            builder.addBox(-4.8f, 3.2f, signZ, 0.5f, 6.4f, 0.5f, Material.GANTRY_STRUCTURE)
            builder.addBox(4.8f, 3.2f, signZ, 0.5f, 6.4f, 0.5f, Material.GANTRY_STRUCTURE)
            builder.addBox(0f, 6.2f, signZ, 10.2f, 0.7f, 0.5f, Material.GANTRY_STRUCTURE)

            // Background display board
            builder.addBox(0f, 5.0f, signZ, 8.4f, 1.8f, 0.1f, Material.FRACTURE_SIGN_BG)

            // Left guidance arrow banner [SAFE - Green]
            builder.addBox(-2.2f, 5.0f, signZ + 0.08f, 3.2f, 1.2f, 0.06f, Material.FRACTURE_SAFE_GREEN)

            // Right danger guidance arrow banner [BOUNTY - Amber/Red]
            builder.addBox(2.2f, 5.0f, signZ + 0.08f, 3.2f, 1.2f, 0.06f, Material.FRACTURE_HAZARD_AMBER)

            // Central divider pylon separating the lanes down the track
            builder.addBox(0f, 0.6f, -18.0f, 0.35f, 1.2f, 12.0f, Material.TUNNEL_FRAME)
            builder.addBox(0f, 1.25f, -18.0f, 0.15f, 0.12f, 12.0f, Material.ROAD_CURB_MAGENTA)

            return builder.build()
        }

        /**
         * Power-Up 1: Kinetic Shield Orb (Compact cyan energy crystal with protective shield icon)
         */
        fun createShieldOrbMesh(): Mesh {
            val builder = MeshBuilder()
            builder.addOctahedron(0f, 0f, 0f, 0.30f, Material.POWERUP_SHIELD)
            builder.addBox(0f, 0f, 0f, 0.42f, 0.42f, 0.05f, Material.BRIDGE_GUARD_CYAN)
            return builder.build()
        }

        /**
         * Power-Up 2: Overdrive Booster Capsule (Compact rocket thruster capsule with blazing core)
         */
        fun createOverdriveOrbMesh(): Mesh {
            val builder = MeshBuilder()
            builder.addBox(0f, 0f, 0f, 0.28f, 0.45f, 0.28f, Material.POWERUP_OVERDRIVE)
            builder.addBox(-0.20f, -0.05f, 0f, 0.10f, 0.32f, 0.10f, Material.BUILDING_DARK)
            builder.addBox(0.20f, -0.05f, 0f, 0.10f, 0.32f, 0.10f, Material.BUILDING_DARK)
            builder.addBox(0f, 0.26f, 0f, 0.16f, 0.16f, 0.16f, Material.HURDLE_AMBER)
            return builder.build()
        }

        /**
         * Power-Up 3: Phase Battery Cell (Compact violet power cylinder with glowing terminals)
         */
        fun createPhaseBatteryOrbMesh(): Mesh {
            val builder = MeshBuilder()
            builder.addBox(0f, 0f, 0f, 0.30f, 0.42f, 0.30f, Material.POWERUP_BATTERY)
            builder.addBox(0f, 0.24f, 0f, 0.18f, 0.10f, 0.18f, Material.ROAD_CURB_MAGENTA)
            builder.addBox(0f, -0.24f, 0f, 0.18f, 0.10f, 0.18f, Material.ROAD_CURB_MAGENTA)
            return builder.build()
        }

        /**
         * Power-Up 4: Time Brake Chrono Dial (Compact emerald ring with spinning time tick markers)
         */
        fun createTimeBrakeOrbMesh(): Mesh {
            val builder = MeshBuilder()
            builder.addOctahedron(0f, 0f, 0f, 0.28f, Material.POWERUP_CHRONO)
            builder.addBox(0f, 0f, 0f, 0.44f, 0.04f, 0.44f, Material.FRACTURE_SAFE_GREEN)
            return builder.build()
        }

        /**
         * Power-Up 5: Quantum Magnet (Compact horseshoe electromagnetic core with neon pole tips)
         */
        fun createMagnetOrbMesh(): Mesh {
            val builder = MeshBuilder()
            // Central magnetic core
            builder.addOctahedron(0f, 0f, 0f, 0.26f, Material.POWERUP_MAGNET)
            // U-shape / horseshoe magnetic arms
            builder.addBox(-0.16f, 0.06f, 0f, 0.08f, 0.26f, 0.12f, Material.MAGNET_POLE_RED)
            builder.addBox(0.16f, 0.06f, 0f, 0.08f, 0.26f, 0.12f, Material.POWERUP_MAGNET)
            builder.addBox(0f, -0.08f, 0f, 0.34f, 0.09f, 0.12f, Material.BUILDING_DARK)
            // Magnetic field rings
            builder.addBox(0f, 0.20f, 0f, 0.44f, 0.03f, 0.44f, Material.POWERUP_MAGNET)
            return builder.build()
        }

        /**
         * Power-Up 6: Neon Hoverboard Orb (Track collectible pickup)
         * Compact spinning miniature mag-lev hoverboard with glowing cyan edge rails,
         * magenta nose diode, rear thruster jets, and gyroscopic anti-grav ring.
         */
        fun createHoverboardOrbMesh(): Mesh {
            val builder = MeshBuilder()
            // Miniature hoverboard deck plate (~0.24m x 0.48m)
            builder.addBox(0f, 0f, 0f, 0.24f, 0.04f, 0.48f, Material.POWERUP_HOVERBOARD_DECK)
            // Left & Right neon cyan rails
            builder.addBox(-0.13f, 0.01f, 0f, 0.03f, 0.05f, 0.50f, Material.POWERUP_HOVERBOARD_CYAN)
            builder.addBox(0.13f, 0.01f, 0f, 0.03f, 0.05f, 0.50f, Material.POWERUP_HOVERBOARD_CYAN)
            // Magenta aerodynamic nose tip
            builder.addOctahedron(0f, 0.02f, -0.28f, 0.06f, Material.POWERUP_HOVERBOARD_MAGENTA)
            // Rear micro plasma jet nozzles
            builder.addBox(-0.07f, 0.01f, 0.28f, 0.05f, 0.05f, 0.08f, Material.POWERUP_HOVERBOARD_THRUSTER)
            builder.addBox(0.07f, 0.01f, 0.28f, 0.05f, 0.05f, 0.08f, Material.POWERUP_HOVERBOARD_THRUSTER)
            // Gyroscopic anti-grav outer levitation ring
            builder.addBox(0f, 0f, 0f, 0.44f, 0.025f, 0.44f, Material.POWERUP_HOVERBOARD_CYAN)
            return builder.build()
        }

        /**
         * Neon Mag-Lev Hoverboard: Futuristic cyber hoverboard deck mounted under the runner's feet.
         * Features dark carbon deck, foot traction grip pads, high-intensity neon cyan edge rails,
         * magenta chevron front nose, twin rear plasma ion thrusters, and underside anti-grav repulsor pads.
         */
        fun createNeonHoverboardMesh(
            primaryMat: Material = Material.POWERUP_HOVERBOARD_CYAN,
            accentMat: Material = Material.POWERUP_HOVERBOARD_MAGENTA
        ): Mesh {
            val builder = MeshBuilder()
            // Main dark cyber deck plate (~0.52m wide x 1.25m long)
            builder.addBox(0f, 0f, 0f, 0.52f, 0.05f, 1.25f, Material.POWERUP_HOVERBOARD_DECK)

            // Textured foot traction grip pads (front and back stances)
            builder.addBox(-0.14f, 0.035f, 0.20f, 0.18f, 0.02f, 0.35f, Material.BUILDING_DARK)
            builder.addBox(0.14f, 0.035f, -0.20f, 0.18f, 0.02f, 0.35f, Material.BUILDING_DARK)

            // High-luminance neon primary edge glow rails running full length
            builder.addBox(-0.28f, 0.01f, 0f, 0.05f, 0.07f, 1.30f, primaryMat)
            builder.addBox(0.28f, 0.01f, 0f, 0.05f, 0.07f, 1.30f, primaryMat)

            // Aerodynamic upturned nose wedge (front accent chevron)
            builder.addBox(0f, 0.03f, -0.65f, 0.40f, 0.06f, 0.16f, accentMat)
            builder.addOctahedron(0f, 0.05f, -0.74f, 0.08f, accentMat)

            // Twin rear ion plasma thruster housings & glowing exhaust flames
            builder.addBox(-0.18f, 0.02f, 0.65f, 0.12f, 0.10f, 0.16f, Material.BUILDING_DARK)
            builder.addBox(0.18f, 0.02f, 0.65f, 0.12f, 0.10f, 0.16f, Material.BUILDING_DARK)
            builder.addBox(-0.18f, 0.02f, 0.77f, 0.08f, 0.08f, 0.16f, accentMat)
            builder.addBox(0.18f, 0.02f, 0.77f, 0.08f, 0.08f, 0.16f, accentMat)

            // Dual underside anti-grav repulsor pads (cast subtle downward hover glow)
            builder.addBox(0f, -0.04f, -0.32f, 0.36f, 0.03f, 0.28f, primaryMat)
            builder.addBox(0f, -0.04f, 0.32f, 0.36f, 0.03f, 0.28f, primaryMat)

            return builder.build()
        }

        /**
         * Quantum Magnet Satellite Drone: Miniature floating horseshoe magnet with dual red/cyan poles
         * that orbits gracefully around the character without obstructing forward visibility.
         */
        fun createMagnetFieldMesh(): Mesh {
            val builder = MeshBuilder()
            // Base arch
            builder.addBox(0f, 0f, 0f, 0.26f, 0.07f, 0.10f, Material.BUILDING_DARK)
            // Left red pole arm
            builder.addBox(-0.10f, 0.10f, 0f, 0.06f, 0.16f, 0.10f, Material.MAGNET_POLE_RED)
            // Right cyan pole arm
            builder.addBox(0.10f, 0.10f, 0f, 0.06f, 0.16f, 0.10f, Material.POWERUP_MAGNET)
            // Electromagnetic neon spark bridging the tips
            builder.addBox(0f, 0.17f, 0f, 0.15f, 0.02f, 0.035f, Material.POWERUP_MAGNET)
            // Floating micro core
            builder.addOctahedron(0f, 0.05f, 0f, 0.05f, Material.POWERUP_MAGNET)
            return builder.build()
        }

        /**
         * Kinetic Aegis Deflector Drone: Miniature floating energy buckler drone with cyan forcefield
         * that orbits the runner gracefully without any bulky bubbles obscuring track visibility.
         */
        fun createKineticShieldBubbleMesh(): Mesh {
            val builder = MeshBuilder()
            // Dark buckler chassis plate (~0.24m x 0.28m)
            builder.addBox(0f, 0f, 0f, 0.24f, 0.28f, 0.04f, Material.BUILDING_DARK)
            // Protective glowing cross crest
            builder.addBox(0f, 0f, 0.025f, 0.16f, 0.04f, 0.02f, Material.POWERUP_SHIELD)
            builder.addBox(0f, 0f, 0.025f, 0.04f, 0.20f, 0.02f, Material.POWERUP_SHIELD)
            // Floating diamond energy pulse core
            builder.addOctahedron(0f, 0f, 0.04f, 0.06f, Material.POWERUP_SHIELD)
            // Micro energy rim
            builder.addBox(0f, 0f, 0f, 0.28f, 0.32f, 0.015f, Material.BRIDGE_GUARD_CYAN)
            return builder.build()
        }

        /**
         * Overdrive Micro Hyperthrusters: Sleek, compact exhaust plasma jets tight to runner's upper back
         */
        fun createOverdriveTrailMesh(): Mesh {
            val builder = MeshBuilder()
            // Compact twin plasma flames (~0.28m length, 0.07m width)
            builder.addBox(-0.16f, 0.55f, 0.22f, 0.07f, 0.07f, 0.28f, Material.POWERUP_OVERDRIVE)
            builder.addBox(0.16f, 0.55f, 0.22f, 0.07f, 0.07f, 0.28f, Material.POWERUP_OVERDRIVE)
            // Amber core spark
            builder.addBox(0f, 0.56f, 0.24f, 0.05f, 0.05f, 0.20f, Material.HURDLE_AMBER)
            return builder.build()
        }

        /**
         * "The Null" Chaser Entity: Ominous jagged shadow anomaly with pulsing necrotic crimson core
         * and reaching void tendrils that pursues the runner from behind.
         */
        fun createNullChaserMesh(): Mesh {
            val builder = MeshBuilder()

            // Central dark void body mass
            builder.addBox(0f, 1.4f, 0f, 2.2f, 2.4f, 1.8f, Material.NULL_VOID_DARK)
            builder.addOctahedron(0f, 1.4f, 0f, 1.5f, Material.NULL_VOID_DARK)

            // Inner sinister glowing crimson core
            builder.addOctahedron(0f, 1.4f, 0f, 0.85f, Material.NULL_CORE_CRIMSON)
            builder.addBox(0f, 1.4f, 0f, 0.95f, 0.35f, 0.95f, Material.NULL_CORE_CRIMSON)

            // Jagged crown horns / spires
            builder.addBox(-0.75f, 2.7f, -0.2f, 0.28f, 1.1f, 0.28f, Material.NULL_VOID_DARK)
            builder.addBox(0.75f, 2.7f, -0.2f, 0.28f, 1.1f, 0.28f, Material.NULL_VOID_DARK)
            builder.addBox(0f, 2.9f, 0f, 0.35f, 1.3f, 0.35f, Material.NULL_TENDRIL_VIOLET)

            // Reaching shadow tendrils / claws pointing toward runner (-Z)
            builder.addBox(-1.35f, 1.1f, -0.85f, 0.32f, 0.32f, 1.6f, Material.NULL_TENDRIL_VIOLET)
            builder.addBox(1.35f, 1.1f, -0.85f, 0.32f, 0.32f, 1.6f, Material.NULL_TENDRIL_VIOLET)
            builder.addBox(-1.45f, 0.6f, -1.35f, 0.22f, 0.22f, 0.9f, Material.NULL_CORE_CRIMSON)
            builder.addBox(1.45f, 0.6f, -1.35f, 0.22f, 0.22f, 0.9f, Material.NULL_CORE_CRIMSON)

            // Lower trailing smoke tentacles (+Z)
            builder.addBox(-0.6f, 0.3f, 0.75f, 0.45f, 0.9f, 0.45f, Material.NULL_VOID_DARK)
            builder.addBox(0.6f, 0.3f, 0.75f, 0.45f, 0.9f, 0.45f, Material.NULL_VOID_DARK)
            builder.addBox(0f, 0.2f, 1.0f, 0.5f, 0.8f, 0.5f, Material.NULL_TENDRIL_VIOLET)

            return builder.build()
        }

        /**
         * Drone Swarm Searchlight Cone: Projected volumetric alert cone sweeping track lanes
         */
        fun createDroneSearchlightConeMesh(): Mesh {
            val builder = MeshBuilder()
            // Top emitter mount
            builder.addBox(0f, 3.2f, 0f, 0.4f, 0.2f, 0.4f, Material.DRONE_SCANNER_RED)
            // Projected beam cone hitting ground
            builder.addBox(0f, 1.6f, 0f, 1.6f, 3.0f, 1.6f, Material.DRONE_SEARCHLIGHT_RED)
            // Ground footprint disc
            builder.addBox(0f, 0.05f, 0f, 2.2f, 0.06f, 2.2f, Material.DRONE_SCANNER_RED)
            return builder.build()
        }

        // ==========================================
        // BIOME 1: URBAN CITY STREETS & HIGHWAY MESHES
        // ==========================================

        fun createSubwayRoadMesh(width: Float, length: Float): Mesh = createCityStreetRoadMesh(width, length)

        fun createSubwaySceneryMesh(): Mesh = createCityBuildingsSceneryMesh()

        // ==========================================
        // BIOME 2: RUSTFALL DESERT CANYON MESHES
        // ==========================================

        fun createDesertCanyonRoadMesh(width: Float, length: Float): Mesh {
            val builder = MeshBuilder()
            // Arid sandstone roadbed with natural strata
            builder.addBox(0f, -0.1f, -length * 0.5f, width, 0.2f, length, Material.CANYON_SANDSTONE_DARK)

            // Timber trestle reinforcement ties
            var curZ = 0.0f
            while (curZ > -length) {
                builder.addBox(0f, 0.01f, curZ, width * 0.92f, 0.035f, 0.45f, Material.CANYON_TRESTLE_WOOD)
                curZ -= 2.6f
            }

            // Warm sunset amber lane divider markers
            val dashLength = 3.8f
            val gapLength = 3.2f
            curZ = 0.0f
            while (curZ > -length) {
                val zCenter = curZ - dashLength * 0.5f
                builder.addBox(-1.1f, 0.02f, zCenter, 0.14f, 0.04f, dashLength, Material.CANYON_SUNSET_AMBER)
                builder.addBox(1.1f, 0.02f, zCenter, 0.14f, 0.04f, dashLength, Material.CANYON_SUNSET_AMBER)
                curZ -= (dashLength + gapLength)
            }

            // Natural sandstone rock curbs with heavy timber edge barriers
            val curbWidth = 0.5f
            val curbHeight = 0.38f
            builder.addBox(-width * 0.5f - curbWidth * 0.5f, curbHeight * 0.5f, -length * 0.5f, curbWidth, curbHeight, length, Material.CANYON_SANDSTONE_LIGHT)
            builder.addBox(-width * 0.5f - curbWidth * 0.5f, curbHeight + 0.15f, -length * 0.5f, 0.2f, 0.25f, length, Material.CANYON_TRESTLE_WOOD)

            builder.addBox(width * 0.5f + curbWidth * 0.5f, curbHeight * 0.5f, -length * 0.5f, curbWidth, curbHeight, length, Material.CANYON_SANDSTONE_LIGHT)
            builder.addBox(width * 0.5f + curbWidth * 0.5f, curbHeight + 0.15f, -length * 0.5f, 0.2f, 0.25f, length, Material.CANYON_TRESTLE_WOOD)

            return builder.build()
        }

        fun createDesertCanyonSceneryMesh(): Mesh {
            val builder = MeshBuilder()

            // LEFT SIDE: Towering Sandstone Canyon Cliff Formations & Saguaro Cactus
            // Base canyon bluff
            builder.addBox(-9.5f, 8.0f, -15.0f, 6.5f, 16.0f, 30.0f, Material.CANYON_SANDSTONE_DARK)
            builder.addBox(-7.5f, 5.0f, -12.0f, 2.5f, 10.0f, 14.0f, Material.CANYON_SANDSTONE_LIGHT)
            builder.addBox(-9.0f, 16.0f, -20.0f, 5.5f, 8.0f, 12.0f, Material.CANYON_SANDSTONE_DARK)

            // Tall Desert Saguaro Cactus (left)
            builder.addBox(-5.8f, 2.2f, -7.0f, 0.42f, 4.4f, 0.42f, Material.CANYON_CACTUS_GREEN)
            // Left branch
            builder.addBox(-6.2f, 2.5f, -7.0f, 0.7f, 0.35f, 0.35f, Material.CANYON_CACTUS_GREEN)
            builder.addBox(-6.55f, 3.4f, -7.0f, 0.35f, 1.8f, 0.35f, Material.CANYON_CACTUS_GREEN)
            // Right branch
            builder.addBox(-5.4f, 2.9f, -7.0f, 0.7f, 0.35f, 0.35f, Material.CANYON_CACTUS_GREEN)
            builder.addBox(-5.05f, 3.7f, -7.0f, 0.35f, 1.6f, 0.35f, Material.CANYON_CACTUS_GREEN)

            // RIGHT SIDE: Sandstone Pinnacles, Rust Fuel Pipeline, and Desert Shrubbery
            builder.addBox(9.5f, 9.0f, -15.0f, 6.5f, 18.0f, 30.0f, Material.CANYON_SANDSTONE_DARK)
            builder.addBox(7.8f, 4.0f, -18.0f, 3.0f, 8.0f, 12.0f, Material.CANYON_SANDSTONE_LIGHT)

            // Elevated Industrial Rust Fuel Pipeline on Trestles
            builder.addBox(6.4f, 4.2f, -15.0f, 0.65f, 0.65f, 30.0f, Material.CANYON_RUST_PIPE)
            // A-frame trestle supports
            builder.addBox(6.4f, 2.0f, -6.0f, 0.3f, 4.0f, 0.3f, Material.CANYON_TRESTLE_WOOD)
            builder.addBox(6.4f, 2.0f, -22.0f, 0.3f, 4.0f, 0.3f, Material.CANYON_TRESTLE_WOOD)

            // Second Saguaro Cactus (right)
            builder.addBox(5.6f, 1.8f, -25.0f, 0.38f, 3.6f, 0.38f, Material.CANYON_CACTUS_GREEN)
            builder.addBox(5.95f, 2.2f, -25.0f, 0.6f, 0.3f, 0.3f, Material.CANYON_CACTUS_GREEN)
            builder.addBox(6.25f, 3.0f, -25.0f, 0.3f, 1.6f, 0.3f, Material.CANYON_CACTUS_GREEN)

            return builder.build()
        }

        // ==========================================
        // BIOME 3: BIOLUMINESCENT JUNGLE RUINS MESHES
        // ==========================================

        fun createRuinsViaductRoadMesh(width: Float, length: Float): Mesh {
            val builder = MeshBuilder()
            // Ancient weathered stone roadway deck
            builder.addBox(0f, -0.1f, -length * 0.5f, width, 0.2f, length, Material.RUINS_STONE_ANCIENT)

            // Moss-covered flagstone stepping bands
            var curZ = 0.0f
            while (curZ > -length) {
                builder.addBox(0f, 0.01f, curZ, width * 0.85f, 0.03f, 0.8f, Material.RUINS_MOSS_GREEN)
                curZ -= 3.2f
            }

            // Emerald bioluminescent rune vine dividers
            val dashLength = 3.5f
            val gapLength = 2.5f
            curZ = 0.0f
            while (curZ > -length) {
                val zCenter = curZ - dashLength * 0.5f
                builder.addBox(-1.1f, 0.02f, zCenter, 0.16f, 0.04f, dashLength, Material.RUINS_GLOW_FLORA)
                builder.addBox(1.1f, 0.02f, zCenter, 0.16f, 0.04f, dashLength, Material.RUINS_GLOW_FLORA)
                curZ -= (dashLength + gapLength)
            }

            // Heavy carved stone curbs entwined with moss and glowing flora pods
            val curbWidth = 0.55f
            val curbHeight = 0.42f
            builder.addBox(-width * 0.5f - curbWidth * 0.5f, curbHeight * 0.5f, -length * 0.5f, curbWidth, curbHeight, length, Material.RUINS_STONE_ANCIENT)
            builder.addBox(-width * 0.5f - curbWidth * 0.5f, curbHeight + 0.08f, -length * 0.5f, 0.25f, 0.15f, length, Material.RUINS_MOSS_GREEN)
            // Periodic glowing flora gems along curbs
            curZ = -4.0f
            while (curZ > -length) {
                builder.addOctahedron(-width * 0.5f - curbWidth * 0.5f, curbHeight + 0.22f, curZ, 0.14f, Material.RUINS_GLOW_FLORA)
                builder.addOctahedron(width * 0.5f + curbWidth * 0.5f, curbHeight + 0.22f, curZ, 0.14f, Material.RUINS_GLOW_FLORA)
                curZ -= 6.0f
            }

            builder.addBox(width * 0.5f + curbWidth * 0.5f, curbHeight * 0.5f, -length * 0.5f, curbWidth, curbHeight, length, Material.RUINS_STONE_ANCIENT)
            builder.addBox(width * 0.5f + curbWidth * 0.5f, curbHeight + 0.08f, -length * 0.5f, 0.25f, 0.15f, length, Material.RUINS_MOSS_GREEN)

            return builder.build()
        }

        fun createOvergrownRuinsSceneryMesh(): Mesh {
            val builder = MeshBuilder()

            // LEFT SIDE: Massive Ancient Stone Monolith Pillars & Giant Tree Trunk
            // Monolith pillar 1
            builder.addBox(-7.5f, 6.0f, -8.0f, 2.2f, 12.0f, 2.2f, Material.RUINS_STONE_ANCIENT)
            builder.addBox(-7.5f, 10.0f, -8.0f, 2.3f, 4.0f, 2.3f, Material.RUINS_MOSS_GREEN)
            // Monolith pillar 2 (partially collapsed)
            builder.addBox(-8.0f, 3.5f, -22.0f, 2.4f, 7.0f, 2.4f, Material.RUINS_STONE_ANCIENT)

            // Giant ancient gnarled tree trunk
            builder.addBox(-9.5f, 8.0f, -15.0f, 3.2f, 16.0f, 3.2f, Material.RUINS_TREE_BARK)
            // Overhanging tree branches
            builder.addBox(-6.8f, 12.0f, -15.0f, 4.0f, 1.2f, 1.8f, Material.RUINS_TREE_BARK)
            builder.addBox(-6.0f, 12.8f, -15.0f, 3.2f, 1.8f, 2.4f, Material.RUINS_MOSS_GREEN)

            // Floating / hanging bioluminescent spore bulbs
            builder.addOctahedron(-6.0f, 9.5f, -15.0f, 0.55f, Material.RUINS_VIOLET_SPORE)
            builder.addOctahedron(-7.2f, 7.0f, -10.0f, 0.40f, Material.RUINS_GLOW_FLORA)

            // RIGHT SIDE: Ancient Stone Archway & Glowing Flora Shrines
            // Collapsed archway
            builder.addBox(7.5f, 5.0f, -12.0f, 2.0f, 10.0f, 2.0f, Material.RUINS_STONE_ANCIENT)
            builder.addBox(7.5f, 9.5f, -12.0f, 2.2f, 2.0f, 2.2f, Material.RUINS_MOSS_GREEN)
            builder.addBox(9.5f, 7.0f, -18.0f, 3.5f, 14.0f, 3.5f, Material.RUINS_TREE_BARK)

            // Glowing flora lanterns and mystical violet spores
            builder.addOctahedron(6.2f, 2.8f, -7.0f, 0.45f, Material.RUINS_GLOW_FLORA)
            builder.addOctahedron(6.8f, 4.2f, -20.0f, 0.50f, Material.RUINS_VIOLET_SPORE)

            return builder.build()
        }

        // ==========================================
        // BIOME 4: ORBITAL SKYDECK MESHES
        // ==========================================

        fun createOrbitalSkydeckRoadMesh(width: Float, length: Float): Mesh {
            val builder = MeshBuilder()
            // Translucent deep-blue aerospace glass road deck
            builder.addBox(0f, -0.1f, -length * 0.5f, width, 0.2f, length, Material.ORBITAL_GLASS_FLOOR)

            // Photovoltaic solar array substrate visible under glass
            var curZ = 0.0f
            while (curZ > -length) {
                builder.addBox(0f, -0.06f, curZ, width * 0.88f, 0.04f, 1.2f, Material.ORBITAL_SOLAR_BLUE)
                curZ -= 2.4f
            }

            // Crisp solar-gold power strip lane markings
            val dashLength = 4.2f
            val gapLength = 2.8f
            curZ = 0.0f
            while (curZ > -length) {
                val zCenter = curZ - dashLength * 0.5f
                builder.addBox(-1.1f, 0.02f, zCenter, 0.14f, 0.04f, dashLength, Material.ORBITAL_GOLD_TRIM)
                builder.addBox(1.1f, 0.02f, zCenter, 0.14f, 0.04f, dashLength, Material.ORBITAL_GOLD_TRIM)
                curZ -= (dashLength + gapLength)
            }

            // High-luster white aerospace chassis curbs with holographic cyan guide rails
            val curbWidth = 0.5f
            val curbHeight = 0.4f
            builder.addBox(-width * 0.5f - curbWidth * 0.5f, curbHeight * 0.5f, -length * 0.5f, curbWidth, curbHeight, length, Material.ORBITAL_WHITE_CHASSIS)
            builder.addBox(-width * 0.5f - curbWidth * 0.5f, curbHeight + 0.12f, -length * 0.5f, 0.16f, 0.12f, length, Material.ORBITAL_HOLOGRAM_CYAN)

            builder.addBox(width * 0.5f + curbWidth * 0.5f, curbHeight * 0.5f, -length * 0.5f, curbWidth, curbHeight, length, Material.ORBITAL_WHITE_CHASSIS)
            builder.addBox(width * 0.5f + curbWidth * 0.5f, curbHeight + 0.12f, -length * 0.5f, 0.16f, 0.12f, length, Material.ORBITAL_HOLOGRAM_CYAN)

            return builder.build()
        }

        fun createOrbitalSkydeckSceneryMesh(): Mesh {
            val builder = MeshBuilder()

            // LEFT SIDE: Floating Solar Wing Array & Aerospace Girder Spire
            builder.addBox(-8.0f, 5.0f, -15.0f, 0.6f, 10.0f, 0.6f, Material.ORBITAL_WHITE_CHASSIS)
            // Giant photovoltaic wing
            builder.addBox(-8.0f, 7.5f, -15.0f, 4.8f, 0.15f, 16.0f, Material.ORBITAL_SOLAR_BLUE)
            builder.addBox(-8.0f, 7.5f, -15.0f, 5.0f, 0.22f, 0.3f, Material.ORBITAL_GOLD_TRIM)
            // Telemetry transceiver pod
            builder.addBox(-6.0f, 2.5f, -10.0f, 1.2f, 1.2f, 1.2f, Material.ORBITAL_WHITE_CHASSIS)
            builder.addOctahedron(-6.0f, 3.5f, -10.0f, 0.45f, Material.ORBITAL_HOLOGRAM_CYAN)

            // RIGHT SIDE: Orbital Comm Spire & Holographic Navigation Beacon
            builder.addBox(8.0f, 10.0f, -18.0f, 0.8f, 20.0f, 0.8f, Material.ORBITAL_WHITE_CHASSIS)
            builder.addBox(8.0f, 16.0f, -18.0f, 3.2f, 0.15f, 12.0f, Material.ORBITAL_SOLAR_BLUE)
            // Holographic beacon diamond projector
            builder.addBox(6.4f, 2.8f, -8.0f, 0.8f, 0.8f, 0.8f, Material.ORBITAL_WHITE_CHASSIS)
            builder.addOctahedron(6.4f, 4.0f, -8.0f, 0.6f, Material.ORBITAL_GOLD_TRIM)
            builder.addBox(6.4f, 4.0f, -8.0f, 1.6f, 0.08f, 1.6f, Material.ORBITAL_HOLOGRAM_CYAN)

            return builder.build()
        }

        // ==========================================
        // TRANSIT GATEWAY PORTAL MESH (ZONE BOUNDARY)
        // ==========================================

        /**
         * Monumental Hyperspace Transit Gateway: Spans across the track at zone thresholds
         * (900m, 1,800m, 2,800m) with massive structural pylons and shimmering energy rings.
         */
        fun createTransitGatewayMesh(): Mesh {
            val builder = MeshBuilder()
            val gateZ = -15.0f
            val pylonHeight = 8.5f

            // Left & Right massive hyper-gateway pylons
            builder.addBox(-5.4f, pylonHeight * 0.5f, gateZ, 0.9f, pylonHeight, 1.2f, Material.BUILDING_DARK)
            builder.addBox(-5.4f, pylonHeight * 0.5f, gateZ, 1.0f, pylonHeight * 0.85f, 0.4f, Material.ORBITAL_WHITE_CHASSIS)
            builder.addBox(-5.4f, pylonHeight + 0.3f, gateZ, 0.5f, 0.5f, 0.5f, Material.HURDLE_AMBER) // Warning beacon

            builder.addBox(5.4f, pylonHeight * 0.5f, gateZ, 0.9f, pylonHeight, 1.2f, Material.BUILDING_DARK)
            builder.addBox(5.4f, pylonHeight * 0.5f, gateZ, 1.0f, pylonHeight * 0.85f, 0.4f, Material.ORBITAL_WHITE_CHASSIS)
            builder.addBox(5.4f, pylonHeight + 0.3f, gateZ, 0.5f, 0.5f, 0.5f, Material.HURDLE_AMBER) // Warning beacon

            // Heavy overhead portal lintel
            builder.addBox(0f, pylonHeight, gateZ, 11.7f, 1.2f, 1.4f, Material.BUILDING_DARK)
            builder.addBox(0f, pylonHeight, gateZ, 11.2f, 0.4f, 1.5f, Material.ORBITAL_WHITE_CHASSIS)

            // Inner shimmering energy portal field rings
            // Outer ring (Cyan)
            builder.addBox(0f, 4.4f, gateZ, 9.6f, 6.2f, 0.15f, Material.LASER_CYAN)
            // Accent energy ring (Magenta)
            builder.addBox(0f, 4.4f, gateZ - 0.2f, 8.4f, 5.2f, 0.1f, Material.ROAD_CURB_MAGENTA)
            // Central opening pass-through clearance frame (dark hollow center)
            builder.addBox(0f, 3.8f, gateZ + 0.1f, 7.2f, 4.8f, 0.2f, Material.ORBITAL_GOLD_TRIM)

            return builder.build()
        }
    }
}
