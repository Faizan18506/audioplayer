package com.example.audioplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import com.example.audioplayer.R
import com.example.audioplayer.data.PlayerManager
import com.example.audioplayer.presentation.PlayerActivity

@UnstableApi
class AudioPlayerService : android.app.Service() {
    private var mediaSession: MediaSession? = null
    private var notificationManager: PlayerNotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        val player = PlayerManager.ensurePlayer(applicationContext)
        mediaSession = MediaSession.Builder(this, player).build()
        val channelId = NOTIFICATION_CHANNEL_ID
        createNotificationChannel(channelId)

        notificationManager = PlayerNotificationManager.Builder(this, NOTIFICATION_ID, channelId)
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: androidx.media3.common.Player): CharSequence {
                    return PlayerManager.currentSong.value?.title ?: getString(R.string.app_name)
                }

                override fun createCurrentContentIntent(player: androidx.media3.common.Player): PendingIntent? {
                    val intent = Intent(this@AudioPlayerService, PlayerActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    return PendingIntent.getActivity(
                        this@AudioPlayerService,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
                    )
                }

                override fun getCurrentContentText(player: androidx.media3.common.Player): CharSequence? {
                    return PlayerManager.currentSong.value?.artist
                }

                override fun getCurrentLargeIcon(
                    player: androidx.media3.common.Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): android.graphics.Bitmap? {
                    return null
                }
            })
            .setSmallIconResourceId(R.drawable.baseline_music_note_24)
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                    if (ongoing) {
                        startForeground(notificationId, notification)
                    } else {
                        stopForeground(false)
                    }
                }

                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    stopSelf()
                }
            })
            .build().apply {
                setUseNextAction(true)
                setUsePreviousAction(true)
                setUsePlayPauseActions(true)
                setPlayer(player)
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Ensure ongoing notification if already playing
        if (PlayerManager.isPlaying.value) {
            // NotificationManager will call startForeground via listener
            notificationManager?.invalidate()
        } else {
            // Show a silent non-ongoing notification to keep service alive briefly
            val nm = ContextCompat.getSystemService(this, NotificationManager::class.java)
            val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_music_note_24)
                .setContentTitle(getString(R.string.app_name))
                .build()
            startForeground(NOTIFICATION_ID, notification)
        }
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // App task removed (swiped away) -> stop playback and shut down service
        try {
            PlayerManager.pause()
            PlayerManager.release()
        } catch (_: Throwable) {}
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        notificationManager?.setPlayer(null)
        mediaSession?.release()
        notificationManager = null
        mediaSession = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Audio playback"
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "audio_playback"
        const val NOTIFICATION_ID = 1001
    }
}


