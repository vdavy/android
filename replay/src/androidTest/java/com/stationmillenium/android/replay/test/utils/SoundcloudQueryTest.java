package com.stationmillenium.android.replay.test.utils;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import com.stationmillenium.android.replay.dto.TrackDTO;
import com.stationmillenium.android.replay.utils.ReplayTrackRestLoader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Test {@link ReplayTrackRestLoader}
 * Created by vincent on 28/08/16.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class SoundcloudQueryTest {

    private ReplayTrackRestLoader restClient;

    @Before
    public void beforeTest() {
        restClient = new ReplayTrackRestLoader(InstrumentationRegistry.getTargetContext());
    }

    @Test
    public void testTracksList() {
        List<TrackDTO> trackDTOs = restClient.loadInBackground();
        assertNotNull(trackDTOs);
        assertFalse(0 == trackDTOs.size());
        for (TrackDTO trackDTO : trackDTOs) {
            assertNotNull(trackDTO);
            assertTrue(trackDTO.getId() > 0);
            assertTrue(trackDTO.getDuration() > 0);
            assertNotNull(trackDTO.getTitle());
            assertNotNull(trackDTO.getDate());
            assertNotNull(trackDTO.getFileURL());
        }
    }

}
