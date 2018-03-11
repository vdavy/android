package com.stationmillenium.android.replay.test.activities;

import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
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
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.stationmillenium.android.replay.test.activities.RecyclerViewMatcher.withRecyclerView;
import static com.stationmillenium.android.replay.test.activities.TestUtils.withCustomConstraints;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.not;

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
        onView(withId(R.id.replay_count)).check(matches(withText("14 playlists disponibles")));
        onView(withRecyclerView(R.id.replay_recyclerview).atPosition(0)).check(matches(hasDescendant(allOf(withId(R.id.replay_title), withText(any(String.class))))));
        onView(withRecyclerView(R.id.replay_recyclerview).atPosition(0)).check(matches(hasDescendant(allOf(withId(R.id.replay_description), withText(any(String.class))))));
        onView(withRecyclerView(R.id.replay_recyclerview).atPosition(0)).check(matches(hasDescendant(allOf(withId(R.id.replay_date), withText(any(String.class))))));
        onView(withRecyclerView(R.id.replay_recyclerview).atPosition(0)).check(matches(hasDescendant(allOf(withId(R.id.replay_duration), withText(any(String.class))))));
    }

    @Test
    public void testExtraReplay() throws InterruptedException {
        onView(allOf(withId(R.id.replay_viewpager), hasFocus())).perform(withCustomConstraints(swipeLeft(), isDisplayingAtLeast(85)));
        testHeader();
        onView(withId(R.id.replay_recyclerview)).perform(scrollToPosition(49));
        UiDevice.getInstance(getInstrumentation()).swipe(500, 1000, 500, 500, 150);
        Thread.sleep(5000);
        onView(withId(R.id.replay_count)).check(matches(withText("80 replays disponibles")));
    }

    @Test
    public void testReload() throws InterruptedException {
        onView(allOf(withId(R.id.replay_srl), hasFocus())).perform(withCustomConstraints(swipeDown(), isDisplayingAtLeast(85)));
        onView(withId(R.id.replay_count)).check(matches(withText("14 playlists disponibles")));
    }

    @Test
    public void testFABClick() {
        onView(allOf(withId(R.id.replay_viewpager), hasFocus())).perform(withCustomConstraints(swipeLeft(), isDisplayingAtLeast(85)));
        onView(withId(R.id.replay_search_menu)).check(doesNotExist());
        onView(withId(R.id.search_fab)).perform(click());
        onView(withId(R.id.replay_search_menu)).check(matches(isDisplayed()));
        onView(withId(R.id.search_fab)).check(matches(not(isDisplayed())));
        pressBack();
        pressBack();
        onView(withId(R.id.replay_search_menu)).check(doesNotExist());
        onView(withId(R.id.search_fab)).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchViewRotate() throws InterruptedException, RemoteException {
        onView(allOf(withId(R.id.replay_viewpager), hasFocus())).perform(withCustomConstraints(swipeLeft(), isDisplayingAtLeast(85)));
        onView(withId(R.id.replay_search_menu)).check(doesNotExist());
        onView(withId(R.id.search_fab)).perform(click());
        onView(withId(R.id.replay_search_menu)).check(matches(isDisplayed()));
        onView(withId(R.id.search_fab)).check(matches(not(isDisplayed())));

        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).setOrientationLeft();
        onView(withId(R.id.replay_search_menu)).check(matches(isDisplayed()));
        onView(withId(R.id.search_fab)).check(matches(not(isDisplayed())));

        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).setOrientationRight();
        onView(withId(R.id.replay_search_menu)).check(matches(isDisplayed()));
        onView(withId(R.id.search_fab)).check(matches(not(isDisplayed())));

        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).setOrientationNatural();
        onView(withId(R.id.replay_search_menu)).check(matches(isDisplayed()));
        onView(withId(R.id.search_fab)).check(matches(not(isDisplayed())));

        pressBack();
        pressBack();
        onView(withId(R.id.replay_search_menu)).check(doesNotExist());
        onView(withId(R.id.search_fab)).check(matches(isDisplayed()));
    }
}
