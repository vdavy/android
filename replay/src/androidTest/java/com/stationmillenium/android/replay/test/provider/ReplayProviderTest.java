package com.stationmillenium.android.replay.test.provider;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.stationmillenium.android.replay.SoundcloudRestClient;
import com.stationmillenium.android.replay.dto.TrackDTO;
import com.stationmillenium.android.replay.provider.ReplayContract;
import com.stationmillenium.android.replay.provider.ReplayProvider;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static com.stationmillenium.android.replay.provider.ReplayContract.Columns.ARTWORK_URL;
import static com.stationmillenium.android.replay.provider.ReplayContract.Columns.DESCRIPTION;
import static com.stationmillenium.android.replay.provider.ReplayContract.Columns.DURATION;
import static com.stationmillenium.android.replay.provider.ReplayContract.Columns.GENRE;
import static com.stationmillenium.android.replay.provider.ReplayContract.Columns.LAST_MODIFIED;
import static com.stationmillenium.android.replay.provider.ReplayContract.Columns.STREAM_URL;
import static com.stationmillenium.android.replay.provider.ReplayContract.Columns.TAG_LIST;
import static com.stationmillenium.android.replay.provider.ReplayContract.Columns.WAVEFORM_URL;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Test of {@link ReplayContract} of {@link UriMatcher}
 * Created by vincent on 29/08/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ReplayProviderTest {

    @Mock
    private SoundcloudRestClient soundcloudRestClient;

    @InjectMocks
    private ReplayProvider replayProvider;

    @Before
    public void before() {
        replayProvider = new ReplayProvider();
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTypeWithNoMatch() {
        Uri uriToTest = new Uri.Builder().appendPath("Fake").build();
        replayProvider.getType(uriToTest);
    }

    @Test
    public void testUriMatcherMatch() {
        Uri uriToTest = ReplayContract.ROOT_URI.buildUpon().appendPath(ReplayContract.DEFAULT_MATCH).build();
        Assert.assertEquals(ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + ReplayContract.MIME_TYPE, replayProvider.getType(uriToTest));
    }

    @Test
    public void testReplayQuery() {
        Uri replayUri = ReplayContract.ROOT_URI.buildUpon().appendPath(ReplayContract.DEFAULT_MATCH).build();
        List<TrackDTO> mockedTrackList = getMockTrackDTOs(5);
        when(soundcloudRestClient.getTracksList()).thenReturn(mockedTrackList);

        Cursor cursor = replayProvider.query(replayUri, null, null, null, null);

        assertNotNull(cursor);
        assertEquals(mockedTrackList.size(), cursor.getCount());
        assertEquals(10, cursor.getColumnCount());
        int index = 0;
        while(cursor.moveToNext()) {
            assertEquals(mockedTrackList.get(index).getId(), cursor.getInt(cursor.getColumnIndex(_ID)));
            assertEquals(mockedTrackList.get(index).getArtworkURL(), cursor.getString(cursor.getColumnIndex(ARTWORK_URL)));
            assertEquals(mockedTrackList.get(index).getDescription(), cursor.getString(cursor.getColumnIndex(DESCRIPTION)));
            assertEquals(mockedTrackList.get(index).getDuration(), cursor.getInt(cursor.getColumnIndex(DURATION)));
            assertEquals(mockedTrackList.get(index).getGenre(), cursor.getString(cursor.getColumnIndex(GENRE)));
            assertEquals(mockedTrackList.get(index).getStreamURL(), cursor.getString(cursor.getColumnIndex(STREAM_URL)));
            assertEquals(mockedTrackList.get(index).getTagList(), cursor.getString(cursor.getColumnIndex(TAG_LIST)));
            assertEquals(mockedTrackList.get(index).getArtworkURL(), cursor.getString(cursor.getColumnIndex(ARTWORK_URL)));
            assertEquals(mockedTrackList.get(index).getWaveformURL(), cursor.getString(cursor.getColumnIndex(WAVEFORM_URL)));
            assertEquals(mockedTrackList.get(index).getLastModified().toString(), cursor.getString(cursor.getColumnIndex(LAST_MODIFIED)));
            index++;
        }

        verify(soundcloudRestClient).getTracksList();
        verifyNoMoreInteractions(soundcloudRestClient);
    }

    private List<TrackDTO> getMockTrackDTOs(int listSize) {
        List<TrackDTO> mockTrackDTOs = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            TrackDTO mockTrack = new TrackDTO();
            mockTrack.setArtworkURL("Artwork" + i);
            mockTrack.setDescription("Description" + i);
            mockTrack.setDuration(i * 100);
            mockTrack.setGenre("Genre" + i);
            mockTrack.setId(i);
            mockTrack.setLastModified(new Date());
            mockTrack.setStreamURL("StreamURL" + i);
            mockTrack.setTagList("TagList" + i);
            mockTrack.setArtworkURL("ArtworkURL" + i);
            mockTrack.setWaveformURL("WaveformURL" + i);
            mockTrackDTOs.add(mockTrack);
        }
        return mockTrackDTOs;
    }
}
