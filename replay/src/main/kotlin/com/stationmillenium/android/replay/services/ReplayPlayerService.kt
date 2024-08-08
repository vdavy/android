package com.stationmillenium.android.replay.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.core.app.ServiceCompat
import androidx.core.app.TaskStackBuilder
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.stationmillenium.android.replay.R
import com.stationmillenium.android.replay.activities.ReplayItemActivity
import com.stationmillenium.android.replay.dto.TrackDTO

class ReplayPlayerService : Service() {

    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var playerNotificationManager: PlayerNotificationManager
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private var replay: TrackDTO? = null
    private var playlistTitle: String? = null

    override fun onCreate() {
        super.onCreate()
        exoPlayer = SimpleExoPlayer.Builder(this).build()
        playerLink = exoPlayer
        playerNotificationManager = initPlayerNotificationManager()
        playerNotificationManager.setUseStopAction(true)
        playerNotificationManager.setPlayer(exoPlayer)

        mediaSession = MediaSessionCompat(this, MEDIA_SESSION_TAG)
        mediaSession.isActive = true
        playerNotificationManager.setMediaSessionToken(mediaSession.sessionToken)
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(exoPlayer)
    }

    private fun initPlayerNotificationManager(): PlayerNotificationManager =
        PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            REPLAY_NOTIF_CHANNEL_ID
        ).setMediaDescriptionAdapter(
            object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    var replayItemIntent =
                        Intent(this@ReplayPlayerService, ReplayItemActivity::class.java)
                    replayItemIntent.putExtra(ReplayItemActivity.REPLAY_ITEM, replay)
                    replayItemIntent.putExtra(ReplayItemActivity.REPLAY_PLAYLIST, playlistTitle)
                    replayItemIntent.putExtra(ReplayItemActivity.REPLAY_PLAYING, true)

                    return TaskStackBuilder.create(this@ReplayPlayerService).run {
                        addNextIntentWithParentStack(replayItemIntent)
                        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE)
                    }
                }

                override fun getCurrentContentText(player: Player): String? =
                    getString(R.string.replay_notification)

                override fun getCurrentContentTitle(player: Player): String = replay?.title
                    ?: ""

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.play_replay_icon)

            }
        ).setNotificationListener(
            object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationCancelled(
                    notificationId: Int,
                    dismissedByUser: Boolean
                ) = stopSelf()

                @RequiresApi(Build.VERSION_CODES.Q)
                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) = ServiceCompat.startForeground(this@ReplayPlayerService, notificationId, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            }
        ).setChannelNameResourceId(R.string.app_name)
            .setChannelDescriptionResourceId(R.string.app_name)
            .build()


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        replay = intent?.getSerializableExtra(ReplayItemActivity.REPLAY_ITEM) as TrackDTO
        playlistTitle = intent?.getStringExtra(ReplayItemActivity.REPLAY_PLAYLIST)
        val dataSourceFactory =
            DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)))
        val source = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(Uri.parse(replay!!.fileURL)))
        with(exoPlayer) {
            com.stationmillenium.android.replay.activities.playerEventListener?.let { addListener(it) }
            ReplayItemActivity.instance?.setPlayerControlPlayerRef(exoPlayer)
            prepare(source)
            playWhenReady = true
        }
        return START_STICKY
    }

    override fun onDestroy() {
        mediaSession.release()
        mediaSessionConnector.setPlayer(null)
        playerNotificationManager.setPlayer(null)
        com.stationmillenium.android.replay.activities.playerEventListener?.let {
            exoPlayer.removeListener(
                it
            )
        }
        exoPlayer.release()
        playerLink = null

        super.onDestroy()
    }
}

private const val NOTIFICATION_ID = 1
private const val REPLAY_NOTIF_CHANNEL_ID = "replayNotifChannelID"
private const val MEDIA_SESSION_TAG = "replay_player_session"

var playerLink: ExoPlayer? = null
