/**
 * 
 */
package com.stationmillenium.android.contentproviders;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.BaseColumns;
import android.util.Log;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.R.string;
import com.stationmillenium.android.dto.CurrentTitleDTO.Song;
import com.stationmillenium.android.exceptions.XMLParserException;
import com.stationmillenium.android.utils.network.NetworkUtils;
import com.stationmillenium.android.utils.xml.XMLSongHistoryParser;

/**
 * Content provider for songs history search
 * @author vincent
 *
 */
public class SongHistoryContentProvider extends ContentProvider {
	
	/**
	 * Contract for the {@link SongHistoryContentProvider}
	 * @author vincent
	 *
	 */
	public static final class SongHistoryContract {
		public static final String AUTHORITY = "com.stationmillenium.android.contentproviders.SongHistoryContentProvider";
		public static final Uri CONTENT_URI = buildContentURI();
		private static final String DEFAULT_MATCH = "songHistory";
		public static final String DATE_SEARCH_SEGMENT = DEFAULT_MATCH + "Date";
		private static final String FULL_TEXT_SEARCH = DEFAULT_MATCH + "/*";
		private static final String DATE_SEARCH = DEFAULT_MATCH + "Date/*";
		private static final String MIME_TYPE = "vnd." + AUTHORITY + "." + DEFAULT_MATCH;
		
		/**
		 * Init the content {@link Uri} 
		 * @return the {@link Uri}
		 */
		private static Uri buildContentURI() {
			Builder uriBuilder = new Builder();
			uriBuilder.scheme(ContentResolver.SCHEME_CONTENT)
			.authority(AUTHORITY)
			.appendPath(DEFAULT_MATCH);
			return uriBuilder.build();
		}
		
		/**
		 * List of available columns in the returned {@link Cursor}
		 * @author vincent
		 *
		 */
		public interface Columns extends BaseColumns { 
			String ARTIST = "artist";
			String TITLE = "title";
			String DATE = "date";
			String IMAGE_PATH = "image_path";
			String IMAGE_WIDTH = "image_width";
			String IMAGE_HEIGHT = "image_height";
		}
	}
	
	//static part
	private static final String TAG = "SongHistoryContentProvider";
	private static final String ACTION_PARAM_NAME = "action";
	private static final String QUERY_PARAM_NAME = "query";
	private static final String SONG_SUGGEST_SEPARATOR = " - ";
	
	/**
	 * List of available HTTP action code for server query
	 * @author vincent
	 *
	 */
	private enum HttpActionCode {
		DEFAULT,
		FULL_TEXT,
		DATE,
		SUGGEST
	}
	
	//list of uri matcher states
	private static final int ALL_SONGS_SEARCH = 0;
	private static final int FULL_TEXT_SEARCH = 1;
	private static final int DATE_SEARCH = 2;
	private static final int SUGGEST_SEARCH = 3;
	private static final UriMatcher URI_MATCHER = buildURIMatcher();
	
	/**
	 * Build the {@link UriMatcher} for {@link SongHistoryContentProvider}
	 * @return the {@link UriMatcher}
	 */
	private static UriMatcher buildURIMatcher() {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(SongHistoryContract.AUTHORITY, SongHistoryContract.DEFAULT_MATCH, ALL_SONGS_SEARCH);
		matcher.addURI(SongHistoryContract.AUTHORITY, SongHistoryContract.FULL_TEXT_SEARCH, FULL_TEXT_SEARCH);
		matcher.addURI(SongHistoryContract.AUTHORITY, SongHistoryContract.DATE_SEARCH, DATE_SEARCH);
		matcher.addURI(SongHistoryContract.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SUGGEST_SEARCH);
		return matcher;
	}
	
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Initialization of SongHistoryContentProvider");
		
		return true;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Querying data for URI : " + uri);
	
