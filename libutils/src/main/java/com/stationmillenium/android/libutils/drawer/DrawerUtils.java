package com.stationmillenium.android.libutils.drawer;

import android.app.Activity;
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

/**
 * Utils to easily manage drawer
 * Created by vincent on 22/01/17.
 */
public class DrawerUtils {

    private static final String TAG = "DrawerUtils";
    private ActionBarDrawerToggle drawerToggle;

    /**
     * Init the drawer toogle
     * @param activity the activity
     * @param drawerLayout the drawer layout
     * @param toolbar the toolbar
     */
    public DrawerUtils(@NonNull Activity activity, @NonNull DrawerLayout drawerLayout, @NonNull Toolbar toolbar, @IdRes int selectedItem) {
        drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        NavigationView navigationView = (NavigationView) drawerLayout.findViewById(R.id.nav_drawer);
        navigationView.setCheckedItem(selectedItem);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d(TAG, "Drawer selected item : " + item);
                return true;
            }
        });
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
