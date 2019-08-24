package com.stationmillenium.android.replay.utils;

import android.content.Context;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.loader.content.AsyncTaskLoader;

import com.stationmillenium.android.replay.dto.TrackDTO;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

import static java.util.Collections.EMPTY_LIST;

/**
 * Class to query Soundcloud track info
 * Created by vincent on 28/08/16.
 */
public class ReplayTrackRestLoader extends AsyncTaskLoader<List<? extends Serializable>> {

    private int limit;
    private List<TrackDTO> alreadyLoadedTracks;
    private String query;
    private int playlistID;

    /**
     * Empty list returned, no data loaded
     * @param context the context
     */
    public ReplayTrackRestLoader(@NonNull Context context) {
        super(context);
    }

    /**
     * Simple tracks load, no extra data or search
     * @param context the context
     * @param playlistID the playlist ID to load
     */
    public ReplayTrackRestLoader(@NonNull Context context, int playlistID) {
        super(context);
        this.playlistID = playlistID;
        Timber.d("Init track loader for playlist ID : %s", playlistID);
    }

    /**
     * Simple tracks load with extra data
     * @param context the context
     * @param playlistID the playlist ID to load
     * @param limit the limit of tracks to load
     * @param alreadyLoadedTracks the already loaded tracks for completion
     */
    public ReplayTrackRestLoader(@NonNull Context context, int playlistID, int limit, List<TrackDTO> alreadyLoadedTracks) {
        super(context);
        this.playlistID = playlistID;
        this.limit = limit;
        this.alreadyLoadedTracks = alreadyLoadedTracks;
        Timber.d("Init track loader for playlist ID %s and limit : %s", playlistID, limit);
    }

    /**
     * Tracks load with search, no extra data
     * @param context the context
     * @param playlistID the playlist ID to load
     * @param query the query to filter tracks
     */
    public ReplayTrackRestLoader(@NonNull Context context, int playlistID, String query) {
        super(context);
        this.playlistID = playlistID;
        this.query = query;
        Timber.d("Init track loader for playlist ID %s and search : %s", playlistID, query);
    }

    /**
     * Tracks load with search and extra data
     * @param context the context
     * @param playlistID the playlist ID to load
     * @param query the query to filter tracks
     * @param limit the limit of tracks to load
     * @param alreadyLoadedTracks the already loaded tracks for completion
     */
    public ReplayTrackRestLoader(@NonNull Context context, int playlistID, String query, int limit, List<TrackDTO> alreadyLoadedTracks) {
        super(context);
        this.playlistID = playlistID;
        this.query = query;
        this.limit = limit;
        this.alreadyLoadedTracks = alreadyLoadedTracks;
        Timber.d("Init track loader for playlist ID %s, limit %s and search : %s", playlistID, limit, query);
    }

    /**
     * Get the track list
     * @return the track list, empty list if error or no data
     */
    @Override
    public List<TrackDTO> loadInBackground() {
        if (playlistID != 0 ) {
            try {
                String url = getGoodURL();
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                if (!isLoadInBackgroundCanceled()) {
                    Timber.v("Query tracks list at URL %s", url);
                    TrackDTO[] trackDTOs = restTemplate.getForObject(url, TrackDTO[].class);
                    Timber.d("Got tracks list : %s", trackDTOs.length);
                    unescapeStringFields(trackDTOs);
                    if (alreadyLoadedTracks != null && alreadyLoadedTracks.size() > 0) {
                        alreadyLoadedTracks.addAll(Arrays.asList(trackDTOs));
                        return alreadyLoadedTracks;
                    }
                    return new ArrayList<>(Arrays.asList(trackDTOs));
                } else {
                    Timber.d("Load in background cancelled");
                }
            } catch (Exception e) {
                Timber.w(e, "Error with tracks list");
            }
        }
        return EMPTY_LIST;
    }

    private void unescapeStringFields(TrackDTO[] trackDTOs) {
        for (TrackDTO trackDTO : trackDTOs) {
            if (trackDTO.getTitle() != null) {
                trackDTO.setTitle(Html.fromHtml(trackDTO.getTitle()).toString());
            }
            if (trackDTO.getFileURL() != null) {
                trackDTO.setFileURL(Html.fromHtml(trackDTO.getFileURL()).toString());
            }
            if (trackDTO.getImageURL() != null) {
                trackDTO.setImageURL(Html.fromHtml(trackDTO.getImageURL()).toString());
            }
        }
    }

    /**
     * Get the good request URL according to search type
     * @return the good URL to query, with clause limit
     */
    private String getGoodURL() {
        String url = query == null || query.length() == 0
                ? URLManager.getTracksURL(getContext(), playlistID)
                : URLManager.getTracksURLWithSearch(getContext(), playlistID, query);
        return URLManager.addPageNumber(getContext(), url, limit);
    }

}
