/**
 *
 */
package com.stationmillenium.android.providers;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.stationmillenium.android.R;
import com.stationmillenium.android.R.string;
import com.stationmillenium.android.dtos.CurrentTrack;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.DateTime;
import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO.Song;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

import static com.stationmillenium.android.providers.SongHistoryContract.ALL_SONGS_SEARCH;
import static com.stationmillenium.android.providers.SongHistoryContract.DATE_SEARCH_INDEX;

/**
 * Content provider for songs history search
 *
 * @author vincent
 */
public class SongHistoryContentProvider extends ContentProvider {

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
                return sendQueryAndGetCursor(Objects.requireNonNull(getContext()).getString(R.string.song_history_url));

            case DATE_SEARCH_INDEX: //case of full text query, process query string
                return sendQueryAndGetCursor(Objects.requireNonNull(getContext()).getString(R.string.song_history_url) + "/" + uri.getLastPathSegment());

            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }
    }

    /**
     * Send the query and return the {@link Cursor}
     *
     * @param url the URL to query
     * @return the {@link Cursor} with results
     */
    private Cursor sendQueryAndGetCursor(String url) {
        if (AppUtils.isNetworkAvailable(getContext())) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                CurrentTrack[] currentTrack = restTemplate.getForObject(url, CurrentTrack[].class);

                return convertSongListToMatrixCursor(currentTrack); //convert and return cursor
            } catch (Exception e) {
                Timber.e(e, "Error getting history tracks");
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
        return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + SongHistoryContract.MIME_TYPE;
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
     * Convert a song list to a {@link Cursor}
     *
     * @param tracks the {@link List} of {@link Song}
     * @return the filled-in {@link MatrixCursor}
     */
    private MatrixCursor convertSongListToMatrixCursor(CurrentTrack[] tracks) {
        MatrixCursor cursor = new MatrixCursor(new String[]{
                SongHistoryContract.Columns._ID,
                SongHistoryContract.Columns.ARTIST,
                SongHistoryContract.Columns.TITLE,
                SongHistoryContract.Columns.DATE,
                SongHistoryContract.Columns.IMAGE_PATH,
        });

        int index = 0;
        for (CurrentTrack track : tracks) {
            if (track != null && track.isTrack()) {
                String imageURL = null;
                if (track.isImage()) {
                    imageURL = getContext().getString(R.string.player_image_url_root) + track.getTime();
                }
                cursor.addRow(new Object[]{
                        index,
                        track.getArtist(),
                        track.getTitle(),
                        sdf.format(new Date(DateTime.parseRfc3339(track.getTime()).getValue())),
                        imageURL,
                });
                index++;
            }
        }
        return cursor;
    }
}