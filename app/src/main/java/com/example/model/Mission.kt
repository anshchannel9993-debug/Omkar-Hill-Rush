package com.example.model

data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val currentProgress: Int,
    val rewardCoins: Int,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
) {
    val progressFraction: Float
        get() = (currentProgress.toFloat() / target.toFloat()).coerceIn(0f, 1f)

    companion object {
        fun getDefaultMissions(): List<Mission> = listOf(
            Mission(
                id = "drive_250",
                title = "Hill Novice",
                description = "Drive 250 meters in a single race",
                target = 250,
                currentProgress = 0,
                rewardCoins = 150
            ),
            Mission(
                id = "collect_30_coins",
                title = "Gold Rush",
                description = "Collect 30 coins in a single run",
                target = 30,
                currentProgress = 0,
                rewardCoins = 200
            ),
            Mission(
                id = "air_stunt",
                title = "Sky Glider",
                description = "Spend 3 seconds airborne during a race",
                target = 3,
                currentProgress = 0,
                rewardCoins = 300
            ),
            Mission(
                id = "reach_500",
                title = "Trail Blazer",
                description = "Reach 500 meters distance",
                target = 500,
                currentProgress = 0,
                rewardCoins = 450
            ),
            Mission(
                id = "upgrade_part",
                title = "Tuning Master",
                description = "Upgrade any component to Level 4",
                target = 4,
                currentProgress = 1,
                rewardCoins = 350
            ),
            Mission(
                id = "unlock_vehicle",
                title = "Garage Expand",
                description = "Unlock 2 different off-road vehicles",
                target = 2,
                currentProgress = 1,
                rewardCoins = 800
            ),
            Mission(
                id = "drive_1000",
                title = "Mountain Legend",
                description = "Reach 1,000 meters in any level",
                target = 1000,
                currentProgress = 0,
                rewardCoins = 1200
            )
        )
    }
}
