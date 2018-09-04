/**
 *
 */
package com.stationmillenium.android.services;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RemoteControlClient;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.PlayerActivity;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.SharedPreferencesConstants;
import com.stationmillenium.android.libutils.activities.PlayerState;
import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO;
import com.stationmillenium.android.libutils.intents.LocalIntents;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;
import com.stationmillenium.android.libutils.mediaplayer.receivers.AudioBecommingNoisyBroadcastReceiver;
import com.stationmillenium.android.libutils.mediaplayer.receivers.PlaybackControlButtonsBroadcastReceiver;
import com.stationmillenium.android.libutils.mediaplayer.receivers.UpdateCurrentTitleBroadcastReceiver;
import com.stationmillenium.android.libutils.mediaplayer.utils.MediaPlayerCurrentPositionGrabber;
import com.stationmillenium.android.libutils.mediaplayer.utils.MediaPlayerNotificationBuilder;
import com.stationmillenium.android.libutils.mediaplayer.utils.MediaPlayerOnAudioFocusChangeHandler;
import com.stationmillenium.android.libutils.mediaplayer.utils.MediaPlayerServiceHandler;
import com.stationmillenium.android.widgets.WidgetProvider;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

import static com.stationmillenium.android.libutils.mediaplayer.utils.MediaPlayerNotificationBuilder.NOTIFICATION_CHANNEL_ID;

