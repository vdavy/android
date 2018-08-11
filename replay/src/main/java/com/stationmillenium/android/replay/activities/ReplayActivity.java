package com.stationmillenium.android.replay.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
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
import com.stationmillenium.android.replay.utils.ReplayPlaylistRestLoader;
import com.stationmillenium.android.replay.utils.SoundcloudTrackRestLoader;
import com.stationmillenium.android.replay.utils.SoundcloudTrackRestLoader.QueryType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.REPLAY;

/**
 * Activity for the replay
 * Created by vincent on 01/09/16.
 */
public class ReplayActivity extends AppCompatActivity implements LoaderCallbacks<List<? extends Serializable>> {

    public static final int TRACK_LOADER_INDEX = 0;
    public static final int PLAYLIST_LOADER_INDEX = 1;
    private static final String LIMIT = "limit";
    private static final String ALREADY_LOADED_ITEMS = "already_loaded_items";
    private static final String SEARCH_TYPE = "search_type";
    private static final String SEARCH_QUERY = "search_query";
    private static final String SEARCH_PARAMS = "search_params";
    private static final String IS_SEARCH_VIEW_EXPANDED_BUNDLE = "expand_search_view";
    private static final String SEARCHVIEW_TEXT = "searchview_text";
    private static final int TOTAL_MAX_REPLAY = 200;
    private static final int PLAYLIST_TAB_INDEX = 0;
    private static final int TITLES_TAB_INDEX = 1;
    public static final String REPLAY_TAG = "ReplayTag";
    public static final String PLAYLIST_BUNDLE = "PlaylistBundle";

    private ReplayActivityBinding binding;
    private ReplayTitleFragment replayTitleFragment;
    private ReplayPlaylistFragment replayPlaylistFragment;
    private MenuItem searchMenuItem;
    private DrawerUtils drawerUtils;

