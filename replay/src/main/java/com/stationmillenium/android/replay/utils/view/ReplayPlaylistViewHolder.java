package com.stationmillenium.android.replay.utils.view;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

import com.stationmillenium.android.replay.activities.ReplayActivity;
import com.stationmillenium.android.replay.databinding.ReplayPlaylistListItemBinding;
import com.stationmillenium.android.replay.dto.PlaylistDTO;

/**
 * Binding and view holder for replay playlist item in list
 * Created by vincent on 10/04/2017.
 */
public class ReplayPlaylistViewHolder extends ViewHolder {

    private ReplayPlaylistListItemBinding binding;

    public ReplayPlaylistViewHolder(View itemView, ReplayActivity replayActivity) {
        super(itemView);
        binding = ReplayPlaylistListItemBinding.bind(itemView);
        binding.setActivity(replayActivity);
    }

    public void bindReplay(PlaylistDTO playlistDTO) {
        binding.setPlaylistItem(playlistDTO);
    }

}