/**
 * Service to play audio stream
 * http://developer.android.com/guide/components/services.html
 *
 * @author vincent
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MediaPlayerService extends Service implements OnPreparedListener, OnErrorListener, OnInfoListener {

    //class constants
    private static final String TAG = "MediaPlayerService";
    private static final String UPDATE_CURRENT_TITLE_TIMER_NAME = "UpdateCurrentTitleTimer";
    public static final int NOTIFICATION_ID = 1;
    private static final int UPDATE_TITLE_START_TIME = 500;
    private static final int UPDATE_TITLE_PERIOD_TIME = 10000;
    private static final String AUTO_RESTART_PLAYER_TIMER_NAME = "AutoRestartPlayerTimer";
    private static final String AUTO_RESTART_PLAYER_DEFAULT_DELAY = "10";
    private static final int MEDIA_PLAYER_MAX_VOLUME = 101;

    private MediaPlayerServiceHandler mediaPlayerServiceHandler;

    //broadcast receivers
    private ComponentName pcbbrComponentName;
    private AudioBecommingNoisyBroadcastReceiver abnbr = new AudioBecommingNoisyBroadcastReceiver(this);
    private UpdateCurrentTitleBroadcastReceiver uctbr = new UpdateCurrentTitleBroadcastReceiver(this);

    //intents and lock for update
    private Timer updateCurrentTitleTimer;
    private Timer autoRestartPlayerTimer;
    private WifiLock wifiLock;
    private boolean playerActivityResumed;

    //vars to manage stream
    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private PlayerState playerState;
    private RemoteControlClient remoteControlClient;

    //new session management API 21
    private MediaSession mediaSession;
    private MediaController.TransportControls transportControls;

    //stream metadata
    private final Object currentSongImageLock = new Object();
    private Bitmap currentSongImage;
    private CurrentTitleDTO currentSong;

    private MediaPlayerNotificationBuilder mediaPlayerNotificationBuilder;
    private MediaPlayerOnAudioFocusChangeHandler mediaPlayerOnAudioFocusChangeHandler;
    private WidgetProvider widgetProvider;

    /**
     * Create an {@link Intent} to open the {@link PlayerActivity} with data
     *
     * @return the {@link Intent}
     */
    private Intent createPlayerActivityIntent() {
        Intent playerIntent = new Intent(MediaPlayerService.this, PlayerActivity.class);
        playerIntent.setAction(LocalIntents.ON_PLAYER_OPEN.toString());
        playerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        playerIntent.putExtra(LocalIntentsData.CURRENT_TITLE.toString(), currentSong);
        playerIntent.putExtra(LocalIntentsData.CURRENT_STATE.toString(), playerState);
        return playerIntent;
    }

    @SuppressLint("NewApi")
    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onCreate() {
        if (!AppUtils.isAPILevel21Available()) { //we use new API in Lollipop
            pcbbrComponentName = new ComponentName(this, PlaybackControlButtonsBroadcastReceiver.class);
        }
        if (AppUtils.isAPILevel26Available()) {
            startForeground(NOTIFICATION_ID, buildForegroundWaintingNotification());
        }

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        Looper mediaPlayerServiceLooper = thread.getLooper();
        mediaPlayerServiceHandler = new MediaPlayerServiceHandler(mediaPlayerServiceLooper, this);

        mediaPlayerNotificationBuilder = new MediaPlayerNotificationBuilder(this);
        mediaPlayerOnAudioFocusChangeHandler = new MediaPlayerOnAudioFocusChangeHandler(this);

        if (AppUtils.isAPILevel21Available()) {
            mediaSession = new MediaSession(this, "MediaPlayerService");
            mediaSession.setCallback(new MediaSession.Callback() {
                @Override
                public void onPlay() {
                    Timber.d("Media session callback play");
                    playMediaPlayer(getApplicationContext());
                }

                @Override
                public void onPause() {
                    Timber.d("Media session callback pause");
                    pauseMediaPlayer(getApplicationContext());
                }

                @Override
                public void onStop() {
                    Timber.d("Media session callback stop");
                    stopMediaPlayer();
                }
            });

            MediaController mediaController = new MediaController(getApplicationContext(), mediaSession.getSessionToken());
            transportControls = mediaController.getTransportControls();
            mediaController.registerCallback(mediaPlayerNotificationBuilder.getMediaControllerCallback());
            mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
            if (!mediaSession.isActive()) {
                mediaSession.setActive(true);
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification buildForegroundWaintingNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notif_icon)
                .setContentTitle(getString(R.string.notification_ticker_text))
                .setContentText(getString(R.string.player_notification_loading))
                .build();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            //process specific intent
            if (LocalIntents.PLAYER_PAUSE.toString().equals(intent.getAction())) {
                pauseMediaPlayerNewWay();
            } else if (LocalIntents.PLAYER_PLAY.toString().equals(intent.getAction())) {
                playMediaPlayerNewWay();
            } else if (LocalIntents.PLAYER_PLAY_PAUSE.toString().equals(intent.getAction())) {
                if (mediaPlayer != null) {
                    if (isMediaPlayerPlaying()) {
                        pauseMediaPlayerNewWay();
                    } else {
                        playMediaPlayerNewWay();
                    }
                }
            } else if (LocalIntents.PLAYER_STOP.toString().equals(intent.getAction())) {
                if ((AppUtils.isAPILevel21Available()) && (transportControls != null)) {
                    transportControls.stop();
                } else {
                    stopMediaPlayer();
                }
            } else if (LocalIntents.PLAYER_OPEN.toString().equals(intent.getAction())) {
                Timber.d("Open the player with data");
                playerActivityResumed = true; //player resumed at same time
                Intent playerIntent = createPlayerActivityIntent();
                startActivity(playerIntent);

            } else if (LocalIntents.PLAYER_ACTIVITY_PAUSE.toString().equals(intent.getAction())) {
                Timber.d("Player activity paused - don't send intents");
                playerActivityResumed = false;

            } else if (LocalIntents.PLAYER_ACTIVITY_RESUME.toString().equals(intent.getAction())) {
                Timber.d("Player activity resumed - send intents");
                playerActivityResumed = true;

            } else {
                // For each start request, send a message to start a job and deliver the
                // start ID so we know which request we're stopping when we finish the job
                Message msg = mediaPlayerServiceHandler.obtainMessage();
                msg.arg1 = startId;
                if (intent.getBooleanExtra(LocalIntentsData.GET_VOLUME_FROM_PREFERENCES.toString(), false)) {
                    msg.arg2 = 1;
                } else {
                    msg.arg2 = 0;
                }
                mediaPlayerServiceHandler.sendMessage(msg);

                //if start media player is required, activity is resumed
                playerActivityResumed = intent.getBooleanExtra(LocalIntentsData.RESUME_PLAYER_ACTIVITY.toString(), true);
            }
        }

        // If we get killed, after returning from here, restart
        return START_NOT_STICKY;
    }

    /**
     * Play the media player using the new Lollipop API if available
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void playMediaPlayerNewWay() {
        if ((AppUtils.isAPILevel21Available()) && (transportControls != null)) {
            transportControls.play();
        } else {
            playMediaPlayer(getApplicationContext());
        }
    }

    /**
     * Pause the media player using the new Lollipop API if available
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void pauseMediaPlayerNewWay() {
        if ((AppUtils.isAPILevel21Available()) && (transportControls != null)) {
            transportControls.pause();
        } else {
            pauseMediaPlayer(getApplicationContext());
        }
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        Timber.d("Player is ready - let's start");
        mp.start();

        //send state intent
        sendStateIntent(PlayerState.PLAYING);

        //wifi lock
        if ((wifiLock != null) && (!wifiLock.isHeld()))
            wifiLock.acquire();

        //player current time update start
        setupCurrentTitlePlayerServiceTimer();
        setupAutoRestartPlayerTimer();
    }

    /**
     * Setup the {@link CurrentTitlePlayerService} timer
     */
    private void setupCurrentTitlePlayerServiceTimer() {
        Timber.d("Register current title service timer");
        updateCurrentTitleTimer = new Timer(UPDATE_CURRENT_TITLE_TIMER_NAME);
        updateCurrentTitleTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                Intent currentTitleServiceIntent = new Intent(MediaPlayerService.this, CurrentTitlePlayerService.class);
                CurrentTitlePlayerService.enqueueWork(MediaPlayerService.this, currentTitleServiceIntent);
            }

        }, UPDATE_TITLE_START_TIME, UPDATE_TITLE_PERIOD_TIME);
    }

    /**
     * Setup the timer for the auto restart player service
     */
    private void setupAutoRestartPlayerTimer() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SharedPreferencesConstants.PLAYER_AUTORESTART, false)) {
            Timber.d("Register auto restart player timer");

            int defaultDelay = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(SharedPreferencesConstants.PLAYER_AUTORESTART_DELAY, AUTO_RESTART_PLAYER_DEFAULT_DELAY))
                    * 1000; //in ms
            autoRestartPlayerTimer = new Timer(AUTO_RESTART_PLAYER_TIMER_NAME);
            autoRestartPlayerTimer.schedule(new TimerTask() {

                private int previousPosition;

                @Override
                public void run() {
                    if (mediaPlayer != null) { //no need to check if media player running if it is null
                        Intent autoRestartPlayerIntent = new Intent(MediaPlayerService.this, AutoRestartPlayerService.class);
                        autoRestartPlayerIntent.putExtra(AutoRestartPlayerService.PREVIOUS_POSITION, previousPosition);
                        autoRestartPlayerIntent.putExtra(AutoRestartPlayerService.CURRENT_POSITION, getPosition());
                        autoRestartPlayerIntent.putExtra(AutoRestartPlayerService.PLAYER_STATE, playerState);
                        AutoRestartPlayerService.enqueueWork(MediaPlayerService.this, autoRestartPlayerIntent);
                        previousPosition = getPosition();
                    }
                }

            }, defaultDelay, defaultDelay);

        } else {
            Timber.d("No need to register auto restart player timer");
        }
    }

    /**
     * Play the media player, if not already playing
     *
     * @param context the {@link Context} to update notification
     */
    public void playMediaPlayer(Context context) {
        Timber.d("Play media player");
        if (isMediaPlayerPlaying()) {
            mediaPlayer.start();
        }
        setMediaPlayerVolume(100);

        //send state intent
        sendStateIntent(PlayerState.PLAYING);

        //update notification
        if (!AppUtils.isAPILevel21Available()) {
            Notification notification = mediaPlayerNotificationBuilder.createNotification(true);
            if ((context.getSystemService(Context.NOTIFICATION_SERVICE)) != null) {
                ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);
            }
        }

        //start timers
        setupCurrentTitlePlayerServiceTimer();
        setupAutoRestartPlayerTimer();
    }

    /**
     * Pause the media player, if playing
     *
     * @param context the {@link Context} to update notification
     */
    private void pauseMediaPlayer(Context context) {
        Timber.d("Pause media player");

        if (isMediaPlayerPlaying()) {
            setMediaPlayerVolume(0);
        }

        //send state intent
        sendStateIntent(PlayerState.PAUSED);

        //update notification
        if (!AppUtils.isAPILevel21Available()) {
            Notification notification = mediaPlayerNotificationBuilder.createNotification(false);
            assert context.getSystemService(Context.NOTIFICATION_SERVICE) != null;
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);
        }

        //stop current title update
        cancelCurrentTitleTimerServiceTimer();
        cancelAutoRestartPlayerServiceTimer();
    }

    /**
     * Stop the player and stop service
     */
    public void stopMediaPlayer() {
        Timber.d("Stop the media player");
        try {
            if (isMediaPlayerPlaying()) {
                mediaPlayer.stop();
            }
        } catch (IllegalStateException e) {
            Timber.w(e, "Error while stopping media player");
        }

        //send state intent
        sendStateIntent(PlayerState.STOPPED);

        //stop current title update
        cancelCurrentTitleTimerServiceTimer();
        cancelAutoRestartPlayerServiceTimer();

        //disengage widget update
        //unregisterWidgetBR();

        //stop service
        Timber.d("Stop service");
        stopForeground(true);
        stopSelf();
    }

    /**
     * Send the state intent
     *
     * @param state the {@link PlayerState}
     */
    private void sendStateIntent(PlayerState state) {
        //send the state
        playerState = state;
        if (playerActivityResumed) { //send intents only if activity is resumed
            Intent stateIntent = new Intent(this, PlayerActivity.class);
            stateIntent.setAction(state.getAssociatedIntent().toString());
            stateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(stateIntent);
        }

        //send local broadcast intent for widget
        Intent widgetIntent = new Intent(state.getAssociatedIntent().toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(widgetIntent);

        //adjust the remote control state
        switch (playerState) {
            case BUFFERING:
                propagatePlaybackState(PlaybackState.STATE_BUFFERING, RemoteControlClient.PLAYSTATE_BUFFERING);
                break;

            case PAUSED:
                propagatePlaybackState(PlaybackState.STATE_PAUSED, RemoteControlClient.PLAYSTATE_PAUSED);
                break;

            case PLAYING:
                propagatePlaybackState(PlaybackState.STATE_PLAYING, RemoteControlClient.PLAYSTATE_PLAYING);
                break;

            case STOPPED:
                propagatePlaybackState(PlaybackState.STATE_STOPPED, RemoteControlClient.PLAYSTATE_STOPPED);
                break;
        }
    }

    /**
     * Propagate playback state in old and fashion way
     * See here for playback configuration : http://stackoverflow.com/questions/26999245/handling-media-buttons-in-android-5-0-lollipop
     * @param androidPlaybackState the playback state in android format
     * @param localState the local playback state
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void propagatePlaybackState(int androidPlaybackState, int localState) {
        if ((AppUtils.isAPILevel21Available()) && (mediaSession != null)) {
            int position = getPosition();
            mediaSession.setPlaybackState(new PlaybackState.Builder()
                    .setActions(
                            PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_STOP)
                    .setState(androidPlaybackState, position, 1.0f)
                    .build());
        } else if (remoteControlClient != null) {
            remoteControlClient.setPlaybackState(localState);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Timber.e("Media player error occured");
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Timber.e("Unknown media player error - stopping media player");
                stopMediaPlayer();
                break;

            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Timber.w("Media player server died error - restarting");
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }
                mediaPlayer = null;
                unregisterWidgetBR();
                try { //reinit media player
                    initMediaPlayer();
                } catch (IOException e) {
                    Timber.w(e, "Error while trying to init media player");
                    Toast.makeText(MediaPlayerService.this, R.string.player_error, Toast.LENGTH_SHORT).show();

                    //stop the service
                    stopMediaPlayer();
                }

        }

        return true; //error handled
    }

    @Override
    public IBinder onBind(Intent intent) {
        //no binding service - return null
        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDestroy() {
        Timber.d("Destroying service");

        //release media player
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        //player current time and title update stop
        cancelCurrentTitleTimerServiceTimer();
        cancelAutoRestartPlayerServiceTimer();

        //free resources
        Timber.d("Free handlers");
        unregisterWidgetBR();
        if (audioManager != null) {
            if (remoteControlClient != null) {
                audioManager.unregisterRemoteControlClient(remoteControlClient);
            }

            audioManager.abandonAudioFocus(mediaPlayerOnAudioFocusChangeHandler);
            if ((!AppUtils.isAPILevel21Available()) && (pcbbrComponentName != null)) { //not used if not remote control client
                audioManager.unregisterMediaButtonEventReceiver(pcbbrComponentName);
            }
        }
        if (abnbr.isRegistered()) {
            unregisterReceiver(abnbr);
            abnbr.setRegistered(false);
        }
        if (uctbr.isRegistered()) {
            LocalBroadcastManager.getInstance(MediaPlayerService.this).unregisterReceiver(uctbr);
            uctbr.setRegistered(false);
        }
        if ((AppUtils.isAPILevel21Available()) && (mediaSession != null) && (mediaSession.isActive())) {
            mediaSession.release();
        }

        //wifi lock
        if ((wifiLock != null) && (wifiLock.isHeld()))
            wifiLock.release();

        super.onDestroy();
    }

    /**
     * Cancel {@link CurrentTitlePlayerService} timer
     */
    private void cancelCurrentTitleTimerServiceTimer() {
        Timber.d("Cancel current title timer");
        if (updateCurrentTitleTimer != null) {
            updateCurrentTitleTimer.cancel();
        }
    }

    /**
     * Cancel {@link com.stationmillenium.android.services.AutoRestartPlayerService} timer
     */
    private void cancelAutoRestartPlayerServiceTimer() {
        Timber.d("Cancel auto restart player service timer");
        if (autoRestartPlayerTimer != null) {
            autoRestartPlayerTimer.cancel();
        }
    }

    /**
     * Init the media player
     *
     * @throws IOException if any error occurs
     */
    public void initMediaPlayer() throws IOException {
        Timber.d("Init media player");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(getResources().getString(R.string.player_stream_url));
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.prepareAsync();

        //set reference
        MediaPlayerCurrentPositionGrabber.setMediaPlayerReference(mediaPlayer);
        registerWidgetBR();

        //send state intent
        sendStateIntent(PlayerState.BUFFERING);

        //send tracking info
        Intent statsTrackerServiceIntent = new Intent(this, StatsTrackerService.class);
        StatsTrackerService.enqueueWork(this, statsTrackerServiceIntent);
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            Timber.d("Media player start buffering...");
            sendStateIntent(PlayerState.BUFFERING);
            cancelAutoRestartPlayerServiceTimer();
            //update notification
            if (!AppUtils.isAPILevel21Available()) {
                Notification notification = mediaPlayerNotificationBuilder.createNotification(false);
                assert getSystemService(Context.NOTIFICATION_SERVICE) != null;
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);
            }
            return true;
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            Timber.d("Media player end buffering...");
            sendStateIntent(PlayerState.PLAYING);
            setupAutoRestartPlayerTimer();
            if (!AppUtils.isAPILevel21Available()) {
                Notification notification = mediaPlayerNotificationBuilder.createNotification(true);
                assert getSystemService(Context.NOTIFICATION_SERVICE) != null;
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);
            }
            return true;
        }

        return false;
    }

    /**
     * Check if the media player is playing
     *
     * @return true if playing, false if not
     */
    public boolean isMediaPlayerPlaying() {
        try {
            return (mediaPlayer != null) && (mediaPlayer.isPlaying());
        } catch (IllegalStateException e) {
            Timber.w(e, "Error in isPlaying");
            return false;
        }
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public Object getCurrentSongImageLock() {
        return currentSongImageLock;
    }

    public CurrentTitleDTO getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(CurrentTitleDTO currentSong) {
        this.currentSong = currentSong;
    }

    public void setCurrentSongImage(Bitmap currentSongImage) {
        this.currentSongImage = currentSongImage;
    }

    public RemoteControlClient getRemoteControlClient() {
        return remoteControlClient;
    }

    public void setRemoteControlClient(RemoteControlClient remoteControlClient) {
        this.remoteControlClient = remoteControlClient;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public ComponentName getPcbbrComponentName() {
        return pcbbrComponentName;
    }

    public AudioBecommingNoisyBroadcastReceiver getAbnbr() {
        return abnbr;
    }

    public UpdateCurrentTitleBroadcastReceiver getUctbr() {
        return uctbr;
    }

    public void setWifiLock(WifiLock wifiLock) {
        this.wifiLock = wifiLock;
    }

    public Bitmap getCurrentSongImage() {
        return currentSongImage;
    }

    public MediaPlayerNotificationBuilder getMediaPlayerNotificationBuilder() {
        return mediaPlayerNotificationBuilder;
    }

    public MediaPlayerOnAudioFocusChangeHandler getMediaPlayerOnAudioFocusChangeHandler() {
        return mediaPlayerOnAudioFocusChangeHandler;
    }

    public MediaSession getMediaSession() {
        return mediaSession;
    }

    public int getPosition() {
        try {
            return (mediaPlayer != null) ? mediaPlayer.getCurrentPosition() : 0;
        } catch (IllegalStateException e) {
            Timber.w(e, "Error in getCurrentPosition");
            return 0;
        }
    }

    /**
     * Set the media player volume in percent (0 low volume, 100 max volume)
     * @param volume the volume in percent
     */
    public void setMediaPlayerVolume(int volume) {
        // see : http://stackoverflow.com/questions/5215459/android-mediaplayer-setvolume-function
        float volumeLog = 1 - (float)(Math.log(MEDIA_PLAYER_MAX_VOLUME - volume) / Math.log(MEDIA_PLAYER_MAX_VOLUME));
        if (mediaPlayer != null) { //NPE here reported by firebase crash reporter
            mediaPlayer.setVolume(volumeLog, volumeLog);
        }
    }

    private void registerWidgetBR() {
        if (widgetProvider == null) {
            widgetProvider = new WidgetProvider();
            for (LocalIntents localIntent : new LocalIntents[]{LocalIntents.CURRENT_TITLE_UPDATED, LocalIntents.ON_PLAYER_PLAY, LocalIntents.ON_PLAYER_PAUSE, LocalIntents.ON_PLAYER_STOP, LocalIntents.ON_PLAYER_BUFFERING}) {
                LocalBroadcastManager.getInstance(this).registerReceiver(widgetProvider, new IntentFilter(localIntent.toString()));
            }
        }
    }

    private void unregisterWidgetBR() {
        if (widgetProvider != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(widgetProvider);
            widgetProvider = null;
        }
    }

}
