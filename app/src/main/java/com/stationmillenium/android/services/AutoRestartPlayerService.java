package com.stationmillenium.android.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.stationmillenium.android.activities.PlayerActivity;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.intents.LocalIntents;

/**
 * Service to auto restart player in case of network break
 * Starts only if enabled
 * Created by vincent on 08/02/15.
 */
public class AutoRestartPlayerService extends IntentService {

    public static final String PREVIOUS_POSITION = "PreviousPosition";
    public static final String CURRENT_POSITION = "CurrentPosition";
    public static final String PLAYER_STATE = "PlayerState";
    private static final String TAG = "AutoRestartPlayService";
    private static final long DELTA_MIN = 500;
    private static final int MAX_TRIES = 5;
    private static final long POST_DELAY = 1000;

    private Handler handler;

    public AutoRestartPlayerService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Check if we need to auto restart player");
        int previousPosition = intent.getIntExtra(PREVIOUS_POSITION, 0);
        int currentPosition = intent.getIntExtra(CURRENT_POSITION, 0);
        PlayerActivity.PlayerState state = (PlayerActivity.PlayerState) intent.getSerializableExtra(PLAYER_STATE);

        if (state == PlayerActivity.PlayerState.PLAYING) {
            if ((previousPosition > 0) && (currentPosition > 0)) {
                int delta = currentPosition - previousPosition;
                if ((delta >= 0) && (delta < DELTA_MIN)) {
                    Log.i(TAG, "Player is stuck - restart needed");
                    sendStopIntentToMediaPlayer();
                    setupHandler();
                }
            } else {
                Log.i(TAG, "Invalid intent extras");
            }
        } else {
            Log.d(TAG, "Player not playing");
        }
    }

    /**
     * Setup the handler for media player service auto restart
     */
    private void setupHandler() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (!AppUtils.isMediaPlayerServiceRunning(AutoRestartPlayerService.this)) {
                    Log.d(TAG, "Media player service is stopped - restart it");
                    Intent mediaPlayerIntent = new Intent(getBaseContext(), MediaPlayerService.class);
                    startService(mediaPlayerIntent);
                } else if (msg.what < MAX_TRIES) {
                    Log.d(TAG, "Media player service is not yet stopped - wait...");
                    handler.sendEmptyMessageDelayed(msg.what + 1, POST_DELAY);
                } else {
                    Log.w(TAG, "Can't restart media player service - exiting...");
                }
                return true;
            }
        });
        handler.sendEmptyMessageDelayed(0, POST_DELAY);
    }

    /**
     * Send a stop intent to the media player service
     */
    private void sendStopIntentToMediaPlayer() {
        Intent stopPlayerIntent = new Intent(LocalIntents.PLAYER_STOP.toString());
        stopPlayerIntent.setClass(this, MediaPlayerService.class);
        startService(stopPlayerIntent);
    }
}
