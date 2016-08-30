package com.stationmillenium.android.replay.test.provider;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.stationmillenium.android.replay.provider.ReplayContract;
import com.stationmillenium.android.replay.provider.ReplayProvider;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test of {@link ReplayContract} of {@link UriMatcher}
 * Created by vincent on 29/08/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ReplayProviderTest {

    private ReplayProvider replayProvider;

    @Before
    public void before() {
        replayProvider = new ReplayProvider();
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

}
