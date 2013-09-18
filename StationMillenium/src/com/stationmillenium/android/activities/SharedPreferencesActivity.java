/**
 * 
 */
package com.stationmillenium.android.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.services.AlarmService;
import com.stationmillenium.android.utils.Utils;
import com.stationmillenium.android.utils.intents.LocalIntents;
import com.stationmillenium.android.utils.preferences.ListPreferenceMultiSelect;
import com.stationmillenium.android.utils.preferences.TimePreference;

/**
 * Activity to manage application preferences
 * @author vincent
 *
 */
public class SharedPreferencesActivity extends PreferenceActivity {

	/**
	 * Constants for shared preferences
	 * @author vincent
	 *
	 */
	public interface SharedPreferencesConstants {
		String WIFI_ONLY = "preferences_player_wifi_only";
		String AUTOSTART_RADIO = "preferences_player_autostart";
		String TWEETS_DISPLAY_NUMBER = "preferences_tweets_display_number";
		String PIWIK_TRACKING_ID = "piwik_tracking_id";
		String ALARM_ENABLED = "preferences_alarm_enabled";
		String ALARM_TIME = "preferences_alarm_time";
		String ALARM_DAYS = "preferences_alarm_days";
		String ALARM_DAYS_STRING = "preferences_alarm_days_string";
	}

	//static intialization part
	private static final String TAG = "PreferencesActivity";
	private static final String ALARM_DAYS_LIST_STRING_SEPARATOR = "\\|";
	
	//preference fields
	private CheckBoxPreference alarmEnabled;
	private ListPreference newsNumber;
	private TimePreference alarmTime;
	private MultiSelectListPreference alarmDaysList;
	private ListPreference alarmDaysListString;
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//init view
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Load preferences");
		
		if (Utils.isAPILevel11Available()) {
			addPreferencesFromResource(R.xml.preferences);
			initializePreferenceFields(true); //load fields
			initAlarmDaysList(); //init alarm days field
			
		} else {
			addPreferencesFromResource(R.xml.preferences_api_level_10);
			initializePreferenceFields(false); //load fields
			initAlarmDaysListAsString(); //init alarm days string field
		}
		
