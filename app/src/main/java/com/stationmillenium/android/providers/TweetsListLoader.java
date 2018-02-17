package com.stationmillenium.android.providers;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;

import com.stationmillenium.android.libutils.SharedPreferencesConstants;
import com.stationmillenium.android.libutils.dtos.TweetItem;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
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
        Timber.d("Load tweets...");

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
                    .getString(SharedPreferencesConstants.TWEETS_DISPLAY_NUMBER, MAX_TWEETS_DEFAULT);
            int maxTweets = Integer.parseInt(maxTweetsString);
            Timber.d("Max tweets to load : %s", maxTweets);

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

            Timber.d("Gathered tweets list : %s", tweetItemList);
            return tweetItemList;

        } catch (Exception e) { //if any error occurs
            Timber.e(e, "Error while getting latest tweets");
            return null;
        }
    }
}
