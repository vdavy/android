package com.stationmillenium.android.replay.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.databinding.ReplayItemFragmentBinding;
import com.stationmillenium.android.replay.dto.TrackDTO;

/**
 * Replay item fragment
 * Created by vincent on 28/11/16.
 */
public class ReplayItemFragment extends Fragment {

    private ReplayItemFragmentBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.replay_item_fragment, container, false);
        return binding.getRoot();
    }

    /**
     * Bind the replay data
     * @param replay replay to display
     */
    public void setReplay(TrackDTO replay) {
        binding.setReplayItem(replay);
        setPercentPlayed(0);
    }

    /**
     * Get the root view to dock media player controls
     * @return the fragment root view
     */
    public View getRootView() {
        return binding.getRoot();
    }

    /**
     * Set the progress bar visibility
     * @param visible <code>true</code> : visible, <code>false</code> : hidden
     */
    public void setProgressBarVisible(boolean visible) {
        binding.replayItemProgressbar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Set the played percent for waveform hiding
     * @param playedPercent the played percent, starting from 0 and ending to 10000
     */
    public void setPercentPlayed(int playedPercent) {
        binding.setPercentPlayed(playedPercent);
    }
}
