package com.stationmillenium.android.replay.activities

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.framework.*
import com.google.android.material.snackbar.Snackbar
import com.stationmillenium.android.libutils.PiwikTracker
import com.stationmillenium.android.libutils.PiwikTracker.PiwikPages
import com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.REPLAY_ITEM_CHROMECAST
import com.stationmillenium.android.libutils.activities.PlayerState
import com.stationmillenium.android.libutils.cast.ActivityCastUtils
import com.stationmillenium.android.libutils.cast.ActivitySessionManagerListener
import com.stationmillenium.android.replay.R
import com.stationmillenium.android.replay.databinding.ReplayItemActivityBinding
import com.stationmillenium.android.replay.dto.TrackDTO
import timber.log.Timber
import java.util.*

/**
 * Activity to play a replay
 * Inspired from http://stackoverflow.com/questions/3747139/how-can-i-show-a-mediacontroller-while-playing-audio-in-android/5265629#5265629
 * Created by vincent on 28/11/16.
 */
class ReplayItemActivity : AppCompatActivity(), Player.EventListener {

    private lateinit var replayItemFragment: ReplayItemFragment
    private lateinit var replayItemActivityBinding: ReplayItemActivityBinding

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var playerNotificationManager: PlayerNotificationManager

    private var downloadManager: DownloadManager? = null
    private var replayToDownload: TrackDTO? = null

    private var replay: TrackDTO? = null
    private var playlistTitle: String? = null
    private var mediaPlayerStopped: Boolean = false
    private var replayPosition: Int = 0
    private var playedPercentTimer: Timer? = null

    private var playerBuffering = false

