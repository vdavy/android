package com.stationmillenium.android.utils.mediaplayer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.services.MediaPlayerService;

import java.lang.ref.WeakReference;

/**
 * Class to receive intent about audio becomming noisy
 *
 * @author vincent
 */
public class AudioBecommingNoisyBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "AudioBecommingNoisyBroadcastReceiver";

    private WeakReference<MediaPlayerService> mediaPlayerServiceRef;
    private boolean registered;

    /**
     * Create a new {@link com.stationmillenium.android.utils.mediaplayer.receivers.AudioBecommingNoisyBroadcastReceiver}
     *
     * @param mediaPlayerService
     */
    public AudioBecommingNoisyBroadcastReceiver(MediaPlayerService mediaPlayerService) {
        mediaPlayerServiceRef = new WeakReference<>(mediaPlayerService);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Received intent that audio becomes noisy");
        if (mediaPlayerServiceRef.get() != null) {
            mediaPlayerServiceRef.get().stopMediaPlayer();
        }
    }

    /**
     * @return the registered
     */
    public boolean isRegistered() {
        return registered;
    }

    /**
     * @param registered the registered to set
     */
    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

}
