package com.stationmillenium.android.replay.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.databinding.ReplayFragmentBinding;
import com.stationmillenium.android.replay.dto.TrackDTO;
import com.stationmillenium.android.replay.utils.view.ReplayTitleAdapter;

import java.util.List;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * Replay fragment
 * Created by vincent on 01/09/16.
 */
public class ReplayTitleFragment extends Fragment {

    private ReplayFragmentBinding binding;
    private ReplayTitleAdapter replayTitleAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        replayTitleAdapter = new ReplayTitleAdapter((ReplayActivity) getActivity());
        binding = DataBindingUtil.inflate(inflater, R.layout.replay_fragment, container, false);
        binding.replayRecyclerview.setAdapter(replayTitleAdapter);
        binding.replaySrl.setColorSchemeResources(R.color.primary, R.color.accent);
        binding.replaySrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((ReplayActivity) getActivity()).onTrackRefresh();
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
                        && linearLayoutManager.findLastVisibleItemPosition() == (replayTitleAdapter.getItemCount() - 1)
                        && linearLayoutManager.findLastCompletelyVisibleItemPosition() == (replayTitleAdapter.getItemCount() - 1)) {
                    ((ReplayActivity) getActivity()).triggerExtraDataLoad(ReplayActivity.TRACK_LOADER_INDEX, replayTitleAdapter.getItemCount());
                }
            }
        });
        ((ReplayActivity) getActivity()).setReplayTitleFragment(this);
        ((ReplayActivity) getActivity()).requestTracksDataLoad();
        return binding.getRoot();
    }

    /**
     * Set the data list for display
     * @param replayList the replay title items
     */
    public void setReplayTitleList(List<TrackDTO> replayList) {
        replayTitleAdapter.setTrackDTOs(replayList);
        replayTitleAdapter.notifyDataSetChanged();
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
        return replayTitleAdapter.getItemCount();
    }

}