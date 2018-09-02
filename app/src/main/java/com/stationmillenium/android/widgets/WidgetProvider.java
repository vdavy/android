/**
 *
 */
package com.stationmillenium.android.widgets;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.HomeActivity;
import com.stationmillenium.android.activities.PlayerActivity;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.activities.PlayerState;
import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO;
import com.stationmillenium.android.libutils.intents.LocalIntents;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;
import com.stationmillenium.android.services.MediaPlayerService;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import timber.log.Timber;

/**
 * Class to manage the widget
 *
 * @author vincent
 */
public class WidgetProvider extends AppWidgetProvider {

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Timber.d("Widget updating...");
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        //play pending intent
        Intent playIntent = new Intent(context, MediaPlayerService.class);
        playIntent.putExtra(LocalIntentsData.RESUME_PLAYER_ACTIVITY.toString(), false);
        PendingIntent playPendingIntent = AppUtils.isAPILevel26Available()
                ? PendingIntent.getForegroundService(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                : PendingIntent.getService(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_play_button, playPendingIntent);

        //pause pending intent
        Intent pauseIntent = new Intent(context, MediaPlayerService.class);
        pauseIntent.setAction(LocalIntents.PLAYER_PAUSE.toString());
        PendingIntent pausePendingIntent = PendingIntent.getService(context, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_pause_button, pausePendingIntent);

        //stop pending intent
        Intent stopIntent = new Intent(context, MediaPlayerService.class);
        stopIntent.setAction(LocalIntents.PLAYER_STOP.toString());
        PendingIntent stopPendingIntent = PendingIntent.getService(context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_stop_button, stopPendingIntent);

        //set global pending intent
        setMainWidgetPartsPendingIntent(context, remoteViews, HomeActivity.class);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onReceive(@NonNull final Context context, @NonNull Intent intent) {
        Timber.d("Widget receiving intent : %s", intent);
        super.onReceive(context, intent);
        if (LocalIntents.CURRENT_TITLE_UPDATED.toString().equals(intent.getAction())) {

            if (AppUtils.isMediaPlayerServiceRunning(context)) { //check if media player service is running to apply data
                Timber.d("Media player service running - applying received data...");
                CurrentTitleDTO songData = (CurrentTitleDTO) intent.getExtras().get(LocalIntentsData.CURRENT_TITLE.toString());

                if (songData != null) {
                    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

                    //update current title
                    if ((songData.getCurrentSong().getArtist() != null) && (songData.getCurrentSong().getTitle() != null)) {
                        remoteViews.setTextViewText(R.id.widget_artist_text, songData.getCurrentSong().getArtist());
                        remoteViews.setTextViewText(R.id.widget_title_text, songData.getCurrentSong().getTitle());
                    } else {
                        remoteViews.setTextViewText(R.id.widget_artist_text, context.getResources().getString(R.string.player_no_title));
                        remoteViews.setTextViewText(R.id.widget_title_text, "");
                    }

                    //update image
                    Glide.with(context)
                        .asBitmap()
                        .load(songData.getCurrentSong().getImageURL())
                        .apply(new RequestOptions()
                            .placeholder(R.drawable.player_default_image)
                            .error(R.drawable.player_default_image)
                            .centerCrop()
                            .transform(new RoundedCornersTransformation(context.getResources().getDimensionPixelSize(R.dimen.widget_rounded_image), 0)))
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                updateWidgetData(resource);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                if (errorDrawable instanceof BitmapDrawable) {
                                    Bitmap errorBitmap = ((BitmapDrawable) errorDrawable).getBitmap();
                                    updateWidgetData(errorBitmap);
                                }
                            }

                            private void updateWidgetData(Bitmap resource) {
                                RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                                remoteView.setImageViewBitmap(R.id.widget_image, resource);

                                //update the widget
                                updateWidget(context, remoteView);
                            }
                        });

                    updateWidget(context, remoteViews);
                }

            } else {
                Timber.d("Media player service not running - reseting widgets...");
                updateWidgetsStates(context, PlayerState.STOPPED); //reset the widget
            }

        } else if (PlayerState.PLAYING.getAssociatedIntent().toString().equals(intent.getAction())) {
            updateWidgetsStates(context, PlayerState.PLAYING);
        } else if (PlayerState.PAUSED.getAssociatedIntent().toString().equals(intent.getAction())) {
            updateWidgetsStates(context, PlayerState.PAUSED);
        } else if (PlayerState.STOPPED.getAssociatedIntent().toString().equals(intent.getAction())) {
            updateWidgetsStates(context, PlayerState.STOPPED);
        } else if (PlayerState.BUFFERING.getAssociatedIntent().toString().equals(intent.getAction())) {
            updateWidgetsStates(context, PlayerState.BUFFERING);
        }
    }

    /**
     * Update the buttons visibility states
     *
     * @param context     the {@link Context}
     * @param playerState the player state
     */
    @TargetApi(Build.VERSION_CODES.O)
    private void updateWidgetsStates(Context context, PlayerState playerState) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        //set buttons display
        boolean playButtonVisible = false;
        boolean pauseButtonVisible = false;
        boolean stopButtonVisible = false;
        switch (playerState) {
            case PLAYING:
                playButtonVisible = false;
                pauseButtonVisible = true;
                stopButtonVisible = true;

                //set global pending intent
                setMainWidgetPartsPendingIntent(context, remoteViews, PlayerActivity.class);
                break;

            case BUFFERING:
                //buttons display
                playButtonVisible = false;
                pauseButtonVisible = false;
                stopButtonVisible = true;

                //text view loading text
                remoteViews.setTextViewText(R.id.widget_artist_text, context.getResources().getString(R.string.player_widget_loading));
                remoteViews.setTextViewText(R.id.widget_title_text, "");

                //set global pending intent
                setMainWidgetPartsPendingIntent(context, remoteViews, PlayerActivity.class);
                break;

            case PAUSED:
                //buttons display
                playButtonVisible = true;
                pauseButtonVisible = false;
                stopButtonVisible = true;

                //update play pending intent
                Intent pauseIntent = new Intent(context, MediaPlayerService.class);
                pauseIntent.setAction(LocalIntents.PLAYER_PLAY.toString());
                PendingIntent pausePendingIntent = PendingIntent.getService(context, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widget_play_button, pausePendingIntent);

                //set global pending intent
                setMainWidgetPartsPendingIntent(context, remoteViews, PlayerActivity.class);
                break;

            case STOPPED:
                playButtonVisible = true;
                pauseButtonVisible = false;
                stopButtonVisible = false;

                //update play pending intent
                Intent playIntent = new Intent(context, MediaPlayerService.class);
                playIntent.putExtra(LocalIntentsData.RESUME_PLAYER_ACTIVITY.toString(), false);
                PendingIntent playPendingIntent = AppUtils.isAPILevel26Available()
                        ? PendingIntent.getForegroundService(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                        : PendingIntent.getService(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widget_play_button, playPendingIntent);

                //set global pending intent
                setMainWidgetPartsPendingIntent(context, remoteViews, HomeActivity.class);

                //clear widget
                remoteViews.setImageViewResource(R.id.widget_image, R.drawable.player_default_image);
                remoteViews.setTextViewText(R.id.widget_artist_text, context.getResources().getString(R.string.player_widget_title));
                remoteViews.setTextViewText(R.id.widget_title_text, "");
                break;
        }

        //update buttons
        remoteViews.setViewVisibility(R.id.widget_play_button, (playButtonVisible) ? View.VISIBLE : View.GONE);
        remoteViews.setViewVisibility(R.id.widget_pause_button, (pauseButtonVisible) ? View.VISIBLE : View.GONE);
        remoteViews.setViewVisibility(R.id.widget_stop_button, (stopButtonVisible) ? View.VISIBLE : View.GONE);

        updateWidget(context, remoteViews);
    }

    /**
     * Set the {@link PendingIntent} on the main widget part
     *
     * @param context       the {@link Context}
     * @param remoteViews   the {@link RemoteViews} for setting {@link PendingIntent}
     * @param activityClazz the {@link Activity} class
     */
    private <T extends Activity> void setMainWidgetPartsPendingIntent(Context context, RemoteViews remoteViews, Class<T> activityClazz) {
        //main widget parts pending intent
        Intent mainActivityIntent = new Intent(context, activityClazz);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_image, mainActivityPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.widget_text_layout, mainActivityPendingIntent);
    }

    /**
     * Update the widget
     *
     * @param context     the context
     * @param remoteViews the {@link RemoteViews}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void updateWidget(Context context, RemoteViews remoteViews) {
        //update the current widget
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, WidgetProvider.class);
        awm.partiallyUpdateAppWidget(awm.getAppWidgetIds(componentName), remoteViews);
    }

}
