package com.mofuapps.selectablenotificationsound.system

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mofuapps.selectablenotificationsound.domain.alarm.Alarm
import com.mofuapps.selectablenotificationsound.domain.notification.AlarmNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import xyz.aprildown.ultimateringtonepicker.getParcelableExtraCompat
import javax.inject.Inject


@AndroidEntryPoint
class AlarmReceiver: BroadcastReceiver() {
    @Inject lateinit var notificationManager: AlarmNotificationManager

    @SuppressLint("ShowToast")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            return
        }

        val message = intent.getStringExtra(Alarm.KEY_NOTIFICATION_MESSAGE) ?: ""
        val repeat = intent.getBooleanExtra(Alarm.KEY_REPEAT_NOTIFICATION, false)
        val sound = intent.getParcelableExtraCompat<Uri>(Alarm.KEY_SOUND_URI)

        notificationManager.startNotification(message, repeat, sound)
    }
}