package com.stationmillenium.android.libutils.mediaplayer.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.PlayerActivity;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.activities.PlayerState;
import com.stationmillenium.android.libutils.intents.LocalIntents;
import com.stationmillenium.android.services.MediaPlayerService;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Notification builder for media player
 * Created by vincent on 14/12/14.
 */
public class MediaPlayerNotificationBuilder {

    private static final int[] COMPACT_VIEW_DOUBLE_ACTIONS = {0, 1};
    private static final int[] COMPACT_VIEW_SINGLE_ACTIONS = {0};
    public static final String NOTIFICATION_CHANNEL_ID = "channelId";

    private static WeakReference<MediaPlayerService> mediaPlayerServiceRef;

    private MediaController.Callback callback;

    private String currentTitle;
    private Bitmap titleArt;
    private PlayerState playerState;

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
                    Timber.d("New media metadata : %s", metadata.getDescription());

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

                    Notification notification = mediaPlayerServiceRef.get().getMediaPlayerNotificationBuilder().initializeNotification(mediaPlayerServiceRef.get().getPlayerState() == PlayerState.PLAYING);
                    if (mediaPlayerServiceRef.get().getSystemService(Context.NOTIFICATION_SERVICE) != null) {
                        ((NotificationManager) mediaPlayerServiceRef.get().getSystemService(Context.NOTIFICATION_SERVICE)).notify(MediaPlayerService.NOTIFICATION_ID, notification);
                    }
                }

                @Override
                public void onPlaybackStateChanged(@NonNull PlaybackState state) {
                    switch (state.getState()) {
                        case PlaybackState.STATE_PLAYING:
                            Timber.d("Playback change state playing received");
                            setupState(true);
                            break;

                        case PlaybackState.STATE_PAUSED:
                            Timber.d("Playback change state playing received");
                            setupState(false);
                            break;

                        case PlaybackState.STATE_BUFFERING: //no notification to display during buffering
                            Timber.d("Playback change state buffering received");
                            setupState(false);
                            break;
                        case PlaybackState.STATE_CONNECTING:
                            break;
                        case PlaybackState.STATE_ERROR:
                            break;
                        case PlaybackState.STATE_FAST_FORWARDING:
                            break;
                        case PlaybackState.STATE_NONE:
                            break;
                        case PlaybackState.STATE_REWINDING:
                            break;
                        case PlaybackState.STATE_SKIPPING_TO_NEXT:
                            break;
                        case PlaybackState.STATE_SKIPPING_TO_PREVIOUS:
                            break;
                        case PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM:
                            break;
                        case PlaybackState.STATE_STOPPED:
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
        assert mediaPlayerServiceRef.get().getSystemService(Context.NOTIFICATION_SERVICE) != null;
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
                Bitmap localImage = ((mediaPlayerServiceRef.get().getCurrentSongImage() == null)
                        ? BitmapFactory.decodeResource(mediaPlayerServiceRef.get().getResources(), R.drawable.player_default_image)
                        : mediaPlayerServiceRef.get().getCurrentSongImage());
                titleArt = localImage.copy(localImage.getConfig(), false);
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
                return notificationAPI21(pauseAction, playerPendingIntent, stateText, stopPendingIntent, pausePlayPendingIntent);
            } else {
                return notificationOldFashion(pauseAction, playerPendingIntent, stateText, stopPendingIntent, pausePlayPendingIntent);
            }

        } else {
            return null;
        }
    }

    private Notification notificationOldFashion(boolean pauseAction, PendingIntent playerPendingIntent, String stateText, PendingIntent stopPendingIntent, PendingIntent pausePlayPendingIntent) {
        //create notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mediaPlayerServiceRef.get())
                .setSmallIcon(R.drawable.ic_notif_icon)
                .setLargeIcon(titleArt.copy(titleArt.getConfig(), false)) //avoid recycled image
                .setTicker(mediaPlayerServiceRef.get().getString(R.string.notification_ticker_text))
                .setContentTitle(mediaPlayerServiceRef.get().getString(R.string.app_name))
                .setContentText(currentTitle)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(titleArt.copy(titleArt.getConfig(), false)) //avoid recycled image
                        .setSummaryText(currentTitle))
                .setContentInfo(stateText)
                .setContentIntent(playerPendingIntent);

        //add the chronometer
        if (playerState == PlayerState.PLAYING) {
            notificationBuilder.setWhen(System.currentTimeMillis() - mediaPlayerServiceRef.get().getPosition())
                    .setShowWhen(true)
                    .setUsesChronometer(true);
        }

        //don't add play/pause button if buffering
        if (playerState != PlayerState.BUFFERING) {
            //add proper action (pause or play)
            notificationBuilder.addAction((pauseAction) ? R.drawable.pause : R.drawable.play,
                    mediaPlayerServiceRef.get().getString((pauseAction) ? R.string.player_pause : R.string.player_play),
                    pausePlayPendingIntent);
        }

        notificationBuilder.addAction(R.drawable.stop, mediaPlayerServiceRef.get().getString(R.string.player_stop), stopPendingIntent);
        return notificationBuilder.build();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Notification notificationAPI21(boolean pauseAction, PendingIntent playerPendingIntent, String stateText, PendingIntent stopPendingIntent, PendingIntent pausePlayPendingIntent) {
        int[] compactViewActions = ((playerState == PlayerState.PLAYING) || (playerState == PlayerState.PAUSED))
                ? COMPACT_VIEW_DOUBLE_ACTIONS
                : COMPACT_VIEW_SINGLE_ACTIONS;

        //create notification
        Notification.Builder notificationBuilder = getNotificationBuilder()
                .setSmallIcon(R.drawable.ic_notif_icon)
                .setLargeIcon(titleArt.copy(titleArt.getConfig(), false)) //avoid recycled image
                .setTicker(mediaPlayerServiceRef.get().getString(R.string.notification_ticker_text))
                .setContentTitle(mediaPlayerServiceRef.get().getString(R.string.app_name))
                .setContentText(currentTitle)
                .setStyle(new Notification.MediaStyle()
                        .setShowActionsInCompactView(compactViewActions)  // only show play/pause inalarm compact view
                        .setMediaSession(mediaPlayerServiceRef.get().getMediaSession().getSessionToken()))
                .setSubText(stateText)
                .setContentIntent(playerPendingIntent);

        //add the chronometer
        if (playerState == PlayerState.PLAYING) {
            notificationBuilder.setWhen(System.currentTimeMillis() - mediaPlayerServiceRef.get().getPosition())
                    .setShowWhen(true)
                    .setUsesChronometer(true);
        }

        //don't add play/pause button if buffering
        if (playerState != PlayerState.BUFFERING) {
            notificationBuilder.addAction(new Notification.Action.Builder((pauseAction) ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                    mediaPlayerServiceRef.get().getString((pauseAction) ? R.string.player_pause : R.string.player_play),
                    pausePlayPendingIntent).build());
        }

        notificationBuilder.addAction(new Notification.Action.Builder(R.drawable.ic_media_stop, mediaPlayerServiceRef.get().getString(R.string.player_stop), stopPendingIntent).build());
        return notificationBuilder.build();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private Notification.Builder getNotificationBuilder() {
        if (AppUtils.isAPILevel26Available()) {
            NotificationManager notificationManager = (NotificationManager) mediaPlayerServiceRef.get().getSystemService(Context.NOTIFICATION_SERVICE);
            assert notificationManager != null;
            if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, mediaPlayerServiceRef.get().getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
                notificationManager.createNotificationChannel(channel);
            }
            return new Notification.Builder(mediaPlayerServiceRef.get(), NOTIFICATION_CHANNEL_ID);
        } else {
            return  new Notification.Builder(mediaPlayerServiceRef.get());
        }
    }
}
