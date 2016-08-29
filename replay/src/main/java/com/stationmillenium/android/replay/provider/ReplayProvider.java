package com.stationmillenium.android.replay.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.stationmillenium.android.replay.BuildConfig;

import static com.stationmillenium.android.replay.provider.ReplayContract.ALL_REPLAY;
import static com.stationmillenium.android.replay.provider.ReplayContract.URI_MATCHER;

/**
 * Content provider for replay
 * Created by vincent on 29/08/16.
 */
public class ReplayProvider extends ContentProvider {

    private static final String TAG = "ReplayProvider";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
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
