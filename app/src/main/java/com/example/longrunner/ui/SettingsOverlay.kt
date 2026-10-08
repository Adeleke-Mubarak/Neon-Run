package com.example.longrunner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.longrunner.game.settings.GraphicsQuality
import com.example.longrunner.game.settings.LaneSensitivity
import com.example.longrunner.game.settings.SettingsManager

@Composable
fun SettingsOverlay(
    settingsManager: SettingsManager,
    onTestSfx: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sfxVol by remember { mutableFloatStateOf(settingsManager.sfxVolume) }
    var musicVol by remember { mutableFloatStateOf(settingsManager.musicVolume) }
    var sfxEnabled by remember { mutableStateOf(settingsManager.isSfxEnabled) }
    var screenShake by remember { mutableStateOf(settingsManager.isScreenShakeEnabled) }
    var glitchFx by remember { mutableStateOf(settingsManager.isGlitchShaderEnabled) }
    var haptics by remember { mutableStateOf(settingsManager.isHapticsEnabled) }
    var graphicsQuality by remember { mutableStateOf(settingsManager.graphicsQuality) }
    var laneSensitivity by remember { mutableStateOf(settingsManager.laneSensitivity) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkOverlay)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .background(PanelBackground, RoundedCornerShape(20.dp))
                .border(2.dp, NeonCyan, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TERMINAL SETTINGS",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "AUDIO, GRAPHICS & CONTROLS",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFF1B1838), RoundedCornerShape(10.dp))
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable { onClose() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "✕ CLOSE",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable Settings Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECTION 1: AUDIO CONFIGURATION
                SettingsSection(title = "AUDIO PROTOCOLS") {
                    // Master SFX Volume
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SOUND EFFECTS (SFX)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${(sfxVol * 100).toInt()}%",
                                    color = NeonCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF221F45), RoundedCornerShape(6.dp))
                                        .border(1.dp, NeonGold, RoundedCornerShape(6.dp))
                                        .clickable {
                                            onTestSfx()
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "TEST",
                                        color = NeonGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Slider(
                            value = sfxVol,
                            onValueChange = {
                                sfxVol = it
                                settingsManager.sfxVolume = it
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = Color(0xFF25214E)
                            )
                        )
                    }

                    // Music Volume
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MUSIC / AMBIENT SYNTH",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${(musicVol * 100).toInt()}%",
                                color = NeonMagenta,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Slider(
                            value = musicVol,
                            onValueChange = {
                                musicVol = it
                                settingsManager.musicVolume = it
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = NeonMagenta,
                                activeTrackColor = NeonMagenta,
                                inactiveTrackColor = Color(0xFF25214E)
                            )
                        )
                    }

                    // Master Audio Mute Toggle
                    ToggleRow(
                        label = "ENABLE AUDIO OUTPUT",
                        subtitle = "Global toggle for sound synthesis",
                        checked = sfxEnabled,
                        onCheckedChange = {
                            sfxEnabled = it
                            settingsManager.isSfxEnabled = it
                        }
                    )
                }

                // SECTION 2: VISUALS & POST-PROCESSING
                SettingsSection(title = "VISUAL & DISPLAY MATRIX") {
                    // Graphics Profile Segmented Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "GRAPHICS QUALITY TIER",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GraphicsQuality.entries.forEach { quality ->
                                val isChosen = graphicsQuality == quality
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isChosen) NeonCyan.copy(alpha = 0.25f) else Color(0xFF131032),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.5.dp,
                                            if (isChosen) NeonCyan else Color(0xFF332F66),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            graphicsQuality = quality
                                            settingsManager.graphicsQuality = quality
                                            onTestSfx()
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = quality.displayName.uppercase(),
                                        color = if (isChosen) NeonCyan else Color.White.copy(alpha = 0.7f),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                        Text(
                            text = graphicsQuality.description,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Camera Motion / Shake Toggle
                    ToggleRow(
                        label = "DYNAMIC CAMERA SHAKE",
                        subtitle = "Impact tremors and speed sway",
                        checked = screenShake,
                        onCheckedChange = {
                            screenShake = it
                            settingsManager.isScreenShakeEnabled = it
                        }
                    )

                    // Glitch FX Toggle
                    ToggleRow(
                        label = "REALITY GLITCH SHADERS",
                        subtitle = "Chromatic quantum distortion during fractures",
                        checked = glitchFx,
                        onCheckedChange = {
                            glitchFx = it
                            settingsManager.isGlitchShaderEnabled = it
                        }
                    )
                }

                // SECTION 3: CONTROLS & RESPONSE
                SettingsSection(title = "CONTROLS & HAPTICS") {
                    // Lane Sensitivity Segmented Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "LANE SWIPE SENSITIVITY",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LaneSensitivity.entries.forEach { sens ->
                                val isChosen = laneSensitivity == sens
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isChosen) NeonGold.copy(alpha = 0.25f) else Color(0xFF131032),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.5.dp,
                                            if (isChosen) NeonGold else Color(0xFF332F66),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            laneSensitivity = sens
                                            settingsManager.laneSensitivity = sens
                                            onTestSfx()
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sens.displayName.uppercase(),
                                        color = if (isChosen) NeonGold else Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // Haptics Toggle
                    ToggleRow(
                        label = "HAPTIC FEEDBACK",
                        subtitle = "Tactile impulse on jumps and near misses",
                        checked = haptics,
                        onCheckedChange = {
                            haptics = it
                            settingsManager.isHapticsEnabled = it
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Actions: Reset to Defaults & Done
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        settingsManager.resetToDefaults()
                        sfxVol = settingsManager.sfxVolume
                        musicVol = settingsManager.musicVolume
                        sfxEnabled = settingsManager.isSfxEnabled
                        screenShake = settingsManager.isScreenShakeEnabled
                        glitchFx = settingsManager.isGlitchShaderEnabled
                        haptics = settingsManager.isHapticsEnabled
                        graphicsQuality = settingsManager.graphicsQuality
                        laneSensitivity = settingsManager.laneSensitivity
                        onTestSfx()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF221F45)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(0.45f).height(46.dp)
                ) {
                    Text(
                        text = "RESET DEFAULTS",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(0.55f).height(46.dp)
                ) {
                    Text(
                        text = "APPLY & CLOSE",
                        color = Color.Black,
                        fontSize = 13.sp,
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
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0E2A), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF2B2760), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            color = NeonCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace
        )
        content()
    }
}

@Composable
fun ToggleRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NeonCyan,
                uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                uncheckedTrackColor = Color(0xFF25214E)
            )
        )
    }
}
