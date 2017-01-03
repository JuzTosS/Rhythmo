package com.juztoss.rhythmo;

import android.annotation.SuppressLint;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.juztoss.rhythmo.presenters.RhythmoApp;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by JuzTosS on 8/21/2016.
 */
@RunWith(AndroidJUnit4.class)
public class CopyMediaToDevice
{
    @Rule
    public ServiceTestRule mServiceTestRule = new ServiceTestRule();

    @SuppressLint("CommitPrefEdits")
    @Test
    public void execute() throws Exception
    {
        RemoveMediaFromDevice.doRemove();

        RhythmoApp app = (RhythmoApp) InstrumentationRegistry.getTargetContext().getApplicationContext();
        app.getSharedPreferences().edit().putBoolean(RhythmoApp.FIRST_RUN, false).commit();
        app.getSharedPreferences().edit().putBoolean(RhythmoApp.LIBRARY_BUILD_HAD_STARTED, true).commit();

        TestHelper.copyFiles();
        TestHelper.updateLibrary(null);

        Assert.assertTrue(true);
    }

}
