package com.juztoss.rhythmo;

import android.app.Activity;
import android.support.design.widget.TabLayout;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.juztoss.rhythmo.views.activities.PlayerActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by JuzTosS on 7/29/2016.
 */
@RunWith(AndroidJUnit4.class)
public class TouchToWakeUp
{
    @Rule
    public ActivityTestRule<PlayerActivity> mActivityRule = new ActivityTestRule<PlayerActivity>(PlayerActivity.class);

    @Test
    public void wakeUpNeo() throws Exception
    {
        openActionBarOverflowOrOptionsMenu(mActivityRule.getActivity());
        Thread.sleep(1000);
        assertTrue(true);
    }

}
