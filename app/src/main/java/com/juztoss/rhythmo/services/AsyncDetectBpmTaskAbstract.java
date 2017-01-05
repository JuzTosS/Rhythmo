package com.juztoss.rhythmo.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.juztoss.rhythmo.models.DatabaseHelper;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.utils.SystemHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuzTosS on 6/26/2016.
 */
public abstract class AsyncDetectBpmTaskAbstract<T extends AsyncDetectBpmTaskAbstract.OnDetectBpmUpdate> extends AsyncTask<String, String, Void>
{
    protected final String UPDATE_COMPLETE = "UpdateComplete";
    protected RhythmoApp mApp;
    int mMaxProgressValue;
    int mOverallProgress;
    int mPlaylistIndex;
    boolean mResetBpm;
    public List<T> mBuildLibraryProgressUpdate;


    private PowerManager.WakeLock mWakeLock;

    public interface OnDetectBpmUpdate<X extends AsyncDetectBpmTaskAbstract>
    {
        void onStartBuildingLibrary(X task);

        void onProgressUpdate(X task,
                              int overallProgress, int maxProgress,
                              boolean mediaStoreTransferDone);

        void onFinishBuildingLibrary(X task);

    }

    public AsyncDetectBpmTaskAbstract(RhythmoApp context, int playlistIndex, boolean resetBpm)
    {
        mResetBpm = resetBpm;
        mApp = context;
        mBuildLibraryProgressUpdate = new ArrayList<>();
        mPlaylistIndex = playlistIndex;
    }

    @Override
    protected void onCancelled(Void aVoid)
    {
        super.onCancelled(aVoid);
        mWakeLock.release();
    }

    @Override
    protected Void doInBackground(String... params)
    {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        detectSongsBpm();
        publishProgress(UPDATE_COMPLETE);
        return null;
    }

    abstract double detectBpm(double oldBpm, String fullPath, String name);

    private void detectSongsBpm()
    {
        Cursor songsCursor;

        if (mPlaylistIndex < 0)
        {
            songsCursor = mApp.getDatabaseHelper().getReadableDatabase().query(DatabaseHelper.TABLE_MUSIC_LIBRARY,
                new String[]{DatabaseHelper._ID, DatabaseHelper.MUSIC_LIBRARY_PATH, DatabaseHelper.MUSIC_LIBRARY_NAME, DatabaseHelper.MUSIC_LIBRARY_BPMX10},
                null, null, null, null, null);
        }
        else
        {
            if(mPlaylistIndex < mApp.getPlaylists().size())
                songsCursor = mApp.getPlaylists().get(mPlaylistIndex).getCursor();
            else
            {
                Log.e(AsyncDetectBpmTaskAbstract.class.toString(), "Wrong playlist index: " + mPlaylistIndex);
                return;
            }
        }

        if (songsCursor.getCount() <= 0)
        {
            songsCursor.close();
            return;
        }

        mOverallProgress = 0;
        mMaxProgressValue = songsCursor.getCount();

        int affectedCount = 0;
        try
        {
            int idIndex = songsCursor.getColumnIndex(DatabaseHelper._ID);
            int nameIndex = songsCursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_NAME);
            int pathIndex = songsCursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_PATH);
            int bpmIndex = songsCursor.getColumnIndex(DatabaseHelper.MUSIC_LIBRARY_BPMX10);

            long lastUpdated = System.currentTimeMillis();
            for (int i = 0; i < songsCursor.getCount(); i++)
            {
                if(isCancelled())
                {
                    Log.d(AsyncDetectBpmTaskAbstract.class.toString(), " task has been cancelled");
                    return;
                }

                songsCursor.moveToPosition(i);
                String path = songsCursor.getString(pathIndex);
                String name = songsCursor.getString(nameIndex);
                String songId = songsCursor.getString(idIndex);
                String fullPath = path + SystemHelper.SEPARATOR + name;
                int hadDetectedBpm = mResetBpm ? 0 : songsCursor.getInt(bpmIndex);

                double bpm = detectBpm(hadDetectedBpm / 10.0, fullPath, name);
                if (bpm >= RhythmoApp.MAX_BPM)
                {
                    bpm = bpm / 2;
                    if (bpm >= RhythmoApp.MAX_BPM)
                    {
                        bpm = bpm / 2;
                        if (bpm >= RhythmoApp.MAX_BPM)
                        {
                            bpm = 0;//Too big value for real BPM, unset BPM for the song.
                        }
                    }
                }

                int bpmX10 = (int) (bpm * 10);
                if(!mResetBpm && bpmX10 == hadDetectedBpm)
                {
                    mOverallProgress++;
                    continue;
                }
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.MUSIC_LIBRARY_BPMX10, bpmX10);
                values.put(DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10, bpmX10);
                int rowsAffected = mApp.getDatabaseHelper().getWritableDatabase().update(DatabaseHelper.TABLE_MUSIC_LIBRARY, values, DatabaseHelper._ID + "= ?", new String[]{songId});
                if(rowsAffected > 0)
                    affectedCount++;

                mOverallProgress++;
                long now = System.currentTimeMillis();
                if (now - lastUpdated > 1000)
                {
                    lastUpdated = now;
                    publishProgress();
                }
            }
        }
        finally
        {
            songsCursor.close();
        }

        Log.d(AsyncDetectBpmTaskAbstract.class.toString(), "affectedCount=" + affectedCount);
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();


        if (mBuildLibraryProgressUpdate != null)
            for (int i = 0; i < mBuildLibraryProgressUpdate.size(); i++)
                if (mBuildLibraryProgressUpdate.get(i) != null)
                    mBuildLibraryProgressUpdate.get(i).onStartBuildingLibrary(this);

        PowerManager pm = (PowerManager) mApp.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();

    }

    @Override
    protected void onProgressUpdate(String... progressParams)
    {
        super.onProgressUpdate(progressParams);
        if (mBuildLibraryProgressUpdate != null)
            for (int i = 0; i < mBuildLibraryProgressUpdate.size(); i++)
            {
                if (mBuildLibraryProgressUpdate.get(i) != null)
                {
                    boolean done = progressParams.length > 0 && progressParams[0].equals(UPDATE_COMPLETE);
                    mBuildLibraryProgressUpdate.get(i).onProgressUpdate(this, mOverallProgress, mMaxProgressValue, done);
                }
            }
    }

    @Override
    protected void onPostExecute(Void arg0)
    {
        mWakeLock.release();

        if (mBuildLibraryProgressUpdate != null)
            for (int i = 0; i < mBuildLibraryProgressUpdate.size(); i++)
                if (mBuildLibraryProgressUpdate.get(i) != null)
                    mBuildLibraryProgressUpdate.get(i).onFinishBuildingLibrary(this);

    }

    /**
     * Setter methods.
     */
    public void setOnBuildLibraryProgressUpdate(T buildLibraryProgressUpdate)
    {
        if (buildLibraryProgressUpdate != null)
            mBuildLibraryProgressUpdate.add(buildLibraryProgressUpdate);
    }
}
