/**
 * 
 */
package com.stationmillenium.android.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.utils.intents.LocalIntents;

/**
 * {@link BroadcastReceiver} to program alarm on phone startup
 * @author vincent
 *
 */
public class AlarmBootCompletedBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = "AlarmBootCompletedBroadcastReceiver";
	
	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Boot completed broadcast intent received - lauch alarm programmation");
		
		//launch alarm service
		Intent alarmIntent = new Intent(context, AlarmService.class);
		alarmIntent.setAction(LocalIntents.SET_ALARM_TIME.toString());
		context.startService(alarmIntent);
	}

}