/**
 * 
 */
package com.stationmillenium.android.activities.fragments;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.MainActivity;
import com.stationmillenium.android.utils.TweetItem;
import com.stationmillenium.android.utils.Utils;

/**
 * Links fragment
 * @author vincent
 *
 */
public class LinksFragment extends ListFragment {

	
	/**
	 * Tweets loader based on {@link AsyncTask}
	 * Based on Twitter4j API 
	 * @see http://www.twitter4j.org 
	 * @author vincent
	 *
	 */
	private class TweetsLoader extends AsyncTask<String, Void, List<TweetItem>> {

		private static final String TAG = "TweetsLoader";
		private static final int MAX_TWEETS = 6;

		//references
		private String consumerKey;
		private String consumerSecret;
		private WeakReference<ArrayAdapter<TweetItem>> arrayAdapterRef;
		
		/**
		 * Create a new {@link TweetsLoader}
		 * @param consumerKey the Twitter consumer key
		 * @param consumerSecret the Twitter consumer secret
		 * @param arrayAdapater the {@link ArrayAdapter} to fill-in list
		 */
		public TweetsLoader(String consumerKey, String consumerSecret, ArrayAdapter<TweetItem> arrayAdapater) {
			this.consumerKey = consumerKey;
			this.consumerSecret = consumerSecret;
			arrayAdapterRef = new WeakReference<ArrayAdapter<TweetItem>>(arrayAdapater);
		}

		@Override
		protected List<TweetItem> doInBackground(String... params) {
			Log.d(TAG, "Load tweets...");

			//build tweeter auth conf
	        ConfigurationBuilder cb = new ConfigurationBuilder();
	        cb.setOAuthConsumerKey(consumerKey)
	        	.setOAuthConsumerSecret(consumerSecret)
	        	.setApplicationOnlyAuthEnabled(true)
	        	.setUseSSL(true);
	        Twitter twitter = new TwitterFactory(cb.build()).getInstance();
	        try {
	        	twitter.getOAuth2Token(); //load token 
				ResponseList<twitter4j.Status> tweetsList = twitter.getUserTimeline(params[0]); //load tweets
				
				List<TweetItem> tweetItemList = new ArrayList<TweetItem>();
				for (twitter4j.Status status : tweetsList) { //process each tweet
					//load tweet url
					String tweetURL = null;
					if ((status.getURLEntities() != null) && (status.getURLEntities().length > 0)) 
						tweetURL = status.getURLEntities()[0].getURL();
					
					tweetItemList.add(new TweetItem(status.getText(), tweetURL)); //add to list
					
					if (tweetItemList.size() >= MAX_TWEETS) //limit tweets list
						break;
				}
				
				Log.d(TAG, "Gathered tweets list : " + tweetItemList);
				return tweetItemList;
				
			} catch (Exception e) { //if any error occurs
				Log.e(TAG, "Error while getting latest tweets", e);
				return null;
			}
			
		}
		
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		protected void onPostExecute(List<TweetItem> result) {
			if ((result != null) && (arrayAdapterRef.get() != null)) {
				Log.d(TAG, "Fill-in tweets list");
				arrayAdapterRef.get().clear();
				if (Utils.isAPILevel11Available())
					arrayAdapterRef.get().addAll(result); //addAll available only in API level 11
				else {
					for (TweetItem tweet : result)
						arrayAdapterRef.get().add(tweet);
				}
				
			} else {
				Log.w(TAG, "Error while filling-in tweets list - tweets list : " + result 
						+ " - array adapater : " + arrayAdapterRef.get());
			}
		}

		

	}

	//static parts
	private final static String TAG = "LinksFragment";
	private final static String ARRAY_ADAPATER_BUNDLE = "ARRAY_ADAPATER_BUNDLE"; 
	
	//instance vars
	private TweetsLoader tweetsLoader;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ArrayAdapter<TweetItem> arrayAdapter = new ArrayAdapter<TweetItem>(getActivity(), R.layout.tweets_list_item, R.id.tweets_item_text);
		setListAdapter(arrayAdapter);
		
		//get saved array data if any available
		if ((savedInstanceState != null) && (savedInstanceState.getSerializable(ARRAY_ADAPATER_BUNDLE) != null)) { 
			Log.d(TAG, "Array data available on view creation");
			Object[] tweetItemArray = (Object[]) savedInstanceState.getSerializable(ARRAY_ADAPATER_BUNDLE);
			for (Object tweet : tweetItemArray)
				arrayAdapter.add((TweetItem) tweet);
		}
		
		return inflater.inflate(R.layout.links_fragment, container, false);
	}

	@SuppressWarnings("unchecked")
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "Resuming links fragment");

		//load tweets data if needed
		if (getListAdapter().getCount() == 0) {
			Log.d(TAG, "Launch latest tweets loading...");
			tweetsLoader = new TweetsLoader(getString(R.string.tweeter_consumer_key), 
					getString(R.string.tweeter_consumer_secret), 
					(ArrayAdapter<TweetItem>) getListAdapter());
			tweetsLoader.execute(getString(R.string.tweeter_user_name));
		} 
		
		//set title and activity full screen
		((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.links_activity_title);
		
		//social networks buttons : add links
		//facebook
		((ImageView) getView().findViewById(R.id.open_facebook)).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Open facebook app");
				openSocialNetwork(R.string.facebook_internal_url, R.string.facebook_web_url);
			}
		});
		
		//twitter
		((ImageView) getView().findViewById(R.id.open_twitter)).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Open twitter app");
				openSocialNetwork(R.string.twitter_internal_url, R.string.twitter_web_url);
			}
		});
		
		//web site
		((ImageView) getView().findViewById(R.id.open_web_site)).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Open web site");
				String url = getString(R.string.web_site_url);
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(browserIntent);
			}
		});
	}

	@Override
	public void onPause() {
		Log.d(TAG, "Pause links fragment");
		if (tweetsLoader != null) {
			Log.d(TAG, "Cancel the async tweets loader");
			tweetsLoader.cancel(true); //cancel loading if running
		}
		
		((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name); //reset activity title
		super.onPause();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		//get tweet url
		Log.d(TAG, "List item clicked - open URL");
		TweetItem item = (TweetItem) l.getItemAtPosition(position);
		String url = item.getTweetURL();
		if (url != null) {
			if ((!url.startsWith("http://")) && (!url.startsWith("https://")))
			   url = "http://" + url;
			
			//open url
			Log.d(TAG, "URL to open : " + url);
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(browserIntent);
		} else
			Log.d(TAG, "No URL on this tweet");
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "Backup array adapter values...");
		List<TweetItem> tweetItemList = new ArrayList<TweetItem>();
		for (int i = 0; i < getListAdapter().getCount(); i++) 
			tweetItemList.add((TweetItem) getListAdapter().getItem(i));
		outState.putSerializable(ARRAY_ADAPATER_BUNDLE, tweetItemList.toArray(new TweetItem[0]));

		super.onSaveInstanceState(outState);
	}

	/**
	 * Open social network native app if available or browser instead
	 * @param internalUrlID the id of the internal native app URL
	 * @param webUrlID the id of the web browser URL
	 */
	private void openSocialNetwork(int internalUrlID, int webUrlID) {
		try {
			//launch app
			String uri = getString(internalUrlID);
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			startActivity(intent);
		
		} catch(ActivityNotFoundException e) {
			Log.w(TAG, "Native app not found", e);
			
			//lauch web browser instead
			String url = getString(webUrlID);
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(browserIntent);
		}
	}

}
