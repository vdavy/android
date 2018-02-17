/**
 *
 */
package com.stationmillenium.android.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.stationmillenium.android.libutils.intents.LocalIntents;

import timber.log.Timber;

/**
 * {@link BroadcastReceiver} to program alarm on phone startup
 *
 * @author vincent
 */
public class AlarmBootCompletedBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmBootCompletedBR";

    /* (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Timber.d("Boot completed broadcast intent received - lauch alarm programmation");

            //launch alarm service
            Intent alarmIntent = new Intent(context, AlarmService.class);
            alarmIntent.setAction(LocalIntents.SET_ALARM_TIME.toString());
            AlarmService.enqueueWork(context, alarmIntent);
        }
    }

}
