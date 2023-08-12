package com.mofuapps.selectablenotificationsound.domain.alarm

import android.net.Uri

interface NotifyZeroAlarmManager {
    fun setAlarm(triggerTime: Long, message: String, repeat: Boolean, sound: Uri?)

    fun cancelAlarm()
}