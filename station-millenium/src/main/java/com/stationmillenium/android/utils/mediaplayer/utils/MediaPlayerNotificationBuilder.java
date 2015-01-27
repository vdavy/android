package com.stationmillenium.android.utils.mediaplayer.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.PlayerActivity;
import com.stationmillenium.android.services.MediaPlayerService;
import com.stationmillenium.android.utils.AppUtils;
import com.stationmillenium.android.utils.intents.LocalIntents;

import java.lang.ref.WeakReference;

/**
 * Notification builder for media player
 * Created by vincent on 14/12/14.
 */
public class MediaPlayerNotificationBuilder {

    private static final String TAG = "MediaPlayerNotificationBuilder";

    private static WeakReference<MediaPlayerService> mediaPlayerServiceRef;

    private MediaController.Callback callback;

    /**
     * Create a new MediaPlayerNotificationBuilder
     *
     * @param mediaPlayerService the service
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MediaPlayerNotificationBuilder(MediaPlayerService mediaPlayerService) {
        mediaPlayerServiceRef = new WeakReference<>(mediaPlayerService);
        if (AppUtils.isAPILevel21Available()) {
            callback = new MediaController.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadata metadata) {
                    super.onMetadataChanged(metadata);
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "New media metadata : " + metadata.getDescription());
                    }
                    Notification notification = mediaPlayerServiceRef.get().getMediaPlayerNotificationBuilder().createNotification(mediaPlayerServiceRef.get().getPlayerState() == PlayerActivity.PlayerState.PLAYING);
                    ((NotificationManager) mediaPlayerServiceRef.get().getSystemService(Context.NOTIFICATION_SERVICE)).notify(MediaPlayerService.NOTIFICATION_ID, notification);
                }

                @Override
                public void onPlaybackStateChanged(PlaybackState state) {
                    switch (state.getState()) {
                        case PlaybackState.STATE_PLAYING:
                            Log.d(TAG, "Playback change state playing received");
                            setupState(true, R.string.player_play_toast);
                            break;

                        case PlaybackState.STATE_PAUSED:
                            Log.d(TAG, "Playback change state playing received");
                            setupState(false, R.string.player_pause_toast);
                            break;
                    }
                }
            };
        }
    }

    /**
     * Handle the player state
     * @param pauseAction insert pause action
     * @param toastResource the toast resource
     */
    private void setupState(boolean pauseAction, int toastResource) {
        Notification notification = createNotification(pauseAction);
        ((NotificationManager) mediaPlayerServiceRef.get().getSystemService(Context.NOTIFICATION_SERVICE)).notify(MediaPlayerService.NOTIFICATION_ID, notification);
        Toast.makeText(mediaPlayerServiceRef.get(), mediaPlayerServiceRef.get().getResources().getString(toastResource), Toast.LENGTH_SHORT).show();
    }

    public MediaController.Callback getMediaControllerCallback() {
        return callback;
    }

