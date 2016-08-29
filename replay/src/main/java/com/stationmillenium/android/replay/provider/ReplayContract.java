package com.stationmillenium.android.replay.provider;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract for the {@link ReplayProvider}
 *
 * @author vincent
 */
public final class ReplayContract {

    public static final String AUTHORITY = "com.stationmillenium.android.replay.provider.ReplayProvider";
    public static final UriMatcher URI_MATCHER = buildURIMatcher();
    public static final Uri ROOT_URI = buildRootURI();
    public static final String DEFAULT_MATCH = "replay";
    public static final String MIME_TYPE = "vnd." + AUTHORITY + "." + DEFAULT_MATCH;

    //list of uri matcher states
    public static final int ALL_REPLAY = 0;

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
     * Build the {@link UriMatcher} for {@link ReplayProvider}
     *
     * @return the {@link UriMatcher}
     */
    public static UriMatcher buildURIMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, DEFAULT_MATCH, ALL_REPLAY);
        return matcher;
    }

    /**
     * List of available columns in the returned {@link Cursor}
     *
     * @author vincent
     */
    public interface Columns extends BaseColumns {
        String DURATION = "duration";
        String TITLE = "title";
        String DESCRIPTION = "description";
        String LAST_MODIFIED = "lastModified";
        String TAG_LIST = "tagList";
        String GENRE = "genre";
        String STREAM_URL = "streamURL";
        String WAVEFORM_URL = "waveformURL";
        String ARTWORK_URL = "artworkURL";
    }
}
