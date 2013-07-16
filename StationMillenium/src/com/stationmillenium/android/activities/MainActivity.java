package com.stationmillenium.android.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.fragments.HomeFragment;
import com.stationmillenium.android.utils.LocalIntentsData;
import com.stationmillenium.android.utils.Utils;

/**
 * Main activity : drawer manager and home  
 * @author vincent
 *
 */
public class MainActivity extends FragmentActivity {

	private static final String TAG = "MainActivity";
	
	//widgets
	
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
	
        //add home fragment 
        getSupportFragmentManager().beginTransaction().add(R.id.content_frame, new HomeFragment()).commit();
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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

//	@SuppressLint("NewApi")
//	@Override
//	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		//change action bar title
//		if (Utils.isAPILevel14Available()) {
//			String actionBarTitle = getResources().getStringArray(R.array.menu_array)[position];
//			getActionBar().setTitle(actionBarTitle);
//		}
//		
//		//manage list
//		leftDrawer.setItemChecked(position, true);
//		drawerLayout.closeDrawer(leftDrawer);
//		
//		//navigate or swap fragments
//		switch (position) {
//		case 1:
//			
//			break;
//
//		default:
//			HomeFragment2 homeFragment2 = new HomeFragment2();
//			getSupportFragmentManager()
//				.beginTransaction()
//				.setCustomAnimations(R.anim.change_fragment_fadein, R.anim.change_fragment_fadeout,
//						R.anim.change_fragment_fadein, R.anim.change_fragment_fadeout)
//				.replace(R.id.content_frame, homeFragment2)
//				.commit();
//			break;
//		}
//	}

	/**
	 * Start the {@link PlayerActivity}
	 * @param view the {@link View} originating the event
	 */
	public void startPlayer(View view) {
		Log.d(TAG, "Launch player");
		Intent playerIntent = new Intent(this, PlayerActivity.class);
		if (!Utils.isMediaPlayerServiceRunning(getApplicationContext())) 
			playerIntent.putExtra(LocalIntentsData.ALLOW_AUTOSTART.toString(), true);
			
		startActivity(playerIntent);
	}
	
}
