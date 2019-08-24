package com.stationmillenium.android.libutils;

import android.content.Context;

import androidx.annotation.NonNull;

import org.matomo.sdk.Matomo;
import org.matomo.sdk.Tracker;
import org.matomo.sdk.TrackerBuilder;
import org.matomo.sdk.extra.TrackHelper;

import timber.log.Timber;

/**
 * Utils to help with piwik tracker
 * Created by vdavy on 29/01/16.
 */
public class PiwikTracker {

    private static final String APP_TRACKER = "App tracker";
    private static final String STREAM_TRACKER = "Stream tracker";

    private static Tracker piwikAppTracker;
    private static Tracker piwikStreamTracker;
    private static int piwikGoalId;

    public enum PiwikPages {
        PLAYER("/player"),
        PLAYER_CHROMECAST("/player/chromecast"),
        ANTENNA_GRID("/main/antenna-grid"),
        HOME("/main"),
        REPLAY("/replay"),
        REPLAY_ITEM("/replay/item"),
        REPLAY_ITEM_DOWNLOAD("/replay/item/download"),
        REPLAY_ITEM_CHROMECAST("/replay/item/chromecast"),
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
        Matomo matomo = Matomo.getInstance(context);
        piwikAppTracker = new TrackerBuilder(context.getString(R.string.piwik_url), context.getResources().getInteger(R.integer.piwik_app_site_id), APP_TRACKER).build(matomo);
        piwikStreamTracker = new TrackerBuilder(context.getString(R.string.piwik_url), context.getResources().getInteger(R.integer.piwik_stream_site_id), STREAM_TRACKER).build(matomo);
        piwikGoalId = context.getResources().getInteger(R.integer.piwik_goal_id);
    }

    /**
     * Track stream goal
     */
    public static void trackStream() {
        Timber.d("PIWIK / Track stream");
        if (!BuildConfig.DEBUG) {
            if (piwikStreamTracker != null) {
                TrackHelper.track().goal(piwikGoalId).with(piwikStreamTracker);
            }
        } else {
            Timber.v("Debug mode - not sending stream tracking info");
        }
    }

    /**
     * Track activity view
     * @param page the page to track
     */
    public static void trackScreenView(@NonNull PiwikPages page) {
        Timber.d("PIWIK / Track page : %s", page);

        if (!BuildConfig.DEBUG) {
            if (piwikAppTracker != null) {
                TrackHelper.track().screen(page.getPath()).with(piwikAppTracker);
            }
        } else {
            Timber.v("Debug mode - not sending page tracking info");
        }
    }

    /**
     * Track activity view, with title
     * @param page the page to track
     * @param title the page title
     */
    public static void trackScreenViewWithTitle(@NonNull PiwikPages page, @NonNull String title) {
        Timber.d("PIWIK / Track page : " + page + " - with title : " + title);
        if (!BuildConfig.DEBUG) {
            if (piwikAppTracker != null) {
                TrackHelper.track().screen(page.getPath()).title(title).with(piwikAppTracker);
            }
        } else {
            Timber.v("Debug mode - not sending page tracking info");
        }
    }

    /**
     * Piwik User Id
     * @return the user ID, or null if not available
     */
    public static String getUserId() {
        return (piwikAppTracker != null) ? piwikAppTracker.getUserId() : null;
    }

}
