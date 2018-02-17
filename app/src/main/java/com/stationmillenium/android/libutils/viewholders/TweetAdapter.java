package com.stationmillenium.android.libutils.viewholders;

import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.HomeActivity;
import com.stationmillenium.android.libutils.dtos.TweetItem;

import java.util.List;

import timber.log.Timber;

/**
 * Tweet adapter for recycle view
 * Created by vincent on 23/10/16.
 */
public class TweetAdapter extends Adapter<TweetViewHolder> {

    private HomeActivity homeActivity;
    private List<TweetItem> tweetItems;

    public TweetAdapter(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

    @Override
    public TweetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Timber.v("Create view...");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tweets_list_item, parent, false);
        return new TweetViewHolder(view, homeActivity);
    }

    @Override
    public void onBindViewHolder(TweetViewHolder holder, int position) {
        Timber.v("Bind view...");
        TweetItem trackDTO = tweetItems.get(position);
        holder.bindTweet(trackDTO);
    }

    @Override
    public int getItemCount() {
        return (tweetItems != null) ? tweetItems.size() : 0;
    }

    public void setTweetItems(List<TweetItem> tweetItems) {
        this.tweetItems = tweetItems;
    }
}
