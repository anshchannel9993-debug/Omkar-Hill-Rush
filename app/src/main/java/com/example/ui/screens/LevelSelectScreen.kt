package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.GameViewModel
import com.example.ScreenState
import com.example.model.GameLevel
import com.example.ui.components.CoinBadge
import com.example.ui.components.GlassGamingButton
import com.example.ui.theme.GameBorder
import com.example.ui.theme.GameCoinGold
import com.example.ui.theme.GameDarkBackground
import com.example.ui.theme.GameFuelGreen
import com.example.ui.theme.GameGlassBorder
import com.example.ui.theme.GameGlassOverlay
import com.example.ui.theme.GameNeonCyan
import com.example.ui.theme.GameNeonOrange
import com.example.ui.theme.GameSurfaceElevated
import com.example.ui.theme.GameTextMuted
import com.example.ui.theme.GameTextPrimary

@Composable
fun LevelSelectScreen(
    viewModel: GameViewModel
) {
    val coins by viewModel.coins.collectAsState()
    val levels by viewModel.levels.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val unlockedLevels = viewModel.prefs.getUnlockedLevels()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameDarkBackground)
            .testTag("level_select_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
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
                            .testTag("level_select_back_button"),
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
                            text = "SELECT TRACK",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "CONQUER UNIQUE BIOMES & ELEVATIONS",
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
                        text = "RACE TRACK",
                        icon = Icons.Default.PlayArrow,
                        onClick = { viewModel.navigateTo(ScreenState.PLAYING) },
                        isPrimary = true,
                        testTag = "start_selected_track_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6-Level Grid (3 columns x 2 rows in landscape)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(levels) { level ->
                    val isUnlocked = unlockedLevels.contains(level.id) || level.unlockCost == 0
                    val isSelected = level.id == selectedLevel.id
                    val bestDist = viewModel.prefs.getBestDistance(level.id)

                    LevelCard(
                        level = level,
                        isUnlocked = isUnlocked,
                        isSelected = isSelected,
                        bestDistance = bestDist,
                        onSelect = { viewModel.selectLevel(level) },
                        onUnlock = { viewModel.unlockLevel(level) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: GameLevel,
    isUnlocked: Boolean,
    isSelected: Boolean,
    bestDistance: Int,
    onSelect: () -> Unit,
    onUnlock: () -> Unit
) {
    val cardBrush = Brush.verticalGradient(
        colors = listOf(
            level.skyTopColor.copy(alpha = 0.65f),
            level.terrainTopColor.copy(alpha = 0.5f),
            Color(0xFF0F1524)
        )
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(cardBrush)
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) GameNeonOrange else GameGlassBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable {
                if (isUnlocked) onSelect() else onUnlock()
            }
            .padding(12.dp)
            .testTag("level_card_${level.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = level.name,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )

                // Difficulty badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x99000000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = level.difficulty.uppercase(),
                        color = GameNeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }

            Text(
                text = level.subtitle,
                color = GameTextMuted,
                fontSize = 10.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Best Distance or Lock Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isUnlocked) {
                    Column {
                        Text(
                            text = "RECORD",
                            color = GameTextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${bestDistance}m",
                            color = GameCoinGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GameNeonOrange)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GameSurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "SELECT",
                                color = GameTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = GameCoinGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "UNLOCK: %,d c".format(level.unlockCost),
                            color = GameCoinGold,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
