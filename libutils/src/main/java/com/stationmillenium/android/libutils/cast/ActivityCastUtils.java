package com.stationmillenium.android.libutils.cast;

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
import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.libutils.R;

import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_BUFFERING;
import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_PAUSED;
import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_PLAYING;

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
                playingOnChromecast.setPlayingOnChromecast(true);
                break;

            default:
                playingOnChromecast.setPlayingOnChromecast(false);
        }
    }

    public ActivityCastUtils(Activity activity, PlayingOnChromecast playingOnChromecast) {
        this.activity = activity;
        this.playingOnChromecast = playingOnChromecast;
    }

    public void startCast(CastSession castSession, String artist, String title, String imageURL, String mediaURL, int streamType, long playPosition, View snackBarView, PiwikTracker.PiwikPages piwikPages) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        mediaMetadata.putString(MediaMetadata.KEY_ARTIST, artist);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, title);
        mediaMetadata.addImage(new WebImage(Uri.parse(imageURL != null ? imageURL : activity.getString(R.string.cast_default_image_url))));
        MediaInfo mediaInfo = new MediaInfo.Builder(mediaURL)
                .setStreamType(streamType)
                .setContentType(CAST_MIME_TYPE)
                .setMetadata(mediaMetadata)
                .build();
        MediaLoadOptions.Builder mediaLoadOptionsBuilder = new MediaLoadOptions.Builder()
                .setAutoplay(true);
        if (playPosition > 0) {
            mediaLoadOptionsBuilder.setPlayPosition(playPosition);
        }
        remoteMediaClient = castSession.getRemoteMediaClient();
        remoteMediaClient.registerCallback(rmcListener);
        remoteMediaClient.load(mediaInfo, mediaLoadOptionsBuilder.build());
        Snackbar.make(snackBarView, R.string.player_casting, Snackbar.LENGTH_SHORT).show();
        playingOnChromecast.setPlayingOnChromecast(true);
        PiwikTracker.trackScreenView(piwikPages);
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

    public void endingSession() {
        playingOnChromecast.setPlayingOnChromecast(false);
    }

}
