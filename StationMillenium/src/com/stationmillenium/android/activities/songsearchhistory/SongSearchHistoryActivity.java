/**
 * 
 */
package com.stationmillenium.android.activities.songsearchhistory;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.contentproviders.SongHistoryContentProvider.SongHistoryContract;
import com.stationmillenium.android.utils.intents.LocalIntentsData;

/**
 * Activity to display the song search history
 * @author vincent
 *
 */
public class SongSearchHistoryActivity extends ActionBarActivity implements LoaderCallbacks<Cursor>, OnItemClickListener {

	//static parts
	private static final String TAG = "SongSearchHistoryActivity";
	
	//widgets list
	private ListView historyListView;
	private ProgressBar progressBar;
	private TextView noDataTextView;
	
	//instance vars
	private SimpleCursorAdapter cursorAdapter;
	
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
		
		//init the loader
		displayLoadingWidgets();
		getSupportLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Create the loader");
		return new CursorLoader(this, SongHistoryContract.CONTENT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Loading is finished - display data...");
		displayLoadedDataWidgets(cursor != null);
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
		noDataTextView.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Display the widgets for loaded data
	 * @param dataProperlyLoaded <code>true</code> if the data have been properly loaded (no error), <code>false</code> if data loading error occured
	 */
	private void displayLoadedDataWidgets(boolean dataProperlyLoaded) {
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
}
