# Technical Notes — 3D Engine & Android Implementation

## 1. Engine & Runtime Choice: Custom OpenGL ES 3.0 Engine in Kotlin
### Rationale:
1. **Compatibility & Reliability:** Third-party heavy C++ engines (Filament native bins, Unreal, Unity, heavy sceneview wrappers) frequently introduce NDK ABI version mismatches, CMake compilation friction, and Gradle daemon breakage on modern Android SDKs (API 34-37) and newer Java versions (Java 25).
2. **Zero External Overhead:** Standard `android.opengl.GLES30` is built directly into every modern Android device (Android 4.3+ / MinSdk 24+).
3. **Deterministic 60 FPS Performance:** With custom GLSL shaders and direct VBO/IBO buffers, every single draw call, vertex calculation, and matrix transform is fully controlled and optimized for low battery drain and minimal memory footprint.
4. **Clean Compose Integration:** Jetpack Compose flawlessly overlays on top of `GLSurfaceView` with zero alpha blending overhead when configured properly.

## 2. Mathematical Coordinate System
- **Coordinate Space:** Right-handed 3D coordinate system.
  - **X Axis:** Horizontal / lateral (-X is Left Lane, 0 is Center Lane, +X is Right Lane). Lane spacing = 2.2 units.
  - **Y Axis:** Vertical height (0 = ground, +Y is up into the sky).
  - **Z Axis:** Longitudinal depth (-Z is forward down the track, toward the horizon).
- **Camera:**
  - Placed at $(X_{player}, Y_{player} + 3.8, Z_{player} + 7.5)$ looking forward toward $(X_{player}, Y_{player} + 1.2, Z_{player} - 12.0)$.
  - Dynamic interpolation smoothing (Lerp factor = 0.15f) for cinematic fluidity.

## 3. Shader Design
- **Vertex Shader:** Transforms vertices by Model-View-Projection (MVP) matrix, computes World Position and Normal, passes interpolated color, UV, and depth coordinates to fragment stage.
- **Fragment Shader:**
  - Ambient illumination $(0.3, 0.35, 0.45)$.
  - Directional sunlight with diffuse Lambertian reflection.
  - Distance Fog: Exponential squared decay $\text{fogFactor} = e^{-(\text{dist} \cdot \text{density})^2}$ with dark cyber-atmospheric color $(0.05, 0.04, 0.09)$ to seamlessly conceal segment spawning at the horizon.
  - Emissive glow boost for neon edges, collectibles, and character lights.

## 4. Object Pooling & Memory Budget
- Active Track Segments: 8-10 segments pool.
- Active Obstacles: 15-20 obstacle instances pool.
- Active Collectibles: 30-40 shard instances pool.
- Memory allocation inside the render/update loop is strictly 0 bytes.
