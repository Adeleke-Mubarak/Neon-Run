package com.example.longrunner.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.longrunner.game.core.GameState
import com.example.longrunner.game.core.GameStats
import com.example.longrunner.game.player.CharacterData
import com.example.longrunner.game.powerups.PowerUpType
import com.example.longrunner.game.world.WorldEventType

// Cyber Theme Colors
val NeonCyan = Color(0xFF00F0FF)
val NeonMagenta = Color(0xFFFF007F)
val NeonGold = Color(0xFFFFD700)
val NeonViolet = Color(0xFFB026FF)
val DarkOverlay = Color(0xCC050210)
val PanelBackground = Color(0xEE0A091E)

@Composable
fun GameHUD(
    stats: GameStats,
    onStartRun: () -> Unit,
    onPauseRun: () -> Unit,
    onResumeRun: () -> Unit,
    onRestartRun: () -> Unit,
    onReturnToHome: () -> Unit = {},
    onActivateAbility: () -> Unit = {},
    onTogglePhaseShift: () -> Unit = {},
    onSelectCharacter: (String) -> Unit = {},
    onUnlockCharacter: (String) -> Boolean = { false },
    isCharacterUnlocked: (String) -> Boolean = { true },
    onUpgradePowerUp: (PowerUpType) -> Boolean = { false },
    getUpgradeLevel: (PowerUpType) -> Int = { 1 },
    getUpgradeCost: (PowerUpType) -> Int = { 60 },
    achievements: List<com.example.longrunner.game.progression.Achievement> = emptyList(),
    onClaimMission: (String) -> Boolean = { false },
    onClaimAllMissions: () -> Pair<Int, Int> = { Pair(0, 0) },
    onActivateHoverboard: () -> Unit = {},
    onEquipHoverboard: (String) -> Boolean = { true },
    onUnlockHoverboard: (String) -> Boolean = { false },
    isHoverboardUnlocked: (String) -> Boolean = { true },
    settingsManager: com.example.longrunner.game.settings.SettingsManager? = null,
    onPlayUiClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isCharacterRosterOpen by remember { mutableStateOf(false) }
    var isUpgradesOpen by remember { mutableStateOf(false) }
    var isMissionsOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // The Null Void Vignette (pulsing border shadow when pursued)
        if (stats.state == GameState.RUNNING && stats.nullTension > 0.05f) {
            NullVoidVignette(stats = stats)
        }

        // Top HUD Bar (Score, Shards, Credits, Combo, Active PowerUps, Pause button)
        if (stats.state == GameState.RUNNING || stats.state == GameState.PAUSED) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.TopCenter)
            ) {
                TopStatusBar(
                    stats = stats,
                    onPause = onPauseRun,
                    modifier = Modifier.fillMaxWidth()
                )

                // Combo Badge with Decay Meter
                if (stats.combo > 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                    ComboBar(stats = stats)
                }

                // Active Power-Up Badges & Countdown Gauges
                ActivePowerUpsRow(stats = stats)

                // The Null Threat Proximity Gauge
                if (stats.isNullAlert || stats.isNullCritical) {
                    Spacer(modifier = Modifier.height(4.dp))
                    NullThreatGauge(stats = stats)
                }

                // Near Miss Floating Toast Banner
                AnimatedVisibility(
                    visible = stats.isNearMissActive,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                ) {
                    NearMissBanner()
                }

                // Mission Accomplished Toast
                AnimatedVisibility(
                    visible = stats.missionBannerMessage.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF00C853), Color(0xFF64DD17))),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "✨ " + stats.missionBannerMessage,
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Achievement / Promotion Toast
                AnimatedVisibility(
                    visible = stats.achievementBannerMessage.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFFFF6D00), Color(0xFFFFAB00))),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "🏆 " + stats.achievementBannerMessage,
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Dynamic Multi-Biome Zone Announcement Toast
                AnimatedVisibility(
                    visible = stats.biomeBannerMessage.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                ) {
                    val accentColor = Color(stats.biomeAccentColorHex)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xEE121824),
                                        Color(0xF0080B12)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(2.dp, accentColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "🌍 " + stats.biomeBannerMessage,
                            color = accentColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        if (stats.biomeBannerSubtitle.isNotEmpty()) {
                            Text(
                                text = stats.biomeBannerSubtitle,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                // Phase Reality Floating Watermark Banner
                AnimatedVisibility(
                    visible = stats.isPhaseShiftActive,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(NeonViolet.copy(alpha = 0.45f), NeonMagenta.copy(alpha = 0.45f))),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, NeonViolet, RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🌀 PHASE REALITY // ETHEREAL PASS",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Route Fracture Imminent Warning
                AnimatedVisibility(
                    visible = stats.isFractureWarning,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xDDE65100), Color(0xDDFF8F00))),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.5.dp, NeonGold, RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️ " + stats.fractureWarningTitle,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        if (stats.fractureWarningSubtitle.isNotEmpty()) {
                            Text(
                                text = stats.fractureWarningSubtitle,
                                color = NeonGold,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Active Fracture Route Indicator
                AnimatedVisibility(
                    visible = stats.isInFractureZone && stats.activeRouteName.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xCC004D40), Color(0xCC00B4D8))),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, NeonCyan, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⚡ " + stats.activeRouteName,
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Fracture Cleared Triumphant Banner
                AnimatedVisibility(
                    visible = stats.fractureClearMessage.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xDD1B5E20), Color(0xDD00C853))),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.5.dp, Color(0xFF69F0AE), RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = stats.fractureClearMessage,
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // World Event Imminent Warning Banner
                AnimatedVisibility(
                    visible = stats.isWorldEventWarning,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp)
                ) {
                    WorldEventWarningBanner(stats = stats)
                }

                // Active World Event Status Badge
                AnimatedVisibility(
                    visible = stats.worldEventType != WorldEventType.NONE && !stats.isWorldEventWarning,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp)
                ) {
                    ActiveWorldEventBadge(stats = stats)
                }
            }

            // Phase Shift Action Button (Bottom-Left)
            if (stats.state == GameState.RUNNING) {
                PhaseShiftButton(
                    stats = stats,
                    onClick = onTogglePhaseShift,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 36.dp, start = 18.dp)
                )
            }

            // Hoverboard Deployment Action Button (Bottom-Right, above Ability Button)
            if (stats.state == GameState.RUNNING) {
                HoverboardButton(
                    stats = stats,
                    onClick = {
                        onPlayUiClick()
                        onActivateHoverboard()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 104.dp, end = 18.dp)
                )
            }

            // Ability Action Button (Bottom-Right)
            if (stats.state == GameState.RUNNING) {
                AbilityButton(
                    stats = stats,
                    onClick = onActivateAbility,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 36.dp, end = 18.dp)
                )
            }
        }

        // START SCREEN (Tap to Run)
        AnimatedVisibility(
            visible = stats.state == GameState.READY && !isCharacterRosterOpen && !isUpgradesOpen && !isMissionsOpen && !isSettingsOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            StartOverlay(
                stats = stats,
                onStart = {
                    onPlayUiClick()
                    onStartRun()
                },
                onOpenRoster = {
                    onPlayUiClick()
                    isCharacterRosterOpen = true
                },
                onOpenUpgrades = {
                    onPlayUiClick()
                    isUpgradesOpen = true
                },
                onOpenMissions = {
                    onPlayUiClick()
                    isMissionsOpen = true
                },
                onOpenSettings = {
                    onPlayUiClick()
                    isSettingsOpen = true
                },
                onPlayClick = onPlayUiClick
            )
        }

        // PAUSE OVERLAY
        AnimatedVisibility(
            visible = stats.state == GameState.PAUSED && !isCharacterRosterOpen && !isUpgradesOpen && !isMissionsOpen && !isSettingsOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            PauseOverlay(
                onResume = onResumeRun,
                onRestart = onRestartRun,
                onReturnToHome = {
                    onPlayUiClick()
                    onReturnToHome()
                },
                onOpenRoster = {
                    onPlayUiClick()
                    isCharacterRosterOpen = true
                },
                onOpenUpgrades = {
                    onPlayUiClick()
                    isUpgradesOpen = true
                },
                onOpenMissions = {
                    onPlayUiClick()
                    isMissionsOpen = true
                },
                onOpenSettings = {
                    onPlayUiClick()
                    isSettingsOpen = true
                }
            )
        }

        // GAME OVER OVERLAY
        AnimatedVisibility(
            visible = stats.state == GameState.GAME_OVER && !isCharacterRosterOpen && !isUpgradesOpen && !isMissionsOpen && !isSettingsOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            GameOverOverlay(
                stats = stats,
                onRestart = onRestartRun,
                onReturnToHome = {
                    onPlayUiClick()
                    onReturnToHome()
                },
                onOpenRoster = {
                    onPlayUiClick()
                    isCharacterRosterOpen = true
                },
                onOpenUpgrades = {
                    onPlayUiClick()
                    isUpgradesOpen = true
                },
                onOpenMissions = {
                    onPlayUiClick()
                    isMissionsOpen = true
                },
                onOpenSettings = {
                    onPlayUiClick()
                    isSettingsOpen = true
                }
            )
        }

        // CHARACTER SELECTION MODAL
        AnimatedVisibility(
            visible = isCharacterRosterOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            CharacterSelectionOverlay(
                activeCharacterId = stats.character.id,
                bankCredits = stats.bankCredits,
                isUnlocked = isCharacterUnlocked,
                onSelectCharacter = { id ->
                    onPlayUiClick()
                    onSelectCharacter(id)
                    isCharacterRosterOpen = false
                },
                onUnlockWithCredits = { id ->
                    val ok = onUnlockCharacter(id)
                    if (ok) onPlayUiClick()
                    ok
                },
                onClose = {
                    onPlayUiClick()
                    isCharacterRosterOpen = false
                }
            )
        }

        // POWER-UP UPGRADES MODAL
        AnimatedVisibility(
            visible = isUpgradesOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            UpgradesOverlay(
                bankCredits = stats.bankCredits,
                getUpgradeLevel = getUpgradeLevel,
                getUpgradeCost = getUpgradeCost,
                onUpgradePowerUp = { type ->
                    val ok = onUpgradePowerUp(type)
                    if (ok) onPlayUiClick()
                    ok
                },
                equippedHoverboard = stats.equippedHoverboard,
                isHoverboardUnlocked = isHoverboardUnlocked,
                onEquipHoverboard = { id ->
                    val ok = onEquipHoverboard(id)
                    if (ok) onPlayUiClick()
                    ok
                },
                onUnlockHoverboard = { id ->
                    val ok = onUnlockHoverboard(id)
                    if (ok) onPlayUiClick()
                    ok
                },
                onClose = {
                    onPlayUiClick()
                    isUpgradesOpen = false
                }
            )
        }

        // MISSIONS & ACHIEVEMENTS PROGRESSION MODAL
        AnimatedVisibility(
            visible = isMissionsOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            MissionsOverlay(
                stats = stats,
                achievements = achievements,
                onClaimMission = onClaimMission,
                onClaimAll = onClaimAllMissions,
                onClose = {
                    onPlayUiClick()
                    isMissionsOpen = false
                }
            )
        }

        // SETTINGS CONFIGURATION MODAL
        AnimatedVisibility(
            visible = isSettingsOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            settingsManager?.let { sm ->
                SettingsOverlay(
                    settingsManager = sm,
                    onTestSfx = onPlayUiClick,
                    onClose = {
                        onPlayUiClick()
                        isSettingsOpen = false
                    }
                )
            }
        }
    }
}