    private lateinit var castMenu: MenuItem
    private lateinit var castStateListener: CastStateListener
    private lateinit var castContext: CastContext
    private lateinit var sessionManagerListener: SessionManagerListener<CastSession>
    private lateinit var activityCastUtils: ActivityCastUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        replayItemActivityBinding = DataBindingUtil.setContentView(this, R.layout.replay_item_activity)
        setSupportActionBar(replayItemActivityBinding.replayItemToolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        replayItemFragment = supportFragmentManager.findFragmentById(R.id.replay_item_fragment) as ReplayItemFragment
        extractReplayData()
        if (savedInstanceState != null && savedInstanceState.containsKey(REPLAY_POSITION)) {
            replayPosition = savedInstanceState.getInt(REPLAY_POSITION)
            Timber.d("Restore media player position to : %s", replayPosition)
        }

        // get download manager reference for podcast download
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // Cast init : https://developers.google.com/cast/docs/android_sender_integrate#initialize_the_cast_context
        castContext = CastContext.getSharedInstance(this)
        activityCastUtils = ActivityCastUtils(this) { playingOnChromecast ->
            replayItemFragment.setPlayingOnChromecast(playingOnChromecast)
            if (playingOnChromecast) {
//                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                cancelTimer()
            }
            replayItemActivityBinding.epPlayerView.isEnabled = !playingOnChromecast
            if (replayItemActivityBinding.epPlayerView.isShown) {
                replayItemActivityBinding.epPlayerView.hide()
            }
        }
        sessionManagerListener = ActivitySessionManagerListener(activityCastUtils.rmcListener, activityCastUtils,
                { exoPlayer.stop() },
                { initMediaPlayer() },
                {
                    when {
                        mediaPlayerStopped -> PlayerState.STOPPED
                        exoPlayer.playbackState == ExoPlayer.STATE_READY -> PlayerState.PLAYING
                        playerBuffering -> PlayerState.BUFFERING
                        else -> PlayerState.PAUSED
                    }
                })
        castStateListener = CastStateListener { newState ->
            if (newState != CastState.NO_DEVICES_AVAILABLE) {
                activityCastUtils.showIntroductoryOverlay(castMenu)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (replayPosition > 0) {
            initMediaPlayer()
        }

        // cast part
        castContext.addCastStateListener(castStateListener)
        castContext.sessionManager.addSessionManagerListener(sessionManagerListener, CastSession::class.java)

        piwikTrack(PiwikPages.REPLAY_ITEM)
    }

    private fun piwikTrack(piwikPage: PiwikPages) {
        if (!TextUtils.isEmpty(replay!!.title)) {
            PiwikTracker.trackScreenViewWithTitle(piwikPage, replay!!.title)
        } else {
            PiwikTracker.trackScreenView(piwikPage)
        }
    }

    fun playReplay() {
        replayPosition = 0
        initMediaPlayer()
    }

    private fun initMediaPlayer() {
        if (castContext.castState == CastState.CONNECTED) {
            Timber.d("Play on Chromecast")
            activityCastUtils.startCast(castContext.sessionManager.currentCastSession, playlistTitle,
                    replay!!.title, replay!!.imageURL, replay!!.fileURL,
                    MediaInfo.STREAM_TYPE_BUFFERED, exoPlayer.currentPosition,
                    replayItemActivityBinding.replayItemCoordinatorLayout, REPLAY_ITEM_CHROMECAST)
        } else {
//            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            replayItemFragment.setProgressBarVisible(true)
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this)
            playerNotificationManager = initPlayerNotificationManager()
            playerNotificationManager.setPlayer(exoPlayer)
            replayItemActivityBinding.epPlayerView.player = exoPlayer
            val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)))
            val source = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(replay!!.fileURL))
            with(exoPlayer) {
                prepare(source)
                playWhenReady = true
                addListener(this@ReplayItemActivity)
            }
            mediaPlayerStopped = false
        }
    }

    private fun initPlayerNotificationManager() : PlayerNotificationManager =
        PlayerNotificationManager.createWithNotificationChannel(
                this,
                REPLAY_NOTIF_CHANNEL_ID,
                R.string.app_name,
                R.string.app_name,
                NOTIFICATION_ID,
                object : MediaDescriptionAdapter {
                    override fun createCurrentContentIntent(player: Player?): PendingIntent? {
                        var replayItemIntent = Intent(this@ReplayItemActivity, ReplayItemActivity.javaClass)
                        replayItemIntent.putExtra(REPLAY_ITEM, replay)
                        replayItemIntent.putExtra(REPLAY_PLAYLIST, intent.getStringExtra(REPLAY_PLAYLIST))
                        return PendingIntent.getActivity(
                                this@ReplayItemActivity,
                                0,
                                replayItemIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT)
                    }

                    override fun getCurrentContentText(player: Player?): String? = getString(R.string.replay_notification)


                    override fun getCurrentContentTitle(player: Player?): String = replay?.title ?: ""

                    override fun getCurrentLargeIcon(player: Player?, callback: PlayerNotificationManager.BitmapCallback?): Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.play_replay_icon)
                }
        )

    override fun onPlayerError(error: ExoPlaybackException?) {
        Timber.e(error, "Can't read replay : %s", replay!!.fileSize)
        Snackbar.make(replayItemActivityBinding.replayItemCoordinatorLayout, R.string.replay_unavailable, Snackbar.LENGTH_SHORT).show()
    }

    private fun extractReplayData() {
        val intent = intent
        replay = intent.getSerializableExtra(REPLAY_ITEM) as TrackDTO
        playlistTitle = intent.getStringExtra(REPLAY_PLAYLIST)
        if (replay != null) {
            Timber.v("Display replay item : %s", replay)
            replayItemFragment.setReplay(replay!!)
            supportActionBar!!.title = getString(R.string.replay_item_toolbar_title, replay!!.title)
        } else {
            Snackbar.make(replayItemActivityBinding.replayItemCoordinatorLayout, R.string.replay_unavailable, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!mediaPlayerStopped) {
            replayPosition = exoPlayer.currentPosition.toInt() // backup current position in case of screen rotation
            exoPlayer.stop()
            exoPlayer.removeListener(this)
            playerNotificationManager.setPlayer(null)
            exoPlayer.release()
        }
        mediaPlayerStopped = true
        cancelTimer()

        // cast part
        castContext.removeCastStateListener(castStateListener)
        castContext.sessionManager.removeSessionManagerListener(sessionManagerListener, CastSession::class.java)
    }

    private fun cancelTimer() {
        if (playedPercentTimer != null) {
            playedPercentTimer!!.cancel()
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when(playbackState) {
            ExoPlayer.STATE_READY -> {
                launchPlayedPercentTimer()
                replayItemFragment.setProgressBarVisible(false)
                playerBuffering = false
                if (replayPosition > 0) {
                    exoPlayer.seekTo(replayPosition.toLong())
                }
            }
            ExoPlayer.STATE_ENDED -> {
                cancelTimer()
                exoPlayer.release()
                mediaPlayerStopped = true
            }
            ExoPlayer.STATE_BUFFERING -> {
                replayItemFragment.setProgressBarVisible(true)
                playerBuffering = true
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //the MediaController will hide after 3 seconds - tap the screen to make it appear again
        replayItemActivityBinding.epPlayerView.show()
        return false
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(REPLAY_POSITION, replayPosition)
        super.onSaveInstanceState(outState)
    }
    private fun launchPlayedPercentTimer() {
        playedPercentTimer = Timer()
        val task = object : TimerTask() {

            override fun run() {
                runOnUiThread { replayItemFragment.setPlayedTimeAndDuration(exoPlayer.currentPosition.toInt(), exoPlayer.duration.toInt()) }

            }

        }
        playedPercentTimer!!.schedule(task, PERCENT_PLAYED_TIMER_START.toLong(), PERCENT_PLAYED_TIMER_UPDATE.toLong())
    }

    /**
     * Download a replay
     *
     * @param replayItem replay to download
     */
    fun downloadReplay(replayItem: TrackDTO) {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            startDownload(replayItem)
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {
                Snackbar.make(replayItemActivityBinding.replayItemCoordinatorLayout, R.string.replay_file_download_auth,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.replay_file_download_auth_ask) { requestPermission() }
                        .show()
            } else {
                replayToDownload = replayItem
                requestPermission()
            }
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_EXTERNAL_STORAGE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_EXTERNAL_STORAGE
                && grantResults.isNotEmpty()
                && grantResults[0] == PERMISSION_GRANTED) {
            startDownload(replayToDownload!!)
        } else {
            Snackbar.make(replayItemActivityBinding.replayItemCoordinatorLayout, R.string.replay_file_download_auth_refused,
                    Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun startDownload(replayItem: TrackDTO) {
        val request = DownloadManager.Request(Uri.parse(replayItem.fileURL))
        request.setDescription(replayItem.title)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getString(R.string.replay_file_download_path, replayItem.title))
        request.setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        downloadManager!!.enqueue(request)
        Snackbar.make(replayItemActivityBinding.replayItemCoordinatorLayout, R.string.replay_file_download_auth_granted,
                Snackbar.LENGTH_SHORT).show()
        piwikTrack(PiwikPages.REPLAY_ITEM_DOWNLOAD)
    }

    // https://developers.google.com/cast/docs/android_sender_integrate#add_a_cast_button
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.cast_menu, menu)
        castMenu = CastButtonFactory.setUpMediaRouteButton(applicationContext,
                menu,
                R.id.cast_menu)
        return true
    }

    companion object {

        const val REPLAY_ITEM = "ReplayItem"
        const val REPLAY_PLAYLIST = "ReplayPlaylist"
        private const val REPLAY_POSITION = "replay_position"
        private const val PERCENT_PLAYED_TIMER_START = 0
        private const val PERCENT_PLAYED_TIMER_UPDATE = 500
        private const val REQUEST_PERMISSION_EXTERNAL_STORAGE = 1
    }
}

private const val NOTIFICATION_ID = 1
private const val REPLAY_NOTIF_CHANNEL_ID = "replayNotifChannelID"