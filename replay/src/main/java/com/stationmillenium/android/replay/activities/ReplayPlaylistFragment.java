package com.stationmillenium.android.replay.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.databinding.ReplayPlaylistFragmentBinding;
import com.stationmillenium.android.replay.dto.PlaylistDTO;
import com.stationmillenium.android.replay.utils.view.ReplayPlaylistAdapter;

import java.util.List;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * Replay fragment
 * Created by vincent on 01/09/16.
 */
public class ReplayPlaylistFragment extends Fragment {

    private ReplayPlaylistFragmentBinding binding;
    private ReplayPlaylistAdapter replayPlaylistAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        replayPlaylistAdapter = new ReplayPlaylistAdapter((ReplayActivity) getActivity());
        binding = DataBindingUtil.inflate(inflater, R.layout.replay_playlist_fragment, container, false);
        binding.replayRecyclerview.setAdapter(replayPlaylistAdapter);
        binding.replaySrl.setColorSchemeResources(R.color.primary, R.color.accent);
        binding.replaySrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((ReplayActivity) getActivity()).onPlaylistRefresh();
            }
        });
        binding.replayRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (newState == SCROLL_STATE_IDLE // we are not scrolling
                        // list must be scrolled (not on first position) - avoid list too short and displayed on whole screen
                        && linearLayoutManager.findFirstVisibleItemPosition() > 0
                        && linearLayoutManager.findFirstCompletelyVisibleItemPosition() > 0
                        // last item is fully displayed
                        && linearLayoutManager.findLastVisibleItemPosition() == (replayPlaylistAdapter.getItemCount() - 1)
                        && linearLayoutManager.findLastCompletelyVisibleItemPosition() == (replayPlaylistAdapter.getItemCount() - 1)) {
                    ((ReplayActivity) getActivity()).triggerExtraDataLoad(ReplayActivity.PLAYLIST_LOADER_INDEX, replayPlaylistAdapter.getItemCount());
                }
            }
        });
        ((ReplayActivity) getActivity()).setReplayPlaylistFragment(this);
        ((ReplayActivity) getActivity()).requestPlaylistDataLoad();
        return binding.getRoot();
    }

    /**
     * Set the data list for display
     * @param replayList the replay items
     */
    public void setReplayPlaylistList(List<PlaylistDTO> replayList) {
        replayPlaylistAdapter.setPlaylistDTOs(replayList);
        replayPlaylistAdapter.notifyDataSetChanged();
    }

    /**
     * Are we refreshing data ?
     * @param refreshing {@code true} for yes, {@code false} if not
     */
    public void setRefreshing(boolean refreshing) {
        if (binding != null) {
            binding.replaySrl.setRefreshing(refreshing);
        }
    }

    public int getItemCount() {
        return replayPlaylistAdapter.getItemCount();
    }

}
