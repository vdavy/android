package com.stationmillenium.android.libutils.mediaplayer.utils;

import android.media.AudioManager;

import com.stationmillenium.android.services.MediaPlayerService;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Handler when audio focus change
 * Created by vincent on 14/12/14.
 */
public class MediaPlayerOnAudioFocusChangeHandler implements AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = "MPOnAFChangeHandler";

    private WeakReference<MediaPlayerService> mediaPlayerServiceRef;

    /**
     * Create a new audio focus handler
     *
     * @param service the original service
     */
    public MediaPlayerOnAudioFocusChangeHandler(MediaPlayerService service) {
        mediaPlayerServiceRef = new WeakReference<>(service);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (mediaPlayerServiceRef.get() != null) {
            Timber.d("Audio focus changed - process it...");
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    Timber.d("Audio focus gain - start playing");
                    mediaPlayerServiceRef.get().playMediaPlayer(mediaPlayerServiceRef.get());
                    mediaPlayerServiceRef.get().setMediaPlayerVolume(100);
                    break;

                case AudioManager.AUDIOFOCUS_LOSS:
                    Timber.d("Audio focus loss - stop playing");
                    mediaPlayerServiceRef.get().stopMediaPlayer();
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Timber.d("Audio focus loss transient - pause playing");
                    if (mediaPlayerServiceRef.get().isMediaPlayerPlaying()) {
                        mediaPlayerServiceRef.get().setMediaPlayerVolume(0)  ;
                    }
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Timber.d("Audio focus loss transient with duck - duck volume");
                    if (mediaPlayerServiceRef.get().isMediaPlayerPlaying()) {
                        mediaPlayerServiceRef.get().setMediaPlayerVolume(20)  ;
                    }
                    break;
            }
        }
    }
}
