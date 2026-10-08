# ⚡ Neon Run

An adrenaline-fueled, high-speed 3D cyberpunk endless runner built natively for Android using **OpenGL ES 3.0** and **Jetpack Compose**. Sprint across fractured neon highways, phase through reality, dodge laser barricades, outrun the relentless void entity **The Null**, and surf cybernetic hoverboards through neon-drenched megacities.

---

## 🎮 Gameplay Features

- **High-Velocity 3D Action:** Ultra-responsive lane-switching, dynamic jumping, dive rolls, and sweeping swept-sphere continuous collision detection.
- **Dual Reality Phase Shift:** Shift dimensions in real-time to phase through solid obstacles and uncover hidden phase collectibles.
- **Cyber Vault Hoverboards:** Universal double-tap/click deployment of mag-lev hoverboards that absorb fatal obstacle impacts.
  - **Vector Glide (VG-01):** Agile Core starter deck with crash barrier.
  - **Pulse Striker (PS-09):** +15% score bonus & passive 4.5m magnet aura.
  - **Vortex Mirage (VM-X4):** +30% score boost, 16s duration & 6.0m magnet.
  - **Apex Phantom (AP-99):** +50% score boost, 18s duration & 8.5m vacuum.
  - **Hyperion Prime (HP-OMEGA):** Legendary 22s ride, +2.0x Double Score & 12m Omni-Siphon vacuum.
- **Character Roster:**
  - **Kai:** Balanced athletic cyber runner with Quick Phase.
  - **Zara:** High-speed courier with Ghost Step and 30% snappier lane transitions.
  - **Jax:** Power demolition enforcer with Impact Wave and crate-smashing armor.
  - **Nova:** Energy technician with Flux Siphon and Overcharge vacuum.
  - **Mira:** Acrobatic trickster with Time Slip temporal dilation.
- **Dynamic World Hazards:**
  - **The Null:** Atmospheric pursuing shadow entity with pulsing heartbeat and proximity alert tension.
  - **City Blackouts:** Power grid failures plunging the highway into darkness with glowing hazard lights.
  - **Drone Swarms:** Red searchlight scanner cones sweeping across lanes.
  - **Reality Fractures:** Glitched dimensional sectors offering high-risk alternative route bonuses.
- **Audio & Visual Polish:**
  - Dynamic procedural audio synthesizer (SFX without external audio bloat).
  - Volumetric neon glow, dynamic fog, speed streaks, footstep sparks, and near-miss slow-motion shockwaves.

---

## 🕹️ Controls

| Action | Touch / Gesture | Keyboard / Mouse |
|---|---|---|
| **Move Left / Right** | Swipe Left / Right | Left / Right Arrow or A / D |
| **Jump** | Swipe Up | Up Arrow or W |
| **Slide / Dive Roll** | Swipe Down | Down Arrow or S |
| **Hoverboard Deploy** | Double-Tap anywhere / Tap HUD 🛹 | Double-Click / Tap Button |
| **Phase Shift** | Tap Phase Button (Bottom-Left) | Spacebar / Phase Button |
| **Character Ability** | Tap Ability Button (Bottom-Right) | Ability Button |

---

## 🛠️ Architecture & Tech Stack

- **Graphics:** Custom procedural OpenGL ES 3.0 renderer (`Renderer3D`, `MeshBuilder`, `Shader`).
- **3D Animation:** Skinned GLTF/GLB runtime importer with Mixamo skeletal hierarchy animation support (`CharacterModelRenderer`).
- **UI:** 100% Jetpack Compose with custom cybernetic glowing glassmorphism theme (`GameHUD`, `UpgradesOverlay`, `CharacterRosterOverlay`).
- **Audio:** Custom real-time WAV synthesizer generating procedural cyber sfx via `SoundPool`.
- **Target SDK:** Android 14 (API 34), Min SDK 24 (Android 7.0 Nougat).
- **Language:** Kotlin 1.9+ with Coroutines & StateFlow.

---

## 📦 Building from Source

```bash
# Clone repository
git clone https://github.com/Adeleke-Mubarak/Neon-Run.git
cd Neon-Run

# Run unit test suite (133 tests)
./gradlew testDebugUnitTest

# Assemble Release APK
./gradlew assembleRelease
```
The compiled APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

---

## 📄 License
MIT License. Created by [Adeleke Mubarak](https://github.com/Adeleke-Mubarak).
