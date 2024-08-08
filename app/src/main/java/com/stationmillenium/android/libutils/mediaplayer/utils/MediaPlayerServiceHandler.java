package com.stationmillenium.android.libutils.mediaplayer.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.RemoteControlClient;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ServiceCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.stationmillenium.android.R;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.intents.LocalIntents;
import com.stationmillenium.android.services.MediaPlayerService;

import java.lang.ref.WeakReference;

import timber.log.Timber;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;
import static android.media.AudioManager.STREAM_MUSIC;
import static com.stationmillenium.android.activities.preferences.AlarmSharedPreferencesActivity.AlarmSharedPreferencesConstants.ALARM_VOLUME;

/**
 * Service handler to run in another thread
 *
 * @author vincent
 */
public class MediaPlayerServiceHandler extends Handler {

    //rerefences to service
    //see : http://stackoverflow.com/questions/12084382/what-is-handlerleak
    private WeakReference<MediaPlayerService> mediaPlayerServiceRef;

    public MediaPlayerServiceHandler(Looper looper, MediaPlayerService service) {
        super(looper);
        mediaPlayerServiceRef = new WeakReference<>(service);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void handleMessage(Message msg) {
        Timber.d("Start the MediaPlayerService");
        if (mediaPlayerServiceRef.get() != null) {
            try {
                mediaPlayerServiceRef.get().setAudioManager((AudioManager) mediaPlayerServiceRef.get().getSystemService(Context.AUDIO_SERVICE));
                int result = mediaPlayerServiceRef.get().getAudioManager().requestAudioFocus(mediaPlayerServiceRef.get().getMediaPlayerOnAudioFocusChangeHandler(), STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    Timber.d("Audio focus request granted - start the stream");
                    if (AppUtils.isNetworkAvailable(mediaPlayerServiceRef.get().getApplicationContext())) { //check if network is up
                        try {
                            //init player
                            mediaPlayerServiceRef.get().initMediaPlayer();

                            //add handlers
                            addHandlers(msg);

                            //remote control if api level 14
                            setupRemoteControl();

                            //wifi lock
                            mediaPlayerServiceRef.get().setWifiLock(((WifiManager) mediaPlayerServiceRef.get().getApplicationContext().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "StationMilleniumPlayerWifiLock"));

                            //start in foreground
                            Notification notification = mediaPlayerServiceRef.get().getMediaPlayerNotificationBuilder().createNotification(true);
                            ServiceCompat.startForeground(mediaPlayerServiceRef.get(), MediaPlayerService.NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);

                        } catch (Exception e) {
                            Timber.w(e, "Error while trying to init media player");
                            Toast.makeText(mediaPlayerServiceRef.get(), R.string.player_error, Toast.LENGTH_SHORT).show();

                            //stop the service
                            stopMediaPlayer();
                        }

                    } else {
                        Timber.w("No Internet connection - can't play stream");
                        Toast.makeText(mediaPlayerServiceRef.get(), R.string.player_network_unavailable, Toast.LENGTH_SHORT).show();

                        //stop the service
                        stopMediaPlayer();
                    }

                } else {
                    Timber.w("Audio focus request failed - can't play stream");
                    Toast.makeText(mediaPlayerServiceRef.get(), R.string.player_error, Toast.LENGTH_SHORT).show();

                    //stop the service
                    stopMediaPlayer();
                }

            } catch (Exception npe) {
                Timber.e(npe, "Exception in MediaPlayerService init");
            }

        } else {
            Timber.e("Reference to MediaPlayerService is null ! Nothing can be done");
        }
    }

    private void stopMediaPlayer() {
        mediaPlayerServiceRef.get().getAudioManager().abandonAudioFocus(mediaPlayerServiceRef.get().getMediaPlayerOnAudioFocusChangeHandler());
        mediaPlayerServiceRef.get().stopMediaPlayer();
    }

    private void setupRemoteControl() {
        if (!AppUtils.isAPILevel21Available()) {
            //init the remote control client
            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            mediaButtonIntent.setComponent(mediaPlayerServiceRef.get().getPcbbrComponentName());
            PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(mediaPlayerServiceRef.get().getApplicationContext(), 0, mediaButtonIntent, PendingIntent.FLAG_IMMUTABLE);
            mediaPlayerServiceRef.get().setRemoteControlClient(new RemoteControlClient(mediaPendingIntent));
            mediaPlayerServiceRef.get().getRemoteControlClient().setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                    | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
                    | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                    | RemoteControlClient.FLAG_KEY_MEDIA_STOP);
            mediaPlayerServiceRef.get().getAudioManager().registerRemoteControlClient(mediaPlayerServiceRef.get().getRemoteControlClient());
        }
    }

    private void addHandlers(Message msg) {
        if ((!AppUtils.isAPILevel21Available()) && (mediaPlayerServiceRef.get().getPcbbrComponentName() != null)) { //not used in Lollipop
            mediaPlayerServiceRef.get().getAudioManager().registerMediaButtonEventReceiver(mediaPlayerServiceRef.get().getPcbbrComponentName());
        }
        mediaPlayerServiceRef.get().registerReceiver(mediaPlayerServiceRef.get().getAbnbr(), new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        mediaPlayerServiceRef.get().getAbnbr().setRegistered(true);
        LocalBroadcastManager.getInstance(mediaPlayerServiceRef.get()).registerReceiver(mediaPlayerServiceRef.get().getUctbr(), new IntentFilter(LocalIntents.CURRENT_TITLE_UPDATED.toString()));
        mediaPlayerServiceRef.get().getUctbr().setRegistered(true);
        if (msg.arg2 == 1) { //use volume manager from shared preferences
            Timber.d("Use volume from shared preferences");
            int volumeValue = PreferenceManager.getDefaultSharedPreferences(mediaPlayerServiceRef.get()).getInt(ALARM_VOLUME, mediaPlayerServiceRef.get().getAudioManager().getStreamVolume(STREAM_MUSIC));
            mediaPlayerServiceRef.get().getAudioManager().setStreamVolume(STREAM_MUSIC, volumeValue, 0);
        }
    }

}
