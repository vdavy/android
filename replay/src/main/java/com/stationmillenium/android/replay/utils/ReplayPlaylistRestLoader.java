package com.stationmillenium.android.replay.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;
import android.text.Html;

import com.stationmillenium.android.replay.dto.PlaylistDTO;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

import static java.util.Collections.EMPTY_LIST;

/**
 * Class to query Soundcloud playlist info
 * Created by vincent on 28/08/16.
 */
public class ReplayPlaylistRestLoader extends AsyncTaskLoader<List<? extends Serializable>> {

    private int limit;
    private List<PlaylistDTO> alreadyLoadedPlaylists;
    private String query;

    /**
     * Simple playlist load
     * @param context context
     */
    public ReplayPlaylistRestLoader(@NonNull Context context) {
        super(context);
    }

    /**
     * Playlist search
     * @param context context
     * @param query the query to search playlist
     */
    public ReplayPlaylistRestLoader(@NonNull Context context, String query) {
        super(context);
        this.query = query;
        Timber.d("Init playlist loader with search param : %s", query);
    }

    /**
     * Playlist search with paging
     * @param context context
     * @param query the query to search playlist
     * @param limit max items to display
     * @param alreadyLoadedPlaylists playlists previously loaded
     */
    public ReplayPlaylistRestLoader(@NonNull Context context, String query, int limit, List<PlaylistDTO> alreadyLoadedPlaylists) {
        this(context);
        this.query = query;
        this.limit = limit;
        this.alreadyLoadedPlaylists = alreadyLoadedPlaylists;
        Timber.d("Init playlist loader with search %s and with limit param : %s", query, limit);
    }

    /**
     * Simple playlist with paging
     * @param context context
     * @param limit max items to display
     * @param alreadyLoadedPlaylists playlists previously loaded
     */
    public ReplayPlaylistRestLoader(@NonNull Context context, int limit, List<PlaylistDTO> alreadyLoadedPlaylists) {
        this(context);
        this.limit = limit;
        this.alreadyLoadedPlaylists = alreadyLoadedPlaylists;
        Timber.d("Init playlist loader with limit param : %s", limit);
    }

    /**
     * Get the track list
     * @return the track list, empty list if error or no data
     */
    @Override
    public List<PlaylistDTO> loadInBackground() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            if (!isLoadInBackgroundCanceled()) {
                String url = processLimitClause(getURL());
                Timber.v("Query playlist list at URL %s", url);
                PlaylistDTO[] playlistDTOs = restTemplate.getForObject(url, PlaylistDTO[].class);
                Timber.d("Got playlist list : %s", playlistDTOs.length);
                unescapeStringFields(playlistDTOs);
                if (alreadyLoadedPlaylists != null && alreadyLoadedPlaylists.size() > 0) {
                    alreadyLoadedPlaylists.addAll(Arrays.asList(playlistDTOs));
                    return alreadyLoadedPlaylists;
                }
                return new ArrayList<>(Arrays.asList(playlistDTOs));
            } else {
                Timber.d("Load in background cancelled");
                return EMPTY_LIST;
            }

        } catch (Exception e) {
            Timber.w(e, "Error with playlist list");
            return EMPTY_LIST;
        }
    }

    private void unescapeStringFields(PlaylistDTO[] playlistDTOs) {
        for (PlaylistDTO playlistDTO : playlistDTOs) {
            if (playlistDTO.getTitle() != null) {
                playlistDTO.setTitle(Html.fromHtml(playlistDTO.getTitle()).toString());
            }
            if (playlistDTO.getImageURL() != null) {
                playlistDTO.setImageURL(Html.fromHtml(playlistDTO.getImageURL()).toString());
            }
        }
    }

    private String getURL() {
        return  (query != null && query.length() > 0)
                ? URLManager.getPlaylistsSearchURL(getContext(), query)
                : URLManager.getPlaylistsURL(getContext());
    }

    /**
     * Add the page number
     * @param url the URL to add page number clause
     * @return URL with page number
     */
    private String processLimitClause(String url) {
        return URLManager.addPageNumber(getContext(), url, limit);
    }

}
