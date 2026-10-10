package com.example.longrunner.game.track

import com.example.longrunner.game.core.GameConstants
import kotlin.math.abs

enum class FractureType {
    NONE,
    BRANCH_SPLIT,
    ELEVATED_OVERPASS,
    COLLAPSING_HIGHWAY
}

enum class RouteChoice {
    LEFT_SAFE,
    CENTER_STANDARD,
    RIGHT_HIGH_RISK
}

data class FractureEvent(
    val id: Int,
    val type: FractureType,
    val startZ: Float,
    val length: Float = 60.0f,
    val warningDistance: Float = 48.0f,
    val bonusScore: Long = 1000L,
    val title: String = "",
    val description: String = ""
) {
    val endZ: Float get() = startZ - length
    var chosenRoute: RouteChoice? = null
    var isEntered: Boolean = false
    var isCompleted: Boolean = false
    var bonusAwarded: Boolean = false

    fun isWarning(playerZ: Float): Boolean {
        return !isEntered && !isCompleted && playerZ <= (startZ + warningDistance) && playerZ > startZ
    }

    fun isInside(playerZ: Float): Boolean {
        return playerZ <= startZ && playerZ >= endZ
    }
}

/**
 * Manages procedural reality destabilization zones ("Route Fractures").
 * Fractures trigger at controlled distance intervals, altering track topology,
 * lane hazards, vertical elevation, and risk-reward branching.
 */
class FractureManager(var isEnabled: Boolean = true) {

    private val scheduledFractures = ArrayList<FractureEvent>()
    private var nextFractureDistance: Float = 220.0f
    private var fractureCounter: Int = 0

    var currentFracture: FractureEvent? = null
        private set

    var isWarningActive: Boolean = false
        private set
    var isInFractureZone: Boolean = false
        private set

    var warningTitle: String = ""
        private set
    var warningSubtitle: String = ""
        private set

    var activeRouteName: String = ""
        private set

    var clearBannerMessage: String = ""
        private set
    var clearBannerTimer: Float = 0f
        private set

    // Callbacks for GameEngine integration
    var onFractureWarningTriggered: ((FractureEvent) -> Unit)? = null
    var onFractureEntered: ((FractureEvent, RouteChoice) -> Unit)? = null
    var onFractureCleared: ((FractureEvent, Long) -> Unit)? = null

    init {
        reset()
    }

    fun reset() {
        scheduledFractures.clear()
        nextFractureDistance = 220.0f
        fractureCounter = 0
        currentFracture = null
        isWarningActive = false
        isInFractureZone = false
        warningTitle = ""
        warningSubtitle = ""
        activeRouteName = ""
        clearBannerMessage = ""
        clearBannerTimer = 0f

        // Pre-schedule initial fractures
        scheduleNextFractures(upToDistance = 1200.0f)
    }

    private fun scheduleNextFractures(upToDistance: Float) {
        while (nextFractureDistance <= upToDistance) {
            fractureCounter++
            val type = when (fractureCounter % 3) {
                1 -> FractureType.BRANCH_SPLIT
                2 -> FractureType.ELEVATED_OVERPASS
                else -> FractureType.COLLAPSING_HIGHWAY
            }

            val (title, subtitle) = when (type) {
                FractureType.BRANCH_SPLIT -> Pair(
                    "ROUTE FRACTURE DETECTED",
                    "[LEFT] SAFE PROTOCOL ◀ | ▶ [RIGHT] HIGH-RISK BOUNTY"
                )
                FractureType.ELEVATED_OVERPASS -> Pair(
                    "ELEVATION FRACTURE IMMINENT",
                    "ASCENDING OVERPASS SKYWAY [+3.5M] AHEAD"
                )
                FractureType.COLLAPSING_HIGHWAY -> Pair(
                    "STRUCTURAL DESTABILIZATION",
                    "CRITICAL FAULT LINE DETECTED — HAZARD AVOIDANCE"
                )
                FractureType.NONE -> Pair("", "")
            }

            val length = if (type == FractureType.ELEVATED_OVERPASS) 90.0f else 60.0f
            val startZ = -nextFractureDistance

            scheduledFractures.add(
                FractureEvent(
                    id = fractureCounter,
                    type = type,
                    startZ = startZ,
                    length = length,
                    warningDistance = 48.0f,
                    bonusScore = if (type == FractureType.COLLAPSING_HIGHWAY) 800L else 1000L,
                    title = title,
                    description = subtitle
                )
            )

            // Next fracture spaced 280m to 340m further
            nextFractureDistance += if (type == FractureType.ELEVATED_OVERPASS) 330.0f else 280.0f
        }
    }

