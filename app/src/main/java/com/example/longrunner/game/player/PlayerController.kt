package com.example.longrunner.game.player

import com.example.longrunner.game.core.GameConstants
import com.example.longrunner.game.graphics.AABB
import kotlin.math.abs
import kotlin.math.min

class PlayerController(var characterData: CharacterData = CharacterData.RUNNER) {

    var x: Float = 0f
        private set
    var y: Float = 0f
        private set
    var z: Float = 0f
        private set

    val distanceTraveled: Float get() = kotlin.math.abs(z)

    var currentLane: Int = GameConstants.LANE_CENTER
        private set
    var targetLane: Int = GameConstants.LANE_CENTER
        private set

    var targetX: Float = 0f
        private set

    var speed: Float = GameConstants.BASE_SPEED
        private set

    var velocityY: Float = 0f
        private set

    var isGrounded: Boolean = true
        private set
    var isJumping: Boolean = false
        private set
    var jumpAirTimer: Float = 0f
        private set
    var isSliding: Boolean = false
        private set
    var slideTimer: Float = 0f
        private set

    var stridePhase: Float = 0f
        private set

    val collider = AABB()

    // Collision grace tolerance based on character suit build
    val collisionTolerance: Float
        get() = characterData.collisionTolerance

    // Ability cooldown and activation state
    var abilityCooldownTimer: Float = 0f
        private set
    var abilityDurationTimer: Float = 0f
        private set

    val isAbilityActive: Boolean
        get() = abilityDurationTimer > 0f

    val isAbilityReady: Boolean
        get() = abilityCooldownTimer <= 0f

    // Zara's Ghost Step charge (allows safely phasing through 1 obstacle)
    var hasGhostStepCharge: Boolean = false

    // Track crates smashed for Jax's passive
    var cratesSmashed: Int = 0
        private set

    var surfaceY: Float = GameConstants.GROUND_Y
        private set

    init {
        reset()
    }

    fun setCharacter(newData: CharacterData) {
        characterData = newData
        speed = GameConstants.BASE_SPEED * characterData.speedModifier
        abilityCooldownTimer = 0f
        abilityDurationTimer = 0f
        hasGhostStepCharge = false
    }

    fun updateSurfaceElevation(targetSurfaceY: Float) {
        surfaceY = targetSurfaceY
        if (isGrounded && y < surfaceY) {
            y = surfaceY
        }
    }

    fun reset() {
        x = 0f
        surfaceY = GameConstants.GROUND_Y
        y = GameConstants.GROUND_Y
        z = 0f
        currentLane = GameConstants.LANE_CENTER
        targetLane = GameConstants.LANE_CENTER
        targetX = GameConstants.laneToX(currentLane)
        speed = GameConstants.BASE_SPEED * characterData.speedModifier
        velocityY = 0f
        isGrounded = true
        isJumping = false
        jumpAirTimer = 0f
        isSliding = false
        slideTimer = 0f
        stridePhase = 0f
        abilityCooldownTimer = 0f
        abilityDurationTimer = 0f
        hasGhostStepCharge = false
        cratesSmashed = 0
        updateCollider()
    }

    fun activateAbility(): Boolean {
        if (!isAbilityReady) return false

        abilityDurationTimer = characterData.abilityDuration
        abilityCooldownTimer = characterData.abilityCooldown

        if (characterData.id == CharacterData.ZARA.id) {
            hasGhostStepCharge = true
        }

        return true
    }

    fun incrementCratesSmashed() {
        cratesSmashed++
    }

    fun moveLeft(): Boolean {
        if (targetLane > GameConstants.LANE_LEFT) {
            targetLane--
            targetX = GameConstants.laneToX(targetLane)
            return true
        }
        return false
    }

    fun moveRight(): Boolean {
        if (targetLane < GameConstants.LANE_RIGHT) {
            targetLane++
            targetX = GameConstants.laneToX(targetLane)
            return true
        }
        return false
    }

    fun jump(): Boolean {
        if (isGrounded) {
            isSliding = false
            slideTimer = 0f
            velocityY = GameConstants.JUMP_VELOCITY * characterData.jumpModifier
            isGrounded = false
            isJumping = true
            jumpAirTimer = 0f
            updateCollider()
            return true
        }
        return false
    }

