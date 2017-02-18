package com.stationmillenium.android.libutils.drawer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.stationmillenium.android.libutils.R;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;

/**
 * Utils to easily manage drawer
 * Created by vincent on 22/01/17.
 */
public class DrawerUtils {

    private static final String TAG = "DrawerUtils";
    private static final String ACTIVITY_PREFIX = ".activities";
    private static final String MAIN_ACTIVITY = ".HomeActivity";
    private static final String ANTENNA_GRID_ACTIVITY = ".AntennaGridActivity";
    private static final String PLAYER_ACTIVITY = ".PlayerActivity";
    private static final String REPLAY_ACTIVITY = ".replay.activities.ReplayActivity";
    private static final String SONG_SEARCH_HISTORY_ACTIVITY = ".songsearchhistory.SongSearchHistoryActivity";
    private static final String ALARM_PREFERENCES_ACTIVITY = ".preferences.AlarmSharedPreferencesActivity";
    private static final String PREFERENCES_ACTIVITY = ".preferences.SharedPreferencesActivity";

    private ActionBarDrawerToggle drawerToggle;

    /**
     * Init the drawer toogle
     * @param activity the activity
     * @param drawerLayout the drawer layout
     * @param toolbar the toolbar
     */
    public DrawerUtils(@NonNull final Activity activity, @NonNull final DrawerLayout drawerLayout, @NonNull Toolbar toolbar, @IdRes final int selectedItem) {
        drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        NavigationView navigationView = (NavigationView) drawerLayout.findViewById(R.id.nav_drawer);
        navigationView.setCheckedItem(selectedItem);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() != selectedItem) {
                    Log.d(TAG, "Drawer selected item : " + item);
                    Intent intent = null;
                    if (item.getItemId() == R.id.nav_drawer_home) {
                        intent = initIntent(activity, ACTIVITY_PREFIX + MAIN_ACTIVITY);
                    } else if (item.getItemId() == R.id.nav_drawer_player) {
                        intent = initIntent(activity, ACTIVITY_PREFIX + PLAYER_ACTIVITY);
                        intent.putExtra(LocalIntentsData.ALLOW_AUTOSTART.toString(), true);
                    } else if (item.getItemId() == R.id.nav_drawer_antenna) {
                        intent = initIntent(activity, ACTIVITY_PREFIX + ANTENNA_GRID_ACTIVITY);
                    } else if (item.getItemId() == R.id.nav_drawer_replay) {
                        intent = initIntent(activity, REPLAY_ACTIVITY);
                    } else if (item.getItemId() == R.id.nav_drawer_song_history) {
                        intent = initIntent(activity, ACTIVITY_PREFIX + SONG_SEARCH_HISTORY_ACTIVITY);
                    } else if (item.getItemId() == R.id.nav_drawer_alarm) {
                        intent = initIntent(activity, ACTIVITY_PREFIX + ALARM_PREFERENCES_ACTIVITY);
                    } else if (item.getItemId() == R.id.nav_drawer_settings) {
                        intent = initIntent(activity, ACTIVITY_PREFIX + PREFERENCES_ACTIVITY);
                    }
                    activity.startActivity(intent);
                }

                drawerLayout.closeDrawers();
                return false;
            }
        });

    }

    private Intent initIntent(Context context, String activityClassName) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(context, context.getPackageName() + activityClassName));
        return intent;
    }

    /**
     * Call this method in {@link Activity#onPostCreate(Bundle)}
     */
    public void onPostCreate() {
        drawerToggle.syncState();
    }

    /**
     * Call this method in {@link Activity#onConfigurationChanged(Configuration)}
     * @param newConfig the new configuration
     */
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Call this in {@link Activity#onOptionsItemSelected(MenuItem)}
     * @param item the selected option item
     * @return if the item was handled
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item);
    }
}
