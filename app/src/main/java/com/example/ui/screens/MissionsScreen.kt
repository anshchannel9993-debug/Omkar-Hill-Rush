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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.model.Mission
import com.example.ui.components.CoinBadge
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

@Composable
fun MissionsScreen(
    viewModel: GameViewModel
) {
    val coins by viewModel.coins.collectAsState()
    val missions by viewModel.missions.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameDarkBackground)
            .testTag("missions_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            // Header
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
                            .testTag("missions_back_button"),
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
                            text = "CAREER MISSIONS",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "COMPLETE CHALLENGES & EARN GOLD",
                            color = GameCoinGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                CoinBadge(amount = coins)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Missions List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(missions) { mission ->
                    MissionCard(
                        mission = mission,
                        onClaim = { viewModel.claimMission(mission) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionCard(
    mission: Mission,
    onClaim: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GameGlassOverlay)
            .border(
                1.dp,
                if (mission.isCompleted && !mission.isClaimed) GameCoinGold else GameBorder,
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (mission.isCompleted) GameCoinGold.copy(alpha = 0.2f) else Color(0xFF1E2638)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = if (mission.isCompleted) GameCoinGold else GameTextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = mission.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${mission.currentProgress} / ${mission.target}",
                        color = GameNeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = mission.description,
                    color = GameTextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { mission.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (mission.isCompleted) GameFuelGreen else GameNeonOrange,
                    trackColor = Color(0xFF1E283A)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Action Button
            when {
                mission.isClaimed -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1B2433))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Claimed",
                                tint = GameFuelGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CLAIMED",
                                color = GameFuelGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                mission.isCompleted -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.horizontalGradient(listOf(GameCoinGold, GameFlameRed)))
                            .clickable { onClaim() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("claim_mission_${mission.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CLAIM +%,d c".format(mission.rewardCoins),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1A2234))
                            .border(1.dp, GameBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "+%,d c".format(mission.rewardCoins),
                            color = GameCoinGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