		switch (URI_MATCHER.match(uri)) { //return the proper mime type
			case ALL_SONGS_SEARCH: //case of all song search, no param needed
				Map<String, String> params = prepareHttpQueryParams(HttpActionCode.DEFAULT, null);
				return sendQueryAndGetCursor(params);
				
			case DATE_SEARCH: //case of full text query, process query string
				return sendRequestFromURI(uri, HttpActionCode.DATE);
				
			case FULL_TEXT_SEARCH: //case of full text query, process query string
				return sendRequestFromURI(uri, HttpActionCode.FULL_TEXT);
	
			case SUGGEST_SEARCH: //case of suggest query, process query string as full text with suggest projection
				return sendQueryForSuggestSearch(uri);
				
			default:
				 throw new IllegalArgumentException("Unsupported Uri: " + uri);
		}
	}

	/**
	 * Send the query for suggest search
	 * @param uri the {@link Uri} to get the param
	 * @return the {@link Cursor} with data
	 */
	private Cursor sendQueryForSuggestSearch(Uri uri) {
		//prepare params
		String query = uri.getLastPathSegment();
		Map<String, String> params = prepareHttpQueryParams(HttpActionCode.SUGGEST, query);
		
		//network access
		InputStream is = NetworkUtils.connectToURL(getContext().getString(R.string.song_history_url), //get connection
				params, 
				getContext().getString(R.string.song_history_request_method), 
				getContext().getString(R.string.song_history_content_type), 
				Integer.parseInt(getContext().getString(R.string.song_history_connect_timeout)), 
				Integer.parseInt(getContext().getString(R.string.song_history_read_timeout)));
		
		try { //convert data into cursor
			List<Song> songList = new XMLSongHistoryParser(is).parseXML(); //parse the XML
			MatrixCursor cursor = new MatrixCursor(new String[] { //init the returned cursor
				SongHistoryContract.Columns._ID,
				SearchManager.SUGGEST_COLUMN_TEXT_1,
				SearchManager.SUGGEST_COLUMN_TEXT_2,
				SearchManager.SUGGEST_COLUMN_QUERY
			});
			
			int index = 0;
			for (Song song : songList) { //add each song into the cursor
				cursor.addRow(new Object[] {
					index,
					song.getTitle(),
					song.getArtist(),
					song.getArtist() + SONG_SUGGEST_SEPARATOR + song.getTitle(),
				});
				
				index++;
			}
			
			return cursor;
		} catch (XMLParserException e) { //if any error occurs
			Log.e(TAG, "Error while parsing XML", e);
			return null;
		}
	}

	/**
	 * Send a query by getting params from the {@link Uri}
	 * @param uri the {@link Uri} to get params
	 * @param action the {@link HttpActionCode} to send right query
	 * @return the corresponding {@link Cursor}
	 */
	private Cursor sendRequestFromURI(Uri uri, HttpActionCode action) {
		String query = uri.getLastPathSegment();
		Map<String, String> params = prepareHttpQueryParams(action, query);
		return sendQueryAndGetCursor(params);
	}
	
	/**
	 * Send the query and return the {@link Cursor}
	 * @param params the {@link Map} of parameters
	 * @return the {@link Cursor} with results
	 */
	private Cursor sendQueryAndGetCursor(Map<String, String> params) {
		InputStream is = NetworkUtils.connectToURL(getContext().getString(R.string.song_history_url), //get connection
				params, 
				getContext().getString(R.string.song_history_request_method), 
				getContext().getString(R.string.song_history_content_type), 
				Integer.parseInt(getContext().getString(R.string.song_history_connect_timeout)), 
				Integer.parseInt(getContext().getString(R.string.song_history_read_timeout)));
		try {
			List<Song> songList = new XMLSongHistoryParser(is).parseXML(); //parse the XML
			return convertSongListToMatrixCursor(songList); //convert and return cursor
		} catch (XMLParserException e) {
			Log.e(TAG, "Error while parsing XML", e);
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Request type for URI : " + uri);
		
		switch (URI_MATCHER.match(uri)) { //return the proper mime type
		case ALL_SONGS_SEARCH:
		case DATE_SEARCH:
		case FULL_TEXT_SEARCH:
			return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + SongHistoryContract.MIME_TYPE;

		case SUGGEST_SEARCH:
			return SearchManager.SUGGEST_MIME_TYPE;
		default:
			 throw new IllegalArgumentException("Unsupported Uri: " + uri);
		}
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * Prepare the params for the HTTP query
	 * @param actionCode the action code as {@link HttpActionCode}
	 * @param query the query as {@link string}
	 * @return the {@link Map} of params
	 */
	private Map<String, String> prepareHttpQueryParams(HttpActionCode actionCode, String query) {
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put(ACTION_PARAM_NAME, actionCode.toString());
		if ((query != null) && (!query.equals("")))
			paramsMap.put(QUERY_PARAM_NAME, query);

		return paramsMap;
	}

	/**
	 * Convert a song list to a {@link Cursor}
	 * @param songList the {@link List} of {@link Song}
	 * @return the filled-in {@link MatrixCursor}
	 */
	private MatrixCursor convertSongListToMatrixCursor(List<Song> songList) {
		MatrixCursor cursor = new MatrixCursor(new String[] {
			SongHistoryContract.Columns._ID,
			SongHistoryContract.Columns.ARTIST,
			SongHistoryContract.Columns.TITLE,
			SongHistoryContract.Columns.DATE,
			SongHistoryContract.Columns.IMAGE_PATH,
			SongHistoryContract.Columns.IMAGE_WIDTH,
			SongHistoryContract.Columns.IMAGE_HEIGHT
		});
		
		int index = 0;
		int maxIndex = Integer.parseInt(getContext().getString(R.string.song_history_max_items)) - 1; //index count is -1 than the total size
		for (Song song : songList) { //add each song into the cursor
			cursor.addRow(new Object[] {
				index,
				song.getArtist(),
				song.getTitle(),
				song.getPlayedDate(),
				(song.getMetadata() != null) ? song.getMetadata().getPath() : null,
				(song.getMetadata() != null) ? song.getMetadata().getWidth() : null,
				(song.getMetadata() != null) ? song.getMetadata().getHeight() : null
			});
			
			if (index >= maxIndex) {
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Cursor size reach max allowed size");
				break;
			} else
				index++;
		}
		
		return cursor;
	}
}