    fun getUpcomingFractureForDistance(distanceMeters: Float): FractureEvent? {
        if (!isEnabled) return null
        val targetZ = -distanceMeters
        return scheduledFractures.firstOrNull { it.startZ <= targetZ && it.startZ > targetZ - 120.0f }
    }

    /**
     * Updates fracture detection, warning states, and completion evaluations.
     */
    fun update(playerZ: Float, playerLane: Int, dt: Float) {
        if (!isEnabled) {
            isWarningActive = false
            isInFractureZone = false
            currentFracture = null
            warningTitle = ""
            warningSubtitle = ""
            activeRouteName = ""
            return
        }
        // Banner countdown
        if (clearBannerTimer > 0f) {
            clearBannerTimer -= dt
            if (clearBannerTimer <= 0f) {
                clearBannerTimer = 0f
                clearBannerMessage = ""
            }
        }

        // Dynamically schedule more fractures if player has advanced far
        val playerDist = abs(playerZ)
        if (playerDist + 800.0f > nextFractureDistance) {
            scheduleNextFractures(playerDist + 1200.0f)
        }

        // Locate relevant fracture (either warning or inside)
        val event = scheduledFractures.firstOrNull { !it.isCompleted && (it.isWarning(playerZ) || it.isInside(playerZ)) }

        if (event != null) {
            currentFracture = event

            // 1. Warning Phase
            if (event.isWarning(playerZ)) {
                if (!isWarningActive) {
                    isWarningActive = true
                    warningTitle = event.title
                    warningSubtitle = event.description
                    onFractureWarningTriggered?.invoke(event)
                }
                isInFractureZone = false
            }
            // 2. Inside Fracture Zone
            else if (event.isInside(playerZ)) {
                isWarningActive = false

                if (!event.isEntered) {
                    event.isEntered = true
                    isInFractureZone = true

                    // Record initial lane choice upon crossing threshold
                    val choice = when (playerLane) {
                        GameConstants.LANE_LEFT -> RouteChoice.LEFT_SAFE
                        GameConstants.LANE_RIGHT -> RouteChoice.RIGHT_HIGH_RISK
                        else -> RouteChoice.CENTER_STANDARD
                    }
                    event.chosenRoute = choice

                    activeRouteName = when (event.type) {
                        FractureType.BRANCH_SPLIT -> when (choice) {
                            RouteChoice.LEFT_SAFE -> "SAFE PASSAGE [LOW RISK]"
                            RouteChoice.RIGHT_HIGH_RISK -> "HIGH-RISK BOUNTY SECTOR"
                            RouteChoice.CENTER_STANDARD -> "STANDARD TRANSIT ROUTE"
                        }
                        FractureType.ELEVATED_OVERPASS -> "OVERPASS SKYWAY VIADUCT [+3.5M]"
                        FractureType.COLLAPSING_HIGHWAY -> "UNSTABLE RUNWAY EVASION"
                        FractureType.NONE -> ""
                    }

                    onFractureEntered?.invoke(event, choice)
                }
            }
        } else {
            // Check if player just passed beyond the end of the current fracture
            val current = currentFracture
            if (current != null && !current.isCompleted && playerZ < current.endZ) {
                current.isCompleted = true
                isInFractureZone = false
                isWarningActive = false

                // Evaluate reward bonus
                val isHighRiskOrElevated = (current.chosenRoute == RouteChoice.RIGHT_HIGH_RISK) ||
                        (current.type == FractureType.ELEVATED_OVERPASS) ||
                        (current.type == FractureType.COLLAPSING_HIGHWAY)

                val bonus = if (isHighRiskOrElevated) current.bonusScore else 400L
                current.bonusAwarded = true

                clearBannerMessage = if (isHighRiskOrElevated) {
                    "✨ FRACTURE CONQUERED! +${bonus} PTS"
                } else {
                    "✓ SAFE ROUTE CLEARED! +${bonus} PTS"
                }
                clearBannerTimer = 2.5f

                onFractureCleared?.invoke(current, bonus)
                currentFracture = null
            } else {
                isWarningActive = false
                isInFractureZone = false
            }
        }
    }
}
