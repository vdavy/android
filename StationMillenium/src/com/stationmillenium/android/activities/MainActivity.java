package com.stationmillenium.android.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.fragments.AntennaGridFragment;
import com.stationmillenium.android.activities.fragments.HomeFragment;
import com.stationmillenium.android.activities.fragments.LinksFragment;
import com.stationmillenium.android.activities.fragments.ReplayWebViewFragment;
import com.stationmillenium.android.utils.LocalIntentsData;
import com.stationmillenium.android.utils.Utils;

/**
 * Main activity : drawer manager and home  
 * @author vincent
 *
 */
public class MainActivity extends ActionBarActivity {

	private static final String TAG = "MainActivity";
	
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
		if (savedInstanceState == null) { //if no saved instance - otherwise fragment will be automatically added
	        getSupportFragmentManager()
	        	.beginTransaction()
	        	.add(R.id.content_frame, new HomeFragment())
	        	.commit();
		}
		
        //twitter test 
//        ConfigurationBuilder cb = new ConfigurationBuilder();
//        cb.setOAuthConsumerKey("j20KwHQ4RMIsT5cab4avMA")
//        	.setOAuthConsumerSecret("p1EapWpV2v4uqCUv3GnsNMbMSUdNAp4uiJTioWg1Rg")
////        	.setOAuthAccessToken("523409073-T6riNlkLIz9zqFok9UR8X5ZjCFCTGaMKLgT37Fk")
////        	.setOAuthAccessTokenSecret("iYGHimFhuYRKfFoDTqLFklhrebOjiWCMphO2K45WxIw");
//        	.setApplicationOnlyAuthEnabled(true)
//        	.setUseSSL(true);
//        Twitter twitter = new TwitterFactory(cb.build()).getInstance();
//        try {
//        	twitter.getOAuth2Token(); //load token 
//			ResponseList<Status> tweetsList = twitter.getUserTimeline("millenium22");
//			
//			if (tweetsList.get(0).getURLEntities().length > 0) //load tweet url
//				tweetsList.get(0).getURLEntities()[0].getURL();
//			
//			Log.d(TAG, tweetsList.toString());
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//classic menu case
		if (item.getItemId() == R.id.action_settings) {
			Intent settingsIntent = new Intent(this, SharedPreferencesActivity.class);
			startActivity(settingsIntent);
			return true;
		} else 
			return super.onOptionsItemSelected(item);
	}

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
	
	/**
	 * Display the {@link ReplayWebViewFragment}
	 * @param view the {@link View} originating the event
	 */
	public void displayReplayFragment(View view) {
		Log.d(TAG, "Display the replay fragment");
		displayFragment(new ReplayWebViewFragment());
	}
	
	/**
	 * Display the {@link AntennaGridFragment}
	 * @param view the {@link View} originating the event
	 */
	public void displayAntennaGridFragment(View view) {
		Log.d(TAG, "Display the antenna grid fragment");
		displayFragment(new AntennaGridFragment());			
	}
	
	/**
	 * Display the {@link LinksFragment}
	 * @param view the {@link View} originating the event
	 */
	public void displayLinksFragment(View view) {
		Log.d(TAG, "Display the links fragment");
		displayFragment(new LinksFragment());			
	}
	
	/**
	 * Display a {@link Fragment} with effects and back stack
	 * @param fragmentToDisplay the {@link Fragment} to display
	 */
	private void displayFragment(Fragment fragmentToDisplay) {
		getSupportFragmentManager()
			.beginTransaction()
			.setCustomAnimations(R.anim.change_fragment_fadein, 
					R.anim.change_fragment_fadeout,
					R.anim.change_fragment_fadein,
					R.anim.change_fragment_fadeout)
			.replace(R.id.content_frame, fragmentToDisplay)			
			.addToBackStack(null)
			.commit();
	}
		
}
