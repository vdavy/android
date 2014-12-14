package com.stationmillenium.android.utils.mediaplayer.receivers;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.PlayerActivity;
import com.stationmillenium.android.dto.CurrentTitleDTO;
import com.stationmillenium.android.services.MediaPlayerService;
import com.stationmillenium.android.utils.AppUtils;
import com.stationmillenium.android.utils.intents.LocalIntentsData;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Class to update the current playing title through intent
 *
 * @author vincent
 */
public class UpdateCurrentTitleBroadcastReceiver extends BroadcastReceiver {

    /**
     * Async loader for title image
     *
     * @author vincent
     */
    private class AsyncImageLoader extends AsyncTask<File, Void, Bitmap> {

        private CurrentTitleDTO song;

        /**
         * Create a new {@link AsyncImageLoader} with the song to update params
         *
         * @param song the {@link com.stationmillenium.android.dto.CurrentTitleDTO}
         */
        public AsyncImageLoader(CurrentTitleDTO song) {
            this.song = song;
        }

        @Override
        protected Bitmap doInBackground(File... params) {
            if ((params.length >= 1) && (params[0] != null))
                return BitmapFactory.decodeFile(params[0].getAbsolutePath());
            else
                return null;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        protected void onPostExecute(Bitmap result) {
            if (mediaPlayerServiceRef.get() != null) {
                synchronized (mediaPlayerServiceRef.get().getCurrentSongImageLock()) { //save image
                    mediaPlayerServiceRef.get().setCurrentSongImage(result);
                }

                //update the notification
                boolean notificationUpdateNeeded = (((mediaPlayerServiceRef.get().getCurrentSong() != null)
                        && (song != null)
                        && (!mediaPlayerServiceRef.get().getCurrentSong().getCurrentSong().equals(song.getCurrentSong())))
                        || ((mediaPlayerServiceRef.get().getCurrentSong() == null) && (song != null)));
                mediaPlayerServiceRef.get().setCurrentSong(song);
                if (notificationUpdateNeeded) {
                    Notification notification = mediaPlayerServiceRef.get().getMediaPlayerNotificationBuilder().createNotification(mediaPlayerServiceRef.get().getPlayerState() == PlayerActivity.PlayerState.PLAYING);
                    ((NotificationManager) mediaPlayerServiceRef.get().getSystemService(Context.NOTIFICATION_SERVICE)).notify(MediaPlayerService.NOTIFICATION_ID, notification);
                }

                //set metadata for remote control
                if ((AppUtils.isAPILevel14Available()) && (mediaPlayerServiceRef.get().getRemoteControlClient() != null)) {
                    RemoteControlClient.MetadataEditor metadataEditor = mediaPlayerServiceRef.get().getRemoteControlClient().editMetadata(false);
                    metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, song.getCurrentSong().getArtist());
                    metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, song.getCurrentSong().getArtist());
                    metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, song.getCurrentSong().getTitle());
                    if (result != null)
                        metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, result);
                    else
                        metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, BitmapFactory.decodeResource(mediaPlayerServiceRef.get().getResources(), R.drawable.player_default_image));
                    metadataEditor.apply();
                }
            }
        }
    }

    private static final String TAG = "UpdateCurrentTitleBroadcastReceiver";

    private WeakReference<MediaPlayerService> mediaPlayerServiceRef;
    private boolean registered;

    public UpdateCurrentTitleBroadcastReceiver(MediaPlayerService mediaPlayerService) {
        mediaPlayerServiceRef = new WeakReference<>(mediaPlayerService);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Update the current playing title");
        CurrentTitleDTO songData = (CurrentTitleDTO) intent.getExtras().get(LocalIntentsData.CURRENT_TITLE.toString());
        if (songData != null)
            new AsyncImageLoader(songData).execute(songData.getCurrentSong().getImage());
        else
            Log.w(TAG, "Current title DTO was null !");
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
