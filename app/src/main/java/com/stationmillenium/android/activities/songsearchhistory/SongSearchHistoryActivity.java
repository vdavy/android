/**
 *
 */
package com.stationmillenium.android.activities.songsearchhistory;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.fragments.datetime.DatePickerFragment;
import com.stationmillenium.android.activities.fragments.datetime.TimePickerFragment;
import com.stationmillenium.android.contentproviders.SongHistoryContract;
import com.stationmillenium.android.databinding.SongSearchHistoryActivityBinding;
import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.libutils.drawer.DrawerUtils;
import com.stationmillenium.android.libutils.intents.LocalIntents;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.stationmillenium.android.activities.songsearchhistory.SongSearchHistoryFragment.LoadingState.ERROR;
import static com.stationmillenium.android.activities.songsearchhistory.SongSearchHistoryFragment.LoadingState.LOADED;
import static com.stationmillenium.android.activities.songsearchhistory.SongSearchHistoryFragment.LoadingState.LOADING;
import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.SONG_SEARCH_HISTORY;

/**
 * Activity to display the song search history
 *
 * @author vincent
 */
public class SongSearchHistoryActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    //static parts
    private static final String TAG = "SearchHistoryActivity";
    private static final String SEARCH_QUERY = "SearchQuery";
    private static final String SEARCH_QUERY_TYPE = "SearchQueryType";
    private static final String SEARCH_TIME = "SearchTime";
    private static final String SEARCH_TIME_TYPE = "SearchTimeType";
    private static final int LOADER_INDEX = 1;
    private static final String DATE_PICKER_FRAGMENT = "DatePickerFragment";
    private static final String TIME_PICKER_FRAGMENT = "TimePickerFragment";
    private static final String IS_SEARCH_VIEW_EXPANDED_BUNDLE = "IsSearchViewExpandedBundle";
    private static final String SEARCHVIEW_TEXT = "searchview_text";

    //widgets list
    private MenuItem searchMenuItem;
    private SongSearchHistoryActivityBinding binding;
    private DrawerUtils drawerUtils;
    private SongSearchHistoryFragment fragment;

    //instance vars
    private String query;
    private Calendar searchTimeCalendar;
    private boolean expandActionViewOnCreate;
    private String searchviewText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Create the activity");
        }

        binding = DataBindingUtil.setContentView(this, R.layout.song_search_history_activity);
        setSupportActionBar(binding.songSearchToolbar);
        drawerUtils = new DrawerUtils(this, binding.songSearchDrawerLayout, binding.songSearchToolbar, R.id.nav_drawer_song_history);
        fragment = (SongSearchHistoryFragment) getSupportFragmentManager().findFragmentById(R.id.song_search_fragment);

        //should we re-expand search view ?
        if (savedInstanceState != null) {
            expandActionViewOnCreate = savedInstanceState.getBoolean(IS_SEARCH_VIEW_EXPANDED_BUNDLE);
            searchviewText = savedInstanceState.getString(SEARCHVIEW_TEXT);
        }

        //enable type-to-search feature
        //see : http://developer.android.com/guide/topics/search/search-dialog.html#InvokingTheSearchDialog
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        //init the loader
        fragment.setLoadingState(LOADING);
        getSupportLoaderManager().initLoader(LOADER_INDEX, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PiwikTracker.trackScreenView(SONG_SEARCH_HISTORY);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int index, Bundle bundle) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Create the loader - index : " + index + " - bundle : " + bundle);
        }

        Uri uri;
        if ((bundle != null) && (bundle.getBoolean(SEARCH_QUERY_TYPE))) { //test if we need to do a query search
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Load query search cursor");
            }

            //build the URI with the search query
            uri = SongHistoryContract.CONTENT_URI.buildUpon()
                    .appendPath(bundle.getString(SEARCH_QUERY))
                    .build();

        } else if ((bundle != null) && (bundle.getBoolean(SEARCH_TIME_TYPE))) { //test if we need to do a time search
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Load time search cursor");
            }

            //build the URI with the search query
            uri = SongHistoryContract.ROOT_URI.buildUpon()
                    .appendPath(SongHistoryContract.DATE_SEARCH_SEGMENT)
                    .appendPath(bundle.getString(SEARCH_TIME))
                    .build();

        } else { //do a initial cursor load
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Load initial cursor");
            }
            uri = SongHistoryContract.CONTENT_URI;
        }

        //build the new cursor loader with the proper URI
        return new CursorLoader(this, uri, null, null, null, null);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Loading is finished - display data...");

        //display the proper intro text
        String introText;
        String cursorCount = String.valueOf((cursor != null) ? cursor.getCount() : "0");
        if ((query != null) && (!String.valueOf("").equals(query))) { //text query display intro
            introText = getString(R.string.song_search_intro_text, cursorCount, query);
        } else if (searchTimeCalendar != null) { //time query display intro
            SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.song_history_date_format));
            introText = getString(R.string.song_search_time_intro_text, cursorCount, sdf.format(searchTimeCalendar.getTime()));
        } else {
            introText = getString(R.string.song_search_intro_text_no_search, cursorCount);
        }
        binding.setIntroText(introText);

        //display the widgets and cursor data
        fragment.setLoadingState((cursor != null && cursor.getCount() > 0) ? LOADED: ERROR);
        fragment.getCursorAdapter().swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Reset the loader");
        }
        fragment.getCursorAdapter().swapCursor(null);
    }

    public void displaySongImage(long id) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Item selected");
        }
        Cursor cursor = fragment.getCursorAdapter().getCursor();
        if (id < cursor.getCount()) { //chec id is correct
            cursor.moveToPosition(Long.valueOf(id).intValue()); //move to correct row
            String imagePath = cursor.getString(cursor.getColumnIndex(SongHistoryContract.Columns.IMAGE_PATH)); //get the image path, if available
            if (imagePath != null) { //if image path found, manage it
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Image found for selected track : " + imagePath);
                }

                //display image
                String displayImageActivityTitle = getString(R.string.song_search_image_title,
                        cursor.getString(cursor.getColumnIndex(SongHistoryContract.Columns.ARTIST)),
                        cursor.getString(cursor.getColumnIndex(SongHistoryContract.Columns.TITLE)));
                Intent displayImageIntent = new Intent(this, SongSearchHistoryImageDisplayActivity.class);
                displayImageIntent.putExtra(LocalIntentsData.IMAGE_FILE_PATH.toString(), imagePath);
                displayImageIntent.putExtra(LocalIntentsData.IMAGE_TITLE.toString(), displayImageActivityTitle);
                startActivity(displayImageIntent);

            } else { //there is no image for this track
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "No image found for selected track");
                }
                Snackbar.make(binding.songSearchCoordinatorLayout, R.string.song_search_no_image, Snackbar.LENGTH_SHORT).show();
            }

        } else {
            Log.w(TAG, "Selected id greater than cursor size - do nothing");
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        //configure search widget
        //see : http://developer.android.com/guide/topics/search/search-dialog.html#ConfiguringWidget
        //see also (for using compat API) : http://developer.android.com/guide/topics/ui/actionbar.html#ActionView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.song_search_menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
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
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                MenuItemCompat.collapseActionView(searchMenuItem);
                return false;
            }
        });

        if (expandActionViewOnCreate) {
            MenuItemCompat.expandActionView(searchMenuItem);
            searchView.setQuery(searchviewText, false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerUtils.onOptionsItemSelected(item)) {
            return true;
        } else {
            switch (item.getItemId()) {
                case R.id.song_search_menu_search: //launch a search
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Launch song search requested");
                    }
                    return false;

                case R.id.song_search_menu_time_search: //launch the time search
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Launch time search");
                    }
                    query = null;
                    collapseSearchView();
                    launchDatePickerFragment();
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }

    /**
     * Reinit the song search
     */
    private void reinitSongSearch() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Reinit song search to initial value");
        query = null; //reinit query
        searchTimeCalendar = null; //reinit searched date
        collapseSearchView();
        fragment.setLoadingState(LOADING);
        getSupportLoaderManager().restartLoader(LOADER_INDEX, null, this);
    }

    /**
     * Collapse the {@link SearchView} if needed
     */
    private void collapseSearchView() {
        if ((searchMenuItem != null) && (MenuItemCompat.isActionViewExpanded(searchMenuItem))) { //collapse the search view if needed
            MenuItemCompat.collapseActionView(searchMenuItem);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "New intent received : " + intent);
        }
        setIntent(intent); //save intent

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) { //if we are facing to search query
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Search intent received");
            }
            //get extra data prepared
            String query = intent.getStringExtra(SearchManager.QUERY);
            Bundle extraDataBundle = new Bundle();
            extraDataBundle.putBoolean(SEARCH_QUERY_TYPE, true);
            extraDataBundle.putString(SEARCH_QUERY, query);
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Search params : " + extraDataBundle);

            //update search field value
            this.query = query;

            //launch the search
            fragment.setLoadingState(LOADING);
            getSupportLoaderManager().restartLoader(LOADER_INDEX, extraDataBundle, this);

        } else if (LocalIntents.ON_DATE_PICKED_UP.toString().equals(intent.getAction())) { //date picked up
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Date picked up intent received");
            }
            //save the received data
            searchTimeCalendar = Calendar.getInstance();
            searchTimeCalendar.set(Calendar.YEAR, intent.getIntExtra(LocalIntentsData.SONG_SEARCH_YEAR.toString(), 0));
            searchTimeCalendar.set(Calendar.MONTH, intent.getIntExtra(LocalIntentsData.SONG_SEARCH_MONTH.toString(), 0));
            searchTimeCalendar.set(Calendar.DAY_OF_MONTH, intent.getIntExtra(LocalIntentsData.SONG_SEARCH_DATE.toString(), 0));

            launchTimePickerFragment(); //pick the time up

        } else if (LocalIntents.ON_TIME_PICKED_UP.toString().equals(intent.getAction())) { //time picked up
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Time picked up intent received");
            }
            //save the received data
            if (searchTimeCalendar != null) {
                searchTimeCalendar.set(Calendar.HOUR_OF_DAY, intent.getIntExtra(LocalIntentsData.SONG_SEARCH_HOURS.toString(), 0));
                searchTimeCalendar.set(Calendar.MINUTE, intent.getIntExtra(LocalIntentsData.SONG_SEARCH_MINUTES.toString(), 0));
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Date to search : " + searchTimeCalendar);
                }
                //launch the time search
                launchTimeSearch();
            }

        } else { //other type of intent are not supported
            Log.w(TAG, "Intent not supported : " + intent);
        }
    }

    /**
     * Launch the {@link DatePickerFragment}
     */
    private void launchDatePickerFragment() {
        DialogFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(getSupportFragmentManager(), DATE_PICKER_FRAGMENT);
    }

    /**
     * Launch the {@link TimePickerFragment}
     */
    private void launchTimePickerFragment() {
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.show(getSupportFragmentManager(), TIME_PICKER_FRAGMENT);
    }

    /**
     * Launch the time search based on selected date
     */
    @SuppressLint("SimpleDateFormat")
    private void launchTimeSearch() {
        if (searchTimeCalendar != null) {
            //get formatted time
            SimpleDateFormat sdf = new SimpleDateFormat(SongHistoryContract.DATE_SEARCH_FORMAT);
            String formattedDate = sdf.format(searchTimeCalendar.getTime());

            //init bundle params
            Bundle extraDataBundle = new Bundle();
            extraDataBundle.putBoolean(SEARCH_TIME_TYPE, true);
            extraDataBundle.putString(SEARCH_TIME, formattedDate);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Time search params : " + extraDataBundle);
            }

            //launch the search
            query = null;
            fragment.setLoadingState(LOADING);
            getSupportLoaderManager().restartLoader(LOADER_INDEX, extraDataBundle, this);

        } else {
            Log.w(TAG, "Date to search is null");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //save the expanded state for screen rotation
        outState.putBoolean(IS_SEARCH_VIEW_EXPANDED_BUNDLE, ((searchMenuItem != null) && (MenuItemCompat.isActionViewExpanded(searchMenuItem))));
        outState.putString(SEARCHVIEW_TEXT, ((SearchView) MenuItemCompat.getActionView(searchMenuItem)).getQuery().toString());
        super.onSaveInstanceState(outState);
    }

    public void onRefresh() {
        Log.d(TAG, "Swipe refresh requested");
        fragment.setLoadingState(LOADING);
        reinitSongSearch();
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

}
