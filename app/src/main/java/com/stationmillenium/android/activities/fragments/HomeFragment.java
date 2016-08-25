/**
 *
 */
package com.stationmillenium.android.activities.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stationmillenium.android.R;
import com.stationmillenium.android.utils.PiwikTracker;

import static com.stationmillenium.android.utils.PiwikTracker.PiwikPages.HOME;

/**
 * Home fragment for app
 *
 * @author vincent
 */
public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        PiwikTracker.trackScreenView(getActivity().getApplication(), HOME);
    }
}
