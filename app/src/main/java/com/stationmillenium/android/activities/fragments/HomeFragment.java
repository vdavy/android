/**
 *
 */
package com.stationmillenium.android.activities.fragments;

import android.annotation.TargetApi;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.HomeActivity;
import com.stationmillenium.android.databinding.HomeFragmentBinding;
import com.stationmillenium.android.libutils.dtos.FacebookItem;
import com.stationmillenium.android.libutils.viewholders.FacebookAdapter;

import java.util.List;

/**
 * Home fragment
 *
 * @author vincent
 */
public class HomeFragment extends Fragment {

    //instance vars
    private HomeFragmentBinding binding;
    private FacebookAdapter facebookAdapter;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        facebookAdapter = new FacebookAdapter((HomeActivity) getActivity());
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false);
        binding.setActivity((HomeActivity) getActivity());
        binding.homeSrl.setColorSchemeResources(R.color.primary, R.color.accent);
        binding.homeRecyclerview.setAdapter(facebookAdapter);
        return binding.getRoot();
    }

    /**
     * Set the data list for display
     * @param facebookItems the tweet items
     */
    public void setReplayList(List<FacebookItem> facebookItems) {
        facebookAdapter.setFacebookItems(facebookItems);
        facebookAdapter.notifyDataSetChanged();
    }

    /**
     * Are we refreshing data ?
     * @param refreshing {@code true} for yes, {@code false} if not
     */
    public void setRefreshing(boolean refreshing) {
            binding.homeSrl.setRefreshing(refreshing);
    }

}
