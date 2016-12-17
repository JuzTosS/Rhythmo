package com.juztoss.rhythmo;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.services.AsyncBuildLibraryTask;
import com.juztoss.rhythmo.services.AsyncDetectBpmByNamesTask;
import com.juztoss.rhythmo.services.OnDetectBpmByNamesUpdate;

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

        copyFiles(app);


        AsyncBuildLibraryTask taskBuildLib = new AsyncBuildLibraryTask(app, true, Environment.getExternalStorageDirectory().getPath() + "/" + TestSuite.MUSIC_FOLDER + "/");
        final CountDownLatch latch = new CountDownLatch(2);
        taskBuildLib.setOnBuildLibraryProgressUpdate(new AsyncBuildLibraryTask.OnBuildLibraryProgressUpdate()
        {
            @Override
            public void onStartBuildingLibrary(AsyncBuildLibraryTask task)
            {
            }

            @Override
            public void onProgressUpdate(AsyncBuildLibraryTask task, int overallProgress, int maxProgress, boolean mediaStoreTransferDone)
            {
            }

            @Override
            public void onFinishBuildingLibrary(AsyncBuildLibraryTask task)
            {
                latch.countDown();
            }
        });
        taskBuildLib.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

        AsyncDetectBpmByNamesTask taskDetectBpmByNames = new AsyncDetectBpmByNamesTask(app);
        taskDetectBpmByNames.setOnBuildLibraryProgressUpdate(new OnDetectBpmByNamesUpdate()
        {
            @Override
            public void onStartBuildingLibrary(AsyncDetectBpmByNamesTask task)
            {
            }

            @Override
            public void onProgressUpdate(AsyncDetectBpmByNamesTask task, int overallProgress, int maxProgress, boolean mediaStoreTransferDone)
            {
            }

            @Override
            public void onFinishBuildingLibrary(AsyncDetectBpmByNamesTask task)
            {
                latch.countDown();
            }
        });
        taskDetectBpmByNames.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

        latch.await();

        while (app.getPlaylists().size() > 1)
            app.removePlaylist(1);

        Assert.assertTrue(true);
    }

    private void copyFiles(Context context) throws Exception
    {
        String sdCardPath = Environment.getExternalStorageDirectory().getPath() + "/" + TestSuite.MUSIC_FOLDER;
        File dirs = new File(sdCardPath);
        dirs.mkdirs();

        copyRAW(R.raw.audio220, sdCardPath + "/" + SONG1, context);
        copyRAW(R.raw.audio440, sdCardPath + "/" + SONG2, context);
        copyRAW(R.raw.audio880, sdCardPath + "/" + SONG3, context);

        Assert.assertTrue(true);
    }

    private void copyRAW(int fromId, String toPath, Context context) throws IOException
    {
        InputStream in = context.getResources().openRawResource(fromId);
        FileOutputStream out = new FileOutputStream(toPath);
        byte[] buff = new byte[1024];
        int read = 0;
        try
        {
            while ((read = in.read(buff)) > 0)
            {
                out.write(buff, 0, read);
            }

            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.DATA, toPath);
            values.put(MediaStore.Audio.Media.IS_MUSIC, true);
            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3");
            context.getContentResolver().insert(MediaStore.Audio.Media.getContentUriForPath(toPath), values);
        }
        finally
        {
            in.close();
            out.close();
        }


    }

}
