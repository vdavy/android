/**
 *
 */
package com.stationmillenium.android.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.libutils.activities.PlayerActivityUpdateTitleBroadcastReceiver;
import com.stationmillenium.android.libutils.activities.PlayerState;
import com.stationmillenium.android.libutils.intents.LocalIntents;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;
import com.stationmillenium.android.libutils.mediaplayer.utils.MediaPlayerCurrentPositionGrabber;
import com.stationmillenium.android.services.MediaPlayerService;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.stationmillenium.android.activities.preferences.SharedPreferencesActivity.SharedPreferencesConstants.AUTOSTART_RADIO;
import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.PLAYER;

/**
 * Activity to display the player
 *
 * @author vincent
 */
public class PlayerActivity extends AppCompatActivity {


    //static part
    private static final String TAG = "PlayerActivity";
    private static final String CURRENT_TITLE_SAVE = "CURRENT_TITLE_SAVE";
    private static final String CURRENT_TIME_SAVE = "CURRENT_TIME_SAVE";
    private static final String PLAYER_STATE_SAVE = "PLAYER_STATE_SAVE";
    private static final String HISTORY_LIST_SAVE = "HISTORY_LIST_SAVE";
    private static final String IMAGE_SAVE = "IMAGE_SAVE";
    private static final int REFRESH_TIMEOUT = 1;
    private static final int CURRENT_TIME_TIMER_START = 0;
    private static final int CURRENT_TIME_TIMER_UPDATE = 1000;
    private static final String CURRENT_TIME_TIMER_NAME = "CurrentTimeTimer";

    //instances vars
    private PlayerActivityUpdateTitleBroadcastReceiver playerActivityUpdateTitleBroadcastReceiver;
    private PlayerState playerState = PlayerState.STOPPED;
    private String[] historyListValues;
    private Bitmap currentTitleImage;
    private Calendar lastTimeUpdated;
    private Timer currentPlayingTimeTimer;
    private GoogleApiClient googleApiClient;

    //widgets
    private ImageSwitcher imageSwitcher;
    private ListView historyList;
    private TextView currentTitleTextView;
    private ImageButton playButton;
    private ImageButton pauseButton;
    private ImageButton stopButton;
    private ProgressBar progressBar;
    private TextView playerLoadingTextView;
    private TextView currentTimeTextView;


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Create the activity");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);

