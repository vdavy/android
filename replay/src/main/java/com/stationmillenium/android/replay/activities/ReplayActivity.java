package com.stationmillenium.android.replay.activities;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.replay.BuildConfig;
import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.provider.ReplayContract;

import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.REPLAY;

/**
 * Activity for the replay
 * Created by vincent on 01/09/16.
 */
public class ReplayActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ReplayActivity";
    private static final int LOADER_INDEX = 0;

    private ReplayFragment replayFragment;
    private SimpleCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.replay_activity);
        setSupportActionBar((Toolbar) findViewById(R.id.replay_toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        replayFragment = (ReplayFragment) getSupportFragmentManager().findFragmentById(R.id.replay_fragment);

        initCursorAdapter();
        getSupportLoaderManager().initLoader(LOADER_INDEX, null, this);
    }

    private void initCursorAdapter() {
        cursorAdapter = new SimpleCursorAdapter(this, R.layout.replay_list_item, null,
                new String[]{
                        ReplayContract.Columns.TITLE,
                        ReplayContract.Columns.STREAM_URL
                },
                new int[]{
                        R.id.artist,
                        R.id.title
                }, 0);
        replayFragment.setListAdapter(cursorAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        PiwikTracker.trackScreenView(REPLAY);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri allReplayURI = ReplayContract.ROOT_URI.buildUpon().appendPath(ReplayContract.DEFAULT_MATCH).build();
        return new CursorLoader(this, allReplayURI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Loading is finished - display data...");
        }
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Reset the loader");
        }
        cursorAdapter.swapCursor(null);
    }
}
