package com.mofuapps.selectablenotificationsound.ui

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.mofuapps.selectablenotificationsound.domain.notification.Notification.CHANNEL_ID
import com.mofuapps.selectablenotificationsound.domain.notification.Notification.CHANNEL_NAME
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeChannel(this)
        setContent {
            val context = LocalContext.current
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->

            }
            val permissions = remember {
                mutableStateListOf<String>()
            }

            LaunchedEffect(Unit) {
                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && !checkIfPostNotificationsGranted(context)
                ) {
                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                for (p in permissions) {
                    permissionLauncher.launch(p)
                }
            }
            MyApp()
        }
    }
}

private fun makeChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        /*val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .build()*/

        //val uri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.clock_alarm}")
        val name = CHANNEL_NAME
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        //channel.setSound(uri, audioAttributes)
        channel.setSound(null, null)
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC


        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        notificationManager?.createNotificationChannel(channel)
        NotificationCompat.Builder(context, CHANNEL_ID)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun checkIfPostNotificationsGranted(context: Context): Boolean {
    val permissions = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    )
    return permissions == PackageManager.PERMISSION_GRANTED
}