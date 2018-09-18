package com.stationmillenium.android.libutils.activities;

import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.view.MenuItem;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.PlayerActivity;
import com.stationmillenium.android.activities.fragments.PlayerFragment;

import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_BUFFERING;
import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_PAUSED;
import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_PLAYING;

public class PlayerActivityCastUtils {

    private static final String CAST_MIME_TYPE = "audio/mp3";

    private PlayerActivity playerActivity;
    private RemoteMediaClient remoteMediaClient;
    private PlayerFragment playerFragment;
    private RemoteMediaClient.Callback rmcListener = new RemoteMediaClient.Callback() {
        @Override
        public void onStatusUpdated() {
            switch (remoteMediaClient.getPlayerState()) {
                case PLAYER_STATE_PLAYING:
                case PLAYER_STATE_PAUSED:
                case PLAYER_STATE_BUFFERING:
                    playerFragment.setPlayingOnChromecast(false);
                    break;

                default:
                    playerFragment.setPlayingOnChromecast(false);
            }
        }
    };

    public PlayerActivityCastUtils(PlayerActivity playerActivity, PlayerFragment playerFragment) {
        this.playerActivity = playerActivity;
        this.playerFragment = playerFragment;
    }

    public void startCast(CastSession castSession) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        mediaMetadata.putString(MediaMetadata.KEY_ARTIST, "Station");
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, "Millenium");
        mediaMetadata.addImage(new WebImage(Uri.parse(playerActivity.getString(R.string.image_url))));
        MediaInfo mediaInfo = new MediaInfo.Builder(playerActivity.getString(R.string.player_stream_url))
                .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
                .setContentType(CAST_MIME_TYPE)
                .setMetadata(mediaMetadata)
                .build();
        MediaLoadOptions mediaLoadOptions = new MediaLoadOptions.Builder().setAutoplay(true).build();
        remoteMediaClient = castSession.getRemoteMediaClient();
        remoteMediaClient.registerCallback(rmcListener);
        remoteMediaClient.load(mediaInfo, mediaLoadOptions);
        Snackbar.make(playerActivity.getPlayerActivityBinding().playerCoordinatorLayout, R.string.player_casting, Snackbar.LENGTH_SHORT).show();
    }

    public void showIntroductoryOverlay(MenuItem castMenu) {
        if ((castMenu != null) && castMenu.isVisible()) {
            new Handler().post(() -> new IntroductoryOverlay.Builder(
                    playerActivity, castMenu)
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
