package com.example.longrunner.ui

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.example.longrunner.game.core.GameEngine
import com.example.longrunner.game.graphics.Renderer3D
import com.example.longrunner.game.input.GameInputListener
import com.example.longrunner.game.input.SwipeGestureDetector

@SuppressLint("ViewConstructor")
class GameSurfaceView(
    context: Context,
    val engine: GameEngine
) : GLSurfaceView(context), GameInputListener {

    private val renderer3D = Renderer3D(engine)
    private val gestureDetector = SwipeGestureDetector(context, this)

    init {
        // Request OpenGL ES 3.0
        setEGLContextClientVersion(3)
        // Configure 24-bit depth buffer
        setEGLConfigChooser(8, 8, 8, 8, 24, 0)
        setRenderer(renderer3D)
        renderMode = RENDERMODE_CONTINUOUSLY

        gestureDetector.sensitivityMultiplier = engine.settingsManager.laneSensitivity.thresholdMultiplier
        val prevListener = engine.settingsManager.onSettingsChanged
        engine.settingsManager.onSettingsChanged = {
            prevListener?.invoke()
            gestureDetector.sensitivityMultiplier = engine.settingsManager.laneSensitivity.thresholdMultiplier
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun onSwipeLeft() {
        engine.onSwipeLeft()
    }

    override fun onSwipeRight() {
        engine.onSwipeRight()
    }

    override fun onSwipeUp() {
        engine.onSwipeUp()
    }

    override fun onSwipeDown() {
        engine.onSwipeDown()
    }

    override fun onTap() {
        engine.onTap()
    }

    override fun onDoubleTap() {
        engine.onDoubleTap()
    }

    fun release() {
        renderer3D.release()
    }
}
