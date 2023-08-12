package com.mofuapps.selectablenotificationsound.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController


private val LightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
    onPrimary = Color.White,
    secondary = Yellow500,
    secondaryVariant = Yellow700,
    onSecondary = Color.White,
    surface = Color.White,
    onSurface = BlueGray800,
    background = BlueGray50,
    onBackground = Color.Black
)

@Composable
fun BGCountdownTimerTheme(content: @Composable () -> Unit) {

    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = LightColorPalette.surface
        )
    }


    MaterialTheme(
        colors = LightColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}