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
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.juztoss.rhythmo.TestHelper.MUSIC_FOLDER;
import static com.juztoss.rhythmo.TestHelper.getSongName;
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
        SystemClock.sleep(1000);
        TestHelper.checkScreen(TestHelper.AUDIO_FILES_COUNT, "", "", "", -1, true);

        //Name sort
        try
        {
            onView(allOf(withId(R.id.sort_menu), isDisplayed())).perform(click());
        }
        catch (Exception e)
        {
            openActionBarOverflowOrOptionsMenu(mActivityTestRule.getActivity());
            onView(allOf(withText(R.string.sort), isDisplayed())).perform(click());
        }

        onView(allOf(withText(R.string.sort_alphabetically), isDisplayed())).perform(click());

        onView(allOf(withId(R.id.listView), isDisplayed()))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        TestHelper.checkScreen(TestHelper.AUDIO_FILES_COUNT, getSongName(1), MUSIC_FOLDER, "140.0", 0, false);

        //Click on the next button
        onView(withId(R.id.next_button)).perform(click());
        SystemClock.sleep(1000);

        TestHelper.checkScreen(TestHelper.AUDIO_FILES_COUNT, getSongName(2), MUSIC_FOLDER, "160.0", 1, false);

        onView(withId(R.id.play_button)).perform(click());//Stop playback

        TestHelper.checkScreen(TestHelper.AUDIO_FILES_COUNT, getSongName(2), MUSIC_FOLDER, "160.0", -1, true);
    }

}
