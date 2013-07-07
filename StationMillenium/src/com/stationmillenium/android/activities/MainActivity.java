package com.stationmillenium.android.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.fragments.HomeFragment;
import com.stationmillenium.android.activities.fragments.HomeFragment2;
import com.stationmillenium.android.utils.LocalIntentsData;
import com.stationmillenium.android.utils.Utils;

/**
 * Main activity : drawer manager and home  
 * @author vincent
 *
 */
public class MainActivity extends FragmentActivity implements ListView.OnItemClickListener {

	private static final String TAG = "MainActivity";
	
	//widgets
	private DrawerLayout drawerLayout;
	private ListView leftDrawer;
	private ActionBarDrawerToggle abdt;
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			StrictMode.ThreadPolicy.Builder tpBuilder = new StrictMode.ThreadPolicy.Builder()
	            .detectAll()
	            .penaltyLog()
	            .penaltyDialog();
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				tpBuilder.penaltyFlashScreen();
			
	        StrictMode.setThreadPolicy(tpBuilder.build());
			
	        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
		         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		                 .detectLeakedClosableObjects()
		                 .detectLeakedRegistrationObjects()
		                 .detectLeakedSqlLiteObjects()
		                 .penaltyLog()
		                 .build());
	         }
	     }
 		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//get widgets
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		leftDrawer = (ListView) findViewById(R.id.left_drawer);
		
		//populate the drawer menu
		String[] menuArray = getResources().getStringArray(R.array.menu_array);
 		leftDrawer.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_menu_entry, R.id.drawer_entry_text, menuArray));
 		
 		
 		//init action bar button for drawer
 		final String originalActionBarTitle = String.valueOf(getTitle());
 		final String openedDrawerActionBarTitle = getResources().getString(R.string.action_bar_menu_title);
 		if (Utils.isAPILevel14Available()) {
	 		abdt = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.open_drawer_desc, R.string.close_drawer_desc) {
	 			
	 			@SuppressLint("NewApi")
				@Override
	 			public void onDrawerOpened(View drawerView) {
	 				super.onDrawerOpened(drawerView);
	 				getActionBar().setTitle(openedDrawerActionBarTitle);
	 			}
	 			
	 			@SuppressLint("NewApi")
				@Override
	 			public void onDrawerClosed(View drawerView) {
	 				getActionBar().setTitle(originalActionBarTitle);
	 				super.onDrawerClosed(drawerView);
	 			}
	 		};
	 		drawerLayout.setDrawerListener(abdt);
	 		getActionBar().setDisplayHomeAsUpEnabled(true);
	        getActionBar().setHomeButtonEnabled(true);
 		}
        leftDrawer.setOnItemClickListener(this);
        
        //add home fragment 
        getSupportFragmentManager().beginTransaction().add(R.id.content_frame, new HomeFragment()).commit();
	}

	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (Utils.isAPILevel14Available())
        	// Sync the toggle state after onRestoreInstanceState has occurred.
        	abdt.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (Utils.isAPILevel14Available())
        	abdt.onConfigurationChanged(newConfig);
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//handle drawer menu through action bar
		if ((Utils.isAPILevel14Available()) && (abdt.onOptionsItemSelected(item))) {
			return true;
	    }
		
		//classic menu case
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent settingsIntent = new Intent(this, SharedPreferencesActivity.class);
			startActivity(settingsIntent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
		
	}

	@SuppressLint("NewApi")
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		//change action bar title
		if (Utils.isAPILevel14Available()) {
			String actionBarTitle = getResources().getStringArray(R.array.menu_array)[position];
			getActionBar().setTitle(actionBarTitle);
		}
		
		//manage list
		leftDrawer.setItemChecked(position, true);
		drawerLayout.closeDrawer(leftDrawer);
		
		//navigate or swap fragments
		switch (position) {
		case 1:
			Log.d(TAG, "Launch player");
			Intent playerIntent = new Intent(this, PlayerActivity.class);
			if (!Utils.isMediaPlayerServiceRunning(getApplicationContext())) 
				playerIntent.putExtra(LocalIntentsData.ALLOW_AUTOSTART.toString(), true);
				
			startActivity(playerIntent);
			break;

		default:
			HomeFragment2 homeFragment2 = new HomeFragment2();
			getSupportFragmentManager()
				.beginTransaction()
				.setCustomAnimations(R.anim.change_fragment_fadein, R.anim.change_fragment_fadeout,
						R.anim.change_fragment_fadein, R.anim.change_fragment_fadeout)
				.replace(R.id.content_frame, homeFragment2)
				.commit();
			break;
		}
	}
	
	public void onClickDrawerButton(View view) {
		Log.d(TAG, "Drawer button clicked");
		drawerLayout.openDrawer(leftDrawer);
	}
	
}
