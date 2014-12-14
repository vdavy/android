package com.stationmillenium.android.utils.mediaplayer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.services.MediaPlayerService;
import com.stationmillenium.android.utils.intents.LocalIntents;

/**
 * Class to receive intent about the presses playback control buttons
 *
 * @author vincent
 */
public class PlaybackControlButtonsBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "PlaybackControlButtonsBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Received intent about pressed control buttons");
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if ((event != null) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "Play/pause button pressed");
                    Intent playPauseIntent = new Intent(LocalIntents.PLAYER_PLAY_PAUSE.toString());
                    playPauseIntent.setClass(context, MediaPlayerService.class);
                    context.startService(playPauseIntent);
                    break;

                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "Play button pressed");
                    Intent playIntent = new Intent(LocalIntents.PLAYER_PLAY.toString());
                    playIntent.setClass(context, MediaPlayerService.class);
                    context.startService(playIntent);
                    break;


                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "Pause button pressed");
                    Intent pauseIntent = new Intent(LocalIntents.PLAYER_PAUSE.toString());
                    pauseIntent.setClass(context, MediaPlayerService.class);
                    context.startService(pauseIntent);
                    break;

                case KeyEvent.KEYCODE_MEDIA_STOP:
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "Stop button pressed");
                    Intent stopIntent = new Intent(LocalIntents.PLAYER_STOP.toString());
                    stopIntent.setClass(context, MediaPlayerService.class);
                    context.startService(stopIntent);
                    break;
            }
        }
    }

}
