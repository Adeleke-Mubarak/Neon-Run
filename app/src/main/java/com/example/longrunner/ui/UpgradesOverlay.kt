package com.example.longrunner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.longrunner.game.powerups.HoverboardData
import com.example.longrunner.game.powerups.HoverboardRarity
import com.example.longrunner.game.powerups.PowerUpType

enum class VaultTab {
    POWERUPS,
    HOVERBOARDS
}

@Composable
fun UpgradesOverlay(
    bankCredits: Int,
    getUpgradeLevel: (PowerUpType) -> Int,
    getUpgradeCost: (PowerUpType) -> Int,
    onUpgradePowerUp: (PowerUpType) -> Boolean,
    equippedHoverboard: HoverboardData = HoverboardData.VECTOR_GLIDE,
    isHoverboardUnlocked: (String) -> Boolean = { it == HoverboardData.VECTOR_GLIDE.id },
    onEquipHoverboard: (String) -> Boolean = { true },
    onUnlockHoverboard: (String) -> Boolean = { false },
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(VaultTab.POWERUPS) }

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
            // Header: Title & Bank Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CYBER VAULT",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = if (selectedTab == VaultTab.POWERUPS) "FIELD POWER-UP UPGRADES" else "HOVERBOARD HANGAR",
                        color = if (selectedTab == VaultTab.POWERUPS) NeonCyan else NeonGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Bank Credit Balance
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

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Tabs: [ ⚡ POWER-UPS ] and [ 🛹 HOVERBOARDS ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Power-Ups Tab Button
                val isPowerUpsActive = selectedTab == VaultTab.POWERUPS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isPowerUpsActive) NeonCyan.copy(alpha = 0.25f) else Color(0xFF110E2D),
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.5.dp,
                            if (isPowerUpsActive) NeonCyan else Color(0xFF2C2758),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedTab = VaultTab.POWERUPS }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ POWER-UPS",
                        color = if (isPowerUpsActive) NeonCyan else Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                // Hoverboards Tab Button
                val isBoardsActive = selectedTab == VaultTab.HOVERBOARDS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isBoardsActive) NeonGold.copy(alpha = 0.25f) else Color(0xFF110E2D),
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.5.dp,
                            if (isBoardsActive) NeonGold else Color(0xFF2C2758),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedTab = VaultTab.HOVERBOARDS }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🛹 HOVERBOARDS",
                        color = if (isBoardsActive) NeonGold else Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable Content Area
            if (selectedTab == VaultTab.POWERUPS) {
                // Power-Up Upgrades List
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (type in PowerUpType.values()) {
                        val level = getUpgradeLevel(type)
                        val cost = getUpgradeCost(type)
                        val isMaxLevel = level >= 5
                        val canAfford = !isMaxLevel && bankCredits >= cost
                        val effectiveDuration = type.baseDuration + (level - 1) * type.durationPerLevel

                        PowerUpUpgradeCard(
                            type = type,
                            level = level,
                            cost = cost,
                            effectiveDuration = effectiveDuration,
                            isMaxLevel = isMaxLevel,
                            canAfford = canAfford,
                            onUpgrade = { onUpgradePowerUp(type) }
                        )
                    }
                }
            } else {
                // Hoverboard Models Hangar (ordered strictly from lowest to highest value)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (board in HoverboardData.ALL) {
                        val isEquipped = board.id == equippedHoverboard.id
                        val isUnlocked = isHoverboardUnlocked(board.id)
                        val canAfford = bankCredits >= board.costCredits

                        HoverboardDeckCard(
                            board = board,
                            isEquipped = isEquipped,
                            isUnlocked = isUnlocked,
                            canAfford = canAfford,
                            onEquip = { onEquipHoverboard(board.id) },
                            onUnlock = { onUnlockHoverboard(board.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Close / Return Button
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "CLOSE VAULT",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun HoverboardDeckCard(
    board: HoverboardData,
    isEquipped: Boolean,
    isUnlocked: Boolean,
    canAfford: Boolean,
    onEquip: () -> Unit,
    onUnlock: () -> Unit
) {
    val accentColor = Color(board.primaryColorHex)
    val rarityColor = Color(board.rarity.colorHex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0B24), RoundedCornerShape(14.dp))
            .border(
                if (isEquipped) 2.dp else 1.5.dp,
                if (isEquipped) NeonCyan else accentColor.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            // Header: Name, Model Code, Rarity Badge & Cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🛹", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = board.name.uppercase(),
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "[${board.modelCode}]",
                                color = accentColor,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "DURATION: ${board.durationSeconds.toInt()}s  //  ${board.perkName}",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Rarity Tag
                Box(
                    modifier = Modifier
                        .background(rarityColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .border(1.dp, rarityColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = board.rarity.displayName,
                        color = rarityColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Description & Perk details
            Text(
                text = board.description,
                color = Color.LightGray,
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Perks badge row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1B1838), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "🛡️ Crash Barrier",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (board.scoreMultiplierBonus > 1.0f) {
                    val scorePct = ((board.scoreMultiplierBonus - 1.0f) * 100).toInt()
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1B1838), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "✨ +$scorePct% Score",
                            color = NeonGold,
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (board.magnetRadiusBonus > 0f) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1B1838), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "🧲 ${board.magnetRadiusBonus.toInt()}m Magnet",
                            color = NeonCyan,
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer Action Row (Equipped, Equip, or Unlock)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (board.costCredits == 0) "VALUE: FREE" else "VALUE: 🪙 ${board.costCredits} CREDITS",
                    color = if (board.costCredits == 0) Color.White else NeonGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                when {
                    isEquipped -> {
                        Box(
                            modifier = Modifier
                                .background(NeonCyan.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .border(1.5.dp, NeonCyan, RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "✅ EQUIPPED",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    isUnlocked -> {
                        Button(
                            onClick = onEquip,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = "EQUIP DECK",
                                color = Color.Black,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    else -> {
                        Button(
                            onClick = onUnlock,
                            enabled = canAfford,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canAfford) NeonGold else Color(0xFF221F45),
                                disabledContainerColor = Color(0xFF16142E)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = if (canAfford) "UNLOCK 🪙 ${board.costCredits}" else "NEED 🪙 ${board.costCredits}",
                                color = if (canAfford) Color.Black else Color.Gray,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PowerUpUpgradeCard(
    type: PowerUpType,
    level: Int,
    cost: Int,
    effectiveDuration: Float,
    isMaxLevel: Boolean,
    canAfford: Boolean,
    onUpgrade: () -> Unit
) {
    val cardColor = when (type) {
        PowerUpType.KINETIC_SHIELD -> NeonCyan
        PowerUpType.OVERDRIVE -> NeonGold
        PowerUpType.PHASE_BATTERY -> NeonViolet
        PowerUpType.SCORE_AMPLIFIER -> NeonMagenta
        PowerUpType.TIME_BRAKE -> Color(0xFF00E5FF)
        PowerUpType.MAGNET -> Color(0xFF00E5FF)
        PowerUpType.HOVERBOARD -> NeonCyan
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0B24), RoundedCornerShape(14.dp))
            .border(1.5.dp, cardColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = type.symbol, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = type.displayName.uppercase(),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "DURATION: ${String.format("%.1f", effectiveDuration)}s",
                            color = cardColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Level Badge
                Box(
                    modifier = Modifier
                        .background(
                            if (isMaxLevel) cardColor.copy(alpha = 0.3f) else Color(0xFF1B1838),
                            RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, if (isMaxLevel) cardColor else Color.DarkGray, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isMaxLevel) "MAX LVL" else "LVL $level/5",
                        color = if (isMaxLevel) cardColor else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = type.description,
                color = Color.LightGray,
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Level Progress Indicator & Upgrade Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LinearProgressIndicator(
                    progress = { level / 5.0f },
                    color = cardColor,
                    trackColor = Color(0xFF1B1838),
                    modifier = Modifier
                        .weight(0.55f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onUpgrade,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) cardColor else Color(0xFF221F45),
                        disabledContainerColor = Color(0xFF16142E)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(0.45f)
                        .height(36.dp)
                ) {
                    Text(
                        text = when {
                            isMaxLevel -> "MAX"
                            canAfford -> "UPGRADE $cost🪙"
                            else -> "NEED $cost🪙"
                        },
                        color = when {
                            isMaxLevel -> Color.Gray
                            canAfford -> Color.Black
                            else -> Color.Gray
                        },
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
