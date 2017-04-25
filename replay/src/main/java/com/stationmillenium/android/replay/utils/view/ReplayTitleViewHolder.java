package com.stationmillenium.android.replay.utils.view;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

import com.stationmillenium.android.replay.activities.ReplayActivity;
import com.stationmillenium.android.replay.databinding.ReplayListItemBinding;
import com.stationmillenium.android.replay.dto.TrackDTO;

/**
 * Binding and view holder for replay item in list
 * Created by vincent on 23/10/16.
 */
public class ReplayTitleViewHolder extends ViewHolder {

    private ReplayListItemBinding binding;

    public ReplayTitleViewHolder(View itemView, ReplayActivity replayActivity) {
        super(itemView);
        binding = ReplayListItemBinding.bind(itemView);
        binding.setActivity(replayActivity);
    }

    public void bindReplay(TrackDTO trackDTO) {
        binding.setReplayItem(trackDTO);
    }

}