@Composable
fun TopStatusBar(
    stats: GameStats,
    onPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(DarkOverlay, shape = RoundedCornerShape(12.dp))
            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Score & Multiplier (with Boost flag)
        Column {
            Text(
                text = "SCORE",
                color = Color.LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${stats.score}",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (stats.isBoostActive) " x${stats.multiplier}⚡" else " x${stats.multiplier}",
                    color = if (stats.isBoostActive) NeonViolet else NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                )
            }
        }

        // Mid Stats: Shards & Credits
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Shards
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💎", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${stats.shards}",
                    color = NeonCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Credits
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🪙", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${stats.credits}",
                    color = NeonGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Distance & Pause Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${stats.distance.toInt()}m",
                    color = NeonGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Z${stats.currentBiomeZone} ${stats.currentBiomeName}",
                    color = Color(stats.biomeAccentColorHex),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Pause Icon Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(0xFF1B1A35), RoundedCornerShape(8.dp))
                    .border(1.dp, NeonCyan, RoundedCornerShape(8.dp))
                    .clickable { onPause() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "❚❚",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ComboBar(stats: GameStats) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(DarkOverlay, RoundedCornerShape(8.dp))
            .border(1.dp, NeonMagenta.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "x${stats.combo} COMBO",
            color = NeonMagenta,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { stats.comboProgress },
            color = NeonMagenta,
            trackColor = Color(0xFF200F2B),
            modifier = Modifier
                .width(90.dp)
                .height(6.dp)
        )
    }
}

