package com.stationmillenium.android.replay.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.libutils.drawer.DrawerUtils;
import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.databinding.ReplayActivityBinding;
import com.stationmillenium.android.replay.dto.PlaylistDTO;
import com.stationmillenium.android.replay.dto.TrackDTO;
import com.stationmillenium.android.replay.utils.SoundcloudPlaylistRestLoader;
import com.stationmillenium.android.replay.utils.SoundcloudTrackRestLoader;
import com.stationmillenium.android.replay.utils.SoundcloudTrackRestLoader.QueryType;

import java.io.Serializable;
import java.util.List;

import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.REPLAY;

/**
 * Activity for the replay
 * Created by vincent on 01/09/16.
 */
public class ReplayActivity extends AppCompatActivity implements LoaderCallbacks<List<? extends Serializable>> {

    private static final String TAG = "ReplayActivity";
    public static final int TRACK_LOADER_INDEX = 0;
    public static final int PLAYLIST_LOADER_INDEX = 1;
    private static final String LIMIT = "limit";
    private static final String SEARCH_TYPE = "search_type";
    private static final String SEARCH_QUERY = "search_query";
    private static final String SEARCH_PARAMS = "search_params";
    private static final String IS_SEARCH_VIEW_EXPANDED_BUNDLE = "expand_search_view";
    private static final String SEARCHVIEW_TEXT = "searchview_text";
    private static final int EXTRA_REPLAY_COUNT = 30;
    private static final int TOTAL_MAX_REPLAY = 200;
    public static final String REPLAY_TAG = "ReplayTag";

    private ReplayActivityBinding replayActivityBinding;
    private ReplayTitleFragment replayTitleFragment;
    private ReplayPlaylistFragment replayPlaylistFragment;
    private MenuItem searchMenuItem;
    private DrawerUtils drawerUtils;

