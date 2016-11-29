package com.stationmillenium.android.replay.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.databinding.ReplayItemFragmentBinding;
import com.stationmillenium.android.replay.dto.TrackDTO;
import com.stationmillenium.android.replay.utils.GlideUtils;

/**
 * Replay item fragment
 * Created by vincent on 28/11/16.
 */
public class ReplayItemFragment extends Fragment {

    private ReplayItemFragmentBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.replay_item_fragment, container, false);
        return binding.getRoot();
    }

    public void setReplay(TrackDTO replay) {
        binding.setReplayItem(replay);
        GlideUtils.setReplayImage(binding.replayItemArtwork, replay);
    }

}
