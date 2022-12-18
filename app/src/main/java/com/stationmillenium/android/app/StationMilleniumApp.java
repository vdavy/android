package com.stationmillenium.android.app;

import android.app.Application;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.libutils.PiwikTracker;

import timber.log.Timber;

/**
 * Main app class
 * Created by vincent on 21/09/14.
 */
public class StationMilleniumApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PiwikTracker.initPiwikTrackers(this, BuildConfig.DEBUG);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            String userId = PiwikTracker.getUserId();
            if (userId != null) {
                FirebaseCrashlytics.getInstance().setUserId(userId);
            }
        }
    }

}

