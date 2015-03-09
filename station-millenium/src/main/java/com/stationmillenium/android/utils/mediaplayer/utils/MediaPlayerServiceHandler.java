package com.stationmillenium.android.utils.mediaplayer.utils;

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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.preferences.AlarmSharedPreferencesActivity;
import com.stationmillenium.android.services.MediaPlayerService;
import com.stationmillenium.android.utils.AppUtils;
import com.stationmillenium.android.utils.intents.LocalIntents;

import java.lang.ref.WeakReference;

/**
 * Service handler to run in another thread
 *
 * @author vincent
 */
public class MediaPlayerServiceHandler extends Handler {

    private static final String TAG = "MPServiceHandler";

    //rerefences to service
    //see : http://stackoverflow.com/questions/12084382/what-is-handlerleak
    private WeakReference<MediaPlayerService> mediaPlayerServiceRef;

    public MediaPlayerServiceHandler(Looper looper, MediaPlayerService service) {
        super(looper);
        mediaPlayerServiceRef = new WeakReference<>(service);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void handleMessage(Message msg) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Start the MediaPlayerService");
        if (mediaPlayerServiceRef.get() != null) {
            try {
                mediaPlayerServiceRef.get().setAudioManager((AudioManager) mediaPlayerServiceRef.get().getSystemService(Context.AUDIO_SERVICE));
                int result = mediaPlayerServiceRef.get().getAudioManager().requestAudioFocus(mediaPlayerServiceRef.get().getMediaPlayerOnAudioFocusChangeHandler(), AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "Audio focus request granted - start the stream");

                    if (AppUtils.isNetworkAvailable(mediaPlayerServiceRef.get().getApplicationContext())) { //check if network is up
                        try {
                            //init player
                            mediaPlayerServiceRef.get().initMediaPlayer();

                            //add handlers
                            if ((!AppUtils.isAPILevel21Available()) && (mediaPlayerServiceRef.get().getPcbbrComponentName() != null)) { //not used in Lollipop
                                mediaPlayerServiceRef.get().getAudioManager().registerMediaButtonEventReceiver(mediaPlayerServiceRef.get().getPcbbrComponentName());
                            }
                            mediaPlayerServiceRef.get().registerReceiver(mediaPlayerServiceRef.get().getAbnbr(), new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
                            mediaPlayerServiceRef.get().getAbnbr().setRegistered(true);
                            LocalBroadcastManager.getInstance(mediaPlayerServiceRef.get()).registerReceiver(mediaPlayerServiceRef.get().getUctbr(), new IntentFilter(LocalIntents.CURRENT_TITLE_UPDATED.toString()));
                            mediaPlayerServiceRef.get().getUctbr().setRegistered(true);
                            if (msg.arg2 == 1) { //use volume manager from shared preferences
                                if (BuildConfig.DEBUG)
                                    Log.d(TAG, "Use volume from shared preferences");
                                int volumeValue = PreferenceManager.getDefaultSharedPreferences(mediaPlayerServiceRef.get())
                                        .getInt(AlarmSharedPreferencesActivity.AlarmSharedPreferencesConstants.ALARM_VOLUME, mediaPlayerServiceRef.get().getAudioManager().getStreamVolume(AudioManager.STREAM_MUSIC));
                                mediaPlayerServiceRef.get().getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, volumeValue, 0);
                            }
                            mediaPlayerServiceRef.get().setOriginalVolume(mediaPlayerServiceRef.get().getAudioManager().getStreamVolume(AudioManager.STREAM_MUSIC));

                            //remote control if api level 14
                            if ((AppUtils.isAPILevel14Available()) && (!AppUtils.isAPILevel21Available())) {
                                //init the remote control client
                                Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                                mediaButtonIntent.setComponent(mediaPlayerServiceRef.get().getPcbbrComponentName());
                                PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(mediaPlayerServiceRef.get().getApplicationContext(), 0, mediaButtonIntent, 0);
                                mediaPlayerServiceRef.get().setRemoteControlClient(new RemoteControlClient(mediaPendingIntent));
                                mediaPlayerServiceRef.get().getRemoteControlClient().setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                                        | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
                                        | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                                        | RemoteControlClient.FLAG_KEY_MEDIA_STOP);
                                mediaPlayerServiceRef.get().getAudioManager().registerRemoteControlClient(mediaPlayerServiceRef.get().getRemoteControlClient());
                            }

                            //wifi lock
                            mediaPlayerServiceRef.get().setWifiLock(((WifiManager) mediaPlayerServiceRef.get().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "StationMilleniumPlayerWifiLock"));

                            //start in foreground
                            Notification notification = mediaPlayerServiceRef.get().getMediaPlayerNotificationBuilder().createNotification(true);
                            mediaPlayerServiceRef.get().startForeground(MediaPlayerService.NOTIFICATION_ID, notification);

                        } catch (Exception e) {
                            Log.w(TAG, "Error while trying to init media player", e);
                            Toast.makeText(mediaPlayerServiceRef.get(), mediaPlayerServiceRef.get().getResources().getString(R.string.player_error), Toast.LENGTH_SHORT).show();

                            //stop the service
                            mediaPlayerServiceRef.get().getAudioManager().abandonAudioFocus(mediaPlayerServiceRef.get().getMediaPlayerOnAudioFocusChangeHandler());
                            mediaPlayerServiceRef.get().stopMediaPlayer();
                        }

                    } else {
                        Log.w(TAG, "No Internet connection - can't play stream");
                        Toast.makeText(mediaPlayerServiceRef.get(), mediaPlayerServiceRef.get().getResources().getString(R.string.player_network_unavailable), Toast.LENGTH_SHORT).show();

                        //stop the service
                        mediaPlayerServiceRef.get().getAudioManager().abandonAudioFocus(mediaPlayerServiceRef.get().getMediaPlayerOnAudioFocusChangeHandler());
                        mediaPlayerServiceRef.get().stopMediaPlayer();
                    }

                } else {
                    Log.w(TAG, "Audio focus request failed - can't play stream");
                    Toast.makeText(mediaPlayerServiceRef.get(), mediaPlayerServiceRef.get().getResources().getString(R.string.player_error), Toast.LENGTH_SHORT).show();

                    //stop the service
                    mediaPlayerServiceRef.get().getAudioManager().abandonAudioFocus(mediaPlayerServiceRef.get().getMediaPlayerOnAudioFocusChangeHandler());
                    mediaPlayerServiceRef.get().stopMediaPlayer();
                }

            } catch (Exception npe) {
                Log.e(TAG, "Exception in MediaPlayerService init", npe);
            }

        } else
            Log.e(TAG, "Reference to MediaPlayerService is null ! Nothing can be done");
    }

}
