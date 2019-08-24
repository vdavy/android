package com.stationmillenium.android.replay.utils.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.activities.ReplayActivity;
import com.stationmillenium.android.replay.dto.PlaylistDTO;

import java.util.List;

import timber.log.Timber;

/**
 * Replay playlist adapter for recycle view
 * Created by vincent on 10/04/2017.
 */
public class ReplayPlaylistAdapter extends Adapter<ReplayPlaylistViewHolder> {

    private ReplayActivity replayActivity;
    private List<PlaylistDTO> playlistDTOs;

    public ReplayPlaylistAdapter(ReplayActivity replayActivity) {
        this.replayActivity = replayActivity;
    }

    @Override
    public ReplayPlaylistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Timber.v("Create view...");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.replay_playlist_list_item, parent, false);
        return new ReplayPlaylistViewHolder(view, replayActivity);
    }

    @Override
    public void onBindViewHolder(ReplayPlaylistViewHolder holder, int position) {
        Timber.v("Bind view...");
        PlaylistDTO playlistDTO = playlistDTOs.get(position);
        holder.bindReplay(playlistDTO);
    }

    @Override
    public int getItemCount() {
        return (playlistDTOs != null) ? playlistDTOs.size() : 0;
    }

    public void setPlaylistDTOs(List<PlaylistDTO> playlistDTOs) {
        this.playlistDTOs = playlistDTOs;
    }

    public List<PlaylistDTO> getPlaylistDTOs() {
        return playlistDTOs;
    }
}
