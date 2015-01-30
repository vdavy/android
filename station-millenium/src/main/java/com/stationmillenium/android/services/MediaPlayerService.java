/**
 *
 */
package com.stationmillenium.android.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.PlayerActivity;
import com.stationmillenium.android.activities.PlayerActivity.PlayerState;
import com.stationmillenium.android.dto.CurrentTitleDTO;
import com.stationmillenium.android.utils.AppUtils;
import com.stationmillenium.android.utils.intents.LocalIntents;
import com.stationmillenium.android.utils.intents.LocalIntentsData;
import com.stationmillenium.android.utils.mediaplayer.receivers.AudioBecommingNoisyBroadcastReceiver;
import com.stationmillenium.android.utils.mediaplayer.receivers.PlaybackControlButtonsBroadcastReceiver;
import com.stationmillenium.android.utils.mediaplayer.receivers.UpdateCurrentTitleBroadcastReceiver;
import com.stationmillenium.android.utils.mediaplayer.utils.MediaPlayerCurrentPositionGrabber;
import com.stationmillenium.android.utils.mediaplayer.utils.MediaPlayerNotificationBuilder;
import com.stationmillenium.android.utils.mediaplayer.utils.MediaPlayerOnAudioFocusChangeHandler;
import com.stationmillenium.android.utils.mediaplayer.utils.MediaPlayerServiceHandler;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

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

    //instance vars
    //service internal vars
    private Looper mediaPlayerServiceLooper;
    private MediaPlayerServiceHandler mediaPlayerServiceHandler;

    //broadcast receivers
    private ComponentName pcbbrComponentName;
    private AudioBecommingNoisyBroadcastReceiver abnbr = new AudioBecommingNoisyBroadcastReceiver(this);
    private UpdateCurrentTitleBroadcastReceiver uctbr = new UpdateCurrentTitleBroadcastReceiver(this);

    //intents and lock for update
    private Timer updateCurrentTitleTimer;
    private WifiLock wifiLock;
    private boolean playerActivityResumed;

    //vars to manage stream
    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private int originalVolume;
    private PlayerState playerState;
    private RemoteControlClient remoteControlClient;

    //new session management API 21
    private MediaSession mediaSession;
    private MediaController mediaController;
    private MediaController.TransportControls transportControls;

    //stream metadata
    private final Object currentSongImageLock = new Object();
    private Bitmap currentSongImage;
    private CurrentTitleDTO currentSong;

    private MediaPlayerNotificationBuilder mediaPlayerNotificationBuilder;
    private MediaPlayerOnAudioFocusChangeHandler mediaPlayerOnAudioFocusChangeHandler;

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

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onCreate() {
        if (!AppUtils.isAPILevel21Available()) { //we use new API in Lollipop
            pcbbrComponentName = new ComponentName(this, PlaybackControlButtonsBroadcastReceiver.class);
        }
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mediaPlayerServiceLooper = thread.getLooper();
        mediaPlayerServiceHandler = new MediaPlayerServiceHandler(mediaPlayerServiceLooper, this);

        mediaPlayerNotificationBuilder = new MediaPlayerNotificationBuilder(this);
        mediaPlayerOnAudioFocusChangeHandler = new MediaPlayerOnAudioFocusChangeHandler(this);

        if (AppUtils.isAPILevel21Available()) {
            mediaSession = new MediaSession(this, "MediaPlayerService");
            mediaSession.setCallback(new MediaSession.Callback() {
                @Override
                public void onPlay() {
                    Log.d(TAG, "Media session callback play");
                    playMediaPlayer(getApplicationContext());
                }

                @Override
                public void onPause() {
                    Log.d(TAG, "Media session callback pause");
                    pauseMediaPlayer(getApplicationContext());
                }

                @Override
                public void onStop() {
                    Log.d(TAG, "Media session callback stop");
                    stopMediaPlayer();
                }
            });

            mediaController = new MediaController(getApplicationContext(), mediaSession.getSessionToken());
            transportControls = mediaController.getTransportControls();
            mediaController.registerCallback(mediaPlayerNotificationBuilder.getMediaControllerCallback());
            mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                    MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
            if (!mediaSession.isActive()) {
                mediaSession.setActive(true);
            }

        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            //process specific intent
            if (LocalIntents.PLAYER_PAUSE.toString().equals(intent.getAction()))
                pauseMediaPlayerNewWay();
            else if (LocalIntents.PLAYER_PLAY.toString().equals(intent.getAction()))
                playMediaPlayerNewWay();
            else if (LocalIntents.PLAYER_PLAY_PAUSE.toString().equals(intent.getAction())) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        pauseMediaPlayerNewWay();
                    } else {
                        playMediaPlayerNewWay();
                    }
                }
            } else if (LocalIntents.PLAYER_STOP.toString().equals(intent.getAction()))
                if ((AppUtils.isAPILevel21Available()) && (transportControls != null)) {
                    transportControls.stop();
                } else {
                    stopMediaPlayer();
                }
            else if (LocalIntents.PLAYER_OPEN.toString().equals(intent.getAction())) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "Open the player with data");
                playerActivityResumed = true; //player resumed at same time
                Intent playerIntent = createPlayerActivityIntent();
                startActivity(playerIntent);

            } else if (LocalIntents.PLAYER_ACTIVITY_PAUSE.toString().equals(intent.getAction())) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "Player activity paused - don't send intents");
                playerActivityResumed = false;

            } else if (LocalIntents.PLAYER_ACTIVITY_RESUME.toString().equals(intent.getAction())) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "Player activity resumed - send intents");
                playerActivityResumed = true;

            } else {
                // For each start request, send a message to start a job and deliver the
                // start ID so we know which request we're stopping when we finish the job
                Message msg = mediaPlayerServiceHandler.obtainMessage();
                msg.arg1 = startId;
                if (intent.getBooleanExtra(LocalIntentsData.GET_VOLUME_FROM_PREFERENCES.toString(), false))
                    msg.arg2 = 1;
                else
                    msg.arg2 = 0;
                mediaPlayerServiceHandler.sendMessage(msg);

                //if start media player is required, activity is resumed
                playerActivityResumed = intent.getBooleanExtra(LocalIntentsData.RESUME_PLAYER_ACTIVITY.toString(), true);
            }
        }

        // If we get killed, after returning from here, restart
        return START_STICKY;
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
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Player is ready - let's start");
        mp.start();
        Toast.makeText(this, getResources().getString(R.string.player_play_toast), Toast.LENGTH_SHORT).show();

        //send state intent
        sendStateIntent(PlayerState.PLAYING);

        //wifi lock
        if ((wifiLock != null) && (!wifiLock.isHeld()))
            wifiLock.acquire();

        //player current time update start
        setupCurrentTitlePlayerServiceTimer();
    }

    /**
     * Setup the {@link CurrentTitlePlayerService} timer
     */
    private void setupCurrentTitlePlayerServiceTimer() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Register current title service timer");
        updateCurrentTitleTimer = new Timer(UPDATE_CURRENT_TITLE_TIMER_NAME);
        updateCurrentTitleTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                Intent currentTitleServiceIntent = new Intent(MediaPlayerService.this, CurrentTitlePlayerService.class);
                MediaPlayerService.this.startService(currentTitleServiceIntent);
            }

        }, UPDATE_TITLE_START_TIME, UPDATE_TITLE_PERIOD_TIME);
    }

    /**
     * Play the media player, if not already playing
     *
     * @param context the {@link Context} to update notification
     */
    public void playMediaPlayer(Context context) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Play media player");
        if ((mediaPlayer != null) && (!mediaPlayer.isPlaying()))
            mediaPlayer.start();

        //send state intent
        sendStateIntent(PlayerState.PLAYING);

        //update notification
        if (!AppUtils.isAPILevel21Available()) {
            Notification notification = mediaPlayerNotificationBuilder.createNotification(true);
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);
            Toast.makeText(this, getResources().getString(R.string.player_play_toast), Toast.LENGTH_SHORT).show();
        }

        setupCurrentTitlePlayerServiceTimer();
    }

    /**
     * Pause the media player, if playing
     *
     * @param context the {@link Context} to update notification
     */
    public void pauseMediaPlayer(Context context) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Pause media player");
        if ((mediaPlayer != null) && (mediaPlayer.isPlaying()))
            mediaPlayer.pause();

        //send state intent
        sendStateIntent(PlayerState.PAUSED);

        //update notification
        if (!AppUtils.isAPILevel21Available()) {
            Notification notification = mediaPlayerNotificationBuilder.createNotification(false);
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);
            Toast.makeText(this, getResources().getString(R.string.player_pause_toast), Toast.LENGTH_SHORT).show();
        }

        //stop current title update
        cancelCurrentTitleTimerServiceTimer();
    }

    /**
     * Stop the player and stop service
     */
    public void stopMediaPlayer() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Stop the media player");
        try {
            if ((mediaPlayer != null) && (mediaPlayer.isPlaying()))
                mediaPlayer.stop();
        } catch (IllegalStateException e) {
            Log.w(TAG, "Error while stopping media player", e);
        }
        Toast.makeText(this, getResources().getString(R.string.player_stop_toast), Toast.LENGTH_SHORT).show();

        //send state intent
        sendStateIntent(PlayerState.STOPPED);

        //stop current title update
        cancelCurrentTitleTimerServiceTimer();

        //stop service
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Stop service");
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
        sendBroadcast(widgetIntent);

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
     *
     * @param androidPlaybackState
     * @param localState
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void propagatePlaybackState(int androidPlaybackState, int localState) {
        if ((AppUtils.isAPILevel21Available()) && (mediaSession != null)) {
            int position = (mediaPlayer != null) ? mediaPlayer.getCurrentPosition() : 0;
            mediaSession.setPlaybackState(new PlaybackState.Builder().setState(androidPlaybackState, position, 1.0f).build());
        } else if ((AppUtils.isAPILevel14Available()) && (remoteControlClient != null)) {
            remoteControlClient.setPlaybackState(localState);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "Media player error occured");
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.e(TAG, "Unknown media player error - stopping media player");
                stopMediaPlayer();
                break;

            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.w(TAG, "Media player server died error - restarting");
                if (mediaPlayer != null)
                    mediaPlayer.release();
                mediaPlayer = null;

                try { //reinit media player
                    initMediaPlayer();
                } catch (IOException e) {
                    Log.w(TAG, "Error while trying to init media player", e);
                    Toast.makeText(MediaPlayerService.this, getResources().getString(R.string.player_error), Toast.LENGTH_SHORT).show();

                    //stop the service
                    if (mediaPlayer != null)
                        mediaPlayer.stop();

                    //stop service
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "Stop service");
                    stopForeground(true);
                    stopSelf();
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
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Destroying service");
        //release media player
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        //player current time and title update stop
        cancelCurrentTitleTimerServiceTimer();

        //free resources
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Free handlers");
        if (audioManager != null) {
            if ((AppUtils.isAPILevel14Available() && (remoteControlClient != null))) {
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
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Cancel current title timer");
        if (updateCurrentTitleTimer != null)
            updateCurrentTitleTimer.cancel();
    }

    /**
     * Init the media player
     *
     * @throws IOException if any error occurs
     */
    public void initMediaPlayer() throws IOException {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Init media player");
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

        //send state intent
        sendStateIntent(PlayerState.BUFFERING);
        if (!AppUtils.isAPILevel21Available()) {
            Toast.makeText(this, getResources().getString(R.string.player_loading_toast), Toast.LENGTH_SHORT).show();
        }

        //send tracking info
        Intent statsTrackerServiceIntent = new Intent(this, StatsTrackerService.class);
        startService(statsTrackerServiceIntent);
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Media player start buffering...");
            sendStateIntent(PlayerState.BUFFERING);
            return true;
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Media player end buffering...");
            sendStateIntent(PlayerState.PLAYING);
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
        return (mediaPlayer != null) && (mediaPlayer.isPlaying());
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

    public void setOriginalVolume(int originalVolume) {
        this.originalVolume = originalVolume;
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

    public int getOriginalVolume() {
        return originalVolume;
    }

    public MediaPlayerOnAudioFocusChangeHandler getMediaPlayerOnAudioFocusChangeHandler() {
        return mediaPlayerOnAudioFocusChangeHandler;
    }

    public MediaSession getMediaSession() {
        return mediaSession;
    }

    public int getPosition() {
        return mediaPlayer.getCurrentPosition();
    }
}
