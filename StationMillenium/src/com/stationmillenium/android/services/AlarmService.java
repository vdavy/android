/**
 * 
 */
package com.stationmillenium.android.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.AlarmSharedPreferencesActivity.AlarmSharedPreferencesConstants;
import com.stationmillenium.android.utils.Utils;
import com.stationmillenium.android.utils.intents.LocalIntents;
import com.stationmillenium.android.utils.intents.LocalIntentsData;

/**
 * Service to manage alarm intent : launch player when alarm elapsed and set pending intents
 * @author vincent
 *
 */
public class AlarmService extends IntentService {

	private static final String TAG = "AlarmService";
	private static final String ALARM_DAYS_LIST_STRING_SEPARATOR = "\\|";

	private Handler handler;

	/**
	 * Create a new {@link AlarmService}
	 */
	public AlarmService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		handler = new Handler(); //create handler to display toasts
	}

	/* (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this); //load preferences first
		if (LocalIntents.SET_ALARM_TIME.toString().equals(intent.getAction())) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Set alarm time intent received : " + intent);

			if (preferences.getBoolean(AlarmSharedPreferencesConstants.ALARM_ENABLED, false)) { //check if alarm is enabled
				//get repeat days
				int[] repeatDays = getRepeatDays(preferences); 
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Alarm repeat days : " + repeatDays);

				Calendar alarmTime = getAlarmTimeCalendar(preferences); //get the alarm time set in a calendar
				if (alarmTime != null) { //if alarm time is set
					if (repeatDays.length == 0) { //no repeat days case
						if (BuildConfig.DEBUG)
							Log.d(TAG, "No repeat days for alarm");

						//if alarm time elapsed : set it for tomorrow
						if (Calendar.getInstance().after(alarmTime)) {
							if (BuildConfig.DEBUG)
								Log.d(TAG, "Alarm time elapsed - set if for next day");
							alarmTime.add(Calendar.DATE, 1);
						} 

						//program alarm
						programAlarm(alarmTime);

					} else { //process alarm with repeat days
						if (BuildConfig.DEBUG)
							Log.d(TAG, "Alarm with repeat days");
						int dayIndex = convertCalendarDayIndexToArrayIndex(alarmTime); //convert the day index
						int nextRepeatDay = -1;
						for (int day : repeatDays) { //check each day
							if ((day > dayIndex) 
									|| ((day == dayIndex) && (Calendar.getInstance().before(alarmTime)))) { //if we found the next day for repeat
								nextRepeatDay = day; //save it and exit
								break;
							}
						}

						//the next repeat day has not been found, so use the first in next week
						if (nextRepeatDay == -1)
							nextRepeatDay = repeatDays[0];

						//deal with the case of the repeat day is the same day
						if ((nextRepeatDay == dayIndex) && (Calendar.getInstance().after(alarmTime))) {
							if (BuildConfig.DEBUG)
								Log.d(TAG, "Repeat day is the same day as today - repeat in one week");
							alarmTime.add(Calendar.DATE, 7);

						} else if (nextRepeatDay != dayIndex) { //as repeat date is not today, we need to add some day delay
							if (BuildConfig.DEBUG)
								Log.d(TAG, "Repeat day is not today : add some delay");
							alarmTime.setFirstDayOfWeek(Calendar.SUNDAY); //set start day of week as sunday
							nextRepeatDay += 2; //shift 2 days forward
							if (nextRepeatDay > 7) //at the end of the week, return to the beginning
								nextRepeatDay -= 7; 
							if (nextRepeatDay < Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) //repeat day is next week : add delay of 7 days
								alarmTime.add(Calendar.DATE, 7);
							else if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) //repeat day is nearly next week, only 1 day delay is needed
								alarmTime.add(Calendar.DATE, 1);
								
							alarmTime.set(Calendar.DAY_OF_WEEK, nextRepeatDay); //set the next repeat day
						}

						//program the alarm
						programAlarm(alarmTime);
					}
				
				} else { //alarm time not set
					if (BuildConfig.DEBUG)
						Log.d(TAG, "Alarm time not set - can't program alarm");
					
					displayToast(getString(R.string.alarm_no_time_set));
					preferences.edit() //disable the alarm
						.putBoolean(AlarmSharedPreferencesConstants.ALARM_ENABLED, false)
						.apply();
				}

			} else { //alarm disabled, cancel intent
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Alarm disabled - cancel intent");
				cancelAlarm();
			}

		} else { //launch player
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Received intent to launch player");

			//launch player
			if (!Utils.isMediaPlayerServiceRunning(this)) { //do not launch media player twice
				if (!Utils.isWifiOnlyAndWifiNotConnected(this)) { //if wifi requested, check it is enabled and connected
					//start player service
					if (BuildConfig.DEBUG)
						Log.d(TAG, "Start media player service for alarm triggering");
					Intent playIntent = new Intent(this, MediaPlayerService.class);
					playIntent.putExtra(LocalIntentsData.RESUME_PLAYER_ACTIVITY.toString(), false);
					playIntent.putExtra(LocalIntentsData.GET_VOLUME_FROM_PREFERENCES.toString(), true);
					startService(playIntent);
					displayToast(getString(R.string.alarm_triggered));

				} else {
					Log.w(TAG, "Wifi requested but not connected for alarm");
					displayToast(getString(R.string.player_no_wifi));
				}

			} else {
				displayToast(getString(R.string.alarm_already_triggered));
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Alarm not triggered, media player already running");
			}

			//next, properly reset alarm preference
			if (getRepeatDays(preferences).length == 0) {
				if (BuildConfig.DEBUG)
					Log.d(TAG, "No repeat days set - disable alarm");

				//no repeat days set, so disable alarm
				preferences.edit()
					.putBoolean(AlarmSharedPreferencesConstants.ALARM_ENABLED, false)
					.apply();

			} else {
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Repeat days set - restart service to set alarm");

				//start the service
				Intent alarmIntent = new Intent(this, AlarmService.class);
				alarmIntent.setAction(LocalIntents.SET_ALARM_TIME.toString());
				startService(alarmIntent);
			}
		}
	}

	/**
	 * Cancel the alarm
	 */
	private void cancelAlarm() {
		PendingIntent playPendingIntent = createLaunchPlayerPendingIntent();
		getAlarmManager().cancel(playPendingIntent);
	}

	/**
	 * Create a launch player {@link PendingIntent}
	 */
	private PendingIntent createLaunchPlayerPendingIntent() {
		Intent launchPlayerIntent = new Intent(this, AlarmService.class);
		launchPlayerIntent.setAction(LocalIntents.ON_ALARM_TIME_ELAPSED.toString());
		PendingIntent launchPlayerPendingIntent = PendingIntent.getService(this, 0, launchPlayerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		return launchPlayerPendingIntent;
	}

	/**
	 * Get the {@link AlarmManager}
	 * @return the {@link AlarmManager}
	 */
	private AlarmManager getAlarmManager() {
		return (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	}

	/**
	 * Get the alarm repeat days
	 * @param preferences the {@link SharedPreferences}
	 * @return the repeat days into a int array
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private int[] getRepeatDays(SharedPreferences preferences) {
		Set<String> repeatDaysSet = null;

		//get the day values as properly way
		if (Utils.isAPILevel11Available()) { 
			repeatDaysSet = preferences.getStringSet(AlarmSharedPreferencesConstants.ALARM_DAYS, null);
		} else {
			String repeatDaysString = preferences.getString(AlarmSharedPreferencesConstants.ALARM_DAYS_STRING, null);
			if ((repeatDaysString != null) && (!repeatDaysString.equals(""))) {
				String[] alarmDaysArray = repeatDaysString.split(ALARM_DAYS_LIST_STRING_SEPARATOR);
				repeatDaysSet = new HashSet<String>(Arrays.asList(alarmDaysArray));
			}
		}

		//process set to convert to array
		if ((repeatDaysSet != null) && (!repeatDaysSet.isEmpty())) {
			int[] finalArray = new int[repeatDaysSet.size()];
			List<String> selectedDaysList = new ArrayList<String>(repeatDaysSet);
			Collections.sort(selectedDaysList);
			for (int i = 0; i < selectedDaysList.size(); i++) 
				finalArray[i] = Integer.parseInt(selectedDaysList.get(i));

			return finalArray;

		} else //if no data, return empty array
			return new int[0];
	}

	/**
	 * Get the {@link Calendar} with alarm time set 
	 * @param preferences the {@link SharedPreferences}
	 * @return the {@link Calendar}
	 */
	private Calendar getAlarmTimeCalendar(SharedPreferences preferences) {
		long timePickerTime = preferences.getLong(AlarmSharedPreferencesConstants.ALARM_TIME, 0); //get the time in millis from timepicker
		if (timePickerTime != 0) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Alarm time is set - use it");

			Calendar setTimeCalendar = Calendar.getInstance(); //set in into calendar
			setTimeCalendar.setTimeInMillis(timePickerTime);

			Calendar currentTime = Calendar.getInstance(); //set a current day calendar with hour and minute alarm time
			currentTime.set(Calendar.HOUR_OF_DAY, setTimeCalendar.get(Calendar.HOUR_OF_DAY));
			currentTime.set(Calendar.MINUTE, setTimeCalendar.get(Calendar.MINUTE));
			currentTime.set(Calendar.SECOND, 0);
			currentTime.set(Calendar.MILLISECOND, 0);
			return currentTime;

		} else { //alarm time not set, so return null
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Alarm time is not set - use current time with delta");
			return null;
		}
	}

	/**
	 * Program the alarm
	 * @param programTime the {@link Calendar} for time to program alarm
	 */
	private void programAlarm(Calendar programTime) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Program alarm");

		//program alarm
		PendingIntent playPendingIntent = createLaunchPlayerPendingIntent();
		getAlarmManager().set(AlarmManager.RTC_WAKEUP, programTime.getTimeInMillis(), playPendingIntent);

		//display toast
		String toastText = null; 
		if (programTime.get(Calendar.DATE) == Calendar.getInstance().get(Calendar.DATE)) { //display only hour and minute
			toastText = getString(R.string.alarm_set_same_day, 
					addZeroIfNeeded(programTime.get(Calendar.HOUR_OF_DAY)), 
					addZeroIfNeeded(programTime.get(Calendar.MINUTE)));
		} else { //display hour, minutes and day
			String[] dayNamesArray = getResources().getStringArray(R.array.preferences_alarm_days);
			int dayIndex = convertCalendarDayIndexToArrayIndex(programTime); //convert the day index
			toastText = getString(R.string.alarm_set, 
					dayNamesArray[dayIndex], 
					addZeroIfNeeded(programTime.get(Calendar.HOUR_OF_DAY)), 
					addZeroIfNeeded(programTime.get(Calendar.MINUTE)));
		}

		//show toast
		displayToast(toastText);
	}

	/**
	 * Add a "0" before value if < 10
	 * @param value the value 
	 * @return the converted {@link String}
	 */
	private String addZeroIfNeeded(int value) {
		return (value < 10) ? "0" + value : "" + value;
	}

	/**
	 * Convert a {@link Calendar} day index (from 1 to 7 with index 1 is sunday) to internal array index (from 0 to 6 with index 0 is monday)
	 * @param calendar the {@link Calendar} input 
	 * @return the index as int
	 */
	private int convertCalendarDayIndexToArrayIndex(Calendar calendar) {
		int dayIndex = calendar.get(Calendar.DAY_OF_WEEK);
		if (dayIndex == Calendar.SUNDAY) //shift day
			dayIndex += 7;
		dayIndex -= 2;
		return dayIndex;
	}

	/**
	 * Display a toast using the handler
	 * @param toastText the text
	 */
	private void displayToast(final String toastText) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();				
			}
		});
	}

}
