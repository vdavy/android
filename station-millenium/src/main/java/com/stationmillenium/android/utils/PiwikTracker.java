package com.stationmillenium.android.utils;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.app.StationMilleniumApp;

import org.piwik.sdk.Tracker;

/**
 * Utils to help with piwik tracker
 * Created by vdavy on 29/01/16.
 */
public class PiwikTracker {

    private static final String TAG = "PiwikTracker";

    public enum PiwikPages {
        PLAYER("/player"),
        ANTENNA_GRID("/main/antenna-grid"),
        HOME("/main"),
        LINKS("/main/links"),
        REPLAY("/main/replay"),
        ALARM("/alarm-preferences"),
        PREFERENCES("/preferences"),
        SONG_SEARCH_HISTORY("/song-search-history"),
        SONG_HISTORY_DISPLAY_IMAGE("/song-search-history/display-image"),
        APP_INVITE("/app-invite");

        private String path;

        PiwikPages(String path) {
            this.path = path;
        }

        String getPath() {
            return path;
        }
    }

    /**
     * Track stream goal
     * @param application the application
     */
    public static void trackStream(@NonNull Application application) {
        Log.d(TAG, "PIWIK / Track stream");
        Tracker tracker = ((StationMilleniumApp) application).getPiwikStreamTracker();
        if (tracker != null) {
            tracker.trackGoal(application.getResources().getInteger(R.integer.piwik_goal_id));
        }
    }

    /**
     * Track stream goal
     * @param application the application
     */
    public static void trackScreenView(@NonNull Application application, @NonNull PiwikPages page) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "PIWIK / Track page : " + page);
        }
        Tracker tracker = ((StationMilleniumApp) application).getPiwikAppTracker();
        if (tracker != null) {
            tracker.trackScreenView(page.getPath());
        }
    }
}
