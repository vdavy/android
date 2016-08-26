package com.stationmillenium.android.replay.test.utils;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.stationmillenium.android.replay.utils.URLManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Test {@link com.stationmillenium.android.replay.utils.URLManager}
 * Created by vincent on 28/08/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class URLManagerTest {

    private Context context;

    @Before
    public void beforeTest() {
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testTracksURL() {
        String url = URLManager.getTracksURL(context);
        assertNotNull(url);
        assertEquals("https://api.soundcloud.com/users/148534611/tracks?client_id=8a934c041fbb1f823f9eea645799f03f", url);
    }

    @Test
    public void testPlaylistsURL() {
        String url = URLManager.getPlaylistsURL(context);
        assertNotNull(url);
        assertEquals("https://api.soundcloud.com/users/148534611/playlists?client_id=8a934c041fbb1f823f9eea645799f03f", url);
    }

    @Test
    public void testStreamURL() {
        String url = URLManager.getStreamURL(context, "12345");
        assertNotNull(url);
        assertEquals("https://api.soundcloud.com/tracks/12345/stream?client_id=8a934c041fbb1f823f9eea645799f03f", url);
    }

}
