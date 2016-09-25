package com.stationmillenium.android.libutils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.piwik.sdk.Piwik;
import org.piwik.sdk.TrackHelper;
import org.piwik.sdk.Tracker;

import java.net.MalformedURLException;

/**
 * Utils to help with piwik tracker
 * Created by vdavy on 29/01/16.
 */
public class PiwikTracker {

    private static final String TAG = "PiwikTracker";

    private static Tracker piwikAppTracker;
    private static Tracker piwikStreamTracker;
    private static int piwikGoalId;

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
        SHARE_APP_INVITE("/share/app-invite"),
        SHARE_SOCIAL_NETWORKS("/share/social-networks");

        private String path;

        PiwikPages(String path) {
            this.path = path;
        }

        String getPath() {
            return path;
        }
    }

    /**
     * Init the Piwik trackers
     * @param context the context to get resources
     */
    public static void initPiwikTrackers(@NonNull Context context) {
        try {
            piwikAppTracker = Piwik.getInstance(context).newTracker(context.getString(R.string.piwik_url), context.getResources().getInteger(R.integer.piwik_app_site_id));
        } catch (MalformedURLException e) {
            Log.w(TAG, "Error while piwik app tracker init", e);
        }
        try {
            piwikStreamTracker = Piwik.getInstance(context).newTracker(context.getString(R.string.piwik_url), context.getResources().getInteger(R.integer.piwik_stream_site_id));
        } catch (MalformedURLException e) {
            Log.w(TAG, "Error while piwik stream tracker init", e);
        }
        piwikGoalId = context.getResources().getInteger(R.integer.piwik_goal_id);
    }

    /**
     * Track stream goal
     */
    public static void trackStream() {
        Log.d(TAG, "PIWIK / Track stream");
        if (!BuildConfig.DEBUG) {
            if (piwikStreamTracker != null) {
                TrackHelper.track().goal(piwikGoalId).with(piwikStreamTracker);
            }
        } else {
            Log.v(TAG, "Debug mode - not sending stream tracking info");
        }
    }

    /**
     * Track stream goal
     * @param page the page to track
     */
    public static void trackScreenView(@NonNull PiwikPages page) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "PIWIK / Track page : " + page);
        }
        if (!BuildConfig.DEBUG) {
            if (piwikAppTracker != null) {
                TrackHelper.track().screen(page.getPath()).with(piwikAppTracker);
            }
        } else {
            Log.v(TAG, "Debug mode - not sending page tracking info");
        }
    }
}
