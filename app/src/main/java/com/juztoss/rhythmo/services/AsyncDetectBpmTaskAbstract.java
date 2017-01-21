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
public abstract class AsyncDetectBpmTaskAbstract extends AsyncTask<String, String, Void>
{
    private final String UPDATE_COMPLETE = "UpdateComplete";
    protected RhythmoApp mApp;
    private int mMaxProgressValue;
    private int mOverallProgress;
    private int mPlaylistIndex;
    private boolean mResetBpm;
    private List<Listener> mBuildLibraryProgressUpdate;


    private PowerManager.WakeLock mWakeLock;

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
                    mBuildLibraryProgressUpdate.get(i).onProgressUpdate(mOverallProgress, mMaxProgressValue, done);
                }
            }
    }

    /**
     *
     * @param context - a link to the RhythmoApp
     * @param playlistIndex - index of a playlist, bpm will be detected only of songs of that playlist; -1 if no needed
     * @param resetBpm - resets bpm values before detection
     */
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

    public abstract double detectBpm(double oldBpm, String fullPath, String name);

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
                if (bpm >= RhythmoApp.MAX_DETECTED_BPM)
                {
                    bpm = bpm / 2;
                    if (bpm >= RhythmoApp.MAX_DETECTED_BPM)
                    {
                        bpm = bpm / 2;
                        if (bpm >= RhythmoApp.MAX_DETECTED_BPM)
                        {
                            bpm = 0;//Too big value for real BPM, unset BPM for the song.
                        }
                    }
                }

                int bpmX10 = (int) (bpm * 10);
                if(!mResetBpm && bpmX10 == hadDetectedBpm)
                {
                    mOverallProgress++;
                    publishProgressDelayed();
                    continue;
                }
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.MUSIC_LIBRARY_BPMX10, bpmX10);
                values.put(DatabaseHelper.MUSIC_LIBRARY_BPM_SHIFTEDX10, bpmX10);
                int rowsAffected = mApp.getDatabaseHelper().getWritableDatabase().update(DatabaseHelper.TABLE_MUSIC_LIBRARY, values, DatabaseHelper._ID + "= ?", new String[]{songId});
                if(rowsAffected > 0)
                    affectedCount++;

                mOverallProgress++;

                publishProgressDelayed();
            }
        }
        finally
        {
            songsCursor.close();
        }

        Log.d(AsyncDetectBpmTaskAbstract.class.toString(), "affectedCount=" + affectedCount);
    }

    private long mLastUpdated = 0;
    protected void publishProgressDelayed()
    {
        long now = System.currentTimeMillis();
        if (now - mLastUpdated > 1000 || mLastUpdated <= 0)
        {
            mLastUpdated = now;
            publishProgress();
        }
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();

        PowerManager pm = (PowerManager) mApp.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();

    }

    @Override
    protected void onPostExecute(Void arg0)
    {
        mWakeLock.release();

        if (mBuildLibraryProgressUpdate != null)
            for (int i = 0; i < mBuildLibraryProgressUpdate.size(); i++)
                if (mBuildLibraryProgressUpdate.get(i) != null)
                    mBuildLibraryProgressUpdate.get(i).onFinish();

    }

    /**
     * Setter methods.
     */
    public void setOnBuildLibraryProgressUpdate(Listener buildLibraryProgressUpdate)
    {
        if (buildLibraryProgressUpdate != null)
            mBuildLibraryProgressUpdate.add(buildLibraryProgressUpdate);
    }

    public static abstract class Listener
    {
        public void onProgressUpdate(int overallProgress, int maxProgress,
                              boolean mediaStoreTransferDone)
        {
        }

        public void onFinish()
        {
        }

    }
}
