package com.example.longrunner

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.longrunner.game.core.GameEngine
import com.example.longrunner.game.core.GameStats
import com.example.longrunner.ui.GameHUD
import com.example.longrunner.ui.GameSurfaceView
import com.example.longrunner.ui.theme.LongRunnerTheme

class MainActivity : ComponentActivity() {

    private lateinit var gameEngine: GameEngine
    private var surfaceView: GameSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        gameEngine = GameEngine(this)

        setContent {
            LongRunnerTheme {
                val statsState = remember { mutableStateOf(GameStats()) }
                val isLoadingState = remember { mutableStateOf(true) }

                DisposableEffect(Unit) {
                    gameEngine.onStatsUpdated = { newStats ->
                        runOnUiThread {
                            statsState.value = newStats
                        }
                    }
                    onDispose {
                        gameEngine.onStatsUpdated = null
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // OpenGL ES 3.0 Real-time 3D Viewport
                    AndroidView(
                        factory = { ctx ->
                            GameSurfaceView(ctx, gameEngine).also {
                                surfaceView = it
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Jetpack Compose HUD and Interface Overlay
                    GameHUD(
                        stats = statsState.value,
                        onStartRun = { gameEngine.startRun() },
                        onPauseRun = { gameEngine.pauseRun() },
                        onResumeRun = { gameEngine.resumeRun() },
                        onRestartRun = { gameEngine.restartRun() },
                        onReturnToHome = { gameEngine.returnToHome() },
                        onActivateAbility = { gameEngine.activateAbility() },
                        onTogglePhaseShift = { gameEngine.togglePhaseShift() },
                        onSelectCharacter = { id -> gameEngine.selectCharacter(id) },
                        onUnlockCharacter = { id -> gameEngine.unlockCharacterWithCredits(id) },
                        isCharacterUnlocked = { id -> gameEngine.characterManager.isUnlocked(id) },
                        onUpgradePowerUp = { type -> gameEngine.upgradePowerUp(type) },
                        getUpgradeLevel = { type -> gameEngine.powerUpManager.getUpgradeLevel(type) },
                        getUpgradeCost = { type -> gameEngine.powerUpManager.getUpgradeCost(type) },
                        achievements = gameEngine.achievementManager.achievements,
                        onClaimMission = { id -> gameEngine.claimMissionReward(id) },
                        onClaimAllMissions = { gameEngine.claimAllMissions() },
                        onActivateHoverboard = { gameEngine.activateHoverboard() },
                        onEquipHoverboard = { id -> gameEngine.equipHoverboard(id) },
                        onUnlockHoverboard = { id -> gameEngine.unlockHoverboardWithCredits(id) },
                        isHoverboardUnlocked = { id -> gameEngine.hoverboardManager.isUnlocked(id) },
                        settingsManager = gameEngine.settingsManager,
                        onPlayUiClick = { gameEngine.playUiClick() }
                    )

                    // Premium Cyberpunk Animated Loading Screen
                    if (isLoadingState.value) {
                        com.example.longrunner.ui.LoadingScreen(
                            onLoadingComplete = {
                                isLoadingState.value = false
                            }
                        )
                    }
                }
            }
        }

        window.decorView.post {
            hideSystemUI()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        surfaceView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        gameEngine.pauseRun()
        surfaceView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        surfaceView?.release()
        gameEngine.release()
    }

    private fun hideSystemUI() {
        try {
            val decor = window.peekDecorView() ?: window.decorView ?: return
            val controller = WindowCompat.getInsetsController(window, decor)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } catch (e: Exception) {
            try {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
            } catch (ignored: Exception) {}
        }
    }
}