package com.stationmillenium.android.replay.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.replay.BuildConfig;
import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.SoundcloudRestLoader;
import com.stationmillenium.android.replay.databinding.ReplayActivityBinding;
import com.stationmillenium.android.replay.dto.TrackDTO;

import java.util.List;

import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.REPLAY;

/**
 * Activity for the replay
 * Created by vincent on 01/09/16.
 */
public class ReplayActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<TrackDTO>> {

    private static final String TAG = "ReplayActivity";
    private static final int LOADER_INDEX = 0;

    private ReplayActivityBinding replayActivityBinding;
    private ReplayFragment replayFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        replayActivityBinding = DataBindingUtil.setContentView(this, R.layout.replay_activity);
        setSupportActionBar(replayActivityBinding.replayToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        replayFragment = (ReplayFragment) getSupportFragmentManager().findFragmentById(R.id.replay_fragment);
        getSupportLoaderManager().initLoader(LOADER_INDEX, null, this).forceLoad();
    }

    @Override
    public void onResume() {
        super.onResume();
        PiwikTracker.trackScreenView(REPLAY);
    }

    @Override
    public Loader<List<TrackDTO>> onCreateLoader(int id, Bundle args) {
        return new SoundcloudRestLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<TrackDTO>> loader, List<TrackDTO> data) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Loading is finished - display data...");
        }
        replayFragment.setReplayList(data);
        replayActivityBinding.setReplayData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<TrackDTO>> loader) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Reset the loader");
        }
        replayFragment.setReplayList(null);
        replayActivityBinding.setReplayData(null);
    }
}