@Composable
fun AbilityButton(
    stats: GameStats,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val charColor = Color(stats.character.uiColorHex)
    val isReady = stats.isAbilityReady
    val isActive = stats.isAbilityActive

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    isActive -> charColor.copy(alpha = 0.40f)
                    isReady -> Color(0xDD0C0A20)
                    else -> Color(0x77080614)
                }
            )
            .border(
                width = if (isActive || isReady) 2.dp else 1.dp,
                color = when {
                    isActive -> Color.White
                    isReady -> charColor
                    else -> Color.DarkGray
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = isReady) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isActive) "⚡ ACTIVE" else if (isReady) "⚡ READY" else "⏳ CHARGING",
                    color = when {
                        isActive -> Color.White
                        isReady -> charColor
                        else -> Color.LightGray
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = stats.abilityName.uppercase(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            if (!isReady && !isActive) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (1f - stats.abilityCooldownProgress).coerceIn(0f, 1f) },
                    color = charColor,
                    trackColor = Color(0xFF1B1838),
                    modifier = Modifier
                        .width(84.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Composable
fun HoverboardButton(
    stats: GameStats,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val boardColor = Color(stats.equippedHoverboard.primaryColorHex)
    val isActive = stats.isHoverboardActive

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isActive) boardColor.copy(alpha = 0.35f) else Color(0xDD0C0A20)
            )
            .border(
                width = if (isActive) 2.dp else 1.5.dp,
                color = if (isActive) Color.White else boardColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "🛹", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isActive) "SURFING" else "HOVERBOARD",
                    color = if (isActive) Color.White else boardColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = if (isActive) "${(stats.hoverboardProgress * stats.equippedHoverboard.durationSeconds).toInt()}s" else "TAP / 2x CLICK",
                    color = Color.LightGray,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PhaseShiftButton(
    stats: GameStats,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = stats.isPhaseShiftActive
    val canActivate = stats.canActivatePhaseShift

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    isActive -> NeonViolet.copy(alpha = 0.50f)
                    canActivate -> Color(0xDD110826)
                    else -> Color(0x77080614)
                }
            )
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = when {
                    isActive -> Color.White
                    canActivate -> NeonViolet
                    else -> Color.DarkGray
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = isActive || canActivate) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isActive) "🌀 PHASED" else if (canActivate) "🌀 READY" else "🔋 LOW",
                    color = when {
                        isActive -> Color.White
                        canActivate -> NeonViolet
                        else -> Color.LightGray
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "PHASE SHIFT",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { stats.phaseEnergyProgress },
                color = if (isActive) NeonMagenta else NeonViolet,
                trackColor = Color(0xFF1B1838),
                modifier = Modifier
                    .width(84.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun NearMissBanner() {
    Box(
        modifier = Modifier
            .background(
                Brush.horizontalGradient(listOf(NeonGold.copy(alpha = 0.3f), NeonMagenta.copy(alpha = 0.3f))),
                shape = RoundedCornerShape(8.dp)
            )
            .border(1.dp, NeonGold, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Text(
            text = "⚡ NEAR MISS! +250",
            color = NeonGold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun StartOverlay(
    stats: GameStats,
    onStart: () -> Unit,
    onOpenRoster: () -> Unit,
    onOpenUpgrades: () -> Unit = {},
    onOpenMissions: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onPlayClick: () -> Unit = {}
) {
    val character = stats.character
    val charColor = Color(character.uiColorHex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP STATUS / COMMAND BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Operative Rank Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF131032), RoundedCornerShape(12.dp))
                    .border(1.dp, NeonGold, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "RANK ${stats.operativeRank} [${stats.rankTitle.uppercase()}]",
                    color = NeonGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Vault & Settings Action Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF131032), RoundedCornerShape(12.dp))
                        .border(1.dp, NeonCyan, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🪙 ${stats.bankCredits}",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFF131032), RoundedCornerShape(12.dp))
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable {
                            onPlayClick()
                            onOpenSettings()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⚙️",
                        fontSize = 12.sp
                    )
                }
            }
        }

        // CENTER: BRANDING & OPERATIVE SHOWCASE
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "NEON RUN",
                color = NeonCyan,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
            Text(
                text = "FRACTURE",
                color = NeonMagenta,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Active Operative Showcase Card (Tappable to open roster)
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .background(Color(0xCC0E0D28), RoundedCornerShape(16.dp))
                    .border(1.5.dp, charColor, RoundedCornerShape(16.dp))
                    .clickable {
                        onPlayClick()
                        onOpenRoster()
                    }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .background(charColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .border(1.dp, charColor, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = character.name.take(1),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = character.name.uppercase(),
                                color = charColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${character.role.uppercase()} • ${character.rarity.name}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        text = "[CHANGE ❯]",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ABILITY: ${character.abilityName.uppercase()}",
                        color = NeonCyan,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "PERK: ${character.passiveName.uppercase()}",
                        color = NeonGold,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (stats.highScore > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "BEST RECORD: ${stats.highScore} PTS",
                    color = NeonGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // PRIMARY DEPLOY BUTTON & BOTTOM HUB DOCK
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Primary Run Button
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(
                        Brush.horizontalGradient(listOf(NeonCyan.copy(alpha = 0.35f), NeonMagenta.copy(alpha = 0.35f))),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .border(2.dp, NeonCyan, RoundedCornerShape(18.dp))
                    .clickable { onStart() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DEPLOY RUNNER",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // 4 Navigation Hub Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HubChip(
                    label = "ROSTER",
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onPlayClick()
                        onOpenRoster()
                    }
                )

                HubChip(
                    label = "VAULT",
                    color = NeonCyan,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onPlayClick()
                        onOpenUpgrades()
                    }
                )

                HubChip(
                    label = if (stats.unclaimedMissionsCount > 0) "MISSIONS [${stats.unclaimedMissionsCount}]" else "MISSIONS",
                    color = NeonGold,
                    modifier = Modifier.weight(1.15f),
                    onClick = {
                        onPlayClick()
                        onOpenMissions()
                    }
                )

                HubChip(
                    label = "SETTINGS",
                    color = NeonMagenta,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onPlayClick()
                        onOpenSettings()
                    }
                )
            }

            Text(
                text = "SWIPE: DODGE / JUMP / SLIDE • TAP: ABILITY",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HubChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(Color(0xFF131032), RoundedCornerShape(10.dp))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}

@Composable
fun PauseOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onReturnToHome: () -> Unit = {},
    onOpenRoster: () -> Unit,
    onOpenUpgrades: () -> Unit = {},
    onOpenMissions: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkOverlay),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(PanelBackground, RoundedCornerShape(16.dp))
                .border(2.dp, NeonCyan, RoundedCornerShape(16.dp))
                .padding(32.dp)
        ) {
            Text(
                text = "RUN PAUSED",
                color = NeonCyan,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                modifier = Modifier.fillMaxWidth(0.65f)
            ) {
                Text(
                    text = "RESUME",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onOpenUpgrades,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1838)),
                modifier = Modifier.fillMaxWidth(0.65f)
            ) {
                Text(
                    text = "⚡ UPGRADES",
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onOpenMissions,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2010)),
                modifier = Modifier.fillMaxWidth(0.65f)
            ) {
                Text(
                    text = "🎖️ DIRECTIVES",
                    color = NeonGold,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1535)),
                modifier = Modifier.fillMaxWidth(0.65f)
            ) {
                Text(
                    text = "⚙️ SETTINGS",
                    color = NeonMagenta,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onOpenRoster,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF221F45)),
                modifier = Modifier.fillMaxWidth(0.65f)
            ) {
                Text(
                    text = "CHANGE RUNNER",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                modifier = Modifier.fillMaxWidth(0.65f)
            ) {
                Text(
                    text = "RESTART",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onReturnToHome,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1430)),
                modifier = Modifier.fillMaxWidth(0.65f)
            ) {
                Text(
                    text = "MAIN MENU",
                    color = NeonMagenta,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun GameOverOverlay(
    stats: GameStats,
    onRestart: () -> Unit,
    onReturnToHome: () -> Unit = {},
    onOpenRoster: () -> Unit,
    onOpenUpgrades: () -> Unit = {},
    onOpenMissions: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkOverlay),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .background(PanelBackground, RoundedCornerShape(18.dp))
                .border(2.dp, NeonMagenta, RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "RUN TERMINATED",
                color = NeonMagenta,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Score Summary Breakdown Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F0E2A), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                ResultRow(label = "FINAL SCORE", value = "${stats.score}", color = Color.White)
                ResultRow(label = "DISTANCE", value = "${stats.distance.toInt()} m", color = NeonGold)
                ResultRow(label = "SHARDS COLLECTED", value = "${stats.shards}", color = NeonCyan)
                ResultRow(label = "CREDITS COLLECTED", value = "+${stats.credits}", color = NeonGold)
                ResultRow(label = "VAULT BALANCE", value = "${stats.bankCredits}", color = NeonGold)
                ResultRow(label = "NEAR MISSES", value = "${stats.nearMisses}", color = NeonMagenta)
                ResultRow(label = "BEST RECORD", value = "${stats.highScore}", color = NeonCyan)
            }

            // Operative Rank & Progression Summary Card
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131032), RoundedCornerShape(8.dp))
                    .border(1.dp, NeonGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RANK ${stats.operativeRank} [${stats.rankTitle.uppercase()}]",
                    color = NeonGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${stats.currentRankXp}/${stats.xpToNextRank} XP",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Navigation Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onOpenRoster,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF221F45)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(42.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    Text(
                        text = "ROSTER",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = onOpenUpgrades,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1838)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(42.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    Text(
                        text = "VAULT",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = onOpenMissions,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2010)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(42.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    Text(
                        text = "MISSIONS",
                        color = NeonGold,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF20163A)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(42.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    Text(
                        text = "SETTINGS",
                        color = NeonMagenta,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: BACK TO HOME & REPLAY MISSION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onReturnToHome,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22163A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "MAIN MENU",
                        color = NeonMagenta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "REPLAY MISSION",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun ResultRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ActivePowerUpsRow(stats: GameStats) {
    val hasAny = stats.hasShield || stats.isOverdriveActive || stats.isPhaseBatteryActive ||
            stats.isTimeBrakeActive || stats.isScoreAmplifierActive || stats.isMagnetActive ||
            stats.isHoverboardActive
    if (!hasAny) return

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        if (stats.hasShield) {
            PowerUpChip(
                symbol = "🛡️",
                name = "SHIELD",
                color = NeonCyan,
                progress = stats.shieldProgress,
                isIndeterminate = false
            )
        }
        if (stats.isOverdriveActive) {
            PowerUpChip(
                symbol = "⚡",
                name = "OVERDRIVE",
                color = NeonGold,
                progress = stats.overdriveProgress
            )
        }
        if (stats.isPhaseBatteryActive) {
            PowerUpChip(
                symbol = "🔋",
                name = "BATTERY",
                color = NeonViolet,
                progress = stats.phaseBatteryProgress
            )
        }
        if (stats.isTimeBrakeActive) {
            PowerUpChip(
                symbol = "⏱️",
                name = "CHRONO",
                color = Color(0xFF00E5FF),
                progress = stats.timeBrakeProgress
            )
        }
        if (stats.isScoreAmplifierActive) {
            PowerUpChip(
                symbol = "✨",
                name = "+3x SURGE",
                color = NeonMagenta,
                progress = stats.scoreAmplifierProgress
            )
        }
        if (stats.isMagnetActive) {
            PowerUpChip(
                symbol = "🧲",
                name = "MAGNET",
                color = Color(0xFF00E5FF),
                progress = stats.magnetProgress
            )
        }
        if (stats.isHoverboardActive) {
            PowerUpChip(
                symbol = "🛹",
                name = "HOVERBOARD",
                color = NeonCyan,
                progress = stats.hoverboardProgress
            )
        }
    }
}

@Composable
fun PowerUpChip(
    symbol: String,
    name: String,
    color: Color,
    progress: Float,
    isIndeterminate: Boolean = false
) {
    Box(
        modifier = Modifier
            .background(DarkOverlay, RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = symbol, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Column {
                Text(
                    text = name,
                    color = color,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                if (!isIndeterminate) {
                    LinearProgressIndicator(
                        progress = { progress },
                        color = color,
                        trackColor = color.copy(alpha = 0.2f),
                        modifier = Modifier
                            .width(34.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun NullVoidVignette(stats: GameStats) {
    val alpha = (stats.nullTension * 0.75f).coerceIn(0.1f, 0.85f)
    val borderColor = if (stats.isNullCritical) Color(0xFFFF1744) else Color(0xFFB026FF)
    val borderThickness = if (stats.isNullCritical) 6.dp else 3.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(
                width = borderThickness,
                brush = Brush.verticalGradient(
                    listOf(
                        borderColor.copy(alpha = alpha),
                        Color.Transparent,
                        borderColor.copy(alpha = alpha)
                    )
                ),
                shape = androidx.compose.ui.graphics.RectangleShape
            )
    )
}

@Composable
fun NullThreatGauge(stats: GameStats) {
    val isCrit = stats.isNullCritical
    val bgColors = if (isCrit) {
        listOf(Color(0xEED50000), Color(0xEE880E4F))
    } else {
        listOf(Color(0xDD4A148C), Color(0xDD311B92))
    }
    val borderColor = if (isCrit) Color(0xFFFF1744) else NeonViolet
    val threatText = if (isCrit) "CRITICAL PURSUIT" else "PROXIMITY ALERT"

    Column(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .background(Brush.horizontalGradient(bgColors), RoundedCornerShape(8.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "👁️ THE NULL // $threatText",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
            Text(
                text = String.format("%.1fm", stats.nullDistance),
                color = if (isCrit) Color(0xFFFF8A80) else NeonMagenta,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { stats.nullTension },
            color = if (isCrit) Color(0xFFFF1744) else NeonViolet,
            trackColor = Color.Black.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .height(3.5.dp)
                .clip(RoundedCornerShape(2.dp))
        )
    }
}

@Composable
fun WorldEventWarningBanner(stats: GameStats) {
    Column(
        modifier = Modifier
            .background(
                Brush.horizontalGradient(listOf(Color(0xFFB71C1C), Color(0xFFE65100))),
                shape = RoundedCornerShape(8.dp)
            )
            .border(1.5.dp, Color(0xFFFF5252), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🚨 " + stats.worldEventTitle,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        if (stats.worldEventSubtitle.isNotEmpty()) {
            Text(
                text = stats.worldEventSubtitle,
                color = NeonGold,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun ActiveWorldEventBadge(stats: GameStats) {
    Column(
        modifier = Modifier
            .background(
                Brush.horizontalGradient(listOf(Color(0xDD1A237E), Color(0xDD0D47A1))),
                shape = RoundedCornerShape(8.dp)
            )
            .border(1.2.dp, NeonCyan, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${stats.worldEventType.icon} ${stats.worldEventType.displayName}",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { stats.worldEventProgress },
            color = NeonCyan,
            trackColor = Color.White.copy(alpha = 0.2f),
            modifier = Modifier
                .width(110.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp))
        )
    }
}

