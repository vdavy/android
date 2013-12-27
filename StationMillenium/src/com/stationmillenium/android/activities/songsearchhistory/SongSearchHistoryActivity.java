/**
 * 
 */
package com.stationmillenium.android.activities.songsearchhistory;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.contentproviders.SongHistoryContentProvider.SongHistoryContract;
import com.stationmillenium.android.utils.Utils;
import com.stationmillenium.android.utils.intents.LocalIntentsData;

/**
 * Activity to display the song search history
 * @author vincent
 *
 */
public class SongSearchHistoryActivity extends ActionBarActivity implements LoaderCallbacks<Cursor>, OnItemClickListener {

	//static parts
	private static final String TAG = "SongSearchHistoryActivity";
	private static final String SEARCH_QUERY = "SearchQuery"; 
	private static final String SEARCH_QUERY_TYPE = "SearchQueryType";
	private static final int LOADER_INDEX = 1;
	
	//widgets list
	private ListView historyListView;
	private ProgressBar progressBar;
	private TextView noDataTextView;
	private TextView introTextView;
	private MenuItem searchMenuItem;
	
	//instance vars
	private SimpleCursorAdapter cursorAdapter;
	private String query;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Create the activity");
		super.onCreate(savedInstanceState);
		
		//init widgets
		setContentView(R.layout.song_search_history_activity);
		historyListView = (ListView) findViewById(R.id.song_history_list);
		progressBar = (ProgressBar) findViewById(R.id.song_history_progressbar);
		noDataTextView = (TextView) findViewById(R.id.song_history_no_data_text);
		introTextView = (TextView) findViewById(R.id.song_history_search_result_text);
		
		//set up action bar
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
				
		//cursor adapter
		cursorAdapter = new SimpleCursorAdapter(this, R.layout.song_search_history_list_item, null, 
				new String[]{
					SongHistoryContract.Columns.DATE,
					SongHistoryContract.Columns.ARTIST,
					SongHistoryContract.Columns.TITLE
				}, 
				new int[] {
					R.id.song_history_item_date_text,
					R.id.song_history_item_artist_text,
					R.id.song_history_item_title_text
				}, 0);
		historyListView.setAdapter(cursorAdapter);
		historyListView.setOnItemClickListener(this);
		
