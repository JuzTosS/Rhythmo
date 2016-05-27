package com.juztoss.bpmplayer.services;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.provider.MediaStore;

import com.juztoss.bpmplayer.DatabaseHelper;
import com.juztoss.bpmplayer.R;
import com.juztoss.bpmplayer.presenters.BPMPlayerApp;

import java.util.ArrayList;
/**
 * Created by JuzTosS on 5/27/2016.
 */

public class AsyncBuildLibraryTask extends AsyncTask<String, String, Void>
{
    private Context mContext;
    private BPMPlayerApp mApp;
    public ArrayList<OnBuildLibraryProgressUpdate> mBuildLibraryProgressUpdate;

    private int mOverallProgress = 0;

    private PowerManager.WakeLock wakeLock;

    public AsyncBuildLibraryTask(Context context)
    {
        mContext = context;
        mApp = (BPMPlayerApp) mContext;
        mBuildLibraryProgressUpdate = new ArrayList<>();
    }

    /**
     * Provides callback methods that expose this
     * AsyncTask's progress.
     *
     * @author Saravan Pantham
     */
    public interface OnBuildLibraryProgressUpdate
    {

        /**
         * Called when this AsyncTask begins executing
         * its doInBackground() method.
         */
        void onStartBuildingLibrary();

        /**
         * Called whenever mOverall Progress has been updated.
         */
        void onProgressUpdate(AsyncBuildLibraryTask task,
                                     int overallProgress, int maxProgress,
                                     boolean mediaStoreTransferDone);

        /**
         * Called when this AsyncTask finishes executing
         * its onPostExecute() method.
         */
        void onFinishBuildingLibrary(AsyncBuildLibraryTask task);

    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();

        mApp.setIsBuildingLibrary(true);
        mApp.setIsScanFinished(false);

        if (mBuildLibraryProgressUpdate != null)
            for (int i = 0; i < mBuildLibraryProgressUpdate.size(); i++)
                if (mBuildLibraryProgressUpdate.get(i) != null)
                    mBuildLibraryProgressUpdate.get(i).onStartBuildingLibrary();

        // Acquire a wakelock to prevent the CPU from sleeping while the process is running.
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        wakeLock.acquire();

    }

    @Override
    protected Void doInBackground(String... params)
    {
        Cursor mediaStoreCursor = getSongsFromMediaStore();

        if (mediaStoreCursor != null)
        {
            saveMediaStoreDataToDB(mediaStoreCursor);
            mediaStoreCursor.close();
        }

        //Notify all listeners that the MediaStore transfer is complete.
        publishProgress(new String[]{"MEDIASTORE_TRANSFER_COMPLETE"});

        return null;
    }

    /**
     * Retrieves a cursor of songs from MediaStore. The cursor
     * is limited to songs that are within the folders that the user
     * selected.
     */
    private Cursor getSongsFromMediaStore()
    {
        //Get a cursor of all active music folders.
//        Cursor musicFoldersCursor = mApp.getDBAccessHelper().getAllMusicFolderPaths();

        //Build the appropriate selection statement.
        Cursor mediaStoreCursor = null;
        String sortOrder = null;
        String projection[] = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA};

//        //Grab the cursor of MediaStore entries.
//        if (musicFoldersCursor == null || musicFoldersCursor.getCount() < 1)
//        {
            //No folders were selected by the user. Grab all songs in MediaStore.
//            mediaStoreCursor = MediaStoreAccessHelper.getAllSongs(mContext, projection, sortOrder);
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";

        mediaStoreCursor = contentResolver.query(uri, projection, selection, null, sortOrder);
//        }
//        else
//        {
//            mediaStoreCursor = MediaStoreAccessHelper.getAllSongsWithSelection(mContext,
//                    buildMusicFoldersSelection(musicFoldersCursor),
//                    projection,
//                    sortOrder);
//
//            //Close the music folders cursor.
//            musicFoldersCursor.close();
//        }

