package com.stationmillenium.android.replay.activities

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.MediaController
import android.widget.MediaController.MediaPlayerControl
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
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
import java.io.IOException
import java.util.*

/**
 * Activity to play a replay
 * Inspired from http://stackoverflow.com/questions/3747139/how-can-i-show-a-mediacontroller-while-playing-audio-in-android/5265629#5265629
 * Created by vincent on 28/11/16.
 */
class ReplayItemActivity : AppCompatActivity(), MediaPlayerControl, OnPreparedListener, OnBufferingUpdateListener, OnInfoListener, OnCompletionListener {

    private var replayItemFragment: ReplayItemFragment? = null
    private var replayItemActivityBinding: ReplayItemActivityBinding? = null

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mediaController: MediaController

    private var downloadManager: DownloadManager? = null
    private var replayToDownload: TrackDTO? = null

    private var replay: TrackDTO? = null
    private var playlistTitle: String? = null
    private var bufferPercentage: Int = 0
    private var mediaPlayerStopped: Boolean = false
    private var replayPosition: Int = 0
    private var playedPercentTimer: Timer? = null

    private var playerBuffering = false

    private var castMenu: MenuItem? = null
    private var castStateListener: CastStateListener? = null
    private lateinit var castContext: CastContext
    private var sessionManagerListener: SessionManagerListener<CastSession>? = null
    private var activityCastUtils: ActivityCastUtils? = null

