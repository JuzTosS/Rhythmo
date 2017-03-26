package com.juztoss.rhythmo.services;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.juztoss.rhythmo.models.DatabaseHelper;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by JuzTosS on 5/27/2016.
 */

public class AsyncBuildLibraryTask extends AsyncTask<String, String, Boolean>
{
    private final boolean DEBUG = false;

    private final String PART_LOADED = "PartLoaded";
    private final String ERROR_OCCURRED = "ErrorOccurred";
    private RhythmoApp mApp;
    @Nullable
    private String mFolder;
    private ArrayList<OnBuildLibraryProgressUpdate> mBuildLibraryProgressUpdate;

    public AsyncBuildLibraryTask(RhythmoApp app, @Nullable String folder)
    {
        mApp = app;
        mFolder = folder;
        mBuildLibraryProgressUpdate = new ArrayList<>();
    }

    public AsyncBuildLibraryTask(RhythmoApp app)
    {
        this(app, null);
    }

    public interface OnBuildLibraryProgressUpdate
    {
        void onPartLoaded();

        void onFinish(boolean wasDatabaseChanged);
    }

    private void updateMediaStore()
    {
        final CountDownLatch latch = new CountDownLatch(1);
        String sdCardPath = Environment.getExternalStorageDirectory().getPath();
        MediaScannerConnection.scanFile(mApp, new String[]{sdCardPath}, null,
                new MediaScannerConnection.OnScanCompletedListener()
                {

                    @Override
                    public void onScanCompleted(String path, Uri uri)
                    {
                        if(DEBUG) Log.d(AsyncBuildLibraryTask.class.toString(), "onScanCompleted: " + path + ", " + (uri == null ? "null" : uri));
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

    @Override
    protected Boolean doInBackground(String... params)
    {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        mApp.getLibraryHelper().begin();

        boolean songsWasRemoved = false;
        boolean hasMediaStoreChanges = false;
        boolean hasMediaFileSystemChanges = false;
        try
        {
            hasMediaStoreChanges = saveMediaStoreDataToDB();
            if(mFolder == null)
                hasMediaFileSystemChanges = saveFileSystemSongsToDB();

            if(hasMediaFileSystemChanges)
                publishProgress(PART_LOADED);
        }
        finally
        {
            songsWasRemoved = mApp.getLibraryHelper().end();
        }
        if(DEBUG) Log.d(AsyncBuildLibraryTask.class.toString(), "Finished updating the library.");

        return hasMediaStoreChanges || hasMediaFileSystemChanges || songsWasRemoved;
    }

    private boolean saveFileSystemSongsToDB()
    {
        List<File> lists = StorageUtils.getStorageList();

        if (DEBUG) Log.d(AsyncBuildLibraryTask.class.toString(), "Start updating the library, partitions in a storage: " + lists.size());

        boolean hasNewFiles = false;
        for (File dir : lists)
        {
            boolean dirHasNewFiles = scanDirectory(dir, false);
            hasNewFiles = hasNewFiles || dirHasNewFiles;
        }

        return hasNewFiles;
    }

    private boolean scanDirectory(File directory, boolean parentIsHidden)
    {
        if (directory == null) return false;

        boolean hasNewFiles = false;
        boolean childHasNewFiles = false;
        File[] listFiles = directory.listFiles();
        boolean isHidden = directory.isHidden();
        if (listFiles != null && listFiles.length > 0)
        {
            for (File file : listFiles)
            {
                if (file.isDirectory())
                {
                    childHasNewFiles = scanDirectory(file, isHidden || parentIsHidden);
                }
                else if (isHidden || parentIsHidden)
                {
                    if (mApp.getLibraryHelper().isSong(file))
                    {
                        LibraryHelper.Result result = mApp.getLibraryHelper().addSong(file);
                        hasNewFiles = result == LibraryHelper.Result.ADDED;
                    }
                }

            }
        }

        return hasNewFiles || childHasNewFiles;
    }

    private Cursor getSongsFromMediaStore()
    {
        String projection[] = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED};

        ContentResolver contentResolver = mApp.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";

        if (mFolder != null)
            selection += " AND " + MediaStore.Audio.Media.DATA + " LIKE " + DatabaseUtils.sqlEscapeString(mFolder + "%");


        return contentResolver.query(uri, projection, selection, null, null);
    }

    private boolean saveMediaStoreDataToDB()
    {
        Cursor mediaStoreCursor = getSongsFromMediaStore();
        try
        {
            if(DEBUG) Log.d(AsyncBuildLibraryTask.class.toString(), "Start updating the library, songs in mediaStore: " + mediaStoreCursor.getCount());
            return updateFromMediaStore(mediaStoreCursor);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            publishProgress(ERROR_OCCURRED);
        }
        finally
        {
            mediaStoreCursor.close();
        }

        return false;
    }

    private boolean updateFromMediaStore(Cursor cursor)
    {
        MediaCursor mediaStoreCursor = new MediaCursor(cursor);
        int songsUpdated = 0;
        int songsAdded = 0;
        for (int i = 0; i < mediaStoreCursor.getCount(); i++)
        {
            mediaStoreCursor.moveToPosition(i);
            LibraryHelper.Result result = mApp.getLibraryHelper().addSong(mediaStoreCursor);
            if (result == LibraryHelper.Result.ADDED)
                songsAdded++;
            else if (result == LibraryHelper.Result.UPDATED)
                songsUpdated++;
            else if (result == LibraryHelper.Result.ERROR)
                publishProgress(ERROR_OCCURRED);
        }

        if (DEBUG) Log.d(AsyncBuildLibraryTask.class.toString(), "Songs was added/updated: " + songsAdded + "/" + songsUpdated);

        long finalSongsCount = DatabaseUtils.queryNumEntries(mApp.getDatabaseHelper().getWritableDatabase(), DatabaseHelper.TABLE_MUSIC_LIBRARY);
        if (DEBUG) Log.d(AsyncBuildLibraryTask.class.toString(), "Final songs count: " + finalSongsCount);

        return songsAdded > 0;
    }

    @Override
    protected void onProgressUpdate(String... progressParams)
    {
        if (progressParams.length > 0 && progressParams[0].equals(ERROR_OCCURRED))
        {
            Toast.makeText(mApp, "Error occurred while updating the database!", Toast.LENGTH_LONG).show();
        }
        else if(progressParams.length > 0 && progressParams[0].equals(PART_LOADED))
        {
            if (mBuildLibraryProgressUpdate != null)
                for (int i = 0; i < mBuildLibraryProgressUpdate.size(); i++)
                    if (mBuildLibraryProgressUpdate.get(i) != null)
                        mBuildLibraryProgressUpdate.get(i).onPartLoaded();
        }
    }

    @Override
    protected void onPostExecute(Boolean wasDatabaseChanged)
    {
        if (mBuildLibraryProgressUpdate != null)
            for (int i = 0; i < mBuildLibraryProgressUpdate.size(); i++)
                if (mBuildLibraryProgressUpdate.get(i) != null)
                    mBuildLibraryProgressUpdate.get(i).onFinish(wasDatabaseChanged);

    }

    /**
     * Setter methods.
     */
    public void setOnBuildLibraryProgressUpdate(OnBuildLibraryProgressUpdate
                                                        buildLibraryProgressUpdate)
    {
        if (buildLibraryProgressUpdate != null)
            mBuildLibraryProgressUpdate.add(buildLibraryProgressUpdate);
    }
}
