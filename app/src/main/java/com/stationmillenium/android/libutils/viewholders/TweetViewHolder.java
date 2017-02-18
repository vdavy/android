package com.stationmillenium.android.libutils.viewholders;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

import com.stationmillenium.android.activities.HomeActivity;
import com.stationmillenium.android.databinding.TweetsListItemBinding;
import com.stationmillenium.android.libutils.dtos.TweetItem;

/**
 * Binding and view holder for tweet item in list
 * Created by vincent on 23/10/16.
 */
public class TweetViewHolder extends ViewHolder {

    private TweetsListItemBinding binding;

    public TweetViewHolder(View itemView, HomeActivity homeActivity) {
        super(itemView);
        binding = TweetsListItemBinding.bind(itemView);
        binding.setActivity(homeActivity);
    }

    public void bindTweet(TweetItem item) {
        binding.setTweet(item);
    }

}
