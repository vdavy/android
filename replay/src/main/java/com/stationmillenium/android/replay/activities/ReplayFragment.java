package com.stationmillenium.android.replay.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stationmillenium.android.replay.databinding.ReplayFragmentBinding;

/**
 * Replay fragment
 * Created by vincent on 01/09/16.
 */
public class ReplayFragment extends ListFragment {

    private static final String TAG = "ReplayFragment";
    private ReplayFragmentBinding replayFragmentBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        replayFragmentBinding = ReplayFragmentBinding.inflate(inflater);
        return replayFragmentBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getListView().setNestedScrollingEnabled(true);
        }
        super.onViewCreated(view, savedInstanceState);
    }

    public ReplayFragmentBinding getBinding() {
        return replayFragmentBinding;
    }
}
