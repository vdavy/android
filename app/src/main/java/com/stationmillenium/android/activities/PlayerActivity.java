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
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.fragments.PlayerFragment;
import com.stationmillenium.android.databinding.PlayerActivityBinding;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.libutils.activities.PlayerActivityUpdateTitleBroadcastReceiver;
import com.stationmillenium.android.libutils.activities.PlayerState;
import com.stationmillenium.android.libutils.cast.ActivityCastUtils;
import com.stationmillenium.android.libutils.cast.ActivitySessionManagerListener;
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

import timber.log.Timber;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.PLAYER;
import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.PLAYER_CHROMECAST;
import static com.stationmillenium.android.libutils.SharedPreferencesConstants.AUTOSTART_RADIO;
import static com.stationmillenium.android.libutils.activities.PlayerState.STOPPED;

/**
 * Activity to display the player
 *
 * @author vincent
 */
public class PlayerActivity extends AppCompatActivity {

    //static intialization part
    private static final int CURRENT_TIME_TIMER_START = 0;
    private static final int CURRENT_TIME_TIMER_UPDATE = 1000;
    private static final String CURRENT_TIME_TIMER_NAME = "CurrentTimeTimer";
    private static final int REFRESH_TIMEOUT = 1;

    private DrawerUtils drawerUtils;
    private PlayerFragment playerFragment;

    private PlayerActivityBinding playerActivityBinding;

    private Timer currentPlayingTimeTimer;

    private Calendar lastTimeUpdated;
    private PlayerActivityUpdateTitleBroadcastReceiver playerActivityUpdateTitleBroadcastReceiver;

    private MenuItem castMenu;
    private CastStateListener castStateListener;
    private CastContext castContext;
    private SessionManagerListener<CastSession> sessionManagerListener;
    private ActivityCastUtils activityCastUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //init view
        super.onCreate(savedInstanceState);
        playerActivityBinding = DataBindingUtil.setContentView(this, R.layout.player_activity);
        setSupportActionBar(playerActivityBinding.playerToolbar);

        drawerUtils = new DrawerUtils(this, playerActivityBinding.playerDrawerLayout, playerActivityBinding.playerToolbar, R.id.nav_drawer_player);
        playerFragment = (PlayerFragment) getSupportFragmentManager().findFragmentById(R.id.player_fragment);

        //set the volume stream will be controlled but pressing buttons
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Cast init : https://developers.google.com/cast/docs/android_sender_integrate#initialize_the_cast_context
        castContext = CastContext.getSharedInstance(this);
        activityCastUtils = new ActivityCastUtils(this, (playingOnChromecast) -> playerFragment.setPlayingOnChromecast(playingOnChromecast));
        sessionManagerListener = new ActivitySessionManagerListener(activityCastUtils.getRmcListener(), activityCastUtils, () -> stopPlayer(), () -> startPlayer(), () -> playerFragment.getPlayerState());
        castStateListener = newState -> {
            if (newState != CastState.NO_DEVICES_AVAILABLE) {
                activityCastUtils.showIntroductoryOverlay(castMenu);
            }
        };
    }

    @Override
    protected void onPause() {
        Timber.d("Pausing player activity");
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
        castContext.getSessionManager().removeSessionManagerListener(sessionManagerListener, CastSession.class);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerUtils.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
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
        Timber.d("Resume player activity");
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
                if (getDefaultSharedPreferences(this).getBoolean(AUTOSTART_RADIO, false)
                        && (castContext.getCastState() == CastState.NO_DEVICES_AVAILABLE || castContext.getCastState() == CastState.NOT_CONNECTED)) {
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
        castContext.getSessionManager().addSessionManagerListener(sessionManagerListener, CastSession.class);

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
        Timber.d("Player state intent received : %s", intent);
        if (PlayerState.PAUSED.getAssociatedIntent().toString().equals(intent.getAction())) {
            Timber.d("Pause player");
            playerFragment.setPlayerState(PlayerState.PAUSED);

        } else if (PlayerState.PLAYING.getAssociatedIntent().toString().equals(intent.getAction())) {
            Timber.d("Playing player");
            playerFragment.setPlayerState(PlayerState.PLAYING);

        } else if (PlayerState.STOPPED.getAssociatedIntent().toString().equals(intent.getAction())) {
            Timber.d("Stop player");
            playerFragment.setPlayerState(PlayerState.STOPPED);

        } else if (PlayerState.BUFFERING.getAssociatedIntent().toString().equals(intent.getAction())) {
            Timber.d("Buffering player");
            playerFragment.setPlayerState(PlayerState.BUFFERING);

        } else if (LocalIntents.ON_PLAYER_OPEN.toString().equals(intent.getAction())) {
            Timber.d("Open player with data");
            playerActivityUpdateTitleBroadcastReceiver.onReceive(this, intent);
            playerFragment.setPlayerState((PlayerState) intent.getSerializableExtra(LocalIntentsData.CURRENT_STATE.toString()));
        }
    }

    /**
     * Start the player
     */
    public void startPlayer() {
        //start player service
        Timber.d("Play player button clicked");
        if (castContext.getCastState() == CastState.CONNECTED) {
            Timber.d("Play on Chromecast");
            activityCastUtils.startCast(castContext.getSessionManager().getCurrentCastSession(), getString(R.string.cast_default_artist),
                    getString(R.string.cast_default_title), getString(R.string.cast_default_image_url), getString(R.string.player_stream_url),
                    MediaInfo.STREAM_TYPE_LIVE, 0,
                    playerActivityBinding.playerCoordinatorLayout, PLAYER_CHROMECAST);
        } else if (playerFragment.getPlayerState() == STOPPED) {
            if (!AppUtils.isMediaPlayerServiceRunning(this)) {
                if (!AppUtils.isWifiOnlyAndWifiNotConnected(this)) {
                    //start player service
                    Timber.d("Start media player service");
                    Intent mediaPlayerIntent = new Intent(this, MediaPlayerService.class);
                    startService(mediaPlayerIntent);

                } else {
                    Timber.w("Wifi requested for streaming radio, but not connected");
                    Snackbar.make(playerActivityBinding.playerCoordinatorLayout, R.string.player_no_wifi, Snackbar.LENGTH_SHORT).show();
                }

            } else {
                Timber.d("Media player service already started");
            }
        } else {
            Timber.d("Play the player");
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
     */
    public void stopPlayer() {
        Timber.d("Stop player button clicked");
        Intent stopPlayerIntent = new Intent(LocalIntents.PLAYER_STOP.toString());
        stopPlayerIntent.setClass(this, MediaPlayerService.class);
        startService(stopPlayerIntent);
    }

    /**
     * Pause the player
     */
    public void pausePlayer() {
        Timber.d("Pause player button clicked");
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
            Timber.d("Data refresh requested...");
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

}