    fun slide(): Boolean {
        if (!isSliding) {
            isSliding = true
            slideTimer = GameConstants.SLIDE_DURATION * characterData.slideModifier
            if (!isGrounded) {
                // Dive roll down quickly if swiping down mid-air
                velocityY = -26.0f
            }
            updateCollider()
            return true
        }
        return false
    }

    fun update(dt: Float, verticalDt: Float = dt) {
        // Cooldown and ability timers
        if (abilityCooldownTimer > 0f) {
            abilityCooldownTimer -= dt
            if (abilityCooldownTimer < 0f) abilityCooldownTimer = 0f
        }
        if (abilityDurationTimer > 0f) {
            abilityDurationTimer -= dt
            if (abilityDurationTimer < 0f) abilityDurationTimer = 0f
        }

        // Accelerate speed gradually
        if (speed < GameConstants.MAX_SPEED) {
            speed = min(GameConstants.MAX_SPEED, speed + GameConstants.SPEED_ACCELERATION * dt)
        }

        // Longitudinal progression (-Z direction)
        z -= speed * dt

        // Lateral lane interpolation with character speed modifier
        val dx = targetX - x
        val step = GameConstants.LANE_SWITCH_SPEED * characterData.laneSwitchModifier * dt
        if (abs(dx) <= step) {
            x = targetX
            currentLane = targetLane
        } else {
            x += if (dx > 0) step else -step
        }

        // Vertical physics
        if (!isGrounded) {
            jumpAirTimer += verticalDt

            // Asymmetric gravity: standard rising, 1.35x snappier falling for crisp arcade jump feel
            val gravityMult = if (velocityY < 0f) 1.35f else 1.0f
            velocityY += GameConstants.GRAVITY * gravityMult * verticalDt

            // Airborne safety: if airborne > 0.48s, accelerate descent
            if (jumpAirTimer > 0.48f && velocityY > -22.0f) {
                velocityY = -22.0f
            }

            y += velocityY * verticalDt

            // Landing check or hard airborne timeout:
            // Under any circumstance (slow-mo, frame clamping, terrain edge),
            // a jump CANNOT remain suspended in air past 0.60 seconds.
            if (y <= surfaceY || jumpAirTimer >= 0.60f) {
                y = surfaceY
                velocityY = 0f
                isGrounded = true
                isJumping = false
                jumpAirTimer = 0f
            }
        } else {
            jumpAirTimer = 0f
            // Smoothly track changing surface elevation (ramps and overpasses)
            if (y < surfaceY) {
                y = min(surfaceY, y + 24.0f * verticalDt)
            } else if (y > surfaceY + 0.15f) {
                // Ground dropped beneath runner (e.g. exiting elevated overpass) -> initiate immediate descent
                isGrounded = false
                velocityY = -18.0f
                jumpAirTimer = 0f
            } else {
                y = surfaceY
            }
        }

        // Slide timer
        if (isSliding) {
            slideTimer -= dt
            if (slideTimer <= 0f) {
                isSliding = false
                slideTimer = 0f
            }
        }

        // Running animation stride cycle
        if (isGrounded && !isSliding) {
            stridePhase += speed * dt * 0.75f
        }

        updateCollider(dt)
    }

    private fun updateCollider(dt: Float = 0.016f) {
        val halfW = GameConstants.PLAYER_WIDTH * 0.5f
        val baseHalfD = GameConstants.PLAYER_DEPTH * 0.5f

        // Continuous Swept Collision: expand AABB in Z to enclose the entire frame traversal
        val frameStepZ = speed * dt
        val sweptHalfD = baseHalfD + (frameStepZ * 0.5f)
        val sweptCenterZ = z + (frameStepZ * 0.5f)

        if (isSliding) {
            // Compressed lower hitbox for slide
            val h = GameConstants.PLAYER_SLIDE_HEIGHT
            collider.set(x, y + h * 0.5f, sweptCenterZ, halfW, h * 0.5f, sweptHalfD * 1.1f)
        } else {
            // Standard standing/jumping hitbox
            val h = GameConstants.PLAYER_STAND_HEIGHT
            collider.set(x, y + h * 0.5f, sweptCenterZ, halfW, h * 0.5f, sweptHalfD)
        }
    }

    fun teleport(newX: Float = x, newY: Float = y, newZ: Float = z) {
        x = newX
        y = newY
        z = newZ
        updateCollider(0f)
    }
}
