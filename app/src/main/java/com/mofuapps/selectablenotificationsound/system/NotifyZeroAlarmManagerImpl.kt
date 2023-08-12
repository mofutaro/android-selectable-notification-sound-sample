package com.mofuapps.selectablenotificationsound.system

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.mofuapps.selectablenotificationsound.domain.alarm.Alarm
import com.mofuapps.selectablenotificationsound.domain.alarm.NotifyZeroAlarmManager


class NotifyZeroAlarmManagerImpl(private val context: Context): NotifyZeroAlarmManager {
    private val alarmManager: AlarmManager = context
        .getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private var pendingIntent: PendingIntent? = null

    override fun setAlarm(triggerTime: Long, message: String, repeat: Boolean, sound: Uri?) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val bundle = Bundle()
        bundle.putString(Alarm.KEY_NOTIFICATION_MESSAGE, message)
        bundle.putBoolean(Alarm.KEY_REPEAT_NOTIFICATION, repeat)
        if (sound != null) {
            bundle.putParcelable(Alarm.KEY_SOUND_URI, sound)
        }
        intent.replaceExtras(bundle)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            Alarm.REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE + PendingIntent.FLAG_ONE_SHOT + PendingIntent.FLAG_UPDATE_CURRENT
        )
        Log.d("ALARM", "setAlarmClock")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.d("ALARM", "cannotScheduleExactAlarm")
        } else {
            Log.d("ALARM", "canScheduleExactAlarm")
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, null),
                pendingIntent
            )
        }
    }

    override fun cancelAlarm() {
        pendingIntent?.let{ alarmManager.cancel(it) }
    }

}
