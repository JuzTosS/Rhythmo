package com.juztoss.rhythmo;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.juztoss.rhythmo.presenters.RhythmoApp;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import static com.juztoss.rhythmo.TestSuite.SONG1;
import static com.juztoss.rhythmo.TestSuite.SONG2;
import static com.juztoss.rhythmo.TestSuite.SONG3;

/**
 * Created by JuzTosS on 8/21/2016.
 */
@RunWith(AndroidJUnit4.class)
public class CopyMediaToDevice
{
    @Rule
    public ServiceTestRule mServiceTestRule = new ServiceTestRule();

    @Test
    public void execute() throws Exception
    {
        RemoveMediaFromDevice.doRemove();

        RhythmoApp app = (RhythmoApp) InstrumentationRegistry.getTargetContext().getApplicationContext();
        app.getSharedPreferences().edit().putBoolean(RhythmoApp.FIRST_RUN, false).commit();
        app.getSharedPreferences().edit().putBoolean(RhythmoApp.LIBRARY_BUILD_HAD_STARTED, true).commit();

        TestHelper.copyFiles();
        TestHelper.updateLibrary();

        Assert.assertTrue(true);
    }

}
