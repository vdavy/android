package com.stationmillenium.android.replay.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.stationmillenium.android.replay.dto.TrackDTO;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.EMPTY_LIST;

/**
 * Class to query Soundcloud info
 * Created by vincent on 28/08/16.
 */
public class SoundcloudRestLoader extends AsyncTaskLoader<List<TrackDTO>> {

    public enum QueryType {
        SEARCH,
        GENRE
    }

    private static final String TAG = "SoundcloudRestLoader";
    private int limit;
    private String query;
    private QueryType queryType;

    /**
     * Simple search (no query / no query / no limit)
     * @param context context
     */
    public SoundcloudRestLoader(@NonNull Context context) {
        super(context);
    }

    /**
     * Simple search (no query / no query) with limit
     * @param context context
     * @param limit max replay count to search
     */
    public SoundcloudRestLoader(@NonNull Context context, int limit) {
        this(context);
        this.limit = limit;
        Log.d(TAG, "Init REST loader with limit param : " + limit);
    }

    /**
     * Search with param - no limit
     * @param context context
     * @param query the search query
     * @param queryType the type of query
     */
    public SoundcloudRestLoader(@NonNull Context context, @NonNull QueryType queryType, @NonNull String query) {
        this(context);
        this.query = query;
        this.queryType = queryType;
        Log.d(TAG, "Init REST loader with query search : " + this.query + " - and query type : " + queryType);
    }

    /**
     * Search with param and limit
     * @param context context
     * @param query the search query
     * @param queryType the type of query
     * @param limit max replay count to search
     */
    public SoundcloudRestLoader(@NonNull Context context, @NonNull QueryType queryType, @NonNull String query, int limit) {
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
        try {
            Log.v(TAG, "Get tracks list");
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            if (!isLoadInBackgroundCanceled()) {
                TrackDTO[] trackDTOs = restTemplate.getForObject(processLimitClause(), TrackDTO[].class);
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
     * Add the limit clause if needed
     * @return URL with limit clause, if requested
     */
    private String processLimitClause() {
        return (limit > 0 ) ? URLManager.addLimitClause(getContext(), URLManager.getTracksURL(getContext()), limit) : URLManager.getTracksURL(getContext());
    }

}
