package com.stationmillenium.android.replay.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.databinding.ReplayActivityBinding;
import com.stationmillenium.android.replay.dto.TrackDTO;
import com.stationmillenium.android.replay.utils.SoundcloudRestLoader;
import com.stationmillenium.android.replay.utils.SoundcloudRestLoader.QueryType;

import java.util.List;

import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.REPLAY;

/**
 * Activity for the replay
 * Created by vincent on 01/09/16.
 */
public class ReplayActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<TrackDTO>>, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "ReplayActivity";
    private static final int LOADER_INDEX = 0;
    private static final String LIMIT = "limit";
    private static final String SEARCH_TYPE = "search_type";
    private static final String SEARCH_QUERY = "search_query";
    private static final String SEARCH_PARAMS = "search_params";
    private static final int EXTRA_REPLAY_COUNT = 30;
    private static final int TOTAL_MAX_REPLAY = 200;

    private ReplayActivityBinding replayActivityBinding;
    private ReplayFragment replayFragment;

    private Bundle searchParams;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        replayActivityBinding = DataBindingUtil.setContentView(this, R.layout.replay_activity);
        replayActivityBinding.setActivity(this);
        setSupportActionBar(replayActivityBinding.replayToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(SEARCH_PARAMS)) {
            setToolbarTitle(savedInstanceState.getBundle(SEARCH_PARAMS));
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
        replayFragment.setRefreshing(true);
        if (args != null) {
            if (args.containsKey(SEARCH_TYPE)) {
                return (args.containsKey(LIMIT))
                        ? new SoundcloudRestLoader(this, (QueryType) args.getSerializable(SEARCH_TYPE), args.getString(SEARCH_QUERY), args.getInt(LIMIT))
                        : new SoundcloudRestLoader(this, (QueryType) args.getSerializable(SEARCH_TYPE), args.getString(SEARCH_QUERY));
            } else if (args.containsKey(LIMIT)) {
                return new SoundcloudRestLoader(this, args.getInt(LIMIT));
            }
        }
        return new SoundcloudRestLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<TrackDTO>> loader, List<TrackDTO> data) {
        Log.d(TAG, "Loading is finished - display data...");
        replayFragment.setReplayList(data);
        replayFragment.setRefreshing(false);
        replayActivityBinding.setReplayData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<TrackDTO>> loader) {
        Log.d(TAG, "Reset the loader");
        replayFragment.setReplayList(null);
        replayFragment.setRefreshing(false);
        replayActivityBinding.setReplayData(null);
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "Data refresh requested");
        setToolbarTitle(null); // we reinit the search params to default
        getSupportLoaderManager().restartLoader(LOADER_INDEX, null, this).forceLoad();
    }

    /**
     * Search for the specified genre
     * @param genre the genre to search for
     */
    public void searchGenre(String genre) {
        Log.d(TAG, "Search genre : " + genre);
        Bundle bundle = new Bundle();
        bundle.putSerializable(SEARCH_TYPE, QueryType.GENRE);
        bundle.putString(SEARCH_QUERY, genre);
        setToolbarTitle(bundle);
        getSupportLoaderManager().restartLoader(LOADER_INDEX, bundle, this).forceLoad();
    }

    /**
     * End scrolling happened
     * Trigger extra data load
     * @param replayCount the current replay count
     */
    public void triggerExtraDataLoad(int replayCount) {
        if (replayCount > 0) {
            if (replayCount <= TOTAL_MAX_REPLAY) {
                Log.d(TAG, "Load extra data");
                Bundle bundle = new Bundle();
                bundle.putInt(LIMIT, replayCount + EXTRA_REPLAY_COUNT);
                if (searchParams != null) {
                    Log.v(TAG, "Add search params for extra data load");
                    bundle.putAll(searchParams);
                }
                Snackbar.make(replayActivityBinding.replayCoordinatorLayout, R.string.replay_load_more, Snackbar.LENGTH_SHORT).show();
                getSupportLoaderManager().restartLoader(LOADER_INDEX, bundle, this).forceLoad();
            } else {
                Log.v(TAG, "All extra data already loaded");
                Snackbar.make(replayActivityBinding.replayCoordinatorLayout, R.string.replay_load_more_max_reached, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Log.v(TAG, "Empty replay list - extra load disabled");
        }
    }

    /**
     * Launch replay search
     */
    public void triggerSearch() {
        Log.d(TAG, "Trigger search");
    }

    public void openReplay(TrackDTO replayItem) {
        Log.d(TAG, "Open replay ; " + replayItem) ;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (searchParams != null) {
            outState.putBundle(SEARCH_PARAMS, searchParams);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * Setup the correct toolbar title according to the search params
     * @param searchParams the search params
     */
    private void setToolbarTitle(Bundle searchParams) {
        this.searchParams = searchParams;
        if (searchParams != null) {
            getSupportActionBar().setTitle(getString((searchParams.getSerializable(SEARCH_TYPE) == QueryType.SEARCH)
                    ? R.string.replay_toolbar_search_title : R.string.replay_toolbar_hash_title,
                    searchParams.getString(SEARCH_QUERY)));
        } else {
            getSupportActionBar().setTitle(R.string.replay_toolbar_normal_title);
        }
    }
}
