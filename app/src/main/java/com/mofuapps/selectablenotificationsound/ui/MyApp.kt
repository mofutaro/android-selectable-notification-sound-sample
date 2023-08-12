package com.mofuapps.selectablenotificationsound.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mofuapps.selectablenotificationsound.ui.theme.BGCountdownTimerTheme
import com.mofuapps.selectablenotificationsound.ui.timer.TimerScreen

@Composable
fun MyApp() {
    BGCountdownTimerTheme {
        TimerScreen(modifier = Modifier.fillMaxSize())
    }
}