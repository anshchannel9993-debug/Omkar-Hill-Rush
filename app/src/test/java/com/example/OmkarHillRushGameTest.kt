package com.example

import com.example.model.GameLevel
import com.example.model.UpgradeType
import com.example.model.Vehicle
import com.example.model.VehicleUpgrades
import com.example.physics.GamePhysicsEngine
import com.example.physics.TerrainGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OmkarHillRushGameTest {

    @Test
    fun testAllVehiclesExistAndHaveAttributes() {
        val vehicles = Vehicle.ALL_VEHICLES
        assertEquals(4, vehicles.size)

        val runner = vehicles.find { it.id == "mountain_runner" }
        assertNotNull(runner)
        assertTrue(runner!!.isUnlocked)

        val beast = vehicles.find { it.id == "desert_beast" }
        assertNotNull(beast)
        assertFalse(beast!!.isUnlocked)
        assertEquals(1200, beast.unlockCost)
    }

    @Test
    fun testAllSixLevelsDefined() {
        val levels = GameLevel.ALL_LEVELS
        assertEquals(6, levels.size)
        assertEquals("Green Hills", levels[0].name)
        assertEquals("Rocky Mountains", levels[1].name)
        assertEquals("Desert", levels[2].name)
        assertEquals("Snow Valley", levels[3].name)
        assertEquals("Volcano", levels[4].name)
        assertEquals("Night Highway", levels[5].name)
    }

    @Test
    fun testVehicleUpgradesScaleProperly() {
        val baseRunner = Vehicle.ALL_VEHICLES.first()
        val stockUpgrades = VehicleUpgrades()
        val maxUpgrades = VehicleUpgrades(
            engineLevel = 10,
            suspensionLevel = 10,
            tiresLevel = 10,
            fuelLevel = 10,
            gripLevel = 10
        )

        assertTrue(baseRunner.getEffectiveSpeed(maxUpgrades) > baseRunner.getEffectiveSpeed(stockUpgrades))
        assertTrue(baseRunner.getEffectiveTorque(maxUpgrades) > baseRunner.getEffectiveTorque(stockUpgrades))
        assertTrue(baseRunner.getEffectiveFuelCapacity(maxUpgrades) > baseRunner.getEffectiveFuelCapacity(stockUpgrades))
    }

    @Test
    fun testTerrainGenerationAndPhysics() {
        val level = GameLevel.ALL_LEVELS.first()
        val terrain = TerrainGenerator(level)

        val y0 = terrain.getGroundHeight(0f)
        val y100 = terrain.getGroundHeight(100f)
        assertTrue(y0 > 0f)
        assertTrue(y100 > 0f)

        val vehicle = Vehicle.ALL_VEHICLES.first()
        val engine = GamePhysicsEngine(vehicle, VehicleUpgrades(), level)

        // Step simulation forward
        val initialX = engine.posX
        engine.update(
            deltaTime = 0.016f,
            isGasPressed = true,
            isBrakePressed = false
        )

        // Engine simulation should run smoothly without crashing on flat ground
        assertFalse(engine.isGameOver)
        assertTrue(engine.fuel > 0f)
    }
}
