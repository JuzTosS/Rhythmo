package com.juztoss.rhythmo;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.services.AsyncBuildLibraryTask;
import com.juztoss.rhythmo.services.AsyncDetectBpmByNamesTask;
import com.juztoss.rhythmo.services.OnDetectBpmByNamesUpdate;

import junit.framework.Assert;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isSelected;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.juztoss.rhythmo.utils.SystemHelper.SEPARATOR;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by JuzTosS on 12/19/2016.
 */

public class TestHelper
{

    public static final String MUSIC_FOLDER = "RhythmoTestTemp";
    public static final String MUSIC_FOLDER_NESTED = "nestedDir";
    public static final String MUSIC_FOLDER_NESTED_FULL = MUSIC_FOLDER + SEPARATOR + MUSIC_FOLDER_NESTED;

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

        onView(allOf(withId(R.id.play_button), isDisplayed())).check(matches(!isPlayButtonActive ? not(isSelected()) : isSelected()));

        //TODO: Write play icon checks
//        if (playingSongIndex >= 0)
//        {
//            onView(allOf(withId(R.id.listView), isDisplayed())).check(matches(atPosition(playingSongIndex, hasDescendant(allOf(withId(R.id.playing_state), isDisplayed())))));
//        }
//        else
//        {
//            if(songsInList >= 1)
//                onView(allOf(withId(R.id.listView), isDisplayed())).check(matches(atPosition(0, allOf(hasDescendant(allOf(withId(R.id.playing_state), not(isDisplayed())))))));
//            if(songsInList >= 2)
//                onView(allOf(withId(R.id.listView), isDisplayed())).check(matches(atPosition(1, allOf(hasDescendant(allOf(withId(R.id.playing_state), not(isDisplayed())))))));
//            if(songsInList >= 3)
//                onView(allOf(withId(R.id.listView), isDisplayed())).check(matches(atPosition(2, allOf(hasDescendant(allOf(withId(R.id.playing_state), not(isDisplayed())))))));
//        }
    }

    /**
     *
     * @param activity if not null playlists representations will be updated
     */
    public static void updateLibrary(@Nullable final Activity activity)
    {
        updateMediaStore();

        final CountDownLatch latch = new CountDownLatch(2);
        final RhythmoApp app = (RhythmoApp) InstrumentationRegistry.getTargetContext().getApplicationContext();
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
                if(activity != null)
                {
                    activity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            app.notifyPlaylistsRepresentationUpdated();
                            latch.countDown();
                        }
                    });
                }
                else
                {
                    latch.countDown();
                }
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

    private static void updateMediaStore()
    {
        final CountDownLatch latch = new CountDownLatch(1);
        RhythmoApp context = (RhythmoApp) InstrumentationRegistry.getTargetContext().getApplicationContext();
        String sdCardPath = Environment.getExternalStorageDirectory().getPath() + "/" + MUSIC_FOLDER;
        MediaScannerConnection.scanFile(context, new String[]{sdCardPath}, null,
                new MediaScannerConnection.OnScanCompletedListener()
                {
                    @Override
                    public void onScanCompleted(String path, Uri uri)
                    {
                        latch.countDown();
                    }
                });

        try
        {
            latch.await();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static final int AUDIO_FILES_COUNT = 50;

    public static void copyFiles() throws Exception
    {
        RhythmoApp context = (RhythmoApp) InstrumentationRegistry.getTargetContext().getApplicationContext();
        String sdCardPath = Environment.getExternalStorageDirectory().getPath() + "/" + MUSIC_FOLDER;
        File dirs = new File(sdCardPath);
        dirs.mkdirs();

        for (int i = 0; i < AUDIO_FILES_COUNT / 2; i++)
        {
            int songResource;
            if (i % 3 == 0)
                songResource = R.raw.audio220;
            else if (i % 3 == 1)
                songResource = R.raw.audio440;
            else
                songResource = R.raw.audio880;

            copyRAW(songResource, sdCardPath + "/" + getSongName(i), context);
        }

        sdCardPath = Environment.getExternalStorageDirectory().getPath() + "/" + MUSIC_FOLDER_NESTED_FULL;
        dirs = new File(sdCardPath);
        dirs.mkdirs();

        for (int i = AUDIO_FILES_COUNT / 2; i < AUDIO_FILES_COUNT; i++)
        {
            int songResource;
            if (i % 3 == 0)
                songResource = R.raw.audio220;
            else if (i % 3 == 1)
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
        int read;
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
        if (songNumber == 0)
            prefix = "80 - ";
        else if (songNumber == 1)
            prefix = "140 - ";
        else if (songNumber == 2)
            prefix = "160 - ";
        else
            prefix = "180 - ";

        return prefix + "audio" + Integer.toString(songNumber);
    }

    public static ViewAction clickChildViewWithId(final int id)
    {
        return new ViewAction()
        {
            @Override
            public Matcher<View> getConstraints()
            {
                return null;
            }

            @Override
            public String getDescription()
            {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view)
            {
                View v = view.findViewById(id);
                v.performClick();

            }
        };
    }

    public static RecyclerViewMatcher withRecyclerView(final int recyclerViewId)
    {

        return new RecyclerViewMatcher(recyclerViewId);
    }

    public static class RecyclerViewMatcher
    {

        private final int mRecyclerViewId;

        public RecyclerViewMatcher(int recyclerViewId)
        {
            this.mRecyclerViewId = recyclerViewId;
        }

        public Matcher<View> atPosition(final int position)
        {
            return atPositionOnView(position, -1);
        }

        public Matcher<View> atPositionOnView(final int position, final int targetViewId)
        {

            return new TypeSafeMatcher<View>()
            {
                Resources mResources;
                View mChildView;

                public void describeTo(Description description)
                {
                    String idDescription = Integer.toString(mRecyclerViewId);
                    if (mResources != null)
                    {
                        try
                        {
                            idDescription = this.mResources.getResourceName(mRecyclerViewId);
                        }
                        catch (Resources.NotFoundException var4)
                        {
                            idDescription = String.format("%s (resource name not found)", mRecyclerViewId);
                        }
                    }

                    description.appendText("RecyclerView with id: " + idDescription + " at position: " + position);
                }

                public boolean matchesSafely(View view)
                {

                    mResources = view.getResources();
                    if (mChildView == null)
                    {
                        RecyclerView recyclerView =
                                (RecyclerView) view.getRootView().findViewById(mRecyclerViewId);
                        if (recyclerView != null && recyclerView.getId() == mRecyclerViewId)
                        {
                            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                            if (viewHolder != null)
                            {
                                mChildView = viewHolder.itemView;
                            }
                        }
                        else
                        {
                            return false;
                        }
                    }

                    if (targetViewId == -1)
                    {
                        return view == mChildView;
                    }
                    else
                    {
                        View targetView = mChildView.findViewById(targetViewId);
                        return view == targetView;
                    }
                }
            };
        }
    }
}