/**
 *
 */
package com.stationmillenium.android.services;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.stationmillenium.android.libutils.PiwikTracker;

/**
 * Service to track player starts for statistics
 *
 * @author vincent
 */
public class StatsTrackerService extends JobIntentService {

    /**
     * Unique job ID for this service.
     */
    private static final int JOB_ID = 1002;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, StatsTrackerService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        PiwikTracker.trackStream();
    }

}
