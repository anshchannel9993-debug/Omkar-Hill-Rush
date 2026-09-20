package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.GameViewModel
import com.example.ScreenState
import com.example.physics.GameOverReason
import com.example.physics.StuntEvent
import com.example.ui.components.GlassGamingButton
import com.example.ui.components.PedalButton
import com.example.ui.components.VehicleRenderer
import com.example.ui.theme.GameBorder
import com.example.ui.theme.GameCoinGold
import com.example.ui.theme.GameDarkBackground
import com.example.ui.theme.GameFlameRed
import com.example.ui.theme.GameFuelGreen
import com.example.ui.theme.GameFuelRed
import com.example.ui.theme.GameGlassBorder
import com.example.ui.theme.GameGlassOverlay
import com.example.ui.theme.GameNeonCyan
import com.example.ui.theme.GameNeonOrange
import com.example.ui.theme.GameSurfaceDark
import com.example.ui.theme.GameSurfaceElevated
import com.example.ui.theme.GameTextMuted
import com.example.ui.theme.GameTextPrimary
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun GamePlayScreen(
    viewModel: GameViewModel
) {
    val isPaused by viewModel.isGamePaused.collectAsState()
    val isGameOver by viewModel.isGameOver.collectAsState()
    val gameOverReason by viewModel.gameOverReason.collectAsState()
    val runDistance by viewModel.runDistance.collectAsState()
    val runCoins by viewModel.runCoins.collectAsState()
    val isNewRecord by viewModel.isNewRecord.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()

    var isGasPressed by remember { mutableStateOf(false) }
    var isBrakePressed by remember { mutableStateOf(false) }

    // Smooth camera position
    var camX by remember { mutableFloatStateOf(100f) }
    var camY by remember { mutableFloatStateOf(360f) }

    // State for re-triggering frames
    var frameTick by remember { mutableStateOf(0L) }
    var currentSpeedKmh by remember { mutableFloatStateOf(0f) }
    var currentFuel by remember { mutableFloatStateOf(100f) }
    var activeStunt by remember { mutableStateOf<StuntEvent?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "hud_anim")
    val lowFuelFlash by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "low_fuel_flash"
    )

    // Main 60 FPS Game Loop
    LaunchedEffect(isPaused, isGameOver) {
        var lastTime = System.nanoTime()
        while (!isPaused && !isGameOver) {
            withFrameNanos { now ->
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
                lastTime = now

                val engine = viewModel.physicsEngine
                if (engine != null) {
                    // Update physics
                    engine.update(
                        deltaTime = dt,
                        isGasPressed = isGasPressed,
                        isBrakePressed = isBrakePressed,
                        onCoinCollected = { viewModel.soundSystem.playCoinSound() },
                        onFuelCollected = { viewModel.soundSystem.playFuelSound() },
                        onCrash = { reason -> viewModel.onGameFinished(reason) }
                    )

                    // Smooth camera follow with forward lookahead
                    val targetCamX = engine.posX + (engine.velX * 0.4f).coerceIn(0f, 180f)
                    val targetCamY = engine.posY
                    camX += (targetCamX - camX) * (dt * 6.5f).coerceAtMost(1f)
                    camY += (targetCamY - camY) * (dt * 5.0f).coerceAtMost(1f)

                    // Sound RPM adjustment
                    val speedRatio = (abs(engine.velX) / 250f).coerceIn(0f, 1f)
                    val throttleBoost = if (isGasPressed) 0.5f else 0f
                    viewModel.soundSystem.targetEnginePitch = (0.2f + speedRatio * 0.4f + throttleBoost).coerceIn(0.1f, 1.0f)

                    currentSpeedKmh = (abs(engine.velX) * 0.36f)
                    currentFuel = engine.fuel
                    activeStunt = engine.latestStunt

                    frameTick = now
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameDarkBackground)
            .testTag("gameplay_screen")
    ) {
        val engine = viewModel.physicsEngine

        // Canvas Game Renderer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val scale = 1.05f

            // Offset transforms:
            // Center camera around (canvasWidth * 0.32, canvasHeight * 0.62)
            val originX = canvasWidth * 0.32f
            val originY = canvasHeight * 0.62f

            fun worldToScreen(wx: Float, wy: Float): Offset {
                val sx = originX + (wx - camX) * scale
                val sy = originY - (wy - camY) * scale
                return Offset(sx, sy)
            }

            // 1. Draw Multi-layer Sky Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(selectedLevel.skyTopColor, selectedLevel.skyBottomColor),
                    startY = 0f,
                    endY = canvasHeight * 0.75f
                )
            )

            // 2. Distant Parallax Mountains
            val parallaxFactor1 = 0.15f
            val mountainPath = Path().apply {
                moveTo(0f, canvasHeight)
                val step = 30f
                for (sx in 0..canvasWidth.toInt() step step.toInt()) {
                    val wx = (sx - originX) / scale + camX * parallaxFactor1
                    val my = 380f + sin(wx * 0.0012f) * 110f + cos(wx * 0.003f) * 45f
                    val screenY = originY - (my - camY * 0.2f) * 0.6f
                    lineTo(sx.toFloat(), screenY)
                }
                lineTo(canvasWidth, canvasHeight)
                close()
            }
            drawPath(path = mountainPath, color = selectedLevel.distantMountainColor)

            // 3. Foreground Continuous Terrain
            val terrainPath = Path()
            val surfacePath = Path()
            val startScreenX = -40f
            val endScreenX = canvasWidth + 40f
            val sampleStep = 8f

            var isFirst = true
            var prevX = 0f
            var prevY = 0f

            for (sx in startScreenX.toInt()..endScreenX.toInt() step sampleStep.toInt()) {
                val wx = (sx - originX) / scale + camX
                val wy = selectedLevel.let { engine?.terrain?.getGroundHeight(wx) ?: 320f }
                val spos = worldToScreen(wx, wy)

                if (isFirst) {
                    terrainPath.moveTo(spos.x, canvasHeight + 100f)
                    terrainPath.lineTo(spos.x, spos.y)
                    surfacePath.moveTo(spos.x, spos.y)
                    isFirst = false
                } else {
                    terrainPath.lineTo(spos.x, spos.y)
                    surfacePath.lineTo(spos.x, spos.y)
                }
                prevX = spos.x
                prevY = spos.y
            }
            terrainPath.lineTo(prevX, canvasHeight + 100f)
            terrainPath.close()

            // Draw Underground Fill
            drawPath(
                path = terrainPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        selectedLevel.terrainTopColor,
                        selectedLevel.terrainBottomColor,
                        Color(0xFF0A0F1A)
                    ),
                    startY = originY - 100f,
                    endY = canvasHeight
                )
            )

            // Draw Lush Surface Top Layer (Grass / Snow / Sand / Lava rim)
            drawPath(
                path = surfacePath,
                color = selectedLevel.surfaceAccentColor,
                style = Stroke(width = 7f, cap = StrokeCap.Round)
            )

            // 4. Draw Collectibles (Coins & Fuel)
            engine?.let { eng ->
                // Visible range in world X
                val minWx = camX - 400f
                val maxWx = camX + canvasWidth + 400f

                // Coins
                eng.coins.forEach { coin ->
                    if (!coin.isCollected && coin.x in minWx..maxWx) {
                        val cPos = worldToScreen(coin.x, coin.y)
                        drawCoinItem(this, cPos, coin.value)
                    }
                }

                // Fuel Canisters
                eng.fuelCans.forEach { can ->
                    if (!can.isCollected && can.x in minWx..maxWx) {
                        val fPos = worldToScreen(can.x, can.y)
                        drawFuelItem(this, fPos)
                    }
                }

                // 5. Draw Vehicle
                val vScreenCenter = worldToScreen(eng.posX, eng.posY)
                val rWheelScreen = worldToScreen(eng.rearWheelPos.x, eng.rearWheelPos.y)
                val fWheelScreen = worldToScreen(eng.frontWheelPos.x, eng.frontWheelPos.y)

                // Invert angle for screen coordinate system (where Y grows downward)
                val angleDeg = -Math.toDegrees(eng.angle.toDouble()).toFloat()

                VehicleRenderer.drawVehicle(
                    scope = this,
                    vehicle = selectedVehicle,
                    center = vScreenCenter,
                    angleDegrees = angleDeg,
                    rearWheelPos = rWheelScreen,
                    frontWheelPos = fWheelScreen,
                    rearWheelRot = eng.rearWheelRotation,
                    frontWheelRot = eng.frontWheelRotation,
                    isThrottling = isGasPressed && eng.fuel > 0.01f,
                    exhaustFlame = (frameTick % 2 == 0L)
                )

                // Ground contact particle sparks/dust
                if (eng.rearWheelGrounded && isGasPressed) {
                    val dustPos = rWheelScreen
                    drawCircle(
                        color = selectedLevel.particleColor.copy(alpha = 0.6f),
                        radius = 6f,
                        center = Offset(dustPos.x - 14f, dustPos.y + 4f)
                    )
                }
            }
        }

        // Top HUD Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Distance & Speed
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Distance Gauge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GameGlassOverlay)
                        .border(1.dp, GameGlassBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("distance_counter")
                ) {
                    Column {
                        Text(
                            text = "DISTANCE",
                            color = GameTextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${engine?.distanceMeters ?: 0} m",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Speedometer
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GameGlassOverlay)
                        .border(1.dp, GameGlassBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column {
                        Text(
                            text = "SPEED",
                            color = GameTextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${currentSpeedKmh.toInt()} km/h",
                            color = GameNeonCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Center: Fuel Tank Gauge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(180.dp)
            ) {
                val isLowFuel = currentFuel < 25f
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = "Fuel",
                        tint = if (isLowFuel) GameFuelRed else GameFuelGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isLowFuel) "LOW FUEL!" else "FUEL TANK",
                        color = if (isLowFuel) GameFuelRed else GameTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                val fuelFraction = (currentFuel / 100f).coerceIn(0f, 1f)
                val fuelColor = when {
                    fuelFraction < 0.25f -> GameFuelRed.copy(alpha = lowFuelFlash)
                    fuelFraction < 0.5f -> GameCoinGold
                    else -> GameFuelGreen
                }

                LinearProgressIndicator(
                    progress = { fuelFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .border(1.dp, GameBorder, RoundedCornerShape(5.dp)),
                    color = fuelColor,
                    trackColor = Color(0x661E293B)
                )
            }

            // Right: Coins Collected & Pause Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Run Coins
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GameGlassOverlay)
                        .border(1.dp, GameCoinGold, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = GameCoinGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+${engine?.coinsCollected ?: 0}",
                        color = GameCoinGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }

                // Pause Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GameSurfaceElevated)
                        .border(1.5.dp, GameGlassBorder, CircleShape)
                        .clickable { viewModel.pauseGame() }
                        .testTag("pause_game_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Stunt Popup Banner (Air time / Flips)
        activeStunt?.let { stunt ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(GameFlameRed, GameNeonOrange)))
                    .border(1.5.dp, Color.White, RoundedCornerShape(20.dp))
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stunt.title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        // Bottom Pedals (Large touch targets for gas & brake)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 28.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // BRAKE / REVERSE PEDAL
            PedalButton(
                label = "BRAKE",
                subLabel = "REVERSE / NOSE DOWN",
                isGas = false,
                onPressChanged = { isBrakePressed = it },
                testTag = "brake_button"
            )

            // ACCELERATE / GAS PEDAL
            PedalButton(
                label = "GAS",
                subLabel = "ACCEL / NOSE UP",
                isGas = true,
                onPressChanged = { isGasPressed = it },
                testTag = "accelerate_button"
            )
        }

        // Pause Menu Dialog
        if (isPaused && !isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000))
                    .clickable { /* absorb taps */ }
                    .testTag("pause_dialog"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GameSurfaceDark)
                        .border(2.dp, GameGlassBorder, RoundedCornerShape(20.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "GAME PAUSED",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = "Distance: ${engine?.distanceMeters ?: 0}m  •  Coins: +${engine?.coinsCollected ?: 0}",
                            color = GameTextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        GlassGamingButton(
                            text = "RESUME",
                            icon = Icons.Default.PlayArrow,
                            onClick = { viewModel.resumeGame() },
                            isPrimary = true,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "resume_game_button"
                        )

                        GlassGamingButton(
                            text = "RESTART RUN",
                            icon = Icons.Default.Refresh,
                            onClick = { viewModel.startNewGame() },
                            accentColor = GameNeonCyan,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "restart_game_button"
                        )

                        GlassGamingButton(
                            text = "MAIN MENU",
                            icon = Icons.Default.Home,
                            onClick = { viewModel.navigateTo(ScreenState.MAIN_MENU) },
                            accentColor = GameTextMuted,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "pause_menu_button"
                        )
                    }
                }
            }
        }

        // Game Over Dialog Overlay
        if (isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xEE090D17))
                    .testTag("game_over_dialog"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(420.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(GameSurfaceElevated)
                        .border(2.dp, GameNeonOrange, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title / Cause of Game Over
                        val causeTitle = when (gameOverReason) {
                            GameOverReason.OUT_OF_FUEL -> "OUT OF FUEL!"
                            GameOverReason.DRIVER_CRASHED -> "DRIVER CRASHED!"
                            GameOverReason.VEHICLE_FLIPPED -> "VEHICLE FLIPPED!"
                            else -> "RUN FINISHED!"
                        }

                        Text(
                            text = causeTitle,
                            color = GameFlameRed,
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp,
                            letterSpacing = 2.sp
                        )

                        if (isNewRecord) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GameCoinGold.copy(alpha = 0.2f))
                                    .border(1.dp, GameCoinGold, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "★ NEW DISTANCE RECORD! ★",
                                    color = GameCoinGold,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Stats Grid Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0F1626))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "DISTANCE",
                                    color = GameTextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${runDistance}m",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "COINS EARNED",
                                    color = GameTextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "+$runCoins",
                                    color = GameCoinGold,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Action Buttons: Restart, Upgrade, Menu
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GlassGamingButton(
                                text = "RETRY",
                                icon = Icons.Default.Refresh,
                                onClick = { viewModel.startNewGame() },
                                isPrimary = true,
                                modifier = Modifier.weight(1f),
                                testTag = "game_over_restart_button"
                            )

                            GlassGamingButton(
                                text = "GARAGE",
                                icon = Icons.Default.Build,
                                onClick = { viewModel.navigateTo(ScreenState.GARAGE) },
                                accentColor = GameNeonCyan,
                                modifier = Modifier.weight(1f),
                                testTag = "game_over_garage_button"
                            )

                            GlassGamingButton(
                                text = "MENU",
                                icon = Icons.Default.Home,
                                onClick = { viewModel.navigateTo(ScreenState.MAIN_MENU) },
                                accentColor = GameTextMuted,
                                modifier = Modifier.weight(0.9f),
                                testTag = "game_over_menu_button"
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun drawCoinItem(scope: DrawScope, center: Offset, value: Int) {
    val radius = 11f
    // Outer Gold Rim
    scope.drawCircle(
        color = GameCoinGold,
        radius = radius,
        center = center
    )
    scope.drawCircle(
        color = Color(0xFFE65100),
        radius = radius,
        center = center,
        style = Stroke(width = 1.5f)
    )
    // Inner star or emboss
    scope.drawCircle(
        color = Color(0xFFFFF176),
        radius = radius * 0.6f,
        center = center
    )
}

private fun drawFuelItem(scope: DrawScope, center: Offset) {
    val w = 18f
    val h = 24f

    // Red Jerry Can Body
    scope.drawRoundRect(
        color = Color(0xFFD50000),
        topLeft = Offset(center.x - w / 2f, center.y - h / 2f),
        size = Size(w, h),
        cornerRadius = CornerRadius(3f, 3f)
    )
    // Cap
    scope.drawRect(
        color = Color(0xFFFFFFFF),
        topLeft = Offset(center.x - w * 0.3f, center.y - h * 0.65f),
        size = Size(w * 0.35f, h * 0.2f)
    )
    // Handle
    scope.drawRoundRect(
        color = Color(0xFFB71C1C),
        topLeft = Offset(center.x - w * 0.35f, center.y - h * 0.5f),
        size = Size(w * 0.7f, 4f),
        cornerRadius = CornerRadius(1f, 1f)
    )
    // White Cross / Fuel stripe
    scope.drawLine(
        color = Color(0xFFFFFFFF),
        start = Offset(center.x - w * 0.3f, center.y),
        end = Offset(center.x + w * 0.3f, center.y),
        strokeWidth = 2.5f
    )
}
