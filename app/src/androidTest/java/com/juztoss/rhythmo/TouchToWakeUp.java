package com.juztoss.rhythmo;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.juztoss.rhythmo.views.activities.PlayerActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static junit.framework.Assert.assertTrue;

/**
 * Created by JuzTosS on 7/29/2016.
 */
@RunWith(AndroidJUnit4.class)
public class TouchToWakeUp
{
    @Rule
    public ActivityTestRule<PlayerActivity> mActivityRule = new ActivityTestRule<>(PlayerActivity.class);

    @Test
    public void wakeUpNeo() throws Exception
    {
        openActionBarOverflowOrOptionsMenu(mActivityRule.getActivity());
        Thread.sleep(1000);
        assertTrue(true);
    }

}
