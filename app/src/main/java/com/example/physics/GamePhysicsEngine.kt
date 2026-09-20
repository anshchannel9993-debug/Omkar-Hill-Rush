package com.example.physics

import androidx.compose.ui.geometry.Offset
import com.example.model.GameLevel
import com.example.model.Vehicle
import com.example.model.VehicleUpgrades
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class GameOverReason {
    OUT_OF_FUEL,
    DRIVER_CRASHED,
    VEHICLE_FLIPPED
}

data class StuntEvent(
    val title: String,
    val bonusCoins: Int,
    val timestamp: Long = System.currentTimeMillis()
)

class GamePhysicsEngine(
    val vehicle: Vehicle,
    val upgrades: VehicleUpgrades,
    val level: GameLevel
) {
    val terrain = TerrainGenerator(level)

    // Vehicle state
    var posX: Float = 100f
    var posY: Float = 360f
    var velX: Float = 0f
    var velY: Float = 0f
    var angle: Float = 0f // Radians
    var angularVel: Float = 0f

    // Wheel positions
    var rearWheelPos = Offset(0f, 0f)
    var frontWheelPos = Offset(0f, 0f)
    var rearWheelGrounded = false
    var frontWheelGrounded = false
    var rearWheelRotation: Float = 0f
    var frontWheelRotation: Float = 0f

    // Fuel and Run Stats
    var fuel: Float = 100f
    val maxFuel: Float = vehicle.getEffectiveFuelCapacity(upgrades)
    var distanceMeters: Int = 0
    var coinsCollected: Int = 0
    var isGameOver: Boolean = false
    var gameOverReason: GameOverReason? = null

    // Airborne & Stunt tracking
    var isAirborne: Boolean = false
    private var airTimeSeconds: Float = 0f
    private var totalAirRotation: Float = 0f
    var latestStunt: StuntEvent? = null

    // Physics parameters tuned for exciting hill climb gameplay
    private val gravity = level.gravity * 36f
    private val effSpeed = vehicle.getEffectiveSpeed(upgrades)
    private val effTorque = vehicle.getEffectiveTorque(upgrades)
    private val effSuspension = vehicle.getEffectiveSuspension(upgrades)
    private val effGrip = vehicle.getEffectiveGrip(upgrades)

    val halfWidth = vehicle.chassisLength * 0.5f
    val halfHeight = vehicle.chassisHeight * 0.5f
    val wheelRadius = vehicle.wheelRadius
    val wheelOffset = vehicle.wheelOffset

    // Dynamic item lists
    val coins = mutableListOf<CoinItem>()
    val fuelCans = mutableListOf<FuelItem>()
    private var generatedHorizonX = 0f

    init {
        // Initial terrain placement
        val initialGround = terrain.getGroundHeight(posX)
        posY = initialGround + halfHeight + wheelRadius + 10f
        generateWorldChunk(0f, 2500f)
    }

    private fun generateWorldChunk(fromX: Float, toX: Float) {
        if (toX > generatedHorizonX) {
            coins.addAll(terrain.generateCoins(generatedHorizonX, toX))
            fuelCans.addAll(terrain.generateFuelCans(generatedHorizonX, toX))
            generatedHorizonX = toX
        }
    }

    fun update(
        deltaTime: Float,
        isGasPressed: Boolean,
        isBrakePressed: Boolean,
        onCoinCollected: (Int) -> Unit = {},
        onFuelCollected: () -> Unit = {},
        onCrash: (GameOverReason) -> Unit = {}
    ) {
        if (isGameOver) return

        val dt = deltaTime.coerceIn(0.001f, 0.04f)

        // Expand procedural world horizon as vehicle travels
        if (posX + 2000f > generatedHorizonX) {
            generateWorldChunk(generatedHorizonX, generatedHorizonX + 2500f)
        }

        // 1. Calculate wheel positions relative to chassis
        val cosA = cos(angle)
        val sinA = sin(angle)

        // Local wheel offsets from chassis center:
        // rear wheel: (-wheelOffset, -halfHeight)
        // front wheel: (+wheelOffset, -halfHeight)
        val rLocalX = -wheelOffset
        val rLocalY = -halfHeight + 2f
        val fLocalX = +wheelOffset
        val fLocalY = -halfHeight + 2f

        rearWheelPos = Offset(
            posX + (rLocalX * cosA - rLocalY * sinA),
            posY + (rLocalX * sinA + rLocalY * cosA)
        )
        frontWheelPos = Offset(
            posX + (fLocalX * cosA - fLocalY * sinA),
            posY + (fLocalX * sinA + fLocalY * cosA)
        )

        // 2. Check Ground Collision for both wheels
        val rearGroundY = terrain.getGroundHeight(rearWheelPos.x)
        val frontGroundY = terrain.getGroundHeight(frontWheelPos.x)

        val rearPenetration = (rearGroundY + wheelRadius) - rearWheelPos.y
        val frontPenetration = (frontGroundY + wheelRadius) - frontWheelPos.y

        rearWheelGrounded = rearPenetration > 0f
        frontWheelGrounded = frontPenetration > 0f
        val anyWheelGrounded = rearWheelGrounded || frontWheelGrounded

        // 3. Apply Forces
        var forceX = 0f
        var forceY = -gravity * vehicle.baseMass
        var torque = 0f

        // Fuel consumption
        if (fuel > 0f) {
            val baseBurn = 1.2f / maxFuel
            val throttleBurn = if (isGasPressed) 3.5f / maxFuel else 0f
            fuel = (fuel - (baseBurn + throttleBurn) * dt * 10f).coerceAtLeast(0f)
        }

        val hasFuel = fuel > 0.01f

        if (anyWheelGrounded) {
            // Wheels on ground: suspension spring and traction
            if (rearWheelGrounded) {
                val rearNormal = terrain.getSlopeNormal(rearWheelPos.x)
                val springK = 380f * effSuspension
                val damperC = 22f * effSuspension
                val normVel = velX * rearNormal.x + velY * rearNormal.y
                val springForce = (rearPenetration * springK - normVel * damperC).coerceAtLeast(0f)

                forceX += rearNormal.x * springForce
                forceY += rearNormal.y * springForce

                // Lever torque from rear wheel
                val armX = rearWheelPos.x - posX
                val armY = rearWheelPos.y - posY
                torque += (armX * (rearNormal.y * springForce) - armY * (rearNormal.x * springForce)) * 0.003f

                // Surface traction
                val tangent = Offset(rearNormal.y, -rearNormal.x)
                if (isGasPressed && hasFuel) {
                    val driveForce = 260f * effSpeed * effTorque * effGrip * level.surfaceFriction
                    forceX += tangent.x * driveForce
                    forceY += tangent.y * driveForce
                    torque -= 3.2f * effTorque // Wheelie torque
                    rearWheelRotation += 15f * dt * effSpeed
                } else if (isBrakePressed) {
                    val brakeForce = -180f * effGrip
                    forceX += tangent.x * brakeForce
                    forceY += tangent.y * brakeForce
                    torque += 2.0f
                } else {
                    // Rolling friction
                    forceX -= velX * 0.4f
                }
            }

            if (frontWheelGrounded) {
                val frontNormal = terrain.getSlopeNormal(frontWheelPos.x)
                val springK = 380f * effSuspension
                val damperC = 22f * effSuspension
                val normVel = velX * frontNormal.x + velY * frontNormal.y
                val springForce = (frontPenetration * springK - normVel * damperC).coerceAtLeast(0f)

                forceX += frontNormal.x * springForce
                forceY += frontNormal.y * springForce

                val armX = frontWheelPos.x - posX
                val armY = frontWheelPos.y - posY
                torque += (armX * (frontNormal.y * springForce) - armY * (frontNormal.x * springForce)) * 0.003f

                val tangent = Offset(frontNormal.y, -frontNormal.x)
                if (isGasPressed && hasFuel) {
                    val driveForce = 180f * effSpeed * effTorque * effGrip * level.surfaceFriction
                    forceX += tangent.x * driveForce
                    forceY += tangent.y * driveForce
                    frontWheelRotation += 15f * dt * effSpeed
                } else if (isBrakePressed) {
                    val brakeForce = -220f * effGrip
                    forceX += tangent.x * brakeForce
                    forceY += tangent.y * brakeForce
                }
            }

            // Airborne landing evaluation
            if (isAirborne) {
                isAirborne = false
                if (airTimeSeconds > 1.2f) {
                    val bonus = (airTimeSeconds * 15f).toInt()
                    coinsCollected += bonus
                    latestStunt = StuntEvent("AIR TIME! +${bonus}c", bonus)
                }

                // Check flips
                val fullFlips = (abs(totalAirRotation) / (2f * PI)).toInt()
                if (fullFlips >= 1) {
                    val flipName = if (totalAirRotation > 0) "BACKFLIP" else "FRONTFLIP"
                    val bonus = fullFlips * 50
                    coinsCollected += bonus
                    latestStunt = StuntEvent("$flipName x$fullFlips! +${bonus}c", bonus)
                }

                airTimeSeconds = 0f
                totalAirRotation = 0f
            }

        } else {
            // Completely airborne: Air control & stunts!
            isAirborne = true
            airTimeSeconds += dt
            totalAirRotation += angularVel * dt

            if (isGasPressed) {
                // Pitch nose UP (counter-clockwise)
                torque += 6.5f
            }
            if (isBrakePressed) {
                // Pitch nose DOWN (clockwise)
                torque -= 6.5f
            }

            // Aerodynamic air damping
            angularVel *= 0.985f
            velX *= 0.998f
        }

        // 4. Integrate Velocities and Positions
        val mass = vehicle.baseMass
        velX += (forceX / mass) * dt
        velY += (forceY / mass) * dt
        angularVel += torque * dt

        // Terminal speed limits
        val maxForwardSpeed = 380f * effSpeed
        velX = velX.coerceIn(-120f, maxForwardSpeed)
        velY = velY.coerceIn(-400f, 400f)
        angularVel = angularVel.coerceIn(-8f, 8f)

        posX += velX * dt
        posY += velY * dt
        angle += angularVel * dt

        // Normalize angle within [-PI, PI] for stability
        while (angle > PI) angle -= (2f * PI.toFloat())
        while (angle < -PI) angle += (2f * PI.toFloat())

        // Distance traveled in meters
        distanceMeters = (posX / 20f).toInt().coerceAtLeast(0)

        // 5. Driver / Roof Crash Detection
        val roofX = posX - halfHeight * sinA
        val roofY = posY + halfHeight * cosA
        val roofGroundY = terrain.getGroundHeight(roofX)

        // If roof touches ground and angle is severely tilted upside down
        val isUpsideDown = abs(angle) > (PI * 0.48f)
        if (roofY < roofGroundY + 6f && (isUpsideDown || anyWheelGrounded.not())) {
            isGameOver = true
            gameOverReason = if (isUpsideDown) GameOverReason.VEHICLE_FLIPPED else GameOverReason.DRIVER_CRASHED
            onCrash(gameOverReason!!)
            return
        }

        // Out of fuel stop detection
        if (fuel <= 0.01f && abs(velX) < 8f && anyWheelGrounded) {
            isGameOver = true
            gameOverReason = GameOverReason.OUT_OF_FUEL
            onCrash(gameOverReason!!)
            return
        }

        // 6. Collectibles Detection
        val pickupRadius = 42f
        coins.forEach { coin ->
            if (!coin.isCollected) {
                val dx = posX - coin.x
                val dy = posY - coin.y
                if (sqrt(dx * dx + dy * dy) < pickupRadius) {
                    coin.isCollected = true
                    coinsCollected += coin.value
                    onCoinCollected(coin.value)
                }
            }
        }

        fuelCans.forEach { can ->
            if (!can.isCollected) {
                val dx = posX - can.x
                val dy = posY - can.y
                if (sqrt(dx * dx + dy * dy) < pickupRadius) {
                    can.isCollected = true
                    fuel = 100f
                    onFuelCollected()
                }
            }
        }
    }
}
