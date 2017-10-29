/**
 *
 */
package com.stationmillenium.android.activities.preferences;

import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.databinding.PreferencesActivityBinding;
import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.libutils.drawer.DrawerUtils;

import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.PREFERENCES;

/**
 * Activity to manage application preferences
 *
 * @author vincent
 */
public class SharedPreferencesActivity extends AppCompatActivity {

    //static intialization part
    private static final String TAG = "PreferencesActivity";

    private DrawerUtils drawerUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //init view
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Load main preferences");
        }

        PreferencesActivityBinding preferencesActivityBinding = DataBindingUtil.setContentView(this, R.layout.preferences_activity);
        setSupportActionBar(preferencesActivityBinding.prefToolbar);

        drawerUtils = new DrawerUtils(this, preferencesActivityBinding.prefDrawerLayout, preferencesActivityBinding.prefToolbar, R.id.nav_drawer_settings);
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

    @Override
    protected void onResume() {
        super.onResume();
        PiwikTracker.trackScreenView(PREFERENCES);
    }


}
