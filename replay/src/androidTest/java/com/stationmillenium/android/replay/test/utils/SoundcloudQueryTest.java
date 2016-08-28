package com.stationmillenium.android.replay.test.utils;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import com.stationmillenium.android.replay.SoundcloudRestClient;
import com.stationmillenium.android.replay.dto.TracksDTO;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Test {@link SoundcloudRestClient}
 * Created by vincent on 28/08/16.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class SoundcloudQueryTest {

    private SoundcloudRestClient restClient;

    @Before
    public void beforeTest() {
        restClient = new SoundcloudRestClient(InstrumentationRegistry.getTargetContext());
    }

    @Test
    public void testTracksList() {
        List<TracksDTO> tracksDTOs = restClient.getTracksList();
        assertNotNull(tracksDTOs);
        assertFalse(0 == tracksDTOs.size());
        for (TracksDTO tracksDTO : tracksDTOs) {
            assertNotNull(tracksDTO);
            assertTrue(tracksDTO.getId() > 0);
            assertTrue(tracksDTO.getDuration() > 0);
            assertNotNull(tracksDTO.getTitle());
            assertNotNull(tracksDTO.getDescription());
            assertNotNull(tracksDTO.getLastModified());
            assertNotNull(tracksDTO.getTagList());
            assertNotNull(tracksDTO.getGenre());
            assertNotNull(tracksDTO.getStreamURL());
            assertNotNull(tracksDTO.getWaveformURL());
        }
    }

}
