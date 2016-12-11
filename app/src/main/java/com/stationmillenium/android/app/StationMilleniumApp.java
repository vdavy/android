package com.stationmillenium.android.app;

import android.app.Application;
import android.util.Log;

import com.stationmillenium.android.R;
import com.stationmillenium.android.libutils.PiwikTracker;

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
    }

}

