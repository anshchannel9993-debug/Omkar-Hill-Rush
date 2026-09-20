package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.GameViewModel
import com.example.ScreenState
import com.example.model.UpgradeType
import com.example.model.Vehicle
import com.example.model.VehicleUpgrades
import com.example.ui.components.CoinBadge
import com.example.ui.components.GlassGamingButton
import com.example.ui.components.GlowCard
import com.example.ui.components.StatProgressBar
import com.example.ui.components.VehicleRenderer
import com.example.ui.theme.GameBorder
import com.example.ui.theme.GameCoinGold
import com.example.ui.theme.GameDarkBackground
import com.example.ui.theme.GameFlameRed
import com.example.ui.theme.GameFuelGreen
import com.example.ui.theme.GameGlassBorder
import com.example.ui.theme.GameGlassOverlay
import com.example.ui.theme.GameNeonCyan
import com.example.ui.theme.GameNeonOrange
import com.example.ui.theme.GameSurfaceElevated
import com.example.ui.theme.GameTextMuted
import com.example.ui.theme.GameTextPrimary
import com.example.ui.theme.GameTextSecondary

@Composable
fun GarageScreen(
    viewModel: GameViewModel
) {
    val coins by viewModel.coins.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()
    val upgrades by viewModel.currentUpgrades.collectAsState()

    var vehicleAngle by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameDarkBackground)
            .testTag("garage_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GameSurfaceElevated)
                            .border(1.dp, GameGlassBorder, CircleShape)
                            .clickable { viewModel.navigateTo(ScreenState.MAIN_MENU) }
                            .testTag("garage_back_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GameTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "CUSTOM GARAGE",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "TUNE PERFORMANCE & UNLOCK FLEET",
                            color = GameNeonOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoinBadge(amount = coins)
                    Spacer(modifier = Modifier.width(12.dp))
                    GlassGamingButton(
                        text = "RACE",
                        icon = Icons.Default.PlayArrow,
                        onClick = { viewModel.navigateTo(ScreenState.PLAYING) },
                        isPrimary = true,
                        testTag = "garage_race_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Area: Left Vehicle Showcase & Right Tuning Deck
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left Column: Interactive Vehicle View & Selector
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Vehicle Turntable Display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(GameGlassOverlay)
                            .border(1.5.dp, GameGlassBorder, RoundedCornerShape(16.dp))
                            .pointerInput(Unit) {
                                detectDragGestures { _, dragAmount ->
                                    vehicleAngle = (vehicleAngle + dragAmount.x * 0.3f).coerceIn(-35f, 35f)
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = selectedVehicle.name.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = selectedVehicle.tagLine,
                                        color = GameTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = "DRAG TO TILT",
                                    color = GameNeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Interactive Render
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(115.dp)
                            ) {
                                val cx = size.width / 2f
                                val cy = size.height / 2f + 10f

                                // Platform turntable
                                drawOval(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0x9900E5FF), Color.Transparent),
                                        center = Offset(cx, cy + 20f),
                                        radius = 120f
                                    ),
                                    topLeft = Offset(cx - 130f, cy + 5f),
                                    size = androidx.compose.ui.geometry.Size(260f, 30f)
                                )

                                val rWheel = Offset(cx - selectedVehicle.wheelOffset, cy + selectedVehicle.chassisHeight * 0.4f)
                                val fWheel = Offset(cx + selectedVehicle.wheelOffset, cy + selectedVehicle.chassisHeight * 0.4f)

                                VehicleRenderer.drawVehicle(
                                    scope = this,
                                    vehicle = selectedVehicle,
                                    center = Offset(cx, cy),
                                    angleDegrees = vehicleAngle,
                                    rearWheelPos = rWheel,
                                    frontWheelPos = fWheel,
                                    isThrottling = false
                                )
                            }

                            // Selection / Unlock Button for currently viewed vehicle
                            if (selectedVehicle.isUnlocked) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(GameFuelGreen.copy(alpha = 0.2f))
                                        .border(1.dp, GameFuelGreen, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "CURRENTLY SELECTED",
                                        color = GameFuelGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            } else {
                                GlassGamingButton(
                                    text = "UNLOCK FOR %,d COINS".format(selectedVehicle.unlockCost),
                                    icon = Icons.Default.Lock,
                                    onClick = { viewModel.unlockVehicle(selectedVehicle) },
                                    isPrimary = true,
                                    accentColor = GameCoinGold,
                                    modifier = Modifier.fillMaxWidth(0.85f),
                                    testTag = "unlock_vehicle_button"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Vehicle Selector Tabs
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(vehicles) { veh ->
                            val isSelected = veh.id == selectedVehicle.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) GameNeonOrange.copy(alpha = 0.25f) else GameSurfaceElevated)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) GameNeonOrange else GameGlassBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.selectVehicle(veh) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("vehicle_tab_${veh.id}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!veh.isUnlocked) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = GameCoinGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = veh.name,
                                        color = if (isSelected) Color.White else GameTextSecondary,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Column: Upgrades Tuning Deck
                Column(
                    modifier = Modifier
                        .weight(1.4f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GameGlassOverlay)
                        .border(1.5.dp, GameGlassBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "PERFORMANCE TUNING",
                        color = GameTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UpgradeType.values().forEach { upgradeType ->
                            UpgradeCard(
                                type = upgradeType,
                                currentLevel = upgrades.getLevel(upgradeType),
                                playerCoins = coins,
                                onUpgrade = { viewModel.upgradePart(upgradeType) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpgradeCard(
    type: UpgradeType,
    currentLevel: Int,
    playerCoins: Int,
    onUpgrade: () -> Unit
) {
    val isMax = currentLevel >= VehicleUpgrades.MAX_LEVEL
    val cost = VehicleUpgrades.costForLevel(currentLevel)
    val canAfford = playerCoins >= cost && !isMax

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141C2D))
            .border(1.dp, GameBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = type.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = type.description,
                    color = GameTextMuted,
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                StatProgressBar(
                    label = "",
                    level = currentLevel,
                    maxLevel = VehicleUpgrades.MAX_LEVEL,
                    accentColor = GameNeonOrange
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Upgrade Action Button
            if (isMax) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(GameNeonCyan.copy(alpha = 0.2f))
                        .border(1.dp, GameNeonCyan, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "MAX LEVEL",
                        color = GameNeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (canAfford) GameNeonOrange else Color(0xFF26334A))
                        .border(
                            1.dp,
                            if (canAfford) Color.White.copy(alpha = 0.6f) else GameBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable(enabled = canAfford) { onUpgrade() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("upgrade_${type.name.lowercase()}_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "UPGRADE",
                            color = if (canAfford) Color.White else GameTextMuted,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "%,d c".format(cost),
                            color = if (canAfford) GameCoinGold else GameTextMuted,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
