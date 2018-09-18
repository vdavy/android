package com.stationmillenium.android.libutils.activities;

import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

public class PlayerActivitySessionManagerListener implements SessionManagerListener<CastSession> {

    private RemoteMediaClient.Callback rmcListener;

    public PlayerActivitySessionManagerListener(RemoteMediaClient.Callback rmcListener) {
        this.rmcListener = rmcListener;
    }

    @Override
    public void onSessionStarting(CastSession castSession) {

    }

    @Override
    public void onSessionStarted(CastSession castSession, String s) {

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

    }

    @Override
    public void onSessionResumeFailed(CastSession castSession, int i) {

    }

    @Override
    public void onSessionSuspended(CastSession castSession, int i) {

    }
}
