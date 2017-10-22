
/**
 *
 */
package com.stationmillenium.android.activities.preferences;

import android.annotation.SuppressLint;
import android.app.backup.BackupManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.libutils.SharedPreferencesConstants;

/**
 * Fragment to manage application preferences
 *
 * @author vincent
 */
public class SharedPreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    //static intialization part
    private static final String TAG = "PreferencesFragment";
    private static final String APP_VERSION_PREFERENCE = "preferences_version";

    //preference fields
    private ListPreference newsNumber;
    private ListPreference autorestartDelay;

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //init view
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        //init fields
        newsNumber = (ListPreference) findPreference(SharedPreferencesConstants.TWEETS_DISPLAY_NUMBER);
        initNewsNumber();
        autorestartDelay = (ListPreference) findPreference(SharedPreferencesConstants.PLAYER_AUTORESTART_DELAY);
        initAutorestartDelay();
        initAppVersionPreference();

        //register callback for preference changes
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Initialize the news number params
     */
    private void initNewsNumber() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Init the news number param summary");
        }
        //set up news number summary
        //display the init value
        if (newsNumber.getValue() != null) {
            newsNumber.setSummary(newsNumber.getEntry());
        }

        //set up listener for updates
        newsNumber.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Update news number summary");
                }
                if (newValue != null) {
                    CharSequence summary = newsNumber.getEntries()[newsNumber.findIndexOfValue(newValue.toString())];
                    preference.setSummary(summary);
                } else {
                    preference.setSummary("");
                }
                return true;
            }
        });
    }

    /**
     * Initialize auto restart delay preference
     */
    private void initAutorestartDelay() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Init the autorestart player delay summary");
        }
        if (autorestartDelay.getValue() != null) {
            autorestartDelay.setSummary(getString(R.string.preferences_autorestart_player_timeout_summary, autorestartDelay.getEntry()));
        }

        autorestartDelay.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Update auto restart delay summary");
                }
                if (newValue != null) {
                    CharSequence summary = autorestartDelay.getEntries()[autorestartDelay.findIndexOfValue(newValue.toString())];
                    preference.setSummary(getString(R.string.preferences_autorestart_player_timeout_summary, summary));
                } else {
                    preference.setSummary("");
                }

                return true;
            }
        });
    }

    /**
     * Init the app version preference with version name of app
     */
    @SuppressWarnings("deprecation")
    private void initAppVersionPreference() {
        Preference appVersionPreference = findPreference(APP_VERSION_PREFERENCE);
        try { //try to set the version in app version preference summary
            String version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            appVersionPreference.setSummary(version);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Set app version preference with value : " + version);
            }
        } catch (NameNotFoundException e) { //if any error use default message
            Log.w(TAG, "Error while setting app version preference", e);
            appVersionPreference.setSummary(R.string.preferences_app_version_not_available);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Shared preferences changed - trigger backup");
        }
        new BackupManager(getActivity()).dataChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Unregister OnSharedPreferenceChangeListener");
        }
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

}
