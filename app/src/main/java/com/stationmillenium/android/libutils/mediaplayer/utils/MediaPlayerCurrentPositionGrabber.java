package com.stationmillenium.android.libutils.mediaplayer.utils;

import android.media.MediaPlayer;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Class to get the current position of the media player, wrapper through a weak reference
 * Static access needed because {@link java.lang.ref.WeakReference} and {@link android.media.MediaPlayer} not serializable
 *
 * @author vincent
 */
public class MediaPlayerCurrentPositionGrabber {

    private static WeakReference<MediaPlayer> mediaPlayerRef;

    /**
     * Create a new {@link MediaPlayerCurrentPositionGrabber}
     *
     * @param mediaPlayer the {@link android.media.MediaPlayer} to make ref
     */
    public static void setMediaPlayerReference(MediaPlayer mediaPlayer) {
        Timber.d("Set the media player reference");
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
                Timber.w(e, "Error while getting media player current position");
                return 0;
            }
        } else
            return 0;
    }

}
