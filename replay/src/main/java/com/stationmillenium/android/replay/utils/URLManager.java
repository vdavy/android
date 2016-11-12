package com.stationmillenium.android.replay.utils;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.stationmillenium.android.replay.R;

/**
 * Create the correct URL
 * Created by vincent on 28/08/16.
 */
public class URLManager {

    @NonNull
    public static String getTracksURL(@NonNull Context context) {
        return context.getString(R.string.soundcloud_tracklist_URL, context.getString(R.string.soundcloud_user_id), context.getString(R.string.soudncloud_client_id));
    }

    @NonNull
    public static String getPlaylistsURL(@NonNull Context context) {
        return context.getString(R.string.soundcloud_playlists_URL, context.getString(R.string.soundcloud_user_id), context.getString(R.string.soudncloud_client_id));
    }

    @NonNull
    public static String getStreamURL(@NonNull Context context, @NonNull String trackID) {
        return context.getString(R.string.soundcloud_stream_URL, trackID, context.getString(R.string.soudncloud_client_id));
    }

    public static String addLimitClause(@NonNull Context context, @NonNull String baseURL, int limitSize) {
         return Uri.parse(baseURL).buildUpon().appendQueryParameter(context.getString(R.string.limit_param), String.valueOf(limitSize)).build().toString();
    }
}
