package com.stationmillenium.android.utils.mediaplayer.utils;

import android.media.AudioManager;
import android.util.Log;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.services.MediaPlayerService;

import java.lang.ref.WeakReference;

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
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Audio focus changed - process it...");
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "Audio focus gain - start playing");
                    mediaPlayerServiceRef.get().getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, mediaPlayerServiceRef.get().getOriginalVolume(), 0);
                    mediaPlayerServiceRef.get().playMediaPlayer(mediaPlayerServiceRef.get());
                    break;

                case AudioManager.AUDIOFOCUS_LOSS:
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "Audio focus loss - stop playing");
                    mediaPlayerServiceRef.get().stopMediaPlayer();
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "Audio focus loss transient - pause playing");
                    mediaPlayerServiceRef.get().pauseMediaPlayer(mediaPlayerServiceRef.get());
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "Audio focus loss transient with duck - duck volume");
                    if (mediaPlayerServiceRef.get().isMediaPlayerPlaying()) {
                        mediaPlayerServiceRef.get().setOriginalVolume(mediaPlayerServiceRef.get().getAudioManager().getStreamVolume(AudioManager.STREAM_MUSIC)); //save the original volume value for volume restore
                        int duckVolume = (int) (mediaPlayerServiceRef.get().getAudioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 0.2); //duck volume is 20% of max volume
                        if (mediaPlayerServiceRef.get().getOriginalVolume() > duckVolume) //if original volume is lower than duck volume, no need to duck (duck volume would be higher than dyck volume)
                            mediaPlayerServiceRef.get().getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, duckVolume, 0);
                        else if (BuildConfig.DEBUG)
                            Log.d(TAG, "Original volume lower than duck volume - no need to duck");
                    }
                    break;
            }
        }
    }
}
