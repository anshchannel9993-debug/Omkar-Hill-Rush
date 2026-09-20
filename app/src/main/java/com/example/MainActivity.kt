package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.MissionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.GameDarkBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = GameDarkBackground
                ) {
                    OmkarHillRushApp()
                }
            }
        }
    }
}

@Composable
fun OmkarHillRushApp(
    viewModel: GameViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle back button on Android
    BackHandler(enabled = currentScreen != ScreenState.SPLASH) {
        when (currentScreen) {
            ScreenState.PLAYING -> viewModel.navigateTo(ScreenState.MAIN_MENU)
            ScreenState.GARAGE,
            ScreenState.LEVEL_SELECT,
            ScreenState.MISSIONS,
            ScreenState.SETTINGS -> viewModel.navigateTo(ScreenState.MAIN_MENU)
            ScreenState.MAIN_MENU,
            ScreenState.SPLASH -> {
                // Exit or stay
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
            when (screen) {
                ScreenState.SPLASH -> SplashScreen(
                    onSplashFinished = { viewModel.navigateTo(ScreenState.MAIN_MENU) }
                )
                ScreenState.MAIN_MENU -> MainMenuScreen(viewModel = viewModel)
                ScreenState.GARAGE -> GarageScreen(viewModel = viewModel)
                ScreenState.LEVEL_SELECT -> LevelSelectScreen(viewModel = viewModel)
                ScreenState.MISSIONS -> MissionsScreen(viewModel = viewModel)
                ScreenState.SETTINGS -> SettingsScreen(viewModel = viewModel)
                ScreenState.PLAYING -> GamePlayScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