        //set up action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get widgets
        //image switcher
        imageSwitcher = (ImageSwitcher) findViewById(R.id.player_image_switcher);
        final Context context = this;
        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
             @Override
             public View makeView() {
                 ImageView imageView = new ImageView(context);
                 imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                 imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                 imageView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                 return imageView;
             }
         });


        //current title
        historyList = (ListView) findViewById(R.id.player_history_list);
        currentTitleTextView = (TextView) findViewById(R.id.current_title_text);

        //control widgets
        playButton = (ImageButton) findViewById(R.id.play_button);
        pauseButton = (ImageButton) findViewById(R.id.pause_button);
        stopButton = (ImageButton) findViewById(R.id.stop_button);
        progressBar = (ProgressBar) findViewById(R.id.player_progress_bar);
        playerLoadingTextView = (TextView) findViewById(R.id.player_loading);
        currentTimeTextView = (TextView) findViewById(R.id.player_current_time);

        //set the volume stream will be controlled but pressing buttons
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        PiwikTracker.trackScreenView(PLAYER);
        googleApiClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        AppIndex.AppIndexApi.start(googleApiClient, getAction()).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d(TAG, "App Indexing API - START : Player view recorded successfully.");
                } else {
                    Log.e(TAG, "App Indexing API - START : There was an error recording the player view :"+ status.toString());
                }
            }
        });
    }

    /**
     * Get the Action for app indexing
     * @return the Action
     */
    @NonNull
    private Action getAction() {
        return new Action.Builder(Action.TYPE_LISTEN)
                .setObject(new Thing.Builder()
                        .setName(getString(R.string.player_name))
                        .setDescription(getString(R.string.player_description))
                        .setUrl(Uri.parse(getString(R.string.player_url)))
                        .build())
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    protected void onResume() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Resume player activity");
        }
        super.onResume();
        if (currentTitleImage != null) {
            imageSwitcher.setImageDrawable(new BitmapDrawable(getResources(), currentTitleImage));
        } else {
            imageSwitcher.setImageResource(R.drawable.player_default_image);
        }

        //record the update title broadcast receiver
        if (playerActivityUpdateTitleBroadcastReceiver == null) {
            playerActivityUpdateTitleBroadcastReceiver = new PlayerActivityUpdateTitleBroadcastReceiver(this);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(playerActivityUpdateTitleBroadcastReceiver, new IntentFilter(LocalIntents.CURRENT_TITLE_UPDATED.toString()));

        //restore previous data for list
        if (historyListValues != null) {
            historyList.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.player_history_list_item, R.id.player_history_item_text, historyListValues));
        } else {
            historyList.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.player_history_list_item, R.id.player_history_item_text, new String[]{"", "", "", "", ""}));
        }

        //if the player is not running
        if (!AppUtils.isMediaPlayerServiceRunning(getApplicationContext())) {
            //auto start player
            if ((getIntent() != null) && (getIntent().getBooleanExtra(LocalIntentsData.ALLOW_AUTOSTART.toString(), false))) {
                if (getDefaultSharedPreferences(this).getBoolean(AUTOSTART_RADIO, false)) {
                    startPlayer(null);
                }
                getIntent().removeExtra(LocalIntentsData.ALLOW_AUTOSTART.toString());
            }

            playerState = PlayerState.STOPPED; //be sure we are in stopped state
        } else
            askForRefresh(); //check if need some fresh data

        //adapt player states
        managePlayerStates();
    }

    /**
     * Manage the player states
     */
    private void managePlayerStates() {
        if (playerState != null) {
            switch (playerState) {
                case BUFFERING:
                    playerBuffering();
                    cancelCurrentTimeTimer();
                    break;

                case PAUSED:
                    playerPaused();
                    cancelCurrentTimeTimer();
                    break;

                case PLAYING:
                    playerPlaying();
                    launchCurrentTimeTimer();
                    break;

                case STOPPED:
                    playerStopped();
                    cancelCurrentTimeTimer();
                    break;
            }
        } else
            playerStopped();
    }

    /**
     * Launch the current time timer
     */
    private void launchCurrentTimeTimer() {
        currentPlayingTimeTimer = new Timer(CURRENT_TIME_TIMER_NAME);
        currentPlayingTimeTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                //compute time
                int currentPosition = MediaPlayerCurrentPositionGrabber.getMediaPlayerCurrentPosition();
                int playingTimeSecond = currentPosition / 1000;
                int minutesPlaying = playingTimeSecond / 60;
                playingTimeSecond %= 60;
                final String currentTimeText = getResources().getString(R.string.player_notification_time,
                        (minutesPlaying < 10) ? "0" + minutesPlaying : minutesPlaying,
                        (playingTimeSecond < 10) ? "0" + playingTimeSecond : playingTimeSecond);

                //update in ui thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currentTimeTextView.setText(currentTimeText);
                    }
                });
            }

        }, CURRENT_TIME_TIMER_START, CURRENT_TIME_TIMER_UPDATE);
    }

    @Override
    protected void onPause() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Pausing player activity");
        }
        super.onPause();

        //cancel update title broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playerActivityUpdateTitleBroadcastReceiver);

        //send intent to service that activity is paused
        if (AppUtils.isMediaPlayerServiceRunning(getApplicationContext())) {
            Intent mediaPlayerIntent = new Intent(this, MediaPlayerService.class);
            mediaPlayerIntent.setAction(LocalIntents.PLAYER_ACTIVITY_PAUSE.toString());
            startService(mediaPlayerIntent);
        }

        cancelCurrentTimeTimer();

        //save the pause time
        lastTimeUpdated = Calendar.getInstance();
    }

    /**
     * Cancel the current time timer
     */
    private void cancelCurrentTimeTimer() {
        //stop current time timer
        if (currentPlayingTimeTimer != null) {
            currentPlayingTimeTimer.cancel();
            currentTimeTextView.setText("");
        }
    }

    /**
     * Ask for a data refresh if data are too old
     */
    private void askForRefresh() {
        ///check if a refresh is needed
        Calendar newDate = Calendar.getInstance();
        newDate.add(Calendar.SECOND, -REFRESH_TIMEOUT);
        if ((lastTimeUpdated == null) || (newDate.after(lastTimeUpdated))) { //it's time for a refresh
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Data refresh requested...");
            }
            Intent mediaPlayerIntent = new Intent(this, MediaPlayerService.class);
            mediaPlayerIntent.setAction(LocalIntents.PLAYER_OPEN.toString());
            startService(mediaPlayerIntent);
        } else { //if not refresh needed
            //send intent to service that activity is resumed
            Intent mediaPlayerIntent = new Intent(this, MediaPlayerService.class);
            mediaPlayerIntent.setAction(LocalIntents.PLAYER_ACTIVITY_RESUME.toString());
            startService(mediaPlayerIntent);
        }
    }

    /**
     * Start the player
     *
     * @param view the view which triggered event - not used
     */
    public void startPlayer(View view) {
        //start player service
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Play player button clicked");
        }
        if (playerState == PlayerState.STOPPED) {
            if (!AppUtils.isMediaPlayerServiceRunning(this)) {
                if (!AppUtils.isWifiOnlyAndWifiNotConnected(this)) {
                    //start player service
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Start media player service");
                    }
                    Intent mediaPlayerIntent = new Intent(this, MediaPlayerService.class);
                    startService(mediaPlayerIntent);

                } else {
                    Log.w(TAG, "Wifi requested for streaming radio, but not connected");
                    Toast.makeText(this, R.string.player_no_wifi, Toast.LENGTH_SHORT).show();
                }

            } else if (BuildConfig.DEBUG) {
                Log.d(TAG, "Media player service already started");
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Play the player");
            }
            Intent playPlayerIntent = new Intent(this, MediaPlayerService.class);
            playPlayerIntent.setAction(LocalIntents.PLAYER_PLAY.toString());
            startService(playPlayerIntent);
        }
    }

    /**
     * Buffering state of the player
     */
    private void playerBuffering() {
        progressBar.setVisibility(View.VISIBLE);
        playerLoadingTextView.setVisibility(View.VISIBLE);
        currentTimeTextView.setVisibility(View.GONE);
        playButton.setVisibility(View.GONE);
        pauseButton.setClickable(false);
        pauseButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.VISIBLE);
    }

    /**
     * Player playing
     */
    private void playerPlaying() {
        progressBar.setVisibility(View.GONE);
        playerLoadingTextView.setVisibility(View.GONE);
        currentTimeTextView.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.VISIBLE);
        pauseButton.setClickable(true);
        pauseButton.setVisibility(View.VISIBLE);
        playButton.setVisibility(View.GONE);
    }

    /**
     * Stop the player
     *
     * @param view the view which triggered event
     */
    public void stopPlayer(View view) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Stop player button clicked");
        }
        Intent stopPlayerIntent = new Intent(LocalIntents.PLAYER_STOP.toString());
        stopPlayerIntent.setClass(this, MediaPlayerService.class);
        startService(stopPlayerIntent);
    }

    /**
     * Pause the player
     *
     * @param view the view which triggered event
     */
    public void pausePlayer(View view) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Pause player button clicked");
        }
        Intent pausePlayerIntent = new Intent(LocalIntents.PLAYER_PAUSE.toString());
        pausePlayerIntent.setClass(this, MediaPlayerService.class);
        startService(pausePlayerIntent);
    }

    /**
     * Set widgets when player is stopped
     */
    public void playerStopped() {
        //player has been stopped - reinit views
        imageSwitcher.setImageResource(R.drawable.player_default_image);
        currentTitleTextView.setText("");
        historyList.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.player_history_list_item, R.id.player_history_item_text, new String[]{"", "", "", "", ""}));
        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        playerLoadingTextView.setVisibility(View.GONE);
        currentTimeTextView.setVisibility(View.GONE);

        //reinit local vars too
        historyListValues = null;
        currentTitleImage = null;
    }

    /**
     * Set widgets when player is paused
     */
    private void playerPaused() {
        //player is pause - init widgets
        pauseButton.setVisibility(View.GONE);
        playButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Player state intent received : " + intent);
        }
        if (PlayerState.PAUSED.getAssociatedIntent().toString().equals(intent.getAction())) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Pause player");
            }
            playerState = PlayerState.PAUSED;

        } else if (PlayerState.PLAYING.getAssociatedIntent().toString().equals(intent.getAction())) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Playing player");
            }
            playerState = PlayerState.PLAYING;

        } else if (PlayerState.STOPPED.getAssociatedIntent().toString().equals(intent.getAction())) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Stop player");
            }
            playerState = PlayerState.STOPPED;

        } else if (PlayerState.BUFFERING.getAssociatedIntent().toString().equals(intent.getAction())) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Buffering player");
            }
            playerState = PlayerState.BUFFERING;

        } else if (LocalIntents.ON_PLAYER_OPEN.toString().equals(intent.getAction())) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Open player with data");
            }
            playerActivityUpdateTitleBroadcastReceiver.onReceive(getApplicationContext(), intent);
            playerState = (PlayerState) intent.getSerializableExtra(LocalIntentsData.CURRENT_STATE.toString());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Save activity values...");
        }
        outState.putCharSequence(CURRENT_TITLE_SAVE, currentTitleTextView.getText());
        outState.putCharSequence(CURRENT_TIME_SAVE, currentTimeTextView.getText());
        outState.putSerializable(PLAYER_STATE_SAVE, playerState);
        if (historyListValues != null) {
            outState.putStringArray(HISTORY_LIST_SAVE, historyListValues);
        }
        outState.putParcelable(IMAGE_SAVE, currentTitleImage);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        currentTitleTextView.setText(savedInstanceState.getCharSequence(CURRENT_TITLE_SAVE));
        currentTimeTextView.setText(savedInstanceState.getCharSequence(CURRENT_TIME_SAVE));
        playerState = (PlayerState) savedInstanceState.getSerializable(PLAYER_STATE_SAVE);
        historyListValues = savedInstanceState.getStringArray(HISTORY_LIST_SAVE);
        currentTitleImage = savedInstanceState.getParcelable(IMAGE_SAVE);
    }

    @Override
    public void onStop() {
        AppIndex.AppIndexApi.end(googleApiClient, getAction()).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d(TAG, "App Indexing API - END : Player view recorded successfully.");
                } else {
                    Log.e(TAG, "App Indexing API - END : There was an error recording the player view :"+ status.toString());
                }
            }
        });
        googleApiClient.disconnect();
        super.onStop();
    }

    public void setImageSwitcherDrawable(GlideBitmapDrawable drawable) {
        imageSwitcher.setImageDrawable(drawable);
        currentTitleImage = drawable.getBitmap();
    }

    public void setImageSwitcherDrawable(BitmapDrawable drawable) {
        imageSwitcher.setImageDrawable(drawable);
        currentTitleImage = drawable.getBitmap();
    }

    public void setImageSwitcherResource(@DrawableRes int res) {
        imageSwitcher.setImageResource(res);
    }

    public void setCurrentTitleTextView(String titleTextView) {
        currentTitleTextView.setText(titleTextView);
    }

    /**
     * Set and backup the history list data
     * @param values the data
     */
    public void setHistoryListValues(List<String> values) {
        historyList.setAdapter(new ArrayAdapter<>(this, R.layout.player_history_list_item, R.id.player_history_item_text, values));
        historyListValues = new String[values.size()];
        values.toArray(historyListValues);
    }
}
