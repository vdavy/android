package com.stationmillenium.android.providers;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract for the {@link SongHistoryContentProvider}
 *
 * @author vincent
 */
public final class SongHistoryContract {
    public static final String AUTHORITY = "com.stationmillenium.android.contentproviders.SongHistoryContentProvider";
    public static final Uri CONTENT_URI = buildContentURI();
    public static final Uri ROOT_URI = buildRootURI();
    public static final String DATE_SEARCH_FORMAT = "yyyyMMdd-HHmm";
    public static final String DEFAULT_MATCH = "songHistory";
    public static final String DATE_SEARCH_SEGMENT = DEFAULT_MATCH + "Date";
    public static final String FULL_TEXT_SEARCH = DEFAULT_MATCH + "/*";
    public static final String DATE_SEARCH = DEFAULT_MATCH + "Date/*";
    public static final String MIME_TYPE = "vnd." + AUTHORITY + "." + DEFAULT_MATCH;

    //list of uri matcher states
    public static final int ALL_SONGS_SEARCH = 0;
    public static final int FULL_TEXT_SEARCH_INDEX = 1;
    public static final int DATE_SEARCH_INDEX = 2;
    public static final int SUGGEST_SEARCH = 3;
    public static final int SUGGEST_SEARCH_NO_SUGGEST = 4;

    /**
     * Init the content {@link Uri}
     *
     * @return the {@link Uri}
     */
    private static Uri buildContentURI() {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY)
                .appendPath(DEFAULT_MATCH);
        return uriBuilder.build();
    }

    /**
     * Init the root {@link Uri} with no specified segment
     *
     * @return the {@link Uri}
     */
    private static Uri buildRootURI() {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY);
        return uriBuilder.build();
    }

    /**
     * Build the {@link UriMatcher} for {@link SongHistoryContentProvider}
     *
     * @return the {@link UriMatcher}
     */
    public static UriMatcher buildURIMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, DEFAULT_MATCH, ALL_SONGS_SEARCH);
        matcher.addURI(AUTHORITY, FULL_TEXT_SEARCH, FULL_TEXT_SEARCH_INDEX);
        matcher.addURI(AUTHORITY, DATE_SEARCH, DATE_SEARCH_INDEX);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SUGGEST_SEARCH);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SUGGEST_SEARCH);
        return matcher;
    }

    /**
     * List of available columns in the returned {@link Cursor}
     *
     * @author vincent
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
