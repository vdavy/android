package com.stationmillenium.android.replay.test.provider;

import android.content.UriMatcher;
import android.net.Uri;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.stationmillenium.android.replay.provider.ReplayContract;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test of {@link com.stationmillenium.android.replay.provider.ReplayContract} of {@link android.content.UriMatcher}
 * Created by vincent on 29/08/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class URIMatcherTest {

    @Test
    public void testUriMatcherNoMatch() {
        Uri uriToTest = new Uri.Builder().appendPath("Fake").build();
        Assert.assertEquals(UriMatcher.NO_MATCH, ReplayContract.URI_MATCHER.match(uriToTest));
    }

    @Test
    public void testUriMatcherMatch() {
        Uri uriToTest = ReplayContract.ROOT_URI.buildUpon().appendPath(ReplayContract.DEFAULT_MATCH).build();
        Assert.assertEquals(ReplayContract.ALL_REPLAY, ReplayContract.URI_MATCHER.match(uriToTest));
    }

}
