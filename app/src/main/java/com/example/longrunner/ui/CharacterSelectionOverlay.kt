package com.example.longrunner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.longrunner.game.player.CharacterData
import com.example.longrunner.game.player.CharacterRarity

@Composable
fun CharacterSelectionOverlay(
    activeCharacterId: String,
    bankCredits: Int,
    isUnlocked: (String) -> Boolean,
    onSelectCharacter: (String) -> Unit,
    onUnlockWithCredits: (String) -> Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var previewCharacterId by remember { mutableStateOf(activeCharacterId) }
    val previewChar = CharacterData.findById(previewCharacterId)
    val unlocked = isUnlocked(previewChar.id)
    val isSelected = previewChar.id == activeCharacterId
    val canAfford = bankCredits >= previewChar.unlockCostCredits

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
                .border(2.dp, Color(previewChar.uiColorHex), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            // Header: Title & Bank balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NEON RUNNERS",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "SELECT YOUR OPERATIVE",
                        color = Color(previewChar.uiColorHex),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Credit balance pill
                Box(
                    modifier = Modifier
                        .background(Color(0xFF141235), RoundedCornerShape(12.dp))
                        .border(1.dp, NeonGold, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🪙 $bankCredits CREDITS",
                        color = NeonGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontal Character Picker Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(CharacterData.ALL) { char ->
                    val charUnlocked = isUnlocked(char.id)
                    val isCurrent = char.id == previewCharacterId
                    val themeColor = Color(char.uiColorHex)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(84.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isCurrent) themeColor.copy(alpha = 0.25f) else Color(0xFF100F26)
                            )
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = if (isCurrent) themeColor else Color.Gray.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { previewCharacterId = char.id }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        // Avatar Initial Badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(themeColor.copy(alpha = 0.3f))
                                .border(1.5.dp, themeColor, CircleShape)
                        ) {
                            Text(
                                text = char.name.take(1),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = char.name.uppercase(),
                            color = if (isCurrent) Color.White else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )

                        Text(
                            text = if (charUnlocked) "READY" else "LOCKED",
                            color = if (charUnlocked) NeonCyan else Color.Red,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable Character Details Viewport
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Character Title Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = previewChar.name.uppercase(),
                            color = Color(previewChar.uiColorHex),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = previewChar.role.uppercase(),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Rarity Badge
                    RarityBadge(rarity = previewChar.rarity)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Lore / Personality quote
                Text(
                    text = "\"${previewChar.description}\"",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Section
                StatRow(label = "SPEED", ratio = previewChar.speedModifier / 1.15f, value = "${(previewChar.speedModifier * 100).toInt()}%", color = Color(previewChar.uiColorHex))
                StatRow(label = "AGILITY / JUMP", ratio = previewChar.jumpModifier / 1.25f, value = "${(previewChar.jumpModifier * 100).toInt()}%", color = NeonCyan)
                StatRow(label = "LANE SWITCH", ratio = previewChar.laneSwitchModifier / 1.35f, value = "${(previewChar.laneSwitchModifier * 100).toInt()}%", color = NeonMagenta)
                StatRow(label = "PHASE CAPACITY", ratio = previewChar.phaseModifier / 1.6f, value = "${(previewChar.phaseModifier * 100).toInt()}%", color = NeonViolet)

                Spacer(modifier = Modifier.height(12.dp))

                // Active Ability Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141235), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(previewChar.uiColorHex).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "⚡ ABILITY: ${previewChar.abilityName.uppercase()}",
                                color = Color(previewChar.uiColorHex),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "CD: ${previewChar.abilityCooldown.toInt()}s",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = previewChar.abilityDescription,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Passive Trait Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141235), RoundedCornerShape(10.dp))
                        .border(1.dp, NeonGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "🛡️ PASSIVE: ${previewChar.passiveName.uppercase()}",
                            color = NeonGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = previewChar.passiveDescription,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 14.sp
                        )
                    }
                }

                if (!unlocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "MILESTONE: ${previewChar.unlockRequirementText}",
                        color = Color.Yellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262442)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(0.35f).height(46.dp)
                ) {
                    Text(
                        text = "BACK",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                val actionButtonColor = when {
                    isSelected -> Color(0xFF333344)
                    unlocked -> Color(previewChar.uiColorHex)
                    canAfford -> NeonGold
                    else -> Color(0xFF442222)
                }

                Button(
                    onClick = {
                        if (unlocked) {
                            onSelectCharacter(previewChar.id)
                        } else if (canAfford) {
                            onUnlockWithCredits(previewChar.id)
                        }
                    },
                    enabled = isSelected.not() && (unlocked || canAfford),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = actionButtonColor,
                        disabledContainerColor = actionButtonColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(0.65f).height(46.dp)
                ) {
                    Text(
                        text = when {
                            isSelected -> "ACTIVE RUNNER"
                            unlocked -> "SELECT RUNNER"
                            canAfford -> "UNLOCK (${previewChar.unlockCostCredits} CR)"
                            else -> "NEED ${previewChar.unlockCostCredits} CREDITS"
                        },
                        color = if (isSelected) Color.Gray else Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, ratio: Float, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(110.dp)
        )
        LinearProgressIndicator(
            progress = { ratio.coerceIn(0f, 1f) },
            color = color,
            trackColor = Color(0xFF1E1D3A),
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End,
            modifier = Modifier.width(36.dp)
        )
    }
}

@Composable
fun RarityBadge(rarity: CharacterRarity) {
    val (color, text) = when (rarity) {
        CharacterRarity.CORE -> Pair(NeonCyan, "CORE")
        CharacterRarity.RARE -> Pair(NeonMagenta, "RARE")
        CharacterRarity.EPIC -> Pair(NeonViolet, "EPIC")
        CharacterRarity.LEGENDARY -> Pair(Color(0xFF00FF88), "LEGENDARY")
    }

    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .border(1.dp, color, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
    }
}
