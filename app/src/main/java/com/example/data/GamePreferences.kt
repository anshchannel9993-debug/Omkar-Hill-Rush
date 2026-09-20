package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Mission
import com.example.model.UpgradeType
import com.example.model.VehicleUpgrades
import org.json.JSONArray
import org.json.JSONObject

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("omkar_hill_rush_save", Context.MODE_PRIVATE)

    var coins: Int
        get() = prefs.getInt(KEY_COINS, 300)
        set(value) = prefs.edit().putInt(KEY_COINS, value.coerceAtLeast(0)).apply()

    var selectedVehicleId: String
        get() = prefs.getString(KEY_SELECTED_VEHICLE, "mountain_runner") ?: "mountain_runner"
        set(value) = prefs.edit().putString(KEY_SELECTED_VEHICLE, value).apply()

    var selectedLevelId: String
        get() = prefs.getString(KEY_SELECTED_LEVEL, "green_hills") ?: "green_hills"
        set(value) = prefs.edit().putString(KEY_SELECTED_LEVEL, value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var engineSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENGINE_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_ENGINE_SOUND, value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS, value).apply()

    var playerName: String
        get() = prefs.getString(KEY_PLAYER_NAME, "Racer Omkar") ?: "Racer Omkar"
        set(value) = prefs.edit().putString(KEY_PLAYER_NAME, value).apply()

    fun getUnlockedVehicles(): Set<String> {
        val set = prefs.getStringSet(KEY_UNLOCKED_VEHICLES, null)
        return set ?: setOf("mountain_runner")
    }

    fun unlockVehicle(vehicleId: String) {
        val set = getUnlockedVehicles().toMutableSet()
        set.add(vehicleId)
        prefs.edit().putStringSet(KEY_UNLOCKED_VEHICLES, set).apply()
    }

    fun getUnlockedLevels(): Set<String> {
        val set = prefs.getStringSet(KEY_UNLOCKED_LEVELS, null)
        return set ?: setOf("green_hills")
    }

    fun unlockLevel(levelId: String) {
        val set = getUnlockedLevels().toMutableSet()
        set.add(levelId)
        prefs.edit().putStringSet(KEY_UNLOCKED_LEVELS, set).apply()
    }

    fun getVehicleUpgrades(vehicleId: String): VehicleUpgrades {
        val jsonStr = prefs.getString("${KEY_UPGRADES_PREFIX}$vehicleId", null)
        if (jsonStr == null) return VehicleUpgrades()
        return try {
            val json = JSONObject(jsonStr)
            VehicleUpgrades(
                engineLevel = json.optInt("engine", 1),
                suspensionLevel = json.optInt("suspension", 1),
                tiresLevel = json.optInt("tires", 1),
                fuelLevel = json.optInt("fuel", 1),
                gripLevel = json.optInt("grip", 1)
            )
        } catch (_: Exception) {
            VehicleUpgrades()
        }
    }

    fun saveVehicleUpgrades(vehicleId: String, upgrades: VehicleUpgrades) {
        val json = JSONObject().apply {
            put("engine", upgrades.engineLevel)
            put("suspension", upgrades.suspensionLevel)
            put("tires", upgrades.tiresLevel)
            put("fuel", upgrades.fuelLevel)
            put("grip", upgrades.gripLevel)
        }
        prefs.edit().putString("${KEY_UPGRADES_PREFIX}$vehicleId", json.toString()).apply()
    }

    fun getBestDistance(levelId: String): Int {
        return prefs.getInt("${KEY_BEST_DIST_PREFIX}$levelId", 0)
    }

    fun saveBestDistance(levelId: String, distance: Int): Boolean {
        val currentBest = getBestDistance(levelId)
        if (distance > currentBest) {
            prefs.edit().putInt("${KEY_BEST_DIST_PREFIX}$levelId", distance).apply()
            val total = getTotalDistance() + (distance - currentBest)
            prefs.edit().putInt(KEY_TOTAL_DISTANCE, total).apply()
            return true
        }
        return false
    }

    fun getOverallBestDistance(): Int {
        val levels = listOf("green_hills", "rocky_mountains", "desert_dunes", "snow_valley", "volcano_crag", "night_highway")
        return levels.maxOfOrNull { getBestDistance(it) } ?: 0
    }

    fun getTotalDistance(): Int {
        return prefs.getInt(KEY_TOTAL_DISTANCE, 0)
    }

    fun getMissions(): List<Mission> {
        val jsonStr = prefs.getString(KEY_MISSIONS, null)
        val defaultList = Mission.getDefaultMissions()
        if (jsonStr == null) return defaultList
        return try {
            val array = JSONArray(jsonStr)
            val map = mutableMapOf<String, Pair<Int, Boolean>>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val prog = obj.getInt("progress")
                val claimed = obj.getBoolean("claimed")
                map[id] = Pair(prog, claimed)
            }
            defaultList.map { m ->
                val saved = map[m.id]
                if (saved != null) {
                    val prog = saved.first
                    val claimed = saved.second
                    m.copy(
                        currentProgress = prog,
                        isCompleted = prog >= m.target,
                        isClaimed = claimed
                    )
                } else m
            }
        } catch (_: Exception) {
            defaultList
        }
    }

    fun saveMissions(missions: List<Mission>) {
        val array = JSONArray()
        missions.forEach { m ->
            val obj = JSONObject().apply {
                put("id", m.id)
                put("progress", m.currentProgress)
                put("claimed", m.isClaimed)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_MISSIONS, array.toString()).apply()
    }

    fun resetAllData() {
        prefs.edit().clear().apply()
        coins = 300
    }

    companion object {
        private const val KEY_COINS = "player_coins"
        private const val KEY_SELECTED_VEHICLE = "selected_vehicle"
        private const val KEY_SELECTED_LEVEL = "selected_level"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_ENGINE_SOUND = "engine_sound_enabled"
        private const val KEY_HAPTICS = "haptics_enabled"
        private const val KEY_PLAYER_NAME = "player_name"
        private const val KEY_UNLOCKED_VEHICLES = "unlocked_vehicles"
        private const val KEY_UNLOCKED_LEVELS = "unlocked_levels"
        private const val KEY_UPGRADES_PREFIX = "upgrades_"
        private const val KEY_BEST_DIST_PREFIX = "best_dist_"
        private const val KEY_TOTAL_DISTANCE = "total_career_distance"
        private const val KEY_MISSIONS = "missions_data"
    }
}
