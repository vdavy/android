package com.stationmillenium.android.replay.test.utils;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import com.stationmillenium.android.replay.dto.TrackDTO;
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
public class SoundcloudTrackRestLoaderTest {

    @Test
    public void testTracksList() {
        List<TrackDTO> trackDTOs = new SoundcloudTrackRestLoader(InstrumentationRegistry.getTargetContext()).loadInBackground();
        assertCompleteList(trackDTOs);
    }

    @Test
    public void test10TracksList() {
        List<TrackDTO> trackDTOs = new SoundcloudTrackRestLoader(InstrumentationRegistry.getTargetContext(), 5).loadInBackground();
        assertItemList(trackDTOs, 5);
    }

    private void assertItemList(List<TrackDTO> trackDTOs, int expected) {
        assertNotNull(trackDTOs);
        assertEquals(expected, trackDTOs.size());
        assertReplayList(trackDTOs);
    }

    @Test
    public void testGenreList() {
        List<TrackDTO> trackDTOs = new SoundcloudTrackRestLoader(InstrumentationRegistry.getTargetContext(), SoundcloudTrackRestLoader.QueryType.GENRE, "Sports").loadInBackground();
        assertCompleteList(trackDTOs);
    }

    @Test
    public void testSearchList() {
        List<TrackDTO> trackDTOs = new SoundcloudTrackRestLoader(InstrumentationRegistry.getTargetContext(), SoundcloudTrackRestLoader.QueryType.SEARCH, "Sports").loadInBackground();
        assertCompleteList(trackDTOs);
    }

    @Test
    public void test5SearchList() {
        List<TrackDTO> trackDTOs = new SoundcloudTrackRestLoader(InstrumentationRegistry.getTargetContext(), SoundcloudTrackRestLoader.QueryType.SEARCH, "Sports", 5).loadInBackground();
        assertItemList(trackDTOs, 5);
    }

    @Test
    public void test2GenreList() {
        List<TrackDTO> trackDTOs = new SoundcloudTrackRestLoader(InstrumentationRegistry.getTargetContext(), SoundcloudTrackRestLoader.QueryType.GENRE, "Sport", 2).loadInBackground();
        assertItemList(trackDTOs, 2);
    }

    @Test
    public void test5TagsList() {
        List<TrackDTO> trackDTOs = new SoundcloudTrackRestLoader(InstrumentationRegistry.getTargetContext(), SoundcloudTrackRestLoader.QueryType.TAG, "Sports", 5).loadInBackground();
        assertItemList(trackDTOs, 5);
    }

    @Test
    public void testTagList() {
        List<TrackDTO> trackDTOs = new SoundcloudTrackRestLoader(InstrumentationRegistry.getTargetContext(), SoundcloudTrackRestLoader.QueryType.TAG, "Sports").loadInBackground();
        assertCompleteList(trackDTOs);
    }

    private void assertCompleteList(List<TrackDTO> trackDTOs) {
        assertItemList(trackDTOs, 50);
    }

    private void assertReplayList(List<TrackDTO> trackDTOs) {
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
