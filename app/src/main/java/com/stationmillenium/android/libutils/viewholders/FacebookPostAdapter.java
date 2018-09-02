package com.stationmillenium.android.libutils.viewholders;

import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.HomeActivity;
import com.stationmillenium.android.libutils.dtos.FacebookPost;

import java.util.List;

import timber.log.Timber;

/**
 * Facebook adapter for recycle view
 * Created by vincent on 23/10/16.
 */
public class FacebookPostAdapter extends Adapter<FacebookPostViewHolder> {

    private HomeActivity homeActivity;
    private List<FacebookPost> facebookPosts;

    public FacebookPostAdapter(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

    @Override
    public FacebookPostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Timber.v("Create view...");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.facebook_post, parent, false);
        return new FacebookPostViewHolder(view, homeActivity);
    }

    @Override
    public void onBindViewHolder(FacebookPostViewHolder holder, int position) {
        Timber.v("Bind view...");
        FacebookPost facebookPost = facebookPosts.get(position);
        holder.bindFacebookPost(facebookPost);
    }

    @Override
    public int getItemCount() {
        return (facebookPosts != null) ? facebookPosts.size() : 0;
    }

    public void setFacebookPosts(List<FacebookPost> facebookPosts) {
        this.facebookPosts = facebookPosts;
    }
}
