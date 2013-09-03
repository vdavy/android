/**
 * 
 */
package com.stationmillenium.android.services;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.dto.CurrentTitleDTO;
import com.stationmillenium.android.exceptions.XMLParserException;
import com.stationmillenium.android.utils.LocalIntents;
import com.stationmillenium.android.utils.LocalIntentsData;
import com.stationmillenium.android.utils.Utils;
import com.stationmillenium.android.utils.XMLCurrentTitleParser;

/**
 * Service to manage current title grabbering
 * @author vincent
 *
 */
public class CurrentTitlePlayerService extends IntentService {

	private static final String TAG = "CurrentTitlePlayerService";
	private static final String FILENAME_PATTERN = "\\p{Alnum}{32}.png";

	/**
	 * Create a new player service
	 */
	public CurrentTitlePlayerService() {
		super(TAG);
	}

	/* (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		if (Utils.isNetworkAvailable(this)) { //is network ok ?
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Network is available - get XML data...");
			InputStream is = connectToURLSource(getResources().getString(R.string.player_current_song_url));
			if (is != null) { //input stream ok
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Input stream is OK - process it...");
				try {
					//get and parse XML data
					XMLCurrentTitleParser currentTitleParser = new XMLCurrentTitleParser(is);
					CurrentTitleDTO songDataDTO = currentTitleParser.parseXML();
					if (BuildConfig.DEBUG)
						Log.d(TAG, "Gathered song data : " + songDataDTO);
					
					//process image if needed
					if (songDataDTO.getCurrentSong().getMetadata() != null) {
						cleanupCache(); //clean up cache before adding new image
						manageBitmapImageWithCache(songDataDTO);
					}
					
					//send intent
					Intent intentToSend = new Intent(LocalIntents.CURRENT_TITLE_UPDATED.toString());
					intentToSend.putExtra(LocalIntentsData.CURRENT_TITLE.toString(), songDataDTO);
					if (BuildConfig.DEBUG)
						Log.d(TAG, "Send intent to update current title : " + intentToSend);
					LocalBroadcastManager.getInstance(this).sendBroadcast(intentToSend);
					sendBroadcast(intentToSend); //for the widget
					
				} catch (XMLParserException e) {
					Log.w(TAG, "Error while parsing XML data", e);
				}
				
			} else { //error while getting input stream
				if (BuildConfig.DEBUG)
					Log.d(TAG, "No input stream - stopping service...");
				Toast.makeText(this, getResources().getString(R.string.player_network_error), Toast.LENGTH_SHORT).show();
			}
				
		} else {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Network is unavailable - stopping service...");
			Toast.makeText(this, getResources().getString(R.string.player_network_unavailable), Toast.LENGTH_SHORT).show();
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
			connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
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
			Log.e(TAG, "Error while getting XML data", e);
			return null;
		}
	}
	
	/**
	 * Load the image into the cache if not found
	 * @param songDataDTO the song data
	 */
	private void manageBitmapImageWithCache(CurrentTitleDTO songDataDTO) {
		//extract file name
		String fileName = songDataDTO.getCurrentSong().getMetadata().getPath();
		fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
		
		//check if the image exists in cache
		File imageFile = new File(getCacheDir(), fileName);
		if (!imageFile.exists()) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, fileName + " does not exist - load it from server...");
			
			String imageUrl = getResources().getString(R.string.player_image_url_root) + songDataDTO.getCurrentSong().getMetadata().getPath();
			
			if (BuildConfig.DEBUG)	
				Log.d(TAG, "Image URL : " + imageUrl);
			InputStream imageIs = connectToURLSource(imageUrl);
			
			//write input stream data to file	
			OutputStream imageOs = null;
			try {
				imageOs = new BufferedOutputStream(new FileOutputStream(imageFile));
				int bufferSize = 1024;
				byte[] buffer = new byte[bufferSize];
				int len = 0;
				while ((len = imageIs.read(buffer)) != -1) {
				    imageOs.write(buffer, 0, len);
				}
				
			} catch (FileNotFoundException e) { //handle errors
				Log.e(TAG, "Error while creating cache image file", e);
			} catch (IOException e) {
				Log.e(TAG, "IO error with cache image file", e);
			} finally { //close streams
				try {
					if (imageIs != null)
						imageIs.close();
					if (imageOs != null)
						imageOs.close();
				} catch (IOException e) {
					Log.w(TAG, "Errors during closing of image streams", e);
				}
			}
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Image written to " + imageFile);
		}
		
		//image is cached - set the image file
		songDataDTO.getCurrentSong().setImage(imageFile);
	}

	/**
	 * Cleanup the app cache if cache size is too big
	 */
	private void cleanupCache() {
		Log.d(TAG, "Start cache cleanup...");
		
		//get image files from cache
		File[] cacheFiles = getCacheDir().listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return ((pathname != null) && (pathname.getName().matches(FILENAME_PATTERN)));
			}
		});
		List<File> cacheFileList = Arrays.asList(cacheFiles);
		Log.d(TAG, "Found cache files : " + cacheFileList);
		
		//compute cache size
		long totalCacheSize = 0;
		for (File cacheFile : cacheFileList) 
			totalCacheSize += cacheFile.length();
		Log.d(TAG, "Cache total size (KB) : " + (totalCacheSize / 1024));
		
		//is cache too big ?
		if (totalCacheSize > getResources().getInteger(R.integer.cache_size_max)) {
			Log.d(TAG, "Cache size too big - cleanup needed");
			
			//sort cache file list by last modified date 
			Collections.sort(cacheFileList, new Comparator<File>() {
				@Override
				public int compare(File lhs, File rhs) {
					if (lhs == null) //first file null
						return -1;
					else if (rhs == null) //second file null
						return 1;
					else if (lhs.length() < rhs.length()) //first file older than second
						return -1;
					else if (lhs.length() > rhs.length()) //second file older than first
						return 1;
					else //file have same age
						return 0;
				}
			});
			Log.d(TAG, "Sorted files by age : " + cacheFileList);
			
			//compute cache size to reach
			long cacheSizeToReach = getResources().getInteger(R.integer.cache_size_max) * getResources().getInteger(R.integer.cache_size_to_reach_percent) / 100;
			Log.d(TAG, "Cache size to reach (KB) : " + (cacheSizeToReach / 1024));
			
			//clean cache
			for (File cacheFile : cacheFileList) {
				long cacheFileSize = cacheFile.length(); //save length before file deletion
				cacheFile.delete();
				Log.d(TAG, "File deleted : " + cacheFile);
				
				//file deleted, so substract file length from cache
				totalCacheSize -= cacheFileSize;
				Log.d(TAG, "New cache size (KB) : " + (totalCacheSize / 1024));
				
				//check if cache size is now acceptable
				if (totalCacheSize <= cacheSizeToReach) {
					Log.d(TAG, "Cache size reached requested value - stop cleanup");
					break;
				} else
					Log.d(TAG, "Requested size for cache not reached - continue cleanup");
			}
			
		} else
			Log.d(TAG, "Cache size not too big");
	}
}