		//enable type-to-search feature
		//see : http://developer.android.com/guide/topics/search/search-dialog.html#InvokingTheSearchDialog
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		
		//init the loader
		displayLoadingWidgets();
		getSupportLoaderManager().initLoader(LOADER_INDEX, null, this);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int index, Bundle bundle) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Create the loader - index : " + index + " - bundle : " + bundle);
		
		Uri uri = null;
		if ((bundle != null) && (bundle.getBoolean(SEARCH_QUERY_TYPE))) { //test if we need to do a search
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Load search cursor");
			
			//build the URI with the search query
			uri = SongHistoryContract.CONTENT_URI.buildUpon()
					.appendPath(bundle.getString(SEARCH_QUERY))
					.build();
			
		} else { //do a initial cursor load
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Load initial cursor");
			uri = SongHistoryContract.CONTENT_URI;
		}
		
		//build the new cursor loader with the proper URI
		return new CursorLoader(this, uri, null, null, null, null); 
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Loading is finished - display data...");
		
		//display the proper intro text
		String introText = "";
		String cursorCount = String.valueOf((cursor != null) ? cursor.getCount() : "0");
		if ((query != null) && (!String.valueOf("").equals(query)))
			introText = getString(R.string.song_search_intro_text, cursorCount, query);
		else
			introText = getString(R.string.song_search_intro_text_no_search, cursorCount);
		introTextView.setText(Html.fromHtml(introText));
		
		//display the widgets and cursor data
		displayLoadedDataWidgets((cursor != null) && (cursor.getCount() > 0));
		cursorAdapter.swapCursor(cursor);		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Reset the loader");
		cursorAdapter.swapCursor(null);
	}

	/**
	 * Display the widgets for data loading
	 */
	private void displayLoadingWidgets() {
		historyListView.setVisibility(View.INVISIBLE);
		noDataTextView.setVisibility(View.GONE);
		introTextView.setVisibility(View.INVISIBLE);
		progressBar.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Display the widgets for loaded data
	 * @param dataProperlyLoaded <code>true</code> if the data have been properly loaded (no error), <code>false</code> if data loading error occured
	 */
	private void displayLoadedDataWidgets(boolean dataProperlyLoaded) {
		historyListView.setVisibility(View.VISIBLE);
		introTextView.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
		noDataTextView.setVisibility((dataProperlyLoaded) ? View.GONE : View.VISIBLE);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Item selected");
		Cursor cursor = cursorAdapter.getCursor();
		if (id < cursor.getCount()) { //chec id is correct
			cursor.moveToPosition(Long.valueOf(id).intValue()); //move to correct row 
			String imagePath = cursor.getString(cursor.getColumnIndex(SongHistoryContract.Columns.IMAGE_PATH)); //get the image path, if available
			if (imagePath != null) { //if image path found, manage it
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Image found for selected track : " + imagePath);
				
				//display image
				String displayImageActivityTitle = getString(R.string.song_search_image_title, 
						cursor.getString(cursor.getColumnIndex(SongHistoryContract.Columns.ARTIST)), 
						cursor.getString(cursor.getColumnIndex(SongHistoryContract.Columns.TITLE)));
				Intent displayImageIntent = new Intent(this, SongSearchHistoryImageDisplayActivity.class);
				displayImageIntent.putExtra(LocalIntentsData.IMAGE_FILE_PATH.toString(), imagePath);
				displayImageIntent.putExtra(LocalIntentsData.IMAGE_TITLE.toString(), displayImageActivityTitle);
				startActivity(displayImageIntent);
						
			} else { //there is no image for this track
				if (BuildConfig.DEBUG)
					Log.d(TAG, "No image found for selected track");
				Toast.makeText(this, R.string.song_search_no_image, Toast.LENGTH_SHORT).show();
			}
				
		} else
			Log.w(TAG, "Selected id greater than cursor size - do nothing");
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        //configure search widget
        //see : http://developer.android.com/guide/topics/search/search-dialog.html#ConfiguringWidget
        //see also (for using compat API) : http://developer.android.com/guide/topics/ui/actionbar.html#ActionView
        if (Utils.isAPILevel11Available()) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchMenuItem = menu.findItem(R.id.song_search_menu_search);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        }

        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.song_search_menu_search: //launch a search
            	if (!Utils.isAPILevel11Available()) { //if we don't have action bar 
            		//TODO : check on api level 10
	            	if (BuildConfig.DEBUG)
						Log.d(TAG, "Launch song search requested");
	                onSearchRequested();
	                return true;
            	} else
            		return false;
                
            case R.id.song_search_menu_reinit: //reinit the loader to initial search
            	if (BuildConfig.DEBUG)
					Log.d(TAG, "Reinit song search to initial value");
            	if ((searchMenuItem != null) && (MenuItemCompat.isActionViewExpanded(searchMenuItem))) //collapse the search view if needed
            		MenuItemCompat.collapseActionView(searchMenuItem);
            	displayLoadingWidgets();
            	getSupportLoaderManager().restartLoader(LOADER_INDEX, null, this);
            	return true;
            	
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	 
	@Override
	protected void onNewIntent(Intent intent) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "New intent received : " + intent);
		
		setIntent(intent); //save intent
		
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) { //if we are facing to search query
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
			displayLoadingWidgets();
			getSupportLoaderManager().restartLoader(LOADER_INDEX, extraDataBundle, this);
			
		} else //other type of intent are not supported
			Log.w(TAG, "Intent not supported : " + intent);
	}
	
}
