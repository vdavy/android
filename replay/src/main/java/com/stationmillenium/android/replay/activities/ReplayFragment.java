package com.stationmillenium.android.replay.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.databinding.ReplayFragmentBinding;
import com.stationmillenium.android.replay.dto.TrackDTO;
import com.stationmillenium.android.replay.utils.view.ReplayAdapter;

import java.util.List;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_SETTLING;

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
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                Log.v("Scroll DX", String.valueOf(dx));
                Log.v("Scroll DY", String.valueOf(dy));
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case SCROLL_STATE_IDLE:
                        Log.v("Scroll", "SCROLL_STATE_IDLE");
                        break;
                    case SCROLL_STATE_DRAGGING:
                        Log.v("Scroll", "SCROLL_STATE_DRAGGING");
                        break;
                    case SCROLL_STATE_SETTLING:
                        Log.v("Scroll", "SCROLL_STATE_SETTLING");
                        break;
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
