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
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.utils.AppUtils;
import com.stationmillenium.android.utils.PiwikTracker;

import static com.stationmillenium.android.utils.PiwikTracker.PiwikPages.PREFERENCES;

/**
 * Activity to manage application preferences
 *
 * @author vincent
 */
public class SharedPreferencesActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    //static intialization part
    private static final String TAG = "PreferencesActivity";
    private static final String APP_VERSION_PREFERENCE = "preferences_version";

    //preference fields
    private ListPreference newsNumber;
    private ListPreference autorestartDelay;

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
        if ((AppUtils.isAPILevel14Available() && (getActionBar() != null))) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //init fields
        newsNumber = (ListPreference) findPreference(SharedPreferencesConstants.TWEETS_DISPLAY_NUMBER);
        initNewsNumber();
        autorestartDelay = (ListPreference) findPreference(SharedPreferencesConstants.PLAYER_AUTORESTART_DELAY);
        initAutorestartDelay();
        initAppVersionPreference();

        //register callback for preference changes
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Initialize the news number params
     */
    private void initNewsNumber() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Init the news number param summary");
        //set up news number summary
        //display the init value
        if (newsNumber.getValue() != null) {
            newsNumber.setSummary(newsNumber.getEntry());
        }

        //set up listener for updates
        newsNumber.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "Update news number summary");
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
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Init the autorestart player delay summary");

        if (autorestartDelay.getValue() != null) {
            autorestartDelay.setSummary(getString(R.string.preferences_autorestart_player_timeout_summary, autorestartDelay.getEntry()));
        }

        autorestartDelay.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "Update auto restart delay summary");
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

    @Override
    protected void onResume() {
        super.onResume();
        PiwikTracker.trackScreenView(getApplication(), PREFERENCES);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
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
     * Init the app version preference with version name of app
     */
    @SuppressWarnings("deprecation")
    private void initAppVersionPreference() {
        Preference appVersionPreference = findPreference(APP_VERSION_PREFERENCE);
        try { //try to set the version in app version preference summary
            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            appVersionPreference.setSummary(version);
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Set app version preference with value : " + version);

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
        new BackupManager(this).dataChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Unregister OnSharedPreferenceChangeListener");
        }
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Constants for shared preferences
     *
     * @author vincent
     */
    public interface SharedPreferencesConstants {
        String WIFI_ONLY = "preferences_player_wifi_only";
        String AUTOSTART_RADIO = "preferences_player_autostart";
        String TWEETS_DISPLAY_NUMBER = "preferences_tweets_display_number";
        String PIWIK_TRACKING_ID = "piwik_tracking_id";
        String PLAYER_AUTORESTART = "preferences_player_autorestart";
        String PLAYER_AUTORESTART_DELAY = "preferences_player_autorestart_delay";
    }

    /**
     * See : http://stackoverflow.com/questions/26509180/no-actionbar-in-preferenceactivity-after-upgrade-to-support-library-v21
     *
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.preferences_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
