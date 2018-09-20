package com.stationmillenium.android.libutils.activities;

import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.stationmillenium.android.activities.PlayerActivity;
import com.stationmillenium.android.activities.fragments.PlayerFragment;

import timber.log.Timber;

import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_BUFFERING;
import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_PAUSED;
import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_PLAYING;

public class PlayerActivitySessionManagerListener implements SessionManagerListener<CastSession> {

    private RemoteMediaClient.Callback rmcListener;
    private PlayerActivity playerActivity;
    private PlayerFragment playerFragment;
    private PlayerActivityCastUtils playerActivityCastUtils;

    public PlayerActivitySessionManagerListener(RemoteMediaClient.Callback rmcListener, PlayerActivity playerActivity, PlayerFragment playerFragment, PlayerActivityCastUtils playerActivityCastUtils) {
        this.rmcListener = rmcListener;
        this.playerActivity = playerActivity;
        this.playerFragment = playerFragment;
        this.playerActivityCastUtils = playerActivityCastUtils;
    }

    @Override
    public void onSessionStarting(CastSession castSession) {

    }

    @Override
    public void onSessionStarted(CastSession castSession, String s) {
        if (playerFragment.getPlayerState() != PlayerState.STOPPED) {
            Timber.d("Switching to Chromecast");
            playerActivity.stopPlayer();
            playerActivity.startPlayer(); //will start on chromecast
        }
    }

    @Override
    public void onSessionStartFailed(CastSession castSession, int i) {

    }

    @Override
    public void onSessionEnding(CastSession castSession) {

    }

    @Override
    public void onSessionEnded(CastSession castSession, int i) {
        if (castSession != null && castSession.getRemoteMediaClient() != null && rmcListener != null) {
            castSession.getRemoteMediaClient().unregisterCallback(rmcListener);
        }
    }

    @Override
    public void onSessionResuming(CastSession castSession, String s) {

    }

    @Override
    public void onSessionResumed(CastSession castSession, boolean b) {
        Timber.d("Switching to Chromecast on resume");
        playerActivityCastUtils.checkIfPlayingOnChromecast(castSession.getRemoteMediaClient().getPlayerState());
        if ((castSession.getRemoteMediaClient().getPlayerState() == PLAYER_STATE_PLAYING
                ||castSession.getRemoteMediaClient().getPlayerState() == PLAYER_STATE_PAUSED
                ||castSession.getRemoteMediaClient().getPlayerState() == PLAYER_STATE_BUFFERING)
            && playerFragment.getPlayerState() != PlayerState.STOPPED) {
            playerActivity.stopPlayer();
        }
    }

    @Override
    public void onSessionResumeFailed(CastSession castSession, int i) {

    }

    @Override
    public void onSessionSuspended(CastSession castSession, int i) {

    }
}