        return mediaStoreCursor;
    }

    /**
     * Iterates through mediaStoreCursor and transfers its data
     * over to Jams' private database.
     */
    private void saveMediaStoreDataToDB(Cursor mediaStoreCursor)
    {
        try
        {
            //Initialize the database transaction manually (improves performance).
            DatabaseHelper.db().beginTransaction();

            //Clear out the table.
            DatabaseHelper.db()
                    .delete(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                            null,
                            null);

            //Tracks the progress of this method.
            int subProgress = 0;
            if (mediaStoreCursor.getCount() != 0)
            {
                subProgress = 250000 / (mediaStoreCursor.getCount());
            }
            else
            {
                subProgress = 250000 / 1;
            }


            //Prefetch each column's index.
            final int filePathColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            final int idColIndex = mediaStoreCursor.getColumnIndex(MediaStore.Audio.Media._ID);

            for (int i = 0; i < mediaStoreCursor.getCount(); i++)
            {
                mediaStoreCursor.moveToPosition(i);
                mOverallProgress += subProgress;
                publishProgress();

                String songFilePath = mediaStoreCursor.getString(filePathColIndex);
                String songId = mediaStoreCursor.getString(idColIndex);

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.MUSIC_LIBRARY_PATH, songFilePath);
                values.put(DatabaseHelper.MUSIC_LIBRARY_MEDIA_ID, songId);

                //Add all the entries to the database to build the songs library.
                DatabaseHelper.db().insert(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                        null,
                        values);


            }

        }
        catch (SQLException e)
        {
            // TODO Auto-generated method stub.
            e.printStackTrace();
        }
        finally
        {
            //Close the transaction.
            DatabaseHelper.db().setTransactionSuccessful();
            DatabaseHelper.db().endTransaction();
        }

    }
//
//    /**
//     * Constructs the selection string for limiting the MediaStore
//     * query to specific music folders.
//     */
//    private String buildMusicFoldersSelection(Cursor musicFoldersCursor)
//    {
//        String mediaStoreSelection = MediaStore.Audio.Media.IS_MUSIC + "!=0 AND (";
//        int folderPathColIndex = musicFoldersCursor.getColumnIndex(DBAccessHelper.FOLDER_PATH);
//        int includeColIndex = musicFoldersCursor.getColumnIndex(DBAccessHelper.INCLUDE);
//
//        for (int i = 0; i < musicFoldersCursor.getCount(); i++)
//        {
//            musicFoldersCursor.moveToPosition(i);
//            boolean include = musicFoldersCursor.getInt(includeColIndex) > 0;
//
//            //Set the correct LIKE clause.
//            String likeClause;
//            if (include)
//                likeClause = " LIKE ";
//            else
//                likeClause = " NOT LIKE ";
//
//            //The first " AND " clause was already appended to mediaStoreSelection.
//            if (i != 0 && !include)
//                mediaStoreSelection += " AND ";
//            else if (i != 0 && include)
//                mediaStoreSelection += " OR ";
//
//            mediaStoreSelection += MediaStore.Audio.Media.DATA + likeClause
//                    + "'%" + musicFoldersCursor.getString(folderPathColIndex)
//                    + "/%'";
//
//        }
//
//        //Append the closing parentheses.
//        mediaStoreSelection += ")";
//        return mediaStoreSelection;
//    }

    @Override
    protected void onProgressUpdate(String... progressParams)
    {
        super.onProgressUpdate(progressParams);

        if (progressParams.length > 0 && progressParams[0].equals("MEDIASTORE_TRANSFER_COMPLETE"))
        {
            for (int i = 0; i < mBuildLibraryProgressUpdate.size(); i++)
                if (mBuildLibraryProgressUpdate.get(i) != null)
                    mBuildLibraryProgressUpdate.get(i).onProgressUpdate(this, mOverallProgress,
                            1000000, true);

            return;
        }

        if (mBuildLibraryProgressUpdate != null)
            for (int i = 0; i < mBuildLibraryProgressUpdate.size(); i++)
                if (mBuildLibraryProgressUpdate.get(i) != null)
                    mBuildLibraryProgressUpdate.get(i).onProgressUpdate(this, mOverallProgress, 1000000, false);

    }

    @Override
    protected void onPostExecute(Void arg0)
    {
        //Release the wakelock.
        wakeLock.release();
        mApp.setIsBuildingLibrary(false);
        mApp.setIsScanFinished(true);

        if (mBuildLibraryProgressUpdate != null)
            for (int i = 0; i < mBuildLibraryProgressUpdate.size(); i++)
                if (mBuildLibraryProgressUpdate.get(i) != null)
                    mBuildLibraryProgressUpdate.get(i).onFinishBuildingLibrary(this);

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
