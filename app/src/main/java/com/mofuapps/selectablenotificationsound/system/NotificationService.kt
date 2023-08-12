package com.mofuapps.selectablenotificationsound.system

import android.annotation.TargetApi
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.mofuapps.selectablenotificationsound.domain.notification.Notification
import com.mofuapps.selectablenotificationsound.domain.notification.Notification.KEY_NOTIFICATION_MESSAGE
import com.mofuapps.selectablenotificationsound.domain.notification.Notification.KEY_REPEAT_NOTIFICATION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.aprildown.ultimateringtonepicker.getParcelableExtraCompat
import java.io.IOException

class NotificationService: Service(), AudioManager.OnAudioFocusChangeListener {

    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    private val periodMillis = 2000L
    private var repeat = false
    private lateinit var message: String
    private var sound: Uri? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var audioAttributes: AudioAttributes? = null
    private var prepared: Boolean = false

    private var mediaHandler = object : MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
        override fun onPrepared(mp: MediaPlayer?) {
            prepared = true
            mp?.start()
        }

        override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
            stopSelf()
            return true
        }

    }

    private val wakeLock: PowerManager.WakeLock by lazy {
        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.mofuapps.selectablenotificationsound:ServiceWakelock")
        }
    }

    private fun acquireWakelock() {
        try {
            wakeLock.let {
                wakeLock.setReferenceCounted(false)
                if (!wakeLock.isHeld) {
                    wakeLock.acquire(10*60*1000L /*10 minutes*/)
                }
            }
        } catch (_: RuntimeException) {
        }
    }

    private fun releaseWakelock() = try {
        wakeLock.let {
            if (it.isHeld) {
                it.release()
            }
        }
    } catch (_: RuntimeException) {
    }

    private suspend fun startNotification() {
        delay(periodMillis)
        while(repeat) {
            if (prepared) {
                mediaPlayer?.start()
            }
            makeTimerNotification(message, this)
            delay(periodMillis)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        message = intent?.getStringExtra(KEY_NOTIFICATION_MESSAGE) ?: ""
        repeat = intent?.getBooleanExtra(KEY_REPEAT_NOTIFICATION, false) ?: false
        sound = intent?.getParcelableExtraCompat(Notification.KEY_SOUND_URI)

        audioAttributes = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
            .build()
        if (audioManager == null) {
            audioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }
        var alarmNoise: Uri? = sound
        // Fall back to the system default alarm if the database does not have an alarm stored.
        if (alarmNoise == null) {
            alarmNoise = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnErrorListener(mediaHandler)
        mediaPlayer?.setOnPreparedListener(mediaHandler)
        try {
            when {
                alarmNoise?.toString()?.startsWith("file:///android_asset/") == true -> {
                    val fileName = alarmNoise.toString().removePrefix("file:///android_asset/")
                    this.assets.openFd(fileName).use { afd ->
                        mediaPlayer?.setDataSource(
                            afd.fileDescriptor,
                            afd.startOffset,
                            afd.length
                        )
                    }
                }
                else -> {
                    mediaPlayer?.setDataSource(this, alarmNoise!!)
                }

            }


            startPlayback()
        } catch (t: Throwable) {
            // The alarmNoise may be on the sd card which could be busy right now.
            try {
                // Must reset the media player to clear the error state.
                mediaPlayer?.reset()
            } catch (t2: Throwable) {
                // At this point we just don't play anything.
            }
        }


        Log.d("ALARM", "onStartCommand at NotificationService")
        startForeground(
            Notification.ID,
            getNotificationBuilder(message, this)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build()
        )
        scope.launch {
            startNotification()
            Log.d("ALARM", "stopSelf at NotificationService")
            stopSelf()


        }
        return START_STICKY
    }

    @Throws(IOException::class)
    private fun startPlayback() {
        if (audioManager?.getStreamVolume(AudioManager.STREAM_NOTIFICATION) == 0) {
            return
        }

        mediaPlayer?.setAudioAttributes(audioAttributes)

        mediaPlayer?.run {
            setOnCompletionListener {
                //stop(this@NotificationService)
            }
            prepareAsync()
        }
    }


    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createAudioFocusRequest(aa: AudioAttributes): AudioFocusRequest {
        return AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setOnAudioFocusChangeListener(this@NotificationService)
            .setAcceptsDelayedFocusGain(false)
            .setWillPauseWhenDucked(false)
            .setAudioAttributes(aa)
            .build()
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> stopSelf()
        }
    }


    override fun onCreate() {
        super.onCreate()
        acquireWakelock()
        Log.d("ALARM", "onCreate")
    }

    override fun onDestroy() {
        releaseWakelock()
        Log.d("ALARM", "onDestroy")
        scope.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioAttributes != null) {
                audioManager?.abandonAudioFocusRequest(
                    createAudioFocusRequest(audioAttributes!!)
                )
            } else {

            }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(this@NotificationService)
        }
        super.onDestroy()
    }
}