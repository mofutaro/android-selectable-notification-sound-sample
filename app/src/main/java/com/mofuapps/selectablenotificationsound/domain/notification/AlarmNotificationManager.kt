package com.mofuapps.selectablenotificationsound.domain.notification

import android.net.Uri

interface AlarmNotificationManager {
    fun startNotification(message: String, repeat: Boolean, sound: Uri?)

    fun stopNotification()
}