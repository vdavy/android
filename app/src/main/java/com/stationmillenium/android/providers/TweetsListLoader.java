package com.stationmillenium.android.providers;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.activities.preferences.SharedPreferencesActivity;
import com.stationmillenium.android.libutils.dtos.TweetItem;

import java.util.ArrayList;
import java.util.List;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Loader for all tweets
 * Created by vincent on 17/02/17.
 */
public class TweetsListLoader extends AsyncTaskLoader<List<TweetItem>> {

    private static final String TAG = "TweetsLoader";
    private static final String MAX_TWEETS_DEFAULT = "10";

    private String consumerKey;
    private String consumerSecret;
    private String username;

    public TweetsListLoader(@NonNull Context context, String consumerKey, String consumerSecret, String username) {
        super(context);
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.username = username;
    }

    @Override
    public List<TweetItem> loadInBackground() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Load tweets...");
        }

        //build tweeter auth conf
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setApplicationOnlyAuthEnabled(true);
        Twitter twitter = new TwitterFactory(cb.build()).getInstance();
        try {
            twitter.getOAuth2Token(); //load token
            ResponseList<Status> tweetsList = twitter.getUserTimeline(username); //load tweets

            //max tweets to load
            String maxTweetsString = PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getString(SharedPreferencesActivity.SharedPreferencesConstants.TWEETS_DISPLAY_NUMBER, MAX_TWEETS_DEFAULT);
            int maxTweets = Integer.parseInt(maxTweetsString);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Max tweets to load : " + maxTweets);
            }

            List<TweetItem> tweetItemList = new ArrayList<>();
            for (twitter4j.Status status : tweetsList) { //process each tweet
                //load tweet url
                String tweetURL = null;
                if ((status.getURLEntities() != null) && (status.getURLEntities().length > 0)) {
                    tweetURL = status.getURLEntities()[0].getURL();
                }

                tweetItemList.add(new TweetItem(status.getText(), tweetURL)); //add to list

                if (tweetItemList.size() >= maxTweets) { //limit tweets list
                    break;
                }
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Gathered tweets list : " + tweetItemList);
            }
            return tweetItemList;

        } catch (Exception e) { //if any error occurs
            Log.e(TAG, "Error while getting latest tweets", e);
            return null;
        }
    }
}
