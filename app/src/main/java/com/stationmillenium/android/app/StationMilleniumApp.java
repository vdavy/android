package com.stationmillenium.android.app;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.libutils.PiwikTracker;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * Main app class
 * Created by vincent on 21/09/14.
 */
public class StationMilleniumApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PiwikTracker.initPiwikTrackers(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Fabric.with(this, new Crashlytics());
            String userId = PiwikTracker.getUserId();
            if (userId != null) {
                Crashlytics.setUserIdentifier(userId);
            }
        }
    }

}

