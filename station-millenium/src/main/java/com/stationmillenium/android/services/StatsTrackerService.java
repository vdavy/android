/**
 *
 */
package com.stationmillenium.android.services;

import android.app.IntentService;
import android.content.Intent;

import com.stationmillenium.android.R;
import com.stationmillenium.android.app.StationMilleniumApp;

/**
 * Service to track player starts for statistics
 *
 * @author vincent
 */
public class StatsTrackerService extends IntentService {

    private static final String TAG = "StatsTrackerService";
    //private static final int

    /**
     * Create a new {@link StatsTrackerService}
     */
    public StatsTrackerService() {
        super(TAG);
    }

    /* (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        ((StationMilleniumApp) getApplication()).getPiwikTracker().trackGoal(getResources().getInteger(R.integer.piwik_goal_id));
    }
}
