/**
 *
 */
package com.stationmillenium.android.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.fragments.PlayerFragment;
import com.stationmillenium.android.databinding.PlayerActivityBinding;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.libutils.activities.PlayerActivityUpdateTitleBroadcastReceiver;
import com.stationmillenium.android.libutils.activities.PlayerState;
import com.stationmillenium.android.libutils.drawer.DrawerUtils;
import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO;
import com.stationmillenium.android.libutils.intents.LocalIntents;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;
import com.stationmillenium.android.libutils.mediaplayer.utils.MediaPlayerCurrentPositionGrabber;
import com.stationmillenium.android.services.MediaPlayerService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.PLAYER;
import static com.stationmillenium.android.libutils.SharedPreferencesConstants.AUTOSTART_RADIO;
import static com.stationmillenium.android.libutils.activities.PlayerState.STOPPED;

/**
 * Activity to display the player
 *
 * @author vincent
 */
public class PlayerActivity extends AppCompatActivity {

    //static intialization part
    private static final String TAG = "PlayerActivity";
    private static final int CURRENT_TIME_TIMER_START = 0;
    private static final int CURRENT_TIME_TIMER_UPDATE = 1000;
    private static final String CURRENT_TIME_TIMER_NAME = "CurrentTimeTimer";
    private static final int REFRESH_TIMEOUT = 1;

    private DrawerUtils drawerUtils;
    private PlayerFragment playerFragment;
    private PlayerActivityBinding preferencesActivityBinding;

    private Timer currentPlayingTimeTimer;
    private Calendar lastTimeUpdated;
    private PlayerActivityUpdateTitleBroadcastReceiver playerActivityUpdateTitleBroadcastReceiver;

    private MenuItem castMenu;
    private CastStateListener castStateListener = new CastStateListener() {
        @Override
        public void onCastStateChanged(int newState) {
            if (newState != CastState.NO_DEVICES_AVAILABLE) {
                showIntroductoryOverlay();
            }
        }
    };
    private CastContext castContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //init view
        super.onCreate(savedInstanceState);
        preferencesActivityBinding = DataBindingUtil.setContentView(this, R.layout.player_activity);
        setSupportActionBar(preferencesActivityBinding.playerToolbar);

        drawerUtils = new DrawerUtils(this, preferencesActivityBinding.playerDrawerLayout, preferencesActivityBinding.playerToolbar, R.id.nav_drawer_player);
        playerFragment = (PlayerFragment) getSupportFragmentManager().findFragmentById(R.id.player_fragment);

        //set the volume stream will be controlled but pressing buttons
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Cast init : https://developers.google.com/cast/docs/android_sender_integrate#initialize_the_cast_context
        castContext = CastContext.getSharedInstance(this);
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

        // cast part
        castContext.removeCastStateListener(castStateListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerUtils.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerUtils.onPostCreate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerUtils.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Resume player activity");
        }
        super.onResume();

        //record the update title broadcast receiver
        if (playerActivityUpdateTitleBroadcastReceiver == null) {
            playerActivityUpdateTitleBroadcastReceiver = new PlayerActivityUpdateTitleBroadcastReceiver(this);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(playerActivityUpdateTitleBroadcastReceiver, new IntentFilter(LocalIntents.CURRENT_TITLE_UPDATED.toString()));

        //if the player is not running
        if (!AppUtils.isMediaPlayerServiceRunning(getApplicationContext())) {
            //auto start player
            if ((getIntent() != null) && (getIntent().getBooleanExtra(LocalIntentsData.ALLOW_AUTOSTART.toString(), false))) {
                if (getDefaultSharedPreferences(this).getBoolean(AUTOSTART_RADIO, false)) {
                    startPlayer();
                }
                getIntent().removeExtra(LocalIntentsData.ALLOW_AUTOSTART.toString());
            }

            playerFragment.setPlayerState(PlayerState.STOPPED); //be sure we are in stopped state
        } else {
            askForRefresh(); //check if need some fresh data
        }

        // cast part
        castContext.addCastStateListener(castStateListener);

        // adapt player states
        managePlayerStates();
        PiwikTracker.trackScreenView(PLAYER);
    }

