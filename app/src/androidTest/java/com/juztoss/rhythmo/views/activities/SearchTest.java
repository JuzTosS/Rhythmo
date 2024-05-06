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
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.juztoss.rhythmo.TestHelper.MUSIC_FOLDER_NESTED;
import static com.juztoss.rhythmo.TestHelper.clickChildViewWithId;
import static com.juztoss.rhythmo.TestHelper.getSongName;
import static org.hamcrest.Matchers.allOf;


@RunWith(AndroidJUnit4.class)
public class SearchTest
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
    public void searchTest()
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

        TestHelper.checkScreen(TestHelper.AUDIO_FILES_COUNT, getSongName(43), MUSIC_FOLDER_NESTED, "140.0", 43, false);

        onView(withId(R.id.play_button)).perform(click());//Stop playback

        SystemClock.sleep(100);

        TestHelper.checkScreen(TestHelper.AUDIO_FILES_COUNT, getSongName(43), MUSIC_FOLDER_NESTED, "140.0", -1, true);

        //Test relaunching activity
        onView(allOf(withId(R.id.search_menu), isDisplayed())).perform(click());
        onView(withId(R.id.search_field)).perform(replaceText("43"));//Search for 43

        onView(allOf(withId(R.id.listView), isDisplayed())).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.menu_button)));
        onView(withText(R.string.song_menu_detail)).perform(click());

        pressBack();//Return to main activity

        TestHelper.checkScreen(TestHelper.AUDIO_FILES_COUNT, getSongName(43), MUSIC_FOLDER_NESTED, "140.0", -1, true);

    }

}
