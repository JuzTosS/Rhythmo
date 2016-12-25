package com.juztoss.rhythmo.views.activities;


import android.os.SystemClock;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.TestHelper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.juztoss.rhythmo.TestHelper.getSongName;
import static org.hamcrest.Matchers.allOf;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class SearchTest
{
    @Rule
    public ActivityTestRule<PlayerActivity> mActivityTestRule = new ActivityTestRule<>(PlayerActivity.class);

    @Test
    public void playerActivityTest()
    {
        TestHelper.checkScreen(TestHelper.AUDIO_FILES_COUNT, "", "", "", -1, true);

        onView(allOf(withId(R.id.search_menu), isDisplayed()))
                .perform(click());

        onView(withId(R.id.search_field)).perform(typeText("43"));//Search for 43

        TestHelper.checkScreen(1, "", "", "", -1, true);

        onView(allOf(withId(R.id.listView), isDisplayed()))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click())); //Start playback

        SystemClock.sleep(1000);

        TestHelper.checkScreen(1, "", "", "", 0, false);

        pressBack();//Hide keyboard
        pressBack();//Disable search

        TestHelper.checkScreen(TestHelper.AUDIO_FILES_COUNT, getSongName(43), "RhythmoTestTemp", "180.0", 43, false);

        onView(withId(R.id.play_button)).perform(click());//Stop playback

        SystemClock.sleep(100);

        TestHelper.checkScreen(TestHelper.AUDIO_FILES_COUNT, getSongName(43), "RhythmoTestTemp", "180.0", -1, true);

    }

}
