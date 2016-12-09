package com.stationmillenium.android.replay.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.databinding.ReplayItemActivityBinding;
import com.stationmillenium.android.replay.dto.TrackDTO;
import com.stationmillenium.android.replay.utils.URLManager;

import java.io.IOException;

/**
 * Activity to play a replay
 * Inspired from http://stackoverflow.com/questions/3747139/how-can-i-show-a-mediacontroller-while-playing-audio-in-android/5265629#5265629
 * Created by vincent on 28/11/16.
 */
public class ReplayItemActivity extends AppCompatActivity implements MediaPlayerControl, OnPreparedListener, OnBufferingUpdateListener {

    private static final String TAG = "ReplayItemActivity";
    public static final String REPLAY_ITEM = "ReplayItem";

    private ReplayItemFragment replayItemFragment;
    private ReplayItemActivityBinding replayItemActivityBinding;

    private MediaPlayer mediaPlayer;
    private MediaController mediaController;

    private TrackDTO replay;
    private int bufferPercentage;
    private boolean mediaPlayerStopped;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        initMediaPlayer();
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaController = new MediaController(this);
        try {
            mediaPlayer.setDataSource(URLManager.getStreamURLFromTrack(getBaseContext(), replay));
            mediaPlayer.prepareAsync();
            mediaPlayerStopped = false;
        } catch (IOException e) {
            Log.e(TAG, "Can't read replay : " + URLManager.getStreamURLFromTrack(getBaseContext(), replay), e);
            Snackbar.make(replayItemActivityBinding.replayItemCoordinatorLayout, R.string.replay_unavailable, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void extractReplayData() {
        Intent intent = getIntent();
        replay = (TrackDTO) intent.getSerializableExtra(REPLAY_ITEM);
        if (replay != null) {
            Log.v(TAG, "Display replay item : " + replay);
            replayItemFragment.setReplay(replay);
            getSupportActionBar().setTitle(getString(R.string.replay_item_toolbar_title, replay.getTitle()));
        } else {
            Snackbar.make(replayItemActivityBinding.replayItemCoordinatorLayout, R.string.replay_unavailable, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Search tag for replay from replay item
     *
     * @param tag the tag to search for
     */
    public void searchTag(String tag) {
        Log.d(TAG, "Search tag : " + tag);
        Intent searchTagIntent = new Intent(this, ReplayActivity.class);
        searchTagIntent.putExtra(ReplayActivity.REPLAY_TAG, tag);
        startActivity(searchTagIntent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayerStopped = true;
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    @Override
    public void start() {
        if (!mediaPlayerStopped) {
            mediaPlayer.start();
        }
    }

    @Override
    public void pause() {
        if (!mediaPlayerStopped) {
            mediaPlayer.pause();
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
        Log.d(TAG, "Media player ready");
        mediaPlayer.start();
        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(replayItemFragment.getRootView());
        mediaController.show();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.v(TAG, "Buffer update : " + percent);
        bufferPercentage = percent;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //the MediaController will hide after 3 seconds - tap the screen to make it appear again
        mediaController.show();
        return false;
    }
}
