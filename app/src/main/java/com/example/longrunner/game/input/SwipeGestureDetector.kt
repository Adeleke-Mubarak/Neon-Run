package com.example.longrunner.game.input

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.hypot

interface GameInputListener {
    fun onSwipeLeft()
    fun onSwipeRight()
    fun onSwipeUp()
    fun onSwipeDown()
    fun onTap()
    fun onDoubleTap()
}

/**
 * High-precision gesture recognizer for swipe lane-shifts, jump/slide maneuvers,
 * and rock-solid double-tap/double-click hoverboard deployment across all screen resolutions.
 */
class SwipeGestureDetector(
    context: Context,
    private val listener: GameInputListener
) : GestureDetector.SimpleOnGestureListener() {

    private val detector = GestureDetector(context, this)

    var sensitivityMultiplier: Float = 1.0f

    // Minimum distance & velocity thresholds for deliberate swipes
    private val baseDistance = 50.0f
    private val baseVelocity = 120.0f
    private val minDistance get() = baseDistance * sensitivityMultiplier
    private val minVelocity get() = baseVelocity * sensitivityMultiplier

    // Precise touch tracking for 100% reliable double-tap / double-click
    private var lastTapTime: Long = 0L
    private var lastTapX: Float = 0f
    private var lastTapY: Float = 0f

    private var touchDownTime: Long = 0L
    private var touchDownX: Float = 0f
    private var touchDownY: Float = 0f
    private var isDragging: Boolean = false

    fun onTouchEvent(event: MotionEvent): Boolean {
        val detectorHandled = detector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = event.x
                touchDownY = event.y
                touchDownTime = System.currentTimeMillis()
                isDragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = abs(event.x - touchDownX)
                val dy = abs(event.y - touchDownY)
                if (dx > 40f || dy > 40f) {
                    isDragging = true
                }
            }
            MotionEvent.ACTION_UP -> {
                val upTime = System.currentTimeMillis()
                val elapsed = upTime - touchDownTime
                val dx = abs(event.x - touchDownX)
                val dy = abs(event.y - touchDownY)

                // If touch was a crisp tap without significant drag
                if (!isDragging && elapsed < 380L && dx < 60f && dy < 60f) {
                    val timeSinceLastTap = upTime - lastTapTime
                    val distFromLastTap = hypot((event.x - lastTapX).toDouble(), (event.y - lastTapY).toDouble()).toFloat()

                    // Tolerant double-tap window: 40ms to 450ms, radius up to 250px
                    if (timeSinceLastTap in 40L..450L && distFromLastTap < 250f) {
                        lastTapTime = 0L // Reset so 3rd tap doesn't chain
                        listener.onDoubleTap()
                    } else {
                        lastTapTime = upTime
                        lastTapX = event.x
                        lastTapY = event.y
                        listener.onTap()
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }

        return detectorHandled || true
    }

    override fun onDown(e: MotionEvent): Boolean = true

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (e1 == null) return false

        val diffX = e2.x - e1.x
        val diffY = e2.y - e1.y

        if (abs(diffX) > abs(diffY)) {
            // Horizontal swipe
            if (abs(diffX) > minDistance && abs(velocityX) > minVelocity) {
                if (diffX > 0) {
                    listener.onSwipeRight()
                } else {
                    listener.onSwipeLeft()
                }
                return true
            }
        } else {
            // Vertical swipe
            if (abs(diffY) > minDistance && abs(velocityY) > minVelocity) {
                if (diffY > 0) {
                    listener.onSwipeDown()
                } else {
                    listener.onSwipeUp()
                }
                return true
            }
        }
        return false
    }
}
