package com.stationmillenium.android.replay.utils;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.dto.TrackDTO;

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
    public static String getGenreTracksURL(@NonNull Context context, String genre) {
        return context.getString(R.string.soundcloud_genre_tracklist_URL, context.getString(R.string.soudncloud_client_id), genre);
    }

    @NonNull
    public static String getSearchTracksURL(@NonNull Context context, String search) {
        return context.getString(R.string.soundcloud_search_tracklist_URL, context.getString(R.string.soudncloud_client_id), search);
    }

    @NonNull
    public static String getTagTracksURL(@NonNull Context context, String tag) {
        return context.getString(R.string.soundcloud_tag_tracklist_URL, context.getString(R.string.soudncloud_client_id), tag);
    }

    @NonNull
    public static String addLimitClause(@NonNull Context context, @NonNull String baseURL, int limitSize) {
         return Uri.parse(baseURL).buildUpon().appendQueryParameter(context.getString(R.string.limit_param), String.valueOf(limitSize)).build().toString();
    }

    @NonNull
    public static String getStreamURLFromTrack(@NonNull Context context, @NonNull TrackDTO replay) {
        return Uri.parse(replay.getStreamURL()).buildUpon().appendQueryParameter(context.getString(R.string.client_id_param), context.getString(R.string.soudncloud_client_id)).build().toString();
    }
}
