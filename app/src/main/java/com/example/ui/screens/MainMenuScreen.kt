package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TimeToLeave
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.GameViewModel
import com.example.ScreenState
import com.example.ui.components.CoinBadge
import com.example.ui.components.GlassGamingButton
import com.example.ui.components.VehicleRenderer
import com.example.ui.theme.GameBorder
import com.example.ui.theme.GameCoinGold
import com.example.ui.theme.GameDarkBackground
import com.example.ui.theme.GameFlameRed
import com.example.ui.theme.GameGlassBorder
import com.example.ui.theme.GameGlassOverlay
import com.example.ui.theme.GameNeonCyan
import com.example.ui.theme.GameNeonOrange
import com.example.ui.theme.GameSurfaceElevated
import com.example.ui.theme.GameTextMuted
import com.example.ui.theme.GameTextPrimary
import kotlin.math.sin

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel
) {
    val coins by viewModel.coins.collectAsState()
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val upgrades by viewModel.currentUpgrades.collectAsState()

    val overallBest = viewModel.prefs.getOverallBestDistance()
    val levelBest = viewModel.prefs.getBestDistance(selectedLevel.id)
    val playerName = viewModel.prefs.playerName

    val infiniteTransition = rememberInfiniteTransition(label = "menu_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1524),
                        Color(0xFF090D17),
                        Color(0xFF04060A)
                    )
                )
            )
            .testTag("main_menu_screen")
    ) {
        // Background Decorative Mountain Lines Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Grid lines
            for (y in 0 until h.toInt() step 50) {
                drawLine(
                    color = Color(0x0AFFFFFF),
                    start = Offset(0f, y.toFloat()),
                    end = Offset(w, y.toFloat()),
                    strokeWidth = 1f
                )
            }
            // Silhouette hills
            val hillPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, h)
                for (x in 0..w.toInt() step 20) {
                    val y = h - 70f - sin(x * 0.008f).toFloat() * 30f - sin(x * 0.002f).toFloat() * 40f
                    lineTo(x.toFloat(), y)
                }
                lineTo(w, h)
                close()
            }
            drawPath(
                path = hillPath,
                color = Color(0x18FF6D00)
            )
        }

        // Top Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Profile badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(GameSurfaceElevated)
                    .border(1.dp, GameGlassBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .clickable { viewModel.navigateTo(ScreenState.SETTINGS) }
                    .testTag("player_profile_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(GameNeonOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = playerName,
                        color = GameTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Best: ${overallBest}m",
                        color = GameNeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Title Watermark
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "OMKAR HILL RUSH",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "TRACK: ${selectedLevel.name.uppercase()}",
                    color = GameNeonOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }

            // Coins & Settings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CoinBadge(amount = coins) {
                    viewModel.navigateTo(ScreenState.GARAGE)
                }

                // Settings icon button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GameSurfaceElevated)
                        .border(1.dp, GameGlassBorder, CircleShape)
                        .clickable { viewModel.navigateTo(ScreenState.SETTINGS) }
                        .testTag("settings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = GameTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Center Showcase: Vehicle Preview & START RACE
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 68.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Quick Action Panels: Garage & Vehicles & Levels
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.width(170.dp)
            ) {
                GlassGamingButton(
                    text = "GARAGE",
                    icon = Icons.Default.Build,
                    onClick = { viewModel.navigateTo(ScreenState.GARAGE) },
                    accentColor = GameNeonOrange,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "garage_button"
                )

                GlassGamingButton(
                    text = "TRACKS",
                    icon = Icons.Default.Landscape,
                    onClick = { viewModel.navigateTo(ScreenState.LEVEL_SELECT) },
                    accentColor = GameNeonCyan,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "levels_button"
                )

                GlassGamingButton(
                    text = "MISSIONS",
                    icon = Icons.Default.EmojiEvents,
                    onClick = { viewModel.navigateTo(ScreenState.MISSIONS) },
                    accentColor = GameCoinGold,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "missions_button"
                )
            }

            // Center: Vehicle Turntable Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Vehicle Details Card
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GameGlassOverlay)
                        .border(1.5.dp, GameGlassBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = selectedVehicle.name.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = selectedVehicle.tagLine,
                            color = GameTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Vehicle Graphic Preview Canvas
                        Canvas(
                            modifier = Modifier
                                .size(width = 240.dp, height = 110.dp)
                                .clickable { viewModel.navigateTo(ScreenState.GARAGE) }
                        ) {
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f + floatY

                            // Turntable shadow
                            drawOval(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0x88FF6D00), Color.Transparent),
                                    center = Offset(centerX, centerY + 25f),
                                    radius = 80f
                                ),
                                topLeft = Offset(centerX - 90f, centerY + 15f),
                                size = androidx.compose.ui.geometry.Size(180f, 25f)
                            )

                            // Render Vehicle
                            val rWheel = Offset(centerX - selectedVehicle.wheelOffset, centerY + selectedVehicle.chassisHeight * 0.4f)
                            val fWheel = Offset(centerX + selectedVehicle.wheelOffset, centerY + selectedVehicle.chassisHeight * 0.4f)

                            VehicleRenderer.drawVehicle(
                                scope = this,
                                vehicle = selectedVehicle,
                                center = Offset(centerX, centerY),
                                angleDegrees = 0f,
                                rearWheelPos = rWheel,
                                frontWheelPos = fWheel,
                                isThrottling = false
                            )
                        }

                        // Vehicle stats quick tags
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            QuickStatTag("Engine Lv ${upgrades.engineLevel}")
                            QuickStatTag("Suspension Lv ${upgrades.suspensionLevel}")
                            QuickStatTag("Tires Lv ${upgrades.tiresLevel}")
                        }
                    }
                }
            }

            // Right: Big START RACE Button & Track record
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.width(180.dp)
            ) {
                // Large Start Race Button
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(GameNeonOrange, GameFlameRed)
                            )
                        )
                        .border(2.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(22.dp))
                        .clickable { viewModel.navigateTo(ScreenState.PLAYING) }
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .testTag("start_game_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Game",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "START RACE",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Track Best Box
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GameSurfaceElevated)
                        .border(1.dp, GameBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TRACK RECORD",
                            color = GameTextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${levelBest} METERS",
                            color = GameNeonCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1E2638))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = GameTextPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
