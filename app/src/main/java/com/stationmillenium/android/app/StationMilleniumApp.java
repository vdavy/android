package com.stationmillenium.android.app;

import android.app.Application;
import android.util.Log;

import com.stationmillenium.android.R;

import org.piwik.sdk.Piwik;
import org.piwik.sdk.Tracker;

import java.net.MalformedURLException;

/**
 * Main app class
 * Created by vincent on 21/09/14.
 */
public class StationMilleniumApp extends Application {

    private static final String TAG = "StationMilleniumApp";

    private Tracker piwikAppTracker;
    private Tracker piwikStreamTracker;


    @Override
    public void onCreate() {
        super.onCreate();

        piwikAppTracker = initPiwikAppTracker();
        piwikStreamTracker = initPiwikStreamTracker();
    }

    private Tracker initPiwikAppTracker() {
        try {
            return Piwik.getInstance(this).newTracker(getString(R.string.piwik_url), getResources().getInteger(R.integer.piwik_app_site_id));
        } catch (MalformedURLException e) {
            Log.w(TAG, "Error while piwik app tracker init", e);
            return null;
        }
    }

    private Tracker initPiwikStreamTracker() {
        try {
            return Piwik.getInstance(this).newTracker(getString(R.string.piwik_url), getResources().getInteger(R.integer.piwik_stream_site_id));
        } catch (MalformedURLException e) {
            Log.w(TAG, "Error while piwik stream tracker init", e);
            return null;
        }
    }

    public Tracker getPiwikAppTracker() {
        return piwikAppTracker;
    }

    public Tracker getPiwikStreamTracker() {
        return piwikStreamTracker;
    }
}

