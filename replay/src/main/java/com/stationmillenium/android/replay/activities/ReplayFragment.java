package com.stationmillenium.android.replay.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stationmillenium.android.replay.R;

/**
 * Replay fragment
 * Created by vincent on 01/09/16.
 */
public class ReplayFragment extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.replay_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getListView().setNestedScrollingEnabled(true);
        }
        super.onViewCreated(view, savedInstanceState);
    }

}
