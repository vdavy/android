package com.stationmillenium.android.replay.activities

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
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
import com.google.android.exoplayer2.Player
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
import com.stationmillenium.android.replay.services.ReplayPlayerService
import com.stationmillenium.android.replay.services.playerLink
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

    private var downloadManager: DownloadManager? = null
    private var replayToDownload: TrackDTO? = null

    private var replay: TrackDTO? = null
    private var playlistTitle: String? = null
    private var mediaPlayerStopped: Boolean = false
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
        instance = this
        setSupportActionBar(replayItemActivityBinding.replayItemToolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        replayItemFragment = supportFragmentManager.findFragmentById(R.id.replay_item_fragment) as ReplayItemFragment
        val alreadyPlaying = extractReplayData()
        if (savedInstanceState != null && savedInstanceState.containsKey(REPLAY_PLAYING) || alreadyPlaying) {
            Timber.d("Restore media player playing")
            playerLink?.addListener(this)
            setPlayerControlPlayerRef(playerLink)
            launchPlayedPercentTimer()
        }

        // get download manager reference for podcast download
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // Cast init : https://developers.google.com/cast/docs/android_sender_integrate#initialize_the_cast_context
        castContext = CastContext.getSharedInstance(this)
        activityCastUtils = ActivityCastUtils(this) { playingOnChromecast ->
            replayItemFragment.setPlayingOnChromecast(playingOnChromecast)
            if (playingOnChromecast) {
                cancelTimer()
            }
            replayItemActivityBinding.epPlayerView.isEnabled = !playingOnChromecast
            if (replayItemActivityBinding.epPlayerView.isShown) {
                replayItemActivityBinding.epPlayerView.hide()
            }
        }
        sessionManagerListener = ActivitySessionManagerListener(activityCastUtils.rmcListener, activityCastUtils,
                { playerLink?.stop() },
                { initMediaPlayer() },
                {
                    when {
                        mediaPlayerStopped -> PlayerState.STOPPED
                        playerLink?.playbackState == ExoPlayer.STATE_READY -> PlayerState.PLAYING
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
        initMediaPlayer()
    }

    private fun initMediaPlayer() {
        if (castContext.castState == CastState.CONNECTED) {
            Timber.d("Play on Chromecast")
            activityCastUtils.startCast(castContext.sessionManager.currentCastSession, playlistTitle,
                    replay!!.title, replay!!.imageURL, replay!!.fileURL,
                    MediaInfo.STREAM_TYPE_BUFFERED, /*exoPlayer.currentPosition*/0,
                    replayItemActivityBinding.replayItemCoordinatorLayout, REPLAY_ITEM_CHROMECAST)
        } else {
            replayItemFragment.setProgressBarVisible(true)
            playerEventListener = this
            val replayServiceIntent = Intent(this, ReplayPlayerService::class.java)
            replayServiceIntent.putExtra(REPLAY_ITEM, replay)
            replayServiceIntent.putExtra(REPLAY_PLAYLIST, playlistTitle)
            startService(replayServiceIntent)
            mediaPlayerStopped = false
        }
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        Timber.e(error, "Can't read replay : %s", replay!!.fileSize)
        Snackbar.make(replayItemActivityBinding.replayItemCoordinatorLayout, R.string.replay_unavailable, Snackbar.LENGTH_SHORT).show()
    }

    private fun extractReplayData() : Boolean {
        replay = intent.getSerializableExtra(REPLAY_ITEM) as TrackDTO
        playlistTitle = intent.getStringExtra(REPLAY_PLAYLIST)
        if (replay != null) {
            Timber.v("Display replay item : %s", replay)
            replayItemFragment.setReplay(replay!!)
            supportActionBar!!.title = getString(R.string.replay_item_toolbar_title, replay!!.title)
        } else {
            Snackbar.make(replayItemActivityBinding.replayItemCoordinatorLayout, R.string.replay_unavailable, Snackbar.LENGTH_SHORT).show()
        }
        return intent.getBooleanExtra(REPLAY_PLAYING, false)
    }

    override fun onPause() {
        super.onPause()
        playerLink?.removeListener(this)
        playerEventListener = null
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
            }
            ExoPlayer.STATE_ENDED -> {
                cancelTimer()
                playerLink?.removeListener(this)
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
        outState.putBoolean(REPLAY_PLAYING, true)
        super.onSaveInstanceState(outState)
    }

    private fun launchPlayedPercentTimer() {
        playedPercentTimer = Timer()
        val task = object : TimerTask() {

            override fun run() {
                runOnUiThread { replayItemFragment.setPlayedTimeAndDuration(playerLink?.currentPosition?.toInt() ?: 0,
                        playerLink?.duration?.toInt() ?: 0) }

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

    fun setPlayerControlPlayerRef(player: ExoPlayer?) {
        replayItemActivityBinding.epPlayerView.player = player
    }

    companion object {

        const val REPLAY_ITEM = "ReplayItem"
        const val REPLAY_PLAYLIST = "ReplayPlaylist"
        const val REPLAY_PLAYING = "replay_playing"
        private const val PERCENT_PLAYED_TIMER_START = 0
        private const val PERCENT_PLAYED_TIMER_UPDATE = 500
        private const val REQUEST_PERMISSION_EXTERNAL_STORAGE = 1

        var instance : ReplayItemActivity? = null
    }
}

var playerEventListener : Player.EventListener? = null
