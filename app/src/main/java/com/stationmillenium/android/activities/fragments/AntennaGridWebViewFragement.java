package com.stationmillenium.android.activities.fragments;

import android.os.Bundle;

import com.stationmillenium.android.R;
import com.stationmillenium.android.libutils.PiwikTracker;

import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.ANTENNA_GRID;

/**
 * Antenna grid webview fragment
 * Created by vincent on 27/02/16.
 */
public class AntennaGridWebViewFragement extends AbstractWebViewFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.title = R.string.antenna_grid_activity_title;
        super.url = R.string.antenna_grid_page_url;
    }

    @Override
    public void onResume() {
        super.onResume();
        PiwikTracker.trackScreenView(getActivity().getApplication(), ANTENNA_GRID);
    }
}
