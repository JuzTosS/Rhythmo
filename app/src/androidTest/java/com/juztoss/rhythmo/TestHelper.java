package com.juztoss.rhythmo;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.services.AsyncBuildLibraryTask;
import com.juztoss.rhythmo.services.AsyncDetectBpmByNamesTask;
import com.juztoss.rhythmo.services.OnDetectBpmByNamesUpdate;

import junit.framework.Assert;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isSelected;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by JuzTosS on 12/19/2016.
 */

public class TestHelper
{

    public static final String MUSIC_FOLDER = "RhythmoTestTemp";

    public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher)
    {
        checkNotNull(itemMatcher);
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class)
        {
            @Override
            public void describeTo(Description description)
            {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view)
            {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                if (viewHolder == null)
                {
                    // has no item on such position
                    return false;
                }
                return itemMatcher.matches(viewHolder.itemView);
            }
        };
    }

    public static void checkScreen(int songsInList, String headerTitle, String headerDescription, String headerBpm, int playingSongIndex, boolean isPlayButtonActive)
    {
        onView(allOf(withId(R.id.listView), isDisplayed())).check(new RecyclerViewItemCountAssertion(songsInList + 1));//+1 because we have last empty element

        if (headerBpm.length() > 0)
        {
            onView(allOf(withId(R.id.bpm_header_label))).check(matches(withText(headerBpm)));
            onView(allOf(withId(R.id.bpm_header_label))).check(matches(isDisplayed()));
        }

        if (headerTitle.length() > 0)
        {
            onView(allOf(withId(R.id.first_header_line))).check(matches(withText(headerTitle)));
            onView(allOf(withId(R.id.first_header_line))).check(matches(isDisplayed()));
        }

        if (headerDescription.length() > 0)
        {
            onView(allOf(withId(R.id.second_header_line))).check(matches(withText(headerDescription)));
            onView(allOf(withId(R.id.second_header_line))).check(matches(isDisplayed()));
        }

        if (playingSongIndex >= 0)
        {
            onView(allOf(withId(R.id.play_button), isDisplayed())).check(matches(!isPlayButtonActive ? not(isSelected()) : isSelected()));
            onView(allOf(withId(R.id.listView), isDisplayed())).check(matches(atPosition(playingSongIndex, allOf(hasDescendant(allOf(withId(R.id.playing_state), isDisplayed()))))));
        }
        else
        {
//            if(songsInList >= 1)
//                onView(allOf(withId(R.id.listView), isDisplayed())).check(matches(atPosition(0, allOf(hasDescendant(allOf(withId(R.id.playing_state), not(isDisplayed())))))));
//            if(songsInList >= 2)
//                onView(allOf(withId(R.id.listView), isDisplayed())).check(matches(atPosition(1, allOf(hasDescendant(allOf(withId(R.id.playing_state), not(isDisplayed())))))));
//            if(songsInList >= 3)
//                onView(allOf(withId(R.id.listView), isDisplayed())).check(matches(atPosition(2, allOf(hasDescendant(allOf(withId(R.id.playing_state), not(isDisplayed())))))));
        }
    }

    public static void updateLibrary()
    {
        final CountDownLatch latch = new CountDownLatch(2);
        RhythmoApp app = (RhythmoApp) InstrumentationRegistry.getTargetContext().getApplicationContext();
        AsyncBuildLibraryTask taskBuildLib = new AsyncBuildLibraryTask(app, true, Environment.getExternalStorageDirectory().getPath() + "/" + MUSIC_FOLDER + "/");
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
        try
        {
            latch.await();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    public static final int AUDIO_FILES_COUNT = 50;
    public static void copyFiles() throws Exception
    {
        RhythmoApp context = (RhythmoApp) InstrumentationRegistry.getTargetContext().getApplicationContext();
        String sdCardPath = Environment.getExternalStorageDirectory().getPath() + "/" + MUSIC_FOLDER;
        File dirs = new File(sdCardPath);
        dirs.mkdirs();

        for(int i = 0; i < AUDIO_FILES_COUNT; i++)
        {
            int songResource;
            if(i % 3 == 0)
                songResource = R.raw.audio220;
            else if(i % 3 == 1)
                songResource = R.raw.audio440;
            else
                songResource = R.raw.audio880;

            copyRAW(songResource, sdCardPath + "/" + getSongName(i), context);
        }
    }

    private static void copyRAW(int fromId, String toPath, Context context) throws IOException
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

    public static class RecyclerViewItemCountAssertion implements ViewAssertion
    {
        private final int expectedCount;

        public RecyclerViewItemCountAssertion(int expectedCount)
        {
            this.expectedCount = expectedCount;
        }

        @Override
        public void check(View view, NoMatchingViewException noViewFoundException)
        {
            if (noViewFoundException != null)
            {
                throw noViewFoundException;
            }

            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            assertThat(adapter.getItemCount(), is(expectedCount));
        }
    }


    /**
     * Return song name for the given song number value
     *
     * @param songNumber - a song number
     */
    public static String getSongName(int songNumber)
    {
        String prefix;
        if(songNumber == 0)
            prefix = "120 - ";
        else if(songNumber == 1)
            prefix = "140 - ";
        else if(songNumber == 2)
            prefix = "160 - ";
        else
            prefix = "180 - ";

        return prefix + " audio" + Integer.toString(songNumber);
    }
}
