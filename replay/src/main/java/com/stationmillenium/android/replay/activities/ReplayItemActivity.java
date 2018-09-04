package com.stationmillenium.android.replay.activities;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.libutils.PiwikTracker.PiwikPages;
import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.databinding.ReplayItemActivityBinding;
import com.stationmillenium.android.replay.dto.TrackDTO;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Activity to play a replay
 * Inspired from http://stackoverflow.com/questions/3747139/how-can-i-show-a-mediacontroller-while-playing-audio-in-android/5265629#5265629
 * Created by vincent on 28/11/16.
 */
public class ReplayItemActivity extends AppCompatActivity implements MediaPlayerControl, OnPreparedListener, OnBufferingUpdateListener, OnInfoListener, OnCompletionListener {

    public static final String REPLAY_ITEM = "ReplayItem";
    private static final String REPLAY_POSITION = "replay_position";
    private static final int PERCENT_PLAYED_TIMER_START = 0;
    private static final int PERCENT_PLAYED_TIMER_UPDATE = 500;
    private static final int REQUEST_PERMISSION_EXTERNAL_STORAGE = 1;

    private ReplayItemFragment replayItemFragment;
    private ReplayItemActivityBinding replayItemActivityBinding;

    private MediaPlayer mediaPlayer;
    private MediaController mediaController;

    private DownloadManager downloadManager;
    private TrackDTO replayToDownload;

    private TrackDTO replay;
    private int bufferPercentage;
    private boolean mediaPlayerStopped;
    private int replayPosition;
    private Timer playedPercentTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        replayItemActivityBinding = DataBindingUtil.setContentView(this, R.layout.replay_item_activity);
        setSupportActionBar(replayItemActivityBinding.replayItemToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        replayItemFragment = (ReplayItemFragment) getSupportFragmentManager().findFragmentById(R.id.replay_item_fragment);
        extractReplayData();
        if (savedInstanceState != null && savedInstanceState.containsKey(REPLAY_POSITION)) {
            replayPosition = savedInstanceState.getInt(REPLAY_POSITION);
            Timber.d("Restore media player position to : %s", replayPosition);
        }

        // get download manager reference for podcast download
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initMediaPlayer();
        PiwikTracker.trackScreenViewWithTitle(PiwikPages.REPLAY_ITEM, replay.getTitle());
    }

    private void initMediaPlayer() {
        replayItemFragment.setProgressBarVisible(true);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaController = new MediaController(this);
        try {
            mediaPlayer.setDataSource(replay.getFileURL());
            mediaPlayer.prepareAsync();
            mediaPlayerStopped = false;
        } catch (IOException e) {
            Timber.e(e, "Can't read replay : %s", replay.getFileSize());
            Snackbar.make(replayItemActivityBinding.replayItemCoordinatorLayout, R.string.replay_unavailable, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void extractReplayData() {
        Intent intent = getIntent();
        replay = (TrackDTO) intent.getSerializableExtra(REPLAY_ITEM);
        if (replay != null) {
            Timber.v("Display replay item : %s", replay);
            replayItemFragment.setReplay(replay);
            getSupportActionBar().setTitle(getString(R.string.replay_item_toolbar_title, replay.getTitle()));
        } else {
            Snackbar.make(replayItemActivityBinding.replayItemCoordinatorLayout, R.string.replay_unavailable, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        replayPosition = mediaPlayer.getCurrentPosition(); // backup current position in case of screen rotation
        mediaPlayerStopped = true;
        cancelTimer();
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    private void cancelTimer() {
        if (playedPercentTimer != null) {
            playedPercentTimer.cancel();
        }
    }

    @Override
    public void start() {
        if (!mediaPlayerStopped) {
            mediaPlayer.start();
            launchPlayedPercentTimer();
        }
    }

    @Override
    public void pause() {
        if (!mediaPlayerStopped) {
            mediaPlayer.pause();
            cancelTimer();
        }
    }

    @Override
    public int getDuration() {
        return (!mediaPlayerStopped) ? mediaPlayer.getDuration() : 0;
    }

    @Override
    public int getCurrentPosition() {
        // call only when play, if not raise exception
        return (!mediaPlayerStopped) ? mediaPlayer.getCurrentPosition() : 0;
    }

    @Override
    public void seekTo(int pos) {
        if (!mediaPlayerStopped) {
            mediaPlayer.seekTo(pos);
        }
    }

    @Override
    public boolean isPlaying() {
        return !mediaPlayerStopped && mediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return bufferPercentage;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Timber.d("Media player ready");
        // Media player ready - let's play sound
        mediaPlayer.start();
        launchPlayedPercentTimer();
        replayItemFragment.setProgressBarVisible(false);
        if (replayPosition > 0) {
            mediaPlayer.seekTo(replayPosition);
        }
        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(replayItemFragment.getRootView());
        mediaController.show();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Timber.v("Buffer update : %s", percent);
        bufferPercentage = percent;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //the MediaController will hide after 3 seconds - tap the screen to make it appear again
        mediaController.show();
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(REPLAY_POSITION, replayPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        // handle case with buffering
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                Timber.v("Media player buffering...");
                replayItemFragment.setProgressBarVisible(true);
                return true;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                Timber.v("Media player end buffering");
                replayItemFragment.setProgressBarVisible(false);
                return true;
        }
        return false;
    }

    private void launchPlayedPercentTimer() {
        playedPercentTimer = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(() -> {
                    replayItemFragment.setPlayedTimeAndDuration(getCurrentPosition(), getDuration());
                });

            }

        };
        playedPercentTimer.schedule(task, PERCENT_PLAYED_TIMER_START, PERCENT_PLAYED_TIMER_UPDATE);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Timber.d("Replay done - stop timer");
        cancelTimer();
        replayItemFragment.setPlayedTimeAndDuration(1, 0);
    }

    /**
     * Download a replay
     *
     * @param replayItem replay to download
     */
    public void downloadReplay(TrackDTO replayItem) {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            startDownload(replayItem);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {
                Snackbar.make(replayItemActivityBinding.replayItemCoordinatorLayout, R.string.replay_file_download_auth,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.replay_file_download_auth_ask, v -> requestPermission())
                        .show();
            } else {
                replayToDownload = replayItem;
                requestPermission();
            }
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSION_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_EXTERNAL_STORAGE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDownload(replayToDownload);
        } else {
            Snackbar.make(replayItemActivityBinding.replayItemCoordinatorLayout, R.string.replay_file_download_auth_refused,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private void startDownload(TrackDTO replayItem) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(replayItem.getFileURL()));
        request.setDescription(replayItem.getTitle());
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getString(R.string.replay_file_download_path, replayItem.getTitle()));
        request.setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(request);
        Snackbar.make(replayItemActivityBinding.replayItemCoordinatorLayout, R.string.replay_file_download_auth_granted,
                Snackbar.LENGTH_SHORT).show();
    }
}
