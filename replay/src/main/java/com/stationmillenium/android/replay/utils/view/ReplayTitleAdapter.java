package com.stationmillenium.android.replay.utils.view;

import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.activities.ReplayActivity;
import com.stationmillenium.android.replay.dto.TrackDTO;

import java.util.List;

import timber.log.Timber;

/**
 * Replay adapter for recycle view
 * Created by vincent on 23/10/16.
 */
public class ReplayTitleAdapter extends Adapter<ReplayTitleViewHolder> {

    private ReplayActivity replayActivity;
    private List<TrackDTO> trackDTOs;

    public ReplayTitleAdapter(ReplayActivity replayActivity) {
        this.replayActivity = replayActivity;
    }

    @Override
    public ReplayTitleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Timber.v("Create view...");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.replay_list_item, parent, false);
        return new ReplayTitleViewHolder(view, replayActivity);
    }

    @Override
    public void onBindViewHolder(ReplayTitleViewHolder holder, int position) {
        Timber.v("Bind view...");
        TrackDTO trackDTO = trackDTOs.get(position);
        holder.bindReplay(trackDTO);
    }

    @Override
    public int getItemCount() {
        return (trackDTOs != null) ? trackDTOs.size() : 0;
    }

    public void setTrackDTOs(List<TrackDTO> trackDTOs) {
        this.trackDTOs = trackDTOs;
    }
}
