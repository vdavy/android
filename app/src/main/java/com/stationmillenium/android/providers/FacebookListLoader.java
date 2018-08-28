package com.stationmillenium.android.providers;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.stationmillenium.android.libutils.SharedPreferencesConstants;
import com.stationmillenium.android.libutils.dtos.FacebookItem;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Loader for all facebook posts
 * Created by vincent on 17/02/17.
 */
public class FacebookListLoader extends AsyncTaskLoader<List<FacebookItem>> {

    private static final String MAX_TWEETS_DEFAULT = "10";

    private String consumerKey;
    private String consumerSecret;
    private String username;

    public FacebookListLoader(@NonNull Context context, String consumerKey, String consumerSecret, String username) {
        super(context);
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.username = username;
    }

    @Override
    public List<FacebookItem> loadInBackground() {
        Timber.d("Load facebook feed...");

        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/132025813602837/feed",
                response -> {
                   Timber.d(response.toString());
                });

        request.executeAsync();

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
            Timber.d("Max facebook posts to load : %s", maxTweets);

            List<FacebookItem> facebookItemList = new ArrayList<>();
            for (twitter4j.Status status : tweetsList) { //process each tweet
                //load tweet url
                String tweetURL = null;
                if ((status.getURLEntities() != null) && (status.getURLEntities().length > 0)) {
                    tweetURL = status.getURLEntities()[0].getURL();
                }

                facebookItemList.add(new FacebookItem(status.getText(), tweetURL)); //add to list

                if (facebookItemList.size() >= maxTweets) { //limit tweets list
                    break;
                }
            }

            Timber.d("Gathered facebook posts list : %s", facebookItemList);
            return facebookItemList;

        } catch (Exception e) { //if any error occurs
            Timber.e(e, "Error while getting latest facebook feed");
            return null;
        }
    }
}
