/**
 * 
 */
package com.stationmillenium.android.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

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
	}


	private static final String TAG = "PreferencesActivity";

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//init view
		super.onCreate(savedInstanceState);
		Log.d(TAG, "Load preferences");
		addPreferencesFromResource(R.xml.preferences);

		//set up action bar - if available
		if (Utils.isAPILevel14Available()) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Log.d(TAG, "Navigate up");
			NavUtils.navigateUpFromSameTask(this);
			return true;

		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

}
