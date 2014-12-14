package com.stationmillenium.android.utils.mediaplayer.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.PlayerActivity;
import com.stationmillenium.android.services.MediaPlayerService;
import com.stationmillenium.android.utils.intents.LocalIntents;

import java.lang.ref.WeakReference;

/**
 * Notification builder for media player
 * Created by vincent on 14/12/14.
 */
public class MediaPlayerNotificationBuilder {

    private static WeakReference<MediaPlayerService> mediaPlayerServiceRef;

    /**
     * Create a new {@link com.stationmillenium.android.utils.mediaplayer.receivers.AudioBecommingNoisyBroadcastReceiver}
     *
     * @param mediaPlayerService
     */
    public MediaPlayerNotificationBuilder(MediaPlayerService mediaPlayerService) {
        mediaPlayerServiceRef = new WeakReference<>(mediaPlayerService);
    }

    /**
     * Create a notification
     *
     * @param pauseAction <code>true</code> to add pause action in {@link android.app.Notification}, <code>false</code> to add play action
     * @return the {@link android.app.Notification}
     */
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

            //create notification
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mediaPlayerServiceRef.get())
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setLargeIcon(playerImage)
                    .setTicker(mediaPlayerServiceRef.get().getString(R.string.notification_ticker_text))
                    .setContentTitle(mediaPlayerServiceRef.get().getString(R.string.app_name))
                    .setContentText(currentTitle)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(playerImage)
                            .setSummaryText(currentTitle))
                    .setContentInfo(stateText)
                    .setContentIntent(playerPendingIntent);

            //add proper action (pause or play)
            //pending intent for pausing or playing player
            Intent pausePlayIntent = new Intent(((pauseAction) ? LocalIntents.PLAYER_PAUSE : LocalIntents.PLAYER_PLAY).toString());
            PendingIntent pausePlayPendingIntent = PendingIntent.getService(mediaPlayerServiceRef.get(), 0, pausePlayIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.addAction((pauseAction) ? R.drawable.ic_player_pause : R.drawable.ic_player_play,
                    mediaPlayerServiceRef.get().getString((pauseAction) ? R.string.player_pause : R.string.player_play),
                    pausePlayPendingIntent);

            //pending intent for stopping player
            Intent stopIntent = new Intent(LocalIntents.PLAYER_STOP.toString());
            PendingIntent stopPendingIntent = PendingIntent.getService(mediaPlayerServiceRef.get(), 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.addAction(R.drawable.ic_player_stop, mediaPlayerServiceRef.get().getString(R.string.player_stop), stopPendingIntent);

            return notificationBuilder.build();

        } else
            return null;
    }
}
