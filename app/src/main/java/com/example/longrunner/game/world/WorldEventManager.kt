package com.example.longrunner.game.world

import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

enum class WorldEventType(
    val displayName: String,
    val subtitle: String,
    val icon: String,
    val defaultDuration: Float
) {
    NONE("Normal Operations", "", "", 0f),
    DRONE_SWARM("DRONE SWARM", "HOSTILE AIRSPACE // SCANNER SWEEP ACTIVE", "🚨", 14.0f),
    REALITY_FRACTURE("REALITY FRACTURE", "QUANTUM GLITCH // HIGH RISK EXTRACTION", "🌀", 13.0f)
}

/**
 * Manages procedural dynamic world events altering lighting, audio, hazards, and mechanics.
 */
class WorldEventManager(private val random: Random = Random.Default) {

    var currentEvent: WorldEventType = WorldEventType.NONE
        private set

    var eventTimer: Float = 0f
        private set

    var totalDuration: Float = 0f
        private set

    var isWarningActive: Boolean = false
        private set

    var warningTimer: Float = 0f
        private set

    var pendingEvent: WorldEventType = WorldEventType.NONE
        private set

    // Next trigger distance threshold
    var nextEventDistance: Float = 350.0f
        private set

    // Visual shader parameters
    var glitchFactor: Float = 0f
        private set

    // Drone Swarm searchlight sweep coordinate (-X to +X across 3 lanes)
    var searchlightX: Float = 0f
        private set

    val isEventActive: Boolean
        get() = currentEvent != WorldEventType.NONE

    val progress: Float
        get() = if (totalDuration > 0f) (eventTimer / totalDuration).coerceIn(0f, 1f) else 0f

    val eventProgress: Float
        get() = progress

    val warningTitle: String
        get() = if (isWarningActive) pendingEvent.displayName else currentEvent.displayName

    val warningSubtitle: String
        get() = if (isWarningActive) pendingEvent.subtitle else currentEvent.subtitle


    // Callbacks
    var onEventWarning: ((WorldEventType) -> Unit)? = null
    var onEventStarted: ((WorldEventType) -> Unit)? = null
    var onEventEnded: ((WorldEventType) -> Unit)? = null

    fun reset() {
        currentEvent = WorldEventType.NONE
        pendingEvent = WorldEventType.NONE
        eventTimer = 0f
        totalDuration = 0f
        isWarningActive = false
        warningTimer = 0f
        nextEventDistance = 350.0f
        glitchFactor = 0f
        searchlightX = 0f
    }

    fun triggerEvent(type: WorldEventType, duration: Float = type.defaultDuration) {
        if (type == WorldEventType.NONE) {
            endEvent()
            return
        }
        currentEvent = type
        totalDuration = duration
        eventTimer = duration
        isWarningActive = false
        warningTimer = 0f
        onEventStarted?.invoke(type)
    }

    fun endEvent() {
        val ended = currentEvent
        currentEvent = WorldEventType.NONE
        eventTimer = 0f
        totalDuration = 0f
        isWarningActive = false
        warningTimer = 0f
        if (ended != WorldEventType.NONE) {
            onEventEnded?.invoke(ended)
        }
    }

    fun update(playerZ: Float, dt: Float) {
        val distance = abs(playerZ)

        // 1. Check distance trigger for upcoming world event warning (3.0s in advance)
        if (!isEventActive && !isWarningActive && distance >= nextEventDistance - 50.0f) {
            // Pick next random event (rotate through DRONE_SWARM, REALITY_FRACTURE)
            val events = listOf(
                WorldEventType.DRONE_SWARM,
                WorldEventType.REALITY_FRACTURE
            )
            pendingEvent = events[random.nextInt(events.size)]
            isWarningActive = true
            warningTimer = 3.2f
            onEventWarning?.invoke(pendingEvent)
        }

        // 2. Count down warning
        if (isWarningActive) {
            warningTimer -= dt
            if (warningTimer <= 0f) {
                isWarningActive = false
                triggerEvent(pendingEvent)
                // Schedule next event 400 - 550m further
                nextEventDistance = distance + 420.0f + random.nextFloat() * 100.0f
            }
        }

        // 3. Update active event duration & transitions
        if (isEventActive) {
            eventTimer -= dt
            if (eventTimer <= 0f) {
                endEvent()
            }
        }

        // 4. Smooth transitions for shader uniforms & searchlights
        val targetGlitch = if (currentEvent == WorldEventType.REALITY_FRACTURE) {
            // Pulsing glitch intensity with bursts
            (0.5f + 0.5f * sin(eventTimer * 12.0f)).coerceIn(0.1f, 1.0f)
        } else 0f
        glitchFactor += (targetGlitch - glitchFactor) * (dt * 8.0f).coerceAtMost(1.0f)

        if (currentEvent == WorldEventType.DRONE_SWARM) {
            // Searchlight oscillates smoothly between -3.2m (left lane) and +3.2m (right lane)
            searchlightX = sin(eventTimer * 2.8f) * 3.2f
        } else {
            searchlightX = 0f
        }
    }
}