    /**
     * Manage the player states
     */
    private void managePlayerStates() {
        if (playerFragment.getPlayerState() != null) {
            switch (playerFragment.getPlayerState()) {
                case BUFFERING:
                case PAUSED:
                case STOPPED:
                    cancelCurrentTimeTimer();
                    break;

                case PLAYING:
                    launchCurrentTimeTimer();
                    break;
            }
        } else {
            playerFragment.setPlayerState(STOPPED);
        }
    }

    /**
     * Force the stopped state for reinit
     */
    public void forceStopState() {
        playerFragment.setPlayerState(STOPPED);
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
            playerFragment.setPlayerState(PlayerState.PAUSED);

        } else if (PlayerState.PLAYING.getAssociatedIntent().toString().equals(intent.getAction())) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Playing player");
            }
            playerFragment.setPlayerState(PlayerState.PLAYING);

        } else if (PlayerState.STOPPED.getAssociatedIntent().toString().equals(intent.getAction())) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Stop player");
            }
            playerFragment.setPlayerState(PlayerState.STOPPED);

        } else if (PlayerState.BUFFERING.getAssociatedIntent().toString().equals(intent.getAction())) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Buffering player");
            }
            playerFragment.setPlayerState(PlayerState.BUFFERING);

        } else if (LocalIntents.ON_PLAYER_OPEN.toString().equals(intent.getAction())) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Open player with data");
            }
            playerActivityUpdateTitleBroadcastReceiver.onReceive(this, intent);
            playerFragment.setPlayerState((PlayerState) intent.getSerializableExtra(LocalIntentsData.CURRENT_STATE.toString()));
        }
    }

    /**
     * Start the player
     *
     */
    public void startPlayer() {
        //start player service
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Play player button clicked");
        }
        if (playerFragment.getPlayerState() == STOPPED) {
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
                    Snackbar.make(preferencesActivityBinding.playerCoordinatorLayout, R.string.player_no_wifi, Snackbar.LENGTH_SHORT).show();
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
                final int minutesPlaying = playingTimeSecond / 60;
                playingTimeSecond %= 60;

                //update in ui thread
                final int finalPlayingTimeSecond = playingTimeSecond;
                runOnUiThread(() -> playerFragment.setPlayingTime((minutesPlaying < 10) ? "0" + minutesPlaying : String.valueOf(minutesPlaying),
                        (finalPlayingTimeSecond < 10) ? "0" + finalPlayingTimeSecond : String.valueOf(finalPlayingTimeSecond)));
            }

        }, CURRENT_TIME_TIMER_START, CURRENT_TIME_TIMER_UPDATE);
    }

    /**
     * Cancel the current time timer
     */
    private void cancelCurrentTimeTimer() {
        //stop current time timer
        if (currentPlayingTimeTimer != null) {
            currentPlayingTimeTimer.cancel();
            playerFragment.setPlayingTime("", "");
        }
    }

    /**
     * Stop the player
     *
     */
    public void stopPlayer() {
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
     */
    public void pausePlayer() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Pause player button clicked");
        }
        Intent pausePlayerIntent = new Intent(LocalIntents.PLAYER_PAUSE.toString());
        pausePlayerIntent.setClass(this, MediaPlayerService.class);
        startService(pausePlayerIntent);
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

    public void setSongData(CurrentTitleDTO songData) {
        if (songData != null) {
            playerFragment.setSongData(songData.getCurrentSong());

            //update the history view
            List<String> historyTextList = new ArrayList<>();
            for (CurrentTitleDTO.Song historySong : songData.getHistory()) {
                String historyText = getResources().getString(R.string.player_current_title, historySong.getArtist(), historySong.getTitle());
                historyTextList.add(historyText);
            }
            playerFragment.setHistoryList(historyTextList);
        } else {
            playerFragment.setSongData(null);
            playerFragment.setHistoryList(null);
        }
    }

    // https://developers.google.com/cast/docs/android_sender_integrate#add_a_cast_button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.cast_menu, menu);
        castMenu = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                menu,
                R.id.cast_menu);
        return true;
    }

    private void showIntroductoryOverlay() {
        if ((castMenu != null) && castMenu.isVisible()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    new IntroductoryOverlay.Builder(
                            PlayerActivity.this, castMenu)
                            .setTitleText(R.string.introducing_cast)
                            .setSingleTime()
                            .build()
                            .show();
                }
            });
        }
    }
}
