/**
 *
 */
package com.stationmillenium.android.activities.songsearchhistory;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.google.android.material.snackbar.Snackbar;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.fragments.datetime.DatePickerFragment;
import com.stationmillenium.android.activities.fragments.datetime.TimePickerFragment;
import com.stationmillenium.android.databinding.SongSearchHistoryActivityBinding;
import com.stationmillenium.android.libutils.DateTime;
import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.libutils.drawer.DrawerUtils;
import com.stationmillenium.android.libutils.intents.LocalIntents;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;
import com.stationmillenium.android.providers.SongHistoryContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import timber.log.Timber;

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
    private static final String SEARCH_TIME = "SearchTime";
    private static final String SEARCH_TIME_TYPE = "SearchTimeType";
    private static final int LOADER_INDEX = 1;
    private static final String DATE_PICKER_FRAGMENT = "DatePickerFragment";
    private static final String TIME_PICKER_FRAGMENT = "TimePickerFragment";

    //widgets list
    private SongSearchHistoryActivityBinding binding;
    private DrawerUtils drawerUtils;
    private SongSearchHistoryFragment fragment;

    //instance vars
    private Calendar searchTimeCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("Create the activity");
        binding = DataBindingUtil.setContentView(this, R.layout.song_search_history_activity);
        binding.setActivity(this);
        setSupportActionBar(binding.songSearchToolbar);
        drawerUtils = new DrawerUtils(this, binding.songSearchDrawerLayout, binding.songSearchToolbar, R.id.nav_drawer_song_history);
        fragment = (SongSearchHistoryFragment) getSupportFragmentManager().findFragmentById(R.id.song_search_fragment);

        //enable type-to-search feature
        //see : http://developer.android.com/guide/topics/search/search-dialog.html#InvokingTheSearchDialog
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        //init the loader
        fragment.setLoadingState(LOADING);
        LoaderManager.getInstance(this).initLoader(LOADER_INDEX, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PiwikTracker.trackScreenView(SONG_SEARCH_HISTORY);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int index, Bundle bundle) {
        Timber.d("Create the loader - index : " + index + " - bundle : " + bundle);

        Uri uri;
        if ((bundle != null) && (bundle.getBoolean(SEARCH_TIME_TYPE))) { //test if we need to do a time search
            Timber.d("Load time search cursor");
            //build the URI with the search query
            uri = SongHistoryContract.ROOT_URI.buildUpon()
                    .appendPath(SongHistoryContract.DATE_SEARCH_SEGMENT)
                    .appendPath(bundle.getString(SEARCH_TIME))
                    .build();

        } else { //do a initial cursor load
            Timber.d("Load initial cursor");
            uri = SongHistoryContract.CONTENT_URI;
        }

        //build the new cursor loader with the proper URI
        return new CursorLoader(this, uri, null, null, null, null);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Timber.d("Loading is finished - display data...");

        //display the proper intro text
        String introText;
        String cursorCount = String.valueOf((cursor != null) ? cursor.getCount() : "0");
        if (searchTimeCalendar != null) { //time query display intro
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
        Timber.d("Reset the loader");
        fragment.getCursorAdapter().swapCursor(null);
    }

    public void displaySongImage(long id) {
        Timber.d("Item selected");
        Cursor cursor = fragment.getCursorAdapter().getCursor();
        if (id < cursor.getCount()) { //check id is correct
            cursor.moveToPosition(Long.valueOf(id).intValue()); //move to correct row
            String imagePath = cursor.getString(cursor.getColumnIndex(SongHistoryContract.Columns.IMAGE_PATH)); //get the image path, if available
            if (imagePath != null) { //if image path found, manage it
                Timber.d("Image found for selected track : %s", imagePath);

                //display image
                String displayImageActivityTitle = getString(R.string.song_search_image_title,
                        cursor.getString(cursor.getColumnIndex(SongHistoryContract.Columns.ARTIST)),
                        cursor.getString(cursor.getColumnIndex(SongHistoryContract.Columns.TITLE)));
                Intent displayImageIntent = new Intent(this, SongSearchHistoryImageDisplayActivity.class);
                displayImageIntent.putExtra(LocalIntentsData.IMAGE_FILE_PATH.toString(), imagePath);
                displayImageIntent.putExtra(LocalIntentsData.IMAGE_TITLE.toString(), displayImageActivityTitle);
                startActivity(displayImageIntent);

            } else { //there is no image for this track
                Timber.d("No image found for selected track");
                Snackbar.make(binding.songSearchCoordinatorLayout, R.string.song_search_no_image, Snackbar.LENGTH_SHORT).show();
            }

        } else {
            Timber.w("Selected id greater than cursor size - do nothing");
        }
    }


    /**
     * Reinit the song search
     */
    private void reinitSongSearch() {
        Timber.d("Reinit song search to initial value");
        searchTimeCalendar = null; //reinit searched date
        fragment.setLoadingState(LOADING);
        LoaderManager.getInstance(this).restartLoader(LOADER_INDEX, null, this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Timber.d("New intent received : %s", intent);
        setIntent(intent); //save intent

        if (LocalIntents.ON_DATE_PICKED_UP.toString().equals(intent.getAction())) { //date picked up
            Timber.d("Date picked up intent received");
            //save the received data
            searchTimeCalendar = Calendar.getInstance();
            searchTimeCalendar.set(Calendar.YEAR, intent.getIntExtra(LocalIntentsData.SONG_SEARCH_YEAR.toString(), 0));
            searchTimeCalendar.set(Calendar.MONTH, intent.getIntExtra(LocalIntentsData.SONG_SEARCH_MONTH.toString(), 0));
            searchTimeCalendar.set(Calendar.DAY_OF_MONTH, intent.getIntExtra(LocalIntentsData.SONG_SEARCH_DATE.toString(), 0));

            launchTimePickerFragment(); //pick the time up

        } else if (LocalIntents.ON_TIME_PICKED_UP.toString().equals(intent.getAction())) { //time picked up
            Timber.d("Time picked up intent received");
            //save the received data
            if (searchTimeCalendar != null) {
                searchTimeCalendar.set(Calendar.HOUR_OF_DAY, intent.getIntExtra(LocalIntentsData.SONG_SEARCH_HOURS.toString(), 0));
                searchTimeCalendar.set(Calendar.MINUTE, intent.getIntExtra(LocalIntentsData.SONG_SEARCH_MINUTES.toString(), 0));
                Timber.d("Date to search : %s", searchTimeCalendar);
                //launch the time search
                launchTimeSearch();
            }

        } else { //other type of intent are not supported
            Timber.w("Intent not supported : %s", intent);
        }
    }

    /**
     * Launch the {@link DatePickerFragment}
     */
    public void launchDatePickerFragment() {
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
            String formattedDate = new DateTime(searchTimeCalendar.getTime()).toStringRfc3339();

            //init bundle params
            Bundle extraDataBundle = new Bundle();
            extraDataBundle.putBoolean(SEARCH_TIME_TYPE, true);
            extraDataBundle.putString(SEARCH_TIME, formattedDate);
            Timber.d("Time search params : %s", extraDataBundle);

            //launch the search
            fragment.setLoadingState(LOADING);
            LoaderManager.getInstance(this).restartLoader(LOADER_INDEX, extraDataBundle, this);

        } else {
            Timber.w("Date to search is null");
        }
    }

    /**
     * Called from data binding
     */
    public void onRefresh() {
        Timber.d("Swipe refresh requested");
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
