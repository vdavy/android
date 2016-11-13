package com.stationmillenium.android.replay.test.utils;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import com.stationmillenium.android.replay.dto.TrackDTO;
import com.stationmillenium.android.replay.utils.SoundcloudRestLoader;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Test {@link SoundcloudRestLoader}
 * Created by vincent on 28/08/16.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class SoundcloudRestLoaderTest {

    @Test
    public void testTracksList() {
        List<TrackDTO> trackDTOs = new SoundcloudRestLoader(InstrumentationRegistry.getTargetContext()).loadInBackground();
        assertNotNull(trackDTOs);
        assertEquals(50, trackDTOs.size());
        assertReplayList(trackDTOs);
    }

    @Test
    public void testTracksListWithLimit() {
        List<TrackDTO> trackDTOs = new SoundcloudRestLoader(InstrumentationRegistry.getTargetContext(), 10).loadInBackground();
        assertNotNull(trackDTOs);
        assertEquals(10, trackDTOs.size());
        assertReplayList(trackDTOs);
    }

    private void assertReplayList(List<TrackDTO> trackDTOs) {
        for (TrackDTO trackDTO : trackDTOs) {
            assertNotNull(trackDTO);
            assertTrue(trackDTO.getId() > 0);
            assertTrue(trackDTO.getDuration() > 0);
            assertNotNull(trackDTO.getTitle());
            assertNotNull(trackDTO.getDescription());
            assertNotNull(trackDTO.getLastModified());
            assertNotNull(trackDTO.getTagList());
            assertNotNull(trackDTO.getGenre());
            assertNotNull(trackDTO.getStreamURL());
            assertNotNull(trackDTO.getWaveformURL());
        }
    }


}
