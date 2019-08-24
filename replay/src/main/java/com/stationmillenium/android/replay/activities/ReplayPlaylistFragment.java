package com.stationmillenium.android.replay.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.databinding.ReplayPlaylistFragmentBinding;
import com.stationmillenium.android.replay.dto.PlaylistDTO;
import com.stationmillenium.android.replay.utils.view.ReplayPlaylistAdapter;

import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;

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
        binding.replaySrl.setOnRefreshListener(() -> ((ReplayActivity) getActivity()).onPlaylistRefresh());
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
                    ((ReplayActivity) getActivity()).triggerExtraDataLoad(ReplayActivity.PLAYLIST_LOADER_INDEX, replayPlaylistAdapter.getPlaylistDTOs());
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
