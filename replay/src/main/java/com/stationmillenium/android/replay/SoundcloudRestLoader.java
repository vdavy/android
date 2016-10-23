package com.stationmillenium.android.replay;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.stationmillenium.android.replay.dto.TrackDTO;
import com.stationmillenium.android.replay.utils.URLManager;

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

    private static final String TAG = "SoundcloudRestLoader";

    public SoundcloudRestLoader(Context context) {
        super(context);
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
                TrackDTO[] trackDTOs = restTemplate.getForObject(URLManager.getTracksURL(getContext()), TrackDTO[].class);
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

}
