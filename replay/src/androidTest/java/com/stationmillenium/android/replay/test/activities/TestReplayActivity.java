package com.stationmillenium.android.replay.test.activities;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.activities.ReplayActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.stationmillenium.android.replay.test.activities.RecyclerViewMatcher.withRecyclerView;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;

/**
 * Test for replay activity
 * Created by vincent on 12/11/16.
 */
@RunWith(AndroidJUnit4.class)
public class TestReplayActivity {

    @Rule
    public ActivityTestRule<ReplayActivity> mActivityTestRule = new ActivityTestRule<>(ReplayActivity.class);

    @Test
    public void testHeader() {
        onView(withId(R.id.replay_count)).check(matches(withText("50 replays disponibles")));
        onView(withRecyclerView(R.id.replay_recyclerview).atPosition(0)).check(matches(hasDescendant(allOf(withId(R.id.replay_title), withText(any(String.class))))));
        onView(withRecyclerView(R.id.replay_recyclerview).atPosition(0)).check(matches(hasDescendant(allOf(withId(R.id.replay_description), withText(any(String.class))))));
        onView(withRecyclerView(R.id.replay_recyclerview).atPosition(0)).check(matches(hasDescendant(allOf(withId(R.id.replay_date), withText(any(String.class))))));
        onView(withRecyclerView(R.id.replay_recyclerview).atPosition(0)).check(matches(hasDescendant(allOf(withId(R.id.replay_duration), withText(any(String.class))))));
    }

    @Test
    public void testExtraReplay() throws InterruptedException {
        testHeader();
        onView(withId(R.id.replay_recyclerview)).perform(scrollToPosition(49));
        UiDevice.getInstance(getInstrumentation()).swipe(500, 1000, 500, 500, 150);
        Thread.sleep(5000);
        onView(withId(R.id.replay_count)).check(matches(withText("80 replays disponibles")));
    }

    @Test
    public void testReload() throws InterruptedException {
        onView(withId(R.id.replay_recyclerview)).perform(swipeDown());
        onView(withId(R.id.replay_count)).check(matches(withText("50 replays disponibles")));
    }

}
