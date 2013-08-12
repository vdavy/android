/**
 * 
 */
package com.stationmillenium.android.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.utils.Utils;

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
	}


	private static final String TAG = "PreferencesActivity";
	
	private ListPreference newsNumber;

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//init view
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Load preferences");
		addPreferencesFromResource(R.xml.preferences);

		//set up action bar - if available
		if (Utils.isAPILevel14Available()) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		//set up news number summary
		//display the init value
		newsNumber = (ListPreference) findPreference(SharedPreferencesConstants.TWEETS_DISPLAY_NUMBER);
		final String[] arrayValues = getResources().getStringArray(R.array.preferences_links_tweets_number_values);
		if (newsNumber.getValue() != null) {
			int position = Integer.parseInt(newsNumber.getValue()) - 1;
			newsNumber.setSummary(arrayValues[position]);
		}
		
		//set upt listener for updates
		newsNumber.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				int position = Integer.parseInt(newValue.toString()) - 1;
				preference.setSummary(arrayValues[position]);
				return false;
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
	
}
