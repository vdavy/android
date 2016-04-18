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

    private static final String TAG = "MPNotificationBuilder";
    private static final int[] COMPACT_VIEW_DOUBLE_ACTIONS = {0, 1};
    private static final int[] COMPACT_VIEW_SINGLE_ACTIONS = {0};

    private static WeakReference<MediaPlayerService> mediaPlayerServiceRef;

    private MediaController.Callback callback;

    private String currentTitle;
    private Bitmap titleArt;
    private PlayerActivity.PlayerState playerState;

    /**
     * Create a new MediaPlayerNotificationBuilder
     *
     * @param mediaPlayerService the service
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MediaPlayerNotificationBuilder(final MediaPlayerService mediaPlayerService) {
        mediaPlayerServiceRef = new WeakReference<>(mediaPlayerService);
        if (AppUtils.isAPILevel21Available()) {
            callback = new MediaController.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadata metadata) {
                    super.onMetadataChanged(metadata);
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "New media metadata : " + metadata.getDescription());
                    }

                    //set up data
                    currentTitle = ((metadata.getText(MediaMetadata.METADATA_KEY_ARTIST) == null)
                            || (metadata.getText(MediaMetadata.METADATA_KEY_TITLE) == null))
                            ? mediaPlayerServiceRef.get().getString(R.string.player_current_title_unavailable_notification)
                            : mediaPlayerServiceRef.get().getString(R.string.player_current_title_notification,
                            metadata.getText(MediaMetadata.METADATA_KEY_ARTIST),
                            metadata.getText(MediaMetadata.METADATA_KEY_TITLE));

                    titleArt = (metadata.getBitmap(MediaMetadata.METADATA_KEY_ART) == null)
                            ? BitmapFactory.decodeResource(mediaPlayerServiceRef.get().getResources(), R.drawable.player_default_image)
                            : metadata.getBitmap(MediaMetadata.METADATA_KEY_ART);

                    Notification notification = mediaPlayerServiceRef.get().getMediaPlayerNotificationBuilder().initializeNotification(mediaPlayerServiceRef.get().getPlayerState() == PlayerActivity.PlayerState.PLAYING);
                    ((NotificationManager) mediaPlayerServiceRef.get().getSystemService(Context.NOTIFICATION_SERVICE)).notify(MediaPlayerService.NOTIFICATION_ID, notification);
                }

                @Override
                public void onPlaybackStateChanged(PlaybackState state) {
                    switch (state.getState()) {
                        case PlaybackState.STATE_PLAYING:
                            Log.d(TAG, "Playback change state playing received");
                            setupState(true);
                            break;

                        case PlaybackState.STATE_PAUSED:
                            Log.d(TAG, "Playback change state playing received");
                            setupState(false);
                            break;

                        case PlaybackState.STATE_BUFFERING: //no notification to display during buffering
                            Log.d(TAG, "Playback change state buffering received");
                            setupState(false);
                            break;
                    }
                }
            };
        }
    }

    /**
     * Handle the player state
     *
     * @param pauseAction   insert pause action
     */
    private void setupState(boolean pauseAction) {
        Notification notification = createNotification(pauseAction);
        ((NotificationManager) mediaPlayerServiceRef.get().getSystemService(Context.NOTIFICATION_SERVICE)).notify(MediaPlayerService.NOTIFICATION_ID, notification);
    }

    public MediaController.Callback getMediaControllerCallback() {
        return callback;
    }

    /**
     * Initialize a notification with data already set
     *
     * @param pauseAction <code>true</code> to add pause action in {@link android.app.Notification}, <code>false</code> to add play action
     * @return the {@link android.app.Notification}
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Notification createNotification(boolean pauseAction) {
        if (mediaPlayerServiceRef.get() != null) {
            //title part
            currentTitle = ((mediaPlayerServiceRef.get().getCurrentSong() == null)
                    || (mediaPlayerServiceRef.get().getCurrentSong().getCurrentSong().getArtist() == null)
                    || (mediaPlayerServiceRef.get().getCurrentSong().getCurrentSong().getTitle() == null))
                    ? mediaPlayerServiceRef.get().getString(R.string.player_current_title_unavailable_notification)
                    : mediaPlayerServiceRef.get().getString(R.string.player_current_title_notification,
                    mediaPlayerServiceRef.get().getCurrentSong().getCurrentSong().getArtist(),
                    mediaPlayerServiceRef.get().getCurrentSong().getCurrentSong().getTitle());

            //song image part
            synchronized (mediaPlayerServiceRef.get().getCurrentSongImageLock()) {
                titleArt = (mediaPlayerServiceRef.get().getCurrentSongImage() == null)
                        ? BitmapFactory.decodeResource(mediaPlayerServiceRef.get().getResources(), R.drawable.player_default_image)
                        : mediaPlayerServiceRef.get().getCurrentSongImage();
            }

            playerState = mediaPlayerServiceRef.get().getPlayerState();

            return initializeNotification(pauseAction);
        } else
            return null;


    }

    /**
     * Create a notification
     *
     * @param pauseAction <code>true</code> to add pause action in {@link android.app.Notification}, <code>false</code> to add play action
     * @return the {@link android.app.Notification}
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Notification initializeNotification(boolean pauseAction) {
        if (mediaPlayerServiceRef.get() != null) {
            //create intent for notification
            Intent playerIntent = new Intent(mediaPlayerServiceRef.get(), PlayerActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mediaPlayerServiceRef.get());
            stackBuilder.addParentStack(PlayerActivity.class);
            stackBuilder.addNextIntent(playerIntent);
            PendingIntent playerPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            //manage state text
            String stateText;
            switch (playerState) {
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

            //pending intent for stopping player
            Intent stopIntent = new Intent(LocalIntents.PLAYER_STOP.toString());
            PendingIntent stopPendingIntent = PendingIntent.getService(mediaPlayerServiceRef.get(), 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            //pending intent for pausing or playing player
            Intent pausePlayIntent = new Intent(((pauseAction) ? LocalIntents.PLAYER_PAUSE : LocalIntents.PLAYER_PLAY).toString());
            PendingIntent pausePlayPendingIntent = PendingIntent.getService(mediaPlayerServiceRef.get(), 0, pausePlayIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (AppUtils.isAPILevel21Available()) {
                int[] compactViewActions = ((playerState == PlayerActivity.PlayerState.PLAYING) || (playerState == PlayerActivity.PlayerState.PAUSED))
                        ? COMPACT_VIEW_DOUBLE_ACTIONS
                        : COMPACT_VIEW_SINGLE_ACTIONS;

                //create notification
                Notification.Builder notificationBuilder = new Notification.Builder(mediaPlayerServiceRef.get())
                        .setSmallIcon(R.drawable.ic_notif_icon)
                        .setLargeIcon(Bitmap.createBitmap(titleArt)) //avoid recycled image
                        .setTicker(mediaPlayerServiceRef.get().getString(R.string.notification_ticker_text))
                        .setContentTitle(mediaPlayerServiceRef.get().getString(R.string.app_name))
                        .setContentText(currentTitle)
                        .setStyle(new Notification.MediaStyle()
                                .setShowActionsInCompactView(compactViewActions)  // only show play/pause in compact view
                                .setMediaSession(mediaPlayerServiceRef.get().getMediaSession().getSessionToken()))
                        .setContentInfo(stateText)
                        .setContentIntent(playerPendingIntent);

                //add the chronometer
                if (playerState == PlayerActivity.PlayerState.PLAYING) {
                    notificationBuilder.setWhen(System.currentTimeMillis() - mediaPlayerServiceRef.get().getPosition())
                            .setShowWhen(true)
                            .setUsesChronometer(true);
                }

                //don't add play/pause button if buffering
                if (playerState != PlayerActivity.PlayerState.BUFFERING) {
                    notificationBuilder.addAction((pauseAction) ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                            mediaPlayerServiceRef.get().getString((pauseAction) ? R.string.player_pause : R.string.player_play),
                            pausePlayPendingIntent);
                }

                notificationBuilder.addAction(R.drawable.ic_media_stop, mediaPlayerServiceRef.get().getString(R.string.player_stop), stopPendingIntent);
                return notificationBuilder.build();

            } else {
                //create notification
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mediaPlayerServiceRef.get())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(Bitmap.createBitmap(titleArt)) //avoid recycled image
                        .setTicker(mediaPlayerServiceRef.get().getString(R.string.notification_ticker_text))
                        .setContentTitle(mediaPlayerServiceRef.get().getString(R.string.app_name))
                        .setContentText(currentTitle)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(Bitmap.createBitmap(titleArt)) //avoid recycled image
                                .setSummaryText(currentTitle))
                        .setContentInfo(stateText)
                        .setContentIntent(playerPendingIntent);

                //add the chronometer
                if (playerState == PlayerActivity.PlayerState.PLAYING) {
                    notificationBuilder.setWhen(System.currentTimeMillis() - mediaPlayerServiceRef.get().getPosition())
                            .setShowWhen(true)
                            .setUsesChronometer(true);
                }

                //don't add play/pause button if buffering
                if (playerState != PlayerActivity.PlayerState.BUFFERING) {
                    //add proper action (pause or play)
                    notificationBuilder.addAction((pauseAction) ? R.drawable.ic_player_pause : R.drawable.ic_player_play,
                            mediaPlayerServiceRef.get().getString((pauseAction) ? R.string.player_pause : R.string.player_play),
                            pausePlayPendingIntent);
                }

                notificationBuilder.addAction(R.drawable.ic_player_stop, mediaPlayerServiceRef.get().getString(R.string.player_stop), stopPendingIntent);
                return notificationBuilder.build();
            }

        } else
            return null;
    }
}
