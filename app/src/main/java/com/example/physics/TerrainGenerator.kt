package com.example.physics

import androidx.compose.ui.geometry.Offset
import com.example.model.GameLevel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class CoinItem(
    val id: Int,
    val x: Float,
    val y: Float,
    val value: Int = 10,
    var isCollected: Boolean = false
)

data class FuelItem(
    val id: Int,
    val x: Float,
    val y: Float,
    var isCollected: Boolean = false
)

class TerrainGenerator(private val level: GameLevel) {

    // Multi-octave parameters tuned per level
    private val ampMult = level.hillAmplitudeMultiplier
    private val freqMult = level.hillFrequencyMultiplier

    fun getGroundHeight(x: Float): Float {
        if (x < 0f) return 300f

        // Base undulating waves
        val w1 = 0.0035f * freqMult
        val w2 = 0.009f * freqMult
        val w3 = 0.022f * freqMult
        val w4 = 0.0012f * freqMult

        val a1 = 70f * ampMult
        val a2 = 35f * ampMult
        val a3 = 14f * ampMult
        val a4 = 110f * ampMult

        var y = 320f +
                sin(x * w1) * a1 +
                cos(x * w2) * a2 +
                sin(x * w3) * a3 +
                sin(x * w4) * a4

        // Add special jump ramps every ~160 meters (3200 units)
        val rampCycle = 3200f
        val rampX = (x % rampCycle)
        if (rampX in 1200f..1600f) {
            val rampPhase = ((rampX - 1200f) / 400f) * PI.toFloat()
            y += sin(rampPhase) * 60f * ampMult
        }

        // Add steep valley dips every ~250 meters
        val dipCycle = 5000f
        val dipX = (x % dipCycle)
        if (dipX in 2200f..2800f) {
            val dipPhase = ((dipX - 2200f) / 600f) * PI.toFloat()
            y -= sin(dipPhase) * 75f * ampMult
        }

        return y
    }

    fun getSlopeNormal(x: Float): Offset {
        val dx = 2.0f
        val y1 = getGroundHeight(x - dx)
        val y2 = getGroundHeight(x + dx)
        val dy = (y2 - y1) / (2f * dx)

        // Normal vector pointing UP from surface (in our coordinate system, larger Y is upward ground)
        val len = sqrt(dy * dy + 1f)
        return Offset(-dy / len, 1f / len)
    }

    fun getSlopeAngle(x: Float): Float {
        val dx = 2.0f
        val y1 = getGroundHeight(x - dx)
        val y2 = getGroundHeight(x + dx)
        val dy = (y2 - y1) / (2f * dx)
        return kotlin.math.atan2(dy, 1f)
    }

    fun generateCoins(startX: Float, endX: Float): List<CoinItem> {
        val coins = mutableListOf<CoinItem>()
        var curX = (startX / 100f).toInt() * 100f
        var id = (curX / 10f).toInt()

        while (curX < endX) {
            // Spawn coin arcs on hill crests and lines along hills
            val groundY = getGroundHeight(curX)
            val isRamp = ((curX % 3200f) in 1200f..1800f)

            if (isRamp) {
                // High arc in the air for jump collectibles
                for (step in 0..4) {
                    val cx = curX + step * 40f
                    val cy = getGroundHeight(cx) + 50f + (2 - Math.abs(step - 2)) * 30f
                    coins.add(CoinItem(id = id++, x = cx, y = cy, value = 25))
                }
                curX += 260f
            } else if ((curX.toInt() % 160) == 0) {
                // Line of 3 coins along the road
                for (step in 0..2) {
                    val cx = curX + step * 32f
                    val cy = getGroundHeight(cx) + 26f
                    coins.add(CoinItem(id = id++, x = cx, y = cy, value = 10))
                }
                curX += 180f
            } else {
                curX += 80f
            }
        }
        return coins
    }

    fun generateFuelCans(startX: Float, endX: Float): List<FuelItem> {
        val cans = mutableListOf<FuelItem>()
        // Place fuel can every ~95 meters (1900 units)
        val interval = 1900f
        val firstIndex = (startX / interval).toInt()
        val lastIndex = (endX / interval).toInt()

        for (i in firstIndex..lastIndex) {
            val fx = i * interval + 850f
            if (fx >= startX && fx < endX && fx > 400f) {
                val fy = getGroundHeight(fx) + 28f
                cans.add(FuelItem(id = i, x = fx, y = fy))
            }
        }
        return cans
    }
}
