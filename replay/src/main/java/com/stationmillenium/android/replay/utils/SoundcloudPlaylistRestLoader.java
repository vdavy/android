package com.stationmillenium.android.replay.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.stationmillenium.android.replay.dto.PlaylistDTO;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.EMPTY_LIST;

/**
 * Class to query Soundcloud playlist info
 * Created by vincent on 28/08/16.
 */
public class SoundcloudPlaylistRestLoader extends AsyncTaskLoader<List<? extends Serializable>> {

    private static final String TAG = "SCPlaylistRestLoader";
    private int limit;

    /**
     * Simple search (no query / no query / no limit)
     * @param context context
     */
    public SoundcloudPlaylistRestLoader(@NonNull Context context) {
        super(context);
    }

    /**
     * Simple search (no query / no query) with limit
     * @param context context
     * @param limit max replay count to search
     */
    public SoundcloudPlaylistRestLoader(@NonNull Context context, int limit) {
        this(context);
        this.limit = limit;
        Log.d(TAG, "Init REST loader with limit param : " + limit);
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
                String url = processLimitClause(URLManager.getPlaylistsURL(getContext()));
                Log.v(TAG, "Query tracks list at URL " + url);
                PlaylistDTO[] playlistDTOs = restTemplate.getForObject(url, PlaylistDTO[].class);
                Log.d(TAG, "Got playlist list : " + playlistDTOs.length);
                return Arrays.asList(playlistDTOs);
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
     * @param url the URL to add limit clause
     * @return URL with limit clause, if requested
     */
    private String processLimitClause(String url) {
        return (limit > 0 ) ? URLManager.addLimitClause(getContext(), url, limit) : url;
    }

}
