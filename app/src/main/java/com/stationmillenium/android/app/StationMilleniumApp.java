package com.stationmillenium.android.app;

import android.app.Application;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.libutils.PiwikTracker;

import timber.log.Timber;

/**
 * Main app class
 * Created by vincent on 21/09/14.
 */
public class StationMilleniumApp extends Application {

    private static final String TAG = "StationMilleniumApp";

    @Override
    public void onCreate() {
        super.onCreate();
        PiwikTracker.initPiwikTrackers(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

}

