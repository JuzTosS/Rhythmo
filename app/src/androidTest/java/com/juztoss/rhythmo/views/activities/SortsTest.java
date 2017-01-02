package com.juztoss.rhythmo.views.activities;


import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.TestHelper;
import com.juztoss.rhythmo.audio.BpmDetector;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.juztoss.rhythmo.TestHelper.getSongName;
import static com.juztoss.rhythmo.TestHelper.withRecyclerView;
import static org.hamcrest.Matchers.allOf;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class SortsTest
{
    @Rule
    public ActivityTestRule<PlayerActivity> mActivityTestRule = new ActivityTestRule<>(PlayerActivity.class);

    @Test
    public void sortsTest()
    {
        TestHelper.checkScreen(TestHelper.AUDIO_FILES_COUNT, "", "", "", -1, true);

        //Name sort
        onView(allOf(withId(R.id.sort_menu), isDisplayed())).perform(click());
        onView(allOf(withText(R.string.sort_alphabetically), isDisplayed())).perform(click());

        List<String> songNames = new ArrayList<>();
        for(int i = 0; i < TestHelper.AUDIO_FILES_COUNT; i++)
        {
            songNames.add(getSongName(i));
        }

        Collections.sort(songNames);
        checkVisiblePlaylistList(songNames);


        //BPM sort
        onView(allOf(withId(R.id.sort_menu), isDisplayed())).perform(click());
        onView(allOf(withText(R.string.sort_by_bpm), isDisplayed())).perform(click());

        Collections.sort(songNames, new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                Integer bpm1 = (int)(BpmDetector.detectFromName(o1) * 10);
                Integer bpm2 = (int)(BpmDetector.detectFromName(o2) * 10);
                if(!bpm1.equals(bpm2))
                    return bpm1.compareTo(bpm2);
                else
                    return o1.compareTo(o2);
            }
        });
        checkVisiblePlaylistList(songNames);


        //Directories sort
        onView(allOf(withId(R.id.sort_menu), isDisplayed())).perform(click());
        onView(allOf(withText(R.string.sort_by_folders), isDisplayed())).perform(click());

        List<String> songNamesFirstFolder = new ArrayList<>();
        for(int i = 0; i < TestHelper.AUDIO_FILES_COUNT / 2; i++)
        {
            songNamesFirstFolder.add(getSongName(i));
        }

        List<String> songNamesSecondFolder = new ArrayList<>();
        for(int i = TestHelper.AUDIO_FILES_COUNT / 2; i < TestHelper.AUDIO_FILES_COUNT; i++)
        {
            songNamesSecondFolder.add(getSongName(i));
        }

        Collections.sort(songNamesFirstFolder);
        Collections.sort(songNamesSecondFolder);

        songNames.clear();
        songNames.addAll(songNamesFirstFolder);
        songNames.addAll(songNamesSecondFolder);

        checkVisiblePlaylistList(songNames);
    }

    /**
     * Checking sorting
     * @param songNames
     */
    private void checkVisiblePlaylistList(List<String> songNames)
    {
        for(int i = 0; i < TestHelper.AUDIO_FILES_COUNT; i++)
        {
            onView(allOf(withId(R.id.listView), isDisplayed())).perform(scrollToPosition(i));
            onView(allOf(withRecyclerView(R.id.listView).atPosition(i), isDisplayed())).
                    check(matches(hasDescendant(allOf(withId(R.id.first_line), withText(songNames.get(i))))));
        }
    }

}
