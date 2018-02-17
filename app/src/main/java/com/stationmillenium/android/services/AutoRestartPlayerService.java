package com.stationmillenium.android.services;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.activities.PlayerState;
import com.stationmillenium.android.libutils.intents.LocalIntents;

import timber.log.Timber;

/**
 * Service to auto restart player in case of network break
 * Starts only if enabled
 * Created by vincent on 08/02/15.
 */
public class AutoRestartPlayerService extends JobIntentService {

    /**
     * Unique job ID for this service.
     */
    private static final int JOB_ID = 1003;

    public static final String PREVIOUS_POSITION = "PreviousPosition";
    public static final String CURRENT_POSITION = "CurrentPosition";
    public static final String PLAYER_STATE = "PlayerState";
    private static final String TAG = "AutoRestartPlayService";
    private static final long DELTA_MIN = 500;
    private static final int MAX_TRIES = 5;
    private static final long POST_DELAY = 1000;

    private Handler handler;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, AutoRestartPlayerService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Timber.d("Check if we need to auto restart player");
        int previousPosition = intent.getIntExtra(PREVIOUS_POSITION, 0);
        int currentPosition = intent.getIntExtra(CURRENT_POSITION, 0);
        PlayerState state = (PlayerState) intent.getSerializableExtra(PLAYER_STATE);

        if (state == PlayerState.PLAYING) {
            if ((previousPosition > 0) && (currentPosition > 0)) {
                int delta = currentPosition - previousPosition;
                if ((delta >= 0) && (delta < DELTA_MIN)) {
                    Timber.i("Player is stuck - restart needed");
                    sendStopIntentToMediaPlayer();
                    setupHandler();
                }
            } else {
                Timber.i("Invalid intent extras");
            }
        } else {
            Timber.d("Player not playing");
        }
    }

    /**
     * Setup the handler for media player service auto restart
     */
    private void setupHandler() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper(), msg -> {
            if (!AppUtils.isMediaPlayerServiceRunning(AutoRestartPlayerService.this)) {
                Timber.d("Media player service is stopped - restart it");
                Intent mediaPlayerIntent = new Intent(getBaseContext(), MediaPlayerService.class);
                startService(mediaPlayerIntent);
            } else if (msg.what < MAX_TRIES) {
                Timber.d("Media player service is not yet stopped - wait...");
                handler.sendEmptyMessageDelayed(msg.what + 1, POST_DELAY);
            } else {
                Timber.w("Can't restart media player service - exiting...");
            }
            return true;
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
