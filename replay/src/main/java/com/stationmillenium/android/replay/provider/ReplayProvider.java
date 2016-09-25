package com.stationmillenium.android.replay.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.stationmillenium.android.replay.BuildConfig;
import com.stationmillenium.android.replay.dto.TrackDTO;
import com.stationmillenium.android.replay.utils.SoundcloudRestClient;

import java.util.List;

import static com.stationmillenium.android.replay.provider.ReplayContract.ALL_REPLAY;
import static com.stationmillenium.android.replay.provider.ReplayContract.URI_MATCHER;

/**
 * Content provider for replay
 * Created by vincent on 29/08/16.
 */
public class ReplayProvider extends ContentProvider {

    private static final String TAG = "ReplayProvider";
    public static final String[] COLUMN_NAMES = new String[]{ReplayContract.Columns._ID,
            ReplayContract.Columns.DURATION,
            ReplayContract.Columns.TITLE,
            ReplayContract.Columns.DESCRIPTION,
            ReplayContract.Columns.LAST_MODIFIED,
            ReplayContract.Columns.TAG_LIST,
            ReplayContract.Columns.GENRE,
            ReplayContract.Columns.STREAM_URL,
            ReplayContract.Columns.WAVEFORM_URL,
            ReplayContract.Columns.ARTWORK_URL};

    private SoundcloudRestClient soundcloudRestClient;

    @Override
    public boolean onCreate() {
        soundcloudRestClient = new SoundcloudRestClient(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
         if (BuildConfig.DEBUG) {
             Log.d(TAG, "Querying replay for URI : " + uri);
         }

        switch (URI_MATCHER.match(uri)) {
            case ALL_REPLAY:
                List<TrackDTO> trackDTOs = soundcloudRestClient.getTracksList();
                return trackDTOToCursor(trackDTOs);

            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }
    }

    private Cursor trackDTOToCursor(List<TrackDTO> trackDTOs) {
        MatrixCursor cursor = new MatrixCursor(COLUMN_NAMES);
        for (TrackDTO track : trackDTOs) {
            cursor.addRow(new Object[] {
                    track.getId(),
                    track.getDuration(),
                    track.getTitle(),
                    track.getDescription(),
                    track.getLastModified(),
                    track.getTagList(),
                    track.getGenre(),
                    track.getStreamURL(),
                    track.getWaveformURL(),
                    track.getArtworkURL()
            });
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Request type for URI : " + uri);
        }

        switch (URI_MATCHER.match(uri)) { //return the proper mime type
            case ALL_REPLAY:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + ReplayContract.MIME_TYPE;

            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

}
