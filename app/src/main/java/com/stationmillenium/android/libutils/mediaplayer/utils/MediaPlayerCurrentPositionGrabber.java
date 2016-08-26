package com.stationmillenium.android.libutils.mediaplayer.utils;

import android.media.MediaPlayer;
import android.util.Log;

import com.stationmillenium.android.BuildConfig;

import java.lang.ref.WeakReference;

/**
 * Class to get the current position of the media player, wrapper through a weak reference
 * Static access needed because {@link java.lang.ref.WeakReference} and {@link android.media.MediaPlayer} not serializable
 *
 * @author vincent
 */
public class MediaPlayerCurrentPositionGrabber {

    private static final String TAG = "CurrentMPTimeGrabber";

    private static WeakReference<MediaPlayer> mediaPlayerRef;

    /**
     * Create a new {@link MediaPlayerCurrentPositionGrabber}
     *
     * @param mediaPlayer the {@link android.media.MediaPlayer} to make ref
     */
    public static void setMediaPlayerReference(MediaPlayer mediaPlayer) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Set the media player reference");
        mediaPlayerRef = new WeakReference<>(mediaPlayer);
    }

    /**
     * Get the {@link android.media.MediaPlayer} current position
     *
     * @return the current position or 0 if not available
     * @see android.media.MediaPlayer#getCurrentPosition()
     */
    public static int getMediaPlayerCurrentPosition() {
        if ((mediaPlayerRef != null) && (mediaPlayerRef.get() != null)) {
            try {
                return mediaPlayerRef.get().getCurrentPosition();
            } catch (IllegalStateException e) {
                Log.w(TAG, "Error while getting media player current position", e);
                return 0;
            }
        } else
            return 0;
    }

}
