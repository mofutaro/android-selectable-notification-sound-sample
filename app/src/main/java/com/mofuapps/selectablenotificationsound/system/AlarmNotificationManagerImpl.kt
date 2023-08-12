package com.mofuapps.selectablenotificationsound.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.mofuapps.selectablenotificationsound.domain.notification.AlarmNotificationManager
import com.mofuapps.selectablenotificationsound.domain.notification.Notification

class AlarmNotificationManagerImpl(private val context: Context): AlarmNotificationManager {
    private val serviceClass = NotificationService::class.java
    private val serviceIntent = Intent(context, serviceClass)

    override fun startNotification(message: String, repeat: Boolean, sound: Uri?) {
        serviceIntent.putExtra(Notification.KEY_NOTIFICATION_MESSAGE, message)
        serviceIntent.putExtra(Notification.KEY_REPEAT_NOTIFICATION, repeat)
        if (sound != null) {
            serviceIntent.putExtra(Notification.KEY_SOUND_URI, sound)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    override fun stopNotification() {
        context.stopService(serviceIntent)
    }
}