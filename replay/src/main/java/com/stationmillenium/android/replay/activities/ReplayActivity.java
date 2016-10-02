package com.stationmillenium.android.replay.activities;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.replay.BuildConfig;
import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.databinding.ReplayActivityBinding;
import com.stationmillenium.android.replay.provider.ReplayContract;

import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.REPLAY;

/**
 * Activity for the replay
 * Created by vincent on 01/09/16.
 */
public class ReplayActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ReplayActivity";
    private static final int LOADER_INDEX = 0;

    private ReplayActivityBinding replayActivityBinding;
    private ReplayFragment replayFragment;
    private SimpleCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        replayActivityBinding = DataBindingUtil.setContentView(this, R.layout.replay_activity);
        setSupportActionBar(replayActivityBinding.replayToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        replayFragment = (ReplayFragment) getSupportFragmentManager().findFragmentById(R.id.replay_fragment);

        initCursorAdapter();
        getSupportLoaderManager().initLoader(LOADER_INDEX, null, this);
    }

    private void initCursorAdapter() {
        cursorAdapter = new SimpleCursorAdapter(this, R.layout.replay_list_item, null, new String[]{
                ReplayContract.Columns.TITLE,
                ReplayContract.Columns.DESCRIPTION,
                ReplayContract.Columns.ARTWORK_URL,
                ReplayContract.Columns.WAVEFORM_URL
        },
        new int[]{
                R.id.replay_title,
                R.id.replay_description,
                R.id.replay_artwork,
                R.id.replay_layout
        }, 0);
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(final View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.replay_artwork) {
                    // set round image : http://stackoverflow.com/questions/25278821/how-do-rounded-image-with-glide-library
                    Glide.with(ReplayActivity.this)
                    .load(cursor.getString(columnIndex)).asBitmap().centerCrop().into(new BitmapImageViewTarget((ImageView) view) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(ReplayActivity.this.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            view.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                    return true;
                } else if (view.getId() == R.id.replay_layout){
                    Glide.with(ReplayActivity.this)
                            .load(cursor.getString(columnIndex)).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            view.setBackground(new BitmapDrawable(resource));
                        }
                    });
                    return true;
                }
                return false;
            }
        });
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
        replayActivityBinding.setReplayData(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Reset the loader");
        }
        cursorAdapter.swapCursor(null);
        replayActivityBinding.setReplayData(null);
    }
}
