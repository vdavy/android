package com.stationmillenium.android.replay.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.databinding.ReplayItemActivityBinding;
import com.stationmillenium.android.replay.dto.TrackDTO;

/**
 * Activity to play a replay
 * Created by vincent on 28/11/16.
 */

public class ReplayItemActivity extends AppCompatActivity {

    private static final String TAG = "ReplayItemActivity";
    public static final String REPLAY_ITEM = "ReplayItem";

    private ReplayItemFragment replayItemFragment;
    private ReplayItemActivityBinding replayItemActivityBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        replayItemActivityBinding = DataBindingUtil.setContentView(this, R.layout.replay_item_activity);
        setSupportActionBar(replayItemActivityBinding.replayItemToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        replayItemFragment = (ReplayItemFragment) getSupportFragmentManager().findFragmentById(R.id.replay_item_fragment);
        extractReplayData();
    }

    private void extractReplayData() {
        Intent intent = getIntent();
        TrackDTO replay = (TrackDTO) intent.getSerializableExtra(REPLAY_ITEM);
        if (replay != null) {
            Log.v(TAG, "Display replay item : " + replay);
            replayItemFragment.setReplay(replay);
            getSupportActionBar().setTitle(getString( R.string.replay_item_toolbar_title, replay.getTitle()));
        } else {
            Snackbar.make(replayItemActivityBinding.replayItemCoordinatorLayout, R.string.replay_unavailable, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Search tag for replay from replay item
     * @param tag the tag to search for
     */
    public void searchTag(String tag) {
        Log.d(TAG, "Search tag : " + tag);
        Intent searchTagIntent = new Intent(this, ReplayActivity.class);
        searchTagIntent.putExtra(ReplayActivity.REPLAY_TAG, tag);
        startActivity(searchTagIntent);
        finish();
    }
}
