package com.stationmillenium.android.replay.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.databinding.ReplayFragmentBinding;
import com.stationmillenium.android.replay.dto.TrackDTO;
import com.stationmillenium.android.replay.utils.view.ReplayAdapter;

import java.util.List;

/**
 * Replay fragment
 * Created by vincent on 01/09/16.
 */
public class ReplayFragment extends Fragment {

    private ReplayFragmentBinding binding;
    private ReplayAdapter replayAdapter = new ReplayAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.replay_fragment, container, false);
        binding.replayRecyclerview.setAdapter(replayAdapter);
        return binding.getRoot();
    }

    public void setReplayList(List<TrackDTO> replayList) {
        replayAdapter.setTrackDTOs(replayList);
        replayAdapter.notifyDataSetChanged();
    }
}
