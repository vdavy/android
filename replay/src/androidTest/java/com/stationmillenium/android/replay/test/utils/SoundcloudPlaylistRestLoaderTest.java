package com.stationmillenium.android.replay.test.utils;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import com.stationmillenium.android.replay.dto.PlaylistDTO;
import com.stationmillenium.android.replay.dto.TrackDTO;
import com.stationmillenium.android.replay.utils.SoundcloudPlaylistRestLoader;
import com.stationmillenium.android.replay.utils.SoundcloudTrackRestLoader;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Test {@link SoundcloudTrackRestLoader}
 * Created by vincent on 28/08/16.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class SoundcloudPlaylistRestLoaderTest {

    @Test
    public void testPlaylistList() {
        List<PlaylistDTO> playlistDTOs = new SoundcloudPlaylistRestLoader(InstrumentationRegistry.getTargetContext()).loadInBackground();
        assertCompleteList(playlistDTOs);
    }

    @Test
    public void test5PlaylistList() {
        List<PlaylistDTO> playlistDTOs = new SoundcloudPlaylistRestLoader(InstrumentationRegistry.getTargetContext(), 5).loadInBackground();
        assertItemList(playlistDTOs, 5);
    }

    private void assertItemList(List<PlaylistDTO> playlistDTOs, int expected) {
        assertNotNull(playlistDTOs);
        assertEquals(expected, playlistDTOs.size());
        assertPlaylistList(playlistDTOs);
    }

    private void assertCompleteList(List<PlaylistDTO> playlistDTOs) {
        assertItemList(playlistDTOs, 50);
    }

    private void assertPlaylistList(List<PlaylistDTO> playlistDTOs) {
        for (PlaylistDTO playlistDTO : playlistDTOs) {
            assertNotNull(playlistDTO);
            assertNotNull(playlistDTO.getTitle());
            assertNotNull(playlistDTO.getLastModified());
            assertTrue(playlistDTO.getTrackCount() > 0);
            assertTracks(playlistDTO.getTracks());
        }
    }

    private void assertTracks(List<TrackDTO> trackDTOs) {
        for (TrackDTO trackDTO : trackDTOs) {
            assertNotNull(trackDTO);
            assertTrue(trackDTO.getId() > 0);
            assertTrue(trackDTO.getDuration() > 0);
            assertNotNull(trackDTO.getTitle());
            assertNotNull(trackDTO.getLastModified());
            assertNotNull(trackDTO.getTagList());
            assertNotNull(trackDTO.getStreamURL());
            assertNotNull(trackDTO.getWaveformURL());
        }
    }


}
