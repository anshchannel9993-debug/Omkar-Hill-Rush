package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GameColorScheme = darkColorScheme(
    primary = GameNeonOrange,
    onPrimary = GameTextPrimary,
    primaryContainer = GameSurfaceElevated,
    onPrimaryContainer = GameNeonOrange,
    secondary = GameNeonCyan,
    onSecondary = GameDarkBackground,
    secondaryContainer = GameSurfaceHighlight,
    onSecondaryContainer = GameNeonCyan,
    tertiary = GameCoinGold,
    onTertiary = GameDarkBackground,
    background = GameDarkBackground,
    onBackground = GameTextPrimary,
    surface = GameSurfaceDark,
    onSurface = GameTextPrimary,
    surfaceVariant = GameSurfaceElevated,
    onSurfaceVariant = GameTextSecondary,
    outline = GameBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = GameDarkBackground.toArgb()
                it.navigationBarColor = GameDarkBackground.toArgb()
                val controller = WindowCompat.getInsetsController(it, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = GameColorScheme,
        typography = Typography,
        content = content
    )
}
