package com.stationmillenium.android.libutils.viewholders;

import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.HomeActivity;
import com.stationmillenium.android.libutils.dtos.FacebookItem;

import java.util.List;

import timber.log.Timber;

/**
 * Facebook adapter for recycle view
 * Created by vincent on 23/10/16.
 */
public class FacebookAdapter extends Adapter<FacebookViewHolder> {

    private HomeActivity homeActivity;
    private List<FacebookItem> facebookItems;

    public FacebookAdapter(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

    @Override
    public FacebookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Timber.v("Create view...");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.facebook_list_item, parent, false);
        return new FacebookViewHolder(view, homeActivity);
    }

    @Override
    public void onBindViewHolder(FacebookViewHolder holder, int position) {
        Timber.v("Bind view...");
        FacebookItem facebookDTO = facebookItems.get(position);
        holder.bindFacebook(facebookDTO);
    }

    @Override
    public int getItemCount() {
        return (facebookItems != null) ? facebookItems.size() : 0;
    }

    public void setFacebookItems(List<FacebookItem> facebookItems) {
        this.facebookItems = facebookItems;
    }
}
