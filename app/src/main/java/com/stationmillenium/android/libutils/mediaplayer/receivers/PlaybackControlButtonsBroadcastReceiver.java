package com.stationmillenium.android.libutils.mediaplayer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.stationmillenium.android.libutils.intents.LocalIntents;
import com.stationmillenium.android.services.MediaPlayerService;

import timber.log.Timber;

/**
 * Class to receive intent about the presses playback control buttons
 *
 * @author vincent
 */
public class PlaybackControlButtonsBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("Received intent about pressed control buttons");
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if ((event != null) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    Timber.d("Play/pause button pressed");
                    Intent playPauseIntent = new Intent(LocalIntents.PLAYER_PLAY_PAUSE.toString());
                    playPauseIntent.setClass(context, MediaPlayerService.class);
                    context.startService(playPauseIntent);
                    break;

                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    Timber.d("Play button pressed");
                    Intent playIntent = new Intent(LocalIntents.PLAYER_PLAY.toString());
                    playIntent.setClass(context, MediaPlayerService.class);
                    context.startService(playIntent);
                    break;


                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    Timber.d("Pause button pressed");
                    Intent pauseIntent = new Intent(LocalIntents.PLAYER_PAUSE.toString());
                    pauseIntent.setClass(context, MediaPlayerService.class);
                    context.startService(pauseIntent);
                    break;

                case KeyEvent.KEYCODE_MEDIA_STOP:
                    Timber.d("Stop button pressed");
                    Intent stopIntent = new Intent(LocalIntents.PLAYER_STOP.toString());
                    stopIntent.setClass(context, MediaPlayerService.class);
                    context.startService(stopIntent);
                    break;
            }
        }
    }

}
