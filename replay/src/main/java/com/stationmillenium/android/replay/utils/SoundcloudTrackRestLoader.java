package com.stationmillenium.android.replay.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.stationmillenium.android.replay.dto.PlaylistDTO;
import com.stationmillenium.android.replay.dto.TrackDTO;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.EMPTY_LIST;

/**
 * Class to query Soundcloud track info
 * Created by vincent on 28/08/16.
 */
public class SoundcloudTrackRestLoader extends AsyncTaskLoader<List<? extends Serializable>> {

    public enum QueryType {
        SEARCH,
        GENRE,
        TAG
    }

    private static final String TAG = "SCTrackRestLoader";
    private int limit;
    private String query;
    private QueryType queryType;
    private PlaylistDTO playlistDTO;

    /**
     * Simple search (no query / no query / no limit)
     * @param context context
     */
    public SoundcloudTrackRestLoader(@NonNull Context context) {
        super(context);
    }

    /**
     * Simple search (no query / no query) with limit
     * @param context context
     * @param limit max replay count to search
     */
    public SoundcloudTrackRestLoader(@NonNull Context context, int limit) {
        this(context);
        this.limit = limit;
        Log.d(TAG, "Init REST loader with limit param : " + limit);
    }

    /**
     * Simple search for playlist - no query make
     * @param context context
     * @param playlistDTO the playlist to extract titles
     */
    public SoundcloudTrackRestLoader(@NonNull Context context, @NonNull PlaylistDTO playlistDTO) {
        super(context);
        this.playlistDTO = playlistDTO;
    }

    /**
     * Simple search for playlist with limit - query make for extra load
     * @param context context
     * @param playlistDTO the playlist to extract titles
     * @param limit the limit of tracks to load
     */
    public SoundcloudTrackRestLoader(@NonNull Context context, @NonNull PlaylistDTO playlistDTO, int limit) {
        this(context, playlistDTO);
        this.limit = limit;
    }

    /**
     * Search with param - no limit
     * @param context context
     * @param query the search query
     * @param queryType the type of query
     */
    public SoundcloudTrackRestLoader(@NonNull Context context, @NonNull QueryType queryType, @NonNull String query) {
        this(context);
        // clean up wrong chars : http://glaforge.appspot.com/article/how-to-remove-accents-from-a-string
        this.query = Normalizer.normalize(query, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        this.queryType = queryType;
        Log.d(TAG, "Init REST loader with query search : " + this.query + " - and query type : " + this.queryType);
    }

    /**
     * Search with param and limit
     * @param context context
     * @param query the search query
     * @param queryType the type of query
     * @param limit max replay count to search
     */
    public SoundcloudTrackRestLoader(@NonNull Context context, @NonNull QueryType queryType, @NonNull String query, int limit) {
        this(context, queryType, query);
        this.limit= limit;
        Log.d(TAG, "Search limit is : " + this.limit);
    }

    /**
     * Get the track list
     * @return the track list, empty list if error or no data
     */
    @Override
    public List<TrackDTO> loadInBackground() {
        if (playlistDTO != null && limit == 0) {
            Log.d(TAG, "Read tracks form playlist : " + playlistDTO.getTracks().size());
            TrackListDateSorter.sortTrackListByDescDate(playlistDTO.getTracks());
            return playlistDTO.getTracks();
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            if (!isLoadInBackgroundCanceled()) {
                String url = getGoodURL();
                Log.v(TAG, "Query tracks list at URL " + url);
                TrackDTO[] trackDTOs = restTemplate.getForObject(url, TrackDTO[].class);
                Log.d(TAG, "Got tracks list : " + trackDTOs.length);
                return Arrays.asList(trackDTOs);
            } else {
                Log.d(TAG, "Load in background cancelled");
                return EMPTY_LIST;
            }

        } catch (Exception e) {
            Log.w(TAG, "Error with tracks list", e);
            return EMPTY_LIST;
        }
    }

    /**
     * Get the good request URL according to search type
     * @return the good URL to query, with clause limit
     */
    private String getGoodURL() {
        String url = null;
        if (playlistDTO != null) {
            url = URLManager.getPlaylistTracksURL(getContext(), playlistDTO.getId());
        } else if (queryType != null) {
            switch (queryType) {
                case GENRE:
                    url = URLManager.getGenreTracksURL(getContext(), query);
                    break;

                case SEARCH:
                    url = URLManager.getSearchTracksURL(getContext(), query);
                    break;

                case TAG:
                    url = URLManager.getTagTracksURL(getContext(), query);
                    break;
            }
        } else {
            url = URLManager.getTracksURL(getContext());
        }
        return processLimitClause(url);
    }
    /**
     * Add the limit clause if needed
     * @param url the URL to add limit clause
     * @return URL with limit clause, if requested
     */
    private String processLimitClause(String url) {
        return (limit > 0 ) ? URLManager.addLimitClause(getContext(), url, limit) : url;
    }

}
