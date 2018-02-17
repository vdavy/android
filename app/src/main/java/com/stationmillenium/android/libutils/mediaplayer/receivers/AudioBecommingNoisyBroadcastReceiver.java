package com.stationmillenium.android.libutils.mediaplayer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.stationmillenium.android.services.MediaPlayerService;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Class to receive intent about audio becomming noisy
 *
 * @author vincent
 */
public class AudioBecommingNoisyBroadcastReceiver extends BroadcastReceiver {

    private WeakReference<MediaPlayerService> mediaPlayerServiceRef;
    private boolean registered;

    /**
     * Create a new {@link com.stationmillenium.android.libutils.mediaplayer.receivers.AudioBecommingNoisyBroadcastReceiver}
     *
     * @param mediaPlayerService the media player service
     */
    public AudioBecommingNoisyBroadcastReceiver(MediaPlayerService mediaPlayerService) {
        mediaPlayerServiceRef = new WeakReference<>(mediaPlayerService);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("Received intent that audio becomes noisy");
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
