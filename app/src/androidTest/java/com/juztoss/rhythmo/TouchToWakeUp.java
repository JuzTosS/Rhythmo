package com.juztoss.rhythmo;

import android.content.Context;
import android.content.Intent;
import androidx.test.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.juztoss.rhythmo.views.activities.PlayerActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static junit.framework.Assert.assertTrue;

/**
 * Created by JuzTosS on 7/29/2016.
 */
@RunWith(AndroidJUnit4.class)
public class TouchToWakeUp
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
    public void wakeUpNeo() throws Exception
    {
        openActionBarOverflowOrOptionsMenu(mActivityRule.getActivity());
        Thread.sleep(1000);
        assertTrue(true);
    }

}
