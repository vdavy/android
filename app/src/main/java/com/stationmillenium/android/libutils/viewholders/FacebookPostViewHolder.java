package com.stationmillenium.android.libutils.viewholders;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.stationmillenium.android.activities.HomeActivity;
import com.stationmillenium.android.databinding.FacebookPostBinding;
import com.stationmillenium.android.libutils.dtos.FacebookPost;

/**
 * Binding and view holder for facebook item in list
 * Created by vincent on 23/10/16.
 */
public class FacebookPostViewHolder extends ViewHolder {

    private FacebookPostBinding binding;

    public FacebookPostViewHolder(View itemView, HomeActivity homeActivity) {
        super(itemView);
        binding = FacebookPostBinding.bind(itemView);
        binding.setActivity(homeActivity);
    }

    public void bindFacebookPost(FacebookPost item) {
        binding.setFacebookPost(item);
    }

}
