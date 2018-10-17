package com.stationmillenium.android.libutils.cast;

import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.stationmillenium.android.libutils.activities.PlayerState;

import timber.log.Timber;

import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_BUFFERING;
import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_PAUSED;
import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_PLAYING;

public class ActivitySessionManagerListener implements SessionManagerListener<CastSession> {

    @FunctionalInterface
    public interface StopPlayer {
        void stopPlayer();
    }

    @FunctionalInterface
    public interface StartPlayer {
        void startPlayer();
    }

    @FunctionalInterface
    public interface GetPlayerState {
        PlayerState getPlayerState();
    }

    private RemoteMediaClient.Callback rmcListener;
    private ActivityCastUtils activityCastUtils;

    private StopPlayer stopPlayer;
    private StartPlayer startPlayer;
    private GetPlayerState getPlayerState;

    public ActivitySessionManagerListener(RemoteMediaClient.Callback rmcListener, ActivityCastUtils activityCastUtils, StopPlayer stopPlayer, StartPlayer startPlayer, GetPlayerState getPlayerState) {
        this.rmcListener = rmcListener;
        this.activityCastUtils = activityCastUtils;
        this.stopPlayer = stopPlayer;
        this.startPlayer = startPlayer;
        this.getPlayerState = getPlayerState;
    }

    @Override
    public void onSessionStarting(CastSession castSession) {

    }

    @Override
    public void onSessionStarted(CastSession castSession, String s) {
        if (getPlayerState.getPlayerState() != PlayerState.STOPPED) {
            Timber.d("Switching to Chromecast");
            stopPlayer.stopPlayer();
            startPlayer.startPlayer(); //will start on chromecast
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
        activityCastUtils.checkIfPlayingOnChromecast(castSession.getRemoteMediaClient().getPlayerState());
        if ((castSession.getRemoteMediaClient().getPlayerState() == PLAYER_STATE_PLAYING
                ||castSession.getRemoteMediaClient().getPlayerState() == PLAYER_STATE_PAUSED
                ||castSession.getRemoteMediaClient().getPlayerState() == PLAYER_STATE_BUFFERING)
            && getPlayerState.getPlayerState() != PlayerState.STOPPED) {
            stopPlayer.stopPlayer();
        }
    }

    @Override
    public void onSessionResumeFailed(CastSession castSession, int i) {

    }

    @Override
    public void onSessionSuspended(CastSession castSession, int i) {

    }
}