    /**
     * Create a notification
     *
     * @param pauseAction <code>true</code> to add pause action in {@link android.app.Notification}, <code>false</code> to add play action
     * @return the {@link android.app.Notification}
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Notification createNotification(boolean pauseAction) {
        if (mediaPlayerServiceRef.get() != null) {
            //create intent for notification
            Intent playerIntent = new Intent(mediaPlayerServiceRef.get(), PlayerActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mediaPlayerServiceRef.get());
            stackBuilder.addParentStack(PlayerActivity.class);
            stackBuilder.addNextIntent(playerIntent);
            PendingIntent playerPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            //manage state text
            String stateText;
            switch (mediaPlayerServiceRef.get().getPlayerState()) {
                case BUFFERING:
                    stateText = mediaPlayerServiceRef.get().getResources().getString(R.string.player_notification_loading);
                    break;

                case PLAYING:
                    stateText = mediaPlayerServiceRef.get().getResources().getString(R.string.player_notification_play);
                    break;

                case PAUSED:
                    stateText = mediaPlayerServiceRef.get().getResources().getString(R.string.player_notification_pause);
                    break;

                default:
                    stateText = "";
                    break;
            }

            //set the current title
            String currentTitle = ((mediaPlayerServiceRef.get().getCurrentSong() == null)
                    || (mediaPlayerServiceRef.get().getCurrentSong().getCurrentSong().getArtist() == null)
                    || (mediaPlayerServiceRef.get().getCurrentSong().getCurrentSong().getTitle() == null))
                    ? mediaPlayerServiceRef.get().getString(R.string.player_current_title_unavailable_notification)
                    : mediaPlayerServiceRef.get().getString(R.string.player_current_title_notification,
                    mediaPlayerServiceRef.get().getCurrentSong().getCurrentSong().getArtist(),
                    mediaPlayerServiceRef.get().getCurrentSong().getCurrentSong().getTitle());

            //set the player image
            Bitmap playerImage;
            synchronized (mediaPlayerServiceRef.get().getCurrentSongImageLock()) {
                playerImage = (mediaPlayerServiceRef.get().getCurrentSongImage() == null)
                        ? BitmapFactory.decodeResource(mediaPlayerServiceRef.get().getResources(), R.drawable.player_default_image)
                        : mediaPlayerServiceRef.get().getCurrentSongImage();
            }

            //pending intent for stopping player
            Intent stopIntent = new Intent(LocalIntents.PLAYER_STOP.toString());
            PendingIntent stopPendingIntent = PendingIntent.getService(mediaPlayerServiceRef.get(), 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            //pending intent for pausing or playing player
            Intent pausePlayIntent = new Intent(((pauseAction) ? LocalIntents.PLAYER_PAUSE : LocalIntents.PLAYER_PLAY).toString());
            PendingIntent pausePlayPendingIntent = PendingIntent.getService(mediaPlayerServiceRef.get(), 0, pausePlayIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (AppUtils.isAPILevel21Available()) {
                //create notification
                Notification.Builder notificationBuilder = new Notification.Builder(mediaPlayerServiceRef.get())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(Bitmap.createBitmap(playerImage)) //avoid recycled image
                        .setTicker(mediaPlayerServiceRef.get().getString(R.string.notification_ticker_text))
                        .setContentTitle(mediaPlayerServiceRef.get().getString(R.string.app_name))
                        .setContentText(currentTitle)
                        .setStyle(new Notification.MediaStyle()
                                .setShowActionsInCompactView(0, 1)  // only show play/pause in compact view
                                .setMediaSession(mediaPlayerServiceRef.get().getMediaSession().getSessionToken()))
                        .setWhen(System.currentTimeMillis() - mediaPlayerServiceRef.get().getPosition())
                        .setShowWhen(true)
                        .setUsesChronometer(true)
                        .setContentInfo(stateText)
                        .setContentIntent(playerPendingIntent);

                notificationBuilder.addAction((pauseAction) ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                        mediaPlayerServiceRef.get().getString((pauseAction) ? R.string.player_pause : R.string.player_play),
                        pausePlayPendingIntent);

                notificationBuilder.addAction(android.R.drawable.ic_media_previous, mediaPlayerServiceRef.get().getString(R.string.player_stop), stopPendingIntent);
                return notificationBuilder.build();

            } else {
                //create notification
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mediaPlayerServiceRef.get())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(Bitmap.createBitmap(playerImage)) //avoid recycled image
                        .setTicker(mediaPlayerServiceRef.get().getString(R.string.notification_ticker_text))
                        .setContentTitle(mediaPlayerServiceRef.get().getString(R.string.app_name))
                        .setContentText(currentTitle)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setWhen(System.currentTimeMillis() - mediaPlayerServiceRef.get().getPosition())
                        .setShowWhen(true)
                        .setUsesChronometer(true)
                        .setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(Bitmap.createBitmap(playerImage)) //avoid recycled image
                                .setSummaryText(currentTitle))
                        .setContentInfo(stateText)
                        .setContentIntent(playerPendingIntent);

                //add proper action (pause or play)
                notificationBuilder.addAction((pauseAction) ? R.drawable.ic_player_pause : R.drawable.ic_player_play,
                        mediaPlayerServiceRef.get().getString((pauseAction) ? R.string.player_pause : R.string.player_play),
                        pausePlayPendingIntent);

                notificationBuilder.addAction(R.drawable.ic_player_stop, mediaPlayerServiceRef.get().getString(R.string.player_stop), stopPendingIntent);
                return notificationBuilder.build();
            }

        } else
            return null;
    }
}
