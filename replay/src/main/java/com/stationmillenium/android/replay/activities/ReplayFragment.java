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
import com.stationmillenium.android.replay.utils.view.ReplayAdapter;

import java.util.List;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * Replay fragment
 * Created by vincent on 01/09/16.
 */
public class ReplayFragment extends Fragment {

    private ReplayFragmentBinding binding;
    private ReplayAdapter replayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        replayAdapter = new ReplayAdapter((ReplayActivity) getActivity());
        binding = DataBindingUtil.inflate(inflater, R.layout.replay_fragment, container, false);
        binding.replayRecyclerview.setAdapter(replayAdapter);
        binding.replaySrl.setColorSchemeResources(R.color.primary, R.color.accent);
        binding.replaySrl.setOnRefreshListener((SwipeRefreshLayout.OnRefreshListener) getActivity());
        binding.replayRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (newState == SCROLL_STATE_IDLE
                        && linearLayoutManager.findLastVisibleItemPosition() == (replayAdapter.getItemCount() - 1)
                        && linearLayoutManager.findLastCompletelyVisibleItemPosition() == (replayAdapter.getItemCount() - 1)) {
                    ((ReplayActivity) getActivity()).triggerExtraDataLoad();
                }
            }
        });
        return binding.getRoot();
    }

    /**
     * Set the data list for display
     * @param replayList the replay items
     */
    public void setReplayList(List<TrackDTO> replayList) {
        replayAdapter.setTrackDTOs(replayList);
        replayAdapter.notifyDataSetChanged();
    }

    /**
     * Are we refreshing data ?
     * @param refreshing {@code true} for yes, {@code false} if not
     */
    public void setRefreshing(boolean refreshing) {
        binding.replaySrl.setRefreshing(refreshing);
    }

}
