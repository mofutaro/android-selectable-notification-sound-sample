package com.mofuapps.selectablenotificationsound.system

import android.Manifest
import android.annotation.TargetApi
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mofuapps.selectablenotificationsound.R
import com.mofuapps.selectablenotificationsound.domain.notification.Notification

class NotificationRepeater(
    private val context: Context,
    private val contentIntent: Intent
) {

    companion object {
        // Message codes used with the ringtone thread.
        private const val EVENT_PLAY = 1
        private const val EVENT_STOP = 2
        private const val SOUND_URI_KEY = "SOUND_URI_KEY"
        private const val LOOP = "LOOP"
        private const val TITLE = "TITLE"
        private const val TEXT = "TEXT"

        private inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelable(key, T::class.java)
            } else {
                @Suppress("DEPRECATION")
                getParcelable(key)
            }
        }
    }

    private val mHandler: Handler =
        object : Handler(HandlerThread("ringtone-player").apply { start() }.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    EVENT_PLAY -> {
                        val data = msg.data
                        val uri = data.getParcelableCompat<Uri>(SOUND_URI_KEY)
                        //if (uri != mPlaybackDelegate.currentPlayingUri) {
                            //mPlaybackDelegate.stop(context)
                            mPlaybackDelegate.start(
                                context,
                                data.getString(TITLE, ""),
                                data.getString(TEXT, ""),
                                uri,
                                data.getBoolean(LOOP),
                                contentIntent
                            )
                        //}
                    }
                    EVENT_STOP -> mPlaybackDelegate.stop(context)
                }
            }
        }

    private val mPlaybackDelegate: RepeaterDelegate by lazy {
        NotificationManagerRepeaterDelegate()
    }


    fun start(title: String, text: String, sound: Uri, repeat: Boolean ) {
        postMessage(EVENT_PLAY, title, text, sound, repeat)
    }

    fun stop() {
        postMessage(EVENT_STOP, "", "", null, false)
    }

    private fun postMessage(messageCode: Int, title: String, text: String, soundUri: Uri?, loop: Boolean) {
        synchronized(this) {
            val message = mHandler.obtainMessage(messageCode)
            if (soundUri != null) {
                val bundle = Bundle()
                bundle.putParcelable(SOUND_URI_KEY, soundUri)
                bundle.putString(TITLE, title)
                bundle.putString(TEXT, text)
                bundle.putBoolean(LOOP, loop)
                message.data = bundle
            }

            mHandler.sendMessage(message)
        }
    }

    private fun checkAsyncRingtonePlayerThread() {
        check(Looper.myLooper() == mHandler.looper) {
            "Must be on the AsyncRingtonePlayer thread!"
        }
    }

    private interface RepeaterDelegate {
        fun start(context: Context, title: String, text: String, sound: Uri?, repeat: Boolean, intent: Intent)

        fun stop(context: Context)
    }

    private inner class NotificationManagerRepeaterDelegate: RepeaterDelegate,
        AudioManager.OnAudioFocusChangeListener {

        private val ASSET_URI_PREFIX = "file:///android_asset/"
        private var audioManager: AudioManager? = null
        private var mediaPlayer: MediaPlayer? = null
        private var audioAttributes: AudioAttributes? = null
        private var canRepeat: Boolean = false

        override fun start(
            context: Context,
            title: String,
            text: String,
            sound: Uri?,
            repeat: Boolean,
            intent: Intent
        ) {
            checkAsyncRingtonePlayerThread()
            canRepeat = repeat

            audioAttributes = AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                .build()
            if (audioManager == null) {
                audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            }
            var alarmNoise: Uri? = sound
            // Fall back to the system default alarm if the database does not have an alarm stored.
            if (alarmNoise == null) {
                alarmNoise = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer()
            mediaPlayer?.setOnErrorListener { _, _, _ ->
                stop(context)
                true
            }

            try {
                // If alarmNoise is a custom ringtone on the sd card the app must be granted
                // android.permission.READ_EXTERNAL_STORAGE. Pre-M this is ensured at app
                // installation time. M+, this permission can be revoked by the user any time.

                when {
                    alarmNoise?.toString()?.startsWith(ASSET_URI_PREFIX) == true -> {
                        val fileName = alarmNoise.toString().removePrefix(ASSET_URI_PREFIX)
                        context.assets.openFd(fileName).use { afd ->
                            mediaPlayer?.setDataSource(
                                afd.fileDescriptor,
                                afd.startOffset,
                                afd.length
                            )
                        }
                    }
                    else -> {
                        mediaPlayer?.setDataSource(context, alarmNoise!!)
                    }

                }


                repeat(
                    context,
                    title,
                    text,
                    intent
                )
            } catch (t: Throwable) {
                // The alarmNoise may be on the sd card which could be busy right now.
                try {
                    // Must reset the media player to clear the error state.
                    mediaPlayer?.reset()
                } catch (t2: Throwable) {
                    // At this point we just don't play anything.
                }
            }
        }

        override fun stop(context: Context) {
            checkAsyncRingtonePlayerThread()

            // Stop audio playing
            canRepeat = false
            if (mediaPlayer != null) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (audioAttributes != null) {
                    audioManager?.abandonAudioFocusRequest(
                        createAudioFocusRequest(audioAttributes!!)
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(this)
            }
        }

        private fun repeat(
            context: Context,
            title: String,
            text: String,
            intent: Intent
        ) {
            // Do not play alarms if stream volume is 0 (typically because ringer mode is silent).
            if (audioManager?.getStreamVolume(AudioManager.STREAM_NOTIFICATION) == 0) {
                return
            }

            // Indicate the ringtone should be played via the alarm stream.
            mediaPlayer?.setAudioAttributes(audioAttributes)

            mediaPlayer?.run {
                //isLooping = false
                this.setOnCompletionListener {
                    stop(context)
                }

                this.prepare() // suspend

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    audioManager?.requestAudioFocus(createAudioFocusRequest(audioAttributes!!))
                } else {
                    @Suppress("DEPRECATION")
                    audioManager?.requestAudioFocus(
                        this@NotificationManagerRepeaterDelegate,
                        AudioManager.STREAM_NOTIFICATION,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                    )
                }

                do {
                    this.start()
                    Thread.sleep(2000)

                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        NotificationManagerCompat.from(context).notify(
                            Notification.ID,
                            getNotificationBuilder(
                                context,
                                title,
                                text,
                                intent
                            ).build()
                        )
                    }
                } while (canRepeat)
            }
        }

        override fun onAudioFocusChange(focusChange: Int) {
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> stop()
            }
        }

        @TargetApi(Build.VERSION_CODES.O)
        @RequiresApi(Build.VERSION_CODES.O)
        private fun createAudioFocusRequest(aa: AudioAttributes): AudioFocusRequest {
            return AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setOnAudioFocusChangeListener(this@NotificationManagerRepeaterDelegate)
                .setAcceptsDelayedFocusGain(false)
                .setWillPauseWhenDucked(false)
                .setAudioAttributes(aa)
                .build()
        }

        private fun getNotificationBuilder(context: Context, title: String, text: String, intent: Intent): NotificationCompat.Builder {
            intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            val pendingIntent = TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(intent)
                getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
            }
            return NotificationCompat.Builder(context, Notification.CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_MAX)
        }

    }
}