		//set up action bar - if available
		if (Utils.isAPILevel14Available()) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		//init fields
		initNewsNumber();
		initAlarmTime();
		initAlarmEnabled();
	}

	/**
	 * Init the alarm enabled field
	 */
	private void initAlarmEnabled() {
		alarmEnabled.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				long alarmTime = PreferenceManager.getDefaultSharedPreferences(SharedPreferencesActivity.this).getLong(SharedPreferencesConstants.ALARM_TIME, 0);
				if (alarmTime != 0) { //alarm time is set
					sendUpdateAlarmTimeIntent(); //send intent to update alarm
					return true;
				} else { //alarm time is not set
					if (BuildConfig.DEBUG)
						Log.d(TAG, "Alarm time not set - can't program alarm");
					Toast.makeText(SharedPreferencesActivity.this, R.string.alarm_no_time_set, Toast.LENGTH_SHORT).show();
					return false;
				}
			}
		});
	}
	
	/**
	 * Init the alarm time field
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initAlarmTime() {
		alarmTime.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				sendUpdateAlarmTimeIntent(); //send intent to update alarm
				return true;
			}
		});
	}

	/**
	 * Initialize preference fields
	 * @param isAPILevel11Available if the API level 11 is available
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initializePreferenceFields(boolean isAPILevel11Available) {
		alarmEnabled = (CheckBoxPreference) findPreference(SharedPreferencesConstants.ALARM_ENABLED);
		alarmTime = (TimePreference) findPreference(SharedPreferencesConstants.ALARM_TIME);
		newsNumber = (ListPreference) findPreference(SharedPreferencesConstants.TWEETS_DISPLAY_NUMBER);
		if (isAPILevel11Available)
			alarmDaysList = (MultiSelectListPreference) findPreference(SharedPreferencesConstants.ALARM_DAYS);
		else
			alarmDaysListString = (ListPreferenceMultiSelect) findPreference(SharedPreferencesConstants.ALARM_DAYS_STRING);
	}
	
	/**
	 * Initialize the news number params
	 */
	private void initNewsNumber() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Init the news number param summary");
		//set up news number summary
		//display the init value
		final String[] arrayValues = getResources().getStringArray(R.array.preferences_links_tweets_number_values);
		if (newsNumber.getValue() != null) {
			int position = Integer.parseInt(newsNumber.getValue()) - 1;
			newsNumber.setSummary(arrayValues[position]);
		}
		
		//set up listener for updates
		newsNumber.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Update news number summary");
				int position = Integer.parseInt(newValue.toString()) - 1;
				preference.setSummary(arrayValues[position]);
				return true;
			}
		});
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Navigate up");
			NavUtils.navigateUpFromSameTask(this);
			return true;

		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}
	
	/**
	 * Initilize the alarm days list
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initAlarmDaysList() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Init the alaram days list summary");
		//get values
		Set<String> selectedDays = alarmDaysList.getValues();
		
		//manage init summary
		manageAlarmDaysSummary(alarmDaysList, selectedDays);
		
		//add update summary listener
		alarmDaysList.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@SuppressWarnings("unchecked")
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Update the alaram days list summary");
				manageAlarmDaysSummary(preference, (Set<String>) newValue);
				sendUpdateAlarmTimeIntent(); //update alarm
				return true;
			}
		});
	}
	
	/**
	 * Initilize the alarm days list, in string format (for api level 10 only)
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initAlarmDaysListAsString() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Init the alarm days list summary");
		//get values
		String alarmDaysString = alarmDaysListString.getValue();
		Set<String> selectedDays = convertAlarmDaysStringToSet(alarmDaysString);
		
		//manage init summary
		manageAlarmDaysSummary(alarmDaysListString, selectedDays);
		
		//add update summary listener
		alarmDaysListString.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Update the alarm days list summary");
				Set<String> selectedDays = convertAlarmDaysStringToSet((String) newValue);
				manageAlarmDaysSummary(preference, selectedDays);	
				sendUpdateAlarmTimeIntent(); //update alarm
				return true;
			}
		});
	}

	/**
	 * Convert the alarm days list from {@link String} to {@link Set}
	 * @param alarmDaysString the original {@link String}
	 * @return the converted {@link Set}
	 */
	private Set<String> convertAlarmDaysStringToSet(String alarmDaysString) {
		Set<String> selectedDays = null;
		if ((alarmDaysString != null) && (!alarmDaysString.equals(""))) {
			String[] alarmDaysArray = alarmDaysString.split(ALARM_DAYS_LIST_STRING_SEPARATOR);
			selectedDays = new HashSet<String>(Arrays.asList(alarmDaysArray));
		}
		return selectedDays;
	}

	/**
	 * Set the correct summary on alarm days {@link MultiSelectListPreference}
	 * @param alarmDaysList the {@link MultiSelectListPreference}
	 * @param selectedDays the {@link Set} of selected days
	 */
	private void manageAlarmDaysSummary(Preference alarmDaysList, Set<String> selectedDays) {
		String[] dayNames = getResources().getStringArray(R.array.preferences_alarm_days);
		if (selectedDays != null) { //if some data available
			List<String> selectedDaysList = new ArrayList<String>(selectedDays);
			Collections.sort(selectedDaysList);
			
			//manage summary
			if (!selectedDaysList.isEmpty()) { //if some data - set up summary
				String summary =  "";
				int i = 1;	
				for (String day : selectedDaysList) {
					summary += dayNames[Integer.parseInt(day)];
					if (i < selectedDaysList.size()) {
						summary += getString(R.string.preferences_alarm_activation_days_separator) + " ";
						i++;
					}
				}
				alarmDaysList.setSummary(summary);
				
			} else { //display no data summary
				alarmDaysList.setSummary(getString(R.string.preferences_alarm_activation_days_no_selection));
			}
			
		} else { //display no data summary
			alarmDaysList.setSummary(getString(R.string.preferences_alarm_activation_days_no_selection));
		}
	}
	
	/**
	 * Send intent to updaye alarm time
	 */
	private void sendUpdateAlarmTimeIntent() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Send intent to update alarm time");
		Intent alarmIntent = new Intent(this, AlarmService.class);
		alarmIntent.setAction(LocalIntents.SET_ALARM_TIME.toString());
		startService(alarmIntent);
	}
	
	@Override
	protected void onResume() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Resuming the preferences screen");
		
		super.onResume(); //resume all activity
		
		//refresh the alarm enabled value
		boolean alarmEnabledValue = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SharedPreferencesConstants.ALARM_ENABLED, false);
		alarmEnabled.setChecked(alarmEnabledValue);
	}
	
}
