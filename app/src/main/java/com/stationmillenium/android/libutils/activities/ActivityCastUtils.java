package com.stationmillenium.android.libutils.activities;

import android.app.Activity;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.PlayerActivity;
import com.stationmillenium.android.libutils.PiwikTracker;

import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_BUFFERING;
import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_PAUSED;
import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_PLAYING;
import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.PLAYER_CHROMECAST;

public class ActivityCastUtils {

    private static final String CAST_MIME_TYPE = "audio/mp3";

    @FunctionalInterface
    public interface PlayingOnChromecast {
        void setPlayingOnChromecast(boolean playingOnChromecast);
    }

    private Activity activity;
    private RemoteMediaClient remoteMediaClient;
    private PlayingOnChromecast playingOnChromecast;
    private RemoteMediaClient.Callback rmcListener = new RemoteMediaClient.Callback() {
        @Override
        public void onStatusUpdated() {
            checkIfPlayingOnChromecast(remoteMediaClient.getPlayerState());
        }
    };

    public void checkIfPlayingOnChromecast(int playerStatus) {
        switch (playerStatus) {
            case PLAYER_STATE_PLAYING:
            case PLAYER_STATE_PAUSED:
            case PLAYER_STATE_BUFFERING:
                playingOnChromecast.setPlayingOnChromecast(false);
                break;

            default:
                playingOnChromecast.setPlayingOnChromecast(false);
        }
    }

    public ActivityCastUtils(PlayerActivity activity, PlayingOnChromecast playingOnChromecast) {
        this.activity = activity;
        this.playingOnChromecast = playingOnChromecast;
    }

    public void startCast(CastSession castSession, String artist, String title, String imageURL, String mediaURL, int streamType, View snackBarView) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        mediaMetadata.putString(MediaMetadata.KEY_ARTIST, artist);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, title);
        mediaMetadata.addImage(new WebImage(Uri.parse(imageURL)));
        MediaInfo mediaInfo = new MediaInfo.Builder(mediaURL)
                .setStreamType(streamType)
                .setContentType(CAST_MIME_TYPE)
                .setMetadata(mediaMetadata)
                .build();
        MediaLoadOptions mediaLoadOptions = new MediaLoadOptions.Builder().setAutoplay(true).build();
        remoteMediaClient = castSession.getRemoteMediaClient();
        remoteMediaClient.registerCallback(rmcListener);
        remoteMediaClient.load(mediaInfo, mediaLoadOptions);
        Snackbar.make(snackBarView, R.string.player_casting, Snackbar.LENGTH_SHORT).show();
        PiwikTracker.trackScreenView(PLAYER_CHROMECAST);
    }

    public void showIntroductoryOverlay(MenuItem castMenu) {
        if ((castMenu != null) && castMenu.isVisible()) {
            new Handler().post(() -> new IntroductoryOverlay.Builder(
                    activity, castMenu)
                    .setTitleText(R.string.introducing_cast)
                    .setSingleTime()
                    .build()
                    .show());
        }
    }


    public RemoteMediaClient.Callback getRmcListener() {
        return rmcListener;
    }

}
