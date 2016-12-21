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
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.juztoss.rhythmo.TestSuite.SONG1;
import static com.juztoss.rhythmo.TestSuite.SONG2;
import static org.hamcrest.Matchers.allOf;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class PlayerActivityTest
{
    @Rule
    public ActivityTestRule<PlayerActivity> mActivityTestRule = new ActivityTestRule<>(PlayerActivity.class);

    @Test
    public void playerActivityTest()
    {
        TestHelper.checkScreen(3, "", "", "", -1, true);

        onView(allOf(withId(R.id.listView), isDisplayed()))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        TestHelper.checkScreen(3, SONG1, "RhythmoTestTemp", "120.0", 0, false);

        //Click on the next button
        onView(withId(R.id.next_button)).perform(click());
        SystemClock.sleep(1000);

        TestHelper.checkScreen(3, SONG2, "RhythmoTestTemp", "140.0", 1, false);
    }

}
