/**
 * 
 */
package com.stationmillenium.android.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.preferences.SharedPreferencesActivity.SharedPreferencesConstants;
import com.stationmillenium.android.utils.Utils;

/**
 * Service to track player starts for statistics
 * @author vincent
 *
 */
public class StatsTrackerService extends IntentService {

	private static final String TAG = "StatsTrackerService";
	//private static final int
	
	/**
	 * Create a new {@link StatsTrackerService}
	 */
	public StatsTrackerService() {
		super(TAG);
	}

	/* (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		if (Utils.isNetworkAvailable(this)) { //is network ok ?
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Network is available - send tracking info...");
			
			//check if we already have an tracking id
			String trackingId = PreferenceManager.getDefaultSharedPreferences(this).getString(SharedPreferencesConstants.PIWIK_TRACKING_ID, "");
			String trackingURL = "";
			if ((trackingId != null) && (trackingId.equals(""))) { //we don't have an id
				if (BuildConfig.DEBUG)
					Log.d(TAG, "No tracking ID found");
				trackingURL = getString(R.string.tracking_url);
			} else { //we already have an id
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Tracking ID found : " + trackingId);
				trackingURL = getString(R.string.tracking_url_with_id) + trackingId;
			}
			
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Tracking URL to use : " + trackingURL);
			
			//send the tracking info
			InputStream is = connectToURLSource(trackingURL);
			if (is != null) { //input stream ok
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Input stream is OK - process it...");
				
				try {
					//read tracking id
					BufferedReader br = new BufferedReader(new InputStreamReader(is));
					trackingId = br.readLine();
					
					if (BuildConfig.DEBUG)
						Log.d(TAG, "Read tracking ID : " + trackingId);
					
					//save tracking id into preferences
					Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
					editor.putString(SharedPreferencesConstants.PIWIK_TRACKING_ID, trackingId);
					editor.apply();
					
					//close streams
					br.close();
					is.close();
					
				} catch (IOException e) {
					Log.w(TAG, "Error while parsing tracking data", e);
				}
				
			} else { //error while getting input stream
				if (BuildConfig.DEBUG)
					Log.d(TAG, "No input stream");
			}
				
		} else {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Network is unavailable");
		}
	}

	/**
	 * Connect to the URL source	
	 * @param urlText the URL to connect as text
	 * @return the {@link InputStream} of the connection
	 */
	private InputStream connectToURLSource(String urlText) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Connect to server to get data");
		try {
			//set up connection
			URL url = new URL(urlText);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(Integer.parseInt(getResources().getString(R.string.player_connection_connect_timeout)));
			connection.setReadTimeout(Integer.parseInt(getResources().getString(R.string.player_connection_read_timeout)));
			connection.setRequestMethod(getResources().getString(R.string.player_connection_request_method));
			connection.setRequestProperty("Content-Type", "text/html; charset=utf-8");
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Connection to use : " + connection);
			
			//connect
			connection.connect();
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Response code : " + connection.getResponseCode());
			return connection.getInputStream();
			
		} catch (MalformedURLException e) {
			Log.e(TAG, "Error with URL", e);
			return null;
		} catch (IOException e) {
			Log.e(TAG, "Error while getting data", e);
			return null;
		}
	}

}
