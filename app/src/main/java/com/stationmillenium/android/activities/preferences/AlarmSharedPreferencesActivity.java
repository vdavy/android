/**
 *
 */
package com.stationmillenium.android.activities.preferences;

import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.databinding.AlarmPreferencesActivityBinding;
import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.libutils.drawer.DrawerUtils;
import com.stationmillenium.android.libutils.intents.LocalIntents;
import com.stationmillenium.android.services.AlarmService;

import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.ALARM;

/**
 * Activity to manage alarm preferences
 *
 * @author vincent
 */
public class AlarmSharedPreferencesActivity extends AppCompatActivity {

    //static intialization part
    private static final String TAG = "AlarmPreferenceActivity";

    private AlarmPreferencesActivityBinding alarmPreferencesActivityBinding;
    private DrawerUtils drawerUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //init view
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Load alarm preferences");
        }

        alarmPreferencesActivityBinding = DataBindingUtil.setContentView(this, R.layout.alarm_preferences_activity);
        setSupportActionBar(alarmPreferencesActivityBinding.alarmPrefToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        drawerUtils = new DrawerUtils(this, alarmPreferencesActivityBinding.alarmPrefDrawerLayout, alarmPreferencesActivityBinding.alarmPrefToolbar, R.id.nav_drawer_alarm);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PiwikTracker.trackScreenView(ALARM);
    }

    /**
     * Constants for shared preferences
     *
     * @author vincent
     */
    public interface AlarmSharedPreferencesConstants {
        String ALARM_ENABLED = "preferences_alarm_enabled";
        String ALARM_TIME = "preferences_alarm_time";
        String ALARM_DAYS = "preferences_alarm_days";
        String ALARM_DAYS_STRING = "preferences_alarm_days_string";
        String ALARM_VOLUME = "preferences_alarm_volume";
    }

    /**
     * Send intent to updaye alarm time
     */
    public void sendUpdateAlarmTimeIntent() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Send intent to update alarm time");
        }
        Intent alarmIntent = new Intent(this, AlarmService.class);
        alarmIntent.setAction(LocalIntents.SET_ALARM_TIME.toString());
        startService(alarmIntent);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerUtils.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerUtils.onPostCreate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerUtils.onConfigurationChanged(newConfig);
    }

}