    private val mediaPlayerCurrentPosition: Long
        get() {
            return try {
                (mediaPlayer.currentPosition).toLong()
            } catch (e: Exception) {
                0
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        replayItemActivityBinding = DataBindingUtil.setContentView(this, R.layout.replay_item_activity)
        setSupportActionBar(replayItemActivityBinding!!.replayItemToolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        replayItemFragment = supportFragmentManager.findFragmentById(R.id.replay_item_fragment) as ReplayItemFragment?
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
            replayItemFragment!!.setPlayingOnChromecast(playingOnChromecast)
            if (playingOnChromecast) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                cancelTimer()
            }
            mediaController.isEnabled = !playingOnChromecast
            if (mediaController.isShowing) {
                mediaController.hide()
            }
        }
        sessionManagerListener = ActivitySessionManagerListener(activityCastUtils!!.rmcListener, activityCastUtils,
                { mediaPlayer.stop() },
                { initMediaPlayer() },
                {
                    when {
                        mediaPlayerStopped -> PlayerState.STOPPED
                        isPlaying -> PlayerState.PLAYING
                        playerBuffering -> PlayerState.BUFFERING
                        else -> PlayerState.PAUSED
                    }
                })
        castStateListener = CastStateListener { newState ->
            if (newState != CastState.NO_DEVICES_AVAILABLE) {
                activityCastUtils!!.showIntroductoryOverlay(castMenu)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (replayPosition > 0) {
            initMediaPlayer()
        }

        // cast part
        castContext.addCastStateListener(castStateListener!!)
        castContext.sessionManager.addSessionManagerListener(sessionManagerListener!!, CastSession::class.java)

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
            activityCastUtils!!.startCast(castContext.sessionManager.currentCastSession, playlistTitle,
                    replay!!.title, replay!!.imageURL, replay!!.fileURL,
                    MediaInfo.STREAM_TYPE_BUFFERED, mediaPlayerCurrentPosition,
                    replayItemActivityBinding!!.replayItemCoordinatorLayout, REPLAY_ITEM_CHROMECAST)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            replayItemFragment!!.setProgressBarVisible(true)
            mediaPlayer = MediaPlayer()
            with(mediaPlayer) {
                setOnPreparedListener(this@ReplayItemActivity)
                setOnInfoListener(this@ReplayItemActivity)
                setOnCompletionListener(this@ReplayItemActivity)
            }
            mediaController = MediaController(this)
            try {
                mediaPlayer.setDataSource(replay!!.fileURL)
                mediaPlayer.prepareAsync()
                mediaPlayerStopped = false
            } catch (e: IOException) {
                Timber.e(e, "Can't read replay : %s", replay!!.fileSize)
                Snackbar.make(replayItemActivityBinding!!.replayItemCoordinatorLayout, R.string.replay_unavailable, Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    private fun extractReplayData() {
        val intent = intent
        replay = intent.getSerializableExtra(REPLAY_ITEM) as TrackDTO
        playlistTitle = intent.getStringExtra(REPLAY_PLAYLIST)
        if (replay != null) {
            Timber.v("Display replay item : %s", replay)
            replayItemFragment!!.setReplay(replay!!)
            supportActionBar!!.title = getString(R.string.replay_item_toolbar_title, replay!!.title)
        } else {
            Snackbar.make(replayItemActivityBinding!!.replayItemCoordinatorLayout, R.string.replay_unavailable, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!mediaPlayerStopped) {
            replayPosition = mediaPlayerCurrentPosition.toInt() // backup current position in case of screen rotation
            mediaPlayer.stop()
            mediaPlayer.release()
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

    override fun start() {
        if (!mediaPlayerStopped) {
            mediaPlayer.start()
            launchPlayedPercentTimer()
        }
    }

    override fun pause() {
        if (!mediaPlayerStopped) {
            mediaPlayer.pause()
            cancelTimer()
        }
    }

    override fun getDuration(): Int {
        return if (!mediaPlayerStopped) mediaPlayer.duration else 0
    }

    override fun getCurrentPosition(): Int {
        // call only when play, if not raise exception
        return if (!mediaPlayerStopped) mediaPlayer.currentPosition else 0
    }

    override fun seekTo(pos: Int) {
        if (!mediaPlayerStopped) {
            mediaPlayer.seekTo(pos)
        }
    }

    override fun isPlaying(): Boolean {
        return !mediaPlayerStopped && mediaPlayer.isPlaying
    }

    override fun getBufferPercentage(): Int {
        return bufferPercentage
    }

    override fun canPause(): Boolean {
        return true
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getAudioSessionId(): Int {
        return mediaPlayer.audioSessionId
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        Timber.d("Media player ready")
        // Media player ready - let's play sound
        mediaPlayer.start()
        launchPlayedPercentTimer()
        replayItemFragment!!.setProgressBarVisible(false)
        if (replayPosition > 0) {
            mediaPlayer.seekTo(replayPosition)
        }
        mediaController.setMediaPlayer(this)
        mediaController.setAnchorView(replayItemFragment!!.rootView)
        mediaController.show()
    }

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
        Timber.v("Buffer update : %s", percent)
        bufferPercentage = percent
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //the MediaController will hide after 3 seconds - tap the screen to make it appear again
        mediaController.show()
        return false
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(REPLAY_POSITION, replayPosition)
        super.onSaveInstanceState(outState)
    }

    override fun onInfo(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        // handle case with buffering
        when (what) {
            MEDIA_INFO_BUFFERING_START -> {
                Timber.v("Media player buffering...")
                replayItemFragment!!.setProgressBarVisible(true)
                playerBuffering = true
                return true
            }
            MEDIA_INFO_BUFFERING_END -> {
                Timber.v("Media player end buffering")
                replayItemFragment!!.setProgressBarVisible(false)
                playerBuffering = false
                return true
            }
        }
        return false
    }

    private fun launchPlayedPercentTimer() {
        playedPercentTimer = Timer()
        val task = object : TimerTask() {

            override fun run() {
                runOnUiThread { replayItemFragment!!.setPlayedTimeAndDuration(currentPosition, duration) }

            }

        }
        playedPercentTimer!!.schedule(task, PERCENT_PLAYED_TIMER_START.toLong(), PERCENT_PLAYED_TIMER_UPDATE.toLong())
    }

    override fun onCompletion(mp: MediaPlayer) {
        Timber.d("Replay done - stop timer")
        cancelTimer()
        replayItemFragment!!.setPlayedTimeAndDuration(1, 0)
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
                Snackbar.make(replayItemActivityBinding!!.replayItemCoordinatorLayout, R.string.replay_file_download_auth,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.replay_file_download_auth_ask) { v -> requestPermission() }
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
            Snackbar.make(replayItemActivityBinding!!.replayItemCoordinatorLayout, R.string.replay_file_download_auth_refused,
                    Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun startDownload(replayItem: TrackDTO) {
        val request = DownloadManager.Request(Uri.parse(replayItem.fileURL))
        request.setDescription(replayItem.title)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getString(R.string.replay_file_download_path, replayItem.title))
        request.setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        downloadManager!!.enqueue(request)
        Snackbar.make(replayItemActivityBinding!!.replayItemCoordinatorLayout, R.string.replay_file_download_auth_granted,
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
