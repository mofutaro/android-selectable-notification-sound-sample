package com.mofuapps.selectablenotificationsound.ui.timer

import android.net.Uri

enum class TimerScreenStage {
    STAND_BY,
    RUNNING,
    PAUSED,
    FINISHED
}

data class TimerScreenUIState(
    val stage: TimerScreenStage,
    val visualIndicator: Float,
    val numericalIndicator: String,
    val sound: Uri?,
    val soundName: String?,
    val repeat: Boolean
)