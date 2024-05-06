package com.juztoss.rhythmo.views.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.android.material.tabs.TabLayout;
import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.views.activities.PlayerActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

/**
 * Created by JuzTosS on 7/29/2016.
 */
@RunWith(AndroidJUnit4.class)
public class OperationsWithPlaylists
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

    private void addPlaylist()
    {
        openActionBarOverflowOrOptionsMenu(mActivityRule.getActivity());
        onView(withText(mActivityRule.getActivity().getString(R.string.new_playlist))).perform(click());
    }

    private void removePlaylist()
    {
        openActionBarOverflowOrOptionsMenu(mActivityRule.getActivity());
        onView(withText(mActivityRule.getActivity().getString(R.string.remove_playlist))).perform(click());
    }

    @Test
    public void addAndRemovePlaylist() throws Exception
    {
        //Get initial tabs count
        TabLayout tabs = (TabLayout) mActivityRule.getActivity().findViewById(R.id.tab_layout);
        int tabsCount = tabs.getTabCount();

        //Open menu and click on add new playlist
        addPlaylist();
        assertEquals(tabsCount + 1, tabs.getTabCount());
        assertEquals(tabsCount, tabs.getSelectedTabPosition());

        onView(withText(mActivityRule.getActivity().getString(R.string.main_playlist_name))).perform(click());

        addPlaylist();
        assertEquals(tabsCount + 2, tabs.getTabCount());
        assertEquals(tabsCount + 1, tabs.getSelectedTabPosition());
        //Remove the just added playlist
        removePlaylist();
        removePlaylist();
        assertEquals(tabsCount, tabs.getTabCount());
    }

    @Test
    public void renamePlaylist() throws Exception
    {
        Activity activity = mActivityRule.getActivity();
        //Add new playlist
        TabLayout tabs = (TabLayout) activity.findViewById(R.id.tab_layout);
        addPlaylist();

        //Check name of an empty playlist
        TabLayout.Tab tab = tabs.getTabAt(tabs.getTabCount() - 1);
        String defaultPlaylistName = activity.getString(R.string.default_playlist_name);
        assertEquals(tab.getText(), defaultPlaylistName);

        //Launch the rename dialog
        openActionBarOverflowOrOptionsMenu(mActivityRule.getActivity());
        onView(withText(activity.getString(R.string.rename_playlist))).perform(click());
        onView(withText(activity.getString(R.string.rename_dialog_title))).check(matches(isDisplayed()));
        onView(withText(activity.getString(R.string.dialog_ok))).check(matches(isDisplayed()));
        onView(withText(activity.getString(R.string.dialog_cancel))).check(matches(isDisplayed()));
        onView(withText(defaultPlaylistName)).check(matches(isDisplayed()));

        //Put other name
        String newName = "new name";
        onView(withText(defaultPlaylistName)).perform(replaceText(newName));
        onView(withText(activity.getString(R.string.dialog_cancel))).perform(click());

        //Check name after renaming was cancelled
        tab = tabs.getTabAt(tabs.getTabCount() - 1);
        assertEquals(defaultPlaylistName, tab.getText());

        //Launch the rename dialog
        openActionBarOverflowOrOptionsMenu(mActivityRule.getActivity());
        onView(withText(activity.getString(R.string.rename_playlist))).perform(click());
        onView(withText(activity.getString(R.string.rename_dialog_title))).check(matches(isDisplayed()));
        onView(withText(activity.getString(R.string.dialog_ok))).check(matches(isDisplayed()));
        onView(withText(activity.getString(R.string.dialog_cancel))).check(matches(isDisplayed()));
        onView(withText(defaultPlaylistName)).check(matches(isDisplayed()));

        //Put other name
        String newName2 = "new name2";
        onView(withText(defaultPlaylistName)).perform(replaceText(newName2));
        onView(withText(activity.getString(R.string.dialog_ok))).perform(click());

        //Check name after renaming was performed
        tab = tabs.getTabAt(tabs.getTabCount() - 1);
        assertEquals(newName2, tab.getText());

        //Remove the just added playlist
        removePlaylist();
    }
}
