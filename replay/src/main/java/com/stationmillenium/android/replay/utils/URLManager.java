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
    public static String getStreamURLFromTrack(@NonNull Context context, @NonNull TrackDTO replay) {
        return Uri.parse(replay.getStreamURL()).buildUpon().appendQueryParameter(context.getString(R.string.client_id_param), context.getString(R.string.soudncloud_client_id)).build().toString();
    }

    @NonNull
    public static String getPlaylistsURL(@NonNull Context context) {
        return context.getString(R.string.replay_playlists_URL);
    }

    @NonNull
    public static String getPlaylistsSearchURL(@NonNull Context context, String query) {
        return Uri.parse(context.getString(R.string.replay_playlists_URL))
                .buildUpon()
                .appendQueryParameter(context.getString(R.string.search_param), query)
                .build().toString();
    }

    @NonNull
    public static String getPlaylistTracksURL(@NonNull Context context, int playlistId) {
        return context.getString(R.string.soundcloud_playlist_tracks_URL, String.valueOf(playlistId), context.getString(R.string.soudncloud_client_id));
    }
}
