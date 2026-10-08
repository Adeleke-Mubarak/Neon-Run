package com.example.longrunner.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    onLoadingComplete: () -> Unit
) {
    val progress = remember { Animatable(0f) }
    var currentLogIndex by remember { mutableStateOf(0) }
    val fadeAlpha = remember { Animatable(1f) }

    val logMessages = listOf(
        "INITIALIZING HARDWARE ACCELERATION [GLES 3.0]...",
        "COMPILING NEON SHADER PROGRAMS & MATRICES...",
        "SYNCHRONIZING OPERATIVE PROFILES: KAI, ZARA, JAX...",
        "CONNECTING TO 128 BPM SYNTHWAVE AUDIO MATRIX...",
        "GENERATING PROCEDURAL VIADUCTS & ROUTE FRACTURES...",
        "NEURAL LINK ONLINE. ENTERING NEON CITY..."
    )

    // Infinite breathing animations for glow
    val infiniteTransition = rememberInfiniteTransition(label = "cyberGlow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val scanlineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanlineY"
    )

    LaunchedEffect(Unit) {
        // Animate progress over ~2.4 seconds with realistic cyber loading steps
        for (i in 0 until logMessages.size) {
            currentLogIndex = i
            val target = (i + 1).toFloat() / logMessages.size
            progress.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 380, easing = LinearEasing)
            )
            delay(40)
        }
        delay(200)
        // Smooth fade out
        fadeAlpha.animateTo(0f, animationSpec = tween(400))
        onLoadingComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(fadeAlpha.value)
            .background(Color(0xFF07050C))
    ) {
        // Background Cyber-Grid Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Perspective horizontal grid lines
            for (i in 1..10) {
                val y = height * (0.45f + i * 0.055f)
                val lineAlpha = (i / 10f) * 0.25f
                drawLine(
                    color = Color(0xFF00F0FF).copy(alpha = lineAlpha),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.5f
                )
            }

            // Radial background glow at center
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00F0FF).copy(alpha = 0.18f * pulseGlow),
                        Color(0xFFFF007F).copy(alpha = 0.08f * pulseGlow),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, height * 0.45f),
                    radius = width * 0.75f
                ),
                center = Offset(width * 0.5f, height * 0.45f),
                radius = width * 0.75f
            )

            // High-tech horizontal scanline sweep
            val scanY = height * scanlineY
            drawLine(
                color = Color(0xFF00F0FF).copy(alpha = 0.15f),
                start = Offset(0f, scanY),
                end = Offset(width, scanY),
                strokeWidth = 2.0f
            )
        }

        // Central Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1.0f))

            // Cyberpunk Logo Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(92.dp)
                    .border(2.dp, Color(0xFF00F0FF).copy(alpha = pulseGlow), RoundedCornerShape(16.dp))
                    .background(Color(0xFF110D24).copy(alpha = 0.85f), RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = "⚡",
                    fontSize = 44.sp,
                    color = Color(0xFF00F0FF)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Game Title
            Text(
                text = "NEON RUN",
                style = TextStyle(
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 8.sp,
                    color = Color.White,
                    shadow = Shadow(
                        color = Color(0xFF00F0FF),
                        offset = Offset(0f, 0f),
                        blurRadius = 24f * pulseGlow
                    )
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle Tag
            Text(
                text = "CYBERNETIC HIGHWAY // NEURAL SYNC",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp,
                    color = Color(0xFFFF007F).copy(alpha = 0.9f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1.0f))

            // Progress Bar Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Percentage & status row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "LOADING SYSTEM",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF00F0FF).copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${(progress.value * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Segmented Neon Progress Bar Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color(0xFF15102A), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progress.value)
                            .height(8.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF00F0FF),
                                        Color(0xFFFF007F)
                                    )
                                ),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Live Terminal Diagnostic Log
                Text(
                    text = "> ${logMessages[currentLogIndex]}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF908AA0),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.height(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
