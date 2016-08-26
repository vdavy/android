package com.stationmillenium.android.replay.utils;

import android.content.Context;

import com.stationmillenium.android.replay.R;

/**
 * Create the correct URL
 * Created by vincent on 28/08/16.
 */
public class URLManager {

    public static String getTracksURL(Context context) {
        return context.getString(R.string.soundcloud_tracklist_URL, context.getString(R.string.soundcloud_user_id), context.getString(R.string.soudncloud_client_id));
    }

    public static String getPlaylistsURL(Context context) {
        return context.getString(R.string.soundcloud_playlists_URL, context.getString(R.string.soundcloud_user_id), context.getString(R.string.soudncloud_client_id));
    }

    public static String getStreamURL(Context context, String trackID) {
        return context.getString(R.string.soundcloud_stream_URL, trackID, context.getString(R.string.soudncloud_client_id));
    }
}
