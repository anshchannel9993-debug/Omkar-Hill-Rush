package com.example.model

import androidx.compose.ui.graphics.Color

data class VehicleUpgrades(
    val engineLevel: Int = 1,
    val suspensionLevel: Int = 1,
    val tiresLevel: Int = 1,
    val fuelLevel: Int = 1,
    val gripLevel: Int = 1
) {
    fun getLevel(type: UpgradeType): Int = when (type) {
        UpgradeType.ENGINE -> engineLevel
        UpgradeType.SUSPENSION -> suspensionLevel
        UpgradeType.TIRES -> tiresLevel
        UpgradeType.FUEL_TANK -> fuelLevel
        UpgradeType.GRIP -> gripLevel
    }

    fun withUpgrade(type: UpgradeType): VehicleUpgrades = when (type) {
        UpgradeType.ENGINE -> copy(engineLevel = (engineLevel + 1).coerceAtMost(MAX_LEVEL))
        UpgradeType.SUSPENSION -> copy(suspensionLevel = (suspensionLevel + 1).coerceAtMost(MAX_LEVEL))
        UpgradeType.TIRES -> copy(tiresLevel = (tiresLevel + 1).coerceAtMost(MAX_LEVEL))
        UpgradeType.FUEL_TANK -> copy(fuelLevel = (fuelLevel + 1).coerceAtMost(MAX_LEVEL))
        UpgradeType.GRIP -> copy(gripLevel = (gripLevel + 1).coerceAtMost(MAX_LEVEL))
    }

    companion object {
        const val MAX_LEVEL = 10
        fun costForLevel(currentLevel: Int): Int {
            return (currentLevel * 150) + (currentLevel * currentLevel * 40)
        }
    }
}

enum class UpgradeType(val title: String, val description: String, val iconName: String) {
    ENGINE("Engine Tuning", "Boosts maximum speed & hill-climbing power", "speed"),
    SUSPENSION("Suspension", "Absorbs harsh landings & stabilizes hill climbs", "spring"),
    TIRES("Off-Road Tires", "Enhances traction on steep inclines & loose dirt", "tire"),
    FUEL_TANK("Fuel Capacity", "Increases tank capacity for longer cross-country runs", "fuel"),
    GRIP("Grip & Downforce", "Improves cornering & airborne rotation control", "grip")
}

data class Vehicle(
    val id: String,
    val name: String,
    val tagLine: String,
    val unlockCost: Int,
    val isUnlocked: Boolean = false,
    val primaryColor: Color,
    val accentColor: Color,
    val chassisLength: Float,
    val chassisHeight: Float,
    val wheelRadius: Float,
    val wheelOffset: Float,
    val baseMass: Float,
    val baseSpeedMultiplier: Float,
    val baseTorqueMultiplier: Float,
    val baseSuspensionStiffness: Float,
    val baseGripMultiplier: Float,
    val baseFuelCapacity: Float
) {
    fun getEffectiveSpeed(upgrades: VehicleUpgrades): Float {
        return baseSpeedMultiplier * (1f + (upgrades.engineLevel - 1) * 0.12f)
    }

    fun getEffectiveTorque(upgrades: VehicleUpgrades): Float {
        return baseTorqueMultiplier * (1f + (upgrades.engineLevel - 1) * 0.15f)
    }

    fun getEffectiveSuspension(upgrades: VehicleUpgrades): Float {
        return baseSuspensionStiffness * (1f + (upgrades.suspensionLevel - 1) * 0.10f)
    }

    fun getEffectiveGrip(upgrades: VehicleUpgrades): Float {
        return baseGripMultiplier * (1f + (upgrades.tiresLevel - 1) * 0.12f + (upgrades.gripLevel - 1) * 0.08f)
    }

    fun getEffectiveFuelCapacity(upgrades: VehicleUpgrades): Float {
        return baseFuelCapacity * (1f + (upgrades.fuelLevel - 1) * 0.15f)
    }

    companion object {
        val ALL_VEHICLES = listOf(
            Vehicle(
                id = "mountain_runner",
                name = "Mountain Runner",
                tagLine = "Agile 4x4 Trail Blazer",
                unlockCost = 0,
                isUnlocked = true,
                primaryColor = Color(0xFFFF5722),
                accentColor = Color(0xFFFFC107),
                chassisLength = 70f,
                chassisHeight = 32f,
                wheelRadius = 14f,
                wheelOffset = 24f,
                baseMass = 1.0f,
                baseSpeedMultiplier = 1.0f,
                baseTorqueMultiplier = 1.0f,
                baseSuspensionStiffness = 1.0f,
                baseGripMultiplier = 1.0f,
                baseFuelCapacity = 100f
            ),
            Vehicle(
                id = "desert_beast",
                name = "Desert Beast",
                tagLine = "High-Rev Dune Buggy",
                unlockCost = 1200,
                isUnlocked = false,
                primaryColor = Color(0xFF00E5FF),
                accentColor = Color(0xFF00B0FF),
                chassisLength = 65f,
                chassisHeight = 26f,
                wheelRadius = 13f,
                wheelOffset = 26f,
                baseMass = 0.8f,
                baseSpeedMultiplier = 1.25f,
                baseTorqueMultiplier = 0.95f,
                baseSuspensionStiffness = 1.15f,
                baseGripMultiplier = 1.1f,
                baseFuelCapacity = 90f
            ),
            Vehicle(
                id = "thunder_truck",
                name = "Thunder Truck",
                tagLine = "Heavy Duty Monster Offroader",
                unlockCost = 3500,
                isUnlocked = false,
                primaryColor = Color(0xFF7C4DFF),
                accentColor = Color(0xFFFFAB00),
                chassisLength = 85f,
                chassisHeight = 42f,
                wheelRadius = 19f,
                wheelOffset = 30f,
                baseMass = 1.5f,
                baseSpeedMultiplier = 1.1f,
                baseTorqueMultiplier = 1.45f,
                baseSuspensionStiffness = 1.4f,
                baseGripMultiplier = 1.35f,
                baseFuelCapacity = 130f
            ),
            Vehicle(
                id = "shadow_racer",
                name = "Shadow Racer",
                tagLine = "Aerodynamic Apex Climber",
                unlockCost = 8000,
                isUnlocked = false,
                primaryColor = Color(0xFF1E293B),
                accentColor = Color(0xFF00E676),
                chassisLength = 76f,
                chassisHeight = 25f,
                wheelRadius = 13f,
                wheelOffset = 28f,
                baseMass = 0.9f,
                baseSpeedMultiplier = 1.45f,
                baseTorqueMultiplier = 1.25f,
                baseSuspensionStiffness = 1.2f,
                baseGripMultiplier = 1.3f,
                baseFuelCapacity = 110f
            )
        )
    }
}
