package com.stationmillenium.android.libutils.viewholders;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

import com.stationmillenium.android.activities.HomeActivity;
import com.stationmillenium.android.databinding.FacebookListItemBinding;
import com.stationmillenium.android.libutils.dtos.FacebookItem;

/**
 * Binding and view holder for facebook item in list
 * Created by vincent on 23/10/16.
 */
public class FacebookViewHolder extends ViewHolder {

    private FacebookListItemBinding binding;

    public FacebookViewHolder(View itemView, HomeActivity homeActivity) {
        super(itemView);
        binding = FacebookListItemBinding.bind(itemView);
        binding.setActivity(homeActivity);
    }

    public void bindFacebook(FacebookItem item) {
        binding.setFacebookItem(item);
    }

}