    private Bundle searchParams;
    private boolean expandActionViewOnCreate;
    private String searchviewText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        replayActivityBinding = DataBindingUtil.setContentView(this, R.layout.replay_activity);
        replayActivityBinding.setActivity(this);
        replayActivityBinding.setItemCount(0);
        replayActivityBinding.replayViewpager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return (position == 0) ? new ReplayTitleFragment() : new ReplayPlaylistFragment();
            }

            @Override
            public int getCount() {
                return getResources().getStringArray(R.array.replay_tabs_title).length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return getResources().getStringArray(R.array.replay_tabs_title)[position];
            }
        });
        replayActivityBinding.replayViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private String titleTabTitle;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                replayActivityBinding.setTabIndex(position);
                replayActivityBinding.setItemCount(position == 0 ? replayTitleFragment.getItemCount() : replayPlaylistFragment.getItemCount());
                if (position == 0) {
                    replayActivityBinding.setItemCount(replayTitleFragment.getItemCount());
                    getSupportActionBar().setTitle(titleTabTitle);
                } else {
                    replayActivityBinding.setItemCount(replayPlaylistFragment.getItemCount());
                    titleTabTitle = getSupportActionBar().getTitle().toString();
                    setToolbarTitle(null);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        replayActivityBinding.replayTabs.setupWithViewPager(replayActivityBinding.replayViewpager);
        setSupportActionBar(replayActivityBinding.replayToolbar);

        if (savedInstanceState != null) {
            setToolbarTitle(savedInstanceState.getBundle(SEARCH_PARAMS));
            expandActionViewOnCreate = savedInstanceState.getBoolean(IS_SEARCH_VIEW_EXPANDED_BUNDLE);
            searchviewText = savedInstanceState.getString(SEARCHVIEW_TEXT);
        }

        drawerUtils = new DrawerUtils(this, replayActivityBinding.replayDrawerLayout, replayActivityBinding.replayToolbar, R.id.nav_drawer_replay);
    }

    /**
     * With tabs, fragment are loaded asynchronously and trigger data load, when they are loaded
     */
    public void requestTracksDataLoad() {
        if (getIntent().getStringExtra(REPLAY_TAG) != null) {
            Log.v(TAG, "Direct tag search");
            searchGenre(getIntent().getStringExtra(REPLAY_TAG));
        } else {
            getSupportLoaderManager().initLoader(TRACK_LOADER_INDEX, null, this).forceLoad();
        }
    }

    /**
     * With tabs, fragment are loaded asynchronously and trigger data load, when they are loaded
     */
    public void requestPlaylistDataLoad() {
        getSupportLoaderManager().initLoader(PLAYLIST_LOADER_INDEX, null, this).forceLoad();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.replay_menu, menu);
        searchMenuItem = menu.findItem(R.id.replay_search_menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        if (expandActionViewOnCreate) {
            MenuItemCompat.expandActionView(searchMenuItem);
            replayActivityBinding.searchFab.setVisibility(View.GONE);
            if (searchviewText != null) {
                searchView.setQuery(searchviewText, false);
            }
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                MenuItemCompat.collapseActionView(searchMenuItem);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // see : http://stackoverflow.com/questions/9327826/searchviews-oncloselistener-doesnt-work
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                replayActivityBinding.searchFab.setVisibility(View.VISIBLE);
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                replayActivityBinding.searchFab.setVisibility(View.GONE);
                return true;  // Return true to expand action view
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        PiwikTracker.trackScreenView(REPLAY);
    }

    @Override
    public Loader<List<? extends Serializable>> onCreateLoader(int id, Bundle args) {
        if (id == TRACK_LOADER_INDEX) {
            replayTitleFragment.setRefreshing(true);
            if (args != null) {
                if (args.containsKey(SEARCH_TYPE)) {
                    return (args.containsKey(LIMIT))
                            ? new SoundcloudTrackRestLoader(this, (QueryType) args.getSerializable(SEARCH_TYPE), args.getString(SEARCH_QUERY), args.getInt(LIMIT))
                            : new SoundcloudTrackRestLoader(this, (QueryType) args.getSerializable(SEARCH_TYPE), args.getString(SEARCH_QUERY));
                } else if (args.containsKey(LIMIT)) {
                    return new SoundcloudTrackRestLoader(this, args.getInt(LIMIT));
                }
            }
            return new SoundcloudTrackRestLoader(this);
        } else {
            replayPlaylistFragment.setRefreshing(true);
            return (args != null && args.containsKey(LIMIT)) ? new SoundcloudPlaylistRestLoader(this, args.getInt(LIMIT)) : new SoundcloudPlaylistRestLoader(this);
        }
    }

    @Override
    public void onLoadFinished(Loader<List<? extends Serializable>> loader, List<? extends Serializable> data) {
        if (loader instanceof SoundcloudTrackRestLoader) {
            Log.d(TAG, "Track loading is finished - display data...");
            replayTitleFragment.setReplayTitleList((List<TrackDTO>) data);
            replayTitleFragment.setRefreshing(false);
            replayActivityBinding.setItemCount(data.size());
        } else {
            Log.d(TAG, "Playlist loading is finished - display data...");
            replayPlaylistFragment.setReplayPlaylistList((List<PlaylistDTO>) data);
            replayPlaylistFragment.setRefreshing(false);
            replayActivityBinding.setItemCount(data.size());
        }
    }

    @Override
    public void onLoaderReset(Loader<List<? extends Serializable>> loader) {
        Log.d(TAG, "Reset the loader");
        replayTitleFragment.setReplayTitleList(null);
        replayTitleFragment.setRefreshing(false);
        replayActivityBinding.setItemCount(0);
    }

    public void onTrackRefresh() {
        Log.d(TAG, "Track data refresh requested");
        setToolbarTitle(null); // we reinit the search params to default
        getSupportLoaderManager().restartLoader(TRACK_LOADER_INDEX, null, this).forceLoad();
    }

    public void onPlaylistRefresh() {
        Log.d(TAG, "Playlist data refresh requested");
        getSupportLoaderManager().restartLoader(PLAYLIST_LOADER_INDEX, null, this).forceLoad();
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
        getSupportLoaderManager().restartLoader(TRACK_LOADER_INDEX, bundle, this).forceLoad();
    }

    /**
     * End scrolling happened
     * Trigger extra data load
     * @param loaderIndex the loader index to use, to select data type
     * @param replayCount the current replay count
     */
    public void triggerExtraDataLoad(int loaderIndex, int replayCount) {
        if (replayCount > 0) {
            if (replayCount <= TOTAL_MAX_REPLAY) {
                Log.d(TAG, "Load extra data");
                Bundle bundle = new Bundle();
                bundle.putInt(LIMIT, replayCount + EXTRA_REPLAY_COUNT);
                if (loaderIndex == TRACK_LOADER_INDEX && searchParams != null) {
                    Log.v(TAG, "Add search params for extra data load");
                    bundle.putAll(searchParams);
                }
                Snackbar.make(replayActivityBinding.replayCoordinatorLayout, (loaderIndex == TRACK_LOADER_INDEX) ? R.string.replay_load_more : R.string.playlist_load_more, Snackbar.LENGTH_SHORT).show();
                getSupportLoaderManager().restartLoader(loaderIndex, bundle, this).forceLoad();
            } else {
                Log.v(TAG, "All extra data already loaded");
                Snackbar.make(replayActivityBinding.replayCoordinatorLayout, (loaderIndex == TRACK_LOADER_INDEX) ? R.string.replay_load_more_max_reached : R.string.playlist_load_more_max_reached, Snackbar.LENGTH_SHORT).show();
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
        if (searchMenuItem != null) {
            MenuItemCompat.expandActionView(searchMenuItem);
            replayActivityBinding.replayAppbarLayout.setExpanded(true);
        }
    }

    public void openReplay(TrackDTO replayItem) {
        Log.d(TAG, "Open replay ; " + replayItem) ;
        Intent replayItemIntent = new Intent(this, ReplayItemActivity.class);
        replayItemIntent.putExtra(ReplayItemActivity.REPLAY_ITEM, replayItem);
        startActivity(replayItemIntent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (searchParams != null) {
            outState.putBundle(SEARCH_PARAMS, searchParams);
        }
        outState.putBoolean(IS_SEARCH_VIEW_EXPANDED_BUNDLE, ((searchMenuItem != null) && (MenuItemCompat.isActionViewExpanded(searchMenuItem))));
        if (searchMenuItem != null) {
            outState.putString(SEARCHVIEW_TEXT, ((SearchView) MenuItemCompat.getActionView(searchMenuItem)).getQuery().toString());
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG, "Search : " + query);
            Bundle bundle = new Bundle();
            bundle.putSerializable(SEARCH_TYPE, QueryType.SEARCH);
            bundle.putString(SEARCH_QUERY, query);
            setToolbarTitle(bundle);
            getSupportLoaderManager().restartLoader(TRACK_LOADER_INDEX, bundle, this).forceLoad();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerUtils.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerUtils.onPostCreate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerUtils.onConfigurationChanged(newConfig);
    }

    public void setReplayTitleFragment(ReplayTitleFragment replayTitleFragment) {
        this.replayTitleFragment = replayTitleFragment;
    }

    public void setReplayPlaylistFragment(ReplayPlaylistFragment replayPlaylistFragment) {
        this.replayPlaylistFragment = replayPlaylistFragment;
    }

    /**
     * Open playlist track list from playlist list
     * @param playlistDTO the playlist to open
     */
    public void openPlaylist(PlaylistDTO playlistDTO) {
        Log.v(TAG, "Playlist open required");
    }

}
