package com.stationmillenium.android.providers;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;

import com.stationmillenium.android.R;
import com.stationmillenium.android.libutils.SharedPreferencesConstants;
import com.stationmillenium.android.libutils.dtos.FacebookPost;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

/**
 * Loader for all facebook posts
 * Created by vincent on 17/02/17.
 */
public class FacebookFeedLoader extends AsyncTaskLoader<List<FacebookPost>> {

    private static final String MAX_FACEBOOK_POSTS_DEFAULT = "10";

    public FacebookFeedLoader(@NonNull Context context) {
        super(context);
    }

    @Override
    public List<FacebookPost> loadInBackground() {
        Timber.d("Load facebook feed...");

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            if (!isLoadInBackgroundCanceled()) {
                String url = getFacebookFeedURL();
                Timber.v("Facebook feed URL %s", url);
                FacebookPost[] facebookPosts = restTemplate.getForObject(url, FacebookPost[].class);
                Timber.d("Got Facebook feed : %s", facebookPosts.length);
                return new ArrayList<>(Arrays.asList(facebookPosts));
            }
        } catch (Exception e) { //if any error occurs
            Timber.e(e, "Error while getting latest facebook feed");
        }
        return null;
    }

    private String getFacebookFeedURL() {
        return Uri.parse(getContext().getString(R.string.facebook_feed_url)).buildUpon()
                .appendQueryParameter(getContext().getString(R.string.limit_param_name), PreferenceManager.getDefaultSharedPreferences(getContext())
                        .getString(SharedPreferencesConstants.FACEBOOK_FEED_DISPLAY_NUMBER, MAX_FACEBOOK_POSTS_DEFAULT))
                .build().toString();
    }
}
