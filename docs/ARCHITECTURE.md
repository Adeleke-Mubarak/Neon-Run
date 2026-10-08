# Technical Architecture — Neon Run: Fracture

## 1. Architectural Philosophy
Neon Run: Fracture is architected for high-performance 60 FPS mobile execution on Android. It uses a decoupled, data-driven design separating rendering, game logic, physics/collision, and UI presentation.

```
+-------------------------------------------------------------+
|                  Android Activity / Jetpack UI               |
|            (HUD, Menus, Pause Dialog, Game Over Dialog)      |
+-------------------------------------------------------------+
                               | Events / State
+-------------------------------------------------------------+
|                       GameEngine                             |
|    - GameLoop (Fixed tick update + decoupled render frame)   |
|    - GameStateManager (BOOT, MENU, RUNNING, PAUSED, OVER)   |
|    - Audio & SoundManager                                    |
+-------------------------------------------------------------+
         |                       |                     |
+----------------+      +------------------+   +---------------+
| Player Systems |      |  World & Track   |   | Scoring /     |
| - Controller   |      | - TrackGenerator |   | Collectibles  |
| - Physics/Lanes|      | - Segment Pooling|   | - Shards/Items|
| - Jump & Slide |      | - Obstacles      |   | - Multipliers |
| - 3D Visual/Rig|      | - Fracture Logic |   | - Near-Miss   |
+----------------+      +------------------+   +---------------+
         \                       |                    /
          \                      |                   /
+-------------------------------------------------------------+
|                     CollisionSystem (3D AABB)               |
+-------------------------------------------------------------+
                               |
+-------------------------------------------------------------+
|                      Renderer3D (OpenGL ES 3.0)             |
|   - Shaders (GLSL ES 3.0 with lighting, glow & fog)         |
|   - Mesh & Buffer Objects (VBO / IBO)                       |
|   - Materials (Colors, Emissive, Specular)                  |
|   - CameraController (3rd Person dynamic follower)          |
|   - Scene Graph / Draw Pipeline                             |
+-------------------------------------------------------------+
```

## 2. Component Breakdown

### 2.1 Graphics Pipeline (OpenGL ES 3.0)
- **GLSurfaceView & Renderer:** Native Android hardware acceleration.
- **Custom Shaders:** High-efficiency vertex and fragment shaders providing directional sun/moonlight, ambient illumination, distance fog for seamless segment fade-in, and emissive color multiplier for neon accents.
- **Mesh Subsystem:** Procedural vertex buffers for tracks, obstacles, coins/shards, and articulated character rigs.
- **Matrix Math:** Custom 4x4 matrix and 3D vector transformations for translation, rotation, scale, and perspective projections without external runtime dependencies.

### 2.2 Track & Segment Pooling
- Track segments are instantiated into fixed-size pools (e.g. 10 segments in memory).
- As the player runs forward along the -Z axis, segments trailing behind the camera are recycled and repositioned at the front of the horizon.
- Zero garbage collection allocation during continuous running.

### 2.3 Collision & Physics
- Axis-Aligned Bounding Box (AABB) intersection testing.
- Dynamic hitbox height: Standard running (height = 1.8m), Jumping (elevated Y), Sliding (compressed height = 0.8m).
- Forgiveness margins: Lateral and vertical grace offsets prevent frustrating clipping deaths.

### 2.4 User Interface
- Native Jetpack Compose / Android UI layered over the GLSurfaceView.
- Allows crisp vector typography, animations, responsive design across all screen densities, and zero performance impact on the OpenGL render thread.
