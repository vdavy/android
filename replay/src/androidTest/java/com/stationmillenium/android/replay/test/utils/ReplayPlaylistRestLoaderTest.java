package com.stationmillenium.android.replay.test.utils;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.stationmillenium.android.replay.dto.PlaylistDTO;
import com.stationmillenium.android.replay.utils.ReplayPlaylistRestLoader;
import com.stationmillenium.android.replay.utils.ReplayTrackRestLoader;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Test {@link ReplayTrackRestLoader}
 * Created by vincent on 28/08/16.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class ReplayPlaylistRestLoaderTest {

    @Test
    public void testPlaylistList() {
        List<PlaylistDTO> playlistDTOs = new ReplayPlaylistRestLoader(InstrumentationRegistry.getTargetContext()).loadInBackground();
        assertCompleteList(playlistDTOs);
    }

    @Test
    public void test5PlaylistList() {
        List<PlaylistDTO> playlistDTOs = new ReplayPlaylistRestLoader(InstrumentationRegistry.getTargetContext(), "").loadInBackground();
        assertItemList(playlistDTOs, 5);
    }

    private void assertItemList(List<PlaylistDTO> playlistDTOs, int expected) {
        assertNotNull(playlistDTOs);
        assertEquals(expected, playlistDTOs.size());
        assertPlaylistList(playlistDTOs);
    }

    private void assertCompleteList(List<PlaylistDTO> playlistDTOs) {
        assertItemList(playlistDTOs, 14);
    }

    private void assertPlaylistList(List<PlaylistDTO> playlistDTOs) {
        for (PlaylistDTO playlistDTO : playlistDTOs) {
            assertNotNull(playlistDTO);
            assertNotNull(playlistDTO.getTitle());
            assertTrue(playlistDTO.getCount() > 0);
        }
    }

}
