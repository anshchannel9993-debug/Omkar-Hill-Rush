package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GameBorder
import com.example.ui.theme.GameCoinGold
import com.example.ui.theme.GameCoinGoldDark
import com.example.ui.theme.GameFlameRed
import com.example.ui.theme.GameGlassBorder
import com.example.ui.theme.GameGlassOverlay
import com.example.ui.theme.GameNeonCyan
import com.example.ui.theme.GameNeonOrange
import com.example.ui.theme.GameSurfaceElevated
import com.example.ui.theme.GameTextMuted
import com.example.ui.theme.GameTextPrimary

@Composable
fun GlassGamingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = false,
    accentColor: Color = GameNeonOrange,
    testTag: String = "game_button"
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.94f else 1.0f, label = "button_scale")

    val bgBrush = if (isPrimary) {
        Brush.horizontalGradient(
            colors = listOf(accentColor, GameFlameRed)
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(GameSurfaceElevated, Color(0xFF161F30))
        )
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(bgBrush)
            .border(
                width = if (isPrimary) 2.dp else 1.5.dp,
                color = if (isPrimary) Color.White.copy(alpha = 0.6f) else GameGlassBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            }
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isPrimary) Color.White else accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun CoinBadge(
    amount: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val clickableMod = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() }
    } else Modifier

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .then(clickableMod)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1A1F2C))
            .border(1.5.dp, GameCoinGoldDark, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("coins_counter")
    ) {
        Icon(
            imageVector = Icons.Default.MonetizationOn,
            contentDescription = "Coins",
            tint = GameCoinGold,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "%,d".format(amount),
            color = GameCoinGold,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp
        )
    }
}

@Composable
fun PedalButton(
    label: String,
    subLabel: String,
    isGas: Boolean,
    onPressChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = if (isGas) "accelerate_button" else "brake_button"
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.92f else 1.0f, label = "pedal_scale")

    val activeColor = if (isGas) GameNeonOrange else GameNeonCyan
    val baseGradient = if (isPressed) {
        Brush.verticalGradient(
            colors = listOf(activeColor, activeColor.copy(alpha = 0.6f))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(GameSurfaceElevated, Color(0xFF0F1522))
        )
    }

    Box(
        modifier = modifier
            .size(width = 110.dp, height = 75.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(baseGradient)
            .border(
                width = if (isPressed) 3.dp else 1.5.dp,
                color = if (isPressed) Color.White else activeColor.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onPressChanged(true)
                        tryAwaitRelease()
                        isPressed = false
                        onPressChanged(false)
                    }
                )
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = if (isPressed) Color.White else activeColor,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                letterSpacing = 1.5.sp
            )
            Text(
                text = subLabel,
                color = if (isPressed) Color.White.copy(alpha = 0.8f) else GameTextMuted,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun StatProgressBar(
    label: String,
    level: Int,
    maxLevel: Int = 10,
    accentColor: Color = GameNeonOrange,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = GameTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Lv $level / $maxLevel",
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            for (i in 1..maxLevel) {
                val filled = i <= level
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (filled) accentColor else Color(0xFF1E293B))
                )
            }
        }
    }
}

@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    borderColor: Color = GameBorder,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GameGlassOverlay)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        content()
    }
}
