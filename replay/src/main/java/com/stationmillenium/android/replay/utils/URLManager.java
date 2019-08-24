package com.stationmillenium.android.replay.utils;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.stationmillenium.android.replay.R;

/**
 * Create the correct URL
 * Created by vincent on 28/08/16.
 */
public class URLManager {

    @NonNull
    public static String addPageNumber(@NonNull Context context, @NonNull String baseURL, int totalSize) {
        int pageNumberToSend = context.getResources().getInteger(R.integer.default_page);
        if (totalSize > context.getResources().getInteger(R.integer.default_page_size)) {
            pageNumberToSend = totalSize / context.getResources().getInteger(R.integer.default_page_size);
        }
        return Uri.parse(baseURL).buildUpon()
                .appendQueryParameter(context.getString(R.string.page_number_param), String.valueOf(pageNumberToSend))
                .appendQueryParameter(context.getString(R.string.per_page_param), String.valueOf(context.getResources().getInteger(R.integer.default_page_size)))
                .build().toString();
    }


    @NonNull
    public static String getPlaylistsURL(@NonNull Context context) {
        return context.getString(R.string.replay_playlists_URL);
    }

    @NonNull
    public static String getTracksURL(@NonNull Context context, int playlistID) {
        return Uri.parse(context.getString(R.string.replay_tracks_URL))
                .buildUpon()
                .appendQueryParameter(context.getString(R.string.serie_id_param), String.valueOf(playlistID))
                .build().toString();
    }

    @NonNull
    public static String getTracksURLWithSearch(@NonNull Context context, int playlistID, String query) {
        return Uri.parse(context.getString(R.string.replay_tracks_URL))
                .buildUpon()
                .appendQueryParameter(context.getString(R.string.serie_id_param), String.valueOf(playlistID))
                .appendQueryParameter(context.getString(R.string.search_param), query)
                .build().toString();
    }

    @NonNull
    public static String getPlaylistsSearchURL(@NonNull Context context, String query) {
        return Uri.parse(context.getString(R.string.replay_playlists_URL))
                .buildUpon()
                .appendQueryParameter(context.getString(R.string.search_param), query)
                .build().toString();
    }
}
