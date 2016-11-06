package com.stationmillenium.android.replay.test.utils;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.widget.TextView;

import com.stationmillenium.android.replay.utils.ReplayBindingUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Test {@link ReplayBindingUtil}
 * Created by vincent on 28/08/16.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class ReplayBindingUtilTest {

    private Context context;
    private TextView textView;

    @Before
    public void beforeTest() {
        context = InstrumentationRegistry.getTargetContext();
        textView= new TextView(context);
    }

    @Test
    public void test0Length() {
        ReplayBindingUtil.bindReplayDuration(textView, 0);
        assertNotNull(textView.getText());
        assertEquals("0:00", textView.getText());
    }

    @Test
    public void test2minLength() {
        ReplayBindingUtil.bindReplayDuration(textView, 2*60*1000);
        assertNotNull(textView.getText());
        assertEquals("2:00", textView.getText());
    }

    @Test
    public void test30minLength() {
        ReplayBindingUtil.bindReplayDuration(textView, 30*60*1000);
        assertNotNull(textView.getText());
        assertEquals("30:00", textView.getText());
    }

    @Test
    public void test2min30sLength() {
        ReplayBindingUtil.bindReplayDuration(textView, (2*60 + 30) * 1000);
        assertNotNull(textView.getText());
        assertEquals("2:30", textView.getText());
    }

    @Test
    public void test23min30sLength() {
        ReplayBindingUtil.bindReplayDuration(textView, (23*60 + 30) * 1000);
        assertNotNull(textView.getText());
        assertEquals("23:30", textView.getText());
    }

    @Test
    public void test2min1sLength() {
        ReplayBindingUtil.bindReplayDuration(textView, (2*60 + 1) * 1000);
        assertNotNull(textView.getText());
        assertEquals("2:01", textView.getText());
    }

    @Test
    public void test45min1sLength() {
        ReplayBindingUtil.bindReplayDuration(textView, (45*60 + 1) * 1000);
        assertNotNull(textView.getText());
        assertEquals("45:01", textView.getText());
    }

    @Test
    public void test1hourLength() {
        ReplayBindingUtil.bindReplayDuration(textView, (3600) * 1000);
        assertNotNull(textView.getText());
        assertEquals("1:00:00", textView.getText());
    }

    @Test
    public void test1hour1sLength() {
        ReplayBindingUtil.bindReplayDuration(textView, (60*60 + 1) * 1000);
        assertNotNull(textView.getText());
        assertEquals("1:00:01", textView.getText());
    }

    @Test
    public void test1hour10sLength() {
        ReplayBindingUtil.bindReplayDuration(textView, (60*60 + 10) * 1000);
        assertNotNull(textView.getText());
        assertEquals("1:00:10", textView.getText());
    }

    @Test
    public void test1hour1min10sLength() {
        ReplayBindingUtil.bindReplayDuration(textView, (60*60 + 60 + 10) * 1000);
        assertNotNull(textView.getText());
        assertEquals("1:01:10", textView.getText());
    }

    @Test
    public void test1hour10min10sLength() {
        ReplayBindingUtil.bindReplayDuration(textView, (60*60 + 10*60 + 10) * 1000);
        assertNotNull(textView.getText());
        assertEquals("1:10:10", textView.getText());
    }

    @Test
    public void test1hour10min5sLength() {
        ReplayBindingUtil.bindReplayDuration(textView, (60*60 + 10*60 + 5) * 1000);
        assertNotNull(textView.getText());
        assertEquals("1:10:05", textView.getText());
    }
}
