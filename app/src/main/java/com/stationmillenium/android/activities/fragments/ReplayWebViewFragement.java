package com.stationmillenium.android.activities.fragments;

import android.os.Bundle;

import com.stationmillenium.android.R;
import com.stationmillenium.android.libutils.PiwikTracker;

import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.REPLAY;

/**
 * Replay webview fragment
 * Created by vincent on 27/02/16.
 */
public class ReplayWebViewFragement extends AbstractWebViewFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.title = R.string.replay_activity_title;
        super.url = R.string.replay_url;
    }

    @Override
    public void onResume() {
        super.onResume();
        PiwikTracker.trackScreenView(getActivity().getApplication(), REPLAY);
    }
}