    private Bundle searchParams;
    private Bundle savedSearchParams;
    private boolean expandActionViewOnCreate;
    private String searchviewText;
    private String titleTabTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        titleTabTitle = getString(R.string.replay_toolbar_normal_title);
        binding = DataBindingUtil.setContentView(this, R.layout.replay_activity);
        binding.setActivity(this);
        binding.setItemCount(0);
        binding.replayViewpager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return (position == PLAYLIST_TAB_INDEX) ? new ReplayPlaylistFragment() : new ReplayTitleFragment();
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
        binding.replayViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                binding.setTabIndex(position);
                binding.setItemCount(position == TITLES_TAB_INDEX ? replayTitleFragment.getItemCount() : replayPlaylistFragment.getItemCount());
                if (position == PLAYLIST_TAB_INDEX) {
                    binding.setItemCount(replayPlaylistFragment.getItemCount());
                    titleTabTitle = getSupportActionBar().getTitle().toString();
                    if (savedSearchParams != null) {
                        setToolbarTitle(savedSearchParams);
                        savedSearchParams = null;
                    } else {
                        setToolbarTitle(null);
                    }
                } else {
                    binding.setItemCount(replayTitleFragment.getItemCount());
                    getSupportActionBar().setTitle(titleTabTitle);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });
        binding.replayTabs.setupWithViewPager(binding.replayViewpager);
        setSupportActionBar(binding.replayToolbar);

        if (savedInstanceState != null) {
            setToolbarTitle(savedInstanceState.getBundle(SEARCH_PARAMS));
            if (searchParams != null && searchParams.containsKey(PLAYLIST_BUNDLE)) { //restore saved title for playlists
                getSupportActionBar().setTitle(titleTabTitle);
            }
            expandActionViewOnCreate = savedInstanceState.getBoolean(IS_SEARCH_VIEW_EXPANDED_BUNDLE);
            searchviewText = savedInstanceState.getString(SEARCHVIEW_TEXT);
        }

        drawerUtils = new DrawerUtils(this, binding.replayDrawerLayout, binding.replayToolbar, R.id.nav_drawer_replay);
    }

    /**
     * With tabs, fragment are loaded asynchronously and trigger data load, when they are loaded
     */
    public void requestTracksDataLoad() {
        if (getIntent().getStringExtra(REPLAY_TAG) != null) {
            Timber.v("Direct tag search");
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
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        if (expandActionViewOnCreate) {
            searchMenuItem.expandActionView();
            binding.searchFab.setVisibility(View.GONE);
            if (searchviewText != null) {
                searchView.setQuery(searchviewText, false);
            }
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchMenuItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // see : http://stackoverflow.com/questions/9327826/searchviews-oncloselistener-doesnt-work
        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                binding.searchFab.setVisibility(View.VISIBLE);
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                binding.searchFab.setVisibility(View.GONE);
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
            return createTrackLoader(args);
        } else {
            return createPlaylistLoader(args);
        }
    }

    @NonNull
    private Loader<List<? extends Serializable>> createPlaylistLoader(Bundle args) {
        replayPlaylistFragment.setRefreshing(true);
        if (args != null) {
            if (args.containsKey(SEARCH_QUERY)) {
                return (args.containsKey(LIMIT))
                        ? new ReplayPlaylistRestLoader(this, args.getString(SEARCH_QUERY),
                            args.getInt(LIMIT), (List<PlaylistDTO>) args.getSerializable(ALREADY_LOADED_ITEMS))
                        : new ReplayPlaylistRestLoader(this, args.getString(SEARCH_QUERY));
            } else if (args.containsKey(LIMIT)) {
                return new ReplayPlaylistRestLoader(this, args.getInt(LIMIT), (List<PlaylistDTO>) args.getSerializable(ALREADY_LOADED_ITEMS));
            }
        }
        return new ReplayPlaylistRestLoader(this);
    }

    @NonNull
    private Loader<List<? extends Serializable>> createTrackLoader(Bundle args) {
        replayTitleFragment.setRefreshing(true);
        if (args != null) {
            if (args.containsKey(PLAYLIST_BUNDLE)) {
                binding.replayViewpager.setCurrentItem(TITLES_TAB_INDEX);
                return (args.containsKey(LIMIT))
                        ? new SoundcloudTrackRestLoader(this, (PlaylistDTO) args.get(PLAYLIST_BUNDLE), args.getInt(LIMIT))
                        : new SoundcloudTrackRestLoader(this, (PlaylistDTO) args.get(PLAYLIST_BUNDLE));
            } else if (args.containsKey(SEARCH_TYPE)) {
                return (args.containsKey(LIMIT))
                        ? new SoundcloudTrackRestLoader(this, (QueryType) args.getSerializable(SEARCH_TYPE), args.getString(SEARCH_QUERY),
                            args.getInt(LIMIT))
                        : new SoundcloudTrackRestLoader(this, (QueryType) args.getSerializable(SEARCH_TYPE), args.getString(SEARCH_QUERY));
            } else if (args.containsKey(LIMIT)) {
                return new SoundcloudTrackRestLoader(this, args.getInt(LIMIT));
            }
        }
        return new SoundcloudTrackRestLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<? extends Serializable>> loader, List<? extends Serializable> data) {
        if (loader instanceof SoundcloudTrackRestLoader) {
            Timber.d("Track loading is finished - display data...");
            replayTitleFragment.setReplayTitleList((List<TrackDTO>) data);
            replayTitleFragment.setRefreshing(false);
            if (binding.replayViewpager.getCurrentItem() == TITLES_TAB_INDEX) {
                binding.setItemCount(data.size());
            }
        } else {
            Timber.d("Playlist loading is finished - display data...");
            replayPlaylistFragment.setReplayPlaylistList((List<PlaylistDTO>) data);
            replayPlaylistFragment.setRefreshing(false);
            if (binding.replayViewpager.getCurrentItem() == PLAYLIST_TAB_INDEX) {
                binding.setItemCount(data.size());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<? extends Serializable>> loader) {
        Timber.d("Reset the loader");
        replayTitleFragment.setReplayTitleList(null);
        replayTitleFragment.setRefreshing(false);
        binding.setItemCount(0);
    }

    public void onTrackRefresh() {
        Timber.d("Track data refresh requested");
        setToolbarTitle(null); // we reinit the search params to default
        getSupportLoaderManager().restartLoader(TRACK_LOADER_INDEX, null, this).forceLoad();
    }

    public void onPlaylistRefresh() {
        Timber.d("Playlist data refresh requested");
        setToolbarTitle(null); // we reinit the search params to default
        getSupportLoaderManager().restartLoader(PLAYLIST_LOADER_INDEX, null, this).forceLoad();
    }

    /**
     * Search for the specified genre
     * @param genre the genre to search for
     */
    public void searchGenre(String genre) {
        Timber.d("Search genre : %s", genre);
        Bundle bundle = new Bundle();
        bundle.putSerializable(SEARCH_TYPE, QueryType.GENRE);
        bundle.putString(SEARCH_QUERY, genre);
        binding.replayViewpager.setCurrentItem(TITLES_TAB_INDEX);
        setToolbarTitle(bundle);
        getSupportLoaderManager().restartLoader(TRACK_LOADER_INDEX, bundle, this).forceLoad();
    }

    /**
     * End scrolling happened
     * Trigger extra data load
     * @param loaderIndex the loader index to use, to select data type
     * @param listItems the current item for re-injection
     */
    public void triggerExtraDataLoad(int loaderIndex, List<? extends Serializable> listItems) {
        if (listItems.size() > 0) {
            if ((listItems.size() % getResources().getInteger(R.integer.default_page_size) > 0)
                || (searchParams != null && searchParams.containsKey(PLAYLIST_BUNDLE) && listItems.size() >= ((PlaylistDTO) searchParams.get(PLAYLIST_BUNDLE)).getTrackCount())) {
                Timber.v("All extra data already loaded");
                Snackbar.make(binding.replayCoordinatorLayout,
                        (loaderIndex == TRACK_LOADER_INDEX)
                                ? R.string.playlist_load_playlist_replay_more_max_reached
                                : R.string.playlist_load_playlist_more_max_reached,
                        Snackbar.LENGTH_SHORT).show();
            } else if (listItems.size() <= TOTAL_MAX_REPLAY) {
                Timber.d("Load extra data");
                Bundle bundle = new Bundle();
                bundle.putInt(LIMIT, listItems.size() + getResources().getInteger(R.integer.default_page_size));
                bundle.putSerializable(ALREADY_LOADED_ITEMS, (ArrayList<Serializable>) listItems);
                if (loaderIndex == TRACK_LOADER_INDEX && searchParams != null) {
                    Timber.v("Add search params for extra data load");
                    bundle.putAll(searchParams);
                }
                Snackbar.make(binding.replayCoordinatorLayout, (loaderIndex == TRACK_LOADER_INDEX)
                            ? R.string.replay_load_more
                            : R.string.playlist_load_more,
                        Snackbar.LENGTH_SHORT).show();
                getSupportLoaderManager().restartLoader(loaderIndex, bundle, this).forceLoad();
            } else {
                Timber.v("All extra data already loaded");
                Snackbar.make(binding.replayCoordinatorLayout, (loaderIndex == TRACK_LOADER_INDEX)
                            ? R.string.replay_load_more_max_reached
                            : R.string.playlist_load_more_max_reached,
                        Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Timber.v("Empty replay list - extra load disabled");
        }
    }

    /**
     * Launch replay search
     */
    public void triggerSearch() {
        Timber.d("Trigger search");
        if (searchMenuItem != null) {
            searchMenuItem.expandActionView();
            binding.replayAppbarLayout.setExpanded(true);
        }
    }

    public void openReplay(TrackDTO replayItem) {
        Timber.d("Open replay ; %s", replayItem);
        Intent replayItemIntent = new Intent(this, ReplayItemActivity.class);
        replayItemIntent.putExtra(ReplayItemActivity.REPLAY_ITEM, replayItem);
        startActivity(replayItemIntent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (searchParams != null) {
            outState.putBundle(SEARCH_PARAMS, searchParams);
        }
        outState.putBoolean(IS_SEARCH_VIEW_EXPANDED_BUNDLE, ((searchMenuItem != null) && (searchMenuItem.isActionViewExpanded())));
        if (searchMenuItem != null) {
            outState.putString(SEARCHVIEW_TEXT, ((SearchView) searchMenuItem.getActionView()).getQuery().toString());
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
            if (searchParams.containsKey(PLAYLIST_BUNDLE)) {
                titleTabTitle = getString(R.string.replay_toolbar_playlist_title, ((PlaylistDTO) searchParams.get(PLAYLIST_BUNDLE)).getTitle());
            } else {
                getSupportActionBar().setTitle(getString(R.string.replay_toolbar_search_title, searchParams.getString(SEARCH_QUERY)));
            }
        } else {
            getSupportActionBar().setTitle(R.string.replay_toolbar_normal_title);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Timber.d("Search : %s", query);
            Bundle bundle = new Bundle();
            bundle.putString(SEARCH_QUERY, query);
            setToolbarTitle(bundle);
            getSupportLoaderManager().restartLoader(binding.replayViewpager.getCurrentItem() == PLAYLIST_TAB_INDEX
                    ? PLAYLIST_LOADER_INDEX
                    : TRACK_LOADER_INDEX, bundle, this).forceLoad();
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
        Timber.v("Playlist open required");
        // save search params if we are in search mode
        if (searchParams != null && searchParams.containsKey(SEARCH_QUERY)) {
            savedSearchParams = searchParams;
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable(PLAYLIST_BUNDLE, playlistDTO);
        setToolbarTitle(bundle);
        getSupportLoaderManager().restartLoader(TRACK_LOADER_INDEX, bundle, this).forceLoad();
    }

    @Override
    public void onBackPressed() {
        if (binding.replayViewpager.getCurrentItem() == TITLES_TAB_INDEX) {
            binding.replayViewpager.setCurrentItem(PLAYLIST_TAB_INDEX);
        } else {
            super.onBackPressed();
        }
    }
}
