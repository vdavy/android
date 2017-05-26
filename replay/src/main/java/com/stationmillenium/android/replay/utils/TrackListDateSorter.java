package com.stationmillenium.android.replay.utils;

import com.stationmillenium.android.replay.dto.TrackDTO;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Sort track in list by date desc
 * Created by vincent on 26/05/17.
 */

public class TrackListDateSorter {

    public static void sortTrackListByDescDate(List<TrackDTO> tracksList) {
        Collections.sort(tracksList, new Comparator<TrackDTO>() {
            @Override
            public int compare(TrackDTO o1, TrackDTO o2) {
                if (o1 == null || o1.getLastModified() == null) {
                    return 1;
                } else if (o2 == null || o2.getLastModified() == null) {
                    return -1;
                } else {
                    return o1.getLastModified().compareTo(o2.getLastModified()) * -1;
                }
            }
        });
    }
}
