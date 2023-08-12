package com.mofuapps.selectablenotificationsound.system

import android.Manifest
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mofuapps.selectablenotificationsound.R
import com.mofuapps.selectablenotificationsound.domain.notification.Notification
import com.mofuapps.selectablenotificationsound.domain.notification.Notification.CHANNEL_ID
import com.mofuapps.selectablenotificationsound.domain.notification.Notification.TITLE
import com.mofuapps.selectablenotificationsound.ui.MainActivity

private fun getContentIntent(context: Context): PendingIntent? {
    val intent = Intent(context, MainActivity::class.java)
    //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
    return TaskStackBuilder.create(context).run {
        addNextIntentWithParentStack(intent)
        getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
    }
}

fun getNotificationBuilder(message: String, context: Context): NotificationCompat.Builder {
    //val uri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.clock_alarm}")
    return NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentIntent(getContentIntent(context))
        .setSmallIcon(R.drawable.ic_stat_name)
        .setContentTitle(TITLE)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setVibrate(longArrayOf(0, 500))
        //.setSound(uri)
}


fun makeTimerNotification(message: String, context: Context) {


    val builder = getNotificationBuilder(message, context)

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        Log.d("NOTIFICATION", "NOT GRANTED")
        return
    }
    NotificationManagerCompat.from(context).notify(Notification.ID, builder.build())

}