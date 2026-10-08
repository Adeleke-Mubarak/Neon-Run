# NEON RUN: FRACTURE — Game Design Document

## 1. Executive Summary
**Title:** Neon Run: Fracture  
**Genre:** 3D Endless Runner / Spatial Reaction Runner  
**Target Platform:** Android (Kotlin, OpenGL ES 3.0, Jetpack UI)  
**Perspective:** Third-Person 3D Behind-the-Runner  
**Setting:** Neo-Mombasa / African Cyber-Metropolis with Reality Fractures  

---

## 2. Core Game Loop & Gameplay Pillars

### 2.1 The Gameplay Loop
1. **Initiate Run:** Player enters the track at base speed with their chosen runner.
2. **Lane Navigation:** Traverse 3 primary lanes (Left, Center, Right) using swipe gestures.
3. **Obstacle Traversal:** Jump over low barricades, slide under elevated beam barriers, and switch lanes around heavy cyber-blocks.
4. **Collect & Boost:** Gather Energy Shards, Phase Cores, and Credits while sustaining combos.
5. **Phase Shift & Fracture:** Manage Phase energy to pierce alternate reality layers and navigate split routes when the world fractures.
6. **Failure & Progression:** On collision, calculate run statistics (distance, score, shards, near-misses), award XP, update missions, and return to upgrade or retry.

### 2.2 Core Differentiators
- **Reality Fractures:** The road dynamically splits, elevates, rotates, and recombines based on controlled procedural generation.
- **Phase Shift:** A dual-frequency reality mechanic allowing runners to phase into a secondary dimension where physical barriers become ethereal and secret energy pathways manifest.
- **The Null Threat:** A reality-entropy chaser that intensifies behind the player when mistakes or spatial anomalies occur.

---

## 3. Control Scheme
- **Swipe Left:** Shift one lane left (buffered, smooth lerp).
- **Swipe Right:** Shift one lane right (buffered, smooth lerp).
- **Swipe Up:** Jump (parabolic trajectory, height modifier).
- **Swipe Down:** Slide (crouch slide under high barriers, compresses bounding box).
- **Tap:** Contextual ability activation (e.g. Kai's Quick Phase, Zara's Ghost Step).

---

## 4. Environment & World Design
### Initial World: Neon District
- Inspired by modern and futuristic African urban architecture (geometric patterns, solar-glazed high-rises, kinetic overpasses, vibrant holographic street markets).
- Dynamic lighting: Deep obsidian asphalt, bioluminescent lane markings, cyan and magenta neon signs, overhead transit rails.

### Future Worlds (Roadmap):
- Industrial Sector (heavy machinery, magnetic conveyor belts)
- Skyway City (vertiginous cloud-level highways with wind mechanics)
- Rainfall Sector (storm effects, water reflections, lightning hazards)
- Digital Ruins (glitched low-poly spatial collapses)

---

## 5. Scoring & Reward Systems
- **Distance Score:** Base score accumulated per meter traveled, scaled by active Multiplier.
- **Collectible Points:** Shards add flat score bonuses and fuel the Phase meter.
- **Near-Miss Bonus:** Dodging within tight bounding box tolerances triggers visual slow-mo accents and multiplier boosts.
- **Combos:** Consecutive successful actions (jump -> collect -> slide -> dodge) compound scoring.
