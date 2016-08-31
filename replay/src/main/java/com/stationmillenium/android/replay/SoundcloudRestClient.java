package com.stationmillenium.android.replay;

import android.content.Context;
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
public class SoundcloudRestClient {

    private static final String TAG = "SoundcloudRestClient";

    private Context context;

    /**
     *
     * @param context need context for resources
     */
    public SoundcloudRestClient(Context context) {
        this.context = context;
    }

    /**
     * Get the track list
     * @return the track list, empty list if error or no data
     */
    public List<TrackDTO> getTracksList() {
        try {
            Log.v(TAG, "Get tracks list");
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            TrackDTO[] trackDTOs = restTemplate.getForObject(URLManager.getTracksURL(context), TrackDTO[].class);
            Log.d(TAG, "Got tracks list : " + trackDTOs.length);
            return Arrays.asList(trackDTOs);

        } catch (Exception e) {
            Log.w(TAG, "Error with tracks list", e);
            return EMPTY_LIST;
        }
    }
}
