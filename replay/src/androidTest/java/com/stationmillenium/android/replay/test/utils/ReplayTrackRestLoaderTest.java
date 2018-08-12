package com.stationmillenium.android.replay.test.utils;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import com.stationmillenium.android.replay.dto.PlaylistDTO;
import com.stationmillenium.android.replay.dto.TrackDTO;
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
public class ReplayTrackRestLoaderTest {

    @Test
    public void testTracksList() {
        List<TrackDTO> trackDTOs = new ReplayTrackRestLoader(InstrumentationRegistry.getTargetContext()).loadInBackground();
        assertCompleteList(trackDTOs);
    }

    @Test
    public void test10TracksList() {
        List<TrackDTO> trackDTOs = new ReplayTrackRestLoader(InstrumentationRegistry.getTargetContext(), 5).loadInBackground();
        assertItemList(trackDTOs, 5);
    }

    private void assertItemList(List<TrackDTO> trackDTOs, int expected) {
        assertNotNull(trackDTOs);
        assertEquals(expected, trackDTOs.size());
        assertReplayList(trackDTOs);
    }

    @Test
    public void testGenreList() {
        List<TrackDTO> trackDTOs = new ReplayTrackRestLoader(InstrumentationRegistry.getTargetContext(), ReplayTrackRestLoader.QueryType.GENRE, "Sports").loadInBackground();
        assertCompleteList(trackDTOs);
    }

    @Test
    public void testSearchList() {
        List<TrackDTO> trackDTOs = new ReplayTrackRestLoader(InstrumentationRegistry.getTargetContext(), ReplayTrackRestLoader.QueryType.SEARCH, "Sports").loadInBackground();
        assertCompleteList(trackDTOs);
    }

    @Test
    public void test5SearchList() {
        List<TrackDTO> trackDTOs = new ReplayTrackRestLoader(InstrumentationRegistry.getTargetContext(), ReplayTrackRestLoader.QueryType.SEARCH, "Sports", 5).loadInBackground();
        assertItemList(trackDTOs, 5);
    }

    @Test
    public void test2GenreList() {
        List<TrackDTO> trackDTOs = new ReplayTrackRestLoader(InstrumentationRegistry.getTargetContext(), ReplayTrackRestLoader.QueryType.GENRE, "Sport", 2).loadInBackground();
        assertItemList(trackDTOs, 2);
    }

    @Test
    public void test5TagsList() {
        List<TrackDTO> trackDTOs = new ReplayTrackRestLoader(InstrumentationRegistry.getTargetContext(), ReplayTrackRestLoader.QueryType.TAG, "Sports", 5).loadInBackground();
        assertItemList(trackDTOs, 5);
    }

    @Test
    public void testTagList() {
        List<TrackDTO> trackDTOs = new ReplayTrackRestLoader(InstrumentationRegistry.getTargetContext(), ReplayTrackRestLoader.QueryType.TAG, "Sports").loadInBackground();
        assertCompleteList(trackDTOs);
    }

    private void assertCompleteList(List<TrackDTO> trackDTOs) {
        assertItemList(trackDTOs, 10);
    }

    private void assertReplayList(List<TrackDTO> trackDTOs) {
        for (TrackDTO trackDTO : trackDTOs) {
            assertNotNull(trackDTO);
            assertTrue(trackDTO.getId() > 0);
            assertTrue(trackDTO.getDuration() > 0);
            assertNotNull(trackDTO.getTitle());
            assertNotNull(trackDTO.getDate());
            assertNotNull(trackDTO.getTagList());
            assertNotNull(trackDTO.getFileURL());
            assertNotNull(trackDTO.getWaveformURL());
        }
    }

    @Test
    public void testPlaylistTracksLoad() {
        PlaylistDTO playlistDTO = new PlaylistDTO();
        playlistDTO.setId(318163137);
        List<TrackDTO> trackDTOs = new ReplayTrackRestLoader(InstrumentationRegistry.getTargetContext(), playlistDTO, 5).loadInBackground();
        assertItemList(trackDTOs, 5);
        playlistDTO.setTracks(trackDTOs);
        trackDTOs = new ReplayTrackRestLoader(InstrumentationRegistry.getTargetContext(), playlistDTO).loadInBackground();
        assertItemList(trackDTOs, 5);
    }

}
