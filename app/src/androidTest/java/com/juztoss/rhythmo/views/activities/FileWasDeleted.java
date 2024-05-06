package com.juztoss.rhythmo.views.activities;


import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.SystemClock;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.TestHelper;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.Espresso.onView;
import static com.juztoss.rhythmo.TestHelper.AUDIO_FILES_COUNT;
import static com.juztoss.rhythmo.TestHelper.MUSIC_FOLDER;
import static com.juztoss.rhythmo.TestHelper.getSongName;
import static com.juztoss.rhythmo.TestHelper.withRecyclerView;
import static org.hamcrest.Matchers.allOf;

import androidx.test.runner.AndroidJUnit4;


@RunWith(AndroidJUnit4.class)
public class FileWasDeleted
{
    @Rule
    public ActivityTestRule<PlayerActivity> mActivityRule = new ActivityTestRule<PlayerActivity>(PlayerActivity.class){
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
    public void fileWasDeleted() throws Exception
    {
        TestHelper.checkScreen(AUDIO_FILES_COUNT, "", "", "", -1, true);
        //Adding playlist
        openActionBarOverflowOrOptionsMenu(mActivityRule.getActivity());
        onView(withText(mActivityRule.getActivity().getString(R.string.new_playlist))).perform(click());

        //Adding folder, click on the apply button
        onView(withId(R.id.btnAddToPlaylist)).perform(click());

        //Select all
        for(int i = 0; i < (AUDIO_FILES_COUNT / 2 + 1); i++)
        {
            onView(allOf(withId(R.id.listView), isDisplayed())).perform(RecyclerViewActions.scrollToPosition(i));
            onView(allOf(withRecyclerView(R.id.listView).atPositionOnView(i, R.id.add_icon), isDisplayed())).perform(click());
        }


        onView(withId(R.id.apply)).perform(click());

        String fileToDelete1 = Environment.getExternalStorageDirectory().getPath() + "/" + MUSIC_FOLDER + "/" + getSongName(0);
        String fileToDelete2 = Environment.getExternalStorageDirectory().getPath() + "/" + MUSIC_FOLDER + "/" + getSongName(1);
        String fileToDelete3 = Environment.getExternalStorageDirectory().getPath() + "/" + MUSIC_FOLDER + "/" + getSongName(2);
        File file1 = new File(fileToDelete1);
        File file2 = new File(fileToDelete2);
        File file3 = new File(fileToDelete3);
        if (!file1.delete()) Assert.assertTrue(false);
        if (!file2.delete()) Assert.assertTrue(false);
        if (!file3.delete()) Assert.assertTrue(false);

        SystemClock.sleep(1000);
        TestHelper.checkScreen(AUDIO_FILES_COUNT, "", "", "", -1, true);

        //Adding folder, click on the apply button
        onView(withId(R.id.btnAddToPlaylist)).perform(click());

        TestHelper.updateLibrary(mActivityRule.getActivity());
        //Go back to player activity
        pressBack();

        TestHelper.checkScreen(0, "", "", "", -1, true);

        //Remove playlist
        openActionBarOverflowOrOptionsMenu(mActivityRule.getActivity());
        onView(withText(mActivityRule.getActivity().getString(R.string.remove_playlist))).perform(click());

        TestHelper.copyFiles();
        TestHelper.updateLibrary(mActivityRule.getActivity());
    }
}
