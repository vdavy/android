package com.stationmillenium.android.replay.utils.view;

import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.activities.ReplayActivity;
import com.stationmillenium.android.replay.dto.TrackDTO;

import java.util.List;

/**
 * Replay adapter for recycle view
 * Created by vincent on 23/10/16.
 */
public class ReplayAdapter extends Adapter<ReplayViewHolder> {

    private static final String TAG = "ReplayAdapter";
    private ReplayActivity replayActivity;
    private List<TrackDTO> trackDTOs;

    public ReplayAdapter(ReplayActivity replayActivity) {
        this.replayActivity = replayActivity;
    }

    @Override
    public ReplayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.v(TAG, "Create view...");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.replay_list_item, parent, false);
        return new ReplayViewHolder(view, replayActivity);
    }

    @Override
    public void onBindViewHolder(ReplayViewHolder holder, int position) {
        Log.v(TAG, "Bind view...");
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
