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
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.MainActivity;
import com.stationmillenium.android.activities.PlayerActivity;
import com.stationmillenium.android.activities.PlayerActivity.PlayerState;
import com.stationmillenium.android.dto.CurrentTitleDTO;
import com.stationmillenium.android.services.MediaPlayerService;
import com.stationmillenium.android.utils.AppUtils;
import com.stationmillenium.android.utils.intents.LocalIntents;
import com.stationmillenium.android.utils.intents.LocalIntentsData;

import java.io.File;

/**
 * Class to manage the widget
 *
 * @author vincent
 */
public class WidgetProvider extends AppWidgetProvider {

    //main static fields
    private static final String TAG = "WidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Widget updating...");
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        //play pending intent
        Intent playIntent = new Intent(context, MediaPlayerService.class);
        playIntent.putExtra(LocalIntentsData.RESUME_PLAYER_ACTIVITY.toString(), false);
        PendingIntent playPendingIntent = PendingIntent.getService(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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
        setMainWidgetPartsPendingIntent(context, remoteViews, MainActivity.class);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Widget receiving intent : " + intent);
        super.onReceive(context, intent);
        if (LocalIntents.CURRENT_TITLE_UPDATED.toString().equals(intent.getAction())) {

            if (AppUtils.isMediaPlayerServiceRunning(context)) { //check if media player service is running to apply data
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "Media player service running - applying received data...");

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
                    if (songData.getCurrentSong().getImage() != null) {
                        new WidgetAsyncImageLoader(context).execute(songData.getCurrentSong().getImage());
                    } else
                        remoteViews.setImageViewResource(R.id.widget_image, R.drawable.player_default_image);

                    updateWidget(context, remoteViews);
                }

            } else {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "Media player service not running - reseting widgets...");

                updateWidgetsStates(context, PlayerState.STOPPED); //reset the widget
            }

        } else if (PlayerState.PLAYING.getAssociatedIntent().toString().equals(intent.getAction())) {
            updateWidgetsStates(context, PlayerState.PLAYING);
        } else if (PlayerState.PAUSED.getAssociatedIntent().toString().equals(intent.getAction()))
            updateWidgetsStates(context, PlayerState.PAUSED);
        else if (PlayerState.STOPPED.getAssociatedIntent().toString().equals(intent.getAction()))
            updateWidgetsStates(context, PlayerState.STOPPED);
        else if (PlayerState.BUFFERING.getAssociatedIntent().toString().equals(intent.getAction()))
            updateWidgetsStates(context, PlayerState.BUFFERING);
    }

    /**
     * Update the buttons visibility states
     *
     * @param context     the {@link Context}
     * @param playerState the player state
     */
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
                PendingIntent playPendingIntent = PendingIntent.getService(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widget_play_button, playPendingIntent);

                //set global pending intent
                setMainWidgetPartsPendingIntent(context, remoteViews, MainActivity.class);

                //clear widget
                remoteViews.setImageViewResource(R.id.widget_image, R.drawable.player_default_image);
                remoteViews.setTextViewText(R.id.widget_artist_text, "");
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
        if (AppUtils.isAPILevel11Available())
            awm.partiallyUpdateAppWidget(awm.getAppWidgetIds(componentName), remoteViews);
        else
            awm.updateAppWidget(awm.getAppWidgetIds(componentName), remoteViews);
    }

    /**
     * Async loader for title image
     *
     * @author vincent
     */
    private class WidgetAsyncImageLoader extends AsyncTask<File, Void, Bitmap> {

        private static final String TAG = "WidgetAsyncImageLoader";

        private Context context;

        /**
         * Create a {@link WidgetAsyncImageLoader}
         *
         * @param context the {@link Context}
         */
        public WidgetAsyncImageLoader(Context context) {
            this.context = context;
        }

        @Override
        protected Bitmap doInBackground(File... params) {
            return BitmapFactory.decodeFile(params[0].getAbsolutePath());
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "Update image view");
                RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                remoteView.setImageViewBitmap(R.id.widget_image, result);

                //update the widget
                updateWidget(context, remoteView);
            }
        }

    }

}
