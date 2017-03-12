package com.stationmillenium.android.libutils.mediaplayer.receivers;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.os.Build;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.activities.PlayerState;
import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;
import com.stationmillenium.android.services.MediaPlayerService;

import java.lang.ref.WeakReference;

/**
 * Class to update the current playing title through intent
 *
 * @author vincent
 */
public class UpdateCurrentTitleBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "UpdateCurrentTitleBR";

    private WeakReference<MediaPlayerService> mediaPlayerServiceRef;
    private boolean registered;

    public UpdateCurrentTitleBroadcastReceiver(MediaPlayerService mediaPlayerService) {
        mediaPlayerServiceRef = new WeakReference<>(mediaPlayerService);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Update the current playing title");
        final CurrentTitleDTO songData = (CurrentTitleDTO) intent.getExtras().get(LocalIntentsData.CURRENT_TITLE.toString());
        if (songData != null) {
            Glide.with(context)
                    .load(songData.getCurrentSong().getImageURL())
                    .asBitmap()
                    .placeholder(R.drawable.player_default_image)
                    .error(R.drawable.player_default_image)
                    .centerCrop()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            propagateMetaData(resource, songData);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            if (errorDrawable instanceof BitmapDrawable) {
                                propagateMetaData(((BitmapDrawable) errorDrawable).getBitmap(), songData);
                            }
                        }
                    });
        } else {
            Log.w(TAG, "Current title DTO was null !");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void propagateMetaData(Bitmap songArtBitmap, CurrentTitleDTO song) {
        if (mediaPlayerServiceRef.get() != null) {
            synchronized (mediaPlayerServiceRef.get().getCurrentSongImageLock()) { //save image
                mediaPlayerServiceRef.get().setCurrentSongImage(songArtBitmap);
            }

            //update the notification
            boolean notificationUpdateNeeded = (((mediaPlayerServiceRef.get().getCurrentSong() != null)
                    && (song != null)
                    && (!mediaPlayerServiceRef.get().getCurrentSong().getCurrentSong().equals(song.getCurrentSong())))
                    || ((mediaPlayerServiceRef.get().getCurrentSong() == null) && (song != null)));
            mediaPlayerServiceRef.get().setCurrentSong(song);
            if (notificationUpdateNeeded) {
                if (AppUtils.isAPILevel21Available()) { //we can update notification using new API
                    MediaMetadata.Builder builder = new MediaMetadata.Builder();
                    builder.putString(MediaMetadata.METADATA_KEY_ARTIST, mediaPlayerServiceRef.get().getCurrentSong().getCurrentSong().getArtist());
                    builder.putString(MediaMetadata.METADATA_KEY_TITLE, mediaPlayerServiceRef.get().getCurrentSong().getCurrentSong().getTitle());
                    builder.putBitmap(MediaMetadata.METADATA_KEY_ART, Bitmap.createBitmap(songArtBitmap));
                    mediaPlayerServiceRef.get().getMediaSession().setMetadata(builder.build());
                } else {
                    Notification notification = mediaPlayerServiceRef.get().getMediaPlayerNotificationBuilder().createNotification(mediaPlayerServiceRef.get().getPlayerState() == PlayerState.PLAYING);
                    ((NotificationManager) mediaPlayerServiceRef.get().getSystemService(Context.NOTIFICATION_SERVICE)).notify(MediaPlayerService.NOTIFICATION_ID, notification);
                }
            }

            //set metadata for remote control
            if (mediaPlayerServiceRef.get().getRemoteControlClient() != null) {
                RemoteControlClient.MetadataEditor metadataEditor = mediaPlayerServiceRef.get().getRemoteControlClient().editMetadata(false);
                metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, song.getCurrentSong().getArtist());
                metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, song.getCurrentSong().getArtist());
                metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, song.getCurrentSong().getTitle());
                metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, Bitmap.createBitmap(songArtBitmap));
                metadataEditor.apply();
            }
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
