/**
 *
 */
package com.stationmillenium.android.providers;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.stationmillenium.android.R;
import com.stationmillenium.android.R.string;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO.Song;
import com.stationmillenium.android.libutils.exceptions.XMLParserException;
import com.stationmillenium.android.libutils.network.NetworkUtils;
import com.stationmillenium.android.libutils.xml.XMLSongHistoryParser;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static com.stationmillenium.android.providers.SongHistoryContract.ALL_SONGS_SEARCH;
import static com.stationmillenium.android.providers.SongHistoryContract.DATE_SEARCH_INDEX;
import static com.stationmillenium.android.providers.SongHistoryContract.FULL_TEXT_SEARCH_INDEX;
import static com.stationmillenium.android.providers.SongHistoryContract.SUGGEST_SEARCH;
import static com.stationmillenium.android.providers.SongHistoryContract.SUGGEST_SEARCH_NO_SUGGEST;

/**
 * Content provider for songs history search
 *
 * @author vincent
 */
public class SongHistoryContentProvider extends ContentProvider {

    //static part
    private static final String ACTION_PARAM_NAME = "action";
    private static final String QUERY_PARAM_NAME = "query";
    private static final String SONG_SUGGEST_SEPARATOR = " - ";

    private static final UriMatcher URI_MATCHER = SongHistoryContract.buildURIMatcher();
    //instance vars
    private SimpleDateFormat sdf;

    /**
     * @see android.content.ContentProvider#onCreate()
     */
    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onCreate() {
        Timber.d("Initialization of SongHistoryContentProvider");
        sdf = new SimpleDateFormat(getContext().getString(R.string.song_history_date_format));
        return true;
    }

    /**
     * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Timber.d("Querying data for URI : %s", uri);

        switch (URI_MATCHER.match(uri)) { //return the proper mime type
            case ALL_SONGS_SEARCH: //case of all song search, no param needed
                Map<String, String> params = prepareHttpQueryParams(HttpActionCode.DEFAULT, null);
                return sendQueryAndGetCursor(params);

            case DATE_SEARCH_INDEX: //case of full text query, process query string
                return sendRequestFromURI(uri, HttpActionCode.DATE);

            case FULL_TEXT_SEARCH_INDEX: //case of full text query, process query string
                return sendRequestFromURI(uri, HttpActionCode.FULL_TEXT);

            case SUGGEST_SEARCH: //case of suggest query, process query string as full text with suggest projection
                return sendQueryForSuggestSearch(uri);

            case SUGGEST_SEARCH_NO_SUGGEST: //case of empty suggestion
                Timber.d("Empty suggestions cursor returned");
                return createSuggestCursor();

            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }
    }

    /**
     * Send the query for suggest search
     *
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
            MatrixCursor cursor = createSuggestCursor();

            int index = 0;
            for (Song song : songList) { //add each song into the cursor
                cursor.addRow(new Object[]{
                        index,
                        song.getTitle(),
                        song.getArtist(),
                        song.getArtist() + SONG_SUGGEST_SEPARATOR + song.getTitle(),
                });

                index++;
            }

            return cursor;
        } catch (XMLParserException e) { //if any error occurs
            Timber.e(e, "Error while parsing XML");
            return null;
        }
    }

    /**
     * Create the suggest {@link Cursor}
     *
     * @return the associated {@link MatrixCursor}
     */
    private MatrixCursor createSuggestCursor() {
        return new MatrixCursor(new String[]{ //init the returned cursor
                SongHistoryContract.Columns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_QUERY
        });
    }

    /**
     * Send a query by getting params from the {@link Uri}
     *
     * @param uri    the {@link Uri} to get params
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
     *
     * @param params the {@link Map} of parameters
     * @return the {@link Cursor} with results
     */
    private Cursor sendQueryAndGetCursor(Map<String, String> params) {
        if (AppUtils.isNetworkAvailable(getContext())) {
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
                Timber.e(e, "Error while parsing XML");
                return null;
            }
        } else { //if not network available, cancel request
            Timber.d("Network not available");
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Toast.makeText(getContext(), string.player_network_unavailable, Toast.LENGTH_SHORT).show());
            return null;
        }
    }

    /* (non-Javadoc)
     * @see android.content.ContentProvider#getType(android.net.Uri)
     */
    @Override
    public String getType(@NonNull Uri uri) {
        Timber.d("Request type for URI : %s", uri);

        switch (URI_MATCHER.match(uri)) { //return the proper mime type
            case ALL_SONGS_SEARCH:
            case DATE_SEARCH_INDEX:
            case FULL_TEXT_SEARCH_INDEX:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + SongHistoryContract.MIME_TYPE;

            case SUGGEST_SEARCH:
                return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }
    }

    /**
     * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    /**
     * Prepare the params for the HTTP query
     *
     * @param actionCode the action code as {@link HttpActionCode}
     * @param query      the query as {@link string}
     * @return the {@link Map} of params
     */
    private Map<String, String> prepareHttpQueryParams(HttpActionCode actionCode, String query) {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put(ACTION_PARAM_NAME, actionCode.toString());
        if ((query != null) && (!query.equals("")))
            paramsMap.put(QUERY_PARAM_NAME, query);

        return paramsMap;
    }

    /**
     * Convert a song list to a {@link Cursor}
     *
     * @param songList the {@link List} of {@link Song}
     * @return the filled-in {@link MatrixCursor}
     */
    private MatrixCursor convertSongListToMatrixCursor(List<Song> songList) {
        MatrixCursor cursor = new MatrixCursor(new String[]{
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
            cursor.addRow(new Object[]{
                    index,
                    song.getArtist(),
                    song.getTitle(),
                    sdf.format(song.getPlayedDate()),
                    (song.getMetadata() != null) ? song.getMetadata().getPath() : null,
                    (song.getMetadata() != null) ? song.getMetadata().getWidth() : null,
                    (song.getMetadata() != null) ? song.getMetadata().getHeight() : null
            });

            if (index >= maxIndex) {
                Timber.d("Cursor size reach max allowed size");
                break;
            } else
                index++;
        }

        return cursor;
    }

    /**
     * List of available HTTP action code for server query
     *
     * @author vincent
     */
    private enum HttpActionCode {
        DEFAULT,
        FULL_TEXT,
        DATE,
        SUGGEST
    }

}
