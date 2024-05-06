package com.juztoss.rhythmo.views.activities;


import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.TestHelper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.juztoss.rhythmo.TestHelper.MUSIC_FOLDER;
import static com.juztoss.rhythmo.TestHelper.getSongName;
import static org.hamcrest.Matchers.allOf;


@RunWith(AndroidJUnit4.class)
public class PlayerActivityTest
{
    @Rule
    public ActivityTestRule<PlayerActivity> mActivityTestRule = new ActivityTestRule<PlayerActivity>(PlayerActivity.class){
        @Override
        protected Intent getActivityIntent()
        {
            Context targetContext = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext();
            Intent result = new Intent(targetContext, PlayerActivity.class);
            result.putExtra(PlayerActivity.DISABLE_RESCAN_ON_LAUNCHING, true);
            return result;
        }
    };

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

        TestHelper.checkScreen(TestHelper.AUDIO_FILES_COUNT, getSongName(1), MUSIC_FOLDER, "110.0", 0, false);

        //Click on the next button
        onView(withId(R.id.next_button)).perform(click());
        SystemClock.sleep(100);

        TestHelper.checkScreen(TestHelper.AUDIO_FILES_COUNT, getSongName(2), MUSIC_FOLDER, "130.0", 1, false);

        onView(withId(R.id.play_button)).perform(click());//Stop playback

        TestHelper.checkScreen(TestHelper.AUDIO_FILES_COUNT, getSongName(2), MUSIC_FOLDER, "130.0", -1, true);
    }

}
