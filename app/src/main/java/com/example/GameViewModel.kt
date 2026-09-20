package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundSystem
import com.example.data.GamePreferences
import com.example.model.GameLevel
import com.example.model.Mission
import com.example.model.UpgradeType
import com.example.model.Vehicle
import com.example.model.VehicleUpgrades
import com.example.physics.GameOverReason
import com.example.physics.GamePhysicsEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ScreenState {
    SPLASH,
    MAIN_MENU,
    GARAGE,
    LEVEL_SELECT,
    MISSIONS,
    SETTINGS,
    PLAYING
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val prefs = GamePreferences(application)
    val soundSystem = SoundSystem(application).apply {
        soundEnabled = prefs.soundEnabled
        engineSoundEnabled = prefs.engineSoundEnabled
        hapticsEnabled = prefs.hapticsEnabled
    }

    private val _currentScreen = MutableStateFlow(ScreenState.SPLASH)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    private val _coins = MutableStateFlow(prefs.coins)
    val coins: StateFlow<Int> = _coins.asStateFlow()

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private val _selectedVehicle = MutableStateFlow(Vehicle.ALL_VEHICLES.first())
    val selectedVehicle: StateFlow<Vehicle> = _selectedVehicle.asStateFlow()

    private val _currentUpgrades = MutableStateFlow(VehicleUpgrades())
    val currentUpgrades: StateFlow<VehicleUpgrades> = _currentUpgrades.asStateFlow()

    private val _levels = MutableStateFlow<List<GameLevel>>(GameLevel.ALL_LEVELS)
    val levels: StateFlow<List<GameLevel>> = _levels.asStateFlow()

    private val _selectedLevel = MutableStateFlow(GameLevel.ALL_LEVELS.first())
    val selectedLevel: StateFlow<GameLevel> = _selectedLevel.asStateFlow()

    private val _missions = MutableStateFlow<List<Mission>>(emptyList())
    val missions: StateFlow<List<Mission>> = _missions.asStateFlow()

    // Active Game Physics
    var physicsEngine: GamePhysicsEngine? = null
        private set

    private val _isGamePaused = MutableStateFlow(false)
    val isGamePaused: StateFlow<Boolean> = _isGamePaused.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    private val _gameOverReason = MutableStateFlow<GameOverReason?>(null)
    val gameOverReason: StateFlow<GameOverReason?> = _gameOverReason.asStateFlow()

    private val _runDistance = MutableStateFlow(0)
    val runDistance: StateFlow<Int> = _runDistance.asStateFlow()

    private val _runCoins = MutableStateFlow(0)
    val runCoins: StateFlow<Int> = _runCoins.asStateFlow()

    private val _isNewRecord = MutableStateFlow(false)
    val isNewRecord: StateFlow<Boolean> = _isNewRecord.asStateFlow()

    init {
        refreshState()
    }

    fun refreshState() {
        val unlockedIds = prefs.getUnlockedVehicles()
        _vehicles.value = Vehicle.ALL_VEHICLES.map { v ->
            v.copy(isUnlocked = unlockedIds.contains(v.id) || v.unlockCost == 0)
        }

        val selVehId = prefs.selectedVehicleId
        val chosenVeh = _vehicles.value.find { it.id == selVehId } ?: _vehicles.value.first()
        _selectedVehicle.value = chosenVeh
        _currentUpgrades.value = prefs.getVehicleUpgrades(chosenVeh.id)

        val selLevelId = prefs.selectedLevelId
        _selectedLevel.value = GameLevel.ALL_LEVELS.find { it.id == selLevelId } ?: GameLevel.ALL_LEVELS.first()

        _coins.value = prefs.coins
        _missions.value = prefs.getMissions()
    }

    fun navigateTo(screen: ScreenState) {
        soundSystem.playClickSound()
        if (screen == ScreenState.PLAYING) {
            startNewGame()
        }
        _currentScreen.value = screen
    }

    fun selectVehicle(vehicle: Vehicle) {
        if (!vehicle.isUnlocked) return
        prefs.selectedVehicleId = vehicle.id
        _selectedVehicle.value = vehicle
        _currentUpgrades.value = prefs.getVehicleUpgrades(vehicle.id)
        soundSystem.playClickSound()
    }

    fun unlockVehicle(vehicle: Vehicle): Boolean {
        if (_coins.value >= vehicle.unlockCost) {
            prefs.coins -= vehicle.unlockCost
            _coins.value = prefs.coins
            prefs.unlockVehicle(vehicle.id)
            soundSystem.playFuelSound()
            refreshState()
            updateMissionProgress("unlock_vehicle", prefs.getUnlockedVehicles().size)
            return true
        }
        return false
    }

    fun upgradePart(type: UpgradeType): Boolean {
        val currentLvl = _currentUpgrades.value.getLevel(type)
        if (currentLvl >= VehicleUpgrades.MAX_LEVEL) return false

        val cost = VehicleUpgrades.costForLevel(currentLvl)
        if (_coins.value >= cost) {
            prefs.coins -= cost
            _coins.value = prefs.coins
            val newUpgrades = _currentUpgrades.value.withUpgrade(type)
            _currentUpgrades.value = newUpgrades
            prefs.saveVehicleUpgrades(_selectedVehicle.value.id, newUpgrades)
            soundSystem.playFuelSound()

            val maxLvl = listOf(
                newUpgrades.engineLevel,
                newUpgrades.suspensionLevel,
                newUpgrades.tiresLevel,
                newUpgrades.fuelLevel,
                newUpgrades.gripLevel
            ).maxOrNull() ?: 1
            updateMissionProgress("upgrade_part", maxLvl)
            return true
        }
        return false
    }

    fun selectLevel(level: GameLevel) {
        val unlockedLevels = prefs.getUnlockedLevels()
        if (unlockedLevels.contains(level.id) || level.unlockCost == 0) {
            prefs.selectedLevelId = level.id
            _selectedLevel.value = level
            soundSystem.playClickSound()
        }
    }

    fun unlockLevel(level: GameLevel): Boolean {
        if (_coins.value >= level.unlockCost) {
            prefs.coins -= level.unlockCost
            _coins.value = prefs.coins
            prefs.unlockLevel(level.id)
            soundSystem.playFuelSound()
            selectLevel(level)
            return true
        }
        return false
    }

    fun claimMission(mission: Mission) {
        if (mission.isCompleted && !mission.isClaimed) {
            prefs.coins += mission.rewardCoins
            _coins.value = prefs.coins
            val updated = _missions.value.map {
                if (it.id == mission.id) it.copy(isClaimed = true) else it
            }
            _missions.value = updated
            prefs.saveMissions(updated)
            soundSystem.playCoinSound()
        }
    }

    private fun updateMissionProgress(missionId: String, progress: Int) {
        val updated = _missions.value.map { m ->
            if (m.id == missionId && !m.isClaimed) {
                val newProg = maxOf(m.currentProgress, progress)
                m.copy(currentProgress = newProg, isCompleted = newProg >= m.target)
            } else m
        }
        _missions.value = updated
        prefs.saveMissions(updated)
    }

    fun startNewGame() {
        _isGamePaused.value = false
        _isGameOver.value = false
        _gameOverReason.value = null
        _runDistance.value = 0
        _runCoins.value = 0
        _isNewRecord.value = false

        physicsEngine = GamePhysicsEngine(
            vehicle = _selectedVehicle.value,
            upgrades = _currentUpgrades.value,
            level = _selectedLevel.value
        )
    }

    fun pauseGame() {
        _isGamePaused.value = true
        soundSystem.targetEnginePitch = 0.0f
    }

    fun resumeGame() {
        _isGamePaused.value = false
    }

    fun onGameFinished(reason: GameOverReason) {
        val engine = physicsEngine ?: return
        _isGameOver.value = true
        _gameOverReason.value = reason
        _runDistance.value = engine.distanceMeters
        _runCoins.value = engine.coinsCollected

        soundSystem.playCrashSound()
        soundSystem.targetEnginePitch = 0.0f

        // Save earned coins
        prefs.coins += engine.coinsCollected
        _coins.value = prefs.coins

        // Save high score distance
        val wasRecord = prefs.saveBestDistance(_selectedLevel.value.id, engine.distanceMeters)
        _isNewRecord.value = wasRecord

        // Update missions
        updateMissionProgress("drive_250", engine.distanceMeters)
        updateMissionProgress("reach_500", engine.distanceMeters)
        updateMissionProgress("drive_1000", engine.distanceMeters)
        updateMissionProgress("collect_30_coins", engine.coinsCollected)
    }

    fun toggleSound() {
        val newState = !prefs.soundEnabled
        prefs.soundEnabled = newState
        soundSystem.soundEnabled = newState
    }

    fun toggleEngineSound() {
        val newState = !prefs.engineSoundEnabled
        prefs.engineSoundEnabled = newState
        soundSystem.engineSoundEnabled = newState
    }

    fun toggleHaptics() {
        val newState = !prefs.hapticsEnabled
        prefs.hapticsEnabled = newState
        soundSystem.hapticsEnabled = newState
    }

    override fun onCleared() {
        super.onCleared()
        soundSystem.release()
    }
}